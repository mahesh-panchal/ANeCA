
import java.util.*;

public class SummaryData{

	private ArrayList locations = new ArrayList();
	private ArrayList listOfClades = new ArrayList();
	private Clade currentClade;

	public SummaryData(){
	}

	public void addLocationData(double lat, double lon, double rad){
		locations.add(new PopulationData(lat,lon,rad));
	}

	public ArrayList getLocations(){
		return locations;
	}
		

	public void addClade(String name){
		currentClade = new Clade(name);
		listOfClades.add(currentClade);
	}
	
	public void addSubclade(String name, boolean signif, String geographicdist, boolean tip, String dcSig, String dnSig){
		int ind = listOfClades.indexOf(new Clade(name));
		if(ind >= 0){
			currentClade.addSubclade((Clade)listOfClades.get(ind),signif,geographicdist,tip,dcSig,dnSig);
		} else {
			currentClade.addSubclade(name,signif,geographicdist,tip,dcSig,dnSig);
		}
	}	
		
	public ArrayList getClades(){
		return listOfClades;
	}
}
