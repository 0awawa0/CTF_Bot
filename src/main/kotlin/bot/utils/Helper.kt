package bot.utils

import bot.features.numbers.NumbersUtils
import tornadofx.isLong

object Helper {

    fun anyToBin(number: String): String {

        if (number.startsWith("0b")) {
            val result = NumbersUtils.decToBin(NumbersUtils.binToDec(number.replace("0b", "")))
            return if (result.isEmpty()) "-1 " else "0b$result "
        }

        if (number.startsWith("0x")) {
            val result = NumbersUtils.hexToBin(number.replace("0x", ""))
            return if (result.isEmpty()) { "-1 " } else { "0b$result " }
        }

        val longVal = try { number.toLong() } catch (e: Exception) { -1L }
        val result = NumbersUtils.decToBin(longVal)

        return if (result.isEmpty()) "-1 " else "0b$result "
    }

    fun anyToHex(number: String): String {

        if (number.startsWith("0x")) {
            val result = NumbersUtils.decToHex(NumbersUtils.hexToDec(number.replace("0x", "")))
            return if (result.isEmpty()) "-1 " else "0x$result "
        }

        if (number.startsWith("0b")) {
            val result = NumbersUtils.binToHex(number.replace("0b", ""))
            return if (result.isEmpty()) "-1 " else "0x$result "
        }

        val longVal = try { number.toLong() } catch (e: Exception) { -1L }
        val result = NumbersUtils.decToHex(longVal)

        return if (result.isEmpty()) "-1 " else "0x$result "
    }

    fun anyToDec(number: String): String {

        if (number.startsWith("0b")) {
            return "${NumbersUtils.binToDec(number.replace("0b", ""))} "
        }

        if (number.startsWith("0x")) {
            return "${NumbersUtils.hexToDec(number.replace("0x", ""))} "
        }

        return if (number.isLong()) "$number " else "-1 "
    }
}