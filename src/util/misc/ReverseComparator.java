/**
 * 
 */
package util.misc;

import java.util.Comparator;

/**
 * If T is a Comparable type, then this comparator sorts in the reverse of the natural order for T.
 * @param <T> 
 */
public class ReverseComparator<T extends Comparable<T> > implements Comparator<T> {
	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(T o1, T o2) {
		return o2.compareTo(o1);
	}
}
