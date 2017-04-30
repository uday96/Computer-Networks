#**GO-BACK-N (GBN) RELIABLE RETRANSMISSION PROTOCOL - UDP**

###**Execution**
> - **Compilation :** make
> - **Run Server (Receiver) :** java ReceiverGBN  -p <PORT> -n <MAX_PACKETS> -e <RANDOM DROP PROB> -w <WINDOW SIZE> -d(optional - Debug Mode)
> - **Run Client (Sender) :** java SenderGBN -s <HOST> -p <PORT> -l <PACKET LENGTH> -r <PACKET GEN RATE> -n <MAX PACKETS> -w <WINDOW SIZE> -b <MAX BUFFER SIZE> -d(optional - Debug Mode)

###**Command Line Options**
The command line options provided to the sender are listed below:
> - -d – Turn ON Debug Mode (OFF if -d flag not present)
> - -s string – Receiver Name or IP address.
> - -p integer – Receiver’s Port Number
> - -l integer – PACKET LENGTH, in bytes
> - -r integer – PACKET GEN RATE, in packets per second
> - -n integer – MAX PACKETS
> - -w integer – WINDOW SIZE
> - -b integer – MAX BUFFER SIZE

The command line options provided to the receiver are listed below:
> - -d – Turn ON Debug Mode (OFF if -d flag not present)
> - -p integer – Receiver’s Port Number
> - -n integer – MAX PACKETS
> - -e float – Packet Error Rate (RANDOM DROP PROB)

###**Termination**
The Sender terminates after:
> - MAX PACKETS (a command-line parameter) have been successfully ACKNOWLEDGED
									(OR)
> - If the maximum retransmission attempts for any sequence number exceeds 5.
	
The Receiver terminates after:
> - Acknowledging MAX PACKETS (a command-line parameter).