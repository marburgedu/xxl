## Project ##
### eXtensible and fleXible Library ###
_XXL is a Java [library](http://dbs.mathematik.uni-marburg.de/Home/Research/Projects/xxl) that contains a rich infrastructure for implementing advanced query processing functionality. The library offers low-level components like access to raw disks as well as high-level ones like a query optimizer. On the intermediate levels, XXL provides a demand-driven cursor algebra, a framework for indexing and a powerful package for supporting aggregation. The XXL project provides various packages. See the longer [introduction](http://dbs.mathematik.uni-marburg.de/research/projects/xxl/longer_introduction.htm) to XXL for an explanation of the packages. The library is publicly available under GNU LGPL_

## Contributing to the XXL-Project ##

Since we consider XXL as a toolbox for the entire community, we would be pleased to get feed-back from research projects that use the functionality of XXL. In particular, we encourage people, who have extended the functionality of XXL, to attach their code to our release in a supplement package.

## Papers ##
  1. SIGMOD 2000 http://dl.acm.org/citation.cfm?doid=335191.336562
  1. VLDB 2001 http://www.vldb.org/conf/2001/P039.pdf

## News ##
We are pleased to announce beta version 2.0 of our Java library XXL. This library seamlessly extends Sun’s Java SDK and provides sophisticated frameworks as well as toolboxes modeling complex database functionality. Due to its extensible and generic design, XXL facilitates and accelerates the implementation of new ideas and the comparison to existing functionality.

The main features of XXL are:
  * A demand-driven cursor algebra including efficient implementations of object-relational operators such as joins, difference, MergeSort etc.
  * An extended relational algebra based on java.sql.ResultSet
  * A powerful framework of index-structures, e.g. B+tree, R-tree (linear and quadratic split Guttman et al), R\*tree, RR\*tree, Hilbert R-tree, R-tree (linear split Tan et al), X-tree, M-tree etc.
  * A framework for processing multi-way joins including spatial, temporal, and similarity     joins
  * The support of raw-I/O (using JNI), an own file system implementation, and a record manager
In addition, version 2.0 of XXL comes along with the following new functionality:
  * Skyline query R-tree extension
  * MVBT (Multiversion B+Tree)-Index Implementation
  * MVBT+ bulk-loading approach
  * Sort-based bulk-loading methods for R-trees (including STR and GOPT approach)
  * Top-down buffer tree bulk-loading of R-trees






Note that all classes of version 2.0 are documented in detail, while lots of use-cases simplify their understanding.

All versions of XXL are freely available under the terms of the GNU Lesser General Public License.

&lt;wiki:gadget url="http://www.ohloh.net/p/587514/widgets/project\_partner\_badge.xml" height="53" border="0"/&gt;