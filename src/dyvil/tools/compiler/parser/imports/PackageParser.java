package dyvil.tools.compiler.parser.imports;

import clashsoft.cslib.src.SyntaxException;
import clashsoft.cslib.src.parser.IToken;
import clashsoft.cslib.src.parser.Parser;
import clashsoft.cslib.src.parser.ParserManager;
import dyvil.tools.compiler.ast.CompilationUnit;
import dyvil.tools.compiler.ast.imports.PackageDecl;

public class PackageParser extends Parser
{
	protected CompilationUnit	unit;
	
	private StringBuilder		buffer	= new StringBuilder();
	
	public PackageParser(CompilationUnit unit)
	{
		this.unit = unit;
	}
	
	@Override
	public boolean parse(ParserManager jcp, String value, IToken token) throws SyntaxException
	{
		if (";".equals(value))
		{
			jcp.popParser();
		}
		else
		{
			this.buffer.append(value);
		}
		return true;
	}
	
	@Override
	public void end(ParserManager jcp)
	{
		this.unit.setPackageDecl(new PackageDecl(this.buffer.toString()));
	}
}
