package core;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import utils.STATIC;

import java.util.ArrayList;
import java.util.Arrays;

public class CommandParser {

    public static CommandContainer parser(String raw, MessageReceivedEvent event) {
        String beheaded = raw.replaceFirst(STATIC.PREFIX, "");
        String[] splitBeheaded = beheaded.split(" ");
        String invoke = splitBeheaded[0];

        ArrayList<String> split = new ArrayList<>(Arrays.asList(splitBeheaded));
        String[] args = new String[split.size() - 1];
        split.subList(1, split.size()).toArray(args);

        return new CommandContainer(raw, beheaded, splitBeheaded, invoke, args, event);
    }

    public static class CommandContainer {
        public final String raw;
        public final String beheaded;
        public final String[] splitBeheaded;
        public final String invoke;
        public final String[] args;
        public final MessageReceivedEvent event;

        public CommandContainer(String raw, String beheaded, String[] splitBeheaded, String invoke, String[] args, MessageReceivedEvent event) {
            this.raw = raw;
            this.beheaded = beheaded;
            this.splitBeheaded = splitBeheaded;
            this.invoke = invoke;
            this.args = args;
            this.event = event;
        }
    }
}