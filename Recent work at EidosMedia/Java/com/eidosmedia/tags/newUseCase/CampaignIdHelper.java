package com.eidosmedia.tags.newUseCase;

import java.io.*;
import com.eidosmedia.tags.logger.Logger;

public class CampaignIdHelper {
	private static final String TEXT_FILE = "/methode/meth01/methode-servlets/webclient/WEB-INF/tmp/campaignid.txt";
	private static Logger logger = new Logger();
	
	/**
	 * Open the file from the path and create a File object.
	 */
	private static File openFile(){
		File file = new File(TEXT_FILE);
		try {
			file.createNewFile();
		} catch (Exception e) {
			logger.error(e);
		}
		return file;
	}
	
	/**
	 * Create a buffer and store the file in the buffer. Read the first line (it's only a one line file)
	 * from the buffer and return the value
	 */
	public static String getId() {
		String currentVal = "1";
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(openFile()));
			String line = reader.readLine();
			
			if(line != null){			
				currentVal = line;
			}
		} catch (Exception e){
			logger.error(e);
		} finally {
			try {
				reader.close();
			} catch (Exception e) {}
		}
		
		return currentVal;
	}
	
	/**
	 * Get the current value from the file and increment it. Create a buffer, write the new value to the 
	 * buffer, write the buffer to the file.
	 */	
	public static void setId(){		
		String currentVal = getId();
		BufferedWriter writer = null;
		try {
			int newVal = Integer.parseInt(currentVal) + 1;
			String newValStr = String.valueOf(newVal);
			writer = new BufferedWriter(new FileWriter(openFile()));
			writer.write(newValStr);			
		} catch (Exception e) {
			logger.error(e);
		} finally {
			try {
				writer.close();
			} catch (Exception e) {
				logger.error(e);
			}
		}
		
	}
}