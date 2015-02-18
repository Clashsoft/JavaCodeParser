package dyvil.tools.compiler.ast.type;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import dyvil.tools.compiler.ast.classes.CaptureClass;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.generic.WildcardType;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.Markers;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.util.Util;

public class GenericType extends Type implements ITypeList
{
	public List<IType>	generics;
	
	public GenericType()
	{
		super();
	}
	
	public GenericType(String name)
	{
		super(name);
	}
	
	public GenericType(ICodePosition position, String name)
	{
		super(position, name);
	}
	
	public GenericType(IClass iclass)
	{
		super(iclass);
	}
	
	@Override
	public void setTypes(List<IType> types)
	{
		this.generics = types;
	}
	
	@Override
	public List<IType> getTypes()
	{
		return this.generics;
	}
	
	@Override
	public void addType(IType type)
	{
		if (this.generics == null)
		{
			this.generics = new ArrayList(2);
		}
		this.generics.add(type);
	}
	
	@Override
	public IType resolve(List<Marker> markers, IContext context)
	{
		if (this.theClass != null)
		{
			return this;
		}
		
		IClass iclass;
		if (this.fullName != null)
		{
			iclass = Package.rootPackage.resolveClass(this.fullName);
		}
		else
		{
			iclass = context.resolveClass(this.qualifiedName);
		}
		
		if (iclass != null)
		{
			this.theClass = iclass;
			this.fullName = iclass.getFullName();
			
			if (iclass instanceof CaptureClass)
			{
				return new WildcardType(this.position, this.arrayDimensions, (CaptureClass) iclass);
			}
			
			if (this.generics == null)
			{
				return this;
			}
			
			List<ITypeVariable> variables = iclass.getTypeVariables();
			int len = this.generics.size();
			if (variables == null)
			{
				if (len != 0 && markers != null)
				{
					markers.add(Markers.create(this.position, "generic.not_generic", this.qualifiedName));
				}
				return this;
			}
			if (variables.size() != len && markers != null)
			{
				markers.add(Markers.create(this.position, "generic.count"));
				return this;
			}
			
			for (int i = 0; i < len; i++)
			{
				IType t1 = this.generics.get(i);
				IType t2 = t1.resolve(markers, context);
				if (t1 != t2)
				{
					this.generics.set(i, t2);
				}
				
				if (markers != null)
				{
					ITypeVariable var = variables.get(i);
					if (!var.isSuperTypeOf(t2))
					{
						Marker marker = Markers.create(t1.getPosition(), "generic.type", var.getQualifiedName());
						marker.addInfo("Generic Type: " + t2);
						marker.addInfo("Type Variable: " + var);
						markers.add(marker);
					}
				}
			}
			return this;
		}
		if (markers != null)
		{
			markers.add(Markers.create(this.position, "resolve.type", this.toString()));
		}
		return this;
	}
	
	@Override
	public boolean isGeneric()
	{
		return this.theClass == null || this.theClass.isGeneric();
	}
	
	@Override
	public void addTypeVariables(IType type, Map<String, IType> typeVariables)
	{
		if (type instanceof ITypeVariable)
		{
			if (type.isSuperTypeOf(this))
			{
				if (this.arrayDimensions > 0)
				{
					typeVariables.put(type.getQualifiedName(), this.getArrayType(this.arrayDimensions - type.getArrayDimensions()));
				}
				else
				{
					typeVariables.put(type.getQualifiedName(), this);
				}
			}
		}
		else if (type instanceof GenericType)
		{
			List<IType> types = ((GenericType) type).generics;
			int len = Math.min(this.generics.size(), types.size());
			for (int i = 0; i < len; i++)
			{
				IType t1 = types.get(i);
				IType t2 = this.generics.get(i);
				System.out.println(t1 + " " + t2);
				t1.addTypeVariables(t2, typeVariables);
				t2.addTypeVariables(t1, typeVariables);
			}
		}
	}
	
	@Override
	public String getSignature()
	{
		if (this.generics == null)
		{
			return null;
		}
		
		StringBuilder buf = new StringBuilder();
		this.appendSignature(buf);
		return buf.toString();
	}
	
	@Override
	public void appendSignature(StringBuilder buf)
	{
		for (int i = 0; i < this.arrayDimensions; i++)
		{
			buf.append('[');
		}
		buf.append('L').append(this.getInternalName());
		if (this.generics != null)
		{
			buf.append('<');
			for (IType t : this.generics)
			{
				t.appendSignature(buf);
			}
			buf.append('>');
		}
		buf.append(';');
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		for (int i = 0; i < this.arrayDimensions; i++)
		{
			buffer.append('[');
		}
		buffer.append(this.name);
		if (this.generics != null)
		{
			buffer.append('[');
			Util.astToString(this.generics, Formatting.Type.genericSeperator, buffer);
			buffer.append(']');
		}
		for (int i = 0; i < this.arrayDimensions; i++)
		{
			buffer.append(']');
		}
	}
}
