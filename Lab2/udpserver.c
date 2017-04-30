/* 
 * udpserver.c - A simple UDP echo server 
 * usage: udpserver <port>
 * source: https://www.cs.cmu.edu/afs/cs/academic/class/15213-f99/www/class26/udpserver.c
 */

#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include <netdb.h>
#include <sys/types.h> 
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <math.h>

#define BUFSIZE 1024

/*
 * error - wrapper for perror
 */
void error(char *msg) {
  perror(msg);
  exit(1);
}

float mathserver(char * cmd){
  //printf("Entered Mathserver : %s\n",cmd );
  const char s[2] = " ";
  char *token;
  /* get the first token */
  token = strtok(cmd, s);
  char op[4];
  strcpy(op,tolower(token));
  //printf( "Op: %s\n",op);
  /* walk through other tokens */
  int nums[2],i=0;
  for(i=0;i<2;i++){
    token = strtok(NULL, s);
    if(token != NULL){
      nums[i] = atoi(token);
      //printf( "Num: %d\n",nums[i]);
    }
    else{
      error("Invalid Input");
      break;
    }
  }
  float result = -999;
  if(strcmp(op,"add")==0){
    result = nums[0] + nums[1];
  }
  else if(strcmp(op,"sub")==0){
    result = nums[0] - nums[1];
  }
  else if(strcmp(op,"mul")==0){
    result = nums[0] * nums[1];
  }
  else if(strcmp(op,"div")==0){
    result = (float)nums[0] / nums[1];
  }
  else if(strcmp(op,"exp")==0){
    result = pow(nums[0],nums[1]);
  }
  else{
    error("Invalid Input");
  }
  //printf("Returning %.02f\n", result );
  return result;
}

int main(int argc, char **argv) {
  int sockfd; /* socket */
  int portno; /* port to listen on */
  int clientlen; /* byte size of client's address */
  struct sockaddr_in serveraddr; /* server's addr */
  struct sockaddr_in clientaddr; /* client addr */
  struct hostent *hostp; /* client host info */
  char buf[BUFSIZE]; /* message buf */
  char *hostaddrp; /* dotted decimal host addr string */
  int optval; /* flag value for setsockopt */
  int n; /* message byte size */

  /* 
   * check command line arguments 
   */
  if (argc != 2) {
    fprintf(stderr, "usage: %s <port>\n", argv[0]);
    exit(1);
  }
  portno = atoi(argv[1]);

  /* 
   * socket: create the parent socket 
   */
  sockfd = socket(AF_INET, SOCK_DGRAM, 0);
  if (sockfd < 0) 
    error("ERROR opening socket");

  /* setsockopt: Handy debugging trick that lets 
   * us rerun the server immediately after we kill it; 
   * otherwise we have to wait about 20 secs. 
   * Eliminates "ERROR on binding: Address already in use" error. 
   */
  optval = 1;
  setsockopt(sockfd, SOL_SOCKET, SO_REUSEADDR, 
	     (const void *)&optval , sizeof(int));

  /*
   * build the server's Internet address
   */
  bzero((char *) &serveraddr, sizeof(serveraddr));
  serveraddr.sin_family = AF_INET;
  serveraddr.sin_addr.s_addr = htonl(INADDR_ANY);
  serveraddr.sin_port = htons((unsigned short)portno);

  /* 
   * bind: associate the parent socket with a port 
   */
  if (bind(sockfd, (struct sockaddr *) &serveraddr, 
	   sizeof(serveraddr)) < 0) 
    error("ERROR on binding");

  /* 
   * main loop: wait for a datagram, then echo it
   */
  clientlen = sizeof(clientaddr);
  while (1) {

    /*
     * recvfrom: receive a UDP datagram from a client
     */
    bzero(buf, BUFSIZE);
    n = recvfrom(sockfd, buf, BUFSIZE, 0, (struct sockaddr *) &clientaddr, &clientlen);
    if (n < 0)
      error("ERROR in recvfrom");

    /* 
     * gethostbyaddr: determine who sent the datagram
     */
    hostp = gethostbyaddr((const char *)&clientaddr.sin_addr.s_addr, 
			  sizeof(clientaddr.sin_addr.s_addr), AF_INET);
    if (hostp == NULL)
      error("ERROR on gethostbyaddr");
    hostaddrp = inet_ntoa(clientaddr.sin_addr);
    if (hostaddrp == NULL)
      error("ERROR on inet_ntoa\n");
    //printf("server received datagram from %s (%s)\n",hostp->h_name, hostaddrp);
    //printf("server received %d/%d bytes: %s\n", strlen(buf), n, buf);
    printf("Recieved: %s\n",buf);

    char * cmdstring = (char *)malloc(strlen(buf)+1);
    strcpy(cmdstring,buf);
    float result = mathserver(cmdstring);
    char res[15];
    sprintf(res,"%.02f",result);
    strcpy(buf,res);
    //printf("Res:%s   Buf:%s\n", res,buf);
    /* 
     * sendto: echo the input back to the client 
     */
    n = sendto(sockfd, buf, strlen(buf), 0, (struct sockaddr *) &clientaddr, clientlen);
    if (n < 0) 
      error("ERROR in sendto");
  }
}
