package rv32i

import chisel3.stage.ChiselStage

object GenerateMyModule extends App {
  (new ChiselStage).emitVerilog(
    new MyModule,
    Array("--target-dir", "generated")
  )
}

