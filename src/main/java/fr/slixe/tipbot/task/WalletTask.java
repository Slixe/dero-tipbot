package fr.slixe.tipbot.task;

import java.util.List;
import java.util.TimerTask;

import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.krobot.Krobot;

import com.google.inject.Inject;

import fr.slixe.dero4j.RequestException;
import fr.slixe.dero4j.structure.Tx;
import fr.slixe.tipbot.TipBot;
import fr.slixe.tipbot.Transaction;
import fr.slixe.tipbot.User;
import fr.slixe.tipbot.Wallet;

public class WalletTask extends TimerTask
{
	private static final Logger log = LoggerContext.getContext().getLogger("WalletTask");

	@Inject
	private Wallet wallet;

	@Inject
	private TipBot bot;
	
	private int lastBlockHeight = 0;
	
	public WalletTask() {}

	@Override
	public void run()
	{
		this.lastBlockHeight = this.wallet.getSavedWalletHeight();
		
		int blockHeight;
		int diff = 0;
		try {
			blockHeight = this.wallet.getApi().getHeight();
		} catch (RequestException e) 
		{
			log.error("TipBot can't retrieve block height information from wallet!");
			return;
		}
		
		if (this.lastBlockHeight == blockHeight) 
		{
			log.info("skip this execution, block height is the same.");
			return;
		} else if (blockHeight - this.lastBlockHeight > 1)
		{
			diff = blockHeight - this.lastBlockHeight;
			log.warn("Multiple blocks detected, current wallet block height is " + blockHeight + " and last block height is " + this.lastBlockHeight);
			blockHeight = blockHeight - diff + 1;
			log.warn("Now, it's " + blockHeight);
		}
		
		if (blockHeight < 0)
			blockHeight = 0;
		
		final List<User> users = this.wallet.getDB().getUsers();

		for (User doc : users) {
			final String userId = doc.getKey(); //using userId as key
			final String paymentId = (String) doc.getPaymentId();

			if (paymentId == null) continue;

			final List<Tx> transactions;
			try {
				log.info("Fetch bulk payments with blockHeight " + blockHeight);
				transactions = this.wallet.getApi().getTransactions(paymentId, blockHeight);
			} catch (RequestException e) {
				e.printStackTrace();
				continue;
			}

			Krobot.getRuntime().jda().getUserById(userId).openPrivateChannel().queue((e) -> {

				log.info("Private channel opened with " + e.getUser().getName());

				for (Tx tx : transactions) {
					log.info("New incoming transaction for user " + userId);

					if (this.wallet.getDB().existTx(tx.getTxHash()))
					{
						log.error(String.format("Duplicated TX Hash: '%s', ignored...", tx.getTxHash()));
						continue;
					}
					

					this.wallet.addUnconfirmedFunds(userId, tx.getAmount());
					this.wallet.getDB().addTx(new Transaction(tx.getTxHash(), userId, tx.getBlockHeight(), tx.getAmount()));

					e.sendMessage(bot.dialog("Deposit", String.format("__**Amount**__: %s\n__**Tx hash**__: %s\n__**Block height**__: %s", tx.getAmount(), tx.getTxHash(), tx.getBlockHeight()))).queue();
				}
			});
		}
		this.lastBlockHeight = blockHeight + diff;
		this.wallet.saveWalletHeight(this.lastBlockHeight);
	}
}
