package twitchnotifier.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.TextChannel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.SECRETS;

public class StreamHandler {

    private final String apiUrl = "https://api.twitch.tv/helix/streams?user_login=";

    private List<Stream> streams = new ArrayList<>();

    private EmbedBuilder embedBuilder = new EmbedBuilder();

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private TextChannel announcementChannel;

    public StreamHandler(TextChannel announcementChannel) {
        this.announcementChannel = announcementChannel;
    }

    public void updateStreams() {
        if(!streams.isEmpty()) {
            checkStreams();
        }
    }

    public void addStream(String channelName) {
        Stream stream = null;
        for(Stream existingStream : streams) {
            if(existingStream.getChannelName().equals(channelName)) {
                stream = existingStream;
            }
        }
        if(stream == null) {
            stream = new Stream(channelName);
            streams.add(stream);
        }
    }

    public void setAnnouncementChannel(TextChannel announcementChannel) {
        this.announcementChannel = announcementChannel;
    }

    private void checkStreams() {
        for (Stream stream : streams) {
            boolean isOnline = checkStream(stream);
            boolean statusChanged = (isOnline != stream.isOnline());
            if(statusChanged) {
                // Status changed
                stream.setOnline(isOnline);
                createMessage(isOnline, stream);

                this.announcementChannel.sendMessage(embedBuilder.build()).queue();
            }
        }

    }

    private boolean checkStream(Stream stream) {
        boolean isOnline = false;

        try {
            URL url = new URL(this.apiUrl + stream.getChannelName());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Client-ID", SECRETS.TWITCHCLIENT);
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));


            String result = in.readLine();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(result);

            if(jsonNode.get("data").size() > 0) {
                isOnline = true;
            }

            in.close();
        }
        catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        return isOnline;
    }

    private void createMessage(boolean isOnline, Stream stream) {
        int color = 0xff0000;
        String title = "Stopped Streaming";
        String description = String.format("%s went offline!", stream.getChannelName());

        if(isOnline) {
            color = 0x00ff00;
            title = "Started Streaming";
            description = String.format("%s went online!", stream.getChannelName());
            this.embedBuilder.addField("TWITCH", String.format("https://twitch.tv/%s", stream.getChannelName()), false);
        }

        this.embedBuilder.setAuthor("");
        this.embedBuilder.setColor(color);
        this.embedBuilder.setTitle(title);
        this.embedBuilder.setDescription(description);
    }
}
