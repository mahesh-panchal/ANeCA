package TCS.VGJ.gui;

/*
	File: AngleControlPanel.java
	9/3/96  Larry Barowski
*/

import java.awt.AWTEvent;
import javax.swing.*;
import java.awt.event.*;

import TCS.VGJ.util.DPoint;

/**
 *   An AngleControl, along with a label and buttons for XY plane,
 *   YZ plane, XZ plane.
 *   </p>Here is the <a href="../gui/AngleControlPanel.java">source</a>.
 *
 * @author     Larry Barowski
 * @created    July 2, 2003
 */

class AngleControlPanel extends LPanel implements ActionListener
{
	public static int ANGLE      = 38793;
	public static int DONE       = 38794;

	private AngleControl angle_;


	public AngleControlPanel(int width, int height)
	{
		super();

		constraints.insets.top = constraints.insets.bottom = 0;
		addLabel("Viewing Angles", 0, 0, 1.0, 0.0, 0, 0);
		constraints.insets.top = constraints.insets.bottom = 0;
		angle_ = new AngleControl(width, height);
		addComponent(angle_, 0, 0, 1.0, 1.0, 3, 0);
		constraints.insets.top = constraints.insets.bottom = 0;
		addLabel("Plane:", 1, -1, 0.0, 0.0, 0, 0);
		addButton("XY", 1, 0, 1.0, 0.0, 0, 0, this);
		addButton("XZ", 1, 0, 1.0, 0.0, 0, 0, this);
		addButton("YZ", 0, 0, 1.0, 0.0, 0, 0, this);

		finish();
	}


	public void actionPerformed(ActionEvent event)
	{
		if (event.getSource() instanceof JButton)
		{
			String what = ((JButton) event.getSource()).getText();
			if (((String) what).equals("XY"))
			{
				angle_.setAngles(0.0, Math.PI / 2.0);
				dispatchEvent(new TCSEvent(this, DONE, new DPoint(0.0, Math.PI / 2.0)));
			}
			else if (((String) what).equals("XZ"))
			{
				angle_.setAngles(0.0, 0.0);
				dispatchEvent(new TCSEvent(this, DONE, new DPoint(0.0, 0.0)));
			}
			else if (((String) what).equals("YZ"))
			{
				angle_.setAngles(Math.PI / 2.0, 0.0);
				dispatchEvent(new TCSEvent(this, DONE, new DPoint(Math.PI / 2.0, 0.0)));
			}
		}
	}


	public void processEvent(AWTEvent event)
	{
		if(event instanceof TCSEvent){
			if (event.getSource() instanceof AngleControl)
			{
				if (event.getID() == AngleControl.ANGLE)
				{
					DPoint angles  = (DPoint) ((TCSEvent) event).getInformation();
					dispatchEvent(new TCSEvent(this, ANGLE, new DPoint(angles.x, angles.y)));
				}
				else if (event.getID() == AngleControl.DONE)
				{
					DPoint angles  = (DPoint) ((TCSEvent) event).getInformation();
					dispatchEvent(new TCSEvent(this, DONE, new DPoint(angles.x, angles.y)));
				}
			}
		}
		super.processEvent(event);
	}
}
