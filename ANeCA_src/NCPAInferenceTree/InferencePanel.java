package NCPAInferenceTree;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import ANeCA.NCPA;
import ANeCA.Mediator;
import ANeCA.ProgressionException;

public class InferencePanel extends JPanel implements ActionListener{ // I don't want to create separate or nested classes 
	
	private JTextField gdInputTField;
	private JButton gdInputButton;
	private JTextField gdOutputTField;
	private JButton gdOutputButton;
	private JTextField gdHeaderTField;
	private JButton gdHeaderButton;
	private JTextField gmlHeaderTField;
	private JButton gmlHeaderButton;
	private JButton getSummaryButton; // runs summariser
	private JButton gdSummaryButton;
	private JTextArea gdSummaryTArea;
	private JButton getInferenceButton; // runs inference key
	private JButton gdInferenceButton;
	private JTextArea gdInferenceTArea;
	private JTextField significanceTField;
	private JFileChooser jfc = new JFileChooser();
	private boolean matrixFormat = false;
	
	public InferencePanel(){
		construct();
		setInfo();
	}
	
	private void construct(){
		setLayout(new BorderLayout());
		JPanel topPanel = new JPanel();
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		topPanel.setLayout(gridbag);
		JLabel gdInputLabel = new JLabel("Enter location of the Geodis Input File");
		gbc.insets = new Insets(2,5,2,5);
		gbc.gridwidth = 5;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1;
		gridbag.setConstraints(gdInputLabel,gbc);
		topPanel.add(gdInputLabel);
		JLabel spacer = new JLabel("     ");
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(spacer,gbc);
		topPanel.add(spacer);
		gdInputTField = new JTextField();
		gbc.gridwidth = 5;
		gridbag.setConstraints(gdInputTField,gbc);
		topPanel.add(gdInputTField);
		gdInputButton = new JButton("Browse");
		gdInputButton.addActionListener(this);
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(gdInputButton,gbc);
		topPanel.add(gdInputButton);
		JLabel gdOutputLabel = new JLabel("Enter location of the Geodis Output File");
		gbc.gridwidth = 5;
		gridbag.setConstraints(gdOutputLabel,gbc);
		topPanel.add(gdOutputLabel);
		spacer = new JLabel("     ");
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(spacer,gbc);
		topPanel.add(spacer);
		gdOutputTField = new JTextField();
		gbc.gridwidth = 5;
		gridbag.setConstraints(gdOutputTField,gbc);
		topPanel.add(gdOutputTField);
		gdOutputButton = new JButton("Browse");
		gdOutputButton.addActionListener(this);
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(gdOutputButton,gbc);
		topPanel.add(gdOutputButton);
		JLabel gdHeaderLabel = new JLabel("Enter location of the Geodis geographical File");
		gbc.gridwidth = 5;
		gridbag.setConstraints(gdHeaderLabel,gbc);
		topPanel.add(gdHeaderLabel);
		spacer = new JLabel("     ");
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(spacer,gbc);
		topPanel.add(spacer);
		gdHeaderTField = new JTextField();
		gbc.gridwidth = 5;
		gridbag.setConstraints(gdHeaderTField,gbc);
		topPanel.add(gdHeaderTField);
		gdHeaderButton = new JButton("Browse");
		gdHeaderButton.addActionListener(this);
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(gdHeaderButton,gbc);
		topPanel.add(gdHeaderButton);
		JLabel gmlHeaderLabel = new JLabel("Enter location of the GML Nested Design File");
		gbc.gridwidth = 5;
		gridbag.setConstraints(gmlHeaderLabel,gbc);
		topPanel.add(gmlHeaderLabel);
		spacer = new JLabel("     ");
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(spacer,gbc);
		topPanel.add(spacer);
		gmlHeaderTField = new JTextField();
		gbc.gridwidth = 5;
		gridbag.setConstraints(gmlHeaderTField,gbc);
		topPanel.add(gmlHeaderTField);
		gmlHeaderButton = new JButton("Browse");
		gmlHeaderButton.addActionListener(this);
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(gmlHeaderButton,gbc);
		topPanel.add(gmlHeaderButton);
		add(topPanel,BorderLayout.NORTH);
		JPanel midPanel = new JPanel();
		BoxLayout bl = new BoxLayout(midPanel,BoxLayout.X_AXIS);
		midPanel.setLayout(bl);
		JPanel outputPanel = new JPanel(new BorderLayout());
		JLabel significanceLabel = new JLabel("Significance threshold");
		significanceTField = new JTextField("0.05",5);
		getSummaryButton = new JButton("Summarise");
		getSummaryButton.addActionListener(this);
		JPanel layoutPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		layoutPanel.add(significanceLabel);
		layoutPanel.add(significanceTField);
		layoutPanel.add(getSummaryButton);
		outputPanel.add(layoutPanel,BorderLayout.NORTH);
		gdSummaryTArea = new JTextArea();
		gdSummaryTArea.setEditable(false);
		JScrollPane scrollpane = new JScrollPane(gdSummaryTArea);
		scrollpane.setBorder(
			BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(),
				"Summary of GeoDis NCA statistics",
				TitledBorder.LEADING,
				TitledBorder.ABOVE_TOP
			)
		);
		outputPanel.add(scrollpane,BorderLayout.CENTER);
		layoutPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		gdSummaryButton = new JButton("Save Summary");
		gdSummaryButton.addActionListener(this);
		gdSummaryButton.setEnabled(false);
		layoutPanel.add(gdSummaryButton);
		outputPanel.add(layoutPanel,BorderLayout.SOUTH);
		midPanel.add(outputPanel);
		outputPanel = new JPanel(new BorderLayout());
		getInferenceButton = new JButton("Run Inference Key");
		getInferenceButton.addActionListener(this);
		layoutPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		layoutPanel.add(getInferenceButton);
		outputPanel.add(layoutPanel,BorderLayout.NORTH);
		gdInferenceTArea = new JTextArea();
		gdInferenceTArea.setEditable(false);
		scrollpane = new JScrollPane(gdInferenceTArea);
		scrollpane.setBorder(
			BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(),
				"Results from running the GeoDis Inference Key",
				TitledBorder.LEADING,
				TitledBorder.ABOVE_TOP
			)
		);
		outputPanel.add(scrollpane,BorderLayout.CENTER);
		layoutPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		gdInferenceButton = new JButton("Save Inference");
		gdInferenceButton.addActionListener(this);
		gdInferenceButton.setEnabled(false);
		layoutPanel.add(gdInferenceButton);
		outputPanel.add(layoutPanel,BorderLayout.SOUTH);
		midPanel.add(outputPanel);
		add(midPanel,BorderLayout.CENTER);
	}
	
	private void setInfo(){
		String file = Mediator.getGeoDisInputFile(); 
		if(file != null && new File(file).isFile()){
			gdInputTField.setText(file);
		}
		file = Mediator.getGeoDisOutputFile();
		if(file != null && new File(file).isFile()){
			gdOutputTField.setText(file);
		}
		file = Mediator.getGeoDisOutputFile_MatrixFormat();
		if(file != null && new File(file).isFile()){
			// Analysing matrix format now - This feature has not been added to the rest of this file (I saw no point in it)
			matrixFormat = true;
			gdOutputTField.setText(file);
		}
		file = Mediator.getGeoDisGeographicalFile();
		if(file != null && new File(file).isFile()){
			gdHeaderTField.setText(file);
		}
		file = Mediator.getGMLNestedCladogramFile();
		if(file != null && new File(file).isFile()){
			gmlHeaderTField.setText(file);
		}
	}
	
	public void actionPerformed(ActionEvent e){
		if(e.getSource() == gdInputButton){
			// browse for GeoDis input file
			String file = gdInputTField.getText();
			if(file != null && new File(file).isFile()){
				jfc.setSelectedFile(new File(file));
			}
			if(jfc.showOpenDialog(NCPA.getFrame()) == JFileChooser.APPROVE_OPTION){
				gdInputTField.setText(jfc.getSelectedFile().getAbsolutePath());
				gdSummaryButton.setEnabled(false);
				gdInferenceButton.setEnabled(false);
			}
		} else if(e.getSource() == gdOutputButton){
			// browse for GeoDis ouput file
			String file = gdOutputTField.getText();
			if(file != null && new File(file).isFile()){
				jfc.setSelectedFile(new File(file));
			}
			if(jfc.showOpenDialog(NCPA.getFrame()) == JFileChooser.APPROVE_OPTION){
				gdOutputTField.setText(jfc.getSelectedFile().getAbsolutePath());
				gdSummaryButton.setEnabled(false);
				gdInferenceButton.setEnabled(false);
			}
		} else if(e.getSource() == gdHeaderButton){
			// browse for GeoDis geographical file
			String file = gdHeaderTField.getText();
			if(file != null && new File(file).isFile()){
				jfc.setSelectedFile(new File(file));
			}
			if(jfc.showOpenDialog(NCPA.getFrame()) == JFileChooser.APPROVE_OPTION){
				gdHeaderTField.setText(jfc.getSelectedFile().getAbsolutePath());
				gdSummaryButton.setEnabled(false);
				gdInferenceButton.setEnabled(false);
			}
		} else if(e.getSource() == gmlHeaderButton){
			// browse for GML nested design file
			String file = gmlHeaderTField.getText();
			if(file != null && new File(file).isFile()){
				jfc.setSelectedFile(new File(file));
			}
			if(jfc.showOpenDialog(NCPA.getFrame()) == JFileChooser.APPROVE_OPTION){
				gmlHeaderTField.setText(jfc.getSelectedFile().getAbsolutePath());
				gdSummaryButton.setEnabled(false);
				gdInferenceButton.setEnabled(false);
			}
		} else if(e.getSource() == getSummaryButton){
			// create summary of GeoDis statistics
			try {
				if(new File(gdInputTField.getText()).isFile()
					&& new File(gdOutputTField.getText()).isFile()
					&& new File(gdHeaderTField.getText()).isFile()
					&& new File(gmlHeaderTField.getText()).isFile()){
					try{
						double sig = Double.parseDouble(significanceTField.getText().trim());
						if(0.0 < sig && sig < 1.0){
							CladeData.setSignificanceThreshold(sig);
						} else {
							throw new NumberFormatException("Unrecognised significance rate");
						}
					} catch (NumberFormatException ex) {
						JOptionPane.showMessageDialog(Mediator.getGui().getFrame(),
							"Unable to determine significance threshold.\n"+
							"Using default of 0.05",
							"Warning: Significance level unrecognised.",
							JOptionPane.WARNING_MESSAGE);
						CladeData.setSignificanceThreshold(0.05);
					}
					gdSummaryTArea.setText(Mediator.getGeoDisSummary(gdInputTField.getText(),gdOutputTField.getText(),gdHeaderTField.getText(),gmlHeaderTField.getText()));
					gdSummaryButton.setEnabled(true);
					Mediator.setGeoDisInputFile(gdInputTField.getText());
					Mediator.setGeoDisOutputFile(gdOutputTField.getText());
					Mediator.setGeoDisGeographicalFile(gdHeaderTField.getText());
					Mediator.setGMLNestedCladogramFile(gmlHeaderTField.getText());
				} else {
					gdSummaryTArea.setText("Please check the locations of your files are correct.");
					gdSummaryButton.setEnabled(false);
				}
			} catch (ProgressionException pe){
				Mediator.runInferenceKey();
			}
		} else if(e.getSource() == gdSummaryButton){
			// save summary of GeoDis statistics
			String file = Mediator.getGeoDisInputFile();
			if(file != null){
				jfc.setSelectedFile(new File(file.substring(0,file.lastIndexOf("."))+".gdsum"));
			}
			if(jfc.showSaveDialog(NCPA.getFrame()) == JFileChooser.APPROVE_OPTION){
				if(jfc.getSelectedFile().isFile()){
					if(JOptionPane.showConfirmDialog(NCPA.getFrame(),
						"Are you sure you want to overwrite the existing file?",
						"Warning Message",
						JOptionPane.WARNING_MESSAGE,
						JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION){
							return;
					}
				}
				try{
					FileWriter fw = new FileWriter(jfc.getSelectedFile());
					fw.write(gdSummaryTArea.getText());
					fw.flush();
					fw.close();
					Mediator.setGeoDisSummaryFile(jfc.getSelectedFile().getPath());
				} catch (IOException ex){
					JOptionPane.showMessageDialog(NCPA.getFrame(),
						"An error occurred trying to write the summary to file!",
						"Error Message",
						JOptionPane.ERROR_MESSAGE);
				}
			}
		} else if(e.getSource() == getInferenceButton){
			// run the inference key
			try{
				if(new File(gdInputTField.getText()).isFile()
					&& new File(gdOutputTField.getText()).isFile()
					&& new File(gdHeaderTField.getText()).isFile()
					&& new File(gmlHeaderTField.getText()).isFile()){
					try{
						double sig = Double.parseDouble(significanceTField.getText().trim());
						if(0.0 < sig && sig < 1.0){
							CladeData.setSignificanceThreshold(sig);
						} else {
							throw new NumberFormatException("Unrecognised significance rate");
						}
					} catch (NumberFormatException ex) {
						JOptionPane.showMessageDialog(Mediator.getGui().getFrame(),
							"Unable to determine significance threshold.\n"+
							"Using default of 0.05",
							"Warning: Significance level unrecognised.",
							JOptionPane.WARNING_MESSAGE);
						CladeData.setSignificanceThreshold(0.05);
					}
					gdInferenceTArea.setText(Mediator.getGeoDisInference(gdInputTField.getText(),gdOutputTField.getText(),gdHeaderTField.getText(),gmlHeaderTField.getText()));
					gdInferenceButton.setEnabled(true);
					Mediator.setGeoDisInputFile(gdInputTField.getText());
					Mediator.setGeoDisOutputFile(gdOutputTField.getText());
					Mediator.setGeoDisGeographicalFile(gdHeaderTField.getText());
					Mediator.setGMLNestedCladogramFile(gmlHeaderTField.getText());
				} else {
					gdInferenceTArea.setText("Please check the locations of your files are correct.");
					gdInferenceButton.setEnabled(false);
				}
			} catch (ProgressionException pe){
				Mediator.runInferenceKey();
			}
		} else if(e.getSource() == gdInferenceButton){
			// save results of the inference key
			String file = Mediator.getGeoDisInputFile();
			if(file != null){
				jfc.setSelectedFile(new File(file.substring(0,file.lastIndexOf("."))+".infer"));
			}
			if(jfc.showSaveDialog(NCPA.getFrame()) == JFileChooser.APPROVE_OPTION){
				if(jfc.getSelectedFile().isFile()){
					if(JOptionPane.showConfirmDialog(NCPA.getFrame(),
						"Are you sure you want to overwrite the existing file?",
						"Warning Message",
						JOptionPane.WARNING_MESSAGE,
						JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION){
							return;
					}
				}
				try{
					FileWriter fw = new FileWriter(jfc.getSelectedFile());
					fw.write(gdInferenceTArea.getText());
					fw.flush();
					fw.close();
					Mediator.setGeoDisSummaryFile(jfc.getSelectedFile().getPath());
				} catch (IOException ex){
					JOptionPane.showMessageDialog(NCPA.getFrame(),
						"An error occurred trying to write the summary to file!",
						"Error Message",
						JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}
}