package org.example;


import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.sql.SQLException;

public class Main {

    public static String prefix = "?";
    public static JDA bot;


    public static void main(String[] args) throws SQLException {

        JDABuilder jda = JDABuilder.createDefault(BotSecrets.TOKEN)
                .setStatus(OnlineStatus.ONLINE)
                .setChunkingFilter(ChunkingFilter.ALL)
                .setActivity(Activity.playing("mit Conbert11 Dev."))
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES, GatewayIntent.MESSAGE_CONTENT)
                .enableCache(CacheFlag.ONLINE_STATUS)
                .addEventListeners(
                        new TicketSystem()
                );


        JDA bot = jda.build();
        System.out.println("[INFO] Der JDA Discord Bot wurde erfolgreich gestartet!");
    }
}
