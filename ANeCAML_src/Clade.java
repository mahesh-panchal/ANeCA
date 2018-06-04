
import java.util.*;

public class Clade{

	
	private String cladeName;
	private ArrayList subclades = new ArrayList(); // Will either be list of clades or strings denoting the haplotype.
	private ArrayList significant = new ArrayList(); // An array of whether clade is significant.
	private ArrayList geographicDistribution = new ArrayList(); // An Array of strings. Each string is whitespace separated with the sample size at each location.
	private ArrayList isTipClade = new ArrayList();
	private ArrayList significantDc = new ArrayList();
	private ArrayList significantDn = new ArrayList();
	
	public Clade(String name){
		cladeName = name;
	}

	public boolean equals(Object o){
		if (o instanceof Clade){
			return	((Clade) o).cladeName.equals(this.cladeName);
		} else if (o instanceof String){
			return cladeName.equals(o);
		}
		return false;
	}

	public void addSubclade(Clade c,boolean signif,String geographicdist, boolean tip, String dc, String dn){
		// This will be a subclade
		subclades.add(c);
		significant.add(new Boolean(signif));
		geographicDistribution.add(geographicdist);
		isTipClade.add(new Boolean(tip));
		significantDc.add(dc);
		significantDn.add(dn);
	}

	public void addSubclade(String c,boolean signif,String geographicdist, boolean tip, String dc, String dn){
		// This will be haplotypes or I-T clade
		subclades.add(c);
		significant.add(new Boolean(signif));
		geographicDistribution.add(geographicdist);	
		isTipClade.add(new Boolean(tip));
		significantDc.add(dc);
		significantDn.add(dn);
	}

	public String getCladeName(){
		return cladeName;
	}

	public boolean hasSignificantSubclade(){
		for(Iterator i = significant.iterator(); i.hasNext();){
			if(((Boolean) i.next()).booleanValue()){
				return true;
			}
		}
		return false;
	}

	public void setSubclades(FinalisedCladeData nestingClade){
		for(Iterator i = subclades.iterator(); i.hasNext();){ // Has I-T clade!
			Object clade = i.next();
			String cladelabel;
			if(clade instanceof String) { // I-T will always fall here.
				cladelabel = (String) clade;
				int index = subclades.indexOf(cladelabel);
				FinalisedSubcladeData fscd = new FinalisedSubcladeData(cladelabel,((Boolean)significant.get(index)).booleanValue(),(String)geographicDistribution.get(index), ((Boolean)isTipClade.get(index)).booleanValue(),(String) significantDc.get(index),(String)significantDn.get(index)); // geographic dist = "" if I-T
				if(!cladelabel.contains("I-T")){
					fscd.setHaplotypes((String) clade);
				}
				nestingClade.addSubclade(fscd);
			} else if (clade instanceof Clade){
				cladelabel = ((Clade) clade).getCladeName();
				int index = subclades.indexOf(clade);
				FinalisedSubcladeData fscd = new FinalisedSubcladeData(cladelabel,((Boolean)significant.get(index)).booleanValue(),(String)geographicDistribution.get(index), ((Boolean)isTipClade.get(index)).booleanValue(),(String) significantDc.get(index),(String)significantDn.get(index));
				fscd.setHaplotypes(((Clade)clade).getHaplotypes());
				nestingClade.addSubclade(fscd);
			}
		}
	}

	public String getHaplotypes(){
		StringBuffer stringbuff = new StringBuffer();
		for(Iterator i = subclades.iterator(); i.hasNext();){
			Object clade = i.next();
			if(clade instanceof Clade){
				stringbuff.append(((Clade)clade).getHaplotypes());
			} else if (clade instanceof String){
				if(!((String)clade).contains("I-T")){
					stringbuff.append(clade);
				}
			}
			stringbuff.append(" ");
		}
		return stringbuff.toString();
	}
	
}
