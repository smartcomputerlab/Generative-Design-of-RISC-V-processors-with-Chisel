circuit FsmCounter :
  module FsmCounter :
    input clock : Clock
    input reset : UInt<1>
    output io : { flip start : UInt<1>, count : UInt<6>, busy : UInt<1>, done : UInt<1>}

    reg state : UInt<2>, clock with :
      reset => (reset, UInt<2>("h0")) @[FsmCounter.scala 19:22]
    reg counter : UInt<6>, clock with :
      reset => (reset, UInt<6>("h0")) @[FsmCounter.scala 22:24]
    node _T = eq(UInt<2>("h0"), state) @[FsmCounter.scala 25:17]
    when _T : @[FsmCounter.scala 25:17]
      when io.start : @[FsmCounter.scala 28:22]
        counter <= UInt<1>("h0") @[FsmCounter.scala 29:17]
        state <= UInt<2>("h1") @[FsmCounter.scala 30:15]
    else :
      node _T_1 = eq(UInt<2>("h1"), state) @[FsmCounter.scala 25:17]
      when _T_1 : @[FsmCounter.scala 25:17]
        node _counter_T = add(counter, UInt<1>("h1")) @[FsmCounter.scala 35:26]
        node _counter_T_1 = tail(_counter_T, 1) @[FsmCounter.scala 35:26]
        counter <= _counter_T_1 @[FsmCounter.scala 35:15]
        node _T_2 = eq(counter, UInt<5>("h1f")) @[FsmCounter.scala 37:20]
        when _T_2 : @[FsmCounter.scala 37:30]
          state <= UInt<2>("h2") @[FsmCounter.scala 38:15]
      else :
        node _T_3 = eq(UInt<2>("h2"), state) @[FsmCounter.scala 25:17]
        when _T_3 : @[FsmCounter.scala 25:17]
          state <= UInt<2>("h0") @[FsmCounter.scala 43:13]
    io.count <= counter @[FsmCounter.scala 48:12]
    node _io_busy_T = eq(state, UInt<2>("h1")) @[FsmCounter.scala 49:21]
    io.busy <= _io_busy_T @[FsmCounter.scala 49:12]
    node _io_done_T = eq(state, UInt<2>("h2")) @[FsmCounter.scala 50:21]
    io.done <= _io_done_T @[FsmCounter.scala 50:12]

