import java.io.*;
import java.net.*;
import java.util.*;

//UDPServer
class RecieverGBN
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
         int expectedpkt = 0, ackedpktcount=0;
         while(ackedpktcount<MAX_PACKETS){
        	 byte[] receiveData = new byte[1024];
             byte[] sendData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);
            long recvtime = System.nanoTime();
            double recvtime_total = recvtime*Math.pow(10, -6);
            int recv_milli = (int)recvtime_total;
            int recv_micro = (int)((recvtime_total-recv_milli)*1000);
            int recievenum = receiveData[0];
            int ACK = (recievenum+1)%WINDOW_SIZE;
            if(Math.random() < RANDOM_DROP_PROB){
            	//echo("RECEIVED # " + recievenum+" & Dropped");
            	if(DEBUG_MODE){
            		echo("Seq  " +recievenum+" :   Time Recieved: "+recv_milli+":"+recv_micro+"  Packet Dropped: true");
            	}
            }
            else{
            	if(DEBUG_MODE){
            		echo("Seq  " +recievenum+" :   Time Recieved: "+recv_milli+":"+recv_micro+"  Packet Dropped: false");
            	}            	
                if(expectedpkt==recievenum){
                	InetAddress IPAddress = receivePacket.getAddress();
                    int port = receivePacket.getPort();
                    sendData[0] = (byte)ACK;
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                    serverSocket.send(sendPacket);
                    ackedpktcount++;
                    //echo("RECEIVED # " + recievenum+"  Txn : "+ackedpktcount);
                    expectedpkt = ACK;           	
                }
                else{
                	//echo("RECEIVED # " + recievenum+"  Expected #"+expectedpkt+" : Packet out of Order");
                }           	
            }
            
            
         }
         //System.out.println("Max packets acked "+MAX_PACKETS+" - "+ackedpktcount);
         System.out.printf("\nPacket Error Rate : %f\n\n",RANDOM_DROP_PROB);
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
