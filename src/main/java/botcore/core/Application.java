package botcore.core;

import musicbox.commands.MusicBoxCommand;
import botcore.listeners.CommandListener;
import botcore.listeners.JoinListener;
import botcore.listeners.ReadyListener;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitchnotifier.commands.TwitchNotifierCommand;
import twitchnotifier.jobs.StreamUpdateJob;
import utils.CONFIG;

import javax.security.auth.login.LoginException;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;


public class Application {

    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    private static JDABuilder builder;

    private static TwitchNotifierCommand twitchNotifierCommand = new TwitchNotifierCommand();

    private static MusicBoxCommand musicBoxCommand = new MusicBoxCommand();

    public static void main(String[] args) {
        builder = new JDABuilder(AccountType.BOT);

        builder.setToken(utils.SECRETS.TOKEN);
        builder.setAutoReconnect(CONFIG.RECONNECT);

        addListeners();
        addCommands();

        addScheduledTasks();

        try {
            builder.build();
        } catch (LoginException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private static void addListeners() {
        builder.addEventListener(new JoinListener());
        builder.addEventListener(new ReadyListener());
        builder.addEventListener(new CommandListener());
    }

    private static void addCommands() {
        CommandHandler.addCommand("mb", musicBoxCommand);
        CommandHandler.addCommand("tn", twitchNotifierCommand);
    }

    private static void addScheduledTasks() {
        StreamUpdateJob.setTwitchNotifierCommand(twitchNotifierCommand);
        try {
            SchedulerFactory schedulerFactory = new StdSchedulerFactory();
            Scheduler scheduler = schedulerFactory.getScheduler();

            JobDetail jobDetail = newJob(StreamUpdateJob.class)
                    .withIdentity("StreamUpdateJob")
                    .build();

            CronTrigger trigger = newTrigger()
                    .withIdentity("cronTrigger","myJob1")
                    .withSchedule(cronSchedule("0 0/5 * ? * * *"))
                    .build();

            scheduler.scheduleJob(jobDetail,trigger);
            scheduler.start();

        } catch (SchedulerException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
