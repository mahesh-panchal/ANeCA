package GeoDis;

/* 
	Title:		GeoDis
	Author:		David Posada (dposada@uvigo.es)
				Facultad de Ciencias, Universidad de Vigo
				Vigo 36200, Spain
	Started:	April 1999
	Purpose:	Perform calculations for the nested clade anlaysis (NCA)
	Previous: 	Based on code in BASIC by Alan Templeton, version 4 (4/15/93)


	Version history:

	2.0 (June 1999) 
	-	First release

	2.1 (May 2004)
	- Added threshold for rounding errors
	- Changed variable names to be more informative
	- Small fixes here and there that do not affect calculations
	- Print geographical centers	
	- Add BrowserLauncher.java (thanks to Eric Albert) to acces directly the Geodis web site
	- Improved calculation of correlation probabilities
	- Use FileDialog (more powerful) instead of FileChooser. 
		
	2.2 (June 2004)
	- Now preventing for rounding issues that could make that some clades to have a Dc=0.0001 
	instead of Dc=0 (and sometimes P<=0.6 or P<=0.3 instad of P<=1). Now, I think this issue only affected 
	cases with 1 observation per clade, and maybe more often when localities where really close
	to each other. (thanks to Paul Bloor).
	I also noted that I was using D2R = 0.0174532925 (version 2.1) instead of D2R = 0.017453293 
	(version 2.0). This plus rounding implied that the Z value for some clades occuring at one 
	location was 0.9999999998 instead of 1.0. 
	- Speed up program by jumping some calculations over empty locations
	- Decreased the rounding threshold to compare observed and randomized statistics from 1e-9 to 1e-6. 
	Otherwise saw PCs different runs of the same data with too divergent P-values in some instances. 
	Did not observe so far this problem in Macs.



	Warning: There might be errors when calculating clade center from locations
		close to the pole. In such case is best to use a pairwise distance matrix. 
	
*/

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import java.io.*;
//import byu.*;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import ANeCA.Mediator;
import ANeCA.ProgressionException;

public class GeoDis 
{
	private static final String PROGRAM_NAME = ("Geodis");
	public static final String VERSION_NUMBER = ("2.5");
	private static final double D2R = 0.017453293;         // degrees -> radians conversion factor  (PI/180)  
    //	private static final double D2R = 0.017453293000000002;
	private static final double GC2K = 111.12;             // 1 degree in a great circle = 111.12 km 
	private static final double RADIUS = 6364.963;         // average radius of the earth in km 
    //	private static final double RADIUS = 6364.9629999999997;
//dec	private static final int ROWS = 150;
//dec	private static final int COLS = 150;
	private static final int NA = 999;
	private static final int tip = 1;
	private static final int interior = 0;
	public static String infilename = "none selected";	   // name of the input file
	public static String outfilename = "none selected";    // name of the output file
	public static String logfilename = "logfile";
	private static TextInputStream infile;
	private static TextOutputStream outfile;
	private static TextOutputStream outdoc;
	public static TextOutputStream logfile;
	public static boolean printToFile = false;
	public static boolean weights = false;                 // true if there are outgroup weights
	public static boolean doingDistances = false;          // true if the are user-defined distances
	public static boolean usingDecimalDegrees = false;    // true if coordinates are in decimal degrees
	public static boolean done = false;
	public static Clade[] clad;                           // an array of objects clade
   	public static long start, end;
   	public static double[][] distance;                     // pairwise distances among populations
  
  	private static final double ROUNDING_ERROR = -1e-5;		// threshold to compare two numbers
    //	private static final double ROUNDING_ERROR = -1.0000000000000001E-005;
	public static String url = "http://darwin.uvigo.es/software/geodis.html";
	static String dataName;                                // name of the data set
	
	private static Date time;
 
	static int[] locationIndex;                 
	static String[] locationName;
	static int[] sampleSize;                 
	static double[] Longitude;
	static double[] Latitude;
	
	static int numLocations;                  // number of geographical locations                           
	static int numClades;                     // number of clades
	public static int numPermutations;               // number of permutations
	static JPanel frame;
	static Clade clade;

	public static String currentCladeName;
	static double[][] pairwiseKm;
	static TextOutputStream error;


	public static int i, j, k, c, progress, popA, popB, index, lon_deg, lon_min, lon_sec, lat_deg, lat_min, lat_sec;
	public static double randObsChi, Z, ZB, VZ, VZB, RVZ, RVZB, randTipDistance, randIntDistance, randTipDisNested, 
						 randIntDisNested, randCorrDcWeights, randCorrDnWeights, N,
	                     sum1, sum2, sum3, sumc1, sumc2, sumc3, percentage, dec_lon, dec_lat;
	public static String lon_coor, lat_coor, trash;

	public static boolean verbose = false;

	static
	{
		error = new TextOutputStream(System.err);
	}
	
	public static void main (String[] args) throws IOException
	{
		
		try
		{
		UIManager.getCrossPlatformLookAndFeelClassName();
        	} 
        	catch (Exception e) { }

		frame = new GeoDis_GUI(); 	
		//frame.setTitle("GEODIS " + VERSION_NUMBER + "  (Java " + System.getProperty("java.version") + " from " + 
		//		System.getProperty("java.vendor") + ")" + " -- " + System.getProperty("os.name") +
		//		" " + System.getProperty("os.version") );

		//frame.setResizable(false);	
		//frame.setDefaultCloseOperation(frame.DISPOSE_ON_CLOSE);
		//frame.setJMenuBar(GeoDis_GUI.menuBar); 
		//frame.pack();
		frame.setBounds(20,20,550,300);
		frame.setVisible(true);

	 	//frame.addWindowListener(new WindowAdapter()  	
	 	//{
	    //	public void windowClosing (WindowEvent e) 
	    //	{
	    //       System.exit(0);
	    //    }
	    //});			

	} // method main
 
 
	public static void startLogFile(){
		// to stop null pointer exceptions in command line version // mp
		logfile = new TextOutputStream(logfilename);
		logfile.print("Differentiating population structure from history - Geodis");
		logfile.print("  2.5");
		logfile.print("\n(c) Copyright, 1999-2006 David Posada and Alan Templeton");
		logfile.print("\nContact: David Posada, University of Vigo, Spain (dposada@uvigo.es)");
		logfile.print("\n________________________________________________________________________\n\n");
		logfile.print("Input file: " + infilename);
		logfile.println("\n\n" + (new Date()).toString());
	}

 
	
	public static void statistics()
	{
			logfile.print("\nCalculating statistics and permuting ... ");
			progress = 0;
			start = System.currentTimeMillis();

			for (k=0; k<numClades; k++)
			{
				currentCladeName = clad[k].cladeName;
				CalcChiSquare();

				if (doingDistances)
					matrixDis();
				else
					obsDis();
					
				ITdistances();

				if(weights)
				corrDistOut();
							
				permute();	
			}
			
			end = System.currentTimeMillis();
			logfile.println("OK");
	
	} // method statistics





	/************************ OBS_CHI **************************/
	/* CALCULATE OBSERVED CHI - SQUARE STATISTIC  */
	public static void CalcChiSquare()
	{
		//System.err.println("OBSCHI");
	
		clade = clad[k];
		
		clade.obsChi = 0;
		
		for(i = 0; i < clade.numSubClades; i++)
		{
			//System.err.println("");
			for(j = 0; j < clade.numCladeLocations; j++)
			{
				clade.expObsMatrix[i][j] = (double) (clade.rowTotal[i] * clade.columnTotal[j]) / (double) clade.totaNumObs;
				clade.obsChi= clade.obsChi + Math.pow((double) clade.obsMatrix[i][j] - clade.expObsMatrix[i][j], 2) / (double) clade.expObsMatrix[i][j];
				//System.err.print(clade.obsChi + " ");
			}
		}
		//System.err.println("\nCHI: " + clade.obsChi);

	
	} // method ObsChi
	
	public static void pairwiseKm()
	{
	        pairwiseKm = new double[numLocations][numLocations];
	        logfile.println("\nPairwise distance matrix in km");
	        for(int i = 0; i < numLocations; i++)
	        {
	            logfile.print(locationName[i]);
	            for(int j = 0; j < i; j++)
	            {
	                double Z;
	                if(Latitude[i] == Latitude[j] && Longitude[i] == Longitude[j])
	                {
	                    Z = 1;
	                } else
	                {
	                    Z = Math.sin(Latitude[i]) * Math.sin(Latitude[j]) + Math.cos(Latitude[i]) * Math.cos(Latitude[j]) * Math.cos(Longitude[j] - Longitude[i]);
	                }
	                if(Math.abs(Z) < 1)
	                {
	                    pairwiseKm[i][j] = RADIUS * Math.acos(Z);
	                } else
	                {
	                    pairwiseKm[i][j] = 0;
	                }
	                logfile.printf("%9.2f", pairwiseKm[i][j]);
	            }
	            logfile.println("");
	        }
	        logfile.println("OK");
	}

	/*********************** OBSDIS *****************************/
	/*  CALCULATE OBSERVED GEOGRAPHICAL DISTANCES FROM MIDPOINT */ 
	public static void obsDis()
	{

		clade = clad[k];

		clade.meanLatNest = 0;   // mean latitude for the nesting clade 
		clade.meanLonNest = 0;   // mean longitude for the nesting clade 
		clade.sumTotal = 0;		 // total number of observations in the nesting clade

		for(i = 0; i < clade.numSubClades; i++)
		{
			clade.meanLatitude[i]=0;
			clade.meanLongitude[i]=0;
			clade.Dc[i]=0;
			clade.Dn[i]=0;
			clade.varDc[i]=0;
			clade.varDn[i]=0;
		}

		for(i = 0; i < clade.numSubClades; i++)
		{
			clade.subCladeSum[i] = 0;
			for(j = 0; j < clade.numCladeLocations; j++)
				clade.subCladeSum[i] += clade.absFreq[i][j];	
			clade.sumTotal += clade.subCladeSum[i];
			for(j = 0; j <clade.numCladeLocations; j++)
			 	clade.relFreq[i][j] = clade.absFreq[i][j] / clade.subCladeSum[i];
		}
		
		for(i = 0; i < clade.numSubClades; i++)
			for(j = 0; j < clade.numCladeLocations; j++)
			{
				index = clade.cladeLocIndex[j]-1;
				clade.meanLatNest += clade.absFreq[i][j] / clade.sumTotal * GeoDis.Latitude[index];
				clade.meanLonNest += clade.absFreq[i][j] / clade.sumTotal * GeoDis.Longitude[index];
			}

		for(i = 0; i <clade.numSubClades; i++)
		{	
			for(j = 0; j < clade.numCladeLocations; j++)
			{
				index = clade.cladeLocIndex[j]-1;
				clade.meanLatitude[i] +=  clade.relFreq[i][j] * GeoDis.Latitude[index];
				clade.meanLongitude[i] += clade.relFreq[i][j] * GeoDis.Longitude[index];
			}
					
			for(j = 0; j < clade.numCladeLocations; j++)		
			{
				if (clade.relFreq[i][j] == 0)
					continue;
				
				index = clade.cladeLocIndex[j]-1;
				if (GeoDis.Latitude[index] == clade.meanLatitude[i] && GeoDis.Longitude[index] == clade.meanLongitude[i]) 
					Z = 1;
				else	
					Z =  Math.sin(GeoDis.Latitude[index]) * Math.sin(clade.meanLatitude[i]) + Math.cos(GeoDis.Latitude[index]) * 
				     	Math.cos(clade.meanLatitude[i]) * Math.cos(clade.meanLongitude[i] - GeoDis.Longitude[index]);

				//System.err.println("Dc " + clade.subCladeName[i] + " = " + clade.Dc[i] + "   Z = " + Z);
				
				if (Math.abs(Z) < 1)
				{
					//VZ = 111.12 * (atan(-Z / sqrt(-Z * Z + 1)) + 90);           // does not work, I do not know why 
					//VZ =  GC2K * (asin(-Z) + 90);                               // does not work, I do not know why 
					//VZ = GC2K / D2R * acos(Z);        // it works although gives slightly bigger distances 
					 VZ = RADIUS * Math.acos (Z);       // it works although gives slightly bigger distances 
					if(verbose){
						logfile.println("Dc: Subclade = " + clade.subCladeName + " in " + locationName[j] + "   distance to clade center " + clade.subCladeName[i] + " = " + VZ);
					}
					
					clade.Dc[i] += clade.relFreq[i][j] * VZ;   
					clade.varDc[i] += clade.relFreq[i][j] * Math.pow (VZ, 2);
				}				

				if (GeoDis.Latitude[index] == clade.meanLatNest && GeoDis.Longitude[index] == clade.meanLonNest) 
					ZB = 1;
				else	
					ZB = Math.sin(GeoDis.Latitude[index]) * Math.sin(clade.meanLatNest) + Math.cos(GeoDis.Latitude[index]) * 
				     Math.cos(clade.meanLatNest) * Math.cos(clade.meanLonNest - GeoDis.Longitude[index]);

				if (Math.abs(ZB) >= 1)
				{
					continue;
				}
					//VZB = 111.12 * (atan(-ZB / sqrt(-ZB * ZB + 1)) + 90);   
				VZB = RADIUS * Math.acos (ZB);
				clade.Dn[i] += clade.relFreq[i][j] * VZB ;
				if(verbose){
					logfile.println("clade.Dn[i](" + clade.Dn[i] + ")" 
					+ "+= clade.relFreq[i][j]" + "(" + clade.relFreq[i][j] + ")" 
					+ "* VZB" + "(" + VZB + ")" );
				}
				clade.varDn[i] += clade.relFreq[i][j] * Math.pow (VZB, 2);
				if(verbose){
					logfile.println("Dn: Subclade = " + clade.subCladeName + " in "
					+ locationName[j] + "    distance to nested clade center = "
					+ VZB);
				}
				//}
			}
		

		}
	
	} 

	/*********************** MATRIXDIS *****************************/
	/* Calculate Dn and Dc using pairwise distances */ 

	public static void matrixDis()
	{
		
		clade = clad[k];
		//System.err.println("\n\n" + clade.cladeName);

		for(c=0; c<clade.numSubClades; c++)
		{
			clade.Dc[c]=0;
			clade.Dn[c]=0;
			//clade.varDc[c]=0;
			//clade.varDn[c]=0;
			sum1 = sum2 = sum3 = 0;
			sumc1 = sumc2 = sumc3 = 0;

			for(i=0; i<clade.numCladeLocations; i++)  	
			{	
				
				sum2 += clade.obsMatrix[c][i] * (clade.obsMatrix[c][i]-1) / 2 ;
				sumc2 += (clade.obsMatrix[c][i] * (clade.obsMatrix[c][i]-1) / 2) + 
			         (clade.obsMatrix[c][i] * (clade.columnTotal[i] - clade.obsMatrix[c][i]));	
			
				for (j=0; j<clade.numCladeLocations; j++)
				{	
					popA = clade.cladeLocIndex[i];
					popB = clade.cladeLocIndex[j];
					
					if (j != i)
					{
						sum1 += (double) clade.obsMatrix[c][i] * clade.obsMatrix[c][j] * distance[popA-1][popB-1];
						sum3 += clade.obsMatrix[c][i] * clade.obsMatrix[c][j];			
						sumc1 += (double) clade.obsMatrix[c][i] * clade.columnTotal[j] * distance[popA-1][popB-1];	
						sumc3 += clade.obsMatrix[c][i] * clade.columnTotal[j];
						//System.err.println("\npopA = " + popA + "  popB = " + popB + "   dist= " + distance[popA-1][popB-1]);
						//System.err.println("sumc1: " + clade.obsMatrix[c][i] + " * " + clade.columnTotal[j] +" * " + distance[popA-1][popB-1] + " = " + sumc1);  
						//System.err.println("\nOBS[" + c +"][" + i+ "]= " + clade.obsMatrix[c][i]);  
						//System.err.println("\nCT[" + j +"]= " + clade.columnTotal[j]);  
					}
				}
			} 

			//System.err.println("\nOBS[" + c +"][" + i+ "]= " + clade.obsMatrix[c][i]);  
		
			
			if (sum3 == 0.0)
				clade.Dc[c] = 0.0;
			else
				clade.Dc[c] = sum1 / (sum2 + sum3);				
	
	
			if (sumc3 == 0.0)
				clade.Dn[c] = 0.0;
			else
				clade.Dn[c] = sumc1 / (sumc2 + sumc3);							
				
			//System.err.println("\nClade " + clade.cladeName + " subclade " + c + 
			//                   "  Dc= " + clade.Dc[c] + "  Dn= " + clade.Dn[c]); 		
			//System.err.println("sum1= " + sum1  + "  sum2= " + sum2 + "  sum3= " + sum3); 
			//System.err.println("sumc1= " + sumc1  + "  sumc2= " + sumc2 + "  sumc3= " + sumc3); 	
		}		

	} // method matrixDis





	/******************* ITdistances ****************************/
	/* SET UP TEST STATISTICS FOR Position VS. INTERIOR */
	public static void ITdistances()
	{
		//System.err.println("Position");

		clade = clad[k];

		clade.tipDistance =  0;
		clade.intDistance =  0;
		clade.tipDisNested = 0;
		clade.intDisNested = 0;
		clade.indTipClades = 0;
		clade.indIntClades = 0;


		if(clade.check != (double) clade.numSubClades && clade.check != 0)
		{          
			for(i = 0; i < clade.numSubClades; i++)
			{
				if (clade.Position[i] == tip)
					clade.indTipClades += clade.rowTotal[i];
				else
					clade.indIntClades += clade.rowTotal[i];			
			}
			
			for(i = 0; i < clade.numSubClades; i++)
			{
				// weigthing within class
				clade.tipDistance += clade.Position[i] * clade.Dc[i] * (double) clade.rowTotal[i] / (double) clade.indTipClades;
				clade.tipDisNested += clade.Position[i] * clade.Dn[i] * (double) clade.rowTotal[i] / (double) clade.indTipClades;
				clade.intDistance  += (1 - clade.Position[i]) * clade.Dc[i] *  (double) clade.rowTotal[i] / (double) clade.indIntClades;
				clade.intDisNested += (1 - clade.Position[i]) * clade.Dn[i] * (double) clade.rowTotal[i] / (double) clade.indIntClades;	
				
				// unweighted
				//clade.tipDistance += clade.Position[i] * clade.Dc[i] / (double) clade.check;
				//clade.tipDisNested += clade.Position[i] * clade.Dn[i] / (double) clade.check;
				//clade.intDistance  += (1 - clade.Position[i]) * clade.Dc[i] / (double) (clade.numSubClades - clade.check);
				//clade.intDisNested += (1 - clade.Position[i]) * clade.Dn[i] / (double) (clade.numSubClades - clade.check);	
			}

		   clade.tipIntDistance = clade.intDistance - clade.tipDistance;
		   clade.tipIntDisNested = clade.intDisNested - clade.tipDisNested;
		   if(verbose){
			logfile.println("\nIT " + clade.cladeName + "   indTipClades = " + clade.indTipClades
				+ "   indIntClades + " + clade.indIntClades);
			logfile.println("\nITc " + clade.cladeName + " meanInt = " + clade.intDistance
				+ "   meanTip = " + clade.tipDistance);
			logfile.println("ITn " + clade.cladeName + "   meanInt = " + clade.intDisNested
				+ "   meanTip = " + clade.tipDisNested);
		   }
		}

		//System.err.println("IT clade " + clade.cladeName + "   meanInt = " + clade.intDistance + "  meanTip = " + clade.tipDistance);

	} // method Tip




	/************************* CORRDISTOUT **********************/
	/* CALCULATE CORRELATION OF DISTANCE WITH OUTGROUP WEIGHTS  */ 
	public static void corrDistOut()	
	{	    
	
		double c, n, w;
		
		//System.err.println("CORRDIST");

		clade = clad[k];
	
	    clade.meanDc = 0;
	    clade.meanDn = 0;
	    clade.meanWeight = 0;
	    clade.sumDcxWeight = 0;
	    clade.sumDnxWeight = 0;
	    clade.sumDcSq = 0;
	    clade.sumDnSq = 0;
	    clade.sumWeightSq = 0;
		
		c = n = w = 0;
		
		for(i = 0; i < clade.numSubClades; i++)
			{
	    	clade.meanDc +=  clade.Dc[i] / (double) clade.numSubClades;
	    	clade.meanDn += clade.Dn[i] / (double) clade.numSubClades;
	    	clade.meanWeight += clade.weight[i] / (double) clade.numSubClades;
	    	clade.sumDcxWeight += clade.Dc[i] * clade.weight[i];
	    	clade.sumDnxWeight += clade.Dn[i] * clade.weight[i];
	    	clade.sumDcSq += Math.pow (clade.Dc[i],2); 
	    	clade.sumDnSq += Math.pow(clade.Dn[i],2); 
	    	clade.sumWeightSq += Math.pow(clade.weight[i], 2);
	   	 	}	
	    
	    c = clade.sumDcSq - (double) clade.numSubClades * Math.pow(clade.meanDc,2);
	    n = clade.sumDnSq - (double) clade.numSubClades * Math.pow(clade.meanDn,2);
	   	w = clade.sumWeightSq - (double) clade.numSubClades * Math.pow(clade.meanWeight,2); 

		if(clade.sumDcSq == 0 || c == 0 || w == 0)
			clade.corrDcWeights = NA; 
	    else
			clade.corrDcWeights = (clade.sumDcxWeight - (double)clade.numSubClades * clade.meanDc * clade.meanWeight) / (Math.sqrt(c*w));

		if(clade.sumDnSq == 0 || n == 0 || w == 0)
			clade.corrDnWeights = NA; 
	    else
			clade.corrDnWeights = (clade.sumDnxWeight - (double) clade.numSubClades * clade.meanDn * clade.meanWeight) / (Math.sqrt(n*w));

	      if(clade.corrDcWeights > 1 && clade.corrDcWeights < 2)
	        clade.corrDcWeights = 1;
	      
	      if(clade.corrDcWeights < -1)
	        clade.corrDcWeights = -1;
	      
	      if(clade.corrDnWeights > 1 && clade.corrDnWeights < 2)
	        clade.corrDnWeights = 1;
	      
	      if(clade.corrDnWeights < -1)
	        clade.corrDnWeights = -1;    

		//System.err.println("Correlations clade " + clade.cladeName + ": " + clade.corrDcWeights + " " + clade.corrDnWeights);

	} // method corrDistOut




/********************** PERMUTE *************************/
/* Calculates significance of test statistics by Monte Carlo */
	public static void permute()
	{
		/* INITIALIZE FOR PERMUTATION TESTING */
		//int l, r, s, T, K;
		//Random random = new Random();
		Random random = new Random(13);

		clade = clad[k];
		clade.chiPvalue = 0;

		for(int l = 0; l < 2; l++)
		{
			clade.ITcPvalue[l] = 0;
			clade.ITnPvalue[l] = 0;
			clade.corrDcWPvalue[l] = 0;
			clade.corrDnWPvalue[l] = 0;
		    
		    for(i = 0; i < clade.numSubClades; i++)
			{
				clade.DcPvalue[i][l] = 0;
				clade.DnPvalue[i][l] = 0;
			}
		}


	/*
		fprintf(stderr, "\n\n"); 
		for (i=0; i<numSubClades; i++)
		fprintf(stderr," rowTotal(%d):%d ", i, rowTotal[i]);
		fprintf(stderr, "\n"); 
		for (j=0; j<numCladeLocations; j++)
		fprintf(stderr," columnTotal(%d):%d ", j, columnTotal[j]);
		fprintf(stderr, "\n\n");
		
		fprintf(stderr,"\n\n Permuting %s", title);
		fprintf(stderr,"\n 0                                      %d permutations\n ",numPermutations);
*/		
		for (int K = 0; K < GeoDis.numPermutations; K++)
		{	
				
			progress++;
			percentage = ((double)progress * 100)/((double)numPermutations*(double)numClades); 
	
			randObsChi = 0;

			for(int l = 0; l < clade.totaNumObs; l++)
				clade.RBMatrix[l][0] = random.nextInt();    /* 0 - 32767	*/	
			            
			clade.cumColTotal = 0;                 /* cumColTotal is the cumulative column total */

			for(j = 0; j < clade.numCladeLocations; j++)
			{	
				if (j == 0)
					clade.cumColTotal = 0;
				else				
				clade.cumColTotal = clade.cumColTotal + clade.columnTotal[j-1];
				
				for (int s = 0; s < clade.columnTotal[j]; s++)
				{
					int l = s + clade.cumColTotal;
					clade.RBMatrix[l][1] = j+1;
				}
			}
		/*for (l=0; l<totaNumObs; l++)
				fprintf(stderr,"\nB: %6d %d", RBMatrix[l][0], RBMatrix[l][1]);*/



			/* * * ORDER BY RANDOM NUMBERS * * */
			for(int l = 0; l < (clade.totaNumObs - 1); l++)
			{	
				for(int r = l +1; r < clade.totaNumObs; r++)
			 	{			
					if(clade.RBMatrix[r][0] < clade.RBMatrix[l][0])
					//	continue;
						
					//else
					{	
						int T = clade.RBMatrix[r][0];
						clade.RBMatrix[r][0] = clade.RBMatrix[l][0];
						clade.RBMatrix[l][0] = T;
						T = clade.RBMatrix[r][1];
						clade.RBMatrix[r][1] = clade.RBMatrix[l][1];
						clade.RBMatrix[l][1] = T;
			 		}
			 	}
		 	}
	/*for (l=0; l<totaNumObs; l++)
			fprintf(stderr,"\nAfter: %6d %d", RBMatrix[l][0], RBMatrix[l][1]);*/


			/* * * CALCULATE RANDOM OBSERVATIONS * * */
			for(i = 0; i < clade.numSubClades; i++)
				for(j = 0; j < clade.numCladeLocations; j++)
					clade.randMatrix[i][j] = 0;

			              
			clade.cumRowTotal = 0;                /* cumulative row totals */
				
			for(i = 0; i < clade.numSubClades; i++)
			{
				if (i == 0)
				clade.cumRowTotal = 0;
				
				else
				clade.cumRowTotal += clade.rowTotal[i-1];	
				
				/*fprintf(stderr, "\n\nRow %d: rowTotal[i]:%d",i, rowTotal[i]); */
				
				for(int s = 0; s < clade.rowTotal[i]; s++)
				{
					int l = s + clade.cumRowTotal;
					index = clade.RBMatrix[l][1]-1;
					/*fprintf(stderr, "rowTotal= %d  index=%d  s=%d     ",rowTotal[i], index, s); */
					clade.randMatrix[i][index]++;
				}
			}




		/* prints the randomized table of contingency
				for(i = 0; i < numSubClades; i++)
					{
						fprintf(stderr, "\n");
						for(j = 0; j < numCladeLocations; j++)
							fprintf(stderr, "%d ", randMatrix[i][j]);
					}
		*/		


			/* * CALCULATE RANDOM CHI - SQUARE STATISTIC * */
			randObsChi = 0;
		
			for(i = 0; i < clade.numSubClades; i++)
				for(j = 0; j < clade.numCladeLocations; j++)
					randObsChi += Math.pow( (double) clade.randMatrix[i][j] - clade.expObsMatrix[i][j], 2) / clade.expObsMatrix[i][j];
					
			/*fprintf(fpout,"\n%f ", randObsChi);		*/
					
			if(randObsChi - clade.obsChi >= ROUNDING_ERROR)
				clade.chiPvalue++;


			if (doingDistances)
			{
				for(c=0; c<clade.numSubClades; c++)
				{
					clade.randDc[c]=0.0;
					clade.randDn[c]=0.0;
					//clade.varDc[c]=0;
					//clade.varDn[c]=0;
					sum1 = sum2 = sum3 = 0.0;
					sumc1 = sumc2 = sumc3 = 0.0;

					for(i=0; i<clade.numCladeLocations; i++)  	
					{	
						
						sum2 += clade.randMatrix[c][i] * (clade.randMatrix[c][i]-1) / 2 ;
						sumc2 += (clade.randMatrix[c][i] * (clade.randMatrix[c][i]-1) / 2) + 
					         (clade.randMatrix[c][i] * (clade.columnTotal[i] - clade.randMatrix[c][i]));	
					
						for (j=0; j<clade.numCladeLocations; j++)
						{	
							popA = clade.cladeLocIndex[i];
							popB = clade.cladeLocIndex[j];
							
							if (j != i)
							{
							
								sum1 += (double) clade.randMatrix[c][i] * clade.randMatrix[c][j] * distance[popA-1][popB-1];
								sum3 += clade.randMatrix[c][i] * clade.randMatrix[c][j];			
								sumc1 += (double) clade.randMatrix[c][i] * clade.columnTotal[j] * distance[popA-1][popB-1];	
								sumc3 += clade.randMatrix[c][i] * clade.columnTotal[j];
								//System.err.println("\npopA = " + popA + "  popB = " + popB + "   dist= " + distance[popA-1][popB-1]);
								//System.err.println("sumc1: " + clade.randMatrix[c][i] + " * " + clade.columnTotal[j] +" * " + distance[popA-1][popB-1] + " = " + sumc1);  
								//System.err.println("\nROBS[" + c +"][" + i+ "]= " + clade.randMatrix[c][i]);  
								//System.err.println("\nCT[" + j +"]= " + clade.columnTotal[j]);  
												
							}
						}
					} 

						//System.err.println("\nOBS[" + c +"][" + i+ "]= " + clade.obsMatrix[c][i]);  
						
					
					if (sum3 == 0)
						clade.randDc[c] = 0;
					else
						clade.randDc[c] = sum1 / (sum2 + sum3);				
			
			
					if (sumc3 == 0)
						clade.randDn[c] = 0;
					else
						clade.randDn[c] = sumc1 / (sumc2 + sumc3);							
						
					//System.err.println("\nClade " + clade.cladeName + " subclade " + c + 
	                //                   "  Dc= " + clade.Dc[c] + "  Dn= " + clade.Dn[c]); 		
		
					//System.err.println("sum1= " + sum1  + "  sum2= " + sum2 + "  sum3= " + sum3); 
					//System.err.println("sumc1= " + sumc1  + "  sumc2= " + sumc2 + "  sumc3= " + sumc3); 


					if(clade.Dc[c] - clade.randDc[c] >= ROUNDING_ERROR)
						clade.DcPvalue[c][0]++;

					if(clade.randDc[c] - clade.Dc[c] >= ROUNDING_ERROR )
						clade.DcPvalue[c][1]++;

					if(clade.Dn[c] - clade.randDn[c] >= ROUNDING_ERROR)
						clade.DnPvalue[c][0]++;

					if(clade.randDn[c] - clade.Dn[c] >= ROUNDING_ERROR)
						clade.DnPvalue[c][1]++;


			
				}			
			}
			
			else
			{
				/* * CALCULATE DISTANCE TEST STATISTIC * */

				for(i = 0; i < clade.numSubClades; i++)
				{
					clade.randMeanLatitude[i] = 0;
					clade.randMeanLongitude[i] = 0;
					clade.randDc[i] = 0;
					clade.randDn[i] = 0;
					clade.subCladeSum[i] = 0;
				
					for(j = 0; j < clade.numCladeLocations; j++)
					{
						index = clade.cladeLocIndex[j] - 1;
						clade.absFreq[i][j] = (double) clade.randMatrix[i][j] / (double) sampleSize[index];		
						clade.subCladeSum[i] = clade.subCladeSum[i] + clade.absFreq[i][j];
					}	

					for(j = 0; j < clade.numCladeLocations; j++)
						clade.relFreq[i][j] = clade.absFreq[i][j]/clade.subCladeSum[i];
				}
		/*		
				for(i = 0; i < numSubClades; i++)
				{
					fprintf(stderr,"\n");
					for(j = 0; j < numCladeLocations; j++)
						fprintf(stderr,"Asim: %f ", relFreq[i][j]);
				}
		*/
				
				for(i = 0; i < clade.numSubClades; i++)
				{	
					for(j = 0; j < clade.numCladeLocations; j++)
					{
						index = clade.cladeLocIndex[j]-1;
						clade.randMeanLatitude[i] = clade.randMeanLatitude[i] + clade.relFreq[i][j] * Latitude[index];
						clade.randMeanLongitude[i] = clade.randMeanLongitude[i] + clade.relFreq[i][j] * Longitude[index];
					}
					
					for(j = 0; j < clade.numCladeLocations; j++)
					{
						if (clade.relFreq[i][j] == 0)
							continue;

						index = clade.cladeLocIndex[j] - 1;
						if (Latitude[index] == clade.randMeanLatitude[i] && Longitude[index] == clade.randMeanLongitude[i]) 
							Z = 1;
						else
							Z = Math.sin(Latitude[index]) * Math.sin(clade.randMeanLatitude[i]) + Math.cos(Latitude[index]) * 
							Math.cos(clade.randMeanLatitude[i]) * Math.cos(clade.randMeanLongitude[i] - Longitude[index]);
					
						if (Math.abs(Z) < 1)
						{
							RVZ = RADIUS * Math.acos (Z); 
							clade.randDc[i] += clade.relFreq[i][j] * RVZ;
						}
						
						if (Latitude[index] == clade.meanLatNest && Longitude[index] == clade.meanLonNest) 
							ZB = 1;
						else
							ZB = Math.sin(Latitude[index]) * Math.sin(clade.meanLatNest) + Math.cos(Latitude[index]) * 
							Math.cos(clade.meanLatNest) * Math.cos(clade.meanLonNest - Longitude[index]);
						
						if (Math.abs(ZB) < 1)
						{
							RVZ = RADIUS * Math.acos (ZB);
							clade.randDn[i] += clade.relFreq[i][j] * RVZ;	
						}
					}

					if(clade.Dc[i] - clade.randDc[i] >= ROUNDING_ERROR)
						clade.DcPvalue[i][0]++;

					if(clade.randDc[i] - clade.Dc[i] >= ROUNDING_ERROR)
						clade.DcPvalue[i][1]++;

					if(clade.Dn[i] - clade.randDn[i] >= ROUNDING_ERROR)
						clade.DnPvalue[i][0]++;

					if(clade.randDn[i] - clade.Dn[i] >= ROUNDING_ERROR)
						clade.DnPvalue[i][1]++;
				}
			}


			/* * TEST STATISTICS FOR POSITION VS. INTERIOR * */

			randTipDistance = 0;
			randIntDistance = 0;
			randTipDisNested = 0;
			randIntDisNested = 0;
			
			if(clade.check != (double) clade.numSubClades && clade.check != 0)
			{	
				for(i = 0; i < clade.numSubClades; i++)
				{
					randTipDistance += clade.Position[i] * clade.randDc[i]  * (double) clade.rowTotal[i] / (double) clade.indTipClades;
					randTipDisNested += clade.Position[i] *  clade.randDn[i]  * (double) clade.rowTotal[i] / (double) clade.indTipClades;
					randIntDistance += (1 - clade.Position[i]) *  clade.randDc[i]  * (double) clade.rowTotal[i] / (double) clade.indIntClades;
					randIntDisNested += (1 - clade.Position[i]) * clade.randDn[i]  * (double) clade.rowTotal[i] / (double) clade.indIntClades;
				}
								
				if(clade.tipIntDistance - (randIntDistance - randTipDistance) >= ROUNDING_ERROR)
					clade.ITcPvalue[0]++;

				if((randIntDistance - randTipDistance) - clade.tipIntDistance >= ROUNDING_ERROR)
				 	clade.ITcPvalue[1]++;

				if(clade.tipIntDisNested - (randIntDisNested - randTipDisNested)  >= ROUNDING_ERROR)
					clade.ITnPvalue[0]++;
				
				if((randIntDisNested - randTipDisNested) - clade.tipIntDisNested >= ROUNDING_ERROR)
					clade.ITnPvalue[1]++;
				
			}

			if(!weights){
				continue;
			}
			//{
				/* * CORRELATION TESTS OF DISTANCE WITH OUTGROUP WEIGHTS * */

				 //double c, n, w;
				 
				 clade.meanDc = 0;
				 clade.meanDn = 0;
				 clade.sumDcxWeight = 0;
				 clade.sumDnxWeight = 0;
				 clade.sumDcSq = 0;
				 clade.sumDnSq = 0;

				for(i = 0; i < clade.numSubClades; i++)
					{
				 	clade.meanDc += clade.randDc[i] / (double) clade.numSubClades;
				 	clade.meanDn += clade.randDn[i] / (double) clade.numSubClades;
				 	clade.sumDcxWeight += clade.randDc[i] * clade.weight[i];
				 	clade.sumDnxWeight += clade.randDn[i] * clade.weight[i];
				 	clade.sumDcSq += Math.pow(clade.randDc[i],2); 
				 	clade.sumDnSq += Math.pow(clade.randDn[i],2);
					}
				
	    		double c = clade.sumDcSq - (double) clade.numSubClades * Math.pow(clade.meanDc,2);
	    		double n = clade.sumDnSq - (double) clade.numSubClades * Math.pow(clade.meanDn,2);
	   			double w = clade.sumWeightSq - (double) clade.numSubClades * Math.pow(clade.meanWeight,2); 

				if (clade.corrDcWeights != NA && c > 0 && w > 0)
					//;
				//else
				 	{
				 	clade.randCorrDcWeights = (clade.sumDcxWeight - (double) clade.numSubClades * clade.meanDc * clade.meanWeight)/ 
				 	(Math.sqrt(c*w));

					if(clade.randCorrDcWeights > 1)
							clade.randCorrDnWeights = 1;
					
					if(clade.randCorrDcWeights < -1)
			    		clade.randCorrDcWeights = -1;
				
					if(clade.corrDcWeights - clade.randCorrDcWeights >= ROUNDING_ERROR)
				   		clade.corrDcWPvalue[0]++;
					    
					if(clade.randCorrDcWeights - clade.corrDcWeights >= ROUNDING_ERROR)
				    	clade.corrDcWPvalue[1]++;
					}


				if (clade.corrDnWeights == NA || n <= 0 || w <= 0)
					continue;		
				//else
					//{
				 	clade.randCorrDnWeights = (clade.sumDnxWeight - (double) clade.numSubClades * clade.meanDn * clade.meanWeight)/ 
				 	(Math.sqrt(n*w));
				
					if(clade.randCorrDnWeights > 1)
						clade.randCorrDnWeights = 1;

					if(clade.randCorrDnWeights < -1)
						clade.randCorrDnWeights = -1;

					if(clade.corrDnWeights - clade.randCorrDnWeights >= ROUNDING_ERROR)
						clade.corrDnWPvalue[0]++;

					if(clade.randCorrDnWeights - clade.corrDnWeights >= ROUNDING_ERROR)
						clade.corrDnWPvalue[1]++;
					//}
				//}
	} // end of 1 replicate


		clade.chiPvalue /= (double) GeoDis.numPermutations;

		for(int l = 0; l < 2; l++)
		{
			clade.ITcPvalue[l] /=  (double) GeoDis.numPermutations;
			clade.ITnPvalue[l] /= (double) GeoDis.numPermutations;
			clade.corrDcWPvalue[l] /= (double) GeoDis.numPermutations;
			clade.corrDnWPvalue[l] /= (double) GeoDis.numPermutations;

			for (i =0; i < clade.numSubClades; i++)
			{
			    clade.DcPvalue[i][l] /= (double) GeoDis.numPermutations;
			    clade.DnPvalue[i][l] /= (double) GeoDis.numPermutations;
			}
		}

	} // method permute




	/******************* readLocations ****************************/
	/* Reads the location of the populations given by lat and lon */

	public static void readLocations() throws ProgressionException
	{
		//File file = GeoDis_GUI.inputfile;
		//infilename = file.getPath();
		//System.err.println(file.getPath());
		//infile = new TextInputStream(file.getPath());
		logfile.print("\nReading geographical locations ...");
		infile = new TextInputStream(GeoDis.infilename);
		
		dataName = infile.readLine();  
		//System.err.println (dataName);
      	
		numLocations = infile.readInt();     
		//System.err.println(numLocations);
	
	
		locationName = new String[numLocations];     
		sampleSize = new int[numLocations];         
		Latitude = new double[numLocations];      
		Longitude = new double[numLocations];      
		
		for(j = 0; j < numLocations; j++)
		{
			//trash = infile.readLine();   // read the \n character in PC's
			
			locationName[j] = infile.readLine(); 

			if (locationName[j].length() == 0)			
				locationName[j] = infile.readLine(); 
			
			sampleSize[j] = infile.readInt();


			if (usingDecimalDegrees) 
			{
				Latitude[j] = infile.readFloat();
				Longitude[j] = infile.readFloat();
			}	

			else
			{
				lat_deg = infile.readInt();
				lat_min = infile.readInt();
				lat_sec = infile.readInt();
				lat_coor = infile.readWord();
				lon_deg = infile.readInt();
				lon_min = infile.readInt();				
				lon_sec = infile.readInt();
				lon_coor = infile.readWord();
			
				//System.err.println (sampleSize[j] + " " + lat_deg + " " + lat_min + " " + lat_sec + " " + lat_coor + " " + 
				                      //lon_deg + " " + lon_min + " " + lon_sec + " " + lon_coor);
				
				dec_lat = (double)lat_deg + ((double)lat_min/60.0) + ((double)lat_sec/3600.0);
				dec_lon = (double)lon_deg + ((double)lon_min/60.0) + ((double)lon_sec/3600.0);
				

				if (lat_coor.equalsIgnoreCase("N"))
					Latitude[j] = dec_lat;
				else if (lat_coor.equalsIgnoreCase("S"))
					Latitude[j] = -1.0 * dec_lat; 
				else
				{
					StringBuffer str = new StringBuffer();
					str.append("The letter for the latitude is neither N nor S.");
					str.append("\nNote that latitude should come before longitude.");
					if(!Mediator.isCommandLineApp()){
						JOptionPane.showMessageDialog(frame,"The letter for the latitude is neither N nor S." + 
		           			"\n Note that latitude should come before longitude",
                   			"GeoDis warning", JOptionPane.WARNING_MESSAGE);
				 		//frame.dispose();				
						//System.exit(0);
						throw new ProgressionException(str.toString());
					} else {
						str.append("[GeoDis]\n");
						Mediator.writeError(str.toString());
					}
				}
				
				

				if (lon_coor.equalsIgnoreCase("E"))
					Longitude[j] = dec_lon;
				else if (lon_coor.equalsIgnoreCase("W"))
					Longitude[j] = -1.0 *  dec_lon;
				else
				{
					StringBuffer str = new StringBuffer();
					str.append("The letter for the longitude is neither E nor W.");
					str.append("\nNote that latitude should come before longitude.");
					if(!Mediator.isCommandLineApp()){
						JOptionPane.showMessageDialog(frame,"The letter for the longitude is neither E nor W." + 
		           			"\n See that latitude come first than longitude",
                   			"GeoDis warning", JOptionPane.WARNING_MESSAGE);
				 		//frame.dispose();				
						//System.exit(0);
						throw new ProgressionException(str.toString());
					} else {
						str.append("[GeoDis]\n");
						Mediator.writeError(str.toString());
					}
				}
			}
			
			if (locationName[i] == null || sampleSize[i] <= 0 || Latitude[j] > 90 || Latitude[j]< -90 || Longitude[j] < -180 || Longitude[j] > 180) 
			{
				
				if (locationName[i] == null)
					if(!Mediator.isCommandLineApp()){
						System.err.println ("\nlocationName [i] == null");
					}
				
				if (sampleSize[i] <= 0)
					if(!Mediator.isCommandLineApp()){
						System.err.println ("\nsampleSize[i] <= 0");
					}

				if (Latitude[j] > 90)
					if(!Mediator.isCommandLineApp()){
						System.err.println ("\nLatitude[j] > 90");
					}

				if (Latitude[j]< -90)
					if(!Mediator.isCommandLineApp()){
						System.err.println ("\nLatitude[j]< -90");
					}

				if (Longitude[j] < -180)
					if(!Mediator.isCommandLineApp()){
						System.err.println ("\nLongitude[j] < -180");
					}

				if (Longitude[j] > 180)
					if(!Mediator.isCommandLineApp()){
						System.err.println ("\nLongitude[j] > 180");
					}

				StringBuffer str = new StringBuffer();
				str.append("There was an error reading the population description");
				str.append("\nor its coordinates. If your coordinates are in decimal degrees, have you checked that box?");
				if(!Mediator.isCommandLineApp()){
					JOptionPane.showMessageDialog(frame,"There was an error reading the population description" +
						"\nor its coordinates. If your coordinates are in decimal degrees, have you checked that box?",
                        "GeoDis warning", JOptionPane.WARNING_MESSAGE);
					//frame.dispose();				
					//System.exit(0);
					throw new ProgressionException(str.toString());
				} else {
					str.append("[GeoDis]\n");
					Mediator.writeError(str.toString());
				}
			}
			logfile.print("\nreading " + locationName[j] + " " + sampleSize[j] + " ");
			logfile.printf(" %6.4f",Latitude[j]);
			logfile.printf(" %6.4f",Longitude[j]);
			//System.err.println (locationName[j]);
			//System.err.println(sampleSize[j] + " " + Latitude[j] + " " + Longitude[j]);			
		}

	 	/* TRANSFORM DEGREES TO RADIANS */
		for(j = 0; j < numLocations; j++)
		{
			Latitude[j] *= D2R;
			Longitude[j] *= D2R;
		} 
		logfile.println("\nOK");

 	} // method readLocations


	/********************* readmatrix ***********************/
	/* READS A MATRIX OF POPULATIONS DISTANCES 
	(E.cladeLocIndex. RIVER, COAST) DEFINED BY THE USER */

	public static void readMatrix() throws ProgressionException
	{	
		//System.out.println("Reading Matrix");
		//File file = GeoDis_GUI.inputfile;
		//infilename = file.getPath();
		//System.err.println(file.getPath());
		//infile = new TextInputStream(file.getPath());
		logfile.print("\nReading distance matrix ...");
		infile = new TextInputStream(GeoDis.infilename);

		dataName = infile.readLine();  
		//System.err.println (dataName);


		numLocations = infile.readInt();               // number of locations = dimensions of the distance matrix
		distance = new double[numLocations][numLocations];
		locationIndex = new int[numLocations];                // locationIndex is the index of the population
		locationName = new String[numLocations];               // locationName is the name of each location
		sampleSize = new int[numLocations];                    // sampleSize is the sample size

		for (popA=0; popA<numLocations; popA++)
		{
			locationIndex[popA] = infile.readInt();
			locationName[popA] = infile.readWord();
			sampleSize[popA] = infile.readInt();

			if (locationIndex[popA] <= 0 || locationName[popA] == null || sampleSize[popA] < 0)
			{
				StringBuffer str = new StringBuffer();
				str.append("There was an error reading the population descriptions");
				str.append("\nin the pairwise distance matrix");
				if(!Mediator.isCommandLineApp()){
					JOptionPane.showMessageDialog(frame,"There was an error reading the population descriptions" +
						"\nin the pairwise distance matrix",
                        "GeoDis warning", JOptionPane.WARNING_MESSAGE);
					//frame.dispose();				
					//System.exit(0);
					throw new ProgressionException(str.toString());
				} else {
					str.append("[GeoDis]\n");
					Mediator.writeError(str.toString());
				}
			}
			
			for (popB=0; popB<popA; popB++) 
			{
				distance[popA][popB] = distance[popB][popA] = infile.readDouble();
				

				if (distance[popA][popB] < 0)
				{
					if(!Mediator.isCommandLineApp()){
						JOptionPane.showMessageDialog(frame,"There was an error reading the distance matrix",
	                    	      "GeoDis warning", JOptionPane.WARNING_MESSAGE);
						//frame.dispose();				
						//System.exit(0);
						throw new ProgressionException("There was an error reading the distance matrix");
					} else {
						Mediator.writeError("There was an error reading the distance matrix.[GeoDis]\n");
					}
				}

		    }
		}
		logfile.println("OK");
/*
		for (popA=0; popA<numLocations; popA++)
		{
			System.out.print(locationIndex[popA] + " " + locationName[popA] + " " + sampleSize[popA] + "   ");

			for (popB=0; popB<=popA; popB++) 
			{
				System.out.print(distance[popA][popB] + "  ");
			}

			System.out.println("");
		}
*/
	
	} // method readMatrix



	/******************* readClade ****************************/
	/* Reads the information for each clade */

	public static void readClade() throws ProgressionException
	{
		try{
		numClades = infile.readInt(); 
		logfile.println("\nReading " + numClades + " clades ...");                         
		//System.err.println (numClades);

		if(numClades <= 0)
		{
			StringBuffer str = new StringBuffer();
			str.append("There was a mistake reading the clades.");
			str.append("\nAre you inputting a matrix? If so make sure you check the box");
			if(!Mediator.isCommandLineApp()){
				JOptionPane.showMessageDialog(frame,"There is a mistake reading the clades." +
				"\nAre you inputting a matrix? If so, make sure you check the box",
                        "GeoDis warning", JOptionPane.WARNING_MESSAGE);		
				//frame.dispose();				
				//System.exit(0);
				throw new ProgressionException(str.toString());
			} else {
				str.append("[GeoDis]\n");
				Mediator.writeError(str.toString());
			}
		}
			

		clad = new Clade[numClades];	                         

		for(k=0; k<numClades; k++)
		{
			clad[k] = new Clade();
			currentCladeName = clad[k].cladeName;
			if(infile.EOF())
				{
					if(!Mediator.isCommandLineApp()){
						JOptionPane.showMessageDialog(frame,"There are less clades than indicated.",
                                "GeoDis warning", JOptionPane.WARNING_MESSAGE);		
						//frame.dispose();				
						//System.exit(0);
						throw new ProgressionException("There are less clades than indicated.");
					} else {
						Mediator.writeError("There are less clades than indicated.[GeoDis]\n");
					}
				}
			
			//trash = infile.readLine();   // read the \n character in PC's
			
			clad[k].cladeName = infile.readLine();           
						
		
			if (clad[k].cladeName.length() == 0) // if PC file
					clad[k].cladeName = infile.readLine();           
	
			logfile.println("   Reading clade \"" + clad[k].cladeName + "\" ... ");

			if (clad[k].cladeName == null)
			{
				if(!Mediator.isCommandLineApp()){
					JOptionPane.showMessageDialog(frame,"There was an error reading the clade name.",
                            "GeoDis warning", JOptionPane.WARNING_MESSAGE);

					//frame.dispose();				
					//System.exit(0);
					throw new ProgressionException("There was an error reading the clade name");
				} else {
					Mediator.writeError("There was an error reading the clade name.[GeoDis]\n");
				}
			}
		
	
			//System.err.print ("Reading " + clad[k].cladeName + " ... ");
			
			clad[k].numSubClades = infile.readInt();                   // number of haplotypes in the clade
	
			if (clad[k].numSubClades <= 0)
			{
				StringBuffer str = new StringBuffer();
				str.append("There was an error reading the number of haplotypes (rows)");
				str.append("\nin clade: ");
				str.append(clad[k].cladeName);
				if(!Mediator.isCommandLineApp()){
					JOptionPane.showMessageDialog(frame,"There was an error reading the number of haplotypes (rows) " +
				            "\nin clade: " + clad[k].cladeName,
                            "GeoDis warning", JOptionPane.WARNING_MESSAGE);
					//frame.dispose();				
					//System.exit(0);
					throw new ProgressionException(str.toString());
				} else {
					str.append("[GeoDis]\n");
					Mediator.writeError(str.toString());
				}
			}

			
			if (clad[k].numSubClades == 1)
			{
				StringBuffer str = new StringBuffer();
				str.append("Number of haplotypes in clade ");
				str.append(clad[k].cladeName);
				str.append("\ncannot be one. You need genetic variation for this analysis.");
				if(!Mediator.isCommandLineApp()){
					JOptionPane.showMessageDialog(frame,"Number of haplotypes in clade " + clad[k].cladeName + 
				           "\n cannot be one. You need genetic variation for this analysis.",
	                       "GeoDis warning", JOptionPane.WARNING_MESSAGE);
			 		//frame.dispose();				
					//System.exit(0);
					throw new ProgressionException(str.toString());
				} else {
					str.append("[GeoDis]\n");
					Mediator.writeError(str.toString());
				}
			}



			//System.err.println (clad[k].numSubClades);

			clad[k].rowTotal        = new int[clad[k].numSubClades];        // row total
			clad[k].meanLatitude      = new double[clad[k].numSubClades];     // mean latitude
			clad[k].meanLongitude      = new double[clad[k].numSubClades];     // mean longitude
			clad[k].randMeanLatitude      = new double[clad[k].numSubClades];     // rand mean latitude
			clad[k].randMeanLongitude      = new double[clad[k].numSubClades];     // radn mean longitude
			clad[k].Dc       = new double[clad[k].numSubClades];     // average distance of clade members from the geocenter of 
			                                                  //  their clade 
			clad[k].Dn      = new double[clad[k].numSubClades];     // average distance of clade members from the geocenter of
	   	                                                      //  their nesting clade but averaged over their clade membership
			clad[k].varDc      = new double[clad[k].numSubClades];     // average variance of the distance of clade members from the geocenter
			clad[k].varDn     = new double[clad[k].numSubClades];     // 
			clad[k].Position       = new double[clad[k].numSubClades];     // tip (1) or interior (0)
			clad[k].randDc      = new double[clad[k].numSubClades];     // 
			clad[k].randDn     = new double[clad[k].numSubClades];     // 
			clad[k].subCladeSum       = new double[clad[k].numSubClades];     // 
			clad[k].ITcPvalue   = new double[ 2];              // 
			clad[k].ITnPvalue  = new double[ 2];              // 
			clad[k].corrDcWPvalue     = new double[ 2];              // 
			clad[k].corrDnWPvalue    = new double[ 2];              //  
			clad[k].subCladeName    = new String[clad[k].numSubClades];     // name of each interior clade 
			clad[k].subCladePosition    = new String[clad[k].numSubClades];     // "Tip" or "Interior"
			clad[k].weight     = new double[clad[k].numSubClades];     // outgroup weight

		
			for(i = 0; i < clad[k].numSubClades; i++)
			{
					
				clad[k].subCladeName[i] = infile.readWord();

				if (clad[k].subCladeName[i] == null)
				{
					StringBuffer str = new StringBuffer();
					str.append("There was an error reading the name of the inner clades in clade: ");
					str.append(clad[k].cladeName);
					if(!Mediator.isCommandLineApp()){
						JOptionPane.showMessageDialog(frame,"There was an error reading the name of the inner clades " +
					           "in clade: " + clad[k].cladeName,
	                           "GeoDis warning", JOptionPane.WARNING_MESSAGE);
						//frame.dispose();				
						//System.exit(0);
						throw new ProgressionException(str.toString());
					} else {
						str.append("[GeoDis]\n");
						Mediator.writeError(str.toString());
					}
				}

				//System.err.print (clad[k].subCladeName[i] + " ");
			}

			//System.err.println("");
			
			clad[k].check=0;

			for(i = 0; i < clad[k].numSubClades; i++)
			{
				clad[k].Position[i] = infile.readInt();

				if (clad[k].Position[i] < 0)
				{
					StringBuffer str = new StringBuffer();
					str.append("There was an error reading the tip/interior designation in clade: ");
					str.append(clad[k].cladeName);
					if(!Mediator.isCommandLineApp()){
						JOptionPane.showMessageDialog(frame,"There was an error reading the tip/interior designation " +
					           "in clade: " + clad[k].cladeName,
	                           "GeoDis warning", JOptionPane.WARNING_MESSAGE);
						//frame.dispose();				
						//System.exit(0);
						throw new ProgressionException(str.toString());
					} else {
						str.append("[GeoDis]\n");
						Mediator.writeError(str.toString());
					}
				}

				
				if (clad[k].Position[i] == tip)
					clad[k].subCladePosition[i] = "Tip";        
				
				else
					clad[k].subCladePosition[i] = "Interior";        
				
				clad[k].check += clad[k].Position[i];
				
				//System.err.print (clad[k].Position[i] + " ");
			}
		
			//System.err.println("");


		 	// if there are outgroup weights
			 if(weights)
			{
				//System.err.println("\nReading weights\n");
	
				for(i = 0; i < clad[k].numSubClades; i++)
			 		{
			 			clad[k].weight[i] = infile.readDouble();

						//if(Double.toString(clad[k].weight[i]) == "\n")
							//clad[k].weight[i] = infile.readDouble();
						
						if (clad[k].weight[i] <= 0)
						{
							StringBuffer str = new StringBuffer();
							str.append("There was an error reading \nthe outweights in clade: ");
							str.append(clad[k].cladeName);
							if(!Mediator.isCommandLineApp()){
								JOptionPane.showMessageDialog(frame,"There was an error reading \nthe outweights " +
							           "in clade: " + clad[k].cladeName,
			                           "GeoDis warning", JOptionPane.WARNING_MESSAGE);
								//frame.dispose();				
								//System.exit(0);
								throw new ProgressionException(str.toString());
							} else {
								str.append("[GeoDis]\n");
								Mediator.writeError(str.toString());
							}
						}

						//System.err.print (clad[k].weight[i] + " ");			
					}
			
			//System.err.println("");

			}
		
			clad[k].numCladeLocations = infile.readInt();         // number of columns (geographical locations) 

			if (clad[k].numCladeLocations <= 0)
			{
				StringBuffer str = new StringBuffer();
				str.append("There was an error reading the number of \ngeographical locations (columns) in clade: ");
				str.append(clad[k].cladeName);
				str.append("\nCould be that you have outgroup probabilities \nand forgot to check the box?");
				if(!Mediator.isCommandLineApp()){
					JOptionPane.showMessageDialog(frame,"There was an error reading the number of \ngeographical locations "+
				           "(columns) in clade: " + clad[k].cladeName,
                           "GeoDis warning", JOptionPane.WARNING_MESSAGE);
				
					JOptionPane.showMessageDialog(frame,"Could be that you have outgroup probabilities \nand forgot to check the box? ",
				           "GeoDis warning", JOptionPane.WARNING_MESSAGE);

				
					//frame.dispose();				
					//System.exit(0);
					throw new ProgressionException(str.toString());
				} else {
					str.append("[GeoDis]\n");
					Mediator.writeError(str.toString());
				}
			}

			if (clad[k].numCladeLocations == 1)
			{
				StringBuffer str = new StringBuffer();
				str.append("Number of geographical locations (columns) in clade ");
				str.append(clad[k].cladeName);
				str.append(" cannot be one. You need geographical variation for this analysis.");
				if(!Mediator.isCommandLineApp()){
					JOptionPane.showMessageDialog(frame,"Number of geographical locations (columns) in clade " + clad[k].cladeName +
				           " cannot be one. You need geographical variation for this analysis.",
                           "GeoDis warning", JOptionPane.WARNING_MESSAGE);
					//frame.dispose();				
					//System.exit(0);
					throw new ProgressionException(str.toString());
				} else {
					str.append("[GeoDis]\n");
					Mediator.writeError(str.toString());
				}
			}


			//System.err.println(clad[k].numCladeLocations);

			clad[k].columnTotal = new int[clad[k].numCladeLocations];     // column totals
			clad[k].cladeLocIndex  = new int[clad[k].numCladeLocations];     // indices of geographical locations for this clade

			for(i = 0; i<clad[k].numCladeLocations; i++)
			{
			 	clad[k].cladeLocIndex[i] = infile.readInt();

				if (clad[k].cladeLocIndex[i] <= 0 || clad[k].cladeLocIndex[i] > 1000)
				{
					StringBuffer str = new StringBuffer();
					str.append("There was a mistake reading the location indexes in clade: ");
					str.append(clad[k].cladeName);
					if(!Mediator.isCommandLineApp()){
						JOptionPane.showMessageDialog(frame,"There was a mistake reading the location indexes " + 
					                    "in clade: " + clad[k].cladeName,
	                                    "GeoDis warning", JOptionPane.WARNING_MESSAGE);
						//frame.dispose();				
						//System.exit(0);
						throw new ProgressionException(str.toString());
					} else {
						str.append("[GeoDis]\n");
						Mediator.writeError(str.toString());
					}
				}
				
				//System.err.print ("***"+clad[k].cladeLocIndex[i]);			
			}
			
			clad[k].DcPvalue = new double [clad[k].numSubClades][2];      // clade distance
			clad[k].DnPvalue = new double[clad[k].numSubClades][2];      // nested clade distance
			clad[k].obsMatrix = new int[clad[k].numSubClades][clad[k].numCladeLocations];         // observed number of observations 
			clad[k].randMatrix = new int[clad[k].numSubClades][clad[k].numCladeLocations];        // random number of observations
			clad[k].expObsMatrix = new double[clad[k].numSubClades][clad[k].numCladeLocations];       // expected number of obervations 
			clad[k].relFreq = new double[clad[k].numSubClades][clad[k].numCladeLocations];        // relative frequency of clade i at location j 
			clad[k].absFreq = new double[clad[k].numSubClades][clad[k].numCladeLocations];        // absolute frequency of clade i at location j 
			clad[k].RBMatrix = new int[10000][2];            // the random MX2 matrix described in Roff and Bentzen 


			for(i = 0; i < clad[k].numSubClades; i++)
				for(j = 0; j < clad[k].numCladeLocations; j++)	 
				{
					clad[k].obsMatrix[i][j] = 0;
	   				clad[k].absFreq[i][j] = 0;
				}

			clad[k].totaNumObs = 0;                              // number of observations

			for(i = 0; i < clad[k].numSubClades; i++)
				{
				//System.err.println("");
				for(j = 0; j < clad[k].numCladeLocations; j++)
				{
					int index = clad[k].cladeLocIndex[j]-1;
					clad[k].obsMatrix[i][j] = infile.readInt();

					if (clad[k].obsMatrix[i][j] < 0)
					{
						StringBuffer str = new StringBuffer();
						str.append("There was a mistake reading the observation matrix in clade: ");
						str.append(clad[k].cladeName);
						if(!Mediator.isCommandLineApp()){
							JOptionPane.showMessageDialog(frame,"There was a mistake reading the observation matrix " + 
						                    "in clade: " + clad[k].cladeName,
		                                    "GeoDis warning", JOptionPane.WARNING_MESSAGE);
							//frame.dispose();				
							//System.exit(0);
							throw new ProgressionException(str.toString());
						} else {
							str.append("[GeoDis]\n");
							Mediator.writeError(str.toString());
						}
					}

					clad[k].rowTotal[i] +=  clad[k].obsMatrix[i][j];
					clad[k].columnTotal[j] +=  clad[k].obsMatrix[i][j];
					clad[k].totaNumObs += clad[k].obsMatrix[i][j];
					clad[k].absFreq[i][j] = (double) clad[k].obsMatrix[i][j] / (double) sampleSize[index];
				 
					//System.err.print(clad[k].obsMatrix[i][j] + "/" + sampleSize[index] + "=" + clad[k].absFreq[i][j] + "   ");		
				}
		
			}	
			
		/*	for(i = 0; i < clad[k].numSubClades; i++)
			{
				//System.out.println("");

				//for(j = 0; j < clad[k].numCladeLocations; j++)
					//System.out.print(clad[k].obsMatrix[i][j] + " ");
				
			}
		*/
			//System.err.println("");
			//System.err.println("OK");
			
	} // read 1 clade loop
	
		if(infile.EOF())
			{
				StringBuffer str = new StringBuffer();
				str.append("There are more clades than indicated.[GeoDis]\n");
				if(!Mediator.isCommandLineApp()){
					JOptionPane.showMessageDialog(frame,"There are more clades than indicated.",
                            "GeoDis warning", JOptionPane.WARNING_MESSAGE);		
					//frame.dispose();				
					//System.exit(0);
					throw new ProgressionException(str.toString());
				} else {
					Mediator.writeError(str.toString());
				}
			}
		
		infile.close();
		logfile.println("OK");
		} catch (ArrayIndexOutOfBoundsException abe){
				StringBuffer str = new StringBuffer();
				str.append("There are more sampled locations than indicated.[GeoDis]\n");
				if(!Mediator.isCommandLineApp()){
					JOptionPane.showMessageDialog(frame,"There are more sampled locations than indicated.",
                            "GeoDis warning", JOptionPane.WARNING_MESSAGE);		
					//frame.dispose();				
					//System.exit(0);
					throw new ProgressionException(str.toString());
				} else {
					Mediator.writeError(str.toString());
				}
		}

		//System.err.println("\nDONE READING");
	
	} // method readClade






	/********************* PRINTOUT ***********************/
	/* PRINT RESULTS TO OUTPUT */

	public static void printOut()
	{
		//int dms[];
		//int deg, min, sec;
		//double	dd;
		time = new Date();	

//		if (GeoDis_GUI.outputfile == null)
		if (!printToFile)
		{		
			GeoDis_GUI.outWindow();
			outfile = new TextOutputStream(GeoDis_GUI.outdoc);
			//outfile = Console.out;
		}
		else
		{
			//File outputfile = GeoDis_GUI.outputfile;
			//outfilename = GeoDis_GUI.outputfile.getPath();
			outfile = new TextOutputStream(GeoDis.outfilename);
			if(GeoDis.outfilename.endsWith(".gdout")){
				Mediator.setGeoDisOutputFile(GeoDis.outfilename);
			} else {
				// Unsafe assumption
				Mediator.setGeoDisOutputFile_MatrixFormat(GeoDis.outfilename);
			}
		}	

		outfile.print("Differentiating population structure from history - " + PROGRAM_NAME);
		outfile.print("  " + VERSION_NUMBER);
		outfile.print("\n(c) Copyright, 1999-2006 David Posada and Alan Templeton");
		outfile.print("\nContact: David Posada, University of Vigo, Spain (dposada@uvigo.es)");
		//outfile.print("\nUniversity of Vigo");
		//outfile.print("\ndposada@uvigo.es");
		outfile.print("\n________________________________________________________________________\n\n");
		outfile.print("Input file: " + infilename);
		outfile.print(dataName);
		outfile.print("\n\n" + time.toString());

		if (doingDistances)
		outfile.print("\n\nCalculations from USER-defined distances");


		for(k=0; k<numClades; k++)
		{

			outfile.print("\n\n\n\n\nPERMUTATION ANALYSIS OF " + clad[k].cladeName);
			outfile.print("\n   BASED ON " + GeoDis.numPermutations + " RESAMPLES");
			outfile.println("\n\n\nPART I.  PERMUTATIONAL CONTINGENCY TEST:");

			if(clad[k].numSubClades != 1)             
			{
				outfile.print("\n\n  OBSERVED CHI-SQUARE STATISTIC = ");
				outfile.printf("%10.4f",clad[k].obsChi);
				outfile.print("\n\n  THE PROBABILITY OF A RANDOM CHI-SQUARE BEING GREATER THAN");
				outfile.print("\n    OR EQUAL TO THE OBSERVED CHI-SQUARE = ");
				outfile.printf("%10.4f",clad[k].chiPvalue);
			}		

			else
				outfile.println("\nNO. OF CLADES = 1, CHI-SQUARE N.A.");
			                 

			outfile.println("\n\n\nPART II.  GEOGRAPHIC DISTANCE ANALYSIS:");

			// print geographical centers
			
	
			if (usingDecimalDegrees)
			{
				outfile.print("\nGEOGRAPHICAL CENTERS    LATITUDE    LONGITUDE");
				outfile.printf("\n%16s", clad[k].cladeName);
				outfile.printf("%16.4f", clad[k].meanLatNest / D2R);
				outfile.printf("%16.4f", clad[k].meanLonNest / D2R);
				for(i = 0; i < clad[k].numSubClades; i++)
				{
					outfile.printf("\n%16s", clad[k].subCladeName[i]);
					outfile.printf("%16.4f", clad[k].meanLatitude[i] / D2R);
					outfile.printf("%16.4f", clad[k].meanLongitude[i] / D2R);
				}
			}
			else if (!doingDistances)
			{
				outfile.print("\nGEOGRAPHICAL CENTERS    LATITUDE      LONGITUDE");
				outfile.printf("\n%16s", clad[k].cladeName);

				int dms[] =  DDtoDMS (clad[k].meanLatNest / D2R);
				outfile.printf("      % 4d"+ " ", dms[0]);
				outfile.printf("%02d" + "'", dms[1]);
				outfile.printf("%02d" +"\"", dms[2]);
				/*outfile.printf("(%6.4f)", clad[k].meanLatNest / D2R);*/

				dms =  DDtoDMS (clad[k].meanLonNest / D2R);
				outfile.printf("   % 4d"+ " ", dms[0]);
				outfile.printf("%02d" + "'", dms[1]);
				outfile.printf("%02d" +"\"", dms[2]);
				/*outfile.printf("(%6.4f)", clad[k].meanLonNest  / D2R);*/

				for(i = 0; i < clad[k].numSubClades; i++)
				{
					outfile.printf("\n%16s", clad[k].subCladeName[i]);
		
					dms =  DDtoDMS (clad[k].meanLatitude[i] / D2R);
					outfile.printf("      % 4d"+ " ", dms[0]);
					outfile.printf("%02d" + "'", dms[1]);
					outfile.printf("%02d" +"\"", dms[2]);
					/* outfile.printf("(%6.4f)", clad[k].meanLatitude[i]  / D2R);*/

					dms =  DDtoDMS (clad[k].meanLongitude[i] / D2R);
					outfile.printf("   % 4d"+ " ", dms[0]);
					outfile.printf("%02d" + "'", dms[1]);
					outfile.printf("%02d" +"\"", dms[2]);
					/*outfile.printf("(%6.4f)", clad[k].meanLongitude[i]  / D2R);*/
				}
			}

			for(i = 0; i < clad[k].numSubClades; i++)
			{

				outfile.print("\n\nCLADE " + clad[k].subCladeName[i] + " (" + clad[k].subCladePosition[i] + ")");						
				outfile.print("\n TYPE OF DISTANCE       DISTANCE      PROB.<=      PROB.>=");

				if(clad[k].numSubClades == 1)
				{
					outfile.print("\n      WITHIN CLADE  ");
					outfile.printf("%12.4f",clad[k].Dc[i]);
					outfile.print("N.A      N.A");
					outfile.print("\n      NESTED CLADE  ");
					outfile.printf("%12.4f",clad[k].Dn[i]);
					outfile.print("N.A      N.A");
				}	
				else
				{	
					outfile.print("\n      WITHIN CLADE  ");
					outfile.printf("%12.4f",clad[k].Dc[i]);
					outfile.print("   ");
					outfile.printf("%10.4f",clad[k].DcPvalue[i][0]);
					outfile.print("   ");
					outfile.printf("%10.4f",clad[k].DcPvalue[i][1]);
					

					outfile.print("\n      NESTED CLADE  ");
					outfile.printf("%12.4f",clad[k].Dn[i]);
					outfile.print("   ");
					outfile.printf("%10.4f",clad[k].DnPvalue[i][0]);
					outfile.print("   ");
					outfile.printf("%10.4f",clad[k].DnPvalue[i][1]);
			
				}
			}


			if(GeoDis.weights)
			{
				outfile.print("\n\n\nCORRELATIONS OF DISTANCES WITH OUTGROUP WEIGHTS:\n");
				outfile.print("\n TYPE OF DISTANCE      CORR. COEF.    PROB.<=      PROB.>=");

				if(clad[k].numSubClades == 1)
					outfile.print(" N.A. -- ONLY 1 CLADE IN NESTING GROUP");
				
				else {
				
					if(clad[k].corrDcWeights == NA)
						outfile.print("\n FROM CLADE MIDPT. -- N.A. -- NO DISTANCE OR WEIGHT VARIATION");
					else
					{
						outfile.print("\n FROM CLADE MIDPT.  ");
						outfile.printf("%12.4f",clad[k].corrDcWeights);
						outfile.print("   ");
						outfile.printf("%10.4f",clad[k].corrDcWPvalue[0]);
						outfile.print("   ");
						outfile.printf("%10.4f",clad[k].corrDcWPvalue[1]);
					}
					
					if(clad[k].corrDnWeights == NA)
						outfile.print("\n FROM NESTING MIDPT. -- N.A. -- NO DISTANCE OR WEIGHT VARIATION");
					else
					{
						outfile.print("\n FROM NESTING MIDPT.");
						outfile.printf("%12.4f",clad[k].corrDnWeights);
						outfile.print("   ");
						outfile.printf("%10.4f",clad[k].corrDnWPvalue[0]);
						outfile.print("   ");
						outfile.printf("%10.4f",clad[k].corrDnWPvalue[1]);
					}

				}
			
			}


			if(clad[k].check == (double) clad[k].numSubClades || clad[k].check == 0)
				 outfile.print("\n\n\nNO INTERIOR/TIP CLADES EXIST IN THIS GROUP");       
			else
			{
				outfile.print("\n\n\nPART III.  TEST OF INTERIOR VS. TIP CLADES:");
				outfile.print("\n\n TYPE OF DISTANCE   I-T DISTANCE      PROB.<=      PROB.>=");
				outfile.print("\n      WITHIN CLADE  ");
				outfile.printf("%12.4f",clad[k].tipIntDistance);
				outfile.print("  ");
				outfile.printf(" %10.4f",clad[k].ITcPvalue[0]);
				outfile.print("  ");
				outfile.printf(" %10.4f",clad[k].ITcPvalue[1]);

				outfile.print("\n      NESTED CLADE  ");
				outfile.printf("%12.4f",clad[k].tipIntDisNested);
				outfile.print("  ");
				outfile.printf(" %10.4f",clad[k].ITnPvalue[0]);
				outfile.print("  ");
				outfile.printf(" %10.4f",clad[k].ITnPvalue[1]);
			}

		} // 1 clade loop
	
		long total_time = end-start;
		double seconds = (double) total_time / 1000.0;
		//long minutes = seconds / 60;
		outfile.print("\n\n\n** ANALYSIS FINISHED **\nIt took ");
		//outfile.printf("%5d", minutes);
		//outfile.print(" minutes\n");
		outfile.printf("%5.4f", seconds);
		outfile.print(" seconds.\n");

		
		outfile.close();
		//System.err.println("\nDONE Printing out");

	} // method printOut


	/********************* DDtoDMS ***********************/
	/* transform decimal degrees into DMS */
	public static int[] DDtoDMS (double dd)
	{
		boolean negativeDD = false;
		
		if (dd < 0)
			{
			negativeDD = true;
			dd = Math.abs(dd);
			}	
		
		int dms[] = new int[3];
		int deg, min, sec;

		deg = (int) Math.floor(dd);
		min = (int) Math.floor((dd - deg) * 60);
		sec = (int) Math.round((((dd - deg) * 60) - min) * 60);
		
		if (negativeDD == true)
			deg = deg * -1;
		
		dms[0] = deg;
		dms[1] =  Math.abs(min);
		dms[2] =  Math.abs(sec);
		
		return dms;
	}

    public static void CheckExpiration(Frame theframe)
    {
label0:
        {
label1:
            {
                Calendar now = Calendar.getInstance();
                Calendar _tmp = now;
                Calendar _tmp1 = now;
                if(now.get(2) != 1)
                {
                    Calendar _tmp2 = now;
                    Calendar _tmp3 = now;
                    if(now.get(2) != 2)
                    {
                        break label1;
                    }
                }
                Calendar _tmp4 = now;
                if(now.get(1) == 2005)
                {
                    break label0;
                }
            }
            JOptionPane.showMessageDialog(theframe, "This version has expired! \n    Good bye...", "GeoDis warning", 2);
            theframe.dispose();
            System.exit(0);
        }
    }

} // class GeoDis




class Clade	
{
	public String cladeName;          // name of each clade (e.g. Clade 2-1)
	public int numSubClades;          // number of rows == number of haplotypes
	public int numCladeLocations;     // number of columns == number of geographical locations
	public int totaNumObs;            // total number of observations
	public int[] rowTotal;            // row totals
	public int[] columnTotal;         // column totals
	public int[] cladeLocIndex;       // indices of geographical locations for this clade
	public double[] meanLatitude;     // mean latitude
	public double[] meanLongitude;    // mean longitude
	public double[] randMeanLatitude;     // mean latitude
	public double[] randMeanLongitude;    // mean longitude
	public double[] Dc;               // average distance of clade members from the geocenter of their clade 
	public double[] Dn;               // average distance of clade members from the geocenter of their nesting clade 
	                                   //  but averaged over their clade membership
	public double[] varDc;            // average variance of the distance of clade members from the geocenter
	public double[] varDn;            // 
	public double[] Position;         // tip (1) or interior (0)
	public double[] randDc;           
	public double[] randDn;          
	public double[] subCladeSum;            
	public double[] ITcPvalue;       
	public double[] ITnPvalue;       
	public double[] corrDcWPvalue;  
	public double[] corrDnWPvalue;  
	public String[] subCladeName;    // name of each interior clade 
	public String[] subCladePosition;      
	public double[] weight;          // outgroup weight
	public double[][] DcPvalue ;     // clade distance
	public double[][] DnPvalue;      // nested clade distance
	public int[][] obsMatrix;        // observed number of observations 
	public int[][] randMatrix;       // random number of observations
	public double[][] expObsMatrix;  // expected number of obervations 
	public double[][] relFreq;       // relative frequency of clade i at location j 
	public double[][] absFreq;       // absolute frequency of clade i at location j 
	public int[][] RBMatrix;         // the random MX2 matrix described in Roff and Bentzen 
	public double check;	

	public double obsChi;            // chi square statistic
	public double meanLatNest;       // mean latitude for the nesting clade 
	public double meanLonNest;       // mean longitude for the nesting clade
	public double tipDistance;       // tip distance
	public double tipDisNested;      // tip distance nested clade
	public double intDistance;       // interior distance
	public double intDisNested;      // interior distance nested clade
	public double tipIntDistance;    // tip-interior distance
	public double tipIntDisNested;   // tip-interior distance nested clade

	public double meanDc;   
	public double meanDn;            
	public double meanWeight;      
	public double sumDcxWeight;    
	public double sumDnxWeight;     
	public double sumDcSq; 
	public double sumDnSq; 
	public double sumWeightSq;  
	public double corrDcWeights; 
	public double corrDnWeights;  
	public double randCorrDcWeights; 
	public double randCorrDnWeights; 

	public double chiPvalue;    
	public double sumTotal;
	public int cumColTotal;
	public int cumRowTotal;
	public int indTipClades;         // total individuals in tip clades
	public int indIntClades;         // total individuals in interior clades

	public Clade() {} // constructor

} // class Clade
