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

@Command(value = "withdraw <amount> [address]", desc = "withdraw your coins from bot", errorMP = true)
public class WithdrawCommand implements CommandHandler {

	@Inject
	private Wallet wallet;
	
	@Inject
	private TipBot bot;
	
	@Override
	public Object handle(MessageContext ctx, ArgumentMap args) throws Exception { //TODO add Withdraw to a task and not call withdraw directly..
		BigDecimal amount;

		try {
			amount = new BigDecimal(args.get("amount", String.class));
			if (amount.signum() != 1)
				throw new CommandException(bot.getMessage("withdraw.err.positive-value"));
		}
		catch (NumberFormatException e)
		{
			throw new CommandException(String.format(bot.getMessage("withdraw.err.invalid-amount"), ctx.getUser().getAsMention()));
		}

		String id = ctx.getUser().getId();

		if (!wallet.hasEnoughFunds(id, amount))
		{
			throw new CommandException(bot.getMessage("withdraw.err.not-enough"));
		}

		String address;

		if (args.has("address"))
			address = args.get("address");
		else
			address = wallet.getAddress(id);
		
		if (address == null)
			throw new CommandException("No default withdraw address found! set one with /withdraw-address <address>");

		BigDecimal fee;
		try {
			fee = wallet.getApi().estimateFee(address, amount);
		} catch (RequestException e)
		{
			throw new CommandException(e.getMessage());
		}

		BigDecimal amountWithoutFee = amount.subtract(fee);

		if (amountWithoutFee.signum() != 1)
		{
			throw new CommandException("Not enough funds in this withdrawal. Transaction fees are deducted from the amount to be withdrawn.");
		}
		
		String tx;
		try {
			tx = wallet.getApi().transfer(address, amountWithoutFee);
		} catch (RequestException e) {
			e.printStackTrace();
			throw new CommandException(bot.getMessage("withdraw.err.transfer") + " " + e.getMessage());
		}

		wallet.removeFunds(id, amount);

		return bot.dialog("Withdraw", String.format("You've withdrawn %s **DERO** to:\n%s\n\n__**Tx hash**__:\n%s\n\n__**Fee**__: %s", amountWithoutFee, address, tx, fee));
	}
}
