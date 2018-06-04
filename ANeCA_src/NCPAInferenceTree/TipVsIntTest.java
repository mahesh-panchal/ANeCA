package NCPAInferenceTree;

public class TipVsIntTest{
	private double withinCladeDist;
	private double withinCladeP_ge;
	private double withinCladeP_le;
	private double nestedCladeDist;
	private double nestedCladeP_ge;
	private double nestedCladeP_le;
	
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
	
	public String toString(){
		StringBuffer str = new StringBuffer();
		str.append(withinCladeDist);
		if(significantlyLargeDc(CladeData.getSignificanceThreshold())){
			str.append("*l");
		} else if (significantlySmallDc(CladeData.getSignificanceThreshold())){
			str.append("*s");
		}
		int length = CladeData.statPadLimit - str.length();
		for(int i = 0; i < length; i++){
			str.append(" ");
		}
		str.append(nestedCladeDist);
		if(significantlyLargeDn(CladeData.getSignificanceThreshold())){
			str.append("*l");
		} else if (significantlySmallDn(CladeData.getSignificanceThreshold())){
			str.append("*s");
		}
		return str.toString();
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
	
	public boolean hasSignificance(double level){
		return withinCladeP_ge <= level || withinCladeP_le <= level || nestedCladeP_ge <= level || nestedCladeP_le <= level;
	}

}