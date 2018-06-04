package NestedCladeBuilder;

public class NotTerminalException extends Exception{

	public NotTerminalException(){
		super();
	}
	
	public NotTerminalException(String msg){
		super(msg);
	}
}