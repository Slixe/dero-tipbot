package fr.slixe.tipbot;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

import org.krobot.config.ConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseDocument;
import com.google.inject.Inject;

import fr.slixe.dero4j.RequestException;
import fr.slixe.dero4j.structure.Tx;
import fr.slixe.dero4j.util.MapBuilder;

@Singleton
public class ArangoDatabaseService
{
	private static final Logger log = LoggerFactory.getLogger("Arango");

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
			System.err.println("Couldn't connect to ArangoDB! Please verify your config file.");
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

	public void addTx(Tx tx, String userId)
	{
		BaseDocument base = new BaseDocument();
		base.addAttribute("amount", tx.getAmount());
		base.addAttribute("userId", userId);
		base.addAttribute("blockHeight", tx.getBlockHeight());
		base.addAttribute("confirmed", false);
		base.addAttribute("confirmations", 0);
		
		base.setKey(tx.getTxHash());
		
		txs.insertDocument(base);
	}

	public void updateTx(String txHash, int confirmations)
	{
		BaseDocument doc = txs.getDocument(txHash, BaseDocument.class);
		
		doc.updateAttribute("confirmations", confirmations);
		
		if (confirmations == 20)
			doc.updateAttribute("confirmed", true);
		
		txs.updateDocument(doc.getKey(), doc);
	}

	public void removeTx(String txHash) {
		txs.deleteDocument(txHash);
	}
	
	public List<BaseDocument> getConfirmedTxs()
	{
		return getTxs(true);
	}
	
	public List<BaseDocument> getUnconfirmedTxs()
	{
		return getTxs(false);
	}
	
	private List<BaseDocument> getTxs(boolean confirmed)
	{
		return all("FOR doc IN txs FILTER doc.confirmed == @confirmed RETURN doc", BaseDocument.class, new MapBuilder<String, Object>().put("confirmed", confirmed).get());
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
}
