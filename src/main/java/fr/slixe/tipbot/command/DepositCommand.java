package fr.slixe.tipbot.command;

import org.krobot.MessageContext;
import org.krobot.command.ArgumentMap;
import org.krobot.command.Command;
import org.krobot.command.CommandHandler;

import com.google.inject.Inject;

import fr.slixe.dero4j.structure.Tx.InPayment;
import fr.slixe.tipbot.TipBot;
import fr.slixe.tipbot.Transaction;
import fr.slixe.tipbot.Wallet;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.PrivateChannel;


@Command(value = "deposit <txhash>", desc = "search your deposit", errorMP = true)
public class DepositCommand implements CommandHandler {

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

		String txHash = args.get("txhash");
		Transaction tx;

		StringBuilder builder = new StringBuilder();

		if (wallet.getDB().existTx(txHash))
		{
			tx = wallet.getDB().getTx(txHash);
			builder.append("Transaction already exists.").append("\n\n");
		}
		else
		{
			InPayment payment = wallet.getApi().getTransferByHash(txHash);

			if (payment == null)
			{
				throw new CommandException("This transaction hash does not exist!");
			}

			String userId = wallet.getDB().getUserIdFromPaymentId(payment.getPaymentId());
			tx = new Transaction(payment.getTxHash(), userId, payment.getBlockHeight(), payment.getAmount());
			wallet.getDB().addTx(tx);
			wallet.addUnconfirmedFunds(userId, tx.getAmount());

			builder.append("Transaction was added.").append("\n\n");
		}

		builder.append("**Tx Hash:** ").append(tx.getHash()).append("\n");
		builder.append("**Block Height:** ").append(tx.getBlockHeight()).append("\n");
		builder.append("**Amount:** ").append(tx.getAmount()).append("\n");
		builder.append("**Confirmations:** ").append(tx.getConfirmations()).append("\n");

		chan.sendMessage(bot.dialog("Deposit", builder.toString())).queue();
		
		return null;
	}

}
