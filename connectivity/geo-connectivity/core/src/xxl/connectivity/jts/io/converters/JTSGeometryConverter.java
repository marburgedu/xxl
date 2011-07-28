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
package xxl.connectivity.jts.io.converters;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import xxl.connectivity.jts.Geometry2DAdapter;
import xxl.connectivity.jts.Geometry2DFactory;
import xxl.connectivity.jts.io.PrecisionModelConverter;
import xxl.core.io.converters.Converter;
import xxl.core.spatial.geometries.Geometry2DException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;

/**
 * This is a converter that is able to read and write <code>Geometry2DAdapter</code> objects
 *  
 * Elementary objects like points and linestrings are directly serialized by serializing
 * their coordinates. Serialization of more complex objects like polygons is based on 
 * the serialization of elementary objects. GeometryCollections serialize the 
 * contained Geometries recursively. 
 *  
 * @see xxl.core.io.converters.Converter
 * @see DataInput
 * @see DataOutput
 * @see IOException
 */
public class JTSGeometryConverter extends Converter<Geometry2DAdapter>{
	
	/* some constants indicating the type of the serialized geometries */
	private final static byte POINT 			 = 1;
	private final static byte MULTIPOINT 		 = 2;
	private final static byte LINESTRING 		 = 3;
	private final static byte LINEARRING 		 = 4;
 	private final static byte MULTILINESTRING  	 = 5;
	private final static byte POLYGON 			 = 6;
	private final static byte MULTIPOLYGON 		 = 7;
	private final static byte GEOMETRYCOLLECTION = 8;	
	
	/**
	 * This instance can be used for getting a default instance of an Geometry
	 * converter. It is similar to the <i>Singleton Design Pattern</i> (for
	 * further details see Creational Patterns, Prototype in <i>Design
	 * Patterns: Elements of Reusable Object-Oriented Software</i> by Erich
	 * Gamma, Richard Helm, Ralph Johnson, and John Vlissides) except that
	 * there are no mechanisms to avoid the creation of other instances of
	 * an Geometry converter.
	 */
	public static final JTSGeometryConverter DEFAULT_INSTANCE = new JTSGeometryConverter();	
		
	/**
	 *  Creates a new WKTGeometryConverter- instance	 
	 */
	private JTSGeometryConverter(){}
		
		
	/**
	 * Restores the <code>Geometry2DAdapter</code> object from the stream.
	 * 
	 * <p>This implementation ignores the specified object and returns a new
	 * <code>Geometry2DAdapter</code> object. So it does not matter if the specified
	 * object is <code>null</code>.</p>
	 *
	 * @param input the stream to read the <code>Geometry2DAdapter</code> object from 
	 * @param object the (<code>Geometry2DAdapter</code>) object to be restored. In this
	 *        implementation it is ignored.
	 * @return the read <code>Geometry2DAdapter</code> object.
	 * @throws IOException if I/O errors occur.
	 */
	public Geometry2DAdapter read(DataInput input, Geometry2DAdapter object) throws IOException {
		
		return Geometry2DFactory.wrap(readJTSGeometry(input));
	}
	
	private Geometry readJTSGeometry(DataInput input) throws IOException{
		PrecisionModel p = PrecisionModelConverter.DEFAULT_INSTANCE.read(input, null);
		int SRID = input.readInt();
		
		boolean flag3D = input.readBoolean();
		int geometryType = input.readByte();		
		Geometry geometry = null;
		GeometryFactory f = getGeometryFactory(p); 
		switch(geometryType){
			case POINT : geometry = readPoint(input, f, flag3D); break;
			case MULTIPOINT : geometry =  readMultiPoint(input, f, flag3D); break;
			case LINESTRING : geometry = readLineString(input, f, flag3D); break;
			case LINEARRING : geometry = readLinearRing(input, f, flag3D); break;
			case MULTILINESTRING : geometry = readMultiLineString(input, f, flag3D); break;
			case POLYGON : geometry = readPolygon(input, f, flag3D); break;
			case MULTIPOLYGON : geometry = readMultiPolygon(input, f, flag3D); break;
			case GEOMETRYCOLLECTION : geometry = readGeometryCollection(input); break;
			
			default : throw new Geometry2DException("Deserialize Geometry: Cannot identify type of geometry!");
		}
		geometry.setSRID(SRID);
		return geometry;
	}
	
	/**
	 * Serializes the given <code>Geometry2DAdapter</code> object to the specified data output.
	 * 
	 * @param output the stream to write the WKT- representation of the <code>Geometry2DAdapter</code> 
	 *        object to.
	 * @param geometry the <code>Geometry2DAdapter</code> object whose WKT- representation
	 *        should be written to the data output.
	 * @throws IOException includes any I/O exceptions that may occur.
	 */
	public void write(DataOutput output, Geometry2DAdapter geometry) throws IOException {
		if(geometry == null ) 
			throw new NullPointerException();		
		write(output, geometry.getJTSGeometry());
	}
		
	private void write(DataOutput output, Geometry geometry) throws IOException{
		// JTS provides 3d- Coordinates. We check if one coordinate of the geometry
		// is a 3d- coordinate. We only check one coordinate of the geometry to 
		// decide whether we have to store two or three ordinate per coordinate.
		Coordinate c = geometry.getInteriorPoint().getCoordinate();		
		boolean flag3D = ! Double.isNaN(c.z);
		
		PrecisionModel p = geometry.getPrecisionModel();
		PrecisionModelConverter.DEFAULT_INSTANCE.write( output, p );
		output.writeInt(geometry.getSRID());
		output.writeBoolean(flag3D);
		// choose the appropriate method to serialize the given geometry
		if(geometry.getGeometryType().equalsIgnoreCase("POINT"))
			writePoint(output, (Point) geometry, flag3D);	
		else if(geometry.getGeometryType().equalsIgnoreCase("MULTIPOINT"))
			writeMultiPoint(output, (MultiPoint) geometry, flag3D);
		else if(geometry.getGeometryType().equalsIgnoreCase("LINESTRING"))
			writeLineString(output, (LineString) geometry, flag3D);
		else if(geometry.getGeometryType().equalsIgnoreCase("LINEARRING"))
			writeLinearRing(output, (LinearRing) geometry, flag3D);
		else if(geometry.getGeometryType().equalsIgnoreCase("MULTILINESTRING"))
			writeMultiLineString(output, (MultiLineString) geometry, flag3D);
		else if(geometry.getGeometryType().equalsIgnoreCase("POLYGON"))
			writePolygon(output, (Polygon) geometry, flag3D);
		else if(geometry.getGeometryType().equalsIgnoreCase("MULTIPOLYGON"))
			writeMultiPolygon(output, (MultiPolygon) geometry, flag3D);
		else if(geometry.getGeometryType().equalsIgnoreCase("GEOMETRYCOLLECTION"))
			writeGeometryCollection(output, (GeometryCollection) geometry);
		else throw new Geometry2DException("Don't know how to serialize "+geometry);
	}
			
	
   private static GeometryFactory DOUBLE_GEOMETRYFACTORY 	= new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING));
   private static GeometryFactory FLOAT_GEOMETRYFACTORY 	= new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING_SINGLE));
    
   private static GeometryFactory getGeometryFactory(PrecisionModel p){
		return p.getMaximumSignificantDigits()==16	? DOUBLE_GEOMETRYFACTORY
		   		: p.getMaximumSignificantDigits()==6 ? FLOAT_GEOMETRYFACTORY
		   					 : new GeometryFactory( p );		
	}
	
	private void writeCoordinate(DataOutput output, Coordinate coord, boolean flag3D) throws IOException{
			output.writeDouble(coord.x);
			output.writeDouble(coord.y);
			if(flag3D) output.writeDouble(coord.z);
	}
	
	private Coordinate readCoordinate(DataInput input,  boolean flag3D) throws IOException{
		return new Coordinate(input.readDouble(), input.readDouble(), flag3D ? input.readDouble() : Double.NaN);		
	}
	
	
	private void writePoint(DataOutput output, Point point, boolean flag3D) throws IOException{
		output.writeByte(POINT);	
		writeCoordinate(output, point.getCoordinate(), flag3D);
	}
	
	private Point readPoint(DataInput input, GeometryFactory factory, boolean flag3D) throws IOException{				
		return factory.createPoint( readCoordinate(input, flag3D)); 
	}
	
	
	private void writeMultiPoint(DataOutput output, MultiPoint multiPoint, boolean flag3D) throws IOException{
		output.writeByte(MULTIPOINT);
				
		Coordinate[] coordinates = multiPoint.getCoordinates();
		
		output.writeInt(coordinates.length);
		
		for(int i=0; i< coordinates.length; i++)
			writeCoordinate(output, coordinates[i], flag3D);						
	}
	
	private MultiPoint readMultiPoint(DataInput input, GeometryFactory factory, boolean flag3D) throws IOException{		
		Coordinate[] coordinates = new Coordinate[input.readInt()];
		
		for(int i=0; i< coordinates.length; i++)		
			coordinates[i] = readCoordinate(input, flag3D);
		
		return factory.createMultiPoint(coordinates);		
	}

	
	private void writeLineString(DataOutput output, LineString lineString, boolean flag3D) throws IOException{
		output.writeByte(LINESTRING);
		
		Coordinate[] coordinates = lineString.getCoordinates();

		output.writeInt(coordinates.length);
		
		for(int i=0; i< coordinates.length; i++)
			writeCoordinate(output, coordinates[i], flag3D);		
	}
	
	private LineString readLineString(DataInput input, GeometryFactory factory, boolean flag3D) throws IOException{
		Coordinate[] coordinates = new Coordinate[input.readInt()];
		
		for(int i=0; i< coordinates.length; i++)		
			coordinates[i] = readCoordinate(input, flag3D);
		
		return factory.createLineString(coordinates);		
	}

	
	private void writeLinearRing(DataOutput output, LinearRing linearRing, boolean flag3D) throws IOException{
		output.writeByte(LINEARRING);
		
		Coordinate[] coordinates = linearRing.getCoordinates();

		output.writeInt(coordinates.length);
		
		for(int i=0; i< coordinates.length; i++)
			writeCoordinate(output, coordinates[i], flag3D);		
	}
	
	private LinearRing readLinearRing(DataInput input, GeometryFactory factory, boolean flag3D) throws IOException{
		Coordinate[] coordinates = new Coordinate[input.readInt()];
		
		for(int i=0; i< coordinates.length; i++)		
			coordinates[i] = readCoordinate(input, flag3D);
		
		return factory.createLinearRing(coordinates);		
	}
	
	
	private void writeMultiLineString(DataOutput output, MultiLineString multiLineString, boolean flag3D) throws IOException{
		output.writeByte(MULTILINESTRING);						
		output.writeInt(multiLineString.getNumGeometries());
		for(int i=0; i< multiLineString.getNumGeometries(); i++)
			writeLineString(output, (LineString) multiLineString.getGeometryN(i), flag3D);
	}
	
	private MultiLineString readMultiLineString(DataInput input, GeometryFactory factory, boolean flag3D) throws IOException{		
		LineString[] lineStrings = new LineString[input.readInt()];
		for(int i=0;i< lineStrings.length; i++){
			if(input.readByte()!= LINESTRING)
				throw new Geometry2DException("Deserializing MultiLineString: LineString expected!");
			lineStrings[i] = readLineString(input, factory, flag3D);
		}
			
		return factory.createMultiLineString(lineStrings);	
	}
	
	private void writePolygon(DataOutput output, Polygon polygon, boolean flag3D) throws IOException{
		output.writeByte(POLYGON);
		writeLinearRing(output, (LinearRing) polygon.getExteriorRing(), flag3D);
		output.writeInt(polygon.getNumInteriorRing());
		for(int i=0; i< polygon.getNumInteriorRing(); i++)
			writeLinearRing(output, (LinearRing) polygon.getInteriorRingN(i), flag3D);
	}
	
	private Polygon readPolygon(DataInput input, GeometryFactory factory, boolean flag3D) throws IOException{

		if(input.readByte()!= LINEARRING)
			throw new Geometry2DException("Deserializing MultiLineString: LinearRing expected!");
		LinearRing shell = readLinearRing(input, factory, flag3D);
		
		LinearRing[] holes = new LinearRing[input.readInt()];		
		for(int i=0;i< holes.length; i++){
			if(input.readByte()!= LINEARRING)
				throw new Geometry2DException("Deserializing Polygon: LinearRing expected!");
			holes[i] = readLinearRing(input, factory, flag3D);
		}
		
		return factory.createPolygon(shell, holes);
	}
	
	
	private void writeMultiPolygon(DataOutput output, MultiPolygon multiPolygon, boolean flag3D) throws IOException{
		output.writeByte(MULTIPOLYGON);						
		output.writeInt(multiPolygon.getNumGeometries());
		for(int i=0; i< multiPolygon.getNumGeometries(); i++)
			writePolygon(output, (Polygon) multiPolygon.getGeometryN(i), flag3D);
	}
	
	private MultiPolygon readMultiPolygon(DataInput input,GeometryFactory factory, boolean flag3D) throws IOException{		
		Polygon[] polygons = new Polygon[input.readInt()];
		for(int i=0; i<polygons.length; i++){
			if(input.readByte()!= POLYGON)
				throw new Geometry2DException("Deserializing MultiPolygon: Polygon expected!");
			polygons[i] = readPolygon(input, factory, flag3D);
		}
			
		return factory.createMultiPolygon(polygons);	
	}
	
	
	private void writeGeometryCollection(DataOutput output, GeometryCollection geometryCollection) throws IOException{
		output.writeByte(GEOMETRYCOLLECTION);
		output.writeInt(geometryCollection.getNumGeometries());
		for(int i=0; i< geometryCollection.getNumGeometries(); i++)
			write(output, geometryCollection);		
	}
	

	private GeometryCollection readGeometryCollection(DataInput input) throws IOException{
		Geometry[] geometries = new Geometry[input.readInt()];
		for(int i=0; i< geometries.length; i++)
			geometries[i] = readJTSGeometry(input);		
		return geometries[0].getFactory().createGeometryCollection(geometries);		
	}
}
