package rv32i

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class FsmCounterSpec extends AnyFlatSpec
    with ChiselScalatestTester {

  behavior of "FsmCounter"

  it should "count 32 cycles and generate a VCD waveform" in {

    test(new FsmCounter)
      .withAnnotations(Seq(WriteVcdAnnotation)) { dut =>

        // ----------------------------------------
        // Initial state after reset
        // ----------------------------------------
        dut.io.start.poke(false.B)

        dut.io.busy.expect(false.B)
        dut.io.done.expect(false.B)
        dut.io.count.expect(0.U)

        dut.clock.step(2)

        // ----------------------------------------
        // Start the FSM
        // ----------------------------------------
        dut.io.start.poke(true.B)
        dut.clock.step(1)

        // Deassert start
        dut.io.start.poke(false.B)

        // ----------------------------------------
        // RUN state: 32 iterations
        // ----------------------------------------
        for (i <- 0 until 32) {

          dut.io.busy.expect(true.B)
          dut.io.done.expect(false.B)
          dut.io.count.expect(i.U)

          println(s"Cycle $i : counter = $i")

          dut.clock.step(1)
        }

        // ----------------------------------------
        // DONE state
        // ----------------------------------------
        dut.io.busy.expect(false.B)
        dut.io.done.expect(true.B)
        dut.io.count.expect(32.U)

        println("FSM reached DONE state")

        dut.clock.step(1)

        // ----------------------------------------
        // Return to IDLE
        // ----------------------------------------
        dut.io.busy.expect(false.B)
        dut.io.done.expect(false.B)

        println("FSM returned to IDLE")
      }
  }
}

