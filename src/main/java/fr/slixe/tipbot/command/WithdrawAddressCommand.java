package fr.slixe.tipbot.command;

import org.krobot.MessageContext;
import org.krobot.command.ArgumentMap;
import org.krobot.command.Command;
import org.krobot.command.CommandHandler;

import com.google.inject.Inject;

import fr.slixe.tipbot.ArangoDatabaseService;
import fr.slixe.tipbot.TipBot;
import fr.slixe.tipbot.User;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.PrivateChannel;

@Command(value = "withdraw-address <address>", desc = "set your default withdraw address", errorMP = true)
public class WithdrawAddressCommand implements CommandHandler {

	@Inject
	private TipBot bot;
	
	@Inject
	private ArangoDatabaseService db;
	
	@Override
	public Object handle(MessageContext ctx, ArgumentMap args) throws Exception
	{
		MessageChannel chan = ctx.getChannel();

		if (!(chan instanceof PrivateChannel))
		{
			chan = ctx.getUser().openPrivateChannel().complete();
		}

		String address = args.get("address");
		//TODO verify validity of address

		//TODO update it directly
		User user = db.getUser(ctx.getUser().getId());
		user.setWithdrawAddress(address);

		db.updateUser(user);

		chan.sendMessage(bot.dialog("Withdraw Address", "Your withdrawal address has been set!")).queue();

		return null;
	}

}
