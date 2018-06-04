package ANeCA;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.FileNotFoundException;
import java.util.Date;

/* Things to fix!
* 	Canvas drawing and point selection
* 	I think canvas is replaced with something else when graph is displayed
*	Check dialog boxes exit when OK is pressed
*	Remove all deprecated code
* 	Make sure all functionality is still present
*
*  Also to do
*	Investigate nesting algorithm used by them
*	Create makefiles or batch files for this project
* 	Reorder the file system. (If I can really be bothered and have time!)
* 	
*/

public class NCPA{
/**
 * This class is the front end that combines the multiple packages together
 * which perform NCPA.
 */
 	private static JFrame mainWindow;
 	final static String TCS_MENU_LABEL = "Run TCS - Create haplotype network";
 	final static String NEST_MENU_LABEL = "Run Nesting Algorithm";
 	final static String GEODIS_MENU_LABEL = "Run GeoDis - Nested Clade Analysis";
 	final static String INFERENCE_MENU_LABEL = "Run GeoDis Inference Key";
 	
 	private NCPA(){
	 	Mediator.setGui(this);
 	}
 	
 	public void setWindow(Component c){
	 	mainWindow.getContentPane().removeAll();
	 	mainWindow.getContentPane().add(c,BorderLayout.CENTER);
	 	mainWindow.validate();
	 	mainWindow.repaint();
 	}
 	
 	public static JFrame getFrame(){
	 	return mainWindow;
 	}
 	
 	public static void restoreMenu(){
	 	JMenuBar mb = mainWindow.getJMenuBar();
	 	mb.removeAll();
	 	buildMainMenuBar(mb);
	 	mainWindow.validate();
 	}
 
	public static void main(String[] argv){
		if(argv.length > 0){
			try{
				long t1 = new Date().getTime();
				Mediator.runCommandLineNCPA(argv);
				long t2 = new Date().getTime();
				System.out.println("Analysis took "+((double)(t2-t1)/1000)+" seconds.");
			} catch (FileNotFoundException e) {
				System.out.println("Unable to run command line NCPA");
			}
		} else {
			runNCPA();
			new NCPA();
		}
	}
	
	private static void runNCPA(){
		constructGUI();
	} 
	
	private static void constructGUI(){
		// Construct main window
		mainWindow = new JFrame("Nested Clade Phylogeographic Analysis");
		mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainWindow.setSize(800,600);
		mainWindow.setJMenuBar(buildMainMenuBar(new JMenuBar()));
		// Display GUI
		mainWindow.setVisible(true);
	}
	
	private static JMenuBar buildMainMenuBar(JMenuBar jmb){
		JMenu fileMenu = new JMenu("Main");
		jmb.add(fileMenu);
		NCPAActionListener al = new NCPAActionListener();
		JMenuItem tcsMenuItem = new JMenuItem(TCS_MENU_LABEL);
		tcsMenuItem.addActionListener(al);
		fileMenu.add(tcsMenuItem);
		JMenuItem nestMenuItem = new JMenuItem(NEST_MENU_LABEL);
		nestMenuItem.addActionListener(al);
		fileMenu.add(nestMenuItem);
		JMenuItem geodisMenuItem = new JMenuItem(GEODIS_MENU_LABEL);
		geodisMenuItem.addActionListener(al);
		fileMenu.add(geodisMenuItem);
		JMenuItem infMenuItem = new JMenuItem(INFERENCE_MENU_LABEL);
		infMenuItem.addActionListener(al);
		fileMenu.add(infMenuItem);
		fileMenu.addSeparator();
		JMenuItem exitMenuItem = new JMenuItem("Exit NCPA");
		exitMenuItem.addActionListener(al);
		fileMenu.add(exitMenuItem);
		return jmb;
	}
	
}