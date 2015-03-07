package dyvil.tools.compiler.ast.parameter;

import java.lang.annotation.ElementType;
import java.util.List;

import jdk.internal.org.objectweb.asm.ClassWriter;
import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.field.IVariable;
import dyvil.tools.compiler.ast.member.Member;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.Markers;

public class Parameter extends Member implements IVariable
{
	public IMethod	method;
	
	public int		index;
	public char		seperator;
	public boolean	varargs;
	
	public IValue	defaultValue;
	
	public Parameter()
	{
		super(null);
	}
	
	public Parameter(int index, String name, IType type)
	{
		super(null, name, type);
		this.index = index;
	}
	
	@Override
	public void setValue(IValue value)
	{
		this.defaultValue = value;
	}
	
	@Override
	public IValue getValue()
	{
		return this.defaultValue;
	}
	
	@Override
	public void setIndex(int index)
	{
		this.index = index;
	}
	
	@Override
	public int getIndex()
	{
		return this.index;
	}
	
	public void setSeperator(char seperator)
	{
		this.seperator = seperator;
	}
	
	public char getSeperator()
	{
		return this.seperator;
	}
	
	public void setVarargs()
	{
		this.type = this.type.clone();
		this.type.addArrayDimension();
		this.varargs = true;
	}
	
	public void setVarargs2()
	{
		this.varargs = true;
	}
	
	public boolean isVarargs()
	{
		return this.varargs;
	}
	
	@Override
	public String getDescription()
	{
		return this.type.getExtendedName();
	}
	
	@Override
	public String getSignature()
	{
		return this.type.getSignature();
	}
	
	@Override
	public boolean processAnnotation(Annotation annotation)
	{
		String name = annotation.type.fullName;
		if ("dyvil.lang.annotation.byref".equals(name))
		{
			this.modifiers |= Modifiers.BYREF;
			return true;
		}
		return false;
	}
	
	@Override
	public ElementType getAnnotationType()
	{
		return ElementType.PARAMETER;
	}
	
	@Override
	public void resolveTypes(List<Marker> markers, IContext context)
	{
		super.resolveTypes(markers, context);
		
		if (this.defaultValue != null)
		{
			this.defaultValue.resolveTypes(markers, context);
		}
	}
	
	@Override
	public void resolve(List<Marker> markers, IContext context)
	{
		super.resolve(markers, context);
		
		if (this.defaultValue != null)
		{
			this.defaultValue = this.defaultValue.resolve(markers, context);
		}
	}
	
	@Override
	public void check(List<Marker> markers, IContext context)
	{
		super.check(markers, context);
		
		if (this.defaultValue != null)
		{
			IValue value1 = this.defaultValue.withType(this.type);
			if (value1 == null)
			{
				Marker marker = Markers.create(this.defaultValue.getPosition(), "parameter.type", this.name);
				marker.addInfo("Parameter Type: " + this.type);
				marker.addInfo("Value Type: " + this.defaultValue.getType());
				markers.add(marker);
			}
			else
			{
				this.defaultValue = value1;
			}
			
			this.defaultValue.check(markers, context);
		}
	}
	
	@Override
	public void write(ClassWriter writer)
	{
		if (this.defaultValue == null)
		{
			return;
		}
		
		// Copy the access modifiers and add the STATIC modifier
		int modifiers = this.method.getModifiers() & Modifiers.ACCESS_MODIFIERS | Modifiers.STATIC;
		String name = "parDefault$" + this.method.getQualifiedName() + "$" + this.index;
		String desc = "()" + this.type.getExtendedName();
		MethodWriter mw = new MethodWriter(writer, writer.visitMethod(modifiers, name, desc, null, null));
		mw.visitCode();
		this.defaultValue.writeExpression(mw);
		mw.visitEnd(this.type);
	}
	
	public void write(MethodWriter writer)
	{
		this.index = writer.visitParameter(this.name, this.type);
		
		if ((this.modifiers & Modifiers.BYREF) != 0)
		{
			writer.visitParameterAnnotation(this.index, "Ldyvil/lang/annotation/byref;", true);
		}
		
		for (Annotation a : this.annotations)
		{
			a.write(writer, this.index);
		}
	}
	
	@Override
	public void writeGet(MethodWriter writer, IValue instance)
	{
		writer.visitVarInsn(this.type.getLoadOpcode(), this.index, this.type);
	}
	
	@Override
	public void writeSet(MethodWriter writer, IValue instance, IValue value)
	{
		value.writeExpression(writer);
		
		writer.visitVarInsn(this.type.getStoreOpcode(), this.index, null);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		for (int i = 0; i < this.annotationCount; i++)
		{
			this.annotations[i].toString(prefix, buffer);
			buffer.append(' ');
		}
		
		if (this.varargs)
		{
			this.type.getElementType().toString(prefix, buffer);
			buffer.append("... ");
		}
		else
		{
			this.type.toString(prefix, buffer);
			buffer.append(' ');
		}
		buffer.append(this.name);
		
		if (this.defaultValue != null)
		{
			buffer.append(Formatting.Field.keyValueSeperator).append(' ');
			this.defaultValue.toString(prefix, buffer);
		}
	}
}
