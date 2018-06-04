package TCS.VGJ.gui;

/*
 * File: AlgPropDialog.java
 *
 * 2/25/97   Larry Barowski
 *
*/

import java.awt.AWTEvent;

import java.lang.System;

import javax.swing.*;
import java.awt.event.*;


/**
 * A dialog class that allows the user to specify parameters
 * used by algorithms.
 * </p>Here is the <a href="../gui/AlgPropDialog.java">source</a>.
 *
 * @author     Larry Barowski
 * @created    July 2, 2003
 */

public class AlgPropDialog extends JDialog implements ActionListener
{
	private GraphCanvas graphCanvas_;
	private JFrame frame_;

	private JTextField vertical_, horizontal_;



	public AlgPropDialog(JFrame frame, GraphCanvas graph_canvas)
	{
		super(frame, "Spacing (for some algorithms)", true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		graphCanvas_ = graph_canvas;
		frame_ = frame;
		LPanel p  = new LPanel();

		p.addLabel("Vertical Spacing (pixels)", 1, 1, 0.0, 1.0, 0, 2);
		vertical_ = p.addTextField(8, 0, -1, 1.0, 1.0, 1, 1);
		p.addLabel("Horizontal Spacing (pixels)", 1, 1, 0.0, 1.0, 0, 2);
		horizontal_ = p.addTextField(8, 0, -1, 1.0, 1.0, 1, 1);
		p.addButtonPanel("Apply Cancel", 0, this);

		p.finish();
		getContentPane().add("Center", p);
		showMe();
	}


	public void actionPerformed(ActionEvent event)
	{
		if (event.getSource() instanceof JButton)
		{
			String object = ((JButton) event.getSource()).getText();
			if ("Apply".equals(object))
			{
				boolean ok  = true;
				try
				{
					graphCanvas_.hSpacing = Double.valueOf
							(horizontal_.getText()).doubleValue();
				}
				catch (NumberFormatException e)
				{
					new MessageDialog(frame_, "Error", "Bad format for horizontal spacing.", true);
					ok = false;
				}
				try
				{
					graphCanvas_.vSpacing = Double.valueOf
							(vertical_.getText()).doubleValue();
				}
				catch (NumberFormatException e)
				{
					new MessageDialog(frame_, "Error", "Bad format for vertical spacing.", true);
					ok = false;
				}

				if (ok)
				{
					setVisible(false);
				}
			}
			else if ("Cancel".equals(object))
			{
				setVisible(false);
			}
		}

		//return false;
	}


	public void processEvent(AWTEvent event)
	{
		// Avoid having everything destroyed.
		/*if (event.id == Event.WINDOW_DESTROY)
		{
			setVisible(false);
			return true;
		}
		return */super.processEvent(event);
	}


	public void showMe()
	{
		pack();

		vertical_.setText(String.valueOf(graphCanvas_.vSpacing));
		horizontal_.setText(String.valueOf(graphCanvas_.hSpacing));

		setVisible(true);
	}
}
