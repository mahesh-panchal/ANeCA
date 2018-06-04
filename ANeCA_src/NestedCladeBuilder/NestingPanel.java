package NestedCladeBuilder;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import ANeCA.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class NestingPanel extends JPanel implements ActionListener{
	// Need to mess about with the grid Layout here for a reasonable layout
	
	private JTextField tcsGraphFileTField;
	private JButton tcsGraphFileButton;
	private JTextField geodisHeaderTField;
	private JButton geodisHeaderButton;
	private JTextField geodisInputFTField;
	private JButton geodisInputFButton;
	private JTextField geodisInputFDMTField;
	private JButton geodisInputFDMButton;
	private JTextField geodisGMLFTField;
	private JButton geodisGMLFButton;
	private JLabel messageLabel;
	private JButton createGeodisIButton;
	private JTextArea nestedDesignTArea;
	private JButton geodisButton;
	private JButton saveNDButton;
	private JButton saveNANOVAButton;
	private JFileChooser jfc = new JFileChooser();
	private NestedCladograph ncg;
	private final String dfltTextAreaMsg = "This area displays a summary of the nested design.\nThis summary allows the user to check the nested design."; 
	
	public NestingPanel(){
		constructPanel();
		setInfo();
	}
	
	private void constructPanel(){ // Get this Panels' layout sorted!!!
		setLayout(new BorderLayout());
		JPanel topPanel = new JPanel();
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		topPanel.setLayout(gridbag);
		JLabel label = new JLabel("Enter location of TCS graph file");
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(10,20,5,5);
		gbc.gridwidth = 3;
		gbc.weightx = 1;
		gridbag.setConstraints(label,gbc);
		topPanel.add(label);
		JPanel blank = new JPanel();
		gbc.insets = new Insets(10,20,5,20);
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(blank,gbc);
		topPanel.add(blank);
		tcsGraphFileTField = new JTextField();
		gbc.insets = new Insets(5,20,5,5);
		gbc.gridwidth = 3;
		gridbag.setConstraints(tcsGraphFileTField,gbc);
		topPanel.add(tcsGraphFileTField);
		tcsGraphFileButton = new JButton("Locate TCS File");
		tcsGraphFileButton.addActionListener(this);
		gbc.insets = new Insets(5,20,5,20);
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(tcsGraphFileButton,gbc);
		topPanel.add(tcsGraphFileButton);
		label = new JLabel("Enter location of GeoDis geographical file");
		gbc.insets = new Insets(10,20,5,5);
		gbc.gridwidth = 3;
		gridbag.setConstraints(label,gbc);
		topPanel.add(label);
		blank = new JPanel();
		gbc.insets = new Insets(10,20,5,20);
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(blank,gbc);
		topPanel.add(blank);
		geodisHeaderTField = new JTextField();
		gbc.insets = new Insets(5,20,5,5);
		gbc.gridwidth = 3;
		gridbag.setConstraints(geodisHeaderTField,gbc);
		topPanel.add(geodisHeaderTField);
		geodisHeaderButton = new JButton("Locate GeoDis Header");
		geodisHeaderButton.addActionListener(this);
		gbc.insets = new Insets(5,20,5,20);
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(geodisHeaderButton,gbc);
		topPanel.add(geodisHeaderButton);
		label = new JLabel("Enter location where GeoDis input file (Latitude, Longitude format) will be saved");
		gbc.insets = new Insets(10,20,5,5);
		gbc.gridwidth = 3;
		gridbag.setConstraints(label,gbc);
		topPanel.add(label);
		blank = new JPanel();
		gbc.insets = new Insets(10,20,5,20);
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(blank,gbc);
		topPanel.add(blank);
		geodisInputFTField = new JTextField();
		gbc.insets = new Insets(5,20,5,5);
		gbc.gridwidth = 3;
		gridbag.setConstraints(geodisInputFTField,gbc);
		topPanel.add(geodisInputFTField);
		geodisInputFButton = new JButton("Browse");
		geodisInputFButton.addActionListener(this);
		gbc.insets = new Insets(5,20,5,20);
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(geodisInputFButton,gbc);
		topPanel.add(geodisInputFButton);
		label = new JLabel("Enter location where GeoDis input file (Distance Matrix format) will be saved - Optional");
		gbc.insets = new Insets(10,20,5,5);
		gbc.gridwidth = 3;
		gridbag.setConstraints(label,gbc);
		topPanel.add(label);
		blank = new JPanel();
		gbc.insets = new Insets(10,20,5,20);
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(blank,gbc);
		topPanel.add(blank);
		geodisInputFDMTField = new JTextField();
		gbc.insets = new Insets(5,20,5,5);
		gbc.gridwidth = 3;
		gridbag.setConstraints(geodisInputFDMTField,gbc);
		topPanel.add(geodisInputFDMTField);
		geodisInputFDMButton = new JButton("Browse");
		geodisInputFDMButton.addActionListener(this);
		gbc.insets = new Insets(5,20,5,20);
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(geodisInputFDMButton,gbc);
		topPanel.add(geodisInputFDMButton);
		label = new JLabel("Enter location where the GML Nesting Design file will be saved");
		gbc.insets = new Insets(10,20,5,5);
		gbc.gridwidth = 3;
		gridbag.setConstraints(label,gbc);
		topPanel.add(label);
		blank = new JPanel();
		gbc.insets = new Insets(10,20,5,20);
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(blank,gbc);
		topPanel.add(blank);
		geodisGMLFTField = new JTextField();
		gbc.insets = new Insets(5,20,5,5);
		gbc.gridwidth = 3;
		gridbag.setConstraints(geodisGMLFTField,gbc);
		topPanel.add(geodisGMLFTField);
		geodisGMLFButton = new JButton("Browse");
		geodisGMLFButton.addActionListener(this);
		gbc.insets = new Insets(5,20,5,20);
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(geodisGMLFButton,gbc);
		topPanel.add(geodisGMLFButton);
		messageLabel = new JLabel(" ");
		messageLabel.setForeground(Color.RED);
		messageLabel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK),"Error Message"));
		gbc.insets = new Insets(10,20,5,5);
		gbc.gridwidth = 3;
		gridbag.setConstraints(messageLabel,gbc);
		topPanel.add(messageLabel);
		createGeodisIButton = new JButton("Create Geodis Input");
		createGeodisIButton.addActionListener(this);
		gbc.insets = new Insets(10,20,5,20);
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(createGeodisIButton,gbc);
		topPanel.add(createGeodisIButton);
		add(topPanel,BorderLayout.NORTH);
		nestedDesignTArea = new JTextArea(dfltTextAreaMsg);
		nestedDesignTArea.setEditable(false);
		nestedDesignTArea.setSize(700,500);
		JScrollPane scrollpane = new JScrollPane(nestedDesignTArea);
		gbc.gridheight = 5;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(10,20,5,20);
		gridbag.setConstraints(scrollpane,gbc);
		add(scrollpane,BorderLayout.CENTER);
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(gridbag);
		saveNDButton = new JButton("Save Nested Design");
		saveNDButton.addActionListener(this);
		saveNDButton.setEnabled(false);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(10,20,5,20);
		gbc.gridwidth = 1;
		gridbag.setConstraints(saveNDButton,gbc);
		bottomPanel.add(saveNDButton);
		saveNANOVAButton = new JButton("Save NANOVA Data");
		saveNANOVAButton.addActionListener(this);
		saveNANOVAButton.setEnabled(false);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(10,20,5,20);
		gbc.gridwidth = 1;
		gridbag.setConstraints(saveNANOVAButton,gbc);
		bottomPanel.add(saveNANOVAButton);
		//blank = new JPanel();
		//gbc.insets = new Insets(10,5,5,5);
		//gbc.gridheight = 1;
		//gbc.gridwidth = GridBagConstraints.RELATIVE;
		//gridbag.setConstraints(blank,gbc);
		//bottomPanel.add(blank);
		geodisButton = new JButton("Run GeoDis");
		geodisButton.setEnabled(false);
		geodisButton.addActionListener(this);
		gbc.insets = new Insets(10,20,5,20);
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(geodisButton,gbc);
		bottomPanel.add(geodisButton);
		add(bottomPanel,BorderLayout.SOUTH);
	}
	
	private void setInfo(){
		String graph = Mediator.getTCSGraphFile();
		if(graph != null){
			jfc.setCurrentDirectory(new File(graph).getParentFile());
			if(new File(graph).isFile()){
				tcsGraphFileTField.setText(graph);
			}
			int dot = graph.lastIndexOf(".");
			String filename = graph.substring(0,dot);
			dot = filename.lastIndexOf(".");
			if(dot != -1 ){
				filename = filename.substring(0,dot);
			}
			String distFile = filename + ".dist";
			if(new File(distFile).isFile()){
				geodisHeaderTField.setText(distFile);
			}
			geodisInputFTField.setText(filename+".gdin");
			geodisInputFDMTField.setText(filename+".gdmin");
			geodisGMLFTField.setText(filename+".gml");
		}
	}
	
	public void actionPerformed(ActionEvent e){
		JButton button = (JButton) e.getSource();
		if(button == tcsGraphFileButton){
			if(jfc.showOpenDialog(NCPA.getFrame()) == JFileChooser.APPROVE_OPTION){
				tcsGraphFileTField.setText(jfc.getSelectedFile().getAbsolutePath());
				saveNDButton.setEnabled(false);
				saveNANOVAButton.setEnabled(false);
				geodisButton.setEnabled(false);
				nestedDesignTArea.setText(dfltTextAreaMsg);
				messageLabel.setText(" ");
			}
		} else if (button == geodisHeaderButton){
			if(jfc.showOpenDialog(NCPA.getFrame()) == JFileChooser.APPROVE_OPTION){
				geodisHeaderTField.setText(jfc.getSelectedFile().getAbsolutePath());
				saveNDButton.setEnabled(false);
				saveNANOVAButton.setEnabled(false);
				geodisButton.setEnabled(false);
				nestedDesignTArea.setText(dfltTextAreaMsg);
				messageLabel.setText(" ");
			}
		} else if (button == geodisInputFButton){
			if(jfc.showSaveDialog(NCPA.getFrame()) == JFileChooser.APPROVE_OPTION){
				if(jfc.getSelectedFile().isFile()){
					if(JOptionPane.showConfirmDialog(NCPA.getFrame(),
						"Are you sure you want to overwrite this file?",
						"Overwrite file?",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION){
						geodisInputFTField.setText(jfc.getSelectedFile().getAbsolutePath());
					saveNDButton.setEnabled(false);
					saveNANOVAButton.setEnabled(false);
					geodisButton.setEnabled(false);
					nestedDesignTArea.setText(dfltTextAreaMsg);
					messageLabel.setText(" ");
					}
				} else {
					geodisInputFTField.setText(jfc.getSelectedFile().getAbsolutePath());
					saveNDButton.setEnabled(false);
					saveNANOVAButton.setEnabled(false);
					geodisButton.setEnabled(false);
					nestedDesignTArea.setText(dfltTextAreaMsg);
					messageLabel.setText(" ");
				}
			}
		} else if (button == geodisInputFDMButton){
			if(jfc.showSaveDialog(NCPA.getFrame()) == JFileChooser.APPROVE_OPTION){
				if(jfc.getSelectedFile().isFile()){
					if(JOptionPane.showConfirmDialog(NCPA.getFrame(),
						"Are you sure you want to overwrite this file?",
						"Overwrite file?",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION){
						geodisInputFDMTField.setText(jfc.getSelectedFile().getAbsolutePath());
					saveNDButton.setEnabled(false);
					saveNANOVAButton.setEnabled(false);
					geodisButton.setEnabled(false);
					nestedDesignTArea.setText(dfltTextAreaMsg);
					messageLabel.setText(" ");
					}
				} else {
					geodisInputFDMTField.setText(jfc.getSelectedFile().getAbsolutePath());
					saveNDButton.setEnabled(false);
					saveNANOVAButton.setEnabled(false);
					geodisButton.setEnabled(false);
					nestedDesignTArea.setText(dfltTextAreaMsg);
					messageLabel.setText(" ");
				}
			}
		} else if (button == geodisGMLFButton){
			if(jfc.showSaveDialog(NCPA.getFrame()) == JFileChooser.APPROVE_OPTION){
				if(jfc.getSelectedFile().isFile()){
					if(JOptionPane.showConfirmDialog(NCPA.getFrame(),
						"Are you sure you want to overwrite this file?",
						"Overwrite file?",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION){
						geodisGMLFTField.setText(jfc.getSelectedFile().getAbsolutePath());
						saveNDButton.setEnabled(false);
						saveNANOVAButton.setEnabled(false);
						geodisButton.setEnabled(false);
						nestedDesignTArea.setText(dfltTextAreaMsg);
						messageLabel.setText(" ");
					}
				} else {
					geodisGMLFTField.setText(jfc.getSelectedFile().getAbsolutePath());
					saveNDButton.setEnabled(false);
					saveNANOVAButton.setEnabled(false);
					geodisButton.setEnabled(false);
					nestedDesignTArea.setText(dfltTextAreaMsg);
					messageLabel.setText(" ");
				}
			}
		} else if (button.getText().equals("Create Geodis Input")){
			if(new File(tcsGraphFileTField.getText()).isFile()
				&& new File(geodisHeaderTField.getText()).isFile()
				&& new File(geodisInputFTField.getText()).isAbsolute()
				&& new File(geodisGMLFTField.getText()).isAbsolute()){
				if(new File(geodisInputFTField.getText()).isFile()){
					if(JOptionPane.showConfirmDialog(NCPA.getFrame(),
							"Are you sure you want to overwrite the existing GeoDis input file?",
							"Nesting warning",
							JOptionPane.YES_NO_OPTION,
							JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION ){
						messageLabel.setText("Please select a new name for the GeoDis input file.");
						return; // no further action should be taken
					} else if(new File(geodisGMLFTField.getText()).isFile()){
						if(JOptionPane.showConfirmDialog(NCPA.getFrame(),
								"Are you sure you want to overwrite the existing GML Nested Design file?",
								"Nesting warning",
								JOptionPane.YES_NO_OPTION,
								JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION ){
							messageLabel.setText("Please select a new name for the GML Nested Design file.");
							return; // no further action should be taken
						}
					}
					saveNDButton.setEnabled(false);
					saveNANOVAButton.setEnabled(false);
					geodisButton.setEnabled(false);
					ncg = Mediator.writeGeodisInput(tcsGraphFileTField.getText(),
							geodisHeaderTField.getText(),
							geodisInputFTField.getText(),
							geodisInputFDMTField.getText().trim(),
							geodisGMLFTField.getText()
							);
				} else {
					if(new File(geodisGMLFTField.getText()).isFile()){
						if(JOptionPane.showConfirmDialog(NCPA.getFrame(),
								"Are you sure you want to overwrite the existing GML Nested Design file?",
								"Nesting warning",
								JOptionPane.YES_NO_OPTION,
								JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION ){
							messageLabel.setText("Please select a new name for the GML Nested Design file.");
							return; // no further action should be taken
						}
					}
					saveNDButton.setEnabled(false);
					saveNANOVAButton.setEnabled(false);
					geodisButton.setEnabled(false);
					ncg = Mediator.writeGeodisInput(tcsGraphFileTField.getText(),
							geodisHeaderTField.getText(),
							geodisInputFTField.getText(),
							geodisInputFDMTField.getText().trim(),
							geodisGMLFTField.getText()
							);
				}
				if(ncg != null){
					Mediator.setGeoDisGeographicalFile(geodisHeaderTField.getText());
					Mediator.setGeoDisInputFile(geodisInputFTField.getText());
					Mediator.setGeoDisInputFile_MatrixFormat(geodisInputFDMTField.getText());
					Mediator.setGMLNestedCladogramFile(geodisGMLFTField.getText());
					nestedDesignTArea.setText(ncg.toString());
					messageLabel.setText(" ");
					saveNDButton.setEnabled(true);
					saveNANOVAButton.setEnabled(true);
					geodisButton.setEnabled(true);
				} else {
					messageLabel.setText("An Error has occured. Please check your files and try again.");
				}
			} else {
				messageLabel.setText("Please ensure the paths specified are Absolute Path Names");
				/*JOptionPane.showMessageDialog(NCPA.getFrame(),
					"Please ensure the paths specified are Absolute Path Names",
					"File Error",
					JOptionPane.INFORMATION_MESSAGE);*/
			}
		} else if (button.getText().equals("Save Nested Design")){
			jfc.setSelectedFile(new File(geodisInputFTField.getText().substring(0,geodisInputFTField.getText().lastIndexOf("."))+".nest"));
			if(jfc.showSaveDialog(NCPA.getFrame()) == JFileChooser.APPROVE_OPTION){
				if(jfc.getSelectedFile().isFile()){
					if(JOptionPane.showConfirmDialog(NCPA.getFrame(),
						"Are you sure you want to overwrite this file?",
						"Overwrite file?",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION){
						try{
							FileWriter fw = new FileWriter(jfc.getSelectedFile());
							fw.write(nestedDesignTArea.getText());
							fw.flush();
							fw.close();
							Mediator.setNestingFile(jfc.getSelectedFile().getPath());
						} catch (IOException ex) {
							JOptionPane.showMessageDialog(NCPA.getFrame(),
								"There was an error while writing the file:"+ex.getMessage(),
								"File Error",
								JOptionPane.ERROR_MESSAGE);
						}
					}
				} else {
					try{
						FileWriter fw = new FileWriter(jfc.getSelectedFile());
						fw.write(nestedDesignTArea.getText());
						fw.flush();
						fw.close();
						Mediator.setNestingFile(jfc.getSelectedFile().getPath());
					} catch (IOException ex) {
						JOptionPane.showMessageDialog(NCPA.getFrame(),
							"There was an error while writing the file:"+ex.getMessage(),
							"File Error",
							JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		} else if (button.getText().equals("Save NANOVA Data")){
			jfc.setSelectedFile(new File(geodisInputFTField.getText().substring(0,geodisInputFTField.getText().lastIndexOf("."))+".nanova"));
			if(jfc.showSaveDialog(NCPA.getFrame()) == JFileChooser.APPROVE_OPTION){
				if(jfc.getSelectedFile().isFile()){
					if(JOptionPane.showConfirmDialog(NCPA.getFrame(),
						"Are you sure you want to overwrite this file?",
						"Overwrite file?",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION){
						try{
							FileWriter fw = new FileWriter(jfc.getSelectedFile());
							if(ncg != null){
								fw.write(ncg.getNestedANOVAData());
								fw.flush();
								fw.close();
							} else {
								messageLabel.setText("There is no Nested Data to save");
							}
						} catch (IOException ex) {
							JOptionPane.showMessageDialog(NCPA.getFrame(),
								"There was an error while writing the file:"+ex.getMessage(),
								"File Error",
								JOptionPane.ERROR_MESSAGE);
						}
					}
				} else {
					try{
						FileWriter fw = new FileWriter(jfc.getSelectedFile());
						if(ncg != null){
							fw.write(ncg.getNestedANOVAData());
							fw.flush();
							fw.close();
						} else {
							messageLabel.setText("There is no Nested Data to save");
						}
					} catch (IOException ex) {
						JOptionPane.showMessageDialog(NCPA.getFrame(),
							"There was an error while writing the file:"+ex.getMessage(),
							"File Error",
							JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		} else if (button.getText().equals("Run GeoDis")){
			Mediator.startGeodis();
		} else {
			System.out.println("The Buttons are not working captain!");
		}
	}
	
}