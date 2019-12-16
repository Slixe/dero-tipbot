package fr.slixe.tipbot.command;

import java.math.BigDecimal;

import org.krobot.MessageContext;
import org.krobot.command.ArgumentMap;
import org.krobot.command.Command;
import org.krobot.command.CommandHandler;

import com.google.inject.Inject;

import fr.slixe.tipbot.TipBot;
import fr.slixe.tipbot.User;
import fr.slixe.tipbot.Wallet;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.PrivateChannel;

@Command(value = "balance", desc = "get your balance and deposit address", errorMP = true)
public class BalanceCommand implements CommandHandler {

	@Inject
	private Wallet wallet;
	
	@Inject
	private TipBot bot;
	
	@Override
	public Object handle(MessageContext ctx, ArgumentMap args) throws Exception 
	{
		String id = ctx.getUser().getId();
		User user = wallet.getDB().getUser(id);

		BigDecimal funds = user.getBalance();
		BigDecimal unconfirmedFunds = user.getUnconfirmedBalance();

		MessageChannel chan = ctx.getChannel();

		if (!(chan instanceof PrivateChannel))
		{
			chan = ctx.getUser().openPrivateChannel().complete();
		}

		String address = "No deposit address until you've set a withdraw address.";

		if (user.getWithdrawAddress() != null)
		{
			if (user.getAddress() != null)
				address = user.getAddress();
			else
				address = wallet.getNewAddress(user.getKey());
		}

		chan.sendMessage(bot.dialog("Balance", String.format(bot.getMessage("balance"), funds, unconfirmedFunds, address))).queue();

		return null;
	}
}
