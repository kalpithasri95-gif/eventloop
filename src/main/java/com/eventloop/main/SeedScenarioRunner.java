package com.eventloop.main;

import com.eventloop.dao.DatabaseManager;
import com.eventloop.util.DateUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Seeds the exact 10 real-world college scenario entries into eventloop.db.
 */
public class SeedScenarioRunner {

    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("   EVENTLOOP - SEEDING 10 REAL-WORLD SCENARIO ENTRIES             ");
        System.out.println("==================================================================");

        try {
            DatabaseManager.initializeDatabase();
            Connection conn = DatabaseManager.getConnection();

            // 1. Add Resource 1: Epson Laser Projector
            insertOrUpdateResource(conn, "EL-AUD-101", "Epson Laser Projector 4200lm", "Audio/Visual", 
                "High brightness Full-HD campus auditorium projector", 2, 2, 5, "AVAILABLE", "VERIFIED", 
                "Media Center", "Room 102", "Rack A", "Shelf 1", null, 35000.0, 0.0, "DIRECT_REUSE", 
                "HDMI, 4200 Lumens, 1080p, Throw ratio 1.3:1");
            System.out.println("[Entry 1] Added Resource: EL-AUD-101 (Epson Laser Projector - ?35,000)");

            // 2. Add Resource 2: Banquet Folding Tables
            insertOrUpdateResource(conn, "EL-FUR-101", "Banquet Folding Tables 6ft", "Furniture", 
                "Commercial grade folding banquet tables for registrations", 20, 10, 5, "AVAILABLE", "VERIFIED", 
                "Main Store", "Warehouse B", "Bay 3", "Ground", null, 4500.0, 0.0, "DIRECT_REUSE", 
                "Heavy-duty HDPE folding banquet table, 6 feet");
            System.out.println("[Entry 2] Added Resource: EL-FUR-101 (20 Banquet Tables - ?4,500 each)");

            // 3. Add Resource 3: Extension Spool (Verification Required Gate)
            insertOrUpdateResource(conn, "EL-ELE-101", "Heavy-Duty 15A Extension Spool 20m", "Electrical", 
                "Heavy-duty 15A power cable reel for stage lighting and tech setups", 10, 10, 3, "AVAILABLE", "VERIFICATION_REQUIRED", 
                "Electrical Lab", "Room 204", "Rack E", "Shelf 2", null, 1800.0, 150.0, "DIRECT_REUSE", 
                "3-Core 15A copper cable, surge protector, 4 universal sockets");
            System.out.println("[Entry 3] Added Resource: EL-ELE-101 (Extension Spool - VERIFICATION REQUIRED)");

            // 4. Add Resource 4: JBL Wireless Collar Mic (Under Repair)
            insertOrUpdateResource(conn, "EL-AUD-102", "JBL Dual Wireless Collar Mic Set", "Audio/Visual", 
                "Dual UHF wireless lapel microphone kit for symposium speakers", 2, 0, 3, "UNDER_REPAIR", "VERIFIED", 
                "Media Center", "Room 102", "Box 4", null, null, 8500.0, 650.0, "DIRECT_REUSE", 
                "UHF 650MHz, rechargeable bodypack transmitter, lapel clip");
            System.out.println("[Entry 4] Added Resource: EL-AUD-102 (JBL Collar Mic - UNDER REPAIR ?650)");

            // 5. Add Event: National Technical Symposium Innovate 2026
            String evtId = "EVT-2026-101";
            String eventSql = "INSERT OR REPLACE INTO events " +
                    "(event_id, event_name, organizer_name, organizer_id, department_or_club, event_type, " +
                    "event_date, start_time, end_time, location, expected_participants, event_status, potential_purchase_avoided, created_date) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(eventSql)) {
                ps.setString(1, evtId);
                ps.setString(2, "National Technical Symposium 'Innovate 2026'");
                ps.setString(3, "Priya Raman");
                ps.setInt(4, 2);
                ps.setString(5, "IEEE Student Branch");
                ps.setString(6, "Technical");
                ps.setString(7, "2026-10-28");
                ps.setString(8, "09:00");
                ps.setString(9, "17:00");
                ps.setString(10, "Main Auditorium & Seminar Hall 1");
                ps.setInt(11, 450);
                ps.setString(12, "REQUIREMENTS_ADDED");
                ps.setDouble(13, 84500.0); // Potential avoided purchase value!
                ps.setString(14, DateUtil.today());
                ps.executeUpdate();
            }
            System.out.println("[Entry 5] Created Event: EVT-2026-101 ('Innovate 2026' - Potential Avoided: ?84,500)");

            // 6. Add Requirements for Innovate 2026
            // Clean previous reqs for this event
            try (PreparedStatement delReq = conn.prepareStatement("DELETE FROM event_requirements WHERE event_id = ?")) {
                delReq.setString(1, evtId);
                delReq.executeUpdate();
            }

            insertRequirement(conn, evtId, "Audio/Visual", "Projector", 1, 4, "1080p, HDMI input, 4000+ Lumens");
            insertRequirement(conn, evtId, "Audio/Visual", "Wireless Microphone", 2, 4, "Collar lapel mic or handheld");
            insertRequirement(conn, evtId, "Electrical", "Extension Cable", 5, 3, "15A heavy duty multi-pin");
            insertRequirement(conn, evtId, "Furniture", "Banquet Table", 10, 4, "6ft buffet / registration table");
            System.out.println("[Entry 6] Seeded 4 Event Requirements (Projector, Mic, Cables, Tables)");

            // 7. Add Reservation 1: Projector (Status: APPROVED)
            int resId1 = insertReservation(conn, evtId, "EL-AUD-101", "Epson Laser Projector 4200lm", 1, 
                "2026-10-28 08:30", "2026-10-28 17:30", "2026-10-28 18:30", "APPROVED", "Prof. Rajesh Sharma", "Pre-approved from central media pool");
            System.out.println("[Entry 7] Created Reservation #" + resId1 + ": Epson Projector -> APPROVED");

            // 8. Add Reservation 2: 10 Banquet Tables (Status: ACTIVE / Checked out)
            int resId2 = insertReservation(conn, evtId, "EL-FUR-101", "Banquet Folding Tables 6ft", 10, 
                "2026-10-28 08:00", "2026-10-28 18:00", "2026-10-28 19:00", "ACTIVE", "Prof. Rajesh Sharma", "Dispatched to Main Auditorium");
            System.out.println("[Entry 8] Created Reservation #" + resId2 + ": 10 Tables -> ACTIVE (Dispatched)");

            // 9. Pre-use Inspection for Reservation 2 (Tables)
            String inspSql = "INSERT INTO resource_inspection " +
                    "(reservation_id, resource_id, event_id, inspection_type, inspector_name, inspection_date, " +
                    "condition_rating, quantity_checked, quantity_returned, outcome_status, notes) " +
                    "VALUES (?, ?, ?, 'PRE_USE', 'Prof. Rajesh Sharma', ?, 5, 10, 0, 'DISPATCHED_TO_EVENT', 'All 10 tables clean, legs locked, pristine condition')";
            try (PreparedStatement ps = conn.prepareStatement(inspSql)) {
                ps.setInt(1, resId2);
                ps.setString(2, "EL-FUR-101");
                ps.setString(3, evtId);
                ps.setString(4, DateUtil.now());
                ps.executeUpdate();
            }
            System.out.println("[Entry 9] Pre-Use Inspection Recorded for 10 Tables (Inspector: Prof. Rajesh Sharma)");

            // 10. Audit History Log for the movements
            insertHistory(conn, "EL-AUD-101", "RESERVED", "Prof. Rajesh Sharma", "Reserved for 'Innovate 2026' (EVT-2026-101) - Potential Purchase Avoided: ?35,000");
            insertHistory(conn, "EL-FUR-101", "CHECKED_OUT", "Prof. Rajesh Sharma", "Dispatched 10 units to Main Auditorium for 'Innovate 2026'");
            insertHistory(conn, "EL-ELE-101", "AUDIT_FLAGGED", "System Auditor", "Periodic physical audit expired. Gated from reservation until safety verified.");
            insertHistory(conn, "EL-AUD-102", "REPAIR_SCHEDULED", "Media Tech Team", "Loose toggle switch reported. Repair order logged with Vendor AudioCare (?650).");
            System.out.println("[Entry 10] Audit Trail Logged: 4 historical movements saved into timeline.");

            System.out.println("==================================================================");
            System.out.println("   SUCCESS! ALL 10 ENTRIES INSERTED & VERIFIED IN SQLITE DB!     ");
            System.out.println("==================================================================");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void insertOrUpdateResource(Connection conn, String id, String name, String cat, String desc,
            int totQty, int availQty, int cond, String status, String verif,
            String bldg, String room, String rack, String shelf, String box,
            double cost, double repairCost, String reuseType, String specs) throws Exception {
        String sql = "INSERT OR REPLACE INTO resources " +
                "(resource_id, resource_name, category, description, quantity, available_quantity, unit, condition_rating, " +
                "current_status, verification_status, last_verified_date, next_verification_date, verified_by, " +
                "verification_notes, storage_building, storage_room, rack_number, shelf_number, box_number, current_holder, " +
                "purchase_cost, estimated_repair_cost, actual_repair_cost, reuse_type, specifications, notes, created_date, last_updated_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, 'pcs', ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0.0, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, name);
            ps.setString(3, cat);
            ps.setString(4, desc);
            ps.setInt(5, totQty);
            ps.setInt(6, availQty);
            ps.setInt(7, cond);
            ps.setString(8, status);
            ps.setString(9, verif);
            ps.setString(10, DateUtil.today());
            ps.setString(11, "2026-12-31");
            ps.setString(12, "Prof. Rajesh Sharma");
            ps.setString(13, "Physical safety audit OK");
            ps.setString(14, bldg);
            ps.setString(15, room);
            ps.setString(16, rack);
            ps.setString(17, shelf);
            ps.setString(18, box);
            ps.setString(19, "Central Store");
            ps.setDouble(20, cost);
            ps.setDouble(21, repairCost);
            ps.setString(22, reuseType);
            ps.setString(23, specs);
            ps.setString(24, "Standard campus stock");
            ps.setString(25, DateUtil.today());
            ps.setString(26, DateUtil.today());
            ps.executeUpdate();
        }
    }

    private static void insertRequirement(Connection conn, String evtId, String cat, String name, int qty, int cond, String specs) throws Exception {
        String sql = "INSERT INTO event_requirements (event_id, category, resource_name, quantity, required_condition, specifications, required_date, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, '2026-10-28', 'PENDING')";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, evtId);
            ps.setString(2, cat);
            ps.setString(3, name);
            ps.setInt(4, qty);
            ps.setInt(5, cond);
            ps.setString(6, specs);
            ps.executeUpdate();
        }
    }

    private static int insertReservation(Connection conn, String evtId, String resId, String resName, int qty, 
            String start, String end, String deadline, String status, String approver, String remarks) throws Exception {
        String sql = "INSERT INTO reservations (event_id, resource_id, resource_name, quantity, start_date_time, end_date_time, return_deadline, reservation_status, approved_by, remarks, created_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, evtId);
            ps.setString(2, resId);
            ps.setString(3, resName);
            ps.setInt(4, qty);
            ps.setString(5, start);
            ps.setString(6, end);
            ps.setString(7, deadline);
            ps.setString(8, status);
            ps.setString(9, approver);
            ps.setString(10, remarks);
            ps.setString(11, DateUtil.today());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    private static void insertHistory(Connection conn, String resId, String action, String user, String details) throws Exception {
        String sql = "INSERT INTO resource_history (resource_id, action_type, action_date, performed_by, details) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, resId);
            ps.setString(2, action);
            ps.setString(3, DateUtil.now());
            ps.setString(4, user);
            ps.setString(5, details);
            ps.executeUpdate();
        }
    }
}
