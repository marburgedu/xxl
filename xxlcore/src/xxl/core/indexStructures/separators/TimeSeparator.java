/*
 * XXL: The eXtensible and fleXible Library for data processing
 * 
 * Copyright (C) 2000-2011 Prof. Dr. Bernhard Seeger Head of the Database Research Group Department
 * of Mathematics and Computer Science University of Marburg Germany
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * http://code.google.com/p/xxl/
 */

package xxl.core.indexStructures.separators;

import java.sql.Time;

import xxl.core.functions.AbstractFunction;
import xxl.core.functions.Function;
import xxl.core.indexStructures.Separator;

/**
 * The class {@link xxl.core.indexStructures.Separator Separator} describes the known separtors of
 * the B+ tree. A <tt>Separtor</tt> is a simple key (for example an Integer). The <tt>Separator</tt>
 * of a query is a closed intervall [min, max]. .<br/>
 * <br/>
 * 
 * This class extends <tt>Separator</tt> for using <b>Time</b>.<br/>
 * <br/>
 * 
 * @author Marcus Pinnecke (pinnecke@mathematik.uni-marburg.de)
 * 
 * @see xxl.core.indexStructures.Separator
 * @see java.sql.Time
 */
public class TimeSeparator extends Separator {

  /**
   * Used for a functional like programming style which forces a hard copy in this case.
   */
  public static Function<Object, TimeSeparator> FACTORY_FUNCTION =
      new AbstractFunction<Object, TimeSeparator>() {

        @Override
        public TimeSeparator invoke(Object argument) {
          return new TimeSeparator((argument instanceof Long)
              ? (Long) argument
              : ((Time) argument).getTime());
        }
      };

  /**
   * @see xxl.core.indexStructures.Separator#Separator(Comparable)
   */
  public TimeSeparator(long sepValue) {
    super(sepValue);
  }

  /**
   * @see xxl.core.indexStructures.Separator#Separator(Comparable)
   */
  public TimeSeparator(Long sepValue) {
    super(sepValue);
  }

  /**
   * @see xxl.core.indexStructures.Separator#Separator(Comparable)
   */
  public TimeSeparator(Time sepValue) {
    super(sepValue.getTime());
  }

  @Override
  public Object clone() {
    return new TimeSeparator(new Time((Long) this.sepValue));
  }

}
