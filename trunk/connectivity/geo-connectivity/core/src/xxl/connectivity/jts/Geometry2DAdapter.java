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

import java.awt.Shape;
import java.awt.geom.NoninvertibleTransformException;

import xxl.core.spatial.geometries.Geometry2D;
import xxl.core.spatial.geometries.Geometry2DException;
import xxl.core.spatial.rectangles.Rectangle;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.geom.TopologyException;
import com.vividsolutions.jts.io.WKBWriter;
import com.vividsolutions.jts.io.WKTWriter;
import com.vividsolutions.jts.operation.buffer.BufferOp;
import com.vividsolutions.jts.precision.EnhancedPrecisionOp;
import com.vividsolutions.jump.workbench.ui.renderer.java2D.Java2DConverter;

/** This is the base class of the wrapped JTS geometry model in XXL. It implements
 *  the interface {@link Geometry2D} which is the base interface of the abstract XXL 
 *  geometry model. 
 *  This implementation provides an <a href="http://www.opengis.org/">OGC</a> 
 *  <i>Simple Feature Specification</i> compliant geometry model based on the model 
 *  of the Java Topology Suite (<a href="http://jump-project.org">http://jump-project.org</a>).
 *  Objects of the JTS model are wrapped inside <code>Geometry2DAdapter</code> objects and 
 *  most function calls are delegated to those wrapped objects wherever possible or usefull.
 *  <br><br>
 *  <code>Geometry2DAdapter</code> does not only implement the required methods of the {@link Geometry2D} 
 *  interface, but already implements some methods of its subinterfaces, wherever the underlying
 *  JTS <code>Geometry</code> provides the required functionality. Because of that some
 *  subclasses of <code>Geometry2DAdapter</code> form empty classes which are only needed to 
 *  represent the complete model.
 *    
 * @see Geometry2DFactory
 * @see Geometry2D
 * @see Geometry
 */
public abstract class Geometry2DAdapter implements Geometry2D{        
	
 ////////////////////
 // static fields //
 ////////////////////	
	
	private static final WKBWriter WKB_WRITER_2D = new WKBWriter(2);
	private static final WKBWriter WKB_WRITER_3D = new WKBWriter(3);
	private static final WKTWriter WKT_WRITER_2D = new WKTWriter();	

 ////////////////////
 //  member fields //
 ////////////////////	
	
	/** the encpasulatet JTS-<code>Geometry</code> */
	protected final Geometry geometry; 	
	
 ////////////////////
 //  constructor   //
 ////////////////////		
   
    /** Creates a new two-dimensional <code>Geometry2DAdapter</code>- object specified by the given JTS-<code>Geometry</code>
     *  The constructor can only be called from within this class or its subclasses. Objects of these classes will only be instantiated by calling the
     *  static functions {@link Geometry2DFactory#wrap(Geometry)} or by using a {@link Geometry2DFactory} function.
     * @param geometry the JTS-<code>Geometry</code> wrapped inside this object
     */
    protected Geometry2DAdapter(Geometry geometry){          
        if(geometry == null)
        	throw new Geometry2DException("Cannot handle null value!");        
        this.geometry = geometry;                
    }
    
 //////////////
 // methods  //
 //////////////		

    /** Returns the wrapped JTS-<code>Geometry</code> of this object 
     * @return the wrapped JTS-<code>Geometry</code>
     */
    public Geometry getJTSGeometry(){ 
    	return (Geometry) geometry.clone();
    }
     
    
    /** Creates and returns a physical copy of this <code>Geometry2DAdapter</code> object.
	 * @return a copy of this <code>Geometry2DAdapter</code>
	*/    	
	public Geometry2DAdapter clone() { 
		return Geometry2DFactory.wrap((Geometry) geometry.clone()); 
	}

	
	/**
	 *  Returns whether this <code>Geometry2DAdapter</code> is greater than, equal to,
	 *  or less than another <code>Geometry2DAdapter</code>. <P>
	 *
	 *  If their classes are different, they are compared using the following
	 *  ordering:
	 *  <UL>
	 *    <LI> Point2DAdapter (lowest)
	 *    <LI> MultiPoint2DAdapter
	 *    <LI> LineString2DAdapter
	 *    <LI> LinearRing2DAdapter
	 *    <LI> MultiLineString2DAdapter
	 *    <LI> Polygon2DAdapter
	 *    <LI> MultiPolygon2DAdapter
	 *    <LI> GeometryCollection2DAdapter (highest)
	 *  </UL>
	 *  If the two <code>Geometry2DAdapter</code>s have the same class, their first
	 *  elements are compared. If those are the same, the second elements are
	 *  compared, etc.
	 *
	 *@param  other  a <code>Geometry2DAdapter</code> with which to compare this <code>Geometry</code>
	 *@return a positive number, 0, or a negative number, depending on whether
	 *      this object is greater than, equal to, or less than <code>other</code>
	 */
    public int compareTo(Geometry2D other) { 
    	return(geometry.compareTo(((Geometry2DAdapter)other).geometry)); 
    }
    
    
  /* spatial predicates */
    
    /** Returns <code>true</code> if this geometry is equal to the
     *  specified geometry.
     *  <p>
     *  The <code>equals</code> predicate has the following equivalent definitions:
     *  <ul>
     *  <li>The two geometries have at least one point in common,
     *  and no point of either geometry lies in the exterior of the other geometry.
     *  <li>The DE-9IM Intersection Matrix for the two geometries is T*F**FFF*
     *  </ul>
     *
     *  @param  other  the <code>Geometry2DAdapter</code> with which to compare this <code>Geometry2DAdapter</code>
     *  @return <tt>true</tt> if the given objects equals this geometry, <tt>false</tt> otherwise 
	 */
	public boolean equals(Geometry2D other){ 
		return geometry.equals( ((Geometry2DAdapter)other).geometry ); 
	}

	/** @inheritDoc
	 * <code>other</code> is expected to be of type <code>Geometry2DAdapter</code>
     *  @see Geometry#contains(Geometry)
     */
    public boolean contains(Geometry2D other) {    	
    	return geometry.contains( ((Geometry2DAdapter)other).geometry );
    }
    
    /** @inheritDoc
 	 * <code>other</code> is expected to be of type <code>Geometry2DAdapter</code>
 	 * @see Geometry#within(Geometry)
	 */
	public boolean isWithin(Geometry2D other) { 
		return geometry.within(((Geometry2DAdapter)other).geometry); 
	}

	/** @inheritDoc
	 * <code>other</code> is expected to be of type <code>Geometry2DAdapter</code>
     *  @see Geometry#contains(Geometry)
     */
    public boolean covers(Geometry2D other){
    	return geometry.covers(((Geometry2DAdapter)other).geometry);
    }
        
    /** @inheritDoc
     *  <code>other</code> is expected to be of type <code>Geometry2DAdapter</code>
     *  @see Geometry#coveredBy(Geometry)
     */
    public boolean isCoveredBy(Geometry2D other){
    	return geometry.coveredBy(((Geometry2DAdapter)other).geometry);
    }
    
    /** @inheritDoc
     *  <code>other</code> is expected to be of type <code>Geometry2DAdapter</code>
     *  @see Geometry#overlaps(Geometry)
     */
	public boolean overlaps(Geometry2D other) { 
		return geometry.overlaps(((Geometry2DAdapter)other).geometry); 
	}

    /** @inheritDoc
     *  <code>other</code> is expected to be of type <code>Geometry2DAdapter</code>
     *  @see Geometry#intersects(Geometry)
     */
	public boolean intersects(Geometry2D other){ 
		return geometry.intersects(((Geometry2DAdapter)other).geometry); 
	}

    /** @inheritDoc
     *  <code>other</code> is expected to be of type <code>Geometry2DAdapter</code>
	 * @see Geometry#crosses(Geometry)
	 */
	public boolean crosses(Geometry2D other) { 
		return geometry.crosses(((Geometry2DAdapter)other).geometry); 	
	}
               
    /** @inheritDoc
     *  <code>other</code> is expected to be of type <code>Geometry2DAdapter</code>
	 * @see Geometry#touches(Geometry)
	 */
    public boolean touches(Geometry2D other) { 
    	return geometry.touches(((Geometry2DAdapter)other).geometry); 
    }

    /** @inheritDoc
     *  <code>other</code> is expected to be of type <code>Geometry2DAdapter</code>
	 * @see Geometry#disjoint(Geometry)
	 */
	public boolean isDisjoint(Geometry2D other) { 
		return geometry.disjoint(((Geometry2DAdapter)other).geometry); 
	}
    
    /** @inheritDoc
     *  <code>other</code> is expected to be of type <code>Geometry2DAdapter</code>
	 * @see Geometry#relate(Geometry)
	 */    
	public boolean relate(Geometry2D other, String intersectionPatternMatrix) {
		return geometry.relate(((Geometry2DAdapter)other).geometry, intersectionPatternMatrix); 				
	}  
	
	/** @inheritDoc
	 *  <code>other</code> is expected to be of type <code>Geometry2DAdapter</code>
     * @see Geometry#distance(Geometry)
     */
    public double distance(Geometry2D other){ 
    	return geometry.distance(((Geometry2DAdapter)other).geometry ); 
    }
  
	/** Returns the area of this <code> Geometry2DAdapter</code>-object. 
	 *  The area of a Point is 0, the area of a LineString equals its length.
	 *  For GeometryCollections this method returns the sum of all contained
	 *  Geometries and for Polygons the actual area is returned.
	 * @return the area of this <code> Geometry2DAdapter</code>
     * @see Geometry#getArea()
     */
    public double getArea() { return geometry.getArea(); } 

    /** @inheritDoc 
     * @see Geometry#getBoundary()
     */
    public Geometry2DAdapter getBoundary() { 
    	return Geometry2DFactory.wrap(geometry.getBoundary()); 
    } 

    /** Returns the dimension of this objects' boundary 
     * @return the dimension of this objects' boundary
     * @see Geometry#getBoundaryDimension()
     */
    public int getBoundaryDimension() { 
    	return geometry.getBoundaryDimension(); 
    }
    
    /** Returns the centroid of this <code>Geometry2DAdapter</code>.
     *  <b>Note:</b> This method is required for Surfaces only but can be
     *  extended to arbitrary geometries   
     * @return the centroid of this <code>Geometry2DAdapter</code>
     * @see Geometry#getCentroid()
     */
    public Point2DAdapter getCentroid(){ 
    	return new Point2DAdapter( geometry.getCentroid()); 
    } 
    
    /** Returns a Point of this <code>Geometry2DAdapter</code> which is guaranteed to lie
     *  on the geometries interior.
     *  <b>Note:</b> This method is required for Surfaces only but can be
     *  extended to arbitrary geometries   
     * @return a Point of this <code>Geometry2DAdapter</code> which is guaranteed to lie on the geometries interior
      * @see Geometry#getInteriorPoint()
     */
    public Point2DAdapter getInteriorPoint(){ 
    	return new Point2DAdapter( geometry.getInteriorPoint()); 
    }   
    
    /** @inheritDoc 
     * @see Geometry#getDimension()
     */    
    public int getDimensions() { 
    	return geometry.getDimension(); 
    }    
    
    /** Returns the type of this <code>Geometry2DAdapter</code> as a String
     * @return any of <code>POINT, MULTIPOINT, LINESTRING, MULTILINESTRING, 
     * 		   POLYGON</code> or <code>MULTIPOLYGON</code>
     * @see Geometry#getGeometryType()
     */
    public String getGeometryType(){ 
    	return geometry.getGeometryType(); 
    }

	/** Returns the minimal bounding rectangle (MBR) of the encapsulated geometry 
     * @return the minimal bounding rectangle of the encapsulated geometry using the geometrys precision model
     */
	public Geometry2DAdapter getEnvelope() { 
		return Geometry2DFactory.envelopeToPolygon(
				geometry.getEnvelopeInternal(), geometry.getPrecisionModel()
			); 
	}
	
	/** Returns the minimal bounding rectangle (MBR) of the encapsulated geometry as a XXL- <code>DoublePointRectangle</code>.
     * @return the minimal bounding rectangle of the encapsulated geometry using the geometrys precision model
     */
    public Rectangle getMBR(){
    	return getMBR(true);      		    	
    }
	    
	/** Returns the minimal bounding rectangle (MBR) of the encapsulated geometry. The MBR is returned as a DoublePointRectangle
	 *  or as a FloatPointRectangle, depending on the parameter <code>returnDoublePointRectangle</code>
	 * @param returnDoublePointRectangle indicates, whether to return the mbr as a {@link xxl.core.spatial.rectangles.DoublePointRectangle DoublePointRectangle} 
     * 						  			(<tt>true</tt>) or as a {@link xxl.core.spatial.rectangles.FloatPointRectangle FloatPointRectangle} (<tt>false</tt>).
     * @return the minimal bounding rectangle of the encapsulated geometry
     */
    public Rectangle getMBR(boolean returnDoublePointRectangle){
    	return Geometry2DFactory.envelopeToXXLRectangle(
    				geometry.getEnvelopeInternal(),
    				returnDoublePointRectangle
    			);           
    }
	     
    /** Returns the number of this objects' vertices
     *  @return the number of vertices      
     */
    public int getNumberOfPoints(){    	
    	return geometry.getNumPoints(); 
    }
	    
    /** Returns the length of linear geometries and the perimeter of
     *  areal geometries respectively.
     * @return the length of linear geometries and the perimeter of areal geometries respectively
     * @see Geometry#getLength()
     */
    public double getLength() { 
    	return geometry.getLength(); 
    }
        
    /** @inheritDoc 
     * @see Geometry#getSRID()
     */
	public int getSRID() { 
		return geometry.getSRID(); 
	}
    
	 /** Sets the ID of the Spatial Reference System used by this geometry 
	 * @param SRID the ID of the Spatial Reference System to set
     * @see Geometry#setSRID(int)
     */
	public void setSRID(int SRID) { 
		geometry.setSRID(SRID); 
	}
	
    /** @inheritDoc
     *  @see Geometry#isEmpty()
	 */
	public boolean isEmpty(){ 
		return geometry== null || geometry.isEmpty(); 
	}

	/** determines this <code>Geometry2DAdapter</code>'s precision model
     * @return the PrecisionModel of the underlying JTS- Geometry
     */
    public PrecisionModel getPrecisionModel(){
    	return geometry.getPrecisionModel();
    }    
        
    /** @inheritDoc
     *  @see Geometry#isSimple()
     */
    public boolean isSimple(){ 
    	return geometry.isSimple(); 
    }
        
    /** Tests the validity of this Geometry2DAdapter.
     * @return <tt>true</tt> if the Geometry2DAdapter object is valid, <tt>false</tt> otherwise
     * @see Geometry#isValid()
     */
    public boolean isValid(){ 
    	return geometry.isValid(); 
    }  
    
    /** @inheritDoc 
     *  @see Geometry#buffer(double)
	 */
	public Geometry2DAdapter buffer(double width){
	    return Geometry2DFactory.wrap( geometry.buffer(width)); 
	}

    /** Computes a buffer around this <code>Geometry2DAdapter</code> having the given width and approximation accuracy.  
     * 
     *  If the computed buffer contains circular arcs, these are approximated by the given number of line-segments.
     *   
     *  @param width the maximum distance between a point inside the buffer and this <code>Geometry2DAdapter</code> 
     *  @param quadrantSegments the number of line-segments to approximate circular buffer shapes 
     *  @return a new <code>Geometry2DAdapter</code>-object representing the buffer
     *  @see Geometry#buffer(double, int)
	 */
	public Geometry2DAdapter buffer(double width, int quadrantSegments){
	    return Geometry2DFactory.wrap( geometry.buffer(width, quadrantSegments)); 
	}
	
	/** The end cap style specifies the buffer geometry that will be created at the ends of linestrings. The styles provided are: */
	public static enum EndCapStyle {
		/**	a semi circle (default) */
		ROUND, 
		/**	a straight line perpendicular to the end segment */
		BUTT, 
		/**	a half-square */
		SQUARE
	};
	
    /** Computes a buffer around this <code>Geometry2DAdapter</code> having the given width and approximation accuracy and
     *  using the given end cap style.
     * 
     *  If the computed buffer contains circular arcs, these are approximated by the given number of line-segments.
     * 
     *  @param width the maximum distance between a point inside the buffer and this <code>Geometry2DAdapter</code> 
     *  @param quadrantSegments the number of line-segments to approximate circular buffer shapes 
     *  @param endCapStyle the end cap style to use {@link EndCapStyle}
     *  @return a new <code>Geometry2DAdapter</code>-object representing the buffer
     *  @see Geometry#buffer(double, int)
	 */
	public Geometry2DAdapter buffer(double width, int quadrantSegments, EndCapStyle endCapStyle){
		switch(endCapStyle){
			case BUTT 	: return Geometry2DFactory.wrap(geometry.buffer(width, quadrantSegments, BufferOp.CAP_BUTT));
			case SQUARE : return Geometry2DFactory.wrap(geometry.buffer(width, quadrantSegments, BufferOp.CAP_SQUARE));
			default   	: return Geometry2DFactory.wrap(geometry.buffer(width, quadrantSegments, BufferOp.CAP_ROUND));
		}		
	}

	/** @inheritDoc
	 *  <code>other</code> is expected to be of type <code>Geometry2DAdapter</code>
	 *  <br><br>
	 *  This implementation uses an <code>EnhancedPrecisionOp</code> which uses 
	 *  enhanced precision techniques, if the call to {@link Geometry#difference(Geometry)}
	 *  causes a TopologyException due to robusteness errors.
	 *  @throws TopologyException is thrown, if the <code>EnhancedPrecisionOp</code> encounters a robustness error too.  
	 *  @see EnhancedPrecisionOp#difference(Geometry, Geometry)
	 *  @see TopologyException
	 */
	public Geometry2DAdapter difference(Geometry2D other){
		// the EnhancedPrecisionOp decides whether to use simple or advanced 
		// but more expensive algorithms for the computation		
        return Geometry2DFactory.wrap( EnhancedPrecisionOp.difference(geometry, ((Geometry2DAdapter)other).geometry));  
	}

	/** @inheritDoc
	 *  <code>other</code> is expected to be of type <code>Geometry2DAdapter</code>
	 *  <br><br>
	 *  This implementation uses an <code>EnhancedPrecisionOp</code> which uses 
	 *  enhanced precision techniques, if the call to {@link Geometry#intersection(Geometry)}
	 *  causes a TopologyException due to robusteness errors.
	 *  @throws TopologyException is thrown, if the <code>EnhancedPrecisionOp</code> encounters a robustness error too.  
	 *  @see EnhancedPrecisionOp#intersection(Geometry, Geometry)
	 *  @see TopologyException
	 */
	public Geometry2DAdapter intersection(Geometry2D other) {
        return Geometry2DFactory.wrap( EnhancedPrecisionOp.intersection(geometry, ((Geometry2DAdapter)other).geometry));  
	}
    
    
	/** @inheritDoc
	 *  <code>other</code> is expected to be of type <code>Geometry2DAdapter</code>
	 *  <br><br>
	 *  This implementation uses an <code>EnhancedPrecisionOp</code> which uses 
	 *  enhanced precision techniques, if the call to {@link Geometry#union(Geometry)}
	 *  causes a TopologyException due to robusteness errors.
	 *  @throws TopologyException is thrown, if the <code>EnhancedPrecisionOp</code> encounters a robustness error too.  
	 *  @see EnhancedPrecisionOp#union(Geometry, Geometry)
	 *  @see TopologyException
	 */
	public Geometry2D union(Geometry2D other) {
        return Geometry2DFactory.wrap( EnhancedPrecisionOp.union(geometry, ((Geometry2DAdapter)other).geometry));  
	}

	/** @inheritDoc
	 *  <code>other</code> is expected to be of type <code>Geometry2DAdapter</code>
	 *  <br><br>
	 *  This implementation uses an <code>EnhancedPrecisionOp</code> which uses 
	 *  enhanced precision techniques, if the call to {@link Geometry#symDifference(Geometry)}
	 *  causes a TopologyException due to robusteness errors.
	 *  @throws TopologyException is thrown, if the <code>EnhancedPrecisionOp</code> encounters a robustness error too.  
	 *  @see EnhancedPrecisionOp#symDifference(Geometry, Geometry)
	 *  @see TopologyException
	 */
	public Geometry2D symDifference(Geometry2D other){
        return Geometry2DFactory.wrap( EnhancedPrecisionOp.symDifference(geometry, ((Geometry2DAdapter)other).geometry));  
	}

	/** @inheritDoc
	 *  @see Geometry#convexHull() 
	 */	
	public Geometry2D convexHull() {
		return Geometry2DFactory.wrap( geometry.convexHull());    	
	}

			
	/** Returns a string representation of this geometry.
	 * @return returns the string representation of this geometry
	*/	
	public String toString(){
//	    StringBuffer bf = new StringBuffer("\n"+this.getClass().getSimpleName()+" [ \n\tCOORDINATES={ ");
//	    
//	    Coordinate[] c = geometry.getCoordinates();
//	    for(int i=0;i< c.length; i++) bf.append("("+c[i].x+", "+c[i].y+"),");        
//	    bf.setCharAt(bf.length()-1,'}');
//	    bf.append("\n\tTYPE="+geometry.getGeometryType());
//	    bf.append("\n\tPRECISION="+getPrecisionModel());        
//	    bf.append("\n\tSRID="+getSRID());
//	    bf.append("\n\tMBR="+geometry.getEnvelopeInternal());
//	    bf.append("\n\t]");
//	    return bf.toString();
		return toWKT();
	}
	
	/** @inheritDoc
	 *  @see WKBWriter#write(Geometry)
     */
    public byte[] toWKB() {	
    	Coordinate c = this.geometry.getInteriorPoint().getCoordinate();    	
		return Double.isNaN(c.z) 
				? WKB_WRITER_2D.write(geometry) 
				: WKB_WRITER_3D.write(geometry);
	}


	/** @inheritDoc
	 *  @see WKTWriter#write(Geometry)
     */
    public String toWKT(){ 
    	return WKT_WRITER_2D.write(geometry); 
    }	

	/** Generates a {@link Shape}-representation of this <code>Geometry2DAdapter</code>
	 * 
	 * @param java2DConverter needed to compute the shape of the given geometrie
	 * @return the {@Link Shape}-object representing this geometries shape 
	 * @throws NoninvertibleTransformException
	 */
	public Shape toShape(Java2DConverter java2DConverter) throws NoninvertibleTransformException {
		return java2DConverter.toShape(geometry);
	}

}
