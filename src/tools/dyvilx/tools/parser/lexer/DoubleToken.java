package dyvilx.tools.parser.lexer;

public class DoubleToken extends SingleLineToken
{
	// =============== Fields ===============

	public final double value;
	public final String literal;

	// =============== Constructors ===============

	public DoubleToken(int line, int column, double value, String literal)
	{
		super(line, column);
		this.value = value;
		this.literal = literal;
	}

	// =============== Properties ===============

	@Override
	public int getTag()
	{
		return Tag.DOUBLE;
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
		return "DoubleToken(line: "  + this.line + ", column: " + this.column + ", value: " + this.literal + ")";
	}
}
