package dyvil.array;

import static dyvil.reflect.Opcodes.*;

import java.util.Arrays;
import java.util.function.IntConsumer;
import java.util.function.IntPredicate;
import java.util.function.IntUnaryOperator;

import dyvil.annotation.Intrinsic;
import dyvil.annotation.infix;

public interface CharArray
{
	public static final char[]	EMPTY	= new char[0];
	
	// Basic Array Operations
	
	@Intrinsic({ INSTANCE, ARGUMENTS, ARRAYLENGTH })
	public static @infix int length(char[] array)
	{
		return array.length;
	}
	
	@Intrinsic({ INSTANCE, ARGUMENTS, CALOAD })
	public static @infix char apply(char[] array, int i)
	{
		return array[i];
	}
	
	@Intrinsic({ INSTANCE, ARGUMENTS, CASTORE })
	public static @infix void update(char[] array, int i, char v)
	{
		array[i] = v;
	}
	
	@Intrinsic({ INSTANCE, ARGUMENTS, ARRAYLENGTH, IFEQ })
	public static @infix boolean isEmpty(char[] array)
	{
		return array.length == 0;
	}
	
	public static @infix void forEach(char[] array, IntConsumer action)
	{
		int len = array.length;
		for (int i = 0; i < len; i++)
		{
			action.accept(array[i]);
		}
	}
	
	// Operators
	
	public static @infix char[] $plus(char[] array, char v)
	{
		int len = array.length;
		char[] res = new char[len + 1];
		System.arraycopy(array, 0, res, 0, len);
		res[len] = v;
		return res;
	}
	
	public static @infix char[] $plus$plus(char[] array1, char[] array2)
	{
		int len1 = array1.length;
		int len2 = array2.length;
		char[] res = new char[len1 + len2];
		System.arraycopy(array1, 0, res, 0, len1);
		System.arraycopy(array2, 0, res, len1, len2);
		return res;
	}
	
	public static @infix char[] $minus(char[] array, char v)
	{
		int index = indexOf(array, v, 0);
		if (index < 0)
		{
			return array;
		}
		
		int len = array.length;
		char[] res = new char[len - 1];
		if (index > 0)
		{
			// copy the first part before the index
			System.arraycopy(array, 0, res, 0, index);
		}
		if (index < len)
		{
			// copy the second part after the index
			System.arraycopy(array, index + 1, res, index, len - index - 1);
		}
		return res;
	}
	
	public static @infix char[] $minus$minus(char[] array1, char[] array2)
	{
		int index = 0;
		int len = array1.length;
		char[] res = new char[len];
		
		for (int i = 0; i < len; i++)
		{
			char v = array1[i];
			if (indexOf(array2, v, 0) < 0)
			{
				res[index++] = v;
			}
		}
		
		// Return a resized copy of the temporary array
		return Arrays.copyOf(res, index);
	}
	
	public static @infix char[] $amp(char[] array1, char[] array2)
	{
		int index = 0;
		int len = array1.length;
		char[] res = new char[len];
		
		for (int i = 0; i < len; i++)
		{
			char v = array1[i];
			if (indexOf(array2, v, 0) >= 0)
			{
				res[index++] = v;
			}
		}
		
		// Return a resized copy of the temporary array
		return Arrays.copyOf(res, index);
	}
	
	public static @infix char[] mapped(char[] array, IntUnaryOperator mapper)
	{
		int len = array.length;
		char[] res = new char[len];
		for (int i = 0; i < len; i++)
		{
			res[i] = (char) mapper.applyAsInt(array[i]);
		}
		return res;
	}
	
	public static @infix char[] filtered(char[] array, IntPredicate condition)
	{
		int index = 0;
		int len = array.length;
		char[] res = new char[len];
		for (int i = 0; i < len; i++)
		{
			char v = array[i];
			if (condition.test(v))
			{
				res[index++] = v;
			}
		}
		
		// Return a resized copy of the temporary array
		return Arrays.copyOf(res, index);
	}
	
	public static @infix char[] sorted(char[] array)
	{
		char[] res = array.clone();
		Arrays.sort(res);
		return res;
	}
	
	public static @infix String asString(char[] a)
	{
		return new String(a);
	}
	
	public static @infix String toString(char[] a)
	{
		if (a == null)
		{
			return "null";
		}
		
		int len = a.length;
		if (len <= 0)
		{
			return "[]";
		}
		
		StringBuilder buf = new StringBuilder(len * 3 + 4);
		buf.append('[').append(a[0]);
		for (int i = 1; i < len; i++)
		{
			buf.append(", ");
			buf.append(a[i]);
		}
		return buf.append(']').toString();
	}
	
	// Search Operations
	
	public static @infix int indexOf(char[] array, char v)
	{
		return indexOf(array, v, 0);
	}
	
	public static @infix int indexOf(char[] array, char v, int start)
	{
		for (; start < array.length; start++)
		{
			if (array[start] == v)
			{
				return start;
			}
		}
		return -1;
	}
	
	public static @infix int lastIndexOf(char[] array, char v)
	{
		return lastIndexOf(array, v, array.length - 1);
	}
	
	public static @infix int lastIndexOf(char[] array, char v, int start)
	{
		for (; start >= 0; start--)
		{
			if (array[start] == v)
			{
				return start;
			}
		}
		return -1;
	}
	
	public static @infix boolean contains(char[] array, char v)
	{
		return indexOf(array, v, 0) != -1;
	}
}
