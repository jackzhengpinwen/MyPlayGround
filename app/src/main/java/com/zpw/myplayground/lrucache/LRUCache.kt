package com.zpw.myplayground.lrucache

class LRUCache(private val capacity: Int) {
    private val map = hashMapOf<Int, Node>()
    private val head: Node = Node(0, 0)
    private val tail: Node = Node(0, 0)

    init {
        head.next = tail
        tail.next = head
    }

    fun get(key: Int): Int {
        if(map.containsKey(key)) {
            val node = map[key]
            node?.let { it ->
                remove(it)
                addAtEnd(it)
                return it.value
            }
        }
        return -1
    }

    fun put(key: Int, value: Int) {
        if (map.containsKey(key)) {
            map[key]?.let {
                remove(it)
            }
        }
        val node = Node(key, value)
        addAtEnd(node)
        map[key] = node
        if (map.size > capacity) {
            val first = head.next
            first?.let {
                remove(it)
                map.remove(first.key)
            }
        }
    }

    private fun addAtEnd(node: Node) {
        val prev = tail.prev
        prev?.next = node
        node.prev = prev
        node.next = tail
        tail.prev = node
    }

    private fun remove(node: Node) {
        val next = node.next
        val prev = node.prev
        prev?.next = next
        next?.prev = prev
    }


    data class Node(val key: Int, val value: Int) {
        var next: Node? = null
        var prev: Node? = null
    }
}