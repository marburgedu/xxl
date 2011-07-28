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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.operation.distance.DistanceOp;

/**
 * Computes the minimum and maximum distance and closest and furthest points 
 * between two {@link com.vividsolutions.jts.geom.Geometry Geometry}s using the euclidian metric.
 * It is a direct extension of the class {@link com.vividsolutions.jts.operation.distance.DistanceOp DistanceOp}
 * of the Java Topology Suite.
 */
public class ExtendedDistanceOp extends DistanceOp{

  /**
   * Computes the distance between the closest points of two geometries.
   * @param g0 a {@link com.vividsolutions.jts.geom.Geometry Geometry}
   * @param g1 another {@link com.vividsolutions.jts.geom.Geometry Geometry} 
   * @return the distance between the geometries
   */
  public static double minDistance(Geometry2DAdapter g0, Geometry2DAdapter g1){
    return new DistanceOp(g0.getJTSGeometry(), g1.getJTSGeometry()).distance();   
  }

  /**
   * Computes the distance between the most distant points of two geometries.
   * @param g0 a {@link com.vividsolutions.jts.geom.Geometry Geometry}
   * @param g1 another {@link com.vividsolutions.jts.geom.Geometry Geometry}
   * @return the distance between the geometries
   */
  public static double maxDistance(Geometry2DAdapter g0, Geometry2DAdapter g1)  {
    return new ExtendedDistanceOp(g0.getJTSGeometry(), g1.getJTSGeometry()).maxDistance();    
  }

  /**
   * Computes the the closest points of two geometries.
   * The points are presented in the same order as the input Geometries.
   *
   * @param g0 a {@link   com.vividsolutions.jts.geom.Geometry Geometry}
   * @param g1 another {@link   com.vividsolutions.jts.geom.Geometry Geometry}
   * @return the closest points in the geometries
   */
  public static Point2DAdapter[] closestPoints(Geometry2DAdapter g0, Geometry2DAdapter g1)
  {
    ExtendedDistanceOp distOp = new ExtendedDistanceOp(g0.getJTSGeometry(), g1.getJTSGeometry());
    Coordinate[] coords = distOp.closestPoints();
    Point2DAdapter[] points = new Point2DAdapter[coords.length];
    for(int i=0; i< points.length;i++)
    	points[i] = Geometry2DFactory.createPoint(coords[i].x, coords[i].y, g0.getPrecisionModel());
    return points;
  }

  /**
   * Computes the the most distant points of two geometries.
   * The points are presented in the same order as the input Geometries.
   *
   * @param g0 a {@link   com.vividsolutions.jts.geom.Geometry Geometry}
   * @param g1 another {@link   com.vividsolutions.jts.geom.Geometry Geometry}
   * @return the closest points in the geometries
   */
  public static Point2DAdapter[] furthestPoints(Geometry2DAdapter g0, Geometry2DAdapter g1)
  {
    ExtendedDistanceOp distOp = new ExtendedDistanceOp(g0.getJTSGeometry(), g1.getJTSGeometry());
    Coordinate[] coords = distOp.furthestPoints();
    Point2DAdapter[] points = new Point2DAdapter[coords.length];
    for(int i=0; i< points.length;i++)
    	points[i] = Geometry2DFactory.createPoint(coords[i].x, coords[i].y, g0.getPrecisionModel());
    return points;
  }

  private Geometry[] geom;  
  private Coordinate[] maxDistanceLocation;

  /**
   * Constructs a DistanceOp that computes the distance and closest points between
   * the two specified geometries.
   */
  protected ExtendedDistanceOp(Geometry g0, Geometry g1)
  {    
	super(g0,g1);
    this.geom = new Geometry[]{g0, g1};
  }

  /**
   * Report the distance between the most distant points on the input geometries.
   *
   * @return the distance between the geometries
   */
  protected double maxDistance(){
      double max= Double.MIN_VALUE;
      Coordinate[] c0 = geom[0].getCoordinates();
      Coordinate[] c1 = geom[1].getCoordinates();      
      maxDistanceLocation = new Coordinate[2];
      
      for(int i=0; i< c0.length;i++){
          for(int j=0; j< c1.length;j++){
              double dist = pointToPointDistance(c0[i],c1[j]);
              if(max < dist){
                  max = dist;
                  maxDistanceLocation[0] = c0[i];
                  maxDistanceLocation[1] = c1[j];
              }
          }
      }
      return max;
  }
  
  /**
   * Report the coordinates of the most distant points in the input geometries.
   * The points are presented in the same order as the input Geometries.
   *
   * @return a pair of {@link Coordinate}s of the closest points
   */
  protected Coordinate[] furthestPoints()
  {
    maxDistance();
    
    return new Coordinate[] {
          maxDistanceLocation[0],
          maxDistanceLocation[1]};    
  }
  
  /** Compute the euclidian Distance beetween two Coordinates
   * 
   * @param c0 the first Coordinate
   * @param c1 the second Coordinate
   * @return the euclidian Distance beetween the given Coordinates
   */
  protected double pointToPointDistance(Coordinate c0, Coordinate c1){
	  double dx = c1.x - c0.x;
	  double dy = c1.y - c0.y;
	  return Math.sqrt( dx*dx + dy*dy);
  }
}
