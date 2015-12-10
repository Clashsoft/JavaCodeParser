package dyvil.annotation._internal;

import dyvil.reflect.Modifiers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for <b>inline</b> methods. Methods annotated as <i>inline</i> will
 * be inlined by the <i>Dyvil Compiler</i> without checking for normal inline
 * conditions. That means that instead of an {@code INVOKE} instruction, the
 * entire body of the method will be inserted at the call site.
 *
 * @author Clashsoft
 * @version 1.0
 * @see Modifiers#INLINE
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface inline
{}
