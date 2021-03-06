package dyvilx.tools.compiler.parser.statement;

import dyvilx.tools.compiler.ast.statement.loop.RepeatStatement;
import dyvilx.tools.compiler.parser.DyvilKeywords;
import dyvilx.tools.compiler.parser.expression.ExpressionParser;
import dyvilx.tools.parsing.IParserManager;
import dyvilx.tools.parsing.Parser;
import dyvilx.tools.parsing.lexer.BaseSymbols;
import dyvilx.tools.parsing.token.IToken;

public class RepeatStatementParser extends Parser
{
	// =============== Constants ===============

	protected static final int REPEAT = 1;
	protected static final int ACTION = 2;
	protected static final int WHILE  = 3;

	// =============== Fields ===============

	protected final RepeatStatement statement;

	// =============== Constructors ===============

	public RepeatStatementParser(RepeatStatement statement)
	{
		this.statement = statement;
		this.mode = ACTION;
	}

	// =============== Methods ===============

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();
		switch (this.mode)
		{
		case REPEAT:
			this.mode = ACTION;
			if (type != DyvilKeywords.REPEAT)
			{
				pm.reparse();
				pm.report(token, "repeat.keyword");
			}
			return;
		case ACTION:
			if (type != BaseSymbols.OPEN_CURLY_BRACKET)
			{
				ForStatementParser.reportSingleStatement(pm, token, "repeat.single.deprecated");
			}

			pm.pushParser(new ExpressionParser(this.statement::setAction), true);
			this.mode = WHILE;
			return;
		case WHILE:
			if (type == DyvilKeywords.WHILE)
			{
				this.mode = END;
				pm.pushParser(new ExpressionParser(this.statement::setCondition));
				return;
			}
			if (type == BaseSymbols.SEMICOLON && token.isInferred() && token.next().type() == DyvilKeywords.WHILE)
			{
				this.mode = END;
				pm.skip(1);
				pm.pushParser(new ExpressionParser(this.statement::setCondition));
				return;
			}
			// fallthrough
		case END:
			pm.popParser(true);
		}
	}
}
