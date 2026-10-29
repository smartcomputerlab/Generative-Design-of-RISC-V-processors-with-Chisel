package rv32i

import chisel3._
import chisel3.util._

class ImmediateGenerator(p: CoreParams) extends Module {
  val io = IO(new Bundle {
    val instr = Input(UInt(32.W))
    val imm   = Output(UInt(p.xlen.W))
  })
  val imm12 = io.instr(31, 20)
  io.imm := imm12.asSInt.pad(p.xlen).asUInt
}

