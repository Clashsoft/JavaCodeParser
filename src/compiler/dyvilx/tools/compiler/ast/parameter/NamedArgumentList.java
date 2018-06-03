package dyvilx.tools.compiler.ast.parameter;

import dyvil.annotation.internal.NonNull;
import dyvil.reflect.Modifiers;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.context.IImplicitContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.generic.GenericData;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.backend.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.config.Formatting;
import dyvilx.tools.compiler.util.Markers;
import dyvil.lang.Name;
import dyvilx.tools.parsing.marker.MarkerList;

import java.util.Arrays;

public class NamedArgumentList extends ArgumentList
{
	protected Name[] keys;

	public NamedArgumentList()
	{
		this.keys = new Name[3];
		this.values = new IValue[3];
	}

	public NamedArgumentList(int size)
	{
		this.keys = new Name[size];
		this.values = new IValue[size];
	}

	public NamedArgumentList(Name[] keys, IValue[] values, int size)
	{
		this.keys = keys;
		this.values = values;
		this.size = size;
	}

	@Override
	public NamedArgumentList appended(IValue value)
	{
		return this.appended(null, value);
	}

	@Override
	public NamedArgumentList appended(Name name, IValue value)
	{
		int size = this.size;
		final int index = size++;
		final Name[] keys = new Name[size];
		final IValue[] values = new IValue[size];

		System.arraycopy(this.keys, 0, keys, 0, this.size);
		System.arraycopy(this.values, 0, values, 0, this.size);
		keys[index] = name;
		values[index] = value;

		return new NamedArgumentList(keys, values, size);
	}

	@Override
	public NamedArgumentList concat(ArgumentList that)
	{
		final int size = this.size + that.size;
		final NamedArgumentList list = new NamedArgumentList(size);
		list.addAll(this);
		list.addAll(that);
		return list;
	}

	@Override
	public IValue get(int index, Name key)
	{
		if (key == null)
		{
			return this.values[index];
		}

		final int argIndex = this.findIndex(index, key);
		if (argIndex < 0)
		{
			return null;
		}
		return this.values[argIndex];
	}

	public void setName(int i, Name name)
	{
		if (i < this.size)
		{
			this.keys[i] = name;
		}
	}

	@Override
	public void set(int index, Name key, IValue value)
	{
		if (key == null)
		{
			this.values[index] = value;
			return;
		}

		final int argIndex = this.findIndex(index, key);
		if (argIndex >= 0)
		{
			this.values[argIndex] = value;
		}
	}

	@Override
	protected void ensureCapacity(int min)
	{
		if (min >= this.values.length)
		{
			final Name[] tempKeys = new Name[min];
			final IValue[] tempValues = new IValue[min];
			System.arraycopy(this.keys, 0, tempKeys, 0, this.size);
			System.arraycopy(this.values, 0, tempValues, 0, this.size);
			this.keys = tempKeys;
			this.values = tempValues;
		}
	}

	@Override
	public void add(Name key, IValue value)
	{
		final int size = this.size;
		this.ensureCapacity(size + 1);
		this.values[size] = value;
		this.keys[size] = key;
		this.size = size + 1;
	}

	@Override
	public void addAll(ArgumentList list)
	{
		this.ensureCapacity(this.size + list.size);
		System.arraycopy(list.values, 0, this.values, this.size, list.size);

		if (list instanceof NamedArgumentList)
		{
			System.arraycopy(((NamedArgumentList) list).keys, 0, this.keys, this.size, list.size);
		}
	}

	@Override
	public void insert(int index, Name key, IValue value)
	{
		final int newSize = this.size + 1;
		if (newSize >= this.values.length)
		{
			final Name[] keys = new Name[newSize];
			final IValue[] values = new IValue[newSize];
			System.arraycopy(this.keys, 0, keys, 0, index);
			System.arraycopy(this.values, 0, values, 0, index);
			keys[index] = key;
			values[index] = value;
			System.arraycopy(this.keys, index, keys, index + 1, this.size - index);
			System.arraycopy(this.values, index, values, index + 1, this.size - index);
			this.keys = keys;
			this.values = values;
		}
		else
		{
			System.arraycopy(this.keys, index, this.keys, index + 1, this.size - index);
			System.arraycopy(this.values, index, this.values, index + 1, this.size - index);
			this.keys[index] = key;
			this.values[index] = value;
		}
		this.size = newSize;
	}

	private int findIndex(int index, Name name)
	{
		// First, try to match the parameter name against the argument labels
		for (int i = 0; i < this.size; i++)
		{
			if (this.keys[i] == name)
			{
				return i;
			}
		}

		// The specified name was not found, check the indices:

		if (index >= this.size)
		{
			return -1;
		}

		// Require that no argument labels exists before or at the index
		for (int i = 0; i <= index; i++)
		{
			if (this.keys[i] != null)
			{
				return -1;
			}
		}

		// No argument labels present before the requested index
		return index;
	}

	private int findNextName(int startIndex)
	{
		for (; startIndex < this.size; startIndex++)
		{
			if (this.keys[startIndex] != null)
			{
				return startIndex;
			}
		}

		return this.size;
	}

	@Override
	public int checkMatch(int[] values, IType[] types, int matchStartIndex, int argumentIndex, IParameter param,
		                     IImplicitContext implicitContext)
	{
		final int argIndex = this.findIndex(argumentIndex, param.getLabel());
		if (argIndex < 0)
		{
			// No argument for parameter name

			return param.isVarargs() ? 0 : checkDefault(param);
		}
		if (this.keys[argIndex] == null && param.hasModifier(Modifiers.EXPLICIT))
		{
			return checkDefault(param);
		}

		if (!param.isVarargs())
		{
			// Not a varargs parameter

			return ArgumentList.checkMatch(values, types, matchStartIndex + argIndex, this.values[argIndex],
			                               param.getCovariantType(), implicitContext) ? 0 : MISMATCH;
		}

		// Varargs Parameter
		final int endIndex = this.findNextName(argIndex + 1);
		return ArgumentList.checkVarargsMatch(values, types, matchStartIndex, this.values, argIndex, endIndex, param,
		                                      implicitContext);
	}

	@Override
	public void checkValue(int index, IParameter param, GenericData genericData, SourcePosition position, MarkerList markers, IContext context)
	{
		final int argIndex = this.findIndex(index, param.getLabel());
		if (argIndex < 0)
		{
			this.resolveMissing(param, genericData, position, markers, context);
			return;
		}

		if (!param.isVarargs())
		{
			this.values[argIndex] = convertValue(this.values[argIndex], param, genericData, markers, context);
			return;
		}

		final int endIndex = this.findNextName(argIndex + 1);
		if (!checkVarargsValue(this.values, argIndex, endIndex, param, genericData, markers, context))
		{
			return;
		}

		final int moved = this.size - endIndex;
		if (moved > 0)
		{
			System.arraycopy(this.values, endIndex, this.values, argIndex + 1, moved);
			System.arraycopy(this.keys, endIndex, this.keys, argIndex + 1, moved);
		}
		this.size = argIndex + moved + 1;
	}

	@Override
	public boolean hasParameterOrder()
	{
		return this.size <= 1;
	}

	@Override
	public void writeValues(MethodWriter writer, ParameterList parameters, int startIndex) throws BytecodeException
	{
		if (this.hasParameterOrder())
		{
			super.writeValues(writer, parameters, startIndex);
			return;
		}

		final int locals = writer.localCount();

		final int paramCount = parameters.size() - startIndex;

		// Step 1: Associate parameters to arguments
		final IParameter[] params = new IParameter[this.size];
		for (int i = 0; i < paramCount; i++)
		{
			final IParameter param = parameters.get(i + startIndex);
			final int argIndex = this.findIndex(i, param.getLabel());
			if (argIndex >= 0)
			{
				params[argIndex] = param;
			}
		}

		// Save the local indices in the targets array for later use
		// Maps parameter index -> local index of stored argument
		final int[] targets = new int[paramCount];
		// Fill the array with -1s to mark missing values
		Arrays.fill(targets, -1);

		// Step 2: Write all arguments that already have parameter order
		int argStartIndex = 0;
		for (int i = 0; i < this.size; i++)
		{
			IParameter param = params[i];
			if (param.getIndex() == startIndex + i)
			{
				this.values[i].writeExpression(writer, param.getCovariantType());
				targets[i] = -2;
				argStartIndex = i + 1;
			}
			else
			{
				break;
			}
		}

		// Step 3: Write the remaining arguments in order and save them in local variables

		if (argStartIndex < this.size)
		{
			for (int i = argStartIndex; i < this.size; i++)
			{
				final IParameter parameter = params[i];
				final IType parameterType = parameter.getCovariantType();
				final IValue value = this.values[i];

				final int localIndex = value.writeStore(writer, parameterType);
				targets[parameter.getIndex() - startIndex] = localIndex;
			}

			// Step 4: Load the local variables in order
			for (int i = 0; i < paramCount; i++)
			{
				final IParameter parameter = parameters.get(i + startIndex);
				final int target = targets[parameter.getIndex() - startIndex];

				switch (target)
				{
				case -1:
				case -2:
					// Value for parameter was already written in Step 2
					continue;
				default:
					// Value for parameter exists -> load the variable
					writer.visitVarInsn(parameter.getCovariantType().getLoadOpcode(), target);
				}
			}
		}

		writer.resetLocals(locals);
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.size; i++)
		{
			final Name key = this.keys[i];
			final IValue value = this.values[i];

			value.resolveTypes(markers, context);

			if (key == null)
			{
				continue;
			}

			for (int j = 0; j < i; j++)
			{
				if (this.keys[j] == key)
				{
					markers.add(Markers.semanticError(value.getPosition(), "arguments.duplicate.key", key));
					break;
				}
			}
		}
	}

	@Override
	public void appendValue(@NonNull String indent, @NonNull StringBuilder buffer, int index)
	{
		final Name key = this.keys[index];
		if (key != null)
		{
			buffer.append(key);
			Formatting.appendSeparator(buffer, "parameters.name_value_separator", ':');
		}

		this.values[index].toString(indent, buffer);
	}

	@Override
	protected void appendType(@NonNull StringBuilder buffer, int index)
	{
		final Name key = this.keys[index];

		if (key != null)
		{
			buffer.append(key).append(": ");
		}

		this.values[index].getType().toString("", buffer);
	}

	@Override
	public NamedArgumentList copy()
	{
		return new NamedArgumentList(Arrays.copyOf(this.keys, this.size), Arrays.copyOf(this.values, this.size),
		                             this.size);
	}

	@Override
	public NamedArgumentList toNamed()
	{
		return this;
	}
}