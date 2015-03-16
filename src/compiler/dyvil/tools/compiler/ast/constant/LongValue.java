package dyvil.tools.compiler.ast.constant;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.BoxedValue;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class LongValue extends ASTNode implements INumericValue
{
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
		return Type.LONG;
	}
	
	@Override
	public IValue withType(IType type)
	{
		if (type == Type.LONG)
		{
			return this;
		}
		return type.isSuperTypeOf(Type.LONG) ? new BoxedValue(this, Type.LONG.boxMethod) : null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return type == Type.LONG || type.isSuperTypeOf(Type.LONG);
	}
	
	@Override
	public int getTypeMatch(IType type)
	{
		if (type == Type.LONG)
		{
			return 3;
		}
		if (type.isSuperTypeOf(Type.LONG))
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
