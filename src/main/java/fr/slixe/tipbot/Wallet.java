package fr.slixe.tipbot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;
import java.math.BigDecimal;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.krobot.config.ConfigProvider;

import fr.slixe.dero4j.DeroWallet;
import fr.slixe.dero4j.RequestException;

@Singleton
public class Wallet {

	private static final Logger log = LoggerContext.getContext().getLogger("Wallet");
	
	@Inject
	private ArangoDatabaseService db;

	@Inject
	private ConfigProvider config;
	
	private DeroWallet api;

	private Thread thread;
	
	public Wallet() {}
	
	public void start()
	{
		String host = this.config.at("wallet.host");
		int port = this.config.at("wallet.port", int.class);
		String username = this.config.at("wallet.username");
		String password = this.config.at("wallet.password");
		String daemon = this.config.at("wallet.daemon");
		boolean autoLaunch = this.config.at("wallet.autoLaunch", boolean.class);
		String walletPath = this.config.at("wallet.walletFilePath");
		String walletPassword = this.config.at("wallet.walletPassword");
		if (autoLaunch)
		{
			log.info("Trying to start DERO Wallet...");
			String path = this.config.at("wallet.launchPath");
			
			this.thread = new Thread(() -> {
				Process process;
				
				try {
					String cmd[] = {path, "--testnet", "--rpc-server", String.format("--rpc-bind=%s:%d", host, port), String.format("--rpc-login=%s:%s", username, password),
							String.format("--daemon-address=%s", daemon), String.format("--wallet-file=%s", walletPath), String.format("--password=%s", walletPassword)};
					ProcessBuilder builder = new ProcessBuilder(cmd).redirectInput(Redirect.INHERIT).redirectError(Redirect.INHERIT).redirectOutput(Redirect.INHERIT);
					process = builder.start();
					System.out.println(process.getOutputStream());
				} catch (IOException e)
				{
					log.error("Can't launch DERO Wallet!!");
					e.printStackTrace();
					return;
				}
				
				/*
				if (!walletExist)
				{
					System.out.println("called");
					OutputStream output = process.getOutputStream();
					try {
						output.write("2".getBytes());
						output.flush();
						for (int i = 0; i < 2; i++)
							writer.write("");
						writer.write("0");
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
				*/
				

				BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
				String line;
				
				try {
					while ((line = reader.readLine()) != null)
						log.info(line);
					
					process.waitFor();
				} catch (Exception e) {
					e.printStackTrace();

				}
			});
			
			this.thread.start();
			log.info("DERO Wallet is now launched!");
		}

		this.api = new DeroWallet(host, port, username, password);
		log.info("Wallet API is now initialized!");
	}
	
	public boolean hasEnoughFunds(String id, BigDecimal amount)
	{
		return getFunds(id).compareTo(amount) >= 0;
	}
	
	public void addFunds(String id, BigDecimal amount)
	{
		db.setBalance(id, getFunds(id).add(amount));
	}
	
	public void removeFunds(String id, BigDecimal amount)
	{
		db.setBalance(id, getFunds(id).subtract(amount));
	}
	
	public String getAddress(String id) throws RequestException
	{
		String address = db.getAddress(id);
		if (address != null)
			return address;
		String paymentId = this.api.paymentId();
		address = this.api.generateAddress(paymentId);

		db.setPaymentId(id, paymentId);
		db.setAddress(id, address);

		return address;
	}

	public BigDecimal getFunds(String id)
	{
		return db.getBalance(id);
	}
	
	public ArangoDatabaseService getDB()
	{
		return this.db;
	}

	public void addUnconfirmedFunds(String userId, BigDecimal amount) 
	{
		db.setUnconfirmedBalance(userId, getUnconfirmedFunds(userId).add(amount));
	}
	
	public BigDecimal getUnconfirmedFunds(String userId)
	{
		return db.getUnconfirmedBalance(userId);
	}

	public void removeUnconfirmedFunds(String userId, BigDecimal amount) 
	{
		db.setUnconfirmedBalance(userId, getUnconfirmedFunds(userId).subtract(amount));
	}
	
	public int getSavedWalletHeight()
	{
		return this.config.at("general.wallet-height", int.class);
	}

	public void saveWalletHeight(int lastBlockHeight)
	{
		this.config.set("general.wallet-height", lastBlockHeight);
	}
	
	public DeroWallet getApi()
	{
		return this.api;
	}

}
