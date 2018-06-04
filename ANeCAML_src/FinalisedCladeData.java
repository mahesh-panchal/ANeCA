
import java.util.*;

public class FinalisedCladeData{
	
	private String cladeLabel; // the clade label
	private ArrayList subclades = new ArrayList(); // the FinalisedSubcladeData.
	private String inference; // the clade inference.
	private int[] sampleSizeDistribution; // the distribution of sample size, indexed by location for the whole clade.
	private GMLData gmlData;
	private NexusData nexData;
	private ArrayList locations;
	private double lowestSubcladeAge = Double.MAX_VALUE; // This will remain at max value if the only significant clade is I-T
	private double avePairDiv;
	private final static double mutationRate = 0.00002; // This is the mutation rate for the whole sequence.
	public static final double CONCORDANCE_THRESHOLD = 0.75;  // 75% overlap for concordance
	private CladeGroup cladeGroup; 
	private String locusLabel;

	
	public FinalisedCladeData(String label,ArrayList locs){
		cladeLabel = label;
		locations = locs;
		sampleSizeDistribution = new int[locations.size()];
		for(int i=0; i < sampleSizeDistribution.length; i++){
			sampleSizeDistribution[i] = 0;
		}
	}
	
	public boolean equals(Object o){
		// sufficient representation of equals. same clade on the same locus.
		if(o instanceof FinalisedCladeData){
			return (cladeLabel.equals(((FinalisedCladeData)o).cladeLabel) && locusLabel.equals(((FinalisedCladeData)o).locusLabel));
		}
		return false;
	}

	public FinalisedCladeData clone(){
		FinalisedCladeData newFcd = new FinalisedCladeData(cladeLabel,locations);
		newFcd.subclades = subclades;
		newFcd.inference = inference;
		newFcd.sampleSizeDistribution = sampleSizeDistribution;
		newFcd.gmlData = gmlData;
		newFcd.nexData = nexData;
		newFcd.lowestSubcladeAge = lowestSubcladeAge;
		newFcd.avePairDiv = avePairDiv;
		newFcd.cladeGroup = cladeGroup;
		newFcd.locusLabel = locusLabel;
		return newFcd;
	}
	
	public String toString(){
		StringBuffer str = new StringBuffer();
		str.append(getLocusLabel()).append("-").append(getCladeLabel()).append("\n");
		str.append(inference).append("\n");
		if(cladeTLSEstimate() == Double.MAX_VALUE){
			str.append("TLS: ").append("---");
		} else {
			str.append("TLS: ").append(cladeTLSEstimate());
		}
		str.append(" \\pi^: ").append(averagePairwiseDivergence());
		str.append("\nGeographic Distribution :");
		for (int i = 0; i< sampleSizeDistribution.length;i++){
			str.append(" ").append(sampleSizeDistribution[i]);
		}
		str.append("\n");
		return str.toString();
	}
	
	public void setLocusLabel(String label){
		locusLabel = label;
	}

	public String getLocusLabel(){
		return locusLabel;
	}
	
	public String getCladeLabel(){
		return cladeLabel;
	}

	public void setCladeGroup(CladeGroup cg){
		cladeGroup = cg;
	}
	
	public CladeGroup getCladeGroup(){
		return cladeGroup;
	}
	
	public double cladeTLSEstimate(){
		// Page 214 "When NCPA infers an event or process, the time of that event or process
		// can be estimated as the age of the youngest monophyletic clade that contributed in
		// a statistically significant fashion to the inference (Templeton 2002).
		return lowestSubcladeAge;
	}

	public double averagePairwiseDivergence(){
		return avePairDiv;
	}
	
	private double averagePairwiseDivergence(FinalisedSubcladeData fsd){
		// calculates the average pairwise distance.
		// Uses \pi = (n/(n-1)) * \sum_i^k \sum_j^k [ p_i * p_j * d_ij ] from Arlequin manual. p's are relative freq
		String[] haps = fsd.getHaplotypes().split("\\s+");
		double averageDiff = 0;
		int sampleSize = 0;
		StringBuffer listOfHaps = new StringBuffer();
		for (int i = 0; i < haps.length; i++){
			listOfHaps.append(gmlData.getHaplotype(haps[i])).append(" ");
			sampleSize += gmlData.getHaplotypeFrequency(haps[i]);
		}
		if(sampleSize == 1){
			System.out.println("Locus:"+ locusLabel + " Clade:"+ cladeLabel + " Subclade:" + fsd.getCladeLabel() + " aveDiv:" + 0.0 + " samplesize:" + sampleSize);
			return 0.0;
		}
		haps = listOfHaps.toString().trim().split("\\s+");
		for (int i = 0; i < haps.length; i++){
			String hap1 = nexData.getSequence(haps[i]);
			for (int j = 0; j < haps.length; j++ ){
				String hap2 = nexData.getSequence(haps[j]);
				// Assume strings are the same length.
				int diff = 0;
				for (int k = 0; k < hap1.length(); k++){
					if(hap1.charAt(k) != hap2.charAt(k)){
						diff++;
					}
				}
				averageDiff += diff*gmlData.getHaplotypeFrequency(haps[i])*gmlData.getHaplotypeFrequency(haps[j])/Math.pow(sampleSize,2);
			}
		}
		//System.out.println("Sub: "+fsd.getCladeLabel()+" aveDiff:"+averageDiff+" sampleSize:"+sampleSize+" haps:"+listOfHaps.toString() + " what was in haps.list:" + fsd.getHaplotypes());
		System.out.println("Locus:"+ locusLabel + " Clade:"+ cladeLabel + " Subclade:" + fsd.getCladeLabel() + " aveDiv:" + sampleSize*averageDiff/(sampleSize-1) + " samplesize:" + sampleSize);
		return sampleSize*averageDiff/(sampleSize-1);
	}
	
	private double timePointEstimate(FinalisedSubcladeData fsd){
		// Give the time point estimate of the clade
		return averagePairwiseDivergence(fsd)/mutationRate;
	}
	
	public boolean isConcordantWith(FinalisedCladeData clade){
		// Check if the argument clade is concordant with this one.
		// Calculate total area of clade1 and clade 2
		// Calculate the intersection of clade 1 and clade 2
		// Calculate the area of intersection.
		/* 	if( clade 1 is inside clade 2){
				Then concordent. fits inside case below case below.
			} else if ( clade 2 is inside clade 2){
				Then concordent. fits inside case below
			} else if ( intersect/clade1 is > threshold || intersect/clade2 is > threshold){
				Then concordent.
			} else if ( insersect/clade1 is > threshold but intersect/clade2 < threshold)
		*/
		// This still checks concordence even if the only significant stat was I-T (could be RGF so no time needed)
		Stack thisCladeHull = NCPAGeographicCalculations.convexHull(distributionToList());
		Stack otherCladeHull = NCPAGeographicCalculations.convexHull(clade.distributionToList());
		Stack intersection = NCPAGeographicCalculations.intersect(thisCladeHull,otherCladeHull);
		double thisCladeArea = NCPAGeographicCalculations.area(thisCladeHull);
		double otherCladeArea = NCPAGeographicCalculations.area(otherCladeHull);
		if (intersection != null){
			if(thisCladeArea != 0.0){
				if(NCPAGeographicCalculations.area(intersection)/thisCladeArea >= CONCORDANCE_THRESHOLD){
					return true;
				} else {
					if (otherCladeArea != 0) {
						return NCPAGeographicCalculations.area(intersection)/otherCladeArea >= CONCORDANCE_THRESHOLD;
					} else {
						return false;
					}
				}
			} else {
				if (otherCladeArea != 0) {
					return NCPAGeographicCalculations.area(intersection)/otherCladeArea >= CONCORDANCE_THRESHOLD;
				} else {
					return false;
				}
			}
		}
		return false;
	}
	
	/*public String toString(){
		// return description of the clade.
		return "":
	}*/

	public void addSubclade(FinalisedSubcladeData fsd){
		subclades.add(fsd);
	}

	public void setInference(String inf){
		inference = inf;
	}

	public String getInference(){
		return inference;
	}
	
	public ArrayList getSubclades(){
		return subclades;
	}

	public void calculateStats(){
		StringBuffer haplotypes = new StringBuffer();
		for(Iterator i = subclades.iterator(); i.hasNext();){ // Has I-T clade in it
			FinalisedSubcladeData fsd = (FinalisedSubcladeData) i.next();
			if(fsd.isSignificant() && !fsd.getCladeLabel().contains("I-T")){
				double cladeAge = timePointEstimate(fsd);
				if(cladeAge < lowestSubcladeAge){
					lowestSubcladeAge = cladeAge;
				}
			}
			if(!fsd.getCladeLabel().contains("I-T")){
				haplotypes.append(fsd.getHaplotypes()).append(" ");
				String[] dist = fsd.getDistribution().trim().split("\\s+");
				for(int k = 0; k < dist.length;k++){ // same length as locations.
					sampleSizeDistribution[k] += Integer.parseInt(dist[k]);
				}
			}
		}
		String[] haps = haplotypes.toString().trim().split("\\s+");
		double averageDiff = 0;
		int sampleSize = 0;
		StringBuffer listOfHaps = new StringBuffer();
		for (int i = 0; i < haps.length; i++){
			listOfHaps.append(gmlData.getHaplotype(haps[i])).append(" ");
			sampleSize += gmlData.getHaplotypeFrequency(haps[i]);
		}
		haps = listOfHaps.toString().trim().split("\\s+");
		for (int i = 0; i < haps.length; i++){
			String hap1 = nexData.getSequence(haps[i]);
			for (int j = 0; j < haps.length; j++ ){
				//System.out.println("nex:"+haps[j]+":");
				String hap2 = nexData.getSequence(haps[j]);
				// Assume strings are the same length.
				int diff = 0;
				for (int k = 0; k < hap1.length(); k++){
					if(hap1.charAt(k) != hap2.charAt(k)){
						diff++;
					}
				}
				averageDiff += diff*gmlData.getHaplotypeFrequency(haps[i])*gmlData.getHaplotypeFrequency(haps[j])/Math.pow(sampleSize,2);
			}
		}
		avePairDiv = sampleSize*averageDiff/(sampleSize-1);
		nexData = null; // Not needed any more
		gmlData = null; // Not needed any more
	}

	public void setGMLData(GMLData gd){
		gmlData = gd;
	}

	public void setNexusData(NexusData nd){
		nexData = nd;
	}

	private ArrayList distributionToList(){
		// get the population location distribution in ArrayList form of the current clade.
		ArrayList thelist = new ArrayList();
		for(int i=0; i < sampleSizeDistribution.length; i++){ 
			if(sampleSizeDistribution[i] > 0){
				thelist.add(locations.get(i));
			}
		}
		return thelist;
	}
	


}
