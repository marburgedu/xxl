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

import xxl.core.spatial.geometries.Line2D;

import com.vividsolutions.jts.geom.LineString;

/** Implementation of the {@link Line2D} interface based on JTS {@link LineString}s.
 *  It does not provide additional functionality to <code>LineString2DAdapter</code> but is implemented to
 *  represent the complete OGC geometry model  
 */
public class Line2DAdapter extends LineString2DAdapter implements Line2D{

	/** Sole constructor: wraps the given {@link LineString} inside this object 
	 * @param lineString the JTS-LineString to wrap
	 */
	public Line2DAdapter(LineString lineString) {
		super(lineString); 
		if(geometry.getNumPoints()> 2) 
			throw new IllegalArgumentException("LineString "+lineString+" is not a Line!");
	}		
}
