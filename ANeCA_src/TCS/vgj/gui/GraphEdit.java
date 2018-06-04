package TCS.VGJ.gui;

/*
  Graph File Viewer & Editing Utility
	 Steven Hansen
	 CSE690
 */

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.AWTEvent;
import java.io.IOException;
import java.io.StringReader;
import java.lang.NumberFormatException;
import TCS.VGJ.graph.Graph;
import TCS.VGJ.graph.GMLobject;
import TCS.VGJ.graph.GMLlexer;
import TCS.VGJ.algorithm.GraphUpdate;
import TCS.VGJ.graph.ParseError;
import javax.swing.*;
import java.awt.event.*;


/**
 *	GraphEdit is a text editing window for Graphs
 *	</p>Here is the <a href="../gui/GraphEdit.java">source</a>.
 *
 * @author     Steven Hansen
 * @created    July 2, 2003
 */

public class GraphEdit extends JFrame implements ActionListener
{
	private static JTextArea textarea_;
	private static String originalGraphSpec_;
	private static String lastaction          = "";

	private Graph graph_;
	private GraphUpdate update_;
	private JTextField text1, text2;
	private JLabel label1, label2;
	private JButton okButton, cancelButton;
	private boolean firstTime_                = true;


	public GraphEdit(Graph graph_in, GraphUpdate update_in)
	{

		setTitle("Text Edit " + update_in.getFrame().getTitle() + " (GML)");
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		graph_ = graph_in;

		GMLobject GMLgraph  = new GMLobject("graph", GMLobject.GMLlist);
		graph_.setGMLvalues(GMLgraph);
		GMLgraph.prune();

		update_ = update_in;

		JPanel topPanel      = new JPanel();
		JPanel bottomPanel   = new JPanel();
		JPanel centerPanel   = new JPanel();
		getContentPane().setLayout(new BorderLayout());

		//Set up the menu bar.
		JMenuBar mb          = new JMenuBar();
		JMenu m              = new JMenu("Menu");
		JMenuItem jmi = new JMenuItem("Apply Changes");
		jmi.addActionListener(this);
		m.add(jmi);
		m.addSeparator();
		jmi = new JMenuItem("Exit");
		jmi.addActionListener(this);
		m.add(jmi);
		mb.add(m);
		JMenu m2             = new JMenu("Edit");
		jmi = new JMenuItem("GoToTop");
		jmi.addActionListener(this);
		m2.add(jmi);
		jmi = new JMenuItem("GoToBottom");
		jmi.addActionListener(this);
		m2.add(jmi);
		jmi = new JMenuItem("GoToLine");
		jmi.addActionListener(this);
		m2.add(jmi);
		jmi = new JMenuItem("Search");
		jmi.addActionListener(this);
		m2.add(jmi);
		jmi = new JMenuItem("Replace");
		jmi.addActionListener(this);
		m2.add(jmi);
		mb.add(m2);
		setJMenuBar(mb);

		//add editing controls to the top of the window
		okButton = new JButton("Apply");
		okButton.addActionListener(this);
		cancelButton = new JButton("Done");
		cancelButton.addActionListener(this);
		text1 = new JTextField(15);
		text1.setBackground(Color.white);
		text2 = new JTextField(15);
		text2.setBackground(Color.white);
		label1 = new JLabel("Search for:");
		label2 = new JLabel("    Replace all with:");

		topPanel.add(label1);
		topPanel.add(text1);
		topPanel.add(label2);
		topPanel.add(text2);
		topPanel.add(okButton);
		topPanel.add(cancelButton);
		getContentPane().add("North", topPanel);

		//Add small things at the bottom of the window.
		JButton button = new JButton("Apply Changes");
		button.addActionListener(this);
		bottomPanel.add(button);
		button = new JButton("Exit");
		button.addActionListener(this);
		bottomPanel.add(button);
		getContentPane().add("South", bottomPanel);

		//Add big things to the center area of the window.
		centerPanel.setLayout(new GridLayout(1, 2));
		JPanel p             = new JPanel();
		p.setLayout(new BorderLayout());
		p.add("North", new JLabel("Edit Graph Below; Press \"Apply Changes\" to update the graph.", SwingConstants.CENTER));
		originalGraphSpec_ = GMLgraph.toString(0);
		textarea_ = new JTextArea(originalGraphSpec_, 24, 80);
		textarea_.setBackground(Color.white);
		p.add("Center", new JScrollPane(textarea_));
		centerPanel.add(p);
		getContentPane().add("Center", centerPanel);

		hideAll_();
	}


	public void actionPerformed(ActionEvent event)
	{
		String arg = ((AbstractButton) event.getSource()).getText();
		if (((String) arg).equals("GoToTop"))
		{
			textarea_.select(0, 0);
			return;
		}
		if (((String) arg).equals("GoToBottom"))
		{
			textarea_.select(textarea_.getText().length(), textarea_.getText().length());
			return;
		}
		if (((String) arg).equals("GoToLine"))
		{
			hideAll_();
			buttonLabels_();
			label1.setVisible(true);
			label1.setText("Go To Line #:");
			text1.setVisible(true);
			okButton.setVisible(true);
			cancelButton.setVisible(true);
			lastaction = "Line";
			validate();
			pack();
			textarea_.requestFocus();
			return;
		}
		if (((String) arg).equals("Search"))
		{
			hideAll_();
			buttonLabels_();
			label1.setVisible(true);
			label1.setText("Search for:");
			text1.setVisible(true);
			okButton.setVisible(true);
			cancelButton.setVisible(true);
			lastaction = "Search";
			validate();
			pack();
			text1.setText("");
			textarea_.requestFocus();
			return;
		}
		if (((String) arg).equals("Replace"))
		{
			hideAll_();
			buttonLabels_();
			label1.setVisible(true);
			label1.setText("Search for:");
			text1.setVisible(true);
			label2.setVisible(true);
			text2.setVisible(true);
			okButton.setVisible(true);
			cancelButton.setVisible(true);
			lastaction = "Replace";
			validate();
			pack();
			text1.setText("");
			text2.setText("");
			textarea_.requestFocus();
			return;
		}
		if (((String) arg).equals("Apply"))
		{
			if (lastaction.equals("Search"))
			{
				String text     = textarea_.getText();
				String lookfor  = text1.getText().trim();
				int start       = textarea_.getSelectionEnd();
				int where       = text.indexOf(lookfor, start);
				if (where == -1)
				{  // Wrap around.
					where = text.indexOf(lookfor);
				}
				if (where != -1)
				{
					textarea_.select(where, where + lookfor.length());
				}
			}
			else if (lastaction.equals("Replace"))
			{
				String lookfor   = text1.getText();
				int lookfor_len  = lookfor.length();
				if (lookfor_len > 0)
				{
					int where;
					String text          = textarea_.getText();
					String new_text      = "";
					String replace_with  = text2.getText();
					int prev_where       = 0;
					while ((where = text.indexOf(lookfor, prev_where)) != -1)
					{
						new_text += text.substring(prev_where, where);
						new_text += replace_with;
						prev_where = where + lookfor_len;
					}
					if (prev_where < text.length())
					{
						new_text += text.substring(prev_where, text.length());
					}
					textarea_.setText(new_text);
				}
			}
			else if (lastaction.equals("Line"))
			{
				int whichline;
				try
				{
					whichline = Integer.parseInt(text1.getText().trim());
				}
				catch (NumberFormatException e)
				{
					whichline = -1;
				}

				String text    = textarea_.getText();
				int start;
				int end;
				int count;

				start = -1;
				for (count = 0; count < whichline - 1; count++)
				{
					start = text.indexOf("\n", start + 1);
					if (start == -1)
					{
						break;
					}
				}

				if (start != -1 || whichline == 1)
				{
					end = text.indexOf("\n", start + 1);
					if (end == -1)
					{
						end = text.length() - 1;
					}
					textarea_.select(start + 1, end);
				}
				else if (start == -1 && whichline > 0)
				{
					end = text.length();
					start = text.lastIndexOf("\n", end - 1);
					if (start == -1)
					{
						start = 0;
					}
					textarea_.select(start + 1, end);
				}

			}

			textarea_.requestFocus();
			return;
		}
		else if (((String) arg).equals("Done"))
		{
			hideAll_();
			lastaction = "";

			validate();
			pack();

			textarea_.requestFocus();
			return;
		}
		else if (((String) arg).equals("Find"))
		{  // Find.

			String lookfor  = textarea_.getSelectedText();
			if (lookfor != null && lookfor.length() > 0)
			{
				String text  = textarea_.getText();
				int pos      = textarea_.getSelectionStart();
				int where    = text.indexOf(lookfor, pos + 1);
				if (where == -1)
				{
					// Wrap around.
					where = text.indexOf(lookfor);
				}
				if (where == -1)
				{
					return;
				}
				textarea_.select(where, where + lookfor.length());
			}
			textarea_.requestFocus();
		}
		else if (((String) arg).equals("Find Backwards"))
		{  // Find backwards.

			String lookfor  = textarea_.getSelectedText();
			if (lookfor != null && lookfor.length() > 0)
			{
				String text  = textarea_.getText();
				int pos      = textarea_.getSelectionStart();
				int where    = text.lastIndexOf(lookfor, pos - 1);
				if (where == -1)
				{
					// Wrap around.
					where = text.lastIndexOf(lookfor, text.length() - 1);
				}
				if (where == -1)
				{
					return;
				}
				textarea_.select(where, where + lookfor.length());
			}

			textarea_.requestFocus();
		}

		if (((String) arg).equals("Exit"))
		{
			dispose();
		}
		if (((String) arg).equals("Apply Changes"))
		{
			// perform a graph update here...need to pass text to graph object

			String text                     = textarea_.getText();
			StringReader stream  = new StringReader(text);
			GMLlexer lexer                  = new GMLlexer(stream);

			Graph newgraph                  = null;
			try
			{
				GMLobject GMLgraph  = new GMLobject(lexer, null);
				GMLobject GMLtmp;

				// If the GML doesn't contain a graph, assume it is a graph.
				GMLtmp = GMLgraph.getGMLSubObject("graph", GMLobject.GMLlist, false);
				if (GMLtmp != null)
				{
					GMLgraph = GMLtmp;
				}
				newgraph = new Graph(GMLgraph);

				graph_.copy(newgraph);
				update_.update(true);
			}
			catch (ParseError error)
			{
				MessageDialog dg  = new MessageDialog(this, "Error", error.getMessage() + " at line " + lexer.getLineNumber() + " at or near \"" + lexer.getStringval() + "\".", true);
				return;
			}
			catch (IOException error)
			{
				MessageDialog dg  = new MessageDialog(this, "Error", error.getMessage(), true);
				return;
			}
			textarea_.requestFocus();
		}
	}


	private void buttonLabels_()
	{
		okButton.setText("Apply");
		cancelButton.setText("Done");
	}


	public void processEvent(AWTEvent event)
	{
		if (firstTime_)
		{
			firstTime_ = false;
			textarea_.requestFocus();
		}
		super.processEvent(event);
	}


	private void hideAll_()
	{
		label1.setVisible(false);
		text1.setVisible(false);
		label2.setVisible(false);
		text2.setVisible(false);

		okButton.setText("Find");
		cancelButton.setText("Find Backwards");
	}
}
