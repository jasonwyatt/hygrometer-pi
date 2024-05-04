package us.jwf.hygrometer

import kotlinx.cinterop.ExperimentalForeignApi
import libgpiod.gpiod_chip_get_line
import libgpiod.gpiod_line_release
import libgpiod.gpiod_line_request_output
import libgpiod.gpiod_line_set_value

@OptIn(ExperimentalStdlibApi::class, ExperimentalForeignApi::class)
class LED(
    chip: GpioChip,
    line: UInt,
) : AutoCloseable {
    private val led = gpiod_chip_get_line(chip.ptr, line)

    init {
        gpiod_line_request_output(led, Constants.CONSUMER, 0)
    }

    fun turnOn() = gpiod_line_set_value(led, 1)
    fun turnOff() = gpiod_line_set_value(led, 0)

    override fun close() {
        gpiod_line_release(led)
    }
}