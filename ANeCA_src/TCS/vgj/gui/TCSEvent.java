package TCS.VGJ.gui;

import java.awt.AWTEvent;
/*
This class was created to help remove deprecated events and get things working when events occured
*/
public class TCSEvent extends AWTEvent{
	
	private Object _source = null; // The source of the event
	private int _id = AWTEvent.RESERVED_ID_MAX; // The event id
	private Object load = null; // extra information
	
	public TCSEvent(Object source, int id, Object info){
		super(source,id);
		_source = source;
		_id = id;
		load = info;
	}
	
	public Object getSource(){
		return _source;
	}
	
	public void setSource(Object source){
		_source = source;
	}
	
	public int getID(){
		return _id;
	}
	
	public Object getInformation(){
		return load;
	}
}