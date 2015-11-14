package dyvil.tools.compiler.ast.access;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.member.INamed;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.SingleArgument;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.util.I18n;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public final class FieldAssignment implements IValue, INamed, IReceiverAccess, IValueConsumer
{
	protected ICodePosition position;
	
	protected IValue	receiver;
	protected Name		name;
	protected IValue	value;
	
	protected IDataMember field;
	
	public FieldAssignment(ICodePosition position)
	{
		this.position = position;
	}
	
	public FieldAssignment(ICodePosition position, IValue instance, Name name)
	{
		this.position = position;
		this.receiver = instance;
		this.name = name;
	}
	
	public FieldAssignment(ICodePosition position, IValue instance, IDataMember field, IValue value)
	{
		this.position = position;
		this.receiver = instance;
		this.field = field;
		this.name = field.getName();
		this.value = value;
	}
	
	@Override
	public ICodePosition getPosition()
	{
		return this.position;
	}
	
	@Override
	public void setPosition(ICodePosition position)
	{
		this.position = position;
	}
	
	@Override
	public int valueTag()
	{
		return FIELD_ASSIGN;
	}
	
	@Override
	public boolean isPrimitive()
	{
		return this.field == null ? false : this.field.getType().isPrimitive();
	}
	
	@Override
	public boolean isResolved()
	{
		return this.field != null;
	}
	
	@Override
	public IType getType()
	{
		return this.field == null ? Types.UNKNOWN : this.field.getType();
	}
	
	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (type == Types.VOID)
		{
			return this;
		}
		
		IValue value1 = this.value.withType(type, typeContext, markers, context);
		if (value1 == null)
		{
			return null;
		}
		this.value = value1;
		return this;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return type == Types.VOID || this.value.isType(type);
	}
	
	@Override
	public float getTypeMatch(IType type)
	{
		return this.value.getTypeMatch(type);
	}
	
	@Override
	public void setName(Name name)
	{
		this.name = name;
	}
	
	@Override
	public Name getName()
	{
		return this.name;
	}
	
	@Override
	public void setReceiver(IValue value)
	{
		this.value = value;
	}
	
	@Override
	public IValue getReceiver()
	{
		return this.value;
	}
	
	@Override
	public void setValue(IValue value)
	{
		this.value = value;
	}
	
	public IValue getValue()
	{
		return this.value;
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		if (this.field != null)
		{
			this.field.resolveTypes(markers, context);
		}
		else
		{
			if (this.receiver != null)
			{
				this.receiver.resolveTypes(markers, context);
			}
			
			if (this.value != null)
			{
				this.value.resolveTypes(markers, context);
			}
		}
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		if (this.receiver != null)
		{
			this.receiver = this.receiver.resolve(markers, context);
		}
		
		if (!ICall.privateAccess(context, this.receiver))
		{
			Name name = Name.getQualified(this.name.qualified + "_$eq");
			IArguments arg = new SingleArgument(this.value);
			IMethod m = ICall.resolveMethod(context, this.receiver, name, arg);
			if (m != null)
			{
				MethodCall mc = new MethodCall(this.position, this.receiver, name);
				mc.arguments = arg;
				mc.method = m;
				mc.checkArguments(markers, context);
				return mc;
			}
		}
		
		this.field = ICall.resolveField(context, this.receiver, this.name);
		
		if (this.field == null)
		{
			Marker marker = I18n.createMarker(this.position, "resolve.field", this.name.unqualified);
			marker.addInfo(I18n.getString("name.qualified", this.name.qualified));
			if (this.receiver != null)
			{
				marker.addInfo(I18n.getString("receiver.type", this.receiver.getType()));
			}
			
			markers.add(marker);
		}
		
		if (this.value != null)
		{
			this.value = this.value.resolve(markers, context);
		}
		
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		if (this.receiver != null)
		{
			this.receiver.checkTypes(markers, context);
		}
		
		if (this.field != null)
		{
			this.field = this.field.capture(context);
			this.receiver = this.field.checkAccess(markers, this.position, this.receiver, context);
			this.value = this.field.checkAssign(markers, context, this.position, this.receiver, this.value);
		}
		
		if (this.value != null)
		{
			this.value.checkTypes(markers, context);
		}
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		if (this.receiver != null)
		{
			this.receiver.check(markers, context);
		}
		if (this.value != null)
		{
			this.value.check(markers, context);
		}
	}
	
	@Override
	public IValue foldConstants()
	{
		if (this.receiver != null)
		{
			this.receiver = this.receiver.foldConstants();
		}
		this.value = this.value.foldConstants();
		return this;
	}
	
	@Override
	public IValue cleanup(IContext context, IClassCompilableList compilableList)
	{
		if (this.receiver != null)
		{
			this.receiver = this.receiver.cleanup(context, compilableList);
		}
		this.value = this.value.cleanup(context, compilableList);
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer) throws BytecodeException
	{
		if (this.receiver == null)
		{
			this.value.writeExpression(writer, this.field.getType());
			writer.writeInsn(Opcodes.AUTO_DUP);
		}
		else
		{
			this.receiver.writeExpression(writer);
			this.value.writeExpression(writer);
			writer.writeInsn(Opcodes.AUTO_DUP_X1);
		}
		this.field.writeSet(writer, null, null, this.getLineNumber());
	}
	
	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		if (this.value == null)
		{
			return;
		}
		
		this.field.writeSet(writer, this.receiver, this.value, this.getLineNumber());
	}
	
	@Override
	public String toString()
	{
		return IASTNode.toString(this);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.receiver != null)
		{
			this.receiver.toString("", buffer);
			buffer.append('.');
		}
		
		buffer.append(this.name);
		
		if (this.value != null)
		{
			buffer.append(Formatting.Field.keyValueSeperator);
			this.value.toString(prefix, buffer);
		}
	}
}