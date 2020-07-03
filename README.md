# CTF Bot

## Description
Telegram bot that is being used to organize CTF trainings in Donetsk National University.

## Usage
0) Download the bot .jar file [CTF_Bot.jar](out/artifacts/CTF_Bot_jar/CTF_Bot.jar) or build bot from sources. To build bot from sources you should clone project, open it in IntelliJ IDEA and click

    Build -> Build Artifacts -> CTF_Bot:jar  
1) Create folder "db" in the folder where bot is placed.
2) Put there file "tasks.txt" where all tasks are listed in the following format:

	taskId:|:taskCategory:|:taskName:|:taskCost:|:taskFlag
	
	Example:
	
	2:|:Crypto:|:bad_crypto:|:300:|:donnuCTF{d0n7_us3_y0ur_0wn_cryp70}
3) Create folder "tasks" in the folder where bot is placed.
4) For every task in "tasks.txt" create folder with name equals to task name.
5) In every folder put two files. First - "text.txt" with task description in it. Second - zip file with name "{taskName}.zip".
6) Put BotCredentials file to the folder where bot is placed filled as folows:
   
    token:|:<i>your_bot_token</i>\
    botName:|:<i>your_bot_name</i>
7) Run the CTF_Bot.jar and press "Start bot" or enter testing password and press "Start testing bot"
    
Nor task directory neither task files are neccessary, although you should have at least one.
