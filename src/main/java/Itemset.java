public class Itemset {
  private int[] items;
  private double support;

  public Itemset(int[] items) {
    this.items = items;
    this.support = RG.countSupport(items);
  }

  public double getSupport() {
    return support;
  }

  public int[] getItems() {
    return items;
  }
}
