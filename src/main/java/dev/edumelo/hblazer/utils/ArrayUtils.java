package dev.edumelo.hblazer.utils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ArrayUtils {

	public static <T> T[] concatWithCollection(T[] array1, T[] array2) {
	    List<T> resultList = new ArrayList<>();
	    Collections.addAll(resultList, array1);
	    Collections.addAll(resultList, array2);

	    @SuppressWarnings("unchecked")
	    //the type cast is safe as the array1 has the type T[]
	    T[] resultArray = (T[]) Array.newInstance(array1.getClass().getComponentType(), 0);
	    return resultList.toArray(resultArray);
	}
	
}
