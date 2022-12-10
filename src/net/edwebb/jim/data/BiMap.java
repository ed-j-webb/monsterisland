package net.edwebb.jim.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BiMap<K, V> implements Map<K, V> {

	private Map<K, V> a;
	private Map<V, K> b;
	private BiMap<V, K> inverse;
	
	public BiMap() {
		a = new HashMap<K, V>();
		b = new HashMap<V, K>();
		inverse = new BiMap<V, K>(this); 
	}
	
	private BiMap(BiMap<V, K> inverse) {
		this.a = inverse.b;
		this.b = inverse.a;
		this.inverse = inverse;
	}

	public BiMap<V, K> inverse() {
		return inverse;
	}
	
	@Override
	public void clear() {
		a.clear();
		b.clear();
	}

	@Override
	public boolean containsKey(Object key) {
		return a.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return b.containsKey(value);
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		return a.entrySet();
	}

	@Override
	public V get(Object key) {
		return a.get(key);
	}

	@Override
	public boolean isEmpty() {
		return a.isEmpty();
	}

	@Override
	public Set<K> keySet() {
		return a.keySet();
	}

	@Override
	public V put(K key, V value) {
		V oldVal = a.remove(key);
		b.remove(oldVal);
		a.put(key, value);
		b.put(value, key);
		return oldVal;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> map) {
		for (Entry<? extends K, ? extends V> entry : map.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public V remove(Object key) {
		V oldVal = a.remove(key);
		b.remove(oldVal);
		return oldVal;
	}

	@Override
	public int size() {
		return a.size();
	}

	@Override
	public Collection<V> values() {
		return a.values();
	}
}
