package bot.features.rot


object Rot {

    private const val alphabetLowerCase = "abcdefghijklmnopqrstuvwxyz"
    private const val alphabetUpperCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"

    fun rotate(text: String, n: Int): String {
        var key = n
        while (key < 0)
            key += alphabetLowerCase.length

        val result = StringBuilder()
        for (char in text) {
            if (char.isLowerCase()) {
                val index = alphabetLowerCase.indexOf(char)
                if (index == -1) {
                    result.append(char)
                    continue
                }
                result.append(alphabetLowerCase[(index + key) % alphabetLowerCase.length])
            } else {
                val index = alphabetUpperCase.indexOf(char)
                if (index == -1) {
                    result.append(char)
                    continue
                }
                result.append(alphabetUpperCase[(index + key) % alphabetUpperCase.length])
            }
        }

        return result.toString()
    }
}