package dyvil.tools.compiler.ast.value;

public interface INumericValue extends IConstantValue
{
	public int intValue();
	
	public long longValue();
	
	public float floatValue();
	
	public double doubleValue();
}
