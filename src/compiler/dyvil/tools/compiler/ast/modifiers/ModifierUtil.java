package dyvil.tools.compiler.ast.modifiers;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.member.IClassMember;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.transform.DyvilKeywords;
import dyvil.tools.compiler.transform.DyvilSymbols;
import dyvil.tools.compiler.util.MarkerMessages;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.token.IToken;

public final class ModifierUtil
{
	private ModifierUtil()
	{
	}
	
	public static void writeAccessModifiers(int mod, StringBuilder sb)
	{
		if ((mod & Modifiers.PUBLIC) == Modifiers.PUBLIC)
		{
			sb.append("public ");
		}
		if ((mod & Modifiers.PRIVATE) == Modifiers.PRIVATE)
		{
			sb.append("private ");
		}
		if ((mod & Modifiers.PROTECTED) == Modifiers.PROTECTED)
		{
			sb.append("protected ");
		}
		
		switch (mod & 3)
		{
		case Modifiers.PACKAGE:
			break;
		case Modifiers.PROTECTED:
			sb.append("protected ");
			break;
		case Modifiers.PRIVATE:
			sb.append("private ");
			break;
		}
		
		if ((mod & Modifiers.INTERNAL) == Modifiers.INTERNAL)
		{
			sb.append("internal ");
		}
	}

	public static String classTypeToString(int mod)
	{
		StringBuilder stringBuilder = new StringBuilder();
		writeClassType(mod, stringBuilder);
		return stringBuilder.toString();
	}

	public static void writeClassType(int mod, StringBuilder sb)
	{
		if (mod == 0)
		{
			sb.append("class ");
		}
		else if ((mod & Modifiers.ANNOTATION) == Modifiers.ANNOTATION)
		{
			sb.append("@interface ");
		}
		else if ((mod & Modifiers.INTERFACE_CLASS) == Modifiers.INTERFACE_CLASS)
		{
			sb.append("interface ");
		}
		else if ((mod & Modifiers.ENUM) == Modifiers.ENUM)
		{
			sb.append("enum ");
		}
		else if ((mod & Modifiers.OBJECT_CLASS) == Modifiers.OBJECT_CLASS)
		{
			sb.append("object ");
		}
		else
		{
			sb.append("class ");
		}
	}

	public static String classModifiersToString(int mod)
	{
		StringBuilder stringBuilder = new StringBuilder();
		writeClassModifiers(mod, stringBuilder);
		return stringBuilder.toString();
	}
	
	public static void writeClassModifiers(int mod, StringBuilder sb)
	{
		if ((mod & Modifiers.STATIC) == Modifiers.STATIC)
		{
			sb.append("static ");
		}
		if ((mod & Modifiers.ABSTRACT) == Modifiers.ABSTRACT)
		{
			sb.append("abstract ");
		}
		if ((mod & Modifiers.FINAL) == Modifiers.FINAL)
		{
			sb.append("final ");
		}
		if ((mod & Modifiers.SEALED) == Modifiers.SEALED)
		{
			sb.append("sealed ");
		}
		if ((mod & Modifiers.STRICT) == Modifiers.STRICT)
		{
			sb.append("@Strict ");
		}
		if ((mod & Modifiers.FUNCTIONAL) == Modifiers.FUNCTIONAL)
		{
			sb.append("functional ");
		}
		if ((mod & Modifiers.CASE_CLASS) == Modifiers.CASE_CLASS)
		{
			sb.append("case ");
		}
	}

	public static String fieldModifiersToString(int mod)
	{
		StringBuilder stringBuilder = new StringBuilder();
		writeFieldModifiers(mod, stringBuilder);
		return stringBuilder.toString();
	}
	
	public static void writeFieldModifiers(int mod, StringBuilder sb)
	{
		if ((mod & Modifiers.LAZY) == Modifiers.LAZY)
		{
			sb.append("lazy ");
		}
		else if ((mod & Modifiers.CONST) == Modifiers.CONST)
		{
			sb.append("const ");
		}
		else
		{
			if ((mod & Modifiers.STATIC) == Modifiers.STATIC)
			{
				sb.append("static ");
			}
			if ((mod & Modifiers.FINAL) == Modifiers.FINAL)
			{
				sb.append("final ");
			}
		}
		
		if ((mod & Modifiers.TRANSIENT) == Modifiers.TRANSIENT)
		{
			sb.append("@Transient ");
		}
		if ((mod & Modifiers.VOLATILE) == Modifiers.VOLATILE)
		{
			sb.append("@Volatile ");
		}
	}

	public static String methodModifiersToString(int mod)
	{
		StringBuilder stringBuilder = new StringBuilder();
		writeMethodModifiers(mod, stringBuilder);
		return stringBuilder.toString();
	}
	
	public static void writeMethodModifiers(int mod, StringBuilder sb)
	{
		if ((mod & Modifiers.EXTENSION) == Modifiers.EXTENSION)
		{
			sb.append("extension ");
		}
		else if ((mod & Modifiers.INFIX) != 0 && (mod & Modifiers.INFIX) != Modifiers.STATIC)
		{
			sb.append("infix ");
		}
		else if ((mod & Modifiers.STATIC) == Modifiers.STATIC)
		{
			sb.append("static ");
		}
		
		if ((mod & Modifiers.FINAL) == Modifiers.FINAL)
		{
			sb.append("final ");
		}
		if ((mod & Modifiers.SEALED) == Modifiers.SEALED)
		{
			sb.append("sealed ");
		}
		if ((mod & Modifiers.PREFIX) == Modifiers.PREFIX)
		{
			sb.append("prefix ");
		}
		
		if ((mod & Modifiers.SYNCHRONIZED) == Modifiers.SYNCHRONIZED)
		{
			sb.append("synchronized ");
		}
		if ((mod & Modifiers.NATIVE) == Modifiers.NATIVE)
		{
			sb.append("@Native ");
		}
		if ((mod & Modifiers.ABSTRACT) == Modifiers.ABSTRACT)
		{
			sb.append("abstract ");
		}
		if ((mod & Modifiers.STRICT) == Modifiers.STRICT)
		{
			sb.append("@Strict ");
		}
		if ((mod & Modifiers.INLINE) == Modifiers.INLINE)
		{
			sb.append("inline ");
		}
		if ((mod & Modifiers.OVERRIDE) == Modifiers.OVERRIDE)
		{
			sb.append("override ");
		}
	}
	
	public static void writeParameterModifier(int mod, StringBuilder sb)
	{
		if ((mod & Modifiers.LAZY) == Modifiers.LAZY)
		{
			sb.append("lazy ");
		}
		if ((mod & Modifiers.FINAL) == Modifiers.FINAL)
		{
			sb.append("final ");
		}
		if ((mod & Modifiers.VAR) == Modifiers.VAR)
		{
			sb.append("var ");
		}
	}
	
	public static int readClassTypeModifier(IToken token, IParserManager parserManager)
	{
		switch (token.type())
		{
		case DyvilSymbols.AT:
			// @interface
			if (token.next().type() == DyvilKeywords.INTERFACE)
			{
				parserManager.skip();
				return Modifiers.ANNOTATION;
			}
			return -1;
		case DyvilKeywords.CASE:
			switch (token.next().type())
			{
			// case class
			case DyvilKeywords.CLASS:
				parserManager.skip();
				return Modifiers.CASE_CLASS;
			// case object
			case DyvilKeywords.OBJECT:
				parserManager.skip();
				return Modifiers.OBJECT_CLASS | Modifiers.CASE_CLASS;
			}
			return -1;
		case DyvilKeywords.CLASS:
			return 0;
		case DyvilKeywords.INTERFACE:
			return Modifiers.INTERFACE_CLASS;
		case DyvilKeywords.ENUM:
			return Modifiers.ENUM;
		case DyvilKeywords.OBJECT:
			return Modifiers.OBJECT_CLASS;
		}
		return -1;
	}
	
	public static void checkMethodModifiers(MarkerList markers, IClassMember member, int modifiers, boolean hasValue, String type)
	{
		boolean isStatic = (modifiers & Modifiers.STATIC) != 0;
		boolean isAbstract = (modifiers & Modifiers.ABSTRACT) != 0;
		boolean isNative = (modifiers & Modifiers.NATIVE) != 0;
		
		// If the method does not have an implementation and is static
		if (isStatic && isAbstract)
		{
			markers.add(
					MarkerMessages
							.createError(member.getPosition(), "modifiers.static.abstract", Util.toString(member, type)));
		}
		else if (isAbstract && isNative)
		{
			markers.add(
					MarkerMessages
							.createError(member.getPosition(), "modifiers.native.abstract", Util.toString(member, type)));
		}
		else
		{
			if (isStatic)
			{
				if (!hasValue)
				{
					markers.add(MarkerMessages.createError(member.getPosition(), "modifiers.static.unimplemented",
					                                       Util.toString(member, type)));
				}
			}
			if (isNative)
			{
				if (!hasValue)
				{
					markers.add(MarkerMessages.createError(member.getPosition(), "modifiers.native.implemented",
					                                       Util.toString(member, type)));
				}
			}
			if (isAbstract)
			{
				IClass theClass = member.getTheClass();
				if (!theClass.isAbstract())
				{
					markers.add(MarkerMessages.createError(member.getPosition(), "modifiers.abstract.concrete_class",
					                                       Util.toString(member, type), theClass.getName()));
				}
				if (hasValue)
				{
					markers.add(MarkerMessages.createError(member.getPosition(), "modifiers.abstract.implemented",
					                                       Util.toString(member, type)));
				}
			}
		}
		if (!hasValue && !isAbstract && !isNative)
		{
			markers.add(MarkerMessages
					            .createError(member.getPosition(), "modifiers.unimplemented", Util.toString(member, type)));
		}
	}
}