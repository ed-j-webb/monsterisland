package net.edwebb.jim.view;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JToolBar;

import net.edwebb.jim.control.MapController;
import net.edwebb.jim.control.actions.CoordAction;
import net.edwebb.jim.model.MapModel;
import net.edwebb.jim.model.events.CoordinateChangeEvent;
import net.edwebb.jim.model.events.MapChangeEvent;
import net.edwebb.jim.model.events.MapChangeEvent.MAP_CHANGE_TYPE;
import net.edwebb.jim.model.events.MapChangeListener;
import net.edwebb.mi.data.Coordinate;
import net.edwebb.mi.data.DataStore;

public class CoordinatePanel extends JToolBar implements MapChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private MapModel model;
	private JComboBox<Coordinate> cmbCoords;
	private CoordAction coordAction;

	public CoordinatePanel(MapController controller) {
		setLayout(new FlowLayout(FlowLayout.LEADING));
		add(getCmbCoords());
		add(getCoordAction(controller));
		setFloatable(false);
		setBorder(null);
		setModel(controller.getModel());
	}
	
	public MapModel getModel() {
		return model;
	}
	
	/**
	 * Adds the model's coOrdinate system to the combobox if it is not already present
	 * 
	 * @param cmbBox the Co-ordinates combo box
	 * @param model the map model
	 */
	public void setModel(MapModel model) {
		if (this.model != null) {
			this.model.removeMapChangeListener(this);
		}
		this.model = model;
		model.addMapChangeListener(this);
		JComboBox<Coordinate> cmbBox = getCmbCoords();
		if (model.getDefaultCoOrdinates() == null || model.getDefaultCoOrdinates().getOffset().equals(new Point(0,0))) {
			cmbBox.setSelectedIndex(0);
			return;
		}
		for (int i = 0; i < cmbBox.getItemCount(); i++) {
			if (cmbBox.getItemAt(i).equals(model.getDefaultCoOrdinates())) {
				cmbBox.setSelectedIndex(i);
				return;
			}
		}
		cmbBox.addItem(model.getDefaultCoOrdinates());
	}
	
	/**
	 * Returns the Co-ordinate offset combo box
	 * @return the Co-ordinate offset combo box
	 */
	private JComboBox<Coordinate> getCmbCoords() {
		if (cmbCoords == null) {
			cmbCoords = new JComboBox<Coordinate>(DataStore.getInstance().getCoordinates().toArray(new Coordinate[DataStore.getInstance().getCoordinates().size()]));
			cmbCoords.addItemListener(new ItemListener() {
				
				@Override
				public void itemStateChanged(ItemEvent e) {
					getModel().setCurrentCoOrdinates((Coordinate)e.getItem());
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
	
	/**
	 * Returns the Set Co-ordinates action
	 * @return the Set Co-ordinates action
	 */
	public CoordAction getCoordAction(MapController controller) {
		if (coordAction == null) {
			coordAction = new CoordAction(controller, this.getCmbCoords());
		}
		return coordAction;
	}

	@Override
	public void mapChanged(MapChangeEvent event) {
		if (event.getChangeType().equals(MAP_CHANGE_TYPE.COORDINATE)) {
			CoordinateChangeEvent coordEvent = (CoordinateChangeEvent)event;
			if (coordEvent.getNewCoord() != null) {
				getCmbCoords().setSelectedItem(coordEvent.getNewCoord());
			}
			repaint();
		}
		
	}
	

}
