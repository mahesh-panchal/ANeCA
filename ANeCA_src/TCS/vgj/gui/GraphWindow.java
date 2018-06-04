package TCS.VGJ.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.AWTEvent;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.*;
import java.io.IOException;

import java.net.URL;

import java.util.Hashtable;

import TCS.VGJ.algorithm.GraphAlgorithm;
import TCS.VGJ.graph.*;
//import TCS.VGJ.graph.Graph;
//import TCS.VGJ.graph.Node;
import TCS.VGJ.util.DDimension;
import TCS.VGJ.util.DPoint;
import TCS.clad.*;
//import TCS.clad.GraphIO;
//import TCS.clad.TCS;
import ANeCA.NCPA;
import ANeCA.Mediator;
import TCS.VGJ.algorithm.tree.TreeAlgorithm;
import TCS.VGJ.algorithm.shawn.Spring;

/**
 *  GraphWindow is a graph editing tool window. </p> Here is the <a href="../gui/GraphWindow.java">source</a> .
 *
 * @author     Larry Barowski
 * @created    May 29, 1996
 */
public class GraphWindow
		 extends JPanel implements ActionListener, KeyListener {
	/**  Description of the Field */
	public TCS tcs;
	/**  Description of the Field */
	public LPanel p;
	/**  Description of the Field */
	public JProgressBar progressBar;
	/**  Description of the Field */
	public JButton run_button = new JButton("RUN");
	
	public JButton changes_button = new JButton("Show changes");
	public JComboBox choicegaps;
	/**  Description of the Field */

	public JComboBox choicesel;

	//public JComboBox choicemapchars; //included but not used.
	public JRadioButton cb;
	//public JRadioButton manual_box; //included but not used.

	public JTextField statustextField;
	// public Button stop_button               = new Button("STOP");
	//	public static JTextField   textField;
	/**  Description of the Field */
	public JTextField manualConnections = new JTextField(5);
	/**  Description of the Field */
	public JRadioButton manualBox;
	/**  Description of the Field */
	public JLabel calcLimitLabel;
	/**  Description of the Field */
	public JComboBox conLimit;
	// public Menu weightsMenu                 = new Menu("Outgroup Weights");
	/**  Description of the Field */
	public boolean cancel = false;
	/**  Description of the Field */
	public String filename;
	/**  Description of the Field */
	public String outfile;
	/**  Description of the Field */
	public String logfile;
	/**  Description of the Field */
	public JLabel infilelabel;
	// spacing between controls

	/**  spacing of controls */
	private boolean gapmode = true;
	private GMLobject GMLfile_ = null;
	private Hashtable algHashTable_;
	private Hashtable algNameHashTable_;
	private AlgPropDialog algPropDialog_ = null;
	private JMenu algorithmMenu_;
	private JCheckBoxMenuItem cbmiDirected_;
	private JPanel controls_;
	private boolean doDistances = false;
	private FileDialog fd2 = null;
	private String filename_ = null;
	private FontPropDialog fontPropDialog_ = null;
	private GraphCanvas graphCanvas_;
	private Graph graph_;
	private GroupControl groupControl_ = null;
	private Hashtable menuHashTable_;
	//private NestDialog nestDialog_          = null;
	private pictdialog pictDialog_ = null;
	private ViewportScroller portScroller_;
	private PSdialog psDialog_ = null;
	private JLabel scaleLabel_;
	private double scale_ = 1.0;
	private JCheckBoxMenuItem showControls_;
	private ScrolledPanel viewingPanel_;
	// public final static int ONE_SECOND      = 1000;
	/**  Description of the Field */
	protected static int spacing_ = 4;
	private static URL context_ = null;
	private JButton run_nesting_button  = new JButton("Run Nesting Algorithm");


	// private Timer timer                     = new Timer(ONE_SECOND, new TimerListener());

	/**
	 *  Constructor for the GraphWindow object
	 *
	 * @param  graphIn  The graph to be presented in the GraphWindow
	 */
	public GraphWindow(Graph graphIn) {
		construct(graphIn);
	}


	/**
	 *  Constructor for the GraphWindow object
	 *
	 * @param  directed  Directed Graph?
	 */
	public GraphWindow(boolean directed) {
		construct(new Graph(directed));
	}


	/**
	 *  Description of the Method
	 *
	 * @param  distance  Description of Parameter
	 */
	public void setMaxDistance(int distance) {
		tcs.setMaxDistance(distance);
	}


	/**
	 *  Constructor for the pictSave object
	 *
	 * @param  filename  Description of Parameter
	 */
	public void pictSave(String filename) {
		// GraphIO.pictSave();
		pictDialog_ = GraphIO.pictSave(false, filename, graphCanvas_, this, pictDialog_);
	}



	/**  Run the TCS algorithm with the specified parameters */
	public void runTCS() {
		tcs.runTCS(filename, outfile, true, doDistances, logfile);
	}


	/**
	 *  Opens and prepares TCS for running a the specified file commandline only
	 *
	 * @param  fileToRun  Description of Parameter
	 * @return            Description of the Returned Value
	 */
	public boolean open(String fileToRun) {
		tcs = new TCS();
		filename = fileToRun;
		outfile = fileToRun.concat(".graph");
		logfile = fileToRun.concat(".log");

		return true;
	}


	/**
	 *  Save's the current graph and the standard network file
	 *
	 * @param  filename  name of graph to save
	 */
	public void saveStandard(String filename) {
		// saveStandard(boolean writeNet, String filename, GraphCanvas graphCanvas_, GraphWindow gw, GMLobject GMLfile_, Graph graph_, TCS tcs) {
		GMLfile_ = GraphIO.saveStandard(true, filename, graphCanvas_, this, GMLfile_, graph_, tcs);
	}


	/**
	 *  Description of the Method
	 *
	 * @param  event  Description of Parameter
	 * @param  what   Description of Parameter
	 * @return        Description of the Returned Value
	 */
	//public boolean action(Event event, Object what) {
	public void actionPerformed(ActionEvent event){

		if (event.getSource() instanceof JComboBox) {

			JComboBox choice = (JComboBox)(event.getSource());
			String label = (String) choice.getSelectedItem();

			if (label.equals("Gaps = 5th state")) {
				gapmode = true;
			} else if (label.equals("Gaps = missing")) {
				gapmode = false;
			}

			int mode = GraphCanvas.SELECT_BOTH;

			if (label.equals("Create Branches")) {
				mode = GraphCanvas.CREATE_EDGES;
			} else if (label.equals("Create Nodes")) {
				mode = GraphCanvas.CREATE_NODES;
			} else if (label.equals("Select Branches")) {
				mode = GraphCanvas.SELECT_EDGES;
			} else if (label.equals("Select Nodes")) {
				mode = GraphCanvas.SELECT_NODES;
			} else if (label.equals("Select Nodes & Branches")) {
				mode = GraphCanvas.SELECT_BOTH;
			}

			graphCanvas_.setMouseMode(mode);
		}

		if (event.getSource() instanceof JRadioButton) { // Listener not registered with this : not required
			JRadioButton selected = (JRadioButton) event.getSource();
			String label = selected.getText();

			// int mode = GraphCanvas.SELECT_BOTH;
			if (label.equals("Fix Connection Limit At ")) {
				// disable the calculate choicebox
				conLimit.setEnabled(false);
				manualConnections.setEnabled(true);
			} else {
				conLimit.setEnabled(true);
				manualConnections.setEnabled(false);
				// disable the fixed connection stuff
			}
		} else if (event.getSource() instanceof JCheckBoxMenuItem) {

			String label = ((JCheckBoxMenuItem)(event.getSource())).getText();
			boolean state = ((JCheckBoxMenuItem)(event.getSource())).getState();

			if (label.equals("Scale Node Size")) {
				graphCanvas_.scaleBounds(state);
			} else if (label.equals("Show Controls  [s]")) {

				//if (state) {
				//	controls_.setVisible(true);

				//} else {
				//	controls_.setVisible(false);
				//}
				controls_.setVisible(state);
				validate();
			} else if (label.equals("Directed")) {
				graphCanvas_.setDirected(state);
			} else if (label.equals("Use Node ID As Default Label")) {
				Node.setDefaultLabel(state);
				graphCanvas_.update(false);
			} else if (label.equals("High Quality Display")) {
				graphCanvas_.setQuality(!state ? 1 : 2);
			}
		} else if (event.getSource() instanceof JMenuItem) {

			GraphAlgorithm alg;
			JMenuItem menuitem = (JMenuItem)event.getSource();
			alg = (GraphAlgorithm)algHashTable_.get(menuitem);

			if (alg != null) {
				applyAlgorithm_(alg);
			} else {

				String label = menuitem.getText();

				if (label.equals("Help")) {

					try {
						BrowserLauncher.openURL("file://" + System.getProperty("user.dir") + TCS.docTCS);
					} catch (IOException exc) {
						exc.printStackTrace();
					}

					/* new OpenURL("file://" + System.getProperty("user.dir") + "/docs/TCS1.17.html");*/
					//System.err.print("trying to open " + "file://" + System.getProperty("user.dir") + "/Distribution/TCS 1.16/docs/TCS1.17.html");
					/*
					JFrame frame  = new JFrame();
					JOptionPane.showMessageDialog(frame, "Read the documentation...", "TCS advice", JOptionPane.INFORMATION_MESSAGE);
					frame.dispose();
					*/
				} else if (label.equals("Citation")) {
					JOptionPane.showMessageDialog(NCPA.getFrame(), "Clement M, Posada D, Crandall KA. 2000" + 
					"\nTCS: a computer program to estimate gene genealogies" + "\nMolecular Ecology 9 (10): 1657-1660",
					 "TCS citation", JOptionPane.INFORMATION_MESSAGE);
				} else if (label.equals("TCS web site")) {
					try {
						BrowserLauncher.openURL(TCS.urlTCS);
					} catch (IOException exc) {
						exc.printStackTrace();
					}
				} else if (label.equals("Credits")) {
					JOptionPane.showMessageDialog(NCPA.getFrame(), "TCS has been developed by:" +
							"\n   Mark Clement  -- Computer Science@BYU" +
							"\n   Jacob Derington -- Computer Science @BYU" +
							"\n   Steven Woolley -- Biology @Washington University" +
							"\n   David Posada -- Genetics@University of Vigo (Spain)" +
							"\n\nTCS uses the freeware VGLJ for graph display and manipulation and GUI architecture" +
							"\n   http://www.eng.auburn.edu/department/cse/research/graph_drawing/graph_drawing.html" +
							"\n   VGJ may be distributed under the terms of the GNU General Public License, Version 2" +
							"\n\nTCS uses the BrowserLauncher version 1.4b1 class by Eric Albert" +
							"\n\nThanks to all the users for reporting problems !\n", "TCS credits", JOptionPane.INFORMATION_MESSAGE);
				} else if (label.equals("Open Graph (GML)")) {

					filename_ = GraphIO.openGMLGraph(this, GMLfile_, graph_, graphCanvas_);
					setTitle_();
					if (filename_ != null) {
						GMLobject GMLo =
								GraphIO.loadFile(filename_, GMLfile_, this, graph_, graphCanvas_);
						if(GMLo != null){
							tcs = new TCS(this);
							tcs.fillTaxaInfo(GMLo);
							GMLfile_ = GMLo;
							choicesel.setEnabled(true);
							if(!doDistances){
								changes_button.setEnabled(true);
							}
							run_button.setEnabled(false);
							choicegaps.setEnabled(false);
							cb.setEnabled(false);
							conLimit.setEnabled(false);
							manualBox.setEnabled(false);
							Mediator.setTCSGraphFile(filename);
							run_nesting_button.setEnabled(true);
						} else {
							return;
						}
					}
					else {System.err.print("filename not valid!");}
				
				} else if (label.equals("Select NEXUS/PHYLIP Sequence file")) {

					tcs = new TCS(this);
					if ((filename = GraphIO.getFileName(label, tcs, this)) != null) {
						if(filename.length() < 40){
							infilelabel.setText("  " + filename + "  ");
						} else {
							infilelabel.setText("  ..." + filename.substring(filename.length()-37) + "  ");
						}
						// not changed code above to StringTokenizer stuff 'cause it's deprecated.
						progressBar.setValue(0);
						calcLimitLabel.setText("Connection Limit      ");
						// progressBar.update(frame.progressBar.getGraphics());
						outfile = filename;
						outfile = outfile.concat(".graph");
						run_button.setEnabled(true);
						run_nesting_button.setEnabled(false);
						Mediator.setTCSSequenceFile(filename);
						doDistances = false;
						// stop_button.setEnabled(true);
					} else {
						Mediator.newTCSAnalysis();
					}
					return;
				} else if (label.equals("Select NEXUS/PHYLIP Distance file")) {

					tcs = new TCS(this);
					filename = GraphIO.getFileName(label, tcs, this);
					if(filename.length() < 40){
						infilelabel.setText("  " + filename + "  ");
					} else {
						infilelabel.setText("  ..." + filename.substring(filename.length()-37) + "  ");
					}
					progressBar.setValue(0);
					calcLimitLabel.setText("Connection Limit      ");
					// progressBar.update(frame.progressBar.getGraphics());
					outfile = filename;
					outfile = outfile.concat(".graph");

					doDistances = true;

					if (filename == null) {

						JOptionPane.showMessageDialog(NCPA.getFrame(), "Cannot open distance file", "TCS warning", JOptionPane.WARNING_MESSAGE);
						Mediator.newTCSAnalysis();
						return;
						// System.exit(0);
					} else {
						run_button.setEnabled(true);
						cb.setEnabled(false);
						conLimit.setEnabled(false);
						manualBox.setEnabled(false);
						choicegaps.setEnabled(false);
						run_nesting_button.setEnabled(false);
						Mediator.setTCSSequenceFile(filename);
					}
					return;
				} else if (label.equals("Save Graph (GML)") ||
						label.equals("Save Graph As (GML)")) {
					/*filename_ = GraphIO.saveGML(label, filename_, GMLfile_, this, graph_);
					setTitle_();
					if (filename_ != null) {

						GMLobject gmlgraph = GMLfile_.getGMLSubObject("graph", GMLobject.GMLlist, false);
						graph_.setGMLvalues(gmlgraph);
						gmlgraph.prune();
						tcs = new TCS(this);
						tcs.fillTaxaInfo(gmlgraph);
						//load the info into the TCS stuff...
						Mediator.setTCSGraphFile(filename); // trying to be smart
					}*/
		                        GMLobject gmlgraph = new GMLobject("graph", GMLobject.GMLlist);
		                        graph_.setGMLvalues(gmlgraph);
		                        gmlgraph.prune();
		                        GMLfile_ = gmlgraph;
		                        filename_ = GraphIO.saveGML(label, filename_, GMLfile_, this, graph_);
		                        setTitle_();
		                        if(filename_ != null)
		                        {
		                            tcs = new TCS(this);
		                            tcs.fillTaxaInfo(gmlgraph);
						Mediator.setTCSGraphFile(filename); // trying to be smart
		                        }
				} else if (label.equals("Exit This Window")) {
					destroy();
				} else if (label.equals("Delete Selected Items")) {
					graphCanvas_.deleteSelected(true);
				} else if (label.equals("Select All")) {
					graphCanvas_.selectAll();
				} else if (label.equals("Select Root")) {
					graphCanvas_.selectRoot();
				} else if (label.equals("Remove All Edge Bends")) {
					graphCanvas_.removeEdgeBends();
				} else if (label.equals("Remove All Groups")) {
					graphCanvas_.removeGroups();
				} else if (label.equals("Group Control")) {

					if (groupControl_ == null) {
						groupControl_ = new GroupControl(NCPA.getFrame(), graphCanvas_);
					} else {
						groupControl_.showMe();
					}
				} else if (label.equals("Set New Node Properties")) {
					graphCanvas_.setNodeProperties(true);
				} else if (label.equals("Set Node Spacing")) {

					if (algPropDialog_ == null) {
						algPropDialog_ = new AlgPropDialog(NCPA.getFrame(), graphCanvas_);
					} else {
						algPropDialog_.showMe();
					}
				} else if (label.equals("Set Node Label Font")) {

					if (fontPropDialog_ == null) {
						fontPropDialog_ = new FontPropDialog(NCPA.getFrame(), graphCanvas_,true);
					} //else {
						fontPropDialog_.showMe();
					//}
				} else if (label.equals("Set Edge Label Font")) {

					if (fontPropDialog_ == null) {
						fontPropDialog_ = new FontPropDialog(NCPA.getFrame(), graphCanvas_,false);
					} //else {
						fontPropDialog_.showMe();
					//}
				} else if (label.equals("Edit Text Representation (GML)")) {
					graphCanvas_.unselectItems();

					GraphEdit ge = new GraphEdit(graph_, graphCanvas_);
					ge.pack();
					ge.setVisible(true);

				} else if (label.equals("Save network as PostScript")) {

					if (psDialog_ == null) {
						psDialog_ = new PSdialog(NCPA.getFrame(), graphCanvas_);
					} else {
						psDialog_.pack();
						psDialog_.setVisible(true);

					}
				} else if (label.equals("Save network as PICT")) {

					pictDialog_ = GraphIO.pictSave(true, null, graphCanvas_, this, pictDialog_);

				}
				//removed Nesting option until we are sure it works  SW 4 July 03
				/*else if (label.equals("Cladograph nesting"))
				{
					//added 9-11 jake
					//					GMLobject GMLo = loadFile(filename_);
					// do I want to take care of this when it is loaded or when nesting is done???
					//					tcs.fillTaxaInfo(GMLo);
					//call a nesting program of the graph
					//					System.out.println("I am going to do a cladogram");
					//					System.out.println("this uses the most recently saved graph.  Would you like to save the current one?");
					if (nestDialog_ == null)
					{
						nestDialog_ = new NestDialog(this, graphCanvas_, tcs);
					}
					else
					{
						nestDialog_.pack();
						nestDialog_.setVisible(true);
					}
				}*/
				/*else if (label.equals("Plot histogram"))
				{
					new Histogram(tcs.outweights, tcs.xlabels);
				}*/
			}
		} else if (event.getSource() instanceof JButton) {
			String what = ((JButton)event.getSource()).getText();
			if (((String)what).equals("Scale / 1.25")) {
				scale_ /= 1.25;
				graphCanvas_.setScale(scale_);
				scaleLabel_.setText("Scale: " + scale_);
			} else if (((String)what).equals("Scale * 1.25")) {
				scale_ *= 1.25;
				graphCanvas_.setScale(scale_);
				scaleLabel_.setText("Scale: " + scale_);
			} else if (((String)what).equals("Scale = 1")) {
				scale_ = 1.0;
				graphCanvas_.setScale(scale_);
				scaleLabel_.setText("Scale: " + scale_);
			} else if (((String)what).equals("Center")) {

				DDimension port_dim = viewingPanel_.getPortSize();
				DDimension cont_dim = viewingPanel_.getContentSize();
				double x = (cont_dim.width - port_dim.width) / 2.0;
				double y = (cont_dim.height - port_dim.height) / 2.0;
				viewingPanel_.scrollTo((int)x, (int)y);
				portScroller_.setOffset(x, y);
			} else if (((String)what).equals("RUN")) {

				//timer.start();
				/*int args = 0;
				if (!gapmode) {
					args = 1;
				}*/
				// System.out.println(filename);
				// System.out.println(outfile);
				boolean graphExists = tcs.runTCS(filename, outfile, gapmode, doDistances, outfile + ".log");

				if (graphExists) {
					filename_ = outfile;
					run_nesting_button.setEnabled(true);
					Mediator.setTCSGraphFile(outfile);
					GMLfile_ = GraphIO.loadFile(outfile, GMLfile_, this, graph_, graphCanvas_);
					setTitle_();
					graphCanvas_.selectRoot();
					applyAlgorithm("Tree Down");
					run_button.setEnabled(false);
					choicegaps.setEnabled(false);
					cb.setEnabled(false);
					conLimit.setEnabled(false);
					manualBox.setEnabled(false);
					choicesel.setEnabled(true);
					if(!doDistances){
						changes_button.setEnabled(true);
					}
					GMLfile_ = GraphIO.saveStandard(false, filename_, graphCanvas_, this, GMLfile_, graph_, tcs);

					fd2 = null;
				} else {
					//System.exit(0);
					JOptionPane.showMessageDialog(NCPA.getFrame(),
						"A haplotype network was not created.",
						"TCS Error",
						JOptionPane.ERROR_MESSAGE);
					Mediator.newTCSAnalysis();
				}
			} else if (((String)what).equals("STOP")) {

				//timer.stop();
				//System.exit(0);
				//hide();
				setVisible(false);
				cancel = true;
			} else if (((String)what).equals("Show changes")) {
				Edge.setDefaultLabel(true);
				graphCanvas_.update(false);
				changes_button.setText("Hide changes");
			} else if (((String)what).equals("Hide changes")) {
				Edge.setDefaultLabel(false);
				graphCanvas_.update(false);
				changes_button.setText("Show changes");
			} else if( what.equals("Run Nesting Algorithm")){
				Mediator.createGeoDisFile();
			}
		}

		return;
	}


	/**
	 *  Adds a feature to the algorithm attribute of the GraphWindow object
	 *
	 * @param  alg   The feature to be added to the algorithm attribute
	 * @param  name  The feature to be added to the algorithm attribute
	 */
	public void addAlgorithm(GraphAlgorithm alg, String name) {

		JMenuItem menuitem;
		menuitem = new JMenuItem(name);
		menuitem.addActionListener(this);
		algorithmMenu_.add(menuitem);

		// Algorithms keyed by menuitems.
		algHashTable_.put(menuitem, alg);
		algNameHashTable_.put(name, alg);
	}


	/**
	 *  Adds a feature to the algorithm attribute of the GraphWindow object
	 *
	 * @param  alg       The feature to be added to the algorithm attribute
	 * @param  menuname  The feature to be added to the algorithm attribute
	 * @param  name      The feature to be added to the algorithm attribute
	 */
	public void addAlgorithm(GraphAlgorithm alg, String menuname, String name) {

		JMenu menu = (JMenu)menuHashTable_.get(menuname);

		if (menu == null) {

			return;
		}

		JMenuItem menuitem = new JMenuItem(name);
		menuitem.addActionListener(this);
		menu.add(menuitem);
		algHashTable_.put(menuitem, alg);
		algNameHashTable_.put(menuname + "." + name, alg);
	}


	/**
	 *  Adds a feature to the algorithmMenu attribute of the GraphWindow object
	 *
	 * @param  name  The feature to be added to the algorithmMenu attribute
	 */
	public void addAlgorithmMenu(String name) {

		JMenu menu;
		menu = new JMenu(name);
		algorithmMenu_.add(menu);

		// Menus keyed by names.
		menuHashTable_.put(name, menu);
	}


	/**
	 *  Description of the Method
	 *
	 * @param  name  Description of Parameter
	 */
	public void applyAlgorithm(String name) {

		GraphAlgorithm alg = (GraphAlgorithm)algNameHashTable_.get(name);

		if (alg != null) {
			applyAlgorithm_(alg);
		}
	}


	/**
	 *  Description of the Method
	 *
	 * @param  alg  Description of Parameter
	 */
	public void applyAlgorithm_(GraphAlgorithm alg) {

		if (graph_.numberOfNodes() < 1) {
			//new MessageDialog(this, "Error", "Graph is empty.", true);
			JOptionPane.showMessageDialog(NCPA.getFrame(),
					"Graph is empty",
					"Error",
					JOptionPane.ERROR_MESSAGE);
		} else {
			graph_.removeGroups();
			// Remove group nodes.
			graph_.pack();
			// Pack the indexes.

			String msg;
			graphCanvas_.setWireframe(true);
			msg = alg.compute(graph_, graphCanvas_);
			graphCanvas_.setWireframe(false);

			if (msg != null) {
				//new MessageDialog(this, "Message", msg, true);
				JOptionPane.showMessageDialog(NCPA.getFrame(),
						msg,"Message",
						JOptionPane.INFORMATION_MESSAGE);
			}

			graphCanvas_.update(true);
		}
	}


	/**
	 *  Description of the Method
	 *
	 * @param  event  Description of Parameter
	 * @return        Description of the Returned Value
	 */
	//public boolean handleEvent(Event event) {
	public void processEvent(AWTEvent event){
		if(event instanceof TCSEvent){
		if (event.getSource() instanceof ScrolledPanel) {

			if (event.getID() == ScrolledPanel.RESIZE) {

				DDimension tmp_dim;
				tmp_dim = viewingPanel_.getPortSize();
				portScroller_.setPortSize(tmp_dim.width, tmp_dim.height);
				tmp_dim = viewingPanel_.getContentSize();
				portScroller_.setContentSize(tmp_dim.width, tmp_dim.height);
				tmp_dim = viewingPanel_.getOffset();
				portScroller_.setOffset(tmp_dim.width, tmp_dim.height);

				return;
			} else if (event.getID() == ScrolledPanel.OFFSET) {

				DDimension tmp_dim = viewingPanel_.getOffset();
				portScroller_.setOffset(tmp_dim.width, tmp_dim.height);

				return;
			}
		} else if (event.getSource() instanceof ViewportScroller) {

			if (event.getID() == ViewportScroller.SCROLL) {
				graphCanvas_.setWireframe(true);
				viewingPanel_.scrollTo((int)portScroller_.getOffsetX(), (int)portScroller_.getOffsetY());
			}

			if (event.getID() == ViewportScroller.DONE) {
				graphCanvas_.setWireframe(false);
				viewingPanel_.scrollTo((int)portScroller_.getOffsetX(), (int)portScroller_.getOffsetY());
			}
		} else if (event.getSource() instanceof AngleControlPanel) {

			if (event.getID() == AngleControlPanel.ANGLE) {

				DPoint angles = (DPoint)((TCSEvent)event).getInformation();
				graphCanvas_.setWireframe(true);
				graphCanvas_.setViewAngles(angles.x, angles.y);
			}

			if (event.getID() == AngleControlPanel.DONE) {

				DPoint angles = (DPoint) ((TCSEvent)event).getInformation();
				graphCanvas_.setWireframe(false);
				graphCanvas_.setViewAngles(angles.x, angles.y);
			}
		} else if (event.getSource() instanceof GraphCanvas) {

			if (event.getID() == GraphCanvas.UPDATE) {
				cbmiDirected_.setState(graph_.isDirected());
			}
		}
		// quit from Window Manager menu
		//	else if (event.id == Event.WINDOW_DESTROY) {
		//		destroy();

		//		return false;
		//	}
		}

		// call inherited handler
		super.processEvent(event);
	}


	/**
	 *  Description of the Method
	 *
	 * @param  e    Description of Parameter
	 * @param  key  Description of Parameter
	 * @return      Description of the Returned Value
	 */
	//public boolean keyDown(Event e, int key) {
	public void keyPressed(KeyEvent e){
		int key = e.getKeyCode();
		if (key == 's') {
			showControls_.setState(!showControls_.getState());
		} else {
			//graphCanvas_.keyDown(e, key);
			graphCanvas_.dispatchEvent(e);
		}
		if (showControls_.getState()) {
			controls_.setVisible(true);
		} else {
			controls_.setVisible(false);
		}
		validate();
	}

	public void keyTyped(KeyEvent e){}
	public void keyReleased(KeyEvent e){}


	/**
	 *  Description of the Method
	 *
	 * @param  node_index  Description of Parameter
	 */
	public void selectNode(int node_index) {
		graphCanvas_.selectNode(node_index);
	}


	/**  Sets the title_ attribute of the GraphWindow object */
	private void setTitle_() {
		if (filename_ != null) {
			//setTitle("TCS " + TCS.VERSION + ":   " + filename_);
		}
	}



	// Function is called on an exit menu choice or delete from the WM menu
	/**  Description of the Method */
	private void destroy() {
		//dispose();
	}


	/**
	 *  Description of the Method
	 *
	 * @param  graph_in  Description of Parameter
	 */
	private void construct(Graph graph_in) {
		addKeyListener(this);
		algHashTable_ = new Hashtable();
		algNameHashTable_ = new Hashtable();
		menuHashTable_ = new Hashtable();

		// set main font
		Font guiFont = new Font("Charcoal", Font.PLAIN, 11);

		boolean directed = graph_in.isDirected();

		// create menus
		JMenuBar menubar = NCPA.getFrame().getJMenuBar();
		JMenu menu = new JMenu("File");
		JMenu TCSmenu = new JMenu("TCS");

		//menu.addSeparator();
		JMenuItem jmi = new JMenuItem("Select NEXUS/PHYLIP Sequence file");
		jmi.addActionListener(this);
		menu.add(jmi);
		menu.addSeparator();

		jmi = new JMenuItem("Select NEXUS/PHYLIP Distance file");
		jmi.addActionListener(this);
		menu.add(jmi);
		menu.addSeparator();
		jmi = new JMenuItem("Open Graph (GML)");
		jmi.addActionListener(this);
		menu.add(jmi);
		jmi = new JMenuItem("Save Graph (GML)");
		jmi.addActionListener(this);
		menu.add(jmi);
		jmi = new JMenuItem("Save Graph As (GML)");
		jmi.addActionListener(this);
		menu.add(jmi);

		menu.addSeparator();
		// menu.add(new MenuItem("PostScript Output"));
		// menu.add(new MenuItem("PICT Format Output"));
		jmi = new JMenuItem("Save network as PostScript");
		jmi.addActionListener(this);
		menu.add(jmi);
		jmi = new JMenuItem("Save network as PICT");
		jmi.addActionListener(this);
		menu.add(jmi);
		menu.addSeparator();

		// removed Nesting option, July 4, 03 [SW][DP]
		// menu.add(new MenuItem("Cladograph nesting"));
		// menu.add(new MenuItem("Base Pair Mappings"));//jake 02-07-01

		//menu.add(new MenuItem("-"));
		//menu.add(new MenuItem("Exit This Window"));
		menubar.add(menu);
		algorithmMenu_ = new JMenu("Tree");
		menubar.add(algorithmMenu_);
		jmi = new JMenuItem("Help");
		jmi.addActionListener(this);
		TCSmenu.add(jmi);
		jmi = new JMenuItem("Citation");
		jmi.addActionListener(this);
		TCSmenu.add(jmi);
		jmi = new JMenuItem("TCS web site");
		jmi.addActionListener(this);
		TCSmenu.add(jmi);
		jmi = new JMenuItem("Credits");
		jmi.addActionListener(this);
		TCSmenu.add(jmi);
		menubar.add(TCSmenu);

		// removed weights option [DP]
		// weightsMenu.add(new MenuItem("Plot histogram"));
		// weightsMenu.setEnabled(true);
		// menubar.add(weightsMenu);

		menu = new JMenu("Edit Graph");
		jmi = new JMenuItem("Select All");
		jmi.addActionListener(this);
		menu.add(jmi);
		jmi = new JMenuItem("Select Root");
		jmi.addActionListener(this);
		menu.add(jmi);
		jmi = new JMenuItem("Delete Selected Items");
		jmi.addActionListener(this);
		menu.add(jmi);
		jmi = new JMenuItem("Remove All Edge Bends");
		jmi.addActionListener(this);
		menu.add(jmi);
		jmi = new JMenuItem("Remove All Groups");
		jmi.addActionListener(this);
		menu.add(jmi);
		jmi = new JMenuItem("Group Control");
		jmi.addActionListener(this);
		menu.add(jmi);
		jmi = new JMenuItem("Edit Text Representation (GML)");
		jmi.addActionListener(this);
		menu.add(jmi);
		menubar.add(menu);
		menu = new JMenu("Properties");
		progressBar = new JProgressBar(0, 100);
		progressBar.setFont(guiFont);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);

		showControls_ = new JCheckBoxMenuItem("Show Controls  [s]");
		showControls_.setState(true);
		showControls_.addActionListener(this);
		menu.add(showControls_);

		JCheckBoxMenuItem cbmi = new JCheckBoxMenuItem("Directed");
		cbmi.addActionListener(this);
		cbmiDirected_ = cbmi;
		if (directed) {
			cbmi.setState(true);
		}
		menu.add(cbmi);
		jmi = new JMenuItem("Set New Node Properties");
		jmi.addActionListener(this);
		menu.add(jmi);
		jmi = new JMenuItem("Set Node Spacing");
		jmi.addActionListener(this);
		menu.add(jmi);
		jmi = new JMenuItem("Set Node Label Font");
		jmi.addActionListener(this);
		menu.add(jmi);
		jmi = new JMenuItem("Set Edge Label Font");
		jmi.addActionListener(this);
		menu.add(jmi);
		cbmi = new JCheckBoxMenuItem("Scale Node Size");
		cbmi.addActionListener(this);
		cbmi.setState(true);
		menu.add(cbmi);
		cbmi = new JCheckBoxMenuItem("Use Node ID As Default Label");
		cbmi.addActionListener(this);
		cbmi.setState(true);
		menu.add(cbmi);
		cbmi = new JCheckBoxMenuItem("High Quality Display");
		cbmi.addActionListener(this);
		cbmi.setState(true);
		menu.add(cbmi);
		menubar.add(menu);
		NCPA.getFrame().setJMenuBar(menubar);
		setLayout(new BorderLayout());

		// panel for controls
		p = new LPanel();
		p.setFont(guiFont);
		p.setForeground(Color.black);
		p.setBorder(BorderFactory.createEtchedBorder());

		// Lay out everything
		p.addLineLabel("TCS CLADOGRAM ESTIMATION", 0);
        JPanel runp = new JPanel();
        runp.setFont(guiFont);
        run_button.setFont(guiFont);
        run_button.setEnabled(false);
        runp.add(run_button, "West");
 		infilelabel = new JLabel("No input file    ");
        runp.add(infilelabel, "Center");
        p.addComponent(runp, 1, -1, 1.0D, 1.0D, 0, 0);
		//p.addComponent(infilelabel, 1, -1, 1, 1, 0, 0);

		//run_button.setFont(guiFont);
		//run_button.setEnabled(false);
		run_button.addActionListener(this);
		//p.addComponent(run_button, 1, -1, 1, 1, 0, 0);
		p.addLineLabel("", 0);
		p.addComponent(progressBar, 1, -1, 1, 1, 0, 0);
		p.addLineLabel("", 0);

		JPanel first = new JPanel();
		first.setFont(guiFont);

		ButtonGroup mode_group = new ButtonGroup();
		cb = new JRadioButton("Calculate", true);
		mode_group.add(cb);
		cb.setFont(guiFont);
		first.add(cb, "West");
		cb.addActionListener(this);

		conLimit = new JComboBox();
		conLimit.setFont(guiFont);
		int values = 90;
		while (values < 100) {
			conLimit.addItem("" + values + "%");
			values++;
		}
		first.add(conLimit, "Center");
		conLimit.setSelectedIndex(5);
		conLimit.addActionListener(this);

		calcLimitLabel = new JLabel("Connection Limit");
		calcLimitLabel.setFont(new Font("Charcoal", Font.PLAIN, 11));
		first.add(calcLimitLabel, "East");
		p.addComponent(first, 1, -1, 1, 1, 0, 0);
		p.addLineLabel("", 0);

		JPanel second = new JPanel();
		second.setFont(guiFont);
		manualBox = new JRadioButton("Fix Connection Limit At ", false);
		manualBox.setFont(guiFont);
		mode_group.add(manualBox);
		second.add(manualBox, "West");
		manualBox.addActionListener(this);
		// manualConnections.disable();
		second.add(manualConnections, "Center");
		second.add(new JLabel("steps"), "East");
		p.addComponent(second, 1, -1, 1, 1, 0, 0);
		p.addLineLabel("", 0);
		manualConnections.setEnabled(false);

		//pop-up list of choices
		//JComboBox choicegaps;
		choicegaps = new JComboBox();
		choicegaps.setFont(guiFont);
		choicegaps.addItem("Gaps = 5th state");
		choicegaps.addItem("Gaps = missing");
		choicegaps.addActionListener(this);
		p.addComponent(choicegaps, 1, -1, 1, 1, 0, 0);
		p.addLineLabel("", 0);

		//pop-up list of choices
		choicesel = new JComboBox();
		choicesel.setFont(guiFont);
		choicesel.addItem("Select Nodes & Branches");
		choicesel.addItem("Select Nodes");
		choicesel.addItem("Select Branches");
		choicesel.addItem("Create Nodes");
		choicesel.addItem("Create Branches");
		choicesel.addActionListener(this);
		p.addComponent(choicesel, 2, -2, 2, 2, 0, 0);
		p.addLineLabel("", 0);

		choicesel.setEnabled(false);

		p.addComponent(changes_button,2,-2,2,2,0,0);
		changes_button.addActionListener(this);
		p.addLineLabel("",0);
		changes_button.setEnabled(false);
		
		portScroller_ = new ViewportScroller(90, 90, 500.0, 500.0, 400.0, 400.0, 0.0, 0.0);
		portScroller_.setBackground(Color.white);
		p.constraints.insets.bottom = 0;
		p.addLabel("Viewing Offset", 0, 0, 1.0, 1.0, 0, 0);
		p.constraints.insets.top = p.constraints.insets.bottom = 0;
		p.addComponent(portScroller_, 0, 0, 1.0, 1.0, 0, 0);
		p.constraints.insets.top = p.constraints.insets.bottom = 0;
		p.addButton("Center", guiFont, 0, 0, 1.0, 1.0, 0, 0, this);

		scaleLabel_ = p.addLineLabel("Scale: 1", 0);

		LPanel sp = new LPanel();
		sp.setFont(guiFont);
		sp.spacing = 0;
		sp.constraints.insets.top = sp.constraints.insets.bottom = 0;
		sp.addButton("Scale / 1.25", guiFont, 1, -1, 1.0, 1.0, 0, 0, this);
		sp.addButton("Scale = 1", guiFont, 1, -1, 1.0, 1.0, 0, 0, this);
		sp.addButton("Scale * 1.25", guiFont, 0, -1, 1.0, 1.0, 0, 0, this);
		sp.finish();
		p.addComponent(sp, 0, 0, 1.0, 1.0, 0, 0);
		p.constraints.insets.top = 0;

// 		Adds a panel for 3D controlling of the graph, not needed [DP]
//		AngleControlPanel angc = new AngleControlPanel(180,																									 76);
//		p.addComponent(angc, 0, 0, 1.0, 1.0, 1, 0);
		p.addLineLabel("", 0);

		statustextField = new JTextField(25);
		statustextField.setText("STATUS: Waiting");
		statustextField.setForeground(Color.red);
		//statustextField.setBackground(Color.lightGray);
		statustextField.setEditable(false);
		p.addComponent(statustextField, 1, -1, 1, 1, 0, 0);

		run_nesting_button.setFont(guiFont);
		run_nesting_button.setEnabled(false);
		run_nesting_button.addActionListener(this);
		p.addComponent(run_nesting_button, 0, 0, 1.0, 1.0, 0, 0);

		// super panel is there just to allow controls to be
		// fixed to top instead of centered
		LPanel controls_superpanel = new LPanel(); // need custom event processing
		controls_superpanel.setLayout(new BorderLayout()); // revert layout
		controls_superpanel.setFont(guiFont);
		controls_superpanel.add("North", p);
		p.finish();
		add("West", controls_superpanel);
		controls_ = controls_superpanel;
		graph_ = graph_in;
		Node.defaults.setBoundingBox(20, 20, 20);

		// the graph viewing canvas
		graphCanvas_ = new GraphCanvas(graph_, NCPA.getFrame());
		graphCanvas_.setBackground(Color.white);

		// the graph viewing panel (canvas and scrollbars)
		viewingPanel_ = new ScrolledPanel(graphCanvas_);
		viewingPanel_.setBackground(Color.white);
		add("Center", viewingPanel_);
		// addition of algorithms put here // Mahesh
		TreeAlgorithm talg = new TreeAlgorithm('d');
	    addAlgorithm(talg, "Tree Down");
		talg = new TreeAlgorithm('u');
		addAlgorithm(talg, "Tree Up");
		talg = new TreeAlgorithm('l');
		addAlgorithm(talg, "Tree Left");
		talg = new TreeAlgorithm('r');
		addAlgorithm(talg, "Tree Right");
		Spring spring = new Spring();
		addAlgorithm(spring, "Spring Tree");
		validate();
	}


	/**
	 *  Sets the context attribute of the GraphWindow class
	 *
	 * @param  context  The new context value
	 */
	public static void setContext(URL context) {
		context_ = context;
	}

	/*class TimerListener [DP]
			 implements ActionListener
	{
		public void actionPerformed(ActionEvent evt)
		{
			if (cancel)
			{
				//				System.out.print("Stop button pressed ");
				timer.stop();
				System.exit(0);
			}
		}
	}*/
}
