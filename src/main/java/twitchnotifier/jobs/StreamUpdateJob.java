package twitchnotifier.jobs;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import twitchnotifier.commands.TwitchNotifierCommand;

public class StreamUpdateJob implements Job {

    private static TwitchNotifierCommand twitchNotifierCommand;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        twitchNotifierCommand.updateStreams();
    }

    public static void setTwitchNotifierCommand(TwitchNotifierCommand twitchNotifierCommand) {
        StreamUpdateJob.twitchNotifierCommand = twitchNotifierCommand;
    }
}
