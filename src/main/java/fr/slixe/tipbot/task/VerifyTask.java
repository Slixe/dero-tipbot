package fr.slixe.tipbot.task;

import java.util.List;
import java.util.TimerTask;

import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import com.google.inject.Inject;

import fr.slixe.dero4j.RequestException;
import fr.slixe.tipbot.Transaction;
import fr.slixe.tipbot.Wallet;

public class VerifyTask extends TimerTask {

	private static final Logger log = LoggerContext.getContext().getLogger("VerifyTask");
	
	@Inject
	private Wallet wallet;
	
	public VerifyTask() {}
	
	@Override
	public void run() 
	{
		int blockHeight;
		try {
			blockHeight = wallet.getApi().getHeight();
		} catch (RequestException e) {
			log.error("Looks like wallet isn't reachable...");
			return;
		}

		List<Transaction> txs = wallet.getDB().getUnconfirmedTxs();

		for (Transaction tx : txs)
		{
			int diff = (int) (blockHeight - tx.getBlockHeight());
			diff = diff > 20 ? 20 : diff;

			try {
				if (diff == 20) //we wait 20 blocks to verify instead of veryfing every block
				{
					if (!this.wallet.getApi().isValidTx(tx.getHash()))
					{
						log.error("Invalid transaction !! hash: '" + tx.getHash() + "'");
						this.wallet.getDB().removeTx(tx.getHash());
						continue;
					}
					else {
						log.info("Amount: " + tx.getAmount());
						this.wallet.addFunds(tx.getUserId(), tx.getAmount());
						this.wallet.removeUnconfirmedFunds(tx.getUserId(), tx.getAmount());
					}
				}
			} catch (RequestException e) {
				log.error(e.getMessage());
				continue;
			}
			
			this.wallet.getDB().updateTx(tx.getHash(), diff);			
		}
	}
}
