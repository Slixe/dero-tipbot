# DERO Tip Bot

The DERO Tip Bot is a Discord bot made in Java using the Krobot Framework and ArangoDB.


## How to build

```
git clone https://git.dero.io/slixe/dero-tipbot.git
cd dero-tipbot
git clone https://github.com/krobot-framework/krobot.git
./gradlew fatJar
```
- The final executable jar is in `build/libs` folder.

## How to launch

- If you want to set the bot token as argument, just replace the `[bot_token]` with your current application's bot token from [here](https://discordapp.com/developers/applications/). ~~If you do not pass it as an argument, the program will ask you for it at launch.~~

```
java -jar "DERO - TipBot-all-1.0.0.jar" [bot_token]
```

- Your bot token will be saved in the `.token` file.

- You must configure `arango.json`, `general.json` and `wallet.json` in the generated folder `config`.


## Features

- Auto launch on start for wallet
- Configurable Embed message (icon, color).
- Configurable messages
- Notify when receiving a deposit
- Network information

### Commands

##### Public commands:

- /balance
- /withdraw
- /help
- /info
- /tip

#### Administrators commands:

- /shutdown-wallet
- /shutdown-bot
- /give (for debug only)

## Donations

DERO: 
```
dERokevAZEZVJ2N7o39VH81BXBqX9ojtncnPTDMyiVbmYiTXQY93AUCLcor9xsWCKWhYy25ja89ikZWXWab9kXRB7LYfUmbQyS
```
