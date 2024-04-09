package cofix.common.util;

import java.util.HashMap;

public class DuoMap<K, V> {
  private final HashMap<K, V> kv = new HashMap<>();
  private final HashMap<V, K> vk = new HashMap<>();

  public void put(K key, V value) {
    if (kv.putIfAbsent(key, value) != null) {
      throw new IllegalArgumentException("duplicate key");
    }
    if (vk.putIfAbsent(value, key) != null) {
      throw new IllegalArgumentException("duplicate value");
    }
  }

  public void remove(K key,V value) {
    kv.remove(key, value);
    vk.remove(value,key);
  }

  public boolean containsKey(K key) {
    return kv.containsKey(key);
  }

  public boolean containsValue(V value) {
    return vk.containsKey(value);
  }

  public V getValue(K key) {
    return kv.get(key);
  }

  public K getKey(V value) {
    return vk.get(value);
  }

  public HashMap<K, V> getKv() {
    return kv;
  }

  public HashMap<V, K> getVk() {
    return vk;
  }

  public void clear(){
    kv.clear();
    vk.clear();
  }

}
