package fr.slixe.tipbot.command;

import java.math.BigDecimal;

import javax.inject.Inject;

import fr.slixe.tipbot.Wallet;
import org.krobot.MessageContext;
import org.krobot.command.ArgumentMap;
import org.krobot.command.Command;
import org.krobot.command.CommandHandler;
import org.krobot.util.Dialog;

import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.User;

@Command(value = "tip <to:user> <amount>", desc = "Send some DERO to your friends")
public class TipCommand implements CommandHandler {
	
	@Inject
	private Wallet wallet;
	
	@Override
	public Object handle(MessageContext ctx, ArgumentMap args) throws Exception 
	{
		User to = args.get("to");
		
		if (to.getId().equals(ctx.getUser().getId()))
			return ctx.error("Error!", "You can't tip yourself!");
		
		BigDecimal amount;
		try {
			amount = new BigDecimal(args.get("amount", String.class));
			if (amount.signum() != 1)
				return ctx.error("Error!", "Hey, please put a positive value.");
		}
		catch (NumberFormatException e)
		{
			return ctx.error("Error!", String.format("%s, invalid amount", ctx.getUser().getAsMention()));
		}
		
		String id = ctx.getUser().getId();
		if(!wallet.hasEnoughFunds(id, amount)) {
			return ctx.error("Error!", "You do not have enough **DERO**");
		}
		wallet.removeFunds(id, amount);
		wallet.addFunds(to.getId(), amount);

		PrivateChannel pc = to.openPrivateChannel().complete();
		
		if (pc != null)
		{
			pc.sendMessage(Dialog.info("Tip incoming!", String.format("Hey! You just received a tip of %s **DERO** from %s", amount, ctx.getUser().getAsTag()))).queue();
		}
		
		return ctx.info("Tip command", String.format("You have just sent %s **DERO** to %s!", amount, to.getAsMention()));
	}

}
