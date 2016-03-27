package dyvil.reflect;

/**
 * The <b>Modifiers</b> interface declares all (visible and invisible) modifiers that can be used <i>Dyvil</i> source
 * code and that can appear in class files. Note that only modifiers less than {@code 0xFFFF} will actually appear in
 * the Bytecode, other modifiers such Dyvil-specific ones will be stored in {@link
 * dyvil.annotation._internal.DyvilModifiers DyvilModifiers} annotations.
 *
 * @author Clashsoft
 */
public interface Modifiers
{
	// Access Modifiers
	
	/**
	 * Default (package) access modifier.
	 */
	int PACKAGE = 0x00000000;
	
	/**
	 * {@code public} access modifier.
	 */
	int PUBLIC = 0x00000001;
	
	/**
	 * {@code private} access modifier.
	 */
	int PRIVATE = 0x00000002;
	
	/**
	 * {@code protected} access modifier.
	 */
	int PROTECTED = 0x00000004;
	
	/**
	 * {@code static} modifier.
	 */
	int STATIC = 0x00000008;
	
	/**
	 * {@code final} modifier.
	 */
	int FINAL = 0x00000010;
	
	/**
	 * <i>Dyvil</i> {@code const} modifier. This modifier is just a shortcut for {@code static final} and should be used
	 * to declare constants.
	 */
	int CONST = STATIC | FINAL;
	
	/**
	 * {@code synchronized} modifier.
	 */
	int SYNCHRONIZED = 0x00000020;
	
	/**
	 * {@code volatile} modifier.
	 */
	int VOLATILE = 0x00000040;
	
	/**
	 * Modifier used to declare a method to be a <i>bridge</i> method, i.e. a method generated by the compiler to
	 * support generic formal parameters.
	 */
	int BRIDGE = 0x00000040;
	
	/**
	 * {@code transient} modifier.
	 */
	int TRANSIENT = 0x00000080;
	
	/**
	 * Modifier used to declare that the last parameter of a method is a <i>varargs</i> parameter.
	 */
	int VARARGS = 0x00000080;
	
	/**
	 * {@code native} modifier.
	 */
	int NATIVE = 0x00000100;
	
	/**
	 * {@code abstract} modifier.
	 */
	int ABSTRACT = 0x00000400;
	
	/**
	 * Modifier used to declare that a class is an {@code interface}.
	 */
	int INTERFACE_CLASS = 0x00000200 | ABSTRACT;
	
	/**
	 * {@code stricfp} modifier.
	 */
	int STRICT = 0x00000800;
	
	/**
	 * Modifier used for fields and methods that are not present in the source code and generated by the compiler.
	 */
	int SYNTHETIC = 0x00001000;
	
	/**
	 * Modifier used to declare a class to be an annotation ({@code @interface} ).
	 */
	int ANNOTATION = 0x00002000 | INTERFACE_CLASS;
	
	/**
	 * Modifier used to declare a class to be an {@code enum} class.
	 */
	int ENUM = 0x00004000;
	
	/**
	 * Modifier used for constructors and fields of anonymous classes.
	 */
	int MANDATED = 0x00008000;
	
	// Type Modifiers
	
	/**
	 * <i>Dyvil</i> {@code object} modifier. If a class is marked with this modifier, it is a singleton object class.
	 */
	int OBJECT_CLASS = 0x00010000;
	
	/**
	 * <i>Dyvil</i> {@code object} modifier. If a class is marked with this modifier, it is a case class. This modifier
	 * not be visible in the bytecode.
	 */
	int CASE_CLASS = 0x00020000;
	
	/**
	 * <i>Dyvil</i> {@code functional} modifier. This modifier is a shortcut for the {@link FunctionalInterface}
	 * annotation.
	 */
	int FUNCTIONAL = 0x00040000;

	/**
	 * <i>Dyvil</i> {@code trait} modifier.
	 */
	int TRAIT_CLASS = 0x00080000 | INTERFACE_CLASS;
	
	// Method Modifiers

	/**
	 * <i>Dyvil</i> {@code inline} modifier. If a method is marked with this modifier, it will be inlined by the
	 * compiler to reduce method call overhead.
	 */
	int INLINE = 0x00010000;

	int INFIX_FLAG = 0x00020000;

	/**
	 * <i>Dyvil</i> {@code infix} modifier. If a method is marked with this modifier, it is a method that can be called
	 * on any Object and virtually has the instance as the first parameter. An infix method is always static.
	 */
	int INFIX = INFIX_FLAG | STATIC;

	int EXTENSION_FLAG = 0x00100000;

	int EXTENSION = EXTENSION_FLAG | INFIX;

	/**
	 * <i>Dyvil</i> {@code prefix} modifier. If a method is marked with this modifier, it is a method that can be called
	 * on any Object and virtually uses the first (and only) parameter as the instance.
	 */
	int PREFIX = 0x00040000;
	
	/**
	 * <i>Dyvil</i> {@code override} modifier. This modifier is a shortcut for the {@link Override} annotation.
	 */
	int OVERRIDE = 0x00080000;
	
	// Field Modifiers
	
	/**
	 * <i>Dyvil</i> {@code lazy} modifier. The {@code lazy} modifier can be applied on fields, variables and parameters
	 * and has a different behavior on each different type.
	 */
	int LAZY = 0x00010000;
	
	// Parameter Modifiers
	
	// LAZY
	
	/**
	 * <i>Dyvil</i> {@code var} modifier. This is used to mark that a parameter is Call-By-Reference. If a parameter
	 * doesn't have this flag, it behaves like a normal formal parameter.
	 */
	int VAR = 0x00040000;
	
	// Member Modifiers
	
	/**
	 * <i>Dyvil</i> {@code internal} modifier. This is used to mark that a class, method or field is only visible from
	 * inside the current library / project.
	 */
	int INTERNAL = 0x01000000;
	
	/**
	 * <i>Dyvil</i> {@code deprecated} modifier. This modifier is a shortcut for the {@link Deprecated} annotation.
	 */
	int DEPRECATED = 0x02000000;
	
	/**
	 * <i>Dyvil</i> {@code sealed} modifier. This modifier is used to mark that a class that is {@code sealed}, i.e. it
	 * can only be extended from classes in the same library. All sub-classes are always known at compile time.
	 */
	int SEALED = 0x04000000;
	
	/**
	 * The modifiers that can be used to declare the class type (i.e., {@code class}, {@code interface}, {@code trait},
	 * {@code enum}, {@code object} or {@code annotation} / {@code @interface}). This value excludes the {@code
	 * ABSTRACT} bit flag.
	 */
	int CLASS_TYPE_MODIFIERS = (INTERFACE_CLASS | ANNOTATION | ENUM | OBJECT_CLASS | TRAIT_CLASS) & ~ABSTRACT;

	int VISIBILITY_MODIFIERS = PUBLIC | PROTECTED | PRIVATE;

	/**
	 * The access modifiers.
	 */
	int ACCESS_MODIFIERS = VISIBILITY_MODIFIERS | INTERNAL;
	
	/**
	 * The modifiers that can be used on any member.
	 */
	int MEMBER_MODIFIERS = ACCESS_MODIFIERS | STATIC | FINAL | SYNTHETIC;
	
	/**
	 * The modifiers that can be used on classes.
	 */
	int CLASS_MODIFIERS = MEMBER_MODIFIERS | ABSTRACT | STRICT | CASE_CLASS | FUNCTIONAL | SEALED | TRAIT_CLASS;
	
	/**
	 * The modifiers that can be used on fields.
	 */
	int FIELD_MODIFIERS = MEMBER_MODIFIERS | TRANSIENT | VOLATILE | LAZY;
	
	/**
	 * The modifiers that can be used on methods.
	 */
	int METHOD_MODIFIERS =
			MEMBER_MODIFIERS | ABSTRACT | SYNCHRONIZED | NATIVE | STRICT | INLINE | INFIX | EXTENSION | PREFIX | BRIDGE
					| VARARGS | OVERRIDE;

	/**
	 * The modifiers that can be used on parameters.
	 */
	int PARAMETER_MODIFIERS = FINAL | LAZY | VAR | MANDATED | EXTENSION;

	/**
	 * The modifiers that can be applied to class parameters.
	 */
	int CLASS_PARAMETER_MODIFIERS = ACCESS_MODIFIERS | FINAL;

	/**
	 * The modifiers that cna be applied to variables.
	 */
	int VARIABLE_MODIFIERS = FINAL;
}
