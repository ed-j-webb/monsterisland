package net.edwebb.jim.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollBar;

import net.edwebb.jim.model.MapModel;
import net.edwebb.jim.model.MapSearch;
import net.edwebb.jim.model.events.MapChangeEvent;
import net.edwebb.jim.model.events.MapChangeListener;
import net.edwebb.jim.model.events.ViewChangeEvent;
import net.edwebb.jim.model.events.MapChangeEvent.MAP_CHANGE_TYPE;

public class ViewPanel extends JPanel implements MapChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private MapRuler ruleHorizontal;
	private MapRuler ruleVertical;
	private MapPanel panMap;
	private JScrollBar scrHorizontal;
	private JScrollBar scrVertical;

	private MapModel model;
	
	public ViewPanel(MapModel model) {
		setModel(model);
		this.setPreferredSize(new Dimension(500, 300));
		buildView();
	}
	
	public void setModel(MapModel model) {
		if (this.model != null) {
			this.model.removeMapChangeListener(this);
		}
		this.model = model;
		model.addMapChangeListener(this);
		
		getPanMap().setModel(model);
		getRuleHorizontal().setModel(model);
		getRuleVertical().setModel(model);
		
		getScrHorizontal().setMinimum(model.getBounds().x);
		getScrHorizontal().setValue(model.getView().x);
		getScrHorizontal().setMaximum(model.getBounds().x + model.getBounds().width - model.getView().width + getScrHorizontal().getBlockIncrement() + 1);
		getScrVertical().setMinimum(-model.getBounds().y);
		getScrVertical().setValue(-model.getView().y);
		getScrVertical().setMaximum(model.getBounds().height - model.getBounds().y - model.getView().height + getScrVertical().getBlockIncrement() + 1);
	}
	
	public void setSearch(MapSearch search) {
		getPanMap().setSearch(search);
	}
	
	private void buildView() {
		this.setLayout(new BorderLayout());

		this.add(getRuleHorizontal(), BorderLayout.NORTH);
		this.add(getRuleVertical(), BorderLayout.WEST);
		this.add(getPanMap(), BorderLayout.CENTER);
		this.add(getScrVertical(), BorderLayout.EAST);
		this.add(getScrHorizontal(), BorderLayout.SOUTH);
	}
	
	private MapRuler getRuleHorizontal() {
		if (ruleHorizontal == null) {
			ruleHorizontal = new MapRuler(MapRuler.HORIZONTAL, model);
			ruleHorizontal.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 16));
		} 
		return ruleHorizontal;
	}
	
	private MapRuler getRuleVertical() {
		if (ruleVertical == null) {
			ruleVertical = new MapRuler(MapRuler.VERTICAL, model);
		}
		return ruleVertical;
	}
	
	private MapPanel getPanMap() {
		if (panMap == null) {
			panMap = new MapPanel(model);
			panMap.setFocusable(true);
		}
		return panMap;
	}
	
	public Dimension getDimension() {
		return getPanMap().getSize();
	}
	
	private JScrollBar getScrHorizontal() {
		if (scrHorizontal == null) {
			scrHorizontal = new JScrollBar(JScrollBar.HORIZONTAL, 0, 10, model.getBounds().x, model.getBounds().x + model.getBounds().width);
			scrHorizontal.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 16));
		}
		return scrHorizontal;
	}
	
	private JScrollBar getScrVertical() {
		if (scrVertical == null) {
			scrVertical = new JScrollBar(JScrollBar.VERTICAL, 0, 10, -model.getBounds().y, model.getBounds().height - model.getBounds().y);
		}
		return scrVertical;
	}
	
	@Override
	public void mapChanged(MapChangeEvent event) {
		if (event.getChangeType().equals(MAP_CHANGE_TYPE.VIEW)) {
			ViewChangeEvent viewEvent = (ViewChangeEvent)event;
			if (viewEvent.getNewView() != null) {
				getScrHorizontal().setValue(viewEvent.getNewView().x);
				getScrVertical().setValue(-viewEvent.getNewView().y);

				// TODO Don't think these are needed as they don't change after a model has been loaded
				//getScrHorizontal().setMaximum(model.getBounds().x + model.getBounds().width - model.getView().width + getScrHorizontal().getBlockIncrement() + 1);
				//getScrVertical().setMaximum(model.getBounds().height - model.getBounds().y - model.getView().height + getScrVertical().getBlockIncrement() + 1);
				repaint();
			}
			return;
		}

		if (event.getChangeType().equals(MAP_CHANGE_TYPE.TERRAIN)
		 || event.getChangeType().equals(MAP_CHANGE_TYPE.SELECTED)
		 || event.getChangeType().equals(MAP_CHANGE_TYPE.COORDINATE)
		 || event.getChangeType().equals(MAP_CHANGE_TYPE.FEATURE)
		 || event.getChangeType().equals(MAP_CHANGE_TYPE.FLAG)
		 || event.getChangeType().equals(MAP_CHANGE_TYPE.NOTE)) {
			repaint();
			return;
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
	}

	public void setViewFlags(boolean flag) {
		getPanMap().setViewFlags(flag);
		repaint();
	}
	public boolean getViewFlags() {
		return getPanMap().isViewFlags();
	}
	
	public void addComponentListener(ComponentListener l) {
		panMap.addComponentListener(l);
	}
	
	public void addMouseListener(MouseListener l) {
		panMap.addMouseListener(l);
	}
	
	public void addMouseMotionListener(MouseMotionListener l) {
		panMap.addMouseMotionListener(l);
	}
	
	public void addKeyListener(KeyListener l) {
		panMap.addKeyListener(l);
	}
	
	public void addAdjustmentListener(AdjustmentListener l) {
		scrHorizontal.addAdjustmentListener(l);
		scrVertical.addAdjustmentListener(l);
	}

	public void removeComponentListener(ComponentListener l) {
		panMap.removeComponentListener(l);
	}
	
	public void removeMouseListener(MouseListener l) {
		panMap.removeMouseListener(l);
	}

	public void removeMouseMotionListener(MouseMotionListener l) {
		panMap.removeMouseMotionListener(l);
	}

	public void removeKeyListener(KeyListener l) {
		panMap.removeKeyListener(l);
	}
	
	public void removeAdjustmentListener(AdjustmentListener l) {
		scrHorizontal.removeAdjustmentListener(l);
		scrVertical.removeAdjustmentListener(l);
	}

}

