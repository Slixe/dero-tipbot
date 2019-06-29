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

import fr.slixe.dero4j.structure.Tx;
import fr.slixe.dero4j.util.MapBuilder;

@Singleton
public class ArangoDatabaseService
{
	private static final Logger log = LoggerFactory.getLogger("Arango");

	@Inject
	private ConfigProvider config;
	
	private ArangoDB arango;
	private ArangoDatabase db;
	private ArangoCollection users;
	private ArangoCollection info;
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
		if (!this.db.exists()) {
			this.db.create();
		}

		this.users = this.db.collection("users");
		if (!this.users.exists()) {
			this.users.create();
		}

		this.info = this.db.collection("info");
		if (!this.info.exists()) {
			this.info.create();
		}
		
		this.txs = this.db.collection("txs");
		if (!this.txs.exists()) {
			this.txs.create();
		}

		log.info("Connected to ArangoDB");
	
	}
	
	public void setBalance(String userId, BigDecimal amount)
	{
		BaseDocument doc = users.getDocument(userId, BaseDocument.class);

		if (doc == null) {
			doc = new BaseDocument();
			doc.setKey(userId);

			users.insertDocument(doc);
		}

		doc.addAttribute("balance", amount);
		users.updateDocument(doc.getKey(), doc);
	}

	public BigDecimal getBalance(String userId)
	{
		BaseDocument doc = users.getDocument(userId, BaseDocument.class);
		if (doc == null) {
			return BigDecimal.ZERO;
		}

		String result = (String) doc.getAttribute("balance");
		if (result == null) {
			return BigDecimal.ZERO;
		}

		return new BigDecimal(result);
	}

	public void setUnconfirmedBalance(String userId, BigDecimal amount)
	{
		BaseDocument doc = users.getDocument(userId, BaseDocument.class);

		if (doc == null) {
			doc = new BaseDocument();
			doc.setKey(userId);

			users.insertDocument(doc);
		}

		doc.addAttribute("unconfirmed_balance", amount);
		users.updateDocument(doc.getKey(), doc);
	}

	public BigDecimal getUnconfirmedBalance(String userId)
	{
		BaseDocument doc = users.getDocument(userId, BaseDocument.class);
		if (doc == null) {
			return BigDecimal.ZERO;
		}

		String result = (String) doc.getAttribute("unconfirmed_balance");
		if (result == null) {
			return BigDecimal.ZERO;
		}

		return new BigDecimal(result);
	}

	public void setLastWalletHeight(int blockHeight)
	{
		BaseDocument doc = users.getDocument("infos", BaseDocument.class);

		if (doc == null) {
			doc = new BaseDocument();
			doc.setKey("infos");

			users.insertDocument(doc);
		}

		doc.addAttribute("walletHeight", blockHeight);
		users.updateDocument(doc.getKey(), doc);
	}

	public int getLastWalletHeight()
	{
		int height = 0;
		
		BaseDocument doc = this.info.getDocument("infos", BaseDocument.class);
		
		if (doc == null)
		{
			doc = new BaseDocument();
			doc.setKey("infos");
			this.info.insertDocument(doc);
		}
		
		String str = (String) doc.getAttribute("walletHeight");
		
		if (str == null)
			return height;
		else
			return Integer.parseInt(str);
	}

	public String getAddress(String userId)
	{
		BaseDocument doc = users.getDocument(userId, BaseDocument.class);
		if (doc == null) {
			return null;
		}

		String result = (String) doc.getAttribute("address");
		if (result == null) {
			return null;
		}

		return result;
	}

	public void setAddress(String userId, String address)
	{
		BaseDocument doc = users.getDocument(userId, BaseDocument.class);

		if (doc == null) {
			doc = new BaseDocument();
			doc.setKey(userId);

			users.insertDocument(doc);
		}

		doc.addAttribute("address", address);
		users.updateDocument(doc.getKey(), doc);
	}

	public String getPaymentId(String userId)
	{
		BaseDocument doc = users.getDocument(userId, BaseDocument.class);
		if (doc == null) {
			return null;
		}

		String result = (String) doc.getAttribute("paymentId");
		if (result == null) {
			return null;
		}

		return result;
	}

	public void setPaymentId(String userId, String paymentId)
	{
		BaseDocument doc = users.getDocument(userId, BaseDocument.class);

		if (doc == null) {
			doc = new BaseDocument();
			doc.setKey(userId);
			users.insertDocument(doc);
		}

		doc.addAttribute("paymentId", paymentId);
		users.updateDocument(doc.getKey(), doc);
	}


	public List<BaseDocument> getUsers()
	{
		return all("FOR doc IN users RETURN doc", BaseDocument.class, new HashMap<>());
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
}
