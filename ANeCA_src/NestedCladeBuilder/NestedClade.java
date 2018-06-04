package NestedCladeBuilder;

import java.util.ArrayList;
import java.util.Iterator;
import TCS.VGJ.graph.GMLobject;

public class NestedClade extends Clade{
	private ArrayList subclades = new ArrayList();
	private ArrayList infoSubclades = new ArrayList();
	
	public NestedClade(int id_){
		super(id_);
	}
	
	public void addSubClade(Clade c){
		subclades.add(c);
	}
	
	public int getSampleSize(){
		int acc = 0;
		for(Iterator i = subclades.iterator();i.hasNext();){
			acc += ((Clade) i.next()).getSampleSize();
		}
		return acc;
	}
	
	ArrayList getSubClades(){
		return subclades;
	}
	
	public boolean hasSubClade(Clade c){
		return subclades.contains(c);
	}
	
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
		str.append("\n SubClades :");
		for(Iterator i = subclades.iterator(); i.hasNext();){
			c = (Clade)i.next();
			str.append(" ").append(c.getID());
		}
		return str.toString();
	}
	
	public String getStringDistribution(){
		StringBuffer str = new StringBuffer();
		try{
			if(infoSubclades.size() > 1){
				if(getNumberOfPopulations() > 1){
					String tag;
					int[] dtr;
					Clade c;
					str.append(label).append("\n");
					for(Iterator i = infoSubclades.iterator(); i.hasNext();){
						c = (Clade) i.next();
						tag = c.getLabel();
						tag = tag.replaceAll("\\s*Clade\\s+","").replaceAll("\"","");
						str.append(tag);
						dtr = c.getDistribution();
						for(int j = 0; j < dtr.length; j++){
							str.append("  ").append(dtr[j]);
						}
						str.append("\n");
					}
				}
			}
		} catch (NoPopulationDataException e) {
			str.append("Unable to get clade distribution.").append("\n");
		}
		return str.toString();
	}
	
	public String getGeodisInput(){ // Clade input
		StringBuffer str = new StringBuffer();
		String tag;
		try{
			if(infoSubclades.size() > 1){ // Have genetic variation
				if(getNumberOfPopulations() > 1){
					str.append(label).append("\n");					// 2.2 Line 1
					str.append(infoSubclades.size()).append("\n");		// 2.2 Line 2
					for(Iterator j = infoSubclades.iterator();j.hasNext();){ // 2.2 Line 3
						tag = ((Clade)j.next()).getLabel();
						tag = tag.replaceAll("\\s*Clade\\s+","").replaceAll("\"","");
						str.append(tag).append(" ");
					}
					str.append("\n");
					for(Iterator k = infoSubclades.iterator();k.hasNext();){ // 2.2 Line 4
						if(((Clade)k.next()).isTerminal()){
							str.append(1).append(" ");
						} else{
							str.append(0).append(" ");
						}
					}
					str.append("\n");
					str.append(getNumberOfPopulations()).append("\n");// 2.2 Line 5
					int[] index = new int[getNumberOfPopulations()];
					int[] distr = getDistribution();
					int z = 0;
					for(int k = 0; k < distr.length; k++){
						if(distr[k] > 0){
							index[z++] = k;
							str.append(k+1).append(" "); // 2.2 Line 6
						}
					}
					str.append("\n");
					for(Iterator k = infoSubclades.iterator();k.hasNext();){
						distr = ((Clade) k.next()).getDistribution();
						for(int i = 0; i< index.length;i++){
							str.append(distr[index[i]]).append(" "); // 2.2 Lines of observation matrix
						}
						str.append("\n");
					}
				}
			}
		} catch (NoPopulationDataException e){
			str.append("Unable to get clade distribution.").append("\n");
		}
		return str.toString();
	}
	
	public int getAnalysisCladeCount(){
		try{
			int acc = 0;
			for(Iterator i = subclades.iterator();i.hasNext();){
				acc += ((Clade) i.next()).getAnalysisCladeCount();
			}
			if(infoSubclades.size() > 1 && getNumberOfPopulations() > 1){
				acc++;
			}
			return acc;
		} catch (NoPopulationDataException e){
			return 0;
		}
	}
	
	public void setDistribution(int numPop){
		Clade c;
		for(Iterator k = subclades.iterator();k.hasNext();){
			c = (Clade) k.next();
			if(c.getSampleSize() > 0){
				infoSubclades.add(c); // Generate List of informative clades
			}
		}
		hapDistr = new int[numPop];
		for(int i = 0; i < numPop; i++){
			hapDistr[i] = 0;
		}
		int[] cDist;
		try{
			for(Iterator i = subclades.iterator(); i.hasNext();){
				c = (Clade) i.next();
				c.setDistribution(numPop);
				cDist = c.getDistribution();
				for(int j = 0; j < numPop; j++){
					hapDistr[j] += cDist[j];
				}
			}
		} catch (NoPopulationDataException e){
			System.out.println("Hey, no data after its been set!");
		}
	}
	
	public boolean equals(Object o){
		return super.equals(o);
	}
	
	public GMLobject toGMLobject(){
		GMLobject gmlo = new GMLobject("node",GMLobject.GMLlist);
		gmlo.setValue("id",GMLobject.GMLinteger,new Integer(getID()));
		gmlo.setValue("label",GMLobject.GMLstring,getLabel());
		GMLobject gmlodata = new GMLobject("data",GMLobject.GMLlist);
		for(Iterator i = subclades.iterator(); i.hasNext();){
			Clade c = (Clade) i.next();
			GMLobject gmldata = new GMLobject("Subclade",GMLobject.GMLlist);
			gmldata.setValue("id",GMLobject.GMLinteger,new Integer(c.getID()));
			if(c.getLabel().equals(HaplotypeClade.INFERRED_LABEL)){
				gmldata.setValue("label",GMLobject.GMLstring," ");
			} else {
				gmldata.setValue("label",GMLobject.GMLstring,c.getLabel());
			}
			gmlodata.addObjectToEnd(gmldata);
		}
		gmlo.addObjectToEnd(gmlodata);
		return gmlo;
	}
	
	public ArrayList getListOfHaplotypes(){
		ArrayList al = new ArrayList();
		for(Iterator i = subclades.iterator();i.hasNext();){
			al.addAll(((Clade) i.next()).getListOfHaplotypes());
		}
		return al;
	}
	
	public ArrayList getNestedANOVAData(){
		ArrayList data = new ArrayList();
		for(Iterator i = subclades.iterator();i.hasNext();){
			data.addAll(((Clade)i.next()).getNestedANOVAData());
		}
		for(Iterator i = data.iterator();i.hasNext();){
			((StringBuffer) i.next()).append(",\""+label.split("\\s+")[1]+"\"");
		}
		return data;
	}
			
}