package dyvilx.tools.parser.lexer;

import dyvil.source.position.SourcePosition;

public interface Token
{
	// =============== Properties ===============

	int getTag();

	SourcePosition getPosition();
}
