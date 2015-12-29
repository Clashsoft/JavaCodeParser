package dyvil.tools.compiler.ast.classes;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.util.MarkerMessages;
import dyvil.tools.parsing.marker.MarkerList;

public class TraitMetadata implements IClassMetadata
{
	private final IClass theClass;

	public TraitMetadata(IClass theClass)
	{
		this.theClass = theClass;
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		final IClassBody classBody = this.theClass.getBody();
		if (classBody == null)
		{
			return;
		}

		for (int i = 0, count = classBody.fieldCount(); i < count; i++)
		{
			final IField field = classBody.getField(i);
			if (!field.isField())
			{
				// Skip properties
				continue;
			}

			final ModifierSet modifierSet = field.getModifiers();
			if (!modifierSet.hasIntModifier(Modifiers.PUBLIC))
			{
				modifierSet.addIntModifier(Modifiers.PUBLIC);
			}
			if (!modifierSet.hasIntModifier(Modifiers.STATIC) || !modifierSet.hasIntModifier(Modifiers.FINAL))
			{
				modifierSet.addIntModifier(Modifiers.STATIC);
				modifierSet.addIntModifier(Modifiers.FINAL);
				markers.add(MarkerMessages.createMarker(field.getPosition(), "field.trait.warning", field.getName()));
			}
		}

		for (int i = 0, count = classBody.methodCount(); i < count; i++)
		{
			final IMethod method = classBody.getMethod(i);
			if (!method.hasModifier(Modifiers.PUBLIC))
			{
				method.getModifiers().addIntModifier(Modifiers.PUBLIC);
			}
		}
	}
	
	@Override
	public void resolveTypesBody(MarkerList markers, IContext context)
	{
	}
	
	@Override
	public void resolve(MarkerList markers, IContext context)
	{
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
	}
	
	@Override
	public void write(ClassWriter writer, IValue instanceFields) throws BytecodeException
	{
	}
}
