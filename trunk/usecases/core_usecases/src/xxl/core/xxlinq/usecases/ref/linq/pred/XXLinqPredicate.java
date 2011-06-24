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

package xxl.core.xxlinq.usecases.ref.linq.pred;

import java.util.List;

import xxl.core.functions.Function;
import xxl.core.relational.tuples.Tuple;
import xxl.core.xxlinq.usecases.ref.linq.column.XXLinqColumn;
import xxl.core.xxlinq.usecases.ref.linq.metadata.XXLinqMetadata;

public abstract class XXLinqPredicate implements Function<Tuple, Boolean>{

	@Override
	public Boolean invoke(List<? extends Tuple> arguments) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean invoke() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean invoke(Tuple argument) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean invoke(Tuple argument0, Tuple argument1) {
		// TODO Auto-generated method stub
		return null;
	}

	public void setMetaData(XXLinqMetadata metaData) {
		// TODO Auto-generated method stub
		
	}

	
}
