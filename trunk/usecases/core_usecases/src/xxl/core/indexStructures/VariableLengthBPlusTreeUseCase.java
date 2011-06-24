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
package xxl.core.indexStructures;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import xxl.core.collections.containers.Container;
import xxl.core.collections.containers.io.BlockFileContainer;
import xxl.core.collections.containers.io.BufferedContainer;
import xxl.core.collections.containers.io.ConverterContainer;
import xxl.core.cursors.Cursor;
import xxl.core.functions.AbstractFunction;
import xxl.core.functions.Function;
import xxl.core.indexStructures.keyRanges.StringKeyRange;
import xxl.core.indexStructures.separators.StringSeparator;
import xxl.core.indexStructures.testData.Student;
import xxl.core.indexStructures.vLengthBPlusTree.VariableLengthBPlusTree;
import xxl.core.indexStructures.vLengthBPlusTree.VariableLengthBPlusTree.IndexEntry;
import xxl.core.indexStructures.vLengthBPlusTree.splitStrategy.SimplePrefixBPlusTreeSplit;
import xxl.core.indexStructures.vLengthBPlusTree.underflowHandlers.StandardUnderflowHandler;
import xxl.core.io.LRUBuffer;
import xxl.core.io.converters.Converters;
import xxl.core.io.converters.IntegerConverter;
import xxl.core.io.converters.LongConverter;
import xxl.core.io.converters.MeasuredConverter;
import xxl.core.io.converters.StringConverter;
/**
 * This class shows how to use VariableLengthBPlusTree. 
 * First we load Student data, which is indexed on string value ( @see {@link Student}). 
 * And store Index meta data i order to reuse the index structure.
 * Second we show how to remove and update date. 
 * Also in the last step it will described how to run queiries
 */
public class VariableLengthBPlusTreeUseCase {
	// block size of the underlined container 
	// nodes are mapped to the blocks(pages) 
	public static final int BLOCK_SIZE = 2048;
	// minimal capacity as a fraction of bytes 
	// needed to compute appropriate split
	public static final double MIN_RATIO = 0.4;
	// size of LRU Buffer
	public static final int BUFFER_SIZE = 20;
	// number of elements which we are want to insert
	public static final int NUMBER_OF_ELEMENTS = 100000;
	//
	public static final String path ="vlBplus";
	/**
	 * In order to initialize a tree we need to provide converter for the data with a miximal size in bytes;
	 * We assume that both values name and info of the student is bounded by 50 Bytes 
	 * 
	 * 
	 */
	public static final MeasuredConverter<Student> dataMeasuredConverter = new MeasuredConverter<Student>(){

		@Override
		public int getMaxObjectSize() {
			// 50 bytes for string name
			// 50 bytes for info
			return 50 + 50 +4;
		}

		@Override
		public Student read(DataInput dataInput, Student object)
				throws IOException {
			return Student.DEFAULT_CONVERTER.read(dataInput, object);
		}

		@Override
		public void write(DataOutput dataOutput, Student object)
				throws IOException {
			Student.DEFAULT_CONVERTER.write(dataOutput, object);
		}
	};
	
	/**
	 * We want to index student on their name values. That means we have also variable length keys.
	 * 
	 */
	public static final MeasuredConverter<String> keyConverter = new MeasuredConverter<String>(){

		@Override
		public int getMaxObjectSize() {
			// 50 bytes for string 
			return  50;
		}

		@Override
		public String read(DataInput dataInput, String object)
				throws IOException {
			return StringConverter.DEFAULT_INSTANCE.read(dataInput, object);
		}

		@Override
		public void write(DataOutput dataOutput, String object)
				throws IOException {
			StringConverter.DEFAULT_INSTANCE.write(dataOutput, object);
		}
	};
	/**
	 * this function is used to compute actual serialized size of the data 
	 */
	public static final Function<Object, Integer> getDataSize = new AbstractFunction<Object , Integer>() {
		
		public Integer invoke(Object arg){
			//cast to student
			Student std = (Student)arg;
			int nameSize =  Converters.sizeOf(StringConverter.DEFAULT_INSTANCE, std.getName());
			int infoSize =  Converters.sizeOf(StringConverter.DEFAULT_INSTANCE, std.getInfo());
			return nameSize +infoSize + 4; 
		}
	};
	/**
	 * this function is used to compute actual serialized size of the key
	 */
	public static final Function<Object, Integer> getKeySize = new AbstractFunction<Object , Integer>() {
		
		public Integer invoke(Object arg){
			//cast to string
			String std = (String)arg;
			int nameSize =  Converters.sizeOf(StringConverter.DEFAULT_INSTANCE, std);
			return nameSize; 
		}
	};
	/**
	 * this function is used for mapping the student to its string key 
	 */
	public static final Function<Student, String> getKeyFunction = new AbstractFunction<Student, String>() {
		
		public String invoke(Student st){
			return st.getName();
		}
	};
	
	/**
	 * method saves meta info about the tree. 
	 * this information we need for restoring the tree   
	 * @param btree
	 * @param path
	 * @throws IOException
	 */
	protected static void saveTree(VariableLengthBPlusTree btree, String path) throws IOException{
		DataOutputStream out = new DataOutputStream(new FileOutputStream(new File(path)));
		IndexEntry entry = (IndexEntry) btree.rootEntry();
		StringKeyRange range = (StringKeyRange) btree.rootDescriptor();
		// store root entry
		// 1. id -> longs
		LongConverter.DEFAULT_INSTANCE.write(out, (Long)entry.id());
		// 2. level 
		IntegerConverter.DEFAULT_INSTANCE.write(out, entry.parentLevel());
		// 3. key of the root
		StringConverter.DEFAULT_INSTANCE.write(out, (String)entry.separator().sepValue());
		// store root descriptor which is StringKeyRange
		StringConverter.DEFAULT_INSTANCE.write(out, (String)range.minBound());
		StringConverter.DEFAULT_INSTANCE.write(out, (String)range.maxBound());
		out.close();
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		/**
		 * prepare test data
		 */
		List<Integer> stds = new ArrayList<Integer>(NUMBER_OF_ELEMENTS);
		for(int i = 0; i < NUMBER_OF_ELEMENTS; i++){
			stds.add(i);
		}
		Collections.shuffle(stds, new Random(42));
		/**
		 * 1. initialize tree and load test data
		 */
		// at this step the blockfileContainer is initialized 
		// the state of the container is lost if call  BlockFileContainer(path, BLOCK_SIZE)
		// and provide BLOCK_SIZE information
		VariableLengthBPlusTree tree = new VariableLengthBPlusTree(BLOCK_SIZE, MIN_RATIO , false);
		Container fileContainer = new BlockFileContainer(path, BLOCK_SIZE);
		Container bufferContainer = new BufferedContainer(fileContainer, new LRUBuffer(BUFFER_SIZE));
		// here we need to provide a converter for the nodes 
		Container converterContainer = new ConverterContainer(bufferContainer, tree.nodeConverter());
		// now we initialize tree 
		// so we use standard overflow handler and simple prefix split for string keys 
		// the split index is searched in the interval [0.4B , (1-0.4)B] B is in bytes 
		tree.initialize(null, // <- null because we have no meta data
				null, // <- 
				getKeyFunction, 
				converterContainer,
				keyConverter, 
				dataMeasuredConverter,
				StringSeparator.FACTORY_FUNCTION,
				StringKeyRange.FACTORY_FUNCTION,
				getKeySize,
				getDataSize, 
				new SimplePrefixBPlusTreeSplit(),
				new StandardUnderflowHandler()
		);
		System.out.println("insert data\n");
		// load data tuple by tuple
		int k = 0;
		for(Integer i : stds){
			tree.insert(new Student("name_" + i, i , "info_"+i));
			k++;
			if (k % 5000 == 0)
				System.out.print(".");
		}
		System.out.println();
		// flush containers before we can store meta info  
		converterContainer.flush();
		converterContainer.close();
		// save tree state info  
		// we need to store the id of the root entry its level and key 
		// also we store KeyRange of the tree
		saveTree(tree, path+"_metadata.dat");
		/**
		 * 2. reload tree 
		 */
		System.out.println("reload tree");
		tree = new VariableLengthBPlusTree(BLOCK_SIZE, MIN_RATIO , false);
		// now reeds the blockfilecontainer its state  
		fileContainer = new BlockFileContainer(path);
		bufferContainer = new BufferedContainer(fileContainer, new LRUBuffer(BUFFER_SIZE));
		converterContainer = new ConverterContainer(bufferContainer, tree.nodeConverter());
		// now we read state information about the root entry and 
		DataInputStream in = new DataInputStream( new FileInputStream(new File(path+"_metadata.dat")));
		// read and initialize 
		Long id = LongConverter.DEFAULT_INSTANCE.read(in);
		int level = IntegerConverter.DEFAULT_INSTANCE.readInt(in);
		String key = StringConverter.DEFAULT_INSTANCE.read(in);
		String minKey = StringConverter.DEFAULT_INSTANCE.read(in);
		String maxKey = StringConverter.DEFAULT_INSTANCE.read(in);
		IndexEntry rootEntry = ((IndexEntry)tree.createIndexEntry(level)).initialize(id, new StringSeparator(key));
		StringKeyRange rootDescriptor = new StringKeyRange(minKey, maxKey);		
		in.close();
		tree.initialize(rootEntry, // <- the restored rootEntry 
				rootDescriptor, // <- restored key range
				getKeyFunction, 
				converterContainer, // <- pass file container 
				keyConverter, 
				dataMeasuredConverter,
				StringSeparator.FACTORY_FUNCTION,
				StringKeyRange.FACTORY_FUNCTION,
				getKeySize,
				getDataSize, 
				new SimplePrefixBPlusTreeSplit(),
				new StandardUnderflowHandler()
		);
		// we can now remove some entries 
		Student st = (Student)tree.remove(new Student("name_0", 0, "info_0"));
		System.out.println("Object deleted: " + st);
		// or we can update data
		tree.update(new Student("name_99", 99, "info_99"),
				new Student("name_99", -10, "info_123456789"));
		// 
		/**
		 * query tree
		 */
		// exact match query 
		st = (Student)tree.exactMatchQuery("name_99");
		System.out.println("query with key name_99 :" + st);
		// range query 
		System.out.println("query with keys [name_100, name_2]    :");
		Cursor c =  tree.rangeQuery("name_19", "name_2");
		while(c.hasNext()){
			Student student = (Student)c.next();
			System.out.println(student);
		}
		// in order to get all students data 
		// just call tree.query() method
		
	}

}
