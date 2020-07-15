import bot.features.numbers.NumbersUtils


fun testBinToDec() {

    println("Starting binToDec tests")
    val testCases = arrayOf(
        "101",
        "000000",
        "010101101",
        "1111111111111111111111111111111111111111111111111111111111111111",
        "1000000000000000000000000000000000000000000000000000000000000000",
        ""
    )
    val asserts = arrayOf(
        5L,
        0L,
        173L,
        -1L,
        -1L,
        -1L
    )

    for (i in testCases.indices) {
        assert(NumbersUtils.binToDec(testCases[i]) == asserts[i])
        println("Test ${i + 1} succeed")
    }
    println()
}

fun testDecToBin() {

    println("Starting decToBin tests")

    val testCases = arrayOf(
        11L,
        0L,
        2L,
        1L,
        9223372036854775807L,
        65537L,
        -1L
    )

    val asserts = arrayOf(
        "1011",
        "0",
        "10",
        "1",
        "111111111111111111111111111111111111111111111111111111111111111",
        "10000000000000001",
        ""
    )

    for (i in testCases.indices) {
        assert(NumbersUtils.decToBin(testCases[i]) == asserts[i])
        println("Test ${i + 1} succeed")
    }
    println()
}

fun testHexToDec() {

    println("Starting hexToDec tests")

    val testCases = arrayOf(
        "7fffffffffffffff",
        "8fffffffffffffff",
        "10001",
        "2",
        "FF",
        "A"
    )

    val asserts = arrayOf(
        9223372036854775807,
        -1L,
        65537,
        2L,
        255,
        10
    )

    for (i in testCases.indices) {
        assert(NumbersUtils.hexToDec(testCases[i]) == asserts[i])
        println("Test ${i + 1} succeed")
    }
    println()
}

fun testDecToHex() {

    println("Starting decToHex tests")

    val testCases = arrayOf(
        11L,
        0L,
        2L,
        1L,
        9223372036854775807L,
        65537L,
        -1L
    )

    val asserts = arrayOf(
        "B",
        "0",
        "2",
        "1",
        "7fffffffffffffff",
        "10001",
        ""
    )

    for (i in testCases.indices) {
        assert(NumbersUtils.decToHex(testCases[i]) == asserts[i])
        println("Test ${i + 1} succeed")
    }
    println()
}

fun testNumToChar() {
    println("Starting numToChar tests")

    val testCases = arrayOf(
        0xff000031L,
        0x31L,
        0x000031L,
        50L,
        0b110011L,
        -1L
    )

    val asserts = arrayOf(
        '1',
        '1',
        '1',
        '2',
        '3',
        '.'
    )

    for (i in testCases.indices) {
        assert(NumbersUtils.numToChar(testCases[i]) == asserts[i])
        println("Test ${i + 1} succeed")
    }
    println()
}

fun main() {
    testBinToDec()
    testDecToBin()
    testHexToDec()
    testDecToHex()
    testNumToChar()
}