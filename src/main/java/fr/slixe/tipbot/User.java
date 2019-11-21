package fr.slixe.tipbot;

import com.arangodb.entity.DocumentField;

import java.math.BigDecimal;

public class User
{
	@DocumentField(DocumentField.Type.KEY)
	private String key;
	private String address;
	private String paymentId;
	private BigDecimal balance;
	private BigDecimal unconfirmedBalance;
	private String withdrawAddress;
	
	public User() {}

	public User(String userId, String address, String paymentId, BigDecimal balance, BigDecimal unconfirmedBalance)
	{
		this.key = userId;
		this.address = address;
		this.paymentId = paymentId;
		this.balance = balance;
		this.unconfirmedBalance = unconfirmedBalance;
	}

	public String getKey()
	{
		return key;
	}

	public void setKey(String key)
	{
		this.key = key;
	}
	
	/*
	public String getUserId()
	{
		return userId;
	}

	public void setUserId(String userId)
	{
		this.userId = userId;
	}
	 */
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
	
	public BigDecimal getUnconfirmedBalance()
	{
		return unconfirmedBalance;
	}

	public void setUnconfirmedBalance(BigDecimal balance)
	{
		this.unconfirmedBalance = balance;
	}

	public String getWithdrawAddress()
	{
		return this.withdrawAddress;
	}
	
	public void setWithdrawAddress(String withdrawAddress)
	{
		this.withdrawAddress = withdrawAddress;
	}
}
