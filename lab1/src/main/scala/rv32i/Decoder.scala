package rv32i

import chisel3._
import chisel3.util._

class Decoder extends Module {

  val io = IO(new Bundle {
    val instr    = Input(UInt(32.W))

    val rs1      = Output(UInt(5.W))
    val rs2      = Output(UInt(5.W))
    val rd       = Output(UInt(5.W))

    val aluOp    = Output(UInt(ALUOp.width.W))
    val aluSrc   = Output(Bool())

    val regWrite = Output(Bool())
    val valid    = Output(Bool())
  })

  // ------------------------------------------------------------
  // Instruction fields
  // ------------------------------------------------------------

  val opcode = io.instr(6, 0)
  val funct3 = io.instr(14, 12)
  val funct7 = io.instr(31, 25)

  io.rs1 := io.instr(19, 15)
  io.rs2 := io.instr(24, 20)
  io.rd  := io.instr(11, 7)

  // ------------------------------------------------------------
  // Default control signals
  // ------------------------------------------------------------

  io.aluOp  := ALUOp.NOP
  io.aluSrc := false.B
  io.valid  := false.B

  // ------------------------------------------------------------
  // R-type instructions
  //
  // opcode = 0110011
  //
  // ADD  SUB  SLL  SLT  SLTU
  // XOR  SRL  SRA  OR   AND
  // ------------------------------------------------------------

  when(opcode === "b0110011".U) {

    io.aluOp := MuxLookup(
      Cat(funct7, funct3),
      ALUOp.NOP,
      Seq(
        "b0000000000".U -> ALUOp.ADD,
        "b0100000000".U -> ALUOp.SUB,

        "b0000000001".U -> ALUOp.SLL,

        "b0000000010".U -> ALUOp.SLT,
        "b0000000011".U -> ALUOp.SLTU,

        "b0000000100".U -> ALUOp.XOR,

        "b0000000101".U -> ALUOp.SRL,
        "b0100000101".U -> ALUOp.SRA,

        "b0000000110".U -> ALUOp.OR,
        "b0000000111".U -> ALUOp.AND
      )
    )

    io.valid := io.aluOp =/= ALUOp.NOP
  }

  // ------------------------------------------------------------
  // I-type ALU instructions
  //
  // opcode = 0010011
  //
  // ADDI SLTI SLTIU XORI ORI ANDI
  // SLLI SRLI SRAI
  // ------------------------------------------------------------

  when(opcode === "b0010011".U) {

    // Second ALU operand comes from immediate generator
    io.aluSrc := true.B

    io.aluOp := MuxLookup(
      funct3,
      ALUOp.NOP,
      Seq(

        // ADDI
        "b000".U -> ALUOp.ADD,

        // SLTI
        "b010".U -> ALUOp.SLT,

        // SLTIU
        "b011".U -> ALUOp.SLTU,

        // XORI
        "b100".U -> ALUOp.XOR,

        // ORI
        "b110".U -> ALUOp.OR,

        // ANDI
        "b111".U -> ALUOp.AND,

        // SLLI
        "b001".U -> Mux(
          funct7 === "b0000000".U,
          ALUOp.SLL,
          ALUOp.NOP
        ),

        // SRLI / SRAI
        "b101".U -> Mux(
          funct7 === "b0000000".U,
          ALUOp.SRL,

          Mux(
            funct7 === "b0100000".U,
            ALUOp.SRA,
            ALUOp.NOP
          )
        )
      )
    )

    io.valid := io.aluOp =/= ALUOp.NOP
  }

  // ------------------------------------------------------------
  // Register write enable
  // ------------------------------------------------------------

  io.regWrite := io.valid
}

