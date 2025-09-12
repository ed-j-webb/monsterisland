package net.edwebb.jim.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.SpringLayout;
import javax.swing.border.TitledBorder;
import javax.swing.text.DefaultStyledDocument;

import net.edwebb.jim.MapConstants.ChangeType;
import net.edwebb.jim.model.FeatureComboBoxModel;
import net.edwebb.jim.model.MapModel;
import net.edwebb.jim.model.events.MapChangeEvent;
import net.edwebb.jim.model.events.MapChangeListener;
import net.edwebb.jim.model.events.MapSquareChangeEvent;
import net.edwebb.jim.model.events.SelectedChangeEvent;
import net.edwebb.jim.undo.ChangeUndoManager;
import net.edwebb.jim.undo.UndoListener;
import net.edwebb.jim.undo.UndoableChange;
import net.edwebb.jim.undo.UndoableCombinedChange;
import net.edwebb.jim.undo.UndoableMapChange;
import net.edwebb.mi.data.Creature;
import net.edwebb.mi.data.DataStore;
import net.edwebb.mi.data.Feature;
import net.edwebb.mi.data.Flag;
import net.edwebb.mi.data.Location;
import net.edwebb.mi.data.Plant;
import net.edwebb.mi.data.Terrain;

public class EditPanel extends JPanel implements MapChangeListener, UndoListener {

	/**
	 * The version ID
	 */
	private static final long serialVersionUID = 1L;

	protected static final Insets margin = new Insets(2,2,2,2);
	
	public static final String FEATURE = "feature";
	public static final String NOTES = "notes";
	
	public static final String ACTION = "action";
		public static final String DELETE = "delete";
		public static final String ADD = "add";
		public static final String FLAG = "flag";
	
	public static final Color PRIMARY = Color.MAGENTA;
	public static final Color SECONDARY = Color.PINK;
	public static final Color[] colours = new Color[] {null, PRIMARY, SECONDARY};
		
	private MapModel model;

	private JPanel editor;
		private JComboBox<Terrain> cmbTerrain;
		private JTextArea txtNotes;
		private JButton cmdNotes;
		private JPanel panFlags;
			private JToggleButton[] cmdFlag;
			private JButton cmdMergeFlag;

	private JPanel controls;
		private JComboBox<Location> cmbLocation;
		private JButton cmdLocation;
		private JComboBox<Plant> cmbPlant;
		private JButton cmdPlant;
		private JComboBox<Creature> cmbCreature;
		private JButton cmdCreature;

	private FeatureCellRenderer renderer;
	private TitledBorder border;

	
	private List<ActionListener> listeners = new ArrayList<ActionListener>();
	
	public EditPanel(MapModel model) {
		setModel(model);
		
		setLayout(new SpringLayout());
		add(getEditor());
		add(getControls());
		SpringUtilities.makeCompactGrid(this, this.getComponentCount(), 1, 5, 5, 5, 5);
	}
	
	public MapModel getModel() {
		return model;
	}
	
	public void setModel(MapModel model) {
		if (this.model != null) {
			this.model.removeMapChangeListener(this);
		}
		this.model = model;
		this.model.addMapChangeListener(this);
	}
	
	
	
	@Override
	public void mapChanged(MapChangeEvent event) {
		if (event.getChangeType().equals(ChangeType.SELECTED)) {
			SelectedChangeEvent selectedEvent = (SelectedChangeEvent)event;
			if (selectedEvent.getNewSelected() != null) {
				refresh(selectedEvent.getNewSelected());
			}
			return;
		}

		if (event.getChangeType().equals(ChangeType.COORDINATE)) {
			refresh(model.getSelected());
		}
		
		if (event.getChangeType().equals(ChangeType.FEATURE)
		 || event.getChangeType().equals(ChangeType.TERRAIN)
		 || event.getChangeType().equals(ChangeType.FLAG)
		 || event.getChangeType().equals(ChangeType.NOTE)) {
			MapSquareChangeEvent squareEvent = (MapSquareChangeEvent)event;
			if (squareEvent.getSquare().equals(model.getSelected())) {
				refresh(squareEvent.getSquare());
			}
			return;
		}
	}
	
	@Override
	public void undoManagerChanged(ChangeUndoManager manager) {
	}
	
	@Override
	public void changeMade(UndoableChange change, boolean undone) {
		if (change instanceof UndoableMapChange) {
			UndoableMapChange mapChange = (UndoableMapChange)change;
			if (mapChange.getSquare().equals(model.getSelected())) {
				refresh(mapChange.getSquare());
			}
			return;
		}
		
		if (change instanceof UndoableCombinedChange) {
			UndoableCombinedChange combinedChange = (UndoableCombinedChange)change;
			if (combinedChange.contains(model.getSelected())) {
				refresh(model.getSelected());
			}
			return;
		}
	}

	private void refresh(Point position) {
		while (getEditor().getComponentCount() > 4) {
			getEditor().remove(2);
		}

		if (position == null) {
			getTitle().setTitle("(y, x)");
			getTerrainComboBox().setSelectedItem(null);
			getNotesBox().setText("");
			clearFlags();
		} else {
			getTitle().setTitle("(" + (position.y + getModel().getOffset().y) + ", " + (position.x + getModel().getOffset().x) + ")");

			short[] square = model.getSquare(position);
			if (square != null && square.length > 0) {
				getTerrainComboBox().setSelectedItem(DataStore.getInstance().getTerrain(square[0]));
				for (int i = 1; i < square.length; i++) {
					Feature f = DataStore.getInstance().getFeatureById(square[i]);
					if (f != null) {
						int extra = model.getExtra(position, f);
						MapFeature mf = new MapFeature(f, colours[extra]);
						for (Iterator<ActionListener> it = listeners.iterator(); it.hasNext();) {
							mf.addActionListener(it.next());
						}
						getEditor().add(mf, getEditor().getComponentCount() - 2);
					}
				}
				setFlags(model);
			} else {
				getTerrainComboBox().setSelectedItem(null);
				clearFlags();
			}
			
			String note = model.getSquareNote(position);
			if (note != null) {
				getNotesBox().setText(note);
			} else {
				getNotesBox().setText("");
			}
		}
		SpringUtilities.makeCompactGrid(getEditor(), getEditor().getComponentCount(), 1, 5, 5, 5, 5);
		this.validate();
		this.repaint();
	}
	
	private void clearFlags() {
		for (int i = 0; i < getCmdFlag().length; i++) {
			getCmdFlag()[i].setSelected(false);
			getCmdFlag()[i].setBackground(null);
			getMergeFlag().setVisible(false);
		}
	}
	
	private void setFlags(MapModel model) {
		boolean diff = false;
		for (int i = 0; i < getCmdFlag().length; i++) {
			JToggleButton button = getCmdFlag()[i];
			Flag flag = (Flag)button.getClientProperty(FEATURE);
			if ((model.isFlagged(model.getSelected(), flag))) {
				button.setSelected(true);
			} else {
				button.setSelected(false);
			}
			int extra = model.getExtra(model.getSelected(), flag);
			if (extra == 0) {
				button.setContentAreaFilled(true);
				button.setOpaque(false);
			} else {
				diff = true;
				button.setContentAreaFilled(false);
				button.setOpaque(true);
				button.setBackground(colours[extra]);
			}
		}
		
		if (diff) {
			getMergeFlag().setVisible(true);
		} else {
			getMergeFlag().setVisible(false);
		}
	}

//	private void setFlags(short flags) {
//		for (int i = 0; i < getCmdFlag().length; i++) {
//			short id = ((Flag)getCmdFlag()[i].getClientProperty(FEATURE)).getId();
//			id = (short)(Math.pow(2, id));
//			if ((flags & id) > 0) {
//				getCmdFlag()[i].setSelected(true);
//			} else {
//				getCmdFlag()[i].setSelected(false);
//			}
//		}
//	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
	}

	private TitledBorder getTitle() {
		if (border == null) {
			border = BorderFactory.createTitledBorder("(y, x)");
		}
		return border;
	}
	
	private JPanel getEditor() {
		if (editor == null) {
			editor = new JPanel(new SpringLayout());
			editor.setBorder(getTitle());
			editor.add(getFlags());
			editor.add(getTerrainComboBox());
			editor.add(getNotesBox());
			editor.add(getNotesButton());
			SpringUtilities.makeCompactGrid(editor, editor.getComponentCount(), 1, 5, 5, 5, 5);
		}
		return editor;
	}
	
	private JPanel getControls() {
		if (controls == null) {
			controls = new JPanel(new SpringLayout());
			controls.add(getLocationComboBox());
			controls.add(getLocationButton());
			controls.add(getPlantComboBox());
			controls.add(getPlantButton());
			controls.add(getCreatureComboBox());
			controls.add(getCreatureButton());
			SpringUtilities.makeCompactGrid(controls, controls.getComponentCount() / 2, 2, 5, 5, 5, 5);
		}
		return controls;
		
	}
	
	private FeatureCellRenderer getRenderer() {
		if (renderer == null) {
			renderer = new FeatureCellRenderer();
		}
		return renderer;
	}
	
	private JComboBox<Terrain> getTerrainComboBox() {
		if (cmbTerrain == null) {
			cmbTerrain = new JComboBox<Terrain>(new FeatureComboBoxModel<Terrain>(DataStore.getInstance().getTerrain()));
			cmbTerrain.setRenderer(getRenderer());
			SpringUtilities.fixHeight(cmbTerrain);
		}
		return cmbTerrain;
	}
	
	private JPanel getFlags() {
		if (panFlags == null) {
			panFlags = new JPanel(new FlowLayout(FlowLayout.LEFT));
			cmdFlag = new JToggleButton[DataStore.getInstance().getFlags().size()];
			Iterator<Flag> it = DataStore.getInstance().getFlags().iterator();
			int index = 0;
			while (it.hasNext()) {
				Flag f = it.next();
				JToggleButton but = new JToggleButton(f.getIcon());
				but.setToolTipText(f.getName() + " (" + f.getCode() + ")");
				but.setMargin(margin);
				but.putClientProperty(FEATURE, f);
				but.putClientProperty(ACTION, FLAG);
				panFlags.add(but);
				cmdFlag[index++] = but;
			}
			
			Icon icon = makeImageIcon("merge-16x16.gif");
			cmdMergeFlag = new JButton(icon);
			cmdMergeFlag.setToolTipText("Merge Flags");
			cmdMergeFlag.setMargin(new Insets(1,1,0,0));
			cmdMergeFlag.putClientProperty(ACTION, FLAG);
			panFlags.add(cmdMergeFlag);
			SpringUtilities.fixHeight(panFlags);
		}
		return panFlags;
	}
	
	private JToggleButton[] getCmdFlag() {
		if (cmdFlag == null) {
			getFlags();
		}
		return cmdFlag;
	}
	
	private JButton getMergeFlag() {
		if (cmdMergeFlag == null) {
			getFlags();
		}
		return cmdMergeFlag;
	}
	
	private JTextArea getNotesBox() {
		if (txtNotes == null) {
			txtNotes = new JTextArea(3, 10);
			txtNotes.setPreferredSize(new Dimension(100, 100));
			SpringUtilities.fixHeight(txtNotes);

			DefaultStyledDocument doc = new DefaultStyledDocument();
            doc.setDocumentFilter(new DocumentSizeFilter(125));
            txtNotes.setDocument(doc);
		}
		return txtNotes;
	}
	
	private JButton getNotesButton() {
		if (cmdNotes == null) {
			Icon icon = makeImageIcon("add-16x16.gif");
			cmdNotes = new JButton(icon) {

				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				@Override
				protected void fireActionPerformed(ActionEvent event) {
					putClientProperty(EditPanel.NOTES, getNotesBox().getText());
					super.fireActionPerformed(event);
				}
				
			};
			cmdNotes.setMargin(margin);
			cmdNotes.setContentAreaFilled(false);	
			cmdNotes.putClientProperty(EditPanel.ACTION, EditPanel.ADD);
		}
		return cmdNotes;
	}

	private JComboBox<Location> getLocationComboBox() {
		if (cmbLocation == null) {
			cmbLocation = new JComboBox<Location>(new FeatureComboBoxModel<Location>(DataStore.getInstance().getLocations()));
			cmbLocation.setRenderer(getRenderer());
			SpringUtilities.fixHeight(cmbLocation);
		}
		return cmbLocation;
	}

	private JComboBox<Plant> getPlantComboBox() {
		if (cmbPlant == null) {
			cmbPlant = new JComboBox<Plant>(new FeatureComboBoxModel<Plant>(DataStore.getInstance().getPlants()));
			cmbPlant.setRenderer(getRenderer());
			SpringUtilities.fixHeight(cmbPlant);
		}
		return cmbPlant;
	}

	private JComboBox<Creature> getCreatureComboBox() {
		if (cmbCreature == null) {
			cmbCreature = new JComboBox<Creature>(new FeatureComboBoxModel<Creature>(DataStore.getInstance().getCreatures()));
			cmbCreature.setRenderer(getRenderer());
			SpringUtilities.fixHeight(cmbCreature);
		}
		return cmbCreature;
	}

	private JButton getLocationButton() {
		if (cmdLocation == null) {
			Icon icon = makeImageIcon("add-16x16.gif");
			cmdLocation = new JButton(icon) {

				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				@Override
				protected void fireActionPerformed(ActionEvent event) {
					putClientProperty(EditPanel.FEATURE, getLocationComboBox().getSelectedItem());
					super.fireActionPerformed(event);
				}
				
			};
			cmdLocation.setMargin(margin);
			cmdLocation.setContentAreaFilled(false);			
			
			cmdLocation.putClientProperty(EditPanel.ACTION, EditPanel.ADD);
		}
		return cmdLocation;
	}
	
	private JButton getPlantButton() {
		if (cmdPlant == null) {
			Icon icon = makeImageIcon("add-16x16.gif");
			cmdPlant = new JButton(icon) {

				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				@Override
				protected void fireActionPerformed(ActionEvent event) {
					putClientProperty(EditPanel.FEATURE, getPlantComboBox().getSelectedItem());
					super.fireActionPerformed(event);
				}
				
			};
			cmdPlant.setMargin(margin);
			cmdPlant.setContentAreaFilled(false);			

			cmdPlant.putClientProperty(EditPanel.ACTION, EditPanel.ADD);
		}
		return cmdPlant;
	}
	
	private JButton getCreatureButton() {
		if (cmdCreature == null) {
			Icon icon = makeImageIcon("add-16x16.gif");
			cmdCreature = new JButton(icon) {

				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				@Override
				protected void fireActionPerformed(ActionEvent event) {
					putClientProperty(EditPanel.FEATURE, getCreatureComboBox().getSelectedItem());
					super.fireActionPerformed(event);
				}
				
			};
			cmdCreature.setMargin(margin);
			cmdCreature.setContentAreaFilled(false);			
			cmdCreature.putClientProperty(EditPanel.ACTION, EditPanel.ADD);
		}
		return cmdCreature;
	}

	private ImageIcon makeImageIcon(String path) {
		URL url = this.getClass().getResource(path);
        if (url == null) {
            return null;
        } else {
            return new ImageIcon(url);
        }
	}
	
	public void addActionListener(ActionListener l) {
		listeners.add(l);
		for(int i = 2; i < getEditor().getComponentCount() - 2; i++) {
			((MapFeature)getEditor().getComponent(i)).addActionListener(l);
		}
		getLocationButton().addActionListener(l);
		getCreatureButton().addActionListener(l);
		getPlantButton().addActionListener(l);
		getNotesButton().addActionListener(l);
		getMergeFlag().addActionListener(l);
		for (int i = 0; i < getCmdFlag().length; i++) {
			getCmdFlag()[i].addActionListener(l);
		}
	}
	
	public void removeActionListener(ActionListener l) {
		listeners.remove(l);
		for(int i = 2; i < getEditor().getComponentCount(); i++) {
			((MapFeature)getEditor().getComponent(i)).removeActionListener(l);
		}
		getLocationButton().removeActionListener(l);
		getCreatureButton().removeActionListener(l);
		getPlantButton().removeActionListener(l);
		getNotesButton().removeActionListener(l);
		for (int i = 0; i < getCmdFlag().length; i++) {
			getCmdFlag()[i].removeActionListener(l);
		}
	}
	
	public void addItemListener(ItemListener l) {
		getTerrainComboBox().addItemListener(l);
	}

	public void removeItemListener(ItemListener l) {
		getTerrainComboBox().removeItemListener(l);
	}
}
