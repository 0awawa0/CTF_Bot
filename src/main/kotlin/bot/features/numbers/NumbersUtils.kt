package bot.features.numbers

import java.lang.NumberFormatException


object NumbersUtils {

    fun binToDec(bitString: String): Long {
        return  try {
            bitString.toLong(2)
        } catch (e: NumberFormatException) {
            -1L
        }
    }

    fun decToBin(value: Long): String {

        if (value < 0) return ""

        return value.toString(2)
    }


    fun hexToDec(hex: String): Long {
        return try {
            hex.toLong(16)
        } catch (e: NumberFormatException) {
            -1L
        }
    }

    fun decToHex(value: Long): String {
        if (value < 0) return ""
        return value.toString(16)
    }

    fun binToHex(value: String): String {
        return decToHex(binToDec(value))
    }

    fun hexToBin(value: String): String {
        return decToBin(hexToDec(value))
    }

    fun numToChar(value: Long): Char { return if (value > 0) value.toChar() else '.'}
}