
import java.util.*;

public class NexusData{
		
	private int sequenceLength = 0;
	private ArrayList labels = new ArrayList();
	private ArrayList sequences = new ArrayList();

	public NexusData(){
	}

	public void setSequenceLength(int length){
		sequenceLength = length;
	}

	public void addSequence(String label, String sequence){
		// Doesn't check for duplicates.
		labels.add(label);
		sequences.add(sequence);
	}

	public String getSequence(String hapLabel){
		return (String)sequences.get(labels.indexOf(hapLabel));
	}

}

