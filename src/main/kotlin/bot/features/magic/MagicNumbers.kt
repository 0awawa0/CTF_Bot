//package bot.features.magic
//
//import bot.*
//
//
//object MagicNumbers {
//
//    enum class Magic(val formatName: String, val callback: String) {
//        JPEG_SIGNATURE("JPEG сигнатура", DATA_JPEG_SIGNATURE),
//        JPEG_TAIL("JPEG конец файла", DATA_JPEG_TAIL),
//        PNG_SIGNATURE("PNG сигнатура", DATA_PNG_SIGNATURE),
//        PNG_HEADER("PNG заголовок", DATA_PNG_HEADER),
//        PNG_DATA("PNG данные", DATA_PNG_DATA),
//        PNG_TAIL("PNG конец файла", DATA_PNG_TAIL),
//        ZIP_SIGNATURE("ZIP сигнатура", DATA_ZIP_SIGNATURE),
//        RAR_SIGNATURE("RAR сигнатура", DATA_RAR_SIGNATURE),
//        ELF_SIGNATURE("ELF сигнатура", DATA_ELF_SIGNATURE),
//        CLASS_SIGNATURE("CLASS сигнатура", DATA_CLASS_SIGNATURE),
//        PDF_SIGNATURE("PDF сигнатура", DATA_PDF_SIGNATURE),
//        PDF_TAIL("PDF конец файла", DATA_PDF_TAIL),
//        PSD_SIGNATURE("PSD сигнатура", DATA_PSD_SIGNATURE),
//        RIFF_SIGNATURE("RIFF сигнатура", DATA_RIFF_SIGNATURE),
//        WAVE_TAG("WAVE метка", DATA_WAVE_TAG),
//        AVI_TAG("AVI метка", DATA_AVI_TAG),
//        BMP_SIGNATURE("BMP сигнатура", DATA_BMP_SIGNATURE),
//        DOC_SIGNATURE("DOC сигнатура", DATA_DOC_SIGNATURE),
//        VMDK_SIGNATURE("VMDK сигнатура", DATA_VMDK_SIGNATURE),
//        TAR_SIGNATURE("TAR сигнатура", DATA_TAR_SIGNATURE),
//        SEVEN_Z_SIGNATURE("7Z сигнатура", DATA_7ZIP_SIGNATURE),
//        GZ_SIGNATURE("GZ сигнатура", DATA_GZ_SIGNATURE)
//    }
//
//    private val mapMagicToSignatures = hashMapOf(
//        Pair(Magic.JPEG_SIGNATURE, "FF D8"),
//        Pair(Magic.JPEG_TAIL, "FF D9"),
//        Pair(Magic.PNG_SIGNATURE,"89 50 4E 47 0D 0A 1A 0A"),
//        Pair(Magic.PNG_HEADER, "49 48 44 52"),
//        Pair(Magic.PNG_DATA, "49 44 41 54"),
//        Pair(Magic.PNG_TAIL, "49 45 4E 44 AE 42 60 82"),
//        Pair(Magic.ZIP_SIGNATURE, "50 4B"),
//        Pair(Magic.RAR_SIGNATURE, "52 61 72 21 1A"),
//        Pair(Magic.ELF_SIGNATURE, "7F 45 4C 46"),
//        Pair(Magic.CLASS_SIGNATURE, "CA FE BA BE"),
//        Pair(Magic.PDF_SIGNATURE, "25 50 44 46"),
//        Pair(Magic.PDF_TAIL, "25 25 45 4F 46 0A"),
//        Pair(Magic.PSD_SIGNATURE, "38 42 50 53"),
//        Pair(Magic.RIFF_SIGNATURE, "52 49 46"),
//        Pair(Magic.WAVE_TAG, "57 41 56 45"),
//        Pair(Magic.AVI_TAG, "41 56 49 20"),
//        Pair(Magic.BMP_SIGNATURE, "42 4D"),
//        Pair(Magic.DOC_SIGNATURE, "D0 CF 11 E0 A1 B1 1A E1"),
//        Pair(Magic.VMDK_SIGNATURE, "4B 44 4D"),
//        Pair(Magic.TAR_SIGNATURE, "75 73 74 61 72"),
//        Pair(Magic.SEVEN_Z_SIGNATURE, "37 7A BC AF 27 1C"),
//        Pair(Magic.GZ_SIGNATURE, "1F 8B")
//    )
//
//    fun findMagic(magicNumber: String): ArrayList<Pair<Magic, Boolean>> {
//        val results = ArrayList<Pair<Magic, Boolean>>()
//
//        for (number in mapMagicToSignatures) {
//            if (number.value.startsWith(magicNumber, true)) {
//                if (number.value.equals(magicNumber, true)) {
//                    results.add(Pair(number.key, true))
//                    return results
//                } else {
//                    results.add(Pair(number.key, false))
//                }
//            }
//        }
//
//        return results
//    }
//
//    fun getDataForMagic(callback: String): String {
//        val magic = mapMagicToSignatures.filter { it.key.callback == callback }.toList()
//        if (magic.isEmpty()) return "Нет данных\n"
//        else {
//            return when (magic.take(1)[0].first) {
//                Magic.JPEG_SIGNATURE -> """
//                    <b>FF D8</b>
//
//                    Сигнатура JPEG изображения. Также характерным для JPEG есть магическое число ff d9 - конец файла.
//                    Для анализа таких файлов можно попробовать следующие инструменты: Stegsolve, Exiftool, Binwalk, Foremost.
//                    Есть и другие магические числа, характерные для JPEG: ff c0, ff c1, ff c2, ff c4, ff db, ff dd, ff da, ff fe. Подробнее о них можно почитать тут:
//                    https://ru.wikipedia.org/wiki/JPEG
//                """.trimIndent()
//
//                Magic.JPEG_TAIL -> """
//                    <b>FF D9</b>
//
//                    Маркер конца файла JPEG изображения. Сигнатурой файла является магическое число ff d8.
//                    Для анализа таких файлов можно попробовать следующие инструменты: Stegsolve, Exiftool, Binwalk, Foremost, hex-редактор.
//                    Есть и другие магические числа, характерные для JPEG: ff c0, ff c1, ff c2, ff c4, ff db, ff dd, ff da, ff fe. Подробнее о них можно почитать тут:
//                    https://ru.wikipedia.org/wiki/JPEG
//                """.trimIndent()
//
//                Magic.PNG_SIGNATURE -> """
//                    <b>89 50 4E 47 0D 0A 1A 0A</b>
//
//                    Сигнатура PNG изображения. Также характерными для этого типа файлов являются магические числа:
//                     49 48 44 52 - заголовок
//                     49 44 41 54 - сектор данных
//                     49 45 4E 44 AE 42 60 82 - конец файла
//
//                     Для анализа PNG можно использовать: Exiftool, Binwalk, Stegsolve, Foremost, hex-редактор.
//                     Подробнее можно почитать тут:
//                     https://en.wikipedia.org/wiki/Portable_Network_Graphics
//                """.trimIndent()
//
//                Magic.PNG_HEADER -> """
//                    <b>49 48 44 52</b>
//
//                    Заголовок PNG файла. Также характерными для этого типа файлов являются магические числа:
//                     89 50 4e 47 0d 0a 1a 0a - сигнатура файла
//                     49 44 41 54 - сектор данных
//                     49 45 4E 44 AE 42 60 82 - конец файла
//
//                     Для анализа PNG можно использовать: Exiftool, Binwalk, Stegsolve, Foremost, hex-редактор.
//                     Подробнее можно почитать тут:
//                     https://en.wikipedia.org/wiki/Portable_Network_Graphics
//                """.trimIndent()
//
//                Magic.PNG_DATA -> """
//                    <b>49 44 41 54</b>
//
//                    Сектор данных PNG файла. Также характерными для этого типа файлов являются магические числа:
//                     89 50 4e 47 0d 0a 1a 0a - сигнатура файла
//                     49 48 44 52 - заголовок
//                     49 45 4E 44 AE 42 60 82 - конец файла
//
//                     Для анализа PNG можно использовать: Exiftool, Binwalk, Stegsolve, Foremost, hex-редактор.
//                     Подробнее можно почитать тут:
//                     https://en.wikipedia.org/wiki/Portable_Network_Graphics
//                """.trimIndent()
//
//                Magic.PNG_TAIL -> """
//                    <b>49 44 41 54</b>
//
//                    Маркер конца PNG файла. Также характерными для этого типа файлов являются магические числа:
//                     89 50 4e 47 0d 0a 1a 0a - сигнатура файла
//                     49 48 44 52 - заголовок
//                     49 44 41 54 - сектор данных
//
//                     Для анализа PNG можно использовать: Exiftool, Binwalk, Stegsolve, Foremost, hex-редактор.
//                     Подробнее можно почитать тут:
//                     https://en.wikipedia.org/wiki/Portable_Network_Graphics
//                """.trimIndent()
//
//                Magic.ZIP_SIGNATURE -> """
//                    <b>50 4B</b>
//
//                    Сигнатура zip архива. Помимо сжатия файлов, этот архив используется различными приложениями.
//                    Так, файлы .jar, .docx, .pptx, .xlsx и многие другие имеют сигнатуру 50 4B и являются zip архивами, которые можно распаковать.
//                    Для работы с зашифрованными архивами можно использоать инструменты John The Ripper или hashcat. Также можно попытаться восстановить пароль к архиву с помощью различных онлайн инструментов.
//
//                    Подробнее про формат файла можно почитать тут:
//                    https://en.wikipedia.org/wiki/Zip_(file_format)
//                """.trimIndent()
//
//                Magic.RAR_SIGNATURE -> """
//                    <b>52 61 72 21 1A</b>
//
//                    Сигнатура rar архива. Формат RAR - проприетарный, код распаковки архивов находится в открытом доступе, но создавать ПО для запаковки архивов запрещено лицензией.
//                    Поэтому, в отличие от zip, он не используется другими программами для создания собственных файлов.
//                    Для работы с зашифрованными архивами можно использоать инструменты John The Ripper или hashcat. Также можно попытаться восстановить пароль к архиву с помощью различных онлайн инструментов.
//
//                    Подробнее про RAR:
//                    https://docs.fileformat.com/compression/rar/
//                    https://en.wikipedia.org/wiki/RAR_(file_format)
//                    https://codedread.github.io/bitjs/docs/unrar.html
//                """.trimIndent()
//
//                Magic.ELF_SIGNATURE -> """
//                    <b>7F 45 4C 46</b>
//
//                    Сигнатура elf файла - исполняемого файла для Linux, FreeBSD, Solaris и др. UNIX-подобных систем.
//                     Работа с elf файлами чаще всего подразумевает PWN или Reverse с использованием инструментов: IDA Pro, GDB, Ghidra и пр.
//
//                     Подробнее про ELF:
//                     https://ru.wikipedia.org/wiki/Executable_and_Linkable_Format
//                """.trimIndent()
//
//                Magic.CLASS_SIGNATURE -> """
//                    <b>CA FE BA BE</b>
//
//                    Скомпилированный Java класс. Для реверса таких файлов часто используются следующие декомпиляторы: Procyon, CFR,  JDCore, Jadx, Fernflower, JAD.
//
//                    Подробнее про CLASS:
//                    https://en.wikipedia.org/wiki/Java_class_file
//                """.trimIndent()
//
//                Magic.PDF_SIGNATURE -> """
//                    <b>25 50 44 46</b>
//
//                    Формат документа, разработанный компанией Adobe. Также характерным для PDF является число: 25 25 45 4F 46 0A (%%EOF.) - конец файла.
//
//                    Подробнее о PDF:
//                    https://en.wikipedia.org/wiki/PDF#:~:text=A%20PDF%20file%20is%20a,such%20as%20%25PDF-1.7%20.&text=Strings%2C%20enclosed%20within%20parentheses%20(%20(,may%20contain%208-bit%20characters.
//                    https://www.adobe.com/content/dam/acom/en/devnet/acrobat/pdfs/pdf_reference_1-7.pdf
//                """.trimIndent()
//
//                Magic.PDF_TAIL -> """
//                    <b>25 25 45 4F 46 0A</b>
//
//                    Конец PDF файла. Сигнатура PDF: 25 50 44 46.
//
//                    Подробнее о PDF:
//                    https://en.wikipedia.org/wiki/PDF#:~:text=A%20PDF%20file%20is%20a,such%20as%20%25PDF-1.7%20.&text=Strings%2C%20enclosed%20within%20parentheses%20(%20(,may%20contain%208-bit%20characters.
//                    https://www.adobe.com/content/dam/acom/en/devnet/acrobat/pdfs/pdf_reference_1-7.pdf
//                """.trimIndent()
//
//                Magic.PSD_SIGNATURE -> """
//                    <b>38 42 50 53</b>
//
//                    Сигнатура файла программы Adobe Photoshop (psd).
//
//                    Подробнее о PSD:
//                    https://www.adobe.com/devnet-apps/photoshop/fileformatashtml/
//                """.trimIndent()
//
//                Magic.RIFF_SIGNATURE -> """
//                    <b>52 49 46</b>
//
//                    Сигнатура RIFF файла (.wav аудио и .avi видео). В случае с аудио .wav - сырой, обычно не сжатый поток байт, часто испоьлзуется в категории Stegano.
//                    Для работы с .wav аудиофайлами используются: Audacity, Exiftool.
//
//                    В случае видео могут использоваться различные виды компрессии.
//
//                    Для RIFF файла характерны также следующие магические числа:
//                    57 41 56 45 (WAVE) - метка аудиофайла
//                    41 56 49 20 (AVI.) - метка видеофайла.
//
//                    Подробнее о RIFF:
//                    https://en.wikipedia.org/wiki/WAV
//                    https://ru.wikipedia.org/wiki/Audio_Video_Interleave
//                """.trimIndent()
//
//                Magic.WAVE_TAG -> """
//                    <b>57 41 56 45</b>
//
//                    Метка .wav файла для формата RIFF (сигнатура 52 49 46). Аудиофайлы в формате WAV чаще всего представляют несжатый поток байт и используются для стеганографии.
//
//                     Подробнее о WAV:
//                     https://en.wikipedia.org/wiki/WAV
//                """.trimIndent()
//
//                Magic.AVI_TAG -> """
//                    <b>41 56 49 20</b>
//
//                    Метка .avi файла формата RIFF (сигнатура 52 49 46).
//
//                    Подробнее об AVI:
//                    https://ru.wikipedia.org/wiki/Audio_Video_Interleave
//                """.trimIndent()
//
//                Magic.BMP_SIGNATURE -> """
//                    <b>42 4D</b>
//
//                    Сигнатура BMP изображения.
//
//                    Подробно о BMP:
//                    https://ru.wikipedia.org/wiki/BMP
//                """.trimIndent()
//
//                Magic.DOC_SIGNATURE -> """
//                    <b>D0 CF 11 E0 A1 B1 1A E1</b>
//
//                    Compound File Binary Format - формат для объединения нескольких файлов в одном файле на диске. Используется различными программами. В частности, раньше этот формат файла использовался в пакете Microsoft Office для создания файлов .doc, .ppt, .xls.
//
//                    Подробно о формате:
//                    https://en.wikipedia.org/wiki/Compound_File_Binary_Format
//                """.trimIndent()
//
//                Magic.VMDK_SIGNATURE -> """
//                    <b>4B 44 4D</b>
//
//                    Сигнатура файла жесткого диска виртуальной машины, разработанный VMWare. Кроме виртуальных машин этой фирмы, этот формат файла поддерживается также программами DAEMON Tools Ultra, Sun xVM, QEMU, VirtualBox, Suse studio.
//
//                     Подробнее о формате (PDF файл):
//                     https://www.vmware.com/app/vmdk/?src=vmdk
//                """.trimIndent()
//
//                Magic.TAR_SIGNATURE -> """
//                    <b>75 73 74 61 72</b>
//
//                    Сигнатура .tar архива.
//
//                    Подробнее о формате:
//                    https://en.wikipedia.org/wiki/Tar_(computing)#UStar_format
//                """.trimIndent()
//
//                Magic.SEVEN_Z_SIGNATURE -> """
//                    <b>37 7A BC AF 27 1C</b>
//
//                    Сигнатура 7z архива.
//
//                    Подробнее о формате:
//                    https://www.romvault.com/Understanding7z.pdf
//                """.trimIndent()
//
//                Magic.GZ_SIGNATURE -> """
//                    <b>1F 8B</b>
//
//                    Сигнатура .gz архива.
//
//                    Подробнее о формате:
//                    https://tools.ietf.org/html/rfc1952
//                """.trimIndent()
//
//                else -> "Нет данных\n"
//            }
//        }
//    }
//}