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

import java.util.Collection;

import xxl.core.spatial.geometries.Geometry2DException;
import xxl.core.spatial.points.DoublePoint;
import xxl.core.spatial.points.FloatPoint;
import xxl.core.spatial.points.Point;
import xxl.core.spatial.rectangles.DoublePointRectangle;
import xxl.core.spatial.rectangles.FloatPointRectangle;
import xxl.core.spatial.rectangles.Rectangle;
import xxl.core.util.WrappingRuntimeException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.util.GeometricShapeFactory;

/** This class contains some static-methods for the creation of new <code>Geometry2D</code>-objects
 *  and the conversion between different types of geometrical representations available in XXL.
 *    
 *  @see xxl.core.spatial.rectangles
 *  @see xxl.core.spatial.points
 *  @see xxl.core.spatial.geometries
 *  @see xxl.connectivity.jts
 */
public class Geometry2DFactory {
	
	/** There's no need to instantiate this class */
    private Geometry2DFactory(){}   

    
    /** A static reference to the JTS-Double-Precision-Model */
    public static PrecisionModel DOUBLE_PRECISIONMODEL 	= new PrecisionModel(PrecisionModel.FLOATING);
    
    /** A static reference to the JTS-Float-Precision-Model */
    public static PrecisionModel FLOAT_PRECISIONMODEL 	= new PrecisionModel(PrecisionModel.FLOATING_SINGLE);

   /* some private static references used inside this class */
    private static GeometryFactory DOUBLE_GEOMETRYFACTORY 	= new GeometryFactory(DOUBLE_PRECISIONMODEL);
    private static GeometryFactory FLOAT_GEOMETRYFACTORY 	= new GeometryFactory(FLOAT_PRECISIONMODEL);
        
	private static WKBReader DOUBLE_WKB_READER 	= new WKBReader(DOUBLE_GEOMETRYFACTORY);
    private static WKBReader FLOAT_WKB_READER 	= new WKBReader(FLOAT_GEOMETRYFACTORY);

	private static WKTReader DOUBLE_WKT_READER = new WKTReader(DOUBLE_GEOMETRYFACTORY);
    private static WKTReader FLOAT_WKT_READER = new WKTReader(FLOAT_GEOMETRYFACTORY);
   
    /* some private help-methods used inside this class */
	private static Coordinate pointToCoordinate(xxl.core.spatial.points.Point point){
		if(point.dimensions()<2 || point.dimensions() > 3)
			throw new Geometry2DException("Cannot converert point:"+point);		
		return new Coordinate(point.getValue(0), point.getValue(1), point.dimensions()>2 ? point.getValue(2) : Double.NaN);		
	}
	
	private static boolean isFloat(PrecisionModel p){
		return p.isFloating() && p.getMaximumSignificantDigits()==6;
	}

	private static boolean isDouble(PrecisionModel p){
		return p.isFloating() && p.getMaximumSignificantDigits()==16;
	}

    
	private static GeometryFactory getGeometryFactory(PrecisionModel p){
		return isDouble(p) 	? DOUBLE_GEOMETRYFACTORY
		   		: isFloat(p) ? FLOAT_GEOMETRYFACTORY
		   					 : new GeometryFactory(p);		
	}
	
	
    
    /** This method wraps the given JTS-<code>Geometry</code> in the appropriate
	 *  <code>Geometry2DAdapter</code> subclass. 
	 * @param geometry the <code>Geometry</code> to wrap or <code>null</code>
	 * @return an object of the appropriate <code>Geometry2DAdapter</code> subclass 
	 * 			or the empty Geometry if <code>geometry == null</code>
	 */
	public static Geometry2DAdapter wrap(Geometry geometry){
		
		if(geometry == null)
			throw new Geometry2DException("Null value not allowed!");							
		
		final String geometryType = geometry.getGeometryType();
		if(geometryType.equals("Point")) return new Point2DAdapter((com.vividsolutions.jts.geom.Point)geometry);
		
		if(geometryType.equals("MultiPoint")) return new MultiPoint2DAdapter((MultiPoint)geometry);
		
		if(geometryType.equals("LineString")) 
			return (geometry.getNumPoints()>2 ? new LineString2DAdapter((LineString)geometry) : new Line2DAdapter((LineString) geometry));
		
		if(geometryType.equals("LinearRing")) return new LineString2DAdapter((LinearRing)geometry);
		
		if(geometryType.equals("MultiLineString")) return new MultiLineString2DAdapter((MultiLineString)geometry);
	
		if(geometryType.equals("Polygon")) return new Polygon2DAdapter((Polygon)geometry);
		
		if(geometryType.equals("MultiPolygon")) return new MultiPolygon2DAdapter((MultiPolygon)geometry);    	
		
		if(geometryType.equals("GeometryCollection")) return new GeometryCollection2DAdapter<Geometry2DAdapter>((GeometryCollection)geometry);
		
		throw new IllegalArgumentException(); // should never reach here
	}
	
	
	
	/** Reads a JTS-<code>Geometry</code> from its Well- Known Binary- Representation and wraps
	 *  it in the appropriate <code>Geometry2DAdapter</code> subclass. 
	 * @param wkb the Well Known Binary Representation of the <code>Geometry</code>
	 * @return an object of the appropriate <code>Geometry2DAdapter</code> subclass
	 */
	public static Geometry2DAdapter createFromWKB(byte[] wkb) {
		return createFromWKB(wkb, DOUBLE_PRECISIONMODEL);
	}	

	
	/** Reads a JTS-<code>Geometry</code> from its Well- Known Binary- Representation and wraps
	 *  it in the appropriate <code>Geometry2DAdapter</code> subclass. 
	 * @param wkb the Well Known Binary Representation of the <code>Geometry</code>
	 * @param p the <code>PrecisionModel</code> of the returned <code>Geometry2DAdapter</code>
	 * @return an object of the appropriate <code>Geometry2DAdapter</code> subclass
	 */
	public static Geometry2DAdapter createFromWKB(byte[] wkb, PrecisionModel p) {
		try {WKBReader reader = 
				isDouble(p) ? DOUBLE_WKB_READER
							: isFloat(p) ? FLOAT_WKB_READER
										 : new WKBReader( new GeometryFactory(p) );
			return Geometry2DFactory.wrap( reader.read(wkb) );
		} catch ( Exception e) {
			throw new WrappingRuntimeException(e);
		}	
	}
		

	/** Reads a JTS-<code>Geometry</code> from its Well- Known Text- Representation and wraps
	 *  it in the appropriate <code>Geometry2DAdapter</code> subclass. 
	 * @param wkt the Well Known Binary Representation of the <code>Geometry</code>
	 * @return an object of the appropriate <code>Geometry2DAdapter</code> subclass
	 */
	public static Geometry2DAdapter createFromWKT(String wkt) {
		return createFromWKT(wkt, DOUBLE_PRECISIONMODEL);
	}

	
	/** Reads a JTS-<code>Geometry</code> from its Well- Known Text- Representation and wraps
	 *  it in the appropriate <code>Geometry2DAdapter</code> subclass. 
	 * @param wkt the Well Known Binary Representation of the <code>Geometry</code>
	 * @param p the <code>PrecisionModel</code> of the returned <code>Geometry2DAdapter</code>
	 * @return an object of the appropriate <code>Geometry2DAdapter</code> subclass
	 */
	public static Geometry2DAdapter createFromWKT(String wkt, PrecisionModel p) {
		try { WKTReader reader = 
				isDouble(p) ? DOUBLE_WKT_READER
							: isFloat(p) ? FLOAT_WKT_READER
										 : new WKTReader( getGeometryFactory(p));
			return Geometry2DFactory.wrap ( reader.read(wkt) );
		} catch ( Exception e) {
			throw new WrappingRuntimeException(e);
		}	
	}
		
	
	/** Converts the given JTS- Envelope- object to the corresponding {@link DoublePointRectangle}
	 * @param e the <code>Envelope</code>-object to convert
	 * @return the corresponding XXL-<code>DoublePointRectangle</code>
	 */
	public static Rectangle envelopeToXXLRectangle(Envelope e){
		return envelopeToXXLRectangle(e, true);
	}
	
	
	/** Converts the given JTS- Envelope- object to the corresponding XXl-<code>Rectangle</code>
	 *  The precise result-type can be specified (either {@link DoublePointRectangle} or {@link FloatPointRectangle})  
	 * @param e the <code>Envelope</code>-object to convert
	 * @param returnDoublePointRectangle determines whether a  {@link DoublePointRectangle} or a {@link FloatPointRectangle} is returned
	 * @return the corresponding XXL-<code>Rectangle</code>
	 */
	public static Rectangle envelopeToXXLRectangle(Envelope e, boolean returnDoublePointRectangle){
	    if( returnDoublePointRectangle )
		    return new DoublePointRectangle( new double[]{e.getMinX(), e.getMinY()}, 
		    								 new double[]{e.getMaxX(), e.getMaxY()}
		    								); 
	    return new FloatPointRectangle( new float[]{(float) e.getMinX(), (float) e.getMinY()}, 
	                                    new float[]{(float) e.getMaxX(), (float) e.getMaxY()}
	        						  );                                                    		
	}

	
	/** Converts the given JTS- Envelope- object to the corresponding Geometry2D-class ({@link Polygon2DAdapter}).
	 * @param e the <code>Envelope</code>-object to convert
	 * @return the corresponding {@link Polygon2DAdapter}-object
	 */
	public static Polygon2DAdapter envelopeToPolygon(Envelope e){
		return createRectangle(e.centre(),e.getWidth(),e.getHeight(), DOUBLE_PRECISIONMODEL);
	}

	
	/** Converts the given JTS- Envelope- object to the corresponding Geometry2D-class ({@link Polygon2DAdapter}).
	 * @param e the <code>Envelope</code>-object to convert
	 * @param precision the <code>PrecisionModel</code> of the returned <code>Polygon2DAdapter</code>
	 * @return the corresponding {@link Polygon2DAdapter}-object
	 */
	public static Polygon2DAdapter envelopeToPolygon(Envelope e, PrecisionModel precision){
		return createRectangle(e.centre(),e.getWidth(),e.getHeight(), precision);
	}

	/** Converts the given XXL-Rectangle to the corresponding <tt>Polygon2DAdapter</tt>-object
	 * @param rectangle the rectangle to convert 
     * @return the <tt>Polygon2DAdapter</tt>-object corresponding to the input-rectangle
     */
    public static Polygon2DAdapter xxlRectangleToPolygon2D(Rectangle rectangle){    	
        return createRectangle( xxlPointToPoint2D(rectangle.getCorner(false)), xxlPointToPoint2D( rectangle.getCorner(true)) );        
    }

    
	/** Converts the given XXL-Rectangle to the corresponding <tt>Polygon2DAdapter</tt>-object
	 * @param rectangle the rectangle to convert 
 	 * @param precision the <code>PrecisionModel</code> of the returned <code>Polygon2DAdapter</code>
 	 * @return the <tt>Polygon2DAdapter</tt>-object corresponding to the input-rectangle
     */
    public static Polygon2DAdapter xxlRectangleToPolygon2D(Rectangle rectangle, PrecisionModel precision){    	
        return createRectangle( xxlPointToPoint2D(rectangle.getCorner(false)), xxlPointToPoint2D(rectangle.getCorner(true)), precision);        
    }
    
    
	/** Converts the given XXL-Point to the corresponding <tt>Point2DAdapter</tt>-object
	 * @param point the XXL-Point to convert 
 	 * @return the <tt>Point2DAdapter</tt>-object corresponding to the input-point
     */
    public static Point2DAdapter xxlPointToPoint2D(Point point){
    	return xxlPointToPoint2D(point, DOUBLE_PRECISIONMODEL);
    }        

    
	/** Converts the given XXL-Point to the corresponding <tt>Point2DAdapter</tt>-object
	 * @param point the XXL-Point to convert 
 	 * @param precision the <code>PrecisionModel</code> of the returned <code>Point2DAdapter</code>
 	 * @return the <tt>Point2DAdapter</tt>-object corresponding to the input-point
     */
    public static Point2DAdapter xxlPointToPoint2D(Point point, PrecisionModel precision){           	        
        return createPoint( pointToCoordinate(point), precision);        
    }        
    
    
	/** Converts the given <code>Point2DAdapter</code>-object to the corresponding {@link FloatPoint}
	 * @param point the <code>Point2DAdapter</code> to convert 
 	 * @return the <tt>FloatPoint</tt>-object corresponding to the input-<code>Point2DAdapter</code>
     */
    public static Point point2DToXXLPoint(Point2DAdapter point){
    	return point2DToXXLPoint(point, true);
    }
    
    
	/** Converts the given <code>Point2DAdapter</code>-object to the corresponding XXL-{@link Point}
	 *  The precise result-type can be specified (either {@link DoublePoint} or {@link FloatPoint})  
	 * @param point the <code>Point2DAdapter</code> to convert 
	 * @param returnDoublePoint determines whether a  {@link DoublePoint} or a {@link FloatPoint} is returned
 	 * @return the <tt>FloatPoint</tt>-object corresponding to the input-<code>Point2DAdapter</code>
     */
    public static Point point2DToXXLPoint(Point2DAdapter point, boolean returnDoublePoint){
    	Coordinate c = point.geometry.getCoordinate();
    	return returnDoublePoint 
    		? new DoublePoint( c.z== Double.NaN 
    							? new double[]{c.x, c.y} 
    							: new double[]{c.x, c.y, c.z}
    						 )
    		: new FloatPoint( c.z== Double.NaN 
    							? new float[]{(float) c.x, (float) c.y} 
    							: new float[]{(float) c.x, (float) c.y, (float) c.z});
    }
        

    /** A method to create a Point in 2D.
     * @param x the x-coordinate of the point created
     * @param y the y-coordinate of the point created
     * @return the new JTS-point wrapped in a <code>Point2DAdapter</code>
     */
    public static Point2DAdapter createPoint(double x, double y){
    	return createPoint(new Coordinate(x,y),DOUBLE_PRECISIONMODEL );
    }
    
    
    /** A method to create a Point in 3D.
     * @param x the x-coordinate of the point created
     * @param y the y-coordinate of the point created
     * @param z the z-coordinate of the point created
     * @return the new JTS-point wrapped in a <code>Point2DAdapter</code>
     */
    public static Point2DAdapter createPoint(double x, double y, double z){
    	return createPoint(new Coordinate(x,y,z),DOUBLE_PRECISIONMODEL );
    }

    
    /** A method to create a Point in 2D. The PrecisionModel of the new Point can be specified.
     * @param x the x-coordinate of the point created
     * @param y the y-coordinate of the point created
     * @param precision determines the PrecisionModel to use
     * @return the new JTS-point wrapped in a <code>Point2DAdapter</code>
     */
     public static Point2DAdapter createPoint(double x, double y, PrecisionModel precision){
    	return createPoint(new Coordinate(x,y), precision);
    }
        
    
    /** A method to create a Point in 3D. The PrecisionModel of the new Point can be specified.
     * @param x the x-coordinate of the point created
     * @param y the y-coordinate of the point created
     * @param z the z-coordinate of the point created
     * @param precision the PrecisionModel to use
     * @return the new JTS-point wrapped in a <code>Point2DAdapter</code>
     */
    public static Point2DAdapter createPoint(double x, double y, double z, PrecisionModel precision){
    	return createPoint(new Coordinate(x,y,z), precision);
    }

    
    /** Creates a Point from a JTS-{@link Coordinate}.
     * @param c the Coordinate to create the <code>Point2DAdapter</code>-object from 
     * @param precision the <code>PrecisionModel</code> of the new <code>Point2DAdapter</code>
     * @return the new JTS-point wrapped in a <code>Point2DAdapter</code>
     */
    public static Point2DAdapter createPoint(Coordinate c, PrecisionModel precision){
		GeometryFactory gf =  getGeometryFactory(precision);	
    	return new Point2DAdapter(gf.createPoint(c));
	}
    


    /** Creates a rectangle using the given lower left und upper right corners.
     *  By default the PrecisionModel used is set to Double-Precision.
     *  
     * @param left the lower left corner of the rectangle
     * @param right the upper right corner of the rectangle
     * @return a double-precision rectangle specified by the given corners  
     */
    public static Polygon2DAdapter createRectangle(Point2DAdapter left, Point2DAdapter right){
    	
    	GeometricShapeFactory gsf = new GeometricShapeFactory( DOUBLE_GEOMETRYFACTORY );
        
        gsf.setBase(left.geometry.getCoordinate());        
        gsf.setWidth( right.getX() - left.getX());
        gsf.setHeight( right.getY() - left.getY());
        gsf.setNumPoints(4);
        return new Polygon2DAdapter(gsf.createRectangle());    
    }

    
    /** Creates a rectangle using the given lower left und upper right corners.
     *  The PrecisionModel can be specified.
     *  
     * @param left the lower left corner of the rectangle
     * @param right the upper right corner of the rectangle
     * @param precision the <code>PrecisionModel</code> of the returned <code>Polygon2DAdapter</code>
     * @return a double-precision rectangle specified by the given corners  
     */
    public static Polygon2DAdapter createRectangle(Point2DAdapter left, Point2DAdapter right, PrecisionModel precision){
    	
    	GeometricShapeFactory gsf = new GeometricShapeFactory( getGeometryFactory(precision) );
        
        gsf.setBase(left.geometry.getCoordinate());        
        gsf.setWidth( right.getX() - left.getX());
        gsf.setHeight( right.getY() - left.getY());
        gsf.setNumPoints(4);
        return new Polygon2DAdapter(gsf.createRectangle());    
    }
    
    
    /** Creates a rectangle using the given centre-point and the extensions in both dimensions.
     *  By default the PrecisionModel is set to double-precision.
     *  
     * @param centre the centre-point of the new rectangle
     * @param width the rectangle's extension on the x-axis
     * @param height the rectangle's extension on the y-axis
     * @return a double-precision rectangle specified by the given corners  
     */
    public static Polygon2DAdapter createRectangle(Point2DAdapter centre, double width, double height){
    	return createRectangle(centre.geometry.getCoordinate(), height, height, DOUBLE_PRECISIONMODEL);
    }

    
    /** Creates a rectangle using the given centre-point and the extensions in both dimensions.
     *  By default the PrecisionModel is set to double-precision.
     *  
     * @param centre the centre-point of the new rectangle
     * @param width the rectangle's extension on the x-axis
     * @param height the rectangle's extension on the y-axis
     * @param precision the <code>PrecisionModel</code> of the returned <code>Polygon2DAdapter</code>
     * @return a double-precision rectangle specified by the given corners  
     */
    public static Polygon2DAdapter createRectangle(Point2DAdapter centre, double width, double height, PrecisionModel precision){
    	return createRectangle(centre.geometry.getCoordinate(), height, height, precision);
    }

    
    private static Polygon2DAdapter createRectangle(Coordinate centre, double width, double height, PrecisionModel precision){
    	GeometricShapeFactory gsf = new GeometricShapeFactory( getGeometryFactory(precision) );
        
        gsf.setCentre(centre);        
        gsf.setWidth( width );
        gsf.setHeight( height );
        gsf.setNumPoints(4);
        return new Polygon2DAdapter(gsf.createRectangle());    
    }

    
    /** Creates an arc around the given centre-point using the specified radii. The arc is 
     *  linearly interpolated from the starting-angle <it>fromAngle</it> to the ending-angle 
     *  <it>toAngle</it> with the given number of base-points.
     *  The <code>PrecisionModel</code> is set to double-precision.
     * @param centre the centre of the arc
     * @param radiusX the radius in x-direction
     * @param radiusY the radius in y-direction
     * @param fromAngle the starting-angle of the arc
     * @param toAngle the ending-angle
     * @param numPoints the number of interpolation points
     * @return a <code>LineString2DAdapter</code> representing the arc
     */
    public static LineString2DAdapter createArc(Point2DAdapter centre, double radiusX, double radiusY, double fromAngle, double toAngle, int numPoints){
    	return createArc(centre, radiusX, radiusY, fromAngle, toAngle, numPoints, DOUBLE_PRECISIONMODEL);
    }
        
    
    /** Creates an arc around the given centre-point using the specified radii. The arc is 
     *  linearly interpolated from the starting-angle <it>fromAngle</it> to the ending-angle 
     *  <it>toAngle</it> with the given number of base-points.
     *  The <code>PrecisionModel</code> can be specified.
     * @param centre the centre of the arc
     * @param radiusX the radius in x-direction
     * @param radiusY the radius in y-direction
     * @param fromAngle the starting-angle of the arc
     * @param toAngle the ending-angle
     * @param numPoints the number of interpolation points
     * @param precision the <code>PrecisionModel</code> to use 
     * @return a <code>LineString2DAdapter</code> representing the arc
     */
    public static LineString2DAdapter createArc(Point2DAdapter centre, double radiusX, double radiusY, double fromAngle, double toAngle, int numPoints, PrecisionModel precision){    	    	
        GeometricShapeFactory gsf = new GeometricShapeFactory( getGeometryFactory(precision) );
        
        gsf.setCentre( centre.geometry.getCoordinate() );        
        gsf.setWidth( radiusX * 2);
        gsf.setHeight( radiusY *2 );
        gsf.setNumPoints(numPoints);
        return new LineString2DAdapter(gsf.createArc(fromAngle, toAngle));        
    }         
    

    /** Creates an ellipse around the given centre-point using the specified radii. The ellipse is 
     *  linearly interpolated using the given number of base-points.
     *  The <code>PrecisionModel</code> can be specified.
     * @param centre the centre of the ellipse
     * @param radiusX the radius in x-direction
     * @param radiusY the radius in y-direction
     * @param numPoints the number of interpolation points
     * @return a <code>Polygon2DAdapter</code> representing the ellipse
     */
    public static Polygon2DAdapter createEllipse(Point2DAdapter centre, double radiusX, double radiusY, int numPoints){                
    	return createEllipse(centre, radiusX, radiusY, numPoints, DOUBLE_PRECISIONMODEL );
    }
    
    
    /** Creates an ellipse around the given centre-point using the specified radii. The ellipse is 
     *  linearly interpolated using the given number of base-points.
     *  The <code>PrecisionModel</code> can be specified.
     * @param centre the centre of the ellipse
     * @param radiusX the radius in x-direction
     * @param radiusY the radius in y-direction
     * @param numPoints the number of interpolation points
     * @param precision the <code>PrecisionModel</code> to use 
     * @return a <code>Polygon2DAdapter</code> representing the ellipse
     */
    public static Polygon2DAdapter createEllipse(Point2DAdapter centre, double radiusX, double radiusY, int numPoints, PrecisionModel precision){                
        GeometricShapeFactory gsf = new GeometricShapeFactory( getGeometryFactory( precision ) );        
        gsf.setBase(new Coordinate(centre.getX()-radiusX, centre.getY() - radiusY));
        gsf.setWidth(radiusX*2);
        gsf.setHeight(radiusY*2);
        gsf.setNumPoints(numPoints);
        
        return new Polygon2DAdapter(gsf.createCircle());        
    }
    
    
    /** Connects the given array of points to a line-string. The <code>PrecisionModel</code> is
     *  set to double-precision.
     * @param points the points to connect
     * @return a new <code>LineString2DAdapter</code> connecting the given points 
     */
    public static LineString2DAdapter createLineString2D(Point2DAdapter[] points){
    	return createLineString2D(points, DOUBLE_PRECISIONMODEL);
    }
    
    
    /** Connects the given array of points to a line-string. The <code>PrecisionModel</code> can
     *  be specified.
     * @param points the points to connect
     * @param precision the <code>PrecisionModel</code> to use
     * @return a new <code>LineString2DAdapter</code> connecting the given points 
     */
    public static LineString2DAdapter createLineString2D(Point2DAdapter[] points, PrecisionModel precision){
    	Coordinate[] c = new Coordinate[points.length];
    	for(int i=0; i< points.length; c[i] = points[i].geometry.getCoordinate(), i++);        
        GeometryFactory factory = getGeometryFactory( precision );        
        return new LineString2DAdapter( factory.createLineString(c) );
    }
    

    /** Connects the given array of points to a linear ring. The first and last point of the array are assumed 
     *  to be identical. The <code>PrecisionModel</code> is set to double-precision.
     * @param points the points to connect
     * @return a new <code>LinearRing2DAdapter</code> connecting the given points to a ring 
     */
    public static LinearRing2DAdapter createLinearRing2D(Point2DAdapter[] points){
    	return createLinearRing2D(points, DOUBLE_PRECISIONMODEL);
    }
    
    
    /** Connects the given array of points to a linear ring. The first and last point of the array are assumed 
     *  to be identical. The <code>PrecisionModel</code> can be specified.
     * @param points the points to connect
     * @param precision the <code>PrecisionModel</code> to use 
     * @return a new <code>LinearRing2DAdapter</code> connecting the given points to a ring 
     */
    public static LinearRing2DAdapter createLinearRing2D(Point2DAdapter[] points, PrecisionModel precision){
    	Coordinate[] c = new Coordinate[points.length];
    	for(int i=0; i< points.length; c[i] = points[i].geometry.getCoordinate(), i++);        
        GeometryFactory factory = getGeometryFactory( precision );        
        return new LinearRing2DAdapter( factory.createLinearRing(c) );
    }          
    
    
    /** Creates a polygon with shell and holes specified by the given point-sets.
     *  The <code>PrecisionModel</code> is set to double-precision.
     * @param shell the point-set defining the shell of the polygon
     * @param holes the point-sets defining holes in the polygon
     * @return a new <code>Polygon2DAdapter</code>
     */
    public static Polygon2DAdapter createPolygon2D(Point2DAdapter[] shell, Point2DAdapter[][] holes){
    	return createPolygon2D(shell, holes, DOUBLE_PRECISIONMODEL);
    }
    
    
    /** Creates a polygon with shell and holes specified by the given point-sets.
     *  The <code>PrecisionModel</code> can be specified.
     * @param shell the point-set defining the shell of the polygon
     * @param holes the point-sets defining holes in the polygon
     * @param precision the <code>PrecisionModel</code> to use
     * @return a new <code>Polygon2DAdapter</code>
     */
   	public static Polygon2DAdapter createPolygon2D(Point2DAdapter[] shell, Point2DAdapter[][] holes, PrecisionModel precision){
    	if(shell.length==0) 
    		throw new Geometry2DException("Cannot create polygon out of empty point- set");
    	
    	Coordinate[] sh = new Coordinate[shell.length];
    	for(int i=0; i< shell.length; sh[i] = shell[i].geometry.getCoordinate(), i++);        
        
    	GeometryFactory factory = getGeometryFactory( precision );        
        
    	LinearRing _shell = factory.createLinearRing(sh);
    	LinearRing[] _holes = new LinearRing[holes.length];
    	for(int i=0; i<holes.length;i++){
    		Coordinate[] ho = new Coordinate[shell.length];
        	for(int j=0; j< holes[i].length; ho[j] = holes[i][j].geometry.getCoordinate(), j++);                    
        	_holes[i] = factory.createLinearRing(ho);                    	
    	}
        return new Polygon2DAdapter( factory.createPolygon(_shell, _holes) );
    }           


	/** Wraps the given point-set in a <code>MultiPoint2DAdapter</code>-collection.
	 *  The <code>PrecisionModel</code> is determined from the points' models. 
	 * @param points the points to wrap in the collection
	 * @return a new <code>MultiPoint2DAdapter</code>-object
	 */
	public static MultiPoint2DAdapter createMultiPoint2D(Point2DAdapter[] points) {
   		MultiPoint m = null;			
		if(points== null || points.length==0)
			m = DOUBLE_GEOMETRYFACTORY.createMultiPoint((com.vividsolutions.jts.geom.Point[])null);
		else{
		 	Coordinate[] c = new Coordinate[points.length];
	    	for(int i=0; i< points.length; c[i] = points[i].geometry.getCoordinate(), i++);            	   	    	   
			m = points[0].geometry.getFactory().createMultiPoint(c);
		}		
	    return new MultiPoint2DAdapter( m );	
	}
	
	
	/** Wraps the given line-strings in a <code>MultiLineString2DAdapter</code>-collection.
	 *  The <code>PrecisionModel</code> is determined from the line-strings' models. 
	 * @param lineStrings the line-strings to wrap in the collection
	 * @return a new <code>MultiLineString2DAdapter</code>-object
	 */
	public static MultiLineString2DAdapter createMultiLineString2D(LineString2DAdapter[] lineStrings) {
		MultiLineString m = null;			
		if(lineStrings== null || lineStrings.length==0)
			m = DOUBLE_GEOMETRYFACTORY.createMultiLineString(null);
		else{
			LineString[] l = new LineString[lineStrings.length];
			for(int i=0; i< l.length; i++) l[i] = (LineString) lineStrings[i].geometry;
			m = l[0].getFactory().createMultiLineString(l);
		}		
	    return new MultiLineString2DAdapter( m );	
	}
	

	/** Wraps the given polygons in a <code>MultiPolygon2DAdapter</code>-collection.
	 *  The <code>PrecisionModel</code> is determined from the polygons' models. 
	 * @param polygons the polygons to wrap in the collection
	 * @return a new <code>MultiPolygon2DAdapter</code>-object
	 */
	public static MultiPolygon2DAdapter createMultiPolygon2D(Polygon2DAdapter[] polygons) {
		MultiPolygon m = null;
		if(polygons== null || polygons.length==0)
			m = DOUBLE_GEOMETRYFACTORY.createMultiPolygon(null);
		else{
			Polygon[] p = new Polygon[polygons.length];
			for(int i=0; i< p.length; i++) p[i] = (Polygon) polygons[i].geometry;
			m = p[0].getFactory().createMultiPolygon(p);		
		}
		return new MultiPolygon2DAdapter(m);	
	}	


	/** Wraps the given geometries in a <code>GeometryCollection2DAdapter</code>-object.
	 *  The <code>PrecisionModel</code> is determined from the geometries' models. 
	 * @param geometries the geometries to wrap in the collection
	 * @return a new <code>GeometryCollection<2DAdapter</code>-object
	 */
	public static GeometryCollection2DAdapter createGeometryCollection2D(Geometry2DAdapter[] geometries) {
		GeometryCollection m = null;
		if(geometries == null || geometries.length==0)
				m= DOUBLE_GEOMETRYFACTORY.createGeometryCollection(null);
		else {
			Geometry[] g = new Geometry[geometries.length];
			for(int i=0; i< g.length; i++) g[i] = geometries[i].geometry;
			m = g[0].getFactory().createGeometryCollection(g);
		}
		return new GeometryCollection2DAdapter( m );
	}
	
	

	/** Wraps the given list of geometries in the appropriate {@link GeometryCollection}-subclass.
	 * @param geometryList the geometries to wrap
	 * @return the <code>GeometryCollection</code>-object appropriate for the given set of geometries.
	 */
	public static Geometry2DAdapter buildGeometry(Collection<Geometry> geometryList){
		GeometryFactory factory = geometryList.size()> 0 ? geometryList.iterator().next().getFactory() : getGeometryFactory(DOUBLE_PRECISIONMODEL);		 
		return Geometry2DFactory.wrap(factory.buildGeometry(geometryList));
	}
}
