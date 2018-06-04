package NCPAInferenceTree;

/*
This file requires some serious clean up work.
The file is bloated, repetative, and overly complex.
Requires better complex structure memory and better handling of computational geometry
Final Step: Rework the geometry, buff, and flush the pipes.
*/

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;
import java.util.Collections;
import NestedCladeBuilder.*;

public class CladeData{
	public static final double CONCORDANCE_THRESHOLD = 0.75;  // 75% overlap for concordance
	private static double SIGNIFICANCE_LEVEL = 0.05;
	private static final double RADIUS = 6364.963;              // average radius of the earth in km - Change this if it changes in GeoDis
	private	static final int UNKNOWN = 0;
	private	static final int P_IN = -1;
	private	static final int Q_IN = 1;
	private	static final char END_POINT = 'e';
	private	static final char VERTEX = 'v';
	private	static final char CROSS = '1';
	private	static final char NO_CROSS = '0';
	private String cladeLabel;
	private double chi_sq_stat;
	private double p_chi_sq_stat; 
	private ArrayList subCladeData = new ArrayList();
	private TipVsIntTest test;
	private CladeData nextData = null;
	private PopulationData[] popData; // length field gives the number of populations
	private NestedCladograph nsteplevels; // change to nested cladogram design.
	private CladeDataChain owner;
	static final int lblPadLimit = 10; // characters long
	static final int statPadLimit = 15; // characters long
	static final int popPadLimit = 4; // characters long
	
	public CladeData(String cLabel){
		cladeLabel = cLabel;
	}
	
	// public access methods
	public static void setSignificanceThreshold(double level){
		SIGNIFICANCE_LEVEL = level;
	}
	
	public static double getSignificanceThreshold(){
		return SIGNIFICANCE_LEVEL;
	}
	
	public void setChiSquareStat(double stat){
		chi_sq_stat = stat;
	}
	
	public void setChiSquareProb(double prob){
		p_chi_sq_stat = prob;
	}
	
	public void addSubCladeData(SubCladeData scd){
		subCladeData.add(scd);
	}
	
	public void setTipVsInteriorData(TipVsIntTest tiTest){
		test = tiTest;
	}
	
	public void setPopulationData(PopulationData[] pd){
		popData = pd;
	}
	
	public PopulationData[] getPopulationData(){
		return popData;
	}
	
	public void setNestedCladogram(NestedCladograph ncg){ 
		nsteplevels = ncg;
	}
	
	public NestedCladograph getNestedCladogram(){ 
		return nsteplevels;
	}
	
	public String getCladeLabel(){
		return cladeLabel;
	}
	
	public String toString(){
		StringBuffer str = new StringBuffer();
		str.append(cladeLabel).append("\n");
		str.append("Observed Chi-Square statistic = ").append(chi_sq_stat);
		if(p_chi_sq_stat <= SIGNIFICANCE_LEVEL){
			str.append("*");
		}
		str.append("\n").append("Geographic Distance Analysis\n");
		str.append("Clade     Dc             Dn             Pop:"); // When the padding limits are changed, change here too
		for(int i = 0; i < popData.length;i++){
			int len = Integer.toString(i+1).length();
			for(int j = 0; j < popPadLimit-len;j++){
				str.append(" ");
			}
			str.append(i+1);
		}
		str.append("\n");
		for(Iterator i =  subCladeData.iterator();i.hasNext();){
			//System.out.println("Print subclades");
			str.append(((SubCladeData) i.next()).toString()).append("\n");
		}
		if(test != null){
			//System.out.println("Print subclades I-T");
			str.append ("I-T       ").append(test.toString()).append("\n"); // change padding limits here too
		}
		return str.toString();
	}
	
	public void setSubcladeDistribution(String label, int[] distr){
		SubCladeData scd;
		for(Iterator i = subCladeData.iterator(); i.hasNext();){
			if((scd = (SubCladeData) i.next()).getCladeLabel().split("\\s+")[1].equals(label)){
				scd.setDistribution(distr);
			}
		}
	}
	
	public boolean hasSignificance(){ // Prerequisite of Inference key
		SubCladeData scd;
		for(Iterator i = subCladeData.iterator();i.hasNext();){
			scd = (SubCladeData) i.next();
			if(scd.hasSignificance(SIGNIFICANCE_LEVEL)){
				return true;
			}
		}
		if(test != null){
			if(test.hasSignificance(SIGNIFICANCE_LEVEL)){
				return true;
			}
		}
		return false;
	}
	
	public boolean question1(){ // Checked - Reasoned - Correct (I believe)
		// All the clades have non-overlapping population distributions
		// New method
		Stack[] convexHulls = new Stack[subCladeData.size()];
		for(int i = 0; i < subCladeData.size(); i++){
			convexHulls[i] = convexHull(distributionToList(((SubCladeData) subCladeData.get(i)).getDistribution()));
		}
		for(int i = 1; i < subCladeData.size(); i++){
			for(int j = 0; j < i; j++){
				if(intersect(convexHulls[i],convexHulls[j]) != null){
					return false;
				}
			}
		}
		return true;
		/*int[] cladeDistr = new int[popData.length];
		for(int j = 0; j<cladeDistr.length;j++){
			cladeDistr[j] = 0;
		}
		int[] subcDistr;
		for(Iterator i = subCladeData.iterator(); i.hasNext();){
			SubCladeData scd = (SubCladeData) i.next();
			subcDistr = scd.getDistribution();
			for(int k = 0; k < cladeDistr.length;k++){
				cladeDistr[k] += subcDistr[k];
			}
		} // cladeDistr is now the nesting clades distribution
		for(Iterator i = subCladeData.iterator();i.hasNext();){
			subcDistr = ((SubCladeData) i.next()).getDistribution();
			for(int k = 0; k < subcDistr.length; k++){
				if(subcDistr[k] > 0 && (cladeDistr[k] - subcDistr[k]) != 0){ // If the clade has an individual from pop k and when taken away from the total clade distribution it is no zero
					return false; // we have a distribution overlap, since another clade contributes to the total clade distribution
				}
			}
		}
		return true; // If we get here the distributions do not overlap
		*/
	}
	
	public boolean question2() throws StatusUndeterminedException{ // Checked - Reasoned - Correct (I believe)
		if(test == null){ // There was no contrast since either tips or interiors did not exist
			throw new StatusUndeterminedException();
		}
		return question2a() || question2b() || question2c() || question2d(); // At least one condition must be satisfied
	}
	
	private boolean question2a(){ // Checked - Reasoned - Correct (I believe)
		boolean b1 = false; // There exists at least one tip with Dc significantly small
		boolean b2 = false; // and there exists at least one interior with Dc not significantly small
		SubCladeData scd;
		for(Iterator i = subCladeData.iterator();i.hasNext();){
			scd = (SubCladeData) i.next();
			if(scd.isTip()){ // if tip
				if(scd.significantlySmallDc(SIGNIFICANCE_LEVEL)){
					b1 = true; // has significantly small Dc
				}
			} else { // if interior 
				if(!scd.significantlySmallDc(SIGNIFICANCE_LEVEL)){
					b2 = true; // does not have significantly small Dc
				}
			}
		}
		return b1 && b2;
	}
	
	private boolean question2b(){ // Checked - Reasoned - Correct (I believe)
		boolean b1 = false; // There exists at least one tip with Dc not significantly large
		boolean b2 = false; // and there exists at least one interior with Dc significantly small
		boolean b3 = false; // and there exists at least one interior with Dc not significantly small
		SubCladeData scd;
		for(Iterator i= subCladeData.iterator();i.hasNext();){
			scd = (SubCladeData) i.next();
			if(scd.isTip()){ 
				if(!scd.significantlyLargeDc(SIGNIFICANCE_LEVEL)){
					b1 = true; // At least one tip without Dc sig. large.
				}
			} else {
				if(scd.significantlySmallDc(SIGNIFICANCE_LEVEL)){
					b2 = true; // At least one interior with Dc sig. small
				} else {
					b3 = true; // At least one interior without Dc sig. small
				}
			}
		}
		return b1 && b2 && b3;
	}
	
	private boolean question2c(){ // Checked - Reasoned - Correct (I believe)
		boolean b1 = false; // There exists at least one interior with Dc significantly large
		boolean b2 = true;  // and for all tip clades Dc is not significantly large
		SubCladeData scd;
		for(Iterator i = subCladeData.iterator();i.hasNext();){
			scd = (SubCladeData) i.next();
			if(scd.isTip()){
				if(scd.significantlyLargeDc(SIGNIFICANCE_LEVEL)){ // have tip with sig. large Dc so
					b2 = false; // not all tips have Dc sig. small or non-sig.
				}
			} else {
				if(scd.significantlyLargeDc(SIGNIFICANCE_LEVEL)){ 
					b1 = true; // at least one interior with Dc sig. large
				}
			}
		}
		return b1 && b2;
	}
	
	private boolean question2d(){ // Checked - Reasoned - Correct (I believe)
		// The I-T Dc value is significantly large
		return test.significantlyLargeDc(SIGNIFICANCE_LEVEL);
	}
	
	public boolean question3(){  // Checked - Reasoned - Correct (I believe)
		return question3a() || question3b() || question3c() || question3d(); // At least one must be true
	}
	
	private boolean question3a(){ // Checked - Reasoned - Correct (I believe)
		// There exists at least one clade or I-T with either 
		// Dn significantly large and Dc significantly small 
		// or Dn significantly small and Dc significantly large
		SubCladeData scd;
		for (Iterator i = subCladeData.iterator();i.hasNext();){
			scd = (SubCladeData) i.next();
			if(scd.significantlySmallDc(SIGNIFICANCE_LEVEL) && scd.significantlyLargeDn(SIGNIFICANCE_LEVEL)){
				return true;
			} else if (scd.significantlyLargeDc(SIGNIFICANCE_LEVEL) && scd.significantlySmallDn(SIGNIFICANCE_LEVEL)){
				return true;
			}
		}
		if(test.significantlySmallDc(SIGNIFICANCE_LEVEL) && test.significantlyLargeDn(SIGNIFICANCE_LEVEL)){
			return true;
		} else if (test.significantlyLargeDc(SIGNIFICANCE_LEVEL) && test.significantlySmallDn(SIGNIFICANCE_LEVEL)){
			return true;
		}
		return false;
	}
	
	private boolean question3b(){ // Checked - Reasoned - Correct (I believe)
		// There exists at least one tip with Dn significantly large
		SubCladeData scd;
		for(Iterator i = subCladeData.iterator(); i.hasNext();){
			scd = (SubCladeData) i.next();
			if(scd.isTip() && scd.significantlyLargeDn(SIGNIFICANCE_LEVEL)){
				return true; // have tip with sig. large Dn.
			}
		}
		return false;
	}
	
	private boolean question3c(){ // Checked - Reasoned - Correct (I believe)
		// There exists at least one interior with Dn significantly small
		SubCladeData scd;
		for(Iterator i = subCladeData.iterator(); i.hasNext();){
			scd = (SubCladeData) i.next();
			if(!scd.isTip() && scd.significantlySmallDn(SIGNIFICANCE_LEVEL)){
				return true; // interior with significantly small Dn.
			}
		}
		return false;
	}
	
	private boolean question3d(){ // Checked - Reasoned - Correct (I believe)
		// I-T Dn is significantly small
		// and Dc is non-significant
		return test.significantlySmallDn(SIGNIFICANCE_LEVEL) 
			&& !test.significantlySmallDc(SIGNIFICANCE_LEVEL) 
			&& !test.significantlyLargeDc(SIGNIFICANCE_LEVEL);
	}
	
	public boolean question4(){ // Checked - Reasoned - Correct (I believe)
		return question4a() && question4b();
	}
	
	private boolean question4a(){ // Checked - Reasoned - Correct (I believe)
		// There exists at least two clades with Dc significantly small
		// and their combined population distributions do not overlap with the other remaining clades in the group.
		int[] sigDistr = new int[popData.length]; // distribution of all clades with sig. small Dc.
		for(int i = 0; i < sigDistr.length; i++){ sigDistr[i] = 0;}
		int[] remDistr = new int[popData.length]; // remaining clade distribution
		for(int i = 0; i < remDistr.length; i++){ remDistr[i] = 0;}
		SubCladeData scd;
		int[] distr;
		int numSig = 0; // number of clades with significantly small Dc
		for(Iterator i = subCladeData.iterator(); i.hasNext();){
			scd = (SubCladeData) (i.next());
			distr = scd.getDistribution();
			if(scd.significantlySmallDc(SIGNIFICANCE_LEVEL)){
				numSig++;
				for(int k = 0; k < distr.length;k++){
					sigDistr[k] += distr[k];
				}
			} else {
				for(int j = 0; j < distr.length; j++){
					remDistr[j] += distr[j];
				}
			}
		}
		if(numSig >= 2 && numSig != subCladeData.size()){
			return intersect(convexHull(distributionToList(sigDistr)),convexHull(distributionToList(remDistr))) == null; // true if there is no intersect
		}
		return false; // not enough significant clades or not enough other clades
		/*int[] totalDistr = new int[popData.length]; // nesting clade distribution
		for(int i = 0; i < totalDistr.length; i++){ totalDistr[i] = 0;}
		int[] signfDistr = new int[popData.length]; // distribution of all clades with sig. small Dc.
		for(int i = 0; i < signfDistr.length; i++){ signfDistr[i] = 0;}
		SubCladeData scd;
		int[] distr;
		int numSig = 0; // number of clades with significantly small Dc
		for(Iterator i = subCladeData.iterator(); i.hasNext();){
			scd = (SubCladeData) (i.next());
			distr = scd.getDistribution();
			if(scd.significantlySmallDc(SIGNIFICANCE_LEVEL)){
				numSig++;
				for(int k = 0; k < distr.length;k++){
					signfDistr[k] += distr[k];
				}
			}
			for(int j = 0; j < distr.length; j++){
				totalDistr[j] += distr[j];
			}
		}
		if(numSig >= 2 && numSig != subCladeData.size()){
			for(int r = 0; r < totalDistr.length; r++){
				if(signfDistr[r] > 0 && (totalDistr[r] - signfDistr[r] != 0)){ // if signfDistr makes a contribution then it must be the total contribution or we
					return false; // have overlap
				}
			}
			return true; // No overlap
		}
		return false; // not enough significant clades or not enough other clades
		*/
	}
	
	private boolean question4b(){ // Checked - Reasoned - Correct (I believe)
		// do all subclades (1 level down) not show significant Dc values
		// or does at least one subclade (1 level down) show a significantly large Dc
		if(getCladeLabel().split("\\s+")[1].split("-")[0].equals("1")){ // save some work - will be true if it falls through the loop without any tests by default.
			return true; // since this is a one step clade
		}
		boolean nonsigDc = true;
		boolean siglarDc = false; // Are only looking for one but left in anyway
		Iterator i = owner.iterator();
		CladeData cd = (CladeData) i.next();
		SubCladeData scd;
		while(i.hasNext() && !cd.getCladeLabel().equals(getCladeLabel())){ // Clade label should always trigger first.
			for(Iterator k = subCladeData.iterator();k.hasNext();){
				scd = (SubCladeData) (k.next());
				if(scd.getCladeLabel().split("\\s+")[1].equals(getCladeLabel().split("\\s+")[1])){ // 0 should contain "Clade", 1 should contain [label] 
					// if equal clade data equal to sub clade data then test statistics of clade data
					SubCladeData sscladedata;
					for(Iterator m = cd.subCladeData.iterator(); m.hasNext();){
						sscladedata = (SubCladeData)(m.next());
						if(sscladedata.significantlySmallDc(SIGNIFICANCE_LEVEL)){
							nonsigDc = false;
						} else if(sscladedata.significantlyLargeDc(SIGNIFICANCE_LEVEL)){
							nonsigDc = false;
							siglarDc = true;
							return true; //return nonsigDc || siglarDc; // will speed up result
						}
					}
				}
			}
			cd = (CladeData) i.next();
		}
		return nonsigDc || siglarDc; // This will be an either or (xor)
	}
	
	public boolean question5(){ // This question is the same as 4 // Checked - Reasoned - Correct (I believe)
		return question4a() && question4b();
	}
	
	public boolean question6() throws StatusUndeterminedException{
		if(subCladeData.size() <= 2){ // Need to check this value
			throw new StatusUndeterminedException();
		}
		return question6a() /*|| question6b()*/;
	}
	
	private boolean question6a() throws StatusUndeterminedException{ // Checked - Reasoned - Correct (I believe)
		// Are the clades with significant reversals or significant Dn without sig. Dc 
		// geographically concordant with the other clades with reversals and or sig. Dn w/o sig. Dc
		ArrayList sigClades = new ArrayList();
		int[] signfDistr = new int[popData.length]; // total distribution of [pattern] clades
		int[] distr;
		for(int i=0;i<signfDistr.length;i++){signfDistr[i] = 0;}
		SubCladeData scd;
		for(Iterator i = subCladeData.iterator(); i.hasNext();){
			scd = (SubCladeData) i.next();
			if((!scd.significantlySmallDc(SIGNIFICANCE_LEVEL) && scd.significantlySmallDn(SIGNIFICANCE_LEVEL))
			|| (!scd.significantlyLargeDc(SIGNIFICANCE_LEVEL) && scd.significantlyLargeDn(SIGNIFICANCE_LEVEL))){
				sigClades.add(scd);
				distr = scd.getDistribution();
				for(int j = 0;j < distr.length;j++){
					signfDistr[j] += distr[j];
				}
			}
		}
		if(sigClades.size() < 2){throw new StatusUndeterminedException();} // cannot make comparison unless there are at least 2 clades!
		double totalarea = area(convexHull(distributionToList(signfDistr))); // Total area of hull
		double subarea;
		for(Iterator i = sigClades.iterator();i.hasNext();){
			scd = (SubCladeData) i.next();
			distr = scd.getDistribution();
			subarea = area(convexHull(distributionToList(distr)));
			if((subarea/totalarea) < CONCORDANCE_THRESHOLD){
				return false; // subarea is not X% of the total area
			}
			/*int concPops = 0;
			int pops = 0;
			for(int j = 0; j < distr.length;j++){
				if(popData[j].isSampled()){
					if((signfDistr[j] == 0 && distr[j] == 0) || (distr[j] != 0 && (signfDistr[j]-distr[j]) != 0)){
						concPops++; // have agreed absence or presence in pop j
					}
					pops++;
				}
			}
			if(((double)concPops/(double)pops) < CONCORDANCE_THRESHOLD){ // Concordance is with sampled populations only
				return false; // at least one clade is not geographically concordant with the others in sigClades
			}*/
		}
		return true; // All clades in sigClades are geographically concordant (more or less)
	}
	
	/*private boolean question6b(){
		// This was essentially the same as 6a
		return true;
	}*/
	
	public boolean question7(){ // Checked - Reasoned - Correct (I believe)
		// Does there exist a sampled area (a population) between clades with Dn significantly large and other clades.
		// (or if I-T Dn is significantly small does there exist a sampled area between tips and other clades)
		int[] sigCladeDistr = new int[popData.length]; for(int i=0;i<sigCladeDistr.length;i++){sigCladeDistr[i]=0;}
		int[] otrCladeDistr = new int[popData.length]; for(int i=0;i<otrCladeDistr.length;i++){otrCladeDistr[i]=0;}
		int[] temp;
		SubCladeData scd;
		if(test.significantlySmallDn(SIGNIFICANCE_LEVEL)){ 
			for(Iterator i= subCladeData.iterator();i.hasNext();){
				scd = (SubCladeData) i.next();
				temp = scd.getDistribution();
				if(scd.isTip()){
					for(int j = 0; j < temp.length; j++){
						sigCladeDistr[j] += temp[j];
					}
				} else {
					for(int j = 0; j < temp.length; j++){
						otrCladeDistr[j] += temp[j];
					}
				}
			} 
		} else {
			for(Iterator i= subCladeData.iterator();i.hasNext();){
				scd = (SubCladeData) i.next();
				temp = scd.getDistribution();
				if(scd.significantlyLargeDn(SIGNIFICANCE_LEVEL)){
					for(int j = 0; j < temp.length; j++){
						sigCladeDistr[j] += temp[j];
					}
				} else {
					for(int j = 0; j < temp.length; j++){
						otrCladeDistr[j] += temp[j];
					}
				}
			} 
		}
		return isSampledPopulationBetween(sigCladeDistr,otrCladeDistr,true);
	}
	
	public boolean question8(){ // Checked - Reasoned - Correct (I believe)
		// Does there exist a unsampled area (a unsampled population) between clades with Dn significantly large and other clades.
		// (or if I-T Dn is significantly small does there exist a sampled area between tips and other clades)
		int[] sigCladeDistr = new int[popData.length]; for(int i=0;i<sigCladeDistr.length;i++){sigCladeDistr[i]=0;}
		int[] otrCladeDistr = new int[popData.length]; for(int i=0;i<otrCladeDistr.length;i++){otrCladeDistr[i]=0;}
		int[] temp;
		SubCladeData scd;
		if(test.significantlySmallDn(SIGNIFICANCE_LEVEL)){ 
			for(Iterator i= subCladeData.iterator();i.hasNext();){
				scd = (SubCladeData) i.next();
				temp = scd.getDistribution();
				if(scd.isTip()){
					for(int j = 0; j < temp.length; j++){
						sigCladeDistr[j] += temp[j];
					}
				} else {
					for(int j = 0; j < temp.length; j++){
						otrCladeDistr[j] += temp[j];
					}
				}
			} 
		} else {
			for(Iterator i= subCladeData.iterator();i.hasNext();){
				scd = (SubCladeData) i.next();
				temp = scd.getDistribution();
				if(scd.significantlyLargeDn(SIGNIFICANCE_LEVEL)){
					for(int j = 0; j < temp.length; j++){
						sigCladeDistr[j] += temp[j];
					}
				} else {
					for(int j = 0; j < temp.length; j++){
						otrCladeDistr[j] += temp[j];
					}
				}
			} 
		}
		return isUnsampledPopulationBetween(sigCladeDistr,otrCladeDistr,true);
	}
	
	public boolean question9(){ // Checked - Reasoned - Correct (I believe)
		// Does there exist an unsampled area (an unsampled population) between clades with Dc significantly small and other clades.
		int[] sigCladeDistr = new int[popData.length]; for(int i=0;i<sigCladeDistr.length;i++){sigCladeDistr[i]=0;}
		int[] otrCladeDistr = new int[popData.length]; for(int i=0;i<otrCladeDistr.length;i++){otrCladeDistr[i]=0;}
		int[] temp;
		SubCladeData scd;
		for(Iterator i= subCladeData.iterator();i.hasNext();){
			scd = (SubCladeData) i.next();
			temp = scd.getDistribution();
			if(scd.significantlySmallDc(SIGNIFICANCE_LEVEL)){
				for(int j = 0; j < temp.length; j++){
					sigCladeDistr[j] += temp[j];
				}
			} else {
				for(int j = 0; j < temp.length; j++){
					otrCladeDistr[j] += temp[j];
				}
			}
		} 
		//return isPopulationBetweenAndAllSampled(sigCladeDistr,otrCladeDistr,true);
		return isUnsampledPopulationBetween(sigCladeDistr,otrCladeDistr,true);
	}
	
	public boolean question10(){ // Checked - Reasoned - Correct (I believe)
		// Does there exist an unsampled area (an unsampled population) between clades with Dc significantly small and other clades.
		int[] sigCladeDistr = new int[popData.length]; for(int i=0;i<sigCladeDistr.length;i++){sigCladeDistr[i]=0;}
		int[] otrCladeDistr = new int[popData.length]; for(int i=0;i<otrCladeDistr.length;i++){otrCladeDistr[i]=0;}
		int[] temp;
		SubCladeData scd;
		for(Iterator i= subCladeData.iterator();i.hasNext();){
			scd = (SubCladeData) i.next();
			temp = scd.getDistribution();
			if(scd.significantlySmallDc(SIGNIFICANCE_LEVEL)){
				for(int j = 0; j < temp.length; j++){
					sigCladeDistr[j] += temp[j];
				}
			} else {
				for(int j = 0; j < temp.length; j++){
					otrCladeDistr[j] += temp[j];
				}
			}
		} 
		return isUnsampledPopulationBetween(sigCladeDistr,otrCladeDistr,true);
	}
	
	public boolean question11(){ // Checked - Reasoned - Correct (I believe)
		return question11a() || question11b() || question11c();
	}
	
	private boolean question11a(){ // Checked - Reasoned - Correct (I believe)
		// There exists at least one tip with Dc significantly large
		SubCladeData scd;
		for(Iterator i = subCladeData.iterator(); i.hasNext();){
			scd = (SubCladeData) i.next();
			if(scd.isTip() && scd.significantlyLargeDc(SIGNIFICANCE_LEVEL)){
				return true;
			}
		}
		return false;
	}
	
	private boolean question11b(){ // Checked - Reasoned - Correct (I believe)
		// For all interior clades Dc is significantly small
		SubCladeData scd;
		for(Iterator i = subCladeData.iterator(); i.hasNext();){
			scd = (SubCladeData) i.next();
			if(!scd.isTip() && !scd.significantlySmallDc(SIGNIFICANCE_LEVEL)){ // careful!
				return false;
			}
		}
		return true;
	}
	
	private boolean question11c(){ // Checked - Reasoned - Correct (I believe)
		// I-T Dc is significantly small
		return test.significantlySmallDc(SIGNIFICANCE_LEVEL);
	}
	
	public boolean question12(){ // Checked - Reasoned - Correct (I believe)
		// Does there exist at least one clade with either Dc significantly small and Dn significantly large
		// or Dc significantly large and Dn significantly small.
		SubCladeData scd;
		for(Iterator i = subCladeData.iterator();i.hasNext();){
			scd = (SubCladeData) i.next();
			if(((scd.significantlySmallDc(SIGNIFICANCE_LEVEL) && scd.significantlyLargeDn(SIGNIFICANCE_LEVEL))
				|| (scd.significantlyLargeDc(SIGNIFICANCE_LEVEL) && scd.significantlySmallDn(SIGNIFICANCE_LEVEL)))){
				return true;
			}
		}
		return ((test.significantlySmallDc(SIGNIFICANCE_LEVEL) && test.significantlyLargeDn(SIGNIFICANCE_LEVEL))
				|| (test.significantlyLargeDc(SIGNIFICANCE_LEVEL) && test.significantlySmallDn(SIGNIFICANCE_LEVEL)));
	}
	
	public boolean question13(){ // Checked - Reasoned - Correct (I believe)
		// Does there exist a sampled area (a population) between clades with Dn significantly large and the geographic centre of the other clades.
		// (or if I-T Dn is significantly small does there exist a sampled area between tips and other clades)
		int[] sigCladeDistr = new int[popData.length]; for(int i=0;i<sigCladeDistr.length;i++){sigCladeDistr[i]=0;}
		int[] otrCladeDistr = new int[popData.length]; for(int i=0;i<otrCladeDistr.length;i++){otrCladeDistr[i]=0;}
		int[] temp;
		SubCladeData scd;
		if(test.significantlySmallDn(SIGNIFICANCE_LEVEL)){ 
			for(Iterator i= subCladeData.iterator();i.hasNext();){
				scd = (SubCladeData) i.next();
				temp = scd.getDistribution();
				if(scd.isTip()){
					for(int j = 0; j < temp.length; j++){
						sigCladeDistr[j] += temp[j];
					}
				} else {
					for(int j = 0; j < temp.length; j++){
						otrCladeDistr[j] += temp[j];
					}
				}
			} 
		} else {
			for(Iterator i= subCladeData.iterator();i.hasNext();){
				scd = (SubCladeData) i.next();
				temp = scd.getDistribution();
				if(scd.significantlyLargeDn(SIGNIFICANCE_LEVEL)){
					for(int j = 0; j < temp.length; j++){
						sigCladeDistr[j] += temp[j];
					}
				} else {
					for(int j = 0; j < temp.length; j++){
						otrCladeDistr[j] += temp[j];
					}
				}
			} 
		}
		// for other clades calculate the geographic center
		double meanLat = 0.0;
		double meanLon = 0.0;
		double numInd = 0.0;
		for(int i=0; i< popData.length; i++){
			meanLat += (popData[i].getLatitude()*(double)otrCladeDistr[i]);
			meanLon += (popData[i].getLongitude()*(double)otrCladeDistr[i]);
			numInd += (double)otrCladeDistr[i];
		}
		if(numInd > 0){
			PopulationData geogCentre = new PopulationData(meanLat/numInd,meanLon/numInd,false); // dummy population
			return isSampledPopulationBetween(sigCladeDistr,geogCentre,true);
		} else {
			return false; // since there is no centre of the other geographical clades
		}
	}
	
	public boolean question14(){ // Checked - Reasoned - Correct (I believe)
		// Does there exist a unsampled area (a unsampled population) between clades with Dn significantly large and the geographic centre of the other clades.
		// (or if I-T Dn is significantly small does there exist a sampled area between tips and other clades)
		int[] sigCladeDistr = new int[popData.length]; for(int i=0;i<sigCladeDistr.length;i++){sigCladeDistr[i]=0;}
		int[] otrCladeDistr = new int[popData.length]; for(int i=0;i<otrCladeDistr.length;i++){otrCladeDistr[i]=0;}
		int[] temp;
		SubCladeData scd;
		if(test.significantlySmallDn(SIGNIFICANCE_LEVEL)){ 
			for(Iterator i= subCladeData.iterator();i.hasNext();){
				scd = (SubCladeData) i.next();
				temp = scd.getDistribution();
				if(scd.isTip()){
					for(int j = 0; j < temp.length; j++){
						sigCladeDistr[j] += temp[j];
					}
				} else {
					for(int j = 0; j < temp.length; j++){
						otrCladeDistr[j] += temp[j];
					}
				}
			} 
		} else {
			for(Iterator i= subCladeData.iterator();i.hasNext();){
				scd = (SubCladeData) i.next();
				temp = scd.getDistribution();
				if(scd.significantlyLargeDn(SIGNIFICANCE_LEVEL)){
					for(int j = 0; j < temp.length; j++){
						sigCladeDistr[j] += temp[j];
					}
				} else {
					for(int j = 0; j < temp.length; j++){
						otrCladeDistr[j] += temp[j];
					}
				}
			} 
		}
		// calculate geographic center of other clades
		double meanLat = 0.0;
		double meanLon = 0.0;
		double numInd = 0.0;
		for(int i=0; i< popData.length; i++){
			meanLat += (popData[i].getLatitude()*(double)otrCladeDistr[i]);
			meanLon += (popData[i].getLongitude()*(double)otrCladeDistr[i]);
			numInd += (double)otrCladeDistr[i];
		}
		if(numInd > 0){
			PopulationData geogCentre = new PopulationData(meanLat/numInd,meanLon/numInd,false); // dummy population
			return isUnsampledPopulationBetween(sigCladeDistr,geogCentre,true); 
		} else {
			return false; // since there is no centre of the other geographical clades
		}
	}
	
	public boolean question15(){ // Checked - Reasoned - Correct (I believe)
		// More or less same as question 9
		return question9();
	}
	
	public boolean question16(){ // Checked - Reasoned - Correct (I believe)
		// same as question 10
		return question10();
	}
	
	public boolean question17(){ // Checked - Reasoned - Correct (I believe)
		return question17a() || question17b() || question17c();
	}
	
	private boolean question17a(){ // Checked - Reasoned - Correct (I believe)
		boolean b1 = true; // For all tips Dn is significantly small
		boolean b2 = false; // or (there exists at least one interior with Dn significantly small
		boolean b3 = false; // and there exists at least one interior with Dn not significantly small)
		SubCladeData scd;
		for(Iterator i = subCladeData.iterator(); i.hasNext();){
			scd = (SubCladeData) i.next();
			if(scd.isTip()){
				if(!scd.significantlySmallDn(SIGNIFICANCE_LEVEL)){
					b1 = false;
				}
			} else {
				if(scd.significantlySmallDn(SIGNIFICANCE_LEVEL)){
					b2 = true;
				} else {
					b3 = true;
				}
			}
		}
		return b1 || (b2 && b3);
	}
	
	private boolean question17b(){ // Checked - Reasoned - Correct (I believe)
		// There exists at least one interior with Dn significantly large
		SubCladeData scd;
		for(Iterator i = subCladeData.iterator(); i.hasNext();){
			scd = (SubCladeData) i.next();
			if(!scd.isTip() && scd.significantlyLargeDn(SIGNIFICANCE_LEVEL)){
				return true;
			}
		}
		return false;
	}
	
	private boolean question17c(){ // Checked - Reasoned - Correct (I believe)
		// The I-T Dn is significantly large
		return test.significantlyLargeDn(SIGNIFICANCE_LEVEL);
	}
	
	public boolean question18(){ // Checked - Reasoned - Correct (I believe)
		// Is the branch length between the two defined groups larger than the average number of
		// mutational steps in the sub haplotype network in this nesting clade
		int level; // will never be 0
		try{
			level = Integer.parseInt(cladeLabel.split("\\s+")[1].split("-")[0]);
		} catch (NumberFormatException e){ // label == Total Cladogram
			level = nsteplevels.getLevelsOfNesting()-1; // number of levels minus 1
		}
		Clade nestingClade = null;
		for(Iterator i = nsteplevels.getNestingLevel(level).iterator();i.hasNext();){
			Clade clade = (Clade) i.next();
			if(clade.getLabel().equals(cladeLabel)){
				nestingClade = clade;
			}
		}
		ArrayList hapList = nestingClade.getListOfHaplotypes();
		NStepNetwork subNetwork = new NStepNetwork(0); // construct the sub network
		int numSampledClades = 0;
		for(Iterator i = hapList.iterator();i.hasNext();){
			HaplotypeClade hclade = (HaplotypeClade) i.next();
			if(hclade.getLabel().equals(HaplotypeClade.INFERRED_LABEL)){
				subNetwork.add(new HaplotypeClade(hclade.getID(),HaplotypeClade.INFERRED_LABEL));
			} else {
				subNetwork.add(new HaplotypeClade(hclade.getID(),hclade.getLabel()));
				numSampledClades ++;
			}
		}
		for(Iterator i = hapList.iterator();i.hasNext();){
			HaplotypeClade hclade = (HaplotypeClade) i.next();
			for(Iterator j = hclade.getEdges().iterator();j.hasNext();){
				HaplotypeClade conClade = (HaplotypeClade) j.next();
				if(hapList.contains(conClade)){
					subNetwork.getClade(hclade.getID()).setEdge(subNetwork.getClade(conClade.getID()));
				}
			}
		}
		// Now have a connected subnetwork
		ArrayList visited = new ArrayList();
		int numEdges = dfs((Clade) subNetwork.get(0),visited);
		double averageMutationalSteps = (double)numEdges/(double)numSampledClades; // There should never be division by zero here;
		// Now have average number of mutational steps - Need the shortest branch length between the two groups
		ArrayList smallDcGroup = new ArrayList();
		ArrayList otherGroup = new ArrayList();
		for(Iterator i = subCladeData.iterator();i.hasNext();){
			SubCladeData scd = (SubCladeData) i.next();
			if(scd.significantlySmallDc(SIGNIFICANCE_LEVEL)){
				for(Iterator j = nsteplevels.getNestingLevel(level-1).iterator();j.hasNext();){
					Clade subClade = (Clade) j.next();
					if(subClade instanceof HaplotypeClade){
						if(subClade.getLabel().equals(scd.getCladeLabel().split("\\s+")[1])){
							smallDcGroup.addAll(subClade.getListOfHaplotypes());
						}
					} else {
						if(subClade.getLabel().split("\\s+")[1].equals(scd.getCladeLabel().split("\\s+")[1])){
							smallDcGroup.addAll(subClade.getListOfHaplotypes());
						}
					}
				}
			} else {
				for(Iterator j = nsteplevels.getNestingLevel(level-1).iterator();j.hasNext();){
					Clade subClade = (Clade) j.next();
					if(subClade instanceof HaplotypeClade){
						if(subClade.getLabel().equals(scd.getCladeLabel().split("\\s+")[1])){
							otherGroup.addAll(subClade.getListOfHaplotypes());
						}
					} else {
						if(subClade.getLabel().split("\\s+")[1].equals(scd.getCladeLabel().split("\\s+")[1])){
							otherGroup.addAll(subClade.getListOfHaplotypes());
						}
					}
				}
			}
		}
		int minBranchLength = Integer.MAX_VALUE;
		for(Iterator i = smallDcGroup.iterator(); i.hasNext();){
			Clade start = subNetwork.getClade(((Clade) i.next()).getID());
			visited = new ArrayList(); 
			visited.add(start);
			ArrayList queue = new ArrayList();
			queue.add(start);
			queue.add(new Object()); // object is a level marker
			int counter = 0; // this counts the bfs level == branch length
			boolean otherClade = false; // Break statements bad.
			while(!otherClade){ // (messy) bfs;
				Object thing = queue.remove(0);
				try{
					Clade searchNode = (Clade) thing;
					for(Iterator j = searchNode.getEdges().iterator();j.hasNext() && !otherClade;){
						Clade cld = (Clade) j.next();
						if(!visited.contains(cld)){
							for(Iterator k = otherGroup.iterator();k.hasNext() && !otherClade;){
								Clade otrCld = (Clade) k.next();
								if(cld.getID() == otrCld.getID()){
									otherClade = true;
								}
							}
							if(!otherClade){
								visited.add(cld);
								queue.add(cld);
							}
						}
					}
				} catch (ClassCastException e){
					queue.add(new Object());
					counter++;
				}
			}
			if(counter < minBranchLength){
				minBranchLength = counter;
			}
		}
		return averageMutationalSteps < ((double) minBranchLength);
	}
	
	public boolean question19(){ // Checked - Reasoned - Correct (I believe)
		// Is the species present (sampled or unsampled) within the region (A)
		// Version 1.2 - Changed to exclude area within subclade boundary.
		int[] cladeDistr = new int[popData.length]; 
		for(int i=0;i<cladeDistr.length;i++){cladeDistr[i]=0;}
		int[] temp;
		for(Iterator i = subCladeData.iterator();i.hasNext();){
			temp = ((SubCladeData) i.next()).getDistribution();
			for(int j = 0; j < cladeDistr.length;j++){
				cladeDistr[j] += temp[j];
			}
		}
		boolean insideA = false;
		for(int i = 0; i < popData.length && !insideA; i++){
			if(cladeDistr[i] == 0){ // not members of this clade
				if(insideArea(popData[i],cladeDistr,true)){ // checking the boundary too
					insideA = true;
					//return true;
					for(Iterator sci = subCladeData.iterator();sci.hasNext() && insideA;){ // check inside subclades
						if(insideArea(popData[i],((SubCladeData) sci.next()).getDistribution(),true)){
							insideA = false; // inside a subclade, so not insideA
						}
					}
				}	
			}
		}
		return insideA;
	}
	
	public boolean question20(){ // Checked - Reasoned - Correct (I believe)
		// Is there a sampled species within the region (A)
		// Version 1.2 - Changed to exclude area within subclade boundary.
		int[] cladeDistr = new int[popData.length]; 
		for(int i=0;i<cladeDistr.length;i++){cladeDistr[i]=0;}
		int[] temp;
		for(Iterator i = subCladeData.iterator();i.hasNext();){
			temp = ((SubCladeData) i.next()).getDistribution();
			for(int j = 0; j < cladeDistr.length;j++){
				cladeDistr[j] += temp[j];
			}
		}
		boolean insideA = false;
		for(int i = 0; i < popData.length && !insideA; i++){
			if(popData[i].isSampled() && cladeDistr[i] == 0){ // not members of this clade but sampled
			//if(popData[i].isSampled()){ // not sampled population
				if(insideArea(popData[i],cladeDistr,true)){ // checking the boundary too
					insideA = true;
					//return true;
					for(Iterator sci = subCladeData.iterator();sci.hasNext() && insideA;){ // check inside subclades
						if(insideArea(popData[i],((SubCladeData) sci.next()).getDistribution(),true)){
							insideA = false; // inside a subclade, so not insideA
						}
					}
				}
			}
		}
		return insideA;
	}
	
	/*public boolean question21(){
		return true;
	}*/
	
	// package access methods
	void addNextCladeData(CladeData c){
		nextData = c;
	}
	
	boolean hasNextCladeData(){
		return nextData != null ;
	}
	
	CladeData getNextCladeData(){
		return nextData;
	}
	
	void setCladeDataChain(CladeDataChain c){
		owner = c;
	}
	
	// private access methods
	private boolean isSampledPopulationBetween(int[] sigClades, int[] otherClades, boolean chkrad){
		int[] popDistr = new int[popData.length];
		for(int i =0 ; i < popDistr.length; i++){
			popDistr[i] = sigClades[i] + otherClades[i];
		}
		for(int j = 0; j < popDistr.length;j++){
			if(popData[j].isSampled() && popDistr[j] == 0){ // sampled but not in nesting clade
				PopulationData pd = popData[j];
				boolean insideABC = false;
				boolean checkRad = chkrad;
				boolean nextPopulation = false;
				ArrayList listofvertices = new ArrayList();
				for(int i =0; i < popData.length;i++){
					if(popData[i].isSampled() && popDistr[i] > 0){
						listofvertices.add(popData[i]);
					}
				}
				Collections.sort(listofvertices);
				Stack thehull = new Stack();
				int minmin = 0;
				int minmax = minmin;
				int maxmax = listofvertices.size()-1; 
				int maxmin = maxmax;
				boolean found = false;
				PopulationData temppd = (PopulationData) listofvertices.get(minmin);
				for(int i = 1; i < listofvertices.size() && !found ;i++){ 
					if(temppd.getLongitude() != ((PopulationData) listofvertices.get(i)).getLongitude()){
					//if(Math.abs(temppd.getLatitude() - ((PopulationData) listofvertices.get(i)).getLatitude())> TOLERANCE){
						found = true;
						minmax = i-1;
					}
				}
				if(minmax == maxmax){
					PopulationData pdmin = (PopulationData) listofvertices.get(minmin);
					PopulationData pdmax = (PopulationData) listofvertices.get(maxmax);
					if ((perpProduct(pd,pdmin,pdmax) == 0) && (pd.getLatitude() >= pdmin.getLatitude()) && (pd.getLatitude() <= pdmax.getLatitude())){
						insideABC = true;
						checkRad = false;
					} else {
						if(chkrad){
							double dist; 
							if(pd.getLatitude() < pdmin.getLatitude()){
								dist = greatCircleDistance(pd,pdmin);
							} else if (pd.getLatitude() > pdmax.getLatitude()){
								dist = greatCircleDistance(pd,pdmax);
							} else {
								dist = greatCircleDistance(pd.getLatitude(),pd.getLongitude(),pdmax.getLatitude(),pd.getLongitude());
							}
							if(pd.getPopulationRadius() >= dist){
								insideABC = true;
								checkRad = true;
							}
						} else {
							nextPopulation = true;
						}
					}
				}
				if(!nextPopulation && !insideABC){
					found = false;
					temppd = (PopulationData) listofvertices.get(maxmax);
					for(int i = maxmax-1; i >= 0 && !found; i-- ){
						if(temppd.getLongitude() != ((PopulationData) listofvertices.get(i)).getLongitude()){
						//if(Math.abs(temppd.getLatitude() - ((PopulationData) listofvertices.get(i)).getLatitude()) > TOLERANCE){
							found = true;
							maxmin = i+1;
						}
					}
					PopulationData ptop = (PopulationData) listofvertices.get(minmin);
					PopulationData pnexttotop  = ptop;
					PopulationData pi;
					thehull.push(ptop);
					for(int i = minmax+1; i <= maxmin; i++){
						pi = (PopulationData) listofvertices.get(i);
						if(perpProduct((PopulationData) listofvertices.get(minmin), (PopulationData) listofvertices.get(maxmin), pi) >=0){ 
							while(thehull.size() > 1 && perpProduct(pnexttotop,ptop,pi) >= 0){
								thehull.pop();
								if(thehull.size() > 1){
									ptop = (PopulationData) thehull.pop();
									pnexttotop = (PopulationData) thehull.peek();
									thehull.push(ptop); 
								}
							}
							pnexttotop = (PopulationData) thehull.peek();
							ptop = pi;
							thehull.push(ptop);
						}
					}
					if(maxmax != maxmin){
						thehull.push(listofvertices.get(maxmax));
					}
					int lowbound = thehull.size();
					for(int i=maxmin-1; i >= minmax; i--){
						pi = (PopulationData) listofvertices.get(i);
						if(perpProduct((PopulationData) listofvertices.get(maxmax), (PopulationData) listofvertices.get(minmax), pi) >=0 ){
							while(thehull.size() > lowbound && perpProduct(pnexttotop,ptop,pi) >= 0 ){
								thehull.pop();
								if(thehull.size() > lowbound){
									ptop = (PopulationData) thehull.pop();
									pnexttotop = (PopulationData) thehull.peek();
									thehull.push(ptop);
								}
							}
							pnexttotop = (PopulationData) thehull.peek();
							ptop = pi;
							thehull.push(ptop);
						}
					}
					if(minmax != minmin){
						thehull.push(listofvertices.get(minmin));
					} 
					/*int windingnumber = 0;
					PopulationData piplus1;
					pi = (PopulationData) thehull.get(0);
					for(int i = 0; i < thehull.size()-1; i++){
						piplus1 = (PopulationData) thehull.get(i+1);
						if(pi.getLongitude() <= pd.getLongitude()){
							if(piplus1.getLongitude() > pd.getLongitude()){
								if(perpProduct(pi,piplus1,pd) > 0){ 
									windingnumber++;
								}
							}
						} else {
							if(piplus1.getLongitude() <= pd.getLongitude()){
								if(perpProduct(pi,piplus1,pd) > 0){
									windingnumber--;
								}
							}
						}
						pi = piplus1;
					}*/
					if(windingNumber(pd,thehull) == 0){
						if(chkrad){
							double mindist = Double.MAX_VALUE; 
							double dist;
							temppd = (PopulationData) thehull.get(0);
							int index = 0;
							for(int i=0; i< thehull.size()-1;i++){
								pi = (PopulationData) thehull.get(i);
								dist = greatCircleDistance(pd,pi);
								if(mindist > dist){
									mindist = dist;
									index = i;
									temppd = pi;
								}
							}
							PopulationData pi1 = (PopulationData) thehull.get(index+1);
							PopulationData pi2;
							if(index == 0){
								pi2 = (PopulationData) thehull.get(thehull.size()-2);
							} else {
								pi2 = (PopulationData) thehull.get(index-1);
							}
							if(perpProduct(temppd,pi1,pd) < 0 && perpProduct(temppd,pd,pi2) < 0){
								double maxangle = 1;
								double angle;
								PopulationData point1, point2;
								for(int k = 0;k< thehull.size()-1;k++){
									pi1 = (PopulationData) thehull.get(k);
									pi2 = (PopulationData) thehull.get(k+1);
									angle = dotProduct(pi1,pd,pi2)/(magnitude(pi1,pd)*magnitude(pi2,pd));
									if(maxangle > angle){
										maxangle = angle;
										point1 = pi1;
										point2 = pi2;
									}
								}
								//double[] pdplanecp = crossProduct(addVector(toCartesian(pd),subtractVector(toCartesian(pi1),toCartesian(pi2))),toCartesian(pd));
								//double[] pi1planecp = crossProduct(pi1,pi2);
								//dist = RADIUS*Math.acos(dotProduct(pdplanecp,pi1planecp)/(magnitude(pdplanecp)*magnitude(pi1planecp)));
								dist = perpDistance(pd,pi1,pi2);
							} else {
								if(dotProduct(pd,temppd,pi1) > 0){
									//double[] pdplanecp = crossProduct(addVector(toCartesian(pd),subtractVector(toCartesian(temppd),toCartesian(pi1))),toCartesian(pd));
									//double[] pi1planecp = crossProduct(temppd,pi1);
									//dist = RADIUS*Math.acos(dotProduct(pdplanecp,pi1planecp)/(magnitude(pdplanecp)*magnitude(pi1planecp)));
									dist = perpDistance(pd,temppd,pi1);
								} else if (dotProduct(pd,temppd,pi2) > 0){
									//double[] pdplanecp = crossProduct(addVector(toCartesian(pd),subtractVector(toCartesian(temppd),toCartesian(pi2))),toCartesian(pd));
									//double[] pi1planecp = crossProduct(temppd,pi2);
									//dist = RADIUS*Math.acos(dotProduct(pdplanecp,pi1planecp)/(magnitude(pdplanecp)*magnitude(pi1planecp)));
									dist = perpDistance(pd,temppd,pi2);
								} else {
									dist = greatCircleDistance(pd,temppd);
								}
							}
							if (pd.getPopulationRadius()>= dist){
								insideABC = true;
								checkRad = true;
							}
						}
					} else {
						insideABC = true; // winding number > 0 => inside
					}
				}
				if(insideABC){
					if(!insideArea(pd,sigClades,checkRad) && !insideArea(pd,otherClades,checkRad)){
						return true;
					}
				}
			}
		}
		return false;
	}
	
	private boolean isPopulationBetweenAndAllSampled(int[] sigClades, int[] otherClades, boolean chkrad){
		boolean sampledPopInside = false;
		int[] popDistr = new int[popData.length];
		for(int i =0 ; i < popDistr.length; i++){
			popDistr[i] = sigClades[i] + otherClades[i];
		}
		for(int j = 0; j < popDistr.length;j++){
			if(popDistr[j] == 0){ // not in nesting clade
				PopulationData pd = popData[j];
				boolean insideABC = false;
				boolean checkRad = chkrad;
				boolean nextPopulation = false;
				ArrayList listofvertices = new ArrayList();
				for(int i =0; i < popData.length;i++){
					if(popData[i].isSampled() && popDistr[i] > 0){
						listofvertices.add(popData[i]);
					}
				}
				Collections.sort(listofvertices);
				Stack thehull = new Stack();
				int minmin = 0;
				int minmax = minmin;
				int maxmax = listofvertices.size()-1; 
				int maxmin = maxmax;
				boolean found = false;
				PopulationData temppd = (PopulationData) listofvertices.get(minmin);
				for(int i = 1; i < listofvertices.size() && !found ;i++){ 
					if(temppd.getLongitude() != ((PopulationData) listofvertices.get(i)).getLongitude()){
					//if(Math.abs(temppd.getLatitude() - ((PopulationData) listofvertices.get(i)).getLatitude()) > TOLERANCE){
						found = true;
						minmax = i-1;
					}
				}
				if(minmax == maxmax){
					PopulationData pdmin = (PopulationData) listofvertices.get(minmin);
					PopulationData pdmax = (PopulationData) listofvertices.get(maxmax);
					if ((perpProduct(pd,pdmin,pdmax) == 0) && (pd.getLatitude() >= pdmin.getLatitude()) && (pd.getLatitude() <= pdmax.getLatitude())){
						insideABC = true;
						checkRad = false;
					} else {
						if(chkrad){
							double dist; 
							if(pd.getLatitude() < pdmin.getLatitude()){
								dist = greatCircleDistance(pd,pdmin);
							} else if (pd.getLatitude() > pdmax.getLatitude()){
								dist = greatCircleDistance(pd,pdmax);
							} else {
								dist = greatCircleDistance(pd.getLatitude(),pd.getLongitude(),pdmax.getLatitude(),pd.getLongitude());
							}
							if(pd.getPopulationRadius() >= dist){
								insideABC = true;
								checkRad = true;
							}
						} else {
							nextPopulation = true;
						}
					}
				}
				if(!nextPopulation && !insideABC){
					found = false;
					temppd = (PopulationData) listofvertices.get(maxmax);
					for(int i = maxmax-1; i >= 0 && !found; i-- ){
						if(temppd.getLongitude() != ((PopulationData) listofvertices.get(i)).getLongitude()){
						//if(Math.abs(temppd.getLatitude() - ((PopulationData) listofvertices.get(i)).getLatitude()) > TOLERANCE){
							found = true;
							maxmin = i+1;
						}
					}
					PopulationData ptop = (PopulationData) listofvertices.get(minmin);
					PopulationData pnexttotop  = ptop;
					PopulationData pi;
					thehull.push(ptop);
					for(int i = minmax+1; i <= maxmin; i++){
						pi = (PopulationData) listofvertices.get(i);
						if(perpProduct((PopulationData) listofvertices.get(minmin), (PopulationData) listofvertices.get(maxmin), pi) >=0){ 
							while(thehull.size() > 1 && perpProduct(pnexttotop,ptop,pi) >= 0){
								thehull.pop();
								if(thehull.size() > 1){
									ptop = (PopulationData) thehull.pop();
									pnexttotop = (PopulationData) thehull.peek();
									thehull.push(ptop); 
								}
							}
							pnexttotop = (PopulationData) thehull.peek();
							ptop = pi;
							thehull.push(ptop);
						}
					}
					if(maxmax != maxmin){
						thehull.push(listofvertices.get(maxmax));
					}
					int lowbound = thehull.size();
					for(int i=maxmin-1; i >= minmax; i--){
						pi = (PopulationData) listofvertices.get(i);
						if(perpProduct((PopulationData) listofvertices.get(maxmax), (PopulationData) listofvertices.get(minmax), pi) >=0 ){
							while(thehull.size() > lowbound && perpProduct(pnexttotop,ptop,pi) >= 0 ){
								thehull.pop();
								if(thehull.size() > lowbound){
									ptop = (PopulationData) thehull.pop();
									pnexttotop = (PopulationData) thehull.peek();
									thehull.push(ptop);
								}
							}
							pnexttotop = (PopulationData) thehull.peek();
							ptop = pi;
							thehull.push(ptop);
						}
					}
					if(minmax != minmin){
						thehull.push(listofvertices.get(minmin));
					} 
					/*int windingnumber = 0;
					PopulationData piplus1;
					pi = (PopulationData) thehull.get(0);
					for(int i = 0; i < thehull.size()-1; i++){
						piplus1 = (PopulationData) thehull.get(i+1);
						if(pi.getLongitude() <= pd.getLongitude()){
							if(piplus1.getLongitude() > pd.getLongitude()){
								if(perpProduct(pi,piplus1,pd) > 0){ 
									windingnumber++;
								}
							}
						} else {
							if(piplus1.getLongitude() <= pd.getLongitude()){
								if(perpProduct(pi,piplus1,pd) > 0){
									windingnumber--;
								}
							}
						}
						pi = piplus1;
					}*/
					if(windingNumber(pd,thehull) == 0){
						if(chkrad){
							double mindist = Double.MAX_VALUE; 
							double dist;
							temppd = (PopulationData) thehull.get(0);
							int index = 0;
							for(int i=0; i< thehull.size()-1;i++){
								pi = (PopulationData) thehull.get(i);
								dist = greatCircleDistance(pd,pi);
								if(mindist > dist){
									mindist = dist;
									index = i;
									temppd = pi;
								}
							}
							PopulationData pi1 = (PopulationData) thehull.get(index+1);
							PopulationData pi2;
							if(index == 0){
								pi2 = (PopulationData) thehull.get(thehull.size()-2);
							} else {
								pi2 = (PopulationData) thehull.get(index-1);
							}
							if(perpProduct(temppd,pi1,pd) < 0 && perpProduct(temppd,pd,pi2) < 0){
								double maxangle = 1;
								double angle;
								PopulationData point1, point2;
								for(int k = 0;k< thehull.size()-1;k++){
									pi1 = (PopulationData) thehull.get(k);
									pi2 = (PopulationData) thehull.get(k+1);
									angle = dotProduct(pi1,pd,pi2)/(magnitude(pi1,pd)*magnitude(pi2,pd));
									if(maxangle > angle){
										maxangle = angle;
										point1 = pi1;
										point2 = pi2;
									}
								}
								//double[] pdplanecp = crossProduct(addVector(toCartesian(pd),subtractVector(toCartesian(pi1),toCartesian(pi2))),toCartesian(pd));
								//double[] pi1planecp = crossProduct(pi1,pi2);
								//dist = RADIUS*Math.acos(dotProduct(pdplanecp,pi1planecp)/(magnitude(pdplanecp)*magnitude(pi1planecp)));
								dist = perpDistance(pd,pi1,pi2);
							} else {
								if(dotProduct(pd,temppd,pi1) > 0){
									//double[] pdplanecp = crossProduct(addVector(toCartesian(pd),subtractVector(toCartesian(temppd),toCartesian(pi1))),toCartesian(pd));
									//double[] pi1planecp = crossProduct(temppd,pi1);
									//dist = RADIUS*Math.acos(dotProduct(pdplanecp,pi1planecp)/(magnitude(pdplanecp)*magnitude(pi1planecp)));
									dist = perpDistance(pd,temppd,pi1);
								} else if (dotProduct(pd,temppd,pi2) > 0){
									//double[] pdplanecp = crossProduct(addVector(toCartesian(pd),subtractVector(toCartesian(temppd),toCartesian(pi2))),toCartesian(pd));
									//double[] pi1planecp = crossProduct(temppd,pi2);
									//dist = RADIUS*Math.acos(dotProduct(pdplanecp,pi1planecp)/(magnitude(pdplanecp)*magnitude(pi1planecp)));
									dist = perpDistance(pd,temppd,pi2);
								} else {
									dist = greatCircleDistance(pd,temppd);
								}
							}
							if (pd.getPopulationRadius() >= dist){
								insideABC = true;
								checkRad = true;
							}
						}
					} else {
						insideABC = true; // winding number > 0 => inside
					}
				}
				if(insideABC){
					if(!insideArea(pd,sigClades,checkRad) && !insideArea(pd,otherClades,checkRad)){
						if(!pd.isSampled()){
							return false; 
						} else {
							sampledPopInside = true;
						}
					}
				}
			}
		}
		return sampledPopInside;
	}
	
	private boolean isUnsampledPopulationBetween(int[] sigClades, int[] otherClades, boolean chkrad){
		int[] popDistr = new int[popData.length];
		for(int i =0 ; i < popDistr.length; i++){
			popDistr[i] = sigClades[i] + otherClades[i];
		}
		for(int j = 0; j < popDistr.length;j++){
			if(!popData[j].isSampled()){
				PopulationData pd = popData[j];
				boolean insideABC = false;
				boolean checkRad = chkrad;
				boolean nextPopulation = false;
				ArrayList listofvertices = new ArrayList();
				for(int i =0; i < popData.length;i++){
					if(popData[i].isSampled() && popDistr[i] > 0){
						listofvertices.add(popData[i]);
					}
				}
				Collections.sort(listofvertices);
				Stack thehull = new Stack();
				int minmin = 0;
				int minmax = minmin;
				int maxmax = listofvertices.size()-1; 
				int maxmin = maxmax;
				boolean found = false;
				PopulationData temppd = (PopulationData) listofvertices.get(minmin);
				for(int i = 1; i < listofvertices.size() && !found ;i++){ 
					if(temppd.getLongitude() != ((PopulationData) listofvertices.get(i)).getLongitude()){
					//if(Math.abs(temppd.getLatitude() - ((PopulationData) listofvertices.get(i)).getLatitude()) > TOLERANCE){
						found = true;
						minmax = i-1;
					}
				}
				if(minmax == maxmax){
					PopulationData pdmin = (PopulationData) listofvertices.get(minmin);
					PopulationData pdmax = (PopulationData) listofvertices.get(maxmax);
					if ((perpProduct(pd,pdmin,pdmax) == 0) && (pd.getLatitude() >= pdmin.getLatitude()) && (pd.getLatitude() <= pdmax.getLatitude())){
						insideABC = true;
						checkRad = false;
					} else {
						if(chkrad){
							double dist; 
							if(pd.getLatitude() < pdmin.getLatitude()){
								dist = greatCircleDistance(pd,pdmin);
							} else if (pd.getLatitude() > pdmax.getLatitude()){
								dist = greatCircleDistance(pd,pdmax);
							} else {
								dist = greatCircleDistance(pd.getLatitude(),pd.getLongitude(),pdmax.getLatitude(),pd.getLongitude());
							}
							if(pd.getPopulationRadius() >= dist){
								insideABC = true;
								checkRad = true;
							}
						} else {
							nextPopulation = true;
						}
					}
				}
				if(!nextPopulation && !insideABC){
					found = false;
					temppd = (PopulationData) listofvertices.get(maxmax);
					for(int i = maxmax-1; i >= 0 && !found; i-- ){
						if(temppd.getLongitude() != ((PopulationData) listofvertices.get(i)).getLongitude()){
						//if(Math.abs(temppd.getLatitude() - ((PopulationData) listofvertices.get(i)).getLatitude()) > TOLERANCE){
							found = true;
							maxmin = i+1;
						}
					}
					PopulationData ptop = (PopulationData) listofvertices.get(minmin);
					PopulationData pnexttotop  = ptop;
					PopulationData pi;
					thehull.push(ptop);
					for(int i = minmax+1; i <= maxmin; i++){
						pi = (PopulationData) listofvertices.get(i);
						if(perpProduct((PopulationData) listofvertices.get(minmin), (PopulationData) listofvertices.get(maxmin), pi) >=0){ 
							while(thehull.size() > 1 && perpProduct(pnexttotop,ptop,pi) >= 0){
								thehull.pop();
								if(thehull.size() > 1){
									ptop = (PopulationData) thehull.pop();
									pnexttotop = (PopulationData) thehull.peek();
									thehull.push(ptop); 
								}
							}
							pnexttotop = (PopulationData) thehull.peek();
							ptop = pi;
							thehull.push(ptop);
						}
					}
					if(maxmax != maxmin){
						thehull.push(listofvertices.get(maxmax));
					}
					int lowbound = thehull.size();
					for(int i=maxmin-1; i >= minmax; i--){
						pi = (PopulationData) listofvertices.get(i);
						if(perpProduct((PopulationData) listofvertices.get(maxmax), (PopulationData) listofvertices.get(minmax), pi) >=0 ){
							while(thehull.size() > lowbound && perpProduct(pnexttotop,ptop,pi) >= 0 ){
								thehull.pop();
								if(thehull.size() > lowbound){
									ptop = (PopulationData) thehull.pop();
									pnexttotop = (PopulationData) thehull.peek();
									thehull.push(ptop);
								}
							}
							pnexttotop = (PopulationData) thehull.peek();
							ptop = pi;
							thehull.push(ptop);
						}
					}
					if(minmax != minmin){
						thehull.push(listofvertices.get(minmin));
					} 
					/*int windingnumber = 0;
					PopulationData piplus1;
					pi = (PopulationData) thehull.get(0);
					for(int i = 0; i < thehull.size()-1; i++){
						piplus1 = (PopulationData) thehull.get(i+1);
						if(pi.getLongitude() <= pd.getLongitude()){
							if(piplus1.getLongitude() > pd.getLongitude()){
								if(perpProduct(pi,piplus1,pd) < 0){ 
									windingnumber++;
								}
							}
						} else {
							if(piplus1.getLongitude() <= pd.getLongitude()){
								if(perpProduct(pi,piplus1,pd) > 0){
									windingnumber--;
								}
							}
						}
						pi = piplus1;
					}*/
					if(windingNumber(pd,thehull) == 0){
						if(chkrad){
							double mindist = Double.MAX_VALUE; 
							double dist;
							temppd = (PopulationData) thehull.get(0);
							int index = 0;
							for(int i=0; i< thehull.size()-1;i++){
								pi = (PopulationData) thehull.get(i);
								dist = greatCircleDistance(pd,pi);
								if(mindist > dist){
									mindist = dist;
									index = i;
									temppd = pi;
								}
							}
							PopulationData pi1 = (PopulationData) thehull.get(index+1);
							PopulationData pi2;
							if(index == 0){
								pi2 = (PopulationData) thehull.get(thehull.size()-2);
							} else {
								pi2 = (PopulationData) thehull.get(index-1);
							}
							if(perpProduct(temppd,pi1,pd) < 0 && perpProduct(temppd,pd,pi2) < 0){
								double maxangle = 1;
								double angle;
								PopulationData point1, point2;
								for(int k = 0;k< thehull.size()-1;k++){
									pi1 = (PopulationData) thehull.get(k);
									pi2 = (PopulationData) thehull.get(k+1);
									angle = dotProduct(pi1,pd,pi2)/(magnitude(pi1,pd)*magnitude(pi2,pd));
									if(maxangle > angle){
										maxangle = angle;
										point1 = pi1;
										point2 = pi2;
									}
								}
								//double[] pdplanecp = crossProduct(addVector(toCartesian(pd),subtractVector(toCartesian(pi1),toCartesian(pi2))),toCartesian(pd));
								//double[] pi1planecp = crossProduct(pi1,pi2);
								//dist = RADIUS*Math.acos(dotProduct(pdplanecp,pi1planecp)/(magnitude(pdplanecp)*magnitude(pi1planecp)));
								dist = perpDistance(pd,pi1,pi2);
							} else {
								if(dotProduct(pd,temppd,pi1) > 0){
									//double[] pdplanecp = crossProduct(addVector(toCartesian(pd),subtractVector(toCartesian(temppd),toCartesian(pi1))),toCartesian(pd));
									//double[] pi1planecp = crossProduct(temppd,pi1);
									//dist = RADIUS*Math.acos(dotProduct(pdplanecp,pi1planecp)/(magnitude(pdplanecp)*magnitude(pi1planecp)));
									dist = perpDistance(pd,temppd,pi1);
								} else if (dotProduct(pd,temppd,pi2) > 0){
									//double[] pdplanecp = crossProduct(addVector(toCartesian(pd),subtractVector(toCartesian(temppd),toCartesian(pi2))),toCartesian(pd));
									//double[] pi1planecp = crossProduct(temppd,pi2);
									//dist = RADIUS*Math.acos(dotProduct(pdplanecp,pi1planecp)/(magnitude(pdplanecp)*magnitude(pi1planecp)));
									dist = perpDistance(pd,temppd,pi2);
								} else {
									dist = greatCircleDistance(pd,temppd);
								}
							}
							if (pd.getPopulationRadius() >= dist){
								insideABC = true;
								checkRad = true;
							}
						}
					} else {
						insideABC = true; // winding number > 0 => inside
					}
				}
				if(insideABC){
					if(!insideArea(pd,sigClades,checkRad) && !insideArea(pd,otherClades,checkRad)){
						return true;
					}
				}
			}
		}
		return false;
	}
	
	private boolean isSampledPopulationBetween(int[] popDistr, PopulationData geogCentre, boolean chkrad){
		for(int j = 0; j < popDistr.length;j++){
			if(popData[j].isSampled() && popDistr[j] == 0){ // sampled but not in nesting clade
				PopulationData pd = popData[j];
				boolean insideABC = false;
				boolean checkRad = chkrad;
				boolean nextPopulation = false;
				ArrayList listofvertices = new ArrayList();
				listofvertices.add(geogCentre);
				for(int i =0; i < popData.length;i++){
					if(popData[i].isSampled() && popDistr[i] > 0){
						listofvertices.add(popData[i]);
					}
				}
				if(listofvertices.size()==1){
					return false;
				}
				Collections.sort(listofvertices);
				Stack thehull = new Stack();
				int minmin = 0;
				int minmax = minmin;
				int maxmax = listofvertices.size()-1; 
				int maxmin = maxmax;
				boolean found = false;
				PopulationData temppd = (PopulationData) listofvertices.get(minmin);
				for(int i = 1; i < listofvertices.size() && !found ;i++){ 
					if(temppd.getLongitude() != ((PopulationData) listofvertices.get(i)).getLongitude()){
					//if(Math.abs(temppd.getLatitude() - ((PopulationData) listofvertices.get(i)).getLatitude()) > TOLERANCE){
						found = true;
						minmax = i-1;
					}
				}
				if(minmax == maxmax){
					PopulationData pdmin = (PopulationData) listofvertices.get(minmin);
					PopulationData pdmax = (PopulationData) listofvertices.get(maxmax);
					if ((perpProduct(pd,pdmin,pdmax) == 0) && (pd.getLatitude() >= pdmin.getLatitude()) && (pd.getLatitude() <= pdmax.getLatitude())){
						insideABC = true;
						checkRad = false;
					} else {
						if(chkrad){
							double dist; 
							if(pd.getLatitude() < pdmin.getLatitude()){
								dist = greatCircleDistance(pd,pdmin);
							} else if (pd.getLatitude() > pdmax.getLatitude()){
								dist = greatCircleDistance(pd,pdmax);
							} else {
								dist = greatCircleDistance(pd.getLatitude(),pd.getLongitude(),pdmax.getLatitude(),pd.getLongitude());
							}
							if(pd.getPopulationRadius() >= dist){
								insideABC = true;
								checkRad = true;
							}
						} else {
							nextPopulation = true;
						}
					}
				}
				if(!nextPopulation && !insideABC){
					found = false;
					temppd = (PopulationData) listofvertices.get(maxmax);
					for(int i = maxmax-1; i >= 0 && !found; i-- ){
						if(temppd.getLongitude() != ((PopulationData) listofvertices.get(i)).getLongitude()){
						//if(Math.abs(temppd.getLatitude() - ((PopulationData) listofvertices.get(i)).getLatitude()) > TOLERANCE){
							found = true;
							maxmin = i+1;
						}
					}
					PopulationData ptop = (PopulationData) listofvertices.get(minmin);
					PopulationData pnexttotop  = ptop;
					PopulationData pi;
					thehull.push(ptop);
					for(int i = minmax+1; i <= maxmin; i++){
						pi = (PopulationData) listofvertices.get(i);
						if(perpProduct((PopulationData) listofvertices.get(minmin), (PopulationData) listofvertices.get(maxmin), pi) >=0){ 
							while(thehull.size() > 1 && perpProduct(pnexttotop,ptop,pi) >= 0){
								thehull.pop();
								if(thehull.size() > 1){
									ptop = (PopulationData) thehull.pop();
									pnexttotop = (PopulationData) thehull.peek();
									thehull.push(ptop); 
								}
							}
							pnexttotop = (PopulationData) thehull.peek();
							ptop = pi;
							thehull.push(ptop);
						}
					}
					if(maxmax != maxmin){
						thehull.push(listofvertices.get(maxmax));
					}
					int lowbound = thehull.size();
					for(int i=maxmin-1; i >= minmax; i--){
						pi = (PopulationData) listofvertices.get(i);
						if(perpProduct((PopulationData) listofvertices.get(maxmax), (PopulationData) listofvertices.get(minmax), pi) >=0 ){
							while(thehull.size() > lowbound && perpProduct(pnexttotop,ptop,pi) >= 0 ){
								thehull.pop();
								if(thehull.size() > lowbound){
									ptop = (PopulationData) thehull.pop();
									pnexttotop = (PopulationData) thehull.peek();
									thehull.push(ptop);
								}
							}
							pnexttotop = (PopulationData) thehull.peek();
							ptop = pi;
							thehull.push(ptop);
						}
					}
					if(minmax != minmin){
						thehull.push(listofvertices.get(minmin));
					} 
					/*int windingnumber = 0;
					PopulationData piplus1;
					pi = (PopulationData) thehull.get(0);
					for(int i = 0; i < thehull.size()-1; i++){
						piplus1 = (PopulationData) thehull.get(i+1);
						if(pi.getLongitude() <= pd.getLongitude()){
							if(piplus1.getLongitude() > pd.getLongitude()){
								if(perpProduct(pi,piplus1,pd) < 0){ 
									windingnumber++;
								}
							}
						} else {
							if(piplus1.getLongitude() <= pd.getLongitude()){
								if(perpProduct(pi,piplus1,pd) > 0){
									windingnumber--;
								}
							}
						}
						pi = piplus1;
					}*/
					if(windingNumber(pd,thehull) == 0){
						if(chkrad){
							double mindist = Double.MAX_VALUE; 
							double dist;
							temppd = (PopulationData) thehull.get(0);
							int index = 0;
							for(int i=0; i< thehull.size()-1;i++){
								pi = (PopulationData) thehull.get(i);
								dist = greatCircleDistance(pd,pi);
								if(mindist > dist){
									mindist = dist;
									index = i;
									temppd = pi;
								}
							}
							PopulationData pi1 = (PopulationData) thehull.get(index+1);
							PopulationData pi2;
							if(index == 0){
								pi2 = (PopulationData) thehull.get(thehull.size()-2);
							} else {
								pi2 = (PopulationData) thehull.get(index-1);
							}
							if(perpProduct(temppd,pi1,pd) < 0 && perpProduct(temppd,pd,pi2) < 0){
								double maxangle = 1;
								double angle;
								PopulationData point1, point2;
								for(int k = 0;k< thehull.size()-1;k++){
									pi1 = (PopulationData) thehull.get(k);
									pi2 = (PopulationData) thehull.get(k+1);
									angle = dotProduct(pi1,pd,pi2)/(magnitude(pi1,pd)*magnitude(pi2,pd));
									if(maxangle > angle){
										maxangle = angle;
										point1 = pi1;
										point2 = pi2;
									}
								}
								//double[] pdplanecp = crossProduct(addVector(toCartesian(pd),subtractVector(toCartesian(pi1),toCartesian(pi2))),toCartesian(pd));
								//double[] pi1planecp = crossProduct(pi1,pi2);
								//dist = RADIUS*Math.acos(dotProduct(pdplanecp,pi1planecp)/(magnitude(pdplanecp)*magnitude(pi1planecp)));
								dist = perpDistance(pd,pi1,pi2);
							} else {
								if(dotProduct(pd,temppd,pi1) > 0){
									//double[] pdplanecp = crossProduct(addVector(toCartesian(pd),subtractVector(toCartesian(temppd),toCartesian(pi1))),toCartesian(pd));
									//double[] pi1planecp = crossProduct(temppd,pi1);
									//dist = RADIUS*Math.acos(dotProduct(pdplanecp,pi1planecp)/(magnitude(pdplanecp)*magnitude(pi1planecp)));
									dist = perpDistance(pd,temppd,pi1);
								} else if (dotProduct(pd,temppd,pi2) > 0){
									//double[] pdplanecp = crossProduct(addVector(toCartesian(pd),subtractVector(toCartesian(temppd),toCartesian(pi2))),toCartesian(pd));
									//double[] pi1planecp = crossProduct(temppd,pi2);
									//dist = RADIUS*Math.acos(dotProduct(pdplanecp,pi1planecp)/(magnitude(pdplanecp)*magnitude(pi1planecp)));
									dist = perpDistance(pd,temppd,pi2);
								} else {
									dist = greatCircleDistance(pd,temppd);
								}
							}
							if (pd.getPopulationRadius() >= dist){
								insideABC = true;
								checkRad = true;
							}
						}
					} else {
						insideABC = true; // winding number > 0 => inside
					}
				}
				if(insideABC){
					if(!insideArea(pd,popDistr,checkRad)){
						return true;
					}
				}
			}
		}
		return false;
	}
	
	private boolean isUnsampledPopulationBetween(int[] popDistr, PopulationData geogCentre, boolean chkrad){
		for(int j = 0; j < popDistr.length;j++){
			if(!popData[j].isSampled()){
				PopulationData pd = popData[j];
				boolean insideABC = false;
				boolean checkRad = chkrad;
				boolean nextPopulation = false;
				ArrayList listofvertices = new ArrayList();
				listofvertices.add(geogCentre);
				for(int i =0; i < popData.length;i++){
					if(popData[i].isSampled() && popDistr[i] > 0){
						listofvertices.add(popData[i]);
					}
				}
				if(listofvertices.size()==1){
					return false;
				}
				Collections.sort(listofvertices);
				Stack thehull = new Stack();
				int minmin = 0;
				int minmax = minmin;
				int maxmax = listofvertices.size()-1; 
				int maxmin = maxmax;
				boolean found = false;
				PopulationData temppd = (PopulationData) listofvertices.get(minmin);
				for(int i = 1; i < listofvertices.size() && !found ;i++){ 
					if(temppd.getLongitude() != ((PopulationData) listofvertices.get(i)).getLongitude()){
					//if(Math.abs(temppd.getLatitude() - ((PopulationData) listofvertices.get(i)).getLatitude()) > TOLERANCE){
						found = true;
						minmax = i-1;
					}
				}
				if(minmax == maxmax){
					PopulationData pdmin = (PopulationData) listofvertices.get(minmin);
					PopulationData pdmax = (PopulationData) listofvertices.get(maxmax);
					if ((perpProduct(pd,pdmin,pdmax) == 0) && (pd.getLatitude() >= pdmin.getLatitude()) && (pd.getLatitude() <= pdmax.getLatitude())){
						insideABC = true;
						checkRad = false;
					} else {
						if(chkrad){
							double dist; 
							if(pd.getLatitude() < pdmin.getLatitude()){
								dist = greatCircleDistance(pd,pdmin);
							} else if (pd.getLatitude() > pdmax.getLatitude()){
								dist = greatCircleDistance(pd,pdmax);
							} else {
								dist = greatCircleDistance(pd.getLatitude(),pd.getLongitude(),pdmax.getLatitude(),pd.getLongitude());
							}
							if(pd.getPopulationRadius() >= dist){
								insideABC = true;
								checkRad = true;
							}
						} else {
							nextPopulation = true;
						}
					}
				}
				if(!nextPopulation && !insideABC){
					found = false;
					temppd = (PopulationData) listofvertices.get(maxmax);
					for(int i = maxmax-1; i >= 0 && !found; i-- ){
						if(temppd.getLongitude() != ((PopulationData) listofvertices.get(i)).getLongitude()){
						//if(Math.abs(temppd.getLatitude() - ((PopulationData) listofvertices.get(i)).getLatitude()) > TOLERANCE){
							found = true;
							maxmin = i+1;
						}
					}
					PopulationData ptop = (PopulationData) listofvertices.get(minmin);
					PopulationData pnexttotop  = ptop;
					PopulationData pi;
					thehull.push(ptop);
					for(int i = minmax+1; i <= maxmin; i++){
						pi = (PopulationData) listofvertices.get(i);
						if(perpProduct((PopulationData) listofvertices.get(minmin), (PopulationData) listofvertices.get(maxmin), pi) >=0){ 
							while(thehull.size() > 1 && perpProduct(pnexttotop,ptop,pi) >= 0){
								thehull.pop();
								if(thehull.size() > 1){
									ptop = (PopulationData) thehull.pop();
									pnexttotop = (PopulationData) thehull.peek();
									thehull.push(ptop); 
								}
							}
							pnexttotop = (PopulationData) thehull.peek();
							ptop = pi;
							thehull.push(ptop);
						}
					}
					if(maxmax != maxmin){
						thehull.push(listofvertices.get(maxmax));
					}
					int lowbound = thehull.size();
					for(int i=maxmin-1; i >= minmax; i--){
						pi = (PopulationData) listofvertices.get(i);
						if(perpProduct((PopulationData) listofvertices.get(maxmax), (PopulationData) listofvertices.get(minmax), pi) >=0 ){
							while(thehull.size() > lowbound && perpProduct(pnexttotop,ptop,pi) >= 0 ){
								thehull.pop();
								if(thehull.size() > lowbound){
									ptop = (PopulationData) thehull.pop();
									pnexttotop = (PopulationData) thehull.peek();
									thehull.push(ptop);
								}
							}
							pnexttotop = (PopulationData) thehull.peek();
							ptop = pi;
							thehull.push(ptop);
						}
					}
					if(minmax != minmin){
						thehull.push(listofvertices.get(minmin));
					} 
					/*int windingnumber = 0;
					PopulationData piplus1;
					pi = (PopulationData) thehull.get(0);
					for(int i = 0; i < thehull.size()-1; i++){
						piplus1 = (PopulationData) thehull.get(i+1);
						if(pi.getLongitude() <= pd.getLongitude()){
							if(piplus1.getLongitude() > pd.getLongitude()){
								if(perpProduct(pi,piplus1,pd) < 0){ 
									windingnumber++;
								}
							}
						} else {
							if(piplus1.getLongitude() <= pd.getLongitude()){
								if(perpProduct(pi,piplus1,pd) > 0){
									windingnumber--;
								}
							}
						}
						pi = piplus1;
					}*/
					if(windingNumber(pd,thehull) == 0){
						if(chkrad){
							double mindist = Double.MAX_VALUE; 
							double dist;
							temppd = (PopulationData) thehull.get(0);
							int index = 0;
							for(int i=0; i< thehull.size()-1;i++){
								pi = (PopulationData) thehull.get(i);
								dist = greatCircleDistance(pi,pd);
								if(mindist > dist){
									mindist = dist;
									index = i;
									temppd = pi;
								}
							}
							PopulationData pi1 = (PopulationData) thehull.get(index+1);
							PopulationData pi2;
							if(index == 0){
								pi2 = (PopulationData) thehull.get(thehull.size()-2);
							} else {
								pi2 = (PopulationData) thehull.get(index-1);
							}
							if(perpProduct(temppd,pi1,pd) < 0 && perpProduct(temppd,pd,pi2) < 0){
								double maxangle = 1;
								double angle;
								PopulationData point1, point2;
								for(int k = 0;k< thehull.size()-1;k++){
									pi1 = (PopulationData) thehull.get(k);
									pi2 = (PopulationData) thehull.get(k+1);
									angle = dotProduct(pi1,pd,pi2)/(magnitude(pi1,pd)*magnitude(pi2,pd));
									if(maxangle > angle){
										maxangle = angle;
										point1 = pi1;
										point2 = pi2;
									}
								}
								//double[] pdplanecp = crossProduct(addVector(toCartesian(pd),subtractVector(toCartesian(pi1),toCartesian(pi2))),toCartesian(pd));
								//double[] pi1planecp = crossProduct(pi1,pi2);
								//dist = RADIUS*Math.acos(dotProduct(pdplanecp,pi1planecp)/(magnitude(pdplanecp)*magnitude(pi1planecp)));
								dist = perpDistance(pd,pi1,pi2);
							} else {
								if(dotProduct(pd,temppd,pi1) > 0){
									//double[] pdplanecp = crossProduct(addVector(toCartesian(pd),subtractVector(toCartesian(temppd),toCartesian(pi1))),toCartesian(pd));
									//double[] pi1planecp = crossProduct(temppd,pi1);
									//dist = RADIUS*Math.acos(dotProduct(pdplanecp,pi1planecp)/(magnitude(pdplanecp)*magnitude(pi1planecp)));
									dist = perpDistance(pd,temppd,pi1);
								} else if (dotProduct(pd,temppd,pi2) > 0){
									//double[] pdplanecp = crossProduct(addVector(toCartesian(pd),subtractVector(toCartesian(temppd),toCartesian(pi2))),toCartesian(pd));
									//double[] pi1planecp = crossProduct(temppd,pi2);
									//dist = RADIUS*Math.acos(dotProduct(pdplanecp,pi1planecp)/(magnitude(pdplanecp)*magnitude(pi1planecp)));
									dist = perpDistance(pd,temppd,pi2);
								} else {
									dist = greatCircleDistance(pd,temppd);
								}
							}
							if (pd.getPopulationRadius() >= dist){
								insideABC = true;
								checkRad = true;
							}
						}
					} else {
						insideABC = true; // winding number > 0 => inside
					}
				}
				if(insideABC){
					if(!insideArea(pd,popDistr,checkRad)){
						return true;
					}
				}
			}
		}
		return false;
	}
	
	private boolean insideArea(PopulationData pd, int[] popDistr, boolean chkrad){
		// pre: popDistr.length == popData.length
		ArrayList listofvertices = new ArrayList();
		for(int i =0; i < popData.length;i++){
			if(popData[i].isSampled() && popDistr[i] > 0){
				listofvertices.add(popData[i]);
			}
		}
		// If there are no verticies then there is no convex hull so is not inside
		if(listofvertices.size() == 0){
			return false;
		}
		// Start Andrew's monotone chain algorithm
		// Sort the list according to x,y with min x,min y first upto max x, max y with x priorty over y
		Collections.sort(listofvertices); // The compareto method on the PopulationData class should give above as the natural ordering
		/*for(Iterator i = listofvertices.iterator(); i.hasNext(); ){
			PopulationData popData = (PopulationData) (i.next());
			System.out.println("Lat :"+popData.getLatitude()+" Lon:"+popData.getLongitude());
		}*/
		// perform the chaining
		Stack thehull = new Stack(); // remember this is also a Vector (synchronised ArrayList)
		int minmin = 0; // index of population data with min x and min y
		int minmax = minmin; // index of population data with min x and max y
		int maxmax = listofvertices.size()-1; // index of population data with max x and max y
		int maxmin = maxmax; // index of population data with max x and min y
		boolean found = false;
		PopulationData temppd = (PopulationData) listofvertices.get(minmin);
		for(int i = 1; i < listofvertices.size() && !found ;i++){ // find max minmax
			if(temppd.getLongitude() != ((PopulationData) listofvertices.get(i)).getLongitude()){
			//if(Math.abs(temppd.getLatitude() - ((PopulationData) listofvertices.get(i)).getLatitude()) > TOLERANCE){
				found = true;
				minmax = i-1;
			}
		}
		if(minmax == maxmax){
			// We have a straight line along x = const ; Degenerate case
			// check radius then leave; no need for winding number; check the perpendicular
			PopulationData pdmin = (PopulationData) listofvertices.get(minmin);
			PopulationData pdmax = (PopulationData) listofvertices.get(maxmax);
			if(chkrad){
				double dist; // distance
				if(pd.getLatitude() < pdmin.getLatitude()){
					// then we are below pop[minmin]
					dist = greatCircleDistance(pd,pdmin);
				} else if (pd.getLatitude() > pdmax.getLatitude()){
					// then we are above pop[maxmax]
					dist = greatCircleDistance(pd,pdmax);
				} else {
					// we must be inbetween them
					dist = greatCircleDistance(pd.getLatitude(),pd.getLongitude(),pdmax.getLatitude(),pd.getLongitude());
				}
				return (pd.getPopulationRadius() >= dist);
				/*double dist; // the squared distance
				if(pd.getLongitude() < pdmin.getLongitude()){
					// then we are below pop[minmin]
					dist = Math.pow(pdmin.getLatitude()-pd.getLatitude(),2)+Math.pow(pdmin.getLongitude()-pd.getLongitude(),2);
				} else if (pd.getLongitude() > pdmax.getLongitude()){
					// then we are above pop[maxmax]
					dist = Math.pow(pd.getLatitude()-pdmax.getLatitude(),2)+Math.pow(pd.getLongitude()-pdmax.getLongitude(),2);
				} else {
					// we must be inbetween them
					dist = Math.pow(pd.getLatitude()-pdmin.getLatitude(),2);
				}
				return (Math.pow(pd.getPopulationRadius(),2) >= dist);*/
			} else {
				return (perpProduct(pd,pdmin,pdmax) == 0) && (pd.getLatitude() >= pdmin.getLatitude()) && (pd.getLatitude() <= pdmax.getLatitude()); // if inline then
			}
		}
		found = false;
		temppd = (PopulationData) listofvertices.get(maxmax);
		for(int i = maxmax-1; i >= 0 && !found; i-- ){
			if(temppd.getLongitude() != ((PopulationData) listofvertices.get(i)).getLongitude()){
			//if(Math.abs(temppd.getLatitude()-((PopulationData) listofvertices.get(i)).getLatitude()) > TOLERANCE){
				found = true;
				maxmin = i+1;
			}
		}
		//System.out.println("Clade:"+cladeLabel+":minmin = "+minmin+":minmax = "+minmax+":maxmin = "+maxmin+":maxmax = "+maxmax);
		PopulationData ptop = (PopulationData) listofvertices.get(minmin);
		PopulationData pnexttotop  = ptop;
		PopulationData pi;
		thehull.push(ptop);
		for(int i = minmax+1; i <= maxmin; i++){
			pi = (PopulationData) listofvertices.get(i);
			//System.out.print("Check =>"+listofvertices.get(minmin).toString()+listofvertices.get(maxmin).toString()+pi.toString());
			if(perpProduct((PopulationData) listofvertices.get(minmin), (PopulationData) listofvertices.get(maxmin), pi) >=0){ // if pi are left of or on the line
				//System.out.print("Check1 =>"+listofvertices.get(minmin).toString()+listofvertices.get(maxmin).toString()+pi.toString());
				while(thehull.size() > 1 && perpProduct(pnexttotop,ptop,pi) >= 0){
					thehull.pop();
					if(thehull.size() > 1){
						ptop = (PopulationData) thehull.pop();
						pnexttotop = (PopulationData) thehull.peek();
						thehull.push(ptop); 
					}
				}
				pnexttotop = (PopulationData) thehull.peek();
				ptop = pi;
				thehull.push(ptop);
			}
		}
		if(maxmax != maxmin){
			thehull.push(listofvertices.get(maxmax));
		}
		int lowbound = thehull.size();
		for(int i=maxmin-1; i >= minmax; i--){
			pi = (PopulationData) listofvertices.get(i);
			if(perpProduct((PopulationData) listofvertices.get(maxmax), (PopulationData) listofvertices.get(minmax), pi) >=0 ){ // if pi are left of or on the line
				while(thehull.size() > lowbound && perpProduct(pnexttotop,ptop,pi) >= 0 ){ // have maxmax and one other element
					thehull.pop();
					if(thehull.size() > lowbound){
						ptop = (PopulationData) thehull.pop();
						pnexttotop = (PopulationData) thehull.peek();
						thehull.push(ptop);
					}
				}
				pnexttotop = (PopulationData) thehull.peek();
				ptop = pi;
				thehull.push(ptop);
			}
		}
		if(minmax != minmin){
			thehull.push(listofvertices.get(minmin));
		} // thehull is now the convex hull of the given distribution
		/*System.out.println(cladeLabel);
		for(Iterator i = thehull.iterator(); i.hasNext();){
			PopulationData poplData = (PopulationData) i.next();
			System.out.println("Lat :"+poplData.getLatitude()+" Lon:"+poplData.getLongitude());
		}*/
		// Start the winding number algorithm
		/*int windingnumber = 0;
		PopulationData piplus1;
		pi = (PopulationData) thehull.get(0);
		for(int i = 0; i < thehull.size()-1; i++){
			piplus1 = (PopulationData) thehull.get(i+1); // edge from popdata[i] to popdata[i+1]
			if(pi.getLongitude() <= pd.getLongitude()){
				if(piplus1.getLongitude() > pd.getLongitude()){
					// we have a crossing?
					if(perpProduct(pi,piplus1,pd) < 0){ // if pd to left of line then yes
						windingnumber++;
					}
				}
			} else {
				if(piplus1.getLongitude() <= pd.getLongitude()){
					// we have crossing?
					if(perpProduct(pi,piplus1,pd) > 0){ // if pd is to the right of the line then yes
						windingnumber--;
					}
				}
			}
			pi = piplus1;
		}*/
		if(windingNumber(pd,thehull) == 0){
			if(chkrad){
				// then require bounds check
				double mindist = Double.MAX_VALUE;
				double dist;
				temppd = (PopulationData) thehull.get(0);
				int index = 0;
				for(int i=0; i< thehull.size()-1;i++){
					pi = (PopulationData) thehull.get(i);
					dist = greatCircleDistance(pi,pd);
					//dist = Math.pow(pd.getLatitude()-pi.getLatitude(),2)+Math.pow(pd.getLongitude()-pi.getLongitude(),2);
					if(mindist > dist){
						mindist = dist;
						index = i;
						temppd = pi;
					}
				}
				// temppd is the closest point on the hull
				// check dot or perp product here to check placement
				PopulationData pi1 = (PopulationData) thehull.get(index+1);
				PopulationData pi2;
				if(index == 0){
					pi2 = (PopulationData) thehull.get(thehull.size()-2);
				} else {
					pi2 = (PopulationData) thehull.get(index-1);
				}
				if(perpProduct(temppd,pi1,pd) < 0 && perpProduct(temppd,pd,pi2) < 0){
					// Then angle pi1-temppd-pi2 on the side where pd lies is < PI
					// This hopefully should be a rare case, but ...
					// Calculate the edge of the hull where the angle is the greatest at pd. (i.e. cos(angle) is closest to -1)
					double maxangle = 1; // cosine of the angle
					double angle;
					PopulationData point1, point2;
					for(int k = 0;k< thehull.size()-1;k++){
						pi1 = (PopulationData) thehull.get(k);
						pi2 = (PopulationData) thehull.get(k+1);
						// Need to treat Lat., lon. as x,y here
						angle = dotProduct(pi1,pd,pi2)/(magnitude(pi1,pd)*magnitude(pi2,pd));
						if(maxangle > angle){
							maxangle = angle;
							point1 = pi1;
							point2 = pi2;
						}
					}
					// Now use the sphere calculation to get the real distance
					//double[] pdplanecp = crossProduct(addVector(toCartesian(pd),subtractVector(toCartesian(pi1),toCartesian(pi2))),toCartesian(pd));
					//double[] pi1planecp = crossProduct(pi1,pi2);
					//dist = RADIUS*Math.acos(dotProduct(pdplanecp,pi1planecp)/(magnitude(pdplanecp)*magnitude(pi1planecp)));
					dist = perpDistance(pd,pi1,pi2);
				} else {
					if(dotProduct(pd,temppd,pi1) > 0){
						// Now use the sphere calculation to get the real distance
						//double[] pdplanecp = crossProduct(addVector(toCartesian(pd),subtractVector(toCartesian(temppd),toCartesian(pi1))),toCartesian(pd));
						//double[] pi1planecp = crossProduct(temppd,pi1);
						//dist = RADIUS*Math.acos(dotProduct(pdplanecp,pi1planecp)/(magnitude(pdplanecp)*magnitude(pi1planecp)));
						//dist = Math.pow(perpProduct(temppd,pd,pi1),2)/(Math.pow(pi1.getLatitude()-temppd.getLatitude(),2)+Math.pow(pi1.getLongitude()-temppd.getLongitude(),2));
						//System.out.println(getCladeLabel()+pd.toString()+":"+dist);
						dist = perpDistance(pd,temppd,pi1);
					} else if (dotProduct(pd,temppd,pi2) > 0){
						// Now use the sphere calculation to get the real distance
						//double[] pdplanecp = crossProduct(addVector(toCartesian(pd),subtractVector(toCartesian(temppd),toCartesian(pi2))),toCartesian(pd));
						//double[] pi1planecp = crossProduct(temppd,pi2);
						//dist = RADIUS*Math.acos(dotProduct(pdplanecp,pi1planecp)/(magnitude(pdplanecp)*magnitude(pi1planecp)));
						//dist = Math.pow(perpProduct(temppd,pd,pi2),2)/(Math.pow(pi2.getLatitude()-temppd.getLatitude(),2)+Math.pow(pi2.getLongitude()-temppd.getLongitude(),2));
						dist = perpDistance(pd,temppd,pi2);
					} else {
						// Now use the sphere calculation to get the real distance
						dist = greatCircleDistance(pd,temppd);
						//dist = Math.pow(pd.getLatitude()-temppd.getLatitude(),2)+Math.pow(pd.getLongitude()-temppd.getLongitude(),2);
					}
				}
				return (pd.getPopulationRadius() >= dist);
			} else {
				return false;
			}
		}
		return true; // winding number > 0 => inside 
	}
	
	/*	A lot of these methods below need to be moved to appropriate classes. 
		They defeat the whole process of encapsulation.*/
	private double greatCircleDistance(PopulationData p1,PopulationData p2){
		return greatCircleDistance(p1.getLatitude(),p1.getLongitude(),p2.getLatitude(),p2.getLongitude());
	}
	
	private double greatCircleDistance(double lat1,double lon1,double lat2,double lon2){
		double lat1rad = Math.toRadians(lat1);
		double lon1rad = Math.toRadians(lon1);
		double lat2rad = Math.toRadians(lat2);
		double lon2rad = Math.toRadians(lon2);
		// Uses the modified version from wikipedia - Does not account for "flattening" of the Earth
		//return 2*RADIUS*Math.asin(Math.sqrt(Math.pow(Math.sin((lat1rad-lat2rad)/2),2)+Math.cos(lat1rad)*Math.cos(lat2rad)*Math.pow(Math.sin((lon1rad-lon2rad)/2),2)));
		double num = Math.sqrt(Math.pow(Math.cos(lat2rad)*Math.sin(lon1rad-lon2rad),2)+Math.pow((Math.cos(lat1rad)*Math.sin(lat2rad)-Math.sin(lat1rad)*Math.cos(lat2rad)*Math.cos(lon1rad-lon2rad)),2));
		double denom = Math.sin(lat1rad)*Math.sin(lat2rad)+Math.cos(lat1rad)*Math.cos(lat2rad)*Math.cos(lon1rad-lon2rad);
		double dist = RADIUS*Math.atan(num/denom);
		/*This is for checks
		double[] pop1 = new double[3];
		pop1[0] = RADIUS*Math.cos(lon1rad)*Math.cos(lat1rad);
		pop1[1] = RADIUS*Math.sin(lon1rad)*Math.cos(lat1rad);
		pop1[2] = RADIUS*Math.sin(lat1rad);
		double[] pop2 = new double[3];
		pop2[0] = RADIUS*Math.cos(lon2rad)*Math.cos(lat2rad);
		pop2[1] = RADIUS*Math.sin(lon2rad)*Math.cos(lat2rad);
		pop2[2] = RADIUS*Math.sin(lat2rad);
		double altdist = RADIUS*Math.acos(dotProduct(pop1,pop2)/(magnitude(pop1)*magnitude(pop2)));
		System.out.println("dot="+altdist+":gdc="+dist);
		*/
		return dist;
	}
	
	private double perpDistance(PopulationData p1, PopulationData p2, PopulationData p3){
		return perpDistance(toCartesian(p1),toCartesian(p2),toCartesian(p3));
	}
	
	private double perpDistance(double[] pop1, double[] pop2, double[] pop3){
		// calculates the great circle distance between pop1 and the plane through O, pop2 and pop3.
		double[] pop1planecp = crossProduct(addVector(pop1,subtractVector(pop2,pop3)),pop1);
		double[] pop2planecp = crossProduct(pop2,pop3);
		return RADIUS*Math.acos(dotProduct(pop1planecp,pop2planecp)/(magnitude(pop1planecp)*magnitude(pop2planecp)));
	}
	
	private double[] crossProduct(PopulationData pd1,PopulationData pd2){
		return crossProduct(toCartesian(pd1),toCartesian(pd2));
	}
	
	private double[] crossProduct(double[] pd1xyz, double[] pd2xyz){
		double[] crossproduct = new double[3];
		crossproduct[0] = pd1xyz[1]*pd2xyz[2]-pd1xyz[2]*pd2xyz[1];
		crossproduct[1] = pd1xyz[2]*pd2xyz[0]-pd2xyz[2]*pd1xyz[0];
		crossproduct[2] = pd1xyz[0]*pd2xyz[1]-pd1xyz[1]*pd2xyz[0];
		return crossproduct;
	}
	
	private double[] addVector(double[] pd1, double[] pd2){
		// pd1 to pd2
		double[] newvec = new double[3];
		for(int i =0; i < newvec.length; i++){
			newvec[i] = pd1[i]+pd2[i];
		}
		return newvec;
	}
	
	private double[] subtractVector(double[] pd1, double[] pd2){
		// pd2 from pd1
		double[] newvec = new double[3];
		for(int i =0; i < newvec.length; i++){
			newvec[i] = pd1[i]-pd2[i];
		}
		return newvec;
	}
	
	private double magnitude(double[] xyz){
		double sum = 0;
		for (int i=0; i < xyz.length;i++){
			sum += Math.pow(xyz[i],2);
		}
		return Math.sqrt(sum);
	}
	
	private double[] toCartesian(PopulationData pd){
		// This should be moved to population data class
		double[] xyz = new double[3];
		double lat = Math.toRadians(pd.getLatitude());
		double lon = Math.toRadians(pd.getLongitude());
		xyz[0] = RADIUS*Math.cos(lon)*Math.cos(lat);
		xyz[1] = RADIUS*Math.sin(lon)*Math.cos(lat);
		xyz[2] = RADIUS*Math.sin(lat);
		return xyz;
	}
	
	private PopulationData toPopulationData(double[] xyz){
		// This should be moved to population data class
		double lat = Math.asin(xyz[2]/RADIUS);
		double lon = Math.asin(xyz[1]/(RADIUS*Math.cos(lat)));
		return new PopulationData(Math.toDegrees(lat),Math.toDegrees(lon),false);
	}
	
	private double dotProduct(double[] xyz1, double[] xyz2){
		// Uses origin O
		// if > 0 then angle between O->xyz1 and O->xyz2 is acute
		// if == 0 then angle is right angle
		// if < 0 then angle is obtuse
		double sum = 0;
		for(int i = 0; i < xyz1.length ; i++){
			sum += xyz1[i]*xyz2[i];
		}
		return sum;
	}
	
	private double perpProduct(PopulationData p1, PopulationData p2, PopulationData p3){
		/* This treats the latitude(y) and longitude(x) as x,y co-ordinates to form the hull
			if > 0 then p3 on left of line through p1 to p2
			if == 0 then p3 on line through p1 and p2
			if < 0 then p3 on right of line through p1 to p2
		*/
		//int[] pd1 = toCartesian(p1);
		//int[] pd2 = toCartesian(p2);
		//int[] pd3 = toCartesian(p3);
		//return perpProduct(subtractVector(p1,p2),subtractVector(p3,p2));
		//return (p2.getLatitude()-p1.getLatitude())*(p3.getLongitude()-p1.getLongitude())-(p3.getLatitude()-p1.getLatitude())*(p2.getLongitude()-p1.getLongitude());
		return (p2.getLongitude()-p1.getLongitude())*(p3.getLatitude()-p1.getLatitude())-(p3.getLongitude()-p1.getLongitude())*(p2.getLatitude()-p1.getLatitude());
	}
	
	private double dotProduct(PopulationData p1, PopulationData p2, PopulationData p3){
		/*This treats the latitude and longitude as x,y co-ordinates to form the hull
			if > 0 then angle between p2->p1 and p2->p3 is acute
			if == 0 then angle is right angle
			if < 0 then angle is obtuse
		*/
		//int[] pd1 = toCartesian(p1);
		//int[] pd2 = toCartesian(p2);
		//int[] pd3 = toCartesian(p3);
		//return dotProduct(subtractVector(p1,p2),subtractVector(p3,p2));
		return (p1.getLatitude()-p2.getLatitude())*(p3.getLatitude()-p2.getLatitude())+(p1.getLongitude()-p2.getLongitude())*(p3.getLongitude()-p2.getLongitude());
	}
	
	private double dotProduct(PopulationData p1, PopulationData p2){
		return p1.getLatitude()*p2.getLatitude()+p1.getLongitude()*p2.getLongitude();
	}
	
	private double magnitude(PopulationData p1,PopulationData p2){
		// returns the magnitude of the vector p2->p1
		return Math.sqrt(Math.pow(p1.getLatitude()-p2.getLatitude(),2)+Math.pow(p1.getLongitude()-p2.getLongitude(),2));
	}
	
	private int dfs(Clade clade, ArrayList visited){
		int edges = 0;
		visited.add(clade);
		for(Iterator i = clade.getEdges().iterator();i.hasNext();){
			Clade conClade = (Clade) i.next();
			if(!visited.contains(conClade)){
				edges += 1 + dfs(conClade,visited);
			}
		}
		return edges;
	}
	
	private Stack intersect(Stack hull1, Stack hull2){
		// returns null if there is no intersect, otherwise returns the convex hull corresponding to the intersection
		Stack inters = new Stack(); // head is last element of the vector.
		PopulationData a1,b1,A,B,p,q;
		p = new PopulationData(0,0,false);
		q = new PopulationData(0,0,false);
		char code;
		if(hull1.size() < 4 || hull2.size() < 4){ // two or less points on the hull
			if(hull1.size() == 0 || hull2.size() == 0){
				System.out.println("Error: hull.size() == 0");
				return null; // no intersect
			} else if (hull1.size() == 1 || hull2.size() == 1){
				// The hull size shouldn't be 1
				System.out.println("Error: hull.size() == 1");
				if (((PopulationData) hull1.get(0)).equals(hull2.get(0))){
					inters.push(((PopulationData)hull1.get(0)).copy());
					inters.push(((PopulationData)hull1.get(0)).copy());
					return inters;
				} else {
					return null;
				}
			} else if (hull1.size() == 2 && hull2.size() == 2){
				// single point on the hull
				if (((PopulationData) hull1.get(0)).equals(hull2.get(0))){
					inters.push(((PopulationData)hull1.get(0)).copy());
					inters.push(((PopulationData)hull1.get(0)).copy());
					return inters;
				} else {
					return null;
				}
			} else if (hull1.size() == 2 && hull2.size() == 3){
				if (perpProduct((PopulationData)hull2.get(0),(PopulationData)hull2.get(1),(PopulationData)hull1.get(0)) == 0 
					&& between((PopulationData)hull2.get(0),(PopulationData)hull2.get(1),(PopulationData)hull1.get(0))){
					inters.push(((PopulationData)hull1.get(0)).copy());
					inters.push(((PopulationData)hull1.get(0)).copy());
					return inters;
				} else {
					return null;
				}
			} else if (hull1.size() == 3 && hull2.size() == 2){
				if (perpProduct((PopulationData)hull1.get(0),(PopulationData)hull1.get(1),(PopulationData)hull2.get(0)) == 0 
					&& between((PopulationData)hull1.get(0),(PopulationData)hull1.get(1),(PopulationData)hull2.get(0))){
					inters.push(((PopulationData)hull2.get(0)).copy());
					inters.push(((PopulationData)hull2.get(0)).copy());
					return inters;
				} else {
					return null;
				}
			} else if (hull1.size() == 3 && hull2.size() == 3){
				if((code = segmentIntersect((PopulationData)hull1.get(0),(PopulationData)hull1.get(1),(PopulationData)hull2.get(0),(PopulationData)hull2.get(1),p,q)) != NO_CROSS){
					inters.push(p.copy());
					if(code == END_POINT){
						inters.push(q.copy());
					}
					inters.push(p.copy());
					return inters;
				} else {
					return null;
				}
			} else if (hull1.size() < 4){
				int windp,windq;
				if(hull1.size() == 0){
					System.out.println("Error: hull1.size() == 0");
					return null;
				} else if (hull1.size() == 1){
					System.out.println("Error: hull1.size() == 1");
					windp = windingNumber((PopulationData)hull1.get(0),hull2);
					if(windp == 0){
						return null;
					} else {
						inters.push(((PopulationData)hull1.get(0)).copy());
						inters.push(((PopulationData)hull1.get(0)).copy());
						return inters;
					}
				} else if (hull1.size() == 2){
					windp = windingNumber((PopulationData)hull1.get(0),hull2);
					if(windp == 0){
						return null;
					} else {
						inters.push(((PopulationData)hull1.get(0)).copy());
						inters.push(((PopulationData)hull1.get(0)).copy());
						return inters;
					}
				} else {
					windp = windingNumber((PopulationData)hull1.get(0),hull2);
					windq = windingNumber((PopulationData)hull1.get(1),hull2);
					if((windp == 0) && (windq == 0)){ // both outside
						return null;
					} else if ((windp == 0) || (windq == 0)){ // one is inside
						for(int index = 0; index < hull2.size()-1;index++){
							if((code = segmentIntersect((PopulationData)hull1.get(0),(PopulationData)hull1.get(1),(PopulationData)hull2.get(index),(PopulationData)hull2.get(index+1),p,q)) != NO_CROSS){
								inters.push(p.copy());
								if(code == END_POINT){
									inters.push(q.copy());
								}
							}
						}
						if (inters.empty()){
							return null;
						} else {
							inters.push(((PopulationData)inters.get(0)).copy());
							return inters;
						}
					} else {
						return hull1;
					}
				}
			} else { // hull2.size() < 4
				int windp,windq;
				if(hull2.size() == 0){
					System.out.println("Error: hull2.size() == 0");
					return null;
				} else if (hull2.size() == 1){
					System.out.println("Error: hull2.size() == 1");
					windp = windingNumber((PopulationData)hull2.get(0),hull1);
					if(windp == 0){
						return null;
					} else {
						inters.push(((PopulationData)hull2.get(0)).copy());
						inters.push(((PopulationData)hull2.get(0)).copy());
						return inters;
					}
				} else if (hull2.size() == 2){
					windp = windingNumber((PopulationData)hull2.get(0),hull1);
					if(windp == 0){
						return null;
					} else {
						inters.push(((PopulationData)hull2.get(0)).copy());
						inters.push(((PopulationData)hull2.get(0)).copy());
						return inters;
					}
				} else {
					windp = windingNumber((PopulationData)hull2.get(0),hull1);
					windq = windingNumber((PopulationData)hull2.get(1),hull1);
					if((windp == 0) && (windq == 0)){ // both outside
						return null;
					} else if ((windp == 0) || (windq == 0)){ // one is inside
						for(int index = 0; index < hull1.size()-1;index++){
							if((code = segmentIntersect((PopulationData)hull2.get(0),(PopulationData)hull2.get(1),(PopulationData)hull1.get(index),(PopulationData)hull1.get(index+1),p,q)) != NO_CROSS){
								inters.push(p.copy());
								if(code == END_POINT){
									inters.push(q.copy());
								}
							}
						}
						if (inters.empty()){
							return null;
						} else {
							inters.push(((PopulationData)inters.get(0)).copy());
							return inters;
						}
					} else {
						return hull2;
					}
				}
			}
		}
		int aindex = 1;
		int bindex = 1;
		int n = hull1.size()-1; // num points on hull1
		int m = hull2.size()-1; // num points on hull2
		PopulationData a = (PopulationData) hull1.get(aindex);
		PopulationData b = (PopulationData) hull2.get(bindex);
		int aa = 0; // hull1 counter
		int ba = 0; // hull2 counter 
		boolean firstPoint = true;
		int pos = 0; // UNKNOWN || P_IN || Q_IN 
		double cross,aHB,bHA;
		do{
			a1 = (PopulationData) hull1.get(aindex-1);
			b1 = (PopulationData) hull2.get(bindex-1);
			A = new PopulationData(a.getLatitude()-a1.getLatitude(),a.getLongitude()-a1.getLongitude(),false);
			B = new PopulationData(b.getLatitude()-b1.getLatitude(),b.getLongitude()-b1.getLongitude(),false);
			cross = perpProduct(new PopulationData(0,0,false),A,B);
			aHB = perpProduct(b1,b,a);
			bHA = perpProduct(a1,a,b);
			code = segmentIntersect(a1,a,b1,b,p,q);
			if(code == CROSS || code == VERTEX ){
				if(pos == UNKNOWN && firstPoint){
					aa = ba = 0;
					firstPoint = false;
					inters.push(p.copy());
				}
				pos = inOut(pos,aHB,bHA);
				inters.push(p.copy());
			}
			if(code == END_POINT && dotProduct(A,B) < 0){
				inters.push(p.copy());
				inters.push(q.copy());
				return inters;
			}
			if(cross == 0 && aHB < 0 && bHA < 0){
				// The hulls are disjoint
				return null;
			} else if (cross == 0 && aHB == 0 && bHA == 0){
				// Special case A and B collinear
				if(pos == P_IN){
					ba++;
					bindex++;
					b = (PopulationData) hull2.get(bindex);
					if(bindex >= m){
						bindex = 1;
					}
				} else {
					inters.push(a.copy());
					aa++;
					aindex++;
					a = (PopulationData) hull1.get(aindex);
					if(aindex >= n){
						aindex = 1;
					}
				}
			} else if (cross >= 0){
				if (bHA > 0){
					if(pos == P_IN){
						inters.push(a.copy());
					}
					aa++;
					aindex++;
					a = (PopulationData) hull1.get(aindex);
					if(aindex >= n){
						aindex = 1;
					}
				} else {
					if(pos == Q_IN){
						inters.push(b.copy());
					}
					ba++;
					bindex++;
					b = (PopulationData) hull2.get(bindex);
					if(bindex >= m){
						bindex = 1;
					}
				}
			} else {
				if (aHB > 0){
					if(pos == Q_IN){
						inters.push(b.copy());
					}
					ba++;
					bindex++;
					b = (PopulationData) hull2.get(bindex);
					if(bindex >= m){
						bindex = 1;
					}
				} else {
					if(pos == P_IN){
						inters.push(a.copy());
					}
					aa++;
					aindex++;
					a = (PopulationData) hull1.get(aindex);
					if(aindex >= n){
						aindex = 1;
					}
				}
			}
		}while(((aa < n) || (ba < m)) && (aa < 2*n) && (ba < 2*m));
		if(!firstPoint){
			inters.push(inters.get(0));
		}
		if(pos == UNKNOWN){
			// boundaries of hull 1 and 2 do not cross
			if(windingNumber((PopulationData)hull1.get(0),hull2) != 0){
				return hull1;
			} else if (windingNumber((PopulationData)hull2.get(0),hull1) != 0){
				return hull2;
			} else {
				return null;
			}
		} else { 
			return inters;
		}
	}
	
	private char segmentIntersect(PopulationData a,PopulationData b,PopulationData c,PopulationData d,PopulationData p,PopulationData q){
		double s, t, num, denom;
		char code = '?';
		denom = a.getLongitude()*(d.getLatitude()-c.getLatitude()) +
				b.getLongitude()*(c.getLatitude()-d.getLatitude()) +
				d.getLongitude()*(b.getLatitude()-a.getLatitude()) +
				c.getLongitude()*(a.getLatitude()-b.getLatitude());
		if(denom == 0.0){
			return parallelInt(a,b,c,d,p,q);
		}
		num = a.getLongitude()*(d.getLatitude()-c.getLatitude())+
				c.getLongitude()*(a.getLatitude()-d.getLatitude())+
				d.getLongitude()*(c.getLatitude()-a.getLatitude());
		if(num == 0.0 || num == denom){
			code = VERTEX;
		}
		s = num/denom;
		num = -(a.getLongitude()*(c.getLatitude()-b.getLatitude())+
				b.getLongitude()*(a.getLatitude()-c.getLatitude())+
				c.getLongitude()*(b.getLatitude()-a.getLatitude()));
		if(num == 0.0 || num == denom){
			code = VERTEX;
		}
		t = num/denom;
		if((0.0 < s) && (s < 1.0) && (0.0 < t) && (t < 1.0)){
			code = CROSS;
		} else if ((0.0 > s) || (s > 1.0) || (0.0 > t) || (t > 1.0)){
			code = NO_CROSS;
		}
		p.setLatitude(a.getLatitude()+s*(b.getLatitude()-a.getLatitude()));
		p.setLongitude(a.getLongitude()+s*(b.getLongitude()-a.getLongitude()));
		return code;
	}
	
	private char parallelInt(PopulationData a,PopulationData b,PopulationData c,PopulationData d,PopulationData p,PopulationData q){
		if(!(perpProduct(a,b,c) == 0)){
			return NO_CROSS;
		}
		if(between(a,b,c) && between(a,b,d)){
			p.setLatitude(c.getLatitude());
			p.setLongitude(c.getLongitude());
			q.setLatitude(d.getLatitude());
			q.setLongitude(d.getLongitude());
			return END_POINT;
		}
		if(between(c,d,a) && between(c,d,b)){
			p.setLatitude(a.getLatitude());
			p.setLongitude(a.getLongitude());
			q.setLatitude(b.getLatitude());
			q.setLongitude(b.getLongitude());
			return END_POINT;
		}
		if(between(a,b,c) && between(c,d,b)){
			p.setLatitude(c.getLatitude());
			p.setLongitude(c.getLongitude());
			q.setLatitude(b.getLatitude());
			q.setLongitude(b.getLongitude());
			return END_POINT;
		}
		if(between(a,b,c) && between(c,d,a)){
			p.setLatitude(c.getLatitude());
			p.setLongitude(c.getLongitude());
			q.setLatitude(a.getLatitude());
			q.setLongitude(a.getLongitude());
			return END_POINT;
		}
		if(between(a,b,d) && between(c,d,b)){
			p.setLatitude(d.getLatitude());
			p.setLongitude(d.getLongitude());
			q.setLatitude(b.getLatitude());
			q.setLongitude(b.getLongitude());
			return END_POINT;
		}
		if(between(a,b,d) && between(c,d,a)){
			p.setLatitude(d.getLatitude());
			p.setLongitude(d.getLongitude());
			q.setLatitude(a.getLatitude());
			q.setLongitude(a.getLongitude());
			return END_POINT;
		}
		return NO_CROSS;
	}
	
	private boolean between(PopulationData a, PopulationData b, PopulationData c){
		// assumes a,b,c are collinear (use perpProduct(a,b,c) == 0 for that)
		// Tests if c is between a and b
		if(a.getLongitude() != b.getLongitude()){
			return ((a.getLongitude() <= c.getLongitude())&& (c.getLongitude() <= b.getLongitude())) ||
					((a.getLongitude() >= c.getLongitude())&& (c.getLongitude() >= b.getLongitude()));
		} else {
			return ((a.getLatitude() <= c.getLatitude())&& (c.getLatitude() <= b.getLatitude())) ||
					((a.getLatitude() >= c.getLatitude())&& (c.getLatitude() >= b.getLatitude()));
		}
	}
	
	private int inOut(int pos,double aHB, double bHA){
		if(aHB > 0){
			return P_IN;
		}
		if(bHA > 0){
			return Q_IN;
		}
		return pos;
	}
	
	private Stack convexHull(ArrayList listofvertices){
		// Start Andrew's monotone chain algorithm
		// Sort the list according to x,y with min x,min y first upto max x, max y with x priorty over y
		Collections.sort(listofvertices); // The compareto method on the PopulationData class should give above as the natural ordering
		/*for(Iterator i = listofvertices.iterator(); i.hasNext(); ){
			PopulationData popData = (PopulationData) (i.next());
			System.out.println("Lat :"+popData.getLatitude()+" Lon:"+popData.getLongitude());
		}*/
		// perform the chaining
		Stack thehull = new Stack(); // remember this is also a Vector (synchronised ArrayList)
		int minmin = 0; // index of population data with min x and min y
		int minmax = minmin; // index of population data with min x and max y
		int maxmax = listofvertices.size()-1; // index of population data with max x and max y
		int maxmin = maxmax; // index of population data with max x and min y
		boolean found = false;
		PopulationData temppd = (PopulationData) listofvertices.get(minmin);
		for(int i = 1; i < listofvertices.size() && !found ;i++){ // find max minmax
			if(temppd.getLongitude() != ((PopulationData) listofvertices.get(i)).getLongitude()){
			//if(Math.abs(temppd.getLatitude() - ((PopulationData) listofvertices.get(i)).getLatitude()) > TOLERANCE){
				found = true;
				minmax = i-1;
			}
		}
		if(minmax == maxmax){
			// We have a straight line along x = const (or a single point); Degenerate case
			if(listofvertices.size() == 1){
				thehull.push(listofvertices.get(minmin));
			} else {
				thehull.push(listofvertices.get(minmin));
				thehull.push(listofvertices.get(maxmax));
			}
			thehull.push(listofvertices.get(minmin));
			return thehull;
		}
		found = false;
		temppd = (PopulationData) listofvertices.get(maxmax);
		for(int i = maxmax-1; i >= 0 && !found; i-- ){
			if(temppd.getLongitude() != ((PopulationData) listofvertices.get(i)).getLongitude()){
			//if(Math.abs(temppd.getLatitude()-((PopulationData) listofvertices.get(i)).getLatitude()) > TOLERANCE){
				found = true;
				maxmin = i+1;
			}
		}
		//System.out.println("Clade:"+cladeLabel+":minmin = "+minmin+":minmax = "+minmax+":maxmin = "+maxmin+":maxmax = "+maxmax);
		PopulationData ptop = (PopulationData) listofvertices.get(minmin);
		PopulationData pnexttotop  = ptop;
		PopulationData pi;
		thehull.push(ptop);
		for(int i = minmax+1; i <= maxmin; i++){
			pi = (PopulationData) listofvertices.get(i);
			//System.out.print("Check =>"+listofvertices.get(minmin).toString()+listofvertices.get(maxmin).toString()+pi.toString());
			if(perpProduct((PopulationData) listofvertices.get(minmin), (PopulationData) listofvertices.get(maxmin), pi) >=0){ // if pi are left of or on the line
				//System.out.print("Check1 =>"+listofvertices.get(minmin).toString()+listofvertices.get(maxmin).toString()+pi.toString());
				while(thehull.size() > 1 && perpProduct(pnexttotop,ptop,pi) >= 0){
					thehull.pop();
					if(thehull.size() > 1){
						ptop = (PopulationData) thehull.pop();
						pnexttotop = (PopulationData) thehull.peek();
						thehull.push(ptop); 
					}
				}
				pnexttotop = (PopulationData) thehull.peek();
				ptop = pi;
				thehull.push(ptop);
			}
		}
		if(maxmax != maxmin){
			thehull.push(listofvertices.get(maxmax));
		}
		int lowbound = thehull.size();
		for(int i=maxmin-1; i >= minmax; i--){
			pi = (PopulationData) listofvertices.get(i);
			if(perpProduct((PopulationData) listofvertices.get(maxmax), (PopulationData) listofvertices.get(minmax), pi) >=0 ){ // if pi are left of or on the line
				while(thehull.size() > lowbound && perpProduct(pnexttotop,ptop,pi) >= 0 ){ // have maxmax and one other element
					thehull.pop();
					if(thehull.size() > lowbound){
						ptop = (PopulationData) thehull.pop();
						pnexttotop = (PopulationData) thehull.peek();
						thehull.push(ptop);
					}
				}
				pnexttotop = (PopulationData) thehull.peek();
				ptop = pi;
				thehull.push(ptop);
			}
		}
		if(minmax != minmin){
			thehull.push(listofvertices.get(minmin));
		} // thehull is now the convex hull of the given distribution
		return thehull;
	}
	
	/*private int windingNumber(PopulationData pd, Stack thehull){
		// Start the winding number algorithm
		int windingnumber = 0;
		PopulationData pi, piplus1;
		pi = (PopulationData) thehull.get(0);
		for(int i = 0; i < thehull.size()-1; i++){
			piplus1 = (PopulationData) thehull.get(i+1); // edge from popdata[i] to popdata[i+1]
			if(pi.getLongitude() <= pd.getLongitude()){
				if(piplus1.getLongitude() >= pd.getLongitude()){
					// we have a crossing?
					if(perpProduct(pi,piplus1,pd) >= 0){ // if pd to left of line then yes (or on it)
						windingnumber++;
					}
				}
			} else {
				if(piplus1.getLongitude() <= pd.getLongitude()){
					// we have crossing?
					if(perpProduct(pi,piplus1,pd) <= 0){ // if pd is to the right of the line then yes (or on it)
						windingnumber--;
					}
				}
			}
			pi = piplus1;
		}
		return windingnumber; // winding number > 0 => inside 
	}*/
	private int windingNumber(PopulationData pd, Stack thehull){
		// Start the winding number algorithm
		int windingnumber = 0;
		double perpProd;
		PopulationData pi, piplus1;
		pi = (PopulationData) thehull.get(0);
		for(int i = 0; i < thehull.size()-1; i++){
			piplus1 = (PopulationData) thehull.get(i+1); // edge from popdata[i] to popdata[i+1]
			if(pi.getLatitude() <= pd.getLatitude()){
				if(piplus1.getLatitude() > pd.getLatitude()){
					// we have a crossing?
					perpProd = perpProduct(pi,piplus1,pd);
					if(perpProd > 0){ // if pd to left of line then yes
						windingnumber++;
					} else if (perpProd == 0){ // colinear
						//check x
						if(between(pi,piplus1,pd)){
							return 1;
						}
					}
				} else if (piplus1.getLatitude() == pd.getLatitude()){
					perpProd = perpProduct(pi,piplus1,pd);
					if (perpProd == 0){ // colinear => horizontal
						//check x
						if(between(pi,piplus1,pd)){
							return 1;
						}
					}
				}
			} else {
				if(piplus1.getLatitude() <= pd.getLatitude()){
					// we have crossing?
					perpProd = perpProduct(pi,piplus1,pd);
					if(perpProd < 0){ // if pd is to the right of the line then yes
						windingnumber--;
					} else if (perpProd == 0){ // colinear
						//check x
						if(between(pi,piplus1,pd)){
							return -1;
						}
					}
				}
			}
			pi = piplus1;
		}
		return windingnumber; // winding number != 0 => inside 
	}
	
	private ArrayList distributionToList(int[] distr){
		ArrayList thelist = new ArrayList();
		for(int i=0; i < distr.length; i++){
			if(distr[i] > 0){
				thelist.add(popData[i]);
			}
		}
		return thelist;
	}
	
	private double area(Stack thehull){
		double areasum = 0.0;
		int numvertices = thehull.size() - 1;
		if(numvertices < 3){
			return areasum;
		}
		PopulationData a,b,c;
		a = (PopulationData) thehull.get(0);
		for(int i = 1; i < (numvertices-1); i++){
			b = (PopulationData) thehull.get(i);
			c = (PopulationData) thehull.get(i+1);
			areasum += Math.abs(perpProduct(a,b,c)/2);
		}
		return areasum;
	}
	
}
