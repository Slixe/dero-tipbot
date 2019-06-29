package fr.slixe.dero4j.util;

import java.util.LinkedHashMap;

public class MapBuilder<K, V>
{
	private final LinkedHashMap<K, V> linkedHashMap = new LinkedHashMap<>();

	public MapBuilder() {}

	public MapBuilder<K, V> put(K key, V value)
	{
		this.linkedHashMap.put(key, value);
		return this;
	}

	public LinkedHashMap<K, V> get()
	{
		return this.linkedHashMap;
	}
}
