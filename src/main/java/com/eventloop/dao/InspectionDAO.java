package com.eventloop.dao;

import com.eventloop.model.InspectionModel;
import com.eventloop.util.DateUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class InspectionDAO {

    public boolean insertInspection(InspectionModel ins) throws SQLException {
        String sql = "INSERT INTO resource_inspection (reservation_id, resource_id, event_id, inspection_type, " +
                "inspector_name, inspection_date, condition_rating, quantity_checked, quantity_returned, missing_quantity, " +
                "damage_reported, cleaning_required, repair_required, outcome_status, notes) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ins.getReservationId());
            ps.setString(2, ins.getResourceId());
            ps.setString(3, ins.getEventId());
            ps.setString(4, ins.getInspectionType());
            ps.setString(5, ins.getInspectorName());
            ps.setString(6, ins.getInspectionDate() != null ? ins.getInspectionDate() : DateUtil.now());
            ps.setInt(7, ins.getConditionRating());
            ps.setInt(8, ins.getQuantityChecked());
            ps.setInt(9, ins.getQuantityReturned());
            ps.setInt(10, ins.getMissingQuantity());
            ps.setInt(11, ins.isDamageReported() ? 1 : 0);
            ps.setInt(12, ins.isCleaningRequired() ? 1 : 0);
            ps.setInt(13, ins.isRepairRequired() ? 1 : 0);
            ps.setString(14, ins.getOutcomeStatus());
            ps.setString(15, ins.getNotes());
            return ps.executeUpdate() > 0;
        }
    }

    public List<InspectionModel> getInspectionsByEvent(String eventId) throws SQLException {
        List<InspectionModel> list = new ArrayList<>();
        String sql = "SELECT * FROM resource_inspection WHERE event_id = ? ORDER BY inspection_id DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapInspection(rs));
                }
            }
        }
        return list;
    }

    public List<InspectionModel> getInspectionsByResource(String resourceId) throws SQLException {
        List<InspectionModel> list = new ArrayList<>();
        String sql = "SELECT * FROM resource_inspection WHERE resource_id = ? ORDER BY inspection_id DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, resourceId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapInspection(rs));
                }
            }
        }
        return list;
    }

    public boolean hasPendingPostUseInspection(String eventId) throws SQLException {
        // If event has reservations that are ACTIVE or RETURNED without a matching POST_USE inspection
        String sql = "SELECT COUNT(*) FROM reservations r " +
                "WHERE r.event_id = ? AND r.reservation_status IN ('ACTIVE', 'OVERDUE') " +
                "AND NOT EXISTS (SELECT 1 FROM resource_inspection i WHERE i.reservation_id = r.reservation_id AND i.inspection_type = 'POST_USE')";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    private InspectionModel mapInspection(ResultSet rs) throws SQLException {
        InspectionModel ins = new InspectionModel();
        ins.setInspectionId(rs.getInt("inspection_id"));
        ins.setReservationId(rs.getInt("reservation_id"));
        ins.setResourceId(rs.getString("resource_id"));
        ins.setEventId(rs.getString("event_id"));
        ins.setInspectionType(rs.getString("inspection_type"));
        ins.setInspectorName(rs.getString("inspector_name"));
        ins.setInspectionDate(rs.getString("inspection_date"));
        ins.setConditionRating(rs.getInt("condition_rating"));
        ins.setQuantityChecked(rs.getInt("quantity_checked"));
        ins.setQuantityReturned(rs.getInt("quantity_returned"));
        ins.setMissingQuantity(rs.getInt("missing_quantity"));
        ins.setDamageReported(rs.getInt("damage_reported") == 1);
        ins.setCleaningRequired(rs.getInt("cleaning_required") == 1);
        ins.setRepairRequired(rs.getInt("repair_required") == 1);
        ins.setOutcomeStatus(rs.getString("outcome_status"));
        ins.setNotes(rs.getString("notes"));
        return ins;
    }
}
