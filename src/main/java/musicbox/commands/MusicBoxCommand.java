package musicbox.commands;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import botcore.commands.Command;
import musicbox.core.MusicBox;
import musicbox.core.MusicBoxHandler;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import utils.UTILS;

import java.util.HashMap;
import java.util.Queue;

public class MusicBoxCommand implements Command {

    private HashMap<Guild, MusicBox> musicBoxHashMap = new HashMap<>();

    private int deletionTime = 5000;

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        //boolean deleteAfterwards = true;
        deletionTime = 5000;
        EmbedBuilder embedBuilder = new EmbedBuilder();
        if(musicBoxHashMap.containsKey(event.getGuild())) {
            MusicBox musicBox = musicBoxHashMap.get(event.getGuild());
            embedBuilder = musicBox.getEmbedBuilder();
        }
        embedBuilder.clear();
        embedBuilder.setAuthor("Music Box", null, event.getAuthor().getAvatarUrl());

        if((args.length >= 1)) {
            // Process the users command
            processCommand(args, event, embedBuilder);

            // Cleanup the users message
            event.getMessage().delete().queue();
        } else {
            // Notify the user that he has to provide more arguments
            embedBuilder.setTitle("Fehler!");
            embedBuilder.setDescription("Zu wenige Argumente für den Befehl '!musicbox' übergeben. Versuche es mit '!musicbox help'");
        }

        if(!embedBuilder.isEmpty()) {
            // No usecase for deleteAfterwards right now
            //if(!deleteAfterwards) {
            //    event.getMessage().getChannel().sendMessage(embedBuilder.build()).queue();
            //} else {
                Message message = event.getMessage().getChannel().sendMessage(embedBuilder.build()).complete();
                UTILS.clearMessage(message, deletionTime);
            //}
        }
    }

    @Override
    public void executed(boolean success, MessageReceivedEvent event) {
        //throw(new UnsupportedOperationException());
    }

    @Override
    public String help() {
        return null;
    }

    /**
     *
     */
    private void processCommand (String[] args, MessageReceivedEvent event, EmbedBuilder embedBuilder) {
        if(musicBoxHashMap.containsKey(event.getGuild())) {
            MusicBox musicBox = musicBoxHashMap.get(event.getGuild());
            switch(args[0]) {
                case("add"):
                    musicBox.addTrackToQueue(args[1], event);
                    embedBuilder.clear();
                    break;
                case("clear"):
                    musicBox.clearQueue();
                    break;
                case("current"):
                    embedBuilder.setTitle("Momentaner Track:");
                    AudioTrack currentTrack = musicBox.getTrackScheduler().getCurrentTrack();
                    if(currentTrack == null) {
                        embedBuilder.setDescription("Es wird momentan kein Track abgespielt.");
                    } else {
                        embedBuilder.setDescription(currentTrack.getInfo().title + " " + currentTrack.getState());
                    }
                    break;
                case("leave"):
                    musicBox.leaveVoiceChannel();
                    musicBoxHashMap.remove(event.getGuild());
                    embedBuilder.setTitle("Habe den Channel {" + event.getMember().getVoiceState().getChannel().getName() + "} verlassen.");
                    break;
                case("play"):
                    playTrack(args[1], event);
                    embedBuilder.clear();
                    break;
                case("skip"):
                    musicBox.getTrackScheduler().skipp();
                    embedBuilder.setTitle("Track wurde übersprungen.");
                    break;
                case("playlist"):
                case("queue"):
                    embedBuilder.setTitle("Momentane Playlist:");
                    if(musicBox.getTrackScheduler().getCurrentTrack() != null) {
                        embedBuilder.addField("Momentaner Track:", musicBox.getTrackScheduler().getCurrentTrack().getInfo().title, false);
                    }
                    Queue<AudioTrack> tracks = musicBox.getTrackScheduler().getPlayList();
                    int position = 1;
                    for (AudioTrack track : tracks) {
                        embedBuilder.addField("Position {" + position + "} :", track.getInfo().title, false);
                        position++;
                    }
                    break;
                case("volume"):
                    embedBuilder.setTitle("Lautstärke:");
                    embedBuilder.setDescription(args[1] + "%");
                    musicBox.getTrackScheduler().setVolume(Integer.parseInt(args[1]));
                    break;
                case("stop"):
                    musicBox.getTrackScheduler().stop();
                    embedBuilder.setTitle("Momentaner Track wurde angehalten.");
                    break;
                case("start"):
                case("resume"):
                    musicBox.getTrackScheduler().resume();
                    embedBuilder.setTitle("Track wurde wieder aufgenommen.");
                    break;
                case("jump"):
                    musicBox.getTrackScheduler().jump(Integer.parseInt(args[1]));
                    embedBuilder.setTitle("Springe zu Track {" + Integer.parseInt(args[1]) + "}.");
                    break;
                default:
                    break;
            }
        }
        switch(args[0]) {
            case("help"):
                configureHelpEmbedBuilder(embedBuilder);
                deletionTime = 30000;
                break;
            case("join"):
                joinVoiceChannel(event.getMember().getVoiceState().getChannel());
                embedBuilder.setTitle("Habe den Channel {" + event.getMember().getVoiceState().getChannel().getName() + "} betreten.");
                break;
            default:
                if(!musicBoxHashMap.containsKey(event.getGuild())) {
                    embedBuilder.setTitle("Fehler bei der Ausführung des Befehls! Es existiert keine MusicBox Instanz. {!mb help} um Hilfe zu erhalten.");
                }
                break;
        }
    }

    /***
     * Adds Fields and a Title to the EmbedBuilder to list all botcore.commands and their descriptions
     */
    private void configureHelpEmbedBuilder(EmbedBuilder embedBuilder) {
        embedBuilder.setTitle("Übersicht der Befehle für '!musicbox' :");
        embedBuilder.addField("add {URL}", "Fügt ein Lied / Playlist der momentanen Playlist hinzu.",false);
        embedBuilder.addField("clear", "Löscht die momentane Playlist (momentan laufendes Lied ausgenommen)",false);
        embedBuilder.addField("current", "Gibt das momentane Lied aus.",false);
        embedBuilder.addField("help", "Gibt die Übersicht der Befehle für '!musicbox' aus.",false);
        embedBuilder.addField("join", "Fordert den Bot auf den momentanen Channel zu betreten.",false);
        embedBuilder.addField("jump {Position}", "Springt zu einem Lied in der Playlist mit angegebner Position.",false);
        embedBuilder.addField("leave", "Fordert den Bot auf den momentanen Channel zu verlassen.",false);
        embedBuilder.addField("playlist / queue", "Gibt die momentane Playlist aus.",false);
        embedBuilder.addField("volume {0-100}", "Setzt die Lautstärke auf einen Wert zwischen 0% und 100%. Standardwert = 20",false);
        embedBuilder.addField("stop", "Unterbricht die momentane Wiedergabe.",false);
        embedBuilder.addField("start / resume", "Startet die Wiedergabe oder nimmt ein unterbrochenes Lied wieder auf.",false);
        embedBuilder.addField("skipp", "Überspringt die momentane Wiedergabe.",false);
    }

    /***
     *
     * @param trackUrl Url to music-video
     * @param event the event of the current command
     */
    private void playTrack(String trackUrl, MessageReceivedEvent event) {
        MusicBox musicBox = musicBoxHashMap.get(event.getGuild());
        if(!musicBox.isInChannel()) {
            joinVoiceChannel(event.getMember().getVoiceState().getChannel());
        }
        musicBox.addTrackToQueue(trackUrl, event);
    }

    /***
     *
     * @param channel the VoiceChannel which should be joined
     */
    private void joinVoiceChannel(VoiceChannel channel) {
        MusicBox musicBox = new MusicBox(channel.getGuild().getAudioManager());

        musicBox.getAudioManager().setSendingHandler(new MusicBoxHandler(musicBox.getAudioPlayer()));
        musicBox.getAudioManager().openAudioConnection(channel);
        musicBox.setInChannel(true);
        musicBoxHashMap.put(channel.getGuild(), musicBox);
    }
}
