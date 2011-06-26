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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.NoSuchElementException;

import xxl.core.cursors.Cursor;
import xxl.core.cursors.MetaDataCursor;
import xxl.core.cursors.mappers.Mapper;
import xxl.core.functions.Function;
import xxl.core.relational.tuples.ArrayTuple;
import xxl.core.relational.tuples.Tuple;
import xxl.core.xxlinq.usecases.ref.linq.column.XXLinqColumn;
import xxl.core.xxlinq.usecases.ref.linq.metadata.XXLinqMetadata;
import xxl.core.xxlinq.usecases.ref.linq.pred.XXLinqPredicate;


public abstract class XXLinqCursor implements MetaDataCursor<Tuple, XXLinqMetadata>,
		Iterable<Tuple> {

	//protected XXLinqMetadata metadata;
	protected Function<Tuple, Tuple> tupleMapper;
	protected Function<XXLinqMetadata, XXLinqMetadata> metaDataMapper;
	
	@Override
	public XXLinqMetadata getMetaData() {
		return this.metaDataMapper.invoke();
	}
	
	@Override
	public Iterator<Tuple> iterator() {
		return this;
	}
	
	public static <E> XXLinqCursor createXXLinqCursor(Iterable<E> iter, String tableName, String columnName) {
		Cursor<Tuple> mapper = new Mapper<E, Tuple>(ArrayTuple.FACTORY_METHOD,
				iter.iterator());
		return new XXLinqWrapperCursor(mapper, tableName, columnName);
	}
	
	public static <E> XXLinqCursor createXXLinqCursor(Cursor<E> iter) {
		Cursor<Tuple> mapper = new Mapper<E, Tuple>(ArrayTuple.FACTORY_METHOD,
				iter);
		return new XXLinqWrapperCursor(mapper);
	}

	public XXLinqCursor zip(XXLinqCursor aCursor) {
		return new XXLinqZipCursor(this, aCursor);
	}

	public XXLinqCursor select(XXLinqColumn ... xxLinqColumns) {
		return new XXLinqProjectionCursor(this, xxLinqColumns);
	}

	public XXLinqCursor where(XXLinqPredicate pred) {
		// TODO Auto-generated method stub
		return new XXLinqSelectionCursor(this, pred);
	}
}
