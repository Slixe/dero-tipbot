package fr.slixe.tipbot;

import fr.slixe.dero4j.DeroWallet;
import fr.slixe.dero4j.RequestException;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.math.BigDecimal;

@Singleton
public class Wallet {

	@Inject
	private ArangoDatabaseService db;

	public final DeroWallet api;
	
	public Wallet()
	{
		this.api = new DeroWallet("localhost", 30309, "slixe", "slixe");
	}
	
	public boolean hasEnoughFunds(String id, BigDecimal amount)
	{
		return getFunds(id).compareTo(amount) >= 0;
	}
	
	public void addFunds(String id, BigDecimal amount)
	{
		db.setBalance(id, getFunds(id).add(amount));
	}
	
	public void removeFunds(String id, BigDecimal amount)
	{
		db.setBalance(id, getFunds(id).subtract(amount));
	}
	
	public String getAddress(String id) throws RequestException
	{
		String address = db.getAddress(id);
		if (address != null)
			return address;
		String paymentId = this.api.paymentId();
		address = this.api.generateAddress(paymentId);

		db.setPaymentId(id, paymentId);
		db.setAddress(id, address);

		return address;
	}

	public BigDecimal getFunds(String id)
	{
		return db.getBalance(id);
	}
	
	public ArangoDatabaseService getDB()
	{
		return this.db;
	}

	public void addUnconfirmedFunds(String userId, BigDecimal amount) 
	{
		db.setUnconfirmedBalance(userId, getUnconfirmedFunds(userId).add(amount));
	}
	
	public BigDecimal getUnconfirmedFunds(String userId)
	{
		return db.getUnconfirmedBalance(userId);
	}

	public void removeUnconfirmedFunds(String userId, BigDecimal amount) 
	{
		db.setUnconfirmedBalance(userId, getUnconfirmedFunds(userId).subtract(amount));
	}
}
