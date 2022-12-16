package net.edwebb.mi.pdf;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.InflaterInputStream;

import net.edwebb.mi.data.DataStore;
import net.edwebb.mi.extract.MIExtractor;
import net.edwebb.mi.extract.Stats;
import net.edwebb.mi.extract.Turn;

/**
 * A class to read, process and extract data from a Monster Island Turn Results pdf file
 * @author edw
 *
 */
public class PDFExtractor {

	// Identify and group the start of a PDF object
	private static Pattern objStart = Pattern.compile("([0-9]+) [0-9]+ obj(.*)");
	
	// Identify and group a line of text in the PDF rendering instructions
	private static Pattern textLine = Pattern.compile("BT.*?Tf .*? (.*?) .*?\\((.*)\\).*? ET");
	
	// Identify and extract /Contents [] PDF array
	private static Pattern pageContent = Pattern.compile("/Contents\\[(.*?)\\]");
	
	// Identify and extract a /Kids [] PDF array
	private static Pattern pagesList = Pattern.compile("/Kids\\[(.*?)\\]");
	
	// For each Column on the printed map these are (roughly) where each square is printed
	// (each square is actually made up of 4 smaller squares which is why there are 2 number's per column)
	private static BigDecimal[] columns = new BigDecimal[] {new BigDecimal("41.01"), new BigDecimal("68.13"), 
			                                                new BigDecimal("95.25"), new BigDecimal("122.37"), 
			                                                new BigDecimal("149.49"), new BigDecimal("176.61"), 
			                                                new BigDecimal("203.73"), new BigDecimal("230.85"), 
			                                                new BigDecimal("257.97"), new BigDecimal("285.09"), 
			                                                new BigDecimal("312.21"), new BigDecimal("339.33"), 
			                                                new BigDecimal("366.45"), new BigDecimal("393.57"), 
			                                                new BigDecimal("420.69"), new BigDecimal("447.81"), 
			                                                new BigDecimal("474.93"), new BigDecimal("502.05") };

	// Stores the fingerprint for every known image on the printed map
	// This is populated from an editable config file
	private Map<Integer, String> mapIcons = new HashMap<Integer, String>();


	/**
	 * Create a new PDFExtractor using the mapIcons stored in the file
	 * @param iconFile the File that contains the mapIcon fingerprints
	 * @throws IOException
	 */
	public PDFExtractor(File iconFile) throws IOException {
		mapIcons = readIconFile(iconFile);
	}


	/**
	 * Reads the image fingerprint information from the file and returns a Map of 
	 * Integer fingerprint to feature code
	 * @param file the image fingerprint file
	 * @return a Map of fingerprints to feature codes
	 * @throws IOException
	 */
	private Map<Integer, String> readIconFile(File file) throws IOException {
		Map<Integer, String> map = new HashMap<Integer, String>();

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			String line = reader.readLine();
			while (line != null) {
				String[] data = line.split(";");
				map.put(Integer.valueOf(data[0]), data[1]);
				line = reader.readLine();
			}
			return map;
		} finally {
			try {
				reader.close();
			} catch (Throwable t) {

			}
		}
	}

	private String decode(byte[] bytes) throws IOException {
		return decode(bytes, 0, bytes.length);
	}

	private String decode(byte[] bytes, int start, int length) throws IOException {
		ByteArrayInputStream stream = new ByteArrayInputStream(bytes, start, length);
		return decode(stream);
	}
	
	private String decode(InputStream stream) throws IOException {
        InflaterInputStream zip = new InflaterInputStream(stream);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte b[] = new byte[4092];

        int n;
        while ((n = zip.read(b)) >= 0) {
            out.write(b, 0, n);
        }
        zip.close();
        out.close();
		
		return new String(out.toByteArray(), 0, out.size(), "UTF-8");
	}

	/**
	 * Reads the PDF file and returns a PDFData containing the relevant information
	 * @param file the PDF File to extract data from
	 * @return a PDFData object containing the relevant information
	 * @throws IOException
	 */
	public PDFData getPDFData(File file) throws IOException {
		FileInputStream fis = new FileInputStream(file);
		PDFReader reader = new PDFReader(fis);

		PDFData pdfData = new PDFData();

		String line = null;
		String currentObjectNumber = null;
		String state = null;

		while((line = reader.readLine()) != null) {
			Matcher match = objStart.matcher(line);
			if (match.matches()) {
				currentObjectNumber = match.group(1);
				//System.out.println("Start of object " + currentObjectNumber);
			} else if (line.equals("endobj")) {
				//System.out.println("End of object " + currentObjectNumber);
				currentObjectNumber = null;
				state = null;
			}
			
			if (state != null && state.equals("pages") && line.startsWith("/Kids")) {
				if (!line.contains("]")) {
					state = "inpages";
				}
				String[] parts = line.split(" ");
				for (int i = 2; i < parts.length - 2; i+=3) {
					pdfData.addPage(parts[i]);
				}
			} else if (state != null && state.equals("inpages")) {
				if (line.contains("]")) {
					state = null;
				}
				String[] parts = line.split(" ");
				for (int i = 0; i < parts.length - 2; i+=3) {
					pdfData.addPage(parts[i]);
				}
			} else if (state != null && state.equals("page") && line.startsWith("/Contents")) {
				if (!line.contains("]")) {
					state = "incontent";
				}
				String[] parts = line.split(" ");
				for (int i = 2; i < parts.length - 2; i+=3) {
					pdfData.addContents(currentObjectNumber, parts[i]); 
				}
			} else if (state != null && state.equals("incontent")) {
				if (line.contains("]")) {
					state = null;
				}
				String[] parts = line.split(" ");
				for (int i = 0; i < parts.length - 2; i+=3) {
					pdfData.addContents(currentObjectNumber, parts[i]); 
				}
			} else if (currentObjectNumber != null && line.contains("/Pages")) {
				state = "pages";
			} else if (currentObjectNumber != null && line.contains("/Page")) {
				state = "page";
				if (line.contains("/Contents")) {
					Matcher content = pageContent.matcher(line);
					if (content.find()) {
						String[] parts = content.group(1).split(" ");
						for (int i = 0; i< parts.length; i+=3) {
							pdfData.addContents(currentObjectNumber, parts[i]);
						}
					}
				}
			} else if (currentObjectNumber != null && line.contains("/Length") && line.endsWith("stream")) {
				byte[] stream = reader.readStream();
				if (stream.length > 0) {
					if ( line.contains("/FlateDecode")) {
						String result = decode(stream);
						if (result.contains("/Pages")) {
							Matcher list = pagesList.matcher(result);
							if (list.find()) {
								String[] parts = list.group(1).split(" ");
								for (int i = 0; i< parts.length; i+=3) {
									pdfData.addPage(parts[i]);
								}
							}
						}
						pdfData.addContent(currentObjectNumber, decode(stream));
					} else {
						pdfData.addContent(currentObjectNumber, new String(stream, "ASCII"));
					}
				}
			} else if (currentObjectNumber == null) {
				//System.out.println(line);
			}
		}
		return pdfData;
	}

	/**
	 * Processes the data contained in the PDFData object and produces a text output that
	 * can be fed into MIReader
	 * @param pdfData the PDFData object containing the PDF data
	 * @return a String of the text and mapping info contained in the data
	 * @throws IOException
	 */
	public String extractData(PDFData pdfData) throws IOException {
		String pos = null;
		boolean map = false;
		boolean inImage = false;
		Integer fingerprint = 0;
		Integer mapLeft = null;
		Integer mapTop = null;
		Integer mapRow = null;
		Integer mapColumn = null;
		String line = null;

		BigDecimal imageLeft = null;
		
		StringBuilder sb = new StringBuilder();
		
		Iterator<String> it = pdfData.iterator();
		while (it.hasNext()) {
			
			BufferedReader cleaner = new BufferedReader(new StringReader(it.next()));
			
			while((line = cleaner.readLine()) != null) {
				if (line.startsWith("BT")) {
					Matcher match = textLine.matcher(line);
					match.find();
					if (pos == null) {
						pos = match.group(1);
					} else {
						if (match.group(1).equals(pos)) {
							sb.append(" ");
						} else { 
							sb.append("\r\n");
							pos = match.group(1);
						} 
					}
					// Horrible RegEx to replace \( with ( and \) with ) but you have to escape \ twice
					// Once for the Java String and again for the RegEx
					sb.append(match.group(2).replaceAll("\\\\\\(", "(").replaceAll("\\\\\\)", ")"));
					
					if (map) {
						if (line.contains("Sightings ")) {
							map = false;
							sb.append("\n=== END OF MAP DATA ===\n");
						} else if (mapLeft == null) {
							mapLeft = Integer.valueOf(match.group(2).substring(0, match.group(2).indexOf(" ")).replaceAll("[^-0-9]", ""));
						} else if (mapTop == null) {
							mapTop = Integer.valueOf(match.group(2).substring(0, match.group(2).indexOf(" ")).replaceAll("[^-0-9]", ""));
							mapRow = mapTop;
							//System.out.println("Map Top Left: (" + mapTop + ", " + mapLeft + ")");
							//System.out.println("Row# " + mapRow);
						} else {
							mapRow = Integer.valueOf(match.group(2).substring(0, match.group(2).indexOf(" ")).replaceAll("[^-0-9]", ""));
							//System.out.println("Row# " + mapRow);
						}
					}

					if (line.contains("Turn#") && line.contains("ending in")) {
						map = true;
						sb.append("\n=== START OF MAP DATA ===\n");
					}
				} else if (map) {
					if (line.startsWith("q")) {
						String[] split = line.split(" ");
						//System.out.print( split[5] + ", " + split[6] + ": ");
						imageLeft = new BigDecimal(split[5]);
					} else if (line.startsWith("BI")) {
						inImage = true;
					} else if (inImage) {
						if (line.contains("EI")) {
							inImage = false;
							int column = -1;
							int possible = -1;
							BigDecimal difference = BigDecimal.valueOf(1000);
							for (int i = 0; i < columns.length; i++) {
								if (imageLeft.equals(columns[i])) {
									column = i/2;
									break;
								}
								BigDecimal diff = columns[i].subtract(imageLeft).abs();
								if ( diff.compareTo(difference) < 0) {
									possible = i/2;
									difference = diff;
								}
							}
							
							if (column >= 0) {
								mapColumn = mapLeft + column;
								//System.out.print("is (" + mapRow + ", " + (mapLeft + column) + "): " );
							} else {
								mapColumn = mapLeft + possible;
								//System.out.print("may be (" + mapRow + ", " + (mapLeft + possible) + "): " );
							}
							
							if (mapIcons.containsKey(fingerprint) && !mapIcons.get(fingerprint).equals("")) {
								sb.append("(" + mapRow + "," + mapColumn + ") " + mapIcons.get(fingerprint) + "\n");
								//System.out.println(mapIcons.get(fingerprint));
							} else if (!mapIcons.containsKey(fingerprint)) {
								sb.append("(" + mapRow + "," + mapColumn + ") UNKNOWN-ICON:" + fingerprint + "\n");
								//System.out.println("Unknown Fingerprint " + fingerprint);
								//System.out.println();
							}
							fingerprint = 0;
						} else {
							fingerprint = ((Integer)(fingerprint + line.hashCode())).hashCode();
						}
					}
				}
			}
		}
		//System.out.println(sb.toString());
		return sb.toString();
	}
	
	
	/**
	 * Returns the text and decoded map information that can be fed into MIReader
	 * @param file the PDF file to extract information from
	 * @return text and mapping data contained in the file
	 * @throws IOException
	 */
	public String extract(File file) throws IOException {
		PDFData pdfData = getPDFData(file);
		return extractData(pdfData);
	}
	
	public static void main(String[] args) throws Exception {
		
		PDFExtractor pdfExtractor = new PDFExtractor(new File("C:/Dev3/jIsland/data/mapicons.csv"));
		MIExtractor miExtractor = new MIExtractor(new File("C:/Old Home/D/Ed/"));

		DataStore.createInstance(new File("C:/Dev3/jIsland/data"));
		
		String mode = "D";
	    int[] coords = new int[] {0,0}; //getCoords();  
		
		Stats stats = null;
		
		// TODO: Write some code to batch the turns to fill in the missing fingerprints
		//for (int i = 886; i< 1000; i++) {
		for (int i = 983; i < 1000; i++) {
			System.out.println("Turn #" + i);
			String text = pdfExtractor.extract(new File("C:/Old Home/D/Ed/Monster/Turns/5846/5846-" + i + ".pdf"));
			Turn turn = miExtractor.extract(new StringReader(text), (i==983 ? "N" : mode), coords[0], coords[1], stats);
			coords[0] = turn.getY();
			coords[1] = turn.getX();
			stats = turn.getStats();
		}
	}
}
