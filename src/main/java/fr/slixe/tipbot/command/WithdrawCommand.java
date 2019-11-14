package fr.slixe.tipbot.command;

import java.math.BigDecimal;

import org.krobot.MessageContext;
import org.krobot.command.ArgumentMap;
import org.krobot.command.Command;
import org.krobot.command.CommandHandler;
import org.krobot.util.Dialog;

import com.google.inject.Inject;

import fr.slixe.dero4j.RequestException;
import fr.slixe.tipbot.TipBot;
import fr.slixe.tipbot.Wallet;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.PrivateChannel;

@Command(value = "withdraw <amount> <address>", desc = "withdraw your coins from bot", errorMP = true)
public class WithdrawCommand implements CommandHandler {

	@Inject
	private Wallet wallet;
	
	@Inject
	private TipBot bot;
	
	@Override
	public Object handle(MessageContext ctx, ArgumentMap args) throws Exception { //TODO add Withdraw to a task and not call withdraw directly..
		BigDecimal amount;
		
		MessageChannel chan = ctx.getChannel();
		
		if (!(chan instanceof PrivateChannel))
		{
			chan = ctx.getUser().openPrivateChannel().complete();
		}
		
		try {
			amount = new BigDecimal(args.get("amount", String.class));
			if (amount.signum() != 1)
				throw new CommandException(bot.getMessage("withdraw.err.positive-value"));
		}
		catch (NumberFormatException e)
		{
			throw new CommandException(String.format(bot.getMessage("withdraw.err.invalid-amount"), ctx.getUser().getAsMention()));
		}
		
		String address = args.get("address");
		String id = ctx.getUser().getId();
		
		if (!wallet.hasEnoughFunds(id, amount))
		{
			throw new CommandException(bot.getMessage("withdraw.err.not-enough"));
		}
		
		BigDecimal fee;
		try {
			fee = wallet.getApi().estimateFee(address, amount);
		} catch (RequestException e)
		{
			e.printStackTrace();
			throw new CommandException(e.getMessage());
		}
		String tx;
		try {
			tx = wallet.getApi().transfer(address, amount);
		} catch (RequestException e) {
			e.printStackTrace();
			throw new CommandException(bot.getMessage("withdraw.err.transfer"));
		}
		
		wallet.removeFunds(id, amount);
		
		chan.sendMessage(bot.dialog("Withdraw", String.format("You've withdrawn %s **DERO** to:\n%s\n\n__**Tx hash**__:\n%s\n\n__**Fee**__: %s", amount.subtract(fee), address, tx, fee))).queue();
		
		return null;
	}
}
