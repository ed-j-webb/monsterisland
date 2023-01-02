package net.edwebb.jim.view;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.Spring;
import javax.swing.SpringLayout;

/**
 * A 1.4 file that provides utility methods for
 * creating form- or grid-style layouts with SpringLayout.
 * These utilities are used by several programs, such as
 * SpringBox and SpringCompactGrid.
 */
public class SpringUtilities {
    
    public static final int PREFERRED = -1;
    public static final int MAXIMUM = -2;
    public static final int MINIMUM = -3;
    public static final int WIDTH = -4;
    public static final int HEIGHT = -8;
    
    /**
     * A debugging utility that prints to stdout the component's
     * minimum, preferred, and maximum sizes.
     */
    public static void printSizes(Component c) {
        System.out.println("minimumSize = " + c.getMinimumSize());
        System.out.println("preferredSize = " + c.getPreferredSize());
        System.out.println("maximumSize = " + c.getMaximumSize());
    }

    /**
     * Fixes the default Height of the component. This is done by 
     * setting the Maximum size's height to the preferred size's 
     * height.
     * @param c the component to fix the height of
     */
    public static void fixHeight(JComponent c) {
        Dimension p = c.getPreferredSize();
        Dimension m = c.getMaximumSize();
        c.setMaximumSize(new Dimension(m.width, p.height));
    }
    
    /**
     * fixes the Height of the component to the given value. This 
     * is done by setting
     * the Maximum size's height and the preferred size's height to
     * the given value
     * @param c the component to fix the height of
     * @param height the height of the component. This can be an positive value or one of the constants MINIMUM, MAXIMUM or PREFERRED to set the dimension to the current value of the Minimum, Maximum or Preferred size.
     */
    public static void fixHeight(JComponent c, int height) {
        height = getValue(c, height, HEIGHT);
        Dimension p = c.getPreferredSize();
        Dimension m = c.getMaximumSize();
        c.setMaximumSize(new Dimension(m.width, height));
        c.setPreferredSize(new Dimension(p.width, height));
    }
    
    /**
     * Fixes the default Width of the component. This is done by 
     * setting the Maximum size's width to the preferred size's 
     * width.
     * @param c the component to fix the width of
     */
    public static void fixWidth(JComponent c) {
        Dimension p = c.getPreferredSize();
        Dimension m = c.getMaximumSize();
        c.setMaximumSize(new Dimension(p.width, m.height));
    }

    /**
     * Fixes the width of the component to the given value. This 
     * is done by setting
     * the Maximum size's width and the preferred size's width to
     * the given value
     * @param c the component to fix the width of
     * @param width the width of the component This can be an positive value or one of the constants MINIMUM, MAXIMUM or PREFERRED to set the dimension to the current value of the Minimum, Maximum or Preferred size.
     */
    public static void fixWidth(JComponent c, int width) {
        width = getValue(c, width, WIDTH);
        Dimension p = c.getPreferredSize();
        Dimension m = c.getMaximumSize();
        c.setMaximumSize(new Dimension(width, m.height));
        c.setPreferredSize(new Dimension(width, p.height));
    }

    /**
     * Fixes the default size of the component. This is done by 
     * setting the Maximum size equal to the preferred size.
     * @param c the component to fix the size of
     */
    public static void fixSize(JComponent c) {
        c.setMaximumSize(c.getPreferredSize());
    }
    
    /**
     * Fixes the size of the component to the given values. This 
     * is done by setting
     * the Maximum size and the preferred size equal to the given 
     * values
     * @param c the component to fix the size of
     * @param width the width of the component. This can be an positive value or one of the constants MINIMUM, MAXIMUM or PREFERRED to set the dimension to the current value of the Minimum, Maximum or Preferred size.
     * @param height the height of the component. This can be an positive value or one of the constants MINIMUM, MAXIMUM or PREFERRED to set the dimension to the current value of the Minimum, Maximum or Preferred size.
     */
    public static void fixSize(JComponent c, int width, int height) {
        height = getValue(c, height, HEIGHT);
        width = getValue(c, width, WIDTH);
        c.setMaximumSize(new Dimension(width, height));
        c.setPreferredSize(new Dimension(width, height));
    }

    public static void unfixWidth(JComponent c) {
        int height = getValue(c, MAXIMUM, HEIGHT);
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
    }
    
    public static void unfixHeight(JComponent c) {
        int width = getValue(c, MAXIMUM, HEIGHT);
        c.setMaximumSize(new Dimension(width, Integer.MAX_VALUE));
    }

    public static void unfixSize(JComponent c) {
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
    }

    /**
     * Returns the value to set the dimension to. If value is positive it is returned. If it is one of the constants MAXIMUM, MINIMUM or PREFERRED then the value of the maximum, minimum or preferred size's dimension is returned.
     * @param c the Component to use
     * @param value the dimensions size or one of the constants MAXIMUM, MINIMUM or PREFERRED.
     * @param dimension the dimension that is to be set (either of the constants WIDTH or HEIGHT).
     * @return the size to set the dimension of the control to
     */
    private static int getValue(JComponent c, int value, int dimension) {
        if (value >= 0) {
            return value;
        } else if (PREFERRED == value) {
            return getDimension(c.getPreferredSize(), dimension);
        } else if (MAXIMUM == value) {
            return getDimension(c.getMaximumSize(), dimension);
        } else if (MINIMUM == value) {
            return getDimension(c.getMaximumSize(), dimension);
        } else {
            return value;
        }
    }

    /**
     * Returns the size of the specified dimension from the Dimension.
     * @param size the Dimension to examine.
     * @param dimension either of the constants WIDTH or HEIGHT.
     * @return the size of the specified dimension.
     */
    private static int getDimension(Dimension size, int dimension) {
        if (WIDTH == dimension) {
            return size.width;
        } else {
            return size.height;
        }
    }
    
    /**
     * Aligns the first <code>rows</code> * <code>cols</code>
     * components of <code>parent</code> in
     * a grid. Each component is as big as the maximum
     * preferred width and height of the components.
     * The parent is made just big enough to fit them all.
     *
     * @param rows number of rows
     * @param cols number of columns
     * @param initialX x location to start the grid at
     * @param initialY y location to start the grid at
     * @param xPad x padding between cells
     * @param yPad y padding between cells
     */
    public static void makeGrid(Container parent,
                                int rows, int cols,
                                int initialX, int initialY,
                                int xPad, int yPad) {
        SpringLayout layout;
        try {
            layout = (SpringLayout)parent.getLayout();
        } catch (ClassCastException exc) {
            System.err.println("The first argument to makeGrid must use SpringLayout.");
            return;
        }

        Spring xPadSpring = Spring.constant(xPad);
        Spring yPadSpring = Spring.constant(yPad);
        Spring initialXSpring = Spring.constant(initialX);
        Spring initialYSpring = Spring.constant(initialY);
        int max = rows * cols;

        //Calculate Springs that are the max of the width/height so that all
        //cells have the same size.
        Spring maxWidthSpring = layout.getConstraints(parent.getComponent(0)).
                                    getWidth();
        Spring maxHeightSpring = layout.getConstraints(parent.getComponent(0)).
                                    getWidth();
        for (int i = 1; i < max; i++) {
            SpringLayout.Constraints cons = layout.getConstraints(
                                            parent.getComponent(i));

            maxWidthSpring = Spring.max(maxWidthSpring, cons.getWidth());
            maxHeightSpring = Spring.max(maxHeightSpring, cons.getHeight());
        }

        //Apply the new width/height Spring. This forces all the
        //components to have the same size.
        for (int i = 0; i < max; i++) {
            SpringLayout.Constraints cons = layout.getConstraints(
                                            parent.getComponent(i));

            cons.setWidth(maxWidthSpring);
            cons.setHeight(maxHeightSpring);
        }

        //Then adjust the x/y constraints of all the cells so that they
        //are aligned in a grid.
        SpringLayout.Constraints lastCons = null;
        SpringLayout.Constraints lastRowCons = null;
        for (int i = 0; i < max; i++) {
            SpringLayout.Constraints cons = layout.getConstraints(
                                                 parent.getComponent(i));
            if (i % cols == 0) { //start of new row
                lastRowCons = lastCons;
                cons.setX(initialXSpring);
            } else { //x position depends on previous component
                cons.setX(Spring.sum(lastCons.getConstraint(SpringLayout.EAST),
                                     xPadSpring));
            }

            if (i / cols == 0) { //first row
                cons.setY(initialYSpring);
            } else { //y position depends on previous row
                cons.setY(Spring.sum(lastRowCons.getConstraint(SpringLayout.SOUTH),
                                     yPadSpring));
            }
            lastCons = cons;
        }

        //Set the parent's size.
        SpringLayout.Constraints pCons = layout.getConstraints(parent);
        pCons.setConstraint(SpringLayout.SOUTH,
                            Spring.sum(
                                Spring.constant(yPad),
                                lastCons.getConstraint(SpringLayout.SOUTH)));
        pCons.setConstraint(SpringLayout.EAST,
                            Spring.sum(
                                Spring.constant(xPad),
                                lastCons.getConstraint(SpringLayout.EAST)));
    }

    /* Used by makeCompactGrid. */
    private static SpringLayout.Constraints getConstraintsForCell(
                                                int row, int col,
                                                Container parent,
                                                int cols) {
        SpringLayout layout = (SpringLayout) parent.getLayout();
        Component c = parent.getComponent(row * cols + col);
        return layout.getConstraints(c);
    }
    
    private static boolean isFixedWidthComponent(int row, int col, Container parent, int cols) {
        Component c = parent.getComponent(row * cols + col);
        if (c.getPreferredSize().width == c.getMaximumSize().width) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Aligns the first <code>rows</code> * <code>cols</code>
     * components of <code>parent</code> in
     * a grid. Each component in a column is as wide as the maximum
     * preferred width of the components in that column;
     * height is similarly determined for each row.
     * The parent is made just big enough to fit them all.
     *
     * @param rows number of rows
     * @param cols number of columns
     * @param initialX x location to start the grid at
     * @param initialY y location to start the grid at
     * @param xPad x padding between cells
     * @param yPad y padding between cells
     */
    public static void makeCompactGrid(Container parent,
                                       int rows, int cols,
                                       int initialX, int initialY,
                                       int xPad, int yPad) {
        SpringLayout layout;
        try {
            layout = (SpringLayout)parent.getLayout();
        } catch (ClassCastException exc) {
            System.err.println("The first argument to makeCompactGrid must use SpringLayout.");
            return;
        }

        //Align all cells in each column and make them the same width.
        Spring x = Spring.constant(initialX);
        for (int c = 0; c < cols; c++) {
            Spring width = Spring.constant(0);
            for (int r = 0; r < rows; r++) {
                width = Spring.max(width,
                                   getConstraintsForCell(r, c, parent, cols).
                                       getWidth());
            }
            for (int r = 0; r < rows; r++) {
                SpringLayout.Constraints constraints =
                        getConstraintsForCell(r, c, parent, cols);
                constraints.setX(x);
                constraints.setWidth(width);
            }
            x = Spring.sum(x, Spring.sum(width, Spring.constant(xPad)));
        }

        //Align all cells in each row and make them the same height.
        Spring y = Spring.constant(initialY);
        for (int r = 0; r < rows; r++) {
            Spring height = Spring.constant(0);
            for (int c = 0; c < cols; c++) {
                height = Spring.max(height,
                                    getConstraintsForCell(r, c, parent, cols).
                                        getHeight());
            }
            for (int c = 0; c < cols; c++) {
                SpringLayout.Constraints constraints =
                        getConstraintsForCell(r, c, parent, cols);
                constraints.setY(y);
                constraints.setHeight(height);
            }
            y = Spring.sum(y, Spring.sum(height, Spring.constant(yPad)));
        }

        //Set the parent's size.
        SpringLayout.Constraints pCons = layout.getConstraints(parent);
        pCons.setConstraint(SpringLayout.SOUTH, y);
        pCons.setConstraint(SpringLayout.EAST, x);
    }


    public static void makePropertyGrid(Container parent,
            int rows, int cols,
            int initialX, int initialY,
            int xPad, int yPad) {
        makePropertyGrid(parent, rows, cols, initialX, initialY, xPad, yPad, new int[cols]);
    }
    
    
    /**
     * Aligns the first <code>rows</code> * <code>cols</code>
     * components of <code>parent</code> in
     * a grid. Each column is as wide as the maximum
     * preferred width of the components in that column and 
     * each row is a high as the tallest component. All components 
     * in the first row are as wide as the widest component (as these
     * should be labels for controls) but apart from this all components
     * retain the preferred size.
     * The parent is made just big enough to fit them all.
     *
     * @param rows number of rows
     * @param cols number of columns
     * @param initialX x location to start the grid at
     * @param initialY y location to start the grid at
     * @param xPad x padding between cells
     * @param yPad y padding between cells
     * @param prefWidths the preferred widths of the columns. 0 = use value as calculated
     */
    public static void makePropertyGrid(Container parent,
                                       int rows, int cols,
                                       int initialX, int initialY,
                                       int xPad, int yPad,
                                       int[] prefWidths) {
        SpringLayout layout;
        try {
            layout = (SpringLayout)parent.getLayout();
        } catch (ClassCastException exc) {
            System.err.println("The first argument to makePropertyGrid must use SpringLayout.");
            return;
        }
        
        if (prefWidths.length < cols) {
            System.err.println("The Preferred Widths array must be the same size as the cols argument.");
            return;
        }

        //Align all cells in each column and make them the same width.
        Spring x = Spring.constant(initialX);
        for (int c = 0; c < cols; c++) {
            Spring width = Spring.constant(0);
            for (int r = 0; r < rows; r++) {
                width = Spring.max(width, getConstraintsForCell(r, c, parent, cols).getWidth());
            }
            for (int r = 0; r < rows; r++) {
                SpringLayout.Constraints constraints = getConstraintsForCell(r, c, parent, cols);
                constraints.setX(x);
                if (0 == c || !isFixedWidthComponent(r,c,parent,cols)) {
                    if (prefWidths[c] > 0) {
                        Spring prefWidth = Spring.constant(0, prefWidths[c], Integer.MAX_VALUE);
                        width = Spring.minus(Spring.max(Spring.minus(prefWidth), Spring.minus(width)));
                    }
                    constraints.setWidth(width);
                } 
            }
            x = Spring.sum(x, Spring.sum(width, Spring.constant(xPad)));
        }

        //Align all cells in each row and make them the same height.
        Spring y = Spring.constant(initialY);
        for (int r = 0; r < rows; r++) {
            Spring height = Spring.constant(0);
            for (int c = 0; c < cols; c++) {
                height = Spring.max(height,
                                    getConstraintsForCell(r, c, parent, cols).
                                        getHeight());
            }
            for (int c = 0; c < cols; c++) {
                SpringLayout.Constraints constraints =
                        getConstraintsForCell(r, c, parent, cols);
                constraints.setY(y);
                //if (c > 0) {
                //    constraints.setHeight(height);
                //}
            }
            y = Spring.sum(y, Spring.sum(height, Spring.constant(yPad)));
        }

        //Set the parent's size.
        SpringLayout.Constraints pCons = layout.getConstraints(parent);
        pCons.setConstraint(SpringLayout.SOUTH, y);
        pCons.setConstraint(SpringLayout.EAST, x);
    }

    public static void makeBigGrid(Container parent,
                                       int initialX, int initialY,
                                       int xPad, int yPad) {

        SpringLayout layout;
        try {
            layout = (SpringLayout)parent.getLayout();
        } catch (ClassCastException exc) {
            System.err.println("The first argument to makePropertyGrid must use SpringLayout.");
            return;
        }
        
        JComponent last = null;
        JComponent curr = null;
        Component[] comps = parent.getComponents();
        for (int i = 0; i < comps.length; i++) {
            curr = (JComponent)comps[i];
            unfixWidth(curr);
            if (last != null) {
                layout.putConstraint(SpringLayout.NORTH, curr, yPad, SpringLayout.SOUTH, last);
                layout.putConstraint(SpringLayout.EAST, curr, 0, SpringLayout.EAST, last);
                layout.putConstraint(SpringLayout.WEST, curr, 0, SpringLayout.WEST, last);
            } else {
                layout.putConstraint(SpringLayout.WEST, curr, initialX, SpringLayout.WEST, parent);
                layout.putConstraint(SpringLayout.EAST, parent, initialX, SpringLayout.EAST, curr);
                layout.putConstraint(SpringLayout.NORTH, curr, yPad, SpringLayout.NORTH, parent);
            }
            last = curr;
        }
    }
    
}
