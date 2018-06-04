import java.util.*;

public class NCPAGeographicCalculations {

	private	static final int UNKNOWN = 0;
	private	static final int P_IN = -1;
	private	static final int Q_IN = 1;
	private	static final char END_POINT = 'e';
	private	static final char VERTEX = 'v';
	private	static final char CROSS = '1';
	private	static final char NO_CROSS = '0';
	private static final double RADIUS = 6364.963;              // average radius of the earth in km - Change this if it changes in GeoDis

	
	private NCPAGeographicCalculations(){
	}

	public static Stack intersect(Stack hull1, Stack hull2){
		// returns null if there is no intersect, otherwise returns the convex hull corresponding to the intersection
		Stack inters = new Stack(); // head is last element of the vector.
		PopulationData a1,b1,A,B,p,q;
		p = new PopulationData(0,0);
		q = new PopulationData(0,0);
		char code;
		if(hull1.size() < 4 || hull2.size() < 4){ // two or less points on the hull
			if(hull1.size() == 0 || hull2.size() == 0){
				System.out.println("Error: hull.size() == 0");
				return null; // no intersect
			} else if (hull1.size() == 1 || hull2.size() == 1){
				// The hull size shouldn't be 1
				System.out.println("Error: hull.size() == 1");
				if (((PopulationData) hull1.get(0)).equals(hull2.get(0))){
					inters.push(((PopulationData)hull1.get(0)).copy());
					inters.push(((PopulationData)hull1.get(0)).copy());
					return inters;
				} else {
					return null;
				}
			} else if (hull1.size() == 2 && hull2.size() == 2){
				// single point on the hull
				if (((PopulationData) hull1.get(0)).equals(hull2.get(0))){
					inters.push(((PopulationData)hull1.get(0)).copy());
					inters.push(((PopulationData)hull1.get(0)).copy());
					return inters;
				} else {
					return null;
				}
			} else if (hull1.size() == 2 && hull2.size() == 3){
				if (perpProduct((PopulationData)hull2.get(0),(PopulationData)hull2.get(1),(PopulationData)hull1.get(0)) == 0 
					&& between((PopulationData)hull2.get(0),(PopulationData)hull2.get(1),(PopulationData)hull1.get(0))){
					inters.push(((PopulationData)hull1.get(0)).copy());
					inters.push(((PopulationData)hull1.get(0)).copy());
					return inters;
				} else {
					return null;
				}
			} else if (hull1.size() == 3 && hull2.size() == 2){
				if (perpProduct((PopulationData)hull1.get(0),(PopulationData)hull1.get(1),(PopulationData)hull2.get(0)) == 0 
					&& between((PopulationData)hull1.get(0),(PopulationData)hull1.get(1),(PopulationData)hull2.get(0))){
					inters.push(((PopulationData)hull2.get(0)).copy());
					inters.push(((PopulationData)hull2.get(0)).copy());
					return inters;
				} else {
					return null;
				}
			} else if (hull1.size() == 3 && hull2.size() == 3){
				if((code = segmentIntersect((PopulationData)hull1.get(0),(PopulationData)hull1.get(1),(PopulationData)hull2.get(0),(PopulationData)hull2.get(1),p,q)) != NO_CROSS){
					inters.push(p.copy());
					if(code == END_POINT){
						inters.push(q.copy());
					}
					inters.push(p.copy());
					return inters;
				} else {
					return null;
				}
			} else if (hull1.size() < 4){
				int windp,windq;
				if(hull1.size() == 0){
					System.out.println("Error: hull1.size() == 0");
					return null;
				} else if (hull1.size() == 1){
					System.out.println("Error: hull1.size() == 1");
					windp = windingNumber((PopulationData)hull1.get(0),hull2);
					if(windp == 0){
						return null;
					} else {
						inters.push(((PopulationData)hull1.get(0)).copy());
						inters.push(((PopulationData)hull1.get(0)).copy());
						return inters;
					}
				} else if (hull1.size() == 2){
					windp = windingNumber((PopulationData)hull1.get(0),hull2);
					if(windp == 0){
						return null;
					} else {
						inters.push(((PopulationData)hull1.get(0)).copy());
						inters.push(((PopulationData)hull1.get(0)).copy());
						return inters;
					}
				} else {
					windp = windingNumber((PopulationData)hull1.get(0),hull2);
					windq = windingNumber((PopulationData)hull1.get(1),hull2);
					if((windp == 0) && (windq == 0)){ // both outside
						return null;
					} else if ((windp == 0) || (windq == 0)){ // one is inside
						for(int index = 0; index < hull2.size()-1;index++){
							if((code = segmentIntersect((PopulationData)hull1.get(0),(PopulationData)hull1.get(1),(PopulationData)hull2.get(index),(PopulationData)hull2.get(index+1),p,q)) != NO_CROSS){
								inters.push(p.copy());
								if(code == END_POINT){
									inters.push(q.copy());
								}
							}
						}
						if (inters.empty()){
							return null;
						} else {
							inters.push(((PopulationData)inters.get(0)).copy());
							return inters;
						}
					} else {
						return hull1;
					}
				}
			} else { // hull2.size() < 4
				int windp,windq;
				if(hull2.size() == 0){
					System.out.println("Error: hull2.size() == 0");
					return null;
				} else if (hull2.size() == 1){
					System.out.println("Error: hull2.size() == 1");
					windp = windingNumber((PopulationData)hull2.get(0),hull1);
					if(windp == 0){
						return null;
					} else {
						inters.push(((PopulationData)hull2.get(0)).copy());
						inters.push(((PopulationData)hull2.get(0)).copy());
						return inters;
					}
				} else if (hull2.size() == 2){
					windp = windingNumber((PopulationData)hull2.get(0),hull1);
					if(windp == 0){
						return null;
					} else {
						inters.push(((PopulationData)hull2.get(0)).copy());
						inters.push(((PopulationData)hull2.get(0)).copy());
						return inters;
					}
				} else {
					windp = windingNumber((PopulationData)hull2.get(0),hull1);
					windq = windingNumber((PopulationData)hull2.get(1),hull1);
					if((windp == 0) && (windq == 0)){ // both outside
						return null;
					} else if ((windp == 0) || (windq == 0)){ // one is inside
						for(int index = 0; index < hull1.size()-1;index++){
							if((code = segmentIntersect((PopulationData)hull2.get(0),(PopulationData)hull2.get(1),(PopulationData)hull1.get(index),(PopulationData)hull1.get(index+1),p,q)) != NO_CROSS){
								inters.push(p.copy());
								if(code == END_POINT){
									inters.push(q.copy());
								}
							}
						}
						if (inters.empty()){
							return null;
						} else {
							inters.push(((PopulationData)inters.get(0)).copy());
							return inters;
						}
					} else {
						return hull2;
					}
				}
			}
		}
		int aindex = 1;
		int bindex = 1;
		int n = hull1.size()-1; // num points on hull1
		int m = hull2.size()-1; // num points on hull2
		PopulationData a = (PopulationData) hull1.get(aindex);
		PopulationData b = (PopulationData) hull2.get(bindex);
		int aa = 0; // hull1 counter
		int ba = 0; // hull2 counter 
		boolean firstPoint = true;
		int pos = 0; // UNKNOWN || P_IN || Q_IN 
		double cross,aHB,bHA;
		do{
			a1 = (PopulationData) hull1.get(aindex-1);
			b1 = (PopulationData) hull2.get(bindex-1);
			A = new PopulationData(a.getLatitude()-a1.getLatitude(),a.getLongitude()-a1.getLongitude());
			B = new PopulationData(b.getLatitude()-b1.getLatitude(),b.getLongitude()-b1.getLongitude());
			cross = perpProduct(new PopulationData(0,0),A,B);
			aHB = perpProduct(b1,b,a);
			bHA = perpProduct(a1,a,b);
			code = segmentIntersect(a1,a,b1,b,p,q);
			if(code == CROSS || code == VERTEX ){
				if(pos == UNKNOWN && firstPoint){
					aa = ba = 0;
					firstPoint = false;
					inters.push(p.copy());
				}
				pos = inOut(pos,aHB,bHA);
				inters.push(p.copy());
			}
			if(code == END_POINT && dotProduct(A,B) < 0){
				inters.push(p.copy());
				inters.push(q.copy());
				return inters;
			}
			if(cross == 0 && aHB < 0 && bHA < 0){
				// The hulls are disjoint
				return null;
			} else if (cross == 0 && aHB == 0 && bHA == 0){
				// Special case A and B collinear
				if(pos == P_IN){
					ba++;
					bindex++;
					b = (PopulationData) hull2.get(bindex);
					if(bindex >= m){
						bindex = 1;
					}
				} else {
					inters.push(a.copy());
					aa++;
					aindex++;
					a = (PopulationData) hull1.get(aindex);
					if(aindex >= n){
						aindex = 1;
					}
				}
			} else if (cross >= 0){
				if (bHA > 0){
					if(pos == P_IN){
						inters.push(a.copy());
					}
					aa++;
					aindex++;
					a = (PopulationData) hull1.get(aindex);
					if(aindex >= n){
						aindex = 1;
					}
				} else {
					if(pos == Q_IN){
						inters.push(b.copy());
					}
					ba++;
					bindex++;
					b = (PopulationData) hull2.get(bindex);
					if(bindex >= m){
						bindex = 1;
					}
				}
			} else {
				if (aHB > 0){
					if(pos == Q_IN){
						inters.push(b.copy());
					}
					ba++;
					bindex++;
					b = (PopulationData) hull2.get(bindex);
					if(bindex >= m){
						bindex = 1;
					}
				} else {
					if(pos == P_IN){
						inters.push(a.copy());
					}
					aa++;
					aindex++;
					a = (PopulationData) hull1.get(aindex);
					if(aindex >= n){
						aindex = 1;
					}
				}
			}
		}while(((aa < n) || (ba < m)) && (aa < 2*n) && (ba < 2*m));
		if(!firstPoint){
			inters.push(inters.get(0));
		}
		if(pos == UNKNOWN){
			// boundaries of hull 1 and 2 do not cross
			if(windingNumber((PopulationData)hull1.get(0),hull2) != 0){
				return hull1;
			} else if (windingNumber((PopulationData)hull2.get(0),hull1) != 0){
				return hull2;
			} else {
				return null;
			}
		} else { 
			return inters;
		}
	}
	
	private static char segmentIntersect(PopulationData a,PopulationData b,PopulationData c,PopulationData d,PopulationData p,PopulationData q){
		double s, t, num, denom;
		char code = '?';
		denom = a.getLongitude()*(d.getLatitude()-c.getLatitude()) +
				b.getLongitude()*(c.getLatitude()-d.getLatitude()) +
				d.getLongitude()*(b.getLatitude()-a.getLatitude()) +
				c.getLongitude()*(a.getLatitude()-b.getLatitude());
		if(denom == 0.0){
			return parallelInt(a,b,c,d,p,q);
		}
		num = a.getLongitude()*(d.getLatitude()-c.getLatitude())+
				c.getLongitude()*(a.getLatitude()-d.getLatitude())+
				d.getLongitude()*(c.getLatitude()-a.getLatitude());
		if(num == 0.0 || num == denom){
			code = VERTEX;
		}
		s = num/denom;
		num = -(a.getLongitude()*(c.getLatitude()-b.getLatitude())+
				b.getLongitude()*(a.getLatitude()-c.getLatitude())+
				c.getLongitude()*(b.getLatitude()-a.getLatitude()));
		if(num == 0.0 || num == denom){
			code = VERTEX;
		}
		t = num/denom;
		if((0.0 < s) && (s < 1.0) && (0.0 < t) && (t < 1.0)){
			code = CROSS;
		} else if ((0.0 > s) || (s > 1.0) || (0.0 > t) || (t > 1.0)){
			code = NO_CROSS;
		}
		p.setLatitude(a.getLatitude()+s*(b.getLatitude()-a.getLatitude()));
		p.setLongitude(a.getLongitude()+s*(b.getLongitude()-a.getLongitude()));
		return code;
	}
	
	private static char parallelInt(PopulationData a,PopulationData b,PopulationData c,PopulationData d,PopulationData p,PopulationData q){
		if(!(perpProduct(a,b,c) == 0)){
			return NO_CROSS;
		}
		if(between(a,b,c) && between(a,b,d)){
			p.setLatitude(c.getLatitude());
			p.setLongitude(c.getLongitude());
			q.setLatitude(d.getLatitude());
			q.setLongitude(d.getLongitude());
			return END_POINT;
		}
		if(between(c,d,a) && between(c,d,b)){
			p.setLatitude(a.getLatitude());
			p.setLongitude(a.getLongitude());
			q.setLatitude(b.getLatitude());
			q.setLongitude(b.getLongitude());
			return END_POINT;
		}
		if(between(a,b,c) && between(c,d,b)){
			p.setLatitude(c.getLatitude());
			p.setLongitude(c.getLongitude());
			q.setLatitude(b.getLatitude());
			q.setLongitude(b.getLongitude());
			return END_POINT;
		}
		if(between(a,b,c) && between(c,d,a)){
			p.setLatitude(c.getLatitude());
			p.setLongitude(c.getLongitude());
			q.setLatitude(a.getLatitude());
			q.setLongitude(a.getLongitude());
			return END_POINT;
		}
		if(between(a,b,d) && between(c,d,b)){
			p.setLatitude(d.getLatitude());
			p.setLongitude(d.getLongitude());
			q.setLatitude(b.getLatitude());
			q.setLongitude(b.getLongitude());
			return END_POINT;
		}
		if(between(a,b,d) && between(c,d,a)){
			p.setLatitude(d.getLatitude());
			p.setLongitude(d.getLongitude());
			q.setLatitude(a.getLatitude());
			q.setLongitude(a.getLongitude());
			return END_POINT;
		}
		return NO_CROSS;
	}
	
	private static boolean between(PopulationData a, PopulationData b, PopulationData c){
		// assumes a,b,c are collinear (use perpProduct(a,b,c) == 0 for that)
		// Tests if c is between a and b
		if(a.getLongitude() != b.getLongitude()){
			return ((a.getLongitude() <= c.getLongitude())&& (c.getLongitude() <= b.getLongitude())) ||
					((a.getLongitude() >= c.getLongitude())&& (c.getLongitude() >= b.getLongitude()));
		} else {
			return ((a.getLatitude() <= c.getLatitude())&& (c.getLatitude() <= b.getLatitude())) ||
					((a.getLatitude() >= c.getLatitude())&& (c.getLatitude() >= b.getLatitude()));
		}
	}
	
	private static int inOut(int pos,double aHB, double bHA){
		if(aHB > 0){
			return P_IN;
		}
		if(bHA > 0){
			return Q_IN;
		}
		return pos;
	}
	
	public static Stack convexHull(ArrayList listofvertices){
		// Start Andrew's monotone chain algorithm
		// Sort the list according to x,y with min x,min y first upto max x, max y with x priorty over y
		Collections.sort(listofvertices); // The compareto method on the PopulationData class should give above as the natural ordering
		/*for(Iterator i = listofvertices.iterator(); i.hasNext(); ){
			PopulationData popData = (PopulationData) (i.next());
			System.out.println("Lat :"+popData.getLatitude()+" Lon:"+popData.getLongitude());
		}*/
		// perform the chaining
		Stack thehull = new Stack(); // remember this is also a Vector (synchronised ArrayList)
		int minmin = 0; // index of population data with min x and min y
		int minmax = minmin; // index of population data with min x and max y
		int maxmax = listofvertices.size()-1; // index of population data with max x and max y
		int maxmin = maxmax; // index of population data with max x and min y
		boolean found = false;
		PopulationData temppd = (PopulationData) listofvertices.get(minmin);
		for(int i = 1; i < listofvertices.size() && !found ;i++){ // find max minmax
			if(temppd.getLongitude() != ((PopulationData) listofvertices.get(i)).getLongitude()){
			//if(Math.abs(temppd.getLatitude() - ((PopulationData) listofvertices.get(i)).getLatitude()) > TOLERANCE){
				found = true;
				minmax = i-1;
			}
		}
		if(minmax == maxmax){
			// We have a straight line along x = const (or a single point); Degenerate case
			if(listofvertices.size() == 1){
				thehull.push(listofvertices.get(minmin));
			} else {
				thehull.push(listofvertices.get(minmin));
				thehull.push(listofvertices.get(maxmax));
			}
			thehull.push(listofvertices.get(minmin));
			return thehull;
		}
		found = false;
		temppd = (PopulationData) listofvertices.get(maxmax);
		for(int i = maxmax-1; i >= 0 && !found; i-- ){
			if(temppd.getLongitude() != ((PopulationData) listofvertices.get(i)).getLongitude()){
			//if(Math.abs(temppd.getLatitude()-((PopulationData) listofvertices.get(i)).getLatitude()) > TOLERANCE){
				found = true;
				maxmin = i+1;
			}
		}
		//System.out.println("Clade:"+cladeLabel+":minmin = "+minmin+":minmax = "+minmax+":maxmin = "+maxmin+":maxmax = "+maxmax);
		PopulationData ptop = (PopulationData) listofvertices.get(minmin);
		PopulationData pnexttotop  = ptop;
		PopulationData pi;
		thehull.push(ptop);
		for(int i = minmax+1; i <= maxmin; i++){
			pi = (PopulationData) listofvertices.get(i);
			//System.out.print("Check =>"+listofvertices.get(minmin).toString()+listofvertices.get(maxmin).toString()+pi.toString());
			if(perpProduct((PopulationData) listofvertices.get(minmin), (PopulationData) listofvertices.get(maxmin), pi) >=0){ // if pi are left of or on the line
				//System.out.print("Check1 =>"+listofvertices.get(minmin).toString()+listofvertices.get(maxmin).toString()+pi.toString());
				while(thehull.size() > 1 && perpProduct(pnexttotop,ptop,pi) >= 0){
					thehull.pop();
					if(thehull.size() > 1){
						ptop = (PopulationData) thehull.pop();
						pnexttotop = (PopulationData) thehull.peek();
						thehull.push(ptop); 
					}
				}
				pnexttotop = (PopulationData) thehull.peek();
				ptop = pi;
				thehull.push(ptop);
			}
		}
		if(maxmax != maxmin){
			thehull.push(listofvertices.get(maxmax));
		}
		int lowbound = thehull.size();
		for(int i=maxmin-1; i >= minmax; i--){
			pi = (PopulationData) listofvertices.get(i);
			if(perpProduct((PopulationData) listofvertices.get(maxmax), (PopulationData) listofvertices.get(minmax), pi) >=0 ){ // if pi are left of or on the line
				while(thehull.size() > lowbound && perpProduct(pnexttotop,ptop,pi) >= 0 ){ // have maxmax and one other element
					thehull.pop();
					if(thehull.size() > lowbound){
						ptop = (PopulationData) thehull.pop();
						pnexttotop = (PopulationData) thehull.peek();
						thehull.push(ptop);
					}
				}
				pnexttotop = (PopulationData) thehull.peek();
				ptop = pi;
				thehull.push(ptop);
			}
		}
		if(minmax != minmin){
			thehull.push(listofvertices.get(minmin));
		} // thehull is now the convex hull of the given distribution
		return thehull;
	}

	private static int windingNumber(PopulationData pd, Stack thehull){
		// Start the winding number algorithm
		int windingnumber = 0;
		double perpProd;
		PopulationData pi, piplus1;
		pi = (PopulationData) thehull.get(0);
		for(int i = 0; i < thehull.size()-1; i++){
			piplus1 = (PopulationData) thehull.get(i+1); // edge from popdata[i] to popdata[i+1]
			if(pi.getLatitude() <= pd.getLatitude()){
				if(piplus1.getLatitude() > pd.getLatitude()){
					// we have a crossing?
					perpProd = perpProduct(pi,piplus1,pd);
					if(perpProd > 0){ // if pd to left of line then yes
						windingnumber++;
					} else if (perpProd == 0){ // colinear
						//check x
						if(between(pi,piplus1,pd)){
							return 1;
						}
					}
				} else if (piplus1.getLatitude() == pd.getLatitude()){
					perpProd = perpProduct(pi,piplus1,pd);
					if (perpProd == 0){ // colinear => horizontal
						//check x
						if(between(pi,piplus1,pd)){
							return 1;
						}
					}
				}
			} else {
				if(piplus1.getLatitude() <= pd.getLatitude()){
					// we have crossing?
					perpProd = perpProduct(pi,piplus1,pd);
					if(perpProd < 0){ // if pd is to the right of the line then yes
						windingnumber--;
					} else if (perpProd == 0){ // colinear
						//check x
						if(between(pi,piplus1,pd)){
							return -1;
						}
					}
				}
			}
			pi = piplus1;
		}
		return windingnumber; // winding number != 0 => inside 
	}
	
	public static double area(Stack thehull){
		double areasum = 0.0;
		int numvertices = thehull.size() - 1;
		if(numvertices < 3){
			return areasum;
		}
		PopulationData a,b,c;
		a = (PopulationData) thehull.get(0);
		for(int i = 1; i < (numvertices-1); i++){
			b = (PopulationData) thehull.get(i);
			c = (PopulationData) thehull.get(i+1);
			areasum += Math.abs(perpProduct(a,b,c)/2);
		}
		return areasum;
	}
	private static double perpDistance(PopulationData p1, PopulationData p2, PopulationData p3){
		return perpDistance(toCartesian(p1),toCartesian(p2),toCartesian(p3));
	}
	
	private static double perpDistance(double[] pop1, double[] pop2, double[] pop3){
		// calculates the great circle distance between pop1 and the plane through O, pop2 and pop3.
		double[] pop1planecp = crossProduct(addVector(pop1,subtractVector(pop2,pop3)),pop1);
		double[] pop2planecp = crossProduct(pop2,pop3);
		return RADIUS*Math.acos(dotProduct(pop1planecp,pop2planecp)/(magnitude(pop1planecp)*magnitude(pop2planecp)));
	}
	
	private static double[] crossProduct(PopulationData pd1,PopulationData pd2){
		return crossProduct(toCartesian(pd1),toCartesian(pd2));
	}
	
	private static double[] crossProduct(double[] pd1xyz, double[] pd2xyz){
		double[] crossproduct = new double[3];
		crossproduct[0] = pd1xyz[1]*pd2xyz[2]-pd1xyz[2]*pd2xyz[1];
		crossproduct[1] = pd1xyz[2]*pd2xyz[0]-pd2xyz[2]*pd1xyz[0];
		crossproduct[2] = pd1xyz[0]*pd2xyz[1]-pd1xyz[1]*pd2xyz[0];
		return crossproduct;
	}
	
	private static double[] addVector(double[] pd1, double[] pd2){
		// pd1 to pd2
		double[] newvec = new double[3];
		for(int i =0; i < newvec.length; i++){
			newvec[i] = pd1[i]+pd2[i];
		}
		return newvec;
	}
	
	private static double[] subtractVector(double[] pd1, double[] pd2){
		// pd2 from pd1
		double[] newvec = new double[3];
		for(int i =0; i < newvec.length; i++){
			newvec[i] = pd1[i]-pd2[i];
		}
		return newvec;
	}
	
	private static double magnitude(double[] xyz){
		double sum = 0;
		for (int i=0; i < xyz.length;i++){
			sum += Math.pow(xyz[i],2);
		}
		return Math.sqrt(sum);
	}
	
	private static double[] toCartesian(PopulationData pd){
		// This should be moved to population data class
		double[] xyz = new double[3];
		double lat = Math.toRadians(pd.getLatitude());
		double lon = Math.toRadians(pd.getLongitude());
		xyz[0] = RADIUS*Math.cos(lon)*Math.cos(lat);
		xyz[1] = RADIUS*Math.sin(lon)*Math.cos(lat);
		xyz[2] = RADIUS*Math.sin(lat);
		return xyz;
	}
	
	private static PopulationData toPopulationData(double[] xyz){
		// This should be moved to population data class
		double lat = Math.asin(xyz[2]/RADIUS);
		double lon = Math.asin(xyz[1]/(RADIUS*Math.cos(lat)));
		return new PopulationData(Math.toDegrees(lat),Math.toDegrees(lon));
	}
	
	private static double dotProduct(double[] xyz1, double[] xyz2){
		// Uses origin O
		// if > 0 then angle between O->xyz1 and O->xyz2 is acute
		// if == 0 then angle is right angle
		// if < 0 then angle is obtuse
		double sum = 0;
		for(int i = 0; i < xyz1.length ; i++){
			sum += xyz1[i]*xyz2[i];
		}
		return sum;
	}
	
	private static double perpProduct(PopulationData p1, PopulationData p2, PopulationData p3){
		/* This treats the latitude(y) and longitude(x) as x,y co-ordinates to form the hull
			if > 0 then p3 on left of line through p1 to p2
			if == 0 then p3 on line through p1 and p2
			if < 0 then p3 on right of line through p1 to p2
		*/
		//int[] pd1 = toCartesian(p1);
		//int[] pd2 = toCartesian(p2);
		//int[] pd3 = toCartesian(p3);
		//return perpProduct(subtractVector(p1,p2),subtractVector(p3,p2));
		//return (p2.getLatitude()-p1.getLatitude())*(p3.getLongitude()-p1.getLongitude())-(p3.getLatitude()-p1.getLatitude())*(p2.getLongitude()-p1.getLongitude());
		return (p2.getLongitude()-p1.getLongitude())*(p3.getLatitude()-p1.getLatitude())-(p3.getLongitude()-p1.getLongitude())*(p2.getLatitude()-p1.getLatitude());
	}
	
	private static double dotProduct(PopulationData p1, PopulationData p2, PopulationData p3){
		/*This treats the latitude and longitude as x,y co-ordinates to form the hull
			if > 0 then angle between p2->p1 and p2->p3 is acute
			if == 0 then angle is right angle
			if < 0 then angle is obtuse
		*/
		//int[] pd1 = toCartesian(p1);
		//int[] pd2 = toCartesian(p2);
		//int[] pd3 = toCartesian(p3);
		//return dotProduct(subtractVector(p1,p2),subtractVector(p3,p2));
		return (p1.getLatitude()-p2.getLatitude())*(p3.getLatitude()-p2.getLatitude())+(p1.getLongitude()-p2.getLongitude())*(p3.getLongitude()-p2.getLongitude());
	}
	
	private static double dotProduct(PopulationData p1, PopulationData p2){
		return p1.getLatitude()*p2.getLatitude()+p1.getLongitude()*p2.getLongitude();
	}
	
	private static double magnitude(PopulationData p1,PopulationData p2){
		// returns the magnitude of the vector p2->p1
		return Math.sqrt(Math.pow(p1.getLatitude()-p2.getLatitude(),2)+Math.pow(p1.getLongitude()-p2.getLongitude(),2));
	}

}
