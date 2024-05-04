package us.jwf.hygrometer

import kotlinx.cinterop.ExperimentalForeignApi
import libgpiod.*
import platform.posix.pow
import kotlin.math.roundToInt

@OptIn(ExperimentalForeignApi::class, ExperimentalStdlibApi::class)
class Mcp3008(
    chip: GpioChip,
    chipSelectLineNum: UInt = 89.toUInt(),
    mosiLineNum: UInt = 87.toUInt(),
    misoLineNum: UInt = 88.toUInt(),
    clockLineNum: UInt = 90.toUInt(),
) : AutoCloseable {
    private val chipSelect = gpiod_chip_get_line(chip.ptr, chipSelectLineNum)
    private val mosi = gpiod_chip_get_line(chip.ptr, mosiLineNum)
    private val miso = gpiod_chip_get_line(chip.ptr, misoLineNum)
    private val clock = gpiod_chip_get_line(chip.ptr, clockLineNum)

    init {
        gpiod_line_request_output(chipSelect, Constants.CONSUMER, 1)
        gpiod_line_request_output(mosi, Constants.CONSUMER, 0)
        gpiod_line_request_input(miso, Constants.CONSUMER)
        gpiod_line_request_output(clock, Constants.CONSUMER, 1)
    }

    fun listen(device: Int): Sequence<Float> = sequence {
        while (true) {
            clockTick()
            gpiod_line_set_value(chipSelect, 1)
            clockTick()
            gpiod_line_set_value(chipSelect, 0)
            clockTick()
            // Bug here.. the offsets aren't right..
            sendAndReceive(0x01u)
            val msb = sendAndReceive(0x80u or ((device.toUInt() and 0x7u) shl 4))
            val lsb = sendAndReceive(0x0u)
            yield(((msb and 0x3u) shl 8 or lsb).toInt() / 1024f)
        }
    }

    fun listenVoltage(device: Int, maxVoltage: Float = 3.3f, decimals: Int = 5): Sequence<Float> {
        val multiplier = pow(10.0, decimals.toDouble()).toFloat()
        return listen(device).map { (it * maxVoltage * multiplier).roundToInt() / multiplier }
    }

    private fun sendAndReceive(byte: UInt): UInt {
        var result = 0u
        repeat(8) { i ->
            gpiod_line_set_value(clock, 0)
            result = (result shl 1) or (if (gpiod_line_get_value(miso) == 1) 0u else 1u)
            gpiod_line_set_value(mosi, ((byte and (1u shl (7 - i))) shr (7 - i)).toInt())
            gpiod_line_set_value(clock, 1)
        }
        return result
    }

    private fun clockTick() {
        gpiod_line_set_value(clock, 0)
        gpiod_line_set_value(clock, 1)
    }

    override fun close() {
        gpiod_line_release(chipSelect)
        gpiod_line_release(mosi)
        gpiod_line_release(miso)
        gpiod_line_release(clock)
    }
}