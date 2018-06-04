
import java.util.*;

public class InferenceData{

	public static final String NHCR = "Null hypothesis cannot be rejected.";
	public static final String IO = "Inconclusive outcome.";
	public static final String RGF_ID = "Restricted gene flow with isolation by distance (restricted dispersal by distance in non-sexual species).";
	public static final String IGR_RECvRDGF = "Insufficient genetic resolution to discriminate between range expansion/colonization and restricted dispersal/gene flow.";
	public static final String RGFD_LDD = "Restricted gene flow/dispersal but with some long-distance dispersal.";
	public static final String SDI_IDvLDD = "Sampling design inadequate to discriminate between isolation by distance (short-distance movements) vs. long-distance dispersal.";
	public static final String RGFD_LDDxPGF_EIP = "Restricted gene flow/dispersal but with some long-distance dispersal over intermediate areas not occupied by the species; or past gene flow followed by extinction of intermediate populations.";
	public static final String AF = "Allopatric fragmentation.";
	public static final String GSI_FvID = "Geographic sampling(s) inadequate to discriminate between fragmentation and isolation by distance.";
	public static final String CRE = "Contiguous range expansion.";
	public static final String LDC_SFxPF_RE = "Long-distance colonisation possibly coupled with subsequent fragmentation or past fragmentation followed by range expansion.";
	public static final String SDI_CREvLDCvPF = "Sampling design inadequate to discriminate between contiguous range expansion, long-distance colonisation and past fragmentation.";
	public static final String LDCoPF = "Long-distance colonisation and/or past fragmenation (not necessarily mutually exclusive).";
	public static final String GSI_FvREvID = "Geographic sampling(s) inadequate to discriminate between fragmentation, range expansion and isolation by distance.";
	public static final String IGS = "Inadequate geographical sampling.";
	
	private ArrayList clades = new ArrayList();
	private ArrayList inferences = new ArrayList();

	public InferenceData(){
	}

	public static boolean isInference(String line){
		return line.equals(NHCR) || line.contains(IO) || line.equals(RGF_ID) || line.equals(IGR_RECvRDGF)
			|| line.equals(RGFD_LDD) || line.equals(SDI_IDvLDD) || line.equals(RGFD_LDDxPGF_EIP)
			|| line.equals(AF) || line.equals(GSI_FvID)
			|| line.equals(CRE) || line.equals(LDC_SFxPF_RE) || line.equals(SDI_CREvLDCvPF)
			|| line.equals(LDCoPF) || line.equals(GSI_FvREvID) || line.equals(IGS) ;
	}

	public void addInference(String clade, String inference){
		//System.out.println("Add clade:"+clade);
		clades.add(clade);
		inferences.add(inference);
	}

	public String getInference(String clade){
		return (String)inferences.get(clades.indexOf(clade));
	}
	
	public void replaceInference(String clade, String inference){
		inferences.set(clades.indexOf(clade),inference);
	}
}
