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

package xxl.core.xxlinq.usecases.ref.linq;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import xxl.core.functions.AbstractFunction;
import xxl.core.functions.Function;
import xxl.core.relational.metaData.ColumnMetaData;
import xxl.core.relational.tuples.ArrayTuple;
import xxl.core.relational.tuples.Tuple;
import xxl.core.xxlinq.usecases.ref.linq.column.XXLinqColumn;
import xxl.core.xxlinq.usecases.ref.linq.metadata.XXLinqMetadata;

public class ColumnMetadataUtils {

	public static ColumnMetaData create(final String tableName,
			final String columnName, final Class<?> cl) {

		return new ColumnMetaData() {

			@Override
			public boolean isWritable() throws SQLException {
				return false; // schreiben wollnwa nicht - oder?
			}

			@Override
			public boolean isSigned() throws SQLException {
				// was ist, wenn das garkeine zahlen sind?
				return true; // java kann ja nix anders..
			}

			@Override
			public boolean isSearchable() throws SQLException {
				return true; // grundsätzlich sollte erstmal alles in ner
								// where-clause gehen
				// TODO: was ist mit arrays?
			}

			@Override
			public boolean isReadOnly() throws SQLException {
				return true; // wir wollen nix überschreiben.
			}

			@Override
			public int isNullable() throws SQLException {
				return ResultSetMetaData.columnNullable; // null kann wohl mal
															// vorkommen, glaub
															// ich
			}

			@Override
			public boolean isDefinitelyWritable() throws SQLException {
				return false;
			}

			@Override
			public boolean isCurrency() throws SQLException {
				// TODO is this ever needed?
				return false;
			}

			@Override
			public boolean isCaseSensitive() throws SQLException {
				return true;
			}

			@Override
			public boolean isAutoIncrement() throws SQLException {
				return false;
			}

			@Override
			public String getTableName() throws SQLException {
				return tableName;
			}

			@Override
			public String getSchemaName() throws SQLException {
				return "";
			}

			@Override
			public int getScale() throws SQLException {
				// TODO is this ever needed?
				return 0;
			}

			@Override
			public int getPrecision() throws SQLException {
				// TODO is this ever needed? don't know how to calculate this
				// properly..
				return 0;
			}

			@Override
			public String getColumnTypeName() throws SQLException {
				// TODO does this matter?
				return cl.getSimpleName();
			}

			@Override
			public int getColumnType() throws SQLException {
				// TODO is this ever needed? if so, we need an appropriate
				// mapping to an sql-type
				// (have fun with that :-P)
				// we *will not* use this. do *not* use the mappings from
				// Types.java, because you'll
				// get an exception if you have any "fancy" type in this Column
				// (only ~20 basic types are supported)
				// return 0;
				throw new UnsupportedOperationException(
						"Don't use getColumnType(), it's impossible to"
								+ "support for non-basic types..");
			}

			@Override
			public String getColumnName() throws SQLException {
				return columnName;
			}

			@Override
			public String getColumnLabel() throws SQLException {
				return getColumnName();
			}

			@Override
			public int getColumnDisplaySize() throws SQLException {
				// TODO is this ever needed?
				return 128;
			}

			@Override
			public String getColumnClassName() throws SQLException {
				return cl.getName();
			}

			@Override
			public String getCatalogName() throws SQLException {
				// TODO what is this?
				return "";
			}
		};
	}

	public static Function<XXLinqMetadata, XXLinqMetadata> projectedMetaDataFactory(
			XXLinqMetadata metaData, final XXLinqColumn[] projectedColumns) {
		final Map<Integer, ColumnMetaData> columnMap = new HashMap<Integer, ColumnMetaData>();
		for (int i = 0; i < projectedColumns.length; i++) {
			projectedColumns[i].setMetaData(metaData);
			columnMap.put(i, metaData.getColumnMetaData(projectedColumns[i].getIndex()));
			System.out.println(columnMap);
		}
		Function<XXLinqMetadata, XXLinqMetadata> projectedMetaData = new AbstractFunction<XXLinqMetadata, XXLinqMetadata>() {
			@Override
			public XXLinqMetadata invoke() {
				return new XXLinqMetadata() {

					@Override
					public int getColumnCount() throws SQLException {
						return columnMap.size();
					}

					@Override
					public boolean isAutoIncrement(int column)
							throws SQLException {
						ColumnMetaData cmd = columnMap.get(column-1);
						return cmd.isAutoIncrement();
					}

					@Override
					public boolean isCaseSensitive(int column)
							throws SQLException {
						ColumnMetaData cmd = columnMap.get(column-1);
						return cmd.isCaseSensitive();
					}

					@Override
					public boolean isSearchable(int column) throws SQLException {
						ColumnMetaData cmd = columnMap.get(column-1);
						return cmd.isSearchable();
					}

					@Override
					public boolean isCurrency(int column) throws SQLException {
						ColumnMetaData cmd = columnMap.get(column-1);
						return cmd.isCurrency();
					}

					@Override
					public int isNullable(int column) throws SQLException {
						ColumnMetaData cmd = columnMap.get(column-1);
						return cmd.isNullable();
					}

					@Override
					public boolean isSigned(int column) throws SQLException {
						ColumnMetaData cmd = columnMap.get(column-1);
						return cmd.isSigned();
					}

					@Override
					public int getColumnDisplaySize(int column)
							throws SQLException {
						ColumnMetaData cmd = columnMap.get(column-1);
						return cmd.getColumnDisplaySize();
					}

					@Override
					public String getColumnLabel(int column)
							throws SQLException {
						ColumnMetaData cmd = columnMap.get(column-1);
						return cmd.getColumnLabel();
					}

					@Override
					public String getColumnName(int column) throws SQLException {
						ColumnMetaData cmd = columnMap.get(column-1);
						return cmd.getColumnName();
					}

					@Override
					public String getSchemaName(int column) throws SQLException {
						ColumnMetaData cmd = columnMap.get(column-1);
						return cmd.getSchemaName();
					}

					@Override
					public int getPrecision(int column) throws SQLException {
						ColumnMetaData cmd = columnMap.get(column-1);
						return cmd.getPrecision();
					}

					@Override
					public int getScale(int column) throws SQLException {
						ColumnMetaData cmd = columnMap.get(column-1);
						return cmd.getScale();
					}

					@Override
					public String getTableName(int column) throws SQLException {
						ColumnMetaData cmd = columnMap.get(column-1);
						return cmd.getTableName();
					}

					@Override
					public String getCatalogName(int column)
							throws SQLException {
						ColumnMetaData cmd = columnMap.get(column-1);
						return cmd.getCatalogName();
					}

					@Override
					public int getColumnType(int column) throws SQLException {
						ColumnMetaData cmd = columnMap.get(column-1);
						return cmd.getColumnType();
					}

					@Override
					public String getColumnTypeName(int column)
							throws SQLException {
						ColumnMetaData cmd = columnMap.get(column-1);
						return cmd.getColumnTypeName();
					}

					@Override
					public boolean isReadOnly(int column) throws SQLException {
						ColumnMetaData cmd = columnMap.get(column-1);
						return cmd.isReadOnly();
					}

					@Override
					public boolean isWritable(int column) throws SQLException {
						ColumnMetaData cmd = columnMap.get(column-1);
						return cmd.isWritable();
					}

					@Override
					public boolean isDefinitelyWritable(int column)
							throws SQLException {
						ColumnMetaData cmd = columnMap.get(column-1);
						return cmd.isDefinitelyWritable();
					}

					@Override
					public String getColumnClassName(int column)
							throws SQLException {
						ColumnMetaData cmd = columnMap.get(column-1);
						return cmd.getColumnClassName();
					}

					@Override
					public String toString() {
						return super.toString();
					}

					@Override
					public boolean equals(Object object) {
						// TODO Auto-generated method stub
						return super.equals(object);
					}

					@Override
					public int hashCode() {
						// TODO Auto-generated method stub
						return super.hashCode();
					}

					@Override
					public <T> T unwrap(Class<T> iface) throws SQLException {
						// TODO Auto-generated method stub
						return super.unwrap(iface);
					}

					@Override
					public boolean isWrapperFor(Class<?> iface)
							throws SQLException {
						// TODO Auto-generated method stub
						return super.isWrapperFor(iface);
					}

				};
			}
		};
		return projectedMetaData;
	}

	public static Function<XXLinqMetadata, XXLinqMetadata> wrapMetaDataFactory(
			final String tableName, final String columnName,
			final Class<?> wrappedType) {
		Function<XXLinqMetadata, XXLinqMetadata> wrapMetaData = new AbstractFunction<XXLinqMetadata, XXLinqMetadata>() {

			@Override
			public XXLinqMetadata invoke() {
				ColumnMetaData cmd = ColumnMetadataUtils.create(tableName,
						columnName, wrappedType);
				return new XXLinqMetadata(cmd);
			}

		};
		return wrapMetaData;
	}

	public static final Function<XXLinqMetadata, XXLinqMetadata> zipMetadataFactory(
			final XXLinqMetadata leftMetaData,
			final XXLinqMetadata rightMetaData) {
		Function<XXLinqMetadata, XXLinqMetadata> ZIP_METADATA = new AbstractFunction<XXLinqMetadata, XXLinqMetadata>() {

			@Override
			public XXLinqMetadata invoke() {
				try {
					int countLeft = leftMetaData.getColumnCount();
					int countRight = rightMetaData.getColumnCount();
					ColumnMetaData[] elems = null;

					elems = new ColumnMetaData[countLeft + countRight];

					for (int i = 1; i <= countLeft; i++) {
						elems[i - 1] = leftMetaData.getColumnMetaData(i);
					}
					for (int i = 1 ; i <= countRight; i++) {
						elems[i - 1 + countLeft] = rightMetaData.getColumnMetaData(i);
					}

					return new XXLinqMetadata(elems);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
			}
		};
		return ZIP_METADATA;
	}

	public static final Function<XXLinqMetadata, XXLinqMetadata> idMetadataFactory(
			final XXLinqMetadata xxLinqMetadata) {
		Function<XXLinqMetadata, XXLinqMetadata> ID_METADATA = new AbstractFunction<XXLinqMetadata, XXLinqMetadata>() {

			@Override
			public XXLinqMetadata invoke() {
				return xxLinqMetadata;
			}

		};
		return ID_METADATA;
	}
}
