package dyvilx.tools.parser.lexer;

public class IntToken extends SingleLineToken
{
	// =============== Fields ===============

	public final int    value;
	public final String literal;

	// =============== Constructors ===============

	public IntToken(int line, int column, int value, String literal)
	{
		super(line, column);
		this.value = value;
		this.literal = literal;
	}

	// =============== Properties ===============

	@Override
	public int getTag()
	{
		return Tag.INT;
	}

	@Override
	public int getWidth()
	{
		return this.literal.length();
	}

	// =============== Methods ===============

	@Override
	public String toString()
	{
		return "IntToken(line: " + this.line + ", column: " + this.column + ", value: " + this.literal + ")";
	}
}
