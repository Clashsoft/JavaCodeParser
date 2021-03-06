package dyvilx.tools.compiler.ast.expression.constant;

import dyvil.annotation.internal.NonNull;
import dyvil.lang.Name;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.asm.AnnotationVisitor;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.context.IImplicitContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.expression.access.FieldAccess;
import dyvilx.tools.compiler.ast.field.IDataMember;
import dyvilx.tools.compiler.ast.generic.ITypeContext;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.typevar.CovariantTypeVarType;
import dyvilx.tools.compiler.backend.ClassFormat;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.parsing.marker.MarkerList;

public class EnumValue extends FieldAccess
{
	public EnumValue()
	{
	}

	public EnumValue(SourcePosition position, Name name)
	{
		this.position = position;
		this.name = name;
	}

	public EnumValue(IType type, Name name)
	{
		this.type = type;
		this.name = name;
	}

	public EnumValue(SourcePosition position, IDataMember field)
	{
		super(position, null, field);
	}

	public static <T extends Enum<T>> T eval(IValue value, Class<T> type)
	{
		if (!(value instanceof FieldAccess))
		{
			return null;
		}

		final String valueTypeName = ClassFormat.internalToPackage(value.getType().getInternalName());
		if (!valueTypeName.equals(type.getName()))
		{
			return null;
		}

		final String constantName = ((FieldAccess) value).getName().qualified;

		try
		{
			return Enum.valueOf(type, constantName);
		}
		catch (IllegalArgumentException ex)
		{
			return null;
		}
	}

	@Override
	public int valueTag()
	{
		return ENUM_ACCESS;
	}

	@Override
	public boolean isConstantOrField()
	{
		return true;
	}

	@Override
	public boolean isAnnotationConstant()
	{
		return true;
	}

	@Override
	public IValue toAnnotationConstant(MarkerList markers, IContext context, int depth)
	{
		return this;
	}

	@Override
	public boolean isPolyExpression()
	{
		return this.field == null;
	}

	@Override
	public boolean isResolved()
	{
		return true;
	}

	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		return this;
	}

	@Override
	public boolean isType(IType type)
	{
		if (this.field == null)
		{
			return type.resolveField(this.name) != null || type.canExtract(CovariantTypeVarType.class);
		}

		return super.isType(type);
	}

	@Override
	public int getTypeMatch(IType type, IImplicitContext implicitContext)
	{
		return this.isType(type) ? IValue.EXACT_MATCH : IValue.MISMATCH;
	}

	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (this.field == null)
		{
			final IDataMember field = type.resolveField(this.name);
			if (field == null)
			{
				markers.add(Markers.semanticError(this.position, "resolve.field", this.name));
				return null;
			}

			this.field = field;
		}
		return super.withType(type, typeContext, markers, context);
	}

	public String getInternalName()
	{
		return this.name.qualified;
	}

	@Override
	public Object toObject()
	{
		return null;
	}

	@Override
	public int stringSize()
	{
		return this.getInternalName().length();
	}

	@Override
	public boolean toStringBuilder(StringBuilder builder)
	{
		return false;
	}

	@Override
	public void writeAnnotationValue(AnnotationVisitor visitor, String key)
	{
		visitor.visitEnum(key, this.getType().getExtendedName(), this.getInternalName());
	}

	@Override
	public void toString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		buffer.append('.').append(this.name);
	}
}
