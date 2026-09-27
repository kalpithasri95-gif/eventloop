package com.eventloop.dao;

import com.eventloop.model.ReservationModel;
import com.eventloop.util.DateUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO {

    public boolean insertReservation(ReservationModel res) throws SQLException {
        String sql = "INSERT INTO reservations (event_id, resource_id, resource_name, quantity, " +
                "start_date_time, end_date_time, return_deadline, reservation_status, approved_by, remarks, created_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, res.getEventId());
            ps.setString(2, res.getResourceId());
            ps.setString(3, res.getResourceName());
            ps.setInt(4, res.getQuantity());
            ps.setString(5, res.getStartDateTime());
            ps.setString(6, res.getEndDateTime());
            ps.setString(7, res.getReturnDeadline());
            ps.setString(8, res.getReservationStatus());
            ps.setString(9, res.getApprovedBy());
            ps.setString(10, res.getRemarks());
            ps.setString(11, DateUtil.now());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateStatus(int reservationId, String status) throws SQLException {
        String sql = "UPDATE reservations SET reservation_status = ? WHERE reservation_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, reservationId);
            return ps.executeUpdate() > 0;
        }
    }

    public ReservationModel getById(int reservationId) throws SQLException {
        String sql = "SELECT * FROM reservations WHERE reservation_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reservationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapReservation(rs);
                }
            }
        }
        return null;
    }

    public List<ReservationModel> getAllReservations() throws SQLException {
        List<ReservationModel> list = new ArrayList<>();
        String sql = "SELECT * FROM reservations ORDER BY reservation_id DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ReservationModel rm = mapReservation(rs);
                // Check if ACTIVE and deadline has passed -> mark as OVERDUE
                if ("ACTIVE".equalsIgnoreCase(rm.getReservationStatus()) && DateUtil.isOverdue(rm.getReturnDeadline())) {
                    rm.setReservationStatus("OVERDUE");
                }
                list.add(rm);
            }
        }
        return list;
    }

    public List<ReservationModel> getReservationsByEvent(String eventId) throws SQLException {
        List<ReservationModel> list = new ArrayList<>();
        String sql = "SELECT * FROM reservations WHERE event_id = ? ORDER BY reservation_id DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapReservation(rs));
                }
            }
        }
        return list;
    }

    public List<ReservationModel> getActiveReservationsForResource(String resourceId) throws SQLException {
        List<ReservationModel> list = new ArrayList<>();
        String sql = "SELECT * FROM reservations WHERE resource_id = ? AND reservation_status IN ('REQUESTED', 'APPROVED', 'ACTIVE', 'OVERDUE')";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, resourceId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapReservation(rs));
                }
            }
        }
        return list;
    }

    public List<ReservationModel> findOverlappingReservations(String resourceId, String startDateTime, String endDateTime) throws SQLException {
        List<ReservationModel> overlaps = new ArrayList<>();
        List<ReservationModel> activeList = getActiveReservationsForResource(resourceId);

        for (ReservationModel r : activeList) {
            if (DateUtil.isOverlap(startDateTime, endDateTime, r.getStartDateTime(), r.getEndDateTime())) {
                overlaps.add(r);
            }
        }
        return overlaps;
    }

    private ReservationModel mapReservation(ResultSet rs) throws SQLException {
        ReservationModel r = new ReservationModel();
        r.setReservationId(rs.getInt("reservation_id"));
        r.setEventId(rs.getString("event_id"));
        r.setResourceId(rs.getString("resource_id"));
        r.setResourceName(rs.getString("resource_name"));
        r.setQuantity(rs.getInt("quantity"));
        r.setStartDateTime(rs.getString("start_date_time"));
        r.setEndDateTime(rs.getString("end_date_time"));
        r.setReturnDeadline(rs.getString("return_deadline"));
        r.setReservationStatus(rs.getString("reservation_status"));
        r.setApprovedBy(rs.getString("approved_by"));
        r.setRemarks(rs.getString("remarks"));
        r.setCreatedDate(rs.getString("created_date"));
        return r;
    }
}
