package dyvil.tools.compiler.ast.value;

import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class FloatValue extends ASTNode implements INumericValue
{
	public float	value;
	
	public FloatValue(float value)
	{
		this.value = value;
	}
	
	public FloatValue(ICodePosition position, float value)
	{
		this.position = position;
		this.value = value;
	}
	
	@Override
	public Type getType()
	{
		return Type.FLOAT;
	}
	
	@Override
	public int getValueType()
	{
		return FLOAT;
	}
	
	@Override
	public int intValue()
	{
		return (int) this.value;
	}
	
	@Override
	public long longValue()
	{
		return (long) this.value;
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
	public Float toObject()
	{
		return Float.valueOf(this.value);
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
		writer.visitLdcInsn(this.value);
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.value).append('F');
	}
}