package utils;

import net.dv8tion.jda.core.entities.Message;

import java.util.Timer;
import java.util.TimerTask;

public class UTILS {

    public static void clearMessage(Message msg, int time) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                msg.delete().queue();
            }
        }, time);
    }

}
