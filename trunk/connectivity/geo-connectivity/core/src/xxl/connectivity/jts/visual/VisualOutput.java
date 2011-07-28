/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2011 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 3 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library;  If not, see <http://www.gnu.org/licenses/>. 

    http://code.google.com/p/xxl/

*/
package xxl.connectivity.jts.visual;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.EmptyStackException;
import java.util.Stack;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import xxl.connectivity.jts.Geometry2DAdapter;
import xxl.core.spatial.geometries.Geometry2D;
import xxl.core.spatial.rectangles.Rectangle;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jump.workbench.ui.renderer.java2D.Java2DConverter;
import com.vividsolutions.jump.workbench.ui.renderer.java2D.Java2DConverter.PointConverter;


/** A <code>VisualOutput</code> object provides a drawing area for <code>Geometry2D</code> objects.
 *  The output is buffered and can easily be managed by means of a Stack which can save the current 
 *  output and restore it later. 
 *  <br>
 *  The current output can be saved to an image file (PNG or JPG) with {@link VisualOutput#save(String)}.
 *  <br> The transformations used to transform Geometry coordinates into image coordiates can be accessed
 *  via VisualOutput#getTransformationOp() or VisualOutput#getJava2DConverter. 
 */
public class VisualOutput extends JFrame{
	
	/** used to convert data- coordinates into view- coordinates */
	protected Java2DConverter java2DConverter;
	
	protected AffineTransform transformation;
	
	/** a reference to the graphics-contect of the current image */
	protected Graphics2D graphics;
	
	/** the current image which is shown by this frame */
	protected BufferedImage content;
	
	/** a Stack to provide some backup and rollback functionality to the output */
	protected Stack<Raster> imageStack;
	
	/** indication whether or not to refresh the output after every call to draw() */
	protected boolean repaintImmediately = false;
	
	/** Creates a new output frame.
	 * @param title the title of the output window 
	 * @param universe specifies the width-to-height ratio of the output window
	 * 	 	  and is needed for the transformation of data- coordinates into view coordinates
	 * @param maxExtension specifies the maximum width/height(according to the width-to-height ratio) 
	 */
	public VisualOutput(String title, final Rectangle universe, int maxExtension){
		super(title);				
		
		imageStack= new Stack<Raster>();							
		
		final double m = universe.deltas()[0]/universe.deltas()[1]; 
        final int	width= m>1 ? maxExtension : (int) Math.floor(maxExtension*m); 
        final int	height= m>1 ? (int) Math.floor(maxExtension/m): maxExtension; 

        content = new BufferedImage(width+1,height+1, BufferedImage.TYPE_INT_RGB);
        graphics = (Graphics2D) content.getGraphics();	    
        
        double tX =  - universe.getCorner(false).getValue(0);
        double tY =  - universe.getCorner(false).getValue(1);
        
        double sX = width / universe.deltas()[0];
        double sY = height / universe.deltas()[1];
        
        transformation = new AffineTransform();
        transformation.concatenate(AffineTransform.getScaleInstance(sX,-sY));
        transformation.concatenate(AffineTransform.getTranslateInstance(tX,tY));
        transformation.concatenate(AffineTransform.getTranslateInstance(0,- universe.deltas()[1]));	       
        
        java2DConverter = new Java2DConverter(
	       new PointConverter(){
				private double degToPixX(double d){ return d*(width)/universe.deltas()[0];}
	            private double degToPixY(double d){ return d*(height)/universe.deltas()[1];}
	                                                
				public java.awt.geom.Point2D toViewPoint(Coordinate c) throws NoninvertibleTransformException {
	                int pixX = (int) Math.floor(degToPixX(c.x - universe.getCorner(false).getValue(0)));
	                int pixY = (int) Math.floor(-1*degToPixY(c.y - universe.getCorner(true).getValue(1)));                            
	                return new java.awt.geom.Point2D.Float(pixX, pixY );                        
	            }
	        }
	    );					           

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setSize(width+20, height+50);
        this.setLocation((screenSize.width-width)/2,(screenSize.height-height)/2);        
        this.setBackground(Color.BLACK);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
	    this.setVisible(true);                    	   
	}	
	
	
	/** Returns a Java2DConverter to convert geometry-coordinates into image-coordinates  
	 * @return  a Java2DConverter to convert geometry-coordinates into image-coordinates
	 */
	public Java2DConverter getJava2DConverter(){
		return java2DConverter;
	}
	
	/** Returns the AffineTransform which converts shape-coordinates into image-coordinates
	 * @return the AffineTransform which converts shape-coordinates into image-coordinates
	 */
	public AffineTransform getTransformationOp(){
		return transformation;
	}
	
	/** Replaces the current shown image with an empty one */
	public void clear(){
		content = new BufferedImage(content.getWidth(), content.getHeight(), content.getType());
	}		
	
	/** draws the given <code>Geometry2D</code> objects to the current output image
	 * @param g the geometries to draw
	 * @param c the colors to use for the drawing. The array has to contain at least one color! 
	 */
	public void draw(Geometry2D [] g, Color[] c){
		for(int i=0; i< g.length;i++)
			draw(g[i], c[i<c.length? i: c.length-1]);						
	}
	
	/** draws the given <code>Geometry2D</code> to the current output image
	 * @param g the geometries to draw
	 * @param c the colors to use for the drawing. The array has to contain at least one color! 
	 */
	public void draw(Geometry2D g, Color c){		
		try { 
			draw(((Geometry2DAdapter) g).toShape(java2DConverter),c );
		} catch (NoninvertibleTransformException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param s
	 * @param c
	 */
	public void transformAndDraw(Shape s, Color c){
		draw(transformation.createTransformedShape(s), c);
	}
	
	/**
	 * @param s
	 * @param c
	 */
	public void draw(Shape s, Color c){		
		graphics.setColor(c);		
		graphics.draw(s);
		if(repaintImmediately) repaint();		
	}
	
	/** Fills the interior of the given <code>Geometry2D</code>s using the specified colors.
	 * @param g the geometries to draw
	 * @param c the colors to use for the drawing. The array has to contain at least one color! 
	 */
	public void fill(Geometry2D [] g, Color[] c){
		for(int i=0; i< g.length;i++)
			draw(g[i], c[i<c.length? i: c.length-1]);						
	}
	
	/** Fills the interior of the given <code>Geometry2D</code> using the specified colors.
	 * @param g the geometries to draw
	 * @param c the colors to use for the drawing. The array has to contain at least one color! 
	 */
	public void fill(Geometry2D g, Color c){
		try { 
			fill(((Geometry2DAdapter) g).toShape(java2DConverter),c );
		} catch (NoninvertibleTransformException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param s
	 * @param c
	 */
	public void transformAndFill(Shape s, Color c){
		fill(transformation.createTransformedShape(s), c);
	}

	/**
	 * @param s
	 * @param c
	 */
	public void fill(Shape s, Color c){
		graphics.setColor(c);
		graphics.fill(s);
		if(repaintImmediately) repaint();		
	}
	
	/** saves the current output image on top of the image stack */
	public void push(){
		if(content!= null)					
			imageStack.push(content.getData());		
	}
	
	/** replaces the current output image by the top of the 
	 *  image stack without removing the top of the stack
	 */
	public void peek(){
		try{
			content.setData(imageStack.peek());
		} catch(EmptyStackException e){
			clear();
		}		
		repaint();
	}
	
	/** replaces the current output image by the top of the 
	 *  image stack and removes the top of the stack
	 */
	public void pop(){
		try{
			content.setData(imageStack.pop());
		} catch(EmptyStackException e){
			clear();
		}					
	}
	
	/** simply removes the top of the image stack 
	 *  without replacing the current output image
	 */
	public void remove(){
		try{
			imageStack.pop();
		} catch(EmptyStackException e){
			clear();
		}
	}
	
	/** saves the current output image to the specified file
	 * @param fileName the file to write the image into (extension should be <tt>png</tt> or <tt>jpg</tt>)
	 * @throws IOException 
	 */
	public void save(String fileName) throws IOException{
    	FileOutputStream fos = new FileOutputStream(fileName);
    	ImageIO.write(content, fileName.substring(fileName.length()-3), fos);
    }
	
	@Override
	public void paint(Graphics g){
		super.paint(g);
		g.drawImage(content,10,40, this);		
	}
	
	/**
	 * 
	 */
	public void waitForUser() {
		System.out.println("Press ENTER to continue");
		try { while(System.in.read() != 13);
		} catch (IOException e) {}		
	}


	/** sets the refreshContinuesly field which indicates whether or not 
	 *  to refresh the output after every call to draw() 
	 * @param repaintImmediately the new value of the field
	 */
	public void setRepaintImmediately(boolean repaintImmediately) {
		this.repaintImmediately = repaintImmediately;
	}
}
