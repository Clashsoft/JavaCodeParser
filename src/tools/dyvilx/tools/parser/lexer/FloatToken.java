package dyvilx.tools.parser.lexer;

public class FloatToken extends SingleLineToken
{
	// =============== Fields ===============

	public final float  value;
	public final String literal;

	// =============== Constructors ===============

	public FloatToken(int line, int column, float value, String literal)
	{
		super(line, column);
		this.value = value;
		this.literal = literal;
	}

	// =============== Properties ===============

	@Override
	public int getTag()
	{
		return Tag.FLOAT;
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
		return "FloatToken(line: " + this.line + ", column: " + this.column + ", value: " + this.literal + ")";
	}
}
