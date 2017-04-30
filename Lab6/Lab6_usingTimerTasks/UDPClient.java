import java.io.*;
import java.net.*;
import java.util.*;

//SENDER
class UDPClient
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
         while(vars.totalTransmissions < vars.MAX_PACKETS){
        	 if(vars.initial==true && vars.sendWindow==true){
            	echo("BUFFSIZE : "+vars.BUFFER.size());
            	if(vars.BUFFER.size()<vars.WINDOW_SIZE-1){
            		System.out.println("Waiting for BUFFER refill");
            		currentThread.sleep(1000);
            		listenthread.sleep(1000);
            	}
            	System.out.println("Sending new Window");
            	for(int i=0;i<vars.WINDOW_SIZE-1 && i<vars.BUFFER.size();i++){
                	byte[] sendData = new byte[1024];
                	sendData = vars.BUFFER.get(i);
                	DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, vars.RCV_PORT);
                	vars.lastpktsent = sendData[0];
                	if(i==0){
                		vars.windowstartpkt = sendData[0];
                	}
                	Timer timeouttimer = new Timer();
                	long start = System.nanoTime();
                	timeouttimer.schedule(new Timeout(vars,vars.lastpktsent,clientSocket,currentThread,listenthread), (long)vars.timeout);
                	vars.unacked++;
                	int arrindex =((vars.lastpktsent+1)%vars.WINDOW_SIZE);
                	vars.timesarr[arrindex] = new PktTimes(vars.lastpktsent, start);
                	vars.PktTimers[arrindex] = timeouttimer;
                	echo("Sent [#"+vars.lastpktsent+" : "+sendData.length+"]");
                	clientSocket.send(sendPacket);
                	
                }
            	vars.sendWindow = false;
            	vars.initial = false;
            }
         }
         System.out.println("Max Transmissions reached : "+vars.MAX_PACKETS+" - "+vars.totalTransmissions);
         clientSocket.close();
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
   }
}

class Variables{

   public int MAX_BUFFER_SIZE=-1,WINDOW_SIZE=-1,MAX_PACKETS=-1,PACKET_GEN_RATE=-1,PACKET_LENGTH=-1,RCV_PORT=-1;
   public String RCV_IP = null;
   public boolean DEBUG_MODE = false;
   public ArrayList<byte[]> BUFFER;
   public PktTimes[] timesarr;
   public Timer[] PktTimers;
   public float RTT=0,timeout=100;
   public int totalTransmissions=0, timedoutseqnum=-1,lastACK=-1,lastpktsent=-1,windowstartpkt=-1,unacked=-1,pktgenstartseq=0;
   public boolean ignoretimeout = false, sendWindow=true,initial=true;

   Variables(){
      BUFFER = new ArrayList<byte[]>();
   }
   
   public void init(){
	   timesarr = new PktTimes[WINDOW_SIZE];
	   PktTimers = new Timer[WINDOW_SIZE]; 
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
      System.out.println("Creating " +  threadName );
   }
      
   public synchronized void refreshTimers(){
	   for(int i=0;i<vars.PktTimers.length;i++){
	  		 if(vars.PktTimers[i]!=null){
	  			 vars.PktTimers[i].cancel();
	  			 vars.PktTimers[i].purge();
	  			 vars.PktTimers[i] = null;
	  		}
	  	}
	  	System.out.println("L : Cancelled Existing Timers");
   }
   public synchronized void sendWINDOW(){
	   vars.sendWindow = true;
	  	refreshTimers(); 
	  	 try{
		   InetAddress IPAddress = InetAddress.getByName(vars.RCV_IP);
		   //while(vars.totalTransmissions < vars.MAX_PACKETS){
	        	 if(vars.sendWindow==true){
	            	System.out.println("BUFFSIZE : "+vars.BUFFER.size());
	            	vars.unacked = 0;
	            	if(vars.BUFFER.size()<vars.WINDOW_SIZE-1){
	            		System.out.println("T : Waiting for BUFFER refill");
	            		try{
	            			mainThread.sleep(1000);
		            		t.sleep(1000);
		            		System.out.println("BUFFSIZE : "+vars.BUFFER.size());
	            		}
	            		catch (InterruptedException ei) {
	            			System.err.println("InterruptedException : "+ei);
	            		}
	            		
	            	}
	            	//System.out.println("Sending new Window");
	            	for(int i=0;i<vars.WINDOW_SIZE-1 && i<vars.BUFFER.size();i++){
	                	byte[] sendData = new byte[1024];
	                	sendData = vars.BUFFER.get(i);
	                	DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, vars.RCV_PORT);
	                	vars.lastpktsent = sendData[0];
	                	if(i==0){
	                		vars.windowstartpkt = sendData[0];
	                	}
	                	Timer timeouttimer = new Timer();
	                	long start = System.nanoTime();
	                	timeouttimer.schedule(new Timeout(vars,vars.lastpktsent,clientSocket,mainThread,t), (long)vars.timeout);
	                	vars.unacked++;
	                	int arrindex =((vars.lastpktsent+1)%vars.WINDOW_SIZE);
	                	vars.timesarr[arrindex] = new PktTimes(vars.lastpktsent, start);
	                	vars.PktTimers[arrindex] = timeouttimer;
	                	System.out.println("Sent [#"+vars.lastpktsent+" : "+sendData.length+"]");
	                	clientSocket.send(sendPacket);	                	
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
      System.out.println("Running " +  threadName );
          	  try {
          		while(vars.totalTransmissions < vars.MAX_PACKETS){
    	        	 byte[] receiveData = new byte[1024];
    	        	 DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
    	        	 	 clientSocket.receive(receivePacket);
	                	 vars.unacked--;
		                 long end = System.nanoTime();
		                 int recievenum = receiveData[0];
		                 this.vars.lastACK = recievenum;
		                 //System.out.println("ACK # " + this.vars.lastACK);
		                 vars.PktTimers[vars.lastACK].cancel();
		                 vars.PktTimers[vars.lastACK].purge();
		                 vars.PktTimers[vars.lastACK] = null;
		                 vars.RTT = ((vars.RTT*vars.totalTransmissions)+(end-vars.timesarr[vars.lastACK].startTime))/(vars.totalTransmissions+1);
		                 vars.totalTransmissions = vars.totalTransmissions+1;
		                 System.out.println("ACK # " + this.vars.lastACK+"   Txns : "+vars.totalTransmissions);		                 
		                 /*if(vars.totalTransmissions>10){
		                	 vars.timeout = vars.RTT*2;
		                 }*/
		                 vars.BUFFER.remove(0);
		                 if(!vars.sendWindow && vars.unacked<vars.WINDOW_SIZE){
		                		byte[] sendData = new byte[1024];
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
				                 	Timer timeouttimer = new Timer();
				                 	long start = System.nanoTime();
				                 	timeouttimer.schedule(new Timeout(vars,vars.lastpktsent,clientSocket,mainThread,t), (long)vars.timeout);
				                 	vars.unacked++;
				                 	int arrindex =((vars.lastpktsent+1)%vars.WINDOW_SIZE);
				                 	vars.timesarr[arrindex] = new PktTimes(vars.lastpktsent, start);
				                 	vars.PktTimers[arrindex] = timeouttimer;
				                 	System.out.println("Sent [#"+vars.lastpktsent+" : "+sendData.length+"]");
				                 	clientSocket.send(sendPacket);				                 	
	                 			}
	                 			catch(IndexOutOfBoundsException e){
	                 				//System.err.println("IndexOutOfBoundsException " + e);
	                 				if(vars.BUFFER.size()==0){
	                 					sendWINDOW();
	                 				}
	                 				
	                 			}
					                 	
		                 }		                 
	                 
    	         }
          		System.out.println("Max Transmissions reached : "+vars.MAX_PACKETS+" - "+vars.totalTransmissions);
                clientSocket.close();
                System.exit(0);    	                 
    	      }
    	      catch(IOException e){
    	         //System.err.println("IOException " + e);
    	    	  System.out.println("Socket Closed");
    	      }  
            
      System.out.println("Thread " +  threadName + " exiting.");
   }
   
   public void start () {
      System.out.println("Starting " +  threadName );
      if (t == null) {
         t = new Thread (this, threadName);
         t.start ();
      }
   }
}

class Timeout extends TimerTask {
	   
	   private int sentseqnum;
	   Variables vars;
	   DatagramSocket clientSocket;
	   Thread mainThread,listenThread;
	   
	   Timeout(Variables vars,int sentseqnum,DatagramSocket clientSocket,Thread mainThread,Thread listenThread){
	      this.sentseqnum = sentseqnum;
	      this.vars = vars;
	      this.clientSocket = clientSocket;
	      this.mainThread = mainThread;
	      this.listenThread = listenThread;
	      
	   }
	   // run is a abstract method that defines task performed at scheduled time.
	   public synchronized void run() {
		   
			   System.out.println("Timedout #"+this.sentseqnum);
			   this.vars.timedoutseqnum = this.sentseqnum;
			   this.vars.sendWindow = true;
			   for(int i=0;i<vars.PktTimers.length;i++){
				   if(vars.PktTimers[i]!=null){
					   vars.PktTimers[i].cancel();
					   vars.PktTimers[i].purge();
					   vars.PktTimers[i] = null;
				   }
			   }
			   System.out.println("T : Cancelled Existing Timers");
			   try{
				   InetAddress IPAddress = InetAddress.getByName(vars.RCV_IP);
				   //while(vars.totalTransmissions < vars.MAX_PACKETS){
			        	 if(vars.sendWindow==true){
			            	System.out.println("BUFFSIZE : "+vars.BUFFER.size());
			            	vars.unacked = 0;
			            	if(vars.BUFFER.size()<vars.WINDOW_SIZE-1){
			            		System.out.println("T : Waiting for BUFFER refill");
			            		try{
			            			mainThread.sleep(1000);
				            		listenThread.sleep(1000);
			            		}
			            		catch (InterruptedException ei) {
			            			System.err.println("InterruptedException : "+ei);
			            		}
			            		
			            	}
			            	System.out.println("Sending new Window");
			            	for(int i=0;i<vars.WINDOW_SIZE-1 && i<vars.BUFFER.size();i++){
			                	byte[] sendData = new byte[1024];
			                	sendData = vars.BUFFER.get(i);
			                	DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, vars.RCV_PORT);
			                	vars.lastpktsent = sendData[0];
			                	if(i==0){
			                		vars.windowstartpkt = sendData[0];
			                	}
			                	Timer timeouttimer = new Timer();
			                	long start = System.nanoTime();
			                	timeouttimer.schedule(new Timeout(vars,vars.lastpktsent,clientSocket,mainThread,listenThread), (long)vars.timeout);
			                	vars.unacked++;
			                	int arrindex =((vars.lastpktsent+1)%vars.WINDOW_SIZE);
			                	vars.timesarr[arrindex] = new PktTimes(vars.lastpktsent, start);
			                	vars.PktTimers[arrindex] = timeouttimer;
			                	System.out.println("Sent [#"+vars.lastpktsent+" : "+sendData.length+"]");
			                	clientSocket.send(sendPacket);			                	
			                }
			            	vars.sendWindow = false;
			            	
			        	 }   
				   //}
			   }
			   catch(IOException eio){
				   System.err.println("IOException : "+eio);
			   }
		    
	   }
	}