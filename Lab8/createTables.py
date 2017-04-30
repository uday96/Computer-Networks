files = ["512K","1M","2M"]

for file in files:
	html_content = """<html>
		<style>
				table, th, td {
				    border: 1px solid black;
				    border-collapse: collapse;
				}
				th, td {
				    padding: 5px;
				    text-align: left;
				}
		</style>
		<body>
			<h2><center><b><u>File Size : """+file+"""B</u></b></center></h2>
			<table align="center">
				<tr>
					<th><b>SACK</b></th>
					<th><b>Window Size (KB)</b></th>
					<th><b>Link Delay (ms)</b></th>
					<th><b>Link Drop %</b></th>
					<th><b>TCP Protocol</b></th>
					<th><b>Throughput (KB/s)</b></th>
					<th><b>Latency (s)</b></th>
				</tr>"""

	summary = open("summary_"+file+".txt","r")
	for line in summary:
		line_data = line.split(" ")
		html_content = html_content +"			<tr>\n"
		for line_vals in line_data:
			vals = line_vals.split("=")
			html_content = html_content +"				<td>"+str(vals[1])+"</td>\n"
		html_content = html_content +"			</tr>\n"
	html_content = html_content +"""		</table>
			</body>
		</html>"""
	html_file = open("table_"+file+".html","w")
	html_file.write(html_content)
	summary.close()
	html_file.close()