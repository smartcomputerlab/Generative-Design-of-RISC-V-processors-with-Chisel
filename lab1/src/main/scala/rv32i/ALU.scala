package rv32i
import chisel3._
import chisel3.util._

object ALUOp {
  val width = 4
  val ADD  = 0.U(width.W)
  val SUB  = 1.U(width.W)
  val SLL  = 2.U(width.W)
  val SLT  = 3.U(width.W)
  val SLTU = 4.U(width.W)
  val XOR  = 5.U(width.W)
  val SRL  = 6.U(width.W)
  val SRA  = 7.U(width.W)
  val OR   = 8.U(width.W)
  val AND  = 9.U(width.W)
  val NOP  = 15.U(width.W)
}

class ALU(p: CoreParams) extends Module {
  val io = IO(new Bundle {
    val a  = Input(UInt(p.xlen.W))
    val b  = Input(UInt(p.xlen.W))
    val op = Input(UInt(ALUOp.width.W))
    val y  = Output(UInt(p.xlen.W))
  })
  val shamt = io.b(log2Ceil(p.xlen) - 1, 0)
  io.y := MuxLookup(io.op, 0.U, Seq(
    ALUOp.ADD  -> (io.a + io.b),
    ALUOp.SUB  -> (io.a - io.b),
    ALUOp.SLL  -> (io.a << shamt),
    ALUOp.SLT  -> (io.a.asSInt < io.b.asSInt).asUInt,
    ALUOp.SLTU -> (io.a < io.b).asUInt,
    ALUOp.XOR  -> (io.a ^ io.b),
    ALUOp.SRL  -> (io.a >> shamt),
    ALUOp.SRA  -> (io.a.asSInt >> shamt).asUInt,
    ALUOp.OR   -> (io.a | io.b),
    ALUOp.AND  -> (io.a & io.b)
  ))
}

