package dyvilx.tools.parser.lexer;

import java.util.Iterator;

public interface Lexer extends Iterator<Token>
{
	@Override
	Token next();

	@Override
	boolean hasNext();
}
