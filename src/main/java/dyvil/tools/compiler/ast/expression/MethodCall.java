package dyvil.tools.compiler.ast.expression;

import java.util.Iterator;

import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.api.INamed;
import dyvil.tools.compiler.ast.api.IValued;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class MethodCall extends Call implements INamed, IValued
{
	protected IValue	instance;
	protected String	name;
	
	public MethodCall(ICodePosition position)
	{
		super(position);
	}
	
	public MethodCall(ICodePosition position, IValue instance, String name)
	{
		super(position);
		this.instance = instance;
		this.name = name;
	}
	
	@Override
	public Type getType()
	{
		return this.descriptor.getType();
	}
	
	@Override
	public void setName(String name)
	{
		this.name = name;
	}
	
	@Override
	public String getName()
	{
		return this.name;
	}
	
	@Override
	public void setValue(IValue value)
	{
		if (this.isSugarCall)
		{
			this.arguments.add(value);
		}
		else
		{
			this.instance = value;
		}
	}
	
	@Override
	public IValue getValue()
	{
		return this.instance;
	}
	
	@Override
	public MethodCall applyState(CompilerState state)
	{
		// TODO Operator precedence
		return this;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.isSugarCall && !Formatting.Method.convertSugarCalls)
		{
			if (this.instance != null)
			{
				this.instance.toString("", buffer);
				buffer.append(Formatting.Method.sugarCallStart);
			}
			
			buffer.append(this.name);
			buffer.append(Formatting.Method.sugarCallEnd);
			this.arguments.get(0).toString("", buffer);
		}
		else
		{
			if (this.instance != null)
			{
				this.instance.toString("", buffer);
				buffer.append('.');
			}
			buffer.append(this.name);
			
			if (!this.arguments.isEmpty())
			{
				// TODO Special seperators, named arguments
				buffer.append(Formatting.Method.parametersStart);
				Iterator<IValue> iterator = this.arguments.iterator();
				while (true)
				{
					IValue value = iterator.next();
					value.toString("", buffer);
					if (iterator.hasNext())
					{
						buffer.append(Formatting.Method.parameterSeperator);
					}
					else
					{
						break;
					}
				}
				buffer.append(Formatting.Method.parametersEnd);
			}
			else
			{
				buffer.append(Formatting.Method.emptyParameters);
			}
		}
	}
}
