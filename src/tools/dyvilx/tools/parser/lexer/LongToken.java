package dyvilx.tools.parser.lexer;

public class LongToken extends SingleLineToken
{
	// =============== Fields ===============

	public final long   value;
	public final String literal;

	// =============== Constructors ===============

	public LongToken(int line, int column, long value, String literal)
	{
		super(line, column);
		this.value = value;
		this.literal = literal;
	}

	// =============== Properties ===============

	@Override
	public int getTag()
	{
		return Tag.LONG;
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
		return "LongToken(line: " + this.line + ", column: " + this.column + ", value: " + this.literal + ")";
	}
}
