package net.edwebb.jim.model;

import java.util.List;

import javax.swing.DefaultListModel;

import net.edwebb.mi.data.Feature;

public class FeatureListModel<T extends Feature> extends DefaultListModel<T> {

	/**
	 * The version ID
	 */
	private static final long serialVersionUID = 1L;
	
	List<T> features;
	
	public FeatureListModel(List<T> features) {
		this.features = features;
	}

	@Override
	public int getSize() {
		return features.size();
	}

	@Override
	public T getElementAt(int index) {
		return features.get(index);
	}


}
