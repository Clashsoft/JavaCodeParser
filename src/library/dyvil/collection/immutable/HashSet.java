package dyvil.collection.immutable;

import dyvil.collection.Collection;
import dyvil.collection.ImmutableSet;
import dyvil.collection.MutableSet;
import dyvil.collection.Set;
import dyvil.collection.impl.AbstractHashSet;
import dyvil.lang.literal.ArrayConvertible;
import dyvil.util.ImmutableException;

import java.util.function.Function;
import java.util.function.Predicate;

import static dyvil.collection.impl.AbstractHashMap.DEFAULT_CAPACITY;

@ArrayConvertible
public class HashSet<E> extends AbstractHashSet<E> implements ImmutableSet<E>
{
	private static final long serialVersionUID = -1698577535888129119L;
	
	public static <E> HashSet<E> apply(E... elements)
	{
		return new HashSet(elements);
	}
	
	public static <E> Builder<E> builder()
	{
		return new Builder<E>();
	}
	
	public static <E> Builder<E> builder(int capacity)
	{
		return new Builder<E>(capacity);
	}
	
	public static class Builder<E> implements ImmutableSet.Builder<E>
	{
		private HashSet<E> set;
		
		public Builder()
		{
			this.set = new HashSet<E>(DEFAULT_CAPACITY);
		}
		
		public Builder(int capacity)
		{
			this.set = new HashSet<E>(capacity);
		}
		
		@Override
		public void add(E element)
		{
			if (this.set == null)
			{
				throw new IllegalStateException("Already built!");
			}
			
			this.set.addInternal(element);
		}
		
		@Override
		public ImmutableSet<E> build()
		{
			HashSet<E> set = this.set;
			this.set = null;
			return set;
		}
	}
	
	protected HashSet()
	{
		super(DEFAULT_CAPACITY);
	}
	
	protected HashSet(int capacity)
	{
		super(capacity);
	}
	
	public HashSet(Collection<E> collection)
	{
		super(collection);
	}
	
	public HashSet(Set<E> set)
	{
		super(set);
	}
	
	public HashSet(AbstractHashSet<E> set)
	{
		super(set);
	}
	
	public HashSet(E... elements)
	{
		super(elements);
	}
	
	@Override
	protected void addElement(int hash, E element, int index)
	{
		this.elements[index] = new HashElement(element, hash, this.elements[index]);
		this.size++;
	}
	
	@Override
	protected void removeElement(HashElement<E> element)
	{
		throw new ImmutableException("Iterator.remove() on Immutable Set");
	}
	
	@Override
	public ImmutableSet<E> $plus(E element)
	{
		HashSet<E> newSet = new HashSet<E>(this);
		newSet.ensureCapacityInternal(this.size + 1);
		newSet.addInternal(element);
		return newSet;
	}
	
	@Override
	public ImmutableSet<E> $minus(Object element)
	{
		HashSet<E> newSet = new HashSet<E>(this.size);
		
		for (E element1 : this)
		{
			if (element1 != element && (element == null || !element.equals(element1)))
			{
				newSet.addInternal(element1);
			}
		}
		return newSet;
	}
	
	@Override
	public ImmutableSet<? extends E> $minus$minus(Collection<?> collection)
	{
		HashSet<E> newSet = new HashSet<E>(this.size);
		
		for (E element1 : this)
		{
			if (!collection.contains(element1))
			{
				newSet.addInternal(element1);
			}
		}
		
		return newSet;
	}
	
	@Override
	public ImmutableSet<? extends E> $amp(Collection<? extends E> collection)
	{
		HashSet<E> newSet = new HashSet<E>(this.size);
		
		for (E element1 : this)
		{
			if (collection.contains(element1))
			{
				newSet.addInternal(element1);
			}
		}
		
		return newSet;
	}
	
	@Override
	public ImmutableSet<? extends E> $bar(Collection<? extends E> collection)
	{
		HashSet<E> newSet = new HashSet<E>(this);
		newSet.ensureCapacity(this.size + collection.size());
		for (E element : collection)
		{
			newSet.addInternal(element);
		}
		return newSet;
	}
	
	@Override
	public ImmutableSet<? extends E> $up(Collection<? extends E> collection)
	{
		HashSet<E> newSet = new HashSet<E>(this.size + collection.size());
		
		for (E element : this)
		{
			if (!collection.contains(element))
			{
				newSet.addInternal(element);
			}
		}
		
		for (E element : collection)
		{
			if (!this.contains(element))
			{
				newSet.addInternal(element);
			}
		}
		return newSet;
	}
	
	@Override
	public <R> ImmutableSet<R> mapped(Function<? super E, ? extends R> mapper)
	{
		HashSet<R> newSet = new HashSet<R>(this.size);
		
		for (E element : this)
		{
			newSet.addInternal(mapper.apply(element));
		}
		return newSet;
	}
	
	@Override
	public <R> ImmutableSet<R> flatMapped(Function<? super E, ? extends Iterable<? extends R>> mapper)
	{
		HashSet<R> newSet = new HashSet<R>(this.size << 2);
		
		for (E element : this)
		{
			for (R newElement : mapper.apply(element))
			{
				newSet.addInternal(newElement);
			}
		}
		newSet.flatten();
		return newSet;
	}
	
	@Override
	public ImmutableSet<E> filtered(Predicate<? super E> condition)
	{
		HashSet<E> newSet = new HashSet<E>(this.size);
		
		for (E element : this)
		{
			if (condition.test(element))
			{
				newSet.addInternal(element);
			}
		}
		return newSet;
	}
	
	@Override
	public ImmutableSet<E> copy()
	{
		return new HashSet<E>(this);
	}
	
	@Override
	public MutableSet<E> mutable()
	{
		return new dyvil.collection.mutable.HashSet<E>(this);
	}
	
	@Override
	public java.util.Set<E> toJava()
	{
		return java.util.Collections.unmodifiableSet(super.toJava());
	}
}
