package fr.slixe.tipbot.command;

import java.math.BigDecimal;

import fr.slixe.dero4j.RequestException;
import fr.slixe.tipbot.Wallet;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.PrivateChannel;

import org.krobot.MessageContext;
import org.krobot.command.ArgumentMap;
import org.krobot.command.Command;
import org.krobot.command.CommandHandler;
import org.krobot.util.Dialog;

import com.google.inject.Inject;

@Command(value = "withdraw <amount> <address>", desc = "withdraw your coins from bot")
public class WithdrawCommand implements CommandHandler {

	@Inject
	private Wallet wallet;
	
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
			{
				chan.sendMessage(Dialog.error("Error!", "Hey, please put a positive value.")).queue();
				return null;
			}
		}
		catch (NumberFormatException e)
		{
			chan.sendMessage(Dialog.error("Error!", String.format("%s, invalid amount", ctx.getUser().getAsMention())));
			return null;
		}
		String address = args.get("address");
		String id = ctx.getUser().getId();
		if (!wallet.hasEnoughFunds(id, amount))
		{
			chan.sendMessage(Dialog.error("Error!", "You haven't enough DERO to withdraw.")).queue();
			return null;
		}
		
		wallet.removeFunds(id, amount);
		BigDecimal fee = wallet.api.estimateFee(address, amount);
		String tx;
		try {
			tx = wallet.api.transfer(address, amount);
		} catch (RequestException e) {
			e.printStackTrace();
			chan.sendMessage(Dialog.error("Error!", "An error has occured during withdraw command")).queue();
			return null;
		}
		
		chan.sendMessage(Dialog.info("Withdraw", String.format("You've withdrawn %s **DERO** to:\n%s\n\n__**Tx hash**__:\n%s\n\n__**Fee**__: %s", amount.subtract(fee), address, tx, fee)));
		
		return null;
	}
}
