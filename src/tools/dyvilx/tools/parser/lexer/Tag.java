package dyvilx.tools.parser.lexer;

public final class Tag
{
	public static final int EOF = -1;

	// --------------- General ---------------

	public static final int INT    = 'I';
	public static final int LONG   = 'J';
	public static final int FLOAT  = 'F';
	public static final int DOUBLE = 'D';
	public static final int NAME   = 'N';

	private Tag()
	{
		// no instances
	}
}
