module MyModule(
  input        clock,
  input        reset,
  input  [7:0] io_a,
  input  [7:0] io_b,
  output [7:0] io_y
);
  assign io_y = io_a + io_b; // @[MyModule.scala 12:16]
endmodule
