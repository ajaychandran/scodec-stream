package scodec.stream.decode

import java.lang.ref.WeakReference
import org.scalacheck._
import Prop._
import scala.concurrent.duration._
import scalaz.stream.{Process,process1}
import scodec.{Codec,codecs => C}
import scodec.bits.BitVector

object SpaceLeakTest extends Properties("space-leak") {

  property("head of stream not retained") = secure {
    // make sure that head of stream can be garbage collected
    // as we go; this also checks for stack safety
    val ints =
      C.variableSizeBytes(C.int32, C.repeated(C.int32))
    val N = 400000
    val M = 5
    val chunk = (0 until M).toIndexedSeq
    def chunks = BitVector.unfold(0)(_ => Some(ints.encodeValid(chunk) -> 0))
    val dec = many(ints).take(N)
            . flatMap(chunk => Process.emitAll(chunk))
            . pipe(process1.sum)

    val r = run(chunks) { dec }
    r.runLastOr(0).run == (0 until M).sum * N
  }
}