package TCS.VGJ.graph;

/*
 * File: NodePropertiesDialog.java
 *
 * 5/29/96   Larry Barowski
 *
*/
//import java.awt.Dialog;
//import java.awt.Button;
//import java.awt.Frame;
import java.awt.AWTEvent;
//import java.awt.TextField;
//import java.awt.Choice;
//import java.awt.Checkbox;
import java.awt.Component;
//import java.awt.TextArea;
import java.awt.Color;
//import java.awt.Panel;
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
 * A dialog class for changing the properties of a node. If the node
 * is Node.defaults, the dialog is modeless and does not have fields for
 * changing properties, such as label, without defaults. Otherwise, the
 * dialog is modal.
 *	</p>Here is the <a href="../graph/NodePropertiesDialog.java">source</a>.
 *
 * @author     Larry Barowski
 * @created    July 2, 2003
 */

public class NodePropertiesDialog extends JDialog implements ActionListener
{
	private JTextField posX_, posY_, posZ_, bboxW_, bboxH_, bboxD_, labelText_, imageText_;
	private JComboBox shape_, imageType_, labelPos_;
	private JCheckBox defCB_;

	private Node node_;
	private Component[] notDefault_  = new Component[10];
	private int ndCount_             = 0;
	private JFrame frame_;
	private JComboBox data_;
	private JPanel dataPanel_;
	private JTextArea dataText_;

	private Hashtable dataHash_;
	private String currentData_;


	public NodePropertiesDialog(JFrame frame, Node node_in)
	{
		super(frame, "", true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		frame_ = frame;
		node_ = node_in;
		if (node_ == null)
		{
			node_ = Node.defaults;
		}

		LPanel p  = new LPanel();

		notDefault_[ndCount_++] = p.addLineLabel("Position:", 0);
		notDefault_[ndCount_++] = p.addLabel("X", 1, 1, 0.0, 1.0, 0, 2);
		notDefault_[ndCount_++] = posX_ = p.addTextField(8, 1, -1, 1.0, 1.0, 1, 1);
		notDefault_[ndCount_++] = p.addLabel("Y", 1, 1, 0.0, 1.0, 0, 2);
		notDefault_[ndCount_++] = posY_ = p.addTextField(8, 1, -1, 1.0, 1.0, 1, 1);
		notDefault_[ndCount_++] = p.addLabel("Z", 1, 1, 0.0, 1.0, 0, 2);
		notDefault_[ndCount_++] = posZ_ = p.addTextField(8, 0, -1, 1.0, 1.0, 1, 1);

		p.addLineLabel("Bounding Box:", 0);
		p.addLabel("Height", 1, 1, 0.0, 1.0, 0, 2);
		bboxH_ = p.addTextField(8, 1, -1, 1.0, 1.0, 1, 1);
		p.addLabel("Width", 1, 1, 0.0, 1.0, 0, 2);
		bboxW_ = p.addTextField(8, 1, -1, 1.0, 1.0, 1, 1);
		p.addLabel("Depth", 1, 1, 0.0, 1.0, 0, 2);
		bboxD_ = p.addTextField(8, 0, -1, 1.0, 1.0, 1, 1);

		p.addLineLabel("Shape:", 1);
		shape_ = new JComboBox();
		for (int i = 0; Node.shapeNames[i] != null; i++)
		{
			shape_.addItem(Node.shapeNames[i]);
		}
		shape_.addActionListener(this);
		p.addComponent(shape_, 0, -1, 1.0, 0.0, 0, 1);

		notDefault_[ndCount_++] = p.addLineLabel("Label:", 1);
		notDefault_[ndCount_++] = labelText_ = p.addTextField(8, 0, -1, 1.0, 1.0, 1, 1);

		p.addLabel("Label Position", 1, 1, 0.0, 1.0, 0, 2);
		labelPos_ = new JComboBox();
		labelPos_.addItem("Below");
		labelPos_.addItem("In (Autosize)");
		labelPos_.addItem("Center");
		labelPos_.addActionListener(this);
		p.addComponent(labelPos_, 0, -1, 1.0, 0.0, 0, 1);

		p.addLineLabel("Image: (Leave Height and Width blank for" + " automatic sizing.)", 0);

		p.addLabel("Type", 1, 1, 0.0, 1.0, 0, 2);
		imageType_ = new JComboBox();
		imageType_.addItem("URL");
		imageType_.addItem("File");
		imageType_.addActionListener(this);
		p.addComponent(imageType_, 0, -1, 1.0, 0.0, 0, 1);

		p.addLabel("Source", 1, 1, 0.0, 1.0, 0, 2);
		imageText_ = p.addTextField(8, 0, -1, 1.0, 1.0, 1, 1);

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
		setNode(node_);
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
		if(event instanceof TCSEvent){
			if (event.getSource() instanceof InputDialog)
			{
				String label  = (String) ((TCSEvent) event).getInformation();

				for (int i = 0; i < label.length(); i++)
				{
					char chr  = label.charAt(i);
					if (!(chr >= 'a' && chr <= 'z') &&
							!(chr >= 'A' && chr <= 'Z') &&
							!(i != 0 && chr >= '0' && chr <= '9'))
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
		// Avoid having everything destroyed.
		/*else if (event.id == Event.WINDOW_DESTROY)
		{
			hide();
			return true;
		}
		return*/ super.processEvent(event);
	}


	public void setNode(Node node_in)
	{
		node_ = node_in;
		if (node_ == null)
		{
			node_ = Node.defaults;
		}

		String title;
		if (node_ == Node.defaults)
		{
			title = "Properties For Newly Created Nodes";
		}
		else
		{
			title = "Node " + node_.id_;
		}

		setTitle(title);

		if (node_ != Node.defaults)
		{
			DPoint3 pos  = node_.getPosition3();
			posX_.setText(String.valueOf(pos.x));
			posY_.setText(String.valueOf(pos.y));
			posZ_.setText(String.valueOf(pos.z));
			labelText_.setText(node_.getLabel());

			for (int i = 0; i < ndCount_; i++)
			{
				notDefault_[i].setVisible(true);
			}
		}
		else
		{
			for (int i = 0; i < ndCount_; i++)
			{
				notDefault_[i].setVisible(false);
			}
		}

		DDimension3 bbox  = node_.getBoundingBox3();
		bboxW_.setText(String.valueOf(bbox.width));
		bboxH_.setText(String.valueOf(bbox.height));
		bboxD_.setText(String.valueOf(bbox.depth));

		shape_.setSelectedIndex(node_.getShape());

		imageType_.setSelectedIndex(0);
		String type       = node_.getImageType();
		if (type != null && type.equalsIgnoreCase("file"))
		{
			imageType_.setSelectedIndex(1);
		}
		imageText_.setText(node_.getImageSource());

		labelPos_.setSelectedIndex(node_.getLabelPosition());

		// Can't remove items from a Choice in Java 1.0.
		dataPanel_.remove(data_);
		data_ = new JComboBox();
		dataPanel_.add(data_);
		dataHash_ = (Hashtable) node_.data_.clone();
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

		String value      = (String) dataHash_.get(currentData_);
		if (value == null)
		{
			value = new String("");
		}
		dataText_.setText(value);

		pack();
	}


	private boolean setValues_()
	{
		String current  = new String();
		String tmp;
		boolean gotw    = true;
		boolean goth    = true;
		try
		{
			double x;
			double y;
			double z;
			if (node_ != Node.defaults)
			{
				current = "X";
				x = new Double(posX_.getText().trim()).doubleValue();
				current = "Y";
				y = new Double(posY_.getText().trim()).doubleValue();
				current = "Z";
				z = new Double(posZ_.getText().trim()).doubleValue();
				node_.setPosition(x, y, z);
			}
			current = "Width";
			tmp = bboxW_.getText().trim();
			double w    = 0.0;
			if (tmp.length() > 0)
			{
				w = new Double(tmp).doubleValue();
			}
			else
			{
				gotw = false;
			}
			current = "Height";
			tmp = bboxH_.getText().trim();
			double h    = 0.0;
			if (tmp.length() > 0)
			{
				h = new Double(tmp).doubleValue();
			}
			else
			{
				goth = false;
			}
			current = "Depth";
			double d    = new Double(bboxD_.getText().trim()).doubleValue();

			String src  = imageText_.getText().trim();
			if (src.length() <= 0)
			{
				if (!gotw)
				{
					current = "Width (required if no Image Source)";
					throw new Exception();
				}
				if (!goth)
				{
					current = "Height (required if no Image Source)";
					throw new Exception();
				}
			}
			node_.setBoundingBox(w, h, d);
			node_.setShape(shape_.getSelectedIndex());
			if (node_ != Node.defaults)
			{
				node_.setLabel(labelText_.getText());
			}
			node_.setImageSource(src);
			node_.setImageType((String) imageType_.getSelectedItem());
			node_.setImage(null, !gotw, !goth);
			node_.setLabelPosition((String) labelPos_.getSelectedItem());

			dataHash_.put(currentData_, dataText_.getText());

			for (Enumeration keys = dataHash_.keys(); keys.hasMoreElements(); )
			{
				String key    = (String) keys.nextElement();
				String value  = (String) dataHash_.get(key);
				if (value != null && value.length() != 0)
				{
					node_.data_.put(key, value);
				}
			}
		}
		catch (Exception e)
		{
			new MessageDialog(frame_, "Error", "Entered " + current + " is not a number.", true);
			return false;
		}
		return true;
	}
}
