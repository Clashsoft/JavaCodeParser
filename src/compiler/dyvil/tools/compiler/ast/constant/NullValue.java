package dyvil.tools.compiler.ast.constant;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class NullValue extends ASTNode implements IConstantValue
{
	private static NullValue	NULL;
	
	public NullValue()
	{
	}
	
	public NullValue(ICodePosition position)
	{
		this.position = position;
	}
	
	public static NullValue getNull()
	{
		if (NULL == null)
		{
			NULL = new NullValue();
		}
		return NULL;
	}
	
	@Override
	public int getValueType()
	{
		return IValue.NULL;
	}
	
	@Override
	public boolean isPrimitive()
	{
		return false;
	}
	
	@Override
	public IType getType()
	{
		return Type.NONE;
	}
	
	@Override
	public IValue withType(IType type)
	{
		return type.isPrimitive() ? null : this;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return !type.isPrimitive();
	}
	
	@Override
	public int getTypeMatch(IType type)
	{
		return type.isPrimitive() ? 0 : 2;
	}
	
	@Override
	public Object toObject()
	{
		return null;
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
		writer.writeInsn(Opcodes.ACONST_NULL);
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
		writer.writeInsn(Opcodes.ACONST_NULL);
		writer.writeInsn(Opcodes.RETURN);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("null");
	}
}
