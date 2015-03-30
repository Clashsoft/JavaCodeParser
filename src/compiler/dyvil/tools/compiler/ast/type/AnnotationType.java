package dyvil.tools.compiler.ast.type;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.util.Set;
import java.util.TreeSet;

import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.constant.EnumValue;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.parameter.MethodParameter;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.ast.value.IValueList;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class AnnotationType extends Type
{
	public static final MethodParameter	VALUE	= new MethodParameter();
	
	static
	{
		VALUE.name = Name.getQualified("value");
	}
	
	public RetentionPolicy				retention;
	public Set<ElementType>				targets;
	
	public AnnotationType()
	{
	}
	
	public AnnotationType(String packageName)
	{
		int index = packageName.lastIndexOf('.');
		this.fullName = packageName;
		this.name = Name.getQualified(packageName.substring(index + 1));
	}
	
	public AnnotationType(ICodePosition position, Name name)
	{
		super(position, name);
	}
	
	public AnnotationType(IClass iclass)
	{
		super(iclass);
	}
	
	@Override
	public AnnotationType resolve(MarkerList markers, IContext context)
	{
		if (this.theClass == null)
		{
			IClass iclass = context.resolveClass(this.name);
			
			if (iclass != null)
			{
				this.theClass = iclass;
				this.fullName = iclass.getFullName();
			}
			else if (markers != null)
			{
				markers.add(this.position, "resolve.type", this.toString());
			}
		}
		return this;
	}
	
	public void readMetaAnnotations()
	{
		if (this.theClass == null)
		{
			return;
		}
		if (this.retention == null)
		{
			Annotation retention = this.theClass.getAnnotation(Types.ARetention);
			if (retention != null)
			{
				EnumValue value = (EnumValue) retention.arguments.getValue(0, null);
				this.retention = RetentionPolicy.valueOf(value.name.qualified);
			}
		}
		if (this.targets != null)
		{
			return;
		}
		this.targets = new TreeSet();
		
		Annotation target = this.theClass.getAnnotation(Types.ATarget);
		if (target == null)
		{
			return;
		}
		
		IValueList values = (IValueList) target.arguments.getValue(0, VALUE);
		if (values == null)
		{
			return;
		}
		
		for (IValue v : values)
		{
			EnumValue value = (EnumValue) v;
			this.targets.add(ElementType.valueOf(value.name.qualified));
		}
	}
	
	public Set<ElementType> getTargets()
	{
		return this.targets;
	}
	
	public boolean isTarget(ElementType target)
	{
		if (this.targets == null || this.targets.isEmpty())
		{
			return true;
		}
		return this.targets.contains(target);
	}
}
