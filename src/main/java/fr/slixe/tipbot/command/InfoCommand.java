package fr.slixe.tipbot.command;

import javax.inject.Inject;

import org.krobot.MessageContext;
import org.krobot.command.ArgumentMap;
import org.krobot.command.Command;
import org.krobot.command.CommandHandler;
import org.krobot.util.Dialog;

import fr.slixe.dero4j.RequestException;
import fr.slixe.tipbot.TipBot;
import fr.slixe.tipbot.Wallet;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.PrivateChannel;

@Command(value = "info", desc = "DERO Network information")
public class InfoCommand implements CommandHandler
{
	@Inject
	private Wallet wallet;

	@Inject
	private TipBot bot;
	
	@Override
	public Object handle(MessageContext ctx, ArgumentMap args) //TODO
	{
		MessageChannel chan = ctx.getChannel();
		
		if (!(chan instanceof PrivateChannel))
		{
			chan = ctx.getUser().openPrivateChannel().complete();
		}
		
		int walletHeight = 0;
		int height = 0;
		int topoHeight = 0;
		int difficulty = 0;
		int txMempool = 0;
		long totalSupply = 0;
		
		try {
			walletHeight = this.wallet.getApi().getHeight();
			
		} catch (RequestException e) {
			e.printStackTrace();
			return chan.sendMessage(Dialog.error("Error!", "Wallet isn't available..."));
		}
						
		return chan.sendMessage(bot.dialog("Network information (WIP)", String.format("__**Current wallet height**__: %d"
				+ "\n\n__**DERO NETWORK**__\n\n__**Height / Topoheight**__: %d"
				+ " / %d\n\n__**Difficulty**__: %d"
				+ "\n\n__**Mempool**__: %d"
				+ "\n\n__**Total Supply**__: %d", walletHeight, height, topoHeight, difficulty, txMempool, totalSupply)));
	}
}
