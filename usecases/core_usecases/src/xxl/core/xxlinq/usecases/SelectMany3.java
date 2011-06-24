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
import static xxl.core.xxlinq.columns.ColumnUtils.colCAST;
import static xxl.core.xxlinq.columns.ColumnUtils.colOBJCALL;
import static xxl.core.xxlinq.columns.ColumnUtils.val;
import static xxl.core.xxlinq.usecases.XXLinqDATA.CUSTOMERS;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

import xxl.core.functions.Functional;
import xxl.core.relational.tuples.Tuple;
import xxl.core.xxlinq.AdvTupleCursor;
import xxl.core.xxlinq.usecases.XXLinqDATA.Customer;
import xxl.core.xxlinq.usecases.XXLinqDATA.Order;
import xxl.core.xxlinq.usecases.XXLinqDATA.Product;

public class SelectMany3 extends XXLinqExample {
//LINQ Example: http://msdn.microsoft.com/en-us/vcsharp/aa336758#SelectManyCompoundfrom3

	@Override
	public void executeXXLinq() {
		class Test{
			int id, orderID;
			Date date;
			public Test(int id, int orderID, Date date) {
				super();
				this.id = id;
				this.orderID = orderID;
				this.date = date;
			}

			public Test() {
			}
			
			public String toString(){
				return "(" + id + ", " + orderID + ", " + date + ")";
			}
		}
		try {
			AdvTupleCursor tupleCursor = 
				new AdvTupleCursor(CUSTOMERS, "customers")
			.select(col("value"), colOBJCALL("orders", col("value"), "getOrders"))
			.expand(col("orders"))
			.select(col("value"),col("expand"))
			.where(colOBJCALL("total",  colCAST(col("expand"), Order.class), "getDate").LEQ(val(DateFormat.getDateInstance(DateFormat.SHORT, Locale.GERMAN).parse("06.06.1999"))))
			.select(NewLINQFunctions.tupleFunction("orderInfo", new Functional.UnaryFunction<Tuple,Test>(){

			@Override
			public Test invoke(Tuple arg) {
				Test temp = new Test();
				temp.id = 	((Customer) arg.getObject(1)).getId();
				temp.orderID = 	((Order) arg.getObject(2)).getOrderID();
				temp.date = 	((Order) arg.getObject(2)).getDate();
				
				return temp;
			}
		}));
			;
			
			printExample(tupleCursor);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
