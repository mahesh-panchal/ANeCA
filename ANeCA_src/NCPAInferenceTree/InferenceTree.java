package NCPAInferenceTree;

import java.util.Iterator;

public class InferenceTree{
	private static final String RES = "\nResults : ";
	public static final String NHCR = "Null hypothesis cannot be rejected.\nMoving on to next clade";
	public static final String IO = "Inconclusive outcome.";
	public static final String RGF_ID = "Restricted gene flow with isolation by distance (restricted dispersal by distance in non-sexual species).";
	public static final String IGR_RECvRDGF = "Insufficient genetic resolution to discriminate between range expansion/colonization and restricted dispersal/gene flow.";
	public static final String RGFD_LDD = "Restricted gene flow/dispersal but with some long-distance dispersal.";
	public static final String SDI_IDvLDD = "Sampling design inadequate to discriminate between isolation by distance (short-distance movements) vs. long-distance dispersal.";
	public static final String RGFD_LDDxPGF_EIP = "Restricted gene flow/dispersal but with some long-distance dispersal over intermediate areas not occupied by the species; or past gene flow followed by extinction of intermediate populations.";
	public static final String AF = "Allopatric fragmentation.";
	public static final String PF = "Past fragmentation.";
	public static final String GSI_FvID = "Geographic sampling(s) inadequate to discriminate between fragmentation and isolation by distance.";
	public static final String RE = "Range expansion.";
	public static final String CRE = "Contiguous range expansion.";
	public static final String LDC_SFxPF_RE = "Long-distance colonisation possibly coupled with subsequent fragmentation or past fragmentation followed by range expansion.";
	public static final String SDI_CREvLDCvPF = "Sampling design inadequate to discriminate between contiguous range expansion, long-distance colonisation and past fragmentation.";
	public static final String LDCoPF = "Long-distance colonisation and/or past fragmenation (not necessarily mutually exclusive).";
	public static final String GSI_FvREvID = "Geographic sampling(s) inadequate to discriminate between fragmentation, range expansion and isolation by distance.";
	public static final String IGS = "Inadequate geographical sampling.";
	public static final String LDM = "Long-distance movement.";
	public static final String IE_LDMvGM = "Insufficient evidence to discriminate between long-distance movements of the orgamism and the combined effects of gradual movement during a past range expansion and fragmentation.";
	public static final String OUTCOME = "Outcome.";
	
	// public access methods
	public static String analyse(CladeDataChain cdc){
		cdc.checkGeographicInformation();
		StringBuffer results = new StringBuffer();
		if(cdc != null){
			CladeData current;
			for(Iterator i = cdc.iterator(); i.hasNext();){
				current = (CladeData) i.next();
				results.append(current.getCladeLabel()).append("\n");
				results.append("Chain of Inference : ");
				results.append(applyKey(current)).append("\n\n");
			}
		}
		return results.toString();
	}
	
	// package access methods
	
	// private access methods
	private static String applyKey(CladeData cd){
		if(cd.hasSignificance()){
			// YES - Go to step 1
			return step1(cd);
		} else {
			// NO - Move on to new clade
			return NHCR;
		}
	}

	private static String step1(CladeData cd){
		if(cd.question1()){
			// YES - Go to step 19
			// The distributions do not overlap
			return "1-"+step19(cd);
		} else {
			// NO - Go to step 2
			// The distributions do overlap
			return "1-"+step2(cd);
		}
	}

	private static String step2(CladeData cd){
		try{
			if(cd.question2()){
				// YES - Go to step 3
				// ({E}Dc[s](T) && {E}Dc[!s](I)) || ({E}Dc[!l](T) && {E}Dc[s](I) && {E}Dc[!s](I)) || ({E}Dc[l](I) && {A}Dc[!l](T)) || Dc[l](I-T)
				return "2-"+step3(cd);
			} else {
				// NO - Go to step 11
				// !(({E}Dc[s](T) && {E}Dc[!s](I)) || ({E}Dc[!l](T) && {E}Dc[s](I) && {E}Dc[!s](I)) || ({E}Dc[l](I) && {A}Dc[!l](T)) || Dc[l](I-T))
				return "2-"+step11(cd);
			}
		} catch (StatusUndeterminedException e){
			// Tip/Interior Status cannot be determined
			// There was no Tip/Interior contrast
			return "2 IO\nI-T Status Undetermined: "+IO;
		}
	}

	private static String step3(CladeData cd){
		if(cd.question3()){
			// YES - Go to step 5
			// ( {E}(Dc[s] && Dn[l])(I/T/I-T) || {E}(Dc[l] && Dn[s])(I/T/I-T)) || ( {E}Dn[l](T) ) || ( {E}Dn[s](I) ) || ( (Dn[s] && Dc[ns])(I-T) )
			return "3-"+step5(cd);
		} else {
			// NO - Go to step 4
			// !(( {E}(Dc[s] && Dn[l])(I/T/I-T) || {E}(Dc[l] && Dn[s])(I/T/I-T)) || ( {E}Dn[l](T) ) || ( {E}Dn[s](I) ) || ( (Dn[s] && Dc[ns])(I-T) ))
			return "3-"+step4(cd);
		}
	}

	private static String step4(CladeData cd){
		if(cd.question4()){
			// YES - Go to step 9
			// {E(2)}Dc[s](I/T) do not overlap areas with other clades in the group
			return "4-"+step9(cd);
		} else {
			// NO - have solution 
			// There is at most one clade with Dc[s] or the areas overlap
			return "4 NO\n"+RGF_ID;
		}
	}

	private static String step5(CladeData cd){
		if(cd.question5()){
			// YES - Go to step 15
			// {E(2)}Dc[s](I/T) do not overlap areas with other clades in the group
			return "5-"+step15(cd);
		} else {
			// NO - Go to step 6
			// There is at most one clade with Dc[s] or the areas overlap
			return "5-"+step6(cd);
		}
	}

	private static String step6(CladeData cd){
		try {
			if(cd.question6()){
				// YES - Go to step 13
				// The clades with reversals or (Dn[s] && Dc[ns]) are at least 75% perfectly concordant with the other clades w/ reversals or (Dn[s] && Dc[ns])
				return "6-"+step13(cd);
			} else {
				// NO - Go to step 7
				// At least one clade w/ reversal or (Dn[s] && Dc[ns]) is not 75% perfectly concordant
				return "6-"+step7(cd);
			}
		} catch (StatusUndeterminedException e){
			//Too few clades to determine concordance
			// There are less than 3 clades in the group or there are less than two clades with reversals or (Dn[s] && Dc[ns])
			return "6*-"+step7(cd)+
				"\nToo few Clades: "+IGR_RECvRDGF+
				"\nQuestion 7 and 8 try to distinguish between long vs. short distance movement"; // Is this confusing?
		}
	}

	private static String step7(CladeData cd){
		if(cd.question7()){
			// YES - have solution
			// The two areas have a sampled population in between
			return "7 YES\n"+RGFD_LDD;
		} else {
			// NO - Go to step 8
			// There is no sampled population in between the two areas
			return "7-"+step8(cd);
		}
	}

	private static String step8(CladeData cd){ // note: this has been reversed from the real inference key
		if(cd.question8()){
			// YES - have solution
			// There is an unsampled population between the two areas
			return "8 NO\n"+SDI_IDvLDD;
		} else {
			// NO - have solution
			// There is not an unsampled population between the two areas
			return "8 YES\n"+RGFD_LDDxPGF_EIP;
		}
	}

	private static String step9(CladeData cd){
		if(cd.question9()){
			// YES - Go to step 10
			// There is an unsampled population between the two areas
			return "9-"+step10(cd);
		} else {
			// NO - have solution 
			// There are no unsampled populations between the two areas
			return "9 NO\n"+AF; 
		}
	}

	private static String step10(CladeData cd){ // note: this has been reversed from the real inference key 
		if(cd.question10()){
			// YES - have solution
			// There is an unsampled population between the two areas
			return "10 NO\n"+GSI_FvID;
		} else {
			// NO - have solution
			// There are no unsampled populations between the two areas
			return "10 YES\n"+AF;
		}
	}

	private static String step11(CladeData cd){
		if(cd.question11()){
			// YES - Go to step 12
			// ({E}Dc[l](T) ) || ( {A}Dc[s](I)) || ( Dc[s](I-T))
			//return "11\n"+RE+"\n11-"+step12(cd);
			return "11-"+step12(cd);
		} else {
			// NO - Go to step 17
			// !(({E}Dc[l](T) ) || ( {A}Dc[s](I)) || ( Dc[s](I-T)))
			return "11-"+step17(cd);
		}
	}

	private static String step12(CladeData cd){
		if(cd.question12()){
			// YES - Go to step 13
			// There is at least one reversal in a clade or I-T
			return "12-"+step13(cd);
		} else {
			// NO - have solution
			// There are no reversals
			return "12 NO\n"+CRE;
		}
	}

	private static String step13(CladeData cd){
		if(cd.question13()){
			// YES - have solution
			// There is a sampled population between the group of clades and the geographic center of the other clades
			return "13 YES\n"+LDC_SFxPF_RE; 
		} else {
			// NO - Go to step 14
			// There are no sampled populations between the group of clades and the geographic center of the other clades
			return "13-"+step14(cd);
		}
	}

	private static String step14(CladeData cd){ 
		if(cd.question14()){
			// YES - have solution
			// There is an unsampled population between the group and the geographic center of the other clades
			return "14 YES\n"+SDI_CREvLDCvPF; 
		} else {
			// NO - have solution Go to step 21
			// There is no population between the group and the geographic center of the other clades
			return "14 NO\n"+LDCoPF; 
		}
	}

	private static String step15(CladeData cd){
		if(cd.question15()){
			// YES - Go to step 16
			// There is an unsampled population between the two areas
			return "15-"+step16(cd);
		} else {
			// NO - have solution
			// There are no unsampled populations between the two areas
			return "15 NO\n"+LDCoPF;
		}
	}

	private static String step16(CladeData cd){ // note: this has been reversed from the real inference key
		if(cd.question16()){
			// YES - Go to step 18
			// There is an unsampled population between the two regions
			return "16-"+step18(cd);
		} else {
			// NO - have solution
			// There are no unsampled populations between the two regions
			return "16 NO\n"+AF;
		}
	}

	private static String step17(CladeData cd){
		if(cd.question17()){
			// YES - Go to step 4
			// ( {A}Dn[s](T) || ({E}Dn[s](I) && {E}Dn[!s](I))) || ( {E}Dn[l](I) ) || ( Dn[l](I-T))
			return "17-"+step4(cd);
		} else {
			// NO - have solution
			// !(( {A}Dn[s](T) || ({E}Dn[s](I) && {E}Dn[!s](I))) || ( {E}Dn[l](I) ) || ( Dn[l](I-T)))
			return "17 NO\n"+IO;
		}
	}

	private static String step18(CladeData cd){
		if(cd.question18()){
			// YES - have solution
			// The shortest distance between the two groups of clades (sampled haplotypes within them) is longer than the #edges/#sampled haplotypes
			return "18 YES\n"+GSI_FvID;
		} else {
			// NO - have solution
			// The shortest distance between the two groups of clades (sampled haplotypes within them) is equal or shorter than the #edges/#sampled haplotypes
			return "18 NO\n"+GSI_FvREvID; 
		}
	}

	private static String step19(CladeData cd){
		if(cd.question19()){
			// YES - Go to step 20
			// There is either a sampled or unsampled population between the group of clades
			return "19-"+step20(cd);
		} else {
			// NO - have solution
			// There are no populations between the group of clades
			return "19 NO\n"+AF;
		}
	}

	private static String step20(CladeData cd){
		if(cd.question20()){
			// YES - Go to step 2
			// There was a sampled population in between
			return "20-"+step2(cd);
		} else {
			// NO - have solution
			// There were no sampled populations in between
			return "20 NO\n"+IGS; 
		}
	}

	/*private static String step21(CladeData cd){
		if(cd.question21()){
			// YES - have solution
			return "21 YES"+LDM;
		} else {
			// NO - have solution
			return "21 NO"+IE_LDMvGM;
		}
	}*/

}