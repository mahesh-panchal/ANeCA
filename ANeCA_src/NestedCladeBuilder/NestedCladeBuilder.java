package NestedCladeBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.StringReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import TCS.VGJ.graph.GMLlexer;
import TCS.VGJ.graph.GMLobject;
import TCS.VGJ.graph.ParseError;

public class NestedCladeBuilder{
	
	public static void main(String argv[]){
		//System.out.println(Nester.run(constructHaplotypeNetwork(argv[0])).toString());
		/*try{
			String[] file = splitFile(ANeCA.InputFileReader.readFileToLine(argv[0]));
			for(int i = 0; i < file.length; i++){
				System.out.println(file[i]);
			}
		} catch(IOException e){
		}*/
		System.out.println(buildNetwork(argv[0]).toString());
		//System.out.println(buildNestedCladograph(argv[0]).toGMLobject().toString());
		/*try{
			System.out.println(buildNestedCladograph(buildNestedCladograph(argv[0]).toGMLobject(),9).toGMLobject().toString());
		} catch (ParseError e){
			System.out.println("didn't parse");
		}*/
	}
	
	public static NestedCladograph buildNestedCladograph(String file){
		//return Nester.run(constructHaplotypeNetwork(file)); //for TCS 1.13
		return Nester.run(buildNetwork(file)); // for TCS 1.17
	}
	
	public static NestedCladograph buildNestedCladograph(String gmlNestDesign,int numPops){
		try{	
			GMLlexer gmlLexer = new GMLlexer(new StringReader(ANeCA.InputFileReader.readFileToLine(gmlNestDesign)));
			GMLobject gmlFile = new GMLobject(gmlLexer,null);
			return buildNestedCladograph(gmlFile,numPops);
			/*NestedCladograph ncg = buildNestedCladograph(gmlFile,numPops);
			System.out.println(ncg.toGMLobject().toString());
			return ncg;*/
		} catch (IOException e){
			System.out.println(e.getMessage());
			e.printStackTrace();
			System.exit(0);
		} catch (ParseError e){
			System.out.println(e.getMessage());
			e.printStackTrace();
			System.exit(0);
		}
		return null;
	}
	
	public static NestedCladograph buildNestedCladograph(GMLobject fileObject, int numPops) throws ParseError{
		NestedCladograph ncg = new NestedCladograph();
		for(GMLobject nestedLevel = fileObject.getGMLSubObject("graph",GMLobject.GMLlist,false); nestedLevel!= null; nestedLevel = fileObject.getNextGMLSubObject()){
			if(nestedLevel == null){
				throw new ParseError("The gml file does not contain a graph");
			}
			int nestlevel = ((Integer) nestedLevel.getValue("Level",GMLobject.GMLinteger)).intValue();
			NStepNetwork nsn = new NStepNetwork(nestlevel);
			// we now have a gml object that is the graph
			// Now extract info - using same idea as fillTaxaInfo.
			int source_id;
			Clade source, target;
			GMLobject data;
			String label;
			// get all nodes
			for(GMLobject gmlObject = nestedLevel.getGMLSubObject("node", GMLobject.GMLlist,false); gmlObject != null; gmlObject = nestedLevel.getNextGMLSubObject()){
				source_id = ((Integer) gmlObject.getValue("id",GMLobject.GMLinteger)).intValue();
				label = (String) gmlObject.getValue("label",GMLobject.GMLstring);
				if(label.matches("\\S+.*")){ //if matches non white space characters
					data = gmlObject.getGMLSubObject("data",GMLobject.GMLlist,false);
					String freq = (String) data.getValue("Frequency",GMLobject.GMLstring);
					if (freq != null){
						nsn.add(new HaplotypeClade(source_id,label,freq,(String)data.getValue("Weight",GMLobject.GMLstring)));
					} else {
						NestedClade nc = new NestedClade(source_id);
						nc.setLabel(label);
						NStepNetwork levelBelow = ncg.getNestingLevel(nestlevel-1); 
						if(levelBelow == null){ // this part makes the file order dependent!
							throw new ParseError("Cannot build connections between levels");
						}
						for(GMLobject child = data.getGMLSubObject("Subclade",GMLobject.GMLlist,false); child != null; child = data.getNextGMLSubObject()){
							nc.addSubClade(levelBelow.getClade(((Integer) child.getValue("id",GMLobject.GMLinteger)).intValue()));
						}
						nsn.add(nc);
					}
				} else {
					nsn.add(new HaplotypeClade(source_id,HaplotypeClade.INFERRED_LABEL));
				}
			}
			// then get all edges
			for(GMLobject gmlObject = nestedLevel.getGMLSubObject("edge", GMLobject.GMLlist,false); gmlObject != null; gmlObject = nestedLevel.getNextGMLSubObject()){
				source = nsn.getClade(((Integer) gmlObject.getValue("source",GMLobject.GMLinteger)).intValue());
				target = nsn.getClade(((Integer) gmlObject.getValue("target",GMLobject.GMLinteger)).intValue());
				if(!source.connectedTo(target)){ // To avoid duplicate linkages
					source.setEdge(target);
					target.setEdge(source); 
				}
			}
			ncg.addNestingLevel(nsn);
		}
		ncg.finaliseData(numPops);
		return ncg;
	}

	/*private static NStepNetwork constructHaplotypeNetwork(String file){
		//String[] info;
		String info = "";
		//try{
			//info = splitFile(ANeCA.InputFileReader.readFileToLine(file));
			return buildNetwork(info);
		//} catch (IOException e){
		//	System.out.println(e.getMessage());
		//	e.printStackTrace();
		//	System.exit(0);
		//}
		//return null;
	}
		
	private static String[] splitFile(String ln){
		//String newLn = ln.replaceAll("(?:vgj|graphics)\\s*\\[[\\w.\\s\"]*\\]","").replaceAll("\\s+"," ");
		String newLn = ln.replaceAll("\\s+"," ");
		return newLn.split("\\bno|\\bed");
	}*/
	
	/* This is the new method to parse the graph file for TCS 1.17 onwards*/
	/* It should even work with the old graph files now in TCS 1.13*/
	private static NStepNetwork buildNetwork(String graph_file){
		NStepNetwork zeroStepClades = new NStepNetwork(0);
		try{
			GMLlexer gmlLexer = new GMLlexer(new StringReader(ANeCA.InputFileReader.readFileToLine(graph_file)));
			GMLobject gmlFile = new GMLobject(gmlLexer,null);
			GMLobject gmlGraph = gmlFile.getGMLSubObject("graph", GMLobject.GMLlist, false);
			if(gmlGraph == null){
				throw new ParseError("The gml file does not contain a graph");
			}
			// we now have a gml object that is the graph
			// Now extract info - using same idea as fillTaxaInfo.
			int source_id;
			Clade source, target;
			GMLobject data;
			String label;
			// get all nodes
			for(GMLobject gmlObject = gmlGraph.getGMLSubObject("node", GMLobject.GMLlist,false); gmlObject != null; gmlObject = gmlGraph.getNextGMLSubObject()){
				source_id = ((Integer) gmlObject.getValue("id",GMLobject.GMLinteger)).intValue();
				label = (String) gmlObject.getValue("label",GMLobject.GMLstring);
				if(label.matches("\\S+.*")){ //if matches non white space characters
					data = gmlObject.getGMLSubObject("data",GMLobject.GMLlist,false);
					zeroStepClades.add(new HaplotypeClade(source_id,label,(String)data.getValue("Frequency",GMLobject.GMLstring),(String)data.getValue("Weight",GMLobject.GMLstring)));
				} else if (label.matches("\\s+")){ // at least one whitespace
					zeroStepClades.add(new HaplotypeClade(source_id,HaplotypeClade.INFERRED_LABEL));
				}
			}
			// then get all edges
			for(GMLobject gmlObject = gmlGraph.getGMLSubObject("edge", GMLobject.GMLlist,false); gmlObject != null; gmlObject = gmlGraph.getNextGMLSubObject()){
				source = zeroStepClades.getClade(((Integer) gmlObject.getValue("source",GMLobject.GMLinteger)).intValue());
				target = zeroStepClades.getClade(((Integer) gmlObject.getValue("target",GMLobject.GMLinteger)).intValue());
				if(!source.connectedTo(target)){ // To avoid duplicate linkages
					source.setEdge(target);
					target.setEdge(source); 
				}
			}
		} catch (IOException e){
			System.out.println(e.getMessage());
			e.printStackTrace();
			System.exit(0);
		} catch (ParseError e){
			System.out.println(e.getMessage());
			e.printStackTrace();
			System.exit(0);
		} catch (NullPointerException e){
			System.out.println("Couldn't strip info from graph file: Graph could not be rebuilt.");
			System.out.println(e.getMessage());
			e.printStackTrace();
			System.exit(0);
		}
		return zeroStepClades;
	}
	
	/* This was the old method to parse the graph file. for TCS 1.13
	private static NStepNetwork buildNetwork(String[] data){
		NStepNetwork zeroStepClades = new NStepNetwork(0);
		String[] attributes;
		int idSNo, idTNo;
		Clade source, target;
		for(int i=1;i<data.length;i++){ //ignore first element as contains "graph ["
			if(data[i].matches("^de\\s*\\[[\\w.\\s\"]*\\]\\]?\\s*$")){ // Matches a missing intermediate
				attributes = data[i].split("\\s+");
				try{
					idSNo = Integer.parseInt(attributes[3]); // Assumes Id_value is 4th element
					if((source = zeroStepClades.getClade(idSNo)) != null){
						source.setLabel(HaplotypeClade.INFERRED_LABEL);
					} else {
						zeroStepClades.add(new HaplotypeClade(idSNo,HaplotypeClade.INFERRED_LABEL));
					}
				} catch (NumberFormatException e){
					System.out.println(e.getMessage());
					System.out.println("Unable to continue.");
					System.exit(0);
				}
			} else if (data[i].matches("^ge\\s*\\[[\\w.\\s\"]*\\]\\]?\\s*$")){ // matches an edge
				attributes = data[i].replaceAll("[\\[\\]]","").split("\\s+");
				try{
					idSNo = Integer.parseInt(attributes[2]); // Assumes source_value is 3rd element
					idTNo = Integer.parseInt(attributes[4]); // Assumes target_value is 5th element
					if((source = zeroStepClades.getClade(idSNo)) == null){
						source = new HaplotypeClade(idSNo);
						zeroStepClades.add(source);
					}
					if((target = zeroStepClades.getClade(idTNo)) == null){
						target = new HaplotypeClade(idTNo);
						zeroStepClades.add(target);
					}
					source.setEdge(target);
					//target.setEdge(source);
				} catch (NumberFormatException e){
					System.out.println(e.getMessage());
					System.out.println("Unable to continue.");
					System.exit(0);
				}
			} else if (data[i].matches("^de\\s*\\")){ // Matches a sample haplotype
				attributes = data[i].split("data\\s+");
				try{
					idSNo = Integer.parseInt(attributes[0].split("\\s+")[3]); // Assumes Id_value is 4th element in 1st
					if((source = zeroStepClades.getClade(idSNo)) != null){
						source.setLabel(attributes[0].split("\\s+")[5]);  	// Assumes Label_value is 6th element in 1st
						((HaplotypeClade) source).setData(attributes[1].split("\"")[1],attributes[1].split("\"")[3]);
					} else {
						zeroStepClades.add(new HaplotypeClade(idSNo,attributes[0].split("\\s+")[5],attributes[1].split("\"")[1],attributes[1].split("\"")[3]));
					}
				} catch (NumberFormatException e){
					System.out.println(e.getMessage());
					System.out.println("Unable to continue.");
					System.exit(0);
				}
			}
		}
		return zeroStepClades;
	}*/
	
}