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

import com.vividsolutions.jts.geom.PrecisionModel;

/**
 * This class provides a converter that is able to read and write
 * <code>Geometry2DAdapter</code> objects using their Well- Known Binary (WKT) 
 * representation.
 * 
 * This converter additionaliy stores information concerning the geometries 
 * PrecisionModel.
 * 
 * <p>Example usage (1).
 * <code><pre>
 *   // create a byte array output stream
 *   java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
 *   // create a Geometry
 *   xxl.connectivity.jts.Point2DAdapter geometry0 = xxl.connectivity.jts.Geometry2DFactory.createPoint(5,5);
 *          
 *   // serialize the Geometry object to the given outputstream
 *   WKBGeometryConverter.DEFAULT_INSTANCE.write( new java.io.DataOutputStream( output ), geometry0);
 *  
 *   // create a byte array input stream on the output stream
 *   java.io.ByteArrayInputStream input = new java.io.ByteArrayInputStream(output.toByteArray());
 *  
 *   // read a Geometry from the input stream
 *   Geometry2DAdapter geometry1 = WKBGeometryConverter.DEFAULT_INSTANCE.read( new java.io.DataInputStream( input ));
 *  
 *   // print the original and the deserialized object
 *   System.out.println(geometry0);
 *   System.out.println(geometry1);
 *
 *   // close the streams after use
 *   input.close();
 *   output.close();
 * </pre></code></p>
 * 
 * @see Geometry2DAdapter#toWKB()
 * @see Geometry2DFactory#createFromWKB(byte[], PrecisionModel)
 * @see DataInput
 * @see DataOutput
 * @see IOException
 */
public class WKBGeometryConverter extends Converter<Geometry2DAdapter> {

	/**
	 * This instance can be used for getting a default instance of an Geometry
	 * converter. It is similar to the <i>Singleton Design Pattern</i> (for
	 * further details see Creational Patterns, Prototype in <i>Design
	 * Patterns: Elements of Reusable Object-Oriented Software</i> by Erich
	 * Gamma, Richard Helm, Ralph Johnson, and John Vlissides) except that
	 * there are no mechanisms to avoid the creation of other instances of
	 * an Geometry converter.
	 */
	public static WKBGeometryConverter DEFAULT_INSTANCE = new WKBGeometryConverter();
		
	/**
	 *  Creates a new WKBGeometryConverter- instance	 
	 */
	private WKBGeometryConverter(){};
	
	/**
	 * Restores the <code>Geometry2DAdapter</code> object from its WKB- representation 
	 * from the stream.
	 * 
	 * <p>This implementation ignores the specified object and returns a new
	 * <code>Geometry2DAdapter</code> object. So it does not matter if the specified
	 * object is <code>null</code>.</p>
	 *
	 * @param dataInput the stream to read the <code>Geometry2DAdapter</code> object from 
	 * @param object the (<code>Geometry2DAdapter</code>) object to be restored. In this
	 *        implementation it is ignored.
	 * @return the read <code>Geometry2DAdapter</code> object.
	 * @throws IOException if I/O errors occur.
	 */
	@Override
	public Geometry2DAdapter read(DataInput dataInput, Geometry2DAdapter object) throws IOException {
		PrecisionModel p = PrecisionModelConverter.DEFAULT_INSTANCE.read(dataInput);
		int SRID = dataInput.readInt();
		byte[] wkb = new byte[dataInput.readInt()];
		dataInput.readFully(wkb);			
		object = Geometry2DFactory.createFromWKB(wkb, p);
		object.setSRID(SRID);
		return object;
	}

	/**
	 * Writes the WKB- representation of the given <code>Geometry2DAdapter</code> object
	 * to the specified data output.
	 * 
	 * @param dataOutput the stream to write the WKB- representation of the <code>Geometry2DAdapter</code> 
	 *        object to.
	 * @param object the <code>Geometry2DAdapter</code> object whose WKB- representation
	 *        should be written to the data output.
	 * @throws IOException includes any I/O exceptions that may occur.
	 */
	@Override
	public void write(DataOutput dataOutput, Geometry2DAdapter object) throws IOException {
		byte[] wkb = object.toWKB();
		PrecisionModelConverter.DEFAULT_INSTANCE.write(dataOutput, object.getPrecisionModel());
		dataOutput.writeInt(object.getSRID());
		dataOutput.writeInt(wkb.length);
		dataOutput.write(wkb);
	}
}
