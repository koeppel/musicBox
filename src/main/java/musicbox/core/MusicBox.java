package musicbox.core;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.managers.AudioManager;

public class MusicBox {

    private AudioPlayerManager audioPlayerManager =  new DefaultAudioPlayerManager();

    private AudioManager audioManager;

    private EmbedBuilder embedBuilder = new EmbedBuilder();

    private AudioPlayer audioPlayer = audioPlayerManager.createPlayer();

    private TrackScheduler trackScheduler = new TrackScheduler(audioPlayer, this);

    private boolean inChannel = false;

    public MusicBox(AudioManager audioManager) {
        this.audioManager = audioManager;
        AudioSourceManagers.registerRemoteSources(audioPlayerManager);
        audioPlayer.addListener(trackScheduler);
        trackScheduler.setVolume(20);
    }

    public void clearQueue() {
        this.trackScheduler.clear();
    }

    public AudioPlayerManager getAudioPlayerManager() {
        return audioPlayerManager;
    }

    public AudioManager getAudioManager() {
        return audioManager;
    }

    public EmbedBuilder getEmbedBuilder() {
        return embedBuilder;
    }

    public AudioPlayer getAudioPlayer() {
        return audioPlayer;
    }

    public TrackScheduler getTrackScheduler() {
        return trackScheduler;
    }

    public boolean isInChannel() {
        return inChannel;
    }

    /***
     * Stops the music and leaves the current VoiceChannel
     */
    public void leaveVoiceChannel() {
        if (this.audioManager != null) {
            trackScheduler.clear();
            trackScheduler.stop();
            audioPlayerManager.shutdown();
            audioManager.closeAudioConnection();
            this.inChannel = false;
        }
    }

    public void setInChannel(boolean inChannel) {
        this.inChannel = inChannel;
    }

    /***
     *
     * @param trackUrl Url to music-video
     * @param event the event of the current command
     */
    public void addTrackToQueue(String trackUrl, MessageReceivedEvent event) {
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
}
