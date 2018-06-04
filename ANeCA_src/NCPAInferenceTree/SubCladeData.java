package NCPAInferenceTree;

public class SubCladeData{
	private String cladeLabel;
	private int[] distr;
	private boolean tip;
	private double withinCladeDist;
	private double withinCladeP_ge;
	private double withinCladeP_le;
	private double nestedCladeDist;
	private double nestedCladeP_ge;
	private double nestedCladeP_le;
	
	public SubCladeData(String cLabel){
		cladeLabel = cLabel;
		tip = (cladeLabel.split("\\s+")[2].equals("(Tip)"));
	}
	
	public void setWithinCladeParameters(double dist, double p_le, double p_ge){
		withinCladeDist = dist;
		withinCladeP_ge = p_ge;
		withinCladeP_le = p_le;
	}

	public void setNestedCladeParameters(double dist, double p_le, double p_ge){
		nestedCladeDist = dist;
		nestedCladeP_ge = p_ge;
		nestedCladeP_le = p_le;
	}
	
	public void setDistribution(int[] dtr){
		distr = dtr;
	}
	
	public String toString(){
		StringBuffer str =  new StringBuffer();
		str.append(cladeLabel.split("\\s+")[1]);
		if(tip){
			str.append(" T");
		} else {
			str.append(" I");
		}
		int len = CladeData.lblPadLimit-str.length();
		for(int i=0; i < len ;i++){
			str.append(" ");
		}
		str.append(withinCladeDist);
		//str.append("     ").append(withinCladeDist);
		if(significantlyLargeDc(CladeData.getSignificanceThreshold())){
			str.append("*l");
		} else if (significantlySmallDc(CladeData.getSignificanceThreshold())){
			str.append("*s");
		}
		len = (CladeData.lblPadLimit+CladeData.statPadLimit-str.length());
		for(int i=0; i < len;i++){
			str.append(" ");
		}
		str.append(nestedCladeDist);
		if(significantlyLargeDn(CladeData.getSignificanceThreshold())){
			str.append("*l");
		} else if (significantlySmallDn(CladeData.getSignificanceThreshold())){
			str.append("*s");
		}
		len = (CladeData.lblPadLimit+(2*CladeData.statPadLimit)+4-str.length()); // The +4 includes Pop:
		for(int i=0; i < len;i++){
			str.append(" ");
		}
		for(int i = 0; i < distr.length;i++){
			int length = Integer.toString(distr[i]).length();
			for(int j = 0; j < (CladeData.popPadLimit-length);j++){
				str.append(" ");
			}
			str.append(distr[i]);
		}
		return str.toString();
	}
	
	public String getCladeLabel(){
		return cladeLabel;
	}
	
	public double getWithinCladeDistance(){
		return withinCladeDist;
	}
	
	public double getWithinCladeProbGreater(){
		return withinCladeP_ge;
	}
	
	public boolean significantlyLargeDc(double level){
		return withinCladeP_ge <= level;
	}
	
	public double getWithinCladeProbLess(){
		return withinCladeP_le;
	}
	
	public boolean significantlySmallDc(double level){
		return withinCladeP_le <= level;
	}
	
	public double getNestedCladeDistance(){
		return nestedCladeDist;
	}
	
	public double getNestedCladeProbGreater(){
		return nestedCladeP_ge;
	}
	
	public boolean significantlyLargeDn(double level){
		return nestedCladeP_ge <= level;
	}
	
	public double getNestedCladeProbLess(){
		return nestedCladeP_le;
	}
	
	public boolean significantlySmallDn(double level){
		return nestedCladeP_le <= level;
	}
	
	public boolean isTip(){
		return tip;
	}
	
	public boolean hasSignificance(double level){
		return withinCladeP_ge <= level || withinCladeP_le <= level || nestedCladeP_ge <= level || nestedCladeP_le <= level;
	}
	
	public int[] getDistribution(){
		return distr;
	}
	
}