package rv32i

object RV32IEncoding {

  // Generic R-type encoder
  private def R(
    rd: Int, rs1: Int, rs2: Int,
    f3: Int, f7: Int = 0
  ): BigInt =
    (BigInt(f7  & 0x7f) << 25) |
    (BigInt(rs2 & 0x1f) << 20) |
    (BigInt(rs1 & 0x1f) << 15) |
    (BigInt(f3  & 0x07) << 12) |
    (BigInt(rd  & 0x1f) << 7)  |
    0x33

  // Generic I-type encoder
  private def I(
    rd: Int, rs1: Int, imm: Int, f3: Int
  ): BigInt =
    ((BigInt(imm) & 0xfff) << 20) |
    (BigInt(rs1 & 0x1f) << 15) |
    (BigInt(f3  & 0x07) << 12) |
    (BigInt(rd  & 0x1f) << 7)  |
    0x13

  // ----------------------------------------------------------
  // R-type
  // ----------------------------------------------------------

  def add (rd:Int, rs1:Int, rs2:Int) = R(rd,rs1,rs2,0)
  def sub (rd:Int, rs1:Int, rs2:Int) = R(rd,rs1,rs2,0,0x20)
  def sll (rd:Int, rs1:Int, rs2:Int) = R(rd,rs1,rs2,1)
  def slt (rd:Int, rs1:Int, rs2:Int) = R(rd,rs1,rs2,2)
  def sltu(rd:Int, rs1:Int, rs2:Int) = R(rd,rs1,rs2,3)
  def xor (rd:Int, rs1:Int, rs2:Int) = R(rd,rs1,rs2,4)
  def srl (rd:Int, rs1:Int, rs2:Int) = R(rd,rs1,rs2,5)
  def sra (rd:Int, rs1:Int, rs2:Int) = R(rd,rs1,rs2,5,0x20)
  def or  (rd:Int, rs1:Int, rs2:Int) = R(rd,rs1,rs2,6)
  def and (rd:Int, rs1:Int, rs2:Int) = R(rd,rs1,rs2,7)

  // ----------------------------------------------------------
  // I-type
  // ----------------------------------------------------------

  def addi (rd:Int, rs1:Int, imm:Int) = I(rd,rs1,imm,0)
  def slti (rd:Int, rs1:Int, imm:Int) = I(rd,rs1,imm,2)
  def sltiu(rd:Int, rs1:Int, imm:Int) = I(rd,rs1,imm,3)
  def xori (rd:Int, rs1:Int, imm:Int) = I(rd,rs1,imm,4)
  def ori  (rd:Int, rs1:Int, imm:Int) = I(rd,rs1,imm,6)
  def andi (rd:Int, rs1:Int, imm:Int) = I(rd,rs1,imm,7)

  // ----------------------------------------------------------
  // Shift immediate
  // ----------------------------------------------------------

  def slli(rd:Int, rs1:Int, sh:Int) =
    I(rd,rs1,sh & 0x1f,1)

  def srli(rd:Int, rs1:Int, sh:Int) =
    I(rd,rs1,sh & 0x1f,5)

  def srai(rd:Int, rs1:Int, sh:Int) =
    I(rd,rs1,0x400 | (sh & 0x1f),5)
}

