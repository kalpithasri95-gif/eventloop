package com.eventloop.service;

import com.eventloop.dao.DatabaseManager;
import com.eventloop.dao.EventDAO;
import com.eventloop.dao.ReservationDAO;
import com.eventloop.dao.ResourceDAO;
import com.eventloop.model.EventModel;
import com.eventloop.model.ReservationModel;
import com.eventloop.model.resources.AbstractResource;
import com.eventloop.util.DateUtil;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ReportService {
    private static ReportService instance;
    private final ResourceDAO resourceDAO;
    private final EventDAO eventDAO;
    private final ReservationDAO reservationDAO;

    private ReportService() {
        this.resourceDAO = new ResourceDAO();
        this.eventDAO = new EventDAO();
        this.reservationDAO = new ReservationDAO();
    }

    public static synchronized ReportService getInstance() {
        if (instance == null) {
            instance = new ReportService();
        }
        return instance;
    }

    // 1. Available resources report
    public List<String[]> getAvailableResourcesReport() throws SQLException {
        List<String[]> rows = new ArrayList<>();
        List<AbstractResource> list = resourceDAO.searchResources("", "All Categories", "AVAILABLE", "VERIFIED");
        for (AbstractResource r : list) {
            rows.add(new String[]{
                    r.getResourceId(),
                    r.getResourceName(),
                    r.getCategory(),
                    r.getAvailableQuantity() + " " + r.getUnit(),
                    r.getConditionRating() + "/5",
                    r.getFullLocation(),
                    "₹" + String.format("%,.2f", r.getPurchaseCost())
            });
        }
        return rows;
    }

    // 2. Resources under repair
    public List<String[]> getResourcesUnderRepairReport() throws SQLException {
        List<String[]> rows = new ArrayList<>();
        List<AbstractResource> list = resourceDAO.searchResources("", "All Categories", "UNDER_REPAIR", "All");
        for (AbstractResource r : list) {
            rows.add(new String[]{
                    r.getResourceId(),
                    r.getResourceName(),
                    r.getCategory(),
                    r.getConditionRating() + "/5",
                    "₹" + String.format("%,.2f", r.getEstimatedRepairCost()),
                    r.getFullLocation(),
                    r.getNotes() != null ? r.getNotes() : "Repair in progress"
            });
        }
        return rows;
    }

    // 3. Verification-required report
    public List<String[]> getVerificationRequiredReport() throws SQLException {
        List<String[]> rows = new ArrayList<>();
        List<AbstractResource> list = resourceDAO.getAllResources();
        for (AbstractResource r : list) {
            if ("EXPIRED".equalsIgnoreCase(r.getVerificationStatus()) || 
                "VERIFICATION_REQUIRED".equalsIgnoreCase(r.getVerificationStatus()) || 
                DateUtil.isExpired(r.getNextVerificationDate())) {
                rows.add(new String[]{
                        r.getResourceId(),
                        r.getResourceName(),
                        r.getCategory(),
                        r.getVerificationStatus(),
                        r.getLastVerifiedDate() != null ? r.getLastVerifiedDate() : "Never",
                        r.getNextVerificationDate() != null ? r.getNextVerificationDate() : "Not scheduled",
                        r.getVerifiedBy() != null ? r.getVerifiedBy() : "Pending",
                        r.getFullLocation()
                });
            }
        }
        return rows;
    }

    // 4. Event resource usage report
    public List<String[]> getEventResourceUsageReport() throws SQLException {
        List<String[]> rows = new ArrayList<>();
        List<ReservationModel> list = reservationDAO.getAllReservations();
        for (ReservationModel r : list) {
            rows.add(new String[]{
                    String.valueOf(r.getReservationId()),
                    r.getEventId(),
                    r.getResourceId(),
                    r.getResourceName(),
                    String.valueOf(r.getQuantity()),
                    r.getStartDateTime(),
                    r.getEndDateTime(),
                    r.getReservationStatus()
            });
        }
        return rows;
    }

    // 5. Reservation conflict / active list
    public List<String[]> getReservationConflictReport() throws SQLException {
        List<String[]> rows = new ArrayList<>();
        List<ReservationModel> list = reservationDAO.getAllReservations();
        for (ReservationModel r : list) {
            rows.add(new String[]{
                    String.valueOf(r.getReservationId()),
                    r.getResourceId(),
                    r.getResourceName(),
                    r.getEventId(),
                    r.getStartDateTime() + " - " + r.getEndDateTime(),
                    r.getReservationStatus(),
                    r.getRemarks() != null ? r.getRemarks() : "-"
            });
        }
        return rows;
    }

    // 6. Overdue return report
    public List<String[]> getOverdueReturnReport() throws SQLException {
        List<String[]> rows = new ArrayList<>();
        List<ReservationModel> list = reservationDAO.getAllReservations();
        for (ReservationModel r : list) {
            if ("OVERDUE".equalsIgnoreCase(r.getReservationStatus()) || 
                ("ACTIVE".equalsIgnoreCase(r.getReservationStatus()) && DateUtil.isOverdue(r.getReturnDeadline()))) {
                rows.add(new String[]{
                        String.valueOf(r.getReservationId()),
                        r.getEventId(),
                        r.getResourceId(),
                        r.getResourceName(),
                        String.valueOf(r.getQuantity()),
                        r.getReturnDeadline(),
                        "OVERDUE - Return Immediately"
                });
            }
        }
        return rows;
    }

    // 7. Resource reuse report
    public List<String[]> getResourceReuseReport() throws SQLException {
        List<String[]> rows = new ArrayList<>();
        List<AbstractResource> list = resourceDAO.getAllResources();
        for (AbstractResource r : list) {
            rows.add(new String[]{
                    r.getResourceId(),
                    r.getResourceName(),
                    r.getCategory(),
                    r.getReuseType(),
                    r.getConditionRating() + "/5",
                    r.getCurrentStatus(),
                    "₹" + String.format("%,.2f", r.getPurchaseCost())
            });
        }
        return rows;
    }

    // 8. Repair cost report
    public List<String[]> getRepairCostReport() throws SQLException {
        List<String[]> rows = new ArrayList<>();
        String sql = "SELECT * FROM resources WHERE estimated_repair_cost > 0 OR actual_repair_cost > 0";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                rows.add(new String[]{
                        rs.getString("resource_id"),
                        rs.getString("resource_name"),
                        rs.getString("category"),
                        "₹" + String.format("%,.2f", rs.getDouble("estimated_repair_cost")),
                        "₹" + String.format("%,.2f", rs.getDouble("actual_repair_cost")),
                        rs.getString("current_status")
                });
            }
        }
        return rows;
    }

    // 9. Potential purchase avoidance report
    public List<String[]> getPotentialPurchaseAvoidanceReport() throws SQLException {
        List<String[]> rows = new ArrayList<>();
        List<EventModel> events = eventDAO.getAllEvents();
        double totalAvoided = 0.0;
        for (EventModel e : events) {
            totalAvoided += e.getPotentialPurchaseAvoided();
            rows.add(new String[]{
                    e.getEventId(),
                    e.getEventName(),
                    e.getDepartmentOrClub(),
                    e.getEventDate(),
                    e.getEventStatus(),
                    "₹" + String.format("%,.2f", e.getPotentialPurchaseAvoided())
            });
        }
        rows.add(new String[]{
                "TOTAL AVOIDED", "Campus-wide Procurement Savings", "-", "-", "-", "₹" + String.format("%,.2f", totalAvoided)
        });
        return rows;
    }

    // 10. Resource history report
    public List<String[]> getResourceHistoryReport() throws SQLException {
        List<String[]> rows = new ArrayList<>();
        String sql = "SELECT * FROM resource_history ORDER BY history_id DESC LIMIT 100";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                rows.add(new String[]{
                        String.valueOf(rs.getInt("history_id")),
                        rs.getString("resource_id"),
                        rs.getString("action_type"),
                        rs.getString("action_date"),
                        rs.getString("performed_by"),
                        rs.getString("details")
                });
            }
        }
        return rows;
    }

    public boolean exportToCSV(String filePath, String[] headers, List<String[]> rows) {
        try (FileWriter fw = new FileWriter(filePath)) {
            // Write headers
            fw.write(String.join(",", escapeHeaders(headers)) + "\n");
            // Write rows
            for (String[] row : rows) {
                StringBuilder line = new StringBuilder();
                for (int i = 0; i < row.length; i++) {
                    String val = row[i] != null ? row[i].replace("\"", "\"\"") : "";
                    line.append("\"").append(val).append("\"");
                    if (i < row.length - 1) line.append(",");
                }
                line.append("\n");
                fw.write(line.toString());
            }
            return true;
        } catch (IOException e) {
            System.err.println("CSV export error: " + e.getMessage());
            return false;
        }
    }

    private String[] escapeHeaders(String[] headers) {
        String[] res = new String[headers.length];
        for (int i = 0; i < headers.length; i++) {
            res[i] = "\"" + headers[i].replace("\"", "\"\"") + "\"";
        }
        return res;
    }
}
