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

import xxl.core.relational.tuples.Tuple;
import xxl.core.xxlinq.AdvTupleCursor;
import xxl.core.xxlinq.usecases.ref.NewSelect;

public abstract class XXLinqExample {

	public abstract void executeXXLinq();
	
	public void printExample(AdvTupleCursor tupleCursor){
		System.out.println(tupleCursor.getResultSetMetaData().getAlias());
		System.out.println(tupleCursor.getResultSetMetaData());

		for(Tuple obj : tupleCursor){
			System.out.println(obj);
		}
	}
	public static void main(String[] args) {
		//WHERE
		new WhereSimple1().executeXXLinq();
		//new WhereSimple2().executeXXLinq();
		//new WhereSimple3().executeXXLinq();
		//new WhereDrillDown().executeXXLinq();
		//new WhereIndexed().executeXXLinq();
		//SELECT
		//new SelectSimple1().executeXXLinq();
		//new SelectSimple2().executeXXLinq();
		//new SelectTransformation().executeXXLinq();
		//new SelectAnonymousTypes1().executeXXLinq();
		//new SelectAnonymousTypes2().executeXXLinq();
		//new SelectAnonymousTypes3().executeXXLinq();
		//new SelectIndexed().executeXXLinq();
		//new SelectFiltered().executeXXLinq();
		//new SelectMany1().executeXXLinq();
		//new SelectMany2().executeXXLinq();
		//new SelectMany3().executeXXLinq();
		
		
		//new NewSelect().executeXXLinq();
	}
}
