#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include<math.h>

// Assumes 0 <= max <= RAND_MAX
// Returns in the closed interval [0, max]
long random_at_most(long max) {
  unsigned long
    // max <= RAND_MAX < ULONG_MAX, so this is okay.
    num_bins = (unsigned long) max + 1,
    num_rand = (unsigned long) RAND_MAX + 1,
    bin_size = num_rand / num_bins,
    defect   = num_rand % num_bins;

  long x;
  do {
   x = random();
  }
  // This is carefully written not to overflow
  while (num_rand - defect <= (unsigned long)x);
	
  // Truncated division is intentional
  return x/bin_size;

}

int main(int argc, char *argv[]){
	//printf("%s %s\n",argv[1],argv[2]);
	FILE *fip = fopen( argv[1], "r" );
	FILE *fop = fopen( argv[2], "w" );
	int k = 5,msg_index=0,matrix_index=0,org_index=0;
	char msg[33],org[28],ch;
	char msg_matrix[100][33],org_matrix[100][28];
	while (ch != EOF) {
		if(msg_index==0){
			msg[msg_index]='$';
			msg_index++;
			org[org_index]='$';
			org_index++;
		}
		else if(msg_index==1 || msg_index==2 || msg_index==4 || msg_index==8 || msg_index==16 ){
			msg[msg_index]='P';
			msg_index++;
		}
		else{
			ch = getc(fip);
			if(ch=='\n'){
				msg[msg_index]='\0';
				//printf("%s\n",msg)			;
				strcpy(msg_matrix[matrix_index],msg);
				msg_index=0;
				//matrix_index++;
				org[org_index]='\0';
				//printf("org : %s\n",org);
				strcpy(org_matrix[matrix_index],org);
				org_index=0;
				matrix_index++;
			}
			else{
				msg[msg_index]=ch;
				msg_index++;		
				org[org_index]=ch;
				org_index++;		
			}
		}
        	
	}
	fclose(fip);
	//printf("MInd : %d\n",matrix_index);
	int p1[15] = {3,5,7,9,11,13,15,17,19,21,23,25,27,29,31};
	int p2[15] = {3,6,7,10,11,14,15,18,19,22,23,26,27,30,31};
	int p4[15] = {5,6,7,12,13,14,15,20,21,22,23,28,29,30,31};
	int p8[15] = {9,10,11,12,13,14,15,24,25,26,27,28,29,30,31};
	int p16[15] = {17,18,19,20,21,22,23,24,25,26,27,28,29,30,31};
	int p_ind=0,ones=0,m_ind=0;
	for(m_ind=0;m_ind<matrix_index;m_ind++){
		ones=0;
		for(p_ind=0;p_ind<15;p_ind++){
			int index = p1[p_ind];
			char ch = msg_matrix[m_ind][index];
			if(ch=='1'){
				ones++;			
			}	
		}
		if(ones%2==0){
			msg_matrix[m_ind][1] = '0';					
		}
		else{
			msg_matrix[m_ind][1] = '1';
		}
		//printf("%s\n",msg_matrix[m_ind]);
		
		ones=0;
		for(p_ind=0;p_ind<15;p_ind++){
			int index = p2[p_ind];
			char ch = msg_matrix[m_ind][index];
			if(ch=='1'){
				ones++;			
			}	
		}
		if(ones%2==0){
			msg_matrix[m_ind][2] = '0';					
		}
		else{
			msg_matrix[m_ind][2] = '1';
		}
		//printf("%s\n",msg_matrix[m_ind]);
		
		ones=0;
		for(p_ind=0;p_ind<15;p_ind++){
			int index = p4[p_ind];
			char ch = msg_matrix[m_ind][index];
			if(ch=='1'){
				ones++;			
			}	
		}
		if(ones%2==0){
			msg_matrix[m_ind][4] = '0';					
		}
		else{
			msg_matrix[m_ind][4] = '1';
		}
		//printf("%s\n",msg_matrix[m_ind]);

		ones=0;
		for(p_ind=0;p_ind<15;p_ind++){
			int index = p8[p_ind];
			char ch = msg_matrix[m_ind][index];
			if(ch=='1'){
				ones++;			
			}	
		}
		if(ones%2==0){
			msg_matrix[m_ind][8] = '0';					
		}
		else{
			msg_matrix[m_ind][8] = '1';
		}
		//printf("%s\n",msg_matrix[m_ind]);

		ones=0;
		for(p_ind=0;p_ind<15;p_ind++){
			int index = p16[p_ind];
			char ch = msg_matrix[m_ind][index];
			if(ch=='1'){
				ones++;			
			}	
		}
		if(ones%2==0){
			msg_matrix[m_ind][16] = '0';					
		}
		else{
			msg_matrix[m_ind][16] = '1';
		}
		//printf("%s\n",msg_matrix[m_ind]);
		fprintf(fop,"%s\n",msg_matrix[m_ind]+1);
	}
	fclose(fop);
	printf("\n");
	char singlep_msg[100][33];
	int i=0;
	for(i=0;i<matrix_index;i++){
		strcpy(singlep_msg[i],msg_matrix[i]);
		char s[33];
		strcpy(s,msg_matrix[i]+1);
		//printf("s : %s\n",s);
		long n = strlen(s);
		int parityindex = (int)random_at_most(n-1);
		//printf("n: %lu   PInd: %d\n",n,parityindex);
		char pch = s[parityindex];
		if(pch=='1'){
			s[parityindex]='0';
		}
		else{
			s[parityindex]='1';
		}
		//printf("s1: %s\n",s);
		char ss[33];
		ss[0]='$';
		strcpy(ss+1,s);
		ss[n+1]='\0';
		strcpy(singlep_msg[i],ss);
	}

	FILE * eop = fopen("errorcheck","w");	
	i=0,ones=0,p_ind=0;
	for(m_ind=0;m_ind<matrix_index;m_ind++){
		//printf("%s\n",singlep_msg[m_ind]);
		char c;
		int err[5],err_ind=0;
		ones=0;
		c = singlep_msg[m_ind][1];
		if(c=='1'){
			ones++;			
		}
		for(p_ind=0;p_ind<15;p_ind++){
			int index = p1[p_ind];
			char ch = singlep_msg[m_ind][index];
			if(ch=='1'){
				ones++;			
			}	
		}
		//printf("ones: %d\n",ones);
		if(ones%2==0){
			err[err_ind] = 0;					
		}
		else{
			err[err_ind] = 1;
		}
		//printf("err : %d\n",err[err_ind]);
		err_ind++;

		ones=0;
		c = singlep_msg[m_ind][2];
		if(c=='1'){
			ones++;			
		}
		for(p_ind=0;p_ind<15;p_ind++){
			int index = p2[p_ind];
			char ch = singlep_msg[m_ind][index];
			if(ch=='1'){
				ones++;			
			}	
		}
		if(ones%2==0){
			err[err_ind] = 0;					
		}
		else{
			err[err_ind] = 1;
		}
		//printf("err : %d\n",err[err_ind]);
		err_ind++;

		ones=0;
		c = singlep_msg[m_ind][4];
		if(c=='1'){
			ones++;			
		}
		for(p_ind=0;p_ind<15;p_ind++){
			int index = p4[p_ind];
			char ch = singlep_msg[m_ind][index];
			if(ch=='1'){
				ones++;			
			}	
		}
		if(ones%2==0){
			err[err_ind] = 0;					
		}
		else{
			err[err_ind] = 1;
		}
		//printf("err : %d\n",err[err_ind]);
		err_ind++;

		ones=0;
		c = singlep_msg[m_ind][8];
		if(c=='1'){
			ones++;			
		}
		for(p_ind=0;p_ind<15;p_ind++){
			int index = p8[p_ind];
			char ch = singlep_msg[m_ind][index];
			if(ch=='1'){
				ones++;			
			}	
		}
		if(ones%2==0){
			err[err_ind] = 0;					
		}
		else{
			err[err_ind] = 1;
		}
		//printf("err : %d\n",err[err_ind]);
		err_ind++;

		ones=0;
		c = singlep_msg[m_ind][16];
		if(c=='1'){
			ones++;			
		}
		for(p_ind=0;p_ind<15;p_ind++){
			int index = p16[p_ind];
			char ch = singlep_msg[m_ind][index];
			if(ch=='1'){
				ones++;			
			}	
		}
		if(ones%2==0){
			err[err_ind] = 0;					
		}
		else{
			err[err_ind] = 1;
		}
		//printf("err : %d\n",err[err_ind]);
		err_ind++;
		
		int pos = err[0]+(err[1]*2)+(err[2]*4)+(err[3]*8)+(err[4]*16);

		char ErrCheckOP[1024];
		printf("Original String : %s\nOriginal String with Parity : %s\nCorrupted String : %s\n",org_matrix[m_ind]+1,msg_matrix[m_ind]+1,singlep_msg[m_ind]+1);
		fprintf(eop,"Original String : %s\nOriginal String with Parity : %s\nCorrupted String : %s\n",org_matrix[m_ind]+1,msg_matrix[m_ind]+1,singlep_msg[m_ind]+1);
		printf("Number of Errors Introduced : 1\n");		
		fprintf(eop,"Number of Errors Introduced : 1\n");		
		if(pos==0){
			printf("No errors detected\n");		
			fprintf(eop,"No errors detected\n");		
		}
		else{
			printf("Error Location : %d\n",pos);		
			fprintf(eop,"Error Location : %d\n",pos);		
		}
		printf("\n");
		fprintf(eop,"\n");
	}
	fclose(eop);

	printf("\n");
	char dblep_msg[1000][33];
	i=0;
	int j=0;
	for(i=0;i<matrix_index;i++){
		for(j=0;j<10;j++){
			strcpy(dblep_msg[10*i+j],msg_matrix[i]);
			char s[33];
			strcpy(s,msg_matrix[i]+1);
			printf("s : %s\n",s);
			long n = strlen(s);
			int parityindex1 = (int)random_at_most(n-1);
			int parityindex2 = (int)random_at_most(n-1);
			printf("P1: %d   P2: %d\n",parityindex1,parityindex2);
			char pch = s[parityindex1];
			if(pch=='1'){
				s[parityindex1]='0';
			}
			else{
				s[parityindex1]='1';
			}
			pch = s[parityindex2];
			if(pch=='1'){
				s[parityindex2]='0';
			}
			else{
				s[parityindex2]='1';
			}
			printf("s1: %s\n",s);
			char ss[33];
			ss[0]='$';
			strcpy(ss+1,s);
			ss[n+1]='\0';
			strcpy(dblep_msg[10*i+j],ss);
		}
	}

	FILE * dop = fopen("doubleerrorcheck","w");	
	i=0,ones=0,p_ind=0;
	for(m_ind=0;m_ind<matrix_index*10;m_ind++){
		
		int pcheck[5] = {0,0,0,0,0};
		ones=0;
		for(p_ind=0;p_ind<15;p_ind++){
			int index = p1[p_ind];
			char ch = dblep_msg[m_ind][index];
			if(ch=='1'){
				ones++;			
			}	
		}
		char chk;		
		if(ones%2==0){
			chk='0';
		}
		else{
			chk='1';
		}
		char cc = dblep_msg[m_ind][1];
		if(cc!=chk){
			pcheck[0]=1;
		}			
		else{
			pcheck[0]=0;			
		}		
		//printf("%s\n",msg_matrix[m_ind]);
		
		ones=0;
		for(p_ind=0;p_ind<15;p_ind++){
			int index = p2[p_ind];
			char ch = dblep_msg[m_ind][index];
			if(ch=='1'){
				ones++;			
			}	
		}
	
		if(ones%2==0){
			chk='0';
		}
		else{
			chk='1';
		}
		cc = dblep_msg[m_ind][2];
		if(cc!=chk){
			pcheck[1]=1;
		}			
		else{
			pcheck[1]=0;			
		}		
		//printf("%s\n",msg_matrix[m_ind]);

		ones=0;
		for(p_ind=0;p_ind<15;p_ind++){
			int index = p4[p_ind];
			char ch = dblep_msg[m_ind][index];
			if(ch=='1'){
				ones++;			
			}	
		}
		
		if(ones%2==0){
			chk='0';
		}
		else{
			chk='1';
		}
		cc = dblep_msg[m_ind][4];
		if(cc!=chk){
			pcheck[2]=1;
		}			
		else{
			pcheck[2]=0;			
		}		
		//printf("%s\n",msg_matrix[m_ind]);

		ones=0;
		for(p_ind=0;p_ind<15;p_ind++){
			int index = p8[p_ind];
			char ch = dblep_msg[m_ind][index];
			if(ch=='1'){
				ones++;			
			}	
		}
		
		if(ones%2==0){
			chk='0';
		}
		else{
			chk='1';
		}
		cc = dblep_msg[m_ind][8];
		if(cc!=chk){
			pcheck[3]=1;
		}			
		else{
			pcheck[3]=0;			
		}		
		//printf("%s\n",msg_matrix[m_ind]);

		ones=0;
		for(p_ind=0;p_ind<15;p_ind++){
			int index = p16[p_ind];
			char ch = dblep_msg[m_ind][index];
			if(ch=='1'){
				ones++;			
			}	
		}
		
		if(ones%2==0){
			chk='0';
		}
		else{
			chk='1';
		}
		cc = dblep_msg[m_ind][16];
		if(cc!=chk){
			pcheck[4]=1;
		}			
		else{
			pcheck[4]=0;			
		}		
		//printf("%s\n",msg_matrix[m_ind]);
		
		printf("Original String : %s\nOriginal String with Parity : %s\nCorrupted String : %s\n",org_matrix[m_ind/10]+1,msg_matrix[m_ind/10]+1,dblep_msg[m_ind]+1);
		fprintf(dop,"Original String : %s\nOriginal String with Parity : %s\nCorrupted String : %s\n",org_matrix[m_ind/10]+1,msg_matrix[m_ind/10]+1,dblep_msg[m_ind]+1);
		printf("Number of Errors introduced : 2\nParity bits that failed : ");
		fprintf(dop,"Number of Errors introduced : 2\nParity bits that failed : ");
		int x=0,flag=0;
		for(x=0;x<5;x++){
			//printf("  c : %d  ",pcheck[x]);
			if(pcheck[x]==1){
				flag=1;
				printf(" %.f ",pow(2,x));
				fprintf(dop," %.f ",pow(2,x));			
			}
		}
		printf("\n");
		fprintf(dop,"\n");
		if(flag=0){
			printf("Error Detected : No\n");
			fprintf(dop,"Error Detected : No\n");
		}
		else{
			printf("Error Detected : Yes\n");
			fprintf(dop,"Error Detected : Yes\n");
		}		
		printf("\n");
		fprintf(dop,"\n");
	}	
	fclose(dop);
	
return 0;
}
