package dyvilx.tools.parser.lexer;

import dyvil.source.position.SourcePosition;

public abstract class SingleLineToken implements Token
{
	// =============== Fields ===============

	public final int line;
	public final int column;

	// =============== Constructors ===============

	public SingleLineToken(int line, int column)
	{
		this.line = line;
		this.column = column;
	}

	// =============== Properties ===============

	public int getLine()
	{
		return this.line;
	}

	public int getColumn()
	{
		return this.column;
	}

	public abstract int getWidth();

	// =============== Methods ===============

	@Override
	public String toString()
	{
		return "SingleLineToken(line: " + this.line + ", column: " + this.column + ")";
	}

	@Override
	public SourcePosition getPosition()
	{
		return SourcePosition.apply(this.line, this.column, this.column + this.getWidth());
	}
}
