package fr.slixe.tipbot.command;

import java.text.DateFormat;
import java.util.Date;

import javax.inject.Inject;

import org.krobot.MessageContext;
import org.krobot.command.ArgumentMap;
import org.krobot.command.Command;
import org.krobot.command.CommandHandler;
import org.krobot.util.Dialog;
import org.krobot.util.Markdown;

import fr.slixe.dero4j.RequestException;
import fr.slixe.tipbot.Cache;
import fr.slixe.tipbot.Info;
import fr.slixe.tipbot.TipBot;
import fr.slixe.tipbot.Wallet;

@Command(value = "info", desc = "DERO Network information", errorMP = true, handleMP = true)
public class InfoCommand implements CommandHandler
{
	private static final DateFormat dateFormat = DateFormat.getInstance();
	
	@Inject
	private Wallet wallet;

	@Inject
	private TipBot bot;
	
	@Inject
	private Cache cache;
	
	@Override
	public Object handle(MessageContext ctx, ArgumentMap args) throws Exception
	{
		int walletHeight = 0;	
		
		try {
			walletHeight = this.wallet.getApi().getHeight();
			ctx.send(bot.dialog("Wallet information", "**Height**: " + walletHeight));
		} catch (RequestException ignored) {
			ctx.send(Dialog.error("Wallet information", "Wallet isn't available."));
			//throw new CommandException("Wallet isn't available!");
		}
		
		Info info = cache.getInfo();
		
		if (info == null) {
			throw new CommandException("The daemon isn't available.");
		}
		
		StringBuilder builder = new StringBuilder();
		builder.append("**Height / Topoheight:** ").append(info.getHeight() + " / " + info.getTopoHeight()).append("\n");
		builder.append("**Average Block Time:** ").append(info.getBlockTime()).append("s").append("\n");
		builder.append("**Difficulty:** ").append(info.getDifficulty()).append("\n");
		builder.append("**Mempool:** ").append(info.getTxMempool()).append("\n");
		builder.append("**Total Supply:** ").append(info.getTotalSupply()).append("\n");
		builder.append("**Daemon Version:** ").append(info.getDaemonVersion()).append("\n");
		builder.append("\n").append(Markdown.italic(dateFormat.format(new Date(info.getMillis()))));

		return bot.dialog("Network information", builder.toString());
	}
}