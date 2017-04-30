import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

class ospf{
	
	public static void main(String args[]) throws Exception{	   
	   Variables vars = new Variables();
	   for(int i=0;i<args.length;i=i+2){
	         switch (args[i]){
	            case "-f":
	            	vars.inFileString = (args[i+1]).toString();
	               break;
	            case "-o":
	            	vars.outFileString = (args[i+1]).toString();
	               break;
	            case "-h":
	            	vars.HELLO_INTERVAL = Double.parseDouble(args[i+1]);
	               break;
	            case "-a":
	            	vars.LSA_INTERVAL = Double.parseDouble(args[i+1]);
	               break;
	            case "-s":
	            	vars.SPF_INTERVAL = Double.parseDouble(args[i+1]);
	               break;
	            case "-i":
	            	vars.routerID = Integer.parseInt(args[i+1]);
	               break;
	            default:
	               System.out.println("Invalid CommandLine Args");
	         }
	   }
	   File inFile = new File(vars.inFileString);
	   Scanner input = new Scanner(inFile);
	   int N = input.nextInt();
	   vars.build(N);
	   int num_links = input.nextInt();
	   
	   vars.routerNode = new nodelinks(vars.routerID);
	   vars.routerLSA = new LinkState(vars.routerID);
	   
	   for(int j=0;j<num_links;j++){
		   int node1 = input.nextInt();
		   int node2 = input.nextInt();
		   int minC = input.nextInt();
		   int maxC = input.nextInt();
		   if(node1 == vars.routerID){
			   vars.routerNode.addNeighbor(node2, minC, maxC);
		   }
		   else if(node2 == vars.routerID){
			   vars.routerNode.addNeighbor(node1, minC, maxC);
		   }		   
       }
       input.close();
       
       System.out.println("Starting Listener Thread");
       ListenerThread listener = new ListenerThread(vars);
       listener.start();
       
       System.out.println("Starting Hello Task");
       Timer hellotimer = new Timer();
       hellotimer.scheduleAtFixedRate(new HelloTask(vars), 0, (long) (vars.HELLO_INTERVAL*1000));
       
       System.out.println("Starting LSA Task");
       Timer lsatimer = new Timer();
       lsatimer.scheduleAtFixedRate(new LSATask(vars), (long) (vars.LSA_INTERVAL*1000), (long) (vars.LSA_INTERVAL*1000));
       
       System.out.println("Starting Path Task");
       Timer spftimer = new Timer();
       spftimer.scheduleAtFixedRate(new PathTask(vars), (long) (vars.SPF_INTERVAL*1000), (long) (vars.SPF_INTERVAL*1000));
	}
}

class Variables{
	int routerID=-1,N=0;
	double HELLO_INTERVAL=1,LSA_INTERVAL=5,SPF_INTERVAL=20;
	String outFileString = "output.txt";
	String inFileString = "input.txt";
	nodelinks routerNode = null;
	LinkState routerLSA = null;
	int[] LSASeqnums;
	HashMap<Integer, String> allLSA;
	
	public void build(int N) {
		this.N = N;
		LSASeqnums = new int[N];
		for(int i=0;i<N;i++){
			LSASeqnums[i]=-1;
		}
		allLSA = new HashMap<Integer, String>();
	}
}

class nodelinks{
	HashMap<Integer, Integer[]> neighbors = new HashMap<Integer, Integer[]>();
	int id=-1;
	
	public nodelinks(int id) {
		this.id=id;
	}
	
	public void addNeighbor(int j,int minC, int maxC){
		Integer[] vals = {minC,maxC};
		neighbors.put(j,vals);
	}
	
	public Set<Integer> getNeighborsSet(){
		return neighbors.keySet();
	}
}

class LinkState{
	HashMap<Integer, Integer> neighborCosts = new HashMap<Integer, Integer>();
	int id=-1;
	
	public LinkState(int id) {
		this.id=id;
	}
	
	public void set(int node,int cost){
		neighborCosts.put(node,cost);
	}
	
	public int get(int node){
		return neighborCosts.get(node);
	}
	
	public Set<Integer> getNeighborsSet(){
		return neighborCosts.keySet();
	}
}

class HelloTask extends TimerTask{
	Variables vars = null;
	DatagramSocket sock = null;
	InetAddress host = null;
	
	public HelloTask(Variables vars){
      this.vars = vars;
      try {
			sock = new DatagramSocket();
			host = InetAddress.getByName("localhost");
      }
      catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
      }
      catch (UnknownHostException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
      }
    }
	
    public void run(){
    	System.out.println("#"+vars.routerID+" : Hello Task : Started");
      	 for(int nghbr:vars.routerNode.getNeighborsSet()){
            try {
            	//vars.routerLSA.set(nghbr, 0);
            	String s = "HELLO "+vars.routerID;
            	byte[] b = s.getBytes();
            	DatagramPacket  dp = new DatagramPacket(b , b.length , host , (nghbr+10000));
				sock.send(dp);
				System.out.println("#"+vars.routerID+" : Hello Task : sent to - #"+nghbr);
			}
            catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
      	 }
      	System.out.println("#"+vars.routerID+" : Hello Task : Ended");
    }
}

class LSATask extends TimerTask{
	Variables vars;
	DatagramSocket sock = null;
	InetAddress host = null;
	int seqnum = 0;
	
	public LSATask(Variables vars){
      this.vars = vars;
      try {
			sock = new DatagramSocket();
			host = InetAddress.getByName("localhost");
      }
      catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
      }
      catch (UnknownHostException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
      }
    }
	
    public void run(){
    	System.out.println("#"+vars.routerID+" : LSA Task : Started");
    	
    	String s = "LSA "+vars.routerID+" "+seqnum+" "+vars.routerLSA.neighborCosts.size();
    	for(int nodeKey: vars.routerLSA.getNeighborsSet()){
    		s = s +" "+nodeKey+" "+vars.routerLSA.get(nodeKey);
    	}
    	
    	for(int nghbr:vars.routerNode.getNeighborsSet()){
            try {
            	byte[] b = s.getBytes();
            	DatagramPacket  dp = new DatagramPacket(b , b.length , host , (nghbr+10000));
				sock.send(dp);
				System.out.println("#"+vars.routerID+" : LSA Task : sent to - #"+nghbr);
			}
            catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
      	}
      	seqnum++;
      	System.out.println("#"+vars.routerID+" : LSA Task : Ended");
    }
}

class PathTask extends TimerTask{
	
	Variables vars;
	PrintWriter writer;
	String outputname;
	
	public PathTask(Variables vars){
		this.vars = vars;
		outputname = vars.outFileString;
		if(vars.outFileString.contains(".txt")){
			String outString = vars.outFileString.split(".txt")[0];
			outputname = outString+"-"+vars.routerID+".txt";
		}
		else{
			outputname = vars.outFileString+"-"+vars.routerID+".txt";
		}
		File file = new File(outputname);
		try {
			Files.deleteIfExists(file.toPath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	int minDistance(int dist[], Boolean[] shortestPathSet){
	    int min = Integer.MAX_VALUE, min_index=-1;
	    for (int v = 0; v < vars.N; v++){
	    	if (shortestPathSet[v] == false && dist[v] <= min){
	    		min = dist[v];
	    		min_index = v;
	    	}	            
	    }	 
	    return min_index;
	}
	 
	void printPath(int parent[], int j){
	    
	    if (parent[j]==-1)
	        return;
	 
	    printPath(parent, parent[j]);
	 
	    writer.print(" - "+j);
	}
	 
	void printRoutingTable(int dist[], int n, int parent[]){
	    try {
			writer = new PrintWriter(new FileOutputStream(new File(outputname),true));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    writer.println("-----------------------------------------------------------");
	    writer.println("Routing Table for Node #"+vars.routerID+" at "+new Date());
	    writer.println("-----------------------------------------------------------");
    	writer.println("\nDest\tCost\tPath");
	    for (int i = 0; i < vars.N; i++){
	    	if(i!=vars.routerID){
	    		writer.print("\n "+i+" \t\t "+dist[i]+"\t\t "+vars.routerID);
		        printPath(parent, i);
	    	}	    	
	    }
	    writer.println("\n");
	    writer.close();
	}
	 
	void Dijkstra(int graph[][], int src)
	{
		int dist[] = new int[vars.N];	 
		Boolean shortestPathSet[] = new Boolean[vars.N];	 
		int parent[] = new int[vars.N];
	 
	    for (int i = 0; i < vars.N; i++){
	        parent[i] = -1;
	        dist[i] = Integer.MAX_VALUE;
	        shortestPathSet[i] = false;
	    }

	    dist[src] = 0;
	 
	    for (int count = 0; count < vars.N-1; count++){
	        int u = minDistance(dist, shortestPathSet);	 
	        shortestPathSet[u] = true;
	        for (int v = 0; v < vars.N; v++){
	        	if (!shortestPathSet[v] && graph[u][v]!=0 && dist[u] != Integer.MAX_VALUE && dist[u] + graph[u][v] < dist[v]){
	                parent[v]  = u;
	                dist[v] = dist[u] + graph[u][v];
		        }
	        }	              
	    }
	 
	    printRoutingTable(dist, vars.N, parent);
	}
    
	public synchronized void  run() {
		System.out.println("#"+vars.routerID+" : Computing Shortest Paths : Started");
		int[][] topology = new int[vars.N][vars.N];
		for(int ngbr:vars.routerLSA.getNeighborsSet()){
			topology[vars.routerID][ngbr] = vars.routerLSA.get(ngbr);
		}
		for(int node:vars.allLSA.keySet()){
			String currStateString = vars.allLSA.get(node);
			String[] currState = currStateString.split(" ");
			for(int i=0;i<2*Integer.parseInt(currState[3]);i+=2){
				int ngbr = Integer.parseInt(currState[4+i]);
				int cost = Integer.parseInt(currState[4+i+1]);
				topology[node][ngbr] = cost;
			}
		}
		System.out.println("\nGraph :");
		for(int x=0;x<vars.N;x++){
			for(int y=0;y<vars.N;y++){
				System.out.print(topology[x][y]+" ");
			}
			System.out.println("\n");
		}
		Dijkstra(topology, vars.routerID);		
		System.out.println("#"+vars.routerID+" : Computing Shortest Paths : Ended");
	}
	
}

class ListenerThread extends Thread{  
	Variables vars;
	
	public ListenerThread(Variables vars){
		this.vars = vars;
	}	
	
	public void run(){  
		try
        {
            //1. creating a server socket, parameter is local port number
			DatagramSocket sock = new DatagramSocket((vars.routerID+10000));
             
            //buffer to receive incoming data
            byte[] buffer = new byte[65536];
            DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
             
            //2. Wait for an incoming data
            System.out.println("#"+vars.routerID+" : Server socket created. Waiting for incoming data...");
             
            InetAddress host = InetAddress.getByName("localhost");
            
            //communication loop
            while(true)
            {
                sock.receive(incoming);
                byte[] data = incoming.getData();
                String s = new String(data, 0, incoming.getLength());
                
                //echo the details of incoming data - client message
                System.out.println("#"+vars.routerID+" : recieved : " + s);
                
                String[] words = s.split(" ");
                if(words[0].equals("HELLO")){
                	int srcid = Integer.parseInt(words[1]);
                	Integer[] linkvals = vars.routerNode.neighbors.get(srcid);
                	Random r = new Random();
                	int Low = linkvals[0]; //inclusive
                	int High = linkvals[1]; //exclusive
                	int Result = r.nextInt(High-Low) + Low;
                	s = "HELLOREPLY "+vars.routerID+" "+srcid+" "+Result;
                    DatagramPacket dp = new DatagramPacket(s.getBytes() , s.getBytes().length , host , (srcid+10000));
                    sock.send(dp);              	
                }
                else if(words[0].equals("LSA")){
                	int currLSAseq = Integer.parseInt(words[2]);
                	int srcid = Integer.parseInt(words[1]);
                	if(currLSAseq>vars.LSASeqnums[srcid]){
                		vars.allLSA.put(srcid, s);
                		vars.LSASeqnums[srcid] = currLSAseq;
                		for(int sendPort:vars.routerNode.getNeighborsSet()){
                			if((sendPort!=(incoming.getPort()-10000)) && sendPort!=vars.routerID ){
                				DatagramPacket dp = new DatagramPacket(s.getBytes() , s.getBytes().length , host , (sendPort+10000) );
                                sock.send(dp);
                			}
                		}
                	}
                }
                else if(words[0].equals("HELLOREPLY")){
                	int myid = Integer.parseInt(words[2]);
                	int recvid = Integer.parseInt(words[1]);
                	int cost = Integer.parseInt(words[3]);
                	if(vars.routerID==myid){
                		vars.routerLSA.set(recvid, cost);
                	}
                }
                
            }
        }
         
        catch(IOException e)
        {
            System.err.println("IOException " + e);
        }
	}
}