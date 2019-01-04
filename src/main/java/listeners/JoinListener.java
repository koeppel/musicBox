package listeners;

import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class JoinListener extends ListenerAdapter {
    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        event.getGuild().getTextChannels().get(0).sendMessage("Hallo").queue();
    }
}
