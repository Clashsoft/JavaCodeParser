package dyvil.tools.dpf.ast.value;

import dyvil.tools.dpf.visitor.ValueVisitor;

public class LongValue implements Value
{
	protected long value;
	
	public LongValue(long value)
	{
		this.value = value;
	}
	
	public long getValue()
	{
		return this.value;
	}
	
	public void setValue(long value)
	{
		this.value = value;
	}
	
	@Override
	public void accept(ValueVisitor visitor)
	{
		visitor.visitLong(this.value);
	}
	
	@Override
	public String toString()
	{
		return this.value + "L";
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.value).append('L');
	}
}
