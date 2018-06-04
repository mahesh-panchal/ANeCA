package TCS.VGJ.gui;

/*
 * File: GroupControl.java
 *
 * 6/7/97   Larry Barowski
 *
*/



import java.awt.AWTEvent;
import java.lang.System;
import javax.swing.*;
import java.awt.event.*;




/**
 * A dialog class that allows the user to enter group commands.
 * </p>Here is the <a href="../gui/GroupControl.java">source</a>.
 *
 *@author	Larry Barowski
**/


   public class GroupControl extends JDialog implements ActionListener
   {
	  private GraphCanvas graphCanvas_;
	  private JFrame frame_;
   
   
   
   
	  public GroupControl(JFrame frame, GraphCanvas graph_canvas)
	  {
		 super(frame, "Group Control", true);
		 setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	  
		 graphCanvas_ = graph_canvas;
		 frame_ = frame;
		 LPanel p = new LPanel();
	  
		 p.addButton("Create Group (selected nodes)  [c]", 0, 0, 1.0, 1.0, 1, 0, this);
		 p.constraints.insets.top = 0;
		 p.addButton("Destroy Groups (selected groups)  [d]", 0, 0, 1.0, 1.0, 1, 0, this);
		 p.constraints.insets.top = 0;
		 p.addButton("Group (selected nodes)  [g]", 0, 0, 1.0, 1.0, 1, 0, this);
		 p.constraints.insets.top = 0;
		 p.addButton("Ungroup (selected groups)  [u]", 0, 0, 1.0, 1.0, 1, 0, this);
		 p.addButtonPanel("Cancel", 0, this);
	  
		 p.finish();
		 getContentPane().add("Center", p);
		 showMe();
	  }  
	  
	  public void actionPerformed(ActionEvent event)
	  {
		 if(event.getSource() instanceof JButton)
		 {
			 String object = ((JButton) event.getSource()).getText();
			if("Cancel".equals(object)) {
			   setVisible(false); }
			else if("Create Group (selected nodes)  [c]".equals(object)) {
			   graphCanvas_.groupControl('c'); }
			else if("Destroy Groups (selected groups)  [d]".equals(object)) {
			   graphCanvas_.groupControl('d'); }
			else if("Group (selected nodes)  [g]".equals(object)) {
			   graphCanvas_.groupControl('g'); }
			else if("Ungroup (selected groups)  [u]".equals(object)) {
			   graphCanvas_.groupControl('u'); }
		 }
	  
		 //return false;
	  }  
	  
	  public void processEvent(AWTEvent event) {
	 // Avoid having everything destroyed.
		 /*if (event.id == Event.WINDOW_DESTROY)
		 {
			setVisible(false);
			return true;
		 }
		 return*/ super.processEvent(event);
	  } 
	   
	  public void showMe()
	  {
		 pack();
		 setVisible(true);
	  }  
}      
