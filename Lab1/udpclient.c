/* 
 * tcpclient.c - A simple UDP client
 * usage: udpclient <host> <port>
 */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h> 
#include <time.h>

#define BUFSIZE 1024

/* 
 * error - wrapper for perror
 */
void error(char *msg) {
    perror(msg);
    exit(0);
}

int main(int argc, char **argv) {
    int sockfd, portno, n;
    struct sockaddr_in serveraddr;
    struct hostent *server;
    char *hostname;
    char buf[BUFSIZE];

    time_t	now;

    /* check command line arguments */
    if (argc != 3) {
       fprintf(stderr,"usage: %s <hostname> <port>\n", argv[0]);
       exit(0);
    }
    hostname = argv[1];
    portno = atoi(argv[2]);

    //int loopcount  = 0;
    //double responsetime_avg = 0;
    //for(loopcount = 0;loopcount<500;loopcount++)
    while(1)
    {
        /* socket: create the socket */
        sockfd = socket(PF_INET, SOCK_DGRAM, 0);
        if (sockfd < 0) 
            error("ERROR opening socket");

        /* gethostbyname: get the server's DNS entry */
        server = gethostbyname(hostname);
        if (server == NULL) {
            fprintf(stderr,"ERROR, no such host as %s\n", hostname);
            exit(0);
        }

        /* build the server's Internet address */
        bzero((char *) &serveraddr, sizeof(serveraddr));
        serveraddr.sin_family = AF_INET;
        bcopy((char *)server->h_addr, 
    	  (char *)&serveraddr.sin_addr.s_addr, server->h_length);
        serveraddr.sin_port = htons(portno);

        /* connect: create a connection with the server */
        if (connect(sockfd, &serveraddr, sizeof(serveraddr)) < 0) 
          error("ERROR connecting");

        printf("Server addr : %s , port : %d\n",hostname,portno);

        /* get message line from the user */
        printf("Please enter msg: ");
        bzero(buf, BUFSIZE);
        fgets(buf, BUFSIZE, stdin);
        //strcpy(buf,"Hi");
        //printf(" %s\n",buf );

        /* send the message line to the server */

        clock_t begin = clock();

        n = write(sockfd, buf, strlen(buf));
        if (n < 0) 
          error("ERROR writing to socket");

        /* print the server's reply */
        bzero(buf, BUFSIZE);
        n = read(sockfd, buf, BUFSIZE);

        clock_t end = clock();
        double time_spent = (double)(end - begin) / CLOCKS_PER_SEC;
        //responsetime_avg += time_spent;

        if (n < 0) 
          error("ERROR reading from socket");
        printf("Echo from server: %s", buf);
    	
        printf("Response Time : %0.f us\n",time_spent*1000000);
	}
    //responsetime_avg = responsetime_avg/500;
    //printf("\nAverage Response Time : %0.f us\n", responsetime_avg*1000000);
    close(sockfd);
    return 0;
}
