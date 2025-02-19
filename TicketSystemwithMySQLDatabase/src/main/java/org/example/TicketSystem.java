package org.example;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
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

import java.awt.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TicketSystem extends ListenerAdapter {

    private final Map<String, String> activeTickets = new HashMap<>();
    private final Map<String, Boolean> ticketClaims = new HashMap<>();

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("setup-ticket")) {
            if (!event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
                event.reply("❌ Du hast keine Berechtigung!").setEphemeral(true).queue();
                return;
            }

            event.reply("✅ Ticket-System erfolgreich eingerichtet!").setEphemeral(true).queue();

            EmbedBuilder embed = new EmbedBuilder()
                    .setColor(Color.red)
                    .setDescription("## 🎫 × TICKET-SUPPORT\n" +
                            "Hier kannst du das Team kontaktieren!\n" +
                            "Falls du eine **Frage** hast oder **Hilfe** brauchst, kannst du hier ein Ticket öffnen.\n\n" +
                            "**Regeln:** \n" +
                            "❌ Missbrauch des Ticket-Systems wird bestraft.\n" +
                            "❌ Beleidigungen gegen Teammitglieder sind verboten.\n" +
                            "❌ Links nur senden, wenn du dazu aufgefordert wirst.");

            Button createTicket = Button.success("ticketcreateid", "🎟 Ticket-Erstellen");

            event.getChannel().sendMessageEmbeds(embed.build()).addActionRow(createTicket).queue();
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String buttonId = event.getButton().getId();
        Member member = event.getMember();

        if (buttonId.equals("ticketcreateid")) {
            if (activeTickets.containsKey(member.getId())) {
                event.reply("❌ Du hast bereits ein offenes Ticket!").setEphemeral(true).queue();
                return;
            }

            TextInput issueInput = TextInput.create("issueinputid", "Anliegen", TextInputStyle.PARAGRAPH)
                    .setPlaceholder("Beschreibe dein Anliegen...")
                    .build();

            Modal ticketModal = Modal.create("ticketmodalid", "🎫 Ticket-Support")
                    .addActionRow(issueInput)
                    .build();

            event.replyModal(ticketModal).queue();
        }

        if (buttonId.equals("closebuttonid")) {
            event.getChannel().delete().queue();
            activeTickets.values().remove(event.getChannel().getId());
        }

        if (buttonId.equals("claimbuttonid")) {
            String channelId = event.getChannel().getId();

            if (ticketClaims.getOrDefault(channelId, false)) {
                event.reply("❌ Dieses Ticket wurde bereits übernommen!").setEphemeral(true).queue();
            } else {
                ticketClaims.put(channelId, true);
                event.getChannel().sendMessage("✅ " + member.getAsMention() + " hat dieses Ticket übernommen!").queue();
            }
        }
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        if (!event.getModalId().equals("ticketmodalid")) return;

        Member member = event.getMember();
        String issue = event.getValue("issueinputid").getAsString();

        if (activeTickets.containsKey(member.getId())) {
            event.reply("❌ Du hast bereits ein offenes Ticket!").setEphemeral(true).queue();
            return;
        }

        Category ticketCategory = event.getGuild().getCategoryById("TICKET_CATEGORY_ID");
        if (ticketCategory == null) {
            event.reply("❌ Fehler: Ticket-Kategorie nicht gefunden!").setEphemeral(true).queue();
            return;
        }

        TextChannel ticketChannel = ticketCategory.createTextChannel("ticket-" + member.getEffectiveName()).complete();
        ticketChannel.getManager().putPermissionOverride(member, Collections.singleton(Permission.VIEW_CHANNEL), null).queue();

        activeTickets.put(member.getId(), ticketChannel.getId());

        EmbedBuilder userEmbed = new EmbedBuilder()
                .setColor(Color.red)
                .setTitle("✅ Ticket erstellt!")
                .setDescription("Dein Ticket wurde erstellt! Dein Anliegen: `" + issue + "`");

        event.replyEmbeds(userEmbed.build()).setEphemeral(true).queue();

        EmbedBuilder ticketEmbed = new EmbedBuilder()
                .setColor(Color.red)
                .setDescription("## 🎫 × TICKET-SUPPORT\n" +
                        "Willkommen im **Ticket-Support**!\n" +
                        "Das **Server-Team** wurde benachrichtigt. Bitte habe etwas Geduld.\n\n" +
                        "**Anliegen:** " + issue + " \n" +
                        "**Ersteller:** " + member.getAsMention());

        Button closeButton = Button.danger("closebuttonid", "❌ Ticket-Schließen");
        Button claimButton = Button.primary("claimbuttonid", "✅ Ticket-Übernehmen");

        ticketChannel.sendMessage(member.getAsMention()).addEmbeds(ticketEmbed.build()).addActionRow(closeButton, claimButton).queue();
    }
}
