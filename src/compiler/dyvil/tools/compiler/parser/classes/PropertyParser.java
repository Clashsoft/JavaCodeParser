package dyvil.tools.compiler.parser.classes;

import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.Property;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.util.ModifierTypes;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.lexer.Tokens;
import dyvil.tools.parsing.token.IToken;

public class PropertyParser extends Parser implements IValueConsumer
{
	private static final int	GET_OR_SET	= 1;
	private static final int	GET			= 2;
	private static final int	SET			= 4;
	
	public static final Name	get	= Name.getQualified("get");
	public static final Name	set	= Name.getQualified("set");
	
	protected Property	property;
	private int			modifiers;
	
	public PropertyParser(Property property)
	{
		this.property = property;
		this.mode = GET_OR_SET;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token)
	{
		int type = token.type();
		if (type == BaseSymbols.CLOSE_CURLY_BRACKET)
		{
			pm.popParser();
			return;
		}
		
		switch (this.mode)
		{
		case GET_OR_SET:
			if (type == BaseSymbols.SEMICOLON)
			{
				return;
			}
			
			int mod;
			if ((mod = ModifierTypes.METHOD.parse(type)) >= 0)
			{
				this.modifiers |= mod;
				return;
			}
			
			if (type == Tokens.LETTER_IDENTIFIER)
			{
				int nextType = token.next().type();
				if (nextType == BaseSymbols.COLON || nextType == BaseSymbols.CLOSE_CURLY_BRACKET || nextType == BaseSymbols.SEMICOLON)
				{
					Name name = token.nameValue();
					if (name == get)
					{
						this.property.setGetterModifiers(this.modifiers);
						this.mode = GET;
						return;
					}
					if (name == set)
					{
						this.property.setSetterModifiers(this.modifiers);
						this.mode = SET;
						return;
					}
				}
			}
			
			// No 'get:' or 'set:' tag -> Read-Only Property
			this.property.setGetterModifiers(this.modifiers);
			this.mode = GET;
			pm.pushParser(pm.newExpressionParser(this), true);
			return;
		case GET:
		case SET:
			if (type == BaseSymbols.COLON)
			{
				pm.pushParser(pm.newExpressionParser(this));
				return;
			}
			if (type == BaseSymbols.SEMICOLON || type == BaseSymbols.CLOSE_CURLY_BRACKET)
			{
				this.mode = GET_OR_SET;
				return;
			}
			pm.report(token, "Invalid Property Declaration - ':' expected");
			return;
		}
	}
	
	@Override
	public void setValue(IValue value)
	{
		if (this.mode == GET)
		{
			this.property.setGetter(value);
		}
		else if (this.mode == SET)
		{
			this.property.setSetter(value);
		}
	}
}
