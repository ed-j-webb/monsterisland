package net.edwebb.mi.data;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.TexturePaint;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;

/**
 * A feature that represents a terrain
 * 
 * @author Ed Webb
 *
 */
public class Terrain extends Feature implements Comparable<Terrain> {

	// The terrain's background colour
	private Color colour;
	
	// The terrain's foreground colour
	private Color text;
	
	// The terrain's icon
	private ImageIcon icon;
	
	// The terrain's texture paint object
	private TexturePaint tp;
	
	/**
	 * Create a new terrain with the given id, code, name, colour scheme and icon
	 * @param id the id of the terrain
	 * @param code the code of the terrain. This is by convention one letter in upper case. 
	 * @param name the name of the terrain
	 * @param colour the background colour
	 * @param text the foreground colour
	 * @param icon the icon
	 */
	public Terrain(short id, String code, String name, Color colour, Color text, ImageIcon icon) {
		super(id, code, name);
		this.colour = colour;
		this.text = text;
		this.icon = icon;
	}

	/**
	 * Returns the background colour
	 * @return the background colour
	 */
	public Color getColour() {
		return colour;
	}

	/**
	 * Returns the foreground colour
	 * @return the foreground colour
	 */
	public Color getText() {
		return text;
	}
	
	/**
	 * Returns the image inside the icon
	 * @return the image inside the icon
	 */
	public Image getImage() {
		return icon.getImage();
	}
	
	@Override
	public ImageIcon getIcon() {
		return icon;
	}
	
	/**
	 * Returns the texturepaint for the terrain
	 * @return the texturepaint for the terrain
	 */
	public TexturePaint getTexture() {
		if (tp == null) {
			 // Create a buffered image with transparency
			Image img = icon.getImage();
		    BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

		    // Draw the image on to the buffered image
		    Graphics2D bGr = bimage.createGraphics();
		    bGr.drawImage(img, 0, 0, null);
		    bGr.dispose();
			tp = new TexturePaint(bimage, new java.awt.Rectangle(0, 0, 26, 26));
		}
		return tp;
	}

	@Override
	public int compareTo(Terrain o) {
		if (o == null) {
			return 1;
		} else {
			return getName().compareTo(o.getName());
		}
	}
}


