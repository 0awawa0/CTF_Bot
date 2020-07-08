package bot

import db.DatabaseHelper
import org.jetbrains.exposed.sql.transactions.transaction
import org.telegram.telegrambots.meta.api.methods.send.SendDocument
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import java.io.FileReader
import java.util.*


class MessageMaker {

    companion object {

//        This chars must be escaped in markdown
//        '_', '*', '[', ']', '(', ')', '~', '`', '>', '#', '+', '-', '=', '|', '{', '}', '.', '!'
        var ctfName = ""

        fun getFlagMessage(chatId: Long, flag: String): SendMessage {
            val player = DatabaseHelper.getPlayerById(chatId)!!
            val msgText: String
            for (task in DatabaseHelper.getAllTasks()) {
                if (flag == task.flag) {
                    val solvedTasks = player.solvedTasks.split("|")
                    if (solvedTasks.contains(task.id.toString())) {
                        msgText = "<b>$ctfName</b>\n\nЭто задание ты уже решил, поздравляю! А теперь займись другими!"
                    } else {
                        msgText = "Верно! +${task.price}"
                        transaction {
                            player.currentScore += task.price
                            player.seasonScore += task.price
                            player.solvedTasks += "${task.id}|"
                            player.lastRightAnswer = Date().time
                            DatabaseHelper.playersController.update()
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
            }

            val msg = SendMessage()
            msg.text = "<b>$ctfName</b>\n\nТы не прав, подумай ещё."
            msg.enableHtml(true)
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
            val buttonsTable = listOf(buttonRow1)

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
    }
}