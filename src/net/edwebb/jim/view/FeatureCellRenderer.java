package net.edwebb.jim.view;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

import net.edwebb.jim.data.Feature;

/**
 * A renderer for a Feature in a List Cell. This class uses a JLabel to display the feature's icon, name and code.
 * @author Ed Webb
 *
 */
public class FeatureCellRenderer extends DefaultListCellRenderer {

	/**
	 * The version ID
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public Component getListCellRendererComponent(JList<? extends Object> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		JLabel label = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		Feature feature = (Feature)value;
		if (feature != null) {
			label.setText(feature.getName() + " (" + feature.getCode() + ")");
			label.setIcon(feature.getIcon());
		}
		return label;
	}

}
