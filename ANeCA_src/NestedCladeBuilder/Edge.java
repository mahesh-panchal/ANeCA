package NestedCladeBuilder;

import java.util.ArrayList;
import java.util.Iterator;

class Edge{
	private PsudoClade v1,v2;
	
	public Edge(PsudoClade p1, PsudoClade p2){
		v1 = p1;
		v2 = p2;
	}
	
	public boolean incidentTo(PsudoClade p){
		return p.getID() == v1.getID() || p.getID() == v2.getID();
	}
	
	public boolean equals(Object o){
		if (o instanceof Edge){
			Edge e = (Edge) o;
			return (v1.getID() == e.v1.getID() && v2.getID() == e.v2.getID())
				|| (v1.getID() == e.v2.getID() && v2.getID() == e.v1.getID());
		}
		return false;
	}
	
	public PsudoClade getPsudoClade1(){
		return v1;
	}
	
	public PsudoClade getPsudoClade2(){
		return v2;
	}
	
	public boolean addToExistingClades(ArrayList nestedCladeList){
		NestedClade nc;
		ArrayList temp;
		for(Iterator i = nestedCladeList.iterator();i.hasNext();){
			nc = (NestedClade) i.next();
			if(nc.hasSubClade(v1.getClade())){
				if(!nc.hasSubClade(v2.getClade())){
					nc.addSubClade(v2.getClade());
				}
				return true;
			} else {
				if(nc.hasSubClade(v2.getClade())){
					nc.addSubClade(v1.getClade());
					return true;
				} 
			}
		}
		return false;
	}
	
}
