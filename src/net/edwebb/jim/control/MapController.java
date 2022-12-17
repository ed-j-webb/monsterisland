package net.edwebb.jim.control;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.BevelBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

import net.edwebb.jim.data.MapData;
import net.edwebb.jim.data.MapIndex;
//import net.edwebb.jim.factory.Diff;
import net.edwebb.jim.factory.FactoryManager;
//import net.edwebb.jim.factory.Merge;
import net.edwebb.jim.model.DiffMapModel;
import net.edwebb.jim.model.MapModel;
import net.edwebb.jim.model.MapSearch;
import net.edwebb.jim.model.StandardMapModel;
import net.edwebb.jim.model.UndoableChange;
import net.edwebb.jim.model.UndoableCombinedChange;
import net.edwebb.jim.model.UndoableMapChange;
import net.edwebb.jim.view.EditPanel;
import net.edwebb.jim.view.MiniMap;
import net.edwebb.jim.view.ViewPanel;
import net.edwebb.mi.data.Coordinate;
import net.edwebb.mi.data.DataStore;
import net.edwebb.mi.data.Feature;
import net.edwebb.mi.data.Flag;
import net.edwebb.mi.data.Terrain;
import net.edwebb.mi.extract.MIExtractor;
import net.edwebb.mi.extract.Sighting;
import net.edwebb.mi.extract.Stats;
import net.edwebb.mi.extract.Turn;
import net.edwebb.mi.pdf.PDFExtractor;
import net.edwebb.mi.reader.TurnDigester;
import net.edwebb.mi.reader.TurnReader;

/**
 * The controller of the application. Every change comes through this class. It is responsible for responding to Events raised by the
 * view Components, updating the MapModel and notifying those Components of those changes.
 * @author Ed Webb
 *
 */
public class MapController {

	// Map Square Sizes. Everything works on a base of 26 pixels
	private static final int SMALL_SQUARE = 26;
	private static final int MEDIUM_SQUARE = 52;
	private static final int LARGE_SQUARE = 78;
	private static final String[] SQUARE_SIZES = new String[] {"Small", "Medium", "Large"};
	
	// The frame of the application
	private JFrame frame;

	// The file chooser used by Open, Save and Translate actions
	private JFileChooser save = null;
	private JFileChooser load = null;
	private JFileChooser trans = null;
	private JFileChooser dir = null;
	private JFileChooser csv = null;
	private JFileChooser extract = null;
	
	// The panel that contains the main map display components
	private ViewPanel panView;
		private MouseAdapter selectSquare;
		private AdjustmentListener scrollAdjust;
		private ComponentListener mapResize;
		private KeyListener mapKeyPress;
	
	// The panel that contains the square editing components
	private EditPanel panEdit;
		private ActionListener editSquare;
		private ItemListener selectTerrain;
		
	// The panel that contains the minimap
	private MiniMap miniMap;
		private MouseListener selectView;
	
	// The applications toolbar
	private JToolBar toolBar;
		private NewAction newAction;
		private OpenAction openAction;
		private SaveAction saveAction;
		private TranslateAction translateAction;
		private FindAction findAction;
		private UndoAction undoAction;
		private RedoAction redoAction;
		private JComboBox<Integer> cmbSize;
		private JComboBox<Coordinate> cmbCoords;
		private CoordAction coordAction;
		//private DiffAction diffAction;
		private CompareAction compAction;
		private SaveDifferencesAction saveDifferencesAction;
		private SaveMergedAction saveMergedAction;
		//private MergeAction mergeAction;
		private JToggleButton cmdFlag;
		private ScryeAction scryeAction;
		private ExtractAction extractAction;
	
	// The status bar	
	private JPanel panStatus;
		private JLabel lblMap;
		private JLabel lblStatus;
	
	// The currently open MapModel
	private MapModel model;
	
	// The index of the current MapModel
	private MapIndex index;
	
	// the current search of the MapModel
	private MapSearch search;
	
	// The class that handles undo/redo
	private ChangeUndoManager undoManager;
	
	// Flag to show if the controller is in the process of updating the view
	private boolean updating;
	
	// The new map dimensions dialog box
	private MapDimensions frmDimensions;
	
	// The find dialog box
	private MapFind frmFind;
	
	// The text dialog box
	private MapText frmText;
	
	// The long text dialog box
	private JTextArea txtNote;
	private JScrollPane scrNote;
	
	/**
	 *  Creates a new MapController with an empty 100x100 map.
	 */
	public MapController() {
		this(new StandardMapModel(MEDIUM_SQUARE, new MapData(0, 0, 100, 100), "Unsaved.jim"));
	}

	/**
	 *  Creates a new MapController with the provided MapModel.
	 * @param model the model that the controller should use
	 */
	public MapController(MapModel model) {
		this.setModel(model);
	}

	/**
	 * Sets a new model for the controller to use. 
	 * @param model the model that the controller should use
	 */
	public void setModel(MapModel model) {
		this.model = model;
		setSearch(null);
		buildIndex(model);
		undoManager = new ChangeUndoManager();
		setMapLabel(model);
		getView().setModel(model);
		getEdit().setModel(model);
		getMiniMap().setModel(model);
		addCoOrdinates(getCmbCoords(), model);
		getCmbCoords().setSelectedItem(model.getCurrentCoOrdinates());
		if (getFrame().isVisible()) {
			resize(getView().getDimension());
			refresh();
		}
		if (model instanceof DiffMapModel) {
			getSaveDifferencesAction().setEnabled(true);
			getSaveMergedAction().setEnabled(true);
			getCompareAction().setEnabled(false);
		} else {
			getSaveDifferencesAction().setEnabled(false);
			getSaveMergedAction().setEnabled(false);
			getCompareAction().setEnabled(true);
		}
	}

	private void setMapLabel(MapModel model) {
		StringBuilder sb = new StringBuilder(100);
		sb.append(model.getName());
		sb.append("  |  ");
		Rectangle bounds = model.getBounds();
		if (model.getDefaultCoOrdinates() != null) {
			sb.append(model.getDefaultCoOrdinates().getName());
		} else {
			sb.append("Unknown");
		}
		sb.append(" (");
		sb.append(bounds.y);
		sb.append(",");
		sb.append(bounds.x);
		sb.append(") ");
		sb.append(bounds.width);
		sb.append("x");
		sb.append(bounds.height);
		sb.append("  |  ");
		getMapLabel().setText(sb.toString());
	}
	
	/**
	 * Adds the model's coOrdinate system to the combobox if it is not already present
	 * 
	 * @param cmbBox the Co-ordinates combo box
	 * @param model the map model
	 */
	private void addCoOrdinates(JComboBox<Coordinate> cmbBox, MapModel model) {
		if (model.getDefaultCoOrdinates() == null) {
			return;
		}
		for (int i = 0; i < cmbBox.getItemCount(); i++) {
			if (cmbBox.getItemAt(i) == model.getDefaultCoOrdinates()) {
				return;
			}
		}
		cmbBox.addItem(model.getDefaultCoOrdinates());
	}
	
	/**
	 * Shows the application's Frame.
	 */
	public void showFrame() {
		final JFrame f = getFrame();
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				f.pack();
				f.setVisible(true);
			}
		});
	}
	
	/**
	 * Returns the MapModel the controller is using
	 * @return the MapModel the controller is using
	 */
	public MapModel getModel() {
		return model;
	}
	
	public MapIndex getIndex() {
		if (index == null) {
			buildIndex(getModel());
		}
		return index;
	}
	
	public MapSearch getSearch() {
		if (search == null) {
			setSearch(new MapSearch());
		}
		return search;
	}
	
	/**
	 * Sets a new search for the controller to use. 
	 * @param search the search that the controller should use
	 */
	public void setSearch(MapSearch search) {
		this.search = search;
		getView().setSearch(search);
		getMiniMap().setSearch(search);
	}
	
	/**
	 * Sets a new index for the controller to use
	 * @param model the model to build the index from 
	 */
	public void buildIndex(MapModel model) {
		index = new MapIndex();
		index.index(model);
		getMiniMap().setIndex(index);
	}
	
	/**
	 * Returns the Frame of the application
	 * @return the Frame of the application
	 */
	public JFrame getFrame() {
		if (frame == null) {
			frame = new JFrame();
			frame.setTitle("Java Island Mapper");
			frame.setLayout(new BorderLayout());
			frame.add(getView(), BorderLayout.CENTER);
			JPanel panRight = new JPanel(new BorderLayout());
			frame.add(panRight, BorderLayout.EAST);
			panRight.add(getEdit(), BorderLayout.CENTER);
			panRight.add(getMiniMap(), BorderLayout.SOUTH);
			frame.add(getToolBar(), BorderLayout.NORTH);
			frame.add(getStatusBar(), BorderLayout.SOUTH);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		}
		return frame;
	}
	
	/**
	 * Returns the Panel that holds the components that display the map 
	 * @return the Panel that holds the components that display the map
	 */
	public ViewPanel getView() {
		if (panView == null) {
			panView = new ViewPanel(getModel());
			panView.addAdjustmentListener(getScrollBarAdjustmentListener());
			panView.addKeyListener(getMapKeyPressListener());
			panView.addComponentListener(getMapResizeListener());
			panView.addMouseListener(getSelectSquareListener());
			panView.addMouseMotionListener(getSelectSquareListener());
		}
		return panView;
	}

	/**
	 * Returns the Adjustment Listener that is listening for a change in the value of the scroll bars 
	 * @return the Adjustment Listener that is listening for a change in the value of the scroll bars
	 */
	public AdjustmentListener getScrollBarAdjustmentListener() {
		if (scrollAdjust == null) {
			scrollAdjust = new AdjustmentListener() {
				
				@Override
				public void adjustmentValueChanged(AdjustmentEvent e) {
					Rectangle rect = getModel().getView();
					int orientation = ((JScrollBar)e.getSource()).getOrientation();
					if (orientation == JScrollBar.HORIZONTAL) {
						rect = new Rectangle(e.getValue(), rect.y, rect.width, rect.height);
					} else {
						rect = new Rectangle(rect.x, -e.getValue(), rect.width, rect.height);
					}
					getModel().setView(bound(rect));
					refresh();
				}
			};
		}
		return scrollAdjust;
	}
	
	/**
	 * Returns the Component Listener that is listening for a change in the size of the MapPanel
	 * @return the Component Listener that is listening for a change in the size of the MapPanel
	 */
	public ComponentListener getMapResizeListener() {
		if (mapResize == null) {
			mapResize = new ComponentAdapter() {

				@Override
				public void componentResized(ComponentEvent e) {
					resize(getView().getDimension());
					refresh();
				}

			};
		}
		return mapResize;
	}
	
	/**
	 * Returns the Key Listener that is listening for arrow key presses on the MapPanel
	 * @return the Key Listener that is listening for arrow key presses on the MapPanel
	 */
	public KeyListener getMapKeyPressListener() {
		if (mapKeyPress == null) {
			mapKeyPress = new KeyAdapter() {
				
				@Override
				public void keyPressed(KeyEvent e) {
					boolean scroll = false;
					Rectangle rect = getModel().getView();
					Point sel = getModel().getSelected();
					// Scroll the view
					if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
						rect = new Rectangle(rect.x + 1, rect.y, rect.width, rect.height);
						sel = new Point(sel.x + 1, sel.y);
						scroll = true;
					} else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
						rect = new Rectangle(rect.x - 1, rect.y, rect.width, rect.height);
						sel = new Point(sel.x - 1, sel.y);
						scroll = true;
					} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
						rect = new Rectangle(rect.x, rect.y - 1, rect.width, rect.height);
						sel = new Point(sel.x, sel.y - 1);
						scroll = true;
					} else if (e.getKeyCode() == KeyEvent.VK_UP) {
						rect = new Rectangle(rect.x, rect.y + 1, rect.width, rect.height);
						sel = new Point(sel.x, sel.y + 1);
						scroll = true;
					} else {
						// Update the terrain
						Terrain t = DataStore.getInstance().getTerrain((short)e.getKeyCode());
						if (t != null) {
							handleFeatureEdit(getModel().getSelected(), EditPanel.ADD, t);
						}
						// Flag the square
						Flag f = net.edwebb.mi.data.DataStore.getInstance().getFlag((short)(e.getKeyCode() - 48));
						if (f != null) {
							if (e.isShiftDown()) {
								handleFlagEdit(getModel().getSelected(), f);
							} else if (e.isControlDown()) {
								paintFlag(getModel().getSelected(), f, false);
							} else {
								paintFlag(getModel().getSelected(), f, true);
							}
						}
					}
					if (scroll == true) {
						getModel().setView(bound(rect));
						getModel().setSelected(bound(sel));
					}
					refresh();
				}
			};
		}
		return mapKeyPress;
	}

	private void paintFlag(Point p, Flag f, boolean on) {
		UndoableCombinedChange change = new UndoableCombinedChange("(" + p.y + "," + p.x + ") " + (on ? "Add " : "Remove ") + " " + f.getName() + " flag");
		Point s = new Point(p);
		Point r = new Point(s); 
		for (short x = (short)(p.x - f.getRange() + 1); x < p.x + f.getRange(); x++) {
			for (short y = (short)(p.y + f.getRange() -1); y > p.y - f.getRange(); y--) {
				s.move(x, y);
				r = bound(s);
				// Only set the flag if s and t are the same (i.e. s is not off the edge of the map)
				if (s.equals(r)) {
					change.addChange(getModel().toggleFlag(s, f.getId(), on ? MapModel.ON : MapModel.OFF));
				}
			}
		}
		if (change.hasChanges()) {
			undoManager.addEdit(change);
		}
	}
	
	/**
	 * Returns the Mouse listener that listens for the user clicking on the MapPanel to select a square or mousing over for a tooltip
	 * @return the Mouse listener that listens for the user clicking on the MapPanel to select a square or mousing over for a tooltip
	 */
	public MouseAdapter getSelectSquareListener() {
		if (selectSquare == null) {
			selectSquare = new MouseAdapter() {

				@Override
				public void mouseClicked(MouseEvent e) {
					Rectangle rect = getModel().getView();
					Point p = new Point(rect.x + (e.getPoint().x / (getModel().getSize() + 1)), rect.y - (e.getPoint().y / (getModel().getSize() + 1)));
					e.getComponent().requestFocusInWindow();
					getModel().setView(bound(rect));
					getModel().setSelected(bound(p));
					refresh();
				}

				@Override
				public void mouseMoved(MouseEvent e) {
					Rectangle rect = getModel().getView();
					Point p = new Point(rect.x + (e.getPoint().x / (getModel().getSize() + 1)), rect.y - (e.getPoint().y / (getModel().getSize() + 1)));
					String note = getModel().getSquareNote(p);
					((JComponent)e.getComponent()).setToolTipText(note);
				}
				
				
			};
		}
		return selectSquare;
	}
	
	/**
	 * Returns the panel that contains the square editing components
	 * @return the panel that contains the square editing components
	 */
	public EditPanel getEdit() {
		if (panEdit == null) {
			panEdit = new EditPanel(getModel());
			panEdit.addActionListener(getEditSquareListener());
			panEdit.addItemListener(getSelectTerrainListener());
		}
		return panEdit;
	}
	
	/**
	 * Returns the Action Listener that listens for the user clicking a delete feature, add feature or save note button
	 * @return the Action Listener that listens for the user clicking a delete feature, add feature or save note button
	 */
	public ActionListener getEditSquareListener() {
		if (editSquare == null) {
			editSquare = new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					Point pos = getModel().getSelected();
					if (pos == null) {
						return;
					}

					JComponent comp = (JComponent)e.getSource(); 
					Feature f = (Feature)comp.getClientProperty(EditPanel.FEATURE);
					String action = (String)comp.getClientProperty(EditPanel.ACTION);
					if (f != null) {
						if (action.equals(EditPanel.FLAG)) {
							handleFlagEdit(pos, f);
						} else {
							handleFeatureEdit(pos, action, f);
						}
						return;
					} else if (action.equals(EditPanel.FLAG)) {
						mergeFlags(pos);
					}
					String note = (String)comp.getClientProperty(EditPanel.NOTES);
					if (note != null) {
						handleNoteEdit(pos, note);
					}
				}
			};
		}
		return editSquare;
	}
	
	/**
	 * Returns the Item Listener that is listening for a change to the terrain Combo box for the selected square
	 * @return the Item Listener that is listening for a change to the terrain Combo box for the selected square
	 */
	public ItemListener getSelectTerrainListener() {
		if (selectTerrain == null) {
			selectTerrain = new ItemListener() {
				
				@Override
				public void itemStateChanged(ItemEvent e) {
					if (!updating && e.getStateChange() == ItemEvent.SELECTED) {
						Point pos = getModel().getSelected();
						Terrain t = (Terrain)e.getItem();
						if (pos == null || t == null) {
							return;
						}
						handleFeatureEdit(pos, EditPanel.ADD, t);
						refresh();
					}
				}
			};
		}
		return selectTerrain;
	}
	
	/**
	 * Updates the selected square's notes in the MapModel
	 * @param pos the co-ordinates of the square
	 * @param note the new notes
	 */
	private void handleNoteEdit(Point pos, String note) {
		UndoableCombinedChange change = new UndoableCombinedChange(" (" + pos.y + "," + pos.x + ") add note '" + note + "'");

		change.addChange(getIndex().removeNote(getModel().getSquareNote(pos), pos));
		change.addChange(getModel().setSquareNote(pos, note));
		change.addChange(getIndex().addNote(note, pos));
		if (change.hasChanges()) {
			undoManager.addEdit(change);
		}
		refresh();
	}
	
	/**
	 * Adds or deletes a feature from the selected square 
	 * @param pos the co-ordinates of the square
	 * @param action the action to take. This is one of the EditPanel constants ADD or DELETE
	 * @param f the feature to add or delete
	 */
	private UndoableCombinedChange handleFeatureEdit(Point pos, String action, Feature f) {
		UndoableCombinedChange change = null;
		getIndex().removeExtra(getModel().getExtra(pos, Short.MIN_VALUE), pos);
		if (action.equals(EditPanel.ADD)) {
			if (f instanceof Terrain) {
				change = new UndoableCombinedChange(" (" + pos.y + "," + pos.x + ") change terrain to " + f.getName());
				// Poor design the miniMap change MUST go before mapModel change
				change.addChange(getMiniMap().setPoint(pos, (Terrain)f));
				change.addChange(getModel().setTerrain(pos, f.getId()));
			} else {
				change = new UndoableCombinedChange("(" + pos.y + "," + pos.x + ") add " + f.getName());
				change.addChange(getModel().add(pos, f.getId()));
				change.addChange(getIndex().addPoint(f, pos));
			}
		} else if (action.equals(EditPanel.DELETE)) {
			change = new UndoableCombinedChange("(" + pos.y + "," + pos.x + ") remove " + f.getName());
			change.addChange(getModel().remove(pos, f.getId()));
			change.addChange(getIndex().removePoint(f, pos));
		}
		getIndex().addExtra(getModel().getExtra(pos, Short.MIN_VALUE), pos);

		if (change.hasChanges()) {
			undoManager.addEdit(change);
		}
		refresh();
		if (change.hasChanges()) {
			return change;
		} else {
			return null;
		}
	}
	
	private void handleFlagEdit(Point pos, Feature f) {
		getIndex().removeExtra(getModel().getExtra(pos, Short.MIN_VALUE), pos);
		UndoableChange change = getModel().toggleFlag(pos, f.getId(), MapModel.INVERSE);
		if (change != null) {
			change.setPresentationName("(" + pos.y + "," + pos.x + ") toggle " + f.getName() + " flag");
			undoManager.addEdit(change);
		}
		getIndex().addExtra(getModel().getExtra(pos, Short.MIN_VALUE), pos);
		refresh();
	}
	
	private void mergeFlags(Point pos) {
		UndoableCombinedChange change = new UndoableCombinedChange("(" + pos.y + "," + pos.x + ") " );
		getIndex().removeExtra(getModel().getExtra(pos, Short.MIN_VALUE), pos);
		Iterator<Flag> it = DataStore.getInstance().getFlags().iterator();
		while (it.hasNext()) {
			Flag f = it.next();
			if (getModel().isFlagged(pos, f.getId())) {
				change.addChange(getModel().toggleFlag(pos, f.getId(), MapModel.ON));
			}
		}
		if (change != null) {
			change.setPresentationName("(" + pos.y + "," + pos.x + ") merge flags (not really sure what this does!)");
			undoManager.addEdit(change);
		}
		getIndex().addExtra(getModel().getExtra(pos, Short.MIN_VALUE), pos);
		refresh();
	}
	
	/**
	 * Returns the minimap component
	 * @return the minimap component
	 */
	public MiniMap getMiniMap() {
		if (miniMap == null) {
			miniMap = new MiniMap(getModel());
			miniMap.addMouseListener(getSelectViewListener());
		}
		return miniMap;
	}
	
	/**
	 * Returns the Mouse listener that listens for the user clicking on the MiniMap to select a view
	 * @return the Mouse listener that listens for the user clicking on the MiniMap to select a view
	 */
	public MouseListener getSelectViewListener() {
		if (selectView == null) {
			selectView = new MouseAdapter() {

				@Override
				public void mouseClicked(MouseEvent e) {
					Rectangle bounds = getModel().getBounds();
					Rectangle view = getModel().getView();
					Point p = new Point(e.getPoint());
					p.translate(-view.width / 2 + bounds.x, -view.height/2);
					p.move(p.x, bounds.y - p.y);
					getModel().setView(new Rectangle(p.x, p.y, view.width, view.height));
					refresh();
				}
			};
		}
		return selectView;
	}
	
	/**
	 * Returns the Tool Bar of the application
	 * @return the Tool Bar of the application
	 */
	public JToolBar getToolBar() {
		if (toolBar == null) {
			toolBar = new JToolBar();
			toolBar.setLayout(new FlowLayout(FlowLayout.LEADING));
			toolBar.add(getNewAction());
			toolBar.add(getOpenAction());
			toolBar.add(getSaveAction());
			toolBar.add(getTranslateAction());
			toolBar.add(new JToolBar.Separator());
			toolBar.add(getUndoAction());
			toolBar.add(getRedoAction());
			toolBar.add(new JToolBar.Separator());
			toolBar.add(getFindAction());
			toolBar.add(new JToolBar.Separator());
			toolBar.add(getCmbSize());
			toolBar.add(getCmdFlag());
			toolBar.add(new JToolBar.Separator());
			toolBar.add(getCmbCoords());
			toolBar.add(getCoordAction());
			toolBar.add(new JToolBar.Separator());
			//toolBar.add(getScryeAction());
			toolBar.add(getExtractAction());
			toolBar.add(getCompareAction());
			toolBar.add(getSaveDifferencesAction());
			toolBar.add(getSaveMergedAction());
			//toolBar.add(getDiffAction());
			//toolBar.add(getMergeAction());
			toolBar.add(new JToolBar.Separator());
		}
		return toolBar;
	}
	
	/**
	 * Returns the New Map action
	 * @return the New Map action
	 */
	public NewAction getNewAction() {
		if (newAction == null) {
			newAction = new NewAction();
		}
		return newAction;
	}
	
	/**
	 * Returns the Open Map action
	 * @return the Open Map action
	 */
	public OpenAction getOpenAction() {
		if (openAction == null) {
			openAction = new OpenAction();
		}
		return openAction;
	}
	
	/**
	 * Returns the Save Map action
	 * @return the Save Map action
	 */
	public SaveAction getSaveAction() {
		if (saveAction == null) {
			saveAction = new SaveAction();
		}
		return saveAction;
	}
	
	/**
	 * Returns the Translate Map action
	 * @return the Translate Map action
	 */
	public TranslateAction getTranslateAction() {
		if (translateAction == null) {
			translateAction = new TranslateAction();
		}
		return translateAction;
	}

	/**
	 * Returns the Undo action
	 * @return the Undo action
	 */
	public UndoAction getUndoAction() {
		if (undoAction == null) {
			undoAction = new UndoAction();
		}
		return undoAction;
	}
	
	/**
	 * Returns the Redo action
	 * @return the Redo action
	 */
	public RedoAction getRedoAction() {
		if (redoAction == null) {
			redoAction = new RedoAction();
		}
		return redoAction;
	}
	
	/**
	 * Returns the Find action
	 * @return the Find action
	 */
	public FindAction getFindAction() {
		if (findAction == null) {
			findAction = new FindAction();
		}
		return findAction;
	}
	
	/**
	 * Returns the Set Co-ordinates action
	 * @return the Set Co-ordinates action
	 */
	public CoordAction getCoordAction() {
		if (coordAction == null) {
			coordAction = new CoordAction();
		}
		return coordAction;
	}
	
	/**
	 * Returns the Diff action
	 * @return the Diff action
	 */
//	public DiffAction getDiffAction() {
//		if (diffAction == null) {
//			diffAction = new DiffAction();
//		}
//		return diffAction;
//	}
	
//	/**
//	 * Returns the Merge action
//	 * @return the Merge action
//	 */
//	public MergeAction getMergeAction() {
//		if (mergeAction == null) {
//			mergeAction = new MergeAction();
//		}
//		return mergeAction;
//	}

	/**
	 * Returns the Compare action
	 * @return the Compare action
	 */
	public CompareAction getCompareAction() {
		if (compAction == null) {
			compAction = new CompareAction();
		}
		return compAction;
	}
	
	public SaveDifferencesAction getSaveDifferencesAction() {
		if (saveDifferencesAction == null) {
			saveDifferencesAction = new SaveDifferencesAction();
		}
		return saveDifferencesAction;

	}
	
	public SaveMergedAction getSaveMergedAction() {
		if (saveMergedAction == null) {
			saveMergedAction = new SaveMergedAction();
		}
		return saveMergedAction;

	}
	
	/**
	 * Returns the Scrye action
	 * @return the Scrye action
	 */
	public ScryeAction getScryeAction() {
		if (scryeAction == null) {
			scryeAction = new ScryeAction();
		}
		return scryeAction;
	}

	/**
	 * Returns the Extract action
	 * @return the Extract action
	 */
	public ExtractAction getExtractAction() {
		if (extractAction == null) {
			extractAction = new ExtractAction();
		}
		return extractAction;
	}

	/**
	 * Returns the Show/Hide flags togglebutton
	 * @return the Show/Hide flags togglebutton
	 */
	private JToggleButton getCmdFlag() {
		if (cmdFlag == null) {
			cmdFlag = new JToggleButton(new FlagAction());
			cmdFlag.setText("");
			cmdFlag.setSelected(true);
		}
		return cmdFlag;
	}
	
	private JPanel getStatusBar() {
		if (panStatus == null) {
			panStatus = new JPanel();
			panStatus.setBorder(new BevelBorder(BevelBorder.LOWERED));
			panStatus.setPreferredSize(new Dimension(getFrame().getWidth(), 20));
			panStatus.setLayout(new BoxLayout(panStatus, BoxLayout.X_AXIS));
			panStatus.add(getMapLabel());
			panStatus.add(getStatusLabel());
		}
		return panStatus;
	}

	/**
	 * Returns the map label
	 * @return the map label
	 */
	private JLabel getMapLabel() {
		if (lblMap == null) {
			lblMap = new JLabel("Unsaved Map File  |  ");
		}
		return lblMap;
	}

	/**
	 * Returns the status label
	 * @return the status label
	 */
	private JLabel getStatusLabel() {
		if (lblStatus == null) {
			lblStatus = new JLabel("Java Island Mapper");
		}
		return lblStatus;
	}
	
	/**
	 * Returns the map square size combo box
	 * @return the map square size combo box
	 */
	private JComboBox<Integer> getCmbSize() {
		if (cmbSize == null) {
			cmbSize = new JComboBox<Integer>(new Integer[] {SMALL_SQUARE, MEDIUM_SQUARE, LARGE_SQUARE});
			cmbSize.setSelectedItem(MEDIUM_SQUARE);
			cmbSize.addItemListener(new ItemListener() {
				
				@Override
				public void itemStateChanged(ItemEvent e) {
					getModel().setSize((Integer)e.getItem());
					resize(getView().getDimension());
					refresh();
				}
			});
			cmbSize.setRenderer(new DefaultListCellRenderer() {
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				@Override
				public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
					JLabel l =  (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
					int i = ((Integer)value) / 26 - 1;
					if (i >= 0 && i < SQUARE_SIZES.length) {
						l.setText(SQUARE_SIZES[i]);
					} else {
						l.setText("Size " + value);
					}

					return l;
				}
			});
		}
		return cmbSize;
	}

	/**
	 * Returns the Co-ordinate offset combo box
	 * @return the Co-ordinate offset combo box
	 */
	private JComboBox<Coordinate> getCmbCoords() {
		if (cmbCoords == null) {
			cmbCoords = new JComboBox<Coordinate>(DataStore.getInstance().getCoordinates().toArray(new Coordinate[DataStore.getInstance().getCoordinates().size()]));
			cmbCoords.setSelectedItem(MEDIUM_SQUARE);
			cmbCoords.addItemListener(new ItemListener() {
				
				@Override
				public void itemStateChanged(ItemEvent e) {
					getModel().setCurrentCoOrdinates((Coordinate)e.getItem());
					refresh();
				}
			});
			cmbCoords.setRenderer(new DefaultListCellRenderer() {
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				@Override
				public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
					JLabel l =  (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
					Coordinate c = (Coordinate)value;
					if (c != null) {
						if (c.equals(getModel().getDefaultCoOrdinates())) {
							l.setText(l.getText() + " *");
						}
					}

					return l;
				}
			});
		}
		return cmbCoords;
	}
	
	private JTextArea getTxtNote() {
		if (txtNote == null) {
			txtNote = new JTextArea(6, 30);
			txtNote.setEditable(false);
		}
		return txtNote;
	}
	
	private JScrollPane getScrNote() {
		if (scrNote == null) {
			scrNote = new JScrollPane(getTxtNote());
		}
		return scrNote;
	}
	
	/**
	 * Refreshes the application's components. This method checks the updating flag at the start of the method and returns without action if it is true. 
	 * If it is not true then it sets it to true before requesting the application's frame to repaint itself. This calls the repaint method of all child
	 * components. The updating flag is set to false at the end of the method. The updating flag is required to prevent an endless loop as the scroll 
	 * bars' values may be changed during the refresh which would trigger another call to the refresh() method.
	 */
	private void refresh() {
		if (updating) {
			return;
		}
		updating = true;
		getEdit().refresh();
		getCmbCoords().setSelectedItem(getModel().getCurrentCoOrdinates());
		updating = false;

		getFrame().getRootPane().repaint();
		getUndoAction().setEnabled(undoManager.canUndo());
		getUndoAction().putValue(Action.SHORT_DESCRIPTION, undoManager.getUndoPresentationName());
		getRedoAction().setEnabled(undoManager.canRedo());
		getRedoAction().putValue(Action.SHORT_DESCRIPTION, undoManager.getRedoPresentationName());
		
	}
	
	/**
	 * Ensures that the point is within the bounds of the current MapModel. If either co-ordinate is beyond the bounds of the 
	 * MapModel then the point is adjusted to the closest point within the bounds.
	 * @param p the Point to check and adjust if necessary
	 * @return a point that is within the bounds of the MapModel
	 */
	private Point bound(Point p) {
		Rectangle bounds = getModel().getBounds();
		int x = Math.min(Math.max(p.x, bounds.x), bounds.width + bounds.x);
		int y = Math.max(Math.min(p.y, bounds.y), bounds.y - bounds.height);
		if (p.x != x || p.y != y) {
			return new Point(x, y);
		}
		return p;
	}
	
	/**
	 * Ensures that the rectangle is within the bounds of the current MapModel. If any side is beyond the bounds of the 
	 * MapModel then the rectangle is adjusted to the closest rectangle within the bounds. This method does not adjust the width
	 * and height of the rectangle only the x and y co-ordinates
	 * @param rect the Point to check and adjust if necessary
	 * @return a rectangle that is within the bounds of the MapModel
	 */
	private Rectangle bound(Rectangle rect) {
		Rectangle bounds = getModel().getBounds();
		int x = 0;
		int y = 0;
		
		// Check not beyond top/left
		boolean changed = false;
		if (rect.x < bounds.x) {
			x = bounds.x;
			changed = true;
		} else {
			x = rect.x;
		}
		if (rect.y > bounds.y) {
			y = bounds.y;
			changed = true;
		} else {
			y = rect.y;
		}
		if (changed) {
			return new Rectangle(x, y,  rect.width, rect.height);
		}
		
		// Check not beyond bottom/right
		x = (bounds.x + bounds.width) - (rect.x + rect.width) + 2;
		y = (bounds.y - bounds.height) - (rect.y - rect.height) - 1;
		if (x > 0) {
			x = 0;
		}
		if (y < 0) {
			y = 0;
		}
		if (x < 0 || y > 0) {
			return new Rectangle(rect.x + x, rect.y + y, rect.width, rect.height);
		}

		return rect;
	}
	
	/**
	 * Resizes the MapModel's view based on the dimensions of the MapPanel component
	 * @param size the dimensions of the MapPanel component in pixels
	 */
	private void resize(Dimension size) {
		Rectangle rect = getModel().getView();
		rect = new Rectangle(rect.x, rect.y, size.width / (getModel().getSize() + 1), size.height / (getModel().getSize() + 1));
		int chk = getModel().getBounds().width + getModel().getBounds().x - rect.x - rect.width + 1;
		if (chk < 0) {
			rect = new Rectangle(rect.x + chk, rect.y, rect.width, rect.height);
		}
		getModel().setView(bound(rect));
	}
	
	/**
	 * Makes an ImageIcon from the filepath specified
	 * @param path the path to the image file
	 * @return an ImageIcon
	 */
	private ImageIcon makeImageIcon(String path) {
		URL url = this.getClass().getResource(path);
        if (url == null) {
            return null;
        } else {
            return new ImageIcon(url);
        }
	}

	/**
	 * Returns the Map Dimensions dialog box
	 * @return the Map Dimensions dialog box
	 */
	private MapDimensions getMapDimensions() {
		if (frmDimensions == null) {
			frmDimensions = new MapDimensions(getFrame());
		}
		return frmDimensions;
	}
	
	private MapFind getMapFind() {
		if (frmFind == null) {
			frmFind = new MapFind(getFrame());
		}
		return frmFind;
	}
	
	private MapText getMapText() {
		if (frmText == null) {
			frmText = new MapText(getFrame());
		}
		return frmText;
	}

	private void clearFilters(JFileChooser fc) {
		FileFilter[] ff = fc.getChoosableFileFilters();
		for (int i = 0; i < ff.length; i++) {
			fc.removeChoosableFileFilter(ff[i]);
		}
	}
	
	private JFileChooser getSaveFileChooser() {
		if (save == null) {
			save = new JFileChooser();
			save.setAcceptAllFileFilterUsed(false);
			List<FileFilter> filters = FactoryManager.getInstance().getWriteFilters();
			for (int i = 0; i < filters.size(); i++) {
				save.addChoosableFileFilter(filters.get(i));
			}
		}
		return save;
	}
	
	private JFileChooser getLoadFileChooser() {
		if (load == null) {
			load = new JFileChooser();
			load.setAcceptAllFileFilterUsed(false);
			List<FileFilter> filters = FactoryManager.getInstance().getReadFilters();
			for (int i = 0; i < filters.size(); i++) {
				load.addChoosableFileFilter(filters.get(i));
			}
		}
		return load;
	}

	private JFileChooser getExtractFileChooser() {
		if (extract == null) {
			extract = new JFileChooser();
			extract.setAcceptAllFileFilterUsed(false);
			extract.setMultiSelectionEnabled(true);
			
			FileFilter extractFilter = new FileFilter() {
				@Override
				public boolean accept(File file) {
					return file.isDirectory() || file.getName().toLowerCase().endsWith(".pdf");
				}

				@Override
				public String getDescription() {
					return "MI Turn Result Files (*.pdf)";
				}
			};

			extract.addChoosableFileFilter(extractFilter);
		}
		return extract;
	}
	
	private JFileChooser getTransFileChooser() {
		if (trans == null) {
			trans = new JFileChooser();
			trans.setAcceptAllFileFilterUsed(false);
			List<FileFilter> filters = FactoryManager.getInstance().getTranslateFilters();
			for (int i = 0; i < filters.size(); i++) {
				trans.addChoosableFileFilter(filters.get(i));
			}
		}
		return trans;
	}

	private JFileChooser getDirectoryChooser() {
		if (dir == null) {
			
			dir = new JFileChooser();
         	dir.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		}
		return dir;
	}
	
	private JFileChooser getCSVFileChooser() {
		if (csv == null) {
			csv = new JFileChooser();
			csv.setFileFilter(new FileFilter() {

				@Override
				public boolean accept(File f) {
					return f.getName().toLowerCase().endsWith(".csv");
				}

				@Override
				public String getDescription() {
					return "JIM Translation Files (.csv)";
				}
				
			});
			csv.setAcceptAllFileFilterUsed(false);
			List<FileFilter> filters = FactoryManager.getInstance().getTranslateFilters();
			for (int i = 0; i < filters.size(); i++) {
				csv.addChoosableFileFilter(filters.get(i));
			}
		}
		return csv;
	}

	class NewAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		public NewAction() {
	        putValue(Action.NAME, "New");
	        putValue(Action.SHORT_DESCRIPTION, "Create new map");
	        putValue(Action.LONG_DESCRIPTION, "Create a new map");
	        putValue(Action.SMALL_ICON, makeImageIcon("new-16x16.gif"));
	        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control N"));
	        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_N));
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			MapDimensions d = getMapDimensions();
			int[] dims = d.getDimensions();
			if (dims != null && dims.length == 4 && dims[2] > 0 && dims[3] > 0) {
				setModel(new StandardMapModel(getModel().getSize(), new MapData(dims[0], dims[1], dims[2], dims[3]), "Unsaved.jim"));
			}
		}

	}

	class OpenAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		public OpenAction() {
	        putValue(Action.NAME, "Open");
	        putValue(Action.SHORT_DESCRIPTION, "Open map");
	        putValue(Action.LONG_DESCRIPTION, "Open a map");
	        putValue(Action.SMALL_ICON, makeImageIcon("open-16x16.gif"));
	        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control O"));
	        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_O));
		}
		
		@Override
		public void actionPerformed(ActionEvent evt) {
			JFileChooser fc = getLoadFileChooser();
			int returnVal = fc.showOpenDialog(getFrame());
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				MapData md;
				try {
					md = FactoryManager.getInstance().createFrom(fc.getSelectedFile());
					MapModel m = new StandardMapModel(getModel().getSize(), md, fc.getSelectedFile().getName());
					setModel(m);
					refresh();
					getMiniMap().revalidate();
					if (FactoryManager.getInstance().getUnmatched().size() > 0) {
						StringBuilder sb = new StringBuilder();
						sb.append("There were some features that were not recognised:\n");
						Iterator<String> it = FactoryManager.getInstance().getUnmatched().iterator();
						while (it.hasNext()) {
							sb.append(it.next());
							sb.append("\n");
						}
						getTxtNote().setText(sb.toString());
						getTxtNote().setCaretPosition(0);
						JOptionPane.showMessageDialog(getFrame(), getScrNote(), "Cannot read all data", JOptionPane.WARNING_MESSAGE);
					}
				} catch (IOException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(getFrame(), "Cannot load map from " + fc.getSelectedFile().getAbsolutePath(), "Cannot open file", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}

	class SaveAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		public SaveAction() {
	        putValue(Action.NAME, "Save");
	        putValue(Action.SHORT_DESCRIPTION, "Save map");
	        putValue(Action.LONG_DESCRIPTION, "Save the map");
	        putValue(Action.SMALL_ICON, makeImageIcon("save-16x16.gif"));
	        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control S"));
	        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_S));
		}
		
		@Override
		public void actionPerformed(ActionEvent evt) {
			JFileChooser fc = getSaveFileChooser();
			int returnVal = fc.showSaveDialog(getFrame());
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = suffixFile(fc);
				
				if (file.exists()) {
					int confirm = JOptionPane.showConfirmDialog(getFrame(), file.getName() + " exists. Do you want to overwrite?");
					if (confirm != JOptionPane.OK_OPTION) {
						return;
					}
				}
				try {
					FactoryManager.getInstance().saveTo(getModel().getData(), file);
					getModel().setName(file.getName());
					setMapLabel(getModel());
				} catch (Exception e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(getFrame(), "Cannot save map to " + file.getAbsolutePath());
				}
			}
		}
	}

	class SaveDifferencesAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		public SaveDifferencesAction() {
	        putValue(Action.NAME, "Diff");
	        putValue(Action.SHORT_DESCRIPTION, "Save differences");
	        putValue(Action.LONG_DESCRIPTION, "Save the squares that are different in the compared map");
	        putValue(Action.SMALL_ICON, makeImageIcon("diff-16x16.gif"));
	        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control D"));
	        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_D));
		}
		
		@Override
		public void actionPerformed(ActionEvent evt) {
			if (getModel().getClass().equals(DiffMapModel.class)) {
				DiffMapModel d = (DiffMapModel)getModel();
				MapData md = d.getDifferences();

				JFileChooser fc = getSaveFileChooser();
				int returnVal = fc.showSaveDialog(getFrame());
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = suffixFile(fc);

					if (file.exists()) {
						int confirm = JOptionPane.showConfirmDialog(getFrame(), file.getName() + " exists. Do you want to overwrite?");
						if (confirm != JOptionPane.OK_OPTION) {
							return;
						}
					}
					
					try {
						FactoryManager.getInstance().saveTo(md, file);
					} catch (IOException e) {
						e.printStackTrace();
						JOptionPane.showMessageDialog(getFrame(), "Cannot save map to " + file.getAbsolutePath());
					}
				}
			} else {
				JOptionPane.showMessageDialog(getFrame(), "This can only be done if you are comparing maps");
			}
		}
	}

	class SaveMergedAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		public SaveMergedAction() {
	        putValue(Action.NAME, "Merge");
	        putValue(Action.SHORT_DESCRIPTION, "Save Merged maps");
	        putValue(Action.LONG_DESCRIPTION, "Save all the features from both maps");
	        putValue(Action.SMALL_ICON, makeImageIcon("merge-16x16.gif"));
	        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control M"));
	        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_M));
		}
		
		@Override
		public void actionPerformed(ActionEvent evt) {
			if (getModel().getClass().equals(DiffMapModel.class)) {
				DiffMapModel d = (DiffMapModel)getModel();
				MapData md = d.getMerged();

				JFileChooser fc = getSaveFileChooser();
				int returnVal = fc.showSaveDialog(getFrame());
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = suffixFile(fc);

					if (file.exists()) {
						int confirm = JOptionPane.showConfirmDialog(getFrame(), file.getName() + " exists. Do you want to overwrite?");
						if (confirm != JOptionPane.OK_OPTION) {
							return;
						}
					}
					
					try {
						FactoryManager.getInstance().saveTo(md, file);
					} catch (IOException e) {
						e.printStackTrace();
						JOptionPane.showMessageDialog(getFrame(), "Cannot save map to " + file.getAbsolutePath());
					}
				}
			} else {
				JOptionPane.showMessageDialog(getFrame(), "This can only be done if you are comparing maps");
			}
		}
	}

	class TranslateAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		public TranslateAction() {
	        putValue(Action.NAME, "Translate");
	        putValue(Action.SHORT_DESCRIPTION, "Create translation file");
	        putValue(Action.LONG_DESCRIPTION, "Create a translation file for this file");
	        putValue(Action.SMALL_ICON, makeImageIcon("trans-16x16.gif"));
	        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control T"));
	        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_T));
		}
		
		@Override
		public void actionPerformed(ActionEvent evt) {
			JFileChooser fc = getTransFileChooser();
			int returnVal = fc.showOpenDialog(getFrame());
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				
				if (!file.exists()) {
					JOptionPane.showMessageDialog(getFrame(), "You must select an existing file to read from", "Cannot open file", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				String text = "";
				try {
					text = FactoryManager.getInstance().listTranslations(file);
				} catch (IOException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(getFrame(), "Cannot read from " + file.getAbsolutePath() + " or related files", "Cannot open file", JOptionPane.ERROR_MESSAGE);
					return;
				}

				JFileChooser csv = getCSVFileChooser();
				returnVal = csv.showSaveDialog(getFrame());
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					file = suffixFile(fc);
					if (csv.getSelectedFile().exists()) {
						int confirm = JOptionPane.showConfirmDialog(getFrame(), file.getName() + " exists. Do you want to overwrite?");
						if (confirm != JOptionPane.OK_OPTION) {
							return;
						}
					}
					try {
						FileWriter write = new FileWriter(file);
						write.write(text);
						write.close();
					} catch (IOException e) {
						e.printStackTrace();
						JOptionPane.showMessageDialog(getFrame(), "Cannot save translations to " + file.getAbsolutePath());
					}
				}
			}
		}
	}

	class FindAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		public FindAction() {
	        putValue(Action.NAME, "Find");
	        putValue(Action.SHORT_DESCRIPTION, "Find Feature");
	        putValue(Action.LONG_DESCRIPTION, "Find features on the map");
	        putValue(Action.SMALL_ICON, makeImageIcon("find-16x16.gif"));
	        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control F"));
	        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_F));
		}
		
		@Override
		public void actionPerformed(ActionEvent evt) {
			MapFind mf = getMapFind(); 
			MapFind.FindData fd = mf.getFind();
			List<Point> list;
			String searchTerm;
			if (fd.getFeature() != null) {
				list = getFeatureList(fd.getFeature(), fd.getDistance());
				getSearch().setFoundID(fd.getFeature().getId());
				getSearch().setFoundNote(null);
				searchTerm = fd.getFeature().getName();
			} else if (fd.getNote() != null) {
				list = getNoteList(fd.getNote(), fd.getDistance());
				getSearch().setFoundID((short)0);
				getSearch().setFoundNote(fd.getNote());
				searchTerm = fd.getNote();
			} else {
				return;
			}
			getSearch().setFoundSquares(list);
			if (list.size() > 0) {
				getStatusLabel().setText("Found " + list.size() + " " + searchTerm);
			} else {
				getStatusLabel().setText("Cannot find " + searchTerm);
			}
			setSearch(search);
			refresh();
		}
		
		private List<Point> getFeatureList(Feature f, int d) {
			if (d < 0) {
				return getIndex().getPoints(f);
			} else {
				return getIndex().getPoints(f, getModel().getSelected(), d);
			}
		}

		private List<Point> getNoteList(String n, int d) {
			if (d < 0) {
				return getIndex().getPoints(n);
			} else {
				return getIndex().getPoints(n, getModel().getSelected(), d);
			}
		}
}
	
	class CoordAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		public CoordAction() {
	        putValue(Action.NAME, "Origin");
	        putValue(Action.SHORT_DESCRIPTION, "Set as default Co-ordinates");
	        putValue(Action.LONG_DESCRIPTION, "Set as default Co-ordinates");
	        putValue(Action.SMALL_ICON, makeImageIcon("set-16x16.gif"));
	        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control O"));
	        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_O));
		}
		
		@Override
		public void actionPerformed(ActionEvent evt) {
			Coordinate coord = (Coordinate)getCmbCoords().getSelectedItem();
			if (coord != null) {
				int result = JOptionPane.showConfirmDialog(getFrame(), "Are you sure you want to set this map's default co-ordinates to " + coord + "?\n", "Set Default Co-Ordinates", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
				if (result == JOptionPane.YES_OPTION) {
					UndoableChange change = getModel().setDefaultCoOrdinates(coord);
					if (change != null) {
						change.setPresentationName("Set default co-ordinates to " + coord.getName());
						undoManager.addEdit(change);
					}
					setMapLabel(getModel());
					refresh();
				}
			}
		}
	}

	class UndoAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		public UndoAction() {
	        putValue(Action.NAME, "Undo");
	        putValue(Action.SHORT_DESCRIPTION, "Undo change");
	        putValue(Action.LONG_DESCRIPTION, "Undo a change to the map");
	        putValue(Action.SMALL_ICON, makeImageIcon("undo-16x16.gif"));
	        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control U"));
	        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_U));
		}
		
		@Override
		public void actionPerformed(ActionEvent evt) {
			if (undoManager.canUndo()) {
				undoManager.undo();
				refresh();
			}
		}
	}

	class RedoAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		public RedoAction() {
	        putValue(Action.NAME, "Redo");
	        putValue(Action.SHORT_DESCRIPTION, "Redo change");
	        putValue(Action.LONG_DESCRIPTION, "Redo a change to the map");
	        putValue(Action.SMALL_ICON, makeImageIcon("redo-16x16.gif"));
	        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control R"));
	        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_R));
		}
		
		@Override
		public void actionPerformed(ActionEvent evt) {
			if (undoManager.canRedo()) {
				undoManager.redo();
				refresh();
			}
		}
	}
	
//	class DiffAction extends AbstractAction {
//
//		/**
//		 * 
//		 */
//		private static final long serialVersionUID = 1L;
//		
//		public DiffAction() {
//	        putValue(Action.NAME, "Diff");
//	        putValue(Action.SHORT_DESCRIPTION, "Create a diff file from this map");
//	        putValue(Action.LONG_DESCRIPTION, "Create a diff file from this map");
//	        putValue(Action.SMALL_ICON, makeImageIcon("diff-16x16.gif"));
//	        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control D"));
//	        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_D));
//		}
//		
//		@Override
//		public void actionPerformed(ActionEvent evt) {
//			JFileChooser fc = new JFileChooser();
//			fc.setAcceptAllFileFilterUsed(false);
//			clearFilters(fc);
//			List<FileFilter> filters = FactoryManager.getInstance().getReadFilters();
//			for (int i = 0; i < filters.size(); i++) {
//				fc.addChoosableFileFilter(filters.get(i));
//			}
//			int returnVal = fc.showOpenDialog(getFrame());
//			if (returnVal == JFileChooser.APPROVE_OPTION) {
//				MapData base;
//				try {
//					base = FactoryManager.getInstance().createFrom(fc.getSelectedFile());
//					MapModel extra = getModel();
//					if (extra.isDirty()) {
//						JOptionPane.showMessageDialog(getFrame(), "You must save your map before creating a diff");
//						return;
//					}
//					MapData diff = Diff.diff(((StandardMapModel)getModel()).getData(), base);
//					MapModel m = new StandardMapModel(getModel().getSize(), diff, "Diff");
//					setModel(m);
//					refresh();
//					getMiniMap().revalidate();
//				} catch (IOException e) {
//					e.printStackTrace();
//					JOptionPane.showMessageDialog(getFrame(), "Cannot load map from " + fc.getSelectedFile().getAbsolutePath());
//				} catch (IllegalArgumentException e) {
//					e.printStackTrace();
//					JOptionPane.showMessageDialog(getFrame(), e.getMessage());
//				}
//			}
//		}
//	}
//
//	class MergeAction extends AbstractAction {
//
//		/**
//		 * 
//		 */
//		private static final long serialVersionUID = 1L;
//		
//		public MergeAction() {
//	        putValue(Action.NAME, "Merge");
//	        putValue(Action.SHORT_DESCRIPTION, "Merge this map");
//	        putValue(Action.LONG_DESCRIPTION, "Merge this map with another");
//	        putValue(Action.SMALL_ICON, makeImageIcon("merge-16x16.gif"));
//	        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control M"));
//	        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_M));
//		}
//		
//		@Override
//		public void actionPerformed(ActionEvent evt) {
//			JFileChooser fc = new JFileChooser();
//			fc.setAcceptAllFileFilterUsed(false);
//			clearFilters(fc);
//			List<FileFilter> filters = FactoryManager.getInstance().getReadFilters();
//			for (int i = 0; i < filters.size(); i++) {
//				fc.addChoosableFileFilter(filters.get(i));
//			}
//			int returnVal = fc.showOpenDialog(getFrame());
//			if (returnVal == JFileChooser.APPROVE_OPTION) {
//				MapData base;
//				try {
//					base = FactoryManager.getInstance().createFrom(fc.getSelectedFile());
//					MapModel extra = getModel();
//					if (extra.isDirty()) {
//						JOptionPane.showMessageDialog(getFrame(), "You must save your map before merging it with another");
//						return;
//					}
//					MapData merge = Merge.merge(((StandardMapModel)getModel()).getData(), base);
//					MapModel m = new StandardMapModel(getModel().getSize(), merge, getModel().getName());
//					setModel(m);
//					refresh();
//					getMiniMap().revalidate();
//				} catch (IOException e) {
//					e.printStackTrace();
//					JOptionPane.showMessageDialog(getFrame(), "Cannot load map from " + fc.getSelectedFile().getAbsolutePath());
//				} catch (IllegalArgumentException e) {
//					e.printStackTrace();
//					JOptionPane.showMessageDialog(getFrame(), e.getMessage());
//				}
//			}
//		}
//	}

	class CompareAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		public CompareAction() {
	        putValue(Action.NAME, "Compare");
	        putValue(Action.SHORT_DESCRIPTION, "Compare this map to another");
	        putValue(Action.LONG_DESCRIPTION, "Compare this map to another");
	        putValue(Action.SMALL_ICON, makeImageIcon("compare-16x16.gif"));
	        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control P"));
	        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_P));
		}
		
		@Override
		public void actionPerformed(ActionEvent evt) {
			JFileChooser fc = new JFileChooser();
			fc.setAcceptAllFileFilterUsed(false);
			clearFilters(fc);
			List<FileFilter> filters = FactoryManager.getInstance().getReadFilters();
			for (int i = 0; i < filters.size(); i++) {
				fc.addChoosableFileFilter(filters.get(i));
			}
			int returnVal = fc.showOpenDialog(getFrame());
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				MapData smd;
				try {
					smd = FactoryManager.getInstance().createFrom(fc.getSelectedFile());
					MapModel primary = getModel();
					if (primary.isDirty()) {
						JOptionPane.showMessageDialog(getFrame(), "You must save your map before comparing");
						return;
					}

					MapModel secondary = new StandardMapModel(primary.getSize(), smd, fc.getSelectedFile().getName());

					MapModel m = new DiffMapModel(primary, secondary);
					
					setModel(m);
					refresh();
					getMiniMap().revalidate();
				} catch (IOException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(getFrame(), "Cannot load map from " + fc.getSelectedFile().getAbsolutePath());
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(getFrame(), e.getMessage());
				}
			}
		}
	}

	class FlagAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		public FlagAction() {
	        putValue(Action.NAME, "Show Flags");
	        putValue(Action.SHORT_DESCRIPTION, "Show flags on the map");
	        putValue(Action.LONG_DESCRIPTION, "Show Flags on the map");
	        putValue(Action.SMALL_ICON, makeImageIcon("flag-16x16.gif"));
	        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control F"));
	        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_F));
		}
		
		@Override
		public void actionPerformed(ActionEvent evt) {
			JToggleButton cmd = (JToggleButton)evt.getSource();
			getView().setViewFlags(cmd.isSelected());
			refresh();
		}
	}

	class ScryeAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		public ScryeAction() {
	        putValue(Action.NAME, "Scrye");
	        putValue(Action.SHORT_DESCRIPTION, "Enter Scrying data on to map");
	        putValue(Action.LONG_DESCRIPTION, "Enter Scrying data on to map");
	        putValue(Action.SMALL_ICON, makeImageIcon("ball-16x16.gif"));
	        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control Y"));
	        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_Y));
		}
		
		@Override
		public void actionPerformed(ActionEvent evt) {
			Point p = getModel().getSelected();
			if (p == null) {
				JOptionPane.showMessageDialog(getFrame(), "You must select the square where the scrying occurred.");
				return;
			}
			
			String text = getMapText().getText();
			if (text == null) {
				return; 
			}

			TurnReader r = new TurnReader(text);
			List<net.edwebb.mi.data.Sighting> list = TurnDigester.readScrye(r, p.x, p.y);
			StringBuilder sb = new StringBuilder();
			Iterator<net.edwebb.mi.data.Sighting> it = list.iterator();
			while (it.hasNext()) {
				net.edwebb.mi.data.Sighting s = it.next();
				if (s.getFeature() == null) {
					sb.append("(" + s.getSquare().y  + "," + s.getSquare().x + ") " + s.getThing() + "\n");
				} else {
					handleFeatureEdit(s.getSquare(), EditPanel.ADD, s.getFeature());
				}
			}
			
			if (sb.length() > 0) {
				sb.insert(0, "These sightings were not recognised:\n");
				getTxtNote().setText(sb.toString());
				getTxtNote().setCaretPosition(0);
				JOptionPane.showMessageDialog(getFrame(), getScrNote(), "Cannot read all data", JOptionPane.WARNING_MESSAGE);
			}
			
		}
	}
	
	class ExtractAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		public ExtractAction() {
	        putValue(Action.NAME, "Extract");
	        putValue(Action.SHORT_DESCRIPTION, "Extract data from Turns");
	        putValue(Action.LONG_DESCRIPTION, "Extract data from Monster Island pdf files");
	        putValue(Action.SMALL_ICON, makeImageIcon("extract-16x16.gif"));
	        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control X"));
	        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_X));
		}
		
		@Override
		public void actionPerformed(ActionEvent evt) {
			
			JFileChooser fc = getExtractFileChooser();
			int returnVal = fc.showOpenDialog(getFrame());
			if (returnVal != JFileChooser.APPROVE_OPTION) {
				return;
			}
			
			File[] files = fc.getSelectedFiles();
			PDFExtractor pdfExtractor = new PDFExtractor();
			
			MIExtractor miExtractor;
			
			fc = getDirectoryChooser();
			returnVal = fc.showOpenDialog(getFrame());
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				miExtractor = new MIExtractor(fc.getSelectedFile());
			} else {
				miExtractor = new MIExtractor();
			}
			
			ExtractTask task = new ExtractTask(pdfExtractor, miExtractor, files);
			task.execute();
		}
	}

	class ExtractTask extends SwingWorker<Object, String> {

		private PDFExtractor pdfExtractor;
		private MIExtractor miExtractor;
		private File[] files;
		private Set<Sighting> masterSightings = new HashSet<Sighting>();
		
		public ExtractTask(PDFExtractor pdfExtractor, MIExtractor miExtractor, File[] files) {
			this.pdfExtractor = pdfExtractor;
			this.miExtractor = miExtractor;
			this.files = files;
		}
		
		@Override
		protected Object doInBackground() throws Exception {

			readTurns();
			addToMap();

			refresh();
			getMiniMap().revalidate();
			
			return null;
		}

		private void readTurns() {
			String mode = "D";
		    int[] coords = new int[] {0,0}; //getCoords();  
			
			Stats stats = null;
			
			String text = null;
			for (int i = 0; i < files.length; i++) {
				getStatusLabel().setText("Processing " + files[i].getName());
				try {
					text = pdfExtractor.extract(files[i]);
				} catch (IOException e) {
					JOptionPane.showMessageDialog(getFrame(), "Cannot read data from " + files[i].getAbsolutePath());
					getStatusLabel().setText("Java Island Mapper");
					return;
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println();
				}
				if (text == null || text.length() == 0) {
					JOptionPane.showMessageDialog(getFrame(), "Cannot find any text in " + files[i].getAbsolutePath());
					getStatusLabel().setText("Java Island Mapper");
					return;
				}
				try {
					Turn turn = miExtractor.extract(new StringReader(text), (i==0 ? "N" : mode), coords[0], coords[1], stats);
					coords[0] = turn.getX();
					coords[1] = turn.getY();
					stats = turn.getStats();
					masterSightings.addAll(turn.getSightings());
				} catch (IOException e) {
					JOptionPane.showMessageDialog(getFrame(), "Cannot extract data from " + files[i].getAbsolutePath());
					getStatusLabel().setText("Java Island Mapper");
					return;
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println();
				}
			}
		}
		
		private void addToMap() {
			StringBuilder sb = new StringBuilder();
			String result;
			Iterator<Sighting> it = masterSightings.iterator();
			UndoableCombinedChange change = new UndoableCombinedChange("Update map with extracted information");
			
			while (it.hasNext()) {
				Sighting s = it.next();
				
				if (s.getCode().length() == 8 && s.getCode().startsWith("%")) {
					for (int i = 0; i < s.getCode().length(); i+=4) {
						result = addFeature(new Sighting(s.getX(),s.getY(),s.getCode().substring(i, i + 4)), change);
						if (result != null) {
							sb.append(result);
						}
					}
				} else {
					result = addFeature(s, change);
					if (result != null) {
						sb.append(result);
					}
				}
			}

			if (change.hasChanges()) {
				undoManager.addEdit(change);
			}
			getStatusLabel().setText("Java Island Mapper");
			refresh();
			
			if (sb.length() > 0) {
				sb.insert(0, "These sightings were not recognised:\n");
				getTxtNote().setText(sb.toString());
				getTxtNote().setCaretPosition(0);
				JOptionPane.showMessageDialog(getFrame(), getScrNote(), "Cannot read all data", JOptionPane.WARNING_MESSAGE);
			}
		}

		private String addFeature(Sighting s, UndoableCombinedChange masterChange) {
			Feature f = DataStore.getInstance().getFeatureByCode(s.getCode());
			
			if (f == null) {
				return "(" + s.getY()  + "," + s.getX() + ") " + s.getCode() + "\n";
			} else {
				Point point = new Point(s.getX(), s.getY());
				Rectangle rect = getModel().getBounds();
				rect = new Rectangle(rect.x, rect.y - rect.height, rect.width, rect.height);
				if (!rect.contains(point)) {
					return "(" + s.getY()  + "," + s.getX() + ") " + s.getCode() + " (out of bounds)\n";
				} else {
					UndoableCombinedChange change = handleFeatureEdit(point, EditPanel.ADD, f);
					masterChange.addAllChanges(change);
					undoManager.removeNextUndo();
					getMiniMap().setPoint(point, DataStore.getInstance().getTerrain(getModel().getSquare(point)[0]));
				}
			}
			return null;
		}
	}
	
	public class ChangeUndoManager extends UndoManager {

		@Override
		public boolean replaceEdit(UndoableEdit anEdit) {
			UndoableEdit nextEdit = editToBeUndone();
			if (nextEdit == null) {
				return false;
			} else {
				edits.set(edits.indexOf(nextEdit), anEdit);
				return true;
			}
		}
		
		public boolean removeNextUndo() {
			UndoableEdit nextEdit = editToBeUndone();
			if (nextEdit == null) {
				return false;
			} else {
				trimEdits(edits.size() - 1, edits.size() - 1);
				return true;
			}
		}

		public boolean removeNextRedo() {
			UndoableEdit nextEdit = editToBeRedone();
			if (nextEdit == null) {
				return false;
			} else {
				return edits.remove(nextEdit);
			}
		}
}			
	
	public File suffixFile(JFileChooser fc) {
		File file = fc.getSelectedFile();
		
		// Add the correct suffix if the user was too lazy to write it in themselves
		String desc = fc.getFileFilter().getDescription();
		String suffix = desc.substring(desc.lastIndexOf("(")+2, desc.lastIndexOf(")"));
		if (!file.getName().endsWith(suffix)) {
			file = new File(file.getParentFile(), file.getName() + suffix);
		}
		return file;
	}
	
	public static void main (String[] args) throws IOException {
		DataStore.createInstance(new File("data"));
		FactoryManager.createInstance(new File("data"));
		DataStore.createInstance(new File("data"));
		
		MapController ctrl = new MapController();
		MapModel m = null;
		
		if (args.length > 0) {
			File f = new File(args[0]);
			if (f.exists()) {
				try {
					MapData md = FactoryManager.getInstance().createFrom(f);
					m = new StandardMapModel(MEDIUM_SQUARE, md, "Unsaved.jim");
				} catch (IOException e) {
					int ans = JOptionPane.showOptionDialog(null, "Cannot read map data in file " + args[0] + "\nOpen blank map instead?" , "File Read Error", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, null, null);
					if (ans != JOptionPane.OK_OPTION) {
						System.exit(0);
					}
				}
			} else {
				int ans = JOptionPane.showOptionDialog(null, "Cannot read map data in file " + args[0] + "\nOpen blank map instead?" , "File Read Error", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, null, null);
				if (ans != JOptionPane.OK_OPTION) {
					System.exit(0);
				}
			}
		}

		if (m == null) {
			m = new StandardMapModel(MEDIUM_SQUARE, new MapData(100,100), "new.jim");
		}
		ctrl.showFrame();
		ctrl.setModel(m);
	}
}
