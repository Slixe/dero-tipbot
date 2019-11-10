package fr.slixe.tipbot;

import java.awt.Color;
import java.math.BigDecimal;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.krobot.Krobot;
import org.krobot.KrobotModule;
import org.krobot.config.ConfigProvider;
import org.krobot.module.Include;
import org.krobot.util.Dialog;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.slixe.tipbot.command.BalanceCommand;
import fr.slixe.tipbot.command.HelpCommand;
import fr.slixe.tipbot.command.InfoCommand;
import fr.slixe.tipbot.command.TipCommand;
import fr.slixe.tipbot.command.WithdrawCommand;
import fr.slixe.tipbot.task.VerifyTask;
import fr.slixe.tipbot.task.WalletTask;
import net.dv8tion.jda.core.entities.MessageEmbed;

@Singleton
@Include(commands = { TipCommand.class, BalanceCommand.class, WithdrawCommand.class, InfoCommand.class,
		HelpCommand.class })
@org.krobot.Bot(author = "Slixe", name = "Dero TipBot", version = "0.0.1")
public class TipBot extends KrobotModule {

	//public static final String ICON = "https://blockchain.dero.network/tipbot_logo.png";
	
	private static final Logger log = LoggerContext.getContext().getLogger("TipBot");

	private Timer timer = new Timer();

	private String iconUrl;
	private Color customColor;
	
	@Inject
	private WalletTask task;

	@Inject
	private VerifyTask verifyTask;

	@Inject
	private ArangoDatabaseService db;
	
	@Inject
	private Wallet wallet;

	@Inject
	private ConfigProvider config;
	
	@Override
	public void preInit() {
		folder("config/").configs("arango").withDefaultsIn().classpathFolder("/defaults/");
		folder("config/").configs("wallet").withDefaultsIn().classpathFolder("/defaults/");
		folder("config/").configs("general").withDefaultsIn().classpathFolder("/defaults/");
	}

	@Override
	public void init() {
		prefix("/");

		command("give", (context, map) -> {
			
			wallet.addFunds(context.getUser().getId(), new BigDecimal("50"));
			return null;
		});
		
		loadConfig();
		
		log.info("Dero TipBot is now running!");
	}

	@Override
	public void postInit() {		
		db.start();
		wallet.start();
		
		timer.scheduleAtFixedRate(task, 0, TimeUnit.SECONDS.toMillis(30));
		timer.scheduleAtFixedRate(verifyTask, 0, TimeUnit.SECONDS.toMillis(30));
	}
	
	public void loadConfig()
	{
		this.iconUrl = this.config.at("general.iconUrl");
		this.customColor = Color.decode(this.config.at("general.customColor"));
	}

	public String getMessage(String key)
	{
		return this.config.at("general." + key);
	}
	
	public MessageEmbed dialog(String title, String description)
	{
		return Dialog.dialog(this.customColor, title, description, this.iconUrl);
	}
	
	public static void main(String[] args) throws Exception {
		Krobot.create().readTokenFromArgs(args).saveTokenIn(".token").run(TipBot.class);
	}
}
