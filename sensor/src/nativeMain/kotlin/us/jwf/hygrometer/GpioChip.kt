package us.jwf.hygrometer

import kotlinx.cinterop.ExperimentalForeignApi
import libgpiod.gpiod_chip_close
import libgpiod.gpiod_chip_open_by_name

@OptIn(ExperimentalStdlibApi::class, ExperimentalForeignApi::class)
class GpioChip(
    name: String = "gpiochip1"
) : AutoCloseable {
    val ptr = gpiod_chip_open_by_name(name)

    override fun close() {
        gpiod_chip_close(ptr)
    }
}