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

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import xxl.core.relational.tuples.Tuple;
import xxl.core.xxlinq.usecases.ref.linq.column.XXLinqColumn;
import xxl.core.xxlinq.usecases.ref.linq.cursors.XXLinqCursor;

public class Main {

	/**
	 * @param args
	 * @throws SQLException 
	 */
	public static void main(String[] args) throws SQLException {
		List<String> numbers = Arrays.asList("eins","x","drei","x","fünf","x","sieben");
		List<String> aNumbers = Arrays.asList("eins","zwei","drei","vier","fünf","sechs","sieben");
		XXLinqCursor cursor = XXLinqCursor.createXXLinqCursor(numbers, "numbers1", "digits");
		XXLinqCursor aCursor = XXLinqCursor.createXXLinqCursor(aNumbers, "numbers2", "digits2");
		
		
		XXLinqCursor zipCursor = cursor.zip(aCursor)
		.select(new XXLinqColumn(1),new XXLinqColumn(1),new XXLinqColumn(1),new XXLinqColumn(2))
		//.select(new XXLinqColumn(1),new XXLinqColumn(2),new XXLinqColumn(1),new XXLinqColumn(2))
		//.where(new XXLinqColumn(1).EQ(new XXLinqColumn(3)))
		;
		
		System.out.println(zipCursor.getMetaData().getTableName(1));

		System.out.println(zipCursor.getMetaData());

		for(Tuple obj : zipCursor){
			System.out.println(obj);
		}
	}

}
