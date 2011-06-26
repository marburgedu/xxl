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

package xxl.core.xxlinq.usecases.ref.linq;

import java.util.List;

import xxl.core.functions.AbstractFunction;
import xxl.core.functions.Function;
import xxl.core.relational.metaData.ColumnMetaData;
import xxl.core.relational.tuples.ArrayTuple;
import xxl.core.relational.tuples.Tuple;
import xxl.core.xxlinq.usecases.ref.linq.column.XXLinqColumn;

public class TupleUtils {

	public static final Function<Tuple, Tuple> ZIP_TUPLES = new AbstractFunction<Tuple, Tuple>() {
		
		@Override
		public Tuple invoke(Tuple leftTuple, Tuple rightTuple) {
			
			Object[] leftArray 	= leftTuple.toArray(); 
			Object[] rightArray = rightTuple.toArray(); 
			
			Object[] elems = new Object[leftArray.length + rightArray.length];
			
			System.arraycopy(leftArray,  0, elems,               0,  leftArray.length);
			System.arraycopy(rightArray, 0, elems,leftArray.length, rightArray.length);
			return new ArrayTuple(elems);
		}

		
	};
	
	public static final Function<Tuple, Tuple> ID = new AbstractFunction<Tuple, Tuple>(){

		@Override
		public Tuple invoke(Tuple argument) {
			return argument;
		}
	};
	public static final Function<Tuple, Tuple> projectionFactory(final XXLinqColumn[] projectedColumns){
	
		Function<Tuple, Tuple> mapping = new AbstractFunction<Tuple,Tuple>(){
		
		@Override
		public Tuple invoke(Tuple tuple) {
			Object[] elems = new Object[projectedColumns.length];
			for(int i = 0; i < projectedColumns.length; i++){
				elems[i] = projectedColumns[i].invoke(tuple);
			}
			return new ArrayTuple(elems);
		}
	};
	return mapping;
}
}
