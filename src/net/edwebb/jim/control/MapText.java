package net.edwebb.jim.control;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultEditorKit;

/**
 * The Text dialog box allows a user to enter some arbitrary text that the application will process and do something with.
 * 
 * @author Ed Webb
 *
 */
public class MapText extends JDialog {
	
	/**
	 * The version ID
	 */
	private static final long serialVersionUID = 1L;
	
	private boolean cancelled = false;
	
	private JPanel panInput;
		private JScrollPane scrArea;
		private JTextArea txtArea;
	
	private JPanel panControl;
		private JButton cmdOK;
		private JButton cmdCancel;
	

	private JPopupMenu menu;
		private JMenuItem copy;
		private JMenuItem cut;	
		private JMenuItem paste;
		
	/**
	 * Create a new Find dialog box
	 * @param parent the parent of this box
	 */
	public MapText(Frame parent) {
		super(parent, "Enter Text", true);
		
		this.setLayout(new BorderLayout());
		this.add(getPanInput(), BorderLayout.CENTER);
		this.add(getPanControl(), BorderLayout.SOUTH);
		Point point = new Point(parent.getLocation().x + 40, parent.getLocation().y + 40);
		this.setLocation(point);
	}
	
	/**
	 * Returns the panel containing the input fields
	 * @return the panel containing the input fields
	 */
	private JPanel getPanInput() {
		if (panInput == null) {
			panInput = new JPanel();
			panInput.add(getScrArea());
		}
		return panInput;
	}
	

	/**
	 * Returns the text area that holds the text
	 * @return the text area that holds the text
	 */
	private JTextArea getTxtArea() {
		if (txtArea == null) {
			txtArea = new JTextArea(10, 50);
			txtArea.addMouseListener(new MouseAdapter() {
				public void mouseReleased(final MouseEvent e) {
					if (e.isPopupTrigger()) {
						getCopyItem().setEnabled(getTxtArea().getSelectionStart() != getTxtArea().getSelectionEnd());
						getCutItem().setEnabled(getTxtArea().getSelectionStart() != getTxtArea().getSelectionEnd());
						getPasteItem().setEnabled(e.getComponent().getToolkit().getSystemClipboard().isDataFlavorAvailable(DataFlavor.stringFlavor));
						getContextMenu().show(e.getComponent(), e.getX(), e.getY());
					}
				}
			});

		}
		return txtArea;
	}

	private JPopupMenu getContextMenu() {
		if (menu == null) {
			menu = new JPopupMenu();
		    menu.add(getCopyItem());
		    menu.add(getCutItem());
		    menu.add(getPasteItem());
		}
		return menu;
	}
	
	private JMenuItem getCopyItem() {
		if (copy == null) {
			copy = new JMenuItem(new DefaultEditorKit.CopyAction());
		    copy.setText("Copy");
		}
		return copy;
	}
	
	private JMenuItem getCutItem() {
		if (cut == null) {
			cut = new JMenuItem(new DefaultEditorKit.CutAction());
		    cut.setText("Cut");
		}
		return cut;
	}
	
	private JMenuItem getPasteItem() {
		if (paste == null) {
			paste = new JMenuItem(new DefaultEditorKit.PasteAction());
			paste.setText("Paste");
		}
		return paste;
	}
	
	
	/**
	 * Returns the text pane that holds the text
	 * @return the text pane that holds the text
	 */
	private JScrollPane getScrArea() {
		if (scrArea == null) {
			scrArea = new JScrollPane(getTxtArea());
		}
		return scrArea;
	}
	
	/**
	 * Returns the panel that contains the command button
	 * @return the panel that contains the command button
	 */
	private JPanel getPanControl() {
		if (panControl == null) {
			panControl = new JPanel(new FlowLayout(FlowLayout.TRAILING));
			panControl.add(getCmdOK());
			panControl.add(getCmdCancel());
		}
		return panControl;
	}

	/**
	 * Returns the command button
	 * @return the command button
	 */
	private JButton getCmdOK() {
		if (cmdOK == null) {
			cmdOK = new JButton("OK");
			cmdOK.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					cancelled = false;
					setVisible(false);
					dispose();
				}
			});
		}
		return cmdOK;
	}

	/**
	 * Returns the command button
	 * @return the command button
	 */
	private JButton getCmdCancel() {
		if (cmdCancel == null) {
			cmdCancel = new JButton("Cancel");
			cmdCancel.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					cancelled = true;
					setVisible(false);
					dispose();
				}
			});
		}
		return cmdCancel;
	}
	
	/**
	 * Displays the text dialog box and returns the text entered or null if the dialog was cancelled
	 * @return the entered text or null
	 */
	public String getText() {
		getTxtArea().setText("");
		cancelled = true;
		this.pack();
		this.setVisible(true);
		if (cancelled) {
			return null;
		} else {
			return getTxtArea().getText();
		}
	}
}

