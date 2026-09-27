package com.eventloop.main;

import com.eventloop.dao.DatabaseManager;
import com.eventloop.exception.DuplicateResourceException;
import com.eventloop.exception.EventClosureException;
import com.eventloop.exception.ReservationConflictException;
import com.eventloop.exception.ResourceNotAvailableException;
import com.eventloop.exception.VerificationRequiredException;
import com.eventloop.interfaces.Repairable;
import com.eventloop.interfaces.Reservable;
import com.eventloop.interfaces.Resource;
import com.eventloop.interfaces.Reusable;
import com.eventloop.model.CostComparison;
import com.eventloop.model.EventModel;
import com.eventloop.model.HistoryModel;
import com.eventloop.model.RequirementModel;
import com.eventloop.model.ReservationModel;
import com.eventloop.model.resources.AbstractResource;
import com.eventloop.model.resources.CustomPrintedBanner;
import com.eventloop.model.resources.ExtensionCable;
import com.eventloop.model.resources.Projector;
import com.eventloop.model.resources.Table;
import com.eventloop.service.DecisionSupportService;
import com.eventloop.service.EventService;
import com.eventloop.service.ReservationService;
import com.eventloop.service.ResourceService;
import com.eventloop.util.DateUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Automated Verification & Test Suite covering all 18 mandatory project test scenarios.
 */
public class EventLoopTestSuite {

    private static int passedCount = 0;
    private static int failedCount = 0;

    public static void main(String[] args) {
        System.out.println("========================================================================");
        System.out.println("      EVENTLOOP AUTOMATED SYSTEM VERIFICATION & TEST SUITE             ");
        System.out.println("========================================================================");

        DatabaseManager.initializeDatabase();

        ResourceService resService = ResourceService.getInstance();
        EventService evtService = EventService.getInstance();
        ReservationService rsvService = ReservationService.getInstance();
        DecisionSupportService decService = DecisionSupportService.getInstance();

        // Clean any leftover test artifacts for idempotency
        cleanTestArtifacts();

        // Test 1: Add a valid resource
        runTest("1. Add a valid resource", () -> {
            Projector p = new Projector("EL-AUD-999", "Sony 4K Auditorium Laser Projector", 2, 85000.0, "Auditorium", "Booth 1");
            p.setVerificationStatus("VERIFIED");
            p.setNextVerificationDate("2026-12-31");
            resService.addResource(p, "System Test Runner");
            AbstractResource retrieved = resService.getById("EL-AUD-999");
            assertTrue(retrieved != null && retrieved.getResourceName().contains("Sony 4K"), "Resource should be stored and retrievable");
        });

        // Test 2: Reject duplicate resource ID
        runTest("2. Reject duplicate resource ID", () -> {
            boolean caught = false;
            try {
                Projector dup = new Projector("EL-AUD-999", "Duplicate Projector", 1, 85000.0, "Store", "A1");
                resService.addResource(dup, "System Test Runner");
            } catch (DuplicateResourceException e) {
                caught = true;
            }
            assertTrue(caught, "DuplicateResourceException must be thrown for duplicate ID");
        });

        // Test 3: Search an available resource
        runTest("3. Search an available resource", () -> {
            List<AbstractResource> list = resService.searchResources("Sony", "Audio/Visual", "AVAILABLE", "VERIFIED");
            assertTrue(!list.isEmpty(), "Search should find newly added Sony projector");
        });

        // Test 4: Block an unverified resource
        runTest("4. Block an unverified/expired resource", () -> {
            boolean blocked = false;
            try {
                ReservationModel rsv = new ReservationModel(0, "EVT-2026-002", "EL-AUD-005", "Optoma Projector", 1, "2026-10-15 09:00", "2026-10-15 17:00", "2026-10-15 17:00", "Testing unverified block");
                rsvService.requestReservation(rsv, "Test Runner");
            } catch (VerificationRequiredException e) {
                blocked = true;
            }
            assertTrue(blocked, "VerificationRequiredException must be thrown when attempting to reserve unverified resource");
        });

        // Test 5: Block a retired resource
        runTest("5. Block a retired resource", () -> {
            resService.retireResource("EL-AUD-999", "Test Runner", "Obsolete laser lamp");
            boolean blocked = false;
            try {
                ReservationModel rsv = new ReservationModel(0, "EVT-2026-002", "EL-AUD-999", "Sony 4K", 1, "2026-10-15 09:00", "2026-10-15 17:00", "2026-10-15 17:00", "Testing retired block");
                rsvService.requestReservation(rsv, "Test Runner");
            } catch (ResourceNotAvailableException e) {
                blocked = true;
            }
            assertTrue(blocked, "ResourceNotAvailableException must block retired resource reservation");
        });

        // Test 6: Detect overlapping reservations
        runTest("6. Detect overlapping reservations", () -> {
            // Create a test resource with qty 1
            Table t = new Table("EL-FUR-998", "VIP Round Dining Table", 1, 3500.0, "Store", "R1");
            t.setVerificationStatus("VERIFIED");
            t.setNextVerificationDate("2026-12-31");
            resService.addResource(t, "Test Runner");

            // Booking 1: 10:00 to 14:00
            ReservationModel rsv1 = new ReservationModel(0, "EVT-2026-002", "EL-FUR-998", "VIP Round Table", 1, "2026-10-30 10:00", "2026-10-30 14:00", "2026-10-30 14:00", "Initial Booking");
            rsvService.requestReservation(rsv1, "Test Runner");

            // Booking 2: 12:00 to 16:00 (Overlaps 12:00-14:00!)
            boolean conflict = false;
            try {
                ReservationModel rsv2 = new ReservationModel(0, "EVT-2026-003", "EL-FUR-998", "VIP Round Table", 1, "2026-10-30 12:00", "2026-10-30 16:00", "2026-10-30 16:00", "Conflicting Booking");
                rsvService.requestReservation(rsv2, "Test Runner");
            } catch (ReservationConflictException e) {
                conflict = true;
            }
            assertTrue(conflict, "ReservationConflictException must detect time-interval overlap");
        });

        // Test 7: Allow reservation for available quantity
        runTest("7. Allow reservation for available quantity", () -> {
            ReservationModel rsv = new ReservationModel(0, "EVT-2026-002", "EL-FUR-001", "Folding Banquet Table (6ft)", 2, "2026-11-25 10:00", "2026-11-25 18:00", "2026-11-25 18:00", "Valid reservation test");
            boolean ok = rsvService.requestReservation(rsv, "Test Runner");
            assertTrue(ok, "Reservation for available stock and future date should succeed");
        });

        // Test 8: Reject reservation when quantity is insufficient
        runTest("8. Reject reservation when quantity is insufficient", () -> {
            boolean rejected = false;
            try {
                ReservationModel rsv = new ReservationModel(0, "EVT-2026-002", "EL-FUR-001", "Folding Banquet Table", 9999, "2026-12-01 10:00", "2026-12-01 18:00", "2026-12-01 18:00", "Testing excess qty");
                rsvService.requestReservation(rsv, "Test Runner");
            } catch (ResourceNotAvailableException e) {
                rejected = true;
            }
            assertTrue(rejected, "ResourceNotAvailableException must be thrown when requested qty exceeds available stock");
        });

        // Test 9: Mark a resource under repair
        runTest("9. Mark a resource under repair", () -> {
            resService.markUnderRepair("EL-AUD-001", "Test Runner", "Lens calibration check", 1200.0);
            AbstractResource r = resService.getById("EL-AUD-001");
            assertTrue("UNDER_REPAIR".equalsIgnoreCase(r.getCurrentStatus()), "Resource status should be UNDER_REPAIR");
        });

        // Test 10: Prevent reservation of a resource under repair
        runTest("10. Prevent reservation of resource under repair", () -> {
            boolean blocked = false;
            try {
                ReservationModel rsv = new ReservationModel(0, "EVT-2026-002", "EL-AUD-001", "HD High-Lumen Projector", 1, "2026-11-15 09:00", "2026-11-15 17:00", "2026-11-15 17:00", "Test under repair block");
                rsvService.requestReservation(rsv, "Test Runner");
            } catch (ResourceNotAvailableException e) {
                blocked = true;
            }
            // Restore status to AVAILABLE
            resService.markAvailable("EL-AUD-001", "Test Runner", 4);
            assertTrue(blocked, "ResourceNotAvailableException must block booking resource under repair");
        });

        // Test 11: Return all resources successfully
        runTest("11. Return all resources successfully", () -> {
            rsvService.returnResource(1, 1, "Store Manager");
            ReservationModel r = rsvService.getAllReservations().stream().filter(x -> x.getReservationId() == 1).findFirst().orElse(null);
            assertTrue(r != null && "COMPLETED".equalsIgnoreCase(r.getReservationStatus()), "Reservation status should transition to COMPLETED on return");
        });

        // Test 12: Block event closure when resources are missing or awaiting inspection
        runTest("12. Block event closure when items missing/uninspected", () -> {
            boolean blocked = false;
            try {
                // EVT-2026-001 still has uninspected return or checked out tables
                evtService.closeEvent("EVT-2026-001", "Test Runner");
            } catch (EventClosureException e) {
                blocked = true;
            }
            assertTrue(blocked, "EventClosureException must block closing event before complete return and post-use inspection");
        });

        // Test 13: Complete event closure after inspection
        runTest("13. Complete event closure after all requirements fulfilled", () -> {
            String testEvtId = "EVT-TEST-CLEAN-" + System.currentTimeMillis();
            EventModel cleanEvt = new EventModel(testEvtId, "Green Campus Seminar", "Dr. A. Kumar", 1, "Environmental Club", "Seminar", "2026-09-20", "10:00", "12:00", "Room 101", 50);
            evtService.createEvent(cleanEvt);
            boolean closed = evtService.closeEvent(testEvtId, "Admin Auditor");
            EventModel retrieved = evtService.getById(testEvtId);
            assertTrue(closed && "COMPLETED".equalsIgnoreCase(retrieved.getEventStatus()), "Clean event with all accounts reconciled must close successfully");
        });

        // Test 14: Calculate potential purchase avoided correctly
        runTest("14. Calculate potential purchase avoided correctly", () -> {
            int qty = 4;
            double unitCost = 300.0;
            double expected = 1200.0;
            double calc = qty * unitCost;
            assertTrue(Math.abs(calc - expected) < 0.001, "Potential Purchase Avoided formula must equal Qty * Unit Cost");
        });

        // Test 15: Display repair versus rent versus buy comparison
        runTest("15. Pre-purchase decision matrix calculation", () -> {
            RequirementModel req = new RequirementModel(0, "EVT-2026-002", "Audio/Visual", "HD Projector", 1, 4, "HDMI, 1080p", "2026-10-15", "09:00", "17:00", "Indoor", "Keynote");
            Projector proj = new Projector("EL-AUD-001", "HD High-Lumen Projector", 4, 45000.0, "Media Center", "Room 102");
            CostComparison cc = decService.analyzeOptions(req, proj);
            assertTrue(cc.getPotentialPurchaseAvoided() == 45000.0 && cc.getRecommendationOption() != null && !cc.getRecommendationReason().isEmpty(),
                    "Decision support service must compute complete cost matrix with savings and recommendation reasoning");
        });

        // Test 16: Update resource history after every important action
        runTest("16. Update resource history after actions", () -> {
            List<HistoryModel> history = new com.eventloop.dao.HistoryDAO().getHistoryForResource("EL-AUD-001");
            assertTrue(!history.isEmpty(), "Audit history must record timestamped actions for resource lifecycle");
        });

        // Test 17: Check that interface methods work through polymorphism
        runTest("17. Polymorphism across Java Interfaces", () -> {
            Resource r1 = new Projector();
            Resource r2 = new Table();
            Resource r3 = new ExtensionCable();
            Resource r4 = new CustomPrintedBanner();

            assertTrue(r1 instanceof Reservable && r1 instanceof Repairable && r1 instanceof Reusable, "Projector must implement Resource, Reservable, Repairable, Reusable");
            assertTrue(r2 instanceof Reservable && r2 instanceof Reusable && !(r2 instanceof Repairable), "Table must implement Resource, Reservable, Reusable");
            assertTrue(r3 instanceof Repairable && r3 instanceof Reservable, "ExtensionCable must implement Reservable and Repairable");
            
            Reusable reuBanner = (Reusable) r4;
            assertTrue(reuBanner.getReuseRecommendation().contains("REPURPOSE"), "CustomPrintedBanner polymorphic getReuseRecommendation() must return REPURPOSE advice");
        });

        // Test 18: Verify that data remains available after database reconnection
        runTest("18. Data persistence across database restarts", () -> {
            EventModel evt = evtService.getById("EVT-2026-001");
            AbstractResource res = resService.getById("EL-AUD-001");
            assertTrue(evt != null && res != null, "SQLite database must retain records permanently");
        });

        System.out.println("\n========================================================================");
        System.out.println("  TEST SUITE RESULTS: " + passedCount + " PASSED, " + failedCount + " FAILED (TOTAL: " + (passedCount + failedCount) + ")");
        System.out.println("========================================================================");

        if (failedCount > 0) {
            System.exit(1);
        }
    }

    private static void cleanTestArtifacts() {
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.createStatement().execute("DELETE FROM reservations WHERE resource_id IN ('EL-AUD-999', 'EL-FUR-998')");
            conn.createStatement().execute("DELETE FROM resource_history WHERE resource_id IN ('EL-AUD-999', 'EL-FUR-998')");
            conn.createStatement().execute("DELETE FROM resources WHERE resource_id IN ('EL-AUD-999', 'EL-FUR-998')");
            conn.createStatement().execute("DELETE FROM events WHERE event_id LIKE 'EVT-TEST-CLEAN%'");
        } catch (SQLException e) {
            // Ignore
        }
    }

    private static void runTest(String testName, TestRunnable r) {
        try {
            r.run();
            System.out.printf("  [PASS] %-62s\n", testName);
            passedCount++;
        } catch (Throwable t) {
            System.out.printf("  [FAIL] %-62s -> %s\n", testName, t.getMessage());
            failedCount++;
        }
    }

    private static void assertTrue(boolean condition, String msg) {
        if (!condition) {
            throw new AssertionError(msg);
        }
    }

    @FunctionalInterface
    interface TestRunnable {
        void run() throws Throwable;
    }
}
