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

package xxl.core.xxlinq.usecases;

import static xxl.core.xxlinq.columns.ColumnUtils.*;
import java.util.Arrays;
import java.util.List;

import xxl.core.functions.Functional;
import xxl.core.relational.tuples.Tuple;
import xxl.core.xxlinq.AdvTupleCursor;

public class SelectIndexed extends XXLinqExample {

//	LINQ Example: http://msdn.microsoft.com/en-us/vcsharp/aa336758#SelectIndexed

	@Override
	public void executeXXLinq() {
		 final class Pair<X,Y> {
			  public X num;
			  public Y inPlace;
			  public String toString() {
				return "(" + num + ", " + inPlace + ")";
			}
			}
		List<Integer> numbers = Arrays.asList(5,4,1,3,9,8,6,7,2,0);
		AdvTupleCursor tupleCursor = 
			new AdvTupleCursor( numbers.iterator(), "numbers")
		.select(indexCol("index"), col("value"))
		.select(NewLINQFunctions.tupleFunction("NumInPLace", new Functional.UnaryFunction<Tuple,Pair<Integer,Boolean>>(){

			@Override
			public Pair<Integer, Boolean> invoke(Tuple tuple) {
				Pair<Integer,Boolean> ret = new Pair<Integer,Boolean>();
				ret.num = (Integer) tuple.getObject(2);
				ret.inPlace = ret.num.longValue() == (Long)tuple.getObject(1);
				return ret;
			}}));

		printExample(tupleCursor);
	}

}
