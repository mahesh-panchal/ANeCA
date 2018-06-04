package NestedCladeBuilder;

import java.util.ArrayList;
import java.util.Iterator;

public class PsudoClade{
	protected Clade master; // Needed in order to call back Clade info
	protected ArrayList edges = new ArrayList(); // Allows network to be pruned without affecting actual network
	
	public PsudoClade(Clade c){
		master = c;
		//for(Iterator i = c.getEdges().iterator();i.hasNext();){
		//	edges.add(((Clade)i.next()).getCopy());
		//}
	}

	public String toString(){
		return master.toString();
	}
	
	public int getID(){
		return master.getID();
	}
	
	public Clade getClade(){
		return master;
	}
	
	public int getSampleSize(){
		return master.getSampleSize();
	}
	
	public boolean isTerminal(){
		return edges.size() == 1;
	}
	
	public boolean isStranded(){
		return edges.size() == 0;
	}
	
	public boolean inLoop(){
		// Needs to return true if this clade is part of a loop
		// must check original connections
		// Ugly method is through breadth first search starting from each 
		// connecting clade until reach clade present in bfs from different starting point
		ArrayList connections = master.getEdges();
		if(connections.isEmpty()){
			return false;
		}
		ArrayList[] searchedNodes = new ArrayList[connections.size()];
		ArrayList[] searchFrom = new ArrayList[connections.size()];
		for(int i = 0; i < searchedNodes.length; i++){
			searchedNodes[i] = new ArrayList();
			searchedNodes[i].add(connections.get(i));
			searchFrom[i] = new ArrayList();
			searchFrom[i].add(connections.get(i));
		}
		Clade clade;
		int index;
		ArrayList temp;
		while(nodesToSearch(searchFrom)){
			for(int j = 0; j< searchFrom.length; j++){
				temp = new ArrayList();
				for(Iterator k = searchFrom[j].iterator();k.hasNext();){
					connections = ((Clade) k.next()).getEdges();
					for(Iterator l = connections.iterator();l.hasNext();){
						clade = (Clade) l.next();
						index = rowIndexContains(searchedNodes,clade);
						if(!(clade.equals(master)) &&  index != j){
							if(index == -1){
								searchedNodes[j].add(clade);
								temp.add(clade);
							} else {
								return true;
							}
						}
					}
				}
				searchFrom[j] = temp;
			}
		}
		return false;
	}
	
	public ArrayList getConnectedClades(){
		return edges;
	}
	
	public void addEdge(PsudoClade c){
		edges.add(c);
	}
	
	public void removeEdge(PsudoClade c){
		edges.remove(c);
	}
	
	public PsudoClade getConnectedClade() throws NotTerminalException{
		if(isTerminal()){
			return (PsudoClade) edges.get(0);
		} else {
			throw new NotTerminalException("This clade is not \"terminal\"");
		}
	}
	
	public boolean equals(Object o){
		if(o instanceof PsudoClade){
			return master.getID() == ((PsudoClade) o).getID();
		}
		return false; 
	}
	
	private boolean nodesToSearch(ArrayList[] al){
		for(int i = 0; i < al.length; i++){
			if(!al[i].isEmpty()){
				return true;
			}
		}
		return false;
	}
	
	private int rowIndexContains(ArrayList[] al, Clade c){
		for(int i = 0; i< al.length; i++){
			if(al[i].contains(c)){
				return i;
			}
		}
		return -1;
	}
	
}