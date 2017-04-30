import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;

class cw
{
	static HashMap<String, Double> cw_Multipliers = new HashMap<String, Double>(); 
	static int RWS = 1024; //1MB
	static int MSS = 1; //1KB
	static int CW = 0;
	static int Threshold = 0;
	static String phase = "initial";
    static PrintWriter writer;
	  
	public static void main(String args[]) throws Exception{
	   double Ki=1,Km=1,Kn=1,Kf=1,Ps=0;	   
	   int MAX_SEGMENTS=0;
	   String outputFile = "output.txt";
	   for(int i=0;i<args.length;i=i+2){
	         switch (args[i]){
	            case "-i":
	               Ki = Double.parseDouble(args[i+1]);
	               cw_Multipliers.put("initial", Ki);
	               break;
	            case "-m":
	            	Km = Double.parseDouble(args[i+1]);
	            	cw_Multipliers.put("exponential", Km);
	               break;
	            case "-n":
	            	Kn = Double.parseDouble(args[i+1]);
	            	cw_Multipliers.put("linear", Kn);
	               break;
	            case "-f":
	            	Kf = Double.parseDouble(args[i+1]);
	            	cw_Multipliers.put("timeout", Kf);
	               break;
	            case "-s":
	            	Ps = Double.parseDouble(args[i+1]);
	               break;
	            case "-T":
	               MAX_SEGMENTS = Integer.parseInt(args[i+1]);
	               break;
	            case "-o":
	            	outputFile = args[i+1].toString();
	            	break;
	            default:
	               System.out.println("Invalid CommandLine Args");
	         }
	   }
	   String configString="";
	   for(int i=0;i<args.length-2;i++){
		   configString += args[i]+" ";
	   }
	   writer = new PrintWriter(outputFile, "UTF-8");
	   writer.println(configString);
	   updateCW();
	   Threshold = (int)Math.ceil(0.5*RWS);
	   updatePhase();
	   int SENT=0,round=1;
	   while(SENT < MAX_SEGMENTS){
		   System.out.println("\n------Round #"+round+"------\n");
		   int toSend = (int)Math.ceil(CW/MSS);
		   System.out.println("N : "+toSend);
		   int sent=0;
		   while(sent<toSend){
			   if(Math.random() < Ps){
				   System.out.println("Timeout Occured");
				   sent=0;
				   phase="timeout";
				   Threshold = (int)Math.ceil(0.5*CW); 
				   updateCW();				
			   }
			   else {
				sent++;
				System.out.println("Sent #: "+SENT);
				SENT++;
				updatePhase();
				updateCW();
			   }
		   }	
		   round++;
	   }
	   writer.close();
	    
   }	
	
	private static void updateCW(){		
		double multiplier = cw_Multipliers.get(phase);
		switch (phase) {
		case "initial":
			CW = (int)Math.ceil(MSS*multiplier);
			break;
		case "exponential":
			CW = Math.min(((int)Math.ceil((double)CW+(MSS*multiplier))),RWS);
			break;
		case "linear":
			CW = Math.min(((int)Math.ceil((double)CW+(MSS*multiplier*MSS/CW))),RWS);
			break;
		case "timeout":
			CW = Math.max(1,(int)Math.ceil(CW*multiplier));
			break;
		default:
			break;
		}
		System.out.println("CW_New: "+CW);
	    writer.println(CW);
	}
	
	private static void updatePhase(){
		if(CW < Threshold){
			phase = "exponential";
		}
		else{
			phase = "linear";
		}
		System.out.println("Phase_New: "+phase);
	}
}
