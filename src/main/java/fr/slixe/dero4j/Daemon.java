package fr.slixe.dero4j;

import org.json.JSONObject;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import fr.slixe.dero4j.util.Helper;

public class Daemon
{
	private final String host;

	public Daemon(String host, int port)
	{
		this.host = String.format("http://%s:%d/json_rpc", host, port);
	}

	private JSONObject post(JSONObject json) throws RequestException
	{
		HttpResponse<JsonNode> httpResponse;
		try {
			httpResponse = Unirest.post(host).header("Content-Type", "application/json").body(json).asJson();
		} catch (UnirestException e) {
			e.printStackTrace();
			throw new RequestException("Daemon isn't available");
		}
		
		JSONObject response = httpResponse.getBody().getObject();
		if (!response.has("result"))
			throw new RequestException(response.getString("error"));
		
		return response.getJSONObject("result");
	}

	public int getHeight() throws RequestException
	{
		return post(Helper.json("getheight")).getInt("height");
	}
	
	public JSONObject getInfo() throws RequestException
	{
		return post(Helper.json("get_info"));
	}

	public String getHost()
	{
		return this.host;
	}
}
