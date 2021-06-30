# CTF Bot

## Description
Telegram bot that is used to organize CTF trainings in Donetsk National University.

## Usage
0) Download the bot .jar file https://github.com/0awawa0/CTF_Bot/releases or build bot from sources.
1) If you have not registered your bot yet, read documentation [Telegram APIs](https://core.telegram.org/api) or contact @BotFather bot through Telegram to register your bot. After registering your bot you will get <i>your_bot_token</i> and <i>your_bot_name</i>. Put BotCredentials.json file to the folder where bot is placed filled as follows:
```json
{
    "token": "<i>your_bot_token</i>",
    "name": "<i>your_bot_name</i>"
}
```
    
2) Run the CTF_Bot.jar

You don't need your own web server to make your bot reachable on the internet. That means bot running on your local machine will be reachable from all over the world. That makes hosting CTF competitions and trainings easier than ever.
