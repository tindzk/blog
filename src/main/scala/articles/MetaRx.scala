package articles

import pl.metastack.metadocs.SectionSupport

object MetaRx extends SectionSupport {
  section("example") {
    import pl.metastack.metarx._

    val x = Var(1)

    x.map(_ + 1)
     .filter(_ > 5)
     .attach(println)

    (0 to 10).foreach(x := _)
  }

  import pl.metastack.metarx._

  section("arithmetic") {
    val x = Var(1.0)
    val xOffset = 0.1
    val vx = x + xOffset
    vx.attach(println)
    x := 2.0
  }

  section("sub") {
    val x = Var(42)

    val sub = Sub(23)
    sub.attach(println)

    sub := x  // `sub` will subscribe all values produced on `x`
    x := 200  // Gets propagated to `sub`

    sub := 10 // Cancel subscription and set value to 10
    x := 404  // Doesn't get propagated to `sub`
  }

  section("pickling") {
    import pl.metastack.metarx.Upickle._
    import upickle.default._

    val buffer = Buffer(1, 2, 3)

    val json = write(buffer)
    println(json)

    val decoded = read[Buffer[Int]](json)
    println(decoded)
  }
}
