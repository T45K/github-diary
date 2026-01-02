package ui.navigation

import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.number
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object LocalDateSerializer : KSerializer<LocalDate> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalDate", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalDate) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): LocalDate {
        return LocalDate.parse(decoder.decodeString())
    }
}

object YearMonthSerializer : KSerializer<YearMonth> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("YearMonth", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: YearMonth) {
        encoder.encodeString("${value.year}-${value.month.number}")
    }

    override fun deserialize(decoder: Decoder): YearMonth {
        val parts = decoder.decodeString().split("-")
        return YearMonth(parts[0].toInt(), parts[1].toInt())
    }
}

@Serializable
sealed interface NavRoute {
    @Serializable
    data class Calendar(
        @Serializable(with = YearMonthSerializer::class)
        val yearMonth: YearMonth
    ) : NavRoute

    @Serializable
    data class Preview(
        @Serializable(with = LocalDateSerializer::class)
        val date: LocalDate
    ) : NavRoute

    @Serializable
    data class Edit(
        @Serializable(with = LocalDateSerializer::class)
        val date: LocalDate
    ) : NavRoute

    @Serializable
    data object Settings : NavRoute
}
