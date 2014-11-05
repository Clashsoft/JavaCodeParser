package dyvil.tools.compiler.parser.classes;

import dyvil.tools.compiler.ast.classes.CodeClass;
import dyvil.tools.compiler.ast.structure.CompilationUnit;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;
import dyvil.tools.compiler.parser.type.TypeListParser;
import dyvil.tools.compiler.parser.type.TypeParser;
import dyvil.tools.compiler.util.Modifiers;

public class ClassDeclParser extends Parser
{
	public static final int		MODIFIERS	= 0;
	public static final int		NAME		= 1;
	public static final int		EXTENDS		= 2;
	public static final int		IMPLEMENTS	= 4;
	public static final int		BODY		= 8;
	public static final int		BODY_END	= 16;
	
	protected CompilationUnit	unit;
	
	private CodeClass			theClassDecl;
	private int					modifiers;
	
	public ClassDeclParser(CompilationUnit unit)
	{
		this.unit = unit;
	}
	
	@Override
	public boolean parse(ParserManager jcp, String value, IToken token) throws SyntaxError
	{
		if (this.isInMode(MODIFIERS))
		{
			int i = 0;
			if ((i = Modifiers.CLASS.parse(value)) != -1)
			{
				if ((this.modifiers & i) != 0)
				{
					throw new SyntaxError(token, "Duplicate Modifier '" + value + "'", "Remove this Modifier");
				}
				this.modifiers |= i;
				return true;
			}
			else if ((i = Modifiers.CLASS_TYPE.parse(value)) != -1)
			{
				this.theClassDecl = new CodeClass(token, this.unit, i, null);
				this.theClassDecl.setModifiers(this.modifiers);
				this.unit.addClass(this.theClassDecl);
				
				this.modifiers = 0;
				this.mode = NAME;
				return true;
			}
		}
		if (this.isInMode(NAME))
		{
			if (token.isType(IToken.TYPE_IDENTIFIER))
			{
				this.theClassDecl.setName(value);
				this.mode = EXTENDS | IMPLEMENTS | BODY;
				return true;
			}
		}
		if (this.isInMode(EXTENDS))
		{
			if ("extends".equals(value))
			{
				jcp.pushParser(new TypeParser(this.unit, this.theClassDecl));
				this.mode = IMPLEMENTS | BODY;
				return true;
			}
		}
		if (this.isInMode(IMPLEMENTS))
		{
			if ("implements".equals(value))
			{
				jcp.pushParser(new TypeListParser(this.unit, this.theClassDecl));
				this.mode = BODY;
				return true;
			}
		}
		if (this.isInMode(BODY))
		{
			if ("{".equals(value))
			{
				jcp.pushParser(new ClassBodyParser(this.theClassDecl));
				this.mode = BODY_END;
				return true;
			}
		}
		if (this.isInMode(BODY_END))
		{
			if ("}".equals(value))
			{
				this.theClassDecl.expandPosition(token);
				jcp.popParser();
				return true;
			}
		}
		return false;
	}
}
