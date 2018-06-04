package NCPAInferenceTree;

import java.util.ArrayList;
import java.io.IOException;
import java.io.FileWriter;
import ANeCA.Mediator;
import ANeCA.ProgressionException;
import javax.swing.JOptionPane;

public class CladeDataExtractor{
	
	// public access methods
	public static void main(String argv[]){
		/*  argv[0] is the gdout file
			argv[1] is the gdin file
			argv[2] is the population distribution file
		*/
		try {
			System.out.println(InferenceTree.analyse(buildDataChain(argv[0],argv[1],argv[2],argv[3])));
		}catch(ProgressionException e){
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
	public static void writeGeoDisResults(String gdout, String gdin, String popfile, String gmlfile, String gdsum, String gdinf) throws ProgressionException{
		try{
			CladeDataChain cdc = buildDataChain(gdout,gdin,popfile,gmlfile);
			FileWriter fw = new FileWriter(gdsum);
			fw.write(cdc.toString());
			fw.flush();
			fw.close();
			fw = new FileWriter(gdinf);
			fw.write(InferenceTree.analyse(cdc));
			fw.flush();
			fw.close();
		} catch (IOException e){
		}
	}
	
	public static String getGeoDisSummary(String gdout, String gdin, String popfile, String gmlFile) throws ProgressionException{
		return buildDataChain(gdout,gdin,popfile,gmlFile).toString();
	}
	
	public static String getGeoDisInference(String gdout, String gdin, String popfile, String gmlFile) throws ProgressionException{
		return InferenceTree.analyse(buildDataChain(gdout,gdin,popfile,gmlFile));
	}
	
	// package access methods
	
	// private access methods
	private static CladeDataChain buildDataChain(String fname, String gdinname, String popfile, String gmlNestedDesign) throws ProgressionException{
		try {
			String[] file = ANeCA.InputFileReader.readFileToArray(fname);
			PopulationData[] pd = getPopulationData(popfile);
			CladeDataChain chain = new CladeDataChain(pd,Mediator.getNestedDesign(gmlNestedDesign,pd.length));
			CladeData data = null;
			SubCladeData sData = null;
			TipVsIntTest tiTest = null; 
			String[] param = null;
			for(int i = 0; i< file.length; i++){
				if(file[i].matches("\\s*PERMUTATION\\s+ANALYSIS\\s+OF\\s+[\\s\\w\\d\\-]+")){
					//System.out.println("new clade");
					data = new CladeData(file[i].trim().split("OF\\s+")[1]);
					chain.addCladeData(data);
				} else if(file[i].matches("\\s+OBSERVED\\s+CHI-SQUARE\\s+STATISTIC\\s+=\\s+[\\d.]+")) {
					//System.out.println("chi stat");
					try{
						data.setChiSquareStat(Double.parseDouble(file[i].trim().split("\\s+")[4]));
					} catch (NumberFormatException e){
						StringBuffer str = new StringBuffer();
						str.append("Couldn't parse Chi-square statistic");
						if(!Mediator.isCommandLineApp()){
							JOptionPane.showMessageDialog(Mediator.getGui().getFrame(),str.toString(),
                   				"Inference Key Warning", JOptionPane.ERROR_MESSAGE);
						} else {
							str.append("[GeoDis]\n");
							Mediator.writeError(str.toString());
						}
	                   	throw new ProgressionException(str.toString());
						//System.out.println("Couldn't parse Chi-square statistic");
						//System.out.println(e.getMessage());
						//System.exit(0);
					}
				} else if(file[i].matches("[\\w\\s]+OBSERVED\\s+CHI-SQUARE\\s+=\\s+[\\d.]+")) {
					//System.out.println("chi stat prob");
					try{
						data.setChiSquareProb(Double.parseDouble(file[i].trim().split("\\s+")[7]));
					} catch (NumberFormatException e){
						StringBuffer str = new StringBuffer();
						str.append("Couldn't parse Chi-square statistic probability");
						if(!Mediator.isCommandLineApp()){
							JOptionPane.showMessageDialog(Mediator.getGui().getFrame(),str.toString(),
                   				"Inference Key Warning", JOptionPane.ERROR_MESSAGE);
						} else {
							str.append("[GeoDis]\n");
							Mediator.writeError(str.toString());
						}
	                   	throw new ProgressionException(str.toString());
						//System.out.println("Couldn't parse Chi-square statistic probability");
						//System.out.println(e.getMessage());
						//System.exit(0);
					}
				} else if(file[i].matches("CLADE[\\w\\d\\s\\-\\(\\).]+")) {
					//System.out.println("clade data");
					sData = new SubCladeData(file[i].trim());
					data.addSubCladeData(sData);
				} else if(file[i].matches("\\s*PART III[\\s\\w.:]+")) {
					//System.out.println("switch to tip");
					sData = null;
					tiTest = new TipVsIntTest();
					data.setTipVsInteriorData(tiTest);
				} else if(file[i].matches("\\s*WITHIN\\s+CLADE\\s+[\\d.\\-]+\\s+[\\d.]+\\s+[\\d.]+\\s*")) {
					//System.out.println("rec within clade");
					param = file[i].trim().split("\\s+");
					if(sData != null){
						try{
							sData.setWithinCladeParameters(Double.parseDouble(param[2]),Double.parseDouble(param[3]),Double.parseDouble(param[4]));
						} catch (NumberFormatException e){
							StringBuffer str = new StringBuffer();
							str.append("Couldn't parse Within Clade parameters");
							if(!Mediator.isCommandLineApp()){
								JOptionPane.showMessageDialog(Mediator.getGui().getFrame(),str.toString(),
                   					"Inference Key Warning", JOptionPane.ERROR_MESSAGE);
							} else {
								str.append("[GeoDis]\n");
								Mediator.writeError(str.toString());
							}
	                   		throw new ProgressionException(str.toString());
							//System.out.println("Couldn't parse Within Clade parameters");
							//System.out.println(e.getMessage());
							//System.exit(0);
						}
					} else {
						try{
							tiTest.setWithinCladeParameters(Double.parseDouble(param[2]),Double.parseDouble(param[3]),Double.parseDouble(param[4]));
						} catch (NumberFormatException e){
							StringBuffer str = new StringBuffer();
							str.append("Couldn't parse Within-Clade parameters");
							if(!Mediator.isCommandLineApp()){
								JOptionPane.showMessageDialog(Mediator.getGui().getFrame(),str.toString(),
                   					"Inference Key Warning", JOptionPane.ERROR_MESSAGE);
							} else {
								str.append("[GeoDis]\n");
								Mediator.writeError(str.toString());
							}
	                   		throw new ProgressionException(str.toString());
							//System.out.println("Couldn't parse Within-Clade parameters");
							//System.out.println(e.getMessage());
							//System.exit(0);
						}
					}
				} else if(file[i].matches("\\s*NESTED\\s+CLADE\\s+[\\d.\\-]+\\s+[\\d.]+\\s+[\\d.]+\\s*")) {
					//System.out.println("rec nested clade");
					param = file[i].trim().split("\\s+");
					if(sData != null){
						try{
							sData.setNestedCladeParameters(Double.parseDouble(param[2]),Double.parseDouble(param[3]),Double.parseDouble(param[4]));
						} catch (NumberFormatException e){
							StringBuffer str = new StringBuffer();
							str.append("Couldn't parse Nested Clade parameters");
							if(!Mediator.isCommandLineApp()){
								JOptionPane.showMessageDialog(Mediator.getGui().getFrame(),str.toString(),
                   					"Inference Key Warning", JOptionPane.ERROR_MESSAGE);
							} else {
								str.append("[GeoDis]\n");
								Mediator.writeError(str.toString());
							}
	                   		throw new ProgressionException(str.toString());
							//System.out.println("Couldn't parse Nested Clade parameters");
							//System.out.println(e.getMessage());
							//System.exit(0);
						}
					} else {
						try{
							tiTest.setNestedCladeParameters(Double.parseDouble(param[2]),Double.parseDouble(param[3]),Double.parseDouble(param[4]));
						} catch (NumberFormatException e){
							StringBuffer str = new StringBuffer();
							str.append("Couldn't parse Nested-Clade parameters");
							if(!Mediator.isCommandLineApp()){
								JOptionPane.showMessageDialog(Mediator.getGui().getFrame(),str.toString(),
                   					"Inference Key Warning", JOptionPane.ERROR_MESSAGE);
							} else {
								str.append("[GeoDis]\n");
								Mediator.writeError(str.toString());
							}
	                   		throw new ProgressionException(str.toString());
							//System.out.println("Couldn't parse Nested-Clade parameters");
							//System.out.println(e.getMessage());
							//System.exit(0);
						}
					}
				}
			}
			chain.setTestLabel(addDistributionData(gdinname,chain,pd.length));
			//addDistributionData(gdinname,chain);
			return chain;
		} catch (IOException e){
			StringBuffer str = new StringBuffer();
			str.append("An unknown IO error has occured");
			if(!Mediator.isCommandLineApp()){
				JOptionPane.showMessageDialog(Mediator.getGui().getFrame(),str.toString(),
            	"Inference Key Warning", JOptionPane.ERROR_MESSAGE);
			} else {
				str.append("[GeoDis]\n");
				Mediator.writeError(str.toString());
			}
	        throw new ProgressionException(str.toString());
			//System.out.println(e.getMessage());
			//e.printStackTrace();
			//System.exit(0);
		} catch (NumberFormatException e){
			StringBuffer str = new StringBuffer();
			str.append("Unable to parse number");
			if(!Mediator.isCommandLineApp()){
				JOptionPane.showMessageDialog(Mediator.getGui().getFrame(),str.toString(),
            	"Inference Key Warning", JOptionPane.ERROR_MESSAGE);
			} else {
				str.append("[GeoDis]\n");
				Mediator.writeError(str.toString());
			}
	        throw new ProgressionException(str.toString());
			//System.out.println(e.getMessage());
			//e.printStackTrace();
			//System.exit(0);
		} catch (IncorrectFileFormatException e){
			StringBuffer str = new StringBuffer();
			str.append("The file format needs to be checked.\nUnable to parse file.");
			if(!Mediator.isCommandLineApp()){
				JOptionPane.showMessageDialog(Mediator.getGui().getFrame(),str.toString(),
            	"Inference Key Warning", JOptionPane.ERROR_MESSAGE);
			} else {
				str.append("[GeoDis]\n");
				Mediator.writeError(str.toString());
			}
	        throw new ProgressionException(str.toString());
			//System.out.println(e.getMessage());
			//e.printStackTrace();
			//System.exit(0);
		}
		//return null;
	}
	
	private static String addDistributionData(String dFile, CladeDataChain cdchain, int numPops) throws IOException, NumberFormatException,IncorrectFileFormatException{
		// Adds the distribution data to the clades in the data chain and returns the name of the test
		String[] distrFile = ANeCA.InputFileReader.readFileToArray(dFile); // GeoDis input file line by line
		/*int numPops = 0;
		for(int i = 0; i < distrFile.length; i++){
			if(distrFile[i].matches("\\d+\\s+[a-zA-Z]\\w+\\s+\\d+.*")){
				numPops++;
			}
		}*/
		//final int numPops = Integer.parseInt(distrFile[1]);
		int linePointer = 3 + 2*Integer.parseInt(distrFile[1]);
		while(!distrFile[linePointer].matches("END")){
			// Extract and add Data to chain
			//System.out.println("Line Pointer : "+linePointer);
			CladeData updateClade = cdchain.getClade(distrFile[linePointer]);
			if(updateClade == null){throw new IncorrectFileFormatException("Could not find clade to update with distribution information");}
			int numSubclades = Integer.parseInt(distrFile[linePointer+1]);
			//System.out.println("Number of subclades = "+numSubclades);
			String[] subclades = distrFile[linePointer+2].split("\\s+");
			for(int k = 0; k < numSubclades; k++){
				updateClade.setSubcladeDistribution(subclades[k],convertToIntDistribution(numPops,distrFile[linePointer+5],distrFile[linePointer+6+k]));
			}
			//System.out.println("LP = " + linePointer + " " + numSubclades);
			linePointer += 6 + numSubclades;
		}
		return distrFile[0]; // returns the test label
	}
	
	private static int[] convertToIntDistribution(int size, String index, String distr) throws IncorrectFileFormatException{
		String[] indexes = index.split("\\s+");
		String[] distrs = distr.split("\\s+");
		if(indexes.length != distrs.length){throw new IncorrectFileFormatException("The number of indexes do not correspond to the number of populations represented.");}
		int[] idistr = new int[size];
		for(int i = 0; i < size; i++){
			idistr[i] = 0;
		}
		for(int i = 0; i < indexes.length; i++){
			try{
				idistr[Integer.parseInt(indexes[i])-1] = Integer.parseInt(distrs[i]);
			} catch (NumberFormatException e){
				throw new IncorrectFileFormatException("The file format is incorrect. Cannot generate an integer distribution");
			}
		}
		return idistr;
	}
	
	private static PopulationData[] getPopulationData(String pdfile) throws IOException, NumberFormatException, IncorrectFileFormatException, ProgressionException{
		String[] popDFile = ANeCA.InputFileReader.readFileToArray(pdfile); // line by line
		int numPops = Integer.parseInt(popDFile[1]); // get the number of populations
		ArrayList populations = new ArrayList();
		int linenum = 2;
		while(linenum < popDFile.length){
			if(popDFile[linenum].matches("\\d+\\s+[a-zA-Z]\\w+.*")){
				//Read in population data
				String[] dataline = popDFile[linenum+1].split("\\s+");
				double samplesize = Double.parseDouble(dataline[0]);
				double latitude = Double.parseDouble(dataline[1]);
				double longitude = Double.parseDouble(dataline[2]);
				double radius;
				try {
					radius = Double.parseDouble(dataline[3]);
				} catch (ArrayIndexOutOfBoundsException abe){
					StringBuffer str = new StringBuffer();
					str.append("The radius of location ");
					str.append(popDFile[linenum].split("\\s+")[0]);
					str.append(" has not been specified.");
					str.append("\nAssume radius of 0.0 and continue?");
					if(!Mediator.isCommandLineApp()){
						if(JOptionPane.showConfirmDialog(Mediator.getGui().getFrame(),str.toString(),
                   			"Inference Key Warning", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION){
	                   	 	throw new ProgressionException("Restarting Automated Inference Key");
                   	 	}
					} else {
						str.append("\nYes (Assumed, not confirmed)");
						str.append("[GeoDis]\n");
						Mediator.writeMessage(str.toString());
					}
	                radius = 0.0;
				}
				populations.add(new PopulationData(latitude,longitude,radius,(samplesize > 0)));
			}
			linenum += 2;
		}
		PopulationData[] popD = new PopulationData[populations.size()];
		for(int i=0;i<popD.length;i++){
			popD[i] = (PopulationData) populations.get(i);
		}
		return popD;
	}
	
	/*private static PopulationData[] getPopulationData(String pdfile) throws IOException, NumberFormatException,IncorrectFileFormatException{
		String[] popDFile = ANeCA.InputFileReader.readFileToArray(pdfile); // line by line
		int numPops = Integer.parseInt(popDFile[1]); // get the number of populations
		String[] popIndex = null;
		String[] popRadii = null;
		ArrayList populations = new ArrayList();
		for( int i=0 ; i<numPops; i++){ // Create relative population coordinates to 0,0
			if(i == 0){
				populations.add(new PopulationData(0,0,true));
			} else if (i == 1){
				// Set +x,0 coordinate
				String[] params = popDFile[i+2].split("\\s+");
				populations.add(new PopulationData(Double.parseDouble(params[3]),0,true));
			} else if (i == 2){
				// Set +x,+y coordinate
				String[] params = popDFile[i+2].split("\\s+");
				// variable names should correspond to documentation/report diagram - fundamental geometry is great
				double r = Double.parseDouble(params[3]); // distance from (0,0) to (x,y)
				double s = Double.parseDouble(params[4]); // distance from (d,0) = second point to (x,y)
				double d = ((PopulationData) populations.get(1)).getLatitude(); // distance from (0,0) to (d,0)
				double x,y;
				if(Math.acos((r*r+d*d-s*s)/(2*r*d)) <= Math.PI/2){ // use linear algebra instead
					x = (r*r-s*s+d*d)/(2*d);
					y = Math.sqrt(r*r-x*x);
				} else {
					x = ((r*r-s*s-d*d)/(2*d))+d; // value should be <= 0
					y = Math.sqrt(r*r-x*x);						
				}
				populations.add(new PopulationData(x,y,true));
			} else {
				// set x,y coordinate
				String[] params = popDFile[i+2].split("\\s+");
				// variable names should correspond to documentation/report diagram - fundamental geometry is great
				double r = Double.parseDouble(params[3]); // distance from (0,0) to (x,y)
				double s = Double.parseDouble(params[4]); // distance from (d,0) = second point to (x,y)
				double d = ((PopulationData) populations.get(1)).getLatitude(); // distance from (0,0) to (d,0)
				PopulationData pop3 = (PopulationData) populations.get(2);
				double x,y;
				if(Math.acos((r*r+d*d-s*s)/(2*r*d)) <= Math.PI/2){ // use linear algebra instead
					x = (r*r-s*s+d*d)/(2*d);
					y = Math.sqrt(r*r-x*x); // this is the positive y; it could be negative
				} else {
					x = ((r*r-s*s-d*d)/(2*d))+d; // value should be <= 0
					y = Math.sqrt(r*r-x*x); // this is the positive y; it could be negative						
				}
				// We now have (x,y) and third ref point (a,b) 
				if (Math.abs(Math.sqrt(Math.pow(x-pop3.getLatitude(),2)+Math.pow(y-pop3.getLongitude(),2))-Double.parseDouble(params[5])) > y*0.05){ // Need some threshold to allow for rounding error - This could be a major source of problems
					y = -y;
				}
				populations.add(new PopulationData(x,y,true));
			}
		}
		int lineNum = 2+numPops;
		while((lineNum < popDFile.length) && popDFile[lineNum].matches("\\d+\\s+[a-zA-Z]\\w+(\\s+\\d+){4}.*")){ // This is for the non-sampled populations
			if(populations.size() == 0){
				populations.add(new PopulationData(0,0,false));
			} else if (populations.size() == 1){
				String[] params = popDFile[lineNum].split("\\s+");
				populations.add(new PopulationData(Double.parseDouble(params[3]),0,false));
			} else if (populations.size() == 2){
				String[] params = popDFile[lineNum].split("\\s+");
				double r = Double.parseDouble(params[3]); // distance from (0,0) to (x,y)
				double s = Double.parseDouble(params[4]); // distance from (d,0) = second point to (x,y)
				double d = ((PopulationData) populations.get(1)).getLatitude(); // distance from (0,0) to (d,0)
				double x,y;
				if(Math.acos((r*r+d*d-s*s)/(2*r*d)) <= Math.PI/2){ // use linear algebra instead
					x = (r*r-s*s+d*d)/(2*d);
					y = Math.sqrt(r*r-x*x);
				} else {
					x = ((r*r-s*s-d*d)/(2*d))+d; // value should be <= 0
					y = Math.sqrt(r*r-x*x);						
				}
				populations.add(new PopulationData(x,y,false));
			} else {
				String[] params = popDFile[lineNum].split("\\s+");
				double r = Double.parseDouble(params[3]); // distance from (0,0) to (x,y)
				double s = Double.parseDouble(params[4]); // distance from (d,0) = second point to (x,y)
				double d = ((PopulationData) populations.get(1)).getLatitude(); // distance from (0,0) to (d,0)
				PopulationData pop3 = (PopulationData) populations.get(2);
				double x,y;
				if(Math.acos((r*r+d*d-s*s)/(2*r*d)) <= Math.PI/2){ // use linear algebra instead
					x = (r*r-s*s+d*d)/(2*d);
					y = Math.sqrt(r*r-x*x); // this is the positive y; it could be negative
				} else {
					x = ((r*r-s*s-d*d)/(2*d))+d; // value should be <= 0
					y = Math.sqrt(r*r-x*x); // this is the positive y; it could be negative						
				}
				if (Math.abs(Math.sqrt(Math.pow(x-pop3.getLatitude(),2)+Math.pow(y-pop3.getLongitude(),2))-Double.parseDouble(params[5])) > y*0.05){ // Need some threshold to allow for rounding error - This could be a major source of problems
					y = -y;
				}
				populations.add(new PopulationData(x,y,false));
			}
			lineNum++;
		}
		PopulationData[] popD = new PopulationData[populations.size()];
		for(int i=0;i<popD.length;i++){
			popD[i] = (PopulationData) populations.get(i);
		}
		if((lineNum < popDFile.length) && popDFile[lineNum].matches("(\\d+\\s+){"+(popD.length-1)+"}\\d+\\s*") && popDFile[lineNum+1].matches("(\\d+\\s+){"+(popD.length-1)+"}\\d+\\s*")){
			String[] index = popDFile[lineNum].split("\\s+");
			String[] radii = popDFile[lineNum+1].split("\\s+");
			for(int i = 0; i<index.length;i++){
				popD[Integer.parseInt(index[i])-1].setPopulationRadius(Double.parseDouble(radii[i]));
			}
		}
		return popD;
	}*/
	
	/*private static PopulationData[] getPopulationData(String pdfile) throws IOException, NumberFormatException,IncorrectFileFormatException{
		String[] popDFile = ANeCA.InputFileReader.readFileToArray(pdfile); // line by line
		int numPops = Integer.parseInt(popDFile[1]); // get the number of populations
		String[] popIndex = null;
		String[] popRadii = null;
		if(popDFile.length >= numPops+4 ){
			popIndex = popDFile[numPops+2].split("\\s+"); // Population ids
			popRadii = popDFile[numPops+3].split("\\s+"); // Population sizes
		}
		//Assume distance matrix for the moment
		PopulationData[] popD = new PopulationData[numPops];
		for( int i=0 ; i<numPops; i++){ // Create relative population coordinates to 0,0
			if(i == 0){
				// Set 0,0 coordinate
				if(popDFile.length >= numPops+4){
					int ind = getPopulationIndex(Integer.parseInt(popDFile[i+2].split("\\s+")[0]),popIndex);
					popD[i] = new PopulationData(0,0,Double.parseDouble(popRadii[ind]),true); 
				} else {
					popD[i] = new PopulationData(0,0,true);
				}
			} else if (i == 1){
				// Set +x,0 coordinate
				String[] params = popDFile[i+2].split("\\s+");
				if(popDFile.length >= numPops+4){
					int ind = getPopulationIndex(Integer.parseInt(params[0]),popIndex);
					popD[i] = new PopulationData(Double.parseDouble(params[3]),0,Double.parseDouble(popRadii[ind]),true);
				} else {
					popD[i] = new PopulationData(Double.parseDouble(params[3]),0,true);
				}
			} else if (i == 2){
				// Set +x,+y coordinate
				String[] params = popDFile[i+2].split("\\s+");
				// variable names should correspond to documentation/report diagram - fundamental geometry is great
				double r = Double.parseDouble(params[3]); // distance from (0,0) to (x,y)
				double s = Double.parseDouble(params[4]); // distance from (d,0) = second point to (x,y)
				double d = popD[1].getLatitude(); 		  // distance from (0,0) to (d,0)
				double x,y;
				if(Math.acos((r*r+d*d-s*s)/(2*r*d)) < Math.PI/2){
					x = (r*r-s*s+d*d)/(2*d);
					y = Math.sqrt(r*r-x*x);
				} else {
					x = (s*s-r*r-d*d)/(2*d); // value should be <= 0
					y = Math.sqrt(r*r-x*x);						
				}
				if(popDFile.length >= numPops+4){
					int ind = getPopulationIndex(Integer.parseInt(params[0]),popIndex);
					popD[i] = new PopulationData(x,y,Double.parseDouble(popRadii[ind]),true);
				} else {
					popD[i] = new PopulationData(x,y,true);
				}
			} else {
				// set x,y coordinate
				String[] params = popDFile[i+2].split("\\s+");
				// variable names should correspond to documentation/report diagram - fundamental geometry is great
				double r = Double.parseDouble(params[3]); // distance from (0,0) to (x,y)
				double s = Double.parseDouble(params[4]); // distance from (d,0) = second point to (x,y)
				double d = popD[1].getLatitude(); 		  // distance from (0,0) to (d,0)
				double x,y;
				if(Math.acos((r*r+d*d-s*s)/(2*r*d)) < Math.PI/2){
					x = (r*r-s*s+d*d)/(2*d);
					y = Math.sqrt(r*r-x*x); // this is the positive y; it could be negative
				} else {
					x = (s*s-r*r-d*d)/(2*d); // value should be <= 0
					y = Math.sqrt(r*r-x*x); // this is the positive y; it could be negative						
				}
				// We now have (x,y) and third ref point (a,b) 
				if (Math.abs(Math.sqrt(Math.pow(x-popD[2].getLatitude(),2)+Math.pow(y-popD[2].getLongitude(),2))-Double.parseDouble(params[5])) > y*0.05){ // Need some threshold to allow for rounding error - This could be a major source of problems
					y = -y;
				}
				if(popDFile.length >= numPops+4){
					int ind = getPopulationIndex(Integer.parseInt(params[0]),popIndex);
					popD[i] = new PopulationData(x,y,Double.parseDouble(popRadii[ind]),true);
				} else {
					popD[i] = new PopulationData(x,y,true);
				}
			}
		}
		return popD;
	}
	
	private static int getPopulationIndex(int popNum, String[] popIndex) throws IOException,NumberFormatException,IncorrectFileFormatException{
		for(int i = 0; i < popIndex.length; i++){
			if(popNum == Integer.parseInt(popIndex[i])){
				return i;
			}
		}
		throw new IncorrectFileFormatException("There was a problem parsing the file");
	}*/
}