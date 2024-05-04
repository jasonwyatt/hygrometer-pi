package us.jwf.hygrometer

import kotlinx.cinterop.ExperimentalForeignApi
import libgpiod.gpiod_chip_get_line
import libgpiod.gpiod_line_get_value
import libgpiod.gpiod_line_release
import libgpiod.gpiod_line_request_input

@OptIn(ExperimentalStdlibApi::class, ExperimentalForeignApi::class)
class Button(
    chip: GpioChip,
    line: UInt,
) : AutoCloseable {
    private val button = gpiod_chip_get_line(chip.ptr, line)

    init {
        gpiod_line_request_input(button, Constants.CONSUMER)
    }

    val isPressed: Boolean
        get() = gpiod_line_get_value(button) == 0

    override fun close() {
        gpiod_line_release(button)
    }
}