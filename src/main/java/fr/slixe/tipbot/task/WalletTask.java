package fr.slixe.tipbot.task;

import java.util.List;
import java.util.TimerTask;

import org.krobot.Krobot;
import org.krobot.util.Dialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import fr.slixe.dero4j.RequestException;
import fr.slixe.dero4j.structure.Tx;
import fr.slixe.tipbot.User;
import fr.slixe.tipbot.Wallet;

public class WalletTask extends TimerTask
{
	private static final Logger log = LoggerFactory.getLogger("WalletTask");

	@Inject
	private Wallet wallet;

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
		} else if ((diff = blockHeight - this.lastBlockHeight) > 1)
		{
			log.warn("Multiple blocks detected, current block height is " + blockHeight);
			blockHeight = blockHeight - diff + 1;
			log.warn("Now, it's " + blockHeight + " with a diff at " + diff);
		}
		
		
		final List<User> users = this.wallet.getDB().getUsers();

		for (User doc : users) {
			final String userId = doc.getKey(); //using userId as key
			final String paymentId = (String) doc.getPaymentId();
			
			if (paymentId == null) continue;
			
			final List<Tx> transactions;
			try {
				transactions = this.wallet.getApi().getTransactions(paymentId, blockHeight);
			} catch (RequestException e) {
				e.printStackTrace();
				continue;
			}
			
			Krobot.getRuntime().jda().getUserById(userId).openPrivateChannel().queue((e) -> {
				
				for (Tx tx : transactions) {
					log.info("New incoming transaction for user " + userId);
					this.wallet.addUnconfirmedFunds(userId, tx.getAmount());
					this.wallet.getDB().addTx(tx, userId);

					e.sendMessage(Dialog.info("Deposit", String.format("__**Amount**__: %s\n__**Tx hash**__: %s\n__**Block height**__: %s", tx.getAmount(), tx.getTxHash(), tx.getBlockHeight()))).queue();
				}
			});
		}
		this.lastBlockHeight = blockHeight + diff;
		this.wallet.saveWalletHeight(this.lastBlockHeight);
	}
}
