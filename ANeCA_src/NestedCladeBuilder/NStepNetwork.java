package NestedCladeBuilder;

import java.util.ArrayList;
import java.util.Iterator;
import TCS.VGJ.graph.GMLobject;

public class NStepNetwork extends ArrayList{ 
	private int stepNo;
	
	public NStepNetwork(int n){
		stepNo = n;
	}
	
	public int getStepNumber(){
		return stepNo;
	}
	
	public Clade getClade(int n){
		Clade clade;
		for(Iterator i = iterator();i.hasNext();){
			clade = (Clade)i.next();
			if(clade.getID() == n){ 
				return clade;
			}
		}
		return null;
	}
	
	public boolean autoLink() throws UnlinkableException{
		boolean linkMade = false;
		for(Iterator k = iterator();k.hasNext();){
			if(!(k.next() instanceof NestedClade)){
				throw new UnlinkableException("Have haplotypes here");
			}
		}
		int length = size(); 
		NestedClade linker;
		NestedClade linkee;
		for(int i = 0; i< length;i++){
			linker = (NestedClade) get(i);
			for(int j = 0; j< length; j++){
				linkee = (NestedClade) get(j);
				if(i != j){
					ArrayList links = linker.getSubClades();
					for(Iterator k = links.iterator();k.hasNext();){
						for(Iterator l = ((Clade) k.next()).getEdges().iterator();l.hasNext();){
							if(linkee.hasSubClade((Clade) l.next()) && !linker.connectedTo(linkee)){
								linker.setEdge(linkee);
								linkMade = true;
							}
						}	
					}
				}
			}
		}
		return linkMade; 
	}
	
	public String toString(){
		StringBuffer str = new StringBuffer();
		str.append("Nesting Level: ").append(stepNo).append("\n");
		Clade c;
		for(Iterator i = iterator();i.hasNext();){
			c = (Clade) i.next();
			str.append(c.toString()).append("\n");
		}
		return str.toString();
	}
	
	public String getGeodisInput(){
		StringBuffer str = new StringBuffer();
		for(Iterator i = iterator(); i.hasNext();){
			str.append(((Clade) i.next()).getGeodisInput());
		}
		return str.toString();
	}
	
	public String getCladeDistributions(){
		StringBuffer str = new StringBuffer();
		for(Iterator i = iterator();i.hasNext();){
			str.append(((Clade) i.next()).getStringDistribution());
		}
		return str.toString();
	}
	
	public PsudoStepNetwork getCopy(){
		return new PsudoStepNetwork(this);
	}
	
	public GMLobject toGMLobject(){
		GMLobject gmlObject = new GMLobject("graph",GMLobject.GMLlist);
		gmlObject.setValue("Level",GMLobject.GMLinteger,new Integer(stepNo));
		for(Iterator i = iterator();i.hasNext();){
			Clade c = (Clade) i.next();
			gmlObject.addObjectToEnd(c.toGMLobject());
			// need to add the edges too.
			ArrayList edges = c.getEdges();
			for(Iterator j = edges.iterator();j.hasNext();){ // creates duplicate edges // can be replaced later with edge traversing algorithm to write edges.
				GMLobject gmlo = new GMLobject("edge",GMLobject.GMLlist);
				Clade conTo = (Clade) j.next();
				gmlo.setValue("source",GMLobject.GMLinteger,new Integer(c.getID()));
				gmlo.setValue("target",GMLobject.GMLinteger,new Integer(conTo.getID()));
				gmlObject.addObjectToEnd(gmlo);
			}
		}
		return gmlObject;
	}
	
	public ArrayList getNestedANOVAData(){
		ArrayList data = new ArrayList();
		StringBuffer str = new StringBuffer("\"Individuals\",");
		for(int j = 0; j < stepNo; j++){
			str.append("\"").append(j).append("-step\",");
		}
		str.append("\"Total Cladogram\"");
		data.add(str);
		for(Iterator i = iterator(); i.hasNext();){
			data.addAll(((Clade)i.next()).getNestedANOVAData());
		}
		return data;
	}
}