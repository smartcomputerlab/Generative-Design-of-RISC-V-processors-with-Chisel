package rv32i

import chisel3._
import chisel3.stage.ChiselStage

object GenerateRV32IRCore extends App {

  // Processor configuration
  val p = CoreParams()

  // Initial register values
  // x0 = 0, x1 = 10, x2 = 3, x3-x31 = 0
  val initialRegs =
    Seq(BigInt(0), BigInt(10), BigInt(3)) ++
    Seq.fill(29)(BigInt(0))

  // Generate Verilog
  (new ChiselStage).emitVerilog(
    new RV32IRCore(p, initialRegs),
    Array("--target-dir", "generated")
  )

}

