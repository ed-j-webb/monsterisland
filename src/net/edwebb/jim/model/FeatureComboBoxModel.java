package net.edwebb.jim.model;

import java.util.List;

import javax.swing.ComboBoxModel;

import net.edwebb.mi.data.Feature;

public class FeatureComboBoxModel<T extends Feature> extends FeatureListModel<T> implements ComboBoxModel<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Object selected;
	
	public FeatureComboBoxModel(List<T> features) {
		super(features);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setSelectedItem(Object anItem) {
		this.selected = anItem;
	}

	@Override
	public Object getSelectedItem() {
		return selected;
	}

	
}
