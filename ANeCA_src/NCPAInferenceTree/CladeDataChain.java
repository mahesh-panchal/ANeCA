package NCPAInferenceTree;

import java.util.Iterator;
import java.util.NoSuchElementException;
import NestedCladeBuilder.NestedCladograph;
import ANeCA.Mediator;
import javax.swing.JOptionPane;

public class CladeDataChain{
	
	private CladeData first = null;
	private CladeData last = null;
	private int size = 0;
	private PopulationData[] popData;
	private String testname = "Unidentified test file";
	private NestedCladograph ncg;
	
	public CladeDataChain(PopulationData[] pdata, NestedCladograph nestedCladogram){
		popData = pdata;
		ncg = nestedCladogram;
	}
	
	public CladeDataChain(PopulationData[] pdata, NestedCladograph nestedCladogram, CladeData c){
		popData = pdata;
		ncg = nestedCladogram;
		first = c;
		last = c;
		c.setPopulationData(popData);
		c.setNestedCladogram(ncg);
		c.setCladeDataChain(this);
		size++;
	}
	
	// public access methods
	public void addCladeData(CladeData c){
		if(first != null){
			last.addNextCladeData(c);
		} else {
			first = c;
		}
		last = c;
		size++;
		c.setPopulationData(popData);
		c.setNestedCladogram(ncg);
		c.setCladeDataChain(this);
	}
	
	public Iterator iterator(){
		return new CladeDataChainIterator();
	}
	
	public CladeData getClade(String label){
		CladeData cd;
		for(Iterator i = iterator(); i.hasNext();){
			cd = (CladeData) i.next();
			if(cd.getCladeLabel().equals(label)){
				return cd;
			}
		}
		return null;
	}
	
	public String toString(){
		// Need this to be formatted - This gives the GeoDis Summary
		StringBuffer str = new StringBuffer();
		str.append(testname).append("\n");
		for(int i=0; i < popData.length;i++){
			str.append(i+1).append(" "); // To give the population id
			str.append(popData[i].toString());
		}
		for(Iterator i = iterator();i.hasNext();){
			str.append("\n").append(((CladeData) i.next()).toString());
		}
		return str.toString();
	}
	
	public void setTestLabel(String lbl){
		testname = lbl;
	}
	
	public void checkGeographicInformation(){
		for(int i = 0; i < popData.length; i++){
			if(!popData[i].isSampled()){
				return;
			}
		}
		if(!Mediator.isCommandLineApp()){
			JOptionPane.showMessageDialog(Mediator.getGui().getFrame(),
				"No unsampled populations have been specified.\n"+
				"Sampling Inadequacy detection mechanisms will be bypassed.\n",
				"Inference Key Warning", JOptionPane.WARNING_MESSAGE);
		} else {
			System.out.println("No unsampled populations have been specified.");
			System.out.println("Sampling Inadequacy detection mechanisms will be bypassed.");
		}
	}
	
	// package access methods
	
	// private access methods
	
	// private inner classes
	private class CladeDataChainIterator implements Iterator{
		private CladeData current = null;
		
		CladeDataChainIterator(){
		}
		
		public boolean hasNext(){
			if(current != null){
				return current.hasNextCladeData();
			} else {
				return first != null;
			}
		}
		
		public Object next() throws NoSuchElementException{
			if(hasNext()){
				if(current != null){
					current = current.getNextCladeData();
				} else {
					current = first;
				}
			} else {
				throw new NoSuchElementException();
			}
			return current;
		}
		
		public void remove() throws UnsupportedOperationException{
			throw new UnsupportedOperationException();
		}
	}
	
}