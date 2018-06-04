package NestedCladeBuilder;

import java.util.ArrayList;
import java.util.Iterator;
import TCS.VGJ.graph.GMLobject;

public abstract class Clade{
	protected int id;
	protected String label;
	protected ArrayList edges = new ArrayList();
	protected int[] hapDistr;
	
	public Clade(int id_){
		id = id_;
	}
	
	public void setLabel(String lbl){
		label = lbl;
	}
	
	public String getLabel(){
		return label;
	}
	
	public void setEdge(Clade c){
		edges.add(c);
	}
	
	public int getID(){
		return id;
	}
	
	public boolean isTerminal(){
		return edges.size() <= 1; // If zero shouldn't be in Geodis input until final level at which stage should be tip anyway
	}
	
	public PsudoClade getCopy(){
		return new PsudoClade(this);
	}
	
	public boolean connectedTo(Clade c){
		return edges.contains(c);
	}
	
	public ArrayList getEdges(){
		return edges;
	}
	
	public abstract int getSampleSize();

	public String toString(){
		StringBuffer str = new StringBuffer("Clade: id=");
		str.append(id);
		str.append(" label=").append(label).append("\n");
		Clade c;
		str.append(" Connected to:");
		for(Iterator i = edges.iterator(); i.hasNext();){
			c = (Clade)i.next();
			str.append(" ").append(c.getID());
		}
		return str.toString();
	}
	
	public abstract String getStringDistribution();
	
	public abstract String getGeodisInput();
	
	public int[] getDistribution() throws NoPopulationDataException{
		if(hapDistr == null){
			throw new NoPopulationDataException("Haplotype distributions not set");
		}
		return hapDistr;
	}
	
	public abstract void setDistribution(int NumPop);
	
	public abstract int getAnalysisCladeCount();
	
	public int getNumberOfPopulations() throws NoPopulationDataException{
		int acc = 0;
		int[] dist = getDistribution();
		for( int i = 0; i < dist.length; i++){
			if(dist[i] != 0){
				acc++;
			}
		}
		return acc;
	}
	
	public boolean equals(Object o){
		if(o instanceof Clade){
			return getID()== ((Clade) o).getID();
		}
		return false;
	}
	
	public abstract GMLobject toGMLobject();
	
	public abstract ArrayList getListOfHaplotypes();
	
	public abstract ArrayList getNestedANOVAData();
	// returns the data used in Nested Anova
	
}