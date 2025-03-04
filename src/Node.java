public class Node<E> {
    Node<E> next;
    E data;
    Node<E> previous;

    public Node(Node<E> previous, E data, Node<E> next) {
        this.data = data;
        this.next = next;
        this.previous = previous;
    }
}