package dyvil.tools.compiler.parser.method;

import dyvil.tools.compiler.ast.field.Parameter;
import dyvil.tools.compiler.ast.method.IParameterized;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;
import dyvil.tools.compiler.parser.annotation.AnnotationParser;
import dyvil.tools.compiler.parser.type.TypeParser;
import dyvil.tools.compiler.util.Modifiers;
import dyvil.tools.compiler.util.ParserUtil;
import dyvil.tools.compiler.util.Tokens;

public class ParameterListParser extends Parser
{
	public static final int		TYPE		= 0;
	public static final int		NAME		= 1;
	
	protected IParameterized	parameterized;
	
	private Parameter			parameter	= new Parameter();
	
	public ParameterListParser(IParameterized parameterized)
	{
		this.parameterized = parameterized;
	}
	
	@Override
	public boolean parse(ParserManager pm, String value, IToken token) throws SyntaxError
	{
		int type = token.type();
		if (this.isInMode(TYPE))
		{
			int i = 0;
			if ((i = Modifiers.PARAMETER.parse(value)) != -1)
			{
				this.parameter.addModifier(i);
				return true;
			}
			if (value.charAt(0) == '@')
			{
				pm.pushParser(new AnnotationParser(this.parameter), true);
				return true;
			}
			if (type == Tokens.CLOSE_PARENTHESIS)
			{
				pm.popParser(true);
				return true;
			}
			
			this.mode = NAME;
			pm.pushParser(new TypeParser(this.parameter), true);
			return true;
		}
		if (this.isInMode(NAME))
		{
			if (ParserUtil.isIdentifier(type))
			{
				this.parameter.setName(value);
				return true;
			}
			if (type == Tokens.COMMA)
			{
				this.parameter.setSeperator(value.charAt(0));
				this.end(pm);
				this.mode = TYPE;
				return true;
			}
			if ("...".equals(value))
			{
				this.parameterized.setVarargs();
				this.parameter.setVarargs();
				return true;
			}
			
			pm.popParser(true);
			return true;
		}
		return false;
	}
	
	@Override
	public void end(ParserManager pm)
	{
		if (this.parameter.hasName())
		{
			this.parameterized.addParameter(this.parameter);
			this.parameter = new Parameter();
		}
	}
}
