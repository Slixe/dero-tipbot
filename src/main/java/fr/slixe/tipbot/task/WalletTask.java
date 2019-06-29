package fr.slixe.tipbot.task;

import java.util.List;
import java.util.TimerTask;

import org.krobot.Krobot;
import org.krobot.util.Dialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arangodb.entity.BaseDocument;
import com.google.inject.Inject;

import fr.slixe.dero4j.RequestException;
import fr.slixe.dero4j.structure.Tx;
import fr.slixe.tipbot.Wallet;
import net.dv8tion.jda.core.entities.PrivateChannel;

public class WalletTask extends TimerTask
{
	private static final Logger log = LoggerFactory.getLogger("WalletTask");

	@Inject
	private Wallet wallet;

	private int lastBlockHeight = 0;
	
	public WalletTask() {}

	@Override
	public void run() //TODO improve
	{
		this.lastBlockHeight = this.wallet.getDB().getLastWalletHeight();
		
		int blockHeight;
		int diff = 0;
		try {
			blockHeight = this.wallet.api.getHeight();
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
		
		
		final List<BaseDocument> users = this.wallet.getDB().getUsers();

		for (BaseDocument doc : users) {
			final String userId = doc.getKey();
			final String paymentId = (String) doc.getAttribute("paymentId");
			final PrivateChannel privateChannel = Krobot.getRuntime().jda().getUserById(userId).openPrivateChannel()
					.complete();
			final List<Tx> transactions;
			try {
				transactions = this.wallet.api.getTransactions(paymentId, blockHeight);
			} catch (RequestException e) {
				e.printStackTrace();
				continue;
			}
			for (Tx transaction : transactions) {
				log.info("New incoming transaction for user " + userId);
				this.wallet.addUnconfirmedFunds(userId, transaction.getAmount());
				this.wallet.getDB().addTx(transaction, userId);
				//TODO instead of adding directly, we must wait 20 confirmations
																	   //get_transfer_by_txid with txid as params and compare block height with current
				if (privateChannel != null) {
					pmUser(privateChannel, transaction);
				}
			}
		}
		this.lastBlockHeight = blockHeight + diff;
		this.wallet.getDB().setLastWalletHeight(this.lastBlockHeight);
	}

	private void pmUser(PrivateChannel pc, Tx tx)
	{
		pc.sendMessage(Dialog.info("Deposit", String.format("__**Amount**__: %s\n__**Tx hash**__: %s\n__**Block height**__: %s", tx.getAmount(), tx.getTxHash(), tx.getBlockHeight()))).queue();
	}
}
