
import java.util.*;

public class CladeGroup extends ArrayList{

	public static final String RGF = "RGF";
	public static final String RGF_IBD = "RGF_IBD";
	public static final String RGF_LDD = "RGF_LDD";
	public static final String AF = "AF";
	public static final String CRE = "CRE";
	public static final String LDC = "LDC";
	public static final String FRAG = "FRAG";
	public static final String EXPAN = "EXPAN";
	private String type;

	//Contains FinalisedCladeData.
	private ArrayList gStats = new ArrayList();

	public CladeGroup(String typeOfGroup){
		type = typeOfGroup;
	}

	public String type(){
		return type;
	}

	public void addClade(FinalisedCladeData fcd){
		add(fcd);
		fcd.setCladeGroup(this);
	}

	public CladeGroup merge(CladeGroup cg){
		for(Iterator i = cg.iterator();i.hasNext();){
			FinalisedCladeData fcd = (FinalisedCladeData) i.next();
			if(!contains(fcd)){
				addClade(fcd);
			}
		}
		return this;
	}

	public void calculateGStatistics(){
		// calculate the G statistic. Do this for all combinations of clades within the group.
		// According to Templeton paper, no reason RGF should be in same time frame.
		// Only geographic concordence is needed, and then use a formula to calculate T.
		// Other inferences use G-Statistic to determine whether any of the clades mark the
		// same event.
		ArrayList cladesToInclude = new ArrayList();
		for (Iterator i = iterator();i.hasNext();){
			FinalisedCladeData fcd = (FinalisedCladeData)i.next();
			if(fcd.cladeTLSEstimate() != Double.MAX_VALUE && fcd.cladeTLSEstimate() != 0){
				cladesToInclude.add(fcd);
			}
		}// Exclude clades with only significant I-T's or single haplotypes.
		if(cladesToInclude.size() > 1){
			calculateGStats(cladesToInclude);
		}
	}

	private void calculateGStats(ArrayList listOfClades){
		int perm = (int) Math.pow(2,listOfClades.size());
		for(int i = (perm-1); i > 0; i--){ // Go from largest to smallest
			ArrayList tempArray = new ArrayList();
			String binary = Integer.toBinaryString(i);
			for(int j = 0; j < binary.length(); j++){
				if(binary.charAt(binary.length()-1-j) == '1'){
					tempArray.add(listOfClades.get(listOfClades.size()-1-j));
				}
			}
			if(tempArray.size() > 1){
				if((type.equals(RGF) || type.equals(RGF_IBD) || type.equals(RGF_LDD)) && !isSingleLocus(tempArray)){
					// Is RGF, RGF_IBD, RGF_LDD and not all from same locus.
					// Keep the largest subset.
					ArrayList toRemove = new ArrayList();
					boolean add = true;
					for(Iterator k = gStats.iterator(); k.hasNext();){
						GStatistic addedGStat = (GStatistic) k.next();
						if(addedGStat.clades().containsAll(tempArray)){
							add = false;
						} else if(tempArray.containsAll(addedGStat.clades())){
							toRemove.add(addedGStat);
						}
					}
					if(add){
						gStats.add(new GStatistic(tempArray));
					}
					gStats.removeAll(toRemove);
				//} else if ((type.equals(AF) || type.equals(CRE) || type.equals(LDC)) && hasUniqueLoci(tempArray)) {
				} else if ((type.equals(AF) || type.equals(CRE) || type.equals(LDC)) && !isSingleLocus(tempArray)) {
					// Is an event and not all events are from a single locus.
					if(type.equals(AF)){
						// check intersection of populations in Isolates are 75% of union of populations
						if(isolatesMatch(tempArray)){
							// Keep the largest non-significant subset(s).
							GStatistic gs = new GStatistic(tempArray);
							if(!gs.isSignificantUpperTail()){
								ArrayList toRemove = new ArrayList();
								boolean add = true;
								for(Iterator k = gStats.iterator(); k.hasNext();){
									GStatistic addedGStat = (GStatistic) k.next();
									if(addedGStat.clades().containsAll(tempArray)){
										add = false;
									} else if(tempArray.containsAll(addedGStat.clades())){
										toRemove.add(addedGStat);
									}
								}
								if(add){
									gStats.add(new GStatistic(tempArray));
								}
								gStats.removeAll(toRemove);
							}
						}
					} else if (type.equals(CRE) || type.equals(LDC)){
						// IF is CRE/LDC, check intersection of populations of Interiors are 75% of union of populations,
						// and check intersection of populations of Tips are 75% of union of populations,
						if(interiorTipConcordence(tempArray)){
							// Keep the largest non-significant subset(s).
							GStatistic gs = new GStatistic(tempArray);
							if(!gs.isSignificantUpperTail()){
								ArrayList toRemove = new ArrayList();
								boolean add = true;
								for(Iterator k = gStats.iterator(); k.hasNext();){
									GStatistic addedGStat = (GStatistic) k.next();
									if(addedGStat.clades().containsAll(tempArray)){
										add = false;
									} else if(tempArray.containsAll(addedGStat.clades())){
										toRemove.add(addedGStat);
									}
								}
								if(add){
									gStats.add(new GStatistic(tempArray));
								}
								gStats.removeAll(toRemove);
							}
						}
					}
				}
			}
		}
	}

	private boolean isolatesMatch(ArrayList listOfClades){
		// Confirms the presence of at least 1 isolate.
		ArrayList isolates = new ArrayList(); // list of isolates to compare against.
		for(Iterator i = listOfClades.iterator(); i.hasNext();){
			FinalisedCladeData fcd = (FinalisedCladeData) i.next();
			if(fcd.getInference().contains("1")){
				//passes through the 1-19 NO path
				if(isolates.isEmpty()){
					// add all isolates to the list.
					ArrayList subclades = fcd.getSubclades();
					for(Iterator j = subclades.iterator(); j.hasNext();){
						FinalisedSubcladeData fsd = (FinalisedSubcladeData) j.next();
						if(!fsd.getCladeLabel().contains("I-T")){
							String[] dist = fsd.getDistribution().trim().split("\\s+");
							HashSet isolateIndex = new HashSet();
							for(int k = 0 ; k < dist.length; k++){
								if(Integer.parseInt(dist[k]) > 0){
									isolateIndex.add(new Integer(k));
								}
							}
							isolates.add(isolateIndex);
						}
					}
				} else {
					// Check if all isolates match with first group.
					// If all match, but extra isolates specified then add
					ArrayList subclades = fcd.getSubclades();
					ArrayList potentialIsolates = new ArrayList();
					int matches = 0;
					for(Iterator j = subclades.iterator(); j.hasNext();){
						FinalisedSubcladeData fsd = (FinalisedSubcladeData) j.next();
						if(!fsd.getCladeLabel().contains("I-T")){
							String[] dist = fsd.getDistribution().trim().split("\\s+");
							HashSet isolateIndex = new HashSet();
							for(int k = 0 ; k < dist.length; k++){
								if(Integer.parseInt(dist[k]) > 0){
									isolateIndex.add(new Integer(k));
								}
							}
							boolean mismatch = true;
							boolean noIntersect = true;
							for(Iterator k = isolates.iterator();k.hasNext() && mismatch;){
								HashSet isoInd = (HashSet) k.next();
								HashSet intersection = (HashSet) isoInd.clone();
								intersection.retainAll(isolateIndex);
								int intersectionCardinality = intersection.size();
								if(intersectionCardinality > 0){
									noIntersect = false;
									HashSet union = (HashSet)isoInd.clone();
									union.addAll(isolateIndex);
									int unionCardinality = union.size();
									if(intersectionCardinality/unionCardinality >= 0.75){
										matches++;
										mismatch = false;
									} else {
										// at least one subclade has not enough populations in common.
										return false;
									}
								}
							}
							if(noIntersect){
								potentialIsolates.add(isolateIndex);
							}
						}
					}
					if(matches == isolates.size()){
						isolates.addAll(potentialIsolates);
					}
				}
			}
		}
		// If multiple isolates exist, they will now be in isolates.
		for(Iterator i = listOfClades.iterator(); i.hasNext();){
			FinalisedCladeData fcd = (FinalisedCladeData) i.next();
			HashSet theisolate = new HashSet();
			if(fcd.getInference().contains("4")){
				// Check isolate grouped by Dc, Dn.
				if(isolates.isEmpty()){
					for(Iterator j = fcd.getSubclades().iterator();j.hasNext();){
						FinalisedSubcladeData fsd = (FinalisedSubcladeData) j.next();
						if(!fsd.getCladeLabel().contains("I-T")){
							if(fsd.significantlySmallDc() || fsd.significantlySmallDn()){
								String[] dist = fsd.getDistribution().trim().split("\\s+");
								for(int k = 0 ; k < dist.length; k++){
									if(Integer.parseInt(dist[k]) > 0){
										if(!theisolate.contains(new Integer(k))){
											theisolate.add(new Integer(k));
										}
									}
								}
							}
						}
					}
					isolates.add(theisolate);
				} else {
					for(Iterator j = fcd.getSubclades().iterator();j.hasNext();){
						FinalisedSubcladeData fsd = (FinalisedSubcladeData) j.next();
						if(!fsd.getCladeLabel().contains("I-T")){
							if(fsd.significantlySmallDc() || fsd.significantlySmallDn()){
								String[] dist = fsd.getDistribution().trim().split("\\s+");
								for(int k = 0 ; k < dist.length; k++){
									if(Integer.parseInt(dist[k]) > 0){
										theisolate.add(new Integer(k));
									}
								}
							}
						}
					}
					boolean nomatch = true;
					for(Iterator j = isolates.iterator();j.hasNext();){
						HashSet iso = (HashSet) j.next();
						HashSet intersection = (HashSet)iso.clone();
						intersection.retainAll(theisolate);
						int cardinality = intersection.size();
						HashSet union = (HashSet)iso.clone();
						union.addAll(theisolate);
						int unionCard = union.size();
						if(cardinality/unionCard >= 0.75){
							nomatch = false;
						}
					}
					if(nomatch){
						return false;
					}
				}
			} else if (fcd.getInference().contains("5")) {
				// Check isolate grouped by Dc, Dn.
				if(isolates.isEmpty()){
					for(Iterator j = fcd.getSubclades().iterator();j.hasNext();){
						FinalisedSubcladeData fsd = (FinalisedSubcladeData) j.next();
						if(!fsd.getCladeLabel().contains("I-T")){
							if(fsd.significantlySmallDc()){
								String[] dist = fsd.getDistribution().trim().split("\\s+");
								for(int k = 0 ; k < dist.length; k++){
									if(Integer.parseInt(dist[k]) > 0){
										if(!theisolate.contains(new Integer(k))){
											theisolate.add(new Integer(k));
										}
									}
								}
							}
						}
					}
					isolates.add(theisolate);
				} else {
					for(Iterator j = fcd.getSubclades().iterator();j.hasNext();){
						FinalisedSubcladeData fsd = (FinalisedSubcladeData) j.next();
						if(!fsd.getCladeLabel().contains("I-T")){
							if(fsd.significantlySmallDc()){
								String[] dist = fsd.getDistribution().trim().split("\\s+");
								for(int k = 0 ; k < dist.length; k++){
									if(Integer.parseInt(dist[k]) > 0){
										theisolate.add(new Integer(k));
									}
								}
							}
						}
					}
					boolean nomatch = true;
					for(Iterator j = isolates.iterator();j.hasNext();){
						HashSet iso = (HashSet) j.next();
						HashSet intersection = (HashSet)iso.clone();
						intersection.retainAll(theisolate);
						int intersectionCardinality = intersection.size();
						HashSet union = (HashSet)iso.clone();
						union.addAll(theisolate);
						if(intersectionCardinality/union.size() >= 0.75){
							nomatch = false;
						}
					}
					if(nomatch){
						return false;
					}
				}
			} else {
				if(!fcd.getInference().contains("1")){
					System.out.println("Isolates is not checking right");
					return false;
				}
			}
		}
		return true;
	}

	private boolean interiorTipConcordence(ArrayList listOfClades){
		// Check concordance of both tip clades, and of interior clades.
		ArrayList interiorIntersection = new ArrayList();
		ArrayList tipIntersection = new ArrayList();
		ArrayList interiorUnion = new ArrayList();
		ArrayList tipUnion = new ArrayList();
		for(Iterator i = listOfClades.iterator(); i.hasNext();){
			FinalisedCladeData fcd = (FinalisedCladeData) i.next();
			for(Iterator j = fcd.getSubclades().iterator();j.hasNext();){
				FinalisedSubcladeData fsd = (FinalisedSubcladeData) j.next();
				if(!fsd.getCladeLabel().contains("I-T")){
					if(fsd.isTip()){
						String[] dist = fsd.getDistribution().trim().split("\\s+");
						if(tipUnion.isEmpty()){
							for(int k = 0 ; k < dist.length; k++){
								if(Integer.parseInt(dist[k]) > 0){
									if(!tipIntersection.contains(new Integer(k))){
										tipIntersection.add(new Integer(k));
										tipUnion.add(new Integer(k));
									}
								}
							}
						} else {
							for(int k = 0 ; k < dist.length; k++){
								if(Integer.parseInt(dist[k]) > 0){
									if (!tipUnion.contains(new Integer(k))){
										tipUnion.add(new Integer(k));
									}
								} else {
									if(tipIntersection.contains(new Integer(k))){
										tipIntersection.remove(new Integer(k));							
									}
								}
							}
						}
					} else { 
						String[] dist = fsd.getDistribution().trim().split("\\s+");
						if(interiorUnion.isEmpty()){
							for(int k = 0 ; k < dist.length; k++){
								if(Integer.parseInt(dist[k]) > 0){
									if(!interiorIntersection.contains(new Integer(k))){
										interiorIntersection.add(new Integer(k));
										interiorUnion.add(new Integer(k));
									}
								}
							}
						} else {
							for(int k = 0 ; k < dist.length; k++){
								if(Integer.parseInt(dist[k]) > 0){
									if (!interiorUnion.contains(new Integer(k))){
										interiorUnion.add(new Integer(k));
									}
								} else {
									if(interiorIntersection.contains(new Integer(k))){
										interiorIntersection.remove(new Integer(k));							
									}
								}
							}
						}
					}
				}
			}
		}
		return (tipIntersection.size()/tipUnion.size() >= 0.75 && interiorIntersection.size()/interiorUnion.size() >= 0.75);
	}

	public String toString(){
		StringBuffer str = new StringBuffer();
		str.append("Begin Clade Group - ").append(type).append("\n");
		for(Iterator i = iterator(); i.hasNext();){
			str.append(((FinalisedCladeData) i.next()).toString());
		}
		str.append("\n");
		for(Iterator i = gStats.iterator(); i.hasNext();){
			str.append(((GStatistic) i.next()).toString()).append("\n");
		}
		str.append("End Group\n");
		return str.toString();
	}

	public boolean hasUniqueLoci(ArrayList listOfClades){
		// returns true if and only if all the loci in the list are unique.
		ArrayList uniqueloci = new ArrayList();
		for(Iterator i = listOfClades.iterator();i.hasNext();){
			FinalisedCladeData fcd = (FinalisedCladeData) i.next();
			if(uniqueloci.contains(fcd.getLocusLabel())){
				return false;
			} else {
				uniqueloci.add(fcd.getLocusLabel());
			}
		}
		return true;
	}

	public boolean isSingleLocus(ArrayList listOfClades){
		// returns true if and only if all the loci are the same.
		String locus = null;
		for(Iterator i = listOfClades.iterator();i.hasNext();){
			FinalisedCladeData fcd = (FinalisedCladeData) i.next();
			if(locus == null){
				locus = fcd.getLocusLabel();
			} else if (!locus.equals(fcd.getLocusLabel())) {
				return false;
			}
		}
		return true;
	}

}
