import bot.features.rot.Rot

fun testRot() {

    println("Starting rotate() tests")

    val testCases = arrayOf(
        Pair("Test", 13),
        Pair("Caesar", -13),
        Pair("Bot", 1024),
        Pair("CTF", -300),
        Pair("I love CTF!!!@@#@#!@#123", -300)
    )

    val asserts = arrayOf(
        "Grfg",
        "Pnrfne",
        "Lyd",
        "OFR",
        "U xahq OFR!!!@@#@#!@#123"
    )

    for (i in testCases.indices) {
        assert(Rot.rotate(testCases[i].first, testCases[i].second) == asserts[i])
        println("Test ${i + 1} succeed")
    }
    println()
}

fun main() {
    testRot()
}