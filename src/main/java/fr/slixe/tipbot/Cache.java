package fr.slixe.tipbot;

import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.json.JSONObject;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.slixe.dero4j.RequestException;

@Singleton
public final class Cache {

	private static final Logger log = LoggerContext.getContext().getLogger("Cache");
	
	@Inject
	private TipBot bot;
	
	private Info info;
	
	public Info getInfo()
	{
		if (info == null || (info.getMillis() + 30 * 1000L < System.currentTimeMillis()))
		{
			log.info("Updating 'Info'");
			try {
				JSONObject json = bot.getDaemon().getInfo();
				info = Info.fromJson(json);
			} catch (RequestException e)
			{
				log.error("Can't update 'Info' !");
			}
		}

		return info;
	}
}