package fr.slixe.tipbot.command;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

import javax.inject.Inject;

import org.krobot.MessageContext;
import org.krobot.command.ArgumentMap;
import org.krobot.command.Command;
import org.krobot.command.CommandHandler;

import fr.slixe.tipbot.TipBot;
import fr.slixe.tipbot.Wallet;
import net.dv8tion.jda.core.entities.User;

@Command(value = "tip <to:user> <amount>", desc = "Send some DERO to your friends", errorMP = true)
public class TipCommand implements CommandHandler {
	
	private static final DecimalFormat format = new DecimalFormat("#.############");
	
	@Inject
	private Wallet wallet;
	
	@Inject
	private TipBot bot;
	
	@Override
	public Object handle(MessageContext ctx, ArgumentMap args) throws Exception 
	{
		User to = args.get("to");		

		if (to.isBot())
			throw new CommandException(bot.getMessage("tip.err.tip-bot"));
		
		if (to.getId().equals(ctx.getUser().getId()))
			throw new CommandException(bot.getMessage("tip.err.tip-yourself"));
		
		BigDecimal amount;
		try {
			amount = new BigDecimal(args.get("amount", String.class)).setScale(12, RoundingMode.DOWN);
			if (amount.signum() != 1)
				throw new CommandException(bot.getMessage("tip.err.positive-value"));
			if (amount.scale() > 12)
				throw new CommandException(bot.getMessage("tip.err.scale"));
		}
		catch (NumberFormatException e)
		{
			throw new CommandException(String.format(bot.getMessage("tip.err.invalid-amount"), ctx.getUser().getAsMention()));
		}

		String id = ctx.getUser().getId();
		if(!wallet.hasEnoughFunds(id, amount))
			throw new CommandException(bot.getMessage("tip.err.not-enough"));

		wallet.removeFunds(id, amount);
		wallet.addFunds(to.getId(), amount);
		
		String strAmount = format.format(amount);

		to.openPrivateChannel().queue((e) -> {
			e.sendMessage(bot.dialog("Tip incoming!", String.format(bot.getMessage("tip.incoming"), strAmount, ctx.getUser().getAsTag()))).queue();
		});
		ctx.getUser().openPrivateChannel().queue((e) -> {
			e.sendMessage(bot.dialog("Tip Bot", String.format(bot.getMessage("tip.sent"), strAmount, to.getAsMention()))).queue();
		});
		
		return bot.dialog("Tip Bot", String.format(bot.getMessage("tip.general"), ctx.getUser().getAsMention(), strAmount, to.getAsMention()));
	}
}
