// This is for multi-locus NCPA

import java.io.*;
import java.util.*;

public class ANeCAML{

	private ANeCAML(){
	}
	
	public static void main(String[] ARGV){
		// if argv == 0 run ANeCAMLInterface // forget this. interface take too long.
		// else run command line version or exit
		// Command line version reads in a batch file containing names of files needed for each locus.
		
		// obtain ANeCAdata
		// find cross validated inferences
		// print out the cross validated inferences.
		ANeCAdata dataToWrite = readFiles(ARGV[0]);
		try{
			FileWriter fw = new FileWriter(ARGV[1]);
			fw.write(dataToWrite.findCrossValidatedInferences().toString());
			fw.flush();
			fw.close();
		} catch (Exception e){
			System.out.println(e.getMessage());
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	//public static ANeCAdata readFiles(String nexusFile, String gmlFile, String inferFile, String summaryFile){
	public static ANeCAdata readFiles(String batchFile){
		// parse batch file and then read in data.
		// call each readXfile and throw exception if with interface report exception, else print to log.
		ANeCAdata mldata = new ANeCAdata();
		try{
			String[] filesForEachLocus = readFileToArray(batchFile);
			for(int i = 0; i < filesForEachLocus.length; i++){
				// split line for each file
				ANeCAlocus locus = new ANeCAlocus();
				String[] filesToParse = filesForEachLocus[i].split("\\s+");
				locus.setLocusLabel(filesToParse[0].substring(0,filesToParse[0].lastIndexOf(".")));
				locus.consolidateData(readNexusFile(filesToParse[0]),readGMLFile(filesToParse[1]),readInferenceFile(filesToParse[2]),readSummaryFile(filesToParse[3]));
				mldata.add(locus);
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			System.exit(0);
		}
		return mldata;
	}
	
	private static NexusData readNexusFile(String filename) throws FileNotFoundException,IOException{
		// Reads in data for a single locus.
		// doesn't check for duplicate names.
		String[] file = readFileToArray(filename);
		NexusData ndata = new NexusData();
		for(int i=0;file.length > i;i++){
			if(file[i].contains("NCHAR=")){
				String ncharNum = (file[i].substring(file[i].indexOf("NCHAR="))).split("\\s+")[0].trim();
				ndata.setSequenceLength(Integer.parseInt(ncharNum));
			}
			if(file[i].matches("\\w+\\.\\d+\\s+[AGCT-]+")){
				String[] labelSeq = file[i].split("\\s+");
				ndata.addSequence(labelSeq[0],labelSeq[1]);
			}
		}
		return ndata;
	}
	
	private static GMLData readGMLFile(String filename) throws FileNotFoundException,IOException{
		String[] file = readFileToArray(filename);
		GMLData gmldata = new GMLData();
		boolean node = true; // will encounter first set of nodes in haplotype graph
		StringBuffer strBuff = null;
		String currentHoC = "";
		boolean higherOrder = false; // whether in higher order clades;
		for(int i = 0; i < file.length; i++){
			if(file[i].contains("frequency=")){
				// This reads in all the haplotypes found only in the first graph
				String listHaps = file[i].trim().split("=\\d+\\s+")[1].split("\"")[0];
				gmldata.addHaplotypeIdentifiers(listHaps.split("\\s+")[0],listHaps);
			} else if(file[i].contains("node")){ // moving on to higher level parts here.
				node = true;
				if(strBuff != null){
					gmldata.addCladeHierarchy(currentHoC,strBuff.toString().trim());
				}
			} else if(file[i].contains("label") && higherOrder){
				if(node){
					// we have clade label
					strBuff = new StringBuffer();
					String lbl = file[i].split("\"")[1];
					if(lbl.contains("Total")){
						currentHoC = lbl;
					} else {
						currentHoC = lbl.split("\\s+")[1];
					}
					node = false;
				} else {
					//subclade
					String lbl = file[i].split("\"")[1];
					if(lbl.contains("Clade")){
						strBuff.append(lbl.split("\\s+")[1]).append(" ");
					} else {
						strBuff.append(lbl).append(" ");
					}
				}
			} else if (file[i].contains("Level 1")){
				higherOrder = true;
			}
		}
		if(strBuff != null){
			gmldata.addCladeHierarchy(currentHoC,strBuff.toString().trim());
		}
		return gmldata;
	}
	
	private static InferenceData readInferenceFile(String filename) throws FileNotFoundException,IOException{
		InferenceData infData = new InferenceData();
		String[] inferenceFile = readFileToArray(filename);
		String clade = null;
		for(int i = 0;inferenceFile.length > i; i++){
			if(inferenceFile[i].contains("Clad")){
				if(inferenceFile[i].contains("Clade")){
					clade = inferenceFile[i].trim().split("\\s+")[1];
				} else {
					clade = inferenceFile[i].trim();
				}
			} else if(InferenceData.isInference(inferenceFile[i])){
				if(inferenceFile[i].equals(InferenceData.AF)){
					if(inferenceFile[i-1].contains("4-9")){
						infData.addInference(clade,inferenceFile[i]+"4");
					} else if (inferenceFile[i-1].contains("5-15")){
						infData.addInference(clade,inferenceFile[i]+"5");
					} else if (inferenceFile[i-1].contains("1-19 NO")){
						infData.addInference(clade,inferenceFile[i]+"1");
					} else {
						System.out.println("ERROR reading in AF path of inference.");
					}
					infData.addInference(clade,inferenceFile[i]);
				} else {
					infData.addInference(clade,inferenceFile[i]);
				}
			} else if(inferenceFile[i].equals("Moving on to next clade")){
				infData.addInference(clade,InferenceData.NHCR);
			} else if(inferenceFile[i].contains("Too few Clades")){
				infData.replaceInference(clade,InferenceData.IGR_RECvRDGF);
			}
		}
		return infData;
	}
	
	private static SummaryData readSummaryFile(String filename) throws FileNotFoundException,IOException{
		SummaryData sumdata = new SummaryData();
		String[] summaryfile = readFileToArray(filename);
		String cladename = "";
		for(int i = 0; i < summaryfile.length;i++){
			if(summaryfile[i].matches("\\d+\\s+Lat:\\s+\\d+\\.\\d+\\s+Lon:\\s+\\d+\\.\\d+\\s+Rad:\\s+\\d+\\.\\d+")){
				String[] linesplit = summaryfile[i].split("\\s+");
				// adds both sampled and unsampled.
				sumdata.addLocationData(Double.parseDouble(linesplit[2]),Double.parseDouble(linesplit[4]),Double.parseDouble(linesplit[6]));
			} else if (summaryfile[i].matches("Clade\\s+\\d+\\-\\d+")){
				cladename = summaryfile[i].split("\\s+")[1];
				sumdata.addClade(cladename);
			} else if (summaryfile[i].matches("Total Cladogram")){
				cladename = summaryfile[i];
				sumdata.addClade(summaryfile[i]);
			} else if (summaryfile[i].matches("\\w+\\.?\\-?\\w+\\s+[IT]\\s*\\-?\\d+\\.\\d+(\\*[sl])?\\s+\\-?\\d+\\.\\d+(\\*[sl])?(\\s+\\d+)+")){
				String[] linesplit = summaryfile[i].split("\\s+",4);
				String geograf;
				String sigDc = "";
				String sigDn = "";
				if(linesplit[1].contains(".")){
					geograf = linesplit[3];
					if(linesplit[1].contains("*s")){
						sigDc = "*s";
					} else if(linesplit[1].contains("*l")){
						sigDc = "*l";
					}
					if(linesplit[2].contains("*s")){
						sigDn = "*s";
					} else if (linesplit[2].contains("*l")){
						sigDn = "*l";
					}
				} else {
					geograf = linesplit[3].split("\\s+",2)[1];
					if(linesplit[2].contains("*s")){
						sigDc = "*s";
					} else if(linesplit[2].contains("*l")){
						sigDc = "*l";
					}
					if(linesplit[3].contains("*s")){
						sigDn = "*s";
					} else if (linesplit[3].contains("*l")){
						sigDn = "*l";
					}
				}
				sumdata.addSubclade(linesplit[0],linesplit[1].contains("*")||linesplit[2].contains("*")||linesplit[3].contains("*"),geograf,linesplit[1].contains("T"),sigDc,sigDn);
			} else if (summaryfile[i].matches("I-T\\s+\\-?\\d+\\.\\d+\\s+\\-?\\d+\\.\\d+")){
				String[] linesplit = summaryfile[i].split("\\s+");
				String sigDc = "";
				String sigDn = "";
				if(linesplit[1].contains("*s")){
					sigDc = "*s";
				} else if(linesplit[1].contains("*l")){
					sigDc = "*l";
				}
				if(linesplit[2].contains("*s")){
					sigDn = "*s";
				} else if (linesplit[2].contains("*l")){
					sigDn = "*l";
				}
				sumdata.addSubclade(cladename+" "+linesplit[0],linesplit[1].contains("*")||linesplit[2].contains("*"),"",false,sigDc,sigDn);
			}
		}
		return sumdata;
	}
	
	private static String readFileToLine(String fname) throws FileNotFoundException,IOException{
		BufferedReader buffer = new BufferedReader(new FileReader(fname));
		StringBuffer file = new StringBuffer();
		String line;
		while((line = buffer.readLine()) != null){
			file.append(line);
		}
		return file.toString();
	}
	
	private static String[] readFileToArray(String fname) throws FileNotFoundException,IOException{
		BufferedReader buffer = new BufferedReader(new FileReader(fname));
		ArrayList file = new ArrayList();
		String line;
		while((line = buffer.readLine()) != null){
			file.add(line);
		}
		Object[] fileArray = file.toArray();
		String[] fArray = new String[fileArray.length];
		for(int i = 0; i < fileArray.length; i++){
			fArray[i] = (String) fileArray[i];
		}
		return fArray;
	}

}
