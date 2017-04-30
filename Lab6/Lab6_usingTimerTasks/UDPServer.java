import java.io.*;
import java.net.*;
import java.util.*;

//RECIEVER
class UDPServer
{
   public static void main(String args[]) throws Exception
   {
      int SVR_PORT=-1,MAX_PACKETS=-1,WINDOW_SIZE=-1;
      boolean DEBUG_MODE = false;
      float RANDOM_DROP_PROB=0;
      for(int i=0;i<args.length;i=i+2){
         switch (args[i]){
            case "-d":
               DEBUG_MODE = true;
               i = i-1;
               break;
            case "-p":
               SVR_PORT = Integer.parseInt(args[i+1]);
               break;
            case "-n":
               MAX_PACKETS = Integer.parseInt(args[i+1]);
               break;
            case "-e":
               RANDOM_DROP_PROB = Float.parseFloat(args[i+1]);
               break;
            case "-w":
                WINDOW_SIZE = Integer.parseInt(args[i+1]);
                break;
            default:
               error("Invalid CommandLine Args");
         }
      }
      try{
         DatagramSocket serverSocket = new DatagramSocket(SVR_PORT);
         int expectedpkt = 0,recievedpktscount = 0, ackedpktcount=0;
         while(ackedpktcount<MAX_PACKETS){
        	 byte[] receiveData = new byte[1024];
             byte[] sendData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);
            int recievenum = receiveData[0];
            int ACK = (recievenum+1)%WINDOW_SIZE;
            if(Math.random() < RANDOM_DROP_PROB){
            	echo("RECEIVED # " + recievenum+" & Dropped");
            	recievedpktscount = 0;
            }
            else{
            	recievedpktscount++;
                if(expectedpkt==recievenum){
                	InetAddress IPAddress = receivePacket.getAddress();
                    int port = receivePacket.getPort();
                    sendData[0] = (byte)ACK;
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                    serverSocket.send(sendPacket);
                    ackedpktcount++;
                    echo("RECEIVED # " + recievenum+"  Txn : "+ackedpktcount);
                    expectedpkt = ACK;
                    if(recievedpktscount==WINDOW_SIZE-1){
    	            	//echo("Complete Window Received");
    	            	recievedpktscount = 0;
    	            }            	
                }
                else{
                	echo("Packet out of Order : Expected #"+expectedpkt+" Recieved #"+recievenum);
                	recievedpktscount = 0;
                }           	
            }
            
            
         }
         System.out.println("Max packets acked "+MAX_PACKETS+" - "+ackedpktcount);
         serverSocket.close();
         System.exit(0);
      }
      catch(IOException e){
         System.err.println("IOException " + e);
     }
   }
   public static void echo(String msg)
   {
      System.out.println(msg);
   }
   public static void error(String msg)
   {
      System.out.println("ERROR : "+msg);
      System.exit(0);
   }
}
