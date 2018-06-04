package TCS.VGJ.calc;
/*created sept 11, 2000
 *by Jake Derington
 *
*/
import java.lang.Object;

import java.awt.AWTEvent;
import java.awt.FileDialog;
import java.io.PrintStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;
import java.lang.Process;
import java.lang.Runtime;
import java.lang.Exception;
import TCS.VGJ.graph.Graph;
import java.io.DataOutputStream;
import TCS.VGJ.gui.*;
import TCS.clad.*;
import javax.swing.*;
import java.awt.event.*;

public class CalcDialog extends JDialog implements ActionListener
{

	private Graph graph_;
	private GraphCanvas graphCanvas_;
	private JFrame frame_;

	private JTextField width_, height_, pointSize_, margin_, pWidth_, pHeight_, overlap_, printCmd_;
	private JCheckBox landscape_;

	public TCS tcs;


	public CalcDialog(JFrame frame, GraphCanvas graph_canvas, TCS tdna)
	{
		super(frame, "Clads Base pair changes", true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		graphCanvas_ = graph_canvas;
		frame_ = frame;
		LPanel p  = new LPanel();
		p.addLineLabel("This will give one implementation of base pair changes that occur on each node", 0);
		p.addButtonPanel("Execute Close", 0, this);
		p.finish();
		getContentPane().add("Center", p);
		pack();
		setVisible(true);
		tcs = tdna;
	}


	public void actionPerformed(ActionEvent event)
	{
		if (event.getSource() instanceof JButton)
		{
			String object = ((JButton) event.getSource()).getText();

			if ("Close".equals(object))
			{
				setVisible(false);
			}
			else if ("Execute".equals(object))
			{
				Calc calc  = new Calc(tcs);
				calc.run();
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
}  //end class

