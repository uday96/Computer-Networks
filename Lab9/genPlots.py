import matplotlib.pyplot as plt
import os

Ki=[1,4]
Km=[1,1.5]
Kn=[0.5,1]
Kf=[0.1,0.3]
Ps=[0.01,0.0001]
T=2000

filename="CWVariation"
count=1
for i in Ki:
	for m in Km:
		for n in Kn:
			for f in Kf:
				for s in Ps:
					os.system("make")
					os.system("java cw -i "+str(i)+" -m "+str(m)+" -n "+str(n)+" -f "+str(f)+" -s "+str(s)+" -T "+str(T)+" -o "+filename+str(count)+".txt")
					os.system("python PlotCW.py "+filename+str(count)+".txt")
					count = count +1
