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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import xxl.connectivity.jts.Geometry2DAdapter;
import xxl.core.io.converters.Converter;
import xxl.core.util.WrappingRuntimeException;

/**
 * A converter that is able to read and write <code>Geometry2DAdapter</code> 
 * objects using the Java object- serialization mechanism. The use of this 
 * converter is discouraged because of the weak performance of the default 
 * serializer.  
 * 
 * <p>Example usage (1).
 * <code><pre>
 *   // create a byte array output stream
 *   java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
 *   // create a Geometry
 *   xxl.connectivity.jts.Point2DAdapter geometry0 = xxl.connectivity.jts.Geometry2DFactory.createPoint(5,5);
 *          
 *   // serialize the Geometry object to the given outputstream
 *   SerializableGeometryConverter.DEFAULT_INSTANCE.write( new java.io.DataOutputStream( output ), geometry0);
 *  
 *   // create a byte array input stream on the output stream
 *   java.io.ByteArrayInputStream input = new java.io.ByteArrayInputStream(output.toByteArray());
 *  
 *   // read a Geometry from the input stream
 *   Geometry2DAdapter geometry1 = SerializableGeometryConverter.DEFAULT_INSTANCE.read( new java.io.DataInputStream( input ));
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
 * @see DataInput
 * @see DataOutput
 * @see IOException
 */
public class SerializableGeometryConverter extends Converter<Geometry2DAdapter>{
	
	/**
	 * This instance can be used for getting a default instance of an Geometry
	 * converter. It is similar to the <i>Singleton Design Pattern</i> (for
	 * further details see Creational Patterns, Prototype in <i>Design
	 * Patterns: Elements of Reusable Object-Oriented Software</i> by Erich
	 * Gamma, Richard Helm, Ralph Johnson, and John Vlissides) except that
	 * there are no mechanisms to avoid the creation of other instances of
	 * an Geometry converter.
	 */
	public static SerializableGeometryConverter DEFAULT_INSTANCE = new SerializableGeometryConverter();

	/**
	 *  Creates a new SerializableGeometryConverter- instance	 
	 */
	private SerializableGeometryConverter(){};

	@Override
	public Geometry2DAdapter read(DataInput dataInput, Geometry2DAdapter object) throws IOException {
		int size = dataInput.readInt();
		byte[] data = new byte[size];
		
		dataInput.readFully(data); 
	
		ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(data));		
		try {
			object = (Geometry2DAdapter) in.readObject();				 						
			return object;
		} catch (ClassNotFoundException e) {
			throw new WrappingRuntimeException(e);
		}					
	}

	@Override
	public void write(DataOutput dataOutput, Geometry2DAdapter object) throws IOException {
		ByteArrayOutputStream data = new ByteArrayOutputStream();
		ObjectOutputStream os = new ObjectOutputStream(data);
		os.writeObject(object);
		os.flush();
		os.close();

		dataOutput.writeInt(data.size());
		dataOutput.write(data.toByteArray());		
	}	
}
