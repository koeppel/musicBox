package musicbox;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEvent;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventListener;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import musicbox.commands.MusicBox;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class TrackScheduler implements AudioEventListener {

    private AudioPlayer audioPlayer;

    private Queue<AudioTrack> queue = new LinkedList<>();

    private AudioTrack currentTrack = null;

    private MusicBox musicBox;

    public TrackScheduler(AudioPlayer audioPlayer, MusicBox musicBox) {
        this.audioPlayer = audioPlayer;
        this.musicBox = musicBox;
    }

    /***
     * This is used to check if the Channel is empty or if it should continue playing music
     * @param event AudioEvent like "onEndOfMusic" (or something like that)
     */
    @Override
    public void onEvent(AudioEvent event) {
        if(musicBox.getAudioManager().getConnectedChannel().getMembers().size() == 1) {
            musicBox.leaveVoiceChannel();
        } else if(audioPlayer.getPlayingTrack() == null && !queue.isEmpty()) {
            run();
        }
    }

    /***
     * Takes the next song from queue and plays it
     */
    private void run() {
        AudioTrack track = queue.peek();
        playTrack(track);
    }

    public void queue(AudioTrack track) {
        this.queue.add(track);
    }

    private void playTrack(AudioTrack track) {
        if(track != null) {
            this.audioPlayer.playTrack(track);
            this.currentTrack = track;
            queue.remove(track);
        }
    }

    public void play() {
        if(audioPlayer.getPlayingTrack() == null) {
            run();
        }
    }

    public void skipp() {
        run();
    }

    public void setVolume(int volume) {
        if(volume > 100) {
            volume = 100;
        }
        if(volume < 0) {
            volume = 0;
        }
        audioPlayer.setVolume(volume);
    }

    public void stop() {
        audioPlayer.stopTrack();
    }

    /***
     * Resumes with the current playlist
     */
    public void resume() {
        if(currentTrack == null) {
            run();
        } else {
            AudioTrack clone = currentTrack.makeClone();
            clone.setPosition(currentTrack.getPosition());
            audioPlayer.playTrack(clone);
        }
    }

    /***
     * Removes all Tracks up to the given trackId
     * @param trackId ID of the wanted song
     */
    public void jump(int trackId) {
        if(!queue.isEmpty() && trackId > 0 && trackId <= queue.size()) {
            int i  = 1;
            List<AudioTrack> tracksToRemove = new ArrayList<>();
            for (AudioTrack track : queue) {
                if(trackId != i) {
                    tracksToRemove.add(track);
                    i++;
                } else {
                    playTrack(track);
                    break;
                }
            }
            queue.removeAll(tracksToRemove);
        }
    }

    public Queue<AudioTrack> getPlayList() {
        return this.queue;
    }

    public AudioTrack getCurrentTrack() {
        return this.currentTrack;
    }
}
