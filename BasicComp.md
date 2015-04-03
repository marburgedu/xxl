Under Construction
# Basic Components #

## Functions and Predicates ##

Functional abstraction is a powerful mechanism for writing compact code. Since functions are not First class citizens in Java, XXL provides the interface [Function](http://code.google.com/p/xxl/source/browse/trunk/xxlcore/src/xxl/core/functions/Function.java) which has to be implemented by a functional class. A new functional object is declared at runtime using one of the following methods:

  * An anonymous class is implemented by extending [Function](http://code.google.com/p/xxl/source/browse/trunk/xxlcore/src/xxl/core/functions/Function.java) and overriding a method `invoke` that should contain the executable code. An example for declaring a new function is given as follows:

```java

Function maxComp = new Function() {
public Object invoke (Object o1, Object o2) {
return (((Comparable) o1).compareTo(o2) > 0) ? o1 : o2;
}
}
```

  * The method `compose` of a functional object can be called to declare a new function by composition of functional objects.

The code of a functional object is executed by calling the method `invoke` with the expected number of parameters. Note that functional objects in XXL may have a status and therefore, are more powerful than pure mathematical functions. Due to its importance in database systems, we decided to provide a separate interface ([Predicate](http://code.google.com/p/xxl/source/browse/trunk/xxlcore/src/xxl/core/predicates/Predicate.java)) for Boolean functions. This improves the readability of the code as well as its performance since expensive casts are avoided. Relevant to databases are particularly predicates like `exist` for specifying subqueries and the predicates for supporting a three-value logic.

## Containers ##

A container is an implementation of a map that provides an abstraction from the
underlying physical storage. If an object is inserted into a container, a new
ID is created and returned. An object of a container can only be retrieved via
the corresponding ID. Since a container is generally used for bridging the gap
between levels of a storage hierarchy, mechanisms for buffer management are
already included in a container. There are many different implementations of
containers in XXL. The class
[http://code.google.com/p/xxl/source/browse/trunk/xxlcore/src/xxl/core/collections/containers/MapContainer.java
MapContainer] refers to a container where the set of objects is kept in main
memory. The purpose of this container is to run queries fast in memory and to
support debugging. The class
[http://code.google.com/p/xxl/source/browse/trunk/xxlcore/src/xxl/core/collections/containers/io/BlockFileContainer.java
BlockFileContainer] represents a file of blocks, where a block refers to an
array of bytes with a fixed length. This is for instance useful when
index-structures like R-trees are implemented. Java does not support operations
on binary data and therefore, a block has to be serialized into its object
representation. Java's serialization mechanism is however not appropriate
since it has to be defined at compile time. It is also too inï¬‚exible because
there is only at most one serialization method for a class. XXL overcomes these
deficiencies by introducing the class [ConverterContainer](http://code.google.com/p/xxl/source/browse/trunk/xxlcore/src/xxl/core/collections/containers/io/ConverterContainer.java) that is a decorated container, i.e., an object of this class is a container and consists of a container. In addition, this class provides a converter that transforms an object into a different representation. A [BufferedContainer](http://code.google.com/p/xxl/source/browse/trunk/xxlcore/src/xxl/core/collections/containers/io/BufferedContainer.java) is also a decorator. Its primary task is to support object buffering in XXL. In order to run experiments on external storage without interfering with the underlying operating systems, XXL contains classes that support access to raw devices. There are two possibilities:

  * The class NativeRawAccessoffers native methods on a raw device. By using NativeRawAccess the class RawAccessRAF extends the class java.io.RandomAccessFile, which is the storage interface of BlockFileContainer.

  * XXL offers an implementation of an entire file system that runs on a raw
  * device. This is able to deliver files as objects of a class that extends
  * java.io.RandomAccessFile. Therefore, an object of the class
  * BlockFileContainercan store its blocks in files of XXL's file system.

## Cursor ##

A cursor is an abstract mechanism to access objects within a stream. Cursors in XXL are independent from the specific type of the underlying objects. The interface of a cursor is given by
```java

interface Cursor extends java.util.Iterator {
Object peek();
void update(Object o);
void reset();
void close();
...
}
```

A cursor extends the functionality of the iterator provided in the package
java.util. The peek method reports the next object of the iteration without
changing the state of the iteration. A call of reset sets the cursor to the
beginning of the iteration. The method close stops the iteration and releases
resources like file handles. The method update modifies the current object of the iteration. XXL offers an algebra for processing cursors, i. e., there are a set of operations that require cursors as input and return a cursor as output. We distinguish between three kinds of cursors:  Input cursors are wrappers for transforming a data source into a cursor. For example, XXL provides an input cursor for transforming java.sql.ResultSet into a cursor.   Processing cursors are the ones that modify the input cursor. Examples for such cursors are Join, Grouper, Mapper whose semantics are similar to the ones of the corresponding relational operators.  Flow cursors do not change the objects within the input stream, but they are restricted to change the underlying data row. For example, an instance of the class TeeCursor duplicates the input cursor.