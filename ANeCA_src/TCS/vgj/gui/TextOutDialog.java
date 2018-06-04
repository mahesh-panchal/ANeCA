package TCS.VGJ.gui;

/*
 * File: TextOutDialog.java
 *
 * 12/10/96   Larry Barowski
 *
*/

import java.awt.Color;
import javax.swing.*;
import java.awt.event.*;


/**
 *  A dialog class for output of a single string.
 *  </p>Here is the <a href="../gui/TextOutDialog.java">source</a>.
 *
 * @author     Larry Barowski
 * @created    July 2, 2003
 */

public class TextOutDialog extends JDialog implements ActionListener
{
	private JTextArea text;



	public TextOutDialog(JFrame frame, String title, String text_in, int rows, int columns, boolean modal)
	{
		super(frame, title, modal);
		Construct_(frame, title, text_in, rows, columns, modal);
	}


	public TextOutDialog(JFrame frame, String title, String text_in, boolean modal)
	{
		super(frame, title, modal);

		int oldpos  = -1;

		int pos;
		int rows    = 0;
		int columns    = 0;
		while ((pos = text_in.indexOf('\n', oldpos + 1)) != -1)
		{
			if (pos - oldpos > columns)
			{
				columns = pos - oldpos;
			}
			rows++;
			oldpos = pos;
		}
		columns += 2;
		rows += 2;

		if (rows > 35)
		{
			rows = 35;
		}
		if (columns > 80)
		{
			columns = 80;
		}

		Construct_(frame, title, text_in, rows, columns, modal);
	}


	public void actionPerformed(ActionEvent event)
	{
		if (event.getSource() instanceof JButton)
		{
			hide();
			dispose();
		}
	}


	private void Construct_(JFrame frame, String title, String text_in, int rows, int columns, boolean modal)
	{
		LPanel p  = new LPanel();
		text = new JTextArea(text_in, rows, columns);
		text.setEditable(false);
		text.setBackground(Color.white);
		p.addComponent(text, 0, 0, 1.0, 1.0, 3, 0);
		p.addButtonPanel("OK", 0, this);

		p.finish();
		getContentPane().add("Center", p);
		pack();
		show();
	}
}
