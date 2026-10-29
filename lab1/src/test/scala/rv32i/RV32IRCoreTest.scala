package rv32i

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class RV32IRCoreTest extends AnyFlatSpec with ChiselScalatestTester {

  behavior of "RV32IRCore"

  it should "execute 10 R-type instructions" in {
    val p = CoreParams()

    val initialRegs =
      Seq(BigInt(0), BigInt(10), BigInt(3)) ++
      Seq.fill(29)(BigInt(0))

    test(new RV32ITestWrapper(p, "memory.hex", initialRegs))
      .withAnnotations(Seq(WriteVcdAnnotation)) { dut =>

        // Initial PC
        dut.io.debugPC.expect(0.U)

        // ADD x3, x1, x2
        dut.clock.step()
        dut.io.debugPC.expect(4.U)
        dut.io.debugX3.expect(13.U)

        // SUB x4, x1, x2
        dut.clock.step()
        dut.io.debugPC.expect(8.U)
        dut.io.debugX4.expect(7.U)

        // Remaining 8 instructions
        for (pc <- 12 to 40 by 4) {
          dut.clock.step()
          dut.io.debugPC.expect(pc.U)
        }
      }
  }
}

