files = ["512K","1M","2M"]

for file in files:
	summary = open("summary_"+file+".txt","a")
	config = open(file+".txt","r")
	data = open(file+"_log","r")
	config_count = 0
	avg_time = 0
	avg_speed = 0
	data_count = 0
	for data_line in data:
		if "Downloaded:" in data_line:
			vals = data_line.split("in ")[1]
			vals = vals.split("s (")
			time = float(vals[0])
			speed_vals = vals[1].split(" ")
			speed = -100000
			if speed_vals[1][:2] == "KB":
				speed = float(speed_vals[0])
			elif speed_vals[1][:2] == "MB":
				speed = float(speed_vals[0])*1000
			avg_speed = (avg_speed*data_count + speed)/(data_count + 1)
			avg_time = (avg_time*data_count + time)/(data_count + 1)
			print speed, time, avg_speed, avg_time, speed_vals[1][:2]
			data_count = data_count + 1
			if data_count ==5:
				print "--------------"+str(config_count)
				config_count = config_count + 1
				config_data = config.readline()
				summary.write(config_data[:-1]+" speed="+str(avg_speed)+"KB/s time="+str(avg_time)+"s\n")
				avg_time = 0
				avg_speed = 0
				data_count = 0
	summary.close()
	config.close()
	data.close()

