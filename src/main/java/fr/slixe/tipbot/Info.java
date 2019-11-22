package fr.slixe.tipbot;

import java.math.BigInteger;

import org.json.JSONObject;

public class Info {

	private int height;
	private int topoHeight;
	private double blockTime;
	private BigInteger difficulty;
	private int txMempool;
	private int totalSupply;
	private String daemonVersion;

	private final long millis;
	
	public Info(int height, int topoHeight, double blockTime, BigInteger difficulty, int txMempool, int totalSupply, String daemonVersion)
	{
		this.height = height;
		this.topoHeight = topoHeight;
		this.blockTime = blockTime;
		this.difficulty = difficulty;
		this.txMempool = txMempool;
		this.totalSupply = totalSupply;
		this.daemonVersion = daemonVersion;
		
		this.millis = System.currentTimeMillis();
	}

	public int getHeight() {
		return height;
	}

	public int getTopoHeight() {
		return topoHeight;
	}

	public double getBlockTime() {
		return blockTime;
	}

	public BigInteger getDifficulty() {
		return difficulty;
	}

	public int getTxMempool() {
		return txMempool;
	}

	public int getTotalSupply() {
		return totalSupply;
	}

	public String getDaemonVersion() {
		return daemonVersion;
	}

	public long getMillis()
	{
		return millis;
	}

	public static Info fromJson(JSONObject json)
	{
		return new Info(json.getInt("height"),
			json.getInt("topoheight"),
			json.getDouble("averageblocktime50"),
			json.getBigInteger("difficulty"),
			json.getInt("tx_pool_size"),
			json.getInt("total_supply"),
			json.getString("version"));
	}
}
