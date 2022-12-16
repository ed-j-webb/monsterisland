package net.edwebb.jim.control;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;

import net.edwebb.jim.view.SpringUtilities;

/**
 * A Dialog box to enter the top left square and the width and height of the map
 * 
 * @author Ed Webb
 *
 */
public class MapDimensions extends JDialog {

	/**
	 * The version ID
	 */
	private static final long serialVersionUID = 1L;

	private JPanel panInput;
		private JSpinner spnLeft;
			private SpinnerNumberModel mdlLeft;
		private JSpinner spnTop;
			private SpinnerNumberModel mdlTop;
		private JSpinner spnWidth;
			private SpinnerNumberModel mdlWidth;
		private JSpinner spnHeight;
			private SpinnerNumberModel mdlHeight;
	private JPanel panControl;
		private JButton cmdOK;
	
	/**
	 * Create a new Map Dimensions dialog box
	 * @param parent the parent frame
	 */
	public MapDimensions(Frame parent) {
		super(parent, "Map Dimensions", true);
		this.setLayout(new BorderLayout());
		this.add(getPanInput(), BorderLayout.CENTER);
		this.add(getPanControl(), BorderLayout.SOUTH);
		this.getRootPane().setDefaultButton(getCmdOK());
		Point point = new Point(parent.getLocation().x + 40, parent.getLocation().y + 40);
		this.setLocation(point);
	}

	/**
	 * Returns the input panel
	 * @return the input panel
	 */
	private JPanel getPanInput() {
		if (panInput == null) {
			panInput = new JPanel(new SpringLayout());
			panInput.add(new JLabel("Top"));
			panInput.add(getSpnTop());
			panInput.add(new JLabel("Left"));
			panInput.add(getSpnLeft());
			panInput.add(new JLabel("Width"));
			panInput.add(getSpnWidth());
			panInput.add(new JLabel("Height"));
			panInput.add(getSpnHeight());
			SpringUtilities.makeCompactGrid(panInput, 2, 4, 5, 5, 5, 5);
		}
		return panInput;
	}

	/**
	 * Displays the dialog box and returns an array of 4 integers that represent the top, left, width and height 
	 * that the map should be created with.
	 * @return an array of the top, left, width and height of the map
	 */
	public int[] getDimensions() {
		this.pack();
		this.setVisible(true);
		return new int[] {Integer.valueOf(getSpnTop().getValue().toString()), 
				          Integer.valueOf(getSpnLeft().getValue().toString()), 
				          Integer.valueOf(getSpnWidth().getValue().toString()), 
				          Integer.valueOf(getSpnHeight().getValue().toString())};

	}
	
	/**
	 * Returns the left spinner control
	 * @return the left spinner control
	 */
	private JSpinner getSpnLeft() {
		if (spnLeft == null) {
			spnLeft = new JSpinner(getMdlLeft());
		}
		return spnLeft;
	}

	/**
	 * Returns the model for the left spinner control
	 * @return the model for the left spinner control
	 */
	private SpinnerNumberModel getMdlLeft() {
		if (mdlLeft == null) {
			mdlLeft = new SpinnerNumberModel(0, -500, 500, 1);
		}
		return mdlLeft;
	}

	/**
	 * Returns the top spinner control
	 * @return the top spinner control
	 */
	private JSpinner getSpnTop() {
		if (spnTop == null) {
			spnTop = new JSpinner(getMdlTop());
		}
		return spnTop;
	}

	/**
	 * Returns the model for the top spinner control
	 * @return the model for the top spinner control
	 */
	private SpinnerNumberModel getMdlTop() {
		if (mdlTop == null) {
			mdlTop = new SpinnerNumberModel(0, -500, 500, 1);
		}
		return mdlTop;
	}

	/**
	 * Returns the width spinner control
	 * @return the width spinner control
	 */
	private JSpinner getSpnWidth() {
		if (spnWidth == null) {
			spnWidth = new JSpinner(getMdlWidth());
		}
		return spnWidth;
	}

	/**
	 * Returns the model for the width spinner control
	 * @return the model for the width spinner control
	 */
	private SpinnerNumberModel getMdlWidth() {
		if (mdlWidth == null) {
			mdlWidth = new SpinnerNumberModel(0, 0, 1000, 1);
		}
		return mdlWidth;
	}

	/**
	 * Returns the height spinner control
	 * @return the height spinner control
	 */
	private JSpinner getSpnHeight() {
		if (spnHeight == null) {
			spnHeight = new JSpinner(getMdlHeight());
		}
		return spnHeight;
	}

	/**
	 * Returns the model for the height spinner control
	 * @return the model for the height spinner control
	 */
	private SpinnerNumberModel getMdlHeight() {
		if (mdlHeight == null) {
			mdlHeight = new SpinnerNumberModel(0, 0, 1000, 1);
		}
		return mdlHeight;
	}

	/**
	 * Returns the control panel
	 * @return the control panel
	 */
	private JPanel getPanControl() {
		if (panControl == null) {
			panControl = new JPanel(new FlowLayout(FlowLayout.TRAILING));
			panControl.add(getCmdOK());
		}
		return panControl;
	}

	/**
	 * Returns the OK button
	 * @return the OK button
	 */
	private JButton getCmdOK() {
		if (cmdOK == null) {
			cmdOK = new JButton("OK");
			cmdOK.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
					dispose();
				}
			});
		}
		return cmdOK;
	}
}
