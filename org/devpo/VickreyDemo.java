package org.devpo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This is a simple implementation for Vickrey auction algorithm.
 * This is very simple, so it does not rely on junit, or build tool or use different class files.
 * Just need to compile & run this unique class.
 */
public class VickreyDemo {

  /**
   * Model class, the buyer is the user who place bids on an auction.
   * It's just identified by a name in this example
   */
  class Buyer {
    // Buyer Name
    String name;

    public Buyer(String name) {
      this.name = name;
    }

    /**
     * The buyer name (eg: 'Paul')
     *
     * @return
     */
    public String getName() {
      return name;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Buyer buyer = (Buyer) o;

      return !(name != null ? !name.equals(buyer.name) : buyer.name != null);

    }

    @Override
    public int hashCode() {
      return name != null ? name.hashCode() : 0;
    }

    @Override
    public String toString() {
      return name;
    }
  }


  /**
   * This is an auction object it's simply defined by
   * - a reserve price under which the system shall not validate a sale
   * - an attached list of "Bids". A same buyer can place multiple bids on an object
   */
  class AuctionObject {
    // Do not sell the object below this price
    int reservePrice;

    // Assume the first bids are the earlier ones
    List<Bid> bids = new ArrayList<>();

    public AuctionObject(int reservePrice) {
      this.reservePrice = reservePrice;
    }

    /**
     * Add one or more bids on the object for the given buyer
     * @param buyer the one attached to the bid(s)
     * @param bidPrices one price for each bid you want to place
     */
    public void addNewBids(Buyer buyer, int... bidPrices) {
      for (int bidPrice : bidPrices) {
        bids.add(new Bid(buyer, bidPrice));
      }
    }

    public int getReservePrice() {
      return reservePrice;
    }

    public List<Bid> getBids() {
      return bids;
    }
  }

  /**
   * Simple model for a Bid, describe by
   * - a Buyer (the one who places the bid)
   * - a bid price
   */
  class Bid {
    Buyer buyer;
    int price;

    public Bid(Buyer buyer, int price) {
      this.buyer = buyer;
      this.price = price;
    }

    public Buyer getBuyer() {
      return buyer;
    }

    public int getPrice() {
      return price;
    }
  }

  /**
   * This is the model of an auction result that stores winner & price for the given object
   */
  class AuctionResult {
    AuctionObject object;
    Bid winner;
    int price;

    public AuctionResult(AuctionObject object, Bid winner, int price) {
      this.object = object;
      this.winner = winner;
      this.price = price;
    }
  }

  /**
   * Describe methods that needs to be implemented buy any
   * auction algorithm, to be integrated.
   */
  interface AuctionStrategy {

    /**
     * Return either an AuctionResult if SellObject context allow to decide an
     * Auction "Winner" at a defined price. Returns null if the algorithm can not decide
     * a winner (for instance if no bid or if algorithm rules do not match)
     *
     * @param object the sale object
     * @return AuctionResult if applicable or null in other cases
     */
    AuctionResult computeWinner(AuctionObject object) throws AuctionException;
  }

  /**
   * Convenient exception class for this test
   */
  class AuctionException extends RuntimeException {
    public AuctionException(String message) {
      super(message);
    }
  }

  /**
   * The implementation is based on "Vickrey Auction" type of sealed-bid auction.
   *
   * @see <a href="https://en.wikipedia.org/wiki/Vickrey_auction">Wickrey Auction</a>
   * <p>
   * <p>
   * Teads doc seems "buggy"
   */
  class VickreyAuctionStrategy implements AuctionStrategy {

    /**
     * Vickrey algorithm needs at least 2 bids from 2 different buyers to process AuctionResult.
     * AuctionResult winner buyer will be the buyer who placed the highest bid (strictly over reserve price).
     * AuctionResult price will be the 2nd highest bid from another buyer (strictly above the reserve price).
     * <p>
     * If this 2 rules can not be matched, algorithm will throw an AuctionException with details.
     *
     * @param object the sale object
     * @throws AuctionException if rules on AuctionObject can not be matched
     * @return AuctionResult the result for given auction sale object
     */
    @Override
    public AuctionResult computeWinner(AuctionObject object) throws AuctionException {
      // Filter bids below reserve price
      List<Bid> effectiveBids = object.bids.stream()
          .filter((b) -> b.price > object.reservePrice)
          .collect(Collectors.toList());

      if(effectiveBids.size()<2) throw new AuctionException("Need more bids above reserve !");

      // check if at least 2 buyers
      long differentBuyers = effectiveBids.stream()
          .map(Bid::getBuyer)
          .distinct()
          .count();

      if (differentBuyers < 2) throw new AuctionException("Need more buyers !");

      // Get the highest big == winner
      Bid highestBid = effectiveBids.stream()
          .max((b1, b2) -> Integer.compare(b1.getPrice(), b2.getPrice()))
          .get();
      Buyer highestBuyer = highestBid.getBuyer();

      // Find the second one excluding the winner
      Bid secondHighestBid = effectiveBids.stream()
          .filter((b) -> !b.getBuyer().equals(highestBuyer))
          .max((b1, b2) -> Integer.compare(b1.getPrice(), b2.getPrice()))
          .get();

      return new AuctionResult(object, highestBid, secondHighestBid.getPrice());
    }
  }


  /**
   * This is the unit test to validate Vickrey algorithm.
   * Expected AuctionResult is "E" buyer with a 130
   */
  public void testVickreyAuction(AuctionStrategy vickreyStrategy) {
    AuctionObject object = new AuctionObject(100);
    Buyer aBuyer = new Buyer("A");
    Buyer cBuyer = new Buyer("C");
    Buyer dBuyer = new Buyer("D");
    Buyer eBuyer = new Buyer("E");

    object.addNewBids(aBuyer, 110, 130);
    object.addNewBids(cBuyer, 125);
    object.addNewBids(dBuyer, 105, 115, 90);
    object.addNewBids(eBuyer, 132, 135, 140);

    AuctionResult result = vickreyStrategy.computeWinner(object);

    assert result.price == 130;
    assert result.winner.buyer.equals(eBuyer);

    System.out.println(String.format("Auction buyer = %s", result.winner.buyer));
    System.out.println(String.format("Auction buyer bid price = %d", result.winner.price));
    System.out.println(String.format("Auction price = %d", result.price));
    System.out.println("testVickreyAuction OK");
  }

  /**
   * Test the algorithm against limit cases:
   * - not enough bids
   * - not enough buyers
   */
  public void testVickreyAuctionLimits(AuctionStrategy vickreyStrategy){
    AuctionObject object = new AuctionObject(100);
    Buyer aBuyer = new Buyer("A");
    Buyer bBuyer = new Buyer("B");
    object.addNewBids(aBuyer, 12);

    try {
      vickreyStrategy.computeWinner(object);
    } catch (AuctionException e) {
      assert e.getMessage().equals("Need more bids above reserve !");
    }

    object.addNewBids(aBuyer, 120, 154);

    try {
      vickreyStrategy.computeWinner(object);
    } catch (AuctionException e) {
      assert e.getMessage().equals("Need more buyers !");
    }
    object.addNewBids(bBuyer, 300, 110);

    AuctionResult result = vickreyStrategy.computeWinner(object);

    assert result.winner.price == 300;
    assert result.winner.buyer.equals(bBuyer);
    assert result.price == 154;
    System.out.println("testVickreyAuctionLimits OK");
  }


  /**
   * Run all the tests
   */
  public void runTestSuite(){
    AuctionStrategy strategy = new VickreyAuctionStrategy();
    testVickreyAuction(strategy);
    testVickreyAuctionLimits(strategy);
  }

  public static void main(String[] args) throws IOException {
    VickreyDemo m = new VickreyDemo();
    m.runTestSuite();
  }
}
