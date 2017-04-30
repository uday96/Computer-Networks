import matplotlib.pyplot as plt

files = ["summary_512K.txt","summary_1M.txt","summary_2M.txt"]
indices = ["SACK","WIN","DELAY","DROP","PROT","speed","time"]

AVG_speed=[]
AVG_time=[]
for file in files:
	summary = open(file,"r")
	avg_speed=[0,0]
	avg_time=[0,0]
	count=[0,0]
	for line in summary:
		val = line.split(" ")
		data = val[indices.index("SACK")].split("=")[1]
		spd = float(val[indices.index("speed")].split("=")[1][:-4])
		time = float(val[indices.index("time")].split("=")[1][:-2])
		if data == "OFF":
			avg_speed[0] = (avg_speed[0]*count[0] + spd)/(count[0]+1)
			avg_time[0] = (avg_time[0]*count[0] + time)/(count[0]+1)
			count[0] = count[0]+1
		else:
			avg_speed[1] = (avg_speed[1]*count[1] + spd)/(count[1]+1)
			avg_time[1] = (avg_time[1]*count[1] + time)/(count[1]+1)
			count[1] = count[1]+1
	AVG_speed.append(avg_speed)
	AVG_time.append(avg_time)
print AVG_speed
print AVG_time

plot11 = []
plot12 = []
for ob in AVG_speed:
	plot11.append(ob[0])
	plot12.append(ob[1])

plot21 = []
plot22 = []
for ob in AVG_time:
	plot21.append(ob[0])
	plot22.append(ob[1])


plt.xlabel("File Size (MB)")
plt.ylabel("Throughput (KB/s)")
plt.title("File Size vs Throughput")
plt.plot([0.5,1,2], plot11, linestyle='-', marker='o', color='b',label="SACK=OFF")
plt.plot([0.5,1,2], plot12, linestyle='-', marker='o', color='r',label="SACK=ON")
plt.legend(loc="upper left")
plt.show()

plt.xlabel("File Size (MB)")
plt.ylabel("Time (secs)")
plt.title("File Size vs Latency")
plt.plot([0.5,1,2], plot21, linestyle='-', marker='o', color='b',label="SACK=OFF")
plt.plot([0.5,1,2], plot22, linestyle='-', marker='o', color='r',label="SACK=ON")
plt.legend(loc="upper left")
plt.show()