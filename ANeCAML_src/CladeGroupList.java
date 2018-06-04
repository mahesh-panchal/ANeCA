
import java.util.*;

public class CladeGroupList extends ArrayList{

	CladeGroup rgfibdGroup = new CladeGroup(CladeGroup.RGF_IBD); // RGF_IBD
	CladeGroup rgflddGroup = new CladeGroup(CladeGroup.RGF_LDD); // RGF_LDD + RGF_LDD_PF
	CladeGroup afGroup = new CladeGroup(CladeGroup.AF);     // AF
	CladeGroup creGroup = new CladeGroup(CladeGroup.CRE);	   // CRE
	CladeGroup ldcGroup = new CladeGroup(CladeGroup.LDC);    // LDC_PFxPF_INT + LDC_PF

	CladeGroup rgfGroup = new CladeGroup(CladeGroup.RGF);	   // RGF_IBD + RGF_LDD + RGF_LDD_PF
	CladeGroup fragGroup = new CladeGroup(CladeGroup.FRAG);   // AF + LDC_PFxPF_INT + LDC_PF
	CladeGroup rangeExGroup = new CladeGroup(CladeGroup.EXPAN);// CRE + LDC_PFxPF_INT + LDC_PF
	
	public CladeGroupList(){
	}
	
	public void sortCladesIntoGroups(FinalisedCladeData fcd){
		if(fcd.getInference().equals(InferenceData.RGF_ID)){
			rgfibdGroup.addClade(fcd);
			rgfGroup.addClade(fcd.clone());
		} else if(fcd.getInference().equals(InferenceData.RGFD_LDD) || fcd.getInference().equals(InferenceData.RGFD_LDDxPGF_EIP)){
			rgflddGroup.addClade(fcd);
			rgfGroup.addClade(fcd.clone());
		} else if(fcd.getInference().contains(InferenceData.AF)){
			afGroup.addClade(fcd);
			fragGroup.addClade(fcd.clone());
		} else if(fcd.getInference().equals(InferenceData.CRE)){
			creGroup.addClade(fcd);
			rangeExGroup.addClade(fcd.clone());
		} else if(fcd.getInference().equals(InferenceData.LDC_SFxPF_RE) || fcd.getInference().equals(InferenceData.LDCoPF)){
			ldcGroup.addClade(fcd);
			rangeExGroup.addClade(fcd.clone());
			fragGroup.addClade(fcd.clone());
		} else {
			System.out.println("Check: Clade not sorted : " + fcd.getInference());
		}
	}
		
	public void separateByGeographicConcordence(){
		// Now separate them via concordence.
		findConcordentInferences(rgfibdGroup);
		findConcordentInferences(rgflddGroup);
		findConcordentInferences(afGroup);
		findConcordentInferences(creGroup);
		findConcordentInferences(ldcGroup);
		findConcordentInferences(rgfGroup);
		findConcordentInferences(fragGroup);
		findConcordentInferences(rangeExGroup);
		//System.out.println("RGFIBD:"+rgfibdGroup.size()+" RGFLDD:"+rgflddGroup.size()+" AF:"+afGroup.size()+" CRE:"+creGroup.size()+" LDC:"+ldcGroup.size());
		// Clean up some memory by getting rid of some extra clones
		rgfibdGroup = null;
		rgflddGroup = null;
		afGroup = null;
		creGroup = null;
		ldcGroup = null;
		rgfGroup = null;
		fragGroup = null;
		rangeExGroup = null;
		for(int j = 0; j < size(); j++){
			CladeGroup currentClade = (CladeGroup) get(j);
			int k = j+1;
			while(k < size()){
				if(currentClade.equals(get(k))){
					remove(k);
				} else {
					k++;
				}
			}
		}
		for(Iterator i = iterator(); i.hasNext();){
			((CladeGroup) i.next()).calculateGStatistics();
		}
	}

	private void findConcordentInferences(CladeGroup cg){
		// Write a new algorithm.
		Stack cladesToCheck = new Stack();
		cladesToCheck.addAll(cg);
		while(!cladesToCheck.empty()){
			FinalisedCladeData poppedFCD = (FinalisedCladeData) cladesToCheck.pop();
			if(poppedFCD.getCladeGroup().equals(cg)){
				CladeGroup currentCladeGroup = new CladeGroup(cg.type());
				currentCladeGroup.addClade(poppedFCD);
				for (Iterator i = cladesToCheck.iterator(); i.hasNext();){
					FinalisedCladeData stackFCD = (FinalisedCladeData) i.next();
					if(poppedFCD.isConcordantWith(stackFCD)){
						if(stackFCD.getCladeGroup().equals(cg)){
							currentCladeGroup.addClade(stackFCD);
						} else {
							currentCladeGroup = stackFCD.getCladeGroup().merge(currentCladeGroup);
						}
					}
				}
				if(!contains(currentCladeGroup)){
					add(currentCladeGroup);
				}
			}
		}
	}
	
	public String toString(){
		StringBuffer str = new StringBuffer();
		for(Iterator i = iterator(); i.hasNext();){
			str.append(((CladeGroup) i.next()).toString()).append("\n");
		}
		return str.toString();
	}
}
