package single_linked_list;

public class ListItem {

  private final String data;
  private ListItem next;

  public ListItem(String data) {
    this.data = data;
  }

  public String getData() {
    return data;
  }

  public ListItem getNext() {
    return next;
  }

  public void setNext(ListItem item) {
    next = item;
  }
}