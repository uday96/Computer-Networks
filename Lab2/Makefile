all:udpserver udpclient tcpserver tcpclient 

udpserver: udpserver.c
	gcc -o Mserver udpserver.c -lm

udpclient: udpclient.c
	gcc -o Mclient udpclient.c

tcpserver: tcpserver.c
	gcc -o Fserver tcpserver.c

tcpclient: tcpclient.c
	gcc -o Fclient tcpclient.c