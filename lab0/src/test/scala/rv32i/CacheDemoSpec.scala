package rv32i

import chisel3._
import chiseltest._                      // brings WriteVcdAnnotation into scope
import org.scalatest.flatspec.AnyFlatSpec

/** Run with:  sbt "testOnly demo.CacheDemoSpec"
  * Waveforms: test_run_dir/<test name>/<Module>.vcd   (open with: gtkwave <file>.vcd)
  */
class CacheDemoSpec extends AnyFlatSpec with ChiselScalatestTester {

  behavior of "MainMemory"

  it should "answer a request after the configured latency" in {
    test(new MainMemory(16, 3)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>   // -> MainMemory.vcd
      dut.io.valid.poke(true.B); dut.io.wen.poke(false.B); dut.io.addr.poke(7.U)
      dut.clock.step()                                  // request accepted
      dut.io.valid.poke(false.B)
      var n = 1
      while (!dut.io.respValid.peek().litToBoolean && n < 20) { dut.clock.step(); n += 1 }
      assert(n == 3, s"response came after $n cycles")
      dut.io.rdata.expect(0x107.U)                      // word 7 is initialised to 0x100 + 7
    }
  }

  behavior of "CacheMemory + MainMemory"

  it should "show hits, misses, write-through and eviction" in {
    // WriteVcdAnnotation dumps every signal of CacheSystem, CacheMemory and MainMemory to CacheSystem.vcd
    test(new CacheSystem(lines = 4, latency = 3)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      var cycle = 0
      def b(s: Bool): Boolean = s.peek().litToBoolean

      // one line of the activity trace
      def trace(): Unit = println(
        f"  cycle $cycle%3d | cache hit=${b(dut.io.hit)}%-5s | mem request=${b(dut.io.memReq)}%-5s " +
        f"response=${b(dut.io.memResp)}%-5s | done=${b(dut.io.done)}")

      /** Issue one request, hold it until `done`, and report (hit?, wait cycles, data). */
      def access(name: String, addr: Int, write: Boolean = false, wdata: Long = 0): (Boolean, Int, BigInt) = {
        println(s"--- $name")
        dut.io.valid.poke(true.B); dut.io.wen.poke(write.B)
        dut.io.addr.poke(addr.U); dut.io.wdata.poke(BigInt(wdata).U(32.W))
        val wasHit = b(dut.io.hit)                      // lookup result in the first cycle
        var waited = 0
        while (!b(dut.io.done) && waited < 50) { trace(); dut.clock.step(); cycle += 1; waited += 1 }
        trace()                                         // the cycle in which `done` is high
        val data = dut.io.rdata.peek().litValue
        dut.clock.step(); cycle += 1
        dut.io.valid.poke(false.B)
        println(f"  => ${if (wasHit) "HIT " else "MISS"}, waited $waited cycles, data = 0x$data%x")
        (wasHit, waited, data)
      }

      // address 5 and 9 share cache line 1 (5 % 4 == 9 % 4) but have different tags
      var r = access("read 5 (cold)", 5)
      assert(!r._1 && r._2 > 0 && r._3 == 0x105)

      r = access("read 5 again", 5)
      assert(r._1 && r._2 == 0 && r._3 == 0x105)

      r = access("write 5 <- 0xAAAA (hit, written through to memory)", 5, write = true, wdata = 0xAAAAL)
      assert(r._1 && r._2 > 0)

      r = access("read 5 (sees the new value in the cache)", 5)
      assert(r._1 && r._2 == 0 && r._3 == 0xAAAAL)

      r = access("read 9 (conflict: evicts address 5)", 9)
      assert(!r._1 && r._2 > 0 && r._3 == 0x109)

      r = access("read 5 (evicted -> miss; memory holds the written value)", 5)
      assert(!r._1 && r._3 == 0xAAAAL)

      r = access("write 2 <- 0xBBBB (miss: memory only, no allocate)", 2, write = true, wdata = 0xBBBBL)
      assert(!r._1)

      r = access("read 2 (still not cached -> miss)", 2)
      assert(!r._1 && r._3 == 0xBBBBL)

      val hits = dut.io.hits.peek().litValue
      val misses = dut.io.misses.peek().litValue
      val mr = dut.io.memReads.peek().litValue
      val mw = dut.io.memWrites.peek().litValue
      println(s"\nSUMMARY: cache hits = $hits, misses = $misses | main memory reads = $mr, writes = $mw")
      assert(hits == 3 && misses == 5)
      assert(mr == 4 && mw == 2)                        // 4 read misses, 2 write-through stores
    }
  }
}

