public Itemset {
  private int[] items;
  private float support;

  public Itemset(int[] items) {
    this.items = items;
    this.support = countSupport();
  }

}
