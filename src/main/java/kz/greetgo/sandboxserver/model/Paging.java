package kz.greetgo.sandboxserver.model;

public class Paging {

  public int offset;

  public int limit;

  public static Paging of(int offset, int limit) {
    Paging paging = new Paging();

    paging.offset = offset;
    paging.limit = limit;

    return paging;
  }

  public static Paging defaultPaging() {
    return of(0, 10);
  }

}
