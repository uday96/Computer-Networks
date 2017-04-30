import os

SAC=[];
SAC.append("sudo  sysctl -w net.ipv4.tcp_sack='0'")
SAC.append("sudo  sysctl -w net.ipv4.tcp_sack='1'")

WIN_16K=[]
WIN_16K.append("sudo sysctl -w net.ipv4.tcp_window_scaling='0'")
WIN_16K.append("sudo sysctl -w net.core.rmem_max='16777216'")
WIN_16K.append("sudo sysctl -w net.ipv4.tcp_rmem='4096 87380 16777216'")
WIN_16K.append("sudo sysctl -w net.ipv4.tcp_wmem='4096 16384 16777216'")

WIN_256K=[]
WIN_256K.append("sudo sysctl -w net.ipv4.tcp_window_scaling='1'")
WIN_256K.append("sudo sysctl -w net.core.rmem_max='16777216'")
WIN_256K.append("sudo sysctl -w net.ipv4.tcp_rmem='4096 87380 16777216'")
WIN_256K.append("sudo sysctl -w net.ipv4.tcp_wmem='4096 262144 16777216'")

WIN=[]
WIN.append(WIN_16K)
WIN.append(WIN_256K)

START= "sudo tc -s qdisc ls dev wlan0"
RESET="sudo tc qdisc del dev wlan0  root"

DELAY=[];
DELAY.append("sudo tc qdisc add dev wlan0   root netem delay 2ms 0.2ms 25%")
DELAY.append("sudo tc qdisc add dev wlan0   root netem delay 50ms 5ms 25%")

DROP=[]
DROP.append("sudo tc qdisc change dev wlan0  root netem loss 0.5% 25%")
DROP.append("sudo tc qdisc change dev wlan0  root netem loss 5% 25%")

TCP_PROT=[]
TCP_PROT.append("sudo echo cubic > /proc/sys/net/ipv4/tcp_congestion_control")
TCP_PROT.append("sudo echo reno > /proc/sys/net/ipv4/tcp_congestion_control")

vals_sac = ["OFF","ON"]
vals_win = ["16KB","256KB"]
vals_delay = ["2ms","50ms"]
vals_drop = ["0.5%","5%"]
vals_prot = ["cubic","reno"]
files = ["file_512K","file_1M","file_2M"]
for file in files:
	EXE="sudo wget -a "+file[5:]+"_log -r http://192.168.0.100:8080/"+file+".dat 2>&1 | sudo tee -a "+file[5:]+"_log"
	f=open(file[5:]+".txt","a")
	for p in range(2):
		os.system(TCP_PROT[p])
		for i in range(2):
			os.system(SAC[i])
			for w in range(2):
				for j in WIN[w]:
					os.system(j)
				for k in range(2):
					os.system(RESET)
					os.system(START)
					os.system(DELAY[k])
					for l in range(2):
						os.system(DROP[l])
						f.write("SAC="+vals_sac[i]+" "+"WIN="+vals_win[w]+" "+"DELAY="+vals_delay[k]+" "+"DROP="+vals_drop[l]+" PROTO="+vals_prot[p]+"\n")
						for t in range(0,5):
							os.system(EXE)
