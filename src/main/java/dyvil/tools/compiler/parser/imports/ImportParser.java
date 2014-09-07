package dyvil.tools.compiler.parser.imports;

import dyvil.tools.compiler.ast.CompilationUnit;
import dyvil.tools.compiler.ast.imports.IImport;
import dyvil.tools.compiler.ast.imports.MultiImport;
import dyvil.tools.compiler.ast.imports.PackageImport;
import dyvil.tools.compiler.lexer.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.lexer.token.Token;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;

public class ImportParser extends Parser
{
	public static int			MULTIIMPORT_START	= 1;
	public static int			MULTIIMPORT_END		= 2;
	
	protected CompilationUnit	unit;
	
	private IImport				theImport;
	private StringBuilder		buffer				= new StringBuilder();
	
	public ImportParser(CompilationUnit unit)
	{
		this.unit = unit;
	}
	
	@Override
	public boolean parse(ParserManager pm, String value, IToken token) throws SyntaxError
	{
		if (";".equals(value))
		{
			pm.popParser();
			return true;
		}
		else if (this.mode == 0)
		{
			if ("{".equals(value))
			{
				this.mode = MULTIIMPORT_START;
				this.theImport = new MultiImport(this.buffer.toString());
				this.buffer.delete(0, this.buffer.length());
				return true;
			}
			else if (".;".equals(value))
			{
				this.theImport = new PackageImport(this.buffer.toString());
				pm.popParser();
				return true;
			}
			else if (token.isType(Token.TYPE_IDENTIFIER) || ".".equals(value))
			{
				this.buffer.append(value);
				return true;
			}
		}
		else if (this.mode == MULTIIMPORT_START)
		{
			if (",".equals(value))
			{
				if (this.buffer.length() > 0)
				{
					((MultiImport) this.theImport).addClass(this.buffer.toString());
					this.buffer.delete(0, this.buffer.length());
					return true;
				}				
			}
			else if ("}".equals(value))
			{
				this.mode = MULTIIMPORT_END;
				
				if (this.buffer.length() > 0)
				{
					((MultiImport) this.theImport).addClass(this.buffer.toString());
				}
				return true;
			}
			else if (token.isType(Token.TYPE_IDENTIFIER) || ".".equals(value))
			{
				this.buffer.append(value);
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void end(ParserManager pm)
	{
		this.unit.addImport(this.theImport);
	}
}
