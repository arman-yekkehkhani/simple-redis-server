import org.example.cache.DoublyLinkedList;
import org.junit.jupiter.api.Test;

public class TestDoublyLinkedList {

    @Test
    public void addTest() {
        DoublyLinkedList<String> linkedList = new DoublyLinkedList<>();
        for (int i = 0; i < 10; i++) {
            linkedList.add(String.valueOf(i));
        }

        for (String s : linkedList) {
            System.out.println(s);
        }
    }

    @Test
    public void removeTest() {
        DoublyLinkedList<String> linkedList = new DoublyLinkedList<>();
        String string = "test1";
        String string2 = "test2";
        String string3 = "test3";

        DoublyLinkedList<String>.Item item = linkedList.add(string);
        DoublyLinkedList<String>.Item item2 = linkedList.add(string2);
        DoublyLinkedList<String>.Item item3 = linkedList.add(string3);

        linkedList.remove(item2);

        for (String s : linkedList) {
            System.out.println(s);
        }
    }

    @Test
    public void removeAllFromTailTest() {
        DoublyLinkedList<String> linkedList = new DoublyLinkedList<>();
        for (int i = 0; i < 10; i++) {
            linkedList.add(String.valueOf(i));
        }

        for (int i = 0; i < 10; i++) {
            System.out.println(linkedList.removeLast());
        }

        assert linkedList.isEmpty();
    }

    @Test
    public void memoryTest() {
        DoublyLinkedList<String> linkedList = new DoublyLinkedList<>();
        for (int i = 0; i < 10; i++) {
            linkedList.add(String.valueOf(i));
        }

        for (int i = 0; i < 1000_000_000; i++) {
            linkedList.add(String.valueOf(i));
            linkedList.removeLast();
        }
    }


}
