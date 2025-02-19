package org.example;

import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;
import java.util.List;

import static net.dv8tion.jda.api.interactions.commands.OptionType.INTEGER;

public class register extends ListenerAdapter {


    @Override
    public void onReady(net.dv8tion.jda.api.events.session.ReadyEvent event) {
        List<CommandData> commandData = createCommandData();
        event.getJDA().updateCommands().addCommands(commandData).queue();
    }

    private List<CommandData> createCommandData() {
        List<CommandData> commandData = new ArrayList<>();

        commandData.add(Commands.slash("setup-ticket", "-"));

        return commandData;
    }
}
