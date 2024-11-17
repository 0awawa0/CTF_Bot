package bot

import bot.Bot.Companion.DATA_COMMANDS
import bot.Bot.Companion.DATA_CURRENT_SCOREBOARD
import bot.Bot.Companion.DATA_CURRENT_SCOREBOARD_PAGE
import bot.Bot.Companion.DATA_FILE
import bot.Bot.Companion.DATA_GLOBAL_SCOREBOARD
import bot.Bot.Companion.DATA_GLOBAL_SCOREBOARD_PAGE
import bot.Bot.Companion.DATA_MENU
import bot.Bot.Companion.DATA_TASKS
import bot.features.magic.MagicNumbers
import bot.features.numbers.NumbersUtils
import bot.features.rot.Rot
import bot.utils.Helper
import database.DbHelper
import database.PlayerModel
import org.telegram.telegrambots.meta.api.methods.send.SendDocument
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.send.SendSticker
import org.telegram.telegrambots.meta.api.objects.CallbackQuery
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import ui.compose.shared.dto.Score
import utils.Logger
import java.io.File
import java.lang.ref.WeakReference


class MessageMaker(private val bot: WeakReference<Bot>) {

    private val tag = "MessageMaker"

    //This chars must be escaped in markdown
    //'_', '*', '[', ']', '(', ')', '~', '`', '>', '#', '+', '-', '=', '|', '{', '}', '.', '!'

    private val allowedCharacters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890".toSet() +
            "абвгдеёжзийклмнопрстуфхцчшщъыьэюяАБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ_-.".toSet()

    private val niceSticker: String
        get() = Stickers.getRandomNice()

    private val badSticker: String
        get() = Stickers.getRandomBad()

    suspend fun getFlagSticker(message: Message, flag: String): SendSticker? {
        val start = System.nanoTime()
        val bot = bot.get() ?: return null
        val sticker = SendSticker()
        var msgText = ""

        when (val result = DbHelper.onFlagPassed(bot.competition, message.chatId, flag)) {
            is DbHelper.FlagCheckResult.CorrectFlag -> {
                sticker.sticker = InputFile(niceSticker)
                msgText = "Отлично! +${result.price}"
            }
            is DbHelper.FlagCheckResult.WrongFlag -> {
                sticker.sticker = InputFile(badSticker)
                msgText = "Нет такого флага"
            }
            is DbHelper.FlagCheckResult.SolveExists -> {
                sticker.sticker = InputFile(niceSticker)
                msgText = "Этот флаг ты уже сдал"
            }

            is DbHelper.FlagCheckResult.NoSuchPlayer -> {
                sticker.sticker = InputFile(badSticker)
                msgText = "Упс, кажется мы не знакомы... Выполни команду /start"
            }
        }

        sticker.chatId = message.chatId.toString()
        val menuButton = InlineKeyboardButton(msgText)
        menuButton.callbackData = DATA_MENU
        sticker.replyMarkup = InlineKeyboardMarkup(listOf(listOf(menuButton)))

        val end = System.nanoTime()
        Logger.debug(tag, "Prepared flag sticker in: ${(end - start) / 1000000} ms")
        return sticker
    }

    suspend fun getStartMessage(message: Message): SendMessage {
        val userName = message.from.userName ?: message.from.firstName
        return getStartMessage(userName, message.chatId)
    }

    suspend fun getStartMessage(callback: CallbackQuery): SendMessage {
        val userName = callback.from.userName ?: callback.from.firstName
        return getStartMessage(userName, callback.message.chatId)
    }

    private suspend fun getStartMessage(userName: String, chatId: Long): SendMessage {
        val start = System.nanoTime()
        val usrName = userName.filter { it in allowedCharacters }
        val playerName = usrName.let { if (it.length > 16) it.substring(0..15) else it }
        val player = DbHelper.getPlayer(chatId) ?: DbHelper.add(PlayerModel(chatId, playerName))
            ?: return getErrorMessage(chatId)

        val msgText = "Привет, я  CTF-бот! Я помогаю проводить соревнования по CTF - раздаю задания и проверяю флаги." +
                "Я буду называть тебя ${player.name}, если ты не против.\n\n" +
                "Чтобы сменить имя, используй команду:\n<i>${Bot.MSG_CHANGE_NAME} __новое_имя__</i>\n" +
                "Только постарайся уложиться в 16 " +
                "символов - больше я не запомню :)\n\n" +
                "Чтобы удалить себя из моей базы данных пришли команду:\n<i>${Bot.MSG_DELETE} __своё_текущее_имя__</i>\n\n" +
                "Подробнее о доступных командах:\n<i>${Bot.MSG_COMMANDS_HELP}</i>"

        val menuButton = InlineKeyboardButton()
        menuButton.text = "Меню"
        menuButton.callbackData = DATA_MENU

        val msg = SendMessage()
        msg.text = msgText
        msg.replyMarkup = InlineKeyboardMarkup(listOf(listOf(menuButton)))
        msg.chatId = chatId.toString()
        msg.enableHtml(true)

        val end = System.nanoTime()
        Logger.debug(tag, "Prepared start message in ${(end - start) / 1000000} ms")
        return msg
    }

    suspend fun getMenuMessage(callback: CallbackQuery): SendMessage {
        val userName = callback.from.userName ?: callback.from.firstName
        return getMenuMessage(userName, callback.message.chatId)
    }

    suspend fun getMenuMessage(message: Message): SendMessage {
        val userName = message.from.userName ?: message.from.firstName
        return getMenuMessage(userName, message.chatId)
    }

    private suspend fun getMenuMessage(userName: String, chatId: Long): SendMessage {
        val start = System.nanoTime()
        val bot = bot.get() ?: return getErrorMessage(chatId)
        val player = DbHelper.getPlayer(chatId) ?: return getStartMessage(userName, chatId)
        val (competitionScore, totalScore) = DbHelper.getCompetitionAndTotalScores(player, bot.competition)

        val msgText = """<b>${bot.competition.name}</b>
                |
                |Привет, <i>${player.name}</i>! Твой текущий счёт: $competitionScore. 
                |Твой общий счёт: $totalScore.
                |Для управления используй кнопки. Чтобы сдать флаг напиши
                |<i>${Bot.MSG_FLAG} __твой_флаг__</i>
                |""".trimMargin()

        val buttonRow1 = listOf(
            InlineKeyboardButton("Текущая таблица лидеров").apply { callbackData = DATA_CURRENT_SCOREBOARD },
            InlineKeyboardButton("Общая таблица лидеров").apply { callbackData = DATA_GLOBAL_SCOREBOARD }
        )
        val buttonRow2 = listOf(
            InlineKeyboardButton("Задания").apply { callbackData = DATA_TASKS }
        )
        val buttonRow3 = listOf(
            InlineKeyboardButton("Доступные команды").apply { callbackData = DATA_COMMANDS }
        )
        val buttonsTable = listOf(buttonRow1, buttonRow2, buttonRow3)

        val msg = SendMessage()
        msg.chatId = chatId.toString()
        msg.enableHtml(true)
        msg.text = msgText
        msg.replyMarkup = InlineKeyboardMarkup(buttonsTable)

        val end = System.nanoTime()
        Logger.debug(tag, "Prepared menu message in ${(end - start) / 1000000} ms")
        return msg
    }

    suspend fun getPasswordRequestMessage(chatId: Long): SendMessage {
        val msgText = "Бот находится в состоянии тестирования. " +
                "Для авторизации пришли мне пароль в формате:\n<i>${Bot.MSG_TESTING_PASSWORD} __пароль__</i>"
        val msg = SendMessage()
        msg.chatId = chatId.toString()
        msg.text = msgText
        msg.enableHtml(true)
        return msg
    }

    suspend fun getPasswordWrongMessage(message: Message): SendMessage {
        val msg = SendMessage()
        msg.chatId = message.chatId.toString()
        msg.text = "Неверный пароль. Доступ запрещён"
        return msg
    }


    suspend fun getTasksMessage(callback: CallbackQuery): SendMessage {
        val start = System.nanoTime()
        val bot = bot.get() ?: return getErrorMessage(callback.message.chatId)
        val player = DbHelper.getPlayer(callback.message.chatId) ?: return getStartMessage(callback)
        val msgText = "<b>${bot.competition.name}</b>\n\nСписок заданий: "
        val buttonsList = arrayListOf<List<InlineKeyboardButton>>()

        val tasksList = DbHelper.getTasksList(player, bot.competition)
        for (task in tasksList) {
            val taskSolved = task.third
            val taskPrice = task.second

            buttonsList.add(listOf(
                InlineKeyboardButton(
                    "${task.first.category} - ${taskPrice}: ${task.first.name} ${if (taskSolved) "\u2705" else ""}"
                ).apply { callbackData = "${Bot.DATA_TASK} ${task.first.id}" }
            ))
        }

        buttonsList.add(listOf(
            InlineKeyboardButton(
                "Menu"
            ).apply { callbackData = DATA_MENU }
        ))

        val msg = SendMessage()
        msg.enableHtml(true)
        msg.chatId = callback.message.chatId.toString()
        msg.text = msgText
        msg.replyMarkup = InlineKeyboardMarkup(buttonsList)

        val end = System.nanoTime()
        Logger.debug(tag, "Prepared task list message in ${(end - start) / 1000000}")
        return msg
    }


    suspend fun getCurrentScoreboard(callback: CallbackQuery): SendMessage {
        return getCurrentScoreboardPage(1, callback)
    }

    suspend fun getCurrentScoreboardPage(page: Int, callback: CallbackQuery): SendMessage {
        val start = System.nanoTime()
        val bot = bot.get() ?: return getErrorMessage(callback.message.chatId)
        val scoreboard = DbHelper.getScoreboard(bot.competition)

        val pagesCount = scoreboard.size / ScoreBoardPageSize + (scoreboard.size % ScoreBoardPageSize != 0).toInt()
        if (page > pagesCount) { return getErrorMessage(callback.message.chatId) }

        var pageText = """
                <b>${bot.competition.name}</b>
                |
                |Таблица лидеров по текущей игре:
                |
                |<code>
                """.trimMargin()

        val pageStartIdx = (page - 1) * ScoreBoardPageSize
        for (idx in pageStartIdx until minOf(pageStartIdx + ScoreBoardPageSize, scoreboard.size)) {
            val player = scoreboard[idx]
            val name = player.first.padEnd(16, ' ')
            val number = (idx + 1).toString().padStart(3, ' ')
            val score = player.second.toString().padStart(6, ' ')
            pageText += "%s. %s %s\n".format(number, name, score)
        }
        pageText += "</code>\nСтраница $page из $pagesCount"

        val pageMessage = SendMessage()
        pageMessage.chatId = callback.message.chatId.toString()
        pageMessage.enableHtml(true)
        pageMessage.text = pageText
        pageMessage.replyMarkup = InlineKeyboardMarkup(listOf(
            listOf(
                InlineKeyboardButton("Меню").apply { callbackData = DATA_MENU }
            ),
            if (page < pagesCount) {
                listOf(InlineKeyboardButton("Следующая страница").apply { callbackData = "$DATA_CURRENT_SCOREBOARD_PAGE${page + 1}" })
            } else {
                emptyList()
            }
        ))

        val end = System.nanoTime()
        Logger.debug(tag, "Prepared current scoreboard message in ${(end - start) / 1000000} ms")
        return pageMessage
    }

    suspend fun getGlobalScoreboard(callback: CallbackQuery): SendMessage{
        return getGlobalScoreBoardPage(1, callback)
    }

    suspend fun getGlobalScoreBoardPage(page: Int, callback: CallbackQuery): SendMessage {
        val start = System.nanoTime()
        val bot = bot.get() ?: return getErrorMessage(callback.message.chatId)
        val scoreboard = DbHelper.getScoreboard()
        val pagesCount = scoreboard.size / ScoreBoardPageSize + (scoreboard.size % ScoreBoardPageSize != 0).toInt()

        if (page > pagesCount) { return getErrorMessage(callback.message.chatId) }

        var pageText = """
               <b>${bot.competition.name}</b>
               |
               |Таблица лидеров по всем играм:
               |
               |<code>
               """.trimMargin()

        val pageStartIdx = (page - 1) * ScoreBoardPageSize
        for (idx in pageStartIdx until minOf(pageStartIdx + ScoreBoardPageSize, scoreboard.size)) {
            val player = scoreboard[idx]
            val name = player.first.padEnd(16, ' ')
            val number = (idx + 1).toString().padStart(3, ' ')
            val score = player.second.toString().padStart(6, ' ')
            pageText += "%s. %s %s\n".format(number, name, score)
        }
        pageText += "</code>\nСтраница $page из $pagesCount"

        val pageMessage = SendMessage()
        pageMessage.chatId = callback.message.chatId.toString()
        pageMessage.enableHtml(true)
        pageMessage.text = pageText
        pageMessage.replyMarkup = InlineKeyboardMarkup(listOf(
            listOf(
                InlineKeyboardButton("Меню").apply { callbackData = DATA_MENU }
            ),
            if (page < pagesCount) {
                listOf(InlineKeyboardButton("Следующая страница").apply { callbackData = "$DATA_GLOBAL_SCOREBOARD_PAGE${page + 1}" })
            } else {
                emptyList()
            }
        ))
        val end = System.nanoTime()
        Logger.debug(tag, "Prepared global scoreboard in ${(end - start) / 1000000} ms")
        return pageMessage
    }


    suspend fun getTaskMessage(callback: CallbackQuery, taskId: Long): SendMessage {
        val start = System.nanoTime()
        val bot = bot.get() ?: return getErrorMessage(callback.message.chatId)
        val task = DbHelper.getTask(taskId) ?: return getErrorMessage(callback.message.chatId)
        val attachmentFile = File(task.attachment)
        val taskPrice = DbHelper.getTaskPrice(task)

        val msgText = "<b>${bot.competition.name}</b>\n" +
                "\n${task.name}           ${taskPrice}\n\n${task.description}"
        val msg = SendMessage()
        msg.enableHtml(true)

        msg.chatId = callback.message.chatId.toString()
        msg.text = msgText

        val buttons = arrayListOf<List<InlineKeyboardButton>>()
        if (attachmentFile.exists()) {
            buttons.add(
                listOf(InlineKeyboardButton(attachmentFile.name).apply { callbackData = "$DATA_FILE $taskId" })
            )
        }
        buttons.add(listOf(InlineKeyboardButton("Меню").apply { callbackData = DATA_MENU }))
        msg.replyMarkup = InlineKeyboardMarkup(buttons)

        val end = System.nanoTime()
        Logger.debug(tag, "Prepared task message in ${(end - start) / 1000000} ms")
        return msg
    }

    suspend fun getFileMessage(callback: CallbackQuery, taskId: Long): SendDocument? {
        val start = System.nanoTime()
        val task = DbHelper.getTask(taskId) ?: return null
        val attachmentFile = File(task.attachment)
        if (!attachmentFile.exists()) return null

        val msg = SendDocument()
        msg.chatId = callback.message.chatId.toString()
        msg.document = InputFile(attachmentFile, attachmentFile.name)
        msg.replyMarkup = InlineKeyboardMarkup(listOf(listOf(
            InlineKeyboardButton("Меню").apply { callbackData =  DATA_MENU}
        )))

        val end = System.nanoTime()
        Logger.debug(tag, "Prepared task file message in ${(end - start) / 1000000} ms")
        return msg
    }

    suspend fun getErrorMessage(chatId: Long): SendMessage {
        val msgText = "Ой, возникла какая-то ошибка. Свяжитесь с @awawa0_0 для обратной связи."
        val msg = SendMessage()
        msg.chatId = chatId.toString()
        msg.replyMarkup = InlineKeyboardMarkup(
            listOf(listOf(InlineKeyboardButton("Меню").apply { callbackData = DATA_MENU }))
        )
        msg.text = msgText
        return msg
    }

    suspend fun getUnknownMessage(message: Message): SendMessage {
        val msgText = "Это что? Эльфийский? Я не понимаю. Используй кнопки, пожалуйста."
        val buttonsTable = listOf(listOf(InlineKeyboardButton("Меню").apply { callbackData = DATA_MENU }))

        val msg = SendMessage()
        msg.chatId = message.chatId.toString()
        msg.text = msgText
        msg.replyMarkup = InlineKeyboardMarkup(buttonsTable)

        return msg
    }

    suspend fun getConvertMessage(message: Message, content: String): SendMessage {
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

        msg.chatId = message.chatId.toString()
        msg.text = msgText
        msg.replyMarkup = InlineKeyboardMarkup(
            listOf(
                listOf(
                    InlineKeyboardButton("Меню").apply { callbackData = DATA_MENU }
                )
            )
        )

        return msg
    }

    suspend fun getToHexMessage(message: Message, content: String): SendMessage {

        val msg = SendMessage()
        var msgText = ""
        val numbers = content.split(" ")

        for (number in numbers) {
            msgText += Helper.anyToHex(number)
        }

        msg.chatId = message.chatId.toString()
        msg.text = msgText
        msg.replyMarkup = InlineKeyboardMarkup(
            listOf(
                listOf(
                    InlineKeyboardButton("Меню").apply { callbackData = DATA_MENU }
                )
            )
        )

        return msg
    }

    suspend fun getToDecMessage(message: Message, content: String): SendMessage {
        val msg = SendMessage()
        var msgText = ""
        val numbers = content.split(" ")

        for (number in numbers) {
            msgText += Helper.anyToDec(number)
        }

        msg.chatId = message.chatId.toString()
        msg.text = msgText
        msg.replyMarkup = InlineKeyboardMarkup(
            listOf(
                listOf(
                    InlineKeyboardButton("Меню").apply { callbackData = DATA_MENU }
                )
            )
        )

        return msg
    }

    suspend fun getToBinMessage(message: Message, content: String): SendMessage {
        val msg = SendMessage()
        var msgText = ""
        val numbers = content.split(" ")

        for (number in numbers) {
            msgText += Helper.anyToBin(number)
        }

        msg.chatId = message.chatId.toString()
        msg.text = msgText
        msg.replyMarkup = InlineKeyboardMarkup(
            listOf(
                listOf(
                    InlineKeyboardButton("Меню").apply { callbackData = DATA_MENU }
                )
            )
        )

        return msg
    }

    suspend fun getToStringMessage(message: Message, content: String): SendMessage {
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

        msg.chatId = message.chatId.toString()
        msg.text = msgText
        msg.replyMarkup = InlineKeyboardMarkup(
            listOf(
                listOf(
                    InlineKeyboardButton("Меню").apply { callbackData = DATA_MENU }
                )
            )
        )

        return msg
    }

    suspend fun getMessageToPlayer(id: Long, text: String): SendMessage {
        val msg = SendMessage()
        msg.chatId = id.toString()
        msg.text = text
        msg.replyMarkup = InlineKeyboardMarkup(
            listOf(
                listOf(
                    InlineKeyboardButton("Меню").apply { callbackData = DATA_MENU }
                )
            )
        )

        return msg
    }

    suspend fun getRotMessage(message: Message, content: String): SendMessage {
        val msg = SendMessage()
        msg.chatId = message.chatId.toString()
        msg.replyMarkup = InlineKeyboardMarkup(
            listOf(
                listOf(
                    InlineKeyboardButton("Меню").apply { callbackData = DATA_MENU }
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

    suspend fun getRotBruteMessage(message: Message, content: String): SendMessage {
        val msg = SendMessage()
        msg.chatId = message.chatId.toString()

        val msgText = StringBuilder()
        for (key in 0 until Rot.ALPHABET_LENGTH) {
            msgText.append("Key: $key  Text: ${Rot.rotate(content, key)}\n")
        }
        msg.text = msgText.toString()
        msg.replyMarkup = InlineKeyboardMarkup(
            listOf(
                listOf(
                    InlineKeyboardButton("Меню").apply { callbackData = DATA_MENU }
                )
            )
        )

        return msg
    }

    suspend fun getCheckMagicMessage(message: Message, content: String): SendMessage {
        val msg = SendMessage()
        msg.chatId = message.chatId.toString()

        val msgText = StringBuilder()
        val replyMarkup = ArrayList<List<InlineKeyboardButton>>()

        val magicCheck = MagicNumbers.findMagic(content.trim())

        msgText.append("Результаты поиска")
        for ((i, match) in magicCheck.withIndex()) {
            replyMarkup.add(
                listOf(
                    InlineKeyboardButton(
                        "${i + 1}. ${match.first.formatName} - ${if (match.second) "Полное совпадение" else "Неполное совпадение"}"
                    ).apply {
                        match.first.callback
                    }
                )
            )
        }

        msg.text = msgText.toString()

        replyMarkup.add(listOf(InlineKeyboardButton("Меню").apply{ callbackData = DATA_MENU }))
        msg.replyMarkup = InlineKeyboardMarkup(replyMarkup)

        return msg
    }

    suspend fun getChangeNameMessage(message: Message, newName: String): SendMessage {
        val msg = SendMessage()
        msg.chatId = message.chatId.toString()
        if (newName.isBlank() || newName.length > 16 || newName.any { it !in allowedCharacters}) {
            val msgText = "Имя пользователя не соответствует требованиям формата и не будет изменено."
            msg.text = msgText
            msg.replyMarkup = InlineKeyboardMarkup(listOf(
                listOf(InlineKeyboardButton("Меню").apply { callbackData = DATA_MENU }))
            )
            return msg
        }

        val player = DbHelper.getPlayer(message.chatId) ?: return getStartMessage(message)
        player.name = newName
        player.updateEntity()
        val msgText = "Имя пользователя успешно изменено. Поздравляю, ты теперь официально: <b>${player.name}</b>!"
        msg.text = msgText
        msg.enableHtml(true)
        msg.replyMarkup = InlineKeyboardMarkup(listOf(
            listOf(InlineKeyboardButton("Меню").apply { callbackData = DATA_MENU }))
        )
        return msg
    }

    suspend fun getDeleteMessage(message: Message, content: String): SendMessage {
        val msg = SendMessage()
        msg.chatId = message.chatId.toString()
        val player = DbHelper.getPlayer(message.chatId) ?: return getStartMessage(message)
        if (content != player.name) {
            val msgText = "Нет, так не пойдёт. Чтобы удалить пользователя пришли своё имя."
            msg.text = msgText
            msg.replyMarkup = InlineKeyboardMarkup(listOf(
                listOf(InlineKeyboardButton("Меню").apply { callbackData = DATA_MENU }))
            )
            return msg
        }

        if (!DbHelper.delete(player)) return getErrorMessage(message.chatId)

        msg.text = "Пользователь удалён. Теперь я тебя не знаю."
        msg.replyMarkup = InlineKeyboardMarkup(listOf(
            listOf(InlineKeyboardButton("Меню").apply { callbackData = DATA_MENU }))
        )
        return msg
    }

    suspend fun getMagicData(callback: CallbackQuery, content: String): SendMessage {
        val msg = SendMessage()
        msg.chatId = callback.message.chatId.toString()
        msg.enableHtml(true)
        msg.text = MagicNumbers.getDataForMagic(content.trim())
        msg.replyMarkup = InlineKeyboardMarkup(listOf(
            listOf(InlineKeyboardButton("Меню").apply { callbackData = DATA_MENU }))
        )
        return msg
    }

    suspend fun getCommandsHelpMessage(message: Message): SendMessage {
        return getCommandsHelpMessage(message.chatId)
    }
    suspend fun getCommandsHelpMessage(callback: CallbackQuery): SendMessage {
        return getCommandsHelpMessage(callback.message.chatId)
    }

    private suspend fun getCommandsHelpMessage(chatId: Long): SendMessage {
        val msg = SendMessage()
        msg.chatId = chatId.toString()
        msg.text = """
                Список команд, поддерживаемых ботом. Заметьте, что бот распознаёт десятичные, двоичные и шестнадцатеричные числа. Двоичные числа должны иметь префикс '0b', а шестнадцатеричные '0x'.
                В массивах числа должны быть разделены пробелом. Числа ограничены диапазоном [0:9223372036854775807]

                ${Bot.MSG_FLAG} <string> - проверяет флаг. Если переданная строка является флагом к какому-либо заданию, это задание будет зачтено как решенное.
                
                ${Bot.MSG_COMMANDS_HELP} - показать это сообщение.
                
                ${Bot.MSG_DELETE} __твоё_имя_пользователя__ - удаляет текущего пользователя из базы данных. Имя пользователя требуется для подтверждения удаления.
                  
                ${Bot.MSG_CHANGE_NAME} __новое_имя_пользователя__ - меняет имя текущего пользователя на новое. Имя пользователя не является его уникальным идентификатором. Разные пользователи могут иметь одинаковые имена. Имя пользователя ограничено 16 символами из алфавита: $allowedCharacters 

                ${Bot.MSG_CONVERT} <array of numbers> - переводит массив чисел в двоичную, десятичную и шестнадцатеричную системы счисления.

                ${Bot.MSG_TO_HEX} <array of numbers> - переводит массив чисел в шестнадцатеричную систему счисления.

                ${Bot.MSG_TO_DEC} <array of numbers> - переводит массив чисел в десятичную систему счисления.

                ${Bot.MSG_TO_BIN} <array of numbers> - переводит массив чисел в двоичную систему счисления.

                ${Bot.MSG_TO_STRING} <array of numbers> - переводит массив чисел в одну строку. Числа ограничены 16 битами. Если передано число длиннее 16 бит, будут использованы младшие его 16 бит.

                ${Bot.MSG_ROT} <key> <text> - преобразует текст по алгоритму ROT13 (Шифрование Цезаря) с заданным ключом. Ключ может быть положительным или отрицательным.

                ${Bot.MSG_ROT_BRUTE} <text> - расшифровывает текст по алгоритму ROT13 (Шифрование Цезаря) со всеми возможными вариантами ключа.

                ${Bot.MSG_CHECK_MAGIC} <magic_number> - помогает определить тип файла по магическому числу. Магические числа должны быть указаны в шестнадцатеричном формате без префикса '0x', пример: ff d8. Магическими числами считаются не только сигнатуры файлов (первые n байт), но и другие, характерные для файлов последовательности. Например, "49 44 41 54" - сектор данных (IDAT) PNG файла.
            """.trimIndent()
        msg.replyMarkup = InlineKeyboardMarkup(
            listOf(
                listOf(
                    InlineKeyboardButton("Меню").apply { callbackData = DATA_MENU }
                )
            )
        )

        return msg
    }

    private fun Boolean.toInt() = if (this) 1 else 0

    companion object {
        private const val ScoreBoardPageSize = 25
        private const val MaxMessageSize = 4096
    }
}