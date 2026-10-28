module FsmCounter(
  input        clock,
  input        reset,
  input        io_start,
  output [5:0] io_count,
  output       io_busy,
  output       io_done
);
`ifdef RANDOMIZE_REG_INIT
  reg [31:0] _RAND_0;
  reg [31:0] _RAND_1;
`endif // RANDOMIZE_REG_INIT
  reg [1:0] state; // @[FsmCounter.scala 19:22]
  reg [5:0] counter; // @[FsmCounter.scala 22:24]
  wire [5:0] _counter_T_1 = counter + 6'h1; // @[FsmCounter.scala 35:26]
  assign io_count = counter; // @[FsmCounter.scala 48:12]
  assign io_busy = state == 2'h1; // @[FsmCounter.scala 49:21]
  assign io_done = state == 2'h2; // @[FsmCounter.scala 50:21]
  always @(posedge clock) begin
    if (reset) begin // @[FsmCounter.scala 19:22]
      state <= 2'h0; // @[FsmCounter.scala 19:22]
    end else if (2'h0 == state) begin // @[FsmCounter.scala 25:17]
      if (io_start) begin // @[FsmCounter.scala 28:22]
        state <= 2'h1; // @[FsmCounter.scala 30:15]
      end
    end else if (2'h1 == state) begin // @[FsmCounter.scala 25:17]
      if (counter == 6'h1f) begin // @[FsmCounter.scala 37:30]
        state <= 2'h2; // @[FsmCounter.scala 38:15]
      end
    end else if (2'h2 == state) begin // @[FsmCounter.scala 25:17]
      state <= 2'h0; // @[FsmCounter.scala 43:13]
    end
    if (reset) begin // @[FsmCounter.scala 22:24]
      counter <= 6'h0; // @[FsmCounter.scala 22:24]
    end else if (2'h0 == state) begin // @[FsmCounter.scala 25:17]
      if (io_start) begin // @[FsmCounter.scala 28:22]
        counter <= 6'h0; // @[FsmCounter.scala 29:17]
      end
    end else if (2'h1 == state) begin // @[FsmCounter.scala 25:17]
      counter <= _counter_T_1; // @[FsmCounter.scala 35:15]
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
  state = _RAND_0[1:0];
  _RAND_1 = {1{`RANDOM}};
  counter = _RAND_1[5:0];
`endif // RANDOMIZE_REG_INIT
  `endif // RANDOMIZE
end // initial
`ifdef FIRRTL_AFTER_INITIAL
`FIRRTL_AFTER_INITIAL
`endif
`endif // SYNTHESIS
endmodule
