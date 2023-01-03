package net.edwebb.mi.pdf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A class to hold the data extracted from a Monster Island Turn Results PDF.
 * PDF files are made up of a series of objects. This class stores the three
 * types of object needed to get the data out.
 * 1. Pages this object holds the object numbers for the pages of the document
 * 2. Page these objects hold the object numbers of the content for the page
 * 3. Content these objects hold the text that is used to render the page
 * 
 * Note that the text in the contents will contain rendering instructions and 
 * encoded images not just the text that is visible when the document is opened 
 * in a PDF reader.
 * 
 * @author Ed Webb
 *
 */
public class PDFData {
	private List<String> pageObjects = new ArrayList<String>();
	private Map<String, List<String>> pageContents = new HashMap<String, List<String>>();
	private Map<String, String> contents = new HashMap<String, String>();
	
	/**
	 * Add the object number for a Page object
	 * @param pageNumber the number of the Page PDF object
	 */
	public void addPage(String pageNumber) {
		pageObjects.add(pageNumber);
	}
	
	/**
	 * Add the object number for a Page's content
	 * @param pageNumber the number of the Page PDF object
	 * @param contentNumber the number of the Contents PDF object
	 */
	public void addContents(String pageNumber, String contentNumber) {
		if (!pageContents.containsKey(pageNumber)) {
			pageContents.put(pageNumber, new ArrayList<String>());
		}
		pageContents.get(pageNumber).add(contentNumber);
	}
	
	/**
	 * Add the content of a page
	 * @param contentNumber the number of the Contents PDF object
	 * @param content the text in the content object
	 */
	public void addContent(String contentNumber, String content) {
		contents.put(contentNumber, content);
	}
	
	/**
	 * Returns an iterator that will return the PDF text one whole page at a time
	 * @return an iterator that will return the PDF text one whole page at a time
	 */
	public Iterator<String> iterator() {
		return new PageIterator();
	}
	
	public class PageIterator implements Iterator<String> {

		Iterator<String> itPages;
		
		PageIterator() {
			itPages = pageObjects.iterator();
		}
		
		@Override
		public boolean hasNext() {
			return itPages.hasNext();
		}

		@Override
		public String next() {
			StringBuilder sb = new StringBuilder();
			String page = itPages.next();
			Iterator<String> itContents = pageContents.get(page).iterator();
			while (itContents.hasNext()) {
				String text = contents.get(itContents.next());
				if (text != null) {
					sb.append(text);
				}
			}
					
			return sb.toString();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		
	}
}
