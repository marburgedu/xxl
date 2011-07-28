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

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

import xxl.core.cursors.Cursor;
import xxl.core.cursors.sources.io.FileInputCursor;
import xxl.core.io.converters.Converter;
import xxl.core.spatial.geometries.Geometry2D;
import xxl.core.spatial.rectangles.Rectangle;
import xxl.core.spatial.rectangles.Rectangles;
import xxl.core.util.WrappingRuntimeException;

/** Provides static methods to read geometries from a file or to write geometries 
 *  to a file using a specified converter.
 *  
 *  @see Geometry2DConverter
 */
public class Geometry2DFileIO{	
	
	/** There's no need to create an instance of this class! */
	private Geometry2DFileIO(){};
	
	/** Returns a Cursor over a file containing geometry objects. The given converter
	 *  defines, how the geometries should be restored 
	 *  
	 * @param converter the converter used to restore the geometry- objects   
	 * @param file the binary file containing geometry objects
	 * @param bufferSize the buffer size used by the underlying buffered FileInputCursor  
	 * @param <T> the type of the iterators' elements
	 * @return a Cursor over the file's objects
	 */
	public static <T extends Geometry2D> Cursor<Geometry2D> read(Converter<T> converter, File file, int bufferSize){ 
		return new FileInputCursor<Geometry2D>(converter, file, bufferSize);
	};

	/** Returns a Cursor over a file containing geometry objects. The given converter
	 *  defines, how the geometries should be restored 
	 *  
	 * @param converter the converter used to restore the geometry- objects
	 *  The buffer size of the underlying buffered FileInputCursor is set to 4 KB.
	 * @param file the binary file containing geometry objects
	 * @param <T> the type of the iterators' elements
	 * @return a Cursor over the file's objects
	 */
	public static<T extends Geometry2D> Cursor<Geometry2D> read(Converter<T> converter, File file){ 
		return read( converter, file, 4096);
	};
	
	
	/** Writes the geometrys given by the Geometry- Iterator to the specified file, using
	 *  a buffered FileOutputStream. The given converter defines the serialization of the objects. 
	 * @param converter the converter used to serialize the geometry- objects
	 * @param it the iterator over the output- geometries
	 * @param file the binary file to store the geometries
	 * @param bufferSize the buffer size used by the buffered OutputStream
	 * @param writeUniverseFile determines wheter the bounding-rectangle which surrounds the iterators' 
	 * 		  elements should be written to a *.universe file 
	 * @param <T> the type of the iterators' elements
	 *  
	 */
	public static <T extends Geometry2D> void write(Iterator<T> it, Converter<T> converter, File file, int bufferSize, boolean writeUniverseFile){
		try{
			Rectangle universe = null;
			DataOutputStream out = new DataOutputStream(
					new BufferedOutputStream(
						new FileOutputStream(file),
						bufferSize
						)
					);
			
				while(it.hasNext()){
					Geometry2D g = it.next();
					if( writeUniverseFile ){
						if(universe == null)
							universe = g.getMBR();
						else universe.union(g.getMBR());
					}
					converter.write(out, (T) g);
				}
															
				out.flush();
				out.close();
				if(writeUniverseFile)
					Rectangles.writeSingletonRectangle(new File(file.getAbsolutePath()+".universe"), universe);				
		} catch(IOException e){ 
			throw new WrappingRuntimeException(e); 
		}
	};
	
    /** Writes the geometrys given by the Geometry- Iterator to the specified file, using
	 *  a buffered FileOutputStream. The buffer size for this stream is set to 4 KB.
	 *  The given converter defines the serialization of the objects. 	 
	 *
	 * @param converter the converter used to serialize the geometry- objects
	 * @param it the iterator over the output- geometries
	 * @param file the binary file to store the geometries
	 * @param writeUniverseFile determines wheter the bounding-rectangle which surrounds the iterators' 
	 * 		  elements should be written to a *.universe file 
	 * @param <T> the type of the iterators' elements
	 */
	public static <T extends Geometry2D> void write(Iterator<T> it,Converter<T> converter, File file, boolean writeUniverseFile){
		write(it,converter, file, 4096, writeUniverseFile);
	}
	
	/** Writes the geometrys given by the Geometry- Iterator to the specified file, using
	 *  a buffered FileOutputStream. The given converter defines the serialization of the objects. 
	 *
	 * @param converter the converter used to serialize the geometry- objects
	 * @param it the iterator over the output- geometries
	 * @param file the binary file to store the geometries
	 * @param bufferSize the buffer size used by the buffered OutputStream
	 * @param <T> the type of the iterators' elements
	 *  
	 */
	public static <T extends Geometry2D> void write(Iterator<T> it,Converter<T> converter, File file, int bufferSize){
		write(it,converter, file, bufferSize, false);
	}

	/** Writes the geometrys given by the Geometry- Iterator to the specified file, using
	 *  a buffered FileOutputStream. This converter calls the main-converter with some common
	 *  default-values: the buffersize is set to 4KB and the universe-file will not be written
	 *
	 * @param converter the converter used to serialize the geometry- objects
	 * @param it the iterator over the output- geometries
	 * @param file the binary file to store the geometries
	 * @param <T> the type of the iterators' elements
	 *  
	 */
	public static <T extends Geometry2D> void write(Iterator<T> it,Converter<T> converter, File file){
		write(it,converter, file, 4096, false);
	}
	
}
