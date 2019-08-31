package dyvilx.tools.compiler.ast.attribute.modifiers;

import dyvil.reflect.Modifiers;

public enum BaseModifiers implements Modifier
{
	// Visibility Modifiers
	PACKAGE_PRIVATE(Modifiers.PACKAGE, "package private"),
	PRIVATE(Modifiers.PRIVATE, "private"),
	PRIVATE_PROTECTED(Modifiers.PRIVATE | Modifiers.PROTECTED, "private protected"),
	PROTECTED(Modifiers.PROTECTED, "protected"),
	PUBLIC(Modifiers.PUBLIC, "public"),
	INTERNAL(Modifiers.INTERNAL, "internal"),
	// Access Modifiers
	STATIC(Modifiers.STATIC, "static"),
	FINAL(Modifiers.FINAL, "final"),
	ABSTRACT(Modifiers.ABSTRACT, "abstract"),
	// Method Modifiers
	PREFIX(Modifiers.PREFIX, "prefix"),
	INFIX(Modifiers.INFIX, "infix"),
	POSTFIX(Modifiers.POSTFIX, "postfix"),
	IMPLICIT(Modifiers.IMPLICIT, "implicit"),
	OVERRIDE(Modifiers.OVERRIDE, "override"),
	INLINE(Modifiers.INLINE, "inline"),
	SYNCHRONIZED(Modifiers.SYNCHRONIZED, "synchronized"),
	EXTENSION(Modifiers.EXTENSION, "extension"),
	// Class Modifiers
	CASE(Modifiers.CASE_CLASS, "case"),
	ENUM(Modifiers.ENUM, "enum"),
	// Field Modifiers
	CONST(Modifiers.CONST, "const"),
	LAZY(Modifiers.LAZY, "lazy"),
	// Parameter Modifiers
	EXPLICIT(Modifiers.EXPLICIT, "explicit"),;

	public static final String VISIBILITY_MODIFIERS = "public,private,protected,private protected,package private,";

	/**
	 * The access modifiers.
	 */
	public static final String ACCESS_MODIFIERS = VISIBILITY_MODIFIERS + "internal,";

	/**
	 * The modifiers that can be used on any member.
	 */
	public static final String MEMBER_MODIFIERS = ACCESS_MODIFIERS + "static,final,";

	/**
	 * The modifiers that can be used on classes.
	 */
	public static final String CLASS_MODIFIERS = MEMBER_MODIFIERS + "abstract,case,sealed,";

	/**
	 * The modifiers that can be used on interfaces.
	 */
	public static final String INTERFACE_MODIFIERS = ACCESS_MODIFIERS + "static,sealed,";

	/**
	 * The modifiers that can be used on traits.
	 */
	public static final String TRAIT_MODIFIERS = ACCESS_MODIFIERS + "static,sealed,";

	/**
	 * The modifiers that can be used on annotations.
	 */
	public static final String ANNOTATION_MODIFIERS = ACCESS_MODIFIERS + "static,";

	/**
	 * The modifiers that can be used on enums.
	 */
	public static final String ENUM_MODIFIERS = ACCESS_MODIFIERS + "static,";

	/**
	 * The modifiers that can be used on objects.
	 */
	public static final String OBJECT_MODIFIERS = ACCESS_MODIFIERS + "static,implicit,";

	/**
	 * The modifiers that can be used on fields.
	 */
	public static final String FIELD_MODIFIERS = MEMBER_MODIFIERS + "lazy,implicit,";

	/**
	 * The modifiers that can be used on methods.
	 */
	public static final String METHOD_MODIFIERS =
		MEMBER_MODIFIERS + "abstract,synchronized,native,inline,infix,postfix,prefix,extension,implicit,override,";

	/**
	 * The modifiers that can be applied to class parameters.
	 */
	public static final String CLASS_PARAMETER_MODIFIERS = ACCESS_MODIFIERS + "final,implicit,explicit,override,";

	/**
	 * The modifiers that cna be applied to variables.
	 */
	public static final String VARIABLE_MODIFIERS = "final,implicit,";

	/**
	 * The modifiers that can be used on parameters.
	 */
	public static final String PARAMETER_MODIFIERS = VARIABLE_MODIFIERS + "explicit,";

	public static final String CONSTRUCTOR_MODIFIERS = ACCESS_MODIFIERS;

	public static final String INITIALIZER_MODIFIERS = "private,static,";

	private final long   flags;
	private final String name;

	BaseModifiers(long flags, String name)
	{
		this.flags = flags;
		this.name = name;
	}

	@Override
	public long flags()
	{
		return this.flags;
	}

	@Override
	public String toString()
	{
		return this.name;
	}

	@Override
	public void toString(String indent, StringBuilder builder)
	{
		builder.append(this.name);
	}
}
