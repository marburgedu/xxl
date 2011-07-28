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
import java.awt.geom.NoninvertibleTransformException;
import java.util.Iterator;

import xxl.connectivity.jts.Geometry2DAdapter;
import xxl.core.functions.AbstractFunction;
import xxl.core.util.WrappingRuntimeException;

/** A Geometry-Cursor which draws the Shape-representations of its underlying elements 
 *  to a given Output-Panel.
 * @param <G> the type of the cursors elements
 *   
 * @see VisualOutputCursor
 * @see VisualOutput 
 */
public class VisualGeometry2DCursor<G extends Geometry2DAdapter> extends VisualOutputCursor<G> {
	
	/** Create a new instance of <code>VisualOutputCursor</code>.
	 * @param iterator the iterator to wrap
	 * @param output the drawing area to draw the Geometries to
	 * @param color the color to use
	 */	
	public VisualGeometry2DCursor(Iterator<G> iterator, final VisualOutput output, Color color){
		super(iterator, new AbstractFunction<G,Shape>(){}, output, color, false);
		toShape = new AbstractFunction<G, Shape>(){
			public Shape invoke(G g){
				try {
					return (g).toShape( output.getJava2DConverter());
				} catch (NoninvertibleTransformException e) {
					throw new WrappingRuntimeException(e);
				}
			};
		};
	}
}
