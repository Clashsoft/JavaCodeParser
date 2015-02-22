package dyvil.tools.compiler.parser.type;

import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.generic.WildcardType;
import dyvil.tools.compiler.ast.type.*;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;
import dyvil.tools.compiler.util.ParserUtil;
import dyvil.tools.compiler.util.Tokens;

public class TypeParser extends Parser implements ITyped
{
	public static final int	NAME			= 1;
	public static final int	GENERICS		= 2;
	public static final int	GENERICS_END	= 4;
	public static final int	ARRAY_END		= 8;
	public static final int	WILDCARD_TYPE	= 16;
	public static final int	TUPLE_END		= 128;
	public static final int	LAMBDA_TYPE		= 256;
	public static final int	LAMBDA_END		= 512;
	
	public static final int	UPPER			= 1;
	public static final int	LOWER			= 2;
	
	public ITyped			typed;
	
	private byte			boundMode;
	
	private IType			type;
	private int				arrayDimensions;
	private int				arrayDimensions2;
	
	public TypeParser(ITyped typed)
	{
		this.mode = NAME;
		this.typed = typed;
	}
	
	@Override
	public void parse(ParserManager pm, IToken token) throws SyntaxError
	{
		int type = token.type();
		if (this.isInMode(NAME))
		{
			if (type == Tokens.OPEN_PARENTHESIS)
			{
				TupleType tupleType = new TupleType();
				pm.pushParser(new TypeListParser(tupleType));
				this.type = tupleType;
				this.mode = TUPLE_END;
				return;
			}
			if (type == Tokens.OPEN_SQUARE_BRACKET)
			{
				this.arrayDimensions++;
				this.arrayDimensions2++;
				return;
			}
			if (type == Tokens.ARROW_OPERATOR)
			{
				LambdaType lt = new LambdaType();
				this.type = lt;
				pm.pushParser(new TypeParser(lt));
				this.mode = LAMBDA_END;
				return;
			}
			if (ParserUtil.isIdentifier(type))
			{
				if (token.next().type() == Tokens.OPEN_SQUARE_BRACKET)
				{
					this.type = new GenericType(token, token.value());
					this.mode = GENERICS;
					return;
				}
				
				this.type = new Type(token, token.value());
				this.mode = ARRAY_END;
				return;
			}
			if (type == Tokens.WILDCARD)
			{
				this.type = new WildcardType(token.raw());
				this.mode = WILDCARD_TYPE;
				return;
			}
			throw new SyntaxError(token, "Invalid Type - Invalid Token '" + token.value() + "'");
		}
		if (this.isInMode(TUPLE_END))
		{
			pm.popParser();
			if (type == Tokens.CLOSE_PARENTHESIS)
			{
				if (token.next().type() == Tokens.ARROW_OPERATOR)
				{
					TupleType tupleType = (TupleType) this.type;
					this.type = new LambdaType(tupleType);
					this.mode = LAMBDA_TYPE;
					return;
				}
				
				this.type.expandPosition(token);
				return;
			}
			throw new SyntaxError(token, "Invalid Tuple Type - ')' expected");
		}
		if (this.isInMode(LAMBDA_TYPE))
		{
			pm.pushParser(new TypeParser((LambdaType) this.type));
			this.mode = LAMBDA_END;
			return;
		}
		if (this.isInMode(LAMBDA_END))
		{
			this.type.expandPosition(token.prev());
			pm.popParser(true);
			return;
		}
		if (this.isInMode(ARRAY_END))
		{
			if (type == Tokens.CLOSE_SQUARE_BRACKET)
			{
				this.arrayDimensions2--;
				if (this.arrayDimensions2 == 0)
				{
					this.type.expandPosition(token);
					pm.popParser();
					return;
				}
			}
			
			if (this.arrayDimensions2 > 0)
			{
				this.type.expandPosition(token.prev());
				pm.popParser(true);
				throw new SyntaxError(token.prev(), "Unclosed array brackets");
			}
			
			this.type.expandPosition(token.prev());
			pm.popParser(true);
			return;
		}
		if (this.isInMode(GENERICS))
		{
			if (type == Tokens.OPEN_SQUARE_BRACKET)
			{
				pm.pushParser(new TypeListParser((GenericType) this.type));
				this.mode = GENERICS_END;
				return;
			}
			
			if (type == Tokens.CLOSE_SQUARE_BRACKET)
			{
				this.arrayDimensions2--;
				if (this.arrayDimensions2 == 0)
				{
					this.type.expandPosition(token);
					pm.popParser();
					return;
				}
			}
			
			if (this.arrayDimensions2 > 0)
			{
				this.type.expandPosition(token.prev());
				pm.popParser(true);
				throw new SyntaxError(token.prev(), "Invalid Array Type - ']' expected");
			}
			
			this.type.expandPosition(token.prev());
			pm.popParser(true);
			return;
		}
		if (this.isInMode(WILDCARD_TYPE))
		{
			String value = token.value();
			if (this.boundMode == 0)
			{
				if ("<=".equals(value))
				{
					pm.pushParser(new TypeParser(this));
					this.boundMode = LOWER;
					return;
				}
				else if (">=".equals(value))
				{
					pm.pushParser(new TypeParser(this));
					this.boundMode = UPPER;
					return;
				}
			}
			else if (this.boundMode == UPPER)
			{
				if ("&".equals(value))
				{
					pm.pushParser(new TypeParser(this));
					return;
				}
			}
			pm.popParser(true);
			return;
		}
		if (this.isInMode(GENERICS_END))
		{
			pm.popParser();
			if (type == Tokens.CLOSE_SQUARE_BRACKET)
			{
				return;
			}
			throw new SyntaxError(token, "Invalid Generic Type - ']' expected", true);
		}
	}
	
	@Override
	public void end(ParserManager pm)
	{
		if (this.type != null)
		{
			this.type.setArrayDimensions(this.arrayDimensions);
		}
		this.typed.setType(this.type);
	}
	
	@Override
	public void setType(IType type)
	{
		if (this.boundMode == UPPER)
		{
			((ITypeVariable) this.type).addUpperBound(type);
		}
		else if (this.boundMode == LOWER)
		{
			((ITypeVariable) this.type).setLowerBound(type);
		}
		else
		{
			((ITyped) this.type).setType(type);
		}
	}
	
	@Override
	public IType getType()
	{
		return null;
	}
}
