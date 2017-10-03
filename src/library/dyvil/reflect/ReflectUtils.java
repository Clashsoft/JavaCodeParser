package dyvil.reflect;

import dyvil.annotation.internal.NonNull;

import jdk.internal.misc.JavaLangAccess;
import jdk.internal.misc.SharedSecrets;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public final class ReflectUtils
{
	public static final JavaLangAccess JAVA_LANG_ACCESS = SharedSecrets.getJavaLangAccess();
	public static final sun.misc.Unsafe UNSAFE;

	static
	{
		try
		{
			Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
			field.setAccessible(true);
			UNSAFE = (sun.misc.Unsafe) field.get(null);
		}
		catch (Exception ex)
		{
			throw new Error("Cannot find Unsafe.theUnsafe", ex);
		}
	}

	private ReflectUtils()
	{
		throw new Error("No instances");
	}

	// Classes

	@Deprecated
	public static boolean checkClass(String name)
	{
		try
		{
			Class.forName(name, false, ClassLoader.getSystemClassLoader());
			return true;
		}
		catch (ClassNotFoundException ex)
		{
			return false;
		}
	}

	@Deprecated
	public static Class getClass(String name)
	{
		try
		{
			return Class.forName(name, false, ClassLoader.getSystemClassLoader());
		}
		catch (ClassNotFoundException ex)
		{
			return null;
		}
	}

	@NonNull
	public static File getFileLocation(@NonNull Class<?> klass) throws ClassNotFoundException
	{
		final String classLocation = '/' + klass.getName().replace('.', '/') + ".class";
		final URL url = klass.getResource(classLocation);

		if (url == null)
		{
			throw new ClassNotFoundException("Location not found: " + classLocation);
		}

		final String path = url.toString().replace(File.separatorChar, '/');
		int index = path.lastIndexOf(classLocation);

		if (index < 0)
		{
			throw new ClassNotFoundException("Invalid Path: " + path);
		}

		int startIndex = 0;
		if (path.charAt(index - 1) == '!')
		{
			index--;
			startIndex = 4; // strip leading 'jar:'
		}
		else
		{
			index++;
		}

		final String newPath = path.substring(startIndex, index);
		try
		{
			return new File(new URI(newPath));
		}
		catch (URISyntaxException ex)
		{
			throw new ClassNotFoundException("Invalid URI: " + newPath, ex);
		}
	}
}
