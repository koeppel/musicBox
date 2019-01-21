package twitchnotifier.core;

public class Stream {

    private boolean isOnline = false;

    private String channelName;

    private String thumbnailUrl;

    public Stream(String channelName) {
        this.channelName = channelName;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public String getChannelName() {
        return this.channelName;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }
}
