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

import static xxl.core.xxlinq.columns.ColumnUtils.col;
import static xxl.core.xxlinq.columns.ColumnUtils.colOBJCALL;
import static xxl.core.xxlinq.columns.ColumnUtils.val;
import static xxl.core.xxlinq.usecases.XXLinqDATA.PRODUCTS;
import xxl.core.relational.tuples.Tuple;
import xxl.core.xxlinq.AdvTupleCursor;

public class WhereSimple3 extends XXLinqExample {

//  LINQ Example: http://msdn.microsoft.com/en-us/vcsharp/aa336760#WhereSimple3

	@Override
	public void executeXXLinq() {
		// products in stock and price > 100
		AdvTupleCursor tupleCursor = 
			new AdvTupleCursor(PRODUCTS, "products").
				where(colOBJCALL("stock", col("value"), "getNumberInStock").GT(val(0)).
						AND(colOBJCALL("price", col("value"), "getPrice").GEQ(val(new Double(100)))));
		for(Tuple obj : tupleCursor){
			System.out.println(obj);
		}
	}

}
