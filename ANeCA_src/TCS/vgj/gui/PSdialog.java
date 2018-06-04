package TCS.VGJ.gui;

/*
 * File: PSdialog.java
 *
 * 2/16/97   Larry Barowski
 *
*/

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
import javax.swing.*;
import java.awt.event.*;


/**
 * A dialog class that allows the user to specify parameters for
 * PostScript output.
 * </p>Here is the <a href="../gui/PSdialog.java">source</a>.
 *
 * @author     Larry Barowski
 * @created    July 2, 2003
 */

public class PSdialog extends JDialog implements ActionListener
{
	private Graph graph_;
	private GraphCanvas graphCanvas_;
	private JFrame frame_;

	private JTextField width_, height_, pointSize_, margin_, pWidth_, pHeight_, overlap_, printCmd_;
	private JCheckBox landscape_;



	public PSdialog(JFrame frame, GraphCanvas graph_canvas)
	{
		super(frame, "PostScript Output", true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		graphCanvas_ = graph_canvas;
		frame_ = frame;
		LPanel p  = new LPanel();
		p.addLineLabel("Printed Size (inches):", 0);
		p.addLabel("Width", 1, 1, 0.0, 1.0, 0, 2);
		width_ = p.addTextField("8.5", 8, 1, -1, 1.0, 0.0, 1, 1);
		p.addLabel("Height", 1, 1, 0.0, 1.0, 0, 2);
		height_ = p.addTextField("11", 8, 0, -1, 1.0, 0.0, 1, 1);
		p.addLineLabel("Paper Size (inches) (A4 is 8.2677 x 11.6929):", 0);
		p.addLabel("Width", 1, 1, 0.0, 1.0, 0, 2);
		pWidth_ = p.addTextField("8.5", 8, 1, -1, 1.0, 0.0, 1, 1);
		p.addLabel("Height", 1, 1, 0.0, 1.0, 0, 2);
		pHeight_ = p.addTextField("11", 8, 0, -1, 1.0, 0.0, 1, 1);
		p.addLabel("Font Size (points)", 3, 1, 0.0, 1.0, 0, 2);
		pointSize_ = p.addTextField("10", 8, 0, -1, 1.0, 0.0, 1, 1);
		p.addLabel("Outside Margin (inches)", 3, 1, 0.0, 1.0, 0, 2);
		margin_ = p.addTextField(".5", 8, 0, -1, 1.0, 0.0, 1, 1);
		p.addLabel("Multi-Page Overlap (inches)", 3, 1, 0.0, 1.0, 0, 2);
		overlap_ = p.addTextField(".5", 8, 0, -1, 1.0, 0.0, 1, 1);
		landscape_ = new JCheckBox("Landscape", null, false);
		p.addComponent(landscape_, 0, -1, 0.0, 1.0, 0, 0);
		//p.addLineLabel("System Print Command:", 0);
//	printCmd_ = p.addTextField("/usr/ucb/lpr", 8, 0, -1, 1.0, 1.0, 1, 0);
//	p.addButtonPanel("View Save Print Close", 0);
		p.addButtonPanel("Save Close", 0, this);
		p.finish();
		getContentPane().add("Center", p);
		pack();
		setVisible(true);
	}


	public void actionPerformed(ActionEvent event)
	{
		if (event.getSource() instanceof JButton)
		{
			String object = ((JButton) event.getSource()).getText();
			if ("View".equals(object))
			{
				String PS  = getPS_();
				if (PS != null)
				{
					new TextOutDialog(frame_, "PostScript", PS, false);
				}
			}
			else if ("Close".equals(object))
			{
				setVisible(false);
			}
			else if ("Save".equals(object))
			{
				String PS  = getPS_();
				if (PS == null)
				{
					return;
				}
				try
				{
					String filename;
					FileDialog fd;
					try
					{
						fd = new FileDialog(frame_, "Save PostScript", FileDialog.SAVE);
						fd.setVisible(true);

					}
					catch (Throwable e)
					{
						MessageDialog dg  = new MessageDialog(frame_, "Error", "It appears your VM does not allow file saving.", true);
						return;
					}
					filename = fd.getFile();
					if (filename == null)
					{
						return;
					}

					// Work around JDK Windows bug.
					if (filename.endsWith(".*.*"))
					{
						String tmpstr  = filename.substring(0, filename.length() - 4);
						filename = tmpstr;
					}
					filename = fd.getDirectory() + filename;
					PrintStream ps   = new PrintStream(new FileOutputStream(filename));
					ps.println(PS);
					ps.close();
				}
				catch (IOException e)
				{
					MessageDialog dg  = new MessageDialog(frame_, "Error", e.getMessage(), true);
				}
			}
			else if ("Print".equals(object))
			{
				String PS  = getPS_();
				if (PS == null)
				{
					return;
				}
				File file  = new File("vgj00.ps");
				try
				{
					PrintStream ps  = new PrintStream(new FileOutputStream(file));
					ps.println(PS);
					ps.close();
					String cmd      = new String(printCmd_.getText() + " " + "vgj00.ps");
					Runtime.getRuntime().exec(cmd).waitFor();
				}
				catch (Exception e)
				{
					MessageDialog dg  = new MessageDialog(frame_, "Error", "Printing error: " + e.getMessage(), true);
				}
				try
				{
					file.delete();
				}
				catch (Exception e)
				{  // Delete fails from appletviewer (although
					// writing over is allowed).

				}
			}
		}
		else if (event.getSource() instanceof JCheckBox)
		{
			String tmp   = width_.getText();
			String tmp2  = height_.getText();
			width_.setText(tmp2);
			height_.setText(tmp);
		}
	}  ///////////end of action


	private String getPS_()
	{
		double width       = 7.5;
		double height       = 10;
		double p_width       = 7.5;
		double p_height       = 7.5;
		double fontsize       = 10;
		double margin       = .5;
		double overlap       = .5;
		boolean landscape  = false;

		try
		{
			width = getVal_(width_, "Width", 1.0, 1000000.0);
			height = getVal_(height_, "Height", 1.0, 1000000.0);
			p_width = getVal_(pWidth_, "Parer Width", 1.0, 1000000.0);
			p_height = getVal_(pHeight_, "Paper Height", 1.0, 1000000.0);
			fontsize = getVal_(pointSize_, "Font Size", .5, 100.0);
			margin = getVal_(margin_, "Margin", 0.0, 4.0);
			overlap = getVal_(overlap_, "Overlap", 0.0, 2.0);
		}
		catch (NumberFormatException e)
		{
			return null;
		}

		if (width - margin * 2.0 < 1.0)
		{
			new MessageDialog(frame_, "Error", "Width minus twice margin must be at least one inch.", true);
			return null;
		}
		if (height - margin * 2.0 < 1.0)
		{
			new MessageDialog(frame_, "Error", "Height minus twice margin must be at least one inch.", true);
			return null;
		}

		landscape = landscape_.isSelected();
		return graphCanvas_.toPS(width, height, p_width, p_height, fontsize, margin, overlap, landscape);
	}


	private double getVal_(JTextField text, String name, double min, double max)
	{
		double result  = 1.0;

		try
		{
			result = Double.valueOf(text.getText()).doubleValue();
		}
		catch (NumberFormatException e)
		{
			result = 1.0;
			String msg  = "Bad format for " + name + ".";
			new MessageDialog(frame_, "Error", msg, true);

			throw e;
		}

		if (result < min)
		{
			String msg  = name + " must be at least " + min + ".";
			new MessageDialog(frame_, "Error", msg, true);

			throw new NumberFormatException();
		}

		if (result > max)
		{
			String msg  = name + " must be no more than " + max + ".";
			new MessageDialog(frame_, "Error", msg, true);

			throw new NumberFormatException();
		}

		return result;
	}


	public void processEvent(AWTEvent event)
	{
		// Avoid having everything destroyed.
		/*if (event.getID() == Event.WINDOW_DESTROY)
		{
			hide();
			return true;
		}
		return*/ super.processEvent(event);
	}
}

