package dyvil.function;

@FunctionalInterface
public interface Function1<P1, R>
{
	public R apply(P1 par1);
}