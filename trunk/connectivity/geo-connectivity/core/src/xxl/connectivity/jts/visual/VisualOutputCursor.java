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
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;

import xxl.connectivity.jts.Geometry2DAdapter;
import xxl.core.cursors.SecureDecoratorCursor;
import xxl.core.functions.AbstractFunction;
import xxl.core.functions.Function;
import xxl.core.spatial.KPE;
import xxl.core.spatial.points.Point;
import xxl.core.spatial.rectangles.Rectangle;

/** A <code>VisualOutputCursor</code> wraps any Iterator whose elements can
 *  be converted to {@link java.awt.Shape}- or {@link Geometry2DAdapter}-objects.
 *  <br>
 *  Calling the next object from the wrapped Iterator first draws this
 *  objects' geometric representation to a drawing area before returning
 *  it to the caller.  
 *
 * @param <T> the type of the underlying iterators' elements
 */
public class VisualOutputCursor<T> extends SecureDecoratorCursor<T>{	

	/** This function returns the geometric representation of
	 *  an {@link Point} object
	 */
	public static final Function<Point, Shape> pointToShape 
	  	= new AbstractFunction<Point, Shape>(){
			public Shape invoke(Point p){
				return new java.awt.geom.Line2D.Double(p.getValue(0),p.getValue(1), p.getValue(0),p.getValue(1));
			}
		};

	/** A function to return the geometric representation of
	 *  a {@link Rectangle} object
	 */
	public static final Function<Rectangle, Shape> rectangleToShape 
		= new AbstractFunction<Rectangle, Shape>(){
			public Shape invoke(Rectangle r){
				return new Rectangle2D.Double(
						r.getCorner(false).getValue(0), 
						r.getCorner(false).getValue(1),
						r.deltas()[0], 
						r.deltas()[1]
					);				
			}
		};
	
	/** This function returns the geometric representation of
	 *  a {@link KPE} data- object. It's assumed that {@link KPE#getData()}
	 *  returns an {@link Rectangle}.
	 */
	public static final Function<KPE, Shape> KPEdataToShape 
		= new AbstractFunction<KPE, Shape>(){
			public Shape invoke(KPE k){
				Rectangle r = (Rectangle) k.getData();
				return rectangleToShape.invoke(r);
			}
		};

		
	/** this is the drawing area the objects are drawn to */
	protected VisualOutput output;	
	
	/** the Function which returns the geometric representation of the iterators' elements */
	protected Function<T, Shape> toShape;
	
	/** the color which is used in drawing */
	protected Color color;
	
	/** indicates if the boundary or the interior of the shape should be drawn */
	protected boolean fill;
		
	/** Create a new instance of <code>VisualOutputCursor</code>. 
	 * 
	 * @param iterator the iterator to wrap
	 * @param toShape the function to return the iterators' elements' geometric representation
	 * @param output the drawing area
	 * @param color the color to use
	 * @param fill determines whether to draw the interior or the boundary of the shape
	 */
	public VisualOutputCursor(Iterator<T> iterator, final Function<T, Shape> toShape, final VisualOutput output, Color color, boolean fill) {
		super(iterator);
		this.toShape = new AbstractFunction<T, Shape>(){
							AffineTransform at = output.getTransformationOp();
							public Shape invoke(T t){
								return at.createTransformedShape(
											toShape.invoke(t)
									   );
							}
						};
		this.output = output;
		this.color = color;
		this.fill = fill;			
	}	
	
	/** Create a new instance of <code>VisualOutputCursor</code>. Calls the main-constructor and 
	 *  sets the fill-option to <code>false</code>.
	 *  
	 * @param iterator the iterator to wrap
	 * @param toShape the function to return the iterators' elements' geometric representation
	 * @param output the drawing area
	 * @param color the color to use
	 */
	public VisualOutputCursor(Iterator<T> iterator, final Function<T, Shape> toShape, final VisualOutput output, Color color) {
		this(iterator, toShape, output, color, false);			
	}	
		
	@Override
	public T next() {
		T nextObject = super.next();
		if(fill) output.fill(toShape.invoke(nextObject), color);
			else output.draw(toShape.invoke(nextObject), color);		
		return nextObject;
	}	
}
