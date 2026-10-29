object RV32IEncoding {
  def rType(
    funct7: Int,
    rs2: Int,
    rs1: Int,
    funct3: Int,
    rd: Int
  ): BigInt = {

    (BigInt(funct7 & 0x7f) << 25) |
    (BigInt(rs2 & 0x1f) << 20) |
    (BigInt(rs1 & 0x1f) << 15) |
    (BigInt(funct3 & 0x7) << 12) |
    (BigInt(rd & 0x1f) << 7) |
    BigInt(0x33)
  }
  def add(rd: Int, rs1: Int, rs2: Int): BigInt =
    rType(0x00, rs2, rs1, 0x0, rd)
  def sub(rd: Int, rs1: Int, rs2: Int): BigInt =
    rType(0x20, rs2, rs1, 0x0, rd)
  def sll(rd: Int, rs1: Int, rs2: Int): BigInt =
    rType(0x00, rs2, rs1, 0x1, rd)
  def slt(rd: Int, rs1: Int, rs2: Int): BigInt =
    rType(0x00, rs2, rs1, 0x2, rd)
  def sltu(rd: Int, rs1: Int, rs2: Int): BigInt =
    rType(0x00, rs2, rs1, 0x3, rd)
  def xor(rd: Int, rs1: Int, rs2: Int): BigInt =
    rType(0x00, rs2, rs1, 0x4, rd)
  def srl(rd: Int, rs1: Int, rs2: Int): BigInt =
    rType(0x00, rs2, rs1, 0x5, rd)
  def sra(rd: Int, rs1: Int, rs2: Int): BigInt =
    rType(0x20, rs2, rs1, 0x5, rd)
  def or(rd: Int, rs1: Int, rs2: Int): BigInt =
    rType(0x00, rs2, rs1, 0x6, rd)
  def and(rd: Int, rs1: Int, rs2: Int): BigInt =
    rType(0x00, rs2, rs1, 0x7, rd)
}

