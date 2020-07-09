package bot

import bot.features.numbers.NumbersUtils
import db.DatabaseHelper
import org.telegram.telegrambots.meta.api.methods.send.SendDocument
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import java.lang.Exception


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
                if (number.startsWith("0b")) {
                    msgText += """
                        Binary: $number
                        Hex: 0x${NumbersUtils.binToHex(number.replace("0b", ""))}
                        Decimal: ${NumbersUtils.binToDec(number.replace("0b", ""))}
                        
                        
                        
                    """.trimIndent()
                } else {

                    if (number.startsWith("0x")) {
                        msgText += """
                        Binary: 0b${NumbersUtils.hexToBin(number.replace("0x", ""))}
                        Hex: $number
                        Decimal: ${NumbersUtils.hexToDec(number.replace("0x", ""))}
                        
                        
                        
                    """.trimIndent()
                    } else {
                        val longVal = try {
                            number.toLong()
                        } catch (e: Exception) {
                            -1L
                        }

                        msgText += """
                            Binary: 0b${NumbersUtils.decToBin(longVal)}
                            Hex: 0x${NumbersUtils.decToHex(longVal)}
                            Decimal: $number



                            """.trimIndent()
                    }
                }
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
                msgText += if (number.startsWith("0b")) {
                    "0x${NumbersUtils.binToHex(number.replace("0b", "")).toUpperCase()} "
                } else {
                    if (number.startsWith("0x")) {
                        "$number "
                    } else {
                        val longVal = try {
                            number.toLong()
                        } catch (e: Exception) {
                            -1L
                        }
                        "0x${NumbersUtils.decToHex(longVal).toUpperCase()} "
                    }
                }
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
                msgText += if (number.startsWith("0b")) {
                    "${NumbersUtils.binToDec(number.replace("0b", ""))} "
                } else {
                    if (number.startsWith("0x")) {
                        "${NumbersUtils.hexToDec(number.replace("0x", ""))} "
                    } else {
                        "$number "
                    }
                }
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
                msgText += if (number.startsWith("0b")) {
                    "$number "
                } else {
                    if (number.startsWith("0x")) {
                        "0b${NumbersUtils.hexToBin(number.replace("0x", ""))} "
                    } else {
                        val longVal = try {
                            number.toLong()
                        } catch (e: Exception) {
                            -1L
                        }

                        "0b${NumbersUtils.decToBin(longVal)} "
                    }
                }
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