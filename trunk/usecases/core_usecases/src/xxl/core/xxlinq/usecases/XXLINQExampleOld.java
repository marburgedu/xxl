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

package xxl.core.xxlinq.usecases;

import static xxl.core.xxlinq.AggregateUtils.AGGR;
import static xxl.core.xxlinq.AggregateUtils.COUNT;
import static xxl.core.xxlinq.columns.ColumnUtils.PROJ;
import static xxl.core.xxlinq.columns.ColumnUtils.col;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import xxl.core.relational.tuples.Tuple;
import xxl.core.xxlinq.AdvTupleCursor;
import xxl.core.xxlinq.IterableCursor;

public class XXLINQExampleOld {
	private static List<Integer> randomsList(int count){
		List<Integer> intList = new ArrayList<Integer>();
		Random r = new Random();
		for(int i = 0; i < count;i++ ){
			intList.add(r.nextInt(count));
		}
		return intList;
	}
	
	public static void main(String ... args){
		//XXLINQ:
		AdvTupleCursor atc = new AdvTupleCursor(randomsList(50), "randoms");

		atc = atc.groupBy(PROJ(col(1)), AGGR(COUNT("Anzahl"))).orderBy(col(2), col(1));
		
		for(Tuple t : atc)
			System.out.println("Wert: "+t.getInt(1)+" Anzahl: "+t.getInt(2));
		System.out.println("#############");
		//GOOD OLD JAVA
		//Gruppieren
		List<Integer> randoms = randomsList(50);
		Map<Integer,Integer> randomsSumme = new HashMap<Integer,Integer>();
		for(Integer i : randoms){
			if(randomsSumme.containsKey(i)){
				Integer value = randomsSumme.get(i);
				randomsSumme.put(i, value+1);
			}else{
				randomsSumme.put(i, 1);
			}
		}
		// alle entries aus der map in eine Liste packen um sie dann zu sortieren
		List<Entry<Integer, Integer>> val_count = new ArrayList<Entry<Integer, Integer>>(randomsSumme.entrySet());
		// val_count sortieren: primär nach anzahl der elemente in einem entry, sekundär nach dem wert
		Collections.sort(val_count, new Comparator<Entry<Integer, Integer>>() {
			@Override
			public int compare(Entry<Integer, Integer> o1, Entry<Integer, Integer> o2) {
				// key ist wert, value ist anzahl
				if( o1.getValue().equals(o2.getValue()) ) // gleiche anzahl
					return o1.getKey() - o2.getKey(); // dann nach wert sortieren
				else // ansonsten ist entry mit höherer anzahl "größer"
					return o1.getValue() - o2.getValue();
			}
		});
		
		for(Entry<Integer, Integer> ent : val_count)
			System.out.println("Wert: "+ent.getKey()+" Anzahl: "+ent.getValue());
		
		IterableCursor<Integer> cur = new IterableCursor<Integer>(randomsList(50));
	}
}
