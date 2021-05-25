package bot

import bot.features.magic.MagicNumbers
import bot.features.numbers.NumbersUtils
import bot.features.rot.Rot
import bot.utils.Helper
import db.DatabaseHelper
import org.telegram.telegrambots.meta.api.methods.send.SendDocument
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.send.SendSticker
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
 * @see getFlagSticker - checks the flag and answers if it's right, wrong or user has already passed it earlier. Returns SendSticker
 *
 * @see getFlagMessage - checks the flag and answer if it's right, wrong or user has already passed it earlier. Returns SendMessage
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

        private val niceStickers = setOf(
            "CAACAgIAAxkBAAII818ViQ806_Vkg6bol8ALkVEPOPBIAAIlAAM7YCQUglfAqB1EIS0aBA",
            "CAACAgIAAxkBAAIJR18VnEnsspEr1-c0nfuivqrsLF_4AAJ4CQAC8UK_BcyW4BnRNuwKGgQ",
            "CAACAgIAAxkBAAIJSF8VnFK_9gEmaVBryUw9QPXdIC1VAAINAgACNnYgDjJnEuNd-1iCGgQ",
            "CAACAgIAAxkBAAIJSV8VnFYVJRnEPBZhS3Eu7dWUD5QvAALzAgACnNbnCuAuBHGFD8ECGgQ",
            "CAACAgIAAxkBAAIJSl8VnFo5nBLmgFhi_cM6efEShZqVAAL5BwACGELuCAh1fKDO8HNOGgQ",
            "CAACAgIAAxkBAAIJVl8VnO7R91RmoyvPanX0cuE_9QQNAAJGAANSiZEj-P7l5ArVCh0aBA",
            "CAACAgUAAxkBAAIJVV8VnON2NIhUaBgnHyr4bY0Q-txhAAJuAwAC6QrIA3w0Dz_a7ARtGgQ",
            "CAACAgUAAxkBAAIJVF8VnN7sbq-xIlubyOfF3yTtF4UuAAKsAwAC6QrIA7C0qC7bh6c7GgQ",
            "CAACAgIAAxkBAAIJUl8VnMzHCA0COXVkf7xzgu3A2vBRAAIyCgACbjLYAAH9b7brkPA7TRoE",
            "CAACAgIAAxkBAAIJUV8VnMfihimPG5vP_ZK6u5P87C9fAALhAAPEe4QKi7PG5z3j_7YaBA",
            "CAACAgIAAxkBAAIJUF8VnLvWZA19VDS207QfeRlOKbdoAAJXCQACeVziCSskAcAPGVVeGgQ",
            "CAACAgIAAxkBAAIJT18VnLcHUgsFzCcxHihdJ1AowVFMAAKWAAMfAUwVQT-DgR0Ym0saBA",
            "CAACAgIAAxkBAAIJTl8VnLR1FJnJkBI0VecVVe1qo5o7AALpBwACYyviCXvSqf4U1mKMGgQ",
            "CAACAgIAAxkBAAIJTV8VnK9HKA94FtdBFWjzmCu-Uwn6AALtBwACYyviCSNLVcKzULbEGgQ",
            "CAACAgIAAxkBAAIJTF8VnKeHcCXNyXpeD8gwX2_jZJ7EAAIbAQACYyviCRVFtTZcAq6uGgQ",
            "CAACAgIAAxkBAAIJS18VnJo3e3Ab31YJIBAY0DbWWXhWAAIkAwACz7vUDm0PI10rjwfcGgQ",
            "CAACAgIAAxkBAAIJSl8VnFo5nBLmgFhi_cM6efEShZqVAAL5BwACGELuCAh1fKDO8HNOGgQ",
            "CAACAgIAAxkBAAIJSV8VnFYVJRnEPBZhS3Eu7dWUD5QvAALzAgACnNbnCuAuBHGFD8ECGgQ",
            "CAACAgIAAxkBAAIJSF8VnFK_9gEmaVBryUw9QPXdIC1VAAINAgACNnYgDjJnEuNd-1iCGgQ",
            "CAACAgIAAxkBAAIJWF8WfndKRVEEPtHafWczBHRjcnFXAAI2AgACz7vUDoh3s73RN0lHGgQ",
            "CAACAgUAAxkBAAIJWV8WfoN1skrCCtKeQc2jiZH04zQaAAKHAwAC6QrIAypdTyYxR1EwGgQ",
            "CAACAgIAAxkBAAIJWl8WfpblDWCMQptHaCcoMNvqOlqSAAJ0AQACihKqDqn6Ep_umnvhGgQ",
            "CAACAgIAAxkBAAIJW18Wfp6MbnlP03waFSGOxGKGwftIAAL9CQACLw_wBrlfDQPsjDttGgQ",
            "CAACAgIAAxkBAAIJXF8WfqVcu0_TGUjvTnom-2f6FWbJAAK0AgACW__yCmCB33V_fjhuGgQ",
            "CAACAgIAAxkBAAIJXV8WfqrBeL4l4rCKeOE4g7D5czdEAALoAgACtXHaBlINrrZVgIJbGgQ",
            "CAACAgIAAxkBAAIJXl8WfsLYOjRW-7UltRYl9Dmm9oI2AAL-AANWnb0K2gRhMC751_8aBA",
            "CAACAgIAAxkBAAIJX18WfshmsxFruyhfiwasTdWvcx67AAL8AAMw1J0RU6XxJu0oNegaBA",
            "CAACAgIAAxkBAAIJYF8WftVOWc6aFz_hTTVxJgggCjVXAAIgAAOWn4wOrP1BM_Sqb_kaBA",
            "CAACAgIAAxkBAAIJYV8WfvQNewABa1o6xvFd_15l57iZQAACdAMAAvoLtgjV5oYGGtDaUBoE",
            "CAACAgIAAxkBAAIJi18Wh9_7jkge3HN8iqY8k2f8xNT6AAIiAgACNnYgDjgrKIrs7Ue3GgQ",
            "CAACAgIAAxkBAAIJjF8Wh-UNsbzRQratjVJbM4qjDe3xAALkAAPEe4QKEcIHSKqKDJQaBA",
            "CAACAgIAAxkBAAIJjV8Wh-yWKz_qWTIZcOQ8LW6Hsk_NAAKVAAMfAUwVAfFU0Ca-WLcaBA",
            "CAACAgIAAxkBAAIJjl8Wh_BJYm0aNMft2fD_56Q6QKfiAAImAwACz7vUDqRT7fQiGuLvGgQ",
            "CAACAgIAAxkBAAIJj18Wh_ZBEj4O1SXCb3Dbb-hvPPmlAAJiCQACeVziCYqMcuA2PTO4GgQ",
            "CAACAgIAAxkBAAIJkF8WiATgl_sq4wZXUP-mgKMTDBGmAAIfAAMNttIZUwyqkRgWjiAaBA",
            "CAACAgIAAxkBAAIJkV8WiA0MQOITIucEncqGtw6VDAN9AAJrAAOWn4wOMuKqiPbhniUaBA",
            "CAACAgIAAxkBAAIJkl8WiDPgckyWTmq3PGwwRpmQ16PXAAJXAAMfAUwVF6izXvNT8SwaBA",
            "CAACAgIAAxkBAAIJk18WiD-wYWHHWJ0Uqt6t0jxK-WJ6AAJlAwAC7sShCkZUXth8bywsGgQ"
        )

        private val badStickers = setOf(
            "CAACAgIAAxkBAAIJYl8WgCadydPEHsn8m3Zj11fUJe5cAAJTBAACa8TKCj3zLQSMAAHUshoE",
            "CAACAgIAAxkBAAIJY18WgC9yX5XGzCEYPCJHXI9ELZqqAAIdAQACYyviCcmu_Eb_OpfCGgQ",
            "CAACAgIAAxkBAAIJZF8WgDua5wQQ5HO1iKCvNrXG0B5dAAJYAQACYyviCUGY0kTDklu3GgQ",
            "CAACAgIAAxkBAAIJZV8WgD9zNMm42QH4_baLrEqIPZylAAJgCQACeVziCSWHSkOIrb0eGgQ",
            "CAACAgIAAxkBAAIJZl8WgETaBqdrWRh3toYcL9h11lYpAALjCAACCLcZAqhiD9a4x0BrGgQ",
            "CAACAgUAAxkBAAIJZ18WgEdDX9OK5xUJz1STWuZUVX96AAJ9AwAC6QrIA60D0stxYv2mGgQ",
            "CAACAgIAAxkBAAIJaF8WgFASM7bpj-j7_Uz-jyiHMI_lAAIMAQACVp29Cqpv9dJA3OI9GgQ",
            "CAACAgIAAxkBAAIJaV8WgFzJl9AQTzXJIYcWXwcQcwMGAAJ9AAP3AsgPLsm7Ct3LIkkaBA",
            "CAACAgIAAxkBAAIJal8WgGLg1NhN5ks7mVUTiEhOCmoXAAJ3AAPkoM4HJeKgUTKBHKsaBA",
            "CAACAgIAAxkBAAIJa18WgG69hmc5a6d24vV6FHwJYJdBAAKhAgACLw_wBuuWE9yacw5aGgQ",
            "CAACAgIAAxkBAAIJbF8WgHmNA1mVcNP1_jkowYdim_KfAALcBQAC-gu2CAVrbzSDr7IeGgQ",
            "CAACAgMAAxkBAAIJbV8WgIOeyWFeeDslyHRKZsadXeSfAAKlBQACv4yQBIanELUTj1vxGgQ",
            "CAACAgIAAxkBAAIJbl8WgJKvQCTheD8cOUSfxDu8EVGSAAL7AAO6wJUF9-eECGzvdbsaBA",
            "CAACAgIAAxkBAAIJb18WgJgHzSe8-yozVHmWFops_Hd1AAJ-AAOc1ucKNwEqwAmfLPEaBA",
            "CAACAgIAAxkBAAIJcF8WgJy6BMGp4CjRtzEtsWADzqj3AALbAwACRvusBFAotbLO5Xr6GgQ",
            "CAACAgIAAxkBAAIJcV8WgKMTjJ7QAAEwWx4INPOEbwZdQAACdwADpkRIC2VG38pizwfDGgQ",
            "CAACAgIAAxkBAAIJcl8WgKwmCFw_5Jk44QguB0pZMQGXAAJ2AAN8l30Le9tVTmzP0-8aBA",
            "CAACAgIAAxkBAAIJc18WgLG0g1lL4c0dbqJ-MVl2xA9OAAIIAgAC3PKrB9wfMThSyktwGgQ",
            "CAACAgIAAxkBAAIJdF8WgLyvm7scUeGkKo8CJ1Xs9a4KAAL9AAM2diAO4PMb1BfZQpMaBA",
            "CAACAgIAAxkBAAIJdl8WglCb-0drPxdhlNmHCXdR_AE_AAIDAwAChmBXDtcEnzRXnuejGgQ",
            "CAACAgIAAxkBAAIJd18WglWIu2WmKC7jklabf-d11GduAAIlAwACnNbnCgAB0udXwyXKPxoE",
            "CAACAgIAAxkBAAIJeF8Wgls46s9Z1dTUBv8suIMNAprDAALlAAPEe4QKCJwD6jAVaaUaBA",
            "CAACAgIAAxkBAAIJeV8WgmHTWK_n_yZPClEgDqUKomWpAAL1BwACYyviCa-zO_3ScB8TGgQ",
            "CAACAgIAAxkBAAIJel8WgmeG9nQhdpkxoTQitp-9eKLfAAKMAAMfAUwVzVhIJLQKmaIaBA",
            "CAACAgIAAxkBAAIJe18Wgm7D7YNSkwrbSi8OcjWXmCcQAAKGAQACihKqDtQEq1Y5m_yfGgQ",
            "CAACAgUAAxkBAAIJfF8WgnguQm0k46yhyBSRwXZnYUJmAAJ4AwAC6QrIA9OAY5YOT1JnGgQ",
            "CAACAgUAAxkBAAIJfV8Wgny-k8Iy2t3N5H8o8H24OhhqAALDAwAC6QrIA8UWW3R2DVzfGgQ",
            "CAACAgUAAxkBAAIJfl8WgoJeGFhd8JxyES0cF73YwkjeAAKPAwAC6QrIAzdw6Tx8FEZIGgQ",
            "CAACAgIAAxkBAAIJf18Wgo1OdW50dJtbc08k6ZCExEIhAALoAQACygMGC7r72N5wzGG7GgQ",
            "CAACAgIAAxkBAAIJgF8Wgpgsfp31oJQoshX09Q6_K2RjAAKTAAP3AsgPJeWS_-k7iFUaBA",
            "CAACAgIAAxkBAAIJgV8WgqRij08Nn3jUc-0wLSorHsLyAAL5AANWnb0KlWVuqyorGzYaBA",
            "CAACAgIAAxkBAAIJgl8Wgq68brT5GC4zt-WPHcYYTDd6AAJHAANSiZEj2Audy00CRw0aBA",
            "CAACAgIAAxkBAAIJg18WgrVhBCf-mVf9mx3ugRuaw7QqAAJlAAOWn4wOHfGZDqS0IikaBA",
            "CAACAgIAAxkBAAIJhF8WgsCCF9Xv3oZXsuJhyGaCz9GIAAIgAAPkoM4HJUDHoipmCFQaBA",
            "CAACAgIAAxkBAAIJhV8WgseGFo_xmIsB2uBQ3qAUgLWNAAI6AwACxKtoCxkNYQ8HmB4vGgQ",
            "CAACAgIAAxkBAAIJhl8WgtXFYOYkRWmxjjZLy2uoScg3AALkAgACRxVoCeOpfo6tHHz-GgQ",
            "CAACAgIAAxkBAAIJh18Wgt_mA7aRrzJzPAh995hVLlkLAAJDAAO2j0oJI4YMUY38hXsaBA",
            "CAACAgIAAxkBAAIJiF8WguroLYVozNpYo9jyAw6PjCCJAALTAwACxKtoC5_3eyr8VMfsGgQ",
            "CAACAgIAAxkBAAIJiV8WgvoQ_4OBLhzuTW87dLph0Uv3AAIqAAOhjEELUTaM8bmmvIMaBA",
            "CAACAgIAAxkBAAIJil8Wgwh1N__fBLpG-4Dj3W5cm3sgAAIiBQACa8TKCnMzzBLXvW-LGgQ"
        )

        fun getFlagSticker(chatId: Long, flag: String): SendSticker {

            val sticker = SendSticker()
            var msgText = ""
            val result = DatabaseHelper.onPlayerPassedFlag(chatId, flag)

            when (result.first) {
                DatabaseHelper.FLAG_RESULT_SUCCESS -> {
                    sticker.setSticker(niceStickers.random())
                    msgText = "Отично! +${result.second}"
                }

                DatabaseHelper.FLAG_RESULT_WRONG -> {
                    sticker.setSticker(badStickers.random())
                    msgText = "Ты не прав, подумай ещё"
                }

                DatabaseHelper.FLAG_RESULT_ALREADY_SOLVED -> {
                    sticker.setSticker(niceStickers.random())
                    msgText = "Этот флаг ты уже сдал"
                }
            }

            sticker.chatId = chatId.toString()
            sticker.replyMarkup = InlineKeyboardMarkup(
                listOf(listOf(InlineKeyboardButton().setText(msgText).setCallbackData(DATA_MENU)))
            )

            return sticker
        }

        fun getFlagMessage(chatId: Long, flag: String): SendMessage {

            var msgText = ""

            val result = DatabaseHelper.onPlayerPassedFlag(chatId, flag)
            when (result.first) {
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
                DatabaseHelper.addPlayer(chatId, userName ?: firstName)
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
                                "${task.category} - ${task.price}: ${task.name} ${if (taskSolved) "\u2705" else ""}"
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