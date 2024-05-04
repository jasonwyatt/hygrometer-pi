import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.staticCFunction
import platform.posix.exit
import us.jwf.hygrometer.Button
import us.jwf.hygrometer.GpioChip
import us.jwf.hygrometer.LED
import us.jwf.hygrometer.Mcp3008

@OptIn(ExperimentalForeignApi::class)
object State {
    lateinit var chip: GpioChip
    lateinit var button: Button
    lateinit var led: LED
    lateinit var sensorPower: LED
    lateinit var spi: Mcp3008

    fun setUp() {
        chip = GpioChip()
        sensorPower = LED(chip, 81u)
        button = Button(chip, 82u)
        led = LED(chip, 83u)
        spi = Mcp3008(chip)
        led.turnOn()
        sensorPower.turnOff()
    }

    fun tearDown() {
        spi.close()
        button.close()
        led.turnOff()
        led.close()
        sensorPower.turnOn()
        sensorPower.close()
        chip.close()
    }

    val closeHandler = staticCFunction<Int, Unit> {
        tearDown()
        exit(0)
    }
}