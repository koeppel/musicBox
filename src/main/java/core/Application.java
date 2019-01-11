package core;

import musicbox.commands.MusicBoxCommand;
import listeners.CommandListener;
import listeners.JoinListener;
import listeners.ReadyListener;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.CONFIG;

import javax.security.auth.login.LoginException;

public class Application {

    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    private static JDABuilder builder;

    public static void main(String[] args) {
        builder = new JDABuilder(AccountType.BOT);

        builder.setToken(utils.SECRETS.TOKEN);
        builder.setAutoReconnect(CONFIG.RECONNECT);

        addListeners();
        addCommands();

        try {
            builder.build();
        } catch (LoginException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private static void addListeners() {
        builder.addEventListener(new JoinListener());
        builder.addEventListener(new ReadyListener());
        builder.addEventListener(new CommandListener());
    }

    private static void addCommands() {
        CommandHandler.addCommand("mb", new MusicBoxCommand());
    }
}
