package fr.slixe.tipbot;

import java.math.BigDecimal;

import com.arangodb.entity.DocumentField;

public class Transaction {
	
	@DocumentField(DocumentField.Type.KEY)
	private String hash;
	private String userId;
	private long blockHeight;
	private BigDecimal amount;
	private int confirmations;
	
	public Transaction() {}
	
	public Transaction(String hash, String userId, long blockHeight, BigDecimal amount)
	{
		this.hash = hash;
		this.userId = userId;
		this.blockHeight = blockHeight;
		this.amount = amount;
		this.confirmations = 0;
	}

	public String getHash()
	{
		return hash;
	}

	public void setHash(String hash)
	{
		this.hash = hash;
	}

	public String getUserId()
	{
		return userId;
	}

	public void setUserId(String userId)
	{
		this.userId = userId;
	}

	public long getBlockHeight()
	{
		return blockHeight;
	}

	public void setBlockHeight(long blockHeight)
	{
		this.blockHeight = blockHeight;
	}

	public BigDecimal getAmount()
	{
		return amount;
	}

	public void setAmount(BigDecimal amount)
	{
		this.amount = amount;
	}
	
	public int getConfirmations()
	{
		return confirmations;
	}

	public void setConfirmations(int confirmations)
	{
		this.confirmations = confirmations;
	}
}
