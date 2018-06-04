package TCS.VGJ.gui;

/*
 * File: MessageDialog.java
 *
 * 5/10/96   Larry Barowski
 *
*/

import javax.swing.*;
import java.awt.event.*;


/**
 * A dialog class for displaying a message.
 * </p>Here is the <a href="../gui/MessageDialog.java">source</a>.
 *
 * @author     Larry Barowski
 * @created    July 2, 2003
 */


public class MessageDialog extends JDialog implements ActionListener
{
	public MessageDialog(JFrame frame, String title, String message, boolean modal)
	{
		super(frame, title, modal);

		LPanel p  = new LPanel();
		p.addLabel(message, 0, 0, 1.0, 1.0, 0, 0);
		p.addButtonPanel("OK", 0, this);

		p.finish();
		getContentPane().add("Center", p);
		pack();
		show();
	}


	public void actionPerformed(ActionEvent event)
	{
		if (event.getSource() instanceof JButton && "OK".equals(((JButton) event.getSource()).getText()))
		{
			hide();
			dispose();
		}
	}
}
