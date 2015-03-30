package dyvil.tools.compiler.ast.constant;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.ast.value.BoxValue;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.ast.value.LiteralValue;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class LongValue extends ASTNode implements INumericValue
{
	public static final Type	LONG_CONVERTIBLE	= new Type(Package.dyvilLangLiteral.resolveClass("LongConvertible"));
	
	private static LongValue	NULL;
	
	public long					value;
	
	public LongValue(long value)
	{
		this.value = value;
	}
	
	public LongValue(ICodePosition position, long value)
	{
		this.position = position;
		this.value = value;
	}
	
	public static LongValue getNull()
	{
		if (NULL == null)
		{
			NULL = new LongValue(0L);
		}
		return NULL;
	}
	
	@Override
	public int getValueType()
	{
		return LONG;
	}
	
	@Override
	public Type getType()
	{
		return Types.LONG;
	}
	
	@Override
	public IValue withType(IType type)
	{
		if (type == Types.LONG)
		{
			return this;
		}
		if (type.isSuperTypeOf(Types.LONG))
		{
			return new BoxValue(this, Types.LONG.boxMethod);
		}
		if (LONG_CONVERTIBLE.isSuperTypeOf(type))
		{
			return new LiteralValue(type, this);
		}
		return null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return type == Types.LONG || type.isSuperTypeOf(Types.LONG) || LONG_CONVERTIBLE.isSuperTypeOf(type);
	}
	
	@Override
	public int getTypeMatch(IType type)
	{
		if (type == Types.LONG)
		{
			return 3;
		}
		if (type.isSuperTypeOf(Types.LONG) || LONG_CONVERTIBLE.isSuperTypeOf(type))
		{
			return 2;
		}
		return 0;
	}
	
	@Override
	public int intValue()
	{
		return (int) this.value;
	}
	
	@Override
	public long longValue()
	{
		return this.value;
	}
	
	@Override
	public float floatValue()
	{
		return this.value;
	}
	
	@Override
	public double doubleValue()
	{
		return this.value;
	}
	
	@Override
	public Long toObject()
	{
		return Long.valueOf(this.value);
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
		writer.writeLDC(this.value);
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
		writer.writeLDC(this.value);
		writer.writeInsn(Opcodes.LRETURN);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.value).append('L');
	}
}
