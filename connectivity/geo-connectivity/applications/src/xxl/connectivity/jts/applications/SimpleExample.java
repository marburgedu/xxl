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

package xxl.connectivity.jts.applications;

import java.awt.Color;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import xxl.connectivity.jts.*;
import xxl.connectivity.jts.visual.VisualOutput;
import xxl.core.indexStructures.Descriptor;
import xxl.core.spatial.points.Point;
import xxl.core.spatial.rectangles.DoublePointRectangle;
import xxl.core.spatial.rectangles.Rectangle;

public class SimpleExample {

	public static void main(String[] args) {
		Point2DAdapter p = Geometry2DFactory.createPoint(2, 2);
		System.out.println(p);
		
		MultiPolygon2DAdapter mp = Geometry2DFactory.createMultiPolygon2D(
			new Polygon2DAdapter[] {
				Geometry2DFactory.createPolygon2D(
					new Point2DAdapter[] {
						Geometry2DFactory.createPoint(1, 1),
						Geometry2DFactory.createPoint(1, 3),
						Geometry2DFactory.createPoint(4, 1),
						Geometry2DFactory.createPoint(1, 1)
					},
					new Point2DAdapter[][] {
						new Point2DAdapter[] {
							Geometry2DFactory.createPoint(2, 2),
							Geometry2DFactory.createPoint(3, 2),
							Geometry2DFactory.createPoint(2, 3),
							Geometry2DFactory.createPoint(2, 2)
						},
						new Point2DAdapter[] {
								Geometry2DFactory.createPoint(1, 1),
								Geometry2DFactory.createPoint(2, 1),
								Geometry2DFactory.createPoint(1, 2),
								Geometry2DFactory.createPoint(1, 1)	
						}
					}
				)
			}
		);
		
		System.out.println(mp);
		
		VisualOutput vo = new VisualOutput("test", new DoublePointRectangle(new double[] { 0,0}, new double[] { 5,5}), 400);
		vo.draw(mp, Color.RED);
		vo.draw(p, Color.GREEN);
	}
}
