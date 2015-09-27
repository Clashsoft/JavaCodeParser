package dyvil.tools.compiler.parser.statement;

import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.statement.WhileStatement;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.transform.Symbols;
import dyvil.tools.compiler.util.ParserUtil;

public final class WhileStatementParser extends Parser implements IValueConsumer
{
	public static final int	CONDITION		= 1;
	public static final int	CONDITION_END	= 2;
	public static final int	BLOCK			= 4;
	
	protected WhileStatement statement;
	
	public WhileStatementParser(WhileStatement statement)
	{
		this.statement = statement;
		this.mode = CONDITION;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token) 
	{
		int type = token.type();
		switch (this.mode)
		{
		case CONDITION:
			this.mode = CONDITION_END;
			if (type == Symbols.OPEN_PARENTHESIS)
			{
				pm.pushParser(pm.newExpressionParser(this));
				return;
			}
			pm.reparse();
			pm.report(token, "Invalid While Statement - '(' expected");
			return;
		case CONDITION_END:
			this.mode = BLOCK;
			if (type != Symbols.CLOSE_PARENTHESIS)
			{
				pm.reparse();
				pm.report(token, "Invalid While Statement - ')' expected");
			}
			return;
		case BLOCK:
			if (ParserUtil.isTerminator(type) && !token.isInferred())
			{
				pm.popParser(true);
				return;
			}
			pm.pushParser(pm.newExpressionParser(this), true);
			this.mode = END;
			return;
		case END:
			pm.popParser(true);
			return;
		}
	}
	
	@Override
	public void setValue(IValue value)
	{
		switch (this.mode)
		{
		case CONDITION_END:
			this.statement.setCondition(value);
			break;
		case END:
			this.statement.setAction(value);
			break;
		}
	}
}
