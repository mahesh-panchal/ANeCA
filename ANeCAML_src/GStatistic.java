
import java.util.*;

public class GStatistic {

	private double gStatistic = 0;
	private double degreesOfFreedom;
	private ArrayList listOfCladeNames = new ArrayList();
	private ArrayList listOfClades;

	public GStatistic(ArrayList clades){
		degreesOfFreedom = clades.size()-1;
		listOfClades = clades;
		double numerator = 0.0;
		double denominator = 0.0;
		for(Iterator i = clades.iterator(); i.hasNext();){
			FinalisedCladeData fcd = (FinalisedCladeData) i.next();
			listOfCladeNames.add(fcd.getLocusLabel() + "-" + fcd.getCladeLabel());
			numerator += fcd.cladeTLSEstimate()*(1+fcd.averagePairwiseDivergence());
			denominator += 1+fcd.averagePairwiseDivergence();
		}
		double bigT = numerator/denominator;
		for(Iterator i = clades.iterator(); i.hasNext();){
			FinalisedCladeData fcd = (FinalisedCladeData) i.next();
			gStatistic += (1+fcd.averagePairwiseDivergence())*(1-(fcd.cladeTLSEstimate()/bigT)+Math.log(fcd.cladeTLSEstimate())-Math.log(bigT));
		}
		gStatistic = -2*gStatistic;
	}

	public double statistic(){
		return gStatistic;
	}
	
	public double degreesOfFreedom(){
		return degreesOfFreedom;
	}
	
	public ArrayList clades(){
		return listOfClades;
	}
	
	public String toString(){
		StringBuffer str = new StringBuffer();
		str.append("G:").append(statistic()).append(" DoF:").append(degreesOfFreedom());
		str.append(" Pval:").append(ChiSq.cdf(statistic(),degreesOfFreedom()));
		str.append(" Clades :");
		for(Iterator i = listOfCladeNames.iterator(); i.hasNext();){
			str.append(" ").append((String) i.next());
		}
		return str.toString();
	}
	
	public boolean isSignificantUpperTail(){
		return (ChiSq.cdf(statistic(),degreesOfFreedom()) >= 0.95);
	}
}
