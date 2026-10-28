`timescale 1ns / 1ps

module FsmCounter_tb;

    // ----------------------------------------
    // Testbench signals
    // ----------------------------------------
    reg clock;
    reg reset;
    reg io_start;

    wire [5:0] io_count;
    wire       io_busy;
    wire       io_done;

    integer i;

    // ----------------------------------------
    // Device Under Test (DUT)
    // ----------------------------------------
    FsmCounter dut (
        .clock    (clock),
        .reset    (reset),
        .io_start (io_start),
        .io_count (io_count),
        .io_busy  (io_busy),
        .io_done  (io_done)
    );

    // ----------------------------------------
    // Clock generation
    // Period = 10 ns (100 MHz)
    // ----------------------------------------
    initial begin
        clock = 1'b0;
        forever #5 clock = ~clock;
    end

    // ----------------------------------------
    // VCD waveform generation
    // ----------------------------------------
    initial begin
        $dumpfile("FsmCounter.vcd");
        $dumpvars(0, FsmCounter_tb);
    end

    // ----------------------------------------
    // Test sequence
    // ----------------------------------------
    initial begin

        // Initialize inputs
        reset    = 1'b1;
        io_start = 1'b0;

        $display("======================================");
        $display("       FSM COUNTER TESTBENCH");
        $display("======================================");

        // Apply reset for two clock cycles
        repeat (2) @(negedge clock);

        reset = 1'b0;

        // Verify initial IDLE state
        #1;
        if (io_count !== 6'd0 ||
            io_busy  !== 1'b0 ||
            io_done  !== 1'b0) begin
            $display("ERROR: Incorrect initial state");
            $fatal(1);
        end

        $display("Reset complete: FSM is in IDLE");

        // ----------------------------------------
        // Start FSM
        // ----------------------------------------
        @(negedge clock);
        io_start = 1'b1;

        @(negedge clock);
        io_start = 1'b0;

        $display("FSM started at time %0t", $time);

        // ----------------------------------------
        // RUN state: 32 counting cycles
        // ----------------------------------------
        for (i = 0; i < 32; i = i + 1) begin

            #1;

            if (io_count !== i ||
                io_busy  !== 1'b1 ||
                io_done  !== 1'b0) begin

                $display(
                    "ERROR at iteration %0d: count=%0d busy=%b done=%b",
                    i, io_count, io_busy, io_done
                );

                $fatal(1);
            end

            $display(
                "Time=%0t | Iteration=%0d | Count=%0d | Busy=%b | Done=%b",
                $time, i, io_count, io_busy, io_done
            );

            @(negedge clock);
        end

        // ----------------------------------------
        // Verify DONE state
        // ----------------------------------------
        #1;

        if (io_count !== 6'd32 ||
            io_busy  !== 1'b0 ||
            io_done  !== 1'b1) begin

            $display(
                "ERROR: DONE state incorrect: count=%0d busy=%b done=%b",
                io_count, io_busy, io_done
            );

            $fatal(1);
        end

        $display("FSM reached DONE at time %0t", $time);

        // ----------------------------------------
        // Verify return to IDLE
        // ----------------------------------------
        @(negedge clock);
        #1;

        if (io_busy !== 1'b0 ||
            io_done !== 1'b0) begin

            $display("ERROR: FSM did not return to IDLE");
            $fatal(1);
        end

        $display("FSM returned to IDLE");

        // ----------------------------------------
        // End simulation
        // ----------------------------------------
        $display("======================================");
        $display("          TEST PASSED");
        $display("======================================");

        $finish;
    end

    // ----------------------------------------
    // Simulation timeout
    // ----------------------------------------
    initial begin
        #1000;
        $display("ERROR: Simulation timeout");
        $fatal(1);
    end

endmodule

