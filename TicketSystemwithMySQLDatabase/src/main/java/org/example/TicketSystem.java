package org.example;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import org.example.TicketManager;

import java.awt.*;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public class TicketSystem extends ListenerAdapter {

    private final TicketManager ticketManager = new TicketManager();
    private final Map<String, Boolean> ticketClaims = new HashMap<>();

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("setup-ticket")) {
            if (event.getMember().hasPermission(Permission.ADMINISTRATOR)) {

                event.reply("erfolgreich").setEphemeral(true).queue();

                EmbedBuilder e = new EmbedBuilder()
                        .setColor(Color.red)
                        .setDescription("## \uD83D\uDE91 × TICKET-SUPPORT\n" +
                                "Hier kannst du das Team kontaktieren!\n" +
                                "Falls du eine __Frage__ hast, oder __Hilfe__ brauchst oder auch einen __Anliegen__ hast dann kannst du hier ein Ticket öffnen. \n\n" +
                                "**WICHTIG:** \n" +
                                "- Das Ausnutzen vom Ticket-Support wird bestraft. \n" +
                                "- Das Beleidigen eines Teammitglieds im Ticket-Support wird mit einem Timeout oder mit einem Warn bestraft! \n" +
                                "- Es werden nur Links reingeschickt, wenn du aufgefordert wurdest!");

                Button ticketcreate = Button.success("ticketcreateid", "Ticket-Erstellen");

                event.getChannel().sendMessageEmbeds(e.build()).addActionRow(ticketcreate).queue();

            } else {
                event.reply("Du hast keine Berechtigung!").setEphemeral(true).queue();
            }
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (event.getButton().getId().equals("ticketcreateid")) {

            TextInput anliegeninput = TextInput.create("anliegeninputid", "Anliegen", TextInputStyle.PARAGRAPH).setPlaceholder("Hier Anliegen angeben!").build();

            Modal ticketmodal = Modal.create("ticketmodalid", "Ticket-Support").addActionRow(anliegeninput).build();

            event.replyModal(ticketmodal).queue();
        }

        if (event.getButton().getId().equals("closebuttonid")) {
            Member m = event.getMember();
            String anliegen = ticketManager.getTicketAnliegen(event.getChannel().getId());
            ticketManager.closeTicket(event.getChannel().getId());
            event.getChannel().delete().queue();
        }

        if (event.getButton().getId().equals("claimbuttonid")) {
            Member m = event.getMember();
            String channelId = event.getChannel().getId();

            if (ticketClaims.containsKey(channelId) && ticketClaims.get(channelId)) {
                EmbedBuilder alreadyClaimedEmbed = new EmbedBuilder()
                        .setColor(Color.orange)
                        .setDescription("Ticket wurde bereits übernommen!");

                event.replyEmbeds(alreadyClaimedEmbed.build()).setEphemeral(true).queue();

            } else {
                ticketClaims.put(channelId, true);

                EmbedBuilder claimEmbed = new EmbedBuilder()
                        .setColor(Color.green)
                        .setDescription(m.getAsMention() + " hat dieses Ticket übernommen!");

                event.getChannel().sendMessageEmbeds(claimEmbed.build()).queue();
            }
        }
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        if (event.getModalId().equals("ticketmodalid")) {

            Member m = event.getMember();
            String a = event.getValue("anliegeninputid").getAsString();

            TextChannel textChannel = event.getGuild().getCategoryById("TICKET_CATEGORY_ID").createTextChannel("ticket-" + event.getMember().getEffectiveName()).complete();
            textChannel.getManager().putPermissionOverride(m, EnumSet.of(Permission.VIEW_CHANNEL), null).complete();

            EmbedBuilder asdd = new EmbedBuilder()
                    .setColor(Color.red)
                    .setDescription("Es wurde für dich ein Ticket erstellt! Dein Anliegen ist `" + a + "`!")
                    .setTitle("Ticket erstellt!");

            event.replyEmbeds(asdd.build()).setEphemeral(true).queue();

            EmbedBuilder er = new EmbedBuilder()
                    .setColor(Color.red)
                    .setDescription("## 🎫 × TICKET-SUPPORT\n" +
                            "Willkommen im **Ticket-Support**! \n" +
                            "Das **Server-Team** wurde bereits über dein **Ticket kontaktiert**!\n" +
                            "Habe einfach nur **Geduld**!\n" +
                            "\n" +
                            "**Anliegen:** " + a + " \n" +
                            "**Ticket-Ersteller:** " + m.getAsMention());

            Button closebutton = Button.danger("closebuttonid", "Ticket-Schließen");
            Button claimbutton = Button.primary("claimbuttonid", "Ticket-Übernehmen");

            textChannel.sendMessage(m.getAsMention()).addEmbeds(er.build()).addActionRow(closebutton, claimbutton).queue();

            EmbedBuilder logt = new EmbedBuilder()
                    .setColor(Color.red)
                    .setTitle("Ticket-Log")
                    .setDescription("Es wurde ein Ticket erstellt! \n" +
                            "**Ticket-Ersteller:** " + event.getMember().getAsMention() + "\n" +
                            "**Anliegen:** " + a);

            event.getGuild().getTextChannelById("TICKET_LOG_CHANNEL_ID").sendMessageEmbeds(logt.build()).queue();

            ticketManager.createTicket(m.getId(), textChannel.getId(), a);
        }
    }
}
