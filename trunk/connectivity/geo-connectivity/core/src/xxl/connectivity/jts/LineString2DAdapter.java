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

import xxl.core.spatial.geometries.LineString2D;
import xxl.core.spatial.geometries.Point2D;

import com.vividsolutions.jts.geom.LineString;

/** Implementation of the {@link LineString2D} interface based on JTS {@link LineString}s.
 */
public class LineString2DAdapter extends Geometry2DAdapter implements LineString2D {

	/** Sole constructor: wraps the given {@link LineString} inside this object
	 * @param lineString the JTS-LineString to wrap
	 */	
	public LineString2DAdapter(LineString lineString){
		super(lineString);
 	}	

	/** @inheritDoc
	 * @see LineString#getPointN(int)
	 */
	public Point2D getPoint(int n) {
		return new Point2DAdapter (((LineString) geometry).getPointN(n));
	}

	/** @inheritDoc
	 * @see LineString#getStartPoint()
	 */
	public Point2D getStartPoint() {
		return new Point2DAdapter (((LineString) geometry).getStartPoint());
	}

	/** @inheritDoc
	 * @see LineString#getEndPoint()
	 */
	public Point2D getEndPoint() {
		return new Point2DAdapter (((LineString) geometry).getEndPoint());
	}

	/** @inheritDoc
	 * @see LineString#isClosed()
	 */
	public boolean isClosed() {
		return ((LineString) geometry).isClosed();
	}

	/** @inheritDoc
	 * @see LineString#isRing()
	 */
	public boolean isRing() {
		return ((LineString) geometry).isRing();
	}

	/** Returns the encapsulated Geometry in its declared type.
	 * @return the underlying JTS- Geometry	
	 */
	public LineString getJTSGeometry(){
		return (LineString) super.getJTSGeometry();
	}

}
