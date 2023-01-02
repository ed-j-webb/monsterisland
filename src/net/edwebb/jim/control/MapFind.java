package net.edwebb.jim.control;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;

import net.edwebb.jim.model.FeatureComboBoxModel;
import net.edwebb.jim.view.FeatureCellRenderer;
import net.edwebb.jim.view.SpringUtilities;
import net.edwebb.mi.data.Creature;
import net.edwebb.mi.data.DataStore;
import net.edwebb.mi.data.Feature;
import net.edwebb.mi.data.Location;
import net.edwebb.mi.data.Plant;

/**
 * The Find dialog box allows a user to select a feature or string to search for. The getFind() method should be called to display this dialog
 * box and return a FindData object that contains the search information that the user has selected.
 * 
 * @author Ed Webb
 *
 */
public class MapFind extends JDialog {
	
	/**
	 * The version ID
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * The Location type
	 */
	public static final String LOCATION = "Location";
	
	/**
	 * The Creature type
	 */
	public static final String CREATURE = "Creature";
	
	/**
	 * The plant type
	 */
	public static final String PLANT = "Plant";
	
	/**
	 * The note type
	 */
	public static final String NOTE = "Note";

	
	// 3 different combo boxes depending on the type of feature the user is searching for
	// I would have liked to create one and change the model but the setModel method can 
	// only accept a model containing the exact class not a subclass so I've got 3 combo
	// boxes and swap them in and out of the panel.
	private JPanel panInput;
		private JComboBox<String> cmbType;
		private JComboBox<Location> cmbLocation;
		private JComboBox<Plant> cmbPlant;
		private JComboBox<Creature> cmbCreature;
		private JTextField txtNote;
		
		private JPanel panDistance;
			private ButtonGroup grpDistance;
			private JRadioButton optAll;
			private JRadioButton optSome;
			private JSpinner spnDistance;
		
	private JPanel panControl;
		private JButton cmdFind;
	
	private FeatureCellRenderer renderer;

	private Map<String, JComboBox<? extends Feature>> featureBoxes;

	/**
	 * Create a new Find dialog box
	 * @param parent the parent of this box
	 */
	public MapFind(Frame parent) {
		super(parent, "Find Feature", true);
		
		buildFeatureBoxes();
		
		this.setLayout(new BorderLayout());
		this.add(getPanInput(), BorderLayout.CENTER);
		this.add(getPanControl(), BorderLayout.SOUTH);
		Point point = new Point(parent.getLocation().x + 40, parent.getLocation().y + 40);
		this.setLocation(point);
	}
	
	/**
	 * Place the three comboboxes into a map keyed on the type.
	 */
	private void buildFeatureBoxes() {
		featureBoxes = new HashMap<String, JComboBox<? extends Feature>>();
		featureBoxes.put(LOCATION, getLocationComboBox());
		featureBoxes.put(CREATURE, getCreatureComboBox());
		featureBoxes.put(PLANT, getPlantComboBox());
	}
	
	/**
	 * Returns the panel containing the input fields
	 * @return the panel containing the input fields
	 */
	private JPanel getPanInput() {
		if (panInput == null) {
			panInput = new JPanel(new SpringLayout());
			panInput.add(getCmbType());
			panInput.add(getLocationComboBox());
			panInput.add(getDistancePanel());
			SpringUtilities.makeCompactGrid(panInput, 3, 1, 5, 5, 5, 5);
		}
		return panInput;
	}
	
	/**
	 * Returns the combo box that selects the type of search
	 * @return the combo box that selects the type of search
	 */
	private JComboBox<String> getCmbType() {
		if (cmbType == null) {
			String[] types = new String[] {LOCATION, PLANT, CREATURE, NOTE};
			cmbType = new JComboBox<String>(types);
			SpringUtilities.fixHeight(cmbType);
			cmbType.addItemListener(new ItemListener() {
				
				@Override
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						if (e.getItem().equals(NOTE)) {
							getPanInput().add(getTxtNote(), 1);
						} else {
							getPanInput().add(featureBoxes.get(e.getItem()), 1);
						}
						SpringUtilities.makeCompactGrid(getPanInput(), getPanInput().getComponentCount(), 1, 5, 5, 5, 5);
						getPanInput().validate();
						getRootPane().repaint();
					} else {
						if (e.getItem().equals(NOTE)) {
							getPanInput().remove(getTxtNote());
						} else { 
							getPanInput().remove(featureBoxes.get(e.getItem()));
						}
					}
				}
			});
		}
		return cmbType;
	}
	
	/**
	 * Returns the combo box that holds the locations
	 * @return the combo box that holds the locations
	 */
	private JComboBox<Location> getLocationComboBox() {
		if (cmbLocation == null) {
			cmbLocation = new JComboBox<Location>(new FeatureComboBoxModel<Location>(DataStore.getInstance().getLocations()));
			cmbLocation.setRenderer(getRenderer());
			SpringUtilities.fixHeight(cmbLocation);
		}
		return cmbLocation;
	}

	/**
	 * Returns the combo box that holds the plants
	 * @return the combo box that holds the plants
	 */
	private JComboBox<Plant> getPlantComboBox() {
		if (cmbPlant == null) {
			cmbPlant = new JComboBox<Plant>(new FeatureComboBoxModel<Plant>(DataStore.getInstance().getPlants()));
			cmbPlant.setRenderer(getRenderer());
			SpringUtilities.fixHeight(cmbPlant);
		}
		return cmbPlant;
	}

	/**
	 * Returns the combo box that holds the creatures
	 * @return the combo box that holds the creatures
	 */
	private JComboBox<Creature> getCreatureComboBox() {
		if (cmbCreature == null) {
			cmbCreature = new JComboBox<Creature>(new FeatureComboBoxModel<Creature>(DataStore.getInstance().getCreatures()));
			cmbCreature.setRenderer(getRenderer());
			SpringUtilities.fixHeight(cmbCreature);
		}
		return cmbCreature;
	}

	/**
	 * Returns the text box that holds the note
	 * @return the text box that holds the note
	 */
	private JTextField getTxtNote() {
		if (txtNote == null) {
			txtNote = new JTextField();
			SpringUtilities.fixHeight(txtNote);
		}
		return txtNote;
	}
	
	/**
	 * Returns the cell renderer for the feature combo boxes
	 * @return the cell renderer for the feature combo boxes
	 */
	private FeatureCellRenderer getRenderer() {
		if (renderer == null) {
			renderer = new FeatureCellRenderer();
		}
		return renderer;
	}
	
	/**
	 * Returns the panel that holds the distance inputs
	 * @return the panel that holds the distance inputs
	 */
	private JPanel getDistancePanel() {
		if (panDistance == null) {
			panDistance = new JPanel(new FlowLayout(FlowLayout.LEADING));
			panDistance.add(getOptAll());
			panDistance.add(getOptSome());
			panDistance.add(getSpnDistance());
			panDistance.add(new JLabel("squares"));
		}
		return panDistance;
	}
	
	/**
	 * Returns the button group for the distance radio buttons
	 * @return the button group for the distance radio buttons
	 */
	private ButtonGroup getGrpDistance() {
		if (grpDistance == null) {
			grpDistance = new ButtonGroup();
		}
		return grpDistance;
	}
	
	/**
	 * Returns the All radio button
	 * @return the All radio button
	 */
	private JRadioButton getOptAll() {
		if (optAll == null) {
			optAll = new JRadioButton("All");
			optAll.setSelected(true);
			getGrpDistance().add(optAll);
		}
		return optAll;
	}
	
	/**
	 * Returns the Some radio button
	 * @return the Some radio button
	 */
	private JRadioButton getOptSome() {
		if (optSome == null) {
			optSome = new JRadioButton("Within");
			getGrpDistance().add(optSome);
		}
		return optSome;
	}
	
	/**
	 * Returns the distance spinner
	 * @return the distance spinner
	 */
	private JSpinner getSpnDistance() {
		if (spnDistance == null) {
			spnDistance = new JSpinner(new SpinnerNumberModel(24, 0, 300, 1));
		}
		return spnDistance;
	}
	
	/**
	 * Returns the panel that contains the command button
	 * @return the panel that contains the command button
	 */
	private JPanel getPanControl() {
		if (panControl == null) {
			panControl = new JPanel(new FlowLayout(FlowLayout.TRAILING));
			panControl.add(getCmdFind());
		}
		return panControl;
	}

	/**
	 * Returns the command button
	 * @return the command button
	 */
	private JButton getCmdFind() {
		if (cmdFind == null) {
			cmdFind = new JButton("Find");
			cmdFind.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
					dispose();
				}
			});
		}
		return cmdFind;
	}

	/**
	 * Displays the find dialog box and returns a FindData object containing the user's selection once the dialog is closed
	 * @return a FindData object containing the user's selection
	 */
	public MapFind.FindData getFind() {
		this.pack();
		this.setVisible(true);
		int distance = 0;
		if (getOptAll().isSelected()) {
			distance = -1;
		} else {
			distance = Integer.valueOf(getSpnDistance().getValue().toString());
		}
		String type = getCmbType().getSelectedItem().toString();
		if (type.equals(NOTE)) {
			String s = getTxtNote().getText();
			return new FindData(s, distance);
		} else {
			Feature f = (Feature)featureBoxes.get(type).getSelectedItem();
			return new FindData(f, distance);
		}
	}
	
	/**
	 * An object that contains the selection that the user made in the Find dialog
	 * @author Ed Webb
	 *
	 */
	public class FindData {
		private Feature feature;
		private int distance;
		private String note;
		
		/**
		 * Create find data for a feature
		 * @param feature the feature to search for
		 * @param distance the distance to search or -1 for all
 		 */
		FindData(Feature feature, int distance) {
			this.feature = feature;
			this.distance = distance;
		}

		/**
		 * Create find data for a note
		 * @param note the string to search notes for
		 * @param distance the distance to search or -1 for all
 		 */
		FindData(String note, int distance) {
			this.note = note;
			this.distance = distance;
		}

		/**
		 * Returns the feature to search for
		 * @return the feature to search for
		 */
		public Feature getFeature() {
			return feature;
		}

		/**
		 * Returns the distance to search
		 * @return the distance to search
		 */
		public int getDistance() {
			return distance;
		}

		/**
		 * Returns the string to search notes for
		 * @return the string to search notes for
		 */
		public String getNote() {
			return note;
		}
		
		@Override
		public String toString() {
			if (feature != null) {
				return feature + " within " + distance;
			} else {
				return note + " within " + distance;
			}
		}
		
	}

}

