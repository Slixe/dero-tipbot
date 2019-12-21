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

@Command(value = "balance", desc = "get your balance and deposit address", errorMP = true, handleMP = true)
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

		String address = "No deposit address until you've set a withdraw address.";

		if (user.getWithdrawAddress() != null)
		{
			if (user.getAddress() != null)
				address = user.getAddress();
			else
				address = wallet.getNewAddress(user.getKey());
		}

		return bot.dialog("Balance", String.format(bot.getMessage("balance"), funds, unconfirmedFunds, address));
	}
}
