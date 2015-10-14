package dyvil.tools.dpf.ast.value;

import dyvil.collection.List;
import dyvil.collection.mutable.ArrayList;
import dyvil.tools.dpf.visitor.MapVisitor;
import dyvil.tools.dpf.visitor.ValueVisitor;

public class MapValue extends ValueCreator implements Value, MapVisitor
{
	private List<Value>	keys	= new ArrayList<Value>();
	private List<Value>	values	= new ArrayList<Value>();
	private boolean		valueMode;
	
	public MapValue()
	{
	}
	
	@Override
	public void setValue(Value value)
	{
		if (this.valueMode)
		{
			this.values.add(value);
		}
		else
		{
			this.keys.add(value);
		}
	}
	
	@Override
	public ValueVisitor visitKey()
	{
		this.valueMode = false;
		return this;
	}
	
	@Override
	public ValueVisitor visitValue()
	{
		this.valueMode = true;
		return this;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		int len = this.values.size();
		if (len <= 0)
		{
			buffer.append("[:]");
			return;
		}
		
		buffer.append("[ ");
		this.keys.get(0).toString(prefix, buffer);
		buffer.append(" : ");
		this.keys.get(0).toString(prefix, buffer);
		for (int i = 1; i < len; i++)
		{
			buffer.append(", ");
			this.keys.get(i).toString(prefix, buffer);
			buffer.append(" : ");
			this.values.get(i).toString(prefix, buffer);
		}
		buffer.append(" ]");
	}
}