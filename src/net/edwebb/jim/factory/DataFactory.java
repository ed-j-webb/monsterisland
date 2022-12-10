package net.edwebb.jim.factory;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import javax.swing.filechooser.FileFilter;

import net.edwebb.jim.data.MapData;

/**
 * An interface for DataFactory classes. These are classes that can read from and save to different
 * map file formats
 * 
 * @author Ed Webb
 *
 */
public interface DataFactory {

	public Set<String> getUnmatched();

	public FileFilter getFilter();
	
	public String getSuffix();
	
	public boolean isCreate();
	public boolean isSave();
	public boolean isTranslate();
	
	public MapData createFrom(File file) throws IOException;
	
	public void saveTo(MapData data, File file) throws IOException;
	
	public String listTranslations(File file) throws IOException;
	
}
