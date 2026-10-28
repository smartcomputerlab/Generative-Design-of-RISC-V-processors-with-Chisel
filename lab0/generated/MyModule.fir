circuit MyModule :
  module MyModule :
    input clock : Clock
    input reset : UInt<1>
    output io : { flip a : UInt<8>, flip b : UInt<8>, y : UInt<8>}

    node _io_y_T = add(io.a, io.b) @[MyModule.scala 12:16]
    node _io_y_T_1 = tail(_io_y_T, 1) @[MyModule.scala 12:16]
    io.y <= _io_y_T_1 @[MyModule.scala 12:8]

