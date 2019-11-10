package fr.slixe.tipbot.command;

import java.math.BigDecimal;

import javax.inject.Inject;

import org.krobot.MessageContext;
import org.krobot.command.ArgumentMap;
import org.krobot.command.Command;
import org.krobot.command.CommandHandler;
import org.krobot.util.Dialog;

import fr.slixe.tipbot.TipBot;
import fr.slixe.tipbot.Wallet;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.User;

@Command(value = "tip <to:user> <amount>", desc = "Send some DERO to your friends")
public class TipCommand implements CommandHandler {
	
	@Inject
	private Wallet wallet;
	
	@Inject
	private TipBot bot;
	
	@Override
	public Object handle(MessageContext ctx, ArgumentMap args) throws Exception 
	{
		User to = args.get("to");
		
		MessageChannel chan = ctx.getChannel();
		
		if (!(chan instanceof PrivateChannel))
		{
			chan = ctx.getUser().openPrivateChannel().complete();
		}
		
		if (to.getId().equals(ctx.getUser().getId()))
			return chan.sendMessage(Dialog.error("Error!", bot.getMessage("tip.err.tip-yourself")));
		
		BigDecimal amount;
		try {
			amount = new BigDecimal(args.get("amount", String.class));
			if (amount.signum() != 1)
				return chan.sendMessage(Dialog.error("Error!", bot.getMessage("tip.err.positive-value")));
		}
		catch (NumberFormatException e)
		{
			return chan.sendMessage(Dialog.error("Error!", String.format("tip.err.invalid-amount", ctx.getUser().getAsMention())));
		}
		
		String id = ctx.getUser().getId();
		if(!wallet.hasEnoughFunds(id, amount)) {
			return chan.sendMessage(Dialog.error("Error!", bot.getMessage("tip.err.not-enough")));
		}
		wallet.removeFunds(id, amount);
		wallet.addFunds(to.getId(), amount);

		to.openPrivateChannel().queue((e) -> {
			e.sendMessage(bot.dialog("Tip incoming!", String.format(bot.getMessage("tip.incoming"), amount, ctx.getUser().getAsTag()))).queue();
		});
		ctx.getUser().openPrivateChannel().queue((e) -> {
			e.sendMessage(bot.dialog("Tip command", String.format(bot.getMessage("tip.sent"), amount, to.getAsMention()))).queue();
		});
		
		return bot.dialog("Tip command", String.format(bot.getMessage("tip.general"), ctx.getUser().getAsMention(), amount, to.getAsMention()));
	}

}
