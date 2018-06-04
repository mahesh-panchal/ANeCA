package ANeCA;

import java.io.*;
import java.util.ArrayList;

public class InputFileReader{
	
	public static String readFileToLine(String fname) throws FileNotFoundException,IOException{
		BufferedReader buffer = new BufferedReader(new FileReader(fname));
		StringBuffer file = new StringBuffer();
		String line;
		while((line = buffer.readLine()) != null){
			file.append(line);
		}
		return file.toString();
	}
	
	public static String[] readFileToArray(String fname) throws FileNotFoundException,IOException{
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