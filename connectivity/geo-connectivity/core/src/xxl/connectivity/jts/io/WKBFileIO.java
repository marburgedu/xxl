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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import xxl.connectivity.jts.Geometry2DAdapter;
import xxl.connectivity.jts.Geometry2DFactory;
import xxl.core.cursors.AbstractCursor;
import xxl.core.cursors.Cursor;
import xxl.core.spatial.geometries.Geometry2D;
import xxl.core.util.WrappingRuntimeException;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.InStream;
import com.vividsolutions.jts.io.InputStreamInStream;
import com.vividsolutions.jts.io.OutStream;
import com.vividsolutions.jts.io.OutputStreamOutStream;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKBWriter;


/** This class contains some usefull static methods to read geometries from a text file
 *  or to write geometries to a text file using their well-known- text representation.
 *  <b>Note</b>: 
 *  	<ul><li>the specified methods handle text files only!</li>
 *  		<li>the geometries' precision model is not preserved, that means
 *  			the wkt- representation does not contain any information on
 *  			the precision model of a geometry at all. The precision model that
 *  			the geometries should have can be specified in the <code>read</code> method.
 *  		</li>
 *  	</ul> 
 *  
 *  @see WKBFileIO
 */
public class WKBFileIO{
	
	/** There's no need to create an instance of this class! */
	private WKBFileIO(){};
	
	/** Returns a Cursor over a text file containing geometry objects in wkt- representation.
	 *  
	 * @param file the text file containing geometry objects
	 * @param bufferSize the buffer size used by the underlying buffered FileReader
	 * @param doublePrecision specifies the precision model of the input geometries  
	 * @return a Cursor over the file's objects
	 */
	public static Cursor<Geometry2D> read(final File file, final int bufferSize, final boolean doublePrecision){ 
		try {
			return new AbstractCursor<Geometry2D>(){
				
				private Geometry2DAdapter nextObject;
				private InputStream bufferedInputStream = 
					new BufferedInputStream(
						new FileInputStream(file), 
						bufferSize	
					);
 
				private InStream inStream = new InputStreamInStream( bufferedInputStream) ;					
				private WKBReader wkbReader = 
					new WKBReader( doublePrecision 
									? new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING))
									: new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING_SINGLE))
								);
				
				@Override
				protected boolean hasNextObject() {
					nextObject = null;
					try {		
						if(bufferedInputStream.available()>0)
							nextObject = Geometry2DFactory.wrap(wkbReader.read(inStream));
					} catch (Exception e) {	}				  
					return nextObject != null;
				}
	
				@Override
				protected Geometry2DAdapter nextObject() {	return nextObject;	}
				
				public void close(){
					try {
						bufferedInputStream.close();
					} catch (IOException e) {
						throw new WrappingRuntimeException(e);
					}
				}
			};
		} catch (FileNotFoundException e) {
			throw new WrappingRuntimeException(e);
		}
	}
	
	/** Returns a Cursor over a text file containing geometry objects in wkt- representation.
	 *  The required precision model of the input geometries is set to single precision.
	 * @param file the text file containing geometry objects
	 * @param bufferSize the buffer size used by the underlying buffered FileReader
	 * @return a Cursor over the file's objects
	 */
	public static Cursor<Geometry2D> read(File file, int bufferSize){
		return read(file, bufferSize, false);
	}
	
	/** Returns a Cursor over a text file containing geometry objects in wkt- representation.
	 *  The buffer size of the underlying buffered FileReader is set to 4 KB.
	 * @param file the text file containing geometry objects
	 * @param doublePrecision specifies the precision model of the input geometries  
	 * @return a Cursor over the file's objects
	 */
	public static Cursor<Geometry2D> read(File file, boolean doublePrecision){
		return read(file, 4096, doublePrecision);
	}

	/** Returns a Cursor over a text file containing geometry objects in wkt- representation.
	 *  The buffer size of the underlying buffered FileReader is set to 4 KB and the required
	 *  precision model of the input geometries is set to single precision.
	 * @param file the text file containing geometry objects
	 * @return a Cursor over the file's objects
	 */
	public static Cursor<Geometry2D> read(File file){
		return read(file, 4096, false);
	}
	
	/** Writes the geometrys given by the Geometry- Iterator to the specified text file, using
	 *  a buffered FileWriter.
	 * @param it the iterator over the output- geometries
	 * @param file the text file to store the geometries
	 * @param bufferSize the buffer size used by the buffered FileWriter
	 */
	public static void write(Iterator<Geometry2D> it,File file, int bufferSize){
		WKBWriter wkbWriter = new WKBWriter();
		try{
			BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file), bufferSize);
			OutStream outStream = new OutputStreamOutStream( bufferedOutputStream );
			while(it.hasNext())
				wkbWriter.write(((Geometry2DAdapter)it.next()).getJTSGeometry(), outStream);							
		} catch (IOException e){ throw new WrappingRuntimeException(e); }		
	};
	
	/** Writes the geometrys given by the Geometry- Iterator to the specified text file, using
	 *  a buffered FileWriter. The buffer size for this writer is set to 4 KB.
	 * @param it the iterator over the output- geometries
	 * @param file the text file to store the geometries
	 */
	public static void write(Iterator<Geometry2D> it,File file){
		write(it, file, 4096);
	}
	
}
