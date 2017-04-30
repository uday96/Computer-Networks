---------------------------------
Lab 10: OSPF Routing Algorithm
---------------------------------

Each ospf router must be executed as a seperate linux process in a new terminal with command :
	./ospf -i id -f infile -o outfile -h hi -a lsai -s spfi

The values specified in the command line are:
	• -i id: Node identifier value (i)
	• -f infile: Input file
	• -o outfile: Output file
	• -h hi: HELLO INTERVAL (in seconds) (default : 1sec)
	• -a lsai: LSA INTERVAL (in seconds) (default : 5sec)
	• -s spfi: SPF INTERVAL (in seconds) (default : 20sec)

Command to Execute all Routers using default commandline args :
	python startRouters.py

infile Format :
	The first entry on the first line specifies the number of routers (N). The node indices go from 0 to (N−1). The second entry on the first line specifies the number of links. Each subsequent row contains the tuple (i,j,MinC ij ,MaxC ij). This implies a bidirectional link between nodes i and j.

outfile Format :
	The routing table will be stored in the output file along with the time stamp. The output file name for Node i will be outfile–i.txt, where outfile is specified in the command line.