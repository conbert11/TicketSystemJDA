package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TicketManager {
    private static final String URL = "jdbc:mysql://SERVER_ADDRESS:PORT/BOT_DATABASE";
    private static final String USER = "YOUR_MYSQL_USERNAME";
    private static final String PASSWORD = BotSecrets.dbpassword;

    public TicketManager() {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String sql = "CREATE TABLE IF NOT EXISTS tickets (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "user_id VARCHAR(255) NOT NULL," +
                    "channel_id VARCHAR(255) NOT NULL," +
                    "anliegen TEXT NOT NULL)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.execute();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void createTicket(String userId, String channelId, String anliegen) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String sql = "INSERT INTO tickets (user_id, channel_id, anliegen) VALUES (?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userId);
            pstmt.setString(2, channelId);
            pstmt.setString(3, anliegen);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void closeTicket(String channelId) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String sql = "DELETE FROM tickets WHERE channel_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, channelId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public String getTicketAnliegen(String channelId) {
        String anliegen = null;
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String sql = "SELECT anliegen FROM tickets WHERE channel_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, channelId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                anliegen = rs.getString("anliegen");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return anliegen;
    }
}