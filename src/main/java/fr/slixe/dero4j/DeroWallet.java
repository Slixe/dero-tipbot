package fr.slixe.dero4j;

import static fr.slixe.dero4j.util.Helper.asUint64;
import static fr.slixe.dero4j.util.Helper.json;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import fr.slixe.dero4j.structure.Tx;
import fr.slixe.dero4j.util.Helper;
import fr.slixe.dero4j.util.MapBuilder;

public class DeroWallet implements IWallet
{
	private static final Logger log = LoggerFactory.getLogger("Dero Wallet");
	
	private static final int SCALE = 12;

	private final String host;
	private final String username;
	private final String password;

	public DeroWallet(String host, int port, String username, String password)
	{
		this.host = String.format("http://%s:%d/json_rpc", host, port);
		this.username = username;
		this.password = password;
	}

	private JSONObject request(JSONObject json) throws RequestException
	{
		System.out.println(json);
		HttpResponse<JsonNode> req;
		try {
			req = Unirest.post(host).basicAuth(username, password).header("Content-Type", "application/json").body(json).asJson();
		} catch (UnirestException e) {
			log.error("Wallet is offline or invalid host/username/password ?");
			throw new RequestException("Wallet is offline?");
		}
		JSONObject response = req.getBody().getObject();
		System.out.println(response);
		if (!response.has("result")) {
			throw new RequestException(response.getJSONObject("error").getString("message"));
		}
		
		return response.getJSONObject("result");
	}

	@Override
	public String generateAddress(String id) throws RequestException
	{
		JSONObject json = request(json("make_integrated_address", new MapBuilder<String, Object>().put("payment_id", id).get()));
		return json.getString("integrated_address"); //make_integrated_address
	}

	@Override
	public String getPaymentIdFromAddress(String address) throws RequestException
	{
		JSONObject json = request(json("split_integrated_address", new MapBuilder<String, Object>().put("integrated_address", address).get()));
		return json.getString("payment_id"); //split_integrated_address
	}

	@Override
	public String transfer(String address, BigDecimal amount) throws RequestException
	{
		JSONObject json = request(json("transfer", new MapBuilder<String, Object>().put("address", address).put("amount", Helper.asUint64(amount, SCALE)).get()));
		return json.getString("tx_hash");
	}

	@Override
	public String transferSplit(String address, BigDecimal amount) throws RequestException
	{
		throw new RequestException("Not implemented yet");
		//JSONObject json = request(json("transfer_split", new MapBuilder<String, Object>().put("address", address).put("amount", Helper.asUint64(amount, SCALE)).get()));
		//return json.getString("tx_hash");
	}

	@Override
	public List<Tx> getTransactions(String id, int minHeight) throws RequestException
	{
		List<Tx> list = new ArrayList<>();
		JSONObject json = request(json("get_bulk_payments", new MapBuilder<String, Object>().put("min_block_height", minHeight).put("payment_ids", new String[]{id}).get()));
		if (!json.has("payments")) return list;

		JSONArray array = json.getJSONArray("payments");
		Iterator<Object> it = array.iterator();
		while (it.hasNext()) {
			JSONObject j = (JSONObject) it.next();
			int blockHeight = j.getInt("block_height");
			String txHash = j.getString("tx_hash");
			BigDecimal amount = Helper.toBigDecimal(j.getBigInteger("amount"), SCALE);
			byte lockedTime = (byte) j.getInt("unlock_time");
			list.add(new Tx.In(blockHeight, txHash, amount, lockedTime, null)); //fromAddress is null because it's isn't returned in json response
		}
		return list;
	}

	@Override
	public String getPrimaryAddress() throws RequestException
	{
		return request(json("getaddress")).getString("address"); //getaddress
	}

	@Override
	public int getHeight() throws RequestException
	{
		return request(json("getheight")).getInt("height"); //getheight
	}

	@Override
	public String sendToSC(String scid, String entrypoint, Map<String, Object> scParams) throws RequestException
	{
		return sendToSC(scid, entrypoint, scParams, null);
	}
	
	@Override
	public String sendToSC(String scid, String entrypoint, Map<String, Object> scParams, BigDecimal amount) throws RequestException
	{
		LinkedHashMap<String, Object> params = new MapBuilder<String, Object>().put("get_tx_key", true).get();
		LinkedHashMap<String, Object> scTx = new MapBuilder<String, Object>().put("scid", scid).put("entrypoint", entrypoint).get();
		if (amount != null)
			params.put("value", asUint64(amount, 12));
		
		if (scParams != null)
			scTx.put("params", scParams);
		
		params.put("sc_tx", scTx);		
		return request(json("transfer_split", params)).toString(); //TODO
	}

	@Override
	public String paymentId()
	{
		StringBuilder builder = new StringBuilder(64);
		SecureRandom rnd = new SecureRandom();
		while (builder.length() < 64)
			builder.append(Integer.toHexString(rnd.nextInt()));
		return builder.substring(0, 64);
	}

	@Override
	public boolean isValidTx(String txHash) throws RequestException
	{
		Tx.InPayment tx = getTransferByHash(txHash);
		return tx != null && (tx.getBlockHeight() + 20) <= getHeight();
	}

	@Override
	public Tx.InPayment getTransferByHash(String txHash) throws RequestException
	{
		JSONObject json = request(json("get_transfer_by_txid", new MapBuilder<String, Object>().put("txid", txHash).get()));
		if (!json.has("payments")) {
			return null;
		}
		JSONObject result = json.getJSONObject("payments");
		return new Tx.InPayment(result.getInt("block_height"), result.getString("tx_hash"), Helper.toBigDecimal(result.getBigInteger("amount"), SCALE), (byte) json.getInt("unlock_time"), json.getString("payment_id"));
	}

	@Override
	public BigDecimal estimateFee(String address, BigDecimal amount) throws RequestException
	{
		JSONObject json = request(json("transfer", new MapBuilder<String, Object>().put("do_not_relay", true).put("address", address).put("amount", Helper.asUint64(amount, SCALE)).get()));
		return Helper.toBigDecimal(json.getBigInteger("fee"), SCALE);
	}
}
