package double_linked_list;

public class DoubleLinkedList {

  private ListItem head;
  private ListItem tail;

  public ListItem getHeadElement() {
    return head;
  }

  public ListItem getTailElement() {
    return tail;
  }

  public ListItem popHeadElement() {
    ListItem itemToPop = head;
    if (head != null) {
      head = head.getNext();
      head.setPrev(null);
    }
    return itemToPop;
  }

  public ListItem popTailElement() {
    ListItem itemToPop = tail;
    if (tail != null) {
      tail = tail.getPrev();
      tail.setNext(null);
    }
    return itemToPop;
  }

  public void removeHeadElement() {
    if (head != null) {
      head = head.getNext();
      head.setPrev(null);
    }
  }

  public void removeTailElement() {
    if (tail != null) {
      tail = tail.getPrev();
      tail.setNext(null);
    }
  }

  public void addToHead(ListItem item) {
    if (head != null) {
      head.setPrev(item);
    }
    item.setNext(head);
    head = item;
  }

  public void addToTail(ListItem item) {
    if (tail != null) {
      tail.setNext(item);
    }
    item.setNext(tail);
    tail = item;
  }
}