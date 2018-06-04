package GeoDis;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import java.io.*;
import java.util.*;
import ANeCA.NCPA;
import ANeCA.Mediator;
import ANeCA.ProgressionException;

public class GeoDis_GUI extends JPanel
{

	private JLabel permutations_label = new JLabel ("  Permutations");
	private JLabel title = new JLabel ("Cladistic Nested Analysis");

	//ImageIcon openIcon = new ImageIcon("open.gif");
	//ImageIcon saveIcon = new ImageIcon("save.gif");
	
	private JButton run_button = new JButton("RUN");
	private JButton infile_button = new JButton("Input file");
	private JButton outfile_button = new JButton("Output file");
	//private JButton quit_button = new JButton("Quit");
	private JButton help_button = new JButton("Help");
	private JButton inference_button = new JButton("Run Inference Key");
		
	private JCheckBox weights_checkbox = new JCheckBox(" Outgroup weights",false);
	private JCheckBox matrix_checkbox = new JCheckBox(" User-defined distances",false);
	private JCheckBox dd_checkbox = new JCheckBox(" Decimal degrees",false);

	public static JLabel infile_label = new JLabel("none selected       ");
	public static JLabel outfile_label = new JLabel("none selected       ");
	public static JTextField permutations_field = new JTextField("10000",5);

	private GridBagLayout gridbag = new GridBagLayout();
	//private Container contentPane = this.getContentPane();
  
    public static JMenuBar menuBar = NCPA.getFrame().getJMenuBar();
    private JMenu menuA =  new JMenu("GeoDis");
    private JMenuItem about_Item = new JMenuItem("Info");  
    private JMenuItem citation_Item = new JMenuItem("Citation");  
 	private JMenuItem help_Item = new JMenuItem("Help");  
 	private JMenuItem www_Item = new JMenuItem("WWW");  


	public static JMenuBar menuBar2 = new JMenuBar();
	public static JMenu menu1 =  new JMenu("File");
	public static JMenu menu2 =  new JMenu("Edit");

	public static JMenuItem save_Item = new JMenuItem("Save As");
	public static JMenuItem print_Item = new JMenuItem("Print");
	public static JMenuItem page_Item = new JMenuItem("Page Setup");
    public static JMenuItem copy_Item = new JMenuItem("Copy");  
 	public static JMenuItem cut_Item = new JMenuItem("Cut");  
 	public static JMenuItem selectall_Item = new JMenuItem("Select All");  

	private ProgressMonitor bar;
	private javax.swing.Timer timer;
	public final static int ONE_SECOND = 1000;	
	  
	public static PrintStream outdoc;  
	  
	public static JTextArea textArea = new JTextArea();

	//public static File inputfile;
	//public static File outputfile;
 	 
 	public static JFrame f; 
 	  
	public GeoDis_GUI()
	{
		try
			{
        	UIManager.getSystemLookAndFeelClassName();
        	} 
        catch (Exception e) { }

		/*
		UIManager.getCrossPlatformLookAndFeelClassName() 
               Returns the string for the one look-and-feel guaranteed to work -- the Java Look & Feel. 
        UIManager.getSystemLookAndFeelClassName() 
               Specifies the look and feel for the current platform. On Win32 platforms, this specifies the
               Windows Look & Feel. On Mac OS platforms, this specifies the Mac OS Look & Feel.
               On Sun platforms, it specifies the CDE/Motif Look & Feel. 
        "javax.swing.plaf.metal.MetalLookAndFeel" 
               Specifies the Java Look & Feel. (The codename for this look and feel was Metal.) This
               string is the value returned by the getCrossPlatformLookAndFeelClassName method. 
        "com.sun.java.swing.plaf.windows.WindowsLookAndFeel" 
               Specifies the Windows Look & Feel. Currently, you can use this look and feel only on
               Win32 systems. 
        "com.sun.java.swing.plaf.motif.MotifLookAndFeel" 
               Specifies the CDE/Motif Look & Feel. This look and feel can be used on any platform. 
        "javax.swing.plaf.mac.MacLookAndFeel" 
               Specifies the Mac OS Look & Feel, which can be used only on Mac OS platforms. 
		*/

		setLayout (gridbag);
		
		menuBar.add(menuA);
		menuA.add(about_Item);
		menuA.add(citation_Item);
		menuA.add(help_Item);
		menuA.add(www_Item);
		NCPA.getFrame().setJMenuBar(menuBar);
		
		run_button.addActionListener(new Run_listener(this));
		infile_button.addActionListener(new Infile_listener(this, infile_label));
		outfile_button.addActionListener(new Outfile_listener(this, outfile_label));
		weights_checkbox.addItemListener(new Weights_listener());
		matrix_checkbox.addItemListener(new Matrix_listener());
		dd_checkbox.addItemListener(new DD_listener());
		dd_checkbox.setSelected(true);
		//quit_button.addActionListener(new Quit_listener(this));
		inference_button.addActionListener(new InferenceListener());
		help_Item.addActionListener(new Help_listener());
		about_Item.addActionListener(new About_listener(this));
		citation_Item.addActionListener(new Citation_listener(this));
		www_Item.addActionListener(new WWW_listener(this));

		save_Item.addActionListener(new Common_listener(textArea,this));
		print_Item.addActionListener(new Common_listener(textArea,this));
		page_Item.addActionListener(new Common_listener(textArea,this));
		copy_Item.addActionListener(new Common_listener(textArea,this));
		cut_Item.addActionListener(new Common_listener(textArea,this));
		selectall_Item.addActionListener(new Common_listener(textArea,this));
		
		save_Item.setActionCommand("save");
		print_Item.setActionCommand("print");
	    page_Item.setActionCommand("page");
	 	copy_Item.setActionCommand("copy");
	 	cut_Item.setActionCommand("cut");
		selectall_Item.setActionCommand("selectall");

		timer = new javax.swing.Timer(ONE_SECOND, new TimerListener());                                 
	
		// set main font

		//first_row (title);
		//blank_row(new JLabel(" "));
		second_row(infile_button, infile_label, weights_checkbox);
		blank_row(new JLabel(" "));
		third_row(outfile_button, outfile_label, matrix_checkbox);
		blank_row(new JLabel(" "));
		fourth_row(run_button, dd_checkbox);
		blank_row(new JLabel(" "));
		fifth_row(permutations_field, permutations_label);
		blank_row(new JLabel(" "));
		sixth_row(inference_button);
		inference_button.setEnabled(false);
		setInfo();
		

	} // constructor GeoDis_GUI	
	
	private void setInfo(){
		String gdinfile = Mediator.getGeoDisInputFile();
		if(gdinfile != null){
			GeoDis.infilename = gdinfile;
			String gdoutfile = gdinfile.substring(0,gdinfile.lastIndexOf("."))+".gdout";
			GeoDis.outfilename = gdoutfile;
 			if (gdinfile.length() < 20){	 
 				gdinfile = gdinfile.concat("                         ");
 				gdinfile = gdinfile.substring(0,19);
  			} else {
 				gdinfile = gdinfile.substring(0,16);
 				gdinfile = gdinfile.concat("..."); 				
 			}
 			if (gdoutfile.length() < 20){	 
 				gdoutfile = gdoutfile.concat("                         ");
 				gdoutfile = gdoutfile.substring(0,19);
  			} else {
 				gdoutfile = gdoutfile.substring(0,16);
 				gdoutfile = gdoutfile.concat("..."); 				
 			}
			infile_label.setText(gdinfile);
			outfile_label.setText(gdoutfile);
			GeoDis.printToFile = true;
		}
	}


	private void first_row (JLabel label)
	{		
		//GridBagConstraints constraints = new GridBagConstraints();
		//constraints.gridwidth = GridBagConstraints.REMAINDER;
		//constraints.ipady = 8;
		//gridbag.setConstraints (button, constraints);
		//button.setFont (new Font ("Times", Font.ITALIC, 13));
		//button.setForeground(Color.blue);
		//contentPane.add(button);
		add(new JLabel("   "));
		label.setFont (new Font ("Times", Font.ITALIC, 13));
		label.setForeground(Color.blue);
		add(label);

	} // method first row


	private void second_row (JButton button, JLabel label, JCheckBox check)
	{
		
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.ipadx = 30;
		constraints.ipady = 10;
		constraints.anchor = GridBagConstraints.WEST;
		gridbag.setConstraints (button, constraints);
		button.setMnemonic('i');
		add(button);
		
		add(new JLabel("     "));
		
		constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.WEST;
		//field.setBackground(Color.white);
		gridbag.setConstraints (label, constraints);
		label.setFont (new Font ("Monaco", Font.PLAIN, 9));
		add(label);

		constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.WEST;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints (check, constraints);
		add(check);

	
	} // method second_row


	private void third_row (JButton button, JLabel label, JCheckBox check)
	{
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.ipadx = 20;
		constraints.ipady = 10;
		constraints.anchor = GridBagConstraints.WEST;
		gridbag.setConstraints (button, constraints);
		add(button);
		button.setMnemonic('o');
		
		add(new JLabel(""));
	
		constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.WEST;
		//field.setBackground(Color.white);
		gridbag.setConstraints (label, constraints);
		label.setFont (new Font ("Monaco", Font.PLAIN, 9));
		add(label);

		constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.WEST;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		//check.setEnabled(false);
		gridbag.setConstraints (check, constraints);
		add(check);	


	} // method third_row


	private void fourth_row (JButton button, JCheckBox check)
	{
				
		GridBagConstraints constraints = new GridBagConstraints();
		//constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.ipadx = 40;
		constraints.ipady = 30;
		constraints.anchor = GridBagConstraints.WEST;
		gridbag.setConstraints (button, constraints);
		//button.setFont (new Font ("Times", Font.ITALIC, 13));
		button.setForeground(new Color(25,85,215));
		button.setMnemonic('r');
		add(button);
		
		add(new JLabel(""));
		add(new JLabel(""));

		constraints = new GridBagConstraints();
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.anchor = GridBagConstraints.WEST;
		//check.setEnabled(false);
		gridbag.setConstraints (check, constraints);
		add(check);	



/*
		constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.WEST;
		field.setBackground(Color.white);
		gridbag.setConstraints (field, constraints);
		contentPane.add(field);

		constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.WEST;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		label.setForeground(Color.black);
		gridbag.setConstraints (label, constraints);
		contentPane.add(label);		
*/		
	} // method fourth_row


	private void blank_row (JLabel blank)
	{
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints (blank, constraints);
		add(blank);

	} // method blank_row


	private void fifth_row (JTextField field, JLabel label)
	{
		add(new JLabel(""));
		add(new JLabel(""));
		add(new JLabel(""));

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.WEST;
		field.setBackground(Color.white);
		gridbag.setConstraints (field, constraints);
		add(field);
		
		constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.WEST;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		label.setForeground(Color.black);
		gridbag.setConstraints (label, constraints);
		add(label);				

	} // method fifth_row

	private void sixth_row (JButton button)
	{
		add(new JLabel(""));
		add(new JLabel(""));
		add(new JLabel(""));

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.WEST;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints (button, constraints);
		add(button);
		
	} // method sixth_row

   /**
     * The actionPerformed method in this class
     * is called each time the Timer "goes off".
     */
    class TimerListener implements ActionListener 
    {
        public void actionPerformed(ActionEvent evt) 
        {     
        
            
            if (bar.isCanceled() || GeoDis.done) 
            {
        	 	//System.out.print("*END ");
             	bar.close();
                GeoDis.done = true;
                Toolkit.getDefaultToolkit().beep();
                timer.stop();
			} 
            else 
            {
       			//System.out.print(GeoDis.percentage + "%");
                bar.setNote("                     " + (int)GeoDis.percentage + "%");
                bar.setProgress(GeoDis.progress);
            }
        }
    }

	class Run_listener implements ActionListener
	{
		int k;
		String someValue;
		JFrame frame;

		public Run_listener(JPanel theframe)
		{
			frame = NCPA.getFrame();
			
		} // constructor Run_listener
		
		public void actionPerformed(ActionEvent e)
		{
			
			if (GeoDis.infilename.equalsIgnoreCase("none selected"))
			{
					JOptionPane.showMessageDialog(frame,"No input file was selected",
                "GeoDis warning", JOptionPane.WARNING_MESSAGE);

			}
				
			else
			{
				GeoDis.numPermutations = Integer.parseInt(permutations_field.getText());		

				final SwingWorker worker = new SwingWorker() 
				{
                	public Object construct() 
                	{
                        /*GeoDis.logfile = new TextOutputStream(GeoDis.logfilename);
                        GeoDis.logfile.print("Differentiating population structure from history - Geodis");
                        GeoDis.logfile.print("  2.5");
                        GeoDis.logfile.print("\n(c) Copyright, 1999-2006 David Posada and Alan Templeton");
                        GeoDis.logfile.print("\nContact: David Posada, University of Vigo, Spain (dposada@uvigo.es)");
                        GeoDis.logfile.print("\n________________________________________________________________________\n\n");
                        GeoDis.logfile.print("Input file: " + GeoDis.infilename);
                        GeoDis.logfile.println("\n\n" + (new Date()).toString());*/
                        GeoDis.startLogFile();
		                //...code that might take a while to execute is here.
		                try{
		                	if (GeoDis.doingDistances)
								GeoDis.readMatrix();
							else
								GeoDis.readLocations();

						//try {
							GeoDis.readClade();
						//	}
        				//catch (Exception excep) 
        				//	{ 						
        				//JOptionPane.showMessageDialog(frame,"Error in clade X",
	                	//"GeoDis error", JOptionPane.ERROR_MESSAGE);
						//	}
					
							bar = new ProgressMonitor(frame, "Estimating P-values by permutation...", 
		                                  "", 0, GeoDis.numClades*GeoDis.numPermutations);
											
							bar.setProgress(0);				
							bar.setMillisToDecideToPopup(1 * ONE_SECOND);
							timer.start();
	
							GeoDis.statistics();
						
							GeoDis.done = true;
							timer.stop();
							bar.close();
						
							JOptionPane.showMessageDialog(frame,"The analysis is finished.      ",
	                			"GeoDis information", JOptionPane.INFORMATION_MESSAGE);
							if(!GeoDis.doingDistances)
								GeoDis.pairwiseKm();
						
							GeoDis.printOut();	
							inference_button.setEnabled(true);					
						
							if (GeoDis.printToFile == true){ // we are done,really
								//System.exit(0);
							}
						} catch (ProgressionException e){
	        				JOptionPane.showMessageDialog(frame,"Error in clade X",
			                	"GeoDis error", JOptionPane.ERROR_MESSAGE);
			                Mediator.startGeodis(); // This needs a better clean up.
						}
		                return someValue;       
		           }
                };
			}
		} // method actionPerformed

	} // class Run_listener

 
	class Infile_listener implements ActionListener
	{
		JFrame frame;
		/*JFileChooser dialog;*/
		FileDialog dialog;
		int returnVal;
		JLabel label;
		/*String iname, inameShort;*/
		String iname;
		
		public Infile_listener(JPanel theframe, JLabel thelabel)
		{		
			frame = NCPA.getFrame();
			/*dialog = new JFileChooser("");*/
			label = thelabel;
		} // constructor add_listener
		
		public void actionPerformed(ActionEvent e) throws SecurityException
		{
			try
			{
				dialog = new FileDialog(frame, "Open file with nested clade description", FileDialog.LOAD);
				dialog.setVisible(true);
			}
			catch (Throwable f)
			{
				JOptionPane.showMessageDialog(frame,"It appears your VM does not allow file loading", "GeoDis error", JOptionPane.ERROR_MESSAGE);		
				return;
			}			

			if (dialog.getFile() != null) /* a file was selected */
			{
				iname = dialog.getFile();
				File ifile = new File (dialog.getDirectory() + iname);
				
				/* this file cannot be read */ /* it seems that all this is already automatic for FileDialog? */
				/*if (ifile.Read() == false)
				{
	 				int value = JOptionPane.showConfirmDialog(frame, " File \"" + oname + "\" already exists" + 
	 					"\n would you like overwrite it ?","Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
	 			
	 				if (value == JOptionPane.NO_OPTION)
	 					{
 						dialog.dispose();
 						}
				}
				*/
				
				GeoDis.infilename = dialog.getDirectory() + iname;
				inference_button.setEnabled(false);	

				/* adjust label size */
 				if (iname.length() < 20)
 				{	 
 					iname = iname.concat("                         ");
 					iname = iname.substring(0,19);
  				}
 				else
 				{
 					iname = iname.substring(0,16);
 					iname = iname.concat("..."); 				
 				}
				label.setText(iname);
				GeoDis.logfilename = dialog.getDirectory() + GeoDis.logfilename;
			}
			

			/*
			returnVal = dialog.showOpenDialog(frame);
	
			if (returnVal == JFileChooser.APPROVE_OPTION) 
			{
				inputfile = dialog.getSelectedFile();				
 				
 				iname = inputfile.getName();
 				GeoDis.infilename = iname;
 				inameShort = "" + iname;
 							
 				if (iname.length() < 20)
 				{	
 					iname = iname.concat("                             ");
 					iname = iname.substring(0,20);
 				}
 				
 				else
 				{
 					iname = iname.substring(0,17);
 					iname = iname.concat("..."); 				
 				}
 			
 				if (inputfile.canRead())
 				{
 					label.setText(iname);
 				}
 				
 				else
 				{
	 				int value = JOptionPane.showConfirmDialog(frame, " File \"" + inameShort + "\" does not exist" + 
	 				"\n would you like create it ?","Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
	 				
	 				if (value == JOptionPane.YES_OPTION)
	 				{
						label.setText(iname);
					}
				}
 			}
 		*/
 		
 					
		} // method actionPerformed

	} // class add_listener


	class Outfile_listener implements ActionListener 
	{
	
		JFrame frame;
		//JFileChooser dialog;
		FileDialog dialog;
		int returnVal;
		JLabel label;
		//String oname, onameShort;
		String oname;
		
		public Outfile_listener(JPanel theframe, JLabel thelabel)  
		{ 
			frame = NCPA.getFrame();
			/*dialog = new JFileChooser("");*/
			label = thelabel;
		} // constructor Outfile_listener
		
		public void actionPerformed(ActionEvent e) throws SecurityException
		{
			try
			{
				dialog = new FileDialog(frame, "Open file to print NCPA results", FileDialog.SAVE);
				dialog.setVisible(true);
			}
			catch (Throwable f)
			{
				JOptionPane.showMessageDialog(frame,"It appears your VM does not allow file saving", "GeoDis error", JOptionPane.ERROR_MESSAGE);		
				return;
			}			
			
			if (dialog.getFile() != null) /* a file was selected */
			{
				oname = dialog.getFile();
				File ofile = new File (dialog.getDirectory() + oname);
				
				/* this file already exists */ /* it seems that all this is automatic for FileDialog? */
				/*if (ofile.canWrite() == true)
				{
	 				int value = JOptionPane.showConfirmDialog(frame, " File \"" + oname + "\" already exists" + 
	 					"\n would you like overwrite it ?","Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
	 			
	 				if (value == JOptionPane.NO_OPTION)
	 					{
 						dialog.dispose();
 						}
				}
				*/
				
				GeoDis.outfilename = dialog.getDirectory() + oname;	

				/* adjust label size */
 				if (oname.length() < 20)
 				{	 
 					oname = oname.concat("                         ");
 					oname = oname.substring(0,19);
  				}
 				else
 				{
 					oname = oname.substring(0,16);
 					oname = oname.concat("..."); 				
 				}

				label.setText(oname);
				GeoDis.printToFile = true;
			}
			
			

		/*
			returnVal = dialog.showSaveDialog(frame);

			if (returnVal == JFileChooser.APPROVE_OPTION) 
			{
				outputfile = dialog.getSelectedFile();

 				oname = outputfile.getName();
 				
 				onameShort = "" + oname;
 				
 				if (oname.length() < 20)
 				{	 
 					oname = oname.concat("                         ");
 					oname = oname.substring(0,19);
 				
 				}
 				else
 				{
 					oname = oname.substring(0,16);
 					oname = oname.concat("..."); 				
 				}
 			

  				if (!outputfile.canWrite())
 				{
 					label.setText(oname);
					
				}
	 			
	 			else
	 			{
	 				int value = JOptionPane.showConfirmDialog(frame, " File \"" + onameShort + "\" already exist" + 
	 				"\n would you like overwrite it ?","Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
	 				
	 				if (value == JOptionPane.YES_OPTION)
	 				{
 						label.setText(oname);
 					}
 				}
 			}
		*/
		
		} // method actionPerformed
		
	} // class Outfile_listener



	class Common_listener implements ActionListener 
	{
		JTextArea area;
		String string;
		JFrame frame;
		//File file;
		//int returnVal;
		FileDialog dialog;
		public Common_listener(JTextArea thearea, JPanel theframe)  
		{ 
			area = thearea;
			frame = NCPA.getFrame();
		
		} // constructor Outfile_listener
		
		public void actionPerformed(ActionEvent e) throws SecurityException
		{
	 		 JMenuItem source = (JMenuItem)(e.getSource()); 	
			 string = source.getActionCommand();	 

			if (string.equalsIgnoreCase("save"))
			{
				try
				{
					dialog = new FileDialog(frame, "Open file to save NCPA results", FileDialog.SAVE);
					dialog.setVisible(true);
				}
				catch (Throwable f)
				{
					JOptionPane.showMessageDialog(frame,"It appears your VM does not allow file saving", "GeoDis error", JOptionPane.ERROR_MESSAGE);		
					return;
				}			
			
				if (dialog.getFile() != null) /* a file was selected */
				{
					String sname = dialog.getFile();
					File sfile = new File (dialog.getDirectory() + sname);
					
					/* this file already exists */ /* it seems that all this is automatic for FileDialog? */
					/*if (sfile.canWrite() == true)
					{
		 				int value = JOptionPane.showConfirmDialog(frame, " File \"" + sname + "\" already exists" + 
		 					"\n would you like overwrite it ?","Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		 			
		 				if (value == JOptionPane.NO_OPTION)
		 					{
	 						dialog.dispose();
	 						}
					}
					*/
					
					GeoDis.outfilename = dialog.getDirectory() + sname;	
					GeoDis.printToFile = true;
					GeoDis.printOut();
				}
				
				
				/*
				JFileChooser dialog = new JFileChooser("");
				returnVal = dialog.showSaveDialog(frame);

				if (returnVal == JFileChooser.APPROVE_OPTION) 
				{
					file = dialog.getSelectedFile();
					
	  				if (!file.canWrite())
	 				{
	 					outputfile = file;
						GeoDis.printOut();	
					}
					else
					{
		 				int value = JOptionPane.showConfirmDialog(frame, " File " + GeoDis.outfilename + " already exist" + 
		 				"\n would you like overwrite it ?","Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		 				
		 				if (value == JOptionPane.YES_OPTION)
		 				{
		 					GeoDis.outfilename = file.getName();
		 					GeoDis.printOut();		
	 					}
					}
				} 
				*/

			}

			if (string.equalsIgnoreCase("print"))
			{
				
					// Java 1.2
					// Get a PrinterJob
				    //PrinterJob job = PrinterJob.getPrinterJob();
				    // Ask user for page format (e.g., portrait/landscape)
				   // PageFormat pf = job.pageDialog(job.defaultPage());

				// Obtain a PrintJob object. This posts a Print Dialog.
				// printprefs stores user printing preferences
				Toolkit toolkit = f.getToolkit();
				Properties printprefs = new Properties();
				PrintJob job = toolkit.getPrintJob(f,"GeoDis " + GeoDis.VERSION_NUMBER, printprefs);
				
				// If the user clicked Cancel in the print dialog do nothing
				if (job == null) 
					return;
				
				// Check the size of the component and of the page
				Dimension size = area.getSize();
				Dimension pagesize = job.getPageDimension();
			
				//System.out.println("Text width: " + size.width + "  height: " + size.height);				
				//System.out.println("page width: " + pagesize.width + "  height: " + pagesize.height);
				 
				double dpages = (double)size.height/(double)pagesize.height; 
				long pages = Math.round(dpages+0.5);
				int y = 0;
				
				for (int i=0; (long) i<pages; i++)
				{	
					// Get a Graphics object for the first page of output
					Graphics page = job.getGraphics();		
							
					// Center the ouput on the page
					//page.translate((pagesize.width - size.width)/2, (pagesize.height - size.height)/2);
					page.translate((pagesize.width - size.width)/2, y);
					y -= pagesize.height;
					//y = y - 700;					
					//y -= (pagesize.height-10);
					
					// Draw a border around the output area
					//page.drawRect(-1,1,pagesize.width+1, pagesize.height+1);
					
					// Set clipping region
					page.setClip(0,0,size.width,size.height);
			
					// Print the component
					area.print(page);
					
					// Finish up printing
					page.dispose();   // end the page-send it to the printer
				}

				job.end();        // end the print job
				
				
			}

			if (string.equalsIgnoreCase("page"))
			{
				// not used by now
			}
			
			if (string.equalsIgnoreCase("copy"))
			{
				textArea.copy();
			}

			if (string.equalsIgnoreCase("cut"))
			{
				textArea.cut();
			}


			if (string.equalsIgnoreCase("selectall"))
			{
				textArea.selectAll();
			}
			 

		
			 
		} // method actionPerformed

	} // class Common_listener


	class Matrix_listener implements ItemListener
	{
		float count = 0;
		public Matrix_listener()
		{

		} // constructor Distances_listener
		
		 public void itemStateChanged (ItemEvent event)
		{
			
			if ((double) count%2.0  == 0)
			{
				GeoDis.doingDistances = true;
				dd_checkbox.setEnabled(false);
			}
			
			else 
			{
				GeoDis.doingDistances = false;
				dd_checkbox.setEnabled(true);
			}
			count++;
			

		
		} // method itemStateChangedformed

	} // class Distances_listener


	class Weights_listener implements ItemListener
	{
		float count = 0;

		public Weights_listener()
		{
		
		} // constructor change_listener
		
		 public void itemStateChanged (ItemEvent event)
		{
			if ((double) count%2.0  == 0)
				GeoDis.weights = true;
			
			else 
				GeoDis.weights = false;

			count++;
			
		} // method itemStateChanged

	} // class Weights_listener




	class DD_listener implements ItemListener
	{
		float count = 0;

		public DD_listener()
		{

		} // constructor Distances_listener
		
		 public void itemStateChanged (ItemEvent event)
		{

			if ((double) count%2.0  == 0)
			{
				GeoDis.usingDecimalDegrees = true;
				matrix_checkbox.setEnabled(false);
			}
			
			else 
			{
				GeoDis.usingDecimalDegrees = false;
				matrix_checkbox.setEnabled(true);
			}
			count++;

			
		} // method itemStateChanged

	} // class DD_listener


	class Help_listener implements ActionListener
	{
		JFrame help_frame = new JFrame();
		private JTextArea area = new JTextArea(40,50);
	    
		private GridBagLayout gridbag = new GridBagLayout();
		private Container contentPane = help_frame.getContentPane();
		Document doc = area.getDocument();
	    PrintStream helpdoc = new PrintStream(new DocumentOutputStream(doc));      
		TextOutputStream helpfile = new TextOutputStream(helpdoc);

		
		public void actionPerformed(ActionEvent e)
		{
			help_frame.setTitle("Help for Geodis " + GeoDis.VERSION_NUMBER);
			area.setMargin(new Insets(5,10,10,5));
		    area.setFont (new Font ("Monaco", Font.PLAIN, 9));

			help_frame.setBounds(300,50,600,500);
			help_frame.setResizable(false);	
	       	contentPane.add(area);
 			contentPane.add(new JScrollPane(area), BorderLayout.CENTER);
			printHelp();
			help_frame.setVisible(true);	
	
		} // method actionPerformed-	

		public void printHelp()
		{
		    WindowListener l = new WindowAdapter() 
		    {
		        public void windowClosing(WindowEvent e) 
		        {
		            //System.exit(0);
		            help_frame.dispose();
		        }
		    };
			
            helpfile.println("GEODIS 2.5 Help File\n\n");
            helpfile.println("GeoDis implements the nested clade analysis (Templeton et al 1995) \nTo use GeoDis you must have a cladogram in which a nested structure has been defined\nThe input for GeoDis is the description of this nested cladogram. Building this is file\nis tedious and requires attention. This format is described below line by line\nComments are indicated after double slash (//) and should not appear in the\nfinal input file. Explore the example files provided.\nIt is highly recommended that you read the documentation.\n\n");
            helpfile.println("\nHallucigenia  mtDNA         // locationName of the data set ");
            helpfile.println("\n3                           // Number of populations ");
            helpfile.println("\n1 Green Mountain            // Number and name of the population ");
            helpfile.println("\n7  25 30 30 N  60 25 34 E   // Sample size, latitude and longitude");
            helpfile.println("\n2 Blue Mountain");
            helpfile.println("\n6  22 01 45 N  60 35 22 E");
            helpfile.println("\n Red Mountain");
            helpfile.println("\n7  28 44 56 N  60 21 34 E");
            helpfile.println("\n5                    // Total number of clades in the input file");
            helpfile.println("\nClade 1-1            // name of the nesting clade");
            helpfile.println("\n6                    // Number of clades nested in the nesting clade");
            helpfile.println("\nI II III IV V        // name of these clades");
            helpfile.println("\n0  1   1  1 1        // cladogram location for each clade : tip(1), interior (0)");
            helpfile.println("\n3                    // Number of populations where the nesting clade (Clade 1-1) occurs");
            helpfile.println("\n1 2 3                // Identifier for of each of these populations");
            helpfile.println("\n5 2 4                // Here starts the observation matrix. Columns are populations and rows");
            helpfile.println("\n0 2 0                //  are clades. The first row corresponds to the first subclade (I),");
            helpfile.println("\n0 1 0                //  and so on. So, in clade I there are 5 individuals at");
            helpfile.println("\n0 1 3                //  population number 1 (Green Mountain), 2 at population 2, and 4 at population 3. ");
            helpfile.println("\nClade 1-2            // Next clade at step-1 level");
            helpfile.println("\n...                  // And so on...");
            helpfile.println("\nEND                  // It is safe to include an END the end of the file");
            helpfile.println("\n\nReading the references included in the doc file will help to understand ");
            helpfile.println("\nthe input format, and more importantly, the conclusions of this analysis");
            helpfile.println("\n\n\nDavid Posada");
            helpfile.println("\nJune 2005");


		} // method printHelp

} // class Help_listener



	class About_listener implements ActionListener
	{
		JFrame about_frame = new JFrame();

		public About_listener(JPanel theframe)
		{
			about_frame = NCPA.getFrame();
			
		} // constructor help_listener

		
		public void actionPerformed(ActionEvent e)
		{
			JOptionPane.showMessageDialog(about_frame,
			"* GeoDis is based in a method and code in BASIC by Alan Templeton      \n" + 
			"* Java code by David Posada\n* (c)1999-2006" +
			"\n                   Correspondence to dposada@uvigo.es" +
			"\n                   http://darwin.uvigo.es",
			"About GeoDis", JOptionPane.PLAIN_MESSAGE);

		} // method actionPerformed

	} // class About_listener


	class Citation_listener implements ActionListener
	{
		JFrame citation_frame = new JFrame();

		public Citation_listener(JPanel theframe)
		{
			citation_frame = NCPA.getFrame();
			
		} // constructor help_listener

		public void actionPerformed(ActionEvent e)
		{
			JOptionPane.showMessageDialog(citation_frame,
			"Posada D, Crandall KA, Templeton AR. 2000. GeoDis: A program" + 
			"\n  for the cladistic nested analysis of the geographical distribution    " +
			"\n  of genetic haplotypes. Molecular Ecology 9: 487-488",
			"GeoDis Citation", JOptionPane.PLAIN_MESSAGE);
		} // method actionPerformed

	} // class Ciation_listener



	class WWW_listener implements ActionListener
	{
		JFrame www_frame = new JFrame();

		public WWW_listener(JPanel theframe)
		{
			www_frame = NCPA.getFrame();
			
		} 

		public void actionPerformed(ActionEvent e)
		{
		try {
			BrowserLauncher.openURL(GeoDis.url);
			}
		catch (IOException exc)
    		{
    		exc.printStackTrace();
			}
		} 

	} 


	class Quit_listener implements ActionListener
	{
		JFrame frame;
		public Quit_listener(JPanel theframe)
		{
			frame = NCPA.getFrame();
		} // constructor quit_listener

		
		public void actionPerformed(ActionEvent e)
		{
			frame.dispose();
			System.exit(0);
		} // method actionPerformed

	} // class Quit_listener
	
	class InferenceListener implements ActionListener{
		
		public void actionPerformed(ActionEvent e){
			Mediator.runInferenceKey();
		}
	}


	public static void outWindow()
	{
	    Document doc = textArea.getDocument();
	    outdoc = new PrintStream(new DocumentOutputStream(doc));      

	    WindowListener l = new WindowAdapter() {
	        public void windowClosing(WindowEvent e) {
	            //System.exit(0);
	            f.dispose();
	        }
	    };
		
	    f = new JFrame("GeoDis Output");
	    f.addWindowListener(l); 
	    f.getContentPane().add(new JScrollPane(textArea), BorderLayout.CENTER);
	    f.setSize(500,500);
   		menuBar2.add(menu1);
   		menuBar2.add(menu2);
		menu1.add(save_Item);
		menu1.add(print_Item);
		//menu1.add(page_Item);
		menu2.add(copy_Item);	
		menu2.add(cut_Item);	
		menu2.add(selectall_Item);	
	    textArea.setFont (new Font ("Monaco", Font.PLAIN, 9));
	    textArea.setMargin(new Insets(5,10,10,5));
		f.getContentPane().add(menuBar2, BorderLayout.NORTH);
	    f.setVisible(true);
    
	} // method outWindow

} // class GeoDis_GUI
