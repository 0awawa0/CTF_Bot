package bot.features.magic


object MagicNumbers {

    fun checkMagicNumber(magicNumber: String): String {

        return when (magicNumber.toLowerCase()) {

            "ff d8" -> """
                Сигнатура JPEG изображения. Также характерным для JPEG есть магическое число ff d9 - конец файла.
                
                Есть и другие магические числа, характерные для JPEG: ff c0, ff c1, ff c2, ff c4, ff db, ff dd, ff da, ff fe. Подробнее о них можно почитать тут:
                
                https://ru.wikipedia.org/wiki/JPEG
            """.trimIndent()

            "ff d9" -> """
                Маркер конца для JPEG изображения. Сигнатурой файла является магическое число ff d8.
            """.trimIndent()


            else -> "Unknown magic number"
        }
    }
}