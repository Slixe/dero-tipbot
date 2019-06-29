package fr.slixe.tipbot;

import java.util.Timer;
import java.util.concurrent.TimeUnit;

import org.krobot.Krobot;
import org.krobot.KrobotModule;
import org.krobot.module.Include;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import fr.slixe.tipbot.command.BalanceCommand;
import fr.slixe.tipbot.command.HelpCommand;
import fr.slixe.tipbot.command.InfoCommand;
import fr.slixe.tipbot.command.TipCommand;
import fr.slixe.tipbot.command.WithdrawCommand;
import fr.slixe.tipbot.task.VerifyTask;
import fr.slixe.tipbot.task.WalletTask;

@Include(commands = { TipCommand.class, BalanceCommand.class, WithdrawCommand.class, InfoCommand.class,
		HelpCommand.class })
@org.krobot.Bot(author = "Slixe", name = "Dero TipBot", version = "0.0.1")
public class TipBot extends KrobotModule {

	private static final Logger log = LoggerFactory.getLogger("TipBot");

	public static void main(String[] args) throws Exception {
		Krobot.create().readTokenFromArgs(args).saveTokenIn(".token").run(TipBot.class);
	}

	private Timer timer = new Timer();

	@Inject
	private WalletTask task;

	@Inject
	private VerifyTask verifyTask;

	@Inject
	private ArangoDatabaseService db;

	@Override
	public void preInit() {
		folder("config/").configs("arango").withDefaultsIn().classpathFolder("/defaults/");
	}

	@Override
	public void init() {
		prefix("/"); // TODO put a middleware to prevent crash from wallet out of sync etc

		log.info("Dero TipBot is now running!");
	}

	@Override
	public void postInit() {		
		db.start();
		
		timer.scheduleAtFixedRate(task, 0, TimeUnit.SECONDS.toMillis(30));
		timer.scheduleAtFixedRate(verifyTask, 0, TimeUnit.SECONDS.toMillis(30));
	}
}
