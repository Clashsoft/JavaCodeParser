package dyvil.tools.compiler.parser.classes;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.consumer.IDataMemberConsumer;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.modifiers.Modifier;
import dyvil.tools.compiler.ast.modifiers.ModifierList;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.parser.ParserUtil;
import dyvil.tools.compiler.parser.annotation.AnnotationParser;
import dyvil.tools.compiler.parser.annotation.ModifierParser;
import dyvil.tools.compiler.parser.type.TypeParser;
import dyvil.tools.compiler.transform.DyvilKeywords;
import dyvil.tools.compiler.transform.DyvilSymbols;
import dyvil.tools.parsing.IParserManager;
import dyvil.tools.parsing.Parser;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.token.IToken;

public class DataMemberParser<T extends IDataMember> extends Parser
{
	protected static final int DECLARATOR = 0;
	protected static final int NAME       = 1;
	protected static final int TYPE       = 2;

	protected IDataMemberConsumer<T> consumer;

	private ModifierSet    modifiers;
	private AnnotationList annotations;
	private T              dataMember;

	public DataMemberParser(IDataMemberConsumer<T> consumer)
	{
		this.consumer = consumer;
	}

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();
		switch (this.mode)
		{
		case DECLARATOR:
			switch (type)
			{
			case DyvilSymbols.AT:
				if (this.annotations == null)
				{
					this.annotations = new AnnotationList();
				}

				final Annotation annotation = new Annotation(token.raw());
				this.annotations.add(annotation);
				pm.pushParser(new AnnotationParser(annotation));
				return;
			case DyvilKeywords.VAR:
				this.mode = NAME;
				return;
			case DyvilKeywords.LET:
				this.mode = NAME;

				if (this.modifiers == null)
				{
					this.modifiers = new ModifierList();
				}

				this.modifiers.addIntModifier(Modifiers.FINAL);
				return;
			}

			final Modifier modifier;
			if ((modifier = ModifierParser.parseModifier(token, pm)) != null)
			{
				if (this.modifiers == null)
				{
					this.modifiers = new ModifierList();
				}

				this.modifiers.addModifier(modifier);
				return;
			}
			// Fallthrough
		case NAME:
			if (!ParserUtil.isIdentifier(type))
			{
				pm.report(token, "variable.identifier");
				return;
			}

			this.dataMember = this.consumer.createDataMember(token.raw(), token.nameValue(), Types.UNKNOWN, this.modifiers,
			                                                 this.annotations);

			this.mode = TYPE;
			return;
		case TYPE:
			if (type == BaseSymbols.COLON)
			{
				// ... IDENTIFIER : TYPE ...
				pm.pushParser(new TypeParser(this.dataMember));
				this.mode = END;
				return;
			}
			// Fallthrough
		case END:
			this.consumer.addDataMember(this.dataMember);
			pm.popParser(true);
		}
	}
}
