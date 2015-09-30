# vickrey-demo
Requires Java >= 8 
(Scala SDK used: 2.11.7)

Both versions (Java & Scala) output should be like this:

* => Test: testVickreyAuction
* Auction buyer = E
* Auction buyer bid price = 140
* Auction final price = 130
* => testVickreyAuction OK
* => Test: testVickreyAuctionLimits
* -> Need more bids Exception check OK
* -> Need more buyers Exception check OK
* => testVickreyAuctionLimits OK

Both versions are in a single class file and don't require additional librairies, so you just have to compile & run either VickreyDemo.java or VickreyDemo.scala

From the project root:
JAVA
* javac org/devpo/VickreyDemo.java
* java org/devpo/VickreyDemo

SCALA
* scalac org/devpo/VickreyDemo.java
* scala VickreyDemo
