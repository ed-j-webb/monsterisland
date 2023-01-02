package net.edwebb.jim.factory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.filechooser.FileFilter;

import net.edwebb.jim.model.MapData;

public class FactoryManager {

	private static FactoryManager instance;
	
	private List<DataFactory> factories = new ArrayList<DataFactory>();
	
	private Set<String> unmatched;
	
	private FactoryManager(File featureFolder) throws IOException {
		factories.add(new JIMDataFactory());
		factories.add(new CSVDataFactory());
		factories.add(new KLNDataFactory(new File(featureFolder, "klntrans.csv")));
		factories.add(new EMIDataFactory(new File(featureFolder, "emitrans.csv")));
		factories.add(new KAOSDataFactory(new File(featureFolder, "kaostrans.csv")));
	}
	
	public static void createInstance(File featureFolder) throws IOException {
		instance = new FactoryManager(featureFolder);
	}
	
	public static FactoryManager getInstance() {
		return instance;
	}
	
	public List<FileFilter> getReadFilters() {
		List<FileFilter> list = new ArrayList<FileFilter>();
		Iterator<DataFactory> it = factories.iterator();
		while (it.hasNext()) {
			DataFactory df = it.next();
			if (df.isCreate()) {
				list.add(df.getFilter());
			}
		}
		return list;
	}
	
	public List<FileFilter> getWriteFilters() {
		List<FileFilter> list = new ArrayList<FileFilter>();
		Iterator<DataFactory> it = factories.iterator();
		while (it.hasNext()) {
			DataFactory df = it.next();
			if (df.isSave()) {
				list.add(df.getFilter());
			}
		}
		return list;
	}
	
	public List<FileFilter> getTranslateFilters() {
		List<FileFilter> list = new ArrayList<FileFilter>();
		Iterator<DataFactory> it = factories.iterator();
		while (it.hasNext()) {
			DataFactory df = it.next();
			if (df.isTranslate()) {
				list.add(df.getFilter());
			}
		}
		return list;
	}

	public Set<String> getUnmatched() {
		return unmatched;
	}
	
	public MapData createFrom(File file) throws IOException {
		Iterator<DataFactory> it = factories.iterator();
		while (it.hasNext()) {
			DataFactory df = it.next();
			if (file.getName().toLowerCase().endsWith(df.getSuffix())) {
				if (df.isCreate()) {
					MapData data = df.createFrom(file);
					unmatched = df.getUnmatched();
					return data;
				} else {
					throw new UnsupportedOperationException("Cannot create from this file type");
				}
			}
		}
		throw new UnsupportedOperationException("The file type has not been recognised");
	}	

	public void saveTo(MapData data, File file) throws IOException {
		Iterator<DataFactory> it = factories.iterator();
		while (it.hasNext()) {
			DataFactory df = it.next();
			if (file.getName().toLowerCase().endsWith(df.getSuffix())) {
				if (df.isSave()) {
					df.saveTo(data, file);
					unmatched = df.getUnmatched();
					return;
				} else {
					throw new UnsupportedOperationException("Cannot save to this file type");
				}
			}
		}
		throw new UnsupportedOperationException("The file type has not been recognised");
	}	
	
	public String listTranslations(File file) throws IOException {
		Iterator<DataFactory> it = factories.iterator();
		while (it.hasNext()) {
			DataFactory df = it.next();
			if (file.getName().toLowerCase().endsWith(df.getSuffix())) {
				if (df.isTranslate()) {
					return df.listTranslations(file);
				} else {
					throw new UnsupportedOperationException("Cannot create translations for this file type");
				}
			}
		}
		throw new UnsupportedOperationException("The file type has not been recognised");
	}
}
