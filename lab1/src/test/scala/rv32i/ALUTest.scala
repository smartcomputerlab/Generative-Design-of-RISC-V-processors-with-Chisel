package rv32i

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class ALUTest extends AnyFlatSpec with ChiselScalatestTester {

  behavior of "ALU"

  it should "perform all ALU operations" in {
    test(new ALU(CoreParams()))
      .withAnnotations(Seq(WriteVcdAnnotation)) { dut =>

        def check(a: BigInt, b: BigInt, op: UInt, expected: BigInt): Unit = {
          dut.io.a.poke(a.U)
          dut.io.b.poke(b.U)
          dut.io.op.poke(op)
          dut.clock.step()
          dut.io.y.expect(expected.U)
        }

        check(10, 3, ALUOp.ADD,  13)
        check(10, 3, ALUOp.SUB,   7)
        check(10, 3, ALUOp.AND,   2)
        check(10, 3, ALUOp.OR,   11)
        check(10, 3, ALUOp.XOR,   9)

        check(1,  4, ALUOp.SLL,  16)
        check(16, 2, ALUOp.SRL,   4)

        // Signed: -1 < 1
        check(BigInt("ffffffff", 16), 1, ALUOp.SLT, 1)

        // Unsigned: 0xffffffff > 1
        check(BigInt("ffffffff", 16), 1, ALUOp.SLTU, 0)

        // Arithmetic right shift
        check(
          BigInt("80000000", 16),
          1,
          ALUOp.SRA,
          BigInt("c0000000", 16)
        )

        check(123, 456, ALUOp.NOP, 0)
      }
  }
}

