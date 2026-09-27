package com.eventloop.dao;

import com.eventloop.model.EventModel;
import com.eventloop.model.RequirementModel;
import com.eventloop.util.DateUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EventDAO {

    public boolean insertEvent(EventModel evt) throws SQLException {
        String sql = "INSERT INTO events (event_id, event_name, organizer_name, organizer_id, department_or_club, " +
                "event_type, event_date, start_time, end_time, location, expected_participants, event_status, " +
                "potential_purchase_avoided, created_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, evt.getEventId());
            ps.setString(2, evt.getEventName());
            ps.setString(3, evt.getOrganizerName());
            ps.setInt(4, evt.getOrganizerId());
            ps.setString(5, evt.getDepartmentOrClub());
            ps.setString(6, evt.getEventType());
            ps.setString(7, evt.getEventDate());
            ps.setString(8, evt.getStartTime());
            ps.setString(9, evt.getEndTime());
            ps.setString(10, evt.getLocation());
            ps.setInt(11, evt.getExpectedParticipants());
            ps.setString(12, evt.getEventStatus());
            ps.setDouble(13, evt.getPotentialPurchaseAvoided());
            ps.setString(14, DateUtil.today());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateEvent(EventModel evt) throws SQLException {
        String sql = "UPDATE events SET event_name = ?, organizer_name = ?, department_or_club = ?, " +
                "event_type = ?, event_date = ?, start_time = ?, end_time = ?, location = ?, " +
                "expected_participants = ?, event_status = ?, potential_purchase_avoided = ? WHERE event_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, evt.getEventName());
            ps.setString(2, evt.getOrganizerName());
            ps.setString(3, evt.getDepartmentOrClub());
            ps.setString(4, evt.getEventType());
            ps.setString(5, evt.getEventDate());
            ps.setString(6, evt.getStartTime());
            ps.setString(7, evt.getEndTime());
            ps.setString(8, evt.getLocation());
            ps.setInt(9, evt.getExpectedParticipants());
            ps.setString(10, evt.getEventStatus());
            ps.setDouble(11, evt.getPotentialPurchaseAvoided());
            ps.setString(12, evt.getEventId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateEventStatus(String eventId, String status) throws SQLException {
        String sql = "UPDATE events SET event_status = ? WHERE event_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, eventId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updatePurchaseAvoided(String eventId, double avoidedCost) throws SQLException {
        String sql = "UPDATE events SET potential_purchase_avoided = potential_purchase_avoided + ? WHERE event_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, avoidedCost);
            ps.setString(2, eventId);
            return ps.executeUpdate() > 0;
        }
    }

    public EventModel getById(String eventId) throws SQLException {
        String sql = "SELECT * FROM events WHERE event_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapEvent(rs);
                }
            }
        }
        return null;
    }

    public List<EventModel> getAllEvents() throws SQLException {
        List<EventModel> list = new ArrayList<>();
        String sql = "SELECT * FROM events ORDER BY event_date DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapEvent(rs));
            }
        }
        return list;
    }

    public List<EventModel> getEventsByOrganizer(int organizerId) throws SQLException {
        List<EventModel> list = new ArrayList<>();
        String sql = "SELECT * FROM events WHERE organizer_id = ? ORDER BY event_date DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, organizerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapEvent(rs));
                }
            }
        }
        return list;
    }

    // Requirements operations
    public boolean insertRequirement(RequirementModel req) throws SQLException {
        String sql = "INSERT INTO event_requirements (event_id, category, resource_name, quantity, " +
                "required_condition, specifications, required_date, start_time, end_time, indoor_outdoor, notes, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, req.getEventId());
            ps.setString(2, req.getCategory());
            ps.setString(3, req.getResourceName());
            ps.setInt(4, req.getQuantity());
            ps.setInt(5, req.getRequiredCondition());
            ps.setString(6, req.getSpecifications());
            ps.setString(7, req.getRequiredDate());
            ps.setString(8, req.getStartTime());
            ps.setString(9, req.getEndTime());
            ps.setString(10, req.getIndoorOutdoor());
            ps.setString(11, req.getNotes());
            ps.setString(12, req.getStatus());
            return ps.executeUpdate() > 0;
        }
    }

    public List<RequirementModel> getRequirementsForEvent(String eventId) throws SQLException {
        List<RequirementModel> list = new ArrayList<>();
        String sql = "SELECT * FROM event_requirements WHERE event_id = ? ORDER BY requirement_id";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRequirement(rs));
                }
            }
        }
        return list;
    }

    public boolean updateRequirementStatus(int reqId, String status) throws SQLException {
        String sql = "UPDATE event_requirements SET status = ? WHERE requirement_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, reqId);
            return ps.executeUpdate() > 0;
        }
    }

    private EventModel mapEvent(ResultSet rs) throws SQLException {
        EventModel e = new EventModel();
        e.setEventId(rs.getString("event_id"));
        e.setEventName(rs.getString("event_name"));
        e.setOrganizerName(rs.getString("organizer_name"));
        e.setOrganizerId(rs.getInt("organizer_id"));
        e.setDepartmentOrClub(rs.getString("department_or_club"));
        e.setEventType(rs.getString("event_type"));
        e.setEventDate(rs.getString("event_date"));
        e.setStartTime(rs.getString("start_time"));
        e.setEndTime(rs.getString("end_time"));
        e.setLocation(rs.getString("location"));
        e.setExpectedParticipants(rs.getInt("expected_participants"));
        e.setEventStatus(rs.getString("event_status"));
        e.setPotentialPurchaseAvoided(rs.getDouble("potential_purchase_avoided"));
        e.setCreatedDate(rs.getString("created_date"));
        return e;
    }

    private RequirementModel mapRequirement(ResultSet rs) throws SQLException {
        RequirementModel r = new RequirementModel();
        r.setRequirementId(rs.getInt("requirement_id"));
        r.setEventId(rs.getString("event_id"));
        r.setCategory(rs.getString("category"));
        r.setResourceName(rs.getString("resource_name"));
        r.setQuantity(rs.getInt("quantity"));
        r.setRequiredCondition(rs.getInt("required_condition"));
        r.setSpecifications(rs.getString("specifications"));
        r.setRequiredDate(rs.getString("required_date"));
        r.setStartTime(rs.getString("start_time"));
        r.setEndTime(rs.getString("end_time"));
        r.setIndoorOutdoor(rs.getString("indoor_outdoor"));
        r.setNotes(rs.getString("notes"));
        r.setStatus(rs.getString("status"));
        return r;
    }
}
