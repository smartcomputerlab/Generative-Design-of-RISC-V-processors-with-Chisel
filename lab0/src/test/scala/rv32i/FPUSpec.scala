package rv32.fpu
import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import scala.util.Random

/** Tests for the RV32F FPU, with VCD waveform dumps.
  *
  * Run:            sbt test
  * One test only:  sbt "testOnly rv32.fpu.FPUSpec -- -z directed"
  *
  * Waveforms land in:  test_run_dir/<test_name>/FPU.vcd
  * Open with GTKWave:  gtkwave test_run_dir/<test_name>/FPU.vcd
  *
  * Random runs are kept short on purpose: a VCD of 1000s of 30-cycle divisions gets huge.
  * Raise -DfpuIters=N to run longer (e.g. sbt -DfpuIters=400 test).
  */
class FPUSpec extends AnyFlatSpec with ChiselScalatestTester {

  // VCD on for every test in this file
  private val vcd = Seq(WriteVcdAnnotation)
  private val iters = sys.props.getOrElse("fpuIters", "25").toInt

  // ----------------------------------------------------------------- helpers
  def bits(f: Float): BigInt = BigInt(java.lang.Float.floatToRawIntBits(f).toLong & 0xffffffffL)
  def flt(b: BigInt): Float  = java.lang.Float.intBitsToFloat(b.toInt)
  def expected(f: Float): BigInt = if (f.isNaN) BigInt(0x7fc00000L) else bits(f)

  // mix of fully random, subnormal/zero and "reasonable exponent" operands
  val rnd = new Random(1)
  def rndBits(): BigInt = rnd.nextInt(4) match {
    case 0 => BigInt(rnd.nextInt().toLong & 0xffffffffL)
    case 1 => BigInt((rnd.nextInt() & 0x807fffff).toLong & 0xffffffffL)
    case _ => BigInt(((rnd.nextInt() & 0x80000000) | ((110 + rnd.nextInt(34)) << 23) |
                      (rnd.nextInt() & 0x7fffff)).toLong & 0xffffffffL)
  }

  /** Issue one request, wait for the response pulse, return (data, fflags). */
  def run(d: FPU, op: UInt, a: BigInt, b: BigInt = 0, c: BigInt = 0, rm: Int = 0): (BigInt, Int) = {
    d.io.req.bits.op.poke(op); d.io.req.bits.rm.poke(rm.U)
    val mask = BigInt(0xffffffffL)   // two's-complement view: BigInt(-5) -> 0xfffffffb
    d.io.req.bits.a.poke((a & mask).U); d.io.req.bits.b.poke((b & mask).U); d.io.req.bits.c.poke((c & mask).U)
    d.io.req.valid.poke(true.B)
    d.clock.step()
    d.io.req.valid.poke(false.B)
    var n = 0
    while (!d.io.resp.valid.peek().litToBoolean) {
      d.clock.step(); n += 1
      assert(n < 200, "timeout waiting for response")
    }
    val r = (d.io.resp.bits.data.peek().litValue, d.io.resp.bits.flags.peek().litValue.toInt)
    d.clock.step()   // one idle cycle between operations: makes the waveform easier to read
    r
  }

  // ------------------------------------------------------------------- tests

  // Short, hand-picked sequence: ideal for looking at waveforms in GTKWave.
  "FPU" should "run a directed sequence (waveform: directed)" in {
    test(new FPU).withAnnotations(vcd) { d =>
      d.clock.setTimeout(0)
      assert(run(d, FPOp.FADD,  bits(1.5f),  bits(2.25f))._1 == bits(3.75f))
      assert(run(d, FPOp.FSUB,  bits(1f),    bits(1f))._1    == bits(0f))
      assert(run(d, FPOp.FMUL,  bits(3f),    bits(-4f))._1   == bits(-12f))
      assert(run(d, FPOp.FMADD, bits(2f),    bits(3f), bits(1f))._1 == bits(7f))
      assert(run(d, FPOp.FDIV,  bits(1f),    bits(3f))._1    == bits(1f / 3f))
      assert(run(d, FPOp.FSQRT, bits(2f))._1 == bits(Math.sqrt(2.0).toFloat))
      assert(run(d, FPOp.FDIV,  bits(1f),    bits(0f))._2    == 0x08)   // DZ
      assert(run(d, FPOp.FSQRT, bits(-1f))._2                == 0x10)   // NV
    }
  }

  it should "match JVM IEEE-754 (RNE) for add/sub/mul/div/sqrt/fma (waveform: random_arith)" in {
    test(new FPU).withAnnotations(vcd) { d =>
      d.clock.setTimeout(0)
      for (_ <- 0 until iters) {
        val a = rndBits(); val b = rndBits(); val c = rndBits()
        val (fa, fb, fc) = (flt(a), flt(b), flt(c))
        assert(run(d, FPOp.FADD,  a, b)._1    == expected(fa + fb),  s"fadd  $a $b")
        assert(run(d, FPOp.FSUB,  a, b)._1    == expected(fa - fb),  s"fsub  $a $b")
        assert(run(d, FPOp.FMUL,  a, b)._1    == expected(fa * fb),  s"fmul  $a $b")
        assert(run(d, FPOp.FDIV,  a, b)._1    == expected(fa / fb),  s"fdiv  $a $b")
        assert(run(d, FPOp.FSQRT, a)._1       == expected(Math.sqrt(fa.toDouble).toFloat), s"fsqrt $a")
        assert(run(d, FPOp.FMADD, a, b, c)._1 == expected(Math.fma(fa, fb, fc)), s"fmadd $a $b $c")
      }
    }
  }

  it should "round correctly in all five modes (waveform: rounding_modes)" in {
    test(new FPU).withAnnotations(vcd) { d =>
      d.clock.setTimeout(0)
      val one = bits(1f)
      val tiny = bits(java.lang.Float.intBitsToFloat(0x33800000)) // 2^-24: exactly half an ulp of 1.0
      val up   = bits(java.lang.Float.intBitsToFloat(0x3f800001)) // 1.0 + 1ulp
      assert(run(d, FPOp.FADD, one, tiny, rm = 0)._1 == one)   // RNE: tie -> even
      assert(run(d, FPOp.FADD, one, tiny, rm = 1)._1 == one)   // RTZ
      assert(run(d, FPOp.FADD, one, tiny, rm = 2)._1 == one)   // RDN
      assert(run(d, FPOp.FADD, one, tiny, rm = 3)._1 == up)    // RUP
      assert(run(d, FPOp.FADD, one, tiny, rm = 4)._1 == up)    // RMM: tie -> away
      // overflow behaviour per mode
      val big = bits(Float.MaxValue)
      assert(run(d, FPOp.FADD, big, big, rm = 0)._1 == bits(Float.PositiveInfinity))
      assert(run(d, FPOp.FADD, big, big, rm = 1)._1 == big)    // RTZ -> max finite
      assert(run(d, FPOp.FADD, big, big, rm = 0)._2 == 0x05)   // OF | NX
    }
  }

  it should "convert, compare and classify correctly (waveform: convert_compare)" in {
    test(new FPU).withAnnotations(vcd) { d =>
      d.clock.setTimeout(0)
      assert(run(d, FPOp.FCVT_S_W,  BigInt(-5))._1               == bits(-5f))
      assert(run(d, FPOp.FCVT_S_WU, BigInt(0xffffffffL))._1      == bits(4294967296f))
      assert(run(d, FPOp.FCVT_W_S,  bits(-2.5f), rm = 0)._1      == BigInt(0xfffffffeL)) // RNE -> -2
      assert(run(d, FPOp.FCVT_W_S,  bits(2.5f),  rm = 1)._1      == BigInt(2))           // RTZ
      assert(run(d, FPOp.FCVT_W_S,  bits(1e20f))._1              == BigInt(0x7fffffffL)) // saturate
      assert(run(d, FPOp.FCVT_WU_S, bits(-1f))._2                == 0x10)                // NV
      assert(run(d, FPOp.FLT,   bits(1f), bits(2f))._1           == BigInt(1))
      assert(run(d, FPOp.FLE,   bits(2f), bits(2f))._1           == BigInt(1))
      assert(run(d, FPOp.FEQ,   bits(0f), bits(-0f))._1          == BigInt(1))
      assert(run(d, FPOp.FEQ,   BigInt(0x7fc00000L), bits(1f))._1 == BigInt(0))
      assert(run(d, FPOp.FMIN,  bits(-0f), bits(0f))._1          == bits(-0f))
      assert(run(d, FPOp.FMAX,  bits(-0f), bits(0f))._1          == bits(0f))
      assert(run(d, FPOp.FCLASS, bits(Float.NegativeInfinity))._1 == BigInt(1))
      assert(run(d, FPOp.FCLASS, bits(0f))._1                    == BigInt(0x10))
      assert(run(d, FPOp.FSGNJN, bits(1f), bits(1f))._1          == bits(-1f))
    }
  }
}

