package dyvilx.tools.parser.lexer;

public class SymbolToken extends SingleLineToken
{
	// =============== Fields ===============

	public final int tag;
	public final int width;

	// =============== Constructors ===============

	public SymbolToken(int tag, int line, int column, int width)
	{
		super(line, column);
		this.tag = tag;
		this.width = width;
	}

	// =============== Properties ===============

	@Override
	public int getTag()
	{
		return this.tag;
	}

	@Override
	public int getWidth()
	{
		return this.width;
	}

	// =============== Methods ===============

	@Override
	public String toString()
	{
		final StringBuilder builder = new StringBuilder("SymbolToken(tag: ");
		builder.append(this.tag);
		builder.append(" (");
		builder.appendCodePoint(this.tag);
		builder.append("), line: ");
		builder.append(this.line);
		builder.append(", column: ");
		builder.append(this.column);
		builder.append(", width: ");
		builder.append(this.width);
		builder.append(")");
		return builder.toString();
	}
}
