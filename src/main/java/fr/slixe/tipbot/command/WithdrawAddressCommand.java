package fr.slixe.tipbot.command;

import org.krobot.MessageContext;
import org.krobot.command.ArgumentMap;
import org.krobot.command.Command;
import org.krobot.command.CommandHandler;

import com.google.inject.Inject;

import fr.slixe.tipbot.ArangoDatabaseService;
import fr.slixe.tipbot.TipBot;
import fr.slixe.tipbot.User;

@Command(value = "withdraw-address [address]", desc = "set your default withdraw address", errorMP = true, handleMP = true)
public class WithdrawAddressCommand implements CommandHandler {

	@Inject
	private TipBot bot;
	
	@Inject
	private ArangoDatabaseService db;
	
	@Override
	public Object handle(MessageContext ctx, ArgumentMap args) throws Exception
	{
		if (!args.has("address"))
		{
			String wAddress = this.db.getWithdrawAddress(ctx.getUser().getId());
			String msg = wAddress != null ? ": " + wAddress : " not set.\nPlease do ```/withdraw-address <address>```";

			return bot.dialog("Withdraw Address", "Your withdrawal address is" + msg);
		}

		String address = args.get("address");
		//TODO verify validity of address

		//TODO update it directly
		User user = db.getUser(ctx.getUser().getId());
		user.setWithdrawAddress(address);

		db.updateUser(user);

		return bot.dialog("Withdraw Address", "Your withdrawal address has been set!");
	}

}
