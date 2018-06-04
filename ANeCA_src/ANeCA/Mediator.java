package ANeCA;

import TCS.VGJ.gui.GraphWindow;
import NestedCladeBuilder.NestedCladeBuilder;
import NestedCladeBuilder.NestedCladograph;
import NestedCladeBuilder.NestingPanel;
import GeoDis.GeoDis_GUI;
import GeoDis.GeoDis;
//import fileReader.InputFileReader;
import NCPAInferenceTree.CladeDataExtractor;
import NCPAInferenceTree.CladeData;
import NCPAInferenceTree.InferencePanel;
import java.io.IOException;
import java.io.FileWriter;
import java.io.File;
import java.io.FileNotFoundException;
import TCS.clad.TCS;

public class Mediator{
	private static NCPA gui;
	private static String dna_seq_file = null;					// filename.nex
	private static String graph_file = null;					// filename.nex.graph or just filename.graph for commandLine
	private static String geodis_header_file = null;			// filename.clade
	private static String geodis_input_file = null; 			// filename.gdin
	private static String gml_nest_file = null;				    // filename.gml
	private static String geodis_nest_file = null;				// filename.nest
	private static String nanova_file = null;					// filename.nanova
	private static String geodis_output_file = null;			// filename.gdout
	private static String geodis_summary_file = null;			// filename.gdsum
	private static String geodis_infer_file = null;			    // filename.infer
	private static String geodis_input_matrix_file = null; 		// filename.gdmin
	private static String geodis_output_matrix_file = null;		// filename.gdmout
	private static String geodis_summary_matrix_file = null;	// filename.gdmsum
	private static String geodis_infer_matrix_file = null;		// filename.minfer
	private static boolean commandLineMode = false;
	private static FileWriter log;
	private static final double RADIUS = 6364.963;              // average radius of the earth in km - Change this if it changes in GeoDis
	private static boolean runDMFormat = true;					// set to false if output from matrix format not required.
	private static boolean runNANOVAonly = false;				// set to true to only get NANOVA data
	private static double significance_rate = 0.05;				// The default significance rate.
	
	public static void runCommandLineNCPA(String[] argv) throws FileNotFoundException{
		if((new File(argv[0])).isFile() && (new File(argv[1])).isFile()){
			if(argv.length >= 3){
				if(argv[2].trim().equals("-NANOVA")){
					runNANOVAonly = true;
				} else {
					try{
						Integer.parseInt(argv[2]);
					} catch (NumberFormatException e){
						throw new FileNotFoundException("The files have been found but you didn't specify an integer as the number of random permuations");
					}
					try{
						runDMFormat = (Integer.parseInt(argv[3]) > 0);
					} catch (NumberFormatException e){
						throw new FileNotFoundException("Use 0 to disable distance matrix format, a positive integer otherwise");
					}
					try{
						significance_rate = Double.parseDouble(argv[4]);
						if(significance_rate < 0 || significance_rate > 1){
							throw new FileNotFoundException("The significance threshold provided is incorrect");
						}
					} catch (NumberFormatException e) {
						throw new FileNotFoundException("The significance threshold provided is incorrect");
					}
				}
			}
			//Command Line NCPA
			commandLineMode = true;
			// Run command line version with default parameters except uses distance matrix.
			dna_seq_file = new File(argv[0]).getAbsolutePath();
			String filename = dna_seq_file.substring(0,dna_seq_file.lastIndexOf("."));
			graph_file = filename + ".graph";
			String tcs_logfile = filename + ".tcs.log";
			geodis_header_file = new File(argv[1]).getAbsolutePath();
			geodis_input_file = filename + ".gdin";
			geodis_input_matrix_file = filename + ".gdmin";
			//geodis_distr_file = filename + ".distr";
			gml_nest_file = filename + ".gml";
			geodis_nest_file = filename + ".nest";
			nanova_file = filename + ".nanova";
			geodis_output_file = filename + ".gdout";
			geodis_output_matrix_file = filename + ".gdmout";
			geodis_summary_file = filename + ".gdsum";
			geodis_summary_matrix_file = filename + ".gdmsum";
			geodis_infer_file = filename + ".infer";
			geodis_infer_matrix_file = filename + ".minfer";
			File ncpa_err_log = new File(filename+".errlog");
			try{
				log = new FileWriter(ncpa_err_log);
			} catch (IOException e){
				System.out.println("Aborting run: Unable to create warning/error log");
				System.exit(0);
			}
			try{
				// Run TCS 
				System.out.println("Running TCS");
				if(!(new TCS().runTCS(dna_seq_file,graph_file,true,false,tcs_logfile))){ // not sure about gapmode: true for now
					writeError("Unable to run TCS successfully.[NCPA_main]\n");
				}
				// Run Nesting Algorithm
				System.out.println("Running Nesting algorithm");
				//NestedCladograph ncg = writeGeodisInput(graph_file,geodis_header_file,geodis_input_file,geodis_distr_file); // saves the gdin, and distr files
				NestedCladograph ncg = writeGeodisInput(graph_file,geodis_header_file,geodis_input_file,geodis_input_matrix_file,gml_nest_file); // saves the gdin, gdmin and ncg files
				try{
					FileWriter fw = new FileWriter(geodis_nest_file);
					fw.write(ncg.toString());
					fw.flush();
					fw.close();
				} catch (IOException e){
					writeError("Unable to save nested design.[NCPA_main]\n");
				}
				try{
					FileWriter fw = new FileWriter(nanova_file);
					fw.write(ncg.getNestedANOVAData());
					fw.flush();
					fw.close();
				} catch (IOException e){
					writeError("Unable to save nested ANOVA data.[NCPA_main]\n");
				}
				if(!runNANOVAonly){
					//Setup GeoDis
					System.out.println("Running GeoDis - Latitude, Longitude Format");
					GeoDis.infilename = geodis_input_file;
					GeoDis.outfilename = geodis_output_file;
					if(argv.length >= 3){
						GeoDis.numPermutations = Integer.parseInt(argv[2]);
					} else {
						GeoDis.numPermutations = 10000;
					}
					GeoDis.doingDistances = false;
					GeoDis.printToFile = true;
					GeoDis.usingDecimalDegrees = true;
					//Execute GeoDis
					GeoDis.startLogFile();
			    	if (GeoDis.doingDistances){
						GeoDis.readMatrix();
					} else {
						GeoDis.readLocations();
					}
					GeoDis.readClade();
					GeoDis.statistics();
					GeoDis.done = true;
					if(!GeoDis.doingDistances){
						GeoDis.pairwiseKm();
					}	
					GeoDis.printOut();
					if(runDMFormat){
						System.out.println("Running GeoDis - Distance Matrix Format");
						GeoDis.infilename = geodis_input_matrix_file;
						GeoDis.outfilename = geodis_output_matrix_file;
						if(argv.length >= 3){
							GeoDis.numPermutations = Integer.parseInt(argv[2]);
						} else {
							GeoDis.numPermutations = 10000;
						}
						GeoDis.doingDistances = true;
						GeoDis.printToFile = true;
						GeoDis.usingDecimalDegrees = false;
						//Execute GeoDis
			    		if (GeoDis.doingDistances){
							GeoDis.readMatrix();
						} else {
							GeoDis.readLocations();
						}
						GeoDis.readClade();
						GeoDis.statistics();
						GeoDis.done = true;
						GeoDis.printOut();
					}
					System.out.println("Running Summariser / Inference Key - Latitude, Longitude Format");
					CladeData.setSignificanceThreshold(significance_rate);
					CladeDataExtractor.writeGeoDisResults(
						geodis_output_file,
						geodis_input_file,
						geodis_header_file,
						gml_nest_file,
						geodis_summary_file,
						geodis_infer_file);
					if(runDMFormat){
						System.out.println("Running Summariser / Inference Key - Distance Matrix Format");
						CladeDataExtractor.writeGeoDisResults(
							geodis_output_matrix_file,
							geodis_input_file,
							geodis_header_file,
							gml_nest_file,
							geodis_summary_matrix_file,
							geodis_infer_matrix_file);
					}
				}
			} catch (ProgressionException e) {
				writeError(e.getMessage()+"[????]\n");
				writeError("Ending Run.[NCPA_main]\n");
			}
			try {
				log.flush();
				log.close();
				if(ncpa_err_log.length() == 0){
					if(!ncpa_err_log.delete()){
						throw new IOException("Unable to remove the error log.");
					}
				}
			} catch (IOException e){
				System.out.println("Unable to close log (and maybe remove).");
			}
			System.out.println("Command Line Run complete");
		} else {
			throw new FileNotFoundException("The specified files were not recognised (or it couldn't figure out the number of permutations to run or you wanted a NANOVA).");
		}
	}
	
	public static boolean isCommandLineApp(){
		return commandLineMode;
	}
	
	public static void writeMessage(String message){
		try{
			log.write("Message: ");
			log.write(message);
			log.write("\n");
			log.flush();
		} catch (IOException e){
			System.out.println("Unable to write messages. Exiting run.");
			System.exit(0);
		}
	}
	
	public static void writeWarning(String wrnmsg){
		try{
			log.write("Warning: ");
			log.write(wrnmsg);
			log.write("\n");
			log.flush();
		} catch (IOException e){
			System.out.println("Unable to write messages. Exiting run.");
			System.exit(0);
		}
	}
	
	public static void writeError(String message){
		try{ // Errors are written when should not continue
			log.write("Error: ");
			log.write(message);
			log.write("\n");
			log.flush();
			log.close();
			System.out.println("Error written to log. Exiting run.");
		} catch (IOException e){
			System.out.println("Unable to write messages. Exiting run.");
		}
		System.exit(0);
	}
	
	public static void setGui(NCPA ui){
		gui = ui;
	}
	
	public static NCPA getGui(){
		return gui;
	}
	
	public static void setTCSSequenceFile(String tcsFile){
		dna_seq_file = tcsFile;  // reset other values for new analysis
		graph_file = null;
		geodis_header_file = null;
		geodis_input_file = null;
		gml_nest_file = null;
		geodis_nest_file = null;
		geodis_output_file = null; 
		geodis_summary_file = null;	
		geodis_infer_file = null;
	}
	
	public static String getTCSSequenceFile(){
		return dna_seq_file;
	}
	
	public static void setTCSGraphFile(String tcsFile){
		graph_file = tcsFile;
	}
	
	public static String getTCSGraphFile(){
		return graph_file;
	}
	
	public static void setGeoDisGeographicalFile(String geodisFile){
		geodis_header_file = geodisFile;
	}
	
	public static String getGeoDisGeographicalFile(){
		return geodis_header_file;
	}
	
	public static void setGeoDisInputFile(String geodisFile){
		geodis_input_file = geodisFile;
	}
	
	public static String getGeoDisInputFile(){
		return geodis_input_file;
	}
	
	public static void setGeoDisInputFile_MatrixFormat(String geodisFile){
		geodis_input_matrix_file = geodisFile;
	}
	
	public static String getGeoDisInputFile_MatrixFormat(){
		return geodis_input_matrix_file;
	}
	
	public static void setGMLNestedCladogramFile(String nestFile){
		gml_nest_file = nestFile;
	}
	
	public static String getGMLNestedCladogramFile(){
		return gml_nest_file;
	}
	
	public static void setNestingFile(String nestFile){
		geodis_nest_file = nestFile;
	}
	
	public static String getNestingFile(){
		return geodis_nest_file;
	}
	
	public static void setGeoDisOutputFile(String geodisFile){
		geodis_output_file = geodisFile;
	}
	
	public static String getGeoDisOutputFile(){
		return geodis_output_file;
	}

	public static void setGeoDisOutputFile_MatrixFormat(String geodisFile){
		geodis_output_matrix_file = geodisFile;
	}
	
	public static String getGeoDisOutputFile_MatrixFormat(){
		return geodis_output_matrix_file;
	}

	public static void setGeoDisSummaryFile(String geodisFile){
		geodis_summary_file = geodisFile;
	}
	
	public static String getGeoDisSummaryFile(){
		return geodis_summary_file;
	}
	
	public static void setGeoDisSummaryFile_MatrixFormat(String geodisFile){
		geodis_summary_matrix_file = geodisFile;
	}
	
	public static String getGeoDisSummaryFile_MatrixFormat(){
		return geodis_summary_matrix_file;
	}
	
	public static void setGeoDisInferenceFile(String geodisFile){
		geodis_infer_file = geodisFile;
	}
	
	public static String getGeoDisInferenceFile(){
		return geodis_infer_file;
	}
	
	public static void setGeoDisInferenceFile_MatrixFormat(String geodisFile){
		geodis_infer_matrix_file = geodisFile;
	}
	
	public static String getGeoDisInferenceFile_MatrixFormat(){
		return geodis_infer_matrix_file;
	}
	
	public static void newTCSAnalysis(){
		gui.restoreMenu();
		//gui.setWindow(tcsGraphWindow());
		gui.setWindow(new GraphWindow(true));
	}
		
	public static void createGeoDisFile(){
		gui.restoreMenu();
		gui.setWindow(new NestingPanel());
	}
	
	public static void startGeodis(){
		gui.restoreMenu();
		gui.setWindow(new GeoDis_GUI());
	}
	
	public static void runInferenceKey(){
		gui.restoreMenu();
		gui.setWindow(new InferencePanel());
	}
	
	public static void main(String[] argv){
		try{
			runCommandLineNCPA(argv);
		} catch (FileNotFoundException e) {
			System.out.println("Unable to run command line NCPA");
		}
	}
	
	//public static NestedCladograph writeGeodisInput(String tcsFileName, String distFileName, String geodisInputName, String geodisDistrName){
	//public static NestedCladograph writeGeodisInput(String tcsFileName, String distFileName, String geodisInputName){
	public static NestedCladograph writeGeodisInput(String tcsFileName, String distFileName, String geodisInputName, String geodisDistName, String gmlNestName){
		try{
			String[] distFile = InputFileReader.readFileToArray(distFileName);
			int numPops = 0; // Total number of populations (sampled, and unsampled)
			for(int i = 0; i < distFile.length; i++){
				/*if(distFile[i].matches("\\d+\\s+[a-zA-Z]\\w+\\s+\\d+.*")){
					numPops++;
				}*/
				if(distFile[i].matches("\\d+\\s+[a-zA-Z]\\w*.*")){ // line matches population header line.
					numPops++;
				}
			} 
			int numSampPops = Integer.parseInt(distFile[1].trim()); // secondLine should contain number of sampled populations 
			NestedCladograph ncg = NestedCladeBuilder.buildNestedCladograph(tcsFileName);
			ncg.finaliseData(numPops);
			FileWriter fw = new FileWriter(geodisInputName); // GeoDis Input file - Latitude, Longitude Format
			fw.write(distFile[0]+"\n");
			fw.write(distFile[1]+"\n");
			for(int i = 0; i < numSampPops; i++){
				fw.write(distFile[(i*2)+2]+"\n");
				String[] temp = distFile[(i*2)+3].split("\\s+");
				for(int j=0; j < 3; j++){
					fw.write(temp[j]);
					if(j==2){
						fw.write("\n");
					} else {
						fw.write("\t");
					}
				}
			}
			/*for(int i=0; i< numSampPops+2;i++){ // Assumes distance matrix format
				fw.write(distFile[i]);
				fw.write("\n");
			}*/
			fw.write(ncg.getGeodisInput());
			fw.write("END");
			fw.flush();
			fw.close();
			if(geodisDistName != null && !geodisDistName.equals("")){
				fw = new FileWriter(geodisDistName); // GeoDis Input file - Distance Matrix Format
				fw.write(distFile[0]+"\n");
				fw.write(distFile[1]+"\n");
				for(int i=0; i<numSampPops; i++){
					// Should be updated to adjust for "flattening" of the earth when GeoDis is updated to do the same
					// Current assumption : Earth is a sphere
					fw.write(distFile[(i*2)+2]+"\t");
					String[] temp = distFile[(i*2)+3].split("\\s+");
					fw.write(temp[0]);
					double latitude = Math.toRadians(Double.parseDouble(temp[1]));
					double longitude = Math.toRadians(Double.parseDouble(temp[2]));
					for(int j=0; j < i;j++){
						String[] stemp = distFile[(j*2)+3].split("\\s+");
						double slatitude = Math.toRadians(Double.parseDouble(stemp[1]));
						double slongitude = Math.toRadians(Double.parseDouble(stemp[2]));
						// This version less prone to rounding errors apparently (wikipedia)
						double gcd = 2*RADIUS*Math.asin(Math.sqrt(Math.pow(Math.sin((latitude-slatitude)/2),2)+Math.cos(latitude)*Math.cos(slatitude)*Math.pow(Math.sin((longitude-slongitude)/2),2)));
						fw.write("\t"+gcd);
					}
					fw.write("\n");
				}
				fw.write(ncg.getGeodisInput());
				fw.write("END");
			}
			fw.flush();
			fw.close();
			fw = new FileWriter(gmlNestName);
			fw.write(ncg.toGMLobject().toString());
			fw.flush();
			fw.close();
			return ncg;
		} catch (IOException e){
			System.out.println("Unable to write Geodis Input");
			System.out.println(e.getMessage());
			return null;
		} catch (NumberFormatException e){
			System.out.println("Unable to write Geodis Input");
			System.out.println(e.getMessage());
			return null;
		}
	}

	public static String getGeoDisSummary(String geodisInput, String geodisOutput, String geodisGeog, String gmlNestDsgn) throws ProgressionException{
		return CladeDataExtractor.getGeoDisSummary(geodisOutput,geodisInput,geodisGeog,gmlNestDsgn);
	}
	
	public static NestedCladograph getNestedDesign(String gmlFile,int numPops){
		return NestedCladeBuilder.buildNestedCladograph(gmlFile,numPops);
	}
	
	public static String getGeoDisInference(String geodisInput, String geodisOutput, String geodisGeog, String gmlNestDsgn) throws ProgressionException{
		return CladeDataExtractor.getGeoDisInference(geodisOutput,geodisInput,geodisGeog,gmlNestDsgn);
	}
	
/*	private static GraphWindow tcsGraphWindow(){
		GraphWindow geWindow = new GraphWindow(true);
		TreeAlgorithm talg = new TreeAlgorithm('d');
	    geWindow.addAlgorithm(talg, "Tree Down");
		talg = new TreeAlgorithm('u');
		geWindow.addAlgorithm(talg, "Tree Up");
		talg = new TreeAlgorithm('l');
		geWindow.addAlgorithm(talg, "Tree Left");
		talg = new TreeAlgorithm('r');
		geWindow.addAlgorithm(talg, "Tree Right");
		Spring spring = new Spring();
		geWindow.addAlgorithm(spring, "Spring Tree");
		return geWindow;
	}*/
	
}