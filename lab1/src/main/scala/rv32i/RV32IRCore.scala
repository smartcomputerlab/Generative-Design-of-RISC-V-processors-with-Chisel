package rv32i
import chisel3._
import chisel3.util._

class RV32IRCore(
  p: CoreParams,
  initialRegs: Seq[BigInt] = Seq.empty
) extends Module {
  val io = IO(new Bundle {
    // Instruction-memory interface.
    val imemAddr = Output(UInt(p.xlen.W))
    val imemInst = Input(UInt(32.W))
    // Debug outputs for laboratory verification.
    val debugPC = Output(UInt(p.xlen.W))
    val debugX1 = Output(UInt(p.xlen.W))
    val debugX2 = Output(UInt(p.xlen.W))
    val debugX3 = Output(UInt(p.xlen.W))
    val debugX4 = Output(UInt(p.xlen.W))
  })
  // --------------------------------------------------------------------------
  // Program counter
  // --------------------------------------------------------------------------
  val pc = RegInit(0.U(p.xlen.W))
  io.imemAddr := pc
  io.debugPC := pc
  // --------------------------------------------------------------------------
  // Decoder
  // --------------------------------------------------------------------------
  val decoder = Module(new Decoder)
  decoder.io.instr := io.imemInst
  // --------------------------------------------------------------------------
  // Register file
  // --------------------------------------------------------------------------
  val regFile = Module(
    new RegisterFile(p, initialRegs)
  )
  regFile.io.rs1 := decoder.io.rs1
  regFile.io.rs2 := decoder.io.rs2
  // --------------------------------------------------------------------------
  // ALU
  // --------------------------------------------------------------------------
  val alu = Module(new ALU(p))
  alu.io.a := regFile.io.readData1
  alu.io.b := regFile.io.readData2
  alu.io.op := decoder.io.aluOp
  // --------------------------------------------------------------------------
  // Write-back
  // --------------------------------------------------------------------------
  regFile.io.rd := decoder.io.rd
  regFile.io.writeData := alu.io.y
  regFile.io.writeEnable := decoder.io.regWrite
  // --------------------------------------------------------------------------
  // Program counter update
  // --------------------------------------------------------------------------
  pc := pc + 4.U
  // --------------------------------------------------------------------------
  // Debug signals
  // --------------------------------------------------------------------------
  io.debugX1 := regFile.io.debugRead(1)
  io.debugX2 := regFile.io.debugRead(2)
  io.debugX3 := regFile.io.debugRead(3)
  io.debugX4 := regFile.io.debugRead(4)
}

