package fr.slixe.tipbot.command;

import java.math.BigDecimal;

import org.krobot.MessageContext;
import org.krobot.command.ArgumentMap;
import org.krobot.command.Command;
import org.krobot.command.CommandHandler;

import com.google.inject.Inject;

import fr.slixe.dero4j.RequestException;
import fr.slixe.tipbot.TipBot;
import fr.slixe.tipbot.Wallet;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.PrivateChannel;

@Command(value = "withdraw-max <address>", desc = "withdraw all your coins", errorMP = true)
public class WithdrawMaxCommand implements CommandHandler {

	@Inject
	private Wallet wallet;

	@Inject
	private TipBot bot;

	@Override
	public Object handle(MessageContext ctx, ArgumentMap args) throws Exception
	{
		MessageChannel chan = ctx.getChannel();

		if (!(chan instanceof PrivateChannel))
		{
			chan = ctx.getUser().openPrivateChannel().complete();
		}

		String address = args.get("address");
		String id = ctx.getUser().getId();
		BigDecimal amount = wallet.getFunds(id);

		BigDecimal fee;
		try {
			fee = wallet.getApi().estimateFee(address, amount);
		} catch (RequestException e)
		{
			e.printStackTrace();
			throw new CommandException(e.getMessage());
		}
		
		BigDecimal amountWithoutFee = amount.subtract(fee);

		if (amountWithoutFee.signum() != 1)
		{
			throw new CommandException("Not enough funds!");
		}
		
		String tx;
		try {
			tx = wallet.getApi().transfer(address, amountWithoutFee);
		} catch (RequestException e) {
			e.printStackTrace();
			throw new CommandException(bot.getMessage("withdraw.err.transfer"));
		}

		wallet.removeFunds(id, amount);

		chan.sendMessage(bot.dialog("Withdraw", String.format("You've withdrawn %s **DERO** to:\n%s\n\n__**Tx hash**__:\n%s\n\n__**Fee**__: %s", amountWithoutFee, address, tx, fee))).queue();

		return null;
	}

}
