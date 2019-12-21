package fr.slixe.dero4j.structure;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import fr.slixe.dero4j.Daemon;
import fr.slixe.dero4j.RequestException;
import org.json.JSONObject;

public class SmartContract
{
	private final String url;
	private final String scid;

	public SmartContract(Daemon daemon, String scid)
	{
		this.url = daemon.getHost() + "/gettransactions";
		this.scid = scid;
	}

	public String[] query(boolean balance, String... key) throws RequestException
	{
		HttpResponse<JsonNode> httpResponse;
		try {
			httpResponse = Unirest.post(this.url).header("Content-Type", "application/json").body(new JSONObject().put("txs_hashes", new String[]{scid}).put("sc_keys", key)).asJson();
		} catch (UnirestException e) {
			throw new RequestException(e.getMessage());
		}
		JSONObject json = httpResponse.getBody().getObject();
		if (json.getString("status").equals("TX NOT FOUND"))
			throw new RequestException("This SCID doesn't exist!");
		
		String[] array = new String[key.length];
		JSONObject scKeys = json.getJSONArray("txs").getJSONObject(0).getJSONObject("sc_keys");
		
		for (int i = 0; i < array.length; i++)
		{
			if (!scKeys.has(key[i]) || scKeys.isNull(key[i]))
				array[i] = null;
			else
				array[i] = scKeys.getString(key[i]);
		}
		
		return array;
	}

	public String getID()
	{
		return this.scid;
	}
}
