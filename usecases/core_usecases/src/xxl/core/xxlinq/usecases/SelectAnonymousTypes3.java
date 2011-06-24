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
import static xxl.core.xxlinq.usecases.XXLinqDATA.PRODUCTS;
import xxl.core.xxlinq.columns.*;
import xxl.core.functions.Functional;
import xxl.core.relational.tuples.Tuple;
import xxl.core.xxlinq.AdvTupleCursor;
import xxl.core.xxlinq.columns.ConstructorColumn;
import xxl.core.xxlinq.usecases.XXLinqDATA.Product;


public class SelectAnonymousTypes3 extends XXLinqExample {

//	LINQ Example: http://msdn.microsoft.com/en-us/vcsharp/aa336758#SelectAnonymousTypes3

	@Override
	public void executeXXLinq() {
		System.out.println("Example 1:");
		 class Test{
			String productName, category;
			Double unitPrice;
			public Test() {
			}
			public Test(String productName, String category, Double unitPrice) {
				super();
				this.productName = productName;
				this.category = category;
				this.unitPrice = unitPrice;
			}
			public String toString(){
				return "(" + productName + ", " + category + ", " + unitPrice + ")";
			}
		}
		AdvTupleCursor tupleCursor = 
			new AdvTupleCursor(PRODUCTS, "products")
		.select(NewLINQFunctions.tupleFunction("ProductInfo", new Functional.UnaryFunction<Tuple,Test>(){

			@Override
			public Test invoke(Tuple arg) {
				Test temp = new Test();
				temp.productName = 	((Product) arg.getObject(1)).getProductName();
				temp.category = 	((Product) arg.getObject(1)).getCategory();
				temp.unitPrice = 	((Product) arg.getObject(1)).getPrice();
				
				return temp;
			}
		}));
		System.out.println(tupleCursor.getResultSetMetaData().getAlias());
		System.out.println(tupleCursor.getResultSetMetaData());

		for(Tuple obj : tupleCursor){
			System.out.println(obj);
		}
		//-------------------------------------------------------------	
		System.out.println("Example 2:");
		AdvTupleCursor tupleCursor2 = 
			new AdvTupleCursor(PRODUCTS, "products")
		.select(colNEW(TestClass.class,	colCAST(colOBJCALL(col("value"), "getProductName"),String.class),
									colOBJCALL(col("value"), "getCategory"),
									colOBJCALL(col("value"), "getPrice")
				));
		System.out.println(tupleCursor2.getResultSetMetaData().getAlias());
		System.out.println(tupleCursor2.getResultSetMetaData());
		for(Tuple obj : tupleCursor2){
			System.out.println(obj);
		}
		
	}

}
