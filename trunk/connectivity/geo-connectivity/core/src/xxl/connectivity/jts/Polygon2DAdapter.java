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

import xxl.core.spatial.geometries.LinearRing2D;
import xxl.core.spatial.geometries.Polygon2D;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

/** Implementation of the {@link Polygon2D} interface based on JTS {@link Polygon}s.
 *  This class is only instantiable from {@link Geometry2DFactory#wrap(Geometry)} or {@link xxl.connectivity.jts.Geometry2DFactory}.  
 */
public class Polygon2DAdapter extends Geometry2DAdapter implements Polygon2D {

	/** Sole constructor: will only be called from {@link Geometry2DFactory#wrap(Geometry)} 
	 * @param polygon The JTS-Polygon to wrap
	 */
	public Polygon2DAdapter(com.vividsolutions.jts.geom.Polygon polygon){
		super(polygon);		
 	}

	/** @inheritDoc
	 *  @see Polygon#getExteriorRing()
	 */
	public LinearRing2D getExteriorRing() {
		return new LinearRing2DAdapter( (LinearRing) ((Polygon)geometry).getExteriorRing());
	}

	/** @inheritDoc
	 *  @see Polygon#getNumInteriorRing()
	 */	
	public int getNumInteriorRing() {
		return ((Polygon)geometry).getNumInteriorRing();
	}

	/** @inheritDoc
	 *  @see Polygon#getInteriorRingN(int)
	 */
	public LinearRing2D getInteriorRing(int n) {
		return new LinearRing2DAdapter( (LinearRing) ((Polygon)geometry).getInteriorRingN(n));
	}	
	
	/** Returns the encapsulated Geometry in its declared type.
	 * @return the underlying JTS- Geometry	
	 */
	public Polygon getJTSGeometry(){
		return (Polygon) super.getJTSGeometry();
	}

}
