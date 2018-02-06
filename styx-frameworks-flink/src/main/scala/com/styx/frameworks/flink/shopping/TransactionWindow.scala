package com.styx.frameworks.flink.shopping

import com.styx.domain.events.BaseTransactionEvent
import org.joda.time.DateTime

case class TransactionWindow(event: BaseTransactionEvent, start: DateTime, transactionSum: Double)
