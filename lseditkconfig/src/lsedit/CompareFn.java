package lsedit;

public interface CompareFn 
{
	/* the value 0 if o1 == o2 / -ve if o1 < o2 / else +ve */

	abstract public int compare(Object o1, Object o2);
}

