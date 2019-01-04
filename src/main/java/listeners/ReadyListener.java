package listeners;

import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadyListener extends ListenerAdapter {

    private Logger logger = LoggerFactory.getLogger(ReadyListener.class);

    @Override
    public void onReady(ReadyEvent event) {
        logger.info("Bot is Ready");
    }
}
