import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;


@SuppressWarnings("serial")
public class UtilisationPlot extends JFrame {
	
    public UtilisationPlot() throws IOException {
        super("Packet Gen Rate VS Utilisation Plot : Slotted Aloha");
 
        JPanel chartPanel = createChartPanel();
        add(chartPanel, BorderLayout.CENTER);
 
        setSize(640, 480);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }
 
    private JPanel createChartPanel() throws IOException {
        // creates a line chart object
        // returns the chart panel
    	String chartTitle = "Packet Gen Rate VS Utilisation";
        String xAxisLabel = "Packet Gen Rate";
        String yAxisLabel = "Utilisation";
     
        XYDataset dataset = createDataset();
     
        boolean showLegend = true;
        boolean createURL = false;
        boolean createTooltip = false;
         
        JFreeChart chart = ChartFactory.createXYLineChart(chartTitle,xAxisLabel, yAxisLabel, dataset,PlotOrientation.VERTICAL, showLegend, createTooltip, createURL);
        
        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
	        
	     	      
	     // sets thickness for series (using strokes)
	     renderer.setSeriesStroke(0, new BasicStroke(2.0f));
	     renderer.setSeriesStroke(1, new BasicStroke(2.0f));
	     
	      
	     plot.setRenderer(renderer);
	     
     
        return new ChartPanel(chart);
    	
    }
 
    private XYDataset createDataset() throws IOException {
    	
    	HashMap<Integer,ArrayList<myXYObj>> plotInputsHashMap = new HashMap<Integer,ArrayList<myXYObj>>();
    	GenPlotInputs(plotInputsHashMap);    	
    	
        // creates an XY dataset...
        // returns the dataset
    	        
        Set<Integer> keysIntegers = plotInputsHashMap.keySet();
        Object[] keys = keysIntegers.toArray();
        int key1 = (int) keys[0];
        int key2 = (int) keys[1];
        if(key1>key2){
        	int temp = key1;
        	key1 = key2;
        	key2 = temp;
        }
        
        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries series1 = new XYSeries("W = "+key1);
        XYSeries series2 = new XYSeries("W = "+key2);
        
        ArrayList<myXYObj> arrayList1 = plotInputsHashMap.get(key1);
        for(myXYObj xyObj : arrayList1){
        	//System.out.println("Series1:"+ xyObj.pkt_gen_rate+" "+xyObj.utilisation);
        	series1.add(xyObj.pkt_gen_rate,xyObj.utilisation);
        }
     
        ArrayList<myXYObj> arrayList2 = plotInputsHashMap.get(key2);
        for(myXYObj xyObj : arrayList2){
        	//System.out.println("Series2:"+ xyObj.pkt_gen_rate+" "+xyObj.utilisation);
        	series2.add(xyObj.pkt_gen_rate,xyObj.utilisation);
        }
     
        dataset.addSeries(series1);
        dataset.addSeries(series2);
     
        return dataset;
    }
    
    public void GenPlotInputs(HashMap<Integer,ArrayList<myXYObj>> plotInputsHashMap) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader("output.txt"));
		int W=0;
		double pkt_gen_rate=0;
		try {
		    String line = br.readLine();
		    //System.out.println(line);
		    if(line.contains("Collision Window Size")){
		    	String[] vals = line.split(": ");
		    	W = Integer.parseInt(vals[vals.length-1]);
		    	if(!plotInputsHashMap.containsKey(W)){
		    		plotInputsHashMap.put(W,new ArrayList<myXYObj>());
		    	}
		    }
		    else if(line.contains("Probability Of Packet Generation")){
		    	String[] vals = line.split(": ");
		    	pkt_gen_rate = Double.parseDouble(vals[vals.length-1]);
		    }
		    else if(line.contains("Utilisation")){
		    	String[] vals = line.split(": ");
		    	 double utilisation = Double.parseDouble(vals[vals.length-1]);
		    	 ArrayList<myXYObj> arrayList = plotInputsHashMap.get(W);
		    	arrayList.add(new myXYObj(pkt_gen_rate, utilisation));
		    	//System.out.println(W+" "+ pkt_gen_rate+" "+utilisation);
		    	
		    }
		    
		    while (line != null) {
		        line = br.readLine();
		        if(line==null){
		        	break;
		        }
		        //System.out.println(line);
		        if(line.contains("Collision Window Size")){
			    	String[] vals = line.split(": ");
			    	W = Integer.parseInt(vals[vals.length-1]);
			    	if(!plotInputsHashMap.containsKey(W)){
			    		plotInputsHashMap.put(W,new ArrayList<myXYObj>());
			    	}			    	
			    }
			    else if(line.contains("Probability Of Packet Generation")){
			    	String[] vals = line.split(": ");
			    	pkt_gen_rate = Double.parseDouble(vals[vals.length-1]);
			    }
			    else if(line.contains("Utilisation")){
			    	String[] vals = line.split(": ");
			    	 double utilisation = Double.parseDouble(vals[vals.length-1]);
			    	 ArrayList<myXYObj> arrayList = plotInputsHashMap.get(W);
			    	arrayList.add(new myXYObj(pkt_gen_rate, utilisation));
			    	//System.out.println(W+" "+ pkt_gen_rate+" "+utilisation);
			    }
		    }
		 
		} 
		finally {
		    br.close();
		 
		    File file = new File("output.txt");
		    File file2 = new File("result.txt");
		    if (file2.exists()){
		    	file2.delete();
		    	file2 = new File("result.txt");
		    }
		       
		    boolean success = file.renameTo(file2);
		    if (!success) {
		    	file.delete();
		    }
		}

    }
    
    public static void main(String[] args) throws IOException {
    	
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
					new UtilisationPlot().setVisible(true);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        });
       
    }
}

class myXYObj{
	double pkt_gen_rate;
	double utilisation;
	
	public myXYObj(double pkt_gen_rate, double utilisation) {
		this.pkt_gen_rate = pkt_gen_rate;
		this.utilisation = utilisation;
	}
	
}