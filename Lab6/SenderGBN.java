import java.io.*;
import java.net.*;
import java.text.DecimalFormat;
import java.util.*;

//UDPClient
class SenderGBN
{
		
   public static void main(String args[]) throws Exception
   {  
      Variables vars = new Variables();
      
      for(int i=0;i<args.length;i=i+2){
         switch (args[i]){
            case "-d":
               vars.DEBUG_MODE = true;
               i = i-1;
               break;
            case "-s":
               vars.RCV_IP = args[i+1];
               break;
            case "-p":
               vars.RCV_PORT = Integer.parseInt(args[i+1]);
               break;
            case "-l":
               vars.PACKET_LENGTH = Integer.parseInt(args[i+1]);
               break;
            case "-r":
               vars.PACKET_GEN_RATE = Integer.parseInt(args[i+1]);
               break;
            case "-n":
               vars.MAX_PACKETS = Integer.parseInt(args[i+1]);
               break;
            case "-w":
               vars.WINDOW_SIZE = Integer.parseInt(args[i+1]);
               break;
            case "-b":
               vars.MAX_BUFFER_SIZE = Integer.parseInt(args[i+1]);
               break;
            default:
               error("Invalid CommandLine Args");
         }
      }
      vars.init();
      final Thread currentThread = Thread.currentThread();
      Timer genpktTimer = new Timer("PacketGenerator");
      // Schedule to run after every 1 second(1000 millisecond)
	  genpktTimer.scheduleAtFixedRate(new GeneratePktsTask(vars),0,1000);
	  //currentThread.sleep(3000);
      try{
         DatagramSocket clientSocket = new DatagramSocket();
         Listener listenthread = new Listener( "Listener Thread",clientSocket,vars,currentThread);
         listenthread.start();
         InetAddress IPAddress = InetAddress.getByName(vars.RCV_IP);
         vars.timeout = 100; //in ms
         
         vars.lastpktsent = -1;
         vars.unacked = 0;
         while(vars.totalACKed < vars.MAX_PACKETS){
        	 if(vars.initial==true && vars.sendWindow==true){
            	//echo("BUFFSIZE : "+vars.BUFFER.size());
            	if(vars.BUFFER.size()<vars.WINDOW_SIZE-1){
            		//System.out.println("Waiting for BUFFER refill");
            		currentThread.sleep(1000);
            		listenthread.sleep(1000);
            	}
            	//System.out.println("Sending new Window");
            	for(int i=0;i<vars.WINDOW_SIZE-1 && i<vars.BUFFER.size();i++){
                	byte[] sendData = new byte[vars.PACKET_LENGTH];
                	sendData = vars.BUFFER.get(i);
                	DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, vars.RCV_PORT);
                	vars.lastpktsent = sendData[0];
                	long start = System.nanoTime();
                	int arrindex =((vars.lastpktsent+1)%vars.WINDOW_SIZE);
                	if(vars.attempts[arrindex]>=5){
                		//System.out.println("Seq "+vars.lastpktsent+" :   Retransmission limit reached - "+vars.attempts[arrindex]);
                		DecimalFormat df = new DecimalFormat();
                        df.setMaximumFractionDigits(6);
                  		System.out.println("\nPacket Generation Rate : "+vars.PACKET_GEN_RATE);
                        System.out.println("Packet Length : "+vars.PACKET_LENGTH);
                        System.out.printf("Retransmission Ratio : %f\n",((float)vars.totalTransmissions/vars.totalACKed));
                        System.out.println("Average RTT : "+df.format(vars.RTT*Math.pow(10, -6))+"\n");
                        clientSocket.close();
                        System.exit(0);
                	}
                	clientSocket.send(sendPacket);
                	vars.totalTransmissions++;
                	vars.unacked++;                	
                	vars.timesarr[arrindex] = new PktTimes(vars.lastpktsent, start);
                	vars.attempts[arrindex]++;
                	//echo("Sent [#"+vars.lastpktsent+" : "+sendData.length+"]");
                }
            	vars.sendWindow = false;
            	vars.initial = false;
            }
         }
         genpktTimer.cancel();
         genpktTimer.purge();
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

class PktTimes{
	public int seqnum = -1;
	public long startTime = -1;
	public long endTime = -1;
	
	PktTimes(int seqnum, long startTime){
		this.seqnum = seqnum;
		this.startTime = startTime;
	}
}

class GeneratePktsTask extends TimerTask {
   
   Variables vars;
    
   GeneratePktsTask(Variables vars){
      this.vars = vars;
   }

   // run is a abstract method that defines task performed at scheduled time.
   public synchronized void run() {
      for(int i=0;i<vars.PACKET_GEN_RATE;i++){
         int buffsize = vars.BUFFER.size();
         if(buffsize<vars.MAX_BUFFER_SIZE){
            byte[] pkt = new byte[vars.PACKET_LENGTH];
            new Random().nextBytes(pkt);
            int nextseqnum=0,lastseqnum=-1;
            if(buffsize!=0){
               byte[] lastpkt = vars.BUFFER.get(buffsize-1);
               lastseqnum = lastpkt[0];
               nextseqnum = (lastseqnum+1)%vars.WINDOW_SIZE;
            }
            else{
            	nextseqnum = vars.pktgenstartseq;
            }
            //System.out.println("lastseq# : "+lastseqnum+" || nextseq# : "+nextseqnum);
            pkt[0] = (byte)nextseqnum;
            vars.BUFFER.add(pkt);
         }
      }
      byte[] lastpkt = vars.BUFFER.get(vars.BUFFER.size()-1);
      int lastseqnum = lastpkt[0];
      vars.pktgenstartseq = (lastseqnum+1)%vars.WINDOW_SIZE;
      //System.out.println("PKT_GENERATOR : start #"+vars.pktgenstartseq);
   }
}

class Listener extends Thread {
   private Thread t, mainThread;
   private String threadName;
   DatagramSocket clientSocket;
   private Variables vars;
   
   Listener( String name, DatagramSocket clientSocket,Variables vars, Thread mainThread) {
      this.threadName = name;
      this.clientSocket = clientSocket;
      this.vars = vars;
      this.mainThread = mainThread;
     // //System.out.println("Creating " +  threadName );
   }
      
   public synchronized void sendWINDOW(){
	   vars.sendWindow = true; 
	  	 try{
		   InetAddress IPAddress = InetAddress.getByName(vars.RCV_IP);
		   //while(vars.totalACKed < vars.MAX_PACKETS){
	        	 if(vars.sendWindow==true){
	            	//System.out.println("BUFFSIZE : "+vars.BUFFER.size());
	            	vars.unacked = 0;
	            	if(vars.BUFFER.size()<vars.WINDOW_SIZE-1){
	            		//System.out.println("T : Waiting for BUFFER refill");
	            		try{
	            			mainThread.sleep(1000);
		            		t.sleep(1000);
		            		//System.out.println("BUFFSIZE : "+vars.BUFFER.size());
	            		}
	            		catch (InterruptedException ei) {
	            			System.err.println("InterruptedException : "+ei);
	            		}
	            		
	            	}
	            	//System.out.println("Sending new Window");
	            	for(int i=0;i<vars.WINDOW_SIZE-1 && i<vars.BUFFER.size();i++){
	                	byte[] sendData = new byte[vars.PACKET_LENGTH];
	                	sendData = vars.BUFFER.get(i);
	                	DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, vars.RCV_PORT);
	                	vars.lastpktsent = sendData[0];
	                	long start = System.nanoTime();
	                	int arrindex =((vars.lastpktsent+1)%vars.WINDOW_SIZE);
	                	if(vars.attempts[arrindex]>=5){
	                		System.out.println("Seq "+vars.lastpktsent+" :  Retransmission limit reached - "+vars.attempts[arrindex]);
	                		DecimalFormat df = new DecimalFormat();
	                        df.setMaximumFractionDigits(6);
	                  		System.out.println("\nPacket Generation Rate : "+vars.PACKET_GEN_RATE);
	                        System.out.println("Packet Length : "+vars.PACKET_LENGTH);
	                        System.out.printf("Retransmission Ratio : %f\n",((float)vars.totalTransmissions/vars.totalACKed));
	                        System.out.println("Average RTT : "+df.format(vars.RTT*Math.pow(10, -6))+"\n");
	                        clientSocket.close();
	                        System.exit(0);
	                	}
	                	clientSocket.send(sendPacket);
	                	vars.totalTransmissions++;
	                	vars.unacked++;
	                	vars.timesarr[arrindex] = new PktTimes(vars.lastpktsent, start);
	                	vars.attempts[arrindex]++;
	                	//System.out.println("Sent [#"+vars.lastpktsent+" : "+sendData.length+"]");
	                }
	            	vars.sendWindow = false;
	            	
	        	 }   
		   //}
		   }
		   catch(IOException eio){
			   System.err.println("IOException : "+eio);
		   }
   }
   
   public synchronized void run() {
      //System.out.println("Running " +  threadName );
          	  try {
          		while(vars.totalACKed < vars.MAX_PACKETS){
          			try{
	          			 byte[] receiveData = new byte[vars.PACKET_LENGTH];
	    	        	 DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
	    	        	 int sockettimeout = (int)Math.ceil(vars.timeout);
	    	        	 try{
	    	        		 sockettimeout = (int)Math.ceil((vars.timeout-((System.nanoTime() - vars.timesarr[(vars.lastACK+1)%vars.WINDOW_SIZE].startTime)*Math.pow(10, -6))));
	    	        	 }
	    	        	 catch(NullPointerException enul){
	    	        		 //System.out.println("Null Ptr lastACK");
	    	        	 }
	    	        	 if(sockettimeout<=0){
	    	        		 sockettimeout = 1;
	    	        	 }	    	        	
	    	        	 clientSocket.setSoTimeout(sockettimeout);	 
	    	        	 clientSocket.receive(receivePacket);
	                	 vars.unacked--;
		                 long end = System.nanoTime();
		                 int recievenum = receiveData[0];
		                 this.vars.lastACK = recievenum;
		                 vars.RTT = ((vars.RTT*vars.totalACKed)+(end-vars.timesarr[vars.lastACK].startTime))/(vars.totalACKed+1);
		                 vars.totalACKed++;
		                 //System.out.println("ACK # " + this.vars.lastACK+"   Txns : "+vars.totalACKed);
		                 long gentime = vars.timesarr[vars.lastACK].startTime;
		                 double gentime_total = gentime*Math.pow(10, -6);
		                 int gentime_milli = (int)gentime_total;
		                 int gentime_micro = (int)((gentime_total-gentime_milli)*1000);
		                 double rtt = (end-vars.timesarr[vars.lastACK].startTime)*Math.pow(10, -6);
		                if(vars.DEBUG_MODE){
		                	DecimalFormat df = new DecimalFormat();
			                 df.setMaximumFractionDigits(6);
		                	 System.out.println("Seq "+(recievenum+vars.WINDOW_SIZE-1)%vars.WINDOW_SIZE + " :   Time Generated: "+gentime_milli+":"+gentime_micro+"  RTT: "+df.format(rtt)+"  Number Of Attempts: "+vars.attempts[vars.lastACK]);
		                 }
		                vars.attempts[vars.lastACK]=0;
		                 if(vars.totalACKed>10){
		                	 vars.timeout = (float)(vars.RTT*2*Math.pow(10, -6));
		                	 ////System.out.println("Timeout set to "+vars.timeout);
		                 }
		                 vars.BUFFER.remove(0);
		                 if(!vars.sendWindow && vars.unacked<vars.WINDOW_SIZE){
		                		byte[] sendData = new byte[vars.PACKET_LENGTH];
			                 	int ind=0;
                			 	for(int i=0;i<vars.BUFFER.size();i++){
			                 		byte[] pkt = vars.BUFFER.get(i);
			                 		if(pkt[0]==vars.lastpktsent){
			                 			ind = i+1;
			                 			break;					                 			
			                 		}
			                 	}
			                 	try{
	                 				sendData = vars.BUFFER.get(ind);
	                 				InetAddress IPAddress = InetAddress.getByName(vars.RCV_IP);
				                 	DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, vars.RCV_PORT);
				                 	vars.lastpktsent = sendData[0];
				                 	long start = System.nanoTime();
				                 	clientSocket.send(sendPacket);
				                 	vars.totalTransmissions++;
				                 	vars.unacked++;
				                 	int arrindex =((vars.lastpktsent+1)%vars.WINDOW_SIZE);
				                 	vars.timesarr[arrindex] = new PktTimes(vars.lastpktsent, start);
				                 	vars.attempts[arrindex]++;
				      
				                 	//System.out.println("Sent [#"+vars.lastpktsent+" : "+sendData.length+"]");
	                 			}
	                 			catch(IndexOutOfBoundsException e){
	                 				//System.err.println("IndexOutOfBoundsException " + e);
	                 				if(vars.BUFFER.size()==0){
	                 					sendWINDOW();
	                 				}
	                 				
	                 			}					                 	
		                 }		                 
    	        	 }
    	        	 catch(SocketTimeoutException esock){
    	        		 //System.out.println("TIMEDOUT #"+vars.lastACK);
    	        		 sendWINDOW();
    	        	 }
    	         }   	                 
          		DecimalFormat df = new DecimalFormat();
                df.setMaximumFractionDigits(6);
          		System.out.println("\nPacket Generation Rate : "+vars.PACKET_GEN_RATE);
                System.out.println("Packet Length : "+vars.PACKET_LENGTH);
                System.out.printf("Retransmission Ratio : %f\n",((float)vars.totalTransmissions/vars.totalACKed));
                System.out.println("Average RTT : "+df.format(vars.RTT*Math.pow(10, -6))+"\n");
    	      }
    	      catch(IOException e){
    	         //System.err.println("IOException " + e);
    	    	  //System.out.println("Socket Closed");
    	      }  
            
      //System.out.println("Thread " +  threadName + " exiting.");      
      clientSocket.close();
      System.exit(0); 
   }
   
   public void start () {
      //System.out.println("Starting " +  threadName );
      if (t == null) {
         t = new Thread (this, threadName);
         t.start ();
      }
   }
}

class Variables{

	   public int MAX_BUFFER_SIZE=-1,WINDOW_SIZE=-1,MAX_PACKETS=-1,PACKET_GEN_RATE=-1,PACKET_LENGTH=-1,RCV_PORT=-1;
	   public String RCV_IP = null;
	   public boolean DEBUG_MODE = false;
	   public ArrayList<byte[]> BUFFER;
	   public PktTimes[] timesarr;
	   public float RTT=0,timeout=100;
	   public int totalTransmissions=0, totalACKed=0, timedoutseqnum=-1,lastACK=-1,lastpktsent=-1,unacked=-1,pktgenstartseq=0;
	   public boolean ignoretimeout = false, sendWindow=true,initial=true;
	   public int[] attempts;

	   Variables(){
	      BUFFER = new ArrayList<byte[]>();
	   }
	   
	   public void init(){
		   timesarr = new PktTimes[WINDOW_SIZE]; 
		   attempts = new int[WINDOW_SIZE];
		   for(int i=0;i<WINDOW_SIZE;i++){
			   attempts[i]=0;
		   }			   
	   }
	   
	}