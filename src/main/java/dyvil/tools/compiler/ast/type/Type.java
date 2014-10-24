package dyvil.tools.compiler.ast.type;

import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.ASTObject;
import dyvil.tools.compiler.ast.api.IField;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class Type extends ASTObject implements IContext
{
	public static Type	NONE	= new Type(null);
	
	public static Type	VOID	= new Type("void");
	public static Type	INT		= new Type("int");
	public static Type	LONG	= new Type("long");
	public static Type	FLOAT	= new Type("float");
	public static Type	DOUBLE	= new Type("double");
	public static Type	CHAR	= new Type("char");
	public static Type	BOOL	= new Type("boolean");
	
	public static Type	STRING	= new Type("java.lang.String");
	public static Type	CLASS	= new Type("java.lang.Class");
	
	private String		name;
	private IClass		theClass;
	private char		seperator;
	private int			arrayDimensions;
	
	protected Type()
	{}
	
	protected Type(String name)
	{
		this.name = name;
	}
	
	public Type(String name, IClass iclass)
	{
		this.name = name;
		this.theClass = iclass;
	}
	
	public Type(String name, ICodePosition position)
	{
		this.name = name;
		this.position = position;
	}
	
	public void setClass(IClass theClass)
	{
		this.theClass = theClass;
	}
	
	public void setSeperator(char seperator)
	{
		this.seperator = seperator;
	}
	
	public void setArrayDimensions(int dimensions)
	{
		this.arrayDimensions = dimensions;
	}
	
	public IClass getTheClass()
	{
		return this.theClass;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public char getSeperator()
	{
		return this.seperator;
	}
	
	public int getArrayDimensions()
	{
		return this.arrayDimensions;
	}
	
	public boolean isArrayType()
	{
		return this.arrayDimensions > 0;
	}
	
	@Override
	public Type applyState(CompilerState state, IContext context)
	{
		if (this.position == null)
		{
			return this;
		}
		
		if (state == CompilerState.RESOLVE)
		{
			if (this.theClass == null)
			{
				switch (this.name)
				{
				case "void":
					return VOID;
				case "int":
					return INT;
				case "long":
					return LONG;
				case "float":
					return FLOAT;
				case "double":
					return DOUBLE;
				case "char":
					return CHAR;
				case "bool":
					return BOOL;
				}
				
				this.theClass = context.resolveClass(this.name);
				if (this.theClass == null)
				{
					state.addMarker(new SyntaxError(this.position, "The type '" + this.name + "' could not be resolved."));
				}
			}
		}
		return this;
	}
	
	@Override
	public Type getThisType()
	{
		return this;
	}
	
	@Override
	public boolean isStatic()
	{
		if (this.theClass == null)
		{
			return false;
		}
		return this.theClass.isStatic();
	}
	
	@Override
	public IClass resolveClass(String name)
	{
		if (this.theClass == null)
		{
			return null;
		}
		return this.theClass.resolveClass(name);
	}
	
	@Override
	public IField resolveField(String name)
	{
		if (this.theClass == null)
		{
			return null;
		}
		return this.theClass.resolveField(name);
	}
	
	@Override
	public IMethod resolveMethodName(String name)
	{
		if (this.theClass == null)
		{
			return null;
		}
		return this.theClass.resolveMethodName(name);
	}
	
	@Override
	public IMethod resolveMethod(String name, Type... args)
	{
		if (this.theClass == null)
		{
			return null;
		}
		return this.theClass.resolveMethod(name, args);
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + this.arrayDimensions;
		result = prime * result + (this.name == null ? 0 : this.name.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (!(obj instanceof Type))
		{
			return false;
		}
		Type other = (Type) obj;
		if (this.arrayDimensions != other.arrayDimensions)
		{
			return false;
		}
		if (this.name == null)
		{
			if (other.name != null)
			{
				return false;
			}
		}
		else if (!this.name.equals(other.name))
		{
			return false;
		}
		return true;
	}
	
	@Override
	public String toString()
	{
		StringBuilder buffer = new StringBuilder();
		this.toString("", buffer);
		return buffer.toString();
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(prefix).append(this.name);
		for (int i = 0; i < this.arrayDimensions; i++)
		{
			buffer.append(Formatting.Type.array);
		}
		// TODO Generics
	}
}
