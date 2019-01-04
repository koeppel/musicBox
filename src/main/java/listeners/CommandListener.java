package listeners;

import core.CommandHandler;
import core.CommandParser;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import utils.STATIC;

public class CommandListener extends ListenerAdapter {
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getMessage().getContentRaw().startsWith(STATIC.PREFIX) && !event.getMessage().getAuthor().getId().equals(event.getJDA().getSelfUser().getId())) {
            CommandHandler.handleCommand(CommandParser.parser(event.getMessage().getContentRaw(), event));
        }
    }
}
