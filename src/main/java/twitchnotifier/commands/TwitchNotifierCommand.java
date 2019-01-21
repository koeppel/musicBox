package twitchnotifier.commands;

import botcore.commands.Command;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitchnotifier.core.StreamHandler;

import java.util.HashMap;

public class TwitchNotifierCommand implements Command {

    private HashMap<Guild, StreamHandler> streamHandlerHashMap = new HashMap<>();

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public void updateStreams() {
        logger.info("Started Updating Streams");
        for (StreamHandler streamHandler : streamHandlerHashMap.values()) {
            streamHandler.updateStreams();
        }
        logger.info("Finished Updating Stream");
    }

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        switch (args[0]) {
            case "add":
                StreamHandler streamHandler = streamHandlerHashMap.get(event.getGuild());
                if(streamHandler == null) {
                    streamHandler = new StreamHandler(event.getTextChannel());
                    streamHandlerHashMap.put(event.getGuild(), streamHandler);
                }
                streamHandler.addStream(args[1]);
                break;
            default:
                break;
        }
    }

    @Override
    public void executed(boolean success, MessageReceivedEvent event) {
        // FILL THIS ?
    }

    @Override
    public String help() {
        return null;
    }
}
