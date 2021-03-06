package dyvilx.tools.compiler.ast.field;

import dyvil.lang.Name;
import dyvil.reflect.Modifiers;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.attribute.AttributeList;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.context.CombiningContext;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.context.IDefaultContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.member.AbstractMember;
import dyvilx.tools.compiler.ast.method.CodeMethod;
import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.ast.method.MatchList;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.parameter.CodeParameter;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.backend.classes.ClassWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.config.Formatting;
import dyvilx.tools.compiler.transform.Names;
import dyvilx.tools.compiler.transform.TypeChecker;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.compiler.util.Util;
import dyvilx.tools.parsing.marker.MarkerList;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.annotation.ElementType;

public class Property extends AbstractMember implements IProperty, IDefaultContext
{
	protected IMethod getter;

	protected IMethod setter;
	protected IValue  initializer;

	// Metadata

	protected IClass enclosingClass;

	protected CodeParameter  setterParameter;
	protected SourcePosition initializerPosition;

	public Property(SourcePosition position, Name name, IType type)
	{
		super(position, name, type);
	}

	public Property(SourcePosition position, Name name, IType type, AttributeList attributes)
	{
		super(position, name, type, attributes);
	}

	@Override
	public void setEnclosingClass(IClass enclosingClass)
	{
		this.enclosingClass = enclosingClass;

		if (this.getter != null)
		{
			this.getter.setEnclosingClass(enclosingClass);
		}
		if (this.setter != null)
		{
			this.setter.setEnclosingClass(enclosingClass);
		}
	}

	@Override
	public IClass getEnclosingClass()
	{
		return this.enclosingClass;
	}

	@Override
	public ElementType getElementType()
	{
		return ElementType.METHOD;
	}

	@Override
	public IMethod getGetter()
	{
		return this.getter;
	}

	@Override
	public IMethod initGetter()
	{
		if (this.getter != null)
		{
			return this.getter;
		}
		final CodeMethod getter = new CodeMethod(this.enclosingClass, this.name, this.type);
		getter.setPosition(this.position);
		return this.getter = getter;
	}

	@Override
	public IMethod getSetter()
	{
		return this.setter;
	}

	@Override
	public void setSetterParameterName(Name name)
	{
		this.initSetter();
		this.setterParameter.setName(name);
	}

	@Override
	public IMethod initSetter()
	{
		if (this.setter != null)
		{
			return this.setter;
		}

		final Name name = Name.from(this.name.unqualified + "_=", this.name.qualified + "_$eq");
		this.setter = new CodeMethod(this.enclosingClass, name, Types.VOID);
		this.setter.setPosition(this.position);
		this.setterParameter = new CodeParameter(this.setter, this.position, Names.newValue, this.type);
		this.setter.getParameters().add(this.setterParameter);

		return this.setter;
	}

	@Override
	public IValue getInitializer()
	{
		return this.initializer;
	}

	@Override
	public void setInitializer(IValue value)
	{
		this.initializer = value;
	}

	@Override
	public SourcePosition getInitializerPosition()
	{
		return this.initializerPosition;
	}

	@Override
	public void setInitializerPosition(SourcePosition position)
	{
		this.initializerPosition = position;
	}

	@Override
	public boolean isThisAvailable()
	{
		return true;
	}

	@Override
	public void checkMatch(MatchList<IMethod> list, IValue receiver, Name name, ArgumentList arguments)
	{
		if (this.getter != null)
		{
			this.getter.checkMatch(list, receiver, name, arguments);
		}

		if (this.setter != null)
		{
			this.setter.checkMatch(list, receiver, name, arguments);
		}
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		super.resolveTypes(markers, context);

		if (this.getter != null)
		{
			// Expose getters for non-boolean properties like "prop" as "getProp" to Java.
			// Boolean properties retain their name.
			// (the naming convention recommends names like isFoo or hasBar)
			if (!Types.isSuperType(Types.BOOLEAN, this.type))
			{
				((CodeMethod) this.getter).setInternalName(Util.getGetter(this.name.qualified));
			}

			final AttributeList getterAttributes = this.getter.getAttributes();

			// Add <generated> Modifier and copy Property Modifiers
			getterAttributes.addFlag(Modifiers.GENERATED);
			Field.copyModifiers(this.attributes, getterAttributes);

			this.getter.setType(this.type);
			this.getter.resolveTypes(markers, context);
		}
		if (this.setter != null)
		{
			// Expose setters for properties like "prop" as "setProp" to Java
			// Boolean properties like "hasProp" also use "setHasProp"
			((CodeMethod) this.setter).setInternalName(Util.getSetter(this.name.qualified));

			final AttributeList setterModifiers = this.setter.getAttributes();

			// Add <generated> Modifier and copy Property Modifiers
			setterModifiers.addFlag(Modifiers.GENERATED);
			Field.copyModifiers(this.attributes, setterModifiers);

			this.setterParameter.setPosition(this.setter.getPosition());
			this.setterParameter.setType(this.type);
			this.setter.resolveTypes(markers, context);
		}
		if (this.initializer != null)
		{
			this.initializer.resolveTypes(markers, new CombiningContext(this, context));
		}
	}

	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		super.resolve(markers, context);

		if (this.getter != null)
		{
			this.getter.resolve(markers, context);

			// Infer Type if necessary
			if (this.type == Types.UNKNOWN)
			{
				this.type = this.getter.getType();

				if (this.setterParameter != null)
				{
					this.setterParameter.setType(this.type);
				}
			}
		}

		if (this.type == Types.UNKNOWN)
		{
			markers.add(Markers.semanticError(this.position, "property.type.infer", this.name));
		}

		if (this.setter != null)
		{
			this.setter.resolve(markers, context);
		}
		if (this.initializer != null)
		{
			final IContext context1 = new CombiningContext(this, context);

			final IValue resolved = this.initializer.resolve(markers, context1);

			this.initializer = TypeChecker.convertValue(resolved, Types.VOID, Types.VOID, markers, context1,
			                                            TypeChecker.markerSupplier("property.initializer.type"));
		}
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		super.checkTypes(markers, context);

		if (this.getter != null)
		{
			this.getter.checkTypes(markers, context);
		}
		if (this.setter != null)
		{
			this.setter.checkTypes(markers, context);
		}
		if (this.initializer != null)
		{
			this.initializer.checkTypes(markers, new CombiningContext(this, context));
		}
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		super.check(markers, context);

		if (Types.isVoid(this.type))
		{
			markers.add(Markers.semanticError(this.position, "property.type.void"));
		}

		if (this.getter != null)
		{
			this.getter.check(markers, context);
		}

		if (this.setter != null)
		{
			this.setter.check(markers, context);
		}

		// No setter and no getter
		if (this.getter == null && this.setter == null)
		{
			markers.add(Markers.semantic(this.position, "property.empty", this.name));
		}

		if (this.initializer != null)
		{
			this.initializer.check(markers, new CombiningContext(this, context));
		}
	}

	@Override
	public void foldConstants()
	{
		super.foldConstants();

		if (this.getter != null)
		{
			this.getter.foldConstants();
		}
		if (this.setter != null)
		{
			this.setter.foldConstants();
		}
		if (this.initializer != null)
		{
			this.initializer = this.initializer.foldConstants();
		}
	}

	@Override
	public void cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		super.cleanup(compilableList, classCompilableList);

		if (this.getter != null)
		{
			this.getter.cleanup(compilableList, classCompilableList);
		}
		if (this.setter != null)
		{
			this.setter.cleanup(compilableList, classCompilableList);
		}
		if (this.initializer != null)
		{
			this.initializer = this.initializer.cleanup(compilableList, classCompilableList);
		}
	}

	// Compilation

	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{
		if (this.getter != null)
		{
			this.getter.write(writer);
		}
		if (this.setter != null)
		{
			this.setter.write(writer);
		}
	}

	@Override
	public void writeClassInit(MethodWriter writer) throws BytecodeException
	{
		if (this.initializer != null && !this.hasModifier(Modifiers.STATIC))
		{
			this.initializer.writeExpression(writer, Types.VOID);
		}
	}

	@Override
	public void writeStaticInit(MethodWriter writer) throws BytecodeException
	{
		if (this.initializer != null && this.hasModifier(Modifiers.STATIC))
		{
			this.initializer.writeExpression(writer, Types.VOID);
		}
	}

	@Override
	public void writeSignature(DataOutput out) throws IOException
	{
	}

	@Override
	public void readSignature(DataInput in) throws IOException
	{
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		super.toString(prefix, buffer);
		IDataMember.toString(prefix, buffer, this, "property.type_ascription");
		formatBody(this, prefix, buffer);
	}

	public static void formatBody(IProperty property, String prefix, StringBuilder buffer)
	{
		// Block Start
		if (Formatting.getBoolean("property.block.newline"))
		{
			buffer.append('\n').append(prefix);
		}
		else
		{
			buffer.append(' ');
		}
		buffer.append('{');

		// Initializer

		final IValue initializer = property.getInitializer();
		final IMethod getter = property.getGetter();
		final IMethod setter = property.getSetter();

		if (initializer != null)
		{
			formatInitializer(initializer, prefix, buffer);

			if (getter != null || setter != null)
			{
				buffer.append('\n').append(prefix);
			}
		}

		// Getter
		if (getter != null)
		{
			formatGetter(getter, prefix, buffer);

			if (setter != null)
			{
				buffer.append('\n').append(prefix);
			}
		}

		// Setter
		if (setter != null)
		{
			formatSetter(setter, prefix, buffer);
		}

		// Block End
		buffer.append('\n').append(prefix).append('}');
	}

	private static void formatInitializer(IValue initializer, String prefix, StringBuilder buffer)
	{
		final String initializerPrefix = Formatting.getIndent("property.initializer.indent", prefix);

		buffer.append('\n').append(initializerPrefix).append("init");

		if (Util.formatStatementList(initializerPrefix, buffer, initializer))
		{
			return;
		}

		// Separator
		if (Formatting.getBoolean("property.initializer.separator.space_before"))
		{
			buffer.append(' ');
		}
		buffer.append(':');
		if (Formatting.getBoolean("property.initializer.separator.newline_after"))
		{
			buffer.append('\n').append(initializerPrefix);
		}
		else if (Formatting.getBoolean("property.initializer.separator.space_after"))
		{
			buffer.append(' ');
		}

		initializer.toString(prefix, buffer);

		if (Formatting.getBoolean("property.initializer.semicolon"))
		{
			buffer.append(';');
		}
	}

	private static void formatGetter(IMethod getter, String prefix, StringBuilder buffer)
	{
		final String indent = Formatting.getIndent("property.getter.indent", prefix);

		final IValue value = getter.getValue();

		buffer.append('\n').append(indent);
		getter.getAttributes().toInlineString(indent, buffer);
		buffer.append("get");

		if (value != null)
		{
			if (Util.formatStatementList(indent, buffer, value))
			{
				return;
			}

			// Separator
			if (Formatting.getBoolean("property.getter.separator.space_before"))
			{
				buffer.append(' ');
			}
			buffer.append(':');
			if (Formatting.getBoolean("property.getter.separator.newline_after"))
			{
				buffer.append('\n').append(indent);
			}
			else if (Formatting.getBoolean("property.getter.separator.space_after"))
			{
				buffer.append(' ');
			}

			value.toString(indent, buffer);
		}

		if (Formatting.getBoolean("property.getter.semicolon"))
		{
			buffer.append(';');
		}
	}

	private static void formatSetter(IMethod setter, String prefix, StringBuilder buffer)
	{
		final String indent = Formatting.getIndent("property.setter.indent", prefix);

		final IValue value = setter.getValue();
		final Name setterParameterName = setter.getParameters().get(0).getName();

		buffer.append('\n').append(indent);
		setter.getAttributes().toInlineString(indent, buffer);
		buffer.append("set");

		if (setterParameterName != Names.newValue)
		{
			Formatting.appendSeparator(buffer, "property.setter.parameter.open_paren", '(');
			buffer.append(setterParameterName);
			Formatting.appendSeparator(buffer, "property.setter.parameter.close_paren", ')');
		}

		if (value != null)
		{
			if (Util.formatStatementList(indent, buffer, value))
			{
				return;
			}

			// Separator
			if (Formatting.getBoolean("property.setter.separator.space_before"))
			{
				buffer.append(' ');
			}
			buffer.append(':');
			if (Formatting.getBoolean("property.setter.separator.newline_after"))
			{
				buffer.append('\n').append(indent);
			}
			else if (Formatting.getBoolean("property.setter.separator.space_after"))
			{
				buffer.append(' ');
			}

			value.toString(indent, buffer);
		}

		if (Formatting.getBoolean("property.setter.semicolon"))
		{
			buffer.append(';');
		}
	}
}
