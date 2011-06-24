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

package xxl.core.relational;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;

import xxl.core.collections.queues.ListQueue;
import xxl.core.collections.queues.Queue;
import xxl.core.collections.queues.io.RandomAccessFileQueue;
import xxl.core.cursors.MetaDataCursor;
import xxl.core.functions.AbstractFunction;
import xxl.core.functions.Function;
import xxl.core.io.IOCounter;
import xxl.core.io.converters.ConvertableConverter;
import xxl.core.io.converters.Converters;
import xxl.core.predicates.FeaturePredicate;
import xxl.core.predicates.Predicate;
import xxl.core.relational.cursors.MergeSorter;
import xxl.core.relational.cursors.Orenstein;
import xxl.core.relational.cursors.ResultSetMetaDataCursor;
import xxl.core.relational.cursors.SortMergeJoin.Type;
import xxl.core.relational.resultSets.MetaDataCursorResultSet;
import xxl.core.relational.resultSets.VirtualTable;
import xxl.core.relational.tuples.ArrayTuple;
import xxl.core.relational.tuples.Tuple;
import xxl.core.relational.tuples.TupleConverter;
import xxl.core.relational.tuples.Tuples;
import xxl.core.spatial.KPEzCode;
import xxl.core.spatial.cursors.PointInputCursor;
import xxl.core.spatial.points.FloatPoint;
import xxl.core.spatial.predicates.DistanceWithinMaximum;
import xxl.core.util.BitSet;
import xxl.core.util.WrappingRuntimeException;
import xxl.core.util.metaData.CompositeMetaData;

/**
 * This class demonstrates the usage of the library XXL with a special
 * focus on the integration of similarity join algorithms into
 * the database management system <b>Cloudscape</b>.<br>
 * <tt>Cloudscape</tt> offers the possibility of accessing so called
 * virtual tables by providing a special interface, named VTI
 * (Virtual Table Interface). <br>
 * XXL provides with its package {@link xxl.core.relational} wrapper classes
 * which are able to wrap a {@link java.sql.ResultSet} to a
 * {@link xxl.core.cursors.MetaDataCursor} and vice versa. The class
 * {@link xxl.core.relational.resultSets.VirtualTable} wraps a ResultSet and
 * therefore it can be used in SQL queries like a usual table.
 * <p>
 * The main method represents a minimal JDBC application showing
 * JDBC access to Cloudscape.
 * It provides methods to get a connection to Cloudscape,
 * to execute querys, to close the open connection and
 * to shutdown Cloudscape. <br>
 * Cloudscape applications can be run in three different modes:
 * "Cloudscape applications can run against Cloudscape running in an embedded
 * or a client/server framework. When Cloudscape runs in an embedded framework,
 * the Cloudscape application and Cloudscape run in the same JVM. The application
 * starts up the Cloudscape engine. When Cloudscape runs in a client/server framework,
 * the application runs in a different JVM from Cloudscape. The application only needs
 * to start the client driver, and the connectivity framework provides network connections.
 * (The server must already be running.)"
 *
 * <p>When you run this application, give one of the following arguments:
 * <ul>
 * <li> embedded (default, if none specified)</li>
 * <li> rmijdbcclient (if Cloudscape is running embedded in the RmiJdbc Server framework)</li>
 * <li> sysconnectclient (if Cloudscape is running embedded in the Cloudconnector framework)</li>
 * </ul>
 * <p>
 * A complete similartiy join is performed, i.e. point data can be inserted
 * into database tables, it is retrieved using ResultSetMetaDataCursors, the
 * contained elements are mapped (Tuple --> FloatPoint) and sorted, after that a similarity join
 * based on Jack Orenstein's algorithm is executed and the elements
 * are mapped back (FloatPoint --> Tuple). <br>
 * The resulting elements are inserted into the table 'JoinResults'. With can
 * be viewed with e.g. with <tt>Cloudview</tt> if the argument <tt>clean</tt>
 * has not been specified. An other feature of this use case is given by
 * the possibility of using only a fraction of all elements stored
 * in the database table. Therefore a {@link xxl.core.relational.cursors.Sampler} wraps
 * the ResultSetMetaDataCursors delivering the input tuples. <br>
 * The whole join process can be steered by the arguments given to
 * the main method when calling this application. <br>
 * Note, all arguments have been set to default values for an easier usage.
 *
 * <p>
 * The following arguments can be passed to the main method: <br><br>
 *
 * <tt>rmijdbcclient</tt> - particular Cloudscape mode <br>
 * <tt>sysconnectclient</tt> - particular Cloudscape mode <br>
 * <tt>input</tt> - insert data into Cloudscape tables <br>
 * <tt>createDatabase</tt> - a new database will be created <br>
 * <tt>clean</tt> - used database will be cleaned (removal of all tables) <br>
 * <tt>external</tt> - external file queue will be placed on the location given by the next argument<br>
 * <tt>DBName</tt> - name of the database will be set to the next given argument <br>
 * <tt>DBLocation</tt> - path to the database will be set to the next given argument <br>
 * <tt>file1</tt> - location of first input file will be set to the next given argument <br>
 * <tt>file2</tt> - location of second input file will be set to the next given argument <br>
 * <tt>dim</tt> - dimension of the float points will be set to the next given argument <br>
 * <tt>mem</tt> - memory size in bytes will be set to the next given argument <br>
 * <tt>initialCapacity</tt> - initial capacity of the sweep line areas will be set to the next given argument <br>
 * <tt>p</tt> - fraction of elements to be used from the input will be set to the next given argument <br>
 * <tt>seed</tt> - seed used for generating a sample of the input will be set to the next given argument <br>
 * <tt>epsilon</tt> - epsilon distance will be set to the next given argument <br>
 * <tt>maxLevel</tt> - maximum level to be considered will be set to the next given argument <br>
 * <p>
 * Example usage: <br>
 * <pre>
 * java xxl.applications.relational.SimilarityJoinDemo input file1 C:\\st.ll.bin file2 C:\\rr.ll.bin createDatabase clean external
 * </pre>
 * The resulting output shows the number of join results and the runtime in
 * seconds, the number of element comparisons and,
 * if the sorting algorithm has been set to 'external',
 * the number of performed read/write operations.
 *
 *
 * @see java.sql.DriverManager
 * @see java.sql.Connection
 * @see java.sql.Statement
 * @see java.sql.PreparedStatement
 * @see java.sql.ResultSet
 * @see java.sql.ResultSetMetaData
 * @see java.sql.SQLException
 * @see java.io.File
 * @see xxl.core.comparators.ComparableComparator
 * @see xxl.core.collections.queues.ListQueue
 * @see xxl.core.cursors.wrappers.IteratorCursor
 * @see xxl.core.cursors.MetaDataCursor
 * @see xxl.core.functions.Function
 * @see xxl.core.io.IOCounter
 * @see xxl.core.collections.queues.io.RandomAccessFileQueue
 * @see xxl.core.predicates.MetaDataPredicate
 * @see xxl.core.predicates.Predicate
 * @see xxl.core.relational.tuples.ArrayTuple
 * @see xxl.core.relational.JoinUtils
 * @see xxl.core.relational.cursors.Mapper
 * @see xxl.core.relational.cursors.MergeSorter
 * @see xxl.core.relational.resultSets.MetaDataCursorResultSet
 * @see xxl.core.relational.cursors.Orenstein
 * @see xxl.core.relational.cursors.ResultSetMetaDataCursor
 * @see xxl.core.cursors.filters.Sampler
 * @see xxl.core.relational.resultSets.VirtualTable
 * @see xxl.core.spatial.predicates.DistanceWithinMaximum
 * @see xxl.core.predicates.FeaturePredicate
 * @see xxl.core.spatial.points.FloatPoint
 * @see xxl.core.spatial.KPEzCode
 * @see xxl.core.util.BitSet
 * @see xxl.core.util.WrappingRuntimeException
 *
 */
public class SimilarityJoinDemo {

	/**
	 * Cloudscape application is run in this mode.
	 * default value: "embedded"
	 */
	protected static String framework = "embedded";

	/**
	 * Cloudscape application uses this driver.
	 * default value: "COM.cloudscape.core.JDBCDriver"
	 */
	protected static String driver = "COM.cloudscape.core.JDBCDriver";

	/**
	 * Cloudscape application uses this protocol to communicate.
	 * default value: "jdbc:cloudscape:"
	 */
	protected static String protocol = "jdbc:cloudscape:";

	/**
	 * A flag determining if new data should be inserted.
	 * default value: false
	 */
	protected static boolean input = false;

	/**
	 * A flag determining if the data needed for this application
	 * should be removed.
	 * default value: false
	 */
	protected static boolean drop = false;

	/**
	 * The location of the data base.
	 * default value: "D:\\Datenbanken\\"
	 */
	public static String databaseLocation = "D:\\Datenbanken\\";

	/**
	 * The name of the data base.
	 * default value: "TigerData"
	 */
	public static String databaseName = "TigerData";

	/**
	 * A flag determining if a new database should be created.
	 * default value: false
	 */
	public static boolean createDataBase = false;

	/**
	 * Location of spatial data; points in unit-cube [0, 1)^2
	 * default value: "D:\\user\\kraemerj\\uni\\lokal\\paper\\st.ll.bin"
	 */
	public static String file1 = "D:\\user\\kraemerj\\uni\\lokal\\paper\\st.ll.bin";

	/**
	 * Location of spatial data; points in unit-cube [0, 1)^2
	 * default value: "D:\\user\\kraemerj\\uni\\lokal\\paper\\rr.ll.bin"
	 */
	public static String file2 = "D:\\user\\kraemerj\\uni\\lokal\\paper\\rr.ll.bin";

	/**
	 * Dimension of data.
	 * default value: 2
	 */
	public static int dim = 2;

	/**
	 * Main memory available (in bytes).
	 * default value: 1000000
	 */
	public static int mem = 1000000;

	/**
	 * The initial capacity of the sweep areas.
	 * default value: 30000
	 */
	public static int initialCapacity = 30000;

	/**
	 * Fraction of elements to be used from the input.
	 * default value: 0.01
	 */
	public static double p = 0.01;

	/**
	 * The seed to be used for the Sampler.
	 * Note: same seed as in NestedLoopsJoin use-case in xxl.core.spatial!!
	 * default value: 42
	 */
	public static long seed = 42;

	/**
	 * Epsilon-distance.
	 * default value: 0.01
	 */
	public static float epsilon = 0.01f;

	/**
	 * Maximum level of the partitioning.
	 * default value: 12
	 */
	public static int maxLevel = 12;

	/** A factory method generating the desired tuples contained in the cursors. */
	public static Function<Object, ? extends Tuple> createTuple = ArrayTuple.FACTORY_METHOD;

	/** The join type. */
	public static Type type = Type.THETA_JOIN;

	/**
	 * Use a temporal external file queue.
	 * default value: false
	 */
	public static boolean external = false;

	/**
	 * The path of the temporal external file queue.
	 * default value: ""
	 */
	public static String tmpPath = "";

	/** The start time of the algorithm. */
	protected static long start;

	/** Counter for result tuples. */
	protected static int res = 0;

	/** An IO-counter. */
	protected static final IOCounter counter = new IOCounter();

	/**
	 * Method to determine the mode a
	 * cloudscape application should be run in. <br>
	 * Futhermore arguments for the external sorting algorithm,
	 * the similarity join algorithm and the mapping functions
	 * can be specified.
	 *
	 * @param args the arguments given by a call to main.
	 */
	private static void determineMode(String[] args) {
		int length = args.length;
		try {
			for (int index = 0; index < length; index++) {
				if (args[index].equalsIgnoreCase("rmijdbcclient")) {
					framework = "rmijdbc";
					driver = "COM.cloudscape.core.RmiJdbcDriver";
					protocol = "jdbc:cloudscape:rmi:";
				}
				if (args[index].equalsIgnoreCase("sysconnectclient")) {
					framework = "sysconnect";
					driver = "COM.cloudscape.core.WebLogicDriver";
					protocol = "jdbc:cloudscape:weblogic:";
				}
				if (args[index].equalsIgnoreCase("input"))
					input = true;
				if (args[index].equalsIgnoreCase("createDatabase"))
					createDataBase = true;
				if (args[index].equalsIgnoreCase("clean"))
					drop = true;
				if (args[index].equalsIgnoreCase("external")) {
					external = true;
					tmpPath = args[index+1];
				}
				if (args[index].equalsIgnoreCase("DBName")) {
					databaseName = args[index+1];
				}
				if (args[index].equalsIgnoreCase("DBLocation")) {
					databaseLocation = args[index+1];
				}
				if (args[index].equalsIgnoreCase("file1")) {
					file1 = args[index+1];
				}
				if (args[index].equalsIgnoreCase("file2")) {
					file2 = args[index+1];
				}
				if (args[index].equalsIgnoreCase("dim")) {
					dim = Integer.parseInt(args[index+1]);
				}
				if (args[index].equalsIgnoreCase("mem")) {
					mem = Integer.parseInt(args[index+1]);
				}
				if (args[index].equalsIgnoreCase("initialCapacity")) {
					initialCapacity = Integer.parseInt(args[index+1]);
				}
				if (args[index].equalsIgnoreCase("p")) {
					p = Double.parseDouble(args[index+1]);
				}
				if (args[index].equalsIgnoreCase("seed")) {
					seed = Long.parseLong(args[index+1]);
				}
				if (args[index].equalsIgnoreCase("epsilon")) {
					epsilon = Float.parseFloat(args[index+1]);
				}
				if (args[index].equalsIgnoreCase("maxLevel")) {
					maxLevel = Integer.parseInt(args[index+1]);
				}
			}
		}
		catch (ArrayIndexOutOfBoundsException ae) {
			System.err.println("Wrong argument usage. Please specify all needed parameters.");
			ae.printStackTrace(System.err);
		}
	}

	/**
	 * Returns a JDBC-connection to a Cloudscape database.
	 * If the static class attribute <tt>createDatabase</tt> is <tt>true</tt>
	 * a new database with <tt>dataBaseName</tt>
	 * is created. Otherwise an existing database is used.
	 *
	 * @param databaseLocation the location of the database (path).
	 * @param dataBaseName the name of the database.
	 * @param autoCommit Sets the autoCommit value of the returned connection
	 * 		to this value.
	 * @return a JDBC-connection to the specified database.
	 */
	protected static Connection getConnection(String databaseLocation, String dataBaseName, boolean autoCommit) {
		try {
			Class.forName(driver).newInstance();
			System.out.println("Loaded the appropriate driver.");
			Connection conn = createDataBase ?
				DriverManager.getConnection(protocol + databaseLocation + dataBaseName +";create=true") :
				DriverManager.getConnection(protocol + databaseLocation + dataBaseName+";create=false");
			System.out.println("Connected to database.");
			conn.setAutoCommit(autoCommit);
			System.out.println("Autocommit activated: " +autoCommit);
			return conn;
		}
		catch (Exception e) {
			throw new WrappingRuntimeException(e);
		}
	}

	/**
	 * Closes the given connection after committing the transaction
	 * of this connection.
	 *
	 * @param conn the connection to be closed.
	 * @see #getConnection(String, String, boolean)
	 */
	protected static void closeConnection(Connection conn) {
		try {
			conn.commit();
			conn.close();
			System.out.println("Committed transaction and closed connection.");
		}
		catch (Exception e) {
			throw new WrappingRuntimeException(e);
		}
	}

	/**
	 * "In embedded mode, an application should shut down Cloudscape.
	 * If the application fails to shut down Cloudscape explicitly,
	 * the Cloudscape does not perform a checkpoint when the JVM shuts down,
	 * which means that the next connection will be slower.
	 * Explicitly shutting down Cloudscape with the URL is preferred."
	 * <p>
	 * This style of shutdown will always throw an "exception".
	 */
	protected static void shutDownCloudscape() {
		boolean gotSQLExc = false;
		if (framework.equals("embedded")) {
			try {
				DriverManager.getConnection("jdbc:cloudscape:;shutdown=true");
			}
			catch (SQLException se) {
				gotSQLExc = true;
			}
			if (!gotSQLExc)
				System.out.println("Database did not shut down normally.");
			else
				System.out.println("Database shut down normally.");
		}
	}

	/**
	 * Creating all tables storing needed for the similarity join, i.e.
	 * two tables storing two dimensional points and one table that
	 * will contain the join results at the end of this application.
	 *
	 * @param s Statement used for the execution of the SQL statements.
	 * @throws java.sql.SQLException if the query cannot be fulfilled correctly.
	 */
	public static void createTables(Statement s) throws SQLException {
		// Creating tables for spatial data
		String type = " DOUBLE PRECISION";
		String attributes1 = new String();
		for (int i = 0; i < dim-1; i++)
			attributes1 += "x"+dim+type+",";
		attributes1 += "x"+(dim-1)+type;
		s.execute("CREATE TABLE Spatial1 ("+attributes1+")");
		System.out.println("Created table Spatial1.");

		String attributes2 = new String();
		for (int i = 0; i < dim-1; i++)
			attributes2 += "y"+dim+type+",";
		attributes2 += "y"+(dim-1)+type;
		s.execute("CREATE TABLE Spatial2 ("+attributes2+")");
		System.out.println("Created table Spatial2.");

		s.execute("CREATE TABLE JoinResults ("+attributes1+", "+attributes2+")");
		System.out.println("Created table JoinResults.");
	}

	/**
	 * Dropping all tables needed for the test.
	 *
	 * @param s Statement used for the execution of the SQL statements.
	 * @throws java.sql.SQLException if the query cannot be fulfilled correctly.
	 */
	public static void dropTables(Statement s) throws SQLException {
		s.execute("DROP TABLE Spatial1");
		System.out.println("Dropped table Spatial1.");
		s.execute("DROP TABLE Spatial2");
		System.out.println("Dropped table Spatial2.");
		s.execute("DROP TABLE JoinResults");
		System.out.println("Dropped table JoinResults.");
	}

	/**
	 * Inserts the two dimensional FloatPoints contained in the given file
	 * into the defined table using the specified connection.
	 *
	 * @param file The file containing the FloatPoints.
	 * @param bufferSize The buffer size used for the FloatPointInputIterator.
	 * @param conn The connection the insertion should be fulfilled with.
	 * @param tableName The name of the table the FloatPoints should be inserted in.
	 * @throws java.sql.SQLException if the query cannot be fulfilled correctly.
	 */
	public static void insertPoints(File file, int bufferSize, Connection conn, String tableName) throws SQLException {
		System.out.println("Inserting data into table: "+tableName);
		String prefix = tableName.equalsIgnoreCase("Spatial1") ? "x" : "y";
		Iterator<?> it = new PointInputCursor(file, PointInputCursor.FLOAT_POINT, dim, bufferSize);
		String sql = "INSERT INTO " +tableName+"(";
		String attributes = new String();
		for (int i = 0; i < dim-1; i++)
			attributes += prefix+dim+",";
		attributes += prefix+(dim-1);
		sql += attributes+") values (";
		for (int i = 0; i < dim-1; i++)
			sql += "?, ";
		sql += "?)";

		PreparedStatement insert = conn.prepareStatement(sql);
		while(it.hasNext()) {
			FloatPoint point = (FloatPoint)it.next();
			for (int i = 0; i < dim; i++)
				insert.setDouble(i+1, point.getValue(i));
			insert.executeUpdate();
		}
		insert.close();
	}

	/**
	 * Creating a {@link xxl.core.relational.cursors.ResultSetMetaDataCursor}
	 * based on the specified table.
	 *
	 * @param s Statement used for the execution of the SQL statements.
	 * @param createTuple factory method used to create the tuples in the ResultSetMetaDataCursor.
	 * @param tableName The name of the table the FloatPoints are contained in.
	 * @return the ResultSetMetaDataCursor.
	 * @throws java.sql.SQLException if the query cannot be fulfilled correctly.
	 */
	public static ResultSetMetaDataCursor initializeInput(Statement s, Function<Object, ? extends Tuple> createTuple, String tableName) throws SQLException {
		return new ResultSetMetaDataCursor(s.executeQuery("SELECT * FROM "+tableName), createTuple);
	}

	/**
	 * The spatial join algorithm based on space-filling curves proposed by Jack Orenstein.
	 * See: [Ore 91] Jack A. Orenstein: An Algorithm for Computing the Overlay of k-Dimensional Spaces. SSD 1991:
	 * 381-400 for a detailed explanation. See: [DS 01]: Jens-Peter Dittrich, Bernhard Seeger: GESS: a Scalable Similarity-Join Algorithm for Mining Large Data Sets in High Dimensional
	 * Spaces. ACM SIGKDD-2001. for a review on Orensteins algorithm.
	 * <p>
	 * Orensteins algorithm is based on a binary recursive partitioning, where the binary code
	 * represents the so-called Z-ordering (z-codes).
	 * <p>
	 * Information concerning the implementation of {@link xxl.core.spatial.cursors.Orenstein}: <br>
	 * Orensteins algorithm (ORE) assigns each hypercube of the input relations to disjoint
	 * subspaces of the recursive partitioning whose union entirely
	 * covers the hypercube. ORE sorts the two sets of
	 * hypercubes derived from the input relations (including the
	 * possible replicates) w.r.t. the lexicographical ordering of its
	 * binary code. After that, the relations are merged using two
	 * main-memory stacks Stack_R and Stack_S. It is guaranteed that for two adjacent
	 * hypercubes in the stack, the prefix property is satisfied for
	 * their associated codes. Only those hypercubes are joined
	 * that have the same prefix code.
	 * <p>
	 * A deficiency of ORE is that the different assignment strategies
	 * examined in [Ore91] cause substantial replication rates. This
	 * results in an increase of the problem space and hence, sorting
	 * will be very expensive. Furthermore, ORE has not addressed the
	 * problem of eliminating duplicates in the result set.
	 * <p>
	 * Note that the method <code>reorganize(final Object
	 * currentStatus)</code> could actually be implemented with only 1 LOC. For efficiency
	 * reasons we use a somewhat longer version of the method here.
	 * <p>
	 *
	 * @param input0 the first input cursor.
	 * @param input1 the second input cursor.
	 * @param mem main memory available (in bytes).
	 * @param initialCapacity the initial capacity of the sweep areas.
	 * @param p fraction of elements to be used from the input.
	 * @param seed the seed to be used for the Sampler.
	 * @param epsilon epsilon-distance.
	 * @param maxLevel maximum level of the partitioning.
	 * @param createTuple a factory method generating the desired tuples contained in the cursors.
	 * @param type the join type.
	 * @throws IllegalAccessException failed to determine the object size.
	 *
	 * @return the created MetaDataCursor.
	 * @see xxl.core.spatial.cursors.Orenstein
	 * @see xxl.core.relational.cursors.Orenstein
	 */
	public static MetaDataCursor<Tuple, CompositeMetaData<Object, Object>> performOrenstein(
		final ResultSetMetaDataCursor input0,
		final ResultSetMetaDataCursor input1,
		final int mem,
		final int initialCapacity,
		final double p,
		final long seed,
		final float epsilon,
		final int maxLevel,
		Function<Object, ? extends Tuple> createTuple,
		final Type type
	) throws IllegalAccessException {

		if(external)
			System.out.print("EXTERNAL_ALG\t"+tmpPath);

		// determining the object size
		final int objectSize = xxl.core.util.XXLSystem.getObjectSize(new KPEzCode(new FloatPoint(dim), new BitSet(32)));

		// function delivering a ListQueue in main memory
		// or a RandomAccessFileQueue on external memory
		// depending on the static class attribute 'external'
		final Function<Function<?, Integer>, Queue<Tuple>> newQueue = new AbstractFunction<Function<?, Integer>, Queue<Tuple>>() {
			public Queue<Tuple> invoke(Function<?, Integer> inputBufferSize, Function<?, Integer> outputBufferSize) {
				if (external) {
					File file = null;
					try {
						file = File.createTempFile("RAF", ".queue", new File(tmpPath));
					}
					catch (IOException ioe) {
						ioe.printStackTrace(System.err);
					}
					
					return new RandomAccessFileQueue<Tuple>(
						file,
						new TupleConverter(
							true,
							Converters.getObjectConverter(
								ConvertableConverter.DEFAULT_INSTANCE
							)
						),
						new AbstractFunction<Object, ArrayTuple>() {
							public ArrayTuple invoke() {
								return new ArrayTuple(
									new KPEzCode(
										new FloatPoint(
											dim
										)
									)
								);
							}
						},
						inputBufferSize,
						outputBufferSize
					) {
						public void enqueueObject(Tuple tuple) {
							counter.incWrite();
							super.enqueueObject(tuple);
						}
						
						public Tuple dequeueObject() {
							counter.incRead();
							return super.dequeueObject();
						}
					};
				}
				else
					return new ListQueue<Tuple>();
			}
		};

		// function delivering a MergeSorter
		// with the intention to sort the input cursors
		// uses the above definded function 'newQueue'
		Function<MetaDataCursor<? extends Tuple, CompositeMetaData<Object, Object>>, MetaDataCursor<? extends Tuple, CompositeMetaData<Object, Object>>> newSorter = new AbstractFunction<MetaDataCursor<? extends Tuple, CompositeMetaData<Object, Object>>, MetaDataCursor<? extends Tuple, CompositeMetaData<Object, Object>>>() {
			public MetaDataCursor<? extends Tuple, CompositeMetaData<Object, Object>> invoke(MetaDataCursor<? extends Tuple, CompositeMetaData<Object, Object>> cursor) {
				return new MergeSorter(
					cursor,
					Tuples.getTupleComparator(1),
					objectSize,
					mem,
					(int)(mem*0.4),
					newQueue,
					false
				);
			}
		};

		// the join predicate based on an epsilon distance
		Predicate<Tuple> joinPredicate = new FeaturePredicate<Tuple, FloatPoint>(
			new DistanceWithinMaximum<FloatPoint>(epsilon),
			new AbstractFunction<Tuple, FloatPoint> () {
				public FloatPoint invoke(Tuple tuple) {
					return (FloatPoint)((KPEzCode)tuple.getObject(1)).getData();
				}
			}
		);

		// setting the start time
		start = System.currentTimeMillis();

		/* calling the constructor of the Orenstein join algorithm
		 *
		 * parameter explanation:
		 *
		 * input0: the first input cursor
		 * input1: the second input cursor
		 * joinPredicate: the join predicate to use
		 * newSorter: function for sorting input cursors
		 * createTuple: a factory method generating the desired tuples contained in the cursors
		 * initialCapacity: the initial capacity of the sweep areas
		 * p: fraction of elements to be used from the input
		 * seed: the seed to be used for the Sampler
		 * epsilon: epsilon-distance
		 * maxLevel: maximum level of the partitioning
		 * type: the join type
		 */
		Orenstein orenstein = new Orenstein(
			input0,
			input1,
			joinPredicate,
			newSorter,
			createTuple,
			initialCapacity,
			p,
			seed,
			epsilon,
			maxLevel,
			type
		);

		return orenstein;
	}


	/**
	 * The main method contains the method calls to integrate data
	 * into a Cloudscape table, performing a similarity join on them
	 * and writing the results back to an other Cloudscape table.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 * 		submit parameters when the main method is called.
	 */
	public static void main(String[] args) {

		// determining the mode Cloudscape should run in
		// and parse further arguments (input, clean, ...)
		determineMode(args);

		System.out.println("SimilarityJoinDemo starting in " + framework + " mode.");
		try {

			// retrieving a valid JDBC connection
			Connection conn = getConnection(databaseLocation, databaseName, false);

			// creating some statements based on the current connection
			Statement s = conn.createStatement();
			Statement t = conn.createStatement();

			// defining the input cursors
			ResultSetMetaDataCursor input1, input2;

			// defining the output cursor
			final MetaDataCursor<Tuple, CompositeMetaData<Object, Object>> results;

			// if the argument 'input' has been specified
			if (input) {

				// Creating tables
				createTables(s);

				// Inserting spatial data using buffer size: 1024*1024 byte
				insertPoints(new File(file1), 1024*1024, conn, "Spatial1");
				insertPoints(new File(file2), 1024*1024, conn, "Spatial2");
				System.out.println("Inserted spatial data.");
			}

			/* initializing input cursors
			 *
			 * CLOUDSCAPE ==> XXL
			 *
			 */
			input1 = initializeInput(s, createTuple, "Spatial1");
			input2 = initializeInput(t, createTuple, "Spatial2");

			System.out.println("Performing similarity join based on Orenstein algorithm.");

			/* performing join using Orenstein algorithm
			 *
			 * parameter explanation:
			 * input1: the first input cursor.
			 * input2: the second input cursor.
			 * mem: main memory available (in bytes).
			 * initialCapacity: the initial capacity of the sweep areas.
			 * p: fraction of elements to be used from the input.
			 * seed: the seed to be used for the Sampler.
			 * epsilon: epsilon-distance.
			 * maxLevel: maximum level of the partitioning.
			 * createTuple: a factory method generating the desired tuples contained in the cursors.
			 * type: the join type.
			 */
			results = performOrenstein(input1, input2, mem, initialCapacity,
				p, seed, epsilon, maxLevel, createTuple, Type.THETA_JOIN);

			/* setting virtual table
			 *
			 * XXL ==> CLOUDSCAPE
			 *
			 */
			VirtualTable.SET_RESULTSET = new AbstractFunction<Object, MetaDataCursorResultSet>() {
				public MetaDataCursorResultSet invoke() {
					return new MetaDataCursorResultSet(results) {

						public Object getObject(int columnIndex) throws SQLException {
							res++;
							return super.getObject(columnIndex);
						}
					};
				}
			};

			System.out.println("INSERT INTO JoinResults SELECT * FROM NEW xxl.core.relational.VirtualTable() AS VT");

			// executing query on a virtual table
			s.execute("INSERT INTO JoinResults SELECT * FROM NEW xxl.core.relational.VirtualTable() AS VT");

			System.out.println("\n==============================");
			System.out.println("No. of results:\t"+res+"\t");
			System.out.println("runtime (sec):\t"+(System.currentTimeMillis()-start)/1000.0+"\t");
			System.out.println("element-comparisons:\t"+xxl.core.spatial.cursors.Orenstein.comparisons.counter+"\t");
			if(external)
				System.out.println("IOs(object-count)\tRead:\t"+counter.getReadIO()+"\tWrite:\t"+counter.getWriteIO());

			// closing input-Cursors
			input1.close();
			input2.close();

			// dropping tables if argument 'clean' has been specified
			if (drop) {
				dropTables(s);
			}

			// closing remaining resources and shutting down Cloudscape
			s.close();
			t.close();
			closeConnection(conn);
			shutDownCloudscape();

			System.out.println("SimilarityJoinDemo finished.");

		}
		catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}
}
