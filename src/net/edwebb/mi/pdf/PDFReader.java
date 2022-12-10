package net.edwebb.mi.pdf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A Class that can read PDF data from an InputStream. It is capable 
 * of reading whole lines of text AND streams of binary data as is 
 * necessary when reading PDF data. 
 * 
 * @author Ed Webb
 */
public class PDFReader {

	private static final int CR = 13;
	private static final int LF = 10;
	
	private Integer spareByte;
	
	private static final String[] ENDSTREAM = new String[] {"endstream", "\r\nendstream"};
	
	public static final int PDF_1_2 = 0;
	public static final int PDF_1_3 = 1;
	
	private int mode = 0;
	
	private InputStream is;
	
	/**
	 * Create a new PDFReader to read the PDF from the InputStream provided
	 * @param is
	 */
	public PDFReader(InputStream is) {
		this.is = is;
	}
	
	/**
	 * Reads a line of text from the PDF InputStream
	 * @return
	 * @throws IOException
	 */
	public String readLine() throws IOException {
		int num;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		if (spareByte != null) {
			baos.write(spareByte.intValue());
			spareByte = null;
		}
		
		while (is.available() > 0) {
			num = is.read();
			if (num == CR) {
				num = is.read();
				if (num != LF) {
					// Oops we've started to read the next line. Stash this away ready for the next call
					spareByte = num;
				}

				if (baos.toString().startsWith("%PDF-1.2")) {
					mode = PDF_1_2;
				} else if (baos.toString().startsWith("%PDF-1.3")) {
					mode = PDF_1_3;
				}
				return baos.toString();
			}
			if (num == LF) {
				if (baos.toString().startsWith("%PDF-1.2")) {
					mode = PDF_1_2;
				} else if (baos.toString().startsWith("%PDF-1.3")) {
					mode = PDF_1_3;
				}
				return baos.toString();
			}
			baos.write(num);
		}
		return null;
	}

	/**
	 * Reads the binary data blob of a PDF stream into a byte array.
	 * This method must be called after the start of stream "stream" 
	 * marker has been read. as soon as it encounters the "endstream"
	 * marker the bytes read up to that point are returned.
	 * NB There may be problems IF the binary contains bytes that 
	 * match "endstream" as this method does not use the /Length PDF
	 * value to read the correct number of bytes
	 * 
	 * @return
	 * @throws IOException
	 */
	public byte[] readStream() throws IOException {
		int pos = 0;
		int num;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] hold = new byte[ENDSTREAM[mode].length()];

		if (spareByte != null) {
			num = spareByte.intValue();
			
			// This MAY be the matching string so don't write it yet
			if (ENDSTREAM[mode].codePointAt(pos) == num) {
				hold[pos] = (byte)num;
				pos++;
			} else {
				baos.write(num);
			}
			spareByte = null;
		}
		
		while (is.available() > 0) {
			num = is.read();

			// The bytes don't make up the match string so write what's in the buffer
			// and reset the position
			if (ENDSTREAM[mode].codePointAt(pos) != num && pos > 0) {
				baos.write(hold, 0, pos);
				pos = 0;
			}
			
			// This MAY be the matching string so don't write it yet
			if (ENDSTREAM[mode].codePointAt(pos) == num) {
				hold[pos] = (byte)num;
				pos++;
			} else {
				baos.write(num);
			}

			if (pos >= ENDSTREAM[mode].length()) {
				return baos.toByteArray();
			}
		}
		throw new IOException("End of inputstream but did not find the end of the stream");
	}
	
}
