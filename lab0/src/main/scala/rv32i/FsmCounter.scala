package rv32i

import chisel3._
import chisel3.util._

class FsmCounter extends Module {

  val io = IO(new Bundle {
    val start = Input(Bool())
    val count = Output(UInt(6.W))
    val busy  = Output(Bool())
    val done  = Output(Bool())
  })

  // Define FSM states
  val sIdle :: sRun :: sDone :: Nil = Enum(3)

  // State register
  val state = RegInit(sIdle)

  // Counter register
  val counter = RegInit(0.U(6.W))

  // FSM
  switch(state) {

    is(sIdle) {
      when(io.start) {
        counter := 0.U
        state := sRun
      }
    }

    is(sRun) {
      counter := counter + 1.U

      when(counter === 31.U) {
        state := sDone
      }
    }

    is(sDone) {
      state := sIdle
    }
  }

  // Outputs
  io.count := counter
  io.busy  := state === sRun
  io.done  := state === sDone
}

// Generate Verilog
object FsmCounterMain extends App {
  (new chisel3.stage.ChiselStage)
    .emitVerilog(
      new FsmCounter,
      Array("--target-dir", "generated")
    )
}

