module Decoder(
  input  [31:0] io_instr,
  output [4:0]  io_rs1,
  output [4:0]  io_rs2,
  output [4:0]  io_rd,
  output [3:0]  io_aluOp,
  output        io_regWrite,
  output        io_valid
);
  wire [6:0] opcode = io_instr[6:0]; // @[Decoder.scala 26:24]
  wire [2:0] funct3 = io_instr[14:12]; // @[Decoder.scala 27:24]
  wire [6:0] funct7 = io_instr[31:25]; // @[Decoder.scala 28:24]
  wire [9:0] _io_aluOp_T = {funct7,funct3}; // @[Cat.scala 33:92]
  wire [3:0] _io_aluOp_T_2 = 10'h0 == _io_aluOp_T ? 4'h0 : 4'hf; // @[Mux.scala 81:58]
  wire [3:0] _io_aluOp_T_4 = 10'h100 == _io_aluOp_T ? 4'h1 : _io_aluOp_T_2; // @[Mux.scala 81:58]
  wire [3:0] _io_aluOp_T_6 = 10'h1 == _io_aluOp_T ? 4'h2 : _io_aluOp_T_4; // @[Mux.scala 81:58]
  wire [3:0] _io_aluOp_T_8 = 10'h2 == _io_aluOp_T ? 4'h3 : _io_aluOp_T_6; // @[Mux.scala 81:58]
  wire [3:0] _io_aluOp_T_10 = 10'h3 == _io_aluOp_T ? 4'h4 : _io_aluOp_T_8; // @[Mux.scala 81:58]
  wire [3:0] _io_aluOp_T_12 = 10'h4 == _io_aluOp_T ? 4'h5 : _io_aluOp_T_10; // @[Mux.scala 81:58]
  wire [3:0] _io_aluOp_T_14 = 10'h5 == _io_aluOp_T ? 4'h6 : _io_aluOp_T_12; // @[Mux.scala 81:58]
  wire [3:0] _io_aluOp_T_16 = 10'h105 == _io_aluOp_T ? 4'h7 : _io_aluOp_T_14; // @[Mux.scala 81:58]
  wire [3:0] _io_aluOp_T_18 = 10'h6 == _io_aluOp_T ? 4'h8 : _io_aluOp_T_16; // @[Mux.scala 81:58]
  wire [3:0] _io_aluOp_T_20 = 10'h7 == _io_aluOp_T ? 4'h9 : _io_aluOp_T_18; // @[Mux.scala 81:58]
  wire  _io_valid_T = io_aluOp != 4'hf; // @[Decoder.scala 75:26]
  wire [3:0] _GEN_0 = opcode == 7'h33 ? _io_aluOp_T_20 : 4'hf; // @[Decoder.scala 38:13 51:33 53:14]
  wire  _GEN_1 = opcode == 7'h33 & io_aluOp != 4'hf; // @[Decoder.scala 40:13 51:33 75:14]
  wire  _io_aluOp_T_21 = funct7 == 7'h0; // @[Decoder.scala 117:18]
  wire [3:0] _io_aluOp_T_22 = _io_aluOp_T_21 ? 4'h2 : 4'hf; // @[Decoder.scala 116:24]
  wire  _io_aluOp_T_24 = funct7 == 7'h20; // @[Decoder.scala 128:20]
  wire [3:0] _io_aluOp_T_25 = _io_aluOp_T_24 ? 4'h7 : 4'hf; // @[Decoder.scala 127:14]
  wire [3:0] _io_aluOp_T_26 = _io_aluOp_T_21 ? 4'h6 : _io_aluOp_T_25; // @[Decoder.scala 123:24]
  wire [3:0] _io_aluOp_T_28 = 3'h2 == funct3 ? 4'h3 : 4'h0; // @[Mux.scala 81:58]
  wire [3:0] _io_aluOp_T_30 = 3'h3 == funct3 ? 4'h4 : _io_aluOp_T_28; // @[Mux.scala 81:58]
  wire [3:0] _io_aluOp_T_32 = 3'h4 == funct3 ? 4'h5 : _io_aluOp_T_30; // @[Mux.scala 81:58]
  wire [3:0] _io_aluOp_T_34 = 3'h6 == funct3 ? 4'h8 : _io_aluOp_T_32; // @[Mux.scala 81:58]
  wire [3:0] _io_aluOp_T_36 = 3'h7 == funct3 ? 4'h9 : _io_aluOp_T_34; // @[Mux.scala 81:58]
  wire [3:0] _io_aluOp_T_38 = 3'h1 == funct3 ? _io_aluOp_T_22 : _io_aluOp_T_36; // @[Mux.scala 81:58]
  wire [3:0] _io_aluOp_T_40 = 3'h5 == funct3 ? _io_aluOp_T_26 : _io_aluOp_T_38; // @[Mux.scala 81:58]
  assign io_rs1 = io_instr[19:15]; // @[Decoder.scala 30:21]
  assign io_rs2 = io_instr[24:20]; // @[Decoder.scala 31:21]
  assign io_rd = io_instr[11:7]; // @[Decoder.scala 32:21]
  assign io_aluOp = opcode == 7'h13 ? _io_aluOp_T_40 : _GEN_0; // @[Decoder.scala 87:33 92:14]
  assign io_regWrite = io_valid; // @[Decoder.scala 143:15]
  assign io_valid = opcode == 7'h13 ? _io_valid_T : _GEN_1; // @[Decoder.scala 136:14 87:33]
endmodule
module RegisterFile(
  input         clock,
  input         reset,
  input  [4:0]  io_rs1,
  input  [4:0]  io_rs2,
  input  [4:0]  io_rd,
  input  [31:0] io_writeData,
  input         io_writeEnable,
  output [31:0] io_readData1,
  output [31:0] io_readData2,
  output [31:0] io_debugRead_1,
  output [31:0] io_debugRead_2,
  output [31:0] io_debugRead_3,
  output [31:0] io_debugRead_4
);
`ifdef RANDOMIZE_REG_INIT
  reg [31:0] _RAND_0;
  reg [31:0] _RAND_1;
  reg [31:0] _RAND_2;
  reg [31:0] _RAND_3;
  reg [31:0] _RAND_4;
  reg [31:0] _RAND_5;
  reg [31:0] _RAND_6;
  reg [31:0] _RAND_7;
  reg [31:0] _RAND_8;
  reg [31:0] _RAND_9;
  reg [31:0] _RAND_10;
  reg [31:0] _RAND_11;
  reg [31:0] _RAND_12;
  reg [31:0] _RAND_13;
  reg [31:0] _RAND_14;
  reg [31:0] _RAND_15;
  reg [31:0] _RAND_16;
  reg [31:0] _RAND_17;
  reg [31:0] _RAND_18;
  reg [31:0] _RAND_19;
  reg [31:0] _RAND_20;
  reg [31:0] _RAND_21;
  reg [31:0] _RAND_22;
  reg [31:0] _RAND_23;
  reg [31:0] _RAND_24;
  reg [31:0] _RAND_25;
  reg [31:0] _RAND_26;
  reg [31:0] _RAND_27;
  reg [31:0] _RAND_28;
  reg [31:0] _RAND_29;
  reg [31:0] _RAND_30;
  reg [31:0] _RAND_31;
`endif // RANDOMIZE_REG_INIT
  reg [31:0] regs_0; // @[RegisterFile.scala 30:21]
  reg [31:0] regs_1; // @[RegisterFile.scala 30:21]
  reg [31:0] regs_2; // @[RegisterFile.scala 30:21]
  reg [31:0] regs_3; // @[RegisterFile.scala 30:21]
  reg [31:0] regs_4; // @[RegisterFile.scala 30:21]
  reg [31:0] regs_5; // @[RegisterFile.scala 30:21]
  reg [31:0] regs_6; // @[RegisterFile.scala 30:21]
  reg [31:0] regs_7; // @[RegisterFile.scala 30:21]
  reg [31:0] regs_8; // @[RegisterFile.scala 30:21]
  reg [31:0] regs_9; // @[RegisterFile.scala 30:21]
  reg [31:0] regs_10; // @[RegisterFile.scala 30:21]
  reg [31:0] regs_11; // @[RegisterFile.scala 30:21]
  reg [31:0] regs_12; // @[RegisterFile.scala 30:21]
  reg [31:0] regs_13; // @[RegisterFile.scala 30:21]
  reg [31:0] regs_14; // @[RegisterFile.scala 30:21]
  reg [31:0] regs_15; // @[RegisterFile.scala 30:21]
  reg [31:0] regs_16; // @[RegisterFile.scala 30:21]
  reg [31:0] regs_17; // @[RegisterFile.scala 30:21]
  reg [31:0] regs_18; // @[RegisterFile.scala 30:21]
  reg [31:0] regs_19; // @[RegisterFile.scala 30:21]
  reg [31:0] regs_20; // @[RegisterFile.scala 30:21]
  reg [31:0] regs_21; // @[RegisterFile.scala 30:21]
  reg [31:0] regs_22; // @[RegisterFile.scala 30:21]
  reg [31:0] regs_23; // @[RegisterFile.scala 30:21]
  reg [31:0] regs_24; // @[RegisterFile.scala 30:21]
  reg [31:0] regs_25; // @[RegisterFile.scala 30:21]
  reg [31:0] regs_26; // @[RegisterFile.scala 30:21]
  reg [31:0] regs_27; // @[RegisterFile.scala 30:21]
  reg [31:0] regs_28; // @[RegisterFile.scala 30:21]
  reg [31:0] regs_29; // @[RegisterFile.scala 30:21]
  reg [31:0] regs_30; // @[RegisterFile.scala 30:21]
  reg [31:0] regs_31; // @[RegisterFile.scala 30:21]
  wire  _io_readData1_T = io_rs1 == 5'h0; // @[RegisterFile.scala 32:12]
  wire [31:0] _GEN_1 = 5'h1 == io_rs1 ? regs_1 : regs_0; // @[RegisterFile.scala 31:{22,22}]
  wire [31:0] _GEN_2 = 5'h2 == io_rs1 ? regs_2 : _GEN_1; // @[RegisterFile.scala 31:{22,22}]
  wire [31:0] _GEN_3 = 5'h3 == io_rs1 ? regs_3 : _GEN_2; // @[RegisterFile.scala 31:{22,22}]
  wire [31:0] _GEN_4 = 5'h4 == io_rs1 ? regs_4 : _GEN_3; // @[RegisterFile.scala 31:{22,22}]
  wire [31:0] _GEN_5 = 5'h5 == io_rs1 ? regs_5 : _GEN_4; // @[RegisterFile.scala 31:{22,22}]
  wire [31:0] _GEN_6 = 5'h6 == io_rs1 ? regs_6 : _GEN_5; // @[RegisterFile.scala 31:{22,22}]
  wire [31:0] _GEN_7 = 5'h7 == io_rs1 ? regs_7 : _GEN_6; // @[RegisterFile.scala 31:{22,22}]
  wire [31:0] _GEN_8 = 5'h8 == io_rs1 ? regs_8 : _GEN_7; // @[RegisterFile.scala 31:{22,22}]
  wire [31:0] _GEN_9 = 5'h9 == io_rs1 ? regs_9 : _GEN_8; // @[RegisterFile.scala 31:{22,22}]
  wire [31:0] _GEN_10 = 5'ha == io_rs1 ? regs_10 : _GEN_9; // @[RegisterFile.scala 31:{22,22}]
  wire [31:0] _GEN_11 = 5'hb == io_rs1 ? regs_11 : _GEN_10; // @[RegisterFile.scala 31:{22,22}]
  wire [31:0] _GEN_12 = 5'hc == io_rs1 ? regs_12 : _GEN_11; // @[RegisterFile.scala 31:{22,22}]
  wire [31:0] _GEN_13 = 5'hd == io_rs1 ? regs_13 : _GEN_12; // @[RegisterFile.scala 31:{22,22}]
  wire [31:0] _GEN_14 = 5'he == io_rs1 ? regs_14 : _GEN_13; // @[RegisterFile.scala 31:{22,22}]
  wire [31:0] _GEN_15 = 5'hf == io_rs1 ? regs_15 : _GEN_14; // @[RegisterFile.scala 31:{22,22}]
  wire [31:0] _GEN_16 = 5'h10 == io_rs1 ? regs_16 : _GEN_15; // @[RegisterFile.scala 31:{22,22}]
  wire [31:0] _GEN_17 = 5'h11 == io_rs1 ? regs_17 : _GEN_16; // @[RegisterFile.scala 31:{22,22}]
  wire [31:0] _GEN_18 = 5'h12 == io_rs1 ? regs_18 : _GEN_17; // @[RegisterFile.scala 31:{22,22}]
  wire [31:0] _GEN_19 = 5'h13 == io_rs1 ? regs_19 : _GEN_18; // @[RegisterFile.scala 31:{22,22}]
  wire [31:0] _GEN_20 = 5'h14 == io_rs1 ? regs_20 : _GEN_19; // @[RegisterFile.scala 31:{22,22}]
  wire [31:0] _GEN_21 = 5'h15 == io_rs1 ? regs_21 : _GEN_20; // @[RegisterFile.scala 31:{22,22}]
  wire [31:0] _GEN_22 = 5'h16 == io_rs1 ? regs_22 : _GEN_21; // @[RegisterFile.scala 31:{22,22}]
  wire [31:0] _GEN_23 = 5'h17 == io_rs1 ? regs_23 : _GEN_22; // @[RegisterFile.scala 31:{22,22}]
  wire [31:0] _GEN_24 = 5'h18 == io_rs1 ? regs_24 : _GEN_23; // @[RegisterFile.scala 31:{22,22}]
  wire [31:0] _GEN_25 = 5'h19 == io_rs1 ? regs_25 : _GEN_24; // @[RegisterFile.scala 31:{22,22}]
  wire [31:0] _GEN_26 = 5'h1a == io_rs1 ? regs_26 : _GEN_25; // @[RegisterFile.scala 31:{22,22}]
  wire [31:0] _GEN_27 = 5'h1b == io_rs1 ? regs_27 : _GEN_26; // @[RegisterFile.scala 31:{22,22}]
  wire [31:0] _GEN_28 = 5'h1c == io_rs1 ? regs_28 : _GEN_27; // @[RegisterFile.scala 31:{22,22}]
  wire [31:0] _GEN_29 = 5'h1d == io_rs1 ? regs_29 : _GEN_28; // @[RegisterFile.scala 31:{22,22}]
  wire [31:0] _GEN_30 = 5'h1e == io_rs1 ? regs_30 : _GEN_29; // @[RegisterFile.scala 31:{22,22}]
  wire [31:0] _GEN_31 = 5'h1f == io_rs1 ? regs_31 : _GEN_30; // @[RegisterFile.scala 31:{22,22}]
  wire  _io_readData2_T = io_rs2 == 5'h0; // @[RegisterFile.scala 37:12]
  wire [31:0] _GEN_33 = 5'h1 == io_rs2 ? regs_1 : regs_0; // @[RegisterFile.scala 36:{22,22}]
  wire [31:0] _GEN_34 = 5'h2 == io_rs2 ? regs_2 : _GEN_33; // @[RegisterFile.scala 36:{22,22}]
  wire [31:0] _GEN_35 = 5'h3 == io_rs2 ? regs_3 : _GEN_34; // @[RegisterFile.scala 36:{22,22}]
  wire [31:0] _GEN_36 = 5'h4 == io_rs2 ? regs_4 : _GEN_35; // @[RegisterFile.scala 36:{22,22}]
  wire [31:0] _GEN_37 = 5'h5 == io_rs2 ? regs_5 : _GEN_36; // @[RegisterFile.scala 36:{22,22}]
  wire [31:0] _GEN_38 = 5'h6 == io_rs2 ? regs_6 : _GEN_37; // @[RegisterFile.scala 36:{22,22}]
  wire [31:0] _GEN_39 = 5'h7 == io_rs2 ? regs_7 : _GEN_38; // @[RegisterFile.scala 36:{22,22}]
  wire [31:0] _GEN_40 = 5'h8 == io_rs2 ? regs_8 : _GEN_39; // @[RegisterFile.scala 36:{22,22}]
  wire [31:0] _GEN_41 = 5'h9 == io_rs2 ? regs_9 : _GEN_40; // @[RegisterFile.scala 36:{22,22}]
  wire [31:0] _GEN_42 = 5'ha == io_rs2 ? regs_10 : _GEN_41; // @[RegisterFile.scala 36:{22,22}]
  wire [31:0] _GEN_43 = 5'hb == io_rs2 ? regs_11 : _GEN_42; // @[RegisterFile.scala 36:{22,22}]
  wire [31:0] _GEN_44 = 5'hc == io_rs2 ? regs_12 : _GEN_43; // @[RegisterFile.scala 36:{22,22}]
  wire [31:0] _GEN_45 = 5'hd == io_rs2 ? regs_13 : _GEN_44; // @[RegisterFile.scala 36:{22,22}]
  wire [31:0] _GEN_46 = 5'he == io_rs2 ? regs_14 : _GEN_45; // @[RegisterFile.scala 36:{22,22}]
  wire [31:0] _GEN_47 = 5'hf == io_rs2 ? regs_15 : _GEN_46; // @[RegisterFile.scala 36:{22,22}]
  wire [31:0] _GEN_48 = 5'h10 == io_rs2 ? regs_16 : _GEN_47; // @[RegisterFile.scala 36:{22,22}]
  wire [31:0] _GEN_49 = 5'h11 == io_rs2 ? regs_17 : _GEN_48; // @[RegisterFile.scala 36:{22,22}]
  wire [31:0] _GEN_50 = 5'h12 == io_rs2 ? regs_18 : _GEN_49; // @[RegisterFile.scala 36:{22,22}]
  wire [31:0] _GEN_51 = 5'h13 == io_rs2 ? regs_19 : _GEN_50; // @[RegisterFile.scala 36:{22,22}]
  wire [31:0] _GEN_52 = 5'h14 == io_rs2 ? regs_20 : _GEN_51; // @[RegisterFile.scala 36:{22,22}]
  wire [31:0] _GEN_53 = 5'h15 == io_rs2 ? regs_21 : _GEN_52; // @[RegisterFile.scala 36:{22,22}]
  wire [31:0] _GEN_54 = 5'h16 == io_rs2 ? regs_22 : _GEN_53; // @[RegisterFile.scala 36:{22,22}]
  wire [31:0] _GEN_55 = 5'h17 == io_rs2 ? regs_23 : _GEN_54; // @[RegisterFile.scala 36:{22,22}]
  wire [31:0] _GEN_56 = 5'h18 == io_rs2 ? regs_24 : _GEN_55; // @[RegisterFile.scala 36:{22,22}]
  wire [31:0] _GEN_57 = 5'h19 == io_rs2 ? regs_25 : _GEN_56; // @[RegisterFile.scala 36:{22,22}]
  wire [31:0] _GEN_58 = 5'h1a == io_rs2 ? regs_26 : _GEN_57; // @[RegisterFile.scala 36:{22,22}]
  wire [31:0] _GEN_59 = 5'h1b == io_rs2 ? regs_27 : _GEN_58; // @[RegisterFile.scala 36:{22,22}]
  wire [31:0] _GEN_60 = 5'h1c == io_rs2 ? regs_28 : _GEN_59; // @[RegisterFile.scala 36:{22,22}]
  wire [31:0] _GEN_61 = 5'h1d == io_rs2 ? regs_29 : _GEN_60; // @[RegisterFile.scala 36:{22,22}]
  wire [31:0] _GEN_62 = 5'h1e == io_rs2 ? regs_30 : _GEN_61; // @[RegisterFile.scala 36:{22,22}]
  wire [31:0] _GEN_63 = 5'h1f == io_rs2 ? regs_31 : _GEN_62; // @[RegisterFile.scala 36:{22,22}]
  assign io_readData1 = _io_readData1_T ? 32'h0 : _GEN_31; // @[RegisterFile.scala 31:22]
  assign io_readData2 = _io_readData2_T ? 32'h0 : _GEN_63; // @[RegisterFile.scala 36:22]
  assign io_debugRead_1 = regs_1; // @[RegisterFile.scala 45:27]
  assign io_debugRead_2 = regs_2; // @[RegisterFile.scala 45:27]
  assign io_debugRead_3 = regs_3; // @[RegisterFile.scala 45:27]
  assign io_debugRead_4 = regs_4; // @[RegisterFile.scala 45:27]
  always @(posedge clock) begin
    if (reset) begin // @[RegisterFile.scala 30:21]
      regs_0 <= 32'h0; // @[RegisterFile.scala 30:21]
    end else if (io_writeEnable & io_rd != 5'h0) begin // @[RegisterFile.scala 41:41]
      if (5'h0 == io_rd) begin // @[RegisterFile.scala 42:17]
        regs_0 <= io_writeData; // @[RegisterFile.scala 42:17]
      end
    end
    if (reset) begin // @[RegisterFile.scala 30:21]
      regs_1 <= 32'ha; // @[RegisterFile.scala 30:21]
    end else if (io_writeEnable & io_rd != 5'h0) begin // @[RegisterFile.scala 41:41]
      if (5'h1 == io_rd) begin // @[RegisterFile.scala 42:17]
        regs_1 <= io_writeData; // @[RegisterFile.scala 42:17]
      end
    end
    if (reset) begin // @[RegisterFile.scala 30:21]
      regs_2 <= 32'h3; // @[RegisterFile.scala 30:21]
    end else if (io_writeEnable & io_rd != 5'h0) begin // @[RegisterFile.scala 41:41]
      if (5'h2 == io_rd) begin // @[RegisterFile.scala 42:17]
        regs_2 <= io_writeData; // @[RegisterFile.scala 42:17]
      end
    end
    if (reset) begin // @[RegisterFile.scala 30:21]
      regs_3 <= 32'h0; // @[RegisterFile.scala 30:21]
    end else if (io_writeEnable & io_rd != 5'h0) begin // @[RegisterFile.scala 41:41]
      if (5'h3 == io_rd) begin // @[RegisterFile.scala 42:17]
        regs_3 <= io_writeData; // @[RegisterFile.scala 42:17]
      end
    end
    if (reset) begin // @[RegisterFile.scala 30:21]
      regs_4 <= 32'h0; // @[RegisterFile.scala 30:21]
    end else if (io_writeEnable & io_rd != 5'h0) begin // @[RegisterFile.scala 41:41]
      if (5'h4 == io_rd) begin // @[RegisterFile.scala 42:17]
        regs_4 <= io_writeData; // @[RegisterFile.scala 42:17]
      end
    end
    if (reset) begin // @[RegisterFile.scala 30:21]
      regs_5 <= 32'h0; // @[RegisterFile.scala 30:21]
    end else if (io_writeEnable & io_rd != 5'h0) begin // @[RegisterFile.scala 41:41]
      if (5'h5 == io_rd) begin // @[RegisterFile.scala 42:17]
        regs_5 <= io_writeData; // @[RegisterFile.scala 42:17]
      end
    end
    if (reset) begin // @[RegisterFile.scala 30:21]
      regs_6 <= 32'h0; // @[RegisterFile.scala 30:21]
    end else if (io_writeEnable & io_rd != 5'h0) begin // @[RegisterFile.scala 41:41]
      if (5'h6 == io_rd) begin // @[RegisterFile.scala 42:17]
        regs_6 <= io_writeData; // @[RegisterFile.scala 42:17]
      end
    end
    if (reset) begin // @[RegisterFile.scala 30:21]
      regs_7 <= 32'h0; // @[RegisterFile.scala 30:21]
    end else if (io_writeEnable & io_rd != 5'h0) begin // @[RegisterFile.scala 41:41]
      if (5'h7 == io_rd) begin // @[RegisterFile.scala 42:17]
        regs_7 <= io_writeData; // @[RegisterFile.scala 42:17]
      end
    end
    if (reset) begin // @[RegisterFile.scala 30:21]
      regs_8 <= 32'h0; // @[RegisterFile.scala 30:21]
    end else if (io_writeEnable & io_rd != 5'h0) begin // @[RegisterFile.scala 41:41]
      if (5'h8 == io_rd) begin // @[RegisterFile.scala 42:17]
        regs_8 <= io_writeData; // @[RegisterFile.scala 42:17]
      end
    end
    if (reset) begin // @[RegisterFile.scala 30:21]
      regs_9 <= 32'h0; // @[RegisterFile.scala 30:21]
    end else if (io_writeEnable & io_rd != 5'h0) begin // @[RegisterFile.scala 41:41]
      if (5'h9 == io_rd) begin // @[RegisterFile.scala 42:17]
        regs_9 <= io_writeData; // @[RegisterFile.scala 42:17]
      end
    end
    if (reset) begin // @[RegisterFile.scala 30:21]
      regs_10 <= 32'h0; // @[RegisterFile.scala 30:21]
    end else if (io_writeEnable & io_rd != 5'h0) begin // @[RegisterFile.scala 41:41]
      if (5'ha == io_rd) begin // @[RegisterFile.scala 42:17]
        regs_10 <= io_writeData; // @[RegisterFile.scala 42:17]
      end
    end
    if (reset) begin // @[RegisterFile.scala 30:21]
      regs_11 <= 32'h0; // @[RegisterFile.scala 30:21]
    end else if (io_writeEnable & io_rd != 5'h0) begin // @[RegisterFile.scala 41:41]
      if (5'hb == io_rd) begin // @[RegisterFile.scala 42:17]
        regs_11 <= io_writeData; // @[RegisterFile.scala 42:17]
      end
    end
    if (reset) begin // @[RegisterFile.scala 30:21]
      regs_12 <= 32'h0; // @[RegisterFile.scala 30:21]
    end else if (io_writeEnable & io_rd != 5'h0) begin // @[RegisterFile.scala 41:41]
      if (5'hc == io_rd) begin // @[RegisterFile.scala 42:17]
        regs_12 <= io_writeData; // @[RegisterFile.scala 42:17]
      end
    end
    if (reset) begin // @[RegisterFile.scala 30:21]
      regs_13 <= 32'h0; // @[RegisterFile.scala 30:21]
    end else if (io_writeEnable & io_rd != 5'h0) begin // @[RegisterFile.scala 41:41]
      if (5'hd == io_rd) begin // @[RegisterFile.scala 42:17]
        regs_13 <= io_writeData; // @[RegisterFile.scala 42:17]
      end
    end
    if (reset) begin // @[RegisterFile.scala 30:21]
      regs_14 <= 32'h0; // @[RegisterFile.scala 30:21]
    end else if (io_writeEnable & io_rd != 5'h0) begin // @[RegisterFile.scala 41:41]
      if (5'he == io_rd) begin // @[RegisterFile.scala 42:17]
        regs_14 <= io_writeData; // @[RegisterFile.scala 42:17]
      end
    end
    if (reset) begin // @[RegisterFile.scala 30:21]
      regs_15 <= 32'h0; // @[RegisterFile.scala 30:21]
    end else if (io_writeEnable & io_rd != 5'h0) begin // @[RegisterFile.scala 41:41]
      if (5'hf == io_rd) begin // @[RegisterFile.scala 42:17]
        regs_15 <= io_writeData; // @[RegisterFile.scala 42:17]
      end
    end
    if (reset) begin // @[RegisterFile.scala 30:21]
      regs_16 <= 32'h0; // @[RegisterFile.scala 30:21]
    end else if (io_writeEnable & io_rd != 5'h0) begin // @[RegisterFile.scala 41:41]
      if (5'h10 == io_rd) begin // @[RegisterFile.scala 42:17]
        regs_16 <= io_writeData; // @[RegisterFile.scala 42:17]
      end
    end
    if (reset) begin // @[RegisterFile.scala 30:21]
      regs_17 <= 32'h0; // @[RegisterFile.scala 30:21]
    end else if (io_writeEnable & io_rd != 5'h0) begin // @[RegisterFile.scala 41:41]
      if (5'h11 == io_rd) begin // @[RegisterFile.scala 42:17]
        regs_17 <= io_writeData; // @[RegisterFile.scala 42:17]
      end
    end
    if (reset) begin // @[RegisterFile.scala 30:21]
      regs_18 <= 32'h0; // @[RegisterFile.scala 30:21]
    end else if (io_writeEnable & io_rd != 5'h0) begin // @[RegisterFile.scala 41:41]
      if (5'h12 == io_rd) begin // @[RegisterFile.scala 42:17]
        regs_18 <= io_writeData; // @[RegisterFile.scala 42:17]
      end
    end
    if (reset) begin // @[RegisterFile.scala 30:21]
      regs_19 <= 32'h0; // @[RegisterFile.scala 30:21]
    end else if (io_writeEnable & io_rd != 5'h0) begin // @[RegisterFile.scala 41:41]
      if (5'h13 == io_rd) begin // @[RegisterFile.scala 42:17]
        regs_19 <= io_writeData; // @[RegisterFile.scala 42:17]
      end
    end
    if (reset) begin // @[RegisterFile.scala 30:21]
      regs_20 <= 32'h0; // @[RegisterFile.scala 30:21]
    end else if (io_writeEnable & io_rd != 5'h0) begin // @[RegisterFile.scala 41:41]
      if (5'h14 == io_rd) begin // @[RegisterFile.scala 42:17]
        regs_20 <= io_writeData; // @[RegisterFile.scala 42:17]
      end
    end
    if (reset) begin // @[RegisterFile.scala 30:21]
      regs_21 <= 32'h0; // @[RegisterFile.scala 30:21]
    end else if (io_writeEnable & io_rd != 5'h0) begin // @[RegisterFile.scala 41:41]
      if (5'h15 == io_rd) begin // @[RegisterFile.scala 42:17]
        regs_21 <= io_writeData; // @[RegisterFile.scala 42:17]
      end
    end
    if (reset) begin // @[RegisterFile.scala 30:21]
      regs_22 <= 32'h0; // @[RegisterFile.scala 30:21]
    end else if (io_writeEnable & io_rd != 5'h0) begin // @[RegisterFile.scala 41:41]
      if (5'h16 == io_rd) begin // @[RegisterFile.scala 42:17]
        regs_22 <= io_writeData; // @[RegisterFile.scala 42:17]
      end
    end
    if (reset) begin // @[RegisterFile.scala 30:21]
      regs_23 <= 32'h0; // @[RegisterFile.scala 30:21]
    end else if (io_writeEnable & io_rd != 5'h0) begin // @[RegisterFile.scala 41:41]
      if (5'h17 == io_rd) begin // @[RegisterFile.scala 42:17]
        regs_23 <= io_writeData; // @[RegisterFile.scala 42:17]
      end
    end
    if (reset) begin // @[RegisterFile.scala 30:21]
      regs_24 <= 32'h0; // @[RegisterFile.scala 30:21]
    end else if (io_writeEnable & io_rd != 5'h0) begin // @[RegisterFile.scala 41:41]
      if (5'h18 == io_rd) begin // @[RegisterFile.scala 42:17]
        regs_24 <= io_writeData; // @[RegisterFile.scala 42:17]
      end
    end
    if (reset) begin // @[RegisterFile.scala 30:21]
      regs_25 <= 32'h0; // @[RegisterFile.scala 30:21]
    end else if (io_writeEnable & io_rd != 5'h0) begin // @[RegisterFile.scala 41:41]
      if (5'h19 == io_rd) begin // @[RegisterFile.scala 42:17]
        regs_25 <= io_writeData; // @[RegisterFile.scala 42:17]
      end
    end
    if (reset) begin // @[RegisterFile.scala 30:21]
      regs_26 <= 32'h0; // @[RegisterFile.scala 30:21]
    end else if (io_writeEnable & io_rd != 5'h0) begin // @[RegisterFile.scala 41:41]
      if (5'h1a == io_rd) begin // @[RegisterFile.scala 42:17]
        regs_26 <= io_writeData; // @[RegisterFile.scala 42:17]
      end
    end
    if (reset) begin // @[RegisterFile.scala 30:21]
      regs_27 <= 32'h0; // @[RegisterFile.scala 30:21]
    end else if (io_writeEnable & io_rd != 5'h0) begin // @[RegisterFile.scala 41:41]
      if (5'h1b == io_rd) begin // @[RegisterFile.scala 42:17]
        regs_27 <= io_writeData; // @[RegisterFile.scala 42:17]
      end
    end
    if (reset) begin // @[RegisterFile.scala 30:21]
      regs_28 <= 32'h0; // @[RegisterFile.scala 30:21]
    end else if (io_writeEnable & io_rd != 5'h0) begin // @[RegisterFile.scala 41:41]
      if (5'h1c == io_rd) begin // @[RegisterFile.scala 42:17]
        regs_28 <= io_writeData; // @[RegisterFile.scala 42:17]
      end
    end
    if (reset) begin // @[RegisterFile.scala 30:21]
      regs_29 <= 32'h0; // @[RegisterFile.scala 30:21]
    end else if (io_writeEnable & io_rd != 5'h0) begin // @[RegisterFile.scala 41:41]
      if (5'h1d == io_rd) begin // @[RegisterFile.scala 42:17]
        regs_29 <= io_writeData; // @[RegisterFile.scala 42:17]
      end
    end
    if (reset) begin // @[RegisterFile.scala 30:21]
      regs_30 <= 32'h0; // @[RegisterFile.scala 30:21]
    end else if (io_writeEnable & io_rd != 5'h0) begin // @[RegisterFile.scala 41:41]
      if (5'h1e == io_rd) begin // @[RegisterFile.scala 42:17]
        regs_30 <= io_writeData; // @[RegisterFile.scala 42:17]
      end
    end
    if (reset) begin // @[RegisterFile.scala 30:21]
      regs_31 <= 32'h0; // @[RegisterFile.scala 30:21]
    end else if (io_writeEnable & io_rd != 5'h0) begin // @[RegisterFile.scala 41:41]
      if (5'h1f == io_rd) begin // @[RegisterFile.scala 42:17]
        regs_31 <= io_writeData; // @[RegisterFile.scala 42:17]
      end
    end
  end
// Register and memory initialization
`ifdef RANDOMIZE_GARBAGE_ASSIGN
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_INVALID_ASSIGN
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_REG_INIT
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_MEM_INIT
`define RANDOMIZE
`endif
`ifndef RANDOM
`define RANDOM $random
`endif
`ifdef RANDOMIZE_MEM_INIT
  integer initvar;
`endif
`ifndef SYNTHESIS
`ifdef FIRRTL_BEFORE_INITIAL
`FIRRTL_BEFORE_INITIAL
`endif
initial begin
  `ifdef RANDOMIZE
    `ifdef INIT_RANDOM
      `INIT_RANDOM
    `endif
    `ifndef VERILATOR
      `ifdef RANDOMIZE_DELAY
        #`RANDOMIZE_DELAY begin end
      `else
        #0.002 begin end
      `endif
    `endif
`ifdef RANDOMIZE_REG_INIT
  _RAND_0 = {1{`RANDOM}};
  regs_0 = _RAND_0[31:0];
  _RAND_1 = {1{`RANDOM}};
  regs_1 = _RAND_1[31:0];
  _RAND_2 = {1{`RANDOM}};
  regs_2 = _RAND_2[31:0];
  _RAND_3 = {1{`RANDOM}};
  regs_3 = _RAND_3[31:0];
  _RAND_4 = {1{`RANDOM}};
  regs_4 = _RAND_4[31:0];
  _RAND_5 = {1{`RANDOM}};
  regs_5 = _RAND_5[31:0];
  _RAND_6 = {1{`RANDOM}};
  regs_6 = _RAND_6[31:0];
  _RAND_7 = {1{`RANDOM}};
  regs_7 = _RAND_7[31:0];
  _RAND_8 = {1{`RANDOM}};
  regs_8 = _RAND_8[31:0];
  _RAND_9 = {1{`RANDOM}};
  regs_9 = _RAND_9[31:0];
  _RAND_10 = {1{`RANDOM}};
  regs_10 = _RAND_10[31:0];
  _RAND_11 = {1{`RANDOM}};
  regs_11 = _RAND_11[31:0];
  _RAND_12 = {1{`RANDOM}};
  regs_12 = _RAND_12[31:0];
  _RAND_13 = {1{`RANDOM}};
  regs_13 = _RAND_13[31:0];
  _RAND_14 = {1{`RANDOM}};
  regs_14 = _RAND_14[31:0];
  _RAND_15 = {1{`RANDOM}};
  regs_15 = _RAND_15[31:0];
  _RAND_16 = {1{`RANDOM}};
  regs_16 = _RAND_16[31:0];
  _RAND_17 = {1{`RANDOM}};
  regs_17 = _RAND_17[31:0];
  _RAND_18 = {1{`RANDOM}};
  regs_18 = _RAND_18[31:0];
  _RAND_19 = {1{`RANDOM}};
  regs_19 = _RAND_19[31:0];
  _RAND_20 = {1{`RANDOM}};
  regs_20 = _RAND_20[31:0];
  _RAND_21 = {1{`RANDOM}};
  regs_21 = _RAND_21[31:0];
  _RAND_22 = {1{`RANDOM}};
  regs_22 = _RAND_22[31:0];
  _RAND_23 = {1{`RANDOM}};
  regs_23 = _RAND_23[31:0];
  _RAND_24 = {1{`RANDOM}};
  regs_24 = _RAND_24[31:0];
  _RAND_25 = {1{`RANDOM}};
  regs_25 = _RAND_25[31:0];
  _RAND_26 = {1{`RANDOM}};
  regs_26 = _RAND_26[31:0];
  _RAND_27 = {1{`RANDOM}};
  regs_27 = _RAND_27[31:0];
  _RAND_28 = {1{`RANDOM}};
  regs_28 = _RAND_28[31:0];
  _RAND_29 = {1{`RANDOM}};
  regs_29 = _RAND_29[31:0];
  _RAND_30 = {1{`RANDOM}};
  regs_30 = _RAND_30[31:0];
  _RAND_31 = {1{`RANDOM}};
  regs_31 = _RAND_31[31:0];
`endif // RANDOMIZE_REG_INIT
  `endif // RANDOMIZE
end // initial
`ifdef FIRRTL_AFTER_INITIAL
`FIRRTL_AFTER_INITIAL
`endif
`endif // SYNTHESIS
endmodule
module ALU(
  input  [31:0] io_a,
  input  [31:0] io_b,
  input  [3:0]  io_op,
  output [31:0] io_y
);
  wire [4:0] shamt = io_b[4:0]; // @[ALU.scala 27:19]
  wire [31:0] _io_y_T_1 = io_a + io_b; // @[ALU.scala 29:25]
  wire [31:0] _io_y_T_3 = io_a - io_b; // @[ALU.scala 30:25]
  wire [62:0] _GEN_0 = {{31'd0}, io_a}; // @[ALU.scala 31:25]
  wire [62:0] _io_y_T_4 = _GEN_0 << shamt; // @[ALU.scala 31:25]
  wire  _io_y_T_7 = $signed(io_a) < $signed(io_b); // @[ALU.scala 32:32]
  wire  _io_y_T_8 = io_a < io_b; // @[ALU.scala 33:25]
  wire [31:0] _io_y_T_9 = io_a ^ io_b; // @[ALU.scala 34:25]
  wire [31:0] _io_y_T_10 = io_a >> shamt; // @[ALU.scala 35:25]
  wire [31:0] _io_y_T_13 = $signed(io_a) >>> shamt; // @[ALU.scala 36:42]
  wire [31:0] _io_y_T_14 = io_a | io_b; // @[ALU.scala 37:25]
  wire [31:0] _io_y_T_15 = io_a & io_b; // @[ALU.scala 38:25]
  wire [31:0] _io_y_T_17 = 4'h0 == io_op ? _io_y_T_1 : 32'h0; // @[Mux.scala 81:58]
  wire [31:0] _io_y_T_19 = 4'h1 == io_op ? _io_y_T_3 : _io_y_T_17; // @[Mux.scala 81:58]
  wire [62:0] _io_y_T_21 = 4'h2 == io_op ? _io_y_T_4 : {{31'd0}, _io_y_T_19}; // @[Mux.scala 81:58]
  wire [62:0] _io_y_T_23 = 4'h3 == io_op ? {{62'd0}, _io_y_T_7} : _io_y_T_21; // @[Mux.scala 81:58]
  wire [62:0] _io_y_T_25 = 4'h4 == io_op ? {{62'd0}, _io_y_T_8} : _io_y_T_23; // @[Mux.scala 81:58]
  wire [62:0] _io_y_T_27 = 4'h5 == io_op ? {{31'd0}, _io_y_T_9} : _io_y_T_25; // @[Mux.scala 81:58]
  wire [62:0] _io_y_T_29 = 4'h6 == io_op ? {{31'd0}, _io_y_T_10} : _io_y_T_27; // @[Mux.scala 81:58]
  wire [62:0] _io_y_T_31 = 4'h7 == io_op ? {{31'd0}, _io_y_T_13} : _io_y_T_29; // @[Mux.scala 81:58]
  wire [62:0] _io_y_T_33 = 4'h8 == io_op ? {{31'd0}, _io_y_T_14} : _io_y_T_31; // @[Mux.scala 81:58]
  wire [62:0] _io_y_T_35 = 4'h9 == io_op ? {{31'd0}, _io_y_T_15} : _io_y_T_33; // @[Mux.scala 81:58]
  assign io_y = _io_y_T_35[31:0]; // @[ALU.scala 28:8]
endmodule
module RV32IRCore(
  input         clock,
  input         reset,
  output [31:0] io_imemAddr,
  input  [31:0] io_imemInst,
  output [31:0] io_debugPC,
  output [31:0] io_debugX1,
  output [31:0] io_debugX2,
  output [31:0] io_debugX3,
  output [31:0] io_debugX4
);
`ifdef RANDOMIZE_REG_INIT
  reg [31:0] _RAND_0;
`endif // RANDOMIZE_REG_INIT
  wire [31:0] decoder_io_instr; // @[RV32IRCore.scala 29:23]
  wire [4:0] decoder_io_rs1; // @[RV32IRCore.scala 29:23]
  wire [4:0] decoder_io_rs2; // @[RV32IRCore.scala 29:23]
  wire [4:0] decoder_io_rd; // @[RV32IRCore.scala 29:23]
  wire [3:0] decoder_io_aluOp; // @[RV32IRCore.scala 29:23]
  wire  decoder_io_regWrite; // @[RV32IRCore.scala 29:23]
  wire  decoder_io_valid; // @[RV32IRCore.scala 29:23]
  wire  regFile_clock; // @[RV32IRCore.scala 34:23]
  wire  regFile_reset; // @[RV32IRCore.scala 34:23]
  wire [4:0] regFile_io_rs1; // @[RV32IRCore.scala 34:23]
  wire [4:0] regFile_io_rs2; // @[RV32IRCore.scala 34:23]
  wire [4:0] regFile_io_rd; // @[RV32IRCore.scala 34:23]
  wire [31:0] regFile_io_writeData; // @[RV32IRCore.scala 34:23]
  wire  regFile_io_writeEnable; // @[RV32IRCore.scala 34:23]
  wire [31:0] regFile_io_readData1; // @[RV32IRCore.scala 34:23]
  wire [31:0] regFile_io_readData2; // @[RV32IRCore.scala 34:23]
  wire [31:0] regFile_io_debugRead_1; // @[RV32IRCore.scala 34:23]
  wire [31:0] regFile_io_debugRead_2; // @[RV32IRCore.scala 34:23]
  wire [31:0] regFile_io_debugRead_3; // @[RV32IRCore.scala 34:23]
  wire [31:0] regFile_io_debugRead_4; // @[RV32IRCore.scala 34:23]
  wire [31:0] alu_io_a; // @[RV32IRCore.scala 42:19]
  wire [31:0] alu_io_b; // @[RV32IRCore.scala 42:19]
  wire [3:0] alu_io_op; // @[RV32IRCore.scala 42:19]
  wire [31:0] alu_io_y; // @[RV32IRCore.scala 42:19]
  reg [31:0] pc; // @[RV32IRCore.scala 23:19]
  wire [31:0] _pc_T_1 = pc + 32'h4; // @[RV32IRCore.scala 55:12]
  Decoder decoder ( // @[RV32IRCore.scala 29:23]
    .io_instr(decoder_io_instr),
    .io_rs1(decoder_io_rs1),
    .io_rs2(decoder_io_rs2),
    .io_rd(decoder_io_rd),
    .io_aluOp(decoder_io_aluOp),
    .io_regWrite(decoder_io_regWrite),
    .io_valid(decoder_io_valid)
  );
  RegisterFile regFile ( // @[RV32IRCore.scala 34:23]
    .clock(regFile_clock),
    .reset(regFile_reset),
    .io_rs1(regFile_io_rs1),
    .io_rs2(regFile_io_rs2),
    .io_rd(regFile_io_rd),
    .io_writeData(regFile_io_writeData),
    .io_writeEnable(regFile_io_writeEnable),
    .io_readData1(regFile_io_readData1),
    .io_readData2(regFile_io_readData2),
    .io_debugRead_1(regFile_io_debugRead_1),
    .io_debugRead_2(regFile_io_debugRead_2),
    .io_debugRead_3(regFile_io_debugRead_3),
    .io_debugRead_4(regFile_io_debugRead_4)
  );
  ALU alu ( // @[RV32IRCore.scala 42:19]
    .io_a(alu_io_a),
    .io_b(alu_io_b),
    .io_op(alu_io_op),
    .io_y(alu_io_y)
  );
  assign io_imemAddr = pc; // @[RV32IRCore.scala 24:15]
  assign io_debugPC = pc; // @[RV32IRCore.scala 25:14]
  assign io_debugX1 = regFile_io_debugRead_1; // @[RV32IRCore.scala 59:14]
  assign io_debugX2 = regFile_io_debugRead_2; // @[RV32IRCore.scala 60:14]
  assign io_debugX3 = regFile_io_debugRead_3; // @[RV32IRCore.scala 61:14]
  assign io_debugX4 = regFile_io_debugRead_4; // @[RV32IRCore.scala 62:14]
  assign decoder_io_instr = io_imemInst; // @[RV32IRCore.scala 30:20]
  assign regFile_clock = clock;
  assign regFile_reset = reset;
  assign regFile_io_rs1 = decoder_io_rs1; // @[RV32IRCore.scala 37:18]
  assign regFile_io_rs2 = decoder_io_rs2; // @[RV32IRCore.scala 38:18]
  assign regFile_io_rd = decoder_io_rd; // @[RV32IRCore.scala 49:17]
  assign regFile_io_writeData = alu_io_y; // @[RV32IRCore.scala 50:24]
  assign regFile_io_writeEnable = decoder_io_regWrite; // @[RV32IRCore.scala 51:26]
  assign alu_io_a = regFile_io_readData1; // @[RV32IRCore.scala 43:12]
  assign alu_io_b = regFile_io_readData2; // @[RV32IRCore.scala 44:12]
  assign alu_io_op = decoder_io_aluOp; // @[RV32IRCore.scala 45:13]
  always @(posedge clock) begin
    if (reset) begin // @[RV32IRCore.scala 23:19]
      pc <= 32'h0; // @[RV32IRCore.scala 23:19]
    end else begin
      pc <= _pc_T_1; // @[RV32IRCore.scala 55:6]
    end
  end
// Register and memory initialization
`ifdef RANDOMIZE_GARBAGE_ASSIGN
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_INVALID_ASSIGN
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_REG_INIT
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_MEM_INIT
`define RANDOMIZE
`endif
`ifndef RANDOM
`define RANDOM $random
`endif
`ifdef RANDOMIZE_MEM_INIT
  integer initvar;
`endif
`ifndef SYNTHESIS
`ifdef FIRRTL_BEFORE_INITIAL
`FIRRTL_BEFORE_INITIAL
`endif
initial begin
  `ifdef RANDOMIZE
    `ifdef INIT_RANDOM
      `INIT_RANDOM
    `endif
    `ifndef VERILATOR
      `ifdef RANDOMIZE_DELAY
        #`RANDOMIZE_DELAY begin end
      `else
        #0.002 begin end
      `endif
    `endif
`ifdef RANDOMIZE_REG_INIT
  _RAND_0 = {1{`RANDOM}};
  pc = _RAND_0[31:0];
`endif // RANDOMIZE_REG_INIT
  `endif // RANDOMIZE
end // initial
`ifdef FIRRTL_AFTER_INITIAL
`FIRRTL_AFTER_INITIAL
`endif
`endif // SYNTHESIS
endmodule
