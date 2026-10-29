package rv32i

import chisel3._
import chisel3.util._
import scala.io.Source

class InstructionMemory(
  fileName: String,
  depth: Int = 1024
) extends Module {

  val io = IO(new Bundle {
    val addr = Input(UInt(32.W))
    val inst = Output(UInt(32.W))
  })

  val instructions = {
    val source = Source.fromFile(fileName)

    try {
      source.getLines().toVector
        .map(_.trim)
        .filter(_.nonEmpty)
        .filterNot(_.startsWith("#"))
        .map(line => BigInt(line.replace("_", ""), 16))
    } finally {
      source.close()
    }
  }

  require(
    instructions.nonEmpty,
    s"Instruction memory file '$fileName' is empty"
  )

  require(
    instructions.length <= depth,
    s"Instruction memory contains ${instructions.length} words, but depth is only $depth"
  )

  val mem = VecInit(
    instructions.map(_.U(32.W)) ++
      Seq.fill(depth - instructions.length)(0.U(32.W))
  )

  val wordAddr = io.addr >> 2

  io.inst := mem(wordAddr)
}

