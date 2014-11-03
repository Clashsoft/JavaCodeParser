package dyvil.tools.compiler.ast.type;

import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.structure.IContext;

public class PrimitiveType extends Type
{
	public PrimitiveType(String name)
	{
		super(name);
	}
	
	@Override
	public boolean isResolved()
	{
		return true;
	}
	
	@Override
	public Type resolve(IContext context)
	{
		return this;
	}
	
	@Override
	public void appendInternalName(StringBuilder buf)
	{
		if (this == Type.VOID)
			buf.append("V");
		else if (this == Type.BOOL)
			buf.append("Z");
		else if (this == Type.BYTE)
			buf.append("B");
		else if (this == Type.SHORT)
			buf.append("S");
		else if (this == Type.CHAR)
			buf.append("C");
		else if (this == Type.INT)
			buf.append("I");
		else if (this == Type.LONG)
			buf.append("J");
		else if (this == Type.FLOAT)
			buf.append("F");
		else if (this == Type.DOUBLE)
			buf.append("D");
	}
	
	@Override
	public Type applyState(CompilerState state, IContext context)
	{
		return this;
	}
}
