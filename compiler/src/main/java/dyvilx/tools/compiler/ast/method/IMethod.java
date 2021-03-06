package dyvilx.tools.compiler.ast.method;

import dyvil.lang.Name;
import dyvil.reflect.Modifiers;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.asm.Handle;
import dyvilx.tools.asm.Label;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.generic.GenericData;
import dyvilx.tools.compiler.ast.generic.ITypeContext;
import dyvilx.tools.compiler.ast.generic.ITypeParametricMember;
import dyvilx.tools.compiler.ast.member.MemberKind;
import dyvilx.tools.compiler.ast.method.intrinsic.IntrinsicData;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.parsing.marker.MarkerList;

import static dyvil.reflect.Modifiers.*;

public interface IMethod extends ICallableMember, ITypeParametricMember, IContext
{
	// --------------- Attributes ---------------

	@Override
	default MemberKind getKind()
	{
		return MemberKind.METHOD;
	}

	@Override
	default int getJavaFlags()
	{
		int javaFlags = ICallableMember.super.getJavaFlags();
		if (this.hasModifier(Modifiers.STATIC | Modifiers.ABSTRACT))
		{
			// for static abstract methods, move the abstract modifier
			javaFlags &= ~ABSTRACT;
		}
		if (this.hasModifier(Modifiers.FINAL) && this.getEnclosingClass().hasModifier(Modifiers.INTERFACE))
		{
			// for final interface methods, move the final modifier
			javaFlags &= ~FINAL;
		}
		if (this.hasModifier(Modifiers.EXTENSION))
		{
			// extension methods are internally always static, but the dyvil flags store if they are declared static
			javaFlags |= STATIC;
		}
		return javaFlags;
	}

	@Override
	default long getDyvilFlags()
	{
		long dyvilFlags = ICallableMember.super.getDyvilFlags();
		if (this.hasModifier(Modifiers.STATIC | Modifiers.ABSTRACT))
		{
			// for static abstract methods, move the abstract modifier
			dyvilFlags |= ABSTRACT;
		}
		if (this.hasModifier(Modifiers.FINAL) && this.getEnclosingClass().hasModifier(Modifiers.INTERFACE))
		{
			// for final interface methods, move the final modifier
			dyvilFlags |= FINAL;
		}
		if (this.isStatic() && this.hasModifier(Modifiers.EXTENSION))
		{
			// extension methods are internally always static, but the dyvil flags store if they are declared static
			dyvilFlags |= Modifiers.STATIC;
		}
		return dyvilFlags;
	}

	@Override
	default void setDyvilFlags(long dyvilFlags)
	{
		if ((dyvilFlags & Modifiers.EXTENSION) != 0 && (dyvilFlags & Modifiers.STATIC) == 0)
		{
			// non-static extension methods need to remove the static flag from the previously set Java modifiers
			this.getAttributes().removeFlag(Modifiers.STATIC);
		}
		ICallableMember.super.setDyvilFlags(dyvilFlags);
	}

	boolean isImplicitConversion();

	boolean isFunctional();

	boolean isObjectMethod();

	default boolean isNested()
	{
		return false;
	}

	boolean hasTypeVariables();

	// --------------- This Type ---------------

	IType getReceiverType();

	// --------------- Method Matching ---------------

	void checkMatch(MatchList<IMethod> list, IValue receiver, Name name, ArgumentList arguments);

	void checkImplicitMatch(MatchList<IMethod> list, IValue value, IType type);

	// --------------- Call Checking ---------------

	GenericData getGenericData(GenericData data, IValue instance, ArgumentList arguments);

	IValue checkArguments(MarkerList markers, SourcePosition position, IContext context, IValue receiver,
		ArgumentList arguments, GenericData genericData);

	void checkCall(MarkerList markers, SourcePosition position, IContext context, IValue instance,
		ArgumentList arguments, ITypeContext typeContext);

	// --------------- Override Checking ---------------

	/**
	 * Checks if this method overrides the given {@code candidate} method.
	 *
	 * @param candidate
	 * 	the potential super-method
	 * @param typeContext
	 * 	the type context for type specialization
	 *
	 * @return {@code true}, if this method overrides the given candidate
	 */
	boolean overrides(IMethod candidate, ITypeContext typeContext);

	void addOverride(IMethod method);

	// Generics

	// --------------- Compilation ---------------

	// - - - - - - - - Intrinsics - - - - - - - -

	boolean isIntrinsic();

	IntrinsicData getIntrinsicData();

	// - - - - - - - - Invoke Opcode and Handle - - - - - - - -

	int getInvokeOpcode();

	Handle toHandle();

	// - - - - - - - - Call Compilation - - - - - - - -

	void writeCall(MethodWriter writer, IValue receiver, ArgumentList arguments, ITypeContext typeContext,
		IType targetType, int lineNumber) throws BytecodeException;

	void writeInvoke(MethodWriter writer, IValue receiver, ArgumentList arguments, ITypeContext typeContext,
		int lineNumber) throws BytecodeException;

	void writeJump(MethodWriter writer, Label dest, IValue receiver, ArgumentList arguments, ITypeContext typeContext,
		int lineNumber) throws BytecodeException;

	void writeInvJump(MethodWriter writer, Label dest, IValue receiver, ArgumentList arguments,
		ITypeContext typeContext, int lineNumber) throws BytecodeException;
}
