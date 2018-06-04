public class FinalisedSubcladeData{
		
	private String subcladeLabel;
	private String geographicDist;
	private boolean significant;
	private String haplotypes;
	private boolean tipClade;
	private String sigDc;
	private String sigDn;
	
	public FinalisedSubcladeData(String label, boolean signif, String geoDist, boolean isTip, String dc, String dn){
		subcladeLabel = label;
		geographicDist = geoDist;
		significant = signif;
		tipClade = isTip;
		sigDc = dc;
		sigDn = dn;
	}

	public void setHaplotypes(String haps){
		// the string of unique haplotypes within this clade.
		haplotypes = haps;
	}

	public boolean isSignificant(){
		return significant;
	}

	public String getHaplotypes(){
		return haplotypes;
	}
	
	public String getCladeLabel(){
		return subcladeLabel;
	}

	public String getDistribution(){
		return geographicDist;
	}

	public boolean isTip(){
		return tipClade;
	}

	public boolean significantlySmallDc(){
		return sigDc.contains("s");
	}

	public boolean significantlyLargeDc(){
		return sigDc.contains("l");
	}

	public boolean significantlySmallDn(){
		return sigDn.contains("s");
	}

	public boolean significantlyLargeDn(){
		return sigDn.contains("l");
	}

}
