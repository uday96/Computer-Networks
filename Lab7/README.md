#**Slotted Aloha Protocol**
--------------------------------------
###**Execution**
----------------
**Execute Command :** 
>./ShellScript.sh

The Shell Script file ShellScript.sh compiles and executes the program will generate throughput values for varying 
> - N = 50,
> - p ∈ {0.01, 0.02, 0.03, 0.05, 0.1} and 
> - W ∈ {2, 4} values

The outputs for each run are stored in result.txt

###**Summary of Command Line Options:** 
----------------
The command line options provided are listed below:
> -  -N integer – number of users sharing the channel
> - -W integer – specifies initial value of Collision Window size for all nodes (W (i); default: 2)
> - -M integer – MAX PACKETS
> - -p double – PACKET GEN RATE (p): probability of packet generation per unit time per node


###**Files**
----------------
**SlottedAloha.java :**
> Executes the protocol and stores output in result.txt

**UtilisationPlot.java :** 
> - Reads data from result.txt and Plots a Graph
> - Uses JFreeChart for plotting. jcommon-1.0.23.jar and jfreechart-1.0.19.jar needed for execution.

###**Termination**
----------------
The program terminates after:
> - MAX PACKETS have been successfully transmitted combined across all nodes
									(OR)
> - If the maximum retransmission attempts for any packet exceeds 10.