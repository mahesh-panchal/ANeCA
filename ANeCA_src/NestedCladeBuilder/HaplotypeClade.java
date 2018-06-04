package NestedCladeBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import TCS.VGJ.graph.GMLobject;
import ANeCA.Mediator;
import javax.swing.JOptionPane;

public class HaplotypeClade extends Clade{
	public final static String INFERRED_LABEL = "\" \"";
	private String frequency;
	private String weight;
	
	public HaplotypeClade(int id_){
		super(id_);
	}
	
	public HaplotypeClade(int id_, String lbl){
		super(id_);
		label = lbl;
	}
	
	public HaplotypeClade(int id_, String lbl, String frq, String wght){
		super(id_);
		label = lbl;
		frequency = frq;
		weight = wght;
	}
	
	public void setData(String frq, String wght){
		frequency = frq;
		weight = wght;
	}
	
	public boolean isSample(){
		return !label.equals(INFERRED_LABEL);
	}
	
	public int getSampleSize(){
		if(isSample()){
			try{
				return Integer.parseInt(frequency.split("\\s+")[0].split("=")[1]);
			} catch (Exception e) { // Should be number format exception, but also null pointer exception to
				System.out.println("Hey, problem with calculating sample size");
				return 1;	
			}
		} else {
			return 0;
		}
	}
	
	public void setDistribution(int numPop){
		hapDistr = new int[numPop];
		for(int i=0; i < numPop; i++){
			hapDistr[i] = 0;
		}
		if(frequency != null){
			String[] distr = frequency.split("\\s+");
			for(int i = 1; i< distr.length; i++){
				try{
					hapDistr[Integer.parseInt(distr[i].split("\\.")[1])-1]++;
				} catch (NumberFormatException e){
					if(Mediator.isCommandLineApp()){
						System.out.println("The DNA sequence labels should match the regular expression [\\w]+\\.[\\d]+");
						System.out.println("Please see the documentation for further details");
						System.out.println("Exiting NCPA.");
					} else {
						JOptionPane.showMessageDialog(Mediator.getGui().getFrame(),
							"The DNA sequence labels have been written incorrectly.\n"+
							"Please refer to the documentation and include geographic locations.\n"+
							"Exiting NCPA.",
							"Error: DNA sequence labels incorrect.",
							JOptionPane.ERROR_MESSAGE);
					}
					System.exit(0);
				} catch (ArrayIndexOutOfBoundsException e){
					if(Mediator.isCommandLineApp()){
						System.out.println("The DNA sequence labels have been written incorrectly.");
						System.out.println("Please refer to the documentation and include geographic locations.");
						System.out.println("Exiting NCPA.");
					} else {
						JOptionPane.showMessageDialog(Mediator.getGui().getFrame(),
							"The DNA sequence labels have been written incorrectly.\n"+
							"Please refer to the documentation and include geographic locations.\n"+
							"Exiting NCPA.",
							"Error: DNA sequence labels incorrect.",
							JOptionPane.ERROR_MESSAGE);
					}
					System.exit(0);
				}
			}
		}
	}
		
	public String toString(){
		StringBuffer str = new StringBuffer("Clade: id=");
		str.append(id);
		str.append(" label=").append(label).append("\n");
		if(frequency != null){
			str.append(" ").append(frequency).append("\n");
		}
		if(weight != null){
			str.append(" ").append(weight).append("\n");
		}
		Clade c;
		str.append(" Connected to:");
		for(Iterator i = edges.iterator(); i.hasNext();){
			c = (Clade)i.next();
			str.append(" ").append(c.getID());
		}
		return str.toString();
	}
	
	public int getAnalysisCladeCount(){
		return 0;
	}
	
	public String getStringDistribution(){
		return "";
	}
	
	public String getGeodisInput(){
		return "";
	}
	
	public boolean equals(Object o){
		return super.equals(o);
	}
	
	public GMLobject toGMLobject(){
		GMLobject gmlo = new GMLobject("node",GMLobject.GMLlist);
		gmlo.setValue("id",GMLobject.GMLinteger,new Integer(getID()));
		if(getLabel().equals(INFERRED_LABEL)){
			gmlo.setValue("label",GMLobject.GMLstring," ");
		} else {
			gmlo.setValue("label",GMLobject.GMLstring,getLabel());
		}
		GMLobject gmldata = new GMLobject("data",GMLobject.GMLlist);
		gmldata.setValue("Frequency",GMLobject.GMLstring,frequency);
		gmldata.setValue("Weight",GMLobject.GMLstring,weight);
		gmlo.addObjectToEnd(gmldata);
		return gmlo;
	}
	
	public ArrayList getListOfHaplotypes(){
		ArrayList al = new ArrayList();
		al.add(this);
		return al;
	}

	public ArrayList getNestedANOVAData(){
		ArrayList data = new ArrayList();
		if (frequency != null){
			String[] freq = frequency.split("\\s+");
			for(int i = 1; i < freq.length; i++){
				data.add(new StringBuffer("\""+freq[i]+"\",\""+label+"\""));
			}
		}
		return data;
	}
		
}