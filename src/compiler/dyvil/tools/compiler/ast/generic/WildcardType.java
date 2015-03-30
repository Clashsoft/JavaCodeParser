package dyvil.tools.compiler.ast.generic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.field.FieldMatch;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.ConstructorMatch;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public final class WildcardType extends BaseBounded implements IType
{
	public int	arrayDimensions;
	
	public WildcardType()
	{
	}
	
	public WildcardType(ICodePosition position)
	{
		this.position = position;
	}
	
	@Override
	public void setName(Name name)
	{
	}
	
	@Override
	public Name getName()
	{
		return null;
	}
	
	@Override
	public void setClass(IClass theClass)
	{
	}
	
	@Override
	public IClass getTheClass()
	{
		return null;
	}
	
	@Override
	public void setFullName(String name)
	{
	}
	
	@Override
	public String getFullName()
	{
		return "_";
	}
	
	@Override
	public void setArrayDimensions(int dimensions)
	{
		this.arrayDimensions = dimensions;
	}
	
	@Override
	public int getArrayDimensions()
	{
		return this.arrayDimensions;
	}
	
	@Override
	public IType getElementType()
	{
		WildcardType t = this.clone();
		t.arrayDimensions--;
		return t;
	}
	
	@Override
	public IType getArrayType()
	{
		WildcardType t = this.clone();
		t.arrayDimensions++;
		return t;
	}
	
	@Override
	public IType getArrayType(int dimensions)
	{
		WildcardType t = this.clone();
		t.arrayDimensions = dimensions;
		return t;
	}
	
	@Override
	public boolean isArrayType()
	{
		return this.arrayDimensions > 0;
	}
	
	@Override
	public IType getSuperType()
	{
		return this.upperBoundCount == 0 ? Types.UNKNOWN : this.upperBounds[0];
	}
	
	@Override
	public boolean equals(IType type)
	{
		if (this.arrayDimensions != type.getArrayDimensions())
		{
			return false;
		}
		if (this.upperBoundCount > 0)
		{
			for (int i = 0; i < this.upperBoundCount; i++)
			{
				if (!this.upperBounds[i].isSuperTypeOf(type))
				{
					return false;
				}
			}
		}
		if (this.lowerBound != null)
		{
			if (!type.isSuperTypeOf(this.lowerBound))
			{
				return false;
			}
		}
		return !type.isPrimitive();
	}
	
	@Override
	public boolean isSuperTypeOf(IType type)
	{
		if (this.arrayDimensions != type.getArrayDimensions())
		{
			return false;
		}
		return super.isSuperTypeOf(type);
	}
	
	@Override
	public IType resolveType(Name name)
	{
		IType type;
		for (int i = 0; i < this.upperBoundCount; i++)
		{
			type = this.upperBounds[i].resolveType(name);
			if (type != null)
			{
				return type;
			}
		}
		return null;
	}

	@Override
	public IType resolveType(Name name, IType concrete)
	{
		IType type;
		for (int i = 0; i < this.upperBoundCount; i++)
		{
			type = this.upperBounds[i].resolveType(name, concrete);
			if (type != null)
			{
				return type;
			}
		}
		return null;
	}

	@Override
	public boolean hasTypeVariables()
	{
		return false;
	}

	@Override
	public IType getConcreteType(ITypeContext context)
	{
		if (this.lowerBound != null)
		{
			return this.lowerBound.getConcreteType(context);
		}
		
		WildcardType type = new WildcardType(this.position);
		type.arrayDimensions = this.arrayDimensions;
		type.upperBounds = new IType[this.upperBoundCount];
		type.upperBoundCount = this.upperBoundCount;
		for (int i = 0; i < this.upperBoundCount; i++)
		{
			type.upperBounds[i] = this.upperBounds[i].getConcreteType(context);
		}
		return type;
	}

	@Override
	public boolean isResolved()
	{
		return true;
	}

	@Override
	public IType resolve(MarkerList markers, IContext context)
	{
		this.resolveTypes(markers, context);
		return this;
	}
	
	@Override
	public boolean isStatic()
	{
		return true;
	}
	
	@Override
	public IType getThisType()
	{
		return this;
	}
	
	@Override
	public Package resolvePackage(Name name)
	{
		return null;
	}
	
	@Override
	public IClass resolveClass(Name name)
	{
		return null;
	}
	
	@Override
	public ITypeVariable resolveTypeVariable(Name name)
	{
		for (int i = 0; i < this.upperBoundCount; i++)
		{
			ITypeVariable var = this.upperBounds[i].resolveTypeVariable(name);
			if (var != null)
			{
				return var;
			}
		}
		
		return null;
	}
	
	@Override
	public FieldMatch resolveField(Name name)
	{
		if (this.arrayDimensions > 0 || this.upperBoundCount == 0)
		{
			return null;
		}
		
		for (int i = 0; i < this.upperBoundCount; i++)
		{
			FieldMatch f = this.upperBounds[i].resolveField(name);
			if (f != null)
			{
				return f;
			}
		}
		
		return null;
	}
	
	@Override
	public MethodMatch resolveMethod(IValue instance, Name name, IArguments arguments)
	{
		if (this.arrayDimensions > 0)
		{
			return Types.ARRAY_CLASS.resolveMethod(instance, name, arguments);
		}
		
		List<MethodMatch> list = new ArrayList();
		this.getMethodMatches(list, instance, name, arguments);
		
		if (!list.isEmpty())
		{
			Collections.sort(list);
			return list.get(0);
		}
		
		return null;
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, Name name, IArguments arguments)
	{
		if (this.arrayDimensions > 0)
		{
			Types.ARRAY_CLASS.getMethodMatches(list, instance, name, arguments);
			return;
		}
		
		if (this.upperBoundCount == 0)
		{
			Types.OBJECT_CLASS.getMethodMatches(list, instance, name, arguments);
			return;
		}
		
		for (int i = 0; i < this.upperBoundCount; i++)
		{
			this.upperBounds[i].getMethodMatches(list, instance, name, arguments);
		}
	}
	
	@Override
	public ConstructorMatch resolveConstructor(IArguments arguments)
	{
		return null;
	}
	
	@Override
	public void getConstructorMatches(List<ConstructorMatch> list, IArguments arguments)
	{
	}
	
	@Override
	public byte getAccessibility(IMember member)
	{
		return 0;
	}
	
	@Override
	public String getInternalName()
	{
		if (this.upperBoundCount > 0)
		{
			return this.upperBounds[0].getInternalName();
		}
		return "java/lang/Object";
	}
	
	@Override
	public void appendExtendedName(StringBuilder buffer)
	{
		for (int i = 0; i < this.arrayDimensions; i++)
		{
			buffer.append('[');
		}
		buffer.append('L').append(this.getInternalName()).append(';');
	}
	
	@Override
	public void appendSignature(StringBuilder buffer)
	{
		if (this.lowerBound != null)
		{
			buffer.append('-');
			this.lowerBound.appendSignature(buffer);
		}
		else
		{
			buffer.append('+');
			this.upperBounds[0].appendSignature(buffer);
		}
	}
	
	@Override
	public int getLoadOpcode()
	{
		return Opcodes.ALOAD;
	}
	
	@Override
	public int getArrayLoadOpcode()
	{
		return Opcodes.AALOAD;
	}
	
	@Override
	public int getStoreOpcode()
	{
		return Opcodes.ASTORE;
	}
	
	@Override
	public int getArrayStoreOpcode()
	{
		return Opcodes.AASTORE;
	}
	
	@Override
	public int getReturnOpcode()
	{
		return Opcodes.ARETURN;
	}
	
	@Override
	public WildcardType clone()
	{
		WildcardType clone = new WildcardType(this.position);
		clone.arrayDimensions = this.arrayDimensions;
		clone.lowerBound = this.lowerBound;
		clone.upperBoundCount = this.upperBoundCount;
		clone.upperBounds = this.upperBounds;
		return clone;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		for (int i = 0; i < this.arrayDimensions; i++)
		{
			buffer.append('[');
		}
		
		buffer.append('_');
		if (this.lowerBound != null)
		{
			buffer.append(Formatting.Type.genericLowerBound);
			this.lowerBound.toString(prefix, buffer);
		}
		if (this.upperBoundCount > 0)
		{
			buffer.append(Formatting.Type.genericUpperBound);
			this.upperBounds[0].toString(prefix, buffer);
			for (int i = 1; i < this.upperBoundCount; i++)
			{
				buffer.append(Formatting.Type.genericBoundSeperator);
				this.upperBounds[i].toString(prefix, buffer);
			}
		}
		
		for (int i = 0; i < this.arrayDimensions; i++)
		{
			buffer.append(']');
		}
	}
}
