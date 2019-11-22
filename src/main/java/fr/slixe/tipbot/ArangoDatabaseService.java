package fr.slixe.tipbot;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.krobot.config.ConfigProvider;

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDatabase;
import com.google.inject.Inject;

import fr.slixe.dero4j.RequestException;
import fr.slixe.dero4j.util.MapBuilder;

@Singleton
public class ArangoDatabaseService
{
	private static final Logger log = LoggerContext.getContext().getLogger("Arango");

	@Inject
	private ConfigProvider config;
	
	@Inject
	private Wallet wallet;
	
	private ArangoDB arango;
	private ArangoDatabase db;
	private ArangoCollection users;
	private ArangoCollection txs;
	
	public ArangoDatabaseService()
	{}

	public void start()
	{
		String host = this.config.at("arango.host");
		int port = this.config.at("arango.port", int.class);
		String user = this.config.at("arango.username");
		String password = this.config.at("arango.password");
		String database = this.config.at("arango.database");

		log.info("Connecting to ArangoDB at {}:{}...", host, port);

			this.arango = new ArangoDB.Builder()
					.host(host, port)
					.user(user)
					.password(password)
					.build();	

		this.db = this.arango.db(database);
		
		try {
			if (!this.db.exists()) {
				this.db.create();
			}
		}
		catch (Exception ignored)
		{
			log.error("Couldn't connect to ArangoDB! Please verify your config file.");
			System.exit(1);
		}

		this.users = this.db.collection("users");
		if (!this.users.exists()) {
			this.users.create();
		}
		
		this.txs = this.db.collection("txs");
		if (!this.txs.exists()) {
			this.txs.create();
		}

		log.info("Connected to ArangoDB");
	
	}
	
	public void setBalance(String key, BigDecimal amount)
	{
		User doc = users.getDocument(key, User.class);

		if (doc == null) {
			doc = createUser(key);
			users.insertDocument(doc);
		}

		doc.setBalance(amount);
		users.updateDocument(doc.getKey(), doc);
	}

	public BigDecimal getBalance(String userId)
	{
		User doc = users.getDocument(userId, User.class);
		if (doc == null) {
			doc = createUser(userId);
			users.insertDocument(doc);
		}

		return doc.getBalance();
	}

	public void setUnconfirmedBalance(String key, BigDecimal amount)
	{
		User doc = users.getDocument(key, User.class);

		if (doc == null)
		{
			doc = createUser(key);
			users.insertDocument(doc);
		}

		doc.setUnconfirmedBalance(amount);
		users.updateDocument(doc.getKey(), doc);
	}

	public BigDecimal getUnconfirmedBalance(String userId)
	{
		User doc = users.getDocument(userId, User.class);
		if (doc == null) {
			doc = createUser(userId);
			users.insertDocument(doc);
		}

		return doc.getUnconfirmedBalance();
	}
	
	public User getUser(String userId)
	{
		User user = users.getDocument(userId, User.class);
		
		if (user == null) {
			user = createUser(userId);
			users.insertDocument(user);
		}
		
		return user;
	}

	public void updateUser(User user)
	{
		users.updateDocument(user.getKey(), user);
	}

	public String getAddress(String key)
	{
		User doc = users.getDocument(key, User.class);
		if (doc == null) {
			doc = createUser(key);
			users.insertDocument(doc);
		}

		return doc.getAddress();
	}

	public void setAddress(String key, String address)
	{
		User doc = users.getDocument(key, User.class);

		if (doc == null) {
			doc = createUser(key);
			users.insertDocument(doc);
		}

		doc.setAddress(address);
		users.updateDocument(doc.getKey(), doc);
	}

	public String getPaymentId(String userId)
	{
		User doc = users.getDocument(userId, User.class);
		if (doc == null) {
			doc = createUser(userId);
			users.insertDocument(doc);
		}

		return doc.getPaymentId();
	}

	public void setPaymentId(String key, String paymentId)
	{
		User doc = users.getDocument(key, User.class);

		if (doc == null) {
			doc = createUser(key);
			users.insertDocument(doc);
		}

		doc.setPaymentId(paymentId);
		users.updateDocument(doc.getKey(), doc);
	}


	public List<User> getUsers()
	{
		return all("FOR doc IN users RETURN doc", User.class, new HashMap<>());
	}

	public void addTx(Transaction tx)
	{
		txs.insertDocument(tx);
	}

	public void updateTx(String txHash, int confirmations)
	{
		Transaction doc = txs.getDocument(txHash, Transaction.class);
		
		doc.setConfirmations(confirmations);
		
		txs.updateDocument(doc.getHash(), doc);
	}

	public void removeTx(String txHash) {
		txs.deleteDocument(txHash);
	}
	
	public List<Transaction> getConfirmedTxs()
	{
		return all("FOR doc IN txs FILTER doc.confirmations == @confirmations RETURN doc", Transaction.class, new MapBuilder<String, Object>().put("confirmations", 20).get());
	}
	
	public List<Transaction> getUnconfirmedTxs()
	{
		return all("FOR doc IN txs FILTER doc.confirmations < 20 RETURN doc", Transaction.class, new MapBuilder<String, Object>().get());
	}
	
	protected <T> T first(String query, Class<T> type, Map<String, Object> vars)
	{
		ArangoCursor<T> cursor = db.query(query, vars, null, type);

		if (cursor.hasNext()) {
			return cursor.next();
		}

		return null;
	}

	protected <T> List<T> all(String query, Class<T> type, Map<String, Object> vars)
	{
		return db.query(query, vars, null, type).asListRemaining();
	}
	
	protected User createUser(String userId)
	{
		String paymentId = wallet.getApi().paymentId();
		try {
			return new User(userId, wallet.getApi().generateAddress(paymentId), paymentId, BigDecimal.ZERO, BigDecimal.ZERO);
		} catch (RequestException e) {
			e.printStackTrace();
		}
		
		return new User(userId, null, paymentId, BigDecimal.ZERO, BigDecimal.ZERO);
	}

	public boolean existTx(String txHash)
	{
		//return first("FOR tx IN txs FILTER tx._key == @hash LIMIT 1 RETURN tx._key", String.class, new MapBuilder<String, Object>().put("hash", txHash).get()) != null;
		return txs.documentExists(txHash);
	}

	public Transaction getTx(String txHash)
	{
		return txs.getDocument(txHash, Transaction.class);
	}
	
	public String getUserIdFromPaymentId(String paymentId)
	{
		return first("FOR u IN users FILTER u.paymentId == @paymentId LIMIT 1 RETURN u._key", String.class, new MapBuilder<String, Object>().put("paymentId", paymentId).get());
	}
	
	public boolean hasWithdrawAddress(String userId)
	{
		return first("RETURN DOCUMENT(CONCAT('users/', @userId).withdrawAddress != null", boolean.class, new MapBuilder<String, Object>().put("userId", userId).get());
	}
}
