package dyvil.tools.compiler.ast.constructor;

import dyvil.annotation.internal.NonNull;
import dyvil.annotation.internal.Nullable;
import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.context.IDefaultContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.field.IVariable;
import dyvil.tools.compiler.ast.generic.GenericData;
import dyvil.tools.compiler.ast.generic.ITypeParameter;
import dyvil.tools.compiler.ast.generic.TypeParameterList;
import dyvil.tools.compiler.ast.header.IHeaderUnit;
import dyvil.tools.compiler.ast.member.Member;
import dyvil.tools.compiler.ast.method.Candidate;
import dyvil.tools.compiler.ast.method.MatchList;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.modifiers.ModifierUtil;
import dyvil.tools.compiler.ast.parameter.ArgumentList;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.parameter.ParameterList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.TypeList;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.transform.Deprecation;
import dyvil.tools.compiler.transform.Names;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

import java.lang.annotation.ElementType;

public abstract class AbstractConstructor extends Member implements IConstructor, IDefaultContext
{
	protected @NonNull ParameterList parameters = new ParameterList(3);

	protected @Nullable TypeList exceptions;

	// Metadata
	protected IClass enclosingClass;

	public AbstractConstructor(IClass enclosingClass)
	{
		super(Names.init, Types.VOID);
		this.enclosingClass = enclosingClass;
	}

	public AbstractConstructor(IClass enclosingClass, ModifierSet modifiers)
	{
		super(Names.init, Types.VOID, modifiers);
		this.enclosingClass = enclosingClass;
	}

	public AbstractConstructor(ICodePosition position, ModifierSet modifiers, AnnotationList annotations)
	{
		super(position, Names.init, Types.VOID, modifiers, annotations);
	}

	@Override
	public void setEnclosingClass(IClass enclosingClass)
	{
		this.enclosingClass = enclosingClass;
	}

	@Override
	public IClass getEnclosingClass()
	{
		return this.enclosingClass;
	}

	// Parameters

	@Override
	public ParameterList getParameters()
	{
		return this.parameters;
	}

	@Override
	public boolean addRawAnnotation(String type, IAnnotation annotation)
	{
		switch (type)
		{
		case Deprecation.JAVA_INTERNAL:
		case Deprecation.DYVIL_INTERNAL:
			this.modifiers.addIntModifier(Modifiers.DEPRECATED);
			return true;
		}
		return true;
	}

	@Override
	public ElementType getElementType()
	{
		return ElementType.METHOD;
	}

	@Override
	public TypeList getExceptions()
	{
		if (this.exceptions != null)
		{
			return this.exceptions;
		}
		return this.exceptions = new TypeList();
	}

	@Override
	public boolean isStatic()
	{
		return false;
	}

	@Override
	public IHeaderUnit getHeader()
	{
		return this.enclosingClass.getHeader();
	}

	@Override
	public IClass getThisClass()
	{
		return this.enclosingClass.getThisClass();
	}

	@Override
	public IType getThisType()
	{
		return this.enclosingClass.getThisType();
	}

	@Override
	public IDataMember resolveField(Name name)
	{
		return this.parameters.get(name);
	}

	@Override
	public void getConstructorMatches(MatchList<IConstructor> list, ArgumentList arguments)
	{
	}

	@Override
	public byte checkException(IType type)
	{
		if (this.exceptions == null)
		{
			return FALSE;
		}

		for (int i = 0; i < this.exceptions.size(); i++)
		{
			if (Types.isSuperType(this.exceptions.get(i), type))
			{
				return TRUE;
			}
		}
		return FALSE;
	}

	@Override
	public IType getReturnType()
	{
		return Types.VOID;
	}

	@Override
	public boolean isMember(IVariable variable)
	{
		return this.parameters.isParameter(variable);
	}

	@Override
	public IDataMember capture(IVariable variable)
	{
		return variable;
	}

	@Override
	public void checkMatch(MatchList<IConstructor> list, ArgumentList arguments)
	{
		final int parameterCount = this.parameters.size();
		final int argumentCount = arguments.size();

		if (argumentCount > parameterCount && !this.isVariadic())
		{
			return;
		}

		final int[] matchValues = new int[argumentCount];
		final IType[] matchTypes = new IType[argumentCount];

		int defaults = 0;
		int varargs = 0;
		for (int i = 0; i < parameterCount; i++)
		{
			final IParameter parameter = this.parameters.get(i);
			final int partialVarargs = arguments.checkMatch(matchValues, matchTypes, 0, i, parameter, list);

			if (partialVarargs >= 0)
			{
				varargs += partialVarargs;
				continue;
			}
			if (parameter.getValue() != null)
			{
				defaults++;
				continue;
			}

			return; // Mismatch
		}

		for (int matchValue : matchValues)
		{
			if (matchValue == IValue.MISMATCH)
			{
				return;
			}
		}
		list.add(new Candidate<>(this, matchValues, matchTypes, defaults, varargs));
	}

	@Override
	public IType checkArguments(MarkerList markers, ICodePosition position, IContext context, IType type,
		                           ArgumentList arguments)
	{
		final IClass theClass = this.enclosingClass;

		if (!theClass.isTypeParametric())
		{
			for (int i = 0, count = this.parameters.size(); i < count; i++)
			{
				arguments.checkValue(i, this.parameters.get(i), null, markers, context);
			}
			return type;
		}

		final IType classType = theClass.getThisType();
		final GenericData genericData = new GenericData(theClass);

		classType.inferTypes(type, genericData);
		genericData.lockAvailable();

		// Check Values and infer Types
		for (int i = 0, count = this.parameters.size(); i < count; i++)
		{
			arguments.checkValue(i, this.parameters.get(i), genericData, markers, context);
		}

		genericData.lockAvailable();

		// Check Type Var Inference and Compatibility
		final TypeParameterList typeParams = theClass.getTypeParameters();
		for (int i = 0, count = typeParams.size(); i < count; i++)
		{
			final ITypeParameter typeParameter = typeParams.get(i);
			final IType typeArgument = genericData.resolveType(typeParameter);

			if (typeArgument == null)
			{
				final IType inferredType = typeParameter.getUpperBound();
				markers.add(Markers.semantic(position, "constructor.typevar.infer", theClass.getName(),
				                             typeParameter.getName(), inferredType));
				genericData.addMapping(typeParameter, inferredType);
			}
			else if (!typeParameter.isAssignableFrom(typeArgument, genericData))
			{
				final Marker marker = Markers.semanticError(position, "constructor.typevar.incompatible",
				                                            theClass.getName(), typeParameter.getName());
				marker.addInfo(Markers.getSemantic("type.generic.argument", typeArgument));
				marker.addInfo(Markers.getSemantic("type_parameter.declaration", typeParameter));
				markers.add(marker);
			}
		}

		return classType.getConcreteType(genericData);
	}

	@Override
	public void checkCall(MarkerList markers, ICodePosition position, IContext context, ArgumentList arguments)
	{
		ModifierUtil.checkVisibility(this, position, markers, context);

		if (this.exceptions == null)
		{
			return;
		}

		for (int i = 0; i < this.exceptions.size(); i++)
		{
			IType exceptionType = this.exceptions.get(i);
			if (IContext.isUnhandled(context, exceptionType))
			{
				markers.add(Markers.semanticError(position, "exception.unhandled", exceptionType.toString()));
			}
		}
	}

	@Override
	public String getDescriptor()
	{
		StringBuilder buffer = new StringBuilder();
		buffer.append('(');
		this.parameters.appendDescriptor(buffer);
		buffer.append(")V");
		return buffer.toString();
	}

	@Override
	public String getSignature()
	{
		if (!this.enclosingClass.isTypeParametric() && !this.parameters.needsSignature())
		{
			return null;
		}

		StringBuilder buffer = new StringBuilder();
		buffer.append('(');
		this.parameters.appendSignature(buffer);
		buffer.append(")V");
		return buffer.toString();
	}

	@Override
	public String[] getInternalExceptions()
	{
		if (this.exceptions == null)
		{
			return null;
		}

		final int count = this.exceptions.size();
		if (count == 0)
		{
			return null;
		}

		final String[] array = new String[count];
		for (int i = 0; i < count; i++)
		{
			array[i] = this.exceptions.get(i).getInternalName();
		}
		return array;
	}

	@Override
	public void writeCall(MethodWriter writer, ArgumentList arguments, IType type, int lineNumber)
		throws BytecodeException
	{
		writer.visitTypeInsn(Opcodes.NEW, this.enclosingClass.getInternalName());
		if (type != Types.VOID)
		{
			writer.visitInsn(Opcodes.DUP);
		}

		this.writeArguments(writer, arguments);
		this.writeInvoke(writer, lineNumber);
	}

	@Override
	public void writeInvoke(MethodWriter writer, int lineNumber) throws BytecodeException
	{
		writer.visitLineNumber(lineNumber);

		String owner = this.enclosingClass.getInternalName();
		String name = "<init>";
		String desc = this.getDescriptor();
		writer.visitMethodInsn(Opcodes.INVOKESPECIAL, owner, name, desc, false);
	}

	@Override
	public void writeArguments(MethodWriter writer, ArgumentList arguments) throws BytecodeException
	{
		for (int i = 0, count = this.parameters.size(); i < count; i++)
		{
			arguments.writeValue(i, this.parameters.get(i), writer);
		}
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		super.toString(prefix, buffer);
		buffer.append("init");

		this.parameters.toString(prefix, buffer);

		if (this.exceptions != null && this.exceptions.size() > 0)
		{
			String throwsPrefix = prefix;
			if (Formatting.getBoolean("constructor.throws.newline"))
			{
				throwsPrefix = Formatting.getIndent("constructor.throws.indent", prefix);
				buffer.append('\n').append(throwsPrefix).append("throws ");
			}
			else
			{
				buffer.append(" throws ");
			}

			Util.astToString(throwsPrefix, this.exceptions.getTypes(), this.exceptions.size(),
			                 Formatting.getSeparator("constructor.throws", ','), buffer);
		}

		final IValue value = this.getValue();
		if (value != null)
		{
			if (!Util.formatStatementList(prefix, buffer, value))
			{
				buffer.append(" = ");
				value.toString(prefix, buffer);
			}
		}

		if (Formatting.getBoolean("constructor.semicolon"))
		{
			buffer.append(';');
		}
	}
}
