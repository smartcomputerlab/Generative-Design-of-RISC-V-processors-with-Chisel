 // RV32F single-precision FPU for a RV32IM core  ->  RV32IMF

// Chisel 3.5 / 3.6.  Bit-exact IEEE-754, all 5 RISC-V rounding modes,

// RISC-V canonical NaN, fflags (NV DZ OF UF NX), tininess detected AFTER rounding.

//

//  FMA       : FADD FSUB FMUL FMADD FMSUB FNMSUB FNMADD   (truly fused, single rounding)

//  DivSqrt   : FDIV FSQRT                                  (iterative, 1 bit / cycle, ~29 cycles)

//  FPMisc    : FSGNJ[N|X] FMIN FMAX FEQ FLT FLE FCLASS FCVT.W[U].S FCVT.S.W[U] FMV.X.W FMV.W.X

//  FPU       : top level wrapper (Decoupled request -> Valid response)

//  FRegFile / FCSR / FPDecode : f0-f31, fflags/frm CSRs, instruction decode helper

package rv32.fpu





import chisel3._

import chisel3.util._





// ------------------------------------------------------------------ constants

object FPConst {

  val CanonNaN = "h7fc00000".U(32.W)

  val One      = "h3f800000".U(32.W)

  val RNE = 0.U(3.W); val RTZ = 1.U(3.W); val RDN = 2.U(3.W)

  val RUP = 3.U(3.W); val RMM = 4.U(3.W); val DYN = 7.U(3.W)

}





object FPOp {

  val FADD = 0.U(5.W);   val FSUB = 1.U(5.W);    val FMUL = 2.U(5.W);   val FDIV = 3.U(5.W)

  val FSQRT = 4.U(5.W);  val FMADD = 5.U(5.W);   val FMSUB = 6.U(5.W);  val FNMSUB = 7.U(5.W)

  val FNMADD = 8.U(5.W); val FSGNJ = 9.U(5.W);   val FSGNJN = 10.U(5.W); val FSGNJX = 11.U(5.W)

  val FMIN = 12.U(5.W);  val FMAX = 13.U(5.W);   val FCVT_W_S = 14.U(5.W); val FCVT_WU_S = 15.U(5.W)

  val FCVT_S_W = 16.U(5.W); val FCVT_S_WU = 17.U(5.W); val FMV_X_W = 18.U(5.W)

  val FEQ = 19.U(5.W);   val FLT = 20.U(5.W);    val FLE = 21.U(5.W);   val FCLASS = 22.U(5.W)

  val FMV_W_X = 23.U(5.W)

}





object FPUtil {

  def clz(x: UInt): UInt = PriorityEncoder(Reverse(x))





  /** increment decision for the 5 RISC-V rounding modes */

  def roundUp(rm: UInt, sign: Bool, lsb: Bool, g: Bool, r: Bool): Bool = {

    val inexact = g || r

    MuxLookup(rm, false.B, Seq(

      FPConst.RNE -> (g && (r || lsb)),

      FPConst.RTZ -> false.B,

      FPConst.RDN -> (sign && inexact),

      FPConst.RUP -> (!sign && inexact),

      FPConst.RMM -> g))

  }

}





// ------------------------------------------------------------ operand decode

class FPFields extends Bundle {

  val sign = Bool(); val expF = UInt(8.W); val frac = UInt(23.W)

  val isZero = Bool(); val isSub = Bool(); val isInf = Bool(); val isNaN = Bool(); val isSNaN = Bool()

  val sig = UInt(24.W)   // normalised significand, MSB = 1 (for non-zero finite)

  val exp = SInt(12.W)   // unbiased exponent of that MSB

}





object FPFields {

  def apply(x: UInt): FPFields = {

    val f = Wire(new FPFields)

    f.sign := x(31); f.expF := x(30, 23); f.frac := x(22, 0)

    val expZ  = f.expF === 0.U

    val expO  = f.expF.andR

    val fracZ = f.frac === 0.U

    f.isZero := expZ && fracZ

    f.isSub  := expZ && !fracZ

    f.isInf  := expO && fracZ

    f.isNaN  := expO && !fracZ

    f.isSNaN := expO && !fracZ && !f.frac(22)

    val raw = Cat(!expZ, f.frac)

    val lz  = FPUtil.clz(raw)

    f.sig := Mux(expZ, (raw << lz)(23, 0), raw)

    f.exp := Mux(expZ, (-126).S(12.W) - lz.zext, f.expF.zext - 127.S)

    f

  }

}





// -------------------------------------------------------------- round & pack

/** Takes sign, unbiased exponent of the MSB, normalised sigW-bit significand (MSB=1)

  * and an external sticky bit. Rounds, handles subnormals, overflow and flags. */

class RoundPack(sigW: Int) extends Module {

  require(sigW >= 26)

  val io = IO(new Bundle {

    val sign   = Input(Bool())

    val exp    = Input(SInt(12.W))

    val sig    = Input(UInt(sigW.W))

    val sticky = Input(Bool())

    val rm     = Input(UInt(3.W))

    val out    = Output(UInt(32.W))

    val flags  = Output(UInt(5.W))

  })

  import FPConst._





  val biased  = io.exp +& 127.S

  val isSub   = biased < 1.S

  val shiftS  = 1.S -& biased

  val shiftAmt = Mux(isSub, Mux(shiftS > sigW.S, sigW.U, shiftS.asUInt), 0.U)

  val sh      = shiftAmt(log2Ceil(sigW + 1) - 1, 0)





  val shifted  = io.sig >> sh

  val lostMask = ~((Fill(sigW, 1.U(1.W)) << sh)(sigW - 1, 0))

  val sticky   = io.sticky || (io.sig & lostMask).orR





  val main  = shifted(sigW - 1, sigW - 24)

  val guard = shifted(sigW - 25)

  val rest  = shifted(sigW - 26, 0).orR || sticky

  val inexact = guard || rest





  val inc     = FPUtil.roundUp(io.rm, io.sign, main(0), guard, rest)

  val rounded = main +& inc

  val carry   = rounded(24)

  val expPlus = biased +& Mux(carry, 1.S, 0.S)

  val expField = Mux(isSub, Mux(rounded(23), 1.U, 0.U), expPlus.asUInt)





  val ovf = !isSub && expPlus >= 255.S

  val ovfToInf = MuxLookup(io.rm, true.B, Seq(RTZ -> false.B, RDN -> io.sign, RUP -> !io.sign))

  val inf       = Cat(io.sign, "hff".U(8.W), 0.U(23.W))

  val maxFinite = Cat(io.sign, "hfe".U(8.W), "h7fffff".U(23.W))

  val normalOut = Cat(io.sign, expField(7, 0), rounded(22, 0))

  io.out := Mux(ovf, Mux(ovfToInf, inf, maxFinite), normalOut)





  // tininess after rounding: re-round with unbounded exponent when exactly one bit was shifted

  val main0  = io.sig(sigW - 1, sigW - 24)

  val g0     = io.sig(sigW - 25)

  val r0     = io.sig(sigW - 26, 0).orR || io.sticky

  val carry0 = main0.andR && FPUtil.roundUp(io.rm, io.sign, main0(0), g0, r0)

  val tiny   = isSub && !(sh === 1.U && carry0)





  io.flags := Cat(false.B, false.B, ovf, tiny && inexact, ovf || inexact)

}





// --------------------------------------------------------------------- FMA

/** result = (+/-a*b) +/- c, one rounding. FADD = a*1+c, FMUL = a*b with cAbsent. */

class FMA extends Module {

  val io = IO(new Bundle {

    val a = Input(UInt(32.W)); val b = Input(UInt(32.W)); val c = Input(UInt(32.W))

    val negProd = Input(Bool()); val negC = Input(Bool()); val cAbsent = Input(Bool())

    val rm = Input(UInt(3.W))

    val out = Output(UInt(32.W)); val flags = Output(UInt(5.W))

  })

  import FPConst._





  val A = FPFields(io.a); val B = FPFields(io.b); val C = FPFields(io.c)

  val sP = A.sign ^ B.sign ^ io.negProd

  val sC = C.sign ^ io.negC





  // ---- special cases

  val anyNaN       = A.isNaN || B.isNaN || (C.isNaN && !io.cAbsent)

  val anySNaN      = A.isSNaN || B.isSNaN || (C.isSNaN && !io.cAbsent)

  val infTimesZero = (A.isInf && B.isZero) || (A.isZero && B.isInf)

  val pInf         = (A.isInf || B.isInf) && !infTimesZero

  val cInf         = C.isInf && !io.cAbsent

  val infCancel    = pInf && cInf && (sP =/= sC)

  val isNaNRes     = anyNaN || infTimesZero || infCancel

  val invalid      = anySNaN || infTimesZero || infCancel

  val specialInf   = pInf || cInf

  val pZero        = A.isZero || B.isZero

  val cZero        = C.isZero || io.cAbsent





  // ---- exact product, normalised so MSB is bit 47

  val prod = A.sig * B.sig

  val pTop = prod(47)

  val sigPw = Mux(pTop, prod, prod << 1)

  val sigP = sigPw(47, 0)

  val eP   = (A.exp +& B.exp) + Mux(pTop, 1.S, 0.S)

  val sigC = Cat(C.sig, 0.U(24.W))

  val eC   = C.exp





  // ---- pick larger operand X, align smaller Y (3 extra bits: G R S)

  val pBigger = cZero || (!pZero && ((eP > eC) || (eP === eC && sigP >= sigC)))

  val eX   = Mux(pBigger, eP, eC)

  val eY   = Mux(pBigger, eC, eP)

  val sX   = Mux(pBigger, sP, sC)

  val sY   = Mux(pBigger, sC, sP)

  val sigX = Mux(pBigger, sigP, sigC)

  val sigY = Mux(pBigger, Mux(cZero, 0.U(48.W), sigC), sigP)





  val d  = eX - eY

  val dC = Mux(d > 51.S, 51.U(6.W), d.asUInt)

  val dU = dC(5, 0)

  val yExt    = Cat(sigY, 0.U(3.W))

  val yShift  = yExt >> dU

  val yMask   = ~((Fill(51, 1.U(1.W)) << dU)(50, 0))

  val ySticky = (yExt & yMask).orR

  val yAl     = Cat(yShift(50, 1), yShift(0) | ySticky)





  val xExt52 = Cat(0.U(1.W), sigX, 0.U(3.W))

  val yExt52 = Cat(0.U(1.W), yAl)

  val effSub = sX =/= sY

  val sum    = Mux(effSub, xExt52 - yExt52, xExt52 + yExt52)





  val sumZero = sum === 0.U

  val lz      = FPUtil.clz(sum)

  val sumN    = (sum << lz)(51, 0)

  val eR      = eX + 1.S - lz.zext





  val rp = Module(new RoundPack(52))

  rp.io.sign := sX; rp.io.exp := eR; rp.io.sig := sumN

  rp.io.sticky := false.B; rp.io.rm := io.rm





  // sign of an exact zero result

  val rdn = io.rm === RDN

  val zeroSign = Mux(pZero && cZero,

                   Mux(io.cAbsent, sP, Mux(sP === sC, sP, rdn)),

                   rdn)

  val finiteOut = Mux(pZero,

                    Mux(cZero, Cat(zeroSign, 0.U(31.W)), Cat(sC, io.c(30, 0))),

                    Mux(sumZero, Cat(zeroSign, 0.U(31.W)), rp.io.out))

  val finiteFlags = Mux(pZero || sumZero, 0.U(5.W), rp.io.flags)





  val infOut = Cat(Mux(pInf, sP, sC), "hff".U(8.W), 0.U(23.W))

  io.out   := Mux(isNaNRes, CanonNaN, Mux(specialInf, infOut, finiteOut))

  io.flags := Mux(isNaNRes, Cat(invalid, 0.U(4.W)), Mux(specialInf, 0.U(5.W), finiteFlags))

}





// ----------------------------------------------------------------- Div/Sqrt

class DivSqrt extends Module {

  val io = IO(new Bundle {

    val start  = Input(Bool()); val isSqrt = Input(Bool())

    val a = Input(UInt(32.W)); val b = Input(UInt(32.W)); val rm = Input(UInt(3.W))

    val busy = Output(Bool()); val done = Output(Bool())

    val out = Output(UInt(32.W)); val flags = Output(UInt(5.W))

  })

  import FPConst._





  val sIdle :: sIter :: sFin :: Nil = Enum(3)

  val state   = RegInit(sIdle)

  val cnt     = RegInit(0.U(5.W))

  val isSqrtR = RegInit(false.B)

  val rmR     = RegInit(0.U(3.W))

  val signR   = RegInit(false.B)

  val expR    = RegInit(0.S(12.W))

  val divisor = RegInit(0.U(24.W))

  val rem     = RegInit(0.U(32.W))

  val quo     = RegInit(0.U(27.W))     // quotient / root

  val rad     = RegInit(0.U(54.W))     // sqrt radicand

  val special = RegInit(false.B)

  val specOut = RegInit(0.U(32.W))

  val specFl  = RegInit(0.U(5.W))





  val A = FPFields(io.a); val B = FPFields(io.b)





  // ---- division special cases

  val dSign    = A.sign ^ B.sign

  val bothZero = A.isZero && B.isZero

  val bothInf  = A.isInf && B.isInf

  val dNaN     = A.isNaN || B.isNaN || bothZero || bothInf

  val dNV      = A.isSNaN || B.isSNaN || bothZero || bothInf

  val dInfRes  = A.isInf || B.isZero

  val dZeroRes = A.isZero || B.isInf

  val dDZ      = B.isZero && !A.isZero && !A.isInf && !A.isNaN

  val dSpecial = dNaN || dInfRes || dZeroRes

  val dOut = Mux(dNaN, CanonNaN,

               Mux(dInfRes, Cat(dSign, "hff".U(8.W), 0.U(23.W)), Cat(dSign, 0.U(31.W))))

  val dFl  = Cat(dNV, dDZ, 0.U(3.W))





  // ---- sqrt special cases

  val sqNaN     = A.isNaN || (A.sign && !A.isZero)

  val sqNV      = A.isSNaN || (!A.isNaN && A.sign && !A.isZero)

  val sqSpecial = sqNaN || A.isZero || A.isInf

  val sqOut     = Mux(sqNaN, CanonNaN, io.a)

  val sqFl      = Cat(sqNV, 0.U(4.W))





  io.busy := state =/= sIdle

  io.done := state === sFin





  switch(state) {

    is(sIdle) {

      when(io.start) {

        isSqrtR := io.isSqrt; rmR := io.rm; cnt := 0.U

        quo := 0.U

        when(io.isSqrt) {

          special := sqSpecial; specOut := sqOut; specFl := sqFl

          signR := false.B

          val m = Mux(A.exp(0), Cat(A.sig, 0.U(1.W)), Cat(0.U(1.W), A.sig)) // 25b, value in [1,4)

          rad  := Cat(m, 0.U(1.W), 0.U(28.W))                                // 54b

          expR := A.exp >> 1

          rem  := 0.U

          state := Mux(sqSpecial, sFin, sIter)

        }.otherwise {

          special := dSpecial; specOut := dOut; specFl := dFl

          signR   := dSign

          divisor := B.sig

          rem     := Cat(0.U(8.W), A.sig)

          expR    := A.exp - B.exp

          state   := Mux(dSpecial, sFin, sIter)

        }

      }

    }

    is(sIter) {

      when(isSqrtR) {

        val nextRem = Cat(rem(29, 0), rad(53, 52))

        val trial   = Cat(quo, 1.U(2.W))

        val ge      = nextRem >= trial

        rem  := Mux(ge, nextRem - trial, nextRem)

        quo  := Cat(quo(25, 0), ge)

        rad  := (rad << 2)(53, 0)

      }.otherwise {

        val ge = rem >= divisor

        val r2 = Mux(ge, rem - divisor, rem)

        rem := (r2 << 1)(31, 0)

        quo := Cat(quo(25, 0), ge)

      }

      cnt := cnt + 1.U

      when(cnt === 26.U) { state := sFin }

    }

    is(sFin) { state := sIdle }

  }





  // ---- final normalise + round (combinational from registers, valid in sFin)

  val rp  = Module(new RoundPack(27))

  val top = quo(26)

  rp.io.sign   := signR

  rp.io.exp    := Mux(isSqrtR || top, expR, expR - 1.S)

  rp.io.sig    := Mux(isSqrtR || top, quo, Cat(quo(25, 0), 0.U(1.W)))

  rp.io.sticky := rem =/= 0.U

  rp.io.rm     := rmR

  io.out   := Mux(special, specOut, rp.io.out)

  io.flags := Mux(special, specFl, rp.io.flags)

}





// --------------------------------------------------------------------- Misc

class FPMisc extends Module {

  val io = IO(new Bundle {

    val op = Input(UInt(5.W)); val rm = Input(UInt(3.W))

    val a = Input(UInt(32.W)); val b = Input(UInt(32.W))

    val out = Output(UInt(32.W)); val flags = Output(UInt(5.W))

  })

  import FPConst._

  import FPOp._





  val A = FPFields(io.a); val B = FPFields(io.b)

  val out = WireDefault(0.U(32.W)); val fl = WireDefault(0.U(5.W))





  // ---- sign injection

  val sgnBit = Mux(io.op === FSGNJ, B.sign, Mux(io.op === FSGNJN, !B.sign, A.sign ^ B.sign))

  val sgnj   = Cat(sgnBit, io.a(30, 0))





  // ---- compare / min / max

  val anyNaN   = A.isNaN || B.isNaN

  val anySNaN  = A.isSNaN || B.isSNaN

  val aMag = io.a(30, 0); val bMag = io.b(30, 0)

  val bothZero = A.isZero && B.isZero

  val lt = Mux(A.sign =/= B.sign, A.sign && !bothZero, Mux(A.sign, aMag > bMag, aMag < bMag))

  val eq = (io.a === io.b) || bothZero

  val le = lt || eq

  val minV = Mux(lt || (bothZero && A.sign), io.a, io.b)

  val maxV = Mux(lt || (bothZero && A.sign && !B.sign), io.b, io.a)

  val mmV  = Mux(io.op === FMIN, minV, maxV)

  val mmOut = Mux(A.isNaN && B.isNaN, CanonNaN, Mux(A.isNaN, io.b, Mux(B.isNaN, io.a, mmV)))





  // ---- classify

  val normal = !(A.isZero || A.isSub || A.isInf || A.isNaN)

  val cls = Cat(A.isNaN && A.frac(22), A.isSNaN,

                !A.sign && A.isInf, !A.sign && normal, !A.sign && A.isSub, !A.sign && A.isZero,

                 A.sign && A.isZero,  A.sign && A.isSub,  A.sign && normal,  A.sign && A.isInf)





  // ---- float -> int

  val isU   = io.op === FCVT_WU_S

  val e     = A.exp

  val eNeg  = e < 0.S

  val tooBig = e > 31.S

  val nz    = !A.isZero

  val t     = (A.sig.pad(55) << e(4, 0))(54, 0)

  val intPart = Mux(!eNeg && !tooBig, t(54, 23), 0.U(32.W))

  val g  = Mux(eNeg, nz && e === -1.S, t(22))

  val rs = Mux(eNeg, nz && Mux(e === -1.S, A.sig(22, 0).orR, true.B), t(21, 0).orR)

  val rounded = intPart +& FPUtil.roundUp(io.rm, A.sign, intPart(0), g, rs)

  val cvtOvfS = tooBig || Mux(A.sign, rounded > "h80000000".U, rounded > "h7fffffff".U)

  val cvtOvfU = tooBig || (A.sign && rounded =/= 0.U) || (!A.sign && rounded(32))

  val cvtInvalid = Mux(isU, cvtOvfU, cvtOvfS)

  val signedVal = Mux(A.sign, (0.U(33.W) - rounded)(31, 0), rounded(31, 0))

  val satS = Mux(A.isNaN || !A.sign, "h7fffffff".U(32.W), "h80000000".U(32.W))

  val satU = Mux(A.isNaN || !A.sign, "hffffffff".U(32.W), 0.U(32.W))

  val cvtVal = Mux(isU, Mux(cvtInvalid, satU, rounded(31, 0)), Mux(cvtInvalid, satS, signedVal))

  val cvtFl  = Mux(cvtInvalid, "b10000".U(5.W), Cat(0.U(4.W), g || rs))





  // ---- int -> float

  val iU    = io.op === FCVT_S_WU

  val iSign = !iU && io.a(31)

  val mag   = Mux(iSign, (~io.a + 1.U)(31, 0), io.a)

  val magZ  = mag === 0.U

  val ilz   = FPUtil.clz(mag)

  val rpI   = Module(new RoundPack(32))

  rpI.io.sign := iSign

  rpI.io.exp  := 31.S(12.W) - ilz.zext

  rpI.io.sig  := (mag << ilz)(31, 0)

  rpI.io.sticky := false.B

  rpI.io.rm := io.rm





  switch(io.op) {

    is(FSGNJ, FSGNJN, FSGNJX) { out := sgnj }

    is(FMIN, FMAX)            { out := mmOut; fl := Cat(anySNaN, 0.U(4.W)) }

    is(FEQ)                   { out := !anyNaN && eq; fl := Cat(anySNaN, 0.U(4.W)) }

    is(FLT)                   { out := !anyNaN && lt; fl := Cat(anyNaN, 0.U(4.W)) }

    is(FLE)                   { out := !anyNaN && le; fl := Cat(anyNaN, 0.U(4.W)) }

    is(FCLASS)                { out := cls }

    is(FMV_X_W, FMV_W_X)      { out := io.a }

    is(FCVT_W_S, FCVT_WU_S)   { out := cvtVal; fl := cvtFl }

    is(FCVT_S_W, FCVT_S_WU)   { out := Mux(magZ, 0.U, rpI.io.out); fl := Cat(0.U(4.W), rpI.io.flags(0)) }

  }

  io.out := out; io.flags := fl

}





// ---------------------------------------------------------------- top level

class FPUReq extends Bundle {

  val op = UInt(5.W)

  val rm = UInt(3.W)        // already resolved (rm==DYN replaced by fcsr.frm)

  val a = UInt(32.W); val b = UInt(32.W); val c = UInt(32.W)

}

class FPUResp extends Bundle {

  val data = UInt(32.W); val flags = UInt(5.W)

}





class FPU extends Module {

  val io = IO(new Bundle {

    val req  = Flipped(Decoupled(new FPUReq))

    val resp = Valid(new FPUResp)

  })

  import FPOp._

  val r = io.req.bits

  def opIn(ops: UInt*): Bool = ops.map(_ === r.op).reduce(_ || _)





  val fma  = Module(new FMA)

  val misc = Module(new FPMisc)

  val ds   = Module(new DivSqrt)





  val isFma    = opIn(FADD, FSUB, FMUL, FMADD, FMSUB, FNMSUB, FNMADD)

  val isAddSub = opIn(FADD, FSUB)

  val isDS     = opIn(FDIV, FSQRT)





  fma.io.a := r.a

  fma.io.b := Mux(isAddSub, FPConst.One, r.b)

  fma.io.c := Mux(isAddSub, r.b, r.c)

  fma.io.negProd := opIn(FNMSUB, FNMADD)

  fma.io.negC    := opIn(FSUB, FMSUB, FNMADD)

  fma.io.cAbsent := opIn(FMUL)

  fma.io.rm := r.rm





  misc.io.op := r.op; misc.io.rm := r.rm; misc.io.a := r.a; misc.io.b := r.b





  val busy = RegInit(false.B)

  io.req.ready := !busy





  ds.io.start  := io.req.fire && isDS

  ds.io.isSqrt := r.op === FSQRT

  ds.io.a := r.a; ds.io.b := r.b; ds.io.rm := r.rm





  val respReg   = Reg(new FPUResp)

  val respValid = RegInit(false.B)

  respValid := false.B





  when(io.req.fire) {

    when(isDS) { busy := true.B }.otherwise {

      respReg.data  := Mux(isFma, fma.io.out, misc.io.out)

      respReg.flags := Mux(isFma, fma.io.flags, misc.io.flags)

      respValid := true.B

    }

  }

  when(ds.io.done) {

    respReg.data := ds.io.out; respReg.flags := ds.io.flags

    respValid := true.B; busy := false.B

  }

  io.resp.valid := respValid

  io.resp.bits  := respReg

}





// ------------------------------------------------- FP regfile + fcsr + decode

class FRegFile extends Module {

  val io = IO(new Bundle {

    val rs1 = Input(UInt(5.W)); val rs2 = Input(UInt(5.W)); val rs3 = Input(UInt(5.W))

    val rd1 = Output(UInt(32.W)); val rd2 = Output(UInt(32.W)); val rd3 = Output(UInt(32.W))

    val wen = Input(Bool()); val waddr = Input(UInt(5.W)); val wdata = Input(UInt(32.W))

  })

  val regs = Mem(32, UInt(32.W))            // f0 is a normal register in RISC-V F

  def rd(a: UInt) = Mux(io.wen && io.waddr === a, io.wdata, regs(a))

  io.rd1 := rd(io.rs1); io.rd2 := rd(io.rs2); io.rd3 := rd(io.rs3)

  when(io.wen) { regs(io.waddr) := io.wdata }

}





/** CSRs 0x001 fflags, 0x002 frm, 0x003 fcsr */

class FCSR extends Module {

  val io = IO(new Bundle {

    val setFlags = Input(UInt(5.W))          // OR-accumulate (connect to resp.flags when resp.valid)

    val wen = Input(Bool()); val addr = Input(UInt(12.W)); val wdata = Input(UInt(32.W))

    val rdata = Output(UInt(32.W))

    val frm = Output(UInt(3.W))

  })

  val fflags = RegInit(0.U(5.W)); val frm = RegInit(0.U(3.W))

  fflags := fflags | io.setFlags

  when(io.wen) {

    switch(io.addr) {

      is(1.U) { fflags := io.wdata(4, 0) }

      is(2.U) { frm := io.wdata(2, 0) }

      is(3.U) { fflags := io.wdata(4, 0); frm := io.wdata(7, 5) }

    }

  }

  io.rdata := MuxLookup(io.addr, 0.U(32.W), Seq(

    1.U -> fflags, 2.U -> frm, 3.U -> Cat(frm, fflags)))

  io.frm := frm

}





class FPDecoded extends Bundle {

  val valid = Bool(); val op = UInt(5.W); val rm = UInt(3.W)

  val rs1IsInt = Bool()   // rs1 comes from the INTEGER regfile (FCVT.S.W[U], FMV.W.X)

  val rdIsInt  = Bool()   // result goes to the INTEGER regfile (FEQ/FLT/FLE/FCLASS/FMV.X.W/FCVT.W[U].S)

  val useRs3   = Bool()

} 





object FPDecode {

  /** Decodes OP-FP / FMADD / FMSUB / FNMSUB / FNMADD (fmt = S). FLW/FSW use the integer LSU. */

  def apply(inst: UInt): FPDecoded = {

    import FPOp._

    val d = Wire(new FPDecoded)

    d.valid := false.B; d.op := 0.U; d.rm := inst(14, 12)

    d.rs1IsInt := false.B; d.rdIsInt := false.B; d.useRs3 := false.B

    val opcode = inst(6, 0); val f5 = inst(31, 27); val f3 = inst(14, 12); val r2 = inst(24, 20)

    def set(op: UInt, rs1Int: Boolean = false, rdInt: Boolean = false): Unit = {

      d.valid := true.B; d.op := op; d.rs1IsInt := rs1Int.B; d.rdIsInt := rdInt.B

    }

    when(inst(26, 25) === 0.U) {

      switch(opcode) {

        is("b1000011".U) { set(FMADD);  d.useRs3 := true.B }

        is("b1000111".U) { set(FMSUB);  d.useRs3 := true.B }

        is("b1001011".U) { set(FNMSUB); d.useRs3 := true.B }

        is("b1001111".U) { set(FNMADD); d.useRs3 := true.B }

        is("b1010011".U) {

          switch(f5) {

            is("b00000".U) { set(FADD) }

            is("b00001".U) { set(FSUB) }

            is("b00010".U) { set(FMUL) }

            is("b00011".U) { set(FDIV) }

            is("b01011".U) { when(r2 === 0.U) { set(FSQRT) } }

            is("b00100".U) {

              when(f3 === 0.U) { set(FSGNJ) }.elsewhen(f3 === 1.U) { set(FSGNJN) }.elsewhen(f3 === 2.U) { set(FSGNJX) }

            }

            is("b00101".U) { when(f3 === 0.U) { set(FMIN) }.elsewhen(f3 === 1.U) { set(FMAX) } }

            is("b11000".U) {

              when(r2 === 0.U) { set(FCVT_W_S, rdInt = true) }.elsewhen(r2 === 1.U) { set(FCVT_WU_S, rdInt = true) }

            }

            is("b11010".U) {

              when(r2 === 0.U) { set(FCVT_S_W, rs1Int = true) }.elsewhen(r2 === 1.U) { set(FCVT_S_WU, rs1Int = true) }

            }

            is("b11100".U) {

              when(r2 === 0.U) {

                when(f3 === 0.U) { set(FMV_X_W, rdInt = true) }.elsewhen(f3 === 1.U) { set(FCLASS, rdInt = true) }

              }

            }

            is("b11110".U) { when(r2 === 0.U && f3 === 0.U) { set(FMV_W_X, rs1Int = true) } }

            is("b10100".U) {

              when(f3 === 2.U) { set(FEQ, rdInt = true) }

                .elsewhen(f3 === 1.U) { set(FLT, rdInt = true) }

                .elsewhen(f3 === 0.U) { set(FLE, rdInt = true) }

            }

          }

        }

      }

    }



    d  // Return decoded instruction bundle

  }





  /** rm field of the instruction, with DYN (0b111) replaced by fcsr.frm */

  def resolveRm(instRm: UInt, frm: UInt): UInt = Mux(instRm === FPConst.DYN, frm, instRm)

}

