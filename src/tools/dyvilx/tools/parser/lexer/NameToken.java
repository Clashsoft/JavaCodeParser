package dyvilx.tools.parser.lexer;

import dyvil.lang.Name;

public class NameToken extends SingleLineToken
{
	// =============== Fields ===============

	public final Name   name;
	public final String literal;

	// =============== Constructors ===============

	public NameToken(int line, int column, Name name, String literal)
	{
		super(line, column);
		this.name = name;
		this.literal = literal;
	}

	// =============== Properties ===============

	@Override
	public int getTag()
	{
		return Tag.NAME;
	}

	@Override
	public int getWidth()
	{
		return this.literal.codePointCount(0, this.literal.length());
	}

	// =============== Methods ===============

	@Override
	public String toString()
	{
		return "NameToken(line: " + this.line + ", column: " + this.column + ", value: " + this.literal + ")";
	}
}
