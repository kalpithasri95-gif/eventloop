package com.eventloop.dao;

import com.eventloop.util.DateUtil;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manages SQLite database connection, table initialization, and initial sample data seeding.
 */
public class DatabaseManager {
    private static final String DB_FILE = "eventloop.db";
    private static final String DB_URL = "jdbc:sqlite:" + DB_FILE;

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC Driver not found: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void initializeDatabase() {
        boolean isNewDb = !new File(DB_FILE).exists() || (new File(DB_FILE).length() == 0);

        try (Connection conn = getConnection()) {
            createTables(conn);
            if (isNewDb || isDataEmpty(conn)) {
                seedInitialData(conn);
            }
        } catch (SQLException e) {
            System.err.println("Database initialization error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static boolean isDataEmpty(Connection conn) {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
        } catch (SQLException e) {
            return true;
        }
        return true;
    }

    private static void createTables(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // Enable foreign keys in SQLite
            stmt.execute("PRAGMA foreign_keys = ON;");

            // Users table
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username TEXT UNIQUE NOT NULL, " +
                    "password TEXT NOT NULL, " +
                    "full_name TEXT NOT NULL, " +
                    "email TEXT UNIQUE NOT NULL, " +
                    "role TEXT NOT NULL, " + // ADMIN, ORGANIZER
                    "department TEXT, " +
                    "created_date TEXT);");

            // Storage locations
            stmt.execute("CREATE TABLE IF NOT EXISTS storage_locations (" +
                    "location_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "building TEXT NOT NULL, " +
                    "room TEXT NOT NULL, " +
                    "rack TEXT, " +
                    "shelf TEXT, " +
                    "box TEXT);");

            // Resources table
            stmt.execute("CREATE TABLE IF NOT EXISTS resources (" +
                    "resource_id TEXT PRIMARY KEY, " +
                    "resource_name TEXT NOT NULL, " +
                    "category TEXT NOT NULL, " +
                    "description TEXT, " +
                    "quantity INTEGER NOT NULL, " +
                    "available_quantity INTEGER NOT NULL, " +
                    "unit TEXT DEFAULT 'pcs', " +
                    "condition_rating INTEGER NOT NULL, " + // 1 to 5
                    "current_status TEXT NOT NULL, " + // AVAILABLE, RESERVED, IN_USE, UNDER_INSPECTION, UNDER_REPAIR, MISSING, RETIRED, REPURPOSE_REQUIRED
                    "verification_status TEXT NOT NULL, " + // VERIFIED, VERIFICATION_REQUIRED, EXPIRED
                    "last_verified_date TEXT, " +
                    "next_verification_date TEXT, " +
                    "verified_by TEXT, " +
                    "verification_notes TEXT, " +
                    "storage_building TEXT, " +
                    "storage_room TEXT, " +
                    "rack_number TEXT, " +
                    "shelf_number TEXT, " +
                    "box_number TEXT, " +
                    "current_holder TEXT, " +
                    "purchase_cost REAL NOT NULL, " +
                    "estimated_repair_cost REAL DEFAULT 0, " +
                    "actual_repair_cost REAL DEFAULT 0, " +
                    "reuse_type TEXT DEFAULT 'DIRECT_REUSE', " + // DIRECT_REUSE, MODIFICATION_REQUIRED, REPURPOSE, RECYCLE, RETIRE
                    "specifications TEXT, " +
                    "notes TEXT, " +
                    "created_date TEXT, " +
                    "last_updated_date TEXT);");

            // Events table
            stmt.execute("CREATE TABLE IF NOT EXISTS events (" +
                    "event_id TEXT PRIMARY KEY, " +
                    "event_name TEXT NOT NULL, " +
                    "organizer_name TEXT NOT NULL, " +
                    "organizer_id INTEGER, " +
                    "department_or_club TEXT, " +
                    "event_type TEXT NOT NULL, " +
                    "event_date TEXT NOT NULL, " +
                    "start_time TEXT NOT NULL, " +
                    "end_time TEXT NOT NULL, " +
                    "location TEXT NOT NULL, " +
                    "expected_participants INTEGER, " +
                    "event_status TEXT NOT NULL, " + // DRAFT, REQUIREMENTS_ADDED, RESOURCES_RESERVED, IN_PROGRESS, RETURN_PENDING, INSPECTION_PENDING, COMPLETED, CANCELLED
                    "potential_purchase_avoided REAL DEFAULT 0, " +
                    "created_date TEXT);");

            // Event requirements
            stmt.execute("CREATE TABLE IF NOT EXISTS event_requirements (" +
                    "requirement_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "event_id TEXT NOT NULL, " +
                    "category TEXT NOT NULL, " +
                    "resource_name TEXT NOT NULL, " +
                    "quantity INTEGER NOT NULL, " +
                    "required_condition INTEGER NOT NULL, " +
                    "specifications TEXT, " +
                    "required_date TEXT NOT NULL, " +
                    "start_time TEXT, " +
                    "end_time TEXT, " +
                    "indoor_outdoor TEXT, " +
                    "notes TEXT, " +
                    "status TEXT DEFAULT 'PENDING', " +
                    "FOREIGN KEY(event_id) REFERENCES events(event_id) ON DELETE CASCADE);");

            // Reservations table
            stmt.execute("CREATE TABLE IF NOT EXISTS reservations (" +
                    "reservation_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "event_id TEXT NOT NULL, " +
                    "resource_id TEXT NOT NULL, " +
                    "resource_name TEXT, " +
                    "quantity INTEGER NOT NULL, " +
                    "start_date_time TEXT NOT NULL, " +
                    "end_date_time TEXT NOT NULL, " +
                    "return_deadline TEXT NOT NULL, " +
                    "reservation_status TEXT NOT NULL, " + // REQUESTED, APPROVED, REJECTED, ACTIVE, COMPLETED, CANCELLED, OVERDUE
                    "approved_by TEXT, " +
                    "remarks TEXT, " +
                    "created_date TEXT, " +
                    "FOREIGN KEY(event_id) REFERENCES events(event_id), " +
                    "FOREIGN KEY(resource_id) REFERENCES resources(resource_id));");

            // Resource inspection
            stmt.execute("CREATE TABLE IF NOT EXISTS resource_inspection (" +
                    "inspection_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "reservation_id INTEGER, " +
                    "resource_id TEXT NOT NULL, " +
                    "event_id TEXT, " +
                    "inspection_type TEXT NOT NULL, " + // PRE_USE, POST_USE
                    "inspector_name TEXT NOT NULL, " +
                    "inspection_date TEXT NOT NULL, " +
                    "condition_rating INTEGER NOT NULL, " +
                    "quantity_checked INTEGER NOT NULL, " +
                    "quantity_returned INTEGER, " +
                    "missing_quantity INTEGER DEFAULT 0, " +
                    "damage_reported INTEGER DEFAULT 0, " +
                    "cleaning_required INTEGER DEFAULT 0, " +
                    "repair_required INTEGER DEFAULT 0, " +
                    "outcome_status TEXT NOT NULL, " +
                    "notes TEXT, " +
                    "FOREIGN KEY(resource_id) REFERENCES resources(resource_id));");

            // Resource history / audit trail
            stmt.execute("CREATE TABLE IF NOT EXISTS resource_history (" +
                    "history_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "resource_id TEXT NOT NULL, " +
                    "action_type TEXT NOT NULL, " +
                    "action_date TEXT NOT NULL, " +
                    "performed_by TEXT NOT NULL, " +
                    "details TEXT, " +
                    "FOREIGN KEY(resource_id) REFERENCES resources(resource_id));");

            // Repairs table
            stmt.execute("CREATE TABLE IF NOT EXISTS repairs (" +
                    "repair_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "resource_id TEXT NOT NULL, " +
                    "issue_description TEXT, " +
                    "estimated_cost REAL, " +
                    "actual_cost REAL, " +
                    "repair_status TEXT, " +
                    "vendor_or_technician TEXT, " +
                    "start_date TEXT, " +
                    "completion_date TEXT, " +
                    "FOREIGN KEY(resource_id) REFERENCES resources(resource_id));");
        }
    }

    private static void seedInitialData(Connection conn) throws SQLException {
        // Seed Users
        String userSql = "INSERT OR IGNORE INTO users (user_id, username, password, full_name, email, role, department, created_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(userSql)) {
            ps.setInt(1, 1);
            ps.setString(2, "admin");
            ps.setString(3, "admin123");
            ps.setString(4, "Prof. Rajesh Sharma");
            ps.setString(5, "rajesh.store@college.edu");
            ps.setString(6, "ADMIN");
            ps.setString(7, "Central Store & Purchase Division");
            ps.setString(8, DateUtil.today());
            ps.executeUpdate();

            ps.setInt(1, 2);
            ps.setString(2, "organizer");
            ps.setString(3, "org123");
            ps.setString(4, "Priya Raman");
            ps.setString(5, "priya.ieee@college.edu");
            ps.setString(6, "ORGANIZER");
            ps.setString(7, "IEEE Student Branch");
            ps.setString(8, DateUtil.today());
            ps.executeUpdate();

            ps.setInt(1, 3);
            ps.setString(2, "cultural");
            ps.setString(3, "cult123");
            ps.setString(4, "Karthik Sundar");
            ps.setString(5, "karthik.cult@college.edu");
            ps.setString(6, "ORGANIZER");
            ps.setString(7, "Fine Arts & Cultural Council");
            ps.setString(8, DateUtil.today());
            ps.executeUpdate();
        }

        // Seed Resources
        String resSql = "INSERT OR IGNORE INTO resources (" +
                "resource_id, resource_name, category, description, quantity, available_quantity, unit, " +
                "condition_rating, current_status, verification_status, last_verified_date, next_verification_date, " +
                "verified_by, verification_notes, storage_building, storage_room, rack_number, shelf_number, box_number, " +
                "current_holder, purchase_cost, estimated_repair_cost, actual_repair_cost, reuse_type, specifications, notes, created_date, last_updated_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(resSql)) {
            // Resource 1: Projector (Verified, available)
            insertResource(ps, "EL-AUD-001", "HD High-Lumen Projector (4K Support)", "Audio/Visual", 
                    "Full HD 4200 Lumens HDMI/VGA projector with wireless presenter", 4, 3, "pcs", 
                    4, "AVAILABLE", "VERIFIED", "2026-09-01", "2026-12-01", "Prof. Rajesh Sharma", "Lens cleaned and lamp hours checked",
                    "Media Center", "Room 102", "Rack A", "Shelf 2", "Box P1", "Central Store", 45000.0, 1500.0, 0.0, "DIRECT_REUSE",
                    "4200 Lumens, HDMI x2, VGA, Ceiling/Table mount, Indoor", "High priority resource for technical seminars");

            // Resource 2: Projector (Damaged / Under repair)
            insertResource(ps, "EL-AUD-002", "Epson Portable Projector", "Audio/Visual", 
                    "Portable 3LCD Projector with carrying bag", 2, 0, "pcs", 
                    2, "UNDER_REPAIR", "VERIFICATION_REQUIRED", "2026-07-15", "2026-10-15", "Prof. Rajesh Sharma", "Dim lamp and flickering power socket",
                    "Media Center", "Room 102", "Rack A", "Shelf 3", "Box P2", "Repair Lab", 32000.0, 1800.0, 0.0, "MODIFICATION_REQUIRED",
                    "3300 Lumens, HDMI, XGA, Indoor only", "Awaiting lamp replacement quotation");

            // Resource 3: Extension Cable (Electrical, Verified)
            insertResource(ps, "EL-ELE-001", "Heavy Duty Extension Spike/Cable (15m)", "Electrical", 
                    "4-way surge protected heavy duty 16A extension board with 15m cable", 15, 12, "pcs", 
                    4, "AVAILABLE", "VERIFIED", "2026-09-10", "2026-12-10", "Prof. Rajesh Sharma", "Insulation resistance and earthing tested",
                    "Engineering Block B", "Room 201", "Rack E", "Shelf 1", "Box EC", "Central Store", 750.0, 150.0, 0.0, "DIRECT_REUSE",
                    "15m Length, 16 Amp, 4 Multi-Sockets, Surge protected", "Essential for all auditorium and outdoor stalls");

            // Resource 4: Wireless Microphones (Audio/Visual)
            insertResource(ps, "EL-AUD-003", "Wireless UHF Handheld Microphone Set", "Audio/Visual", 
                    "Dual-channel UHF cordless microphones with receiver", 8, 6, "sets", 
                    5, "AVAILABLE", "VERIFIED", "2026-09-05", "2026-12-05", "Prof. Rajesh Sharma", "RF channels matched and battery contacts inspected",
                    "Auditorium Control Room", "Room A-01", "Rack AUD", "Shelf 1", "Box MIC", "Central Store", 6500.0, 600.0, 0.0, "DIRECT_REUSE",
                    "UHF 550-590MHz, 60m range, 2 Mics + 1 Receiver, Both Indoor/Outdoor", "Tested clear sound output");

            // Resource 5: PA Speakers (Audio/Visual)
            insertResource(ps, "EL-AUD-004", "Portable PA Powered Speaker (300W)", "Audio/Visual", 
                    "300W Active 2-way PA speaker with built-in Bluetooth and mixer", 4, 3, "pcs", 
                    4, "AVAILABLE", "VERIFIED", "2026-08-20", "2026-11-20", "Prof. Rajesh Sharma", "Tested clean distortion-free audio",
                    "Auditorium Control Room", "Room A-01", "Rack AUD", "Shelf 4", "Box SPK", "Central Store", 22000.0, 1200.0, 0.0, "DIRECT_REUSE",
                    "300W RMS, 12 inch woofer, Mic/Line in, Indoor & Outdoor", "Includes tripod speaker stand");

            // Resource 6: Folding Tables (Furniture)
            insertResource(ps, "EL-FUR-001", "Folding Banquet Table (6ft)", "Furniture", 
                    "Heavy-duty HDPE plastic top folding banquet table with steel frame", 30, 22, "pcs", 
                    4, "AVAILABLE", "VERIFIED", "2026-08-10", "2026-11-10", "Prof. Rajesh Sharma", "All leg locking pins checked and verified",
                    "Central Warehouse", "Room W-02", "Bay 1", "Floor Stack", "N/A", "Central Store", 2800.0, 400.0, 0.0, "DIRECT_REUSE",
                    "6ft x 2.5ft, Fold-in-half, 150kg load capacity, Indoor/Outdoor", "Great for registration desks and tech exhibits");

            // Resource 7: Cushioned Chairs (Furniture)
            insertResource(ps, "EL-FUR-002", "Cushioned Event Chair", "Furniture", 
                    "Stackable metal frame padded conference chair", 200, 160, "pcs", 
                    4, "AVAILABLE", "VERIFIED", "2026-08-15", "2026-11-15", "Prof. Rajesh Sharma", "Stacking alignment and leg bushes verified",
                    "Central Warehouse", "Room W-01", "Bay 3", "Stack A-D", "N/A", "Central Store", 1200.0, 150.0, 0.0, "DIRECT_REUSE",
                    "Padded seat, stackable up to 10 high, Indoor use preferred", "Stored in neat stacks of 10");

            // Resource 8: Standee Frames (Equipment)
            insertResource(ps, "EL-EQP-001", "Aluminium Roll-up Standee Frame (6x3 ft)", "Equipment", 
                    "Lightweight aluminium roll-up standee banner base and rod", 12, 10, "pcs", 
                    4, "AVAILABLE", "VERIFIED", "2026-09-02", "2026-12-02", "Prof. Rajesh Sharma", "Roller spring tension and locking bar intact",
                    "Central Store", "Room D-10", "Rack S", "Shelf 2", "Box ST", "Central Store", 1800.0, 250.0, 0.0, "DIRECT_REUSE",
                    "6ft x 3ft frame, Roll-up mechanism, Carry bag included, Indoor", "Reusing frame saves 80% banner cost");

            // Resource 9: Past Event Banner (Decoration - REPURPOSE REQUIRED)
            insertResource(ps, "EL-DEC-002", "Tech Fest 2025 Printed Flex Banner (10x6 ft)", "Decoration", 
                    "Heavy star-flex banner printed with past Tech Fest 2025 branding", 4, 4, "pcs", 
                    3, "REPURPOSE_REQUIRED", "VERIFIED", "2026-08-01", "2026-11-01", "Prof. Rajesh Sharma", "Inspected flex sheet condition",
                    "Central Store", "Room D-11", "Rack B", "Shelf 1", "Roll 4", "Central Store", 1200.0, 0.0, 0.0, "REPURPOSE",
                    "10ft x 6ft flex material, Eyelets intact", "Cannot be reused directly for new named fest; repurpose reverse side for backdrops or student art");

            // Resource 10: Generic College Banner (Decoration)
            insertResource(ps, "EL-DEC-001", "Generic Welcome & College Annual Banner (12x4 ft)", "Decoration", 
                    "Satin cloth banner with institutional crest and universal welcome message", 6, 6, "pcs", 
                    5, "AVAILABLE", "VERIFIED", "2026-09-01", "2026-12-01", "Prof. Rajesh Sharma", "Cleaned and ironed",
                    "Central Store", "Room D-11", "Rack B", "Shelf 2", "Box B1", "Central Store", 1500.0, 200.0, 0.0, "DIRECT_REUSE",
                    "12ft x 4ft, Satin with gold fringe, Indoor/Outdoor", "Direct reuse for inaugural ceremonies");

            // Resource 11: PVC Name Badges (Stationery)
            insertResource(ps, "EL-STA-001", "PVC Name Badge Holder with Lanyard", "Stationery", 
                    "Clear PVC card holder with clip and printed college lanyard", 350, 350, "pcs", 
                    4, "AVAILABLE", "VERIFIED", "2026-09-08", "2026-12-08", "Prof. Rajesh Sharma", "Clips and transparent pouches sanitized",
                    "Central Store", "Room S-05", "Rack ST", "Shelf 3", "Box BG-1", "Central Store", 25.0, 0.0, 0.0, "DIRECT_REUSE",
                    "Standard 90x60mm insert size, swivel clip lanyard", "Print only paper inserts; do not purchase new plastic badge holders");

            // Resource 12: Delegate Folders (Stationery)
            insertResource(ps, "EL-STA-002", "Executive Delegate Folder & Kit", "Stationery", 
                    "Navy blue leatherette document folder with notepad and pen slot", 80, 80, "pcs", 
                    4, "AVAILABLE", "VERIFIED", "2026-09-08", "2026-12-08", "Prof. Rajesh Sharma", "All folders wiped clean and inspected",
                    "Central Store", "Room S-05", "Rack ST", "Shelf 4", "Box FL-2", "Central Store", 120.0, 0.0, 0.0, "DIRECT_REUSE",
                    "A4 size, faux leather, inside pocket", "Reusable for multiple conferences and guest felicitations");

            // Resource 13: Unverified / Expired Resource (Demonstrates Verification Block)
            insertResource(ps, "EL-AUD-005", "Optoma Conference Room Projector", "Audio/Visual", 
                    "Older classroom projector needing preventive lamp safety check", 1, 1, "pcs", 
                    3, "AVAILABLE", "EXPIRED", "2026-05-10", "2026-08-10", "Prof. Rajesh Sharma", "Verification expired. Requires lamp check before issuance.",
                    "Science Block", "Room 304", "Rack P", "Shelf 1", "Box OP", "Science Dept", 35000.0, 1200.0, 0.0, "DIRECT_REUSE",
                    "3000 Lumens, HDMI, VGA, Indoor", "EXPIRED: Blocked from reservation until Admin re-verifies");
        }

        // Seed Events
        String evtSql = "INSERT OR IGNORE INTO events (" +
                "event_id, event_name, organizer_name, organizer_id, department_or_club, event_type, " +
                "event_date, start_time, end_time, location, expected_participants, event_status, potential_purchase_avoided, created_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(evtSql)) {
            // Event 1: In Progress
            ps.setString(1, "EVT-2026-001");
            ps.setString(2, "National Technical Symposium 'Innovate 2026'");
            ps.setString(3, "Priya Raman");
            ps.setInt(4, 2);
            ps.setString(5, "IEEE Student Branch");
            ps.setString(6, "Technical");
            ps.setString(7, DateUtil.today());
            ps.setString(8, "09:00");
            ps.setString(9, "17:00");
            ps.setString(10, "Main Auditorium & Seminar Hall 1");
            ps.setInt(11, 350);
            ps.setString(12, "IN_PROGRESS");
            ps.setDouble(13, 98000.0); // Purchase avoided from Projector, Mics, Tables, Cables
            ps.setString(14, DateUtil.today());
            ps.executeUpdate();

            // Event 2: Sanskriti Cultural Fest
            ps.setString(1, "EVT-2026-002");
            ps.setString(2, "Annual Cultural Fest 'Sanskriti 2026'");
            ps.setString(3, "Karthik Sundar");
            ps.setInt(4, 3);
            ps.setString(5, "Fine Arts & Cultural Council");
            ps.setString(6, "Cultural");
            ps.setString(7, "2026-10-15");
            ps.setString(8, "10:00");
            ps.setString(9, "20:00");
            ps.setString(10, "Open Air Amphitheatre");
            ps.setInt(11, 800);
            ps.setString(12, "REQUIREMENTS_ADDED");
            ps.setDouble(13, 56000.0);
            ps.setString(14, DateUtil.today());
            ps.executeUpdate();

            // Event 3: AI & Cloud Workshop
            ps.setString(1, "EVT-2026-003");
            ps.setString(2, "Hands-on AI & Cloud Computing Workshop");
            ps.setString(3, "Priya Raman");
            ps.setInt(4, 2);
            ps.setString(5, "Computer Science Dept");
            ps.setString(6, "Workshop");
            ps.setString(7, "2026-10-22");
            ps.setString(8, "09:30");
            ps.setString(9, "16:30");
            ps.setString(10, "CS Computing Lab 3");
            ps.setInt(11, 60);
            ps.setString(12, "RESOURCES_RESERVED");
            ps.setDouble(13, 52000.0);
            ps.setString(14, DateUtil.today());
            ps.executeUpdate();
        }

        // Seed Requirements
        String reqSql = "INSERT OR IGNORE INTO event_requirements (" +
                "requirement_id, event_id, category, resource_name, quantity, required_condition, " +
                "specifications, required_date, start_time, end_time, indoor_outdoor, notes, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(reqSql)) {
            ps.setInt(1, 1);
            ps.setString(2, "EVT-2026-001");
            ps.setString(3, "Audio/Visual");
            ps.setString(4, "HD Projector");
            ps.setInt(5, 1);
            ps.setInt(6, 4);
            ps.setString(7, "HDMI support, 1080p, High Lumens");
            ps.setString(8, DateUtil.today());
            ps.setString(9, "09:00");
            ps.setString(10, "17:00");
            ps.setString(11, "Indoor");
            ps.setString(12, "Keynote speaker presentations");
            ps.setString(13, "FULFILLED");
            ps.executeUpdate();

            ps.setInt(2, 2);
            ps.setString(2, "EVT-2026-001");
            ps.setString(3, "Furniture");
            ps.setString(4, "Folding Banquet Table");
            ps.setInt(5, 8);
            ps.setInt(6, 3);
            ps.setString(7, "6ft sturdy folding tables");
            ps.setString(8, DateUtil.today());
            ps.setString(9, "08:30");
            ps.setString(10, "17:30");
            ps.setString(11, "Indoor");
            ps.setString(12, "Registration desk & project displays");
            ps.setString(13, "FULFILLED");
            ps.executeUpdate();

            ps.setInt(3, 3);
            ps.setString(2, "EVT-2026-002");
            ps.setString(3, "Audio/Visual");
            ps.setString(4, "PA Powered Speaker");
            ps.setInt(5, 2);
            ps.setInt(6, 4);
            ps.setString(7, "High output stage sound");
            ps.setString(8, "2026-10-15");
            ps.setString(9, "10:00");
            ps.setString(10, "20:00");
            ps.setString(11, "Outdoor");
            ps.setString(12, "Music and acoustic performances");
            ps.setString(13, "PENDING");
            ps.executeUpdate();
        }

        // Seed Active Reservations
        String resvSql = "INSERT OR IGNORE INTO reservations (" +
                "reservation_id, event_id, resource_id, resource_name, quantity, " +
                "start_date_time, end_date_time, return_deadline, reservation_status, approved_by, remarks, created_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(resvSql)) {
            ps.setInt(1, 1);
            ps.setString(2, "EVT-2026-001");
            ps.setString(3, "EL-AUD-001");
            ps.setString(4, "HD High-Lumen Projector (4K Support)");
            ps.setInt(5, 1);
            ps.setString(6, DateUtil.today() + " 09:00");
            ps.setString(7, DateUtil.today() + " 17:00");
            ps.setString(8, DateUtil.today() + " 18:00");
            ps.setString(9, "ACTIVE");
            ps.setString(10, "Prof. Rajesh Sharma");
            ps.setString(11, "Issued for Symposium Keynote in Main Auditorium");
            ps.setString(12, DateUtil.today());
            ps.executeUpdate();

            ps.setInt(2, 2);
            ps.setString(2, "EVT-2026-001");
            ps.setString(3, "EL-FUR-001");
            ps.setString(4, "Folding Banquet Table (6ft)");
            ps.setInt(5, 8);
            ps.setString(6, DateUtil.today() + " 08:30");
            ps.setString(7, DateUtil.today() + " 17:30");
            ps.setString(8, DateUtil.today() + " 18:30");
            ps.setString(9, "ACTIVE");
            ps.setString(10, "Prof. Rajesh Sharma");
            ps.setString(11, "Issued for registration & project display stalls");
            ps.setString(12, DateUtil.today());
            ps.executeUpdate();
        }

        // Seed Initial History
        String histSql = "INSERT OR IGNORE INTO resource_history (resource_id, action_type, action_date, performed_by, details) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(histSql)) {
            ps.setString(1, "EL-AUD-001");
            ps.setString(2, "CREATED");
            ps.setString(3, "2026-09-01 10:00");
            ps.setString(4, "Prof. Rajesh Sharma");
            ps.setString(5, "Initial onboarding into EventLoop inventory");
            ps.executeUpdate();

            ps.setString(1, "EL-AUD-001");
            ps.setString(2, "VERIFIED");
            ps.setString(3, "2026-09-01 10:30");
            ps.setString(4, "Prof. Rajesh Sharma");
            ps.setString(5, "Periodic verification completed. Lens and filter inspected.");
            ps.executeUpdate();

            ps.setString(1, "EL-AUD-001");
            ps.setString(2, "RESERVED");
            ps.setString(3, DateUtil.today() + " 08:00");
            ps.setString(4, "Prof. Rajesh Sharma");
            ps.setString(5, "Reservation approved for Event EVT-2026-001 (1 unit)");
            ps.executeUpdate();

            ps.setString(1, "EL-AUD-001");
            ps.setString(2, "CHECKED_OUT");
            ps.setString(3, DateUtil.today() + " 08:45");
            ps.setString(4, "Prof. Rajesh Sharma");
            ps.setString(5, "Checked out to Priya Raman after Pre-Use Inspection (Condition: 4/5)");
            ps.executeUpdate();
        }
    }

    private static void insertResource(PreparedStatement ps, String id, String name, String category,
                                       String desc, int qty, int availQty, String unit, int cond,
                                       String status, String verStatus, String lastVer, String nextVer,
                                       String verBy, String verNotes, String bldg, String room,
                                       String rack, String shelf, String box, String holder,
                                       double buyCost, double estRep, double actRep, String reuseType,
                                       String specs, String notes) throws SQLException {
        ps.setString(1, id);
        ps.setString(2, name);
        ps.setString(3, category);
        ps.setString(4, desc);
        ps.setInt(5, qty);
        ps.setInt(6, availQty);
        ps.setString(7, unit);
        ps.setInt(8, cond);
        ps.setString(9, status);
        ps.setString(10, verStatus);
        ps.setString(11, lastVer);
        ps.setString(12, nextVer);
        ps.setString(13, verBy);
        ps.setString(14, verNotes);
        ps.setString(15, bldg);
        ps.setString(16, room);
        ps.setString(17, rack);
        ps.setString(18, shelf);
        ps.setString(19, box);
        ps.setString(20, holder);
        ps.setDouble(21, buyCost);
        ps.setDouble(22, estRep);
        ps.setDouble(23, actRep);
        ps.setString(24, reuseType);
        ps.setString(25, specs);
        ps.setString(26, notes);
        ps.setString(27, DateUtil.today());
        ps.setString(28, DateUtil.today());
        ps.executeUpdate();
    }
}
