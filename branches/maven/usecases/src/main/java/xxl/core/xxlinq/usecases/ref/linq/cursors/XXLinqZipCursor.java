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

package xxl.core.xxlinq.usecases.ref.linq.cursors;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import xxl.core.cursors.Cursor;
import xxl.core.functions.Function;
import xxl.core.relational.tuples.ArrayTuple;
import xxl.core.relational.tuples.Tuple;
import xxl.core.xxlinq.usecases.ref.linq.ColumnMetadataUtils;
import xxl.core.xxlinq.usecases.ref.linq.TupleUtils;
import xxl.core.xxlinq.usecases.ref.linq.metadata.XXLinqMetadata;

public class XXLinqZipCursor extends XXLinqCursor {

	protected XXLinqCursor leftCursor;
	protected XXLinqCursor rightCursor;
	
	public XXLinqZipCursor(XXLinqCursor leftCursor, XXLinqCursor rightCursor) {
		this.leftCursor = leftCursor;
		this.rightCursor = rightCursor;
		this.tupleMapper = TupleUtils.ZIP_TUPLES;
		this.metaDataMapper = ColumnMetadataUtils.zipMetadataFactory(this.leftCursor.getMetaData(), this.rightCursor.getMetaData());
	}

	@Override
	public void open() {
		this.leftCursor.open();
		this.rightCursor.open();
	}

	@Override
	public void close() {
		this.leftCursor.close();
		this.rightCursor.close();

	}

	@Override
	public boolean hasNext() throws IllegalStateException {
		return this.leftCursor.hasNext() || this.rightCursor.hasNext();
	}

	@Override
	public Tuple next() throws IllegalStateException, NoSuchElementException {
		try {
			Tuple leftTuple;
			Tuple rightTuple;
			leftTuple = leftCursor.hasNext() ? leftCursor.next() : new ArrayTuple(new Object[leftCursor.getMetaData().getColumnCount()]);
			rightTuple = rightCursor.hasNext() ? rightCursor.next() : new ArrayTuple(new Object[rightCursor.getMetaData().getColumnCount()]);
		
		return tupleMapper.invoke(leftTuple, rightTuple);
		} catch (SQLException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public Tuple peek() throws IllegalStateException, NoSuchElementException,
			UnsupportedOperationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean supportsPeek() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void remove() throws IllegalStateException,
			UnsupportedOperationException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean supportsRemove() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void update(Tuple object) throws IllegalStateException,
			UnsupportedOperationException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean supportsUpdate() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void reset() throws UnsupportedOperationException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean supportsReset() {
		// TODO Auto-generated method stub
		return false;
	}

	
}
