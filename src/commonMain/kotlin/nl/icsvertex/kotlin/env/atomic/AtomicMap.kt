package nl.icsvertex.kotlin.env.atomic

class AtomicMap<K, V>(
    value: Map<K, V>? = null
) : AtomicRef<Map<K, V>>(value ?: mapOf()) {
    fun get(key: K): V {
        return getValue()[key] ?: throw NoSuchElementException("Key '$key' not found in map!")
    }

    fun getOrDefault(key: K, defaultValue: V): V {
        return getValue()[key] ?: defaultValue
    }

    fun getOrNull(key: K): V? {
        return try {
            getValue()[key]
        } catch (e: Exception) {
            null
        }
    }

    fun put(key: K, value: V) {
        val currentValue = getValue()
        setValue(
            currentValue.plus(Pair(key, value))
        )
    }

    fun put(entry: Pair<K, V>) {
        val currentValue = getValue()
        setValue(
            currentValue.plus(entry)
        )
    }

    fun putAll(entries: Collection<Pair<K, V>>){
        val currentValue = getValue()
        setValue(
            currentValue.plus(entries)
        )
    }

    fun remove(key: K) {
        val currentValue = getValue()
        setValue(
            currentValue.filter { it.key != key }
        )
    }

    fun remove(entry: Map.Entry<K, V>) {
        val currentValue = getValue()
        setValue(
            currentValue.filter { it != entry }
        )
    }

    fun removeAll(keys: List<K>) {
        val currentValue = getValue()
        setValue(
            currentValue.filter { !keys.contains(it.key) }
        )
    }

//    fun removeAll(entries: List<Map.Entry<K, V>>) {
//        val currentValue = getValue()
//        setValue(
//            currentValue.filter { !entries.contains(it) }
//        )
//    }

    fun containsKey(key: K): Boolean {
        return getValue().containsKey(key)
    }

    fun clear() {
        setValue(mapOf())
    }

    val isEmpty: Boolean
        get() = getValue().isEmpty()
}