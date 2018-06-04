
import java.util.*;

public class GMLData{

	private ArrayList cladeIdentifiers = new ArrayList();
	private ArrayList haplotypeIdentifiers = new ArrayList();
	private ArrayList higherOrderClade = new ArrayList();
	private ArrayList subcladeOfHoCs = new ArrayList();
	//private ArrayList subCladeIdentifiers = new ArrayList();

	public GMLData(){
	}

	public void addHaplotypeIdentifiers(String cladeId, String haplotypeIds){
		cladeIdentifiers.add(cladeId);
		haplotypeIdentifiers.add(haplotypeIds);
	}
	
	public void addCladeHierarchy(String hoc, String subhoc){
		higherOrderClade.add(hoc);
		subcladeOfHoCs.add(subhoc);
	}

	/*public void addSubclade(String cladeId, String subclade){
		// subclades will have been read in already and be present in cladeIdentifiers.
		if(cladeIdentifiers.indexOf(cladeId) < 0){
			cladeIdentifiers.add(cladeId);
			subCladeIdentifiers.add(subclade);
			haplotypeIdentifiers.add(haplotypeIdentifiers.get(cladeIdentifiers.indexOf(subclade)));
		} else {
			haplotypeIdentifiers.set(cladeIdentifiers.indexOf(cladeId),getHaplotypeIdentifiers(cladeId) + " " + getHaplotypeIdentifiers(subclade));
			subcladeIdentifiers.set(cladeIdentifiers.indexOf(cladeId)),getSubclades(cladeId) + " " + subclade);
		}
	}*/
	public String getHaplotype(String cladeid){
		int index = cladeIdentifiers.indexOf(cladeid);
		if(index >= 0) {
			return cladeid;
		} else {
			if(cladeid.trim().equals("")){
				return "";
			}
			StringBuffer str = new StringBuffer();
			String[] subs = ((String) subcladeOfHoCs.get(higherOrderClade.indexOf(cladeid))).split("\\s+");
			for(int i = 0; i< subs.length; i++){
				str.append(getHaplotype(subs[i])).append(" ");
			}
			return str.toString().trim();
		}
	}
		
	public String getHaplotypeIdentifiers(String cladeid){
		//System.out.println(cladeid);
		int index = cladeIdentifiers.indexOf(cladeid);
		if(index >= 0){
			return (String) haplotypeIdentifiers.get(index);
		} else {
			//System.out.println("searching: "+cladeid+"#");
			if(cladeid.trim().equals("")){
				return "";
			}
			StringBuffer str = new StringBuffer();
			String[] subs = ((String) subcladeOfHoCs.get(higherOrderClade.indexOf(cladeid))).split("\\s+");
			for(int i = 0; i< subs.length; i++){
				str.append(getHaplotypeIdentifiers(subs[i])).append(" ");
			}
			return str.toString().trim();
		}
	}

	public int getHaplotypeFrequency(String cladeid){
		return getHaplotypeIdentifiers(cladeid).trim().split("\\s+").length;
	}

	/*public String getSubclades(String cladeid){
		return (String) subCladeIdentifiers.get(cladeIdentifiers.indexOf(cladeId));
	}*/

}
