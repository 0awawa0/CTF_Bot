package bot

import db.DatabaseHelper
import org.jetbrains.exposed.sql.transactions.transaction
import org.telegram.telegrambots.meta.api.methods.send.SendDocument
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import java.io.FileReader


class MessageMaker {

    companion object {

        fun getFlagMessage(chatId: Long, flag: String): SendMessage {
            val player = DatabaseHelper.getPlayerById(chatId)!!
            val msgText: String
            for (task in DatabaseHelper.tasks.values) {
                if (flag == task.flag) {
                    val solvedTasks = player.solvedTasks.split("|")
                    if (solvedTasks.contains(task.id.toString())) {
                        msgText = "Это задание ты уже решил, поздравляю! А теперь займись другими!"
                    } else {
                        msgText = "Верно! +${task.cost}"
                        transaction {
                            player.currentScore += task.cost
                            player.seasonScore += task.cost
                            player.solvedTasks += "${task.id}|"
                            DatabaseHelper.playersController.update()
                        }
                    }

                    val msg = SendMessage()
                    msg.text = msgText
                    msg.chatId = chatId.toString()
                    msg.replyMarkup = InlineKeyboardMarkup(
                            listOf(listOf(InlineKeyboardButton().setText("Меню").setCallbackData(DATA_MENU)))
                    )
                    return msg
                }
            }

            val msg = SendMessage()
            msg.text = "Ты не прав, подумай ещё."
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

            val msgText = "Ку, ${userName ?: firstName}! Твой текущий счёт: ${player?.currentScore ?: 0}. Твой счёт за сезон: ${player?.seasonScore ?: 0}\nДля управления используй кнопки. Чтобы сдать флаг напиши /flag \"твой флаг\""
            val buttonRow1 = listOf<InlineKeyboardButton>(
                    InlineKeyboardButton().setText("Таблица лидеров").setCallbackData(DATA_SCOREBOARD),
                    InlineKeyboardButton().setText("Задания").setCallbackData(DATA_TASKS)
            )
            val buttonsTable = listOf(buttonRow1)

            val msg = SendMessage()
            msg.chatId = chatId.toString()
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
            val msgText = "Список заданий: "
            val buttonsList = arrayListOf<List<InlineKeyboardButton>>()
            if (DatabaseHelper.checkPlayerInDatabase(chatId)) {
                for (task in DatabaseHelper.tasks.values) {
                    val taskSolved = task.id in DatabaseHelper.getSolvedTasksForPlayer(chatId)
                    buttonsList.add(listOf(
                            InlineKeyboardButton()
                                    .setText(
                                            "${task.category} - ${task.cost}: ${task.name} ${if (taskSolved) "\uD83D\uDDF8" else ""}"
                                    )
                                    .setCallbackData("/task ${task.id}")
                    ))
                }
            } else {
                return getErrorMessage(chatId)
            }

            val msg = SendMessage()
            msg.chatId = chatId.toString()
            msg.text = msgText
            msg.replyMarkup = InlineKeyboardMarkup(buttonsList)
            return msg
        }


        fun getScoreboardMessage(chatId: Long): SendMessage {
            val scoreboard = DatabaseHelper.getScoreboard()
            var msgText = "Таблица лидеров:\n"
            var i = 1
            for (position in scoreboard) {
                msgText += "$i.  ${position.first}               ${position.second}\n"
                i++
            }

            val msg = SendMessage()
            msg.chatId = chatId.toString()
            msg.text = msgText
            msg.replyMarkup = InlineKeyboardMarkup().setKeyboard(
                    listOf(listOf(InlineKeyboardButton().setText("Меню").setCallbackData(DATA_MENU)))
            )
            return msg
        }


        fun getTaskMessage(chatId: Long, taskId: Long): SendMessage {
            val files = DatabaseHelper.getTaskFiles(taskId)
            val task = DatabaseHelper.getTaskById(taskId)!!
            var msgText = "${task.name}           ${task.cost}\n"
            val msg = SendMessage()
            val textFile = files.find { it.name == "text.txt" }
            val content = files.find { it.nameWithoutExtension == task.name }

            if (textFile != null) {
                val fileReader = FileReader(textFile)
                msgText += fileReader.readText()
                fileReader.close()
            }

            msg.chatId = chatId.toString()
            msg.text = msgText

            val buttons = arrayListOf<List<InlineKeyboardButton>>()

            if (content != null) {
                buttons.add(listOf(InlineKeyboardButton().setText(content.name).setCallbackData("$DATA_FILE $taskId")))
            }

            buttons.add(listOf(InlineKeyboardButton().setText("Меню").setCallbackData(DATA_MENU)))
            msg.replyMarkup = InlineKeyboardMarkup(buttons)

            return msg
        }

        fun getFileMessage(chatId: Long, taskId: Long): SendDocument {
            val contentFile = DatabaseHelper.getTaskFiles(taskId).find { it.nameWithoutExtension == DatabaseHelper.getTaskById(taskId)!!.name}!!
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