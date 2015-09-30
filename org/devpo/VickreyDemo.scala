import scala.collection.mutable

/**
 * This is a simple implementation for Vickrey auction algorithm.
 * This is very simple, so it does not rely on junit, or build tool or use different class files.
 * Just need to compile & run this unique class.
 *
 * @author patriceo
 */
object VickreyDemo {

  /**
   * Model class, the buyer is the user who place bids on an auction.
   * It's just identified by a name in this example
   */
  class Buyer(buyerName: String) {
    var name = buyerName

    override def toString(): String = name
  }

  /**
   * This is an auction object it's simply defined by
   * - a reserve price under which the system shall not validate a sale
   * - an attached list of "Bids". A same buyer can place multiple bids on an object
   */
  class AuctionObject(auctionReservePrice:Int) {
    var reservePrice = auctionReservePrice
    var bids:mutable.MutableList[Bid] = new mutable.MutableList[Bid]

    def addNewBids(buyer:Buyer, bidPrices:Int*): Unit = {
      for(bidPrice <- bidPrices) bids += (new Bid(buyer, bidPrice))
    }
  }

  /**
   * Simple model for a Bid, describe by
   * - a Buyer (the one who places the bid)
   * - a bid price
   */
  class Bid(bidBuyer:Buyer, bidPrice: Int) {
    var buyer = bidBuyer
    var price = bidPrice
  }

  /**
   * This is the model of an auction result that stores winner & price for the given object
   */
  class AuctionResult(resultObjet:AuctionObject, resultWinner:Bid, resultPrice:Int) {
    var auctionObject = resultObjet
    var winner = resultWinner
    var price = resultPrice
  }

  /**
   * Convenient exception class for this test
   */
  case class AuctionException(message:String=null) extends RuntimeException(message)

  /**
   * Describe methods that needs to be implemented buy any
   * auction algorithm, to be integrated.
   */
  trait AuctionStrategy {
    @throws[AuctionException]("if rules can not be matched")
    def determineWinner(auctionObject: AuctionObject): AuctionResult
  }

  /**
   * The implementation is based on "Vickrey Auction" type of sealed-bid auction.
   *
   * @see <a href="https://en.wikipedia.org/wiki/Vickrey_auction">Wickrey Auction</a>
   */
  class VickreyAuctionStrategy extends AuctionStrategy {

    override def determineWinner(auctionObject: AuctionObject): AuctionResult = {
      // Filter bids below reserve price
      val effectiveBids:List[Bid] = auctionObject.bids.toList.filter((b:Bid) => b.price > auctionObject.reservePrice )

      if(effectiveBids.size < 2) throw new AuctionException("Need more bids above reserve !")

      // check if at least 2 buyers (maybe need to tweak equals so that distinct will handle comparison properly)
      val differentBuyers = effectiveBids.map(b => b.buyer).distinct.size

      if(differentBuyers < 2) throw new AuctionException("Need more buyers !")

      val highestBid:Bid = effectiveBids.maxBy((b:Bid)=> b.price)
      val secondHighestBid:Bid = effectiveBids.filter((b) => !b.buyer.equals(highestBid.buyer))
                                              .maxBy((b) => b.price)

      return new AuctionResult(auctionObject, highestBid, secondHighestBid.price)
    }
  }

  /**
   * This is the unit test to validate Vickrey algorithm.
   * Expected AuctionResult is "E" buyer with a 130
   */
  def testVickreyAuction(vickreyStrategy: AuctionStrategy) = {
    println("=> Test: testVickreyAuction")

    val auctionObject:AuctionObject = new AuctionObject(100)
    val aBuyer = new Buyer("A")
    val cBuyer = new Buyer("C")
    val dBuyer = new Buyer("D")
    val eBuyer = new Buyer("E")
    
    auctionObject.addNewBids(aBuyer, 110, 130)
    auctionObject.addNewBids(cBuyer, 125)
    auctionObject.addNewBids(dBuyer, 105, 115, 90)
    auctionObject.addNewBids(eBuyer, 132, 135, 140)
    
    val result:AuctionResult = vickreyStrategy.determineWinner(auctionObject)

    println("Auction buyer = %s".format(result.winner.buyer))
    println("Auction buyer bid price = %d".format(result.winner.price))
    println("Auction final price = %d".format(result.price))
    println("=> testVickreyAuction OK")
  }

  /**
   * Test the algorithm against limit cases:
   * - not enough bids
   * - not enough buyers
   */
   def testVickreyAuctionLimits(vickreyStrategy:AuctionStrategy) = {
    println("=> Test: testVickreyAuctionLimits")
    val auctionObject:AuctionObject = new AuctionObject(100)
    val aBuyer = new Buyer("A")
    val bBuyer = new Buyer("B")
    auctionObject.addNewBids(aBuyer, 12)

    try {
      vickreyStrategy.determineWinner(auctionObject)
    } catch {
      case e:AuctionException => {
        assert("Need more bids above reserve !".equals(e.getMessage()))
        println("-> Need more bids Exception check OK")
      }
    }
    auctionObject.addNewBids(aBuyer, 120, 154)
    try {
      vickreyStrategy.determineWinner(auctionObject)
    } catch {
      case e:AuctionException => {
        assert("Need more buyers !".equals(e.message))
        println("-> Need more buyers Exception check OK")
      }
    }
    auctionObject.addNewBids(bBuyer, 300, 110)

    val result:AuctionResult = vickreyStrategy.determineWinner(auctionObject)

    assert(result.winner.price == 300)
    assert(result.winner.buyer.equals(bBuyer))
    assert(result.price == 154)

   println("=> testVickreyAuctionLimits OK")
  }

  /**
   * Run all the tests
   */
  def main(args: Array[String]): Unit = {
    val vickreyStrategy = new VickreyAuctionStrategy
    testVickreyAuction(vickreyStrategy)
    testVickreyAuctionLimits(vickreyStrategy)
  }
}