package NestedCladeBuilder;

import java.util.ArrayList;
import java.util.Iterator;
import TCS.VGJ.graph.GMLobject;

public class NestedCladograph {
	private ArrayList nestedLevels;
	private int numberOfAmbiguousEdges;
	private int numberOfEdges;
	// Note: The position (index) of the nested level in the ArrayList
	//       must be the level of the NStepNetwork
	
	public NestedCladograph(){
		nestedLevels = new ArrayList();
		numberOfAmbiguousEdges = 0;
		numberOfEdges = 0;
	}
	
	public NestedCladograph(NStepNetwork nl){
		nestedLevels = new ArrayList();
		addNestingLevel(nl);
		numberOfAmbiguousEdges = 0;
		numberOfEdges = 0;
	}
	
	public void addNestingLevel(NStepNetwork nl){
		nestedLevels.add(nl.getStepNumber(),nl);
	}
	
	public NStepNetwork getNestingLevel(int i){
		return (NStepNetwork) nestedLevels.get(i);
	}
	
	public int getLevelsOfNesting(){
		return nestedLevels.size();
	}
	
	public void finaliseData(int numPop){
		((Clade)((NStepNetwork) nestedLevels.get(nestedLevels.size()-1)).get(0)).setDistribution(numPop);
	}
	
	public void setNumberOfEdges(int num){
		numberOfEdges = num;
	}
	
	public int getNumberOfEdges(){
		return numberOfEdges;
	}
	
	public void setNumberOfAmbiguousEdges(int num){
		numberOfAmbiguousEdges = num;
	}
	
	public int getNumberOfAmbiguousEdges(){
		return numberOfAmbiguousEdges;
	}
	
	public String toString(){
		StringBuffer str = new StringBuffer();
		str.append("Haplotype Network Properties:\n");
		str.append("Total Number of Edges: ").append(numberOfEdges).append("\n");
		str.append("Number of Ambiguous Edges: ").append(numberOfAmbiguousEdges).append("\n\n");
		for(Iterator i = nestedLevels.iterator();i.hasNext();){
			str.append(((NStepNetwork) i.next()).toString()).append("\n");
		}
		return str.toString();
	}
	
	public String getGeodisInput(){
		StringBuffer str = new StringBuffer();
		str.append(((Clade)((NStepNetwork) nestedLevels.get(nestedLevels.size()-1)).get(0)).getAnalysisCladeCount());
		str.append("\n");
		for(int i = 1; i < nestedLevels.size();i++){ // 1-step level up
			str.append(((NStepNetwork) nestedLevels.get(i)).getGeodisInput());
		}
		return str.toString();
	}
	
	public String getCladeDistributions(){
		StringBuffer str = new StringBuffer();
		for(int i = 1; i< nestedLevels.size();i++){
			str.append(((NStepNetwork) nestedLevels.get(i)).getCladeDistributions());
		}
		return str.toString();
	}
	
	public GMLobject toGMLobject(){
		GMLobject gmlo = new GMLobject(null,GMLobject.GMLfile);
		for(Iterator i = nestedLevels.iterator();i.hasNext(); ){
			gmlo.addObjectToEnd(((NStepNetwork) i.next()).toGMLobject());
		}
		return gmlo;
	}
	
	public String getNestedANOVAData(){
		StringBuffer str = new StringBuffer();
		ArrayList file = ((NStepNetwork) nestedLevels.get(nestedLevels.size()-1)).getNestedANOVAData();
		for(Iterator j = file.iterator(); j.hasNext();){
			str.append((StringBuffer) j.next()).append("\n");
		}
		return str.toString();
	}
}