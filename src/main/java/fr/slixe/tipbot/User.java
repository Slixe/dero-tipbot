package fr.slixe.tipbot;

import com.arangodb.entity.DocumentField;

import java.math.BigDecimal;

public class User
{
	@DocumentField(DocumentField.Type.KEY)
	private String key;
	private String userId;
	private String address;
	private String paymentId;
	private BigDecimal balance;

	public User() {}

	public User(String key, String userId, String address, String paymentId, BigDecimal balance)
	{
		this.key = key;
		this.userId = userId;
		this.address = address;
		this.paymentId = paymentId;
		this.balance = balance;
	}

	public String getKey()
	{
		return key;
	}

	public String getUserId()
	{
		return userId;
	}

	public void setUserId(String userId)
	{
		this.userId = userId;
	}

	public String getAddress()
	{
		return address;
	}

	public void setAddress(String address)
	{
		this.address = address;
	}

	public String getPaymentId()
	{
		return paymentId;
	}

	public void setPaymentId(String paymentId)
	{
		this.paymentId = paymentId;
	}

	public BigDecimal getBalance()
	{
		return balance;
	}

	public void setBalance(BigDecimal balance)
	{
		this.balance = balance;
	}
}
