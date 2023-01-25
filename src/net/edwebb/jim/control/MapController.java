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
import java.io.IOException;
import java.util.Iterator;

import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;

import net.edwebb.jim.control.actions.CompareAction;
import net.edwebb.jim.control.actions.FindAction;
import net.edwebb.jim.control.actions.FlagAction;
import net.edwebb.jim.control.actions.NewAction;
import net.edwebb.jim.control.actions.OpenAction;
import net.edwebb.jim.control.actions.RedoAction;
import net.edwebb.jim.control.actions.ResizeAction;
import net.edwebb.jim.control.actions.SaveAction;
import net.edwebb.jim.control.actions.SaveDifferencesAction;
import net.edwebb.jim.control.actions.SaveMergedAction;
import net.edwebb.jim.control.actions.ScryeAction;
import net.edwebb.jim.control.actions.TranslateAction;
import net.edwebb.jim.control.actions.UndoAction;
import net.edwebb.jim.control.actions.ExtractAction;
//import net.edwebb.jim.factory.Diff;
import net.edwebb.jim.factory.FactoryManager;
//import net.edwebb.jim.factory.Merge;
import net.edwebb.jim.model.DiffMapModel;
import net.edwebb.jim.model.MapData;
import net.edwebb.jim.model.MapIndex;
import net.edwebb.jim.model.MapModel;
import net.edwebb.jim.model.MapSearch;
import net.edwebb.jim.model.StandardMapModel;
import net.edwebb.jim.undo.ChangeUndoManager;
import net.edwebb.jim.view.CoordinatePanel;
import net.edwebb.jim.view.EditPanel;
import net.edwebb.jim.view.MiniMap;
import net.edwebb.jim.view.ViewPanel;
import net.edwebb.mi.data.DataStore;
import net.edwebb.mi.data.Feature;
import net.edwebb.mi.data.Flag;
import net.edwebb.mi.data.Terrain;

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
		private FindAction findAction;
		private UndoAction undoAction;
		private RedoAction redoAction;
		private JComboBox<Integer> cmbSize;
		private CoordinatePanel panCoordinates;
		//private DiffAction diffAction;
		private CompareAction compAction;
		private SaveDifferencesAction saveDifferencesAction;
		private SaveMergedAction saveMergedAction;
		//private MergeAction mergeAction;
		private JToggleButton cmdFlag;
		private ScryeAction scryeAction;
		private ExtractAction extractAction;
		private ResizeAction resizeAction;
		private TranslateAction translateAction;
	
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
		getIndex().setModel(model);
		setMapLabel(model);
		getView().setModel(model);
		getEdit().setModel(model);
		getUndoManager().setModel(model);
		getMiniMap().setModel(model);
		getCoordinatePanel().setModel(model);
		if (getFrame().isVisible()) {
			resize(getView().getDimension());
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
		model.setSelected(new Point(0, 0));
	}

	public void setMapLabel(MapModel model) {
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
	
	public void setStatusLabel(String text) {
		getStatusLabel().setText(text);
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
			index = new MapIndex(getModel());
		}
		return index;
	}
	
	public MapSearch getSearch() {
		if (search == null) {
			setSearch(new MapSearch());
		}
		return search;
	}
	
	public ChangeUndoManager getUndoManager() {
		if (undoManager == null) {
			undoManager = new ChangeUndoManager(getModel());
			undoManager.addUndoListener(getEdit());
		}
		return undoManager;
	}
	
	/**
	 * Sets a new search for the controller to use. 
	 * @param search the search that the controller should use
	 */
	public void setSearch(MapSearch search) {
		this.search = search;
		getView().setSearch(search);
		getMiniMap().setSearch(search);
		updateSearch();
	}
	
	/**
	 * Sets a new search for the controller to use. 
	 * @param search the search that the controller should use
	 */
	public void updateSearch() {
		getView().repaint();
		getMiniMap().repaint();
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
					getModel().setView(rect);
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
					} else if (e.getKeyCode() >= KeyEvent.VK_0 && e.getKeyCode() <= KeyEvent.VK_7) {
						// Flag the square
						Flag f = DataStore.getInstance().getFlag((short)(e.getKeyCode() - 48));
						if (f != null) {
							if (e.isShiftDown()) {
								handleFlagEdit(getModel().getSelected(), f);
							} else if (e.isControlDown()) {
								paintFlag(getModel().getSelected(), f, false);
							} else {
								paintFlag(getModel().getSelected(), f, true);
							}
						}
					} else {
						// Update the terrain
						Terrain t = DataStore.getInstance().getTerrain((short)e.getKeyCode());
						if (t != null) {
							handleFeatureEdit(getModel().getSelected(), EditPanel.ADD, t);
						}
					}
					if (scroll == true) {
						getModel().setView(rect);
						getModel().setSelected(sel);
					}
				}
			};
		}
		return mapKeyPress;
	}

	private void paintFlag(Point p, Flag f, boolean on) {
		Point s = new Point(p);
		for (short x = (short)(p.x - f.getRange() + 1); x < p.x + f.getRange(); x++) {
			for (short y = (short)(p.y + f.getRange() -1); y > p.y - f.getRange(); y--) {
				s.move(x, y);
				if (getModel().isWithin(s)) {
					getModel().toggleFlag(s, f, on ? MapModel.ON : MapModel.OFF);
				}
			}
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
					getModel().setView(rect);
					getModel().setSelected(p);
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
							handleFlagEdit(pos, (Flag)f);
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
					if (getModel().isBusy()) {
						return;
					}
					if (e.getStateChange() == ItemEvent.SELECTED) {
						Point pos = getModel().getSelected();
						Terrain t = (Terrain)e.getItem();
						if (pos == null || t == null) {
							return;
						}
						handleFeatureEdit(pos, EditPanel.ADD, t);
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
		getModel().setSquareNote(pos, note);
	}
	
	/**
	 * Adds or deletes a feature from the selected square 
	 * @param pos the co-ordinates of the square
	 * @param action the action to take. This is one of the EditPanel constants ADD or DELETE
	 * @param f the feature to add or delete
	 */
	private void handleFeatureEdit(Point pos, String action, Feature f) {
		if (action.equals(EditPanel.ADD)) {
			if (f instanceof Terrain) {
				getModel().setTerrain(pos, (Terrain)f);
			} else {
				getModel().add(pos, f);
			}
		} else if (action.equals(EditPanel.DELETE)) {
			getModel().remove(pos, f);
		}
	}
	
	private void handleFlagEdit(Point pos, Flag f) {
		getModel().toggleFlag(pos, f, MapModel.INVERSE);
	}
	
	private void mergeFlags(Point pos) {
		Iterator<Flag> it = DataStore.getInstance().getFlags().iterator();
		while (it.hasNext()) {
			Flag f = it.next();
			if (getModel().isFlagged(pos, f)) {
				getModel().toggleFlag(pos, f, MapModel.ON);
			}
		}
	}
	
	/**
	 * Returns the minimap component
	 * @return the minimap component
	 */
	public MiniMap getMiniMap() {
		if (miniMap == null) {
			miniMap = new MiniMap(getModel());
			miniMap.setIndex(getIndex());
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
					if (p.x > bounds.x -view.width / 2 )
					p.translate(-view.width / 2 + bounds.x, -view.height/2);
					p.move(p.x, bounds.y - p.y);
					getModel().setView(new Rectangle(p.x, p.y, view.width, view.height));
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
			toolBar.add(getCompareAction());
			toolBar.add(new JToolBar.Separator());
			toolBar.add(getUndoAction());
			toolBar.add(getRedoAction());
			toolBar.add(new JToolBar.Separator());
			toolBar.add(getFindAction());
			toolBar.add(new JToolBar.Separator());
			toolBar.add(getCmbSize());
			toolBar.add(getCmdFlag());
			toolBar.add(new JToolBar.Separator());
			toolBar.add(getCoordinatePanel());
			toolBar.add(new JToolBar.Separator());
			//toolBar.add(getScryeAction());
			toolBar.add(getExtractAction());
			toolBar.add(getResizeAction());
			toolBar.add(getSaveDifferencesAction());
			toolBar.add(getSaveMergedAction());
			toolBar.add(getTranslateAction());
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
			newAction = new NewAction(this);
		}
		return newAction;
	}
	
	/**
	 * Returns the Open Map action
	 * @return the Open Map action
	 */
	public OpenAction getOpenAction() {
		if (openAction == null) {
			openAction = new OpenAction(this);
		}
		return openAction;
	}
	
	/**
	 * Returns the Save Map action
	 * @return the Save Map action
	 */
	public SaveAction getSaveAction() {
		if (saveAction == null) {
			saveAction = new SaveAction(this);
		}
		return saveAction;
	}
	
	/**
	 * Returns the Translate Map action
	 * @return the Translate Map action
	 */
	public TranslateAction getTranslateAction() {
		if (translateAction == null) {
			translateAction = new TranslateAction(this);
		}
		return translateAction;
	}

	/**
	 * Returns the Undo action
	 * @return the Undo action
	 */
	public UndoAction getUndoAction() {
		if (undoAction == null) {
			undoAction = new UndoAction(getUndoManager());
		}
		return undoAction;
	}
	
	/**
	 * Returns the Redo action
	 * @return the Redo action
	 */
	public RedoAction getRedoAction() {
		if (redoAction == null) {
			redoAction = new RedoAction(getUndoManager());
		}
		return redoAction;
	}
	
	/**
	 * Returns the Find action
	 * @return the Find action
	 */
	public FindAction getFindAction() {
		if (findAction == null) {
			findAction = new FindAction(this);
		}
		return findAction;
	}
	
//	/**
//	 * Returns the Diff action
//	 * @return the Diff action
//	 */
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
			compAction = new CompareAction(this);
		}
		return compAction;
	}
	
	public SaveDifferencesAction getSaveDifferencesAction() {
		if (saveDifferencesAction == null) {
			saveDifferencesAction = new SaveDifferencesAction(this);
		}
		return saveDifferencesAction;
	}
	
	public SaveMergedAction getSaveMergedAction() {
		if (saveMergedAction == null) {
			saveMergedAction = new SaveMergedAction(this);
		}
		return saveMergedAction;
	}
	
	/**
	 * Returns the Scrye action
	 * @return the Scrye action
	 */
	public ScryeAction getScryeAction() {
		if (scryeAction == null) {
			scryeAction = new ScryeAction(this);
		}
		return scryeAction;
	}

	/**
	 * Returns the Extract action
	 * @return the Extract action
	 */
	public ExtractAction getExtractAction() {
		if (extractAction == null) {
			extractAction = new ExtractAction(this);
		}
		return extractAction;
	}

	/**
	 * Returns the Resize action
	 * @return the Resize action
	 */
	public ResizeAction getResizeAction() {
		if (resizeAction == null) {
			resizeAction = new ResizeAction(this);
		}
		return resizeAction;
	}

	/**
	 * Returns the Show/Hide flags togglebutton
	 * @return the Show/Hide flags togglebutton
	 */
	private JToggleButton getCmdFlag() {
		if (cmdFlag == null) {
			cmdFlag = new JToggleButton(new FlagAction(this));
			cmdFlag.setText("");
			cmdFlag.setSelected(true);
		}
		return cmdFlag;
	}
	
	private CoordinatePanel getCoordinatePanel() {
		if (panCoordinates == null) {
			panCoordinates = new CoordinatePanel(this);
		}
		return panCoordinates;
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
					//refresh();
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
		getModel().setView(rect);
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
