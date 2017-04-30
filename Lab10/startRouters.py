import sys, subprocess,os,thread,time

if len(sys.argv)!=2:
	print "Invalid Args"
	print "Usage: python startRouters.py <inputFilename>"
	sys.exit(0)

filename = str(sys.argv[1])
inputfile = open(filename,"r")
N = int(inputfile.readline().split(" ")[0])

print "Routers #"+str(N)
os.system("rm output-*")
os.system("javac ospf.java")

try:
	for routerID in range(N):
		cmd = "java ospf -i "+str(routerID)
		subprocess.Popen(['gnome-terminal','-e',cmd])
except Exception as e:
   print "Error: "+str(e)