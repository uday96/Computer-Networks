/* 
 * tcpserver.c - A simple TCP echo server 
 * usage: tcpserver <port>
 * source: https://www.cs.cmu.edu/afs/cs/academic/class/15213-f99/www/class26/tcpserver.c
 */

#include <dirent.h>
#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include <netdb.h>
#include <sys/types.h> 
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
 #include <time.h>

#define BUFSIZE 1024

#if 0
/* 
 * Structs exported from in.h
 */

/* Internet address */
struct in_addr {
  unsigned int s_addr; 
};

/* Internet style socket address */
struct sockaddr_in  {
  unsigned short int sin_family; /* Address family */
  unsigned short int sin_port;   /* Port number */
  struct in_addr sin_addr;	 /* IP address */
  unsigned char sin_zero[...];   /* Pad to size of 'struct sockaddr' */
};

/*
 * Struct exported from netdb.h
 */

/* Domain name service (DNS) host entry */
struct hostent {
  char    *h_name;        /* official name of host */
  char    **h_aliases;    /* alias list */
  int     h_addrtype;     /* host address type */
  int     h_length;       /* length of address */
  char    **h_addr_list;  /* list of addresses */
}
#endif

struct mail {
   int bytes;
   struct mail *next;
};

void  print_list(struct mail *head)
{
    struct mail *temp= (struct mail *)malloc(sizeof(struct mail));
    temp=head;
    if(temp==NULL)
    {
      printf("Empty List\n");
      return;
    }
    printf("Entries : ");
    while(temp!=NULL)
    {
      printf("%d ",temp->bytes);
      temp=temp->next;
    }
    printf("\n");
}

struct mail* append(struct mail* head,int num)
{
  struct mail *temp,*right;
  temp= (struct mail *)malloc(sizeof(struct mail));
  temp->bytes=num;
  if(head==NULL){
    //printf("null\n");
    head = temp;
    head->next =NULL;
  }
  else{
    right=head;
    while(right->next != NULL){
      right=right->next;
    }
    right->next =temp;
    right=temp;
    right->next=NULL;
  }
  //print_list(head);
  return head;
}

/*
 * error - wrapper for perror
 */
void error(char *msg) {
  perror(msg);
  exit(1);
}

void MAILSERVERcheck(){
  DIR           *d;
  d = opendir("./MAILSERVER");
  if(d){
    closedir(d);
    printf("dir : MAILSERVER already exists\n");
  }
  else{
    int dirsuccess = mkdir("./MAILSERVER",0777);
    if(dirsuccess==-1){
      error("Couldnt create MAILSERVER dir");
    }
    //printf("Creadted dir : MAILSERVER\n");
  }
  return;
}

void listfiles(char * buf)
{
  bzero(buf,BUFSIZE);
  DIR           *d;
  struct dirent *dir;
  d = opendir("./MAILSERVER");
  char liststring[BUFSIZE];
  strcpy(liststring,"");
  if (d)
  {
    while ((dir = readdir(d)) != NULL)
    {
      if (dir->d_type == DT_REG)
      {
        //printf("%s\n", dir->d_name);
        strcat(liststring," ");
        strcat(liststring,dir->d_name);        
      }
    }
    closedir(d);
    int len = strlen(liststring);
    //printf("List : '%s' %d\n",liststring,len );
    if(len==0){
      strcpy(buf,"No Users present");
    }
    else{
      strcpy(buf,liststring+1);
    }
  }
  else{
    error("Couldnt open dir MAILSERVER\n");
  }
}

struct mail* parse_mails(FILE * fp,struct mail* head,int * count)
{
  char c;
  int count_ch=0,flag=0,bytes=0;
  *count=0;
  while(1)
  {
    c = (char)fgetc(fp);
    bytes++;
    if( feof(fp) )
    {
       break;
    }
    //printf("%c", c); 
    if(flag==0){
      if(c=='#'){
        count_ch=1;
        flag = 1;
      }  
    }
    else{
      if(c=='#'){
        count_ch++;
      }
      else{
        count_ch=0;
        flag = 0;
      }
    }
    if(count_ch==3){
      *count = *count + 1;
      count_ch = 0;
      flag=0;
      //printf("Bytes: %d\n",bytes);
      head = append(head,bytes);
      bytes=0;
    }
  }
  rewind(fp);
  //printf("count : %d\n",*count);
  return head;
}

void delFileContent(char *org, int prevSize, int skipSize, int nextSize){
  FILE *fp = fopen(org,"r");
  char prevMailContent[prevSize+1],nextMailContent[nextSize+1];
  fread(prevMailContent,prevSize,1,fp);
  fseek(fp,skipSize,SEEK_CUR);
  fread(nextMailContent,nextSize,1,fp);
  prevMailContent[prevSize]='\0';
  nextMailContent[nextSize]='\0';
  fclose(fp);
  fp = fopen(org,"w");
  fprintf(fp,prevMailContent);
  fprintf(fp,nextMailContent);
  fclose(fp);
  //printf("Deleted Content Successfully!\n");
  return ;
}

int main(int argc, char **argv) {
  int parentfd; /* parent socket */
  int childfd; /* child socket */
  int portno; /* port to listen on */
  int clientlen; /* byte size of client's address */
  struct sockaddr_in serveraddr; /* server's addr */
  struct sockaddr_in clientaddr; /* client addr */
  struct hostent *hostp; /* client host info */
  char buf[BUFSIZE]; /* message buffer */
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
  parentfd = socket(AF_INET, SOCK_STREAM, 0);
  if (parentfd < 0) 
    error("ERROR opening socket");

  /* setsockopt: Handy debugging trick that lets 
   * us rerun the server immediately after we kill it; 
   * otherwise we have to wait about 20 secs. 
   * Eliminates "ERROR on binding: Address already in use" error. 
   */
  optval = 1;
  setsockopt(parentfd, SOL_SOCKET, SO_REUSEADDR, 
	     (const void *)&optval , sizeof(int));

  /*
   * build the server's Internet address
   */
  bzero((char *) &serveraddr, sizeof(serveraddr));

  /* this is an Internet address */
  serveraddr.sin_family = AF_INET;

  /* let the system figure out our IP address */
  serveraddr.sin_addr.s_addr = htonl(INADDR_ANY);

  /* this is the port we will listen on */
  serveraddr.sin_port = htons((unsigned short)portno);

  /* 
   * bind: associate the parent socket with a port 
   */
  if (bind(parentfd, (struct sockaddr *) &serveraddr, 
	   sizeof(serveraddr)) < 0) 
    error("ERROR on binding");

  /* 
   * listen: make this socket ready to accept connection requests 
   */
  if (listen(parentfd, 5) < 0) /* allow 5 requests to queue up */ 
    error("ERROR on listen");

  /* 
   * main loop: wait for a connection request, echo input line, 
   * then close connection.
   */
  clientlen = sizeof(clientaddr);
  FILE * currfile = NULL;
  char curruser[BUFSIZE];
  struct mail *head = NULL;
  struct mail *current = NULL;
  MAILSERVERcheck();
  while (1) {

    /* 
     * accept: wait for a connection request 
     */
    childfd = accept(parentfd, (struct sockaddr *) &clientaddr, &clientlen);
    if (childfd < 0) 
      error("ERROR on accept");
    
    /* 
     * gethostbyaddr: determine who sent the message 
     */
    hostp = gethostbyaddr((const char *)&clientaddr.sin_addr.s_addr, 
			  sizeof(clientaddr.sin_addr.s_addr), AF_INET);
    if (hostp == NULL)
      error("ERROR on gethostbyaddr");
    hostaddrp = inet_ntoa(clientaddr.sin_addr);
    if (hostaddrp == NULL)
      error("ERROR on inet_ntoa\n");
    printf("server established connection with %s (%s)\n", 
	   hostp->h_name, hostaddrp);
    
    /* 
     * read: read input string from the client
     */
    bzero(buf, BUFSIZE);
    n = read(childfd, buf, BUFSIZE);
    if (n < 0) 
      error("ERROR reading from socket");
    printf("server received %d bytes: %s\n", n, buf);
    
    if(strcmp(buf,"INVALID")==0){
      close(childfd);
      continue;
    }

    char cmdstring[BUFSIZE],cmdstring_cpy[BUFSIZE];
    strcpy(cmdstring,buf);
    strcpy(cmdstring_cpy,buf);
    const char s[2] = " ";
    char *cmd;

    bzero(buf, BUFSIZE);
    if(strcmp(cmdstring,"LSTU")==0){
      listfiles(buf);  
    }
    else if(strcmp(cmdstring,"QUIT")==0){
      strcpy(buf,"Exited Successfully");
    }
    else if(strcmp(cmdstring,"READM")==0){
      if(currfile==NULL || current==NULL){
        strcpy(buf,"No More Mail");
      }
      else{
        int size=current->bytes;
        //printf("Bytes: %d\n",size );
        char mail_content[size];
        fread(mail_content,size,1,currfile);
        mail_content[size-3]='\0';
        if(mail_content[0]!='\n'){
          buf[0]='\n';
          buf[1]='\n';
          strcpy(buf+2,mail_content);
        }
        else{
          buf[0]='\n';
          strcpy(buf+1,mail_content);
        }
        current = current->next;
        //printf("Mail: %s\nSize: %d\n",mail_content,(int)strlen(mail_content));
        if(current==NULL){
          fclose(currfile);
          currfile=NULL;
        }
      }
    }
    else if(strcmp(cmdstring,"DELM")==0){
      if(currfile==NULL || current==NULL){
        strcpy(buf,"No More Mail");
      }
      else{
        struct mail *otherMails,*prevLink,*nextLink;
        otherMails = head;
        int prevMails_size=0,nextMails_size=0,currSkip_size=0;
        if(current != head){
          while(otherMails->next != current){
            //printf("PrevM : %d\n",otherMails->bytes);
            prevMails_size += otherMails->bytes;
            otherMails = otherMails->next;
          }
          prevLink = otherMails;
          //printf("PrevM : %d\n",otherMails->bytes);
          prevMails_size += otherMails->bytes;          
        }
        currSkip_size = current->bytes;
        nextLink = current->next;
        otherMails = nextLink;
        while(otherMails != NULL){
          //printf("NextM: %d\n",otherMails->bytes);
          nextMails_size += otherMails->bytes;
          otherMails = otherMails->next;
        }
        if(current==head){
          if(nextLink==NULL){
            head = NULL;
            current = NULL;
          }
          else{
            head = head->next;
            current = head;
          }
        }
        else{
          if(nextLink == NULL){
            prevLink->next = NULL;
            current = NULL;
          }
          else{
            prevLink->next = nextLink;
            current = nextLink;  
          }
        }
        //printf("PrevSize : %d    SkipSize : %d   NextSize: %d\n",prevMails_size,currSkip_size,nextMails_size);

        char filename[BUFSIZE];
        char cwd[1024];
        getcwd(cwd, sizeof(cwd));
        //printf("Current working dir: %s\n", cwd);
        strcpy(filename,cwd);
        strcat(filename,"/MAILSERVER/");
        strcat(filename,curruser);
        //printf("fname: %s\n",filename);

        fclose(currfile);
        delFileContent(filename,prevMails_size,currSkip_size,nextMails_size);
        currfile = fopen(filename,"r");
        fseek(currfile,prevMails_size,SEEK_SET);
        
        strcpy(buf,"Deleted Mail Successfully");  
      }
    }
    else if(strcmp(cmdstring,"DONEU")==0){
      if(currfile != NULL){
        fclose(currfile);
      }
      currfile = NULL;
      head = NULL;
      current = NULL;
      bzero(curruser,BUFSIZE);
      strcpy(buf,"End Current Session");
    }
    else{
      cmd = strtok(cmdstring, s);
      if(strcmp(cmd,"ADDU")==0){
        char* userid = (char *)malloc(strlen(cmdstring_cpy));
        strcpy(userid, cmdstring_cpy+strlen(cmd)+1);
        //printf("UID : '%s'\n",userid);
        char filename[BUFSIZE];
        char cwd[1024];
        getcwd(cwd, sizeof(cwd));
        //printf("Current working dir: %s\n", cwd);
        strcpy(filename,cwd);
        strcat(filename,"/MAILSERVER/");
        strcat(filename,userid);
        //printf("fname: %s\n",filename);
        if( access( filename, F_OK ) != -1 ){
          // file exists
          strcpy(buf,"Userid already present");
        }
        else{
          // file doesn't exist
          FILE * file;
          file = fopen(filename, "w");
          fclose(file);
          strcpy(buf,"User Added Successfully");
        }
      }
      else if(strcmp(cmd,"USER")==0){
        char* userid = (char *)malloc(strlen(cmdstring_cpy));
        strcpy(userid, cmdstring_cpy+strlen(cmd)+1);
        //printf("UID : '%s'\n",userid); 
        char filename[BUFSIZE];
        char cwd[1024];
        getcwd(cwd, sizeof(cwd));
        //printf("Current working dir: %s\n", cwd);
        strcpy(filename,cwd);
        strcat(filename,"/MAILSERVER/");
        strcat(filename,userid);
        //printf("fname: %s\n",filename);
        if( access( filename, F_OK ) != -1 ){
          // file exists
          currfile = fopen(filename, "r");
          head = NULL;
          int totalmails=0;
          head = parse_mails(currfile,head,&totalmails);
          sprintf(buf,"User '%s' exists and has %d mails",userid,totalmails);
          strcpy(curruser,userid);
          current = head;
        }
        else{
          // file doesn't exist
          sprintf(buf,"Userid '%s' doesnt exist",userid);
        }
      }
      else if(strcmp(cmd,"SEND")==0){
        cmd = strtok(NULL,s);
        char* recvid = (char *)malloc(strlen(cmdstring_cpy));
        char* msg = (char *)malloc(strlen(cmdstring_cpy));
        strcpy(recvid,cmd);
        //printf("bufcopy: %s\n",cmdstring_cpy);
        int msg_start = strlen("SEND ")+strlen(recvid)+1;
        //cmd = strtok(NULL,s);
        strcpy(msg, cmdstring_cpy+msg_start);
        //printf("Curruser: %s\nRID : %s\nMsg : %s\n",curruser,recvid,msg); 
        char filename_recv[BUFSIZE];
        char cwd[1024];
        getcwd(cwd, sizeof(cwd));
        //printf("Current working dir: %s\n", cwd);
        strcpy(filename_recv,cwd);
        strcat(filename_recv,"/MAILSERVER/");
        strcat(filename_recv,recvid);
        //printf("recvfname: %s\n",filename_recv);
        if( access( filename_recv, F_OK ) != -1 ){
          // file exists
          FILE * recv_spool = fopen(filename_recv, "a");
          //fseek(recv_spool,0,SEEK_END);
          time_t t;
          time(&t);
          char datestring[100];
          sprintf(datestring,"%s",ctime(&t));
          int datestring_len = strlen(datestring);
          char datetimestring[datestring_len+4];
          memset(datetimestring, '\0', sizeof(datetimestring));
          strncpy(datetimestring,datestring,datestring_len-5);
          strcat(datetimestring,"IST ");
          strcat(datetimestring,datestring+(datestring_len-5));
          //printf("datetimestring : %s\n",datetimestring);
          fprintf(recv_spool,"From: %s\nTo: %s\nDate: %sMessage: \n%s\n###\n",curruser,recvid,datetimestring,msg);
          fclose(recv_spool);
          if(strcmp(curruser,recvid)==0){
            char mymsg[BUFSIZE];
            int mymsg_len = sprintf(mymsg,"From: %s\nTo: %s\nDate: %sMessage: \n%s\n###\n",curruser,recvid,datetimestring,msg);
            //printf("mymsglen : %d\n", mymsg_len);
            head = append(head,mymsg_len);
            
            int seeksize=0;
            struct mail* tmp = head;
            if(current!=NULL){
              //printf("Current NOT null\n");
              while(tmp != current){
                //printf("LLB : %d\n",tmp->bytes);
                seeksize += tmp->bytes;
                tmp = tmp->next;
              }
              current = tmp;  
            }            
            else{
              //printf("Current IS null\n");
              while(tmp->next!= NULL){
                //printf("LLB : %d\n",tmp->bytes);
                seeksize += tmp->bytes;
                tmp = tmp->next;
              }
              current = tmp;
            }
            //printf("seeksize : %d\n",seeksize);
            if(currfile!=NULL){
              fclose(currfile);
            }
            //printf("fname: %s\n",filename_recv);
            currfile = fopen(filename_recv,"r");
            fseek(currfile,seeksize,SEEK_SET);
          }
          sprintf(buf,"Mail sent to %s successfully!",recvid);
        }
        else{
          // file doesn't exist
          sprintf(buf,"Userid '%s' doesnt exist",recvid);
        }
      }
      else{
        error("Invalid Input : Server\n");
      }
    }

    /* 
     * write: echo the input string back to the client 
     */
    n = write(childfd, buf, strlen(buf));
    if (n < 0) 
      error("ERROR writing to socket");

    close(childfd);
    if(strcmp(buf,"Exited Successfully")==0){
      currfile = NULL;
      bzero(curruser,BUFSIZE);
      head = NULL;
      current = NULL;
    }
  }
  close(parentfd);
  exit(0);
}
