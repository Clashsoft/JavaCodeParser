package dyvil.tools.compiler.parser.header;

import dyvil.tools.compiler.ast.operator.IOperator;
import dyvil.tools.compiler.ast.operator.IOperatorMap;
import dyvil.tools.compiler.ast.operator.Operator;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.transform.DyvilKeywords;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.lexer.Tokens;
import dyvil.tools.parsing.token.IToken;

public final class OperatorParser extends Parser
{
	private static final int TYPE          = 1;
	private static final int OPERATOR      = 2;
	private static final int OPEN_BRACKET  = 4;
	private static final int PROPERTY      = 8;
	private static final int PRECEDENCE    = 16;
	private static final int ASSOCIATIVITY = 32;
	private static final int COMMA         = 64;

	public static final Name associativity = Name.getQualified("associativity");
	public static final Name precedence    = Name.getQualified("precedence");
	public static final Name none          = Name.getQualified("none");
	public static final Name left          = Name.getQualified("left");
	public static final Name right         = Name.getQualified("right");

	protected IOperatorMap map;
	private   byte         type;
	private   Operator     operator;

	public OperatorParser(IOperatorMap map)
	{
		this.map = map;
		this.mode = TYPE;
	}

	public OperatorParser(IOperatorMap map, byte type)
	{
		this.map = map;

		this.type = type;
		this.mode = OPERATOR;
	}

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();
		switch (this.mode)
		{
		case TYPE:
			this.mode = OPERATOR;
			switch (type)
			{
			case DyvilKeywords.PREFIX:
				this.type = IOperator.PREFIX;
				return;
			case DyvilKeywords.POSTFIX:
				this.type = IOperator.POSTFIX;
				return;
			case DyvilKeywords.INFIX:
				this.type = IOperator.INFIX;
				return;
			}
			pm.report(token, "operator.type.invalid");
			return;
		case OPERATOR:
			this.mode = OPEN_BRACKET;
			if (type == DyvilKeywords.OPERATOR)
			{
				IToken next = token.next();
				Name name = next.nameValue();
				pm.skip();
				if (name == null)
				{
					pm.report(next, "operator.identifier");
					return;
				}

				this.operator = new Operator(name, this.type);
				return;
			}
			pm.report(token, "operator.operator");
			return;
		case OPEN_BRACKET:
			switch (this.type)
			{
			case IOperator.PREFIX:
				if (type == BaseSymbols.OPEN_CURLY_BRACKET)
				{
					this.mode = PROPERTY;
					return;
				}
				pm.popParser();
				this.map.addOperator(this.operator);
				if (type != BaseSymbols.SEMICOLON && type != Tokens.EOF)
				{
					pm.report(token, "operator.prefix.semicolon");
					return;
				}
				return;
			case IOperator.POSTFIX:
				if (type == BaseSymbols.OPEN_CURLY_BRACKET)
				{
					this.mode = PROPERTY;
					return;
				}
				pm.popParser();
				this.map.addOperator(this.operator);
				if (type != BaseSymbols.SEMICOLON && type != Tokens.EOF)
				{
					pm.report(token, "operator.postfix.semicolon");
					return;
				}
				return;
			default: // infix
				this.mode = PROPERTY;
				if (type != BaseSymbols.OPEN_CURLY_BRACKET)
				{
					pm.reparse();
					pm.report(token, "operator.infix.open_brace");
					return;
				}
				return;
			}
		case PROPERTY:
			if (type == Tokens.LETTER_IDENTIFIER)
			{
				Name name = token.nameValue();
				if (name == precedence)
				{
					this.mode = PRECEDENCE;
					return;
				}
				if (name == associativity)
				{
					this.mode = ASSOCIATIVITY;
					return;
				}
				if (this.parseAssociativity(pm, token, name))
				{
					this.mode = COMMA;
					return;
				}
			}
			if ((type & Tokens.INT) != 0)
			{
				this.setPrecedence(pm, token, token.intValue());
				this.mode = COMMA;
				return;
			}
			if (type == BaseSymbols.CLOSE_CURLY_BRACKET)
			{
				pm.popParser();
				this.map.addOperator(this.operator);
				return;
			}
			pm.report(Markers.syntaxError(token, "operator.property.invalid", token));
			return;
		case COMMA:
			if (type == BaseSymbols.CLOSE_CURLY_BRACKET)
			{
				pm.popParser();
				this.map.addOperator(this.operator);
				return;
			}
			this.mode = PROPERTY;
			if (type != BaseSymbols.COMMA)
			{
				pm.reparse();
				pm.report(token, "operator.property.comma");
				return;
			}
			return;
		case PRECEDENCE:
			this.mode = COMMA;
			if ((type & Tokens.INT) == 0)
			{
				pm.reparse();
				pm.report(token, "operator.property.precedence");
				return;
			}
			this.setPrecedence(pm, token, token.intValue());
			return;
		case ASSOCIATIVITY:
			this.mode = COMMA;
			if (type == Tokens.LETTER_IDENTIFIER)
			{
				Name name = token.nameValue();
				if (this.parseAssociativity(pm, token, name))
				{
					return;
				}
			}

			pm.reparse();
			pm.report(token, "operator.property.associativity");
		}
	}

	public boolean parseAssociativity(IParserManager pm, IToken token, Name name)
	{
		if (name == left)
		{
			this.setAssociativity(pm, token, IOperator.LEFT);
			return true;
		}
		if (name == none)
		{
			this.setAssociativity(pm, token, IOperator.NONE);
			return true;
		}
		if (name == right)
		{
			this.setAssociativity(pm, token, IOperator.RIGHT);
			return true;
		}
		return false;
	}

	private void setAssociativity(IParserManager pm, IToken token, byte associativity)
	{
		switch (this.type)
		{
		case IOperator.POSTFIX:
			pm.report(token, "operator.postfix.associativity");
			return;
		case IOperator.PREFIX:
			pm.report(token, "operator.prefix.associativity");
			return;
		}

		this.operator.setAssociativity(associativity);
	}

	private void setPrecedence(IParserManager pm, IToken token, int precedence)
	{
		switch (this.type)
		{
		case IOperator.POSTFIX:
			pm.report(token, "operator.postfix.precedence");
			return;
		case IOperator.PREFIX:
			pm.report(token, "operator.prefix.precedence");
			return;
		}

		this.operator.setPrecedence(precedence);
	}

	@Override
	public boolean reportErrors()
	{
		return this.mode > OPERATOR;
	}
}
