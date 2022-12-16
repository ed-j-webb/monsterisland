package net.edwebb.mi.extract;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class MIWriter {

	private File dbDir;

	private Map<String, BufferedWriter> writers = new HashMap<String, BufferedWriter>();

	public MIWriter(File dbDir) {
		this.dbDir = dbDir;
	}
	
	public void printData(Turn turn) throws IOException {
		try {
			printData(turn, 0);
		} finally {
			closeWriters();
		}
	}
	
	private void printData(Encounter enc, int depth) throws IOException {
		BufferedWriter writer = getWriter("Turns.txt");
		for (int i = 0; i < depth; i++) {
			writer.write("  ");
		}
		writer.write(enc.toString());
		writer.write("\n");
		Iterator<Encounter> encs = enc.getEncounters().iterator();
		while (encs.hasNext()) {
			printData(encs.next(), depth + 1);
		}
		if (enc.getEncType().equals("Turn")) {
			printData(((Turn)enc).getSightings());
			printData(((Turn)enc).getStats());
		}
	}
	
	private void printData(Set<Sighting> sightings) throws IOException {
		BufferedWriter writer = getWriter("Turns.txt");
		if (sightings.size() > 0) {
			writer.write("Sightings\n");
			Iterator<Sighting> it = sightings.iterator();
			while (it.hasNext()) {
				writer.write(it.next().toString());
				writer.write("\n");
			}
		}
	}
	
	private void printData(Stats stats) throws IOException {
		BufferedWriter writer = getWriter("Turns.txt");
		writer.write(stats.toString());
	}
	
	public void writeData(Turn enc) throws IOException {
		try {
			writeData((Encounter)enc);
		} finally {
			closeWriters();
		}
	}
	
	private void writeData(Encounter enc) throws IOException {
		String data = enc.getData();
		if (!data.equals("")) {
			String fileName = data.substring(0, data.indexOf(","));
			BufferedWriter writer = getWriter(fileName + ".csv");
			writer.write(data.substring(data.indexOf(",") + 1));
		}
		Iterator<Encounter> encs = enc.getEncounters().iterator();
		while (encs.hasNext()) {
			writeData(encs.next());
		}
		
		if (enc.getEncType().equals("Turn")) {
			writeData(((Turn)enc).getSightings());
			writeData(((Turn)enc).getStats());
		}
	}
	
	private void writeData(Stats stats) throws IOException {
		String data = stats.getData();
		if (!data.equals("")) {
			String fileName = data.substring(0, data.indexOf(","));
			BufferedWriter writer = getWriter(fileName + ".csv");
			writer.write(data.substring(data.indexOf(",") + 1));
		}
	}

	private void writeData(Set<Sighting> sightings) throws IOException {
		if (sightings.size() > 0) {
			BufferedWriter writer = getWriter("Sightings.csv");
			Iterator<Sighting> it = sightings.iterator();
			while (it.hasNext()) {
				writer.write(it.next().getData());
			}
		}
	}

	private BufferedWriter getWriter(String fileName) throws IOException {
		BufferedWriter writer = writers.get(fileName);
		if (writer == null) {
			writer = new BufferedWriter(new FileWriter(new File(dbDir, fileName), true));
			writers.put(fileName, writer);
		}
		return writer;
	}
	
	private void closeWriters() {
		Iterator<BufferedWriter> it = writers.values().iterator();
		while (it.hasNext()) {
			try {
				it.next().close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		writers.clear();
	}
}
