package fr.slixe.dero4j.structure;

import java.math.BigDecimal;

public class Tx
{
	private int blockHeight;
	private String txHash;
	private BigDecimal amount;
	private byte unlockTime;

	protected Tx(int blockHeight, String txHash, BigDecimal amount, byte unlockTime)
	{
		this.blockHeight = blockHeight;
		this.txHash = txHash;
		this.amount = amount;
		this.unlockTime = unlockTime;
	}

	public int getBlockHeight()
	{
		return blockHeight;
	}

	public String getTxHash()
	{
		return txHash;
	}

	public BigDecimal getAmount()
	{
		return amount;
	}

	public byte getUnlockTime()
	{
		return unlockTime;
	}

	public static class In extends Tx {

		private String fromAddress;

		public In(int blockHeight, String txHash, BigDecimal amount, byte unlockTime, String fromAddress)
		{
			super(blockHeight, txHash, amount, unlockTime);

			this.fromAddress = fromAddress;
		}

		public String getFromAddress()
		{
			return fromAddress;
		}
	}

	public static class InPayment extends Tx {

		private String paymentId;

		public InPayment(int blockHeight, String txHash, BigDecimal amount, byte unlockTime, String paymentId)
		{
			super(blockHeight, txHash, amount, unlockTime);

			this.paymentId = paymentId;
		}

		public String getPaymentId()
		{
			return this.paymentId;
		}
	}

	public static class Out extends Tx {

		private String toAddress;

		public Out(int blockHeight, String txHash, BigDecimal amount, byte unlockTime, String toAddress)
		{
			super(blockHeight, txHash, amount, unlockTime);

			this.toAddress = toAddress;
		}

		public String getToAddress()
		{
			return toAddress;
		}
	}
}