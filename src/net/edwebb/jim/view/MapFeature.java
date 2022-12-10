package net.edwebb.jim.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;

import net.edwebb.jim.data.Feature;

/**
 * A JPanel that contains a feature and a button that allows the feature to be removed from the square
 * 
 * @author Ed Webb
 *
 */
public class MapFeature extends JPanel {

	/**
	 * The version ID
	 */
	private static final long serialVersionUID = 1L;
	
	// The margin for the label
	private static final Insets margin = new Insets(2,2,2,2);
	
	// The feature to be displayed
	private Feature feature;
	
	// The label that the feature will be displayed on
	private JLabel name;
	
	private JPanel buttons;
	
	// The delete button
	private JButton delete;
	
	// The merge button
	private JButton merge;
	
	// The colour to shade the panel with
	private Color decoration;
	
	/** 
	 * Create a new MapFeature for the given feature
	 * @param feature the feature to display
	 */
	public MapFeature(Feature feature, Color decoration) {
		super(new BorderLayout());
		this.feature = feature;
		this.decoration = decoration;
		this.add(getButtons(), BorderLayout.EAST);
		if (decoration != null) {
			getButtons().add(getMerge());
		}
		getButtons().add(getDelete());
		this.add(getFeatureName(), BorderLayout.CENTER);
		this.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		SpringUtilities.fixHeight(this);
	}
	
	private JPanel getButtons() {
		if (buttons == null) {
			buttons = new JPanel();
			buttons.setOpaque(false);
		}
		return buttons;
	}
	
	/**
	 * Returns the delete button
	 * @return the delete button
	 */
	private JButton getDelete() {
		if (delete == null) {
			Icon icon = makeImageIcon("delete-16x16.gif");
			delete = new JButton(icon);
			delete.putClientProperty(EditPanel.FEATURE, feature);
			delete.putClientProperty(EditPanel.ACTION, EditPanel.DELETE);
			delete.setMargin(margin);
			delete.setBorderPainted(false);
			delete.setContentAreaFilled(false);	
			if (decoration != null) {
				delete.setOpaque(true);
				//delete.setBackground(decoration);
			}
		}
		return delete;
	}
	
	/**
	 * Returns the merge button
	 * @return the merge button
	 */
	private JButton getMerge() {
		if (merge == null) {
			Icon icon = makeImageIcon("merge-16x16.gif");
			merge = new JButton(icon);
			merge.putClientProperty(EditPanel.FEATURE, feature);
			merge.putClientProperty(EditPanel.ACTION, EditPanel.ADD);
			merge.setMargin(margin);
			merge.setBorderPainted(false);
			merge.setContentAreaFilled(false);			
			if (decoration != null) {
				merge.setOpaque(true);
				//merge.setBackground(decoration);
			}
		}
		return merge;
	}

	/**
	 * Returns the name label
	 * @return the name label
	 */
	private JLabel getFeatureName() {
		if (name == null) {
			name = new JLabel(feature.getName() + " (" + feature.getCode() + ")", feature.getIcon(), SwingConstants.LEFT);
			if (decoration != null) {
				name.setOpaque(true);
				name.setBackground(decoration);
			}
		}
		return name;
	}
	
	/**
	 * Adds the action listener to the delete button
	 * @param l the listener to add
	 */
	public void addActionListener(ActionListener l) {
		getDelete().addActionListener(l);
		if (decoration != null) {
			getMerge().addActionListener(l);
		}
	}
	
	/**
	 * Removes the action listener from the delete button
	 * @param l the listener to remove
	 */
	public void removeActionListener(ActionListener l) {
		getDelete().removeActionListener(l);
		if (decoration != null) {
			getMerge().removeActionListener(l);
		}
	}
	
	/**
	 * Creates an Image icon from the file specified
	 * @param path the path to the image file
	 * @return an ImageIcon based on the image in the file
	 */
	private ImageIcon makeImageIcon(String path) {
		URL url = this.getClass().getResource(path);
        if (url == null) {
            return null;
        } else {
            return new ImageIcon(url);
        }
	}
}
