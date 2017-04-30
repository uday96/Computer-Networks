#**Lab 9: TCP Congestion Control**
----------------------------------
###**Execution**
**Execute once:**
> - make
> - java cw -i <double> -m <double> -n <double> -f <double> -s <double> -T <int> -o outfile
> - python PlotCW.py outfile

**Execute for parameter combinations:**
> - python genPlots.py

###**Files**
- **cw.java:**
	Given the set of input parameters, the emulation progresses. The congestion window value is printed to the output file (one per line) at each CW update.

- **PlotCW.py :**
	Plots a graph with x-axis being the update number and y-axis the corresponding CW value must be plotted.

- **genPlots.py:**
	Generates the results and graphs obtained for the following parameter combinations:
	Ki ∈ {1, 4}; Km ∈ {1, 1.5}; Kn ∈ {0.5, 1}; Kf ∈ {0.1, 0.3}; Ps ∈ {0.01, 0.0001}.
	Where, K is Congestion Window Multiplier
