package TCS.VGJ.gui;

/*
 * File: InputDialog.java
 *
 * 5/29/96   Larry Barowski
 *
*/

import java.awt.AWTEvent;
import java.awt.Component;
import javax.swing.*;
import java.awt.event.*;

/**
 *  A dialog class for user input of a single string.
 *  </p>Here is the <a href="../gui/InputDialog.java">source</a>.
 *
 * @author     Larry Barowski
 * @created    July 3, 2003
 */

public class InputDialog extends JDialog implements ActionListener
{
	private int event_id;

	private JTextField text;
	private Component postTo_;



	/**
	 * @param  event_id_in  this event will be posted if the user chooses "OK".
	 * @param  frame        Description of the Parameter
	 * @param  title        Description of the Parameter
	 * @param  post_to      Description of the Parameter
	 */
	public InputDialog(JFrame frame, String title, Component post_to, int event_id_in)
	{
		super(frame, "Input", true);

		event_id = event_id_in;
		postTo_ = post_to;

		LPanel p  = new LPanel();
		p.addLabel(title, 0, 0, 1.0, 1.0, 1, 0);
		text = p.addTextField(50, 0, 0, 1.0, 1.0, 1, 0);
		p.addButtonPanel("OK Cancel", 0, this);

		p.finish();
		getContentPane().add("Center", p);
		pack();
		show();
	}


	public void actionPerformed(ActionEvent event)
	{
		if (event.getSource() instanceof JButton)
		{
			String object = ((JButton) event.getSource()).getText();
			if ("OK".equals(object))
			{
				hide();
				dispose();
				postTo_.dispatchEvent(new TCSEvent(this, event_id, text.getText()));
			}
			else if ("Cancel".equals(object))
			{
				hide();
				dispose();
			}
		}
		//return super.action(event, object);
	}
}
