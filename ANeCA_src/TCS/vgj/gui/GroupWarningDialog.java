package TCS.VGJ.gui;

/*
 * File: GroupWarningDialog.java
 *
 * 6/7/97   Larry Barowski
 *
*/


import java.lang.System;
import javax.swing.*;
import java.awt.event.*;


/**
 * An O.K. / Cancel dialog for group deletion.
 * </p>Here is the <a href="../gui/GroupWarningDialog.java">source</a>.
 *
 * @author     Larry Barowski
 * @created    July 3, 2003
 */

public class GroupWarningDialog extends JDialog implements ActionListener
{
	private GraphCanvas graphCanvas_;
	private JFrame frame_;



	public GroupWarningDialog(JFrame frame, GraphCanvas graph_canvas)
	{
		super(frame, "Delete?", true);

		graphCanvas_ = graph_canvas;
		frame_ = frame;
		LPanel p  = new LPanel();

		p.constraints.insets.bottom = 0;
		p.addLabel("Selected items include one or more group nodes.", 0, 0, 1.0, 1.0, 0, 0);
		p.constraints.insets.top = 0;
		p.constraints.insets.bottom = 0;
		p.addLabel("All children of group nodes will be recursively deleted.", 0, 0, 1.0, 1.0, 0, 0);
		p.constraints.insets.top = 0;
		p.addLabel("Delete anyway?", 0, 0, 1.0, 1.0, 0, 0);
		p.addButtonPanel("Delete Cancel", 0, this);

		p.finish();
		getContentPane().add("Center", p);
		showMe();
	}


	public void actionPerformed(ActionEvent event)
	{
		if (event.getSource() instanceof JButton)
		{
			String object = ((JButton) event.getSource()).getText();
			if ("Cancel".equals(object))
			{
				setVisible(false);
			}
			else if ("Delete".equals(object))
			{
				graphCanvas_.deleteSelected(false);
				setVisible(false);
			}
		}

	}


	public void showMe()
	{
		pack();
		setVisible(true);
	}
}
