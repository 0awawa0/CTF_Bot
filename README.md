# CTF Bot

## Description
Telegram bot that is used to organize CTF trainings in Donetsk National University.

## Usage
0) Download the bot .jar file [CTF_Bot.jar](out/artifacts/CTF_Bot_jar/CTF_Bot.jar) or build bot from sources.
1) Create folder "db" in the folder where bot is placed.
2) If you have not register you bot yet, read documentation [Telegram APIs](https://core.telegram.org/api) or contact @BotFather bot through Telegram to register your bot. After registering your bot you will get <i>your_bot_token</i> and <i>your_bot_name</i>. Put BotCredentials file to the folder where bot is placed filled as follows:
   
    token:|:<i>your_bot_token</i>\
    botName:|:<i>your_bot_name</i>
    
3) Run the CTF_Bot.jar. **Note that Java 8 is required. Although the program might run on more recent versions of JVM, using some features still can cause an error.**

You don't need your own web server to make your bot reachable on the internet. That means bot running on your local machine will be reachable from all over the world. That makes hosting CTF competitions and trainings easier than ever.
## GUI

### Main window
![MainWindow](screenshots/MainWindow.png)

Top text field is for CTF event name. The CTF name will be used by bot to look for tasks in database.

"Start bot" button will start Telegram bot.

"Start testing bot" button will start the bot with testing password from "Testing password" field. Players will get access to bot features only after they will send this password to the bot.

"Players" and "Tasks" buttons will open Players window and Tasks window respectively.

### Players window
![PlayersWindow](screenshots/PlayersWindow.png)

"Send message to selected player" button sends message to player selected in the players table.

"Send message to all" button sends message to all players in the database. **Note that currently Telegram Bots API doesn't support broadcast messaging, so trying to send message to many chats immediately will cause an error 429. Therefore messages will be sent with 200 milliseconds interval, so broadcasting message to really big amount of players can take some time. For example, if you have 100 players in the database, it will take up to 20 seconds to send message to everybody.**  

"Refresh current scores" button sets "Current score" field for all users to 0. Other fields will remain untouched.

"Refresh all scores" button sets all fields for all users to default values (except of "Username" obviously).

"Delete player" button deletes currently selected user from database. Note that the program will not ask any confirmation for that and there are no way to restore data after that, so be careful with that.

"Delete all players" button completely cleans all data about players from database. And it will not ask confirmation as well as "Delete player" button.

"Save changes" button and "Cancel changes" button are used to save changed player info or cancel that changes. So when you change some values in the table you still need to press "Save changes" to actually change database. Changed but not committed changes are highlighted, you can see it on the screenshot below.

![ChangedPlayersTable](screenshots/ChangedPlayersTable.png)

### Tasks window

![TasksWindow](screenshots/TasksWindow.png)

"CTF name" is used to separate tasks for different CTF events. So if you want the tasks you add to be available in running Telegram bot, you have to set their CTF name equals to one you set on the Main window.

Other fields are speaking by themselves.

"Add files directory" button opens directory chooser that allow you to pick a directory in your filesystem where additional files for the tasks are laid out. Note that bot will send all files from that directory to players.

"Add task" button creates actually adds task to database according to filled fields.

Other buttons functionality is similar to Players window functionality


## Bot commands
Also bot has additional features that can be helpful during CTF. They can be used by commands:

<b>Bot can process positive integer hex, bin and decimal values. Hex values must be prefixed by `0x` and bin values must be prefixed by `0b`. If error occurs during number processing, bot will return -1.</b>
1. `/convert <array_of_numbers>` - converts numbers array to binary, hexadecimal and decimal values.
2. `/toBin <array_of_numbers>` - converts numbers array to binary.
3. `/toHex <array_of_numbers>` - converts numbers array to hexadecimal.
4. `/toDec <array_of_numbers>` - converts numbers array to decimal.
5. `/toString <array_of_numbers>` - converts numbers array to single string.
6. `/rot <key> <text>` - processes the text by ROT13 (Caesar encryption) algorithm with given key. Key can be positive or negative.
7. `/rotBruteForce <text>` - decrypts the text by ROT13 (Caesar encryption) algorithm with all possible variants of key.
8. `/checkMagic <magic_number>` - helps to determine file type by it's magic number. Magic numbers must be given in hexadecimal format without '0x' prefix, example: ff d8. Not only first n bytes (aka file signature) of file are considered as magic number, but also other patterns that are specific for file type. For example, '49 44 41 54' is a data sector (IDAT) of PNG file.