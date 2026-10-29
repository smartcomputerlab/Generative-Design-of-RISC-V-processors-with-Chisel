package rv32i

import chisel3._

class RV32ITestWrapper(
  p: CoreParams,
  memoryFile: String,
  initialRegs: Seq[BigInt] = Seq.empty
) extends Module {

  val io = IO(new Bundle {
    val debugPC = Output(UInt(p.xlen.W))
    val debugX1 = Output(UInt(p.xlen.W))
    val debugX2 = Output(UInt(p.xlen.W))
    val debugX3 = Output(UInt(p.xlen.W))
    val debugX4 = Output(UInt(p.xlen.W))
  })

  val core = Module(
    new RV32IRCore(p, initialRegs)
  )

  val imem = Module(
    new InstructionMemory(memoryFile)
  )

  imem.io.addr := core.io.imemAddr
  core.io.imemInst := imem.io.inst

  io.debugPC := core.io.debugPC
  io.debugX1 := core.io.debugX1
  io.debugX2 := core.io.debugX2
  io.debugX3 := core.io.debugX3
  io.debugX4 := core.io.debugX4
}

