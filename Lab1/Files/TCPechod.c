/* TCPechod.c - main, TCPechod */
/* Modified by Krishna Sivalingam, IITM */
/* Combined multiple function definitions from different files into this file */
/* Jan. 16, 2017 */

#define	_USE_BSD
#include <sys/types.h>
#include <sys/signal.h>
#include <sys/socket.h>
#include <sys/time.h>
#include <sys/resource.h>
#include <sys/wait.h>
#include <sys/errno.h>
#include <netinet/in.h>

#include <unistd.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <netdb.h>

#define	QLEN		  32	/* maximum connection queue length	*/
#define	BUFSIZE		4096

extern int	errno;

void	reaper(int);
int	TCPechod(int fd);
int	errexit(const char *format, ...);
int	passivesockTCP(const char *service, int qlen);

unsigned short	portbase = 0;	/* port base, for non-root servers	*/

/*------------------------------------------------------------------------
 * passivesockTCP - allocate & bind a server socket using TCP
 *------------------------------------------------------------------------
 */
int
passivesockTCP(const char *service, int qlen)
/*
 * Arguments:
 *      service   - service associated with the desired port
 */
{
	struct servent	*pse;	/* pointer to service information entry	*/
	struct protoent *ppe;	/* pointer to protocol information entry*/
	struct sockaddr_in sin;	/* an Internet endpoint address		*/
	int	s, type;	/* socket descriptor and socket type	*/

	memset(&sin, 0, sizeof(sin));
	sin.sin_family = AF_INET;
	sin.sin_addr.s_addr = INADDR_ANY;

	/* Map service name to port number */
	if ((pse = getservbyname(service, "tcp")) != NULL)
	  sin.sin_port = htons(ntohs((unsigned short)pse->s_port)
			       + portbase);
	else if ((sin.sin_port=htons((unsigned short)atoi(service))) == 0)
	  errexit("can't get \"%s\" service entry\n", service);
	
	/* Map protocol name to protocol number */
	if ((ppe = getprotobyname("tcp")) == NULL)
	  errexit("can't get \"%s\" bprotocol entry\n", "tcp");

	type = SOCK_STREAM;	/* For TCP */

	/* Allocate a socket */
	s = socket(PF_INET, type, ppe->p_proto);
	if (s < 0)
	  errexit("can't create socket: %s\n", strerror(errno));
	
	/* Bind the socket */
	if (bind(s, (struct sockaddr *)&sin, sizeof(sin)) < 0)
		errexit("can't bind to %s port: %s\n", service,
			strerror(errno));
	if (type == SOCK_STREAM && listen(s, qlen) < 0)
		errexit("can't listen on %s port: %s\n", service,
			strerror(errno));

	return s;
}


/*------------------------------------------------------------------------
 * main - Concurrent TCP server for ECHO service
 *------------------------------------------------------------------------
 */
int
main(int argc, char *argv[])
{
  char	*service = "echo";	/* service name or port number	*/
  struct	sockaddr_in fsin;	/* the address of a client	*/
  unsigned int	alen;		/* length of client's address	*/
  int	msock;			/* master server socket		*/
  int	ssock;			/* slave server socket		*/
  
  switch (argc) {
  case	1:
    break;
  case	2:
    service = argv[1];
    break;
  default:
    errexit("usage: TCPechod [port]\n");
  }
  
  msock = passivesockTCP(service, QLEN);
  
  (void) signal(SIGCHLD, reaper);
  
  while (1) {
    alen = sizeof(fsin);
    ssock = accept(msock, (struct sockaddr *)&fsin, &alen);
    if (ssock < 0) {
      if (errno == EINTR)
	continue;
      errexit("accept: %s\n", strerror(errno));
    }
    switch (fork()) {
    case 0:		/* child */
      (void) close(msock);
      exit(TCPechod(ssock));
    default:	/* parent */
      (void) close(ssock);
      break;
    case -1:
      errexit("fork: %s\n", strerror(errno));
    }
  }
}

/*------------------------------------------------------------------------
 * TCPechod - echo data until end of file
 *------------------------------------------------------------------------
 */
int
TCPechod(int fd)
{
	char	buf[BUFSIZ];
	int	cc;

	while ((cc = read(fd, buf, sizeof buf)) > 0) {
	  if (cc < 0)
	    errexit("echo read: %s\n", strerror(errno));
	  if (write(fd, buf, cc) < 0)
	    errexit("echo write: %s\n", strerror(errno));
	}
	return 0;
}

/*------------------------------------------------------------------------
 * reaper - clean up zombie children
 *------------------------------------------------------------------------
 */
void
reaper(int sig)
{
	int	status;

	while (wait3(&status, WNOHANG, (struct rusage *)0) >= 0)
		/* empty */;
}
