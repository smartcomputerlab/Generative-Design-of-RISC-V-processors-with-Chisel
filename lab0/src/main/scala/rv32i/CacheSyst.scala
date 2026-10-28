package rv32i
import chisel3._
import chisel3.util._

/** Slow main memory (word addressed). One request at a time; the answer comes `latency` cycles
  * after the request was accepted. Word i is initialised to 0x100 + i so reads are easy to recognise.
  */
class MainMemory(depth: Int = 64, latency: Int = 3) extends Module {
  val io = IO(new Bundle {
    val valid     = Input(Bool())
    val wen       = Input(Bool())
    val addr      = Input(UInt(log2Ceil(depth).W))
    val wdata     = Input(UInt(32.W))
    val ready     = Output(Bool())          // not busy: a request is accepted when valid && ready
    val respValid = Output(Bool())          // read data valid / write acknowledged
    val rdata     = Output(UInt(32.W))
    val reads     = Output(UInt(8.W))       // activity counters
    val writes    = Output(UInt(8.W))
  })

  val mem    = RegInit(VecInit(Seq.tabulate(depth)(i => (0x100 + i).U(32.W))))
  val busy   = RegInit(false.B)
  val cnt    = Reg(UInt(log2Ceil(latency + 1).W))
  val rWen   = Reg(Bool())
  val rAddr  = Reg(UInt(log2Ceil(depth).W))
  val rWdata = Reg(UInt(32.W))
  val reads  = RegInit(0.U(8.W))
  val writes = RegInit(0.U(8.W))

  io.ready := !busy
  when(io.valid && !busy) {
    busy := true.B; cnt := (latency - 1).U
    rWen := io.wen; rAddr := io.addr; rWdata := io.wdata
    when(io.wen) { writes := writes + 1.U }.otherwise { reads := reads + 1.U }
  }

  io.respValid := busy && cnt === 0.U
  io.rdata     := mem(rAddr)
  when(busy) {
    when(cnt === 0.U) {
      busy := false.B
      when(rWen) { mem(rAddr) := rWdata }
    }.otherwise { cnt := cnt - 1.U }
  }

  io.reads  := reads
  io.writes := writes
}

/** Direct-mapped cache, one 32-bit word per line.
  *   read hit   : answered in the same cycle
  *   read miss  : fetch the word from main memory, fill the line, then answer
  *   write      : write-through; on a hit the line is updated too, on a miss memory only (no allocate)
  * The CPU must hold its request until `done`.
  */
class CacheMemory(lines: Int = 4, addrBits: Int = 6) extends Module {
  require(isPow2(lines) && addrBits > log2Ceil(lines))
  val idxBits = log2Ceil(lines)
  val tagBits = addrBits - idxBits

  val io = IO(new Bundle {
    // CPU side
    val valid        = Input(Bool())
    val wen          = Input(Bool())
    val addr         = Input(UInt(addrBits.W))
    val wdata        = Input(UInt(32.W))
    val done         = Output(Bool())
    val rdata        = Output(UInt(32.W))
    val hit          = Output(Bool())        // lookup result for the current request
    val hits         = Output(UInt(8.W))
    val misses       = Output(UInt(8.W))
    // main-memory side
    val memValid     = Output(Bool())
    val memWen       = Output(Bool())
    val memAddr      = Output(UInt(addrBits.W))
    val memWdata     = Output(UInt(32.W))
    val memReady     = Input(Bool())
    val memRespValid = Input(Bool())
    val memRdata     = Input(UInt(32.W))
  })

  val tags   = Reg(Vec(lines, UInt(tagBits.W)))
  val valids = RegInit(VecInit(Seq.fill(lines)(false.B)))
  val data   = Reg(Vec(lines, UInt(32.W)))

  val idx = io.addr(idxBits - 1, 0)
  val tag = io.addr(addrBits - 1, idxBits)
  val hit = valids(idx) && tags(idx) === tag

  val sIdle :: sMem :: sDone :: Nil = Enum(3)
  val state   = RegInit(sIdle)
  val sent    = RegInit(false.B)          // memory request already accepted
  val hitCnt  = RegInit(0.U(8.W))
  val missCnt = RegInit(0.U(8.W))

  io.done     := false.B
  io.rdata    := data(idx)
  io.hit      := hit
  io.hits     := hitCnt
  io.misses   := missCnt
  io.memWen   := io.wen
  io.memAddr  := io.addr
  io.memWdata := io.wdata
  val req = !sent
  io.memValid := false.B

  switch(state) {
    is(sIdle) {
      when(io.valid) {
        when(hit) { hitCnt := hitCnt + 1.U }.otherwise { missCnt := missCnt + 1.U }
        when(io.wen) {
          when(hit) { data(idx) := io.wdata }       // keep the cached copy current
          sent := false.B; state := sMem            // write-through
        }.elsewhen(hit) {
          io.done := true.B                         // read hit
        }.otherwise {
          sent := false.B; state := sMem            // read miss
        }
      }
    }
    is(sMem) {
      io.memValid := req
      when(req && io.memReady) { sent := true.B }
      when(io.memRespValid) {
        when(!io.wen) { data(idx) := io.memRdata; tags(idx) := tag; valids(idx) := true.B }
        state := sDone
      }
    }
    is(sDone) { io.done := true.B; state := sIdle }
  }
}

/** The two modules wired together, with the activity signals brought out for the test. */
class CacheSystem(lines: Int = 4, latency: Int = 3) extends Module {
  val io = IO(new Bundle {
    val valid     = Input(Bool())
    val wen       = Input(Bool())
    val addr      = Input(UInt(6.W))
    val wdata     = Input(UInt(32.W))
    val done      = Output(Bool())
    val rdata     = Output(UInt(32.W))
    val hit       = Output(Bool())
    val hits      = Output(UInt(8.W))
    val misses    = Output(UInt(8.W))
    val memReq    = Output(Bool())      // cache -> memory request this cycle
    val memResp   = Output(Bool())      // memory -> cache response this cycle
    val memReads  = Output(UInt(8.W))
    val memWrites = Output(UInt(8.W))
  })

  val cache = Module(new CacheMemory(lines, 6))
  val mem   = Module(new MainMemory(64, latency))

  cache.io.valid := io.valid
  cache.io.wen   := io.wen
  cache.io.addr  := io.addr
  cache.io.wdata := io.wdata

  mem.io.valid := cache.io.memValid
  mem.io.wen   := cache.io.memWen
  mem.io.addr  := cache.io.memAddr
  mem.io.wdata := cache.io.memWdata
  cache.io.memReady     := mem.io.ready
  cache.io.memRespValid := mem.io.respValid
  cache.io.memRdata     := mem.io.rdata

  io.done := cache.io.done; io.rdata := cache.io.rdata; io.hit := cache.io.hit
  io.hits := cache.io.hits; io.misses := cache.io.misses
  io.memReq := cache.io.memValid; io.memResp := mem.io.respValid
  io.memReads := mem.io.reads; io.memWrites := mem.io.writes
}

