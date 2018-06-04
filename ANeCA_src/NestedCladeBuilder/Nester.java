package NestedCladeBuilder;

import java.util.ArrayList;
import java.util.Iterator;

public class Nester{
	
	public static final String TOP_LEVEL_LABEL = "Total Cladogram";
		
	private Nester(){
	}
	
	public static NestedCladograph run(NStepNetwork hapNet){
		NestedCladograph nestedTree = new NestedCladograph(hapNet);
		int stepLevel = 1;
		PsudoStepNetwork n_step_level = hapNet.getCopy(); // Create copy of network to allow pruning
		NStepNetwork nplus1_step_level = new NStepNetwork(stepLevel++); // Create n+1 step network.
		int nodeID = hapNet.size();
		ArrayList grouped, ambEdges;
		PsudoClade node;
		boolean loopCondition = true;
		boolean nestAmbLink = false;
		int nestCount; // boolean condition for loop presence. i.e. if 0 have no tips and loops present.
		int cladeCount = 0;
		int unambLinkCount;
		setNestedCladographProperties(nestedTree,n_step_level);
		while(loopCondition){
			cladeCount=0;
			ambEdges = getAmbiguousLinkages(n_step_level);
			unambLinkCount = 1;
			while(n_step_level.size() > 0){
				grouped = new ArrayList();
				nestCount = 0;
				nestAmbLink = (unambLinkCount == 0);
				unambLinkCount = 0;
				for(Iterator i = n_step_level.iterator();i.hasNext();){
					node = (PsudoClade) i.next();
					if(node.isTerminal() && !grouped.contains(node)){
						nestCount++;
						try {
							Edge link = new Edge(node,node.getConnectedClade());
							if(!ambEdges.contains(link) || nestAmbLink){
								ArrayList connections = node.getConnectedClade().getConnectedClades();
								NestedClade nc = new NestedClade(nodeID++);
								nc.setLabel("Clade "+(stepLevel-1)+"-"+(++cladeCount));
								PsudoClade term;
								for(Iterator j = connections.iterator(); j.hasNext();){
									term = (PsudoClade) j.next();
									link = new Edge(term,node.getConnectedClade());
									if (term.isTerminal() && (!ambEdges.contains(link) || nestAmbLink)){
										grouped.add(term);
										nc.addSubClade(term.getClade());
									}
								}
								grouped.add(node.getConnectedClade());
								nc.addSubClade(node.getConnectedClade().getClade());
								nplus1_step_level.add(nc);
								unambLinkCount++; // Force count reset for ambiguous link i.e. check for unambiguous after ambiguous
							}
						} catch (NotTerminalException e) {
							System.out.println("Will never happen");
						}
					} else if(node.isStranded() && !grouped.contains(node)){
						nestCount++;
						// nest all strandeds in this step with randomisation in case of two nodes with same sample size
						ArrayList stranded = getStrandedClades(n_step_level);
						// stranded is sorted by greatest sample size
						while(!stranded.isEmpty()){
							// Line below: gets a random Psudoclade with sample size same as first in list;
							node = (PsudoClade) stranded.get((int)(Math.random()*getSampleSizeRange(stranded)));
							if(node.inLoop()){
								NestedClade nc = new NestedClade(nodeID++);
								nc.setLabel("Clade "+(stepLevel-1)+"-"+(++cladeCount));
								nc.addSubClade(node.getClade());
								nplus1_step_level.add(nc);
							} else {
								ArrayList realConnections = node.getClade().getEdges();
								NestedClade higherClade;
								NestedClade groupingClade = new NestedClade(nodeID++);
								Clade connection;
								int lSampleSize = Integer.MAX_VALUE;
								int size;
								ArrayList lowestSizeSamples = new ArrayList();
								for(Iterator m = nplus1_step_level.iterator(); m.hasNext();){
									higherClade = (NestedClade) m.next();
									size = higherClade.getSampleSize();
									for(Iterator n = realConnections.iterator();n.hasNext();){
										connection = (Clade) n.next();
										if(higherClade.hasSubClade(connection) && lSampleSize > size){
											lowestSizeSamples = new ArrayList();
											lowestSizeSamples.add(higherClade);
											lSampleSize = size;
										} else if(higherClade.hasSubClade(connection) && lSampleSize == size){
											lowestSizeSamples.add(higherClade);
										}
									}
								}
								if(lowestSizeSamples.isEmpty()){
									nplus1_step_level.add(groupingClade);
									groupingClade.setLabel("Clade "+(stepLevel-1)+"-"+(++cladeCount));
								} else {
									groupingClade = (NestedClade) lowestSizeSamples.get((int)(Math.random()*lowestSizeSamples.size()));
									nodeID--;
								}
								groupingClade.addSubClade(node.getClade());
							}
							stranded.remove(node);
							grouped.add(node);
						}
					}
				}
				if(nestCount == 0){
					ArrayList loopEdges = getAmbiguousLinkages(n_step_level);
					Edge edge;
					// sort loop edges.
					ArrayList sortedEdges = new ArrayList();
					int matches = 0;
					while(!loopEdges.isEmpty()){
						if(matches == 0){
							sortedEdges.add(loopEdges.remove(0));
						}
						matches = 0;
						for(int i = 0; i < sortedEdges.size(); i++){
							int j = 0;
							while(j < loopEdges.size()){
								if(((Edge) sortedEdges.get(i)).incidentTo(((Edge) loopEdges.get(j)).getPsudoClade1())
									|| ((Edge) sortedEdges.get(i)).incidentTo(((Edge) loopEdges.get(j)).getPsudoClade2())){
									sortedEdges.add(loopEdges.remove(j));
									matches++;
								} else {
									j++;
								}
							}
						}
					}
					for(Iterator s = sortedEdges.iterator();s.hasNext();){
						edge = (Edge) s.next();
						if(!edge.addToExistingClades(nplus1_step_level)){
							NestedClade nc = new NestedClade(nodeID++);
							nc.setLabel("Clade "+(stepLevel-1)+"-"+(++cladeCount));
							nc.addSubClade(edge.getPsudoClade1().getClade());
							nc.addSubClade(edge.getPsudoClade2().getClade());
							nplus1_step_level.add(nc);
						}
						if(!grouped.contains(edge.getPsudoClade1())){
							grouped.add(edge.getPsudoClade1());
						}
						if(!grouped.contains(edge.getPsudoClade2())){
							grouped.add(edge.getPsudoClade2());
						}
					}
				}
				for (Iterator k = grouped.iterator(); k.hasNext();){
					n_step_level.remove(k.next());
				}
			}
			nestedTree.addNestingLevel(nplus1_step_level);
			if(nplus1_step_level.size() == 1){
				loopCondition = false;
				((Clade) nplus1_step_level.get(0)).setLabel(TOP_LEVEL_LABEL);
			} else {
				try{
					if(nplus1_step_level.autoLink()){
						// The networks are not disjoint! and have been connected.
						n_step_level = nplus1_step_level.getCopy();
						nplus1_step_level = new NStepNetwork(stepLevel++);
					} else {
						// The networks are disjoint!
						NStepNetwork nsn = new NStepNetwork(stepLevel++); // Total Cladogram Level
						NestedClade totnc =  new NestedClade(nodeID++); // Total Cladogram Clade
						totnc.setLabel("Total Cladogram");
						for(Iterator pointer = nplus1_step_level.iterator(); pointer.hasNext();){
							totnc.addSubClade((Clade) pointer.next());
						}
						nsn.add(totnc);
						nestedTree.addNestingLevel(nsn);
						loopCondition = false;
					}
				} catch (UnlinkableException e){
					System.out.println("Unlinkable");
					System.out.println(e.getMessage());
					System.exit(0); // Cannot continue
				}
			}
		}
		return nestedTree;	
	}
	
	private static ArrayList getAmbiguousLinkages(PsudoStepNetwork level){
		ArrayList visitedEdges = new ArrayList(); 
		ArrayList pathEdges = new ArrayList(); 
		ArrayList loopEdges = new ArrayList(); 
		for(Iterator r = level.iterator(); r.hasNext();){
			findLoopEdges(visitedEdges,pathEdges,loopEdges,(PsudoClade) r.next());
		}
		return loopEdges;
	}

	private static void setNestedCladographProperties(NestedCladograph ncg, PsudoStepNetwork level){
		ArrayList visitedEdges = new ArrayList(); 
		ArrayList pathEdges = new ArrayList(); 
		ArrayList loopEdges = new ArrayList(); 
		for(Iterator r = level.iterator(); r.hasNext();){
			findLoopEdges(visitedEdges,pathEdges,loopEdges,(PsudoClade) r.next());
		}
		ncg.setNumberOfEdges(visitedEdges.size());
		ncg.setNumberOfAmbiguousEdges(loopEdges.size());
	}
	
	private static void findLoopEdges(ArrayList visited, ArrayList path, ArrayList loopEdges, PsudoClade parent){
		PsudoClade vertex;
		Edge e; 
		for(Iterator i = parent.getConnectedClades().iterator();i.hasNext();){
			vertex = (PsudoClade) i.next();
			e = new Edge(parent,vertex);
			if(!visited.contains(e)){
				visited.add(e);
				path.add(e);
				if(path.size() > 2){ // have n-cycle (n>=3)
					int dec = path.size()-3; 
					boolean notHaveIndex = true;
					Edge edge;
					while(notHaveIndex){
						edge = (Edge) path.get(dec);
						if(edge.incidentTo(vertex)){
							for(int j = dec; j < path.size();j++){
								edge = (Edge) path.get(j);
								if(!loopEdges.contains(edge)){
									loopEdges.add(edge);
								}
							}
							notHaveIndex = false;
						} else {
							dec--;
							if(dec < 0 ){
								notHaveIndex = false;
							}
						}
					}
					if(dec < 0){
						findLoopEdges(visited,path,loopEdges,vertex);
					}
				} else { // only one or two edges
					findLoopEdges(visited,path,loopEdges,vertex);
				}
				path.remove(e);
			}
		}
	}	
	
	private static ArrayList getStrandedClades(PsudoStepNetwork psn){
		// returns a sorted list of stranded clades at this stage of nesting.
		ArrayList stranded = new ArrayList();
		for(int i=0; i < psn.size();i++){ // int loop used since effect of new Iterator unknown
			if(((PsudoClade) psn.get(i)).isStranded()){
				stranded.add(psn.get(i));
			}
		}
		stranded = sortBySampleSize(stranded);
		return stranded; // will return list with at least one element	
	}
	
	private static ArrayList sortBySampleSize(ArrayList al){
		// will sort by sample size so that stranded clades with greatest sample size are first
		// insertion sort - still bad though
		for(int i = 0; i < al.size();i++){
			for(int j = i+1 ; j<al.size();j++){
				if(((PsudoClade) al.get(i)).getSampleSize() < ((PsudoClade) al.get(j)).getSampleSize()){
					al.add(i,al.remove(j)); 
				}
			}
		}
		return al;
	}
	
	private static int getSampleSizeRange(ArrayList al){
		// pre:: al sorted by greatest sample size
		// index+1 of last element with same sample size
		int sSize = ((PsudoClade) al.get(0)).getSampleSize();
		for(int i = 1; i < al.size(); i++){
			if(((PsudoClade) al.get(i)).getSampleSize() < sSize){
				return i;
			}
		}
		return -1; // should never happen - so throw an error dufus!
	}
}