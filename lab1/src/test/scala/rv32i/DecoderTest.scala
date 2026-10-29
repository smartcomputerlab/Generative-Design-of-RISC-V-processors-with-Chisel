package rv32i

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class DecoderTest extends AnyFlatSpec with ChiselScalatestTester {

  behavior of "Decoder"

  it should "decode R-type and I-type instructions" in {
    test(new Decoder).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>

      // Test one R-type instruction
      def testR(
        instr: BigInt,
        rs1: Int,
        rs2: Int,
        rd: Int,
        op: UInt
      ): Unit = {
        dut.io.instr.poke(instr.U)

        dut.io.rs1.expect(rs1.U)
        dut.io.rs2.expect(rs2.U)
        dut.io.rd.expect(rd.U)

        dut.io.aluOp.expect(op)
        dut.io.aluSrc.expect(false.B)
        dut.io.regWrite.expect(true.B)
        dut.io.valid.expect(true.B)

        dut.clock.step()
      }

      // Test one I-type instruction
      def testI(
        instr: BigInt,
        rs1: Int,
        rd: Int,
        op: UInt
      ): Unit = {
        dut.io.instr.poke(instr.U)

        dut.io.rs1.expect(rs1.U)
        dut.io.rd.expect(rd.U)

        dut.io.aluOp.expect(op)
        dut.io.aluSrc.expect(true.B)
        dut.io.regWrite.expect(true.B)
        dut.io.valid.expect(true.B)

        dut.clock.step()
      }

      // ----------------
      // R-type
      // ----------------

      testR(RV32IEncoding.add (3,  1,  2),  1,  2,  3, ALUOp.ADD)
      testR(RV32IEncoding.sub (4,  2,  5),  2,  5,  4, ALUOp.SUB)
      testR(RV32IEncoding.sll (5,  3,  6),  3,  6,  5, ALUOp.SLL)
      testR(RV32IEncoding.slt (6,  4,  7),  4,  7,  6, ALUOp.SLT)
      testR(RV32IEncoding.sltu(7,  5,  8),  5,  8,  7, ALUOp.SLTU)
      testR(RV32IEncoding.xor (8,  6,  9),  6,  9,  8, ALUOp.XOR)
      testR(RV32IEncoding.srl (9,  7, 10),  7, 10,  9, ALUOp.SRL)
      testR(RV32IEncoding.sra (10, 8, 11),  8, 11, 10, ALUOp.SRA)
      testR(RV32IEncoding.or  (11, 9, 12),  9, 12, 11, ALUOp.OR)
      testR(RV32IEncoding.and (12,10, 13), 10, 13, 12, ALUOp.AND)

      // ----------------
      // I-type
      // ----------------

      testI(RV32IEncoding.addi (13,  1,  10), 1,  13, ALUOp.ADD)
      testI(RV32IEncoding.addi (14,  2, -10), 2,  14, ALUOp.ADD)
      testI(RV32IEncoding.slti (15,  3, 100), 3,  15, ALUOp.SLT)
      testI(RV32IEncoding.sltiu(16,  4, 100), 4,  16, ALUOp.SLTU)
      testI(RV32IEncoding.xori (17,  5,0x55), 5,  17, ALUOp.XOR)
      testI(RV32IEncoding.ori  (18,  6,0x55), 6,  18, ALUOp.OR)
      testI(RV32IEncoding.andi (19,  7,0x55), 7,  19, ALUOp.AND)
      testI(RV32IEncoding.slli (20,  8,   3), 8,  20, ALUOp.SLL)
      testI(RV32IEncoding.srli (21,  9,   3), 9,  21, ALUOp.SRL)
      testI(RV32IEncoding.srai (22, 10,   3), 10, 22, ALUOp.SRA)
    }
  }
}

