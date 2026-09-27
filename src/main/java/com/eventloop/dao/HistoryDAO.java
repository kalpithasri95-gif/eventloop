package com.eventloop.dao;

import com.eventloop.model.HistoryModel;
import com.eventloop.util.DateUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class HistoryDAO {

    public boolean addHistory(String resourceId, String actionType, String performedBy, String details) {
        String sql = "INSERT INTO resource_history (resource_id, action_type, action_date, performed_by, details) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, resourceId);
            ps.setString(2, actionType);
            ps.setString(3, DateUtil.now());
            ps.setString(4, performedBy);
            ps.setString(5, details);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error recording history: " + e.getMessage());
            return false;
        }
    }

    public List<HistoryModel> getHistoryForResource(String resourceId) throws SQLException {
        List<HistoryModel> list = new ArrayList<>();
        String sql = "SELECT * FROM resource_history WHERE resource_id = ? ORDER BY history_id DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, resourceId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new HistoryModel(
                            rs.getInt("history_id"),
                            rs.getString("resource_id"),
                            rs.getString("action_type"),
                            rs.getString("action_date"),
                            rs.getString("performed_by"),
                            rs.getString("details")
                    ));
                }
            }
        }
        return list;
    }

    public List<HistoryModel> getAllHistory() throws SQLException {
        List<HistoryModel> list = new ArrayList<>();
        String sql = "SELECT * FROM resource_history ORDER BY history_id DESC LIMIT 100";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new HistoryModel(
                        rs.getInt("history_id"),
                        rs.getString("resource_id"),
                        rs.getString("action_type"),
                        rs.getString("action_date"),
                        rs.getString("performed_by"),
                        rs.getString("details")
                ));
            }
        }
        return list;
    }
}
