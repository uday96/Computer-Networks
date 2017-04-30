import matplotlib.pyplot as plt
import sys

if len(sys.argv)!=2:
	print "Invalid Args"
	sys.exit(0)

filename = str(sys.argv[1])

CWfile = open(filename,"r")

CWvals=[]
CWupdatenum=[]
updatenum = 0
isfirstline=True
config=""
for line in CWfile:
	if isfirstline:
		config = line
		isfirstline = False
	else:
		CWvals.append(int(line))
		CWupdatenum.append(updatenum)
		updatenum = updatenum + 1

CWfile.close()

imagename = (filename.split(".txt")[0])+".png"

plt.xlabel("CW Update Number")
plt.ylabel("CW Value (KB)")
plt.title("CW Variation")
plt.plot(CWupdatenum, CWvals, linestyle='-', color='b',label=config)
plt.legend(loc="best")
plt.savefig(imagename)
#plt.show()