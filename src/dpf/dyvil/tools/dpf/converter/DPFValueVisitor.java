package dyvil.tools.dpf.converter;

import dyvil.tools.dpf.ast.builder.Builder;
import dyvil.tools.dpf.ast.value.*;
import dyvil.tools.dpf.ast.value.NameAccess;
import dyvil.tools.dpf.ast.value.StringInterpolation;
import dyvil.tools.dpf.visitor.*;
import dyvil.tools.parsing.Name;

public abstract class DPFValueVisitor implements ValueVisitor
{
	protected abstract void setValue(Value value);
	
	@Override
	public void visitInt(int value)
	{
		this.setValue(new IntValue(value));
	}
	
	@Override
	public void visitLong(long value)
	{
		this.setValue(new LongValue(value));
	}
	
	@Override
	public void visitFloat(float value)
	{
		this.setValue(new FloatValue(value));
	}
	
	@Override
	public void visitDouble(double value)
	{
		this.setValue(new DoubleValue(value));
	}
	
	@Override
	public void visitString(String value)
	{
		this.setValue(new StringValue(value));
	}
	
	@Override
	public StringInterpolationVisitor visitStringInterpolation()
	{
		dyvil.tools.dpf.ast.value.StringInterpolation stringInterpolation = new StringInterpolation();
		this.setValue(stringInterpolation);
		return stringInterpolation;
	}
	
	@Override
	public void visitName(Name name)
	{
		this.setValue(new dyvil.tools.dpf.ast.value.NameAccess(name));
	}
	
	@Override
	public ValueVisitor visitValueAccess(Name name)
	{
		dyvil.tools.dpf.ast.value.NameAccess access = new NameAccess(name);
		this.setValue(access);
		return access;
	}
	
	@Override
	public ListVisitor visitList()
	{
		ListValue list = new ListValue();
		this.setValue(list);
		return list;
	}
	
	@Override
	public MapVisitor visitMap()
	{
		MapValue map = new MapValue();
		this.setValue(map);
		return map;
	}
	
	@Override
	public BuilderVisitor visitBuilder(Name name)
	{
		Builder builder = new Builder(name);
		this.setValue(builder);
		return builder;
	}
}