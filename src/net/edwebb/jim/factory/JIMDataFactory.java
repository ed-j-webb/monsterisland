package net.edwebb.jim.factory;

import java.awt.Point;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.filechooser.FileFilter;

import net.edwebb.jim.model.MapData;
import net.edwebb.mi.data.DataStore;
import net.edwebb.mi.data.Decoder;

public class JIMDataFactory implements DataFactory {

	// A file filter to load jim files
	private static final FileFilter jimFilter = new FileFilter() {
		@Override
		public boolean accept(File file) {
			return file.isDirectory() || file.getName().toLowerCase().endsWith(".jim");
		}

		@Override
		public String getDescription() {
			return "Java Island Mapper Files (*.jim)";
		}
	};
	
	// A Set of strings holding features that have not been matched to a feature in jim
	private Set<String> unmatched;
	
	@Override
	public Set<String> getUnmatched() {
		return unmatched;
	}

	@Override
	public FileFilter getFilter() {
		return jimFilter;
	}

	@Override
	public String getSuffix() {
		return ".jim";
	}

	@Override
	public boolean isCreate() {
		return true;
	}

	@Override
	public boolean isSave() {
		return true;
	}
	
	@Override
	public boolean isTranslate() {
		return false;
	}

	@Override
	/**
	 * Create a MapData object from a *.jim file
	 * 
	 * @param file the file containing the map data
	 * @return a MapData object populated from the file
	 * @throws IOException if the File cannot be read
	 */
	public MapData createFrom(File file) throws IOException {
		FileInputStream fis = new FileInputStream(file);
		unmatched = new HashSet<String>();
		DataInputStream dis = new DataInputStream(fis);
		
		try {
			short width = dis.readShort();
			short height = dis.readShort();
			short top = dis.readShort();
			short left = dis.readShort();
			short sely = dis.readShort();
			short selx = dis.readShort();
	
			MapData data = new MapData(top, left, width, height, sely, selx);
	
			short x;
			short y;
			byte z;
	
			while (dis.available() > 0) {
				x = dis.readShort();
				y = dis.readShort();
				z = dis.readByte();
				if (z < 0) {
					byte[] b = new byte[-z];
					dis.readFully(b);
					if (b[0] == 26) {
						// chr(26) substitute means the coordinate offset is stored in this record
						byte[] c = new byte[b.length - 1];
						System.arraycopy(b, 1, c, 0, c.length);
						data.setOffset(x, y, new String(c, "UTF-8"));
					} else {
						data.setSquareNotes(x, y, new String(b, "UTF-8"));
					}
				} else {
					short[] sqr = new short[z];
					for (int i = 0; i < z; i++) {
						sqr[i] = dis.readShort();
					}
					sqr = checkSquare(x, y, sqr);
					data.setSquare(x, y, sqr);
				}
			}
			data.setDirty(false);
			return data;
		} finally {
			dis.close();
		}
	}
	
	/**
	 * Checks if the contents of the square are valid. Removes terrain and feature that are not and adds them to the unmatched set 
	 * @param sqr the array to check
	 * @return a an array only holding valid features
	 */
	private short[] checkSquare(short x, short y, short[] sqr) {
		int k = sqr.length;
		for (int i = 0; i < sqr.length; i++) {
			if (i == 0) {
				if (!DataStore.getInstance().isValid((short)(sqr[i] & 0xff))) {
					if ((sqr[i] & 0xff) != 85) {
						// Don't output U terrain (silently replace with 0)
						unmatched.add(Decoder.charFromShort((short)(sqr[i] & 0xff)));
					}
					sqr[i] = (short)(sqr[i] & 0xff00);
				}
			} else {
				if (!DataStore.getInstance().isValid(sqr[i])) {
					if (sqr[i] < 0 || sqr[i] > 999) {
						unmatched.add(Decoder.stringFromShort(sqr[i]));
					} else {
						unmatched.add(Short.toString(sqr[i]));
					}
					sqr[i] = 0;
					k--;
				}
			}
		}
		
		if (k == sqr.length) {
			return sqr;
		}
		
		short[] temp = new short[k];
		k = 0;
		for (int i = 0; i < sqr.length; i++) {
			if (sqr[i] > 0) {
				temp[k++] = sqr[i];
			}
		}
		
		return temp;
	}
	
	public void saveTo(MapData data, File file) throws IOException {
		FileOutputStream fos = new FileOutputStream(file);
		DataOutputStream dos = new DataOutputStream(fos);
		
		try {
			dos.writeShort(data.getWidth());
			dos.writeShort(data.getHeight());
			dos.writeShort(data.getTop());
			dos.writeShort(data.getLeft());
			dos.writeShort(data.getY());
			dos.writeShort(data.getX());
	
			for (int i = 0; i < data.getWidth(); i++) {
				for (int j = 0; j < data.getHeight(); j++) {
					short[] sqr = data.getSquare(i, j);
					if (sqr != null && sqr.length > 0) {
						dos.writeShort(i);
						dos.writeShort(j);
						dos.writeByte(sqr.length);
						for (int k = 0; k < sqr.length; k++) {
							dos.writeShort(sqr[k]);
						}
					}
				}
			}
			Iterator<Map.Entry<Point, String>> it = data.getMapNotesIterator();
			while (it.hasNext()) {
				Map.Entry<Point, String> entry = it.next();
				dos.writeShort(entry.getKey().x);
				dos.writeShort(entry.getKey().y);
				byte[] b = entry.getValue().getBytes("UTF-8");
				if (b.length > 250) {
					dos.write(-250);
					dos.write(b, 0, 250);
				} else {
					dos.writeByte(-b.length);
					dos.write(b);
				}
			}
	
			if (data.getOffset() != null) {
				dos.writeShort(data.getOffX());
				dos.writeShort(data.getOffY());
				byte[] b = data.getOffset().getBytes("UTF-8");
				dos.writeByte(-b.length - 1);
				dos.write((byte) 26);
				dos.write(b);
			}
	
			data.setDirty(false);
		} finally {
			dos.close();
		}
	}

	@Override
	public String listTranslations(File file) throws IOException {
		throw new UnsupportedOperationException("JIM files do not have a translation file");
	}
}
