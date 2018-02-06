package com.styx.setup

import com.styx.domain.events.TimedEvent
import com.styx.frameworks.flink.SeqSourceFunction
import com.styx.frameworks.flink.connectors.TimedEventWatermarkExtractor
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}

import scala.reflect.ClassTag

trait EventsFromSequence {

  /**
    * * @param inputSeq the constant fix to put on a flink stream. This needs to be serializable
    * * @tparam T the type of your messages, which needs to be able to be serializable
    * * @return a flink source (timestamped and watermarked) which reads from your sequence,
    * */
  def consumeEventsFromSequence[T <: TimedEvent : ClassTag : TypeInformation](inputSeq: Seq[T],
                                                                              env: StreamExecutionEnvironment): Some[DataStream[T]] = {
    Some(env.addSource[T](new SeqSourceFunction[T](inputSeq)).name("Constant source")
      .assignTimestampsAndWatermarks(new TimedEventWatermarkExtractor[T]()).name("Constant source watermarker"))
  }

}
