package fr.slixe.tipbot.command;

import org.krobot.MessageContext;
import org.krobot.command.ArgumentMap;
import org.krobot.command.Command;
import org.krobot.command.CommandHandler;
import org.krobot.util.Dialog;

import com.google.inject.Inject;

import fr.slixe.tipbot.Wallet;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.PrivateChannel;

@Command(value = "balance", desc = "get your balance and deposit address")
public class BalanceCommand implements CommandHandler {

	@Inject
	private Wallet wallet;
	
	@Override
	public Object handle(MessageContext ctx, ArgumentMap args) throws Exception 
	{
		String id = ctx.getUser().getId();
		String funds = wallet.getFunds(id).toString();
		String unconfirmedFunds = wallet.getUnconfirmedFunds(id).toString();
		
		MessageChannel chan = ctx.getChannel();
		
		if (!(chan instanceof PrivateChannel))
		{
			chan = ctx.getUser().openPrivateChannel().complete();
		}
		
		if (funds.equals("0") || funds.equals("0E-12")) //dirty i know :/
			funds = "0.000000000000";
		
		if (unconfirmedFunds.equals("0") || unconfirmedFunds.equals("0E-12"))
			unconfirmedFunds = "0.000000000000";
		
		MessageEmbed embed = Dialog.info("Balance", String.format("__**DERO balance**:__ %s\n__**Unconfirmed balance**:__ %s\n\n__**Deposit address**:__ %s", funds, unconfirmedFunds, wallet.getAddress(id)));
		
		chan.sendMessage(embed).queue();
		return null;
	}
}
