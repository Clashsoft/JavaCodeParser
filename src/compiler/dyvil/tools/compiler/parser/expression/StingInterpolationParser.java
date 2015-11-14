package dyvil.tools.compiler.parser.expression;

import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.StringInterpolation;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.parsing.lexer.Tokens;
import dyvil.tools.parsing.token.IToken;

public final class StingInterpolationParser extends Parser implements IValueConsumer
{
	protected StringInterpolation value;
	
	public StingInterpolationParser(StringInterpolation value)
	{
		this.value = value;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token)
	{
		int type = token.type();
		switch (type)
		{
		case Tokens.STRING_START:
		case Tokens.STRING_PART:
		{
			int nextType = token.next().type();
			if (nextType == Tokens.STRING_PART || nextType == Tokens.STRING_END)
			{
				pm.report(token.next(), "Invalid Format String - Expression expected");
				return;
			}
			this.value.addString(token.stringValue());
			pm.pushParser(pm.newExpressionParser(this));
			return;
		}
		case Tokens.STRING_END:
			this.value.addString(token.stringValue());
			pm.popParser();
			return;
		}
		pm.reparse();
		pm.report(token, "Invalid Format String - String part expected");
		return;
	}
	
	@Override
	public void setValue(IValue value)
	{
		this.value.addValue(value);
	}
}
