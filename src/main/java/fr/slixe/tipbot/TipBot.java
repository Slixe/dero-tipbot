package fr.slixe.tipbot;

import java.awt.Color;
import java.math.BigDecimal;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.krobot.Krobot;
import org.krobot.KrobotModule;
import org.krobot.command.CommandFilter;
import org.krobot.command.ExceptionHandler;
import org.krobot.config.ConfigProvider;
import org.krobot.module.Include;
import org.krobot.util.Dialog;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.slixe.dero4j.Daemon;
import fr.slixe.tipbot.command.BalanceCommand;
import fr.slixe.tipbot.command.CommandException;
import fr.slixe.tipbot.command.HelpCommand;
import fr.slixe.tipbot.command.InfoCommand;
import fr.slixe.tipbot.command.TipCommand;
import fr.slixe.tipbot.command.WithdrawCommand;
import fr.slixe.tipbot.task.VerifyTask;
import fr.slixe.tipbot.task.WalletTask;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.PrivateChannel;

@Singleton
@Include(commands = { TipCommand.class, BalanceCommand.class, WithdrawCommand.class, InfoCommand.class,
		HelpCommand.class })
@org.krobot.Bot(author = "Slixe", name = "Dero TipBot", version = "0.0.1")
public class TipBot extends KrobotModule {

	private static final Logger log = LoggerContext.getContext().getLogger("TipBot");

	private Timer timer = new Timer();

	private Daemon daemon;
	private String iconUrl;
	private String roleId;
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
	
	@Inject
	private ExceptionHandler exceptionHandler;

	@Override
	public void preInit() {
		folder("config/").configs("arango").withDefaultsIn().classpathFolder("/defaults/");
		folder("config/").configs("wallet").withDefaultsIn().classpathFolder("/defaults/");
		folder("config/").configs("general").withDefaultsIn().classpathFolder("/defaults/");
	}

	@Override
	public void init() {
		prefix("/");

		CommandFilter roleFilter = (cmd, context, args) -> { //Role filter

			if (context.getChannel() instanceof PrivateChannel || !context.getMember().getRoles().contains(context.getGuild().getRoleById(this.roleId)))
				cmd.setCancelled(true);
		};
		
		command("give", (context, map) -> { //only for debug

			wallet.addFunds(context.getUser().getId(), new BigDecimal("9.5"));
			return null;
		}).filter(roleFilter);

		command("shutdown-wallet", (context, map) -> {

			if (this.wallet.getProcess() != null)
				this.wallet.getProcess().destroyForcibly();

			return dialog("Wallet shutdown", "The DERO wallet is now stopped.");
		}).filter(roleFilter);

		command("shutdown-bot", (context, map) -> {

			log.warn("Shutdown requested !!");

			if (this.wallet.getProcess() != null)
			{
				log.warn("destroying wallet process...");
				this.wallet.getProcess().destroyForcibly();
				log.warn("Wallet process should be destroyed.");
			}

			context.send(dialog("Shutdown", "DERO Tip Bot will be offline after this message.")).get(); //wait for it

			log.warn("Exiting BOT...");
			
			System.exit(0);
			return null;
		}).filter(roleFilter);

		this.exceptionHandler.on(CommandException.class, (context, t) -> context.send(Dialog.error("Error !", t.getMessage())));

		loadConfig();
		
		log.info("Dero TipBot is now running!");
	}

	@Override
	public void postInit() {		
		db.start();
		wallet.start();
		
		String daemonHost = this.config.at("wallet.daemon");

		this.daemon = new Daemon(daemonHost);
		
		timer.scheduleAtFixedRate(task, 0, TimeUnit.SECONDS.toMillis(30));
		timer.scheduleAtFixedRate(verifyTask, 0, TimeUnit.SECONDS.toMillis(30));
	}
	
	public void loadConfig()
	{
		this.iconUrl = this.config.at("general.iconUrl");
		this.customColor = Color.decode(this.config.at("general.customColor"));
		this.roleId  = this.config.at("general.roleId");
		
		if (this.roleId.isEmpty())
		{
			log.error("RoleId isn't filled in general.json !! Aborting...");
			System.exit(0);
		}
	}

	public String getMessage(String key)
	{
		return this.config.at("general." + key);
	}

	public MessageEmbed dialog(String title, String description)
	{
		return Dialog.dialog(this.customColor, title, description, this.iconUrl);
	}

	public Daemon getDaemon()
	{
		return daemon;
	}
	
	public static void main(String[] args) throws Exception {
		Krobot.create().readTokenFromArgs(args).saveTokenIn(".token").run(TipBot.class);
	}
}
