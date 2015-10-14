package dyvil.tools.compiler.parser.method;

import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.consumer.ITypeConsumer;
import dyvil.tools.compiler.ast.parameter.ClassParameter;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.parameter.IParameterList;
import dyvil.tools.compiler.ast.parameter.MethodParameter;
import dyvil.tools.compiler.ast.type.ArrayType;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.transform.DyvilSymbols;
import dyvil.tools.compiler.util.ModifierTypes;
import dyvil.tools.compiler.util.ParserUtil;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.token.IToken;

public final class ParameterListParser extends Parser implements ITypeConsumer
{
	public static final int	TYPE		= 1;
	public static final int	NAME		= 2;
	public static final int	SEPERATOR	= 4;
	
	protected IParameterList paramList;
	
	private int				modifiers;
	private AnnotationList	annotations;
	
	private IType		type;
	private IParameter	parameter;
	private boolean		varargs;
	
	public ParameterListParser(IParameterList paramList)
	{
		this.paramList = paramList;
		this.mode = TYPE;
	}
	
	private void reset()
	{
		this.mode = TYPE;
		this.modifiers = 0;
		this.annotations = null;
		this.type = null;
		this.parameter = null;
		this.varargs = false;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token)
	{
		int type = token.type();
		switch (this.mode)
		{
		case TYPE:
			if (type == BaseSymbols.SEMICOLON && token.isInferred())
			{
				return;
			}
			int i = 0;
			if ((i = ModifierTypes.PARAMETER.parse(type)) != -1)
			{
				this.modifiers |= i;
				return;
			}
			if (type == DyvilSymbols.AT)
			{
				if (this.annotations == null)
				{
					this.annotations = new AnnotationList();
				}
				
				Annotation annotation = new Annotation(token.raw());
				this.annotations.addAnnotation(annotation);
				pm.pushParser(pm.newAnnotationParser(annotation));
				return;
			}
			if (ParserUtil.isCloseBracket(type))
			{
				pm.popParser(true);
				return;
			}
			this.mode = NAME;
			pm.pushParser(pm.newTypeParser(this), true);
			return;
		case NAME:
			if (type == DyvilSymbols.ELLIPSIS)
			{
				this.varargs = true;
				return;
			}
			this.mode = SEPERATOR;
			if (ParserUtil.isIdentifier(type))
			{
				if (this.varargs)
				{
					this.paramList.setVarargs();
					this.type = new ArrayType(this.type);
				}
				
				this.parameter = this.paramList instanceof IClass ? new ClassParameter(token.nameValue(), this.type)
						: new MethodParameter(token.nameValue(), this.type);
				this.parameter.setPosition(token.raw());
				this.parameter.setModifiers(this.modifiers);
				this.parameter.setAnnotations(this.annotations);
				this.annotations = null;
				this.parameter.setVarargs(this.varargs);
				this.paramList.addParameter(this.parameter);
				
				this.varargs = false;
				
				return;
			}
			pm.report(token, "Invalid Parameter Declaration - Name expected");
			return;
		case SEPERATOR:
			if (ParserUtil.isCloseBracket(type))
			{
				pm.popParser(true);
				return;
			}
			if (type == BaseSymbols.EQUALS)
			{
				pm.pushParser(pm.newExpressionParser(this.parameter));
				return;
			}
			this.reset();
			if (type == BaseSymbols.COMMA || type == BaseSymbols.SEMICOLON)
			{
				return;
			}
			pm.report(token, "Invalid Parameter Declaration - ',' expected");
			return;
		}
	}
	
	@Override
	public void setType(IType type)
	{
		this.type = type;
	}
}
