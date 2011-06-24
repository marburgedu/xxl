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

package xxl.core.xxlinq.usecases.ref.linq.column;

import java.sql.SQLException;
import java.util.List;

import xxl.core.functions.Function;
import xxl.core.relational.metaData.ColumnMetaData;
import xxl.core.relational.tuples.Tuple;
import xxl.core.util.metaData.MetaDataProvider;
import xxl.core.xxlinq.usecases.ref.linq.cursors.XXLinqCursor;
import xxl.core.xxlinq.usecases.ref.linq.metadata.XXLinqMetadata;
import xxl.core.xxlinq.usecases.ref.linq.pred.XXLinqEqualsPredicate;
import xxl.core.xxlinq.usecases.ref.linq.pred.XXLinqPredicate;

public class XXLinqColumn implements Function<Tuple, Object>, MetaDataProvider<ColumnMetaData> {

	private int index;
	
	private ColumnMetaData columnMetaData;
	
	public XXLinqColumn(int index) {
		this.setIndex(index);
	}

	@Override
	public Object invoke(List<? extends Tuple> arguments) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object invoke() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object invoke(Tuple argument) {
		return argument.getObject(getIndex());
	}

	@Override
	public Object invoke(Tuple argument0, Tuple argument1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ColumnMetaData getMetaData() {
		// TODO Auto-generated method stub
		return this.getColumnMetaData();
	}

	public void setMetaData(XXLinqMetadata metadata) {
		System.out.println(this.getIndex());
		this.setColumnMetaData(metadata.getColumnMetaData(this.getIndex()));
	}

	public XXLinqPredicate EQ(XXLinqColumn xxLinqColumn) {
		return new XXLinqEqualsPredicate(this, xxLinqColumn);
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getIndex() {
		return index;
	}

	public void setColumnMetaData(ColumnMetaData columnMetaData) {
		this.columnMetaData = columnMetaData;
	}

	public ColumnMetaData getColumnMetaData() {
		return columnMetaData;
	}

}
