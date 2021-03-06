package dyvilx.tools.compiler.ast.imports;

import dyvil.source.position.SourcePosition;

public abstract class Import implements IImport
{
	protected SourcePosition position;
	protected IImport       parent;
	
	public Import(SourcePosition position)
	{
		this.position = position;
	}
	
	@Override
	public SourcePosition getPosition()
	{
		return this.position;
	}
	
	@Override
	public void setPosition(SourcePosition position)
	{
		this.position = position;
	}
	
	@Override
	public void setParent(IImport parent)
	{
		this.parent = parent;
	}
	
	@Override
	public IImport getParent()
	{
		return this.parent;
	}
	
	public void appendParent(String prefix, StringBuilder builder)
	{
		if (this.parent != null)
		{
			this.parent.toString(prefix, builder);
			builder.append('.');
		}
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		this.toString("", sb);
		return sb.toString();
	}
}
