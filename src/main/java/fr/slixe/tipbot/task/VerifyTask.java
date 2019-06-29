package fr.slixe.tipbot.task;

import java.math.BigDecimal;
import java.util.List;
import java.util.TimerTask;

import org.krobot.util.Dialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arangodb.entity.BaseDocument;
import com.google.inject.Inject;

import fr.slixe.dero4j.RequestException;
import fr.slixe.dero4j.structure.Tx;
import fr.slixe.tipbot.Wallet;
import net.dv8tion.jda.core.entities.PrivateChannel;

public class VerifyTask extends TimerTask {

	private static final Logger log = LoggerFactory.getLogger("VerifyTask");
	
	@Inject
	private Wallet wallet;
	
	public VerifyTask() {}
	
	@Override
	public void run() 
	{
		int blockHeight;
		try {
			blockHeight = wallet.api.getHeight();
		} catch (RequestException e) {
			log.error("Looks like wallet isn't reachable...");
			return;
		}
		
		List<BaseDocument> docs = wallet.getDB().getUnconfirmedTxs();
		
		for (BaseDocument doc : docs)
		{
			String txHash = doc.getKey();
			String userId = (String) doc.getAttribute("userId");
			int txBlockHeight = (int) doc.getAttribute("blockHeight");
			BigDecimal amount = (BigDecimal) doc.getAttribute("amount");
			
			int diff = blockHeight - txBlockHeight;
			diff = diff > 20 ? 20 : diff;
			
			try {
				if (diff == 20) //we wait 20 blocks to verify instead of veryfing every block
				{
					if (!this.wallet.api.isValidTx(txHash))
					{
						this.wallet.getDB().removeTx(txHash);
						continue;
					}
					else {
						this.wallet.addFunds(userId, amount);
						this.wallet.removeUnconfirmedFunds(userId, amount);
					}
				}
			} catch (RequestException e) {
				log.error(e.getMessage());
				continue;
			}
			
			
			this.wallet.getDB().updateTx(txHash, diff);			
		}
	}
}
