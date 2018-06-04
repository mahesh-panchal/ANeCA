
import java.util.*;

public class ANeCAlocus extends ArrayList{

	private String locusLabel;
	
	public ANeCAlocus(){
	}

	public void setLocusLabel(String label){
		locusLabel = label;
	}

	public String getLocusLabel(){
		return locusLabel;
	}

	public void consolidateData(NexusData nexData,GMLData gmlData, InferenceData infData, SummaryData sumData){
		for(Iterator i = sumData.getClades().iterator();i.hasNext();){
			Clade clad = (Clade) i.next();
			if(clad.hasSignificantSubclade()){
				FinalisedCladeData fcd = new FinalisedCladeData(clad.getCladeName(),sumData.getLocations());
				add(fcd);
				fcd.setLocusLabel(locusLabel);
				fcd.setInference(infData.getInference(clad.getCladeName()));
				clad.setSubclades(fcd);
				fcd.setNexusData(nexData);
				fcd.setGMLData(gmlData);
				fcd.calculateStats();
			}
		}
	}

	/*public void setNexusData(NexusData nd){
		nexData = nd;
	}

	public void setGMLData(GMLData gmld){
		gmlData = gmld;
	}

	public void setInferenceData(InferenceData infd){
		infData = infd;
	}

	public void setSummaryData(SummaryData sumd){
		sumData = sumd;
	}*/
}

