package fr.slixe.tipbot.command;

import javax.inject.Inject;

import org.json.JSONObject;
import org.krobot.MessageContext;
import org.krobot.command.ArgumentMap;
import org.krobot.command.Command;
import org.krobot.command.CommandHandler;

import fr.slixe.dero4j.RequestException;
import fr.slixe.tipbot.TipBot;
import fr.slixe.tipbot.Wallet;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.PrivateChannel;

@Command(value = "info", desc = "DERO Network information", errorMP = true)
public class InfoCommand implements CommandHandler
{
	@Inject
	private Wallet wallet;

	@Inject
	private TipBot bot;
	
	@Override
	public Object handle(MessageContext ctx, ArgumentMap args) throws Exception
	{
		MessageChannel chan = ctx.getChannel();
		
		if (!(chan instanceof PrivateChannel))
		{
			chan = ctx.getUser().openPrivateChannel().complete();
		}

		int walletHeight = 0;	
		
		try {
			walletHeight = this.wallet.getApi().getHeight();
		} catch (RequestException e) {
			e.printStackTrace();
			throw new CommandException("Wallet isn't available!");
		}
		
		chan.sendMessage(bot.dialog("Wallet information", "Height: " + walletHeight)).queue();
		
		int height; //stable_height or height?
		int topoHeight; //topoheight
		double blockTime;
		int difficulty;
		int txMempool; //tx_pool_size
		int totalSupply;
		String daemonVersion;
		
		try {
			JSONObject json = bot.getDaemon().getInfo();
			
			height = json.getInt("height");
			topoHeight = json.getInt("topoheight");
			blockTime = json.getDouble("averageblocktime50");
			difficulty = json.getInt("difficulty");
			txMempool = json.getInt("tx_pool_size");
			totalSupply = json.getInt("total_supply");
			daemonVersion = json.getString("version");
		} catch (RequestException e)
		{
			throw new CommandException("Daemon isn't available!");
		}
		
		StringBuilder builder = new StringBuilder();
		builder.append("Height / Topoheight: ").append(height + " / " + topoHeight).append("\n");
		builder.append("Average Block Time: ").append(blockTime).append("\n");
		builder.append("Difficulty: ").append(difficulty).append("\n");
		builder.append("Mempool: ").append(txMempool).append("\n");
		builder.append("Total Supply: ").append(totalSupply).append("\n");
		builder.append("Daemon Version: ").append(daemonVersion).append("\n");

		chan.sendMessage(bot.dialog("Network information", builder.toString())).queue();

		return null;
	}
}
