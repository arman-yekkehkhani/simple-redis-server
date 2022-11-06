package org.example.cache;

import java.util.Iterator;

public class DoublyLinkedList<T> implements Iterable<T> {

    public class Item {
        T data;
        Item next;
        Item previous;

        public Item(T data, Item next, Item previous) {
            this.data = data;
            this.next = next;
            this.previous = previous;
        }
    }

    Item head;
    Item tail;

    public Item add(T data) {
//        if list empty
        if (head == null) {
            Item item = new Item(data, null, null);
            head = item;
            tail = item;
            return item;
        }

        Item item = new Item(data, head, null);
        head.previous = item;
        head = item;

        return item;
    }

    public void remove(Item item) {
        if (isEmpty())
            return;

        unlink(item);
    }

    public T removeLast() {
        if (isEmpty())
            return null;

        Item removed = unlink(tail);
        return removed.data;
    }

    public boolean isEmpty() {
        return head == null;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            Item next = null;

            @Override
            public boolean hasNext() {
                if (isEmpty())
                    return false;
                if (next == null)
                    next = head;
                else
                    next = next.next;
                return next != null;
            }

            @Override
            public T next() {
                return next.data;
            }
        };
    }

    private Item unlink(Item item) {
        //        if head
        if (item.previous == null)
            head = item.next;
        else
            item.previous.next = item.next;

//        if tail
        if (item.next == null)
            tail = item.previous;
        else
            item.next.previous = item.previous;
        return item;
    }

}
