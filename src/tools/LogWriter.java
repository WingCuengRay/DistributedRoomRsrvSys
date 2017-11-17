package tools;

import java.io.FileWriter;
import java.io.IOException;

public class LogWriter {
	
	private FileWriter writer;
	
	
	public LogWriter(String filename){
		try {
			writer = new FileWriter(filename, true);
		}catch(IOException ex) {
			System.out.println("LogWriter constructed failure");
			ex.printStackTrace();
		}
		
	}
	
	public synchronized boolean write(LogItem log){
		if(writer == null)
			return false;
		
		try {
			writer.write(log.format());
			writer.flush();
		} catch (IOException e) {
			//e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
}
