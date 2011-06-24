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

import java.util.NoSuchElementException;

import xxl.core.cursors.Cursor;
import xxl.core.relational.metaData.ColumnMetaData;
import xxl.core.relational.tuples.Tuple;
import xxl.core.xxlinq.usecases.ref.linq.ColumnMetadataUtils;
import xxl.core.xxlinq.usecases.ref.linq.TupleUtils;
import xxl.core.xxlinq.usecases.ref.linq.metadata.XXLinqMetadata;

public class XXLinqWrapperCursor extends XXLinqCursor {

	protected Cursor<Tuple> wrappedCursor;

	public XXLinqWrapperCursor(Cursor<Tuple> cursor, String tableName,
			String columnName) {
		this.wrappedCursor = cursor;
		this.tupleMapper = TupleUtils.ID;
		this.metaDataMapper = ColumnMetadataUtils.wrapMetaDataFactory(tableName, columnName, getWrappedType());
	}

	private Class<?> getWrappedType() {

		Tuple tuple = this.wrappedCursor.peek();
		Object wrappedObject = tuple.getObject(1);

		return wrappedObject == null ? Object.class : wrappedObject.getClass();
	}

	public XXLinqWrapperCursor(Cursor<Tuple> cursor) {
		this(cursor, "javaiterator", "value");
	}

	@Override
	public void close() {
		this.wrappedCursor.close();
	}

	@Override
	public boolean hasNext() throws IllegalStateException {
		return this.wrappedCursor.hasNext();
	}

	@Override
	public Tuple next() throws IllegalStateException, NoSuchElementException {
		return this.wrappedCursor.next();
	}

	@Override
	public void open() {
		this.wrappedCursor.open();
	}

	@Override
	public Tuple peek() throws IllegalStateException, NoSuchElementException,
			UnsupportedOperationException {
		return this.wrappedCursor.peek();
	}

	@Override
	public void remove() throws IllegalStateException,
			UnsupportedOperationException {
		this.wrappedCursor.remove();
	}

	@Override
	public void reset() throws UnsupportedOperationException {
		this.wrappedCursor.reset();
	}

	@Override
	public boolean supportsPeek() {
		return this.wrappedCursor.supportsPeek();
	}

	@Override
	public boolean supportsRemove() {
		return this.wrappedCursor.supportsRemove();
	}

	@Override
	public boolean supportsReset() {
		return this.wrappedCursor.supportsReset();
	}

	@Override
	public boolean supportsUpdate() {
		return this.wrappedCursor.supportsUpdate();
	}

	@Override
	public void update(Tuple object) throws IllegalStateException,
			UnsupportedOperationException {
		// TODO Auto-generated method stub

	}
}
