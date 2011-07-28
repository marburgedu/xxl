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

import xxl.core.spatial.geometries.GeometryCollection2D;

import com.vividsolutions.jts.geom.GeometryCollection;


/** Implementation of the {@link GeometryCollection2D} interface based on JTS {@link GeometryCollection}s.
 * @param <T> the type of the elements of this collection
 */
public class GeometryCollection2DAdapter<T extends Geometry2DAdapter> extends Geometry2DAdapter implements GeometryCollection2D<T> {
	
	/** Sole constructor: wraps the given {@link GeometryCollection} inside this object 
	 *  @param geometryCollection the JTS-GeometryCollection to wrap 
	 */	
	public GeometryCollection2DAdapter(GeometryCollection geometryCollection){
       	super(geometryCollection);		
 	}
	
	/** @inheritDoc
	 *  @see GeometryCollection#getNumGeometries()
	 */
	public int getNumGeometries() {
		return ((GeometryCollection) geometry).getNumGeometries();
	}
	
	/** @inheritDoc
	 *  @see GeometryCollection#getGeometryN(int)
	 */
	public T getGeometry(int n) {
		return (T) Geometry2DFactory.wrap( ((GeometryCollection) geometry).getGeometryN(n));
	}
	
	/** Returns the encapsulated Geometry in its declared type.
	 * @return the underlying JTS- Geometry	
	 */
	public GeometryCollection getJTSGeometry(){
		return (GeometryCollection) super.getJTSGeometry();
	}

}
