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

import xxl.core.spatial.geometries.MultiLineString2D;

import com.vividsolutions.jts.geom.MultiLineString;

/** Implementation of the {@link MultiLineString2D} interface based on JTS {@link MultiLineString}s.
 */
public class MultiLineString2DAdapter extends GeometryCollection2DAdapter<LineString2DAdapter> implements MultiLineString2D<LineString2DAdapter>{

	/** Sole constructor: wraps the given {@link MultiLineString} inside this object 
	 * @param multiLineString The JTS-MultiLineString to wrap 
	 */
	public MultiLineString2DAdapter(MultiLineString multiLineString){
		super(multiLineString);
 	}	
	
	/** @inheritDoc
	 *  @see MultiLineString#isClosed()
	 */
	public boolean isClosed() { 
		return ((MultiLineString)geometry).isClosed();
	}

	/** @inheritDoc
	 */
	public double getLength() {
		return ((MultiLineString)geometry).getLength();
	}
	
	/** Returns the encapsulated Geometry in its declared type.
	 * @return the underlying JTS- Geometry	
	 */
	public MultiLineString getJTSGeometry(){
		return (MultiLineString) super.getJTSGeometry();
	}

}
