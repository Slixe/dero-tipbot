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
		
		/*if (funds.equals("0") || funds.equals("0E-12")) //dirty i know :/
			funds = "0.000000000000";
		
		if (unconfirmedFunds.equals("0") || unconfirmedFunds.equals("0E-12"))
			unconfirmedFunds = "0.000000000000";*/

		String address = wallet.getAddress(id);

		if (user.getWithdrawAddress() == null)
			address = "No deposit address until you've set a withdraw address.";

		chan.sendMessage(bot.dialog("Balance", String.format(bot.getMessage("balance"), funds, unconfirmedFunds, address))).queue();

		return null;
	}
}
