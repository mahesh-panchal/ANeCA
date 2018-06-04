package NCPAInferenceTree;

public class StatusUndeterminedException extends Exception{
	
	public StatusUndeterminedException(){
		super();
	}
	
	public StatusUndeterminedException(String msg){
		super(msg);
	}
}