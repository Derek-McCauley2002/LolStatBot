package com.github.DerekMcCauley2002;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.user.UserStatus;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import java.net.URL;

public class Main {
    public static void main(String[] args) {

        String token = **;

        DiscordApi api = new DiscordApiBuilder().setToken(token).login().join();

        api.updateStatus(UserStatus.ONLINE);
        api.updateActivity(ActivityType.COMPETING, " in a theoretical game. | ?help");

        Commands firstCmd = new Commands(api);
        firstCmd.setupCommands();

        // Print the invite url of your bot
        System.out.println("You can invite the bot by using the following url: " + api.createBotInvite());

    }

}
