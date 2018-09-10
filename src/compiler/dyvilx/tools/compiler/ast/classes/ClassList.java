package dyvilx.tools.compiler.ast.classes;

import dyvil.annotation.internal.NonNull;
import dyvil.collection.iterator.ArrayIterator;
import dyvil.lang.Formattable;
import dyvil.lang.Name;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.phase.Resolvable;
import dyvilx.tools.parsing.marker.MarkerList;

import java.util.Iterator;

public class ClassList implements Formattable, Resolvable, Iterable<IClass>
{
	// =============== Constants ===============

	private static final int DEFAULT_CAPACITY = 1;

	// =============== Fields ===============

	protected IClass[] classes;
	protected int      size;

	// =============== Constructors ===============

	public ClassList()
	{
		this(DEFAULT_CAPACITY);
	}

	public ClassList(int capacity)
	{
		this(capacity <= 0 ? null : new IClass[capacity], 0);
	}

	public ClassList(IClass[] classes, int size)
	{
		this.classes = classes;
		this.size = size;
	}

	// =============== Methods ===============

	// --------------- Access ---------------

	public int size()
	{
		return this.size;
	}

	public IClass get(int index)
	{
		return this.classes[index];
	}

	public IClass get(Name name)
	{
		for (int i = 0; i < this.size; i++)
		{
			final IClass theClass = this.classes[i];
			if (theClass.getName() == name)
			{
				return theClass;
			}
		}
		return null;
	}

	// --------------- Modification ---------------

	public void add(IClass iclass)
	{
		if (this.classes == null)
		{
			this.classes = new IClass[DEFAULT_CAPACITY];
		}

		int index = this.size++;
		if (index >= this.classes.length)
		{
			IClass[] temp = new IClass[this.size];
			System.arraycopy(this.classes, 0, temp, 0, this.classes.length);
			this.classes = temp;
		}

		this.classes[index] = iclass;
	}

	// --------------- Iteration ---------------

	@Override
	public Iterator<IClass> iterator()
	{
		return new ArrayIterator<>(this.classes, 0, this.size);
	}

	// --------------- Resolution Phases ---------------

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.size; i++)
		{
			this.classes[i].resolveTypes(markers, context);
		}
	}

	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.size; i++)
		{
			this.classes[i].resolve(markers, context);
		}
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.size; i++)
		{
			this.classes[i].checkTypes(markers, context);
		}
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.size; i++)
		{
			this.classes[i].check(markers, context);
		}
	}

	@Override
	public void foldConstants()
	{
		for (int i = 0; i < this.size; i++)
		{
			this.classes[i].foldConstants();
		}
	}

	@Override
	public void cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		for (int i = 0; i < this.size; i++)
		{
			this.classes[i].cleanup(compilableList, classCompilableList);
		}
	}

	// --------------- Formatting ---------------

	@Override
	public String toString()
	{
		return Formattable.toString(this);
	}

	@Override
	public void toString(@NonNull StringBuilder buffer)
	{
		this.toString("", buffer);
	}

	public void toString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		for (int i = 0; i < this.size; i++)
		{
			this.classes[i].toString(indent, buffer);

			buffer.append("\n\n").append(indent);
		}
	}
}