import java.util.*;

class SlottedAloha{
	
	public static void main(String args[]){
		int NUM_USERS=0,CW_SIZE_init=2,MAX_PACKETS=-1;
		double PACKET_GEN_RATE=-1;
		for(int i=0;i<args.length;i=i+2){
	         switch (args[i]){
	            case "-N":
	               NUM_USERS = Integer.parseInt(args[i+1]);
	               break;
	            case "-W":
	               CW_SIZE_init = Integer.parseInt(args[i+1]);
	               break;
	            case "-M":
	               MAX_PACKETS = Integer.parseInt(args[i+1]);
	               break;
	            case "-p":
	               PACKET_GEN_RATE = Double.parseDouble(args[i+1]);
	               break;
	            default:
	               error("Invalid CommandLine Args");
	         }
	    }
		
		double AVERAGE_pkt_delay=0,AVERAGE_utilisation=0;
		for(int iters=0;iters<100;iters++){
			HashMap<Integer, ArrayList<Packet>> BUFFERS = new HashMap<Integer, ArrayList<Packet>>();
			boolean[] isBackLogged = new boolean[NUM_USERS];
			int[] BackOffCounters = new int[NUM_USERS];
			int[] CollisionWindows = new int[NUM_USERS];
			for(int i=0;i<NUM_USERS;i++){
				BUFFERS.put(i,new ArrayList<Packet>());
				isBackLogged[i] = false;
				BackOffCounters[i] = 0;
				CollisionWindows[i] = CW_SIZE_init;
			}
			int SimTime=0,pktsTransmitted=0;
			double avgPktDelay=0;
			while(pktsTransmitted<MAX_PACKETS){
				//System.out.println("\n-----------------Time Slot : "+SimTime+"-------------------\n");
				//System.out.println("BKC: "+Arrays.toString(BackOffCounters));
				//System.out.println("isBackLogged: "+Arrays.toString(isBackLogged));
				//System.out.println("CWs: "+Arrays.toString(CollisionWindows));
				//System.out.print("Pkts added to Nodes: ");

				ArrayList<Integer> tosend = new ArrayList<Integer>();

				for(int i=0;i<NUM_USERS;i++){
					if(Math.random()<PACKET_GEN_RATE){
						ArrayList<Packet> node_buff = BUFFERS.get(i);
						if(node_buff.size()<2){
							node_buff.add(new Packet(SimTime));
							//System.out.print(i+" ");
							if(node_buff.size()==1){
								tosend.add(i);
							}
						}
					}
				}
				//System.out.println();
				int num_sendPkt=0,user_sendPkt=-1;
				boolean exitflag=false;
				for(int i=0;i<NUM_USERS;i++){
					ArrayList<Packet> currBUFF = BUFFERS.get(i);				
					if((tosend.contains(i)==true) || ((BackOffCounters[i]==0) && (currBUFF.size()>0))){
						num_sendPkt++;
						user_sendPkt = i;
						Packet pkt = currBUFF.get(0);
						pkt.TxnAttempts++;
						if(pkt.TxnAttempts>10){
							exitflag=true;
						}
					}
				}
				if(exitflag==true){
					break;
				}
				//System.out.println("Users ready for txn : "+num_sendPkt);
				for(int i=0;i<NUM_USERS;i++){
					if((i==user_sendPkt) && (num_sendPkt==1)){
						//System.out.println("Transmission Successful #"+user_sendPkt);
						CollisionWindows[i] = (int)(Math.max(2, ((CollisionWindows[i])*0.75) ));
						ArrayList<Packet> currBUFF = BUFFERS.get(i);
						Packet pkt = currBUFF.get(0);
						//long genTime = pkt.genTime;
						//long currTime = System.nanoTime();
						int genSimTime = pkt.genSimTime;
						//avgPktDelay = (((avgPktDelay*pktsTransmitted)+(currTime-genTime))/(pktsTransmitted+1));
						avgPktDelay = (((avgPktDelay*pktsTransmitted)+(SimTime-genSimTime))/(pktsTransmitted+1));
						currBUFF.remove(0);
						pktsTransmitted++;
						if(currBUFF.size()==0){
							isBackLogged[i]=false;
						}
						else{
							isBackLogged[i]=true;
						}
					}
					else{
						//System.out.println("Transmission Unsuccessful #"+i);
						isBackLogged[i]=true;
						BackOffCounters[i] = 1 + (int)(Math.random()*(CollisionWindows[i]-1));
						CollisionWindows[i] = (int)(Math.min(256, (CollisionWindows[i]*2)));
					}
				}
				for(int i=0;i<NUM_USERS;i++){
					if(BackOffCounters[i]>0){
						BackOffCounters[i]--;
					}
				}
				SimTime++;
				//System.out.println("Num Txns : "+pktsTransmitted);
				
			}
			//System.out.println("\n----------------------------------------------------\n");
			AVERAGE_pkt_delay = ((AVERAGE_pkt_delay*iters)+avgPktDelay)/(iters+1);
			double util_iter = (((double)pktsTransmitted)/SimTime);
			AVERAGE_utilisation = ((AVERAGE_utilisation*iters)+util_iter)/(iters+1);
		}
		System.out.println("\nNumber Of Nodes : "+NUM_USERS);
		System.out.println("Initial Collision Window Size : "+CW_SIZE_init);
		System.out.println("Probability Of Packet Generation : "+PACKET_GEN_RATE);
		//System.out.println("Average Packet Delay : "+avgPktDelay*Math.pow(10, -6)+" ms");
		System.out.println("Average Packet Delay : "+AVERAGE_pkt_delay+" slots");
		System.out.println("Utilisation (Pkts transmitted per Slot) : "+AVERAGE_utilisation);
		System.out.println();
		System.exit(0);
		
	}
	
	public static void error(String msg)
	   {
	      System.out.println("ERROR : "+msg);
	      System.exit(0);
	   }
}

class Packet{
	long genTime;
	int genSimTime;
	int TxnAttempts;
	public Packet(int genSimTime) {
		genTime = System.nanoTime();
		TxnAttempts=0;
		this.genSimTime = genSimTime;
	}
}