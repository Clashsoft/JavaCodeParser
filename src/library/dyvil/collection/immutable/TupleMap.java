package dyvil.collection.immutable;

import java.util.Iterator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

import dyvil.collection.ArrayIterator;
import dyvil.collection.mutable.MutableMap;
import dyvil.lang.Map;
import dyvil.tuple.Tuple2;

public class TupleMap<K, V> implements ImmutableMap<K, V>
{
	private final int				size;
	private final Tuple2<K, V>[]	entries;
	
	public TupleMap(Tuple2<K, V>[] entries)
	{
		this.size = entries.length;
		this.entries = entries;
	}
	
	public TupleMap(Tuple2<K, V>[] entries, int size)
	{
		this.size = size;
		this.entries = entries;
	}
	
	public TupleMap(Map<K, V> map)
	{
		this.size = map.size();
		this.entries = new Tuple2[this.size];
		
		int index = 0;
		for (Iterator<Map.Entry<K, V>> iterator = map.entryIterator(); iterator.hasNext();)
		{
			Map.Entry<K, V> entry = iterator.next();
			this.entries[index++] = new Tuple2(entry.getKey(), entry.getValue());
		}
	}
	
	@Override
	public int size()
	{
		return this.size;
	}
	
	@Override
	public boolean isEmpty()
	{
		return this.size == 0;
	}
	
	@Override
	public Iterator<Tuple2<K, V>> iterator()
	{
		return new ArrayIterator<>(this.entries, this.size);
	}
	
	@Override
	public Iterator<K> keyIterator()
	{
		return new Iterator<K>()
		{
			private int	index;
			
			@Override
			public boolean hasNext()
			{
				return this.index < TupleMap.this.size;
			}
			
			@Override
			public K next()
			{
				return TupleMap.this.entries[this.index++]._1;
			}
		};
	}
	
	@Override
	public Iterator<V> valueIterator()
	{
		return new Iterator<V>()
		{
			private int	index;
			
			@Override
			public boolean hasNext()
			{
				return this.index < TupleMap.this.size;
			}
			
			@Override
			public V next()
			{
				return TupleMap.this.entries[this.index++]._2;
			}
		};
	}
	
	@Override
	public Iterator<Entry<K, V>> entryIterator()
	{
		return new ArrayIterator(this.entries, this.size);
	}
	
	@Override
	public void forEach(Consumer<? super Tuple2<K, V>> action)
	{
		for (int i = 0; i < this.size; i++)
		{
			action.accept(this.entries[i]);
		}
	}
	
	@Override
	public void forEach(BiConsumer<? super K, ? super V> action)
	{
		for (int i = 0; i < this.size; i++)
		{
			Tuple2<K, V> entry = this.entries[i];
			action.accept(entry._1, entry._2);
		}
	}
	
	@Override
	public boolean $qmark(Object key)
	{
		for (int i = 0; i < this.size; i++)
		{
			Tuple2<K, V> entry = this.entries[i];
			if (key == entry._1 || key != null && key.equals(entry._1))
			{
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean $qmark(Object key, Object value)
	{
		for (int i = 0; i < this.size; i++)
		{
			Tuple2<K, V> entry = this.entries[i];
			if (key == entry._1 || key != null && key.equals(entry._1))
			{
				if (value == entry._2 || value != null && value.equals(entry._2))
				{
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public boolean $qmark$colon(V value)
	{
		for (int i = 0; i < this.size; i++)
		{
			Tuple2<K, V> entry = this.entries[i];
			if (value == entry._2 || value != null && value.equals(entry._2))
			{
				return true;
			}
		}
		return false;
	}
	
	@Override
	public V apply(K key)
	{
		for (int i = 0; i < this.size; i++)
		{
			Tuple2<K, V> entry = this.entries[i];
			if (key == entry._1 || key != null && key.equals(entry._1))
			{
				return entry._2;
			}
		}
		return null;
	}
	
	@Override
	public ImmutableMap<K, V> $plus(K key, V value)
	{
		return null;
	}
	
	@Override
	public ImmutableMap<K, V> $plus$plus(Map<? extends K, ? extends V> map)
	{
		return null;
	}
	
	@Override
	public ImmutableMap<K, V> $minus(K key)
	{
		return null;
	}
	
	@Override
	public ImmutableMap<K, V> $minus(K key, V value)
	{
		return null;
	}
	
	@Override
	public ImmutableMap<K, V> $minus$colon(V value)
	{
		return null;
	}
	
	@Override
	public ImmutableMap<K, V> $minus$minus(Map<? extends K, ? extends V> map)
	{
		return null;
	}
	
	@Override
	public <U> ImmutableMap<K, U> mapped(BiFunction<? super K, ? super V, ? extends U> mapper)
	{
		return null;
	}
	
	@Override
	public ImmutableMap<K, V> filtered(BiPredicate<? super K, ? super V> condition)
	{
		return null;
	}
	
	@Override
	public ImmutableMap<K, V> copy()
	{
		return null;
	}
	
	@Override
	public MutableMap<K, V> mutable()
	{
		return null;
	}
	
	@Override
	public String toString()
	{
		if (this.size <= 0)
		{
			return "[]";
		}
		
		StringBuilder builder = new StringBuilder("[ ");
		builder.append(this.entries[0]);
		for (int i = 1; i < this.size; i++)
		{
			builder.append(", ");
			builder.append(this.entries[i]);
		}
		return builder.append(" ]").toString();
	}
}