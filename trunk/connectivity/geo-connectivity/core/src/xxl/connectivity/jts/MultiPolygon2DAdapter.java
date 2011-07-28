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

import xxl.core.spatial.geometries.MultiPolygon2D;

import com.vividsolutions.jts.geom.MultiPolygon;

/** Implementation of the {@link MultiPolygon2D} interface based on JTS {@link MultiPolygon}s.
 *  It does not provide additional functionality to <code>GeometryCollection2DAdapter</code> but is implemented to
 *  represent the complete OGC geometry model  
 */
public class MultiPolygon2DAdapter extends GeometryCollection2DAdapter<Polygon2DAdapter> implements MultiPolygon2D<Polygon2DAdapter>{

	/** Sole constructor: wraps the given {@link MultiPolygon} inside this object 
	 * @param multiPolygon The JTS-MultiPolygon to wrap
	 */
	public MultiPolygon2DAdapter(MultiPolygon multiPolygon){
		super(multiPolygon);
 	}
	
	/** Returns the encapsulated Geometry in its declared type.
	 * @return the underlying JTS- Geometry	
	 */
	public MultiPolygon getJTSGeometry(){
		return (MultiPolygon) super.getJTSGeometry();
	}

}
