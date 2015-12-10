package dyvil.collection;

import dyvil.lang.literal.ArrayConvertible;
import dyvil.lang.literal.NilConvertible;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;

@NilConvertible
@ArrayConvertible
public interface MutableCollection<E> extends Collection<E>
{
	static <E> MutableCollection<E> apply()
	{
		return MutableList.apply();
	}
	
	@SafeVarargs
	static <E> MutableCollection<E> apply(E... elements)
	{
		return MutableList.apply(elements);
	}
	
	// Accessors
	
	@Override
	default boolean isImmutable()
	{
		return false;
	}
	
	@Override
	int size();
	
	@Override
	Iterator<E> iterator();
	
	// Non-mutating Operations
	
	@Override
	MutableCollection<E> $plus(E element);
	
	@Override
	MutableCollection<? extends E> $plus$plus(Collection<? extends E> collection);
	
	@Override
	MutableCollection<E> $minus(Object element);
	
	@Override
	MutableCollection<? extends E> $minus$minus(Collection<?> collection);
	
	@Override
	MutableCollection<? extends E> $amp(Collection<? extends E> collection);
	
	@Override
	<R> MutableCollection<R> mapped(Function<? super E, ? extends R> mapper);
	
	@Override
	<R> MutableCollection<R> flatMapped(Function<? super E, ? extends Iterable<? extends R>> mapper);
	
	@Override
	MutableCollection<E> filtered(Predicate<? super E> condition);
	
	// Mutating Operations
	
	@Override
	void clear();
	
	@Override
	boolean add(E element);
	
	@Override
	boolean remove(Object element);
	
	@Override
	void map(Function<? super E, ? extends E> mapper);
	
	@Override
	void flatMap(Function<? super E, ? extends Iterable<? extends E>> mapper);
	
	// Copying
	
	@Override
	MutableCollection<E> copy();
	
	@Override
	default MutableCollection<E> mutable()
	{
		return this;
	}
	
	@Override
	default MutableCollection<E> mutableCopy()
	{
		return this.copy();
	}
	
	<R> MutableCollection<R> emptyCopy();
	
	@Override
	ImmutableCollection<E> immutable();
	
	@Override
	default ImmutableCollection<E> immutableCopy()
	{
		return this.immutable();
	}
	
	@Override
	ImmutableCollection<E> view();
}
