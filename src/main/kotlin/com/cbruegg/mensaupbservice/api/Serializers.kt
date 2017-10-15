package com.cbruegg.mensaupbservice.api

import kotlinx.serialization.*
import kotlinx.serialization.internal.PrimitiveDesc
import java.util.*

@Serializer(forClass = Date::class)
object DateSerializer : KSerializer<Date> {
  override fun load(input: KInput): Date {
    return Date(input.readLongValue())
  }

  override val serialClassDesc: KSerialClassDesc = PrimitiveDesc("kotlin.Long")

  override fun save(output: KOutput, obj: Date) {
    output.writeLongValue(obj.time)
  }

}