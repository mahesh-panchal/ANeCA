package TCS.VGJ.graph;

/*
 * File: EdgePropertiesDialog.java
 *
 * 4/11/97   Larry Barowski
 *
*/
//import java.awt.Dialog;
//import java.awt.Button;
//import java.awt.Frame;
import java.awt.AWTEvent;
//import java.awt.TextField;
//import java.awt.TextArea;
import java.awt.Color;
import java.awt.Component;
//import java.awt.Choice;
//import java.awt.Panel;
import java.util.StringTokenizer;
import java.util.Hashtable;
import java.util.Enumeration;

import TCS.VGJ.util.DPoint3;
import TCS.VGJ.util.DDimension;
import TCS.VGJ.util.DDimension3;
import TCS.VGJ.gui.MessageDialog;
import TCS.VGJ.gui.LPanel;
import TCS.VGJ.gui.InputDialog;
import TCS.VGJ.gui.TCSEvent;

import javax.swing.*;
import java.awt.event.*;

/**
 * A dialog class for changing the properties of an edge. If the edge
 * is null, the dialog is modeless and does not have fields for
 * changing properties, such as label, without defaults. Otherwise, the
 * dialog is modal.
 *	</p>Here is the <a href="../graph/EdgePropertiesDialog.java">source</a>.
 *
 * @author     Larry Barowski
 * @created    July 2, 2003
 */

public class EdgePropertiesDialog extends JDialog implements ActionListener
{
	private JTextField labelText_;
	private JTextArea pointsText_, dataText_;
	private JPanel dataPanel_;

	private Edge edge_;
	private Graph graph_;
	private Component[] notDefault_  = new Component[3];
	private int ndCount_             = 0;
	private JFrame frame_;
	private JComboBox style_, data_;
	private Hashtable dataHash_;
	private String currentData_;


	public EdgePropertiesDialog(JFrame frame, Edge edge_in, Graph graph_in)
	{
		super(frame, "", true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame_ = frame;

		graph_ = graph_in;
		edge_ = edge_in;
		//  if(edge_ == null)
		//     edge_ = Edge.defaults;

		LPanel p  = new LPanel();
		p.constraints.weighty = 0.0;
		notDefault_[ndCount_++] = p.addLineLabel("Label:", 1);
		labelText_ = p.addTextField(8, 0, -1, 1.0, 0.0, 1, 1);

		p.addLineLabel("Line Style", 1);
		style_ = new JComboBox();
		for (int i = 0; i < Edge.styleLabels.length; i++)
		{
			style_.addItem(Edge.styleLabels[i]);
		}
		style_.addActionListener(this);
		p.addComponent(style_, 0, -1, 1.0, 0.0, 0, 1);

		p.addLineLabel("Points in order x y z:", 0);

		pointsText_ = new JTextArea(4, 20);
		pointsText_.setBackground(Color.white);
		p.addComponent(pointsText_, 0, -1, 1.0, 1.0, 3, 0);

		p.constraints.gridwidth = 1;
		p.constraints.weightx = 0.0;

		p.addLineLabel("Data", 1);
		data_ = new JComboBox();
		data_.addActionListener(this);
		dataPanel_ = new JPanel();
		dataPanel_.add(data_);
		p.addComponent(dataPanel_, 0, -1, 1.0, 0.0, 0, 1);

		dataText_ = new JTextArea(4, 20);
		dataText_.setBackground(Color.white);
		p.constraints.insets.top = 0;
		p.addComponent(dataText_, 0, -1, 1.0, 1.0, 3, 0);

		p.addButtonPanel("Apply Cancel", 0, this);

		p.finish();
		getContentPane().add("Center", p);
		setEdge(edge_, graph_);
	}


	public void actionPerformed(ActionEvent event)
	{
		if (event.getSource() instanceof JButton)
		{
			String object = ((JButton) event.getSource()).getText();
			if ("Apply".equals(object))
			{
				if (setValues_())
				{
					setVisible(false);
				}
			}
			else if ("Cancel".equals(object))
			{
				setVisible(false);
			}
		}
		else if (event.getSource() == data_)
		{
			dataHash_.put(currentData_, dataText_.getText());

			String old_data  = currentData_;
			currentData_ = (String) data_.getSelectedItem();
			if (!currentData_.equals("<NEW>"))
			{
				String value  = (String) dataHash_.get(currentData_);
				if (value == null)
				{
					value = new String("");
				}
				dataText_.setText(value);
			}
			else
			{
				new InputDialog(frame_, "Enter name for new data item " + "(must be a letter followed by letters and numbers).", this, 9999);
				if (data_.getSelectedItem().equals("<NEW>"))
				{
					data_.setSelectedItem(old_data);
				}
			}
		}

	}


	public void processEvent(AWTEvent event)
	{
		// Avoid having everything destroyed.
		if(event instanceof TCSEvent){
			if (event.getSource() instanceof InputDialog)
			{
				String label  = (String) ((TCSEvent) event).getInformation();

				for (int i = 0; i < label.length(); i++)
				{
					char chr  = label.charAt(i);
					if (!(chr >= 'a' && chr <= 'z') && !(chr >= 'A' && chr <= 'Z') && !(i != 0 && chr >= '0' && chr <= '9'))
					{
						new MessageDialog(frame_, "Error", "Bad format for new data item name.", true);
						return;
					}
				}

				data_.addItem(label);
				data_.setSelectedIndex(data_.getItemCount() - 1);
				currentData_ = label;
				dataText_.setText("");
				return;
			} 
		}

		/*if (event.id == Event.WINDOW_DESTROY)
		{
			hide();
			return true;
		}
		return*/ super.processEvent(event);
	}


	public void setEdge(Edge edge_in, Graph graph_in)
	{
		graph_ = graph_in;
		edge_ = edge_in;
		//  if(edge_ == null)
		//     edge_ = Edge.defaults;

		String title;
		//  if(edge_ == Edge.defaults)
		//     title = "Properties For Newly Created Edges";
		//  else
		title = "Edge " + edge_.tail().id_ + " " + edge_.head().id_;

		setTitle(title);

		style_.setSelectedIndex(edge_.getLineStyle());

		//   if(edge_ != Edge.defaults)
		//   {
		labelText_.setText(edge_.getLabel());
		String points_string  = new String();
		DPoint3[] points      = edge_.points();
		for (int i = 0; i < points.length; i++)
		{
			points_string += points[i].x + " " + points[i].y + " " + points[i].z + "\n";
		}
		pointsText_.setText(points_string);

		// Can't remove items from a Choice in Java 1.0.
		dataPanel_.remove(data_);
		data_ = new JComboBox();
		dataPanel_.add(data_);
		dataHash_ = (Hashtable) edge_.data_.clone();
		data_.addItem("<NEW>");
		for (Enumeration keys = dataHash_.keys(); keys.hasMoreElements(); )
		{
			String key  = (String) keys.nextElement();
			data_.addItem(key);
		}
		if (data_.getItemCount() == 1)
		{  // Need at least one item.
			data_.addItem("Data");
		}
		data_.setSelectedIndex(1);
		currentData_ = (String) data_.getItemAt(1);

		String value          = (String) dataHash_.get(currentData_);
		if (value == null)
		{
			value = new String("");
		}
		dataText_.setText(value);

		for (int i = 0; i < ndCount_; i++)
		{
			notDefault_[i].setVisible(true);

		}
		//  }
		//  else
		//     for(int i = 0; i < ndCount_; i++)
		//        notDefault_[i].hide();

		pack();
	}


	private boolean setValues_()
	{
		//   if(edge_ != Edge.defaults)
		//   {
		String points_string       = pointsText_.getText();

		// Scan the points string.
		StringTokenizer tokenizer  = new StringTokenizer(points_string, " \t\n\r,", false);
		int num_tokens             = tokenizer.countTokens();
		a :
		if ((num_tokens % 3) != 0)
		{
			new MessageDialog(frame_, "Error", "Expecting " + "sets of three numbers for points", true);
		}
		else
		{
			int count         = num_tokens / 3;
			DPoint3[] points  = new DPoint3[count];

			for (int i = 0; i < count; i++)
			{
				points[i] = new DPoint3();
			}

			String token;
			int index         = 0;
			double val;
			while (tokenizer.hasMoreTokens())
			{
				for (int element = 0; element < 3; element++)
				{
					token = tokenizer.nextToken();
					try
					{
						val = new Double(token).doubleValue();
					}
					catch (Exception e)
					{
						char el  = 'X';
						if (element == 1)
						{
							el = 'Y';
						}
						if (element == 2)
						{
							el = 'Z';
						}
						new MessageDialog(frame_, "Error", "Element " + el + " of point " + index + " is not" + " a number.", true);
						break a;
					}

					if (element == 0)
					{
						points[index].x = val;
					}
					else if (element == 1)
					{
						points[index].y = val;
					}
					else
					{
						points[index].z = val;
					}
				}
				index++;
			}

			dataHash_.put(currentData_, dataText_.getText());

			for (Enumeration keys = dataHash_.keys(); keys.hasMoreElements(); )
			{
				String key    = (String) keys.nextElement();
				String value  = (String) dataHash_.get(key);
				if (value != null && value.length() != 0)
				{
					edge_.data_.put(key, value);
				}
			}

			// Replace the old edge.
			Edge edge         = new Edge(edge_.tail(), edge_.head(), points, false);
			graph_.insertEdge(edge);
			edge.setLabel(labelText_.getText());
			edge.setLineStyle(style_.getSelectedIndex());
			edge.data_ = edge_.data_;

			return true;
		}
		//   }

		return false;
	}
}
