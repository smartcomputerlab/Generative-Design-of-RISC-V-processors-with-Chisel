package rv32i

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class ImmediateGeneratorTest
    extends AnyFlatSpec
    with ChiselScalatestTester {

  behavior of "ImmediateGenerator"

  it should "generate and sign-extend I-type immediates sequentially" in {

    test(new ImmediateGenerator(CoreParams()))
      .withAnnotations(Seq(WriteVcdAnnotation)) { dut =>

      dut.io.instr.poke(
        RV32IEncoding.addi(1, 0, 10).U
      )
      dut.clock.step()
      dut.io.imm.expect(10.U)

      dut.io.instr.poke(
        RV32IEncoding.addi(2, 1, 20).U
      )
      dut.clock.step()
      dut.io.imm.expect(20.U)

      dut.io.instr.poke(
        RV32IEncoding.addi(3, 2, -1).U
      )
      dut.clock.step()
      dut.io.imm.expect("hffffffff".U)

      dut.io.instr.poke(
        RV32IEncoding.addi(4, 3, -10).U
      )
      dut.clock.step()
      dut.io.imm.expect("hfffffff6".U)

      dut.io.instr.poke(
        RV32IEncoding.addi(5, 4, 127).U
      )
      dut.clock.step()
      dut.io.imm.expect(127.U)

      dut.io.instr.poke(
        RV32IEncoding.addi(6, 5, -128).U
      )
      dut.clock.step()
      dut.io.imm.expect("hffffff80".U)

      dut.io.instr.poke(
        RV32IEncoding.addi(7, 6, 2047).U
      )
      dut.clock.step()
      dut.io.imm.expect(2047.U)

      dut.io.instr.poke(
        RV32IEncoding.addi(8, 7, -2048).U
      )
      dut.clock.step()
      dut.io.imm.expect("hfffff800".U)
    }
  }
}

