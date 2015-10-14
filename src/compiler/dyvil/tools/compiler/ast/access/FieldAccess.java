package dyvil.tools.compiler.ast.access;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.constant.EnumValue;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.IValued;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.member.INamed;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.parameter.EmptyArguments;
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

public final class FieldAccess implements IValue, INamed, IValued
{
	protected ICodePosition	position;
	protected IValue		instance;
	protected Name			name;
	
	protected boolean dotless;
	
	// Metadata
	protected IDataMember	field;
	protected IType			type;
	
	public FieldAccess()
	{
	}
	
	public FieldAccess(ICodePosition position)
	{
		this.position = position;
	}
	
	public FieldAccess(ICodePosition position, IValue instance, Name name)
	{
		this.position = position;
		this.instance = instance;
		this.name = name;
	}
	
	public FieldAccess(ICodePosition position, IValue instance, IDataMember field)
	{
		this.position = position;
		this.instance = instance;
		this.field = field;
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
	
	public MethodCall toMethodCall(IMethod method)
	{
		MethodCall call = new MethodCall(this.position);
		call.instance = this.instance;
		call.name = this.name;
		call.method = method;
		call.dotless = this.dotless;
		call.arguments = EmptyArguments.INSTANCE;
		return call;
	}
	
	@Override
	public int valueTag()
	{
		return FIELD_ACCESS;
	}
	
	public IValue getInstance()
	{
		return this.instance;
	}
	
	public IDataMember getField()
	{
		return this.field;
	}
	
	public boolean isDotless()
	{
		return this.dotless;
	}
	
	public void setDotless(boolean dotless)
	{
		this.dotless = dotless;
	}
	
	@Override
	public boolean isConstantOrField()
	{
		return this.field != null && this.field.hasModifier(Modifiers.CONST);
	}
	
	@Override
	public IType getType()
	{
		if (this.type == null)
		{
			if (this.field == null)
			{
				return Types.UNKNOWN;
			}
			if (this.instance == null)
			{
				return this.type = this.field.getType();
			}
			return this.type = this.field.getType().getConcreteType(this.instance.getType()).getReturnType();
		}
		return this.type;
	}
	
	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		return type.isSuperTypeOf(this.getType()) ? this : null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return this.field == null ? false : type.isSuperTypeOf(this.getType());
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
	public void setValue(IValue value)
	{
		this.instance = value;
	}
	
	@Override
	public IValue getValue()
	{
		return this.instance;
	}
	
	@Override
	public IValue toConstant(MarkerList markers)
	{
		int depth = DyvilCompiler.maxConstantDepth;
		IValue v = this;
		
		do
		{
			if (depth-- < 0)
			{
				markers.add(I18n.createMarker(this.getPosition(), "annotation.field.not_constant", this.name));
				return this;
			}
			
			v = v.foldConstants();
		}
		while (!v.isConstantOrField());
		
		return v.toConstant(markers);
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		if (this.instance != null)
		{
			this.instance.resolveTypes(markers, context);
		}
	}
	
	private IValue resolveMethod(MarkerList markers, IContext context)
	{
		IMethod method = ICall.resolveMethod(context, this.instance, this.name, EmptyArguments.INSTANCE);
		if (method != null)
		{
			AbstractCall mc = this.toMethodCall(method);
			mc.checkArguments(markers, context);
			return mc;
		}
		return null;
	}
	
	private IValue resolveField(MarkerList markers, IContext context)
	{
		IDataMember field = ICall.resolveField(context, this.instance, this.name);
		if (field != null)
		{
			if (field.isEnumConstant())
			{
				EnumValue enumValue = new EnumValue(field.getType(), this.name);
				return enumValue;
			}
			
			this.field = field;
			return this;
		}
		return null;
	}
	
	protected IValue resolveFieldAccess(MarkerList markers, IContext context)
	{
		if (ICall.privateAccess(context, this.instance))
		{
			IValue value = this.resolveField(markers, context);
			if (value != null)
			{
				return value;
			}
			value = this.resolveMethod(markers, context);
			if (value != null)
			{
				return value;
			}
		}
		else
		{
			IValue value = this.resolveMethod(markers, context);
			if (value != null)
			{
				return value;
			}
			value = this.resolveField(markers, context);
			if (value != null)
			{
				return value;
			}
		}
		
		if (this.instance == null)
		{
			IClass iclass = IContext.resolveClass(context, this.name);
			if (iclass != null)
			{
				return new ClassAccess(this.position, iclass.getType());
			}
		}
		
		return null;
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		if (this.instance != null)
		{
			this.instance = this.instance.resolve(markers, context);
		}
		
		IValue v = this.resolveFieldAccess(markers, context);
		if (v != null)
		{
			return v;
		}
		
		Marker marker = I18n.createMarker(this.position, "resolve.method_field", this.name.unqualified);
		marker.addInfo("Qualified Name: " + this.name.qualified);
		if (this.instance != null)
		{
			marker.addInfo("Instance Type: " + this.instance.getType());
		}
		
		markers.add(marker);
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		if (this.instance != null)
		{
			this.instance.checkTypes(markers, context);
		}
		
		if (this.field != null)
		{
			this.field = this.field.capture(context);
			this.instance = this.field.checkAccess(markers, this.position, this.instance, context);
		}
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		if (this.instance != null)
		{
			this.instance.check(markers, context);
		}
	}
	
	@Override
	public IValue foldConstants()
	{
		if (this.field != null && this.field.hasModifier(Modifiers.CONST))
		{
			IValue v = this.field.getValue();
			return v != null && v.isConstantOrField() ? v : this;
		}
		if (this.instance != null)
		{
			this.instance = this.instance.foldConstants();
		}
		return this;
	}
	
	@Override
	public IValue cleanup(IContext context, IClassCompilableList compilableList)
	{
		if (this.instance != null)
		{
			this.instance = this.instance.cleanup(context, compilableList);
		}
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer) throws BytecodeException
	{
		int lineNumber = this.getLineNumber();
		this.field.writeGet(writer, this.instance, lineNumber);
		
		if (this.type != null)
		{
			this.field.getType().writeCast(writer, this.type, lineNumber);
		}
	}
	
	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		IType t = this.field.getType();
		this.writeExpression(writer, t);
		writer.writeInsn(t.getReturnOpcode());
	}
	
	@Override
	public String toString()
	{
		return IASTNode.toString(this);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.instance != null)
		{
			this.instance.toString("", buffer);
			if (this.dotless && !Formatting.Field.useJavaFormat)
			{
				buffer.append(Formatting.Field.dotlessSeperator);
			}
			else
			{
				buffer.append('.');
			}
		}
		
		buffer.append(this.name);
	}
}
