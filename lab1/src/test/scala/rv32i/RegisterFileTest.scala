package rv32i
import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class RegisterFileTest extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "RegisterFile"
  it should "read, write and protect x0" in {
    test(new RegisterFile(CoreParams()))
      .withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
        def write(rd: Int, data: Int, enable: Boolean = true): Unit = {
          dut.io.rd.poke(rd.U)
          dut.io.writeData.poke(data.U)
          dut.io.writeEnable.poke(enable.B)
          dut.clock.step()
        }
        def read(rs1: Int, rs2: Int, exp1: Int, exp2: Int): Unit = {
          dut.io.rs1.poke(rs1.U)
          dut.io.rs2.poke(rs2.U)
          dut.io.readData1.expect(exp1.U)
          dut.io.readData2.expect(exp2.U)
        }
        // x0 must always be zero
        read(0, 0, 0, 0)
        // Normal writes
        write(1, 10)
        write(2, 20)
        read(1, 2, 10, 20)
        write(5, 123)
        read(5, 0, 123, 0)
        // Write disabled
        write(6, 999, enable = false)
        read(6, 0, 0, 0)
        // Writing x0 must have no effect
        write(0, 999)
        read(0, 0, 0, 0)
      }
  }
  it should "initialize registers" in {
    val values = (0 until 32).map(i => BigInt(i * 10))
    test(new RegisterFile(CoreParams(), values))
      .withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
        for (i <- Seq(1, 2, 10, 20, 31)) {
          dut.io.rs1.poke(i.U)
          dut.io.readData1.expect((i * 10).U)
        }
        // x0 remains zero
        dut.io.rs1.poke(0.U)
        dut.io.readData1.expect(0.U)
        dut.clock.step()
      }
  }
}

