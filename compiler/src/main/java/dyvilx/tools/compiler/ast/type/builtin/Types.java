package dyvilx.tools.compiler.ast.type.builtin;

import dyvil.lang.Name;
import dyvil.reflect.Opcodes;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.context.CombiningContext;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.generic.ITypeContext;
import dyvilx.tools.compiler.ast.generic.ITypeParameter;
import dyvilx.tools.compiler.ast.header.IHeaderUnit;
import dyvilx.tools.compiler.ast.structure.Package;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.TypeList;
import dyvilx.tools.compiler.ast.type.compound.ArrayType;
import dyvilx.tools.compiler.ast.type.compound.ImplicitNullableType;
import dyvilx.tools.compiler.ast.type.compound.NullableType;
import dyvilx.tools.compiler.ast.type.compound.UnionType;
import dyvilx.tools.compiler.ast.type.raw.ClassType;
import dyvilx.tools.compiler.ast.type.raw.InternalType;
import dyvilx.tools.compiler.backend.ClassFormat;
import dyvilx.tools.compiler.transform.Names;

import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

public final class Types
{
	public static IHeaderUnit LANG_HEADER;
	public static IContext    BASE_CONTEXT;

	public static final PrimitiveType VOID    = new PrimitiveType(Names.void_, "java/lang/Void",
	                                                              PrimitiveType.VOID_CODE, 'V',
	                                                              Opcodes.ILOAD + Opcodes.RETURN - Opcodes.IRETURN,
	                                                              Opcodes.IALOAD, null);
	public static final PrimitiveType BOOLEAN = new PrimitiveType(Names.boolean_, "java/lang/Boolean",
	                                                              PrimitiveType.BOOLEAN_CODE, 'Z', Opcodes.ILOAD,
	                                                              Opcodes.BALOAD, ClassFormat.BOOLEAN);
	public static final PrimitiveType BYTE    = new PrimitiveType(Names.byte_, "java/lang/Byte",
	                                                              PrimitiveType.BYTE_CODE, 'B', Opcodes.ILOAD,
	                                                              Opcodes.BALOAD, ClassFormat.BOOLEAN);
	public static final PrimitiveType SHORT   = new PrimitiveType(Names.short_, "java/lang/Short",
	                                                              PrimitiveType.SHORT_CODE, 'S', Opcodes.ILOAD,
	                                                              Opcodes.SALOAD, ClassFormat.SHORT);
	public static final PrimitiveType CHAR    = new PrimitiveType(Names.char_, "java/lang/Character",
	                                                              PrimitiveType.CHAR_CODE, 'C', Opcodes.ILOAD,
	                                                              Opcodes.CALOAD, ClassFormat.CHAR);
	public static final PrimitiveType INT     = new PrimitiveType(Names.int_, "java/lang/Integer",
	                                                              PrimitiveType.INT_CODE, 'I', Opcodes.ILOAD,
	                                                              Opcodes.IALOAD, ClassFormat.INT);
	public static final PrimitiveType LONG    = new PrimitiveType(Names.long_, "java/lang/Long",
	                                                              PrimitiveType.LONG_CODE, 'J', Opcodes.LLOAD,
	                                                              Opcodes.LALOAD, ClassFormat.LONG);
	public static final PrimitiveType FLOAT   = new PrimitiveType(Names.float_, "java/lang/Float",
	                                                              PrimitiveType.FLOAT_CODE, 'F', Opcodes.FLOAD,
	                                                              Opcodes.FALOAD, ClassFormat.FLOAT);
	public static final PrimitiveType DOUBLE  = new PrimitiveType(Names.double_, "java/lang/Double",
	                                                              PrimitiveType.DOUBLE_CODE, 'D', Opcodes.DLOAD,
	                                                              Opcodes.DALOAD, ClassFormat.DOUBLE);

	public static final UnknownType UNKNOWN = new UnknownType();
	public static final NullType    NULL    = new NullType();
	public static final AnyType     ANY     = new AnyType();
	public static final NoneType    NONE    = new NoneType();

	public static final NullableType NULLABLE_ANY = new ImplicitNullableType(ANY);

	public static final ClassType OBJECT = new ClassType();
	public static final ClassType STRING = new ClassType();

	public static final ClassType THROWABLE         = new ClassType();
	public static final ClassType EXCEPTION         = new ClassType();
	public static final ClassType RUNTIME_EXCEPTION = new ClassType();
	public static final ClassType SERIALIZABLE      = new ClassType();
	public static final ClassType ENUM              = new ClassType();

	public static IHeaderUnit PRIMITIVES_HEADER;

	public static IClass OBJECT_CLASS;
	public static IClass NULL_CLASS;
	public static IClass NONE_CLASS;
	public static IClass STRING_CLASS;
	public static IClass ENUM_CLASS;

	public static IClass THROWABLE_CLASS;
	public static IClass EXCEPTION_CLASS;
	public static IClass RUNTIME_EXCEPTION_CLASS;
	public static IClass SERIALIZABLE_CLASS;
	public static IClass INTRINSIC_CLASS;

	public static IClass OVERRIDE_CLASS;
	public static IClass MUTATING_CLASS;
	public static IClass MUTABLE_CLASS;
	public static IClass IMMUTABLE_CLASS;
	public static IClass REIFIED_CLASS;
	public static IClass OVERLOADPRIORITY_CLASS;
	public static IClass SWITCHOPTIMIZED_CLASS;
	public static IClass FUNCTIONALINTERFACE_CLASS;

	public static IClass LITERALCONVERTIBLE_CLASS;
	public static IClass FROMBOOLEAN_CLASS;
	public static IClass FROMCHAR_CLASS;
	public static IClass FROMINT_CLASS;
	public static IClass FROMLONG_CLASS;
	public static IClass FROMFLOAT_CLASS;
	public static IClass FROMDOUBLE_CLASS;
	public static IClass FROMSTRING_CLASS;

	private static IClass OBJECT_ARRAY_CLASS;

	public static void initHeaders()
	{
		LANG_HEADER = Package.dyvil.resolveHeader("Lang");
		BASE_CONTEXT = new CombiningContext(LANG_HEADER.getContext(), Package.rootPackage);

		PRIMITIVES_HEADER = Package.dyvilLang.resolveHeader("Primitives");
	}

	public static void initTypes()
	{
		NULL_CLASS = Package.dyvilLangInternal.resolveClass("Null");
		NONE_CLASS = Package.dyvilLangInternal.resolveClass("None");
		OBJECT.theClass = OBJECT_CLASS = Package.javaLang.resolveClass("Object");
		STRING.theClass = STRING_CLASS = Package.javaLang.resolveClass("String");
		ENUM.theClass = ENUM_CLASS = Package.javaLang.resolveClass("Enum");
		THROWABLE.theClass = THROWABLE_CLASS = Package.javaLang.resolveClass("Throwable");
		EXCEPTION.theClass = EXCEPTION_CLASS = Package.javaLang.resolveClass("Exception");
		RUNTIME_EXCEPTION.theClass = RUNTIME_EXCEPTION_CLASS = Package.javaLang.resolveClass("RuntimeException");
		SERIALIZABLE.theClass = SERIALIZABLE_CLASS = Package.javaIO.resolveClass("Serializable");

		OVERRIDE_CLASS = Package.javaLang.resolveClass("Override");
		INTRINSIC_CLASS = Package.dyvilAnnotation.resolveClass("Intrinsic");
		MUTATING_CLASS = Package.dyvilAnnotation.resolveClass("Mutating");
		MUTABLE_CLASS = Package.dyvilAnnotation.resolveClass("Mutable");
		IMMUTABLE_CLASS = Package.dyvilAnnotation.resolveClass("Immutable");
		REIFIED_CLASS = Package.dyvilAnnotation.resolveClass("Reified");
		OVERLOADPRIORITY_CLASS = Package.dyvilAnnotation.resolveClass("OverloadPriority");
		SWITCHOPTIMIZED_CLASS = Package.dyvilAnnotation.resolveClass("SwitchOptimized");
		FUNCTIONALINTERFACE_CLASS = Package.javaLang.resolveClass("FunctionalInterface");

		LITERALCONVERTIBLE_CLASS = Package.dyvilLang.resolveClass("LiteralConvertible");
		FROMINT_CLASS = LITERALCONVERTIBLE_CLASS.resolveClass(Name.fromRaw("FromInt"));
		FROMBOOLEAN_CLASS = LITERALCONVERTIBLE_CLASS.resolveClass(Name.fromRaw("FromBoolean"));
		FROMCHAR_CLASS = LITERALCONVERTIBLE_CLASS.resolveClass(Name.fromRaw("FromChar"));
		FROMLONG_CLASS = LITERALCONVERTIBLE_CLASS.resolveClass(Name.fromRaw("FromLong"));
		FROMFLOAT_CLASS = LITERALCONVERTIBLE_CLASS.resolveClass(Name.fromRaw("FromFloat"));
		FROMDOUBLE_CLASS = LITERALCONVERTIBLE_CLASS.resolveClass(Name.fromRaw("FromDouble"));
		FROMSTRING_CLASS = LITERALCONVERTIBLE_CLASS.resolveClass(Name.fromRaw("FromString"));
	}

	public static IType fromASMType(dyvilx.tools.asm.Type type)
	{
		switch (type.getSort())
		{
		case dyvilx.tools.asm.Type.VOID:
			return VOID;
		case dyvilx.tools.asm.Type.BOOLEAN:
			return BOOLEAN;
		case dyvilx.tools.asm.Type.BYTE:
			return BYTE;
		case dyvilx.tools.asm.Type.SHORT:
			return SHORT;
		case dyvilx.tools.asm.Type.CHAR:
			return CHAR;
		case dyvilx.tools.asm.Type.INT:
			return INT;
		case dyvilx.tools.asm.Type.LONG:
			return LONG;
		case dyvilx.tools.asm.Type.FLOAT:
			return FLOAT;
		case dyvilx.tools.asm.Type.DOUBLE:
			return DOUBLE;
		case dyvilx.tools.asm.Type.OBJECT:
			return new InternalType(type.getInternalName());
		case dyvilx.tools.asm.Type.ARRAY:
			return new ArrayType(fromASMType(type.getElementType()));
		}
		return null;
	}

	public static IClass getObjectArrayClass()
	{
		if (OBJECT_ARRAY_CLASS == null)
		{
			return OBJECT_ARRAY_CLASS = Package.dyvilArray.resolveClass("ObjectArray");
		}
		return OBJECT_ARRAY_CLASS;
	}

	public static int getTypeMatch(IType superType, IType subType)
	{
		if (Types.isSameType(superType, subType))
		{
			return IValue.EXACT_MATCH;
		}
		return Types.isSuperType(superType, subType) ? IValue.SUBTYPE_MATCH : IValue.MISMATCH;
	}

	public static boolean isSameClass(IType type1, IType type2)
	{
		return type1 == type2 || type1.isSameClass(type2);
	}

	public static boolean isSameType(IType type1, IType type2)
	{
		if (type1 == type2)
		{
			return true;
		}

		if (type2.subTypeCheckLevel() > type1.subTypeCheckLevel())
		{
			return type2.isSameType(type1);
		}
		return type1.isSameType(type2);
	}

	public static boolean isVoid(IType type)
	{
		return type.getTypecode() == PrimitiveType.VOID_CODE;
	}

	public static boolean isExactType(IType type1, IType type2)
	{
		return type1 == type2 || type1.isSameType(type2) && type2.isSameType(type1);
	}

	public static boolean isSuperClass(IClass superClass, IClass subClass)
	{
		return superClass == subClass || superClass.getClassType().isSuperClassOf(subClass.getClassType());
	}

	public static boolean isSuperClass(IType superType, IType subType)
	{
		if (superType == subType)
		{
			return true;
		}
		if (subType.subTypeCheckLevel() > superType.subTypeCheckLevel())
		{
			return subType.isSubClassOf(superType);
		}
		return superType.isSuperClassOf(subType);
	}

	public static boolean isSuperType(IType superType, IType subType)
	{
		if (superType == subType)
		{
			return true;
		}
		if (subType.subTypeCheckLevel() > superType.subTypeCheckLevel())
		{
			return subType.isSubTypeOf(superType);
		}
		return superType.isSuperTypeOf(subType);
	}

	public static boolean isAssignable(IType targetType, IType fromType)
	{
		return isSuperType(targetType, fromType) || isConvertible(fromType, targetType);
	}

	public static boolean isConvertible(IType fromType, IType toType)
	{
		return toType.isConvertibleFrom(fromType) || fromType.isConvertibleTo(toType);
	}

	public static Set<IClass> commonClasses(IType type1, IType type2)
	{
		final Set<IClass> superTypes1 = superClasses(type1);
		final Set<IClass> superTypes2 = superClasses(type2);
		superTypes1.retainAll(superTypes2);
		return superTypes1;
	}

	public static IType combine(IType type1, IType type2)
	{
		return UnionType.combine(type1, type2, null);
	}

	private static Set<IClass> superClasses(IType type)
	{
		Set<IClass> types = Collections.newSetFromMap(new IdentityHashMap<>());
		addSuperClasses(type, types);
		return types;
	}

	private static void addSuperClasses(IType type, Collection<IClass> types)
	{
		final IClass theClass = type.getTheClass();
		if (theClass == null)
		{
			return;
		}

		types.add(theClass);

		final IType superType = theClass.getSuperType();
		if (superType != null)
		{
			addSuperClasses(superType, types);
		}

		final TypeList interfaces = theClass.getInterfaces();
		if (interfaces == null)
		{
			return;
		}

		for (int i = 0, count = interfaces.size(); i < count; i++)
		{
			addSuperClasses(interfaces.get(i), types);
		}
	}

	public static IType resolvePrimitive(Name name)
	{
		switch (name.qualified)
		{
		// @formatter:off
		case "void": return VOID;
		case "boolean": return BOOLEAN;
		case "byte": return BYTE;
		case "short": return SHORT;
		case "char": return CHAR;
		case "int": return INT;
		case "long": return LONG;
		case "float": return FLOAT;
		case "double": return DOUBLE;
		case "any": return ANY;
		case "none": return NONE;
		// @formatter:on
		}
		return null;
	}

	public static IType resolveTypeSafely(ITypeContext type, ITypeParameter typeVar)
	{
		final IType resolved = type.resolveType(typeVar);
		return resolved != null ? resolved : typeVar.getUpperBound();
	}
}
