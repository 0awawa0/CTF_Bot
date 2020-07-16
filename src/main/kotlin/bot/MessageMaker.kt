package bot

import bot.features.magic.MagicNumbers
import bot.features.numbers.NumbersUtils
import bot.features.rot.Rot
import bot.utils.Helper
import db.DatabaseHelper
import org.telegram.telegrambots.meta.api.methods.send.SendDocument
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton


/**
 * This class builds answers to users for bot to send.
 * This file will probably be pretty big with lots of functions for particular case.
 * And this class is designed to contain only functions. And all those functions should return something for bot to execute.
 *
 * Here is a list of all functions to simplify navigation:
 *
 * @see getFlagMessage - checks the flag and answer if it's right, wrong or user has already passed it earlier.
 *
 * @see getMenuMessage - greetings message
 *
 * @see getPasswordRequestMessage - asks user for password if bot is in testing mode and user is not authorized.
 *
 * @see getPasswordWrongMessage - tells user that the password he/she sent is incorrect, so it's not authorized.
 *
 * @see getTasksMessage - returns list of all tasks for current CTF competition
 *
 * @see getScoreboardMessage - returns list of users from database sorted by their current score.
 *
 * @see getTaskMessage - returns details for particular task by its id
 *
 * @see getFileMessage - returns file user has requested for particular task. File will be sent only if the files folder for task contains it.
 *
 * @see getErrorMessage - this message means that something bad has happened Receiving this message means that there is a bug in the code and it needs to be fixed.
 *
 * @see getUnknownMessage - sent if unknown command or callback data received by the user. Receiving this message mostly shouldn't mean a bug (though it could be), it's far more probably that there is a typo in a command.
 *
 * @see getConvertMessage - converts given array of numbers to binary, hexadecimal and decimal.
 *
 * @see getToHexMessage - converts given array of numbers to hexadecimal.
 *
 * @see getToDecMessage - converts given array of numbers to decimal
 *
 * @see getToBinMessage - converts given array of numbers to binary
 *
 * @see getToStringMessage - represent given array of numbers as single string. To convert number to
 *              char Long#toChar() function is used. And it will process only least 16 bits of the number.
 *
 * @see getMessageToPlayer - returns message with given text to be sent to some particular player.
 *
 * @see getRotMessage - processes text by ROT13 algorithm with given key.
 *
 * @see getRotBruteMessage - processes text by ROT13 algorithm with all possible keys (there are 26 keys).
 *
 * @see getCheckMagicMessage - checks if given magic number is known for bot.
            * @see MagicNumbers
 *
 * @see getMagicData - returns information about magic number
            * @see MagicNumbers
 *
 * @see getCommandsHelpMessage - returns list of commands available to users with descriptions
 */
class MessageMaker {

    companion object {

        //        This chars must be escaped in markdown
//        '_', '*', '[', ']', '(', ')', '~', '`', '>', '#', '+', '-', '=', '|', '{', '}', '.', '!'
        var ctfName = ""

        fun getFlagMessage(chatId: Long, flag: String): SendMessage {

            var msgText = ""

            when (DatabaseHelper.onPlayerPassedFlag(chatId, flag)) {
                DatabaseHelper.FLAG_RESULT_SUCCESS -> {
                    msgText = "<b>$ctfName</b>\n\nВерный флаг, задание засчитано! Продолжай в том же духе!"
                }

                DatabaseHelper.FLAG_RESULT_WRONG -> {
                    msgText = "<b>$ctfName</b>\n\nТы не прав, подумай ещё."
                }

                DatabaseHelper.FLAG_RESULT_ALREADY_SOLVED -> {
                    msgText = "<b>$ctfName</b>\n\nЭтот флаг ты уже сдал, поздравляю! А теперь займись другими!"
                }

                DatabaseHelper.FLAG_RESULT_ERROR -> {
                    return getErrorMessage(chatId)
                }
            }

            val msg = SendMessage()
            msg.enableHtml(true)
            msg.text = msgText
            msg.chatId = chatId.toString()
            msg.replyMarkup = InlineKeyboardMarkup(
                listOf(listOf(InlineKeyboardButton().setText("Меню").setCallbackData(DATA_MENU)))
            )
            return msg
        }

        fun getMenuMessage(firstName: String, chatId: Long, userName: String?): SendMessage {
            val player = DatabaseHelper.getPlayerById(chatId)
            if (player == null) {
                DatabaseHelper.addNewPlayer(chatId, userName ?: firstName)
            }

            val msgText = """<b>$ctfName</b>
                |
                |Ку, <i>${userName ?: firstName}</i>! Твой текущий счёт: ${player?.currentScore ?: 0}. Твой счёт за сезон: ${player?.seasonScore ?: 0}
                |Для управления используй кнопки. Чтобы сдать флаг напиши /flag "твой флаг"
                |""".trimMargin()

            val buttonRow1 = listOf<InlineKeyboardButton>(
                InlineKeyboardButton().setText("Таблица лидеров").setCallbackData(DATA_SCOREBOARD),
                InlineKeyboardButton().setText("Задания").setCallbackData(DATA_TASKS)
            )
            val buttonRow2 = listOf<InlineKeyboardButton>(
                InlineKeyboardButton().setText("Доступные команды").setCallbackData(DATA_COMMANDS)
            )
            val buttonsTable = listOf(buttonRow1, buttonRow2)

            val msg = SendMessage()
            msg.chatId = chatId.toString()
            msg.enableHtml(true)
            msg.text = msgText
            msg.replyMarkup = InlineKeyboardMarkup(buttonsTable)
            return msg
        }

        fun getPasswordRequestMessage(chatId: Long): SendMessage {
            val msgText = "Бот находится в состоянии тестирования. Для авторизации пришли мне пароль в формате:\n/testing_password <пароль>"
            val msg = SendMessage()
            msg.chatId = chatId.toString()
            msg.text = msgText
            return msg
        }

        fun getPasswordWrongMessage(chatId: Long): SendMessage {
            val msg = SendMessage()
            msg.chatId = chatId.toString()
            msg.text = "Неверный пароль. Дотсуп запрещён"
            return msg
        }


        fun getTasksMessage(chatId: Long): SendMessage {
            val msgText = "<b>$ctfName</b>\n\nСписок заданий: "
            val buttonsList = arrayListOf<List<InlineKeyboardButton>>()
            if (DatabaseHelper.checkPlayerInDatabase(chatId)) {
                for (task in DatabaseHelper.getTasksForCtf(ctfName)) {
                    val taskSolved = task.id.value in DatabaseHelper.getSolvedTasksForPlayer(chatId)
                    buttonsList.add(listOf(
                        InlineKeyboardButton()
                            .setText(
                                "${task.category} - ${task.price}: ${task.name} ${if (taskSolved) "\uD83D\uDDF8" else ""}"
                            )
                            .setCallbackData("/task ${task.id}")
                    ))
                }
            } else {
                return getErrorMessage(chatId)
            }

            val msg = SendMessage()
            msg.enableHtml(true)
            msg.chatId = chatId.toString()
            msg.text = msgText
            msg.replyMarkup = InlineKeyboardMarkup(buttonsList)
            return msg
        }


        fun getScoreboardMessage(chatId: Long): SendMessage {
            val scoreboard = DatabaseHelper.getScoreboard()
            var msgText = """
                <b>$ctfName</b>
                |
                |Таблица лидеров:
                |
                """.trimMargin()
            var i = 1
            val maxLength = 20
            for (position in scoreboard) {
                val userName = if (position.first.length > 20)
                    position.first.slice(0 until maxLength - 3) + "..."
                else
                    position.first.let {
                        var name = it
                        while (name.length < 20)
                            name += " "
                        name
                    }

                val currentScore = position.second.toString().let {
                    var score = "Текущий: $it"
                    while (score.length < 20)
                        score = " $score"
                    score
                }

                val seasonScore = position.third.toString().let {
                    var score = "Сезон: $it"
                    while (score.length < 20)
                        score = " $score"
                    score
                }

                msgText += "$i.  $userName $currentScore  $seasonScore\n"
                i++
            }

            val msg = SendMessage()
            msg.chatId = chatId.toString()
            msg.enableHtml(true)
            msg.text = msgText
            msg.replyMarkup = InlineKeyboardMarkup().setKeyboard(
                listOf(listOf(InlineKeyboardButton().setText("Меню").setCallbackData(DATA_MENU)))
            )
            return msg
        }


        fun getTaskMessage(chatId: Long, taskId: Long): SendMessage {
            val files = DatabaseHelper.getTaskFiles(taskId)
            val task = DatabaseHelper.getTaskById(taskId)!!
            val msgText = "<b>$ctfName</b>\n\n${task.name}           ${task.price}\n\n${task.description}"
            val msg = SendMessage()
            msg.enableHtml(true)

            msg.chatId = chatId.toString()
            msg.text = msgText

            val buttons = arrayListOf<List<InlineKeyboardButton>>()

            for (file in files) {
                buttons.add(listOf(InlineKeyboardButton().setText(file.name).setCallbackData("$DATA_FILE $taskId ${file.name}")))
            }

            buttons.add(listOf(InlineKeyboardButton().setText("Меню").setCallbackData(DATA_MENU)))
            msg.replyMarkup = InlineKeyboardMarkup(buttons)

            return msg
        }

        fun getFileMessage(chatId: Long, taskId: Long, fileName: String): SendDocument {
            val contentFile = DatabaseHelper.getTaskFiles(taskId).find { it.name == fileName}!!
            val msg = SendDocument()
            msg.chatId = chatId.toString()
            msg.document = InputFile(contentFile, contentFile.name)
            msg.replyMarkup = InlineKeyboardMarkup(listOf(listOf(InlineKeyboardButton().setText("Меню").setCallbackData(DATA_MENU))))
            return msg
        }


        fun getErrorMessage(chatId: Long): SendMessage {
            val msgText = "Ой, возникла какая-то ошибка. Свяжитесь с @awawa0_0 для обратной связи."
            val msg = SendMessage()
            msg.chatId = chatId.toString()
            msg.replyMarkup = InlineKeyboardMarkup(
                listOf(listOf(InlineKeyboardButton().setText("Меню").setCallbackData(DATA_MENU)))
            )
            msg.text = msgText
            return msg
        }

        fun getUnknownMessage(chatId:  Long): SendMessage {
            val msgText = "Это что? Эльфийский? Я не понимаю. Используй кнопки, пожалуйста."
            val buttonsTable = listOf(listOf(InlineKeyboardButton().setText("Меню").setCallbackData(DATA_MENU)))

            val msg = SendMessage()
            msg.chatId = chatId.toString()
            msg.text = msgText
            msg.replyMarkup = InlineKeyboardMarkup(buttonsTable)

            return msg
        }

        fun getConvertMessage(chatId: Long, content: String): SendMessage {
            val msg = SendMessage()
            var msgText = ""
            val numbers = content.split(" ")

            for (number in numbers) {
                msgText += """
                    Binary: ${Helper.anyToBin(number)}
                    Hex: ${Helper.anyToHex(number)}
                    Decimal: ${Helper.anyToDec(number)}
                    
                    
                """.trimIndent()
            }

            msg.chatId = chatId.toString()
            msg.text = msgText
            msg.replyMarkup = InlineKeyboardMarkup(
                listOf(
                    listOf(
                        InlineKeyboardButton().setText("Меню").setCallbackData(DATA_MENU)
                    )
                )
            )

            return msg
        }

        fun getToHexMessage(chatId: Long, content: String): SendMessage {

            val msg = SendMessage()
            var msgText = ""
            val numbers = content.split(" ")

            for (number in numbers) {
                msgText += Helper.anyToHex(number)
            }

            msg.chatId = chatId.toString()
            msg.text = msgText
            msg.replyMarkup = InlineKeyboardMarkup(
                listOf(
                    listOf(
                        InlineKeyboardButton().setText("Меню").setCallbackData(DATA_MENU)
                    )
                )
            )

            return msg
        }

        fun getToDecMessage(chatId: Long, content: String): SendMessage {
            val msg = SendMessage()
            var msgText = ""
            val numbers = content.split(" ")

            for (number in numbers) {
                msgText += Helper.anyToDec(number)
            }

            msg.chatId = chatId.toString()
            msg.text = msgText
            msg.replyMarkup = InlineKeyboardMarkup(
                listOf(
                    listOf(
                        InlineKeyboardButton().setText("Меню").setCallbackData(DATA_MENU)
                    )
                )
            )

            return msg
        }

        fun getToBinMessage(chatId: Long, content: String): SendMessage {
            val msg = SendMessage()
            var msgText = ""
            val numbers = content.split(" ")

            for (number in numbers) {
                msgText += Helper.anyToBin(number)
            }

            msg.chatId = chatId.toString()
            msg.text = msgText
            msg.replyMarkup = InlineKeyboardMarkup(
                listOf(
                    listOf(
                        InlineKeyboardButton().setText("Меню").setCallbackData(DATA_MENU)
                    )
                )
            )

            return msg
        }

        fun getToStringMessage(chatId: Long, content: String): SendMessage {
            val msg = SendMessage()
            var msgText = ""
            val numbers = content.split(" ")

            for (number in numbers) {
                val char = Helper.anyToDec(number).trim().toLong()
                if (char != -1L) {
                    msgText += NumbersUtils.numToChar(char)
                } else {
                    msgText += "."
                }
            }

            if (msgText.trim().isEmpty()) {
                msgText = msgText.trim() + "."
            }

            msg.chatId = chatId.toString()
            msg.text = msgText
            msg.replyMarkup = InlineKeyboardMarkup(
                listOf(
                    listOf(
                        InlineKeyboardButton().setText("Меню").setCallbackData(DATA_MENU)
                    )
                )
            )

            return msg
        }

        fun getMessageToPlayer(id: Long, text: String): SendMessage {
            val msg = SendMessage()
            msg.chatId = id.toString()
            msg.text = text
            msg.replyMarkup = InlineKeyboardMarkup(
                listOf(
                    listOf(
                        InlineKeyboardButton().setText("Меню").setCallbackData(DATA_MENU)
                    )
                )
            )

            return msg
        }

        fun getRotMessage(chatId: Long, content: String): SendMessage {
            val msg = SendMessage()
            msg.chatId = chatId.toString()
            msg.replyMarkup = InlineKeyboardMarkup(
                listOf(
                    listOf(
                        InlineKeyboardButton().setText("Меню").setCallbackData(DATA_MENU)
                    )
                )
            )

            val splat = content.trim().split(" ")

            try {
                val key = splat[0].toInt()
                val text = splat.slice(1 until splat.size).joinToString(" ")
                msg.text = Rot.rotate(text, key)
            } catch (e: Exception) {
                msg.text = "-1"
            }

            return msg
        }

        fun getRotBruteMessage(chatId: Long, content: String): SendMessage {
            val msg = SendMessage()
            msg.chatId = chatId.toString()

            val msgText = StringBuilder()
            for (key in 0 until Rot.ALPHABET_LENGTH) {
                msgText.append("Key: $key  Text: ${Rot.rotate(content, key)}\n")
            }
            msg.text = msgText.toString()
            msg.replyMarkup = InlineKeyboardMarkup(
                listOf(
                    listOf(
                        InlineKeyboardButton().setText("Меню").setCallbackData(DATA_MENU)
                    )
                )
            )

            return msg
        }

        fun getCheckMagicMessage(chatId: Long, content: String): SendMessage {
            val msg = SendMessage()
            msg.chatId = chatId.toString()

            val msgText = StringBuilder()
            val replyMarkup = ArrayList<List<InlineKeyboardButton>>()

            val magicCheck = MagicNumbers.findMagic(content.trim())

            msgText.append("Результаты поиска")
            for ((i, match) in magicCheck.withIndex()) {
                replyMarkup.add(
                    listOf(
                        InlineKeyboardButton().setText(
                            "${i + 1}. ${match.first.formatName} - ${if (match.second) "Полное совпадение" else "Неполное совпадение"}"
                        ).setCallbackData(match.first.callback)
                    )
                )
            }

            msg.text = msgText.toString()

            replyMarkup.add(listOf(InlineKeyboardButton().setText("Меню").setCallbackData(DATA_MENU)))
            msg.replyMarkup = InlineKeyboardMarkup(replyMarkup)

            return msg
        }

        fun getMagicData(chatId: Long, content: String): SendMessage {
            val msg = SendMessage()
            msg.chatId = chatId.toString()
            msg.enableHtml(true)
            msg.text = MagicNumbers.getDataForMagic(content.trim())
            msg.replyMarkup = InlineKeyboardMarkup(listOf(
                listOf(InlineKeyboardButton().setText("Меню").setCallbackData(DATA_MENU))
            ))
            return msg
        }

        fun getCommandsHelpMessage(chatId: Long): SendMessage {
            val msg = SendMessage()
            msg.chatId = chatId.toString()
            msg.text = """
                Список команд, поддерживаемых ботом. Заметьте, что бот распознаёт десятичные, двоичные и шестнадцатеричные числа. Двоичные числа должны иметь префикс '0b', а шестнадцатеричные '0x'.
                В массивах числа должны быть разделены пробелом. Числа ограничены диапазоном [0:9223372036854775807]
                
                /flag <string> - проверяет флаг. Если переданная строка является флагом к какому-либо заданию, это задание будет зачтено как решенное.
                
                /convert <array of numbers> - переводит массив чисел в двоичную, десятичную и шестнадцатеричную системы счисления.
                
                /toHex <array of numbers> - переводит массив чисел в шестнадцатеричную систему счисления.
                
                /toDec <array of numbers> - переводит массив чисел в десятичную систему счисления.
                
                /toBin <array of numbers> - переводит массив чисел в двоичную систему счисления.
                
                /toString <array of numbers> - переводит массив чисел в одну строку. Числа ограничены 16 битами. Если передано число длиннее 16 бит, будут использованы младшие его 16 бит.
                
                /rot <key> <text> - преобразует текст по алгоритму ROT13 (Шифрование Цезаря) с заданным ключом. Ключ может быть положительным или отрицательным.
                
                /rotBruteForce <text> - расшифровывает текст по алгоритму ROT13 (Шифрование Цезаря) со всеми возможными вариантами ключа.
                
                /checkMagic <magic_number> - помогает определить тип файла по магическому числу. Магические числа должны быть указаны в шестнадцатеричном формате без префикса '0x', пример: ff d8. Магическими числами считаются не только сигнатуры файлов (первые n байт), но и другие, характерные для файлов последовательности. Например, "49 44 41 54" - сектор данных (IDAT) PNG файла.   
            """.trimIndent()
            msg.replyMarkup = InlineKeyboardMarkup(
                listOf(
                    listOf(
                        InlineKeyboardButton().setText("Меню").setCallbackData(DATA_MENU)
                    )
                )
            )

            return msg
        }
    }
}