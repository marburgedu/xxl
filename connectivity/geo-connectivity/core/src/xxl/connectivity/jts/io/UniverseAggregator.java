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
package xxl.connectivity.jts.io;

import java.util.Iterator;

import xxl.core.cursors.mappers.Aggregator;
import xxl.core.math.functions.AggregationFunction;
import xxl.core.spatial.geometries.Geometry2D;
import xxl.core.spatial.rectangles.Rectangle;

/** This Aggregator incrementally computes the minimum bounding rectangle
 *  for an iterator of {@link Geometry2D Geometry} objects.
 *
 * @see xxl.core.cursors.mappers.Aggregator
 */
public class UniverseAggregator extends Aggregator<Geometry2D, Rectangle>{
	
	/** The aggregate function that computes the actual MBR.
	*/
	public static class UniverseFunction extends AggregationFunction<Geometry2D, Rectangle> {

		public Rectangle invoke(Rectangle aggregate, Geometry2D next){

			if(aggregate == null) aggregate = next.getMBR();
				else aggregate.union(next.getMBR());
			
			return aggregate;
		}
	}

	/** Creates a new UniverseAggregator.
	 *
	 * @param iterator input iterator containing geometries 
	 */
	public UniverseAggregator(Iterator<Geometry2D> iterator){
		super( iterator, new UniverseFunction());
	}
}
