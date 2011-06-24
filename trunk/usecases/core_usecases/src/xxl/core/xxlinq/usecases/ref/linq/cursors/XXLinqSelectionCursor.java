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

import xxl.core.relational.tuples.Tuple;
import xxl.core.xxlinq.usecases.ref.linq.ColumnMetadataUtils;
import xxl.core.xxlinq.usecases.ref.linq.pred.XXLinqPredicate;

public class XXLinqSelectionCursor extends XXLinqCursor {

	protected XXLinqPredicate predicate;
	protected XXLinqCursor parentCursor;
	private Tuple computedNext;
	private boolean computedHasNext;
	
	public XXLinqSelectionCursor(XXLinqCursor xxLinqCursor, XXLinqPredicate predicate) {
		this.computedHasNext = false;
		this.parentCursor = xxLinqCursor;
		this.predicate = predicate;
		this.metaDataMapper = ColumnMetadataUtils.idMetadataFactory(xxLinqCursor.getMetaData());
		this.predicate.setMetaData(xxLinqCursor.getMetaData());
	}

	@Override
	public void open() {
		// TODO Auto-generated method stub

	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean hasNext() throws IllegalStateException {
		if(computedHasNext){
			return true;
		}
		if(parentCursor.hasNext()){
			Tuple tempTuple = parentCursor.next();
			if(predicate.invoke(tempTuple)){
				computedNext = tempTuple;
				computedHasNext = true;
				return true;
			}else{
				return hasNext();
			}
		}else{
			return false;
		}
	}

	@Override
	public Tuple next() throws IllegalStateException, NoSuchElementException {
		if(!computedHasNext){
			hasNext();
		}
		if(computedHasNext){
			computedHasNext = false;
			return computedNext;
		}else throw new NoSuchElementException();
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
