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

package xxl.connectivity.jts.applications;

import java.awt.Color;
import java.awt.geom.Line2D;
import java.util.Comparator;
import java.util.Iterator;

import xxl.connectivity.jts.visual.VisualOutput;
import xxl.connectivity.jts.visual.VisualOutputCursor;
import xxl.core.collections.containers.Container;
import xxl.core.collections.containers.MapContainer;
import xxl.core.collections.queues.DynamicHeap;
import xxl.core.cursors.AbstractCursor;
import xxl.core.cursors.Cursor;
import xxl.core.cursors.filters.Taker;
import xxl.core.cursors.identities.DelayCursor;
import xxl.core.cursors.mappers.Mapper;
import xxl.core.cursors.sources.EmptyCursor;
import xxl.core.functions.AbstractFunction;
import xxl.core.functions.Function;
import xxl.core.indexStructures.Descriptor;
import xxl.core.indexStructures.RTree;
import xxl.core.indexStructures.Tree.Query.Candidate;
import xxl.core.spatial.KPE;
import xxl.core.spatial.points.DoublePoint;
import xxl.core.spatial.points.Point;
import xxl.core.spatial.rectangles.DoublePointRectangle;
import xxl.core.spatial.rectangles.Rectangle;

public class KNNJoin extends AbstractCursor<KPE[]>{

	Iterator<KPE> R;
	Iterator<Candidate> tmp;
	int k;
	RTree S;
	KPE[] next = null; 
	KPE rNext;	
	
	public KNNJoin(Iterator<KPE> r, RTree s, int k) {
		super();
		R = r;
		S = s;
		this.k = k;
		tmp = new EmptyCursor<Candidate>();
	}

	
	@Override
	protected boolean hasNextObject() {
		next = null;		
		while(! tmp.hasNext() && R.hasNext()){
			rNext = R.next();
			tmp = new Taker(
					S.query(new DynamicHeap( new Comparator<Candidate>(){							
							public int compare(Candidate c1, Candidate c2) {
								double d1 = ((Rectangle) c1.descriptor()).minDistance((Point) rNext.getData(), 2);
								double d2 = ((Rectangle) c2.descriptor()).minDistance((Point) rNext.getData(), 2);
								return Double.compare(d1,d2);
							}						
						})),
					k);
		}		
		if( tmp.hasNext() ){
			next = new KPE[]{rNext, (KPE)tmp.next().entry()}; 
			return true;
		} 
		return false;			
	}

	@Override
	protected KPE[] nextObject() {
		return next;
	}

	@Override
	public void remove() {		
		throw new UnsupportedOperationException();
	}

	public static Iterator<Point> getRandomPoints(final Rectangle universe, final int numPoints){
		return new AbstractCursor<Point>(){			
			int num;
			Point next;
			
			@Override
			public void open(){
				super.open();
				num = numPoints;
			}
			
			@Override
			protected boolean hasNextObject() {
				next = new DoublePoint( new double[]{ 
							universe.getCorner(false).getValue(0) + universe.deltas()[0] * Math.random(),
							universe.getCorner(false).getValue(1) + universe.deltas()[1] * Math.random()
							});
				return num-- > 0;
			}

			@Override
			protected Point nextObject() { 
				return next;
			}			
		};
	}
	
	public static void main(String[] args){
		RTree S = new RTree();
		Container container = new MapContainer();
		int m=5, M= 12;
		
		Rectangle universe = new DoublePointRectangle(new double[]{0,0}, new double[]{100,100});

		Function<KPE, Descriptor> getDescriptor = new AbstractFunction<KPE, Descriptor>(){
			@Override
			public Descriptor invoke(KPE k){
				DoublePoint p = (DoublePoint) k.getData();
				return new DoublePointRectangle(p,p);
			}
		};
		final VisualOutput out = new VisualOutput("", universe, 500);
		out.setRepaintImmediately(true);
		
		Iterator<KPE> R= new Mapper<Point, KPE>(
				new AbstractFunction<Point, KPE>(){
					@Override
					public KPE invoke(Point p){
						return new KPE(p);
					}
				},
				getRandomPoints(universe, 20)
			);
		
		S.initialize(getDescriptor, container, m, M);
		Iterator<Point> itS = getRandomPoints(universe, 500);
		while(itS.hasNext()){
			Point p = itS.next();
			out.transformAndDraw(VisualOutputCursor.pointToShape.invoke(p), Color.RED);
			S.insert(new KPE(p));
		}
		
		Cursor<KPE[]> knnJoin = new DelayCursor(new KNNJoin(R, S, 15), 50, false);
		while(knnJoin.hasNext()){
			KPE[] k = knnJoin.next();
			Point p0 = (Point) k[0].getData();
			Point p1 = (Point) k[1].getData();
			Line2D line = new Line2D.Double(p0.getValue(0), p0.getValue(1), p1.getValue(0), p1.getValue(1));
			out.transformAndDraw(line,  Color.BLUE);
			out.transformAndDraw(VisualOutputCursor.pointToShape.invoke(p0), Color.GREEN);
		}
	}
}
