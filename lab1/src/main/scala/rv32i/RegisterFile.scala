package rv32i

import chisel3._
import chisel3.util._

class RegisterFile(
  p: CoreParams,
  initialValues: Seq[BigInt] = Seq.empty
) extends Module {
  require(
    initialValues.isEmpty || initialValues.length == p.numRegs,
    "initialValues must be empty or contain exactly numRegs entries"
  )
  val io = IO(new Bundle {
    val rs1         = Input(UInt(log2Ceil(p.numRegs).W))
    val rs2         = Input(UInt(log2Ceil(p.numRegs).W))
    val rd          = Input(UInt(log2Ceil(p.numRegs).W))
    val writeData   = Input(UInt(p.xlen.W))
    val writeEnable = Input(Bool())
    val readData1   = Output(UInt(p.xlen.W))
    val readData2   = Output(UInt(p.xlen.W))
    val debugRead   = Output(Vec(p.numRegs, UInt(p.xlen.W)))
  })
  val resetValues =
    if (initialValues.nonEmpty) {
      initialValues.map(_.U(p.xlen.W))
    } else {
      Seq.fill(p.numRegs)(0.U(p.xlen.W))
    }
  val regs = RegInit(VecInit(resetValues))
  io.readData1 := Mux(
    io.rs1 === 0.U,
    0.U,
    regs(io.rs1)
  )
  io.readData2 := Mux(
    io.rs2 === 0.U,
    0.U,
    regs(io.rs2)
  )
  when(io.writeEnable && io.rd =/= 0.U) {
    regs(io.rd) := io.writeData
  }
  for (i <- 0 until p.numRegs) {
    io.debugRead(i) := Mux(
      i.U === 0.U,
      0.U,
      regs(i)
    )
  }
}

