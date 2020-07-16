package bot.features.magic

import bot.*


object MagicNumbers {

    enum class Magic(val formatName: String, val callback: String) {
        JPEG_SIGNATURE("JPEG сигнатура", DATA_JPEG_SIGNATURE),
        JPEG_TAIL("JPEG конец файла", DATA_JPEG_TAIL),
        PNG_SIGNATURE("PNG сигнатура", DATA_PNG_SIGNATURE),
        PNG_HEADER("PNG заголовок", DATA_PNG_HEADER),
        PNG_DATA("PNG данные", DATA_PNG_DATA),
        PNG_TAIL("PNG конец файла", DATA_PNG_TAIL)
    }

    private val mapMagicToSignatures = hashMapOf(
        Pair(Magic.JPEG_SIGNATURE, "ff d8"),
        Pair(Magic.JPEG_TAIL, "ff d9"),
        Pair(Magic.PNG_SIGNATURE,"89 50 4e 47 0d 0a 1a 0a"),
        Pair(Magic.PNG_HEADER, "49 48 44 52"),
        Pair(Magic.PNG_DATA, "49 44 41 54"),
        Pair(Magic.PNG_TAIL, "49 45 4E 44 AE 42 60 82")
    )

    fun findMagic(magicNumber: String): ArrayList<Pair<Magic, Boolean>> {
        val results = ArrayList<Pair<Magic, Boolean>>()

        for (number in mapMagicToSignatures) {
            if (number.value.startsWith(magicNumber)) {
                if (number.value == magicNumber) {
                    results.add(Pair(number.key, true))
                    return results
                } else {
                    results.add(Pair(number.key, false))
                }
            }
        }

        return results
    }

    fun getDataForMagic(callback: String): String {
        val magic = mapMagicToSignatures.filter { it.key.callback == callback }.toList()
        if (magic.isEmpty()) return "Нет данных\n"
        else {
            return when (magic.take(1)[0].first) {
                Magic.JPEG_SIGNATURE -> """
                    <b>ff d8</b>
                    
                    Сигнатура JPEG изображения. Также характерным для JPEG есть магическое число ff d9 - конец файла.
                    Есть и другие магические числа, характерные для JPEG: ff c0, ff c1, ff c2, ff c4, ff db, ff dd, ff da, ff fe. Подробнее о них можно почитать тут:
                    https://ru.wikipedia.org/wiki/JPEG
                """.trimIndent()

                Magic.JPEG_TAIL -> """
                    <b>ff d9</b>
                    
                    Маркер конца файла JPEG изображения. Сигнатурой файла является магическое число ff d8.
                    Есть и другие магические числа, характерные для JPEG: ff c0, ff c1, ff c2, ff c4, ff db, ff dd, ff da, ff fe. Подробнее о них можно почитать тут:
                    https://ru.wikipedia.org/wiki/JPEG
                """.trimIndent()

                Magic.PNG_SIGNATURE -> """
                    <b>89 50 4e 47 0d 0a 1a 0a</b>
                    
                    Сигнатура PNG изображения. Также характерными для этого типа файлов являются магические числа:
                     49 48 44 52 - заголовок
                     49 44 41 54 - сектор данных
                     49 45 4E 44 AE 42 60 82 - конец файла
                     
                     Подробнее можно почитать тут:
                     https://en.wikipedia.org/wiki/Portable_Network_Graphics
                """.trimIndent()

                Magic.PNG_HEADER -> """
                    <b>49 48 44 52</b>
                    
                    Заголовок PNG файла. Также характерными для этого типа файлов являются магические числа:
                     89 50 4e 47 0d 0a 1a 0a - сигнатура файла
                     49 44 41 54 - сектор данных
                     49 45 4E 44 AE 42 60 82 - конец файла
                     
                     Подробнее можно почитать тут:
                     https://en.wikipedia.org/wiki/Portable_Network_Graphics
                """.trimIndent()

                Magic.PNG_DATA -> """
                    <b>49 44 41 54</b>
                    
                    Сектор данных PNG файла. Также характерными для этого типа файлов являются магические числа:
                     89 50 4e 47 0d 0a 1a 0a - сигнатура файла
                     49 48 44 52 - заголовок
                     49 45 4E 44 AE 42 60 82 - конец файла
                     
                     Подробнее можно почитать тут:
                     https://en.wikipedia.org/wiki/Portable_Network_Graphics
                """.trimIndent()

                Magic.PNG_TAIL -> """
                    <b>49 44 41 54</b>
                    
                    Маркер конца PNG файла. Также характерными для этого типа файлов являются магические числа:
                     89 50 4e 47 0d 0a 1a 0a - сигнатура файла
                     49 48 44 52 - заголовок
                     49 44 41 54 - сектор данных
                     
                     Подробнее можно почитать тут:
                     https://en.wikipedia.org/wiki/Portable_Network_Graphics
                """.trimIndent()
                else -> "Нет данных\n"
            }
        }
    }
}