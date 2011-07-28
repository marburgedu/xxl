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
package xxl.connectivity.jts;

import xxl.core.spatial.geometries.Point2D;

import com.vividsolutions.jts.geom.Point;


/** Implementation of the {@link Point2D} interface based on JTS {@link Point}s.  
 */
public class Point2DAdapter extends Geometry2DAdapter implements Point2D{

	/** Sole constructor: wraps the given {@link Point} inside this object 
	 * @param point The JTS-Point to wrap
	 */
	public Point2DAdapter(Point point){
		super(point);	
	}	

	/** @inheritDoc
	 *  @see Point#getX()
	 */
	public double getX(){ 
		return ((Point) geometry).getX(); 
	}
	
	/** @inheritDoc
	 *  @see Point#getY()
	 */
	public double getY(){ 
		return ((Point) geometry).getY(); 
	}

	/** Returns the encapsulated Geometry in its declared type.
	 * @return the underlying JTS- Geometry	
	 */
	public Point getJTSGeometry(){
		return (Point) super.getJTSGeometry();
	}
}
