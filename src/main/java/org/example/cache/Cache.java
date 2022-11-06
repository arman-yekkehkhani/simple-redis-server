package org.example.cache;

import java.util.*;

public class Cache<K, V> {

    private final DoublyLinkedList<K> keyList = new DoublyLinkedList<>();
    private final Map<K, Node> map = new HashMap<>();

    private final int capacity;

    public Cache(int capacity) {
        this.capacity = capacity;
    }

    public synchronized V put(K key, V value) {
        Node oldNode = search(key);
        if (oldNode != null) {
            delete(oldNode.key);
        }
        insert(key, value);
        return oldNode == null ? null : oldNode.value;
    }

    public synchronized V get(K key) {
        Node node = search(key);
        return node == null ? null : node.value;
    }

    public synchronized void remove(K key) {
        delete(key);
    }

    public synchronized boolean containsKey(K key) {
        return map.containsKey(key);
    }

    public Iterable<K> keys() {
        return keyList;
    }

    class Node {
        K key;
        V value;
        DoublyLinkedList<K>.Item linkedListRef;

        public Node(K key, V value, DoublyLinkedList<K>.Item linkedListRef) {
            this.key = key;
            this.value = value;
            this.linkedListRef = linkedListRef;
        }
    }

    private Node insert(K key, V value) {
        Node node = new Node(key, value, keyList.add(key));
        map.put(key, node);
        if (map.size() > capacity) {
            K last = keyList.removeLast();
            map.remove(last);
        }
        return node;
    }

    private void delete(K key) {
        Node node = map.remove(key);
        if (node != null)
            keyList.remove(node.linkedListRef);
    }

    private Node search(K key) {
        Node node = map.get(key);
        if (node == null)
            return null;

        delete(key);
        return insert(key, node.value);
    }
}
