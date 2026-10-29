package rv32i

import chisel3._
import chisel3.stage.ChiselStage

object GenerateRV32ITestWrapper extends App {

  // Processor configuration
  val p = CoreParams()

  // Initial register values:
  // x0 = 0
  // x1 = 10
  // x2 = 3
  // x3 to x31 = 0
  val initialRegs =
    Seq(BigInt(0), BigInt(10), BigInt(3)) ++
    Seq.fill(29)(BigInt(0))

  // Instruction memory initialization file
  val memoryFile = "memory.hex"

  // Generate Verilog
  (new ChiselStage).emitVerilog(
    new RV32ITestWrapper(
      p,
      memoryFile,
      initialRegs
    ),
    Array("--target-dir", "generated")
  )

}

