package dyvilx.tools.compiler.transform;

import dyvil.array.ObjectArray;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.context.IImplicitContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.expression.LiteralConversion;
import dyvilx.tools.compiler.ast.generic.ITypeContext;
import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.parsing.marker.Marker;
import dyvilx.tools.parsing.marker.MarkerList;
import dyvil.source.position.SourcePosition;

public final class TypeChecker
{
	public interface MarkerSupplier
	{
		Marker createMarker(SourcePosition position, IType expected, IType actual);
	}

	private TypeChecker()
	{
		// no instances
	}

	public static MarkerSupplier markerSupplier(String error)
	{
		return markerSupplier(error, ObjectArray.EMPTY);
	}

	public static MarkerSupplier markerSupplier(String error, String expectedError, String actualError)
	{
		return (position, expectedType, actualType) -> typeError(position, expectedType, actualType, error,
		                                                         expectedError, actualError);
	}

	public static MarkerSupplier markerSupplier(String error, String expectedError, String actualError, Object arg)
	{
		return (position, expectedType, actualType) -> typeError(position, expectedType, actualType, error,
		                                                         expectedError, actualError, arg);
	}

	public static MarkerSupplier markerSupplier(String error, String expectedError, String actualError, Object... args)
	{
		return (position, expectedType, actualType) -> typeError(position, expectedType, actualType, error,
		                                                         expectedError, actualError, args);
	}

	public static MarkerSupplier markerSupplier(String error, Object arg)
	{
		return (position, expected, actual) -> typeError(position, expected, actual, error, arg);
	}

	public static MarkerSupplier markerSupplier(String error, Object... args)
	{
		return (position, expected, actual) -> typeError(position, expected, actual, error, args);
	}

	public static int getTypeMatch(IValue value, IType type, IImplicitContext context)
	{
		final int direct = value.getTypeMatch(type, context);
		if (direct != IValue.MISMATCH)
		{
			return direct;
		}

		if (Types.isConvertible(value.getType(), type))
		{
			return IValue.CONVERSION_MATCH;
		}

		if (context != null && IContext.resolveImplicits(context, value, type).hasCandidate())
		{
			return IValue.IMPLICIT_CONVERSION_MATCH;
		}

		// No implicit conversions available
		return IValue.MISMATCH;
	}

	private static IValue convertValueDirect(IValue value, IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		final IValue typedValue = value.withType(type, typeContext, markers, context);
		if (typedValue != null)
		{
			return typedValue;
		}

		final IValue converted1 = type.convertFrom(value, value.getType(), typeContext, markers, context);
		if (converted1 != null)
		{
			return converted1;
		}

		final IValue converted2 = value.getType().convertTo(value, type, typeContext, markers, context);
		if (converted2 != null)
		{
			return converted2;
		}

		final IMethod converter = IContext.resolveImplicits(context, value, type).getBestMember();
		if (converter == null)
		{
			return null;
		}

		return LiteralConversion.converting(value, type, converter, markers, context, typeContext);
	}

	public static IValue convertValue(IValue value, IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (type.hasTypeVariables())
		{
			type = type.getConcreteType(typeContext);
		}

		return convertValueDirect(value, type, typeContext, markers, context);
	}

	public static IValue convertValue(IValue value, IType type, ITypeContext typeContext, MarkerList markers, IContext context, MarkerSupplier markerSupplier)
	{
		final IType concreteType = type.getConcreteType(typeContext);
		final IValue newValue = convertValueDirect(value, concreteType, typeContext, markers, context);
		if (newValue != null)
		{
			if (typeContext != null && !typeContext.isReadonly() && type.hasTypeVariables())
			{
				type.inferTypes(newValue.getType(), typeContext);
			}
			return newValue;
		}

		if (value.isResolved())
		{
			markers.add(markerSupplier.createMarker(value.getPosition(), concreteType, value.getType()));
		}
		return value;
	}

	public static Marker typeError(IValue value, IType type, ITypeContext typeContext, String key, Object... args)
	{
		return typeError(value.getPosition(), type.getConcreteType(typeContext), value.getType(), key, args);
	}

	public static Marker typeError(SourcePosition position, IType expected, IType actual, String key, Object... args)
	{
		return typeError(position, expected, actual, key, "type.expected", "value.type", args);
	}

	public static Marker typeError(SourcePosition position, IType expected, IType actual, String key, String expectedError, String actualError, Object... args)
	{
		final Marker marker = Markers.semanticError(position, key, args);
		marker.addInfo(Markers.getSemantic(expectedError, expected));
		marker.addInfo(Markers.getSemantic(actualError, actual));
		return marker;
	}

	public static Marker typeError(SourcePosition position, IType expectedType, IType actualType, String error, String expectedError, String actualError)
	{
		final Marker marker = Markers.semanticError(position, error);
		marker.addInfo(Markers.getSemantic(expectedError, expectedType));
		marker.addInfo(Markers.getSemantic(actualError, actualType));
		return marker;
	}
}
