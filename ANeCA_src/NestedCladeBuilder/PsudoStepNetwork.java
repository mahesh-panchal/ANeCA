package NestedCladeBuilder;

import java.util.ArrayList;
import java.util.Iterator;

public class PsudoStepNetwork extends ArrayList{ 
	private int stepNo;
	
	public PsudoStepNetwork(NStepNetwork n){
		stepNo = n.getStepNumber();
		for(Iterator i = n.iterator(); i.hasNext();){
			add(((Clade) i.next()).getCopy());
		}
		// want autolink method below
		PsudoClade source,target;
		for(int j = 0; j < size();j++){
			source = (PsudoClade) get(j);
			for(int k = 0; k < size(); k++){
				if(j != k){
					target = (PsudoClade) get(k);
					if(source.getClade().getEdges().contains(target.getClade())){
						source.addEdge(target);
					}
				} 
			}
		}
	}
	
	public int getStepNumber(){
		return stepNo;
	}
	
	public PsudoClade getClade(int n){
		PsudoClade clade;
		for(Iterator i = iterator();i.hasNext();){
			clade = (PsudoClade)i.next();
			if(clade.getID() == n){ 
				return clade;
			}
		}
		return null;
	}
	
	public boolean remove(Object o){
		for(Iterator i = ((PsudoClade) o).getConnectedClades().iterator(); i.hasNext();){
			((PsudoClade) i.next()).removeEdge((PsudoClade) o);
		}
		return super.remove(o);
	}
	
	public String toString(){
		StringBuffer str = new StringBuffer();
		PsudoClade c;
		for(Iterator i = iterator();i.hasNext();){
			c = (PsudoClade) i.next();
			str.append(c.toString()).append("\n");
		}
		return str.toString();
	}
		
}