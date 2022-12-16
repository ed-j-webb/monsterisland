package net.edwebb.mi.extract;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Iterator;

import net.edwebb.mi.data.DataStore;

public class MIExtractor {

	private MIReader reader;
	private MIWriter writer;
	

	public MIExtractor() {
		reader = new MIReader();
	}
	
	public MIExtractor(File writerDir) {
		reader = new MIReader();
		writer = new MIWriter(writerDir);
	}
	
	public Turn extract(String filename, String mode, int x, int y, Stats stats) throws FileNotFoundException, IOException {
		File file = new File(filename);
		boolean statsOnly = mode.equals("S") || mode.equals("M");

		Turn turn = reader.read(file, x, y, stats, statsOnly);
		if (mode.equals("R")) {
			if (writer != null) {
				writer.printData(turn);
			}
		} else if (mode.equals("N") || mode.equals("M")) {
			// Output nothing this is the first turn to get the stats
		} else {
			if (writer != null) {
				writer.writeData(turn);
			}
		}
		
		return turn;
	}
	
	public Turn extract(Reader input, String mode, int x, int y, Stats stats) throws FileNotFoundException, IOException {
		boolean statsOnly = mode.equals("S") || mode.equals("M");

		Turn turn = reader.read(input, x, y, stats, statsOnly);
		if (mode.equals("R")) {
			if (writer != null) {
				writer.printData(turn);
			}
		} else if (mode.equals("N") || mode.equals("M")) {
			// Output nothing this is the first turn to get the stats
		} else {
			if (writer != null) {
				writer.writeData(turn);
			}
		}
		
		return turn;
	}
	
	public void print(Encounter enc, int depth) {
		for (int i = 0; i < depth; i++) {
			System.out.print("  ");
		}
		System.out.println(enc);
		Iterator<Encounter> encs = enc.getEncounters().iterator();
		while (encs.hasNext()) {
			print(encs.next(), depth + 1);
		}
	}
	
	public void data(Encounter enc) {
		System.out.print(enc.getData());
		Iterator<Encounter> encs = enc.getEncounters().iterator();
		while (encs.hasNext()) {
			data(encs.next());
		}
	}

	private static String getMode() { 
		System.out.print("Enter the mode (R = Read, D = Data, S = Stats): ");
		
	      BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

	      //  read the username from the command-line; need to use try/catch with the
	      //  readLine() method
	      try {
	         return br.readLine();
	         
	      } catch (IOException ioe) {
	         System.out.println("IO error trying to read mode.");
	         System.exit(1);
	         return null;
	      }
	      
	}

	private static int[] getCoords() {
		System.out.print("Enter the starting square (y,x): ");
		
	      BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

	      try {
	         String[] coords =  br.readLine().split(",");
	         if (coords.length != 2) {
		         System.out.println("Must be entered like '-54,215'");
		         System.exit(1);
	         }
	         int[] xy = new int[2];
	         xy[0] = Integer.valueOf(coords[0]);
	         xy[1] = Integer.valueOf(coords[1]);
	         return xy;
	      } catch (IOException ioe) {
	         System.out.println("IO error trying to read mode.");
	         System.exit(1);
	         return null;
	      } finally {
	    	  try {
	    		  br.close();
	    	  } catch (Throwable t) {
	    		  
	    	  }
	      }
	      
	}

	private static void showUsage() {
		System.out.println("java MIExtractor inputFolder outputFolder dataFolder");
	}
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		if (args.length != 3) {
			showUsage();
			return;
		}
		for (int i = 0; i< args.length; i++) {
			File file = new File(args[i]);
			if (!file.exists() || !file.isDirectory()) {
				showUsage();
				return;
			}
		}
		MIExtractor ex = new MIExtractor(new File(args[1]));
		DataStore.createInstance(new File(args[2]));
		String mode = getMode();
	    int[] coords = new int[] {0,0}; //getCoords();  
		
		File folder = new File(args[0]);
		File[] files = folder.listFiles();
		Stats stats = null;
		String initialMode = mode.equals("S") ? "M" : "N";
		for (int i = 0; i < files.length; i++) {
			Turn turn = ex.extract(files[i].getPath(), i == 0 ? initialMode:mode, coords[1], coords[0], stats);
			coords[0] = turn.getY();
			coords[1] = turn.getX();
			stats = turn.getStats();
		}
		
	}
}
