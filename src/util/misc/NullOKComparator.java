package util.misc;

import java.util.Comparator;

/**
 * A Comparator that compares instances of class T according to
 * the natural order, except that it does not give errors for null,
 * instead it sorts null first in the order. 
 *
 * @param <T> the type of thing we are comparing.
 */
public class NullOKComparator<T extends Comparable<T>> implements Comparator<T>
{
	public int compare(T o1,T o2)
	{
		if (o1 ==null && o2 == null) return 0;
		if (o1 ==null ) return -1;
		if (o2 ==null ) return 1;
		
		return o1.compareTo(o2);
	}
}