/* 
 * tcpclient.c - A simple TCP client
 * usage: tcpclient <host> <port>
 * source: https://www.cs.cmu.edu/afs/cs/academic/class/15213-f99/www/class26/tcpclient.c
 */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h> 

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

    /* check command line arguments */
    if (argc != 3) {
       fprintf(stderr,"usage: %s <hostname> <port>\n", argv[0]);
       exit(0);
    }
    hostname = argv[1];
    portno = atoi(argv[2]);
    while(1){
    	//printf("Continued\n");
	    /* socket: create the socket */
	    sockfd = socket(AF_INET, SOCK_STREAM, 0);
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

	    /* get message line from the user */
	    printf("Main-Prompt> ");
	    bzero(buf, BUFSIZE);
	    fgets(buf, BUFSIZE, stdin);
	    while(strlen(buf)==1){
	    	bzero(buf,BUFSIZE);
	    	fgets(buf, BUFSIZE, stdin);
	    }
	    int buf_len = strlen(buf);
	    //printf("buf: '%s' :%d\n",buf,buf_len);
	    buf[buf_len-1]='\0';
	    buf_len = strlen(buf);
	    //printf("newbuf: '%s' :%d\n",buf,buf_len);
	    char curruser[BUFSIZE];
	    char cmdstring[BUFSIZE],cmdstring_cpy[BUFSIZE];
	    strcpy(cmdstring,buf);
	    strcpy(cmdstring_cpy,buf);
	    const char s[2] = " ";
	    char *cmd_client;

	    bzero(buf, BUFSIZE);
	    if(strcmp(cmdstring,"Listusers")==0){
	    	strcpy(buf,"LSTU");
	    	//printf("1. %s\n",buf);
	    }
	    else if(strcmp(cmdstring,"Quit")==0){
	    	strcpy(buf,"QUIT");	
	    	//printf("4. %s\n",buf);
	    }
	    else{
	    	cmd_client = strtok(cmdstring, s);
	    	if(strcmp(cmd_client,"Adduser")==0){
	    		//printf("2. %s\n",cmdstring);
	    		char* userid = (char *)malloc(buf_len);
				strcpy(userid, cmdstring_cpy+strlen(cmd_client)+1);
				//printf("UID : %s\n",userid);
				char final[BUFSIZE];
				int n = strlen("ADDU ")+strlen(userid);
				strcpy(final,"ADDU ");
				strcpy(final+5,userid);
				final[n]='\0';
				//printf("final : %s\n", final);
	    		strcpy(buf,final);	
		    }
		    else if(strcmp(cmd_client,"SetUser")==0){
		    	//printf("3. %s\n",cmdstring);
	    		char* userid = (char *)malloc(buf_len);
				strcpy(userid, cmdstring_cpy+strlen(cmd_client)+1);
				//printf("UID : %s\n",userid);
				char final[BUFSIZE];
				int n = strlen("USER ")+strlen(userid);
				strcpy(final,"USER ");
				strcpy(final+5,userid);
				final[n]='\0';
				//printf("final : %s\n", final);
	    		strcpy(curruser,userid);
	    		strcpy(buf,final);	
		    }
		    else{
		    	printf("Invalid Input : Client\n");
		    	strcpy(buf,"INVALID");
		    }
	    }

	    //printf("BUF: %s\n",buf);
	    /* send the message line to the server */
	    n = write(sockfd, buf, strlen(buf));
	    if (n < 0) 
	      error("ERROR writing to socket");
	  	//printf("Sent : %s\n",buf );

	  	if(strcmp(buf,"INVALID")==0){
	  		continue;
	  	}

	    /* print the server's reply */
	    bzero(buf, BUFSIZE);
	    n = read(sockfd, buf, BUFSIZE);
	    if (n < 0) 
	      error("ERROR reading from socket");
			printf("Response From Server : %s\n",buf);
		
			if(strcmp(buf,"Exited Successfully")==0){
				break;
			}

			if(strlen(curruser)!=0){
				char checksetuser[BUFSIZE];
				strcpy(checksetuser,"User '");
				strcat(checksetuser,curruser);
				strcat(checksetuser,"'");
				strcat(checksetuser," exists");
				//printf("chk: %s\n", checksetuser);
				if(strstr(buf,checksetuser)!=NULL){
					while(1){
							//printf("AAAAAAAAAAAAA : %s\n",curruser);
					    bzero(buf, BUFSIZE);
					    /* socket: create the socket */
					    sockfd = socket(AF_INET, SOCK_STREAM, 0);
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
					  	
					    /* get message line from the user */
					    printf("Sub-Prompt-%s> ",curruser);
					    bzero(buf, BUFSIZE);
					    //scanf("%*[^\n]%s",buf);
					    fgets(buf, BUFSIZE, stdin);
					    while(strlen(buf)==1){
					    	bzero(buf,BUFSIZE);
					    	fgets(buf, BUFSIZE, stdin);
					    }
					    buf_len = strlen(buf);
					    //printf("buf: '%s' :%d\n",buf,buf_len);
					    buf[buf_len-1]='\0';
					    buf_len = strlen(buf);
					    //printf("newbuf: '%s' :%d\n",buf,buf_len);
					    strcpy(cmdstring,buf);
					    strcpy(cmdstring_cpy,buf);
					    char * token;
					    bzero(buf, BUFSIZE);
					    if(strcmp(cmdstring,"Read")==0){
					    	strcpy(buf,"READM");
					    	//printf("1. %s\n",buf);
					    }
					    else if(strcmp(cmdstring,"Delete")==0){
					    	strcpy(buf,"DELM");	
					    	//printf("4. %s\n",buf);
					    }
					    else if(strcmp(cmdstring,"Done")==0){
					    	strcpy(buf,"DONEU");	
					    	//printf("4. %s\n",buf);
					    }
					    else{
					    	token = strtok(cmdstring, s);
					    	if(strcmp(token,"Send")==0){
					    		//printf("2. %s\n",cmdstring);
					    		char* recvid = (char *)malloc(buf_len);
					    		strcpy(recvid, cmdstring_cpy+strlen(token)+1);
					    		//printf("RID : %s\n",recvid);
					    		char msg[BUFSIZE];
								printf("Type Message: ");
								//fgets(msg,BUFSIZE,stdin);
								char ch;
								int hashcount=0,flag=0,msgindex=0;
								while(1){
									ch = getchar();
									msg[msgindex]=ch;
									if(flag==0){
										if(ch=='#'){
											hashcount =1;
											flag = 1;
										}  
									}
									else{
										if(ch=='#'){
											hashcount++;
										}
										else{
											hashcount=0;
											flag = 0;
										}
									}
									if(hashcount==3){
										msg[msgindex-2]='\0';
										break;
									}
									msgindex++;
								}
								//printf("msg: '%s' %d\n",msg,(int)strlen(msg));
								char final_set[BUFSIZE];
								int n_set = strlen("SEND ")+strlen(recvid)+strlen(msg)+1;
								strcpy(final_set,"SEND ");
								strcat(final_set,recvid);
								strcat(final_set," ");
								strcat(final_set,msg);
								final_set[n_set]='\0';
								//printf("final_set : %s\n", final_set);
					    		strcpy(buf,final_set);	
						    }
						    else{
						    	printf("Invalid Input : Client-User\n");
						    	strcpy(buf,"INVALID");
						    }
					    }

					    //printf("BUF_set: '%s'\n",buf);
					    /* send the message line to the server */
					    n = write(sockfd, buf, strlen(buf));
					    if (n < 0) 
					      error("ERROR writing to socket");

					  	if(strcmp(buf,"INVALID")==0){
					  		continue;
					  	}

					    /* print the server's reply */
					    bzero(buf, BUFSIZE);
					    n = read(sockfd, buf, BUFSIZE);
					    if (n < 0) 
					      error("ERROR reading from socket");
						printf("Response From Server : %s\n",buf);
						if(strcmp(buf,"End Current Session")==0){
							bzero(curruser,BUFSIZE);
							break;
						}
					}
				}
			}
    }
    close(sockfd);
    return 0;
}