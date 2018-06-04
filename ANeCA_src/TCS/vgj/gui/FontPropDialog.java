package TCS.VGJ.gui;

/*
 * File: FontPropDialog.java
 *
 * 4/10/97   Larry Barowski
 *
*/

import java.awt.AWTEvent;
import java.awt.Font;

import java.lang.System;

import javax.swing.*;
import java.awt.event.*;

/**
 * A dialog class that allows the user to specify font
 * properites.
 * </p>Here is the <a href="../gui/FontPropDialog.java">source</a>.
 *
 * @author     Larry Barowski
 * @created    July 2, 2003
 */

public class FontPropDialog extends JDialog implements ActionListener
{
	private GraphCanvas graphCanvas_;
	private JFrame frame_;

	private JTextField name_, size_;
	private boolean fontForNode;



	public FontPropDialog(JFrame frame, GraphCanvas graph_canvas, boolean node)
	{
		super(frame, "", true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		fontForNode = node;
		String label;
		if(node){
			label = "Font for Node labels";
		} else {
			label = "Font for Edge labels";
		}
		setTitle(label);
		graphCanvas_ = graph_canvas;
		frame_ = frame;
		LPanel p  = new LPanel();

		p.addLabel("Font Name", 1, 1, 0.0, 1.0, 0, 2);
		name_ = p.addTextField(15, 0, -1, 1.0, 1.0, 1, 1);
		p.addLabel("Font Size", 1, 1, 0.0, 1.0, 0, 2);
		size_ = p.addTextField(15, 0, -1, 1.0, 1.0, 1, 1);
		p.addButtonPanel("Apply Cancel", 0, this);

		p.finish();
		getContentPane().add("Center", p);
		//showMe();
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
					Font font;
					font = new Font(name_.getText(), Font.PLAIN, Integer.valueOf(size_.getText()).intValue());
					if(fontForNode){
						graphCanvas_.setFont(font);
					} else {
						graphCanvas_.setEdgeFont(font);
					}
				}
				catch (NumberFormatException e)
				{
					new MessageDialog(frame_, "Error", "Bad format for font size.", true);
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

	}


	public void processEvent(AWTEvent event)
	{
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

		Font font  = graphCanvas_.getFont(fontForNode);
		name_.setText(font.getName());
		size_.setText(String.valueOf(font.getSize()));

		setVisible(true);
	}
}
