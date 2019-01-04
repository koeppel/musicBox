package musicbox.commands;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import commands.Command;
import musicbox.MusicBoxHandler;
import musicbox.TrackScheduler;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.managers.AudioManager;
import utils.UTILS;

import java.util.Queue;

public class MusicBox implements Command {

    private AudioPlayerManager audioPlayerManager =  new DefaultAudioPlayerManager();

    private AudioManager audioManager = null;

    private EmbedBuilder embedBuilder = new EmbedBuilder();

    private AudioPlayer audioPlayer = audioPlayerManager.createPlayer();

    private TrackScheduler trackScheduler = new TrackScheduler(audioPlayer, this);

    private boolean inChannel = false;

    private int deletionTime = 5000;

    public MusicBox () {
        AudioSourceManagers.registerRemoteSources(audioPlayerManager);
        audioPlayer.addListener(trackScheduler);
    }

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        //boolean deleteAfterwards = true;
        deletionTime = 5000;
        embedBuilder.clear();
        embedBuilder.setAuthor("Music Box", null, event.getAuthor().getAvatarUrl());

        if((args.length >= 1)) {
            // Process the users command
            processCommand(args, event);

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
        throw(new UnsupportedOperationException());
    }

    @Override
    public String help() {
        return null;
    }

    /***
     *
     * @param args Arguments of the current command
     * @param event Event of the current command
     */
    private void processCommand (String[] args, MessageReceivedEvent event) {
        switch(args[0]) {
            case("add"):
                addTrackToQueue(args[1], event);
                embedBuilder.clear();
                break;
            case("current"):
                embedBuilder.setTitle("Momentaner Track:");
                AudioTrack currentTrack = trackScheduler.getCurrentTrack();
                if(currentTrack == null) {
                    embedBuilder.setDescription("Es wird momentan kein Track abgespielt.");
                } else {
                    embedBuilder.setDescription(currentTrack.getInfo().title + " " + currentTrack.getState());
                }
                break;
            case("join"):
                joinVoiceChannel(event.getMember().getVoiceState().getChannel());
                embedBuilder.setTitle("Habe den Channel {" + event.getMember().getVoiceState().getChannel().getName() + "} betreten.");
                break;
            case("leave"):
                leaveVoiceChannel();
                embedBuilder.setTitle("Habe den Channel {" + event.getMember().getVoiceState().getChannel().getName() + "} verlassen.");
                break;
            case("play"):
                playTrack(args[1], event);
                embedBuilder.clear();
                break;
            case("skipp"):
                trackScheduler.skipp();
                embedBuilder.setTitle("Track wurde übersprungen.");
                break;
            case("playlist"):
            case("queue"):
                embedBuilder.setTitle("Momentane Playlist:");
                if(trackScheduler.getCurrentTrack() != null) {
                    embedBuilder.addField("Momentaner Track:", trackScheduler.getCurrentTrack().getInfo().title, false);
                }
                Queue<AudioTrack> tracks = trackScheduler.getPlayList();
                int position = 1;
                for (AudioTrack track : tracks) {
                    embedBuilder.addField("Position {" + position + "} :", track.getInfo().title, false);
                    position++;
                }
                break;
            case("volume"):
                embedBuilder.setTitle("Lautstärke:");
                embedBuilder.setDescription(args[1] + "%");
                trackScheduler.setVolume(Integer.parseInt(args[1]));
                break;
            case("stop"):
                trackScheduler.stop();
                embedBuilder.setTitle("Momentaner Track wurde angehalten.");
                break;
            case("start"):
            case("resume"):
                trackScheduler.resume();
                embedBuilder.setTitle("Track wurde wieder aufgenommen.");
                break;
            case("jump"):
                trackScheduler.jump(Integer.parseInt(args[1]));
                embedBuilder.setTitle("Springe zu Track {" + Integer.parseInt(args[1]) + "}.");
                break;
            case("help"):
                configureHelpEmbedBuilder();
                deletionTime = 30000;
                break;
            default:
                break;
        }
    }

    /***
     * Adds Fields and a Title to the EmbedBuilder to list all commands and their descriptions
     */
    private void configureHelpEmbedBuilder() {
        embedBuilder.setTitle("Übersicht der Befehle für '!musicbox' :");
        embedBuilder.addField("add {URL}", "Fügt ein Lied / Playlist der momentanen Playlist hinzu.",false);
        embedBuilder.addField("current", "Gibt das momentane Lied aus.",false);
        embedBuilder.addField("help", "Gibt die Übersicht der Befehle für '!musicbox' aus.",false);
        embedBuilder.addField("join", "Fordert den Bot auf den momentanen Channel zu betreten.",false);
        embedBuilder.addField("jump {Position}", "Springt zu einem Lied in der Playlist mit angegebner Position.",false);
        embedBuilder.addField("leave", "Fordert den Bot auf den momentanen Channel zu verlassen.",false);
        embedBuilder.addField("playlist / queue", "Gibt die momentane Playlist aus.",false);
        embedBuilder.addField("volume {0-100}", "Setzt die Lautstärke auf einen Wert zwischen 0% und 100%",false);
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
        if(!inChannel) {
            joinVoiceChannel(event.getMember().getVoiceState().getChannel());
        }
        addTrackToQueue(trackUrl, event);
    }

    /***
     *
     * @param channel the VoiceChannel which should be joined
     */
    private void joinVoiceChannel(VoiceChannel channel) {
        this.audioManager = channel.getGuild().getAudioManager();
        audioManager.setSendingHandler(new MusicBoxHandler(audioPlayer));
        audioManager.openAudioConnection(channel);
        this.inChannel = true;
    }

    /***
     *
     * @param trackUrl Url to music-video
     * @param event the event of the current command
     */
    private void addTrackToQueue(String trackUrl, MessageReceivedEvent event) {
        audioPlayerManager.loadItem(trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                trackScheduler.queue(track);
                if(inChannel) {
                    trackScheduler.play();
                }
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                for (AudioTrack track : playlist.getTracks()) {
                    trackScheduler.queue(track);
                }
                if(inChannel) {
                    trackScheduler.play();
                }
            }

            @Override
            public void noMatches() {
                // Notify the user that we've got nothing
                event.getMessage()
                        .getChannel()
                        .sendMessage(String.format("Konnte den angegebenen Track '%s' nicht finden.", trackUrl))
                        .queue();
            }

            @Override
            public void loadFailed(FriendlyException throwable) {
                // Notify the user that everything exploded
                event.getMessage()
                        .getChannel()
                        .sendMessage(String.format("Konnte den angegebenen Track '%s' nicht laden.", trackUrl))
                        .queue();
            }
        });
    }

    /***
     * Stops the music and leaves the current VoiceChannel
     */
    public void leaveVoiceChannel() {
        if (this.audioManager != null) {
            trackScheduler.stop();
            audioPlayerManager.shutdown();
            audioManager.closeAudioConnection();
            this.inChannel = false;
        }
    }

    /***
     *
     * @return AudioManager
     */
    public AudioManager getAudioManager() {
        return this.audioManager;
    }
}
