package com.eventloop.web;

import com.eventloop.dao.DatabaseManager;
import com.eventloop.dao.EventDAO;
import com.eventloop.dao.HistoryDAO;
import com.eventloop.dao.ReservationDAO;
import com.eventloop.dao.ResourceDAO;
import com.eventloop.exception.EventClosureException;
import com.eventloop.exception.ReservationConflictException;
import com.eventloop.exception.ResourceNotAvailableException;
import com.eventloop.exception.ValidationException;
import com.eventloop.exception.VerificationRequiredException;
import com.eventloop.interfaces.Repairable;
import com.eventloop.interfaces.Reservable;
import com.eventloop.interfaces.Resource;
import com.eventloop.interfaces.Reusable;
import com.eventloop.model.CompatibilityMatch;
import com.eventloop.model.CostComparison;
import com.eventloop.model.EventModel;
import com.eventloop.model.HistoryModel;
import com.eventloop.model.InspectionModel;
import com.eventloop.model.RequirementModel;
import com.eventloop.model.ReservationModel;
import com.eventloop.model.User;
import com.eventloop.model.resources.AbstractResource;
import com.eventloop.model.resources.CustomPrintedBanner;
import com.eventloop.model.resources.ExtensionCable;
import com.eventloop.model.resources.Projector;
import com.eventloop.model.resources.ResourceFactory;
import com.eventloop.model.resources.Table;
import com.eventloop.service.AuthService;
import com.eventloop.service.DecisionSupportService;
import com.eventloop.service.EventService;
import com.eventloop.service.InspectionService;
import com.eventloop.service.MatchingService;
import com.eventloop.service.ReportService;
import com.eventloop.service.ReservationService;
import com.eventloop.service.ResourceService;
import com.eventloop.util.DateUtil;
import com.eventloop.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Embedded Cloud-Ready Web Server for EventLoop.
 * Runs on port defined by environment variable PORT (standard on Render/AWS/Cloud) or 8080.
 * Allows anyone to access EventLoop from mobile phones, tablets, or laptops 24/7 without developer involvement.
 */
public class EventLoopWebServer {
    private static final ResourceService resourceService = ResourceService.getInstance();
    private static final EventService eventService = EventService.getInstance();
    private static final ReservationService reservationService = ReservationService.getInstance();
    private static final MatchingService matchingService = MatchingService.getInstance();
    private static final DecisionSupportService decisionService = DecisionSupportService.getInstance();
    private static final InspectionService inspectionService = InspectionService.getInstance();
    private static final ReportService reportService = ReportService.getInstance();
    private static final AuthService authService = AuthService.getInstance();
    private static final HistoryDAO historyDAO = new HistoryDAO();

    public static void main(String[] args) throws IOException {
        // Initialize SQLite schema and sample data
        System.out.println("[EventLoop Web] Initializing Database...");
        DatabaseManager.initializeDatabase();

        int port = 8080;
        String envPort = System.getenv("PORT");
        if (envPort != null && !envPort.trim().isEmpty()) {
            try {
                port = Integer.parseInt(envPort.trim());
            } catch (NumberFormatException ignored) {}
        }

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // Static files (Web UI)
        server.createContext("/", new StaticFileHandler());

        // REST API Endpoints
        server.createContext("/api/dashboard", new DashboardApiHandler());
        server.createContext("/api/resources", new ResourcesApiHandler());
        server.createContext("/api/events", new EventsApiHandler());
        server.createContext("/api/events/requirements", new RequirementsApiHandler());
        server.createContext("/api/events/close", new EventCloseApiHandler());
        server.createContext("/api/matching", new MatchingApiHandler());
        server.createContext("/api/decision", new DecisionApiHandler());
        server.createContext("/api/reservations", new ReservationsApiHandler());
        server.createContext("/api/reservations/checkout", new CheckoutApiHandler());
        server.createContext("/api/reservations/return", new ReturnApiHandler());
        server.createContext("/api/reports", new ReportsApiHandler());
        server.createContext("/api/auth/login", new LoginApiHandler());
        server.createContext("/api/auth/register", new RegisterApiHandler());
        server.createContext("/api/oop-demo", new OopDemoApiHandler());

        server.setExecutor(null); // Default executor
        server.start();

        System.out.println("=================================================================");
        System.out.println("  EVENTLOOP CLOUD WEB SERVER IS LIVE & READY 24/7!               ");
        System.out.println("  Local URL:        http://localhost:" + port);
        System.out.println("  Mobile / LAN:     http://[YOUR-IP]:" + port);
        System.out.println("=================================================================");
    }

    // -------------------------------------------------------------
    // STATIC FILE HANDLER
    // -------------------------------------------------------------
    private static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            String path = ex.getRequestURI().getPath();
            if (path.equals("/") || path.isEmpty()) {
                path = "/index.html";
            }

            File file = new File("web" + path);
            if (!file.exists() || file.isDirectory()) {
                file = new File("web/index.html");
            }

            String contentType = "text/html";
            if (path.endsWith(".css")) contentType = "text/css";
            else if (path.endsWith(".js")) contentType = "application/javascript";
            else if (path.endsWith(".png")) contentType = "image/png";
            else if (path.endsWith(".ico")) contentType = "image/x-icon";

            byte[] bytes = readAllBytes(file);
            ex.getResponseHeaders().set("Content-Type", contentType + "; charset=UTF-8");
            setCors(ex);
            ex.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = ex.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    // -------------------------------------------------------------
    // REST API HANDLERS
    // -------------------------------------------------------------

    // 1. Dashboard API
    private static class DashboardApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            setCors(ex);
            if (handleOptions(ex)) return;

            try {
                List<AbstractResource> resources = resourceService.getAllResources();
                List<EventModel> events = eventService.getAllEvents();
                List<ReservationModel> reservations = reservationService.getAllReservations();
                List<HistoryModel> histories = historyDAO.getAllHistory();

                int totalQty = 0;
                int availableQty = 0;
                int inUseQty = 0;
                int underRepairQty = 0;
                int verifReqQty = 0;
                double totalAssetVal = 0;
                double totalRepairCost = 0;

                for (AbstractResource r : resources) {
                    totalQty += r.getQuantity();
                    totalAssetVal += r.getPurchaseCost() * r.getQuantity();
                    totalRepairCost += r.getEstimatedRepairCost();

                    if ("UNDER_REPAIR".equalsIgnoreCase(r.getCurrentStatus())) {
                        underRepairQty += r.getQuantity();
                    } else if (r.getAvailableQuantity() > 0 && r.isReservableState()) {
                        availableQty += r.getAvailableQuantity();
                    }

                    if ("EXPIRED".equalsIgnoreCase(r.getVerificationStatus()) || 
                        "VERIFICATION_REQUIRED".equalsIgnoreCase(r.getVerificationStatus()) || 
                        DateUtil.isExpired(r.getNextVerificationDate())) {
                        verifReqQty += r.getQuantity();
                    }
                }

                int activeEvents = 0;
                double totalAvoided = 0;
                for (EventModel e : events) {
                    totalAvoided += e.getPotentialPurchaseAvoided();
                    if (!"COMPLETED".equalsIgnoreCase(e.getEventStatus()) && !"CANCELLED".equalsIgnoreCase(e.getEventStatus())) {
                        activeEvents++;
                    }
                }

                int pendingReturns = 0;
                int overdueCount = 0;
                int reusedCount = 0;

                for (ReservationModel rm : reservations) {
                    if ("ACTIVE".equalsIgnoreCase(rm.getReservationStatus())) {
                        inUseQty += rm.getQuantity();
                        pendingReturns++;
                        if (DateUtil.isOverdue(rm.getReturnDeadline())) overdueCount++;
                    } else if ("OVERDUE".equalsIgnoreCase(rm.getReservationStatus())) {
                        inUseQty += rm.getQuantity();
                        pendingReturns++;
                        overdueCount++;
                    }
                    if ("COMPLETED".equalsIgnoreCase(rm.getReservationStatus()) || "ACTIVE".equalsIgnoreCase(rm.getReservationStatus())) {
                        reusedCount += rm.getQuantity();
                    }
                }

                Map<String, Object> data = new HashMap<>();
                data.put("totalResources", totalQty);
                data.put("availableStock", availableQty);
                data.put("inUseStock", inUseQty);
                data.put("underRepairStock", underRepairQty);
                data.put("verificationDueStock", verifReqQty);
                data.put("activeEvents", activeEvents);
                data.put("pendingReturns", pendingReturns);
                data.put("overdueReturns", overdueCount);
                data.put("purchaseAvoided", totalAvoided);
                data.put("resourcesReused", reusedCount);
                data.put("totalAssetValue", totalAssetVal);
                data.put("totalRepairNeeds", totalRepairCost);
                data.put("events", events);
                data.put("recentActivity", histories.subList(0, Math.min(15, histories.size())));

                sendJsonResponse(ex, 200, JsonUtil.toJson(data));

            } catch (SQLException e) {
                sendError(ex, 500, e.getMessage());
            }
        }
    }

    // 2. Resources API
    private static class ResourcesApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            setCors(ex);
            if (handleOptions(ex)) return;

            String method = ex.getRequestMethod();
            if ("GET".equalsIgnoreCase(method)) {
                String query = ex.getRequestURI().getQuery();
                Map<String, String> qp = parseQueryParams(query);
                String kw = qp.getOrDefault("keyword", "");
                String cat = qp.getOrDefault("category", "All Categories");
                String stat = qp.getOrDefault("status", "All Statuses");
                String ver = qp.getOrDefault("verification", "All");

                try {
                    List<AbstractResource> list = resourceService.searchResources(kw, cat, stat, ver);
                    sendJsonResponse(ex, 200, JsonUtil.toJson(list));
                } catch (SQLException e) {
                    sendError(ex, 500, e.getMessage());
                }
            } else if ("POST".equalsIgnoreCase(method)) {
                String body = readBody(ex);
                Map<String, String> data = JsonUtil.parseSimpleJson(body);

                // Action routing (verify, repair, or add)
                String action = data.getOrDefault("action", "add");
                try {
                    if ("verify".equalsIgnoreCase(action)) {
                        String id = data.get("resourceId");
                        String notes = data.getOrDefault("notes", "Verified via Web Portal");
                        String nextDate = data.get("nextVerificationDate");
                        String user = data.getOrDefault("performedBy", "Store Manager");
                        resourceService.verifyResource(id, user, notes, nextDate);
                        sendJsonResponse(ex, 200, "{\"success\":true,\"message\":\"Resource verified successfully\"}");
                    } else if ("repair".equalsIgnoreCase(action)) {
                        String id = data.get("resourceId");
                        String reason = data.getOrDefault("reason", "Reported defect via Web");
                        double cost = parseDouble(data.get("estimatedRepairCost"), 1000.0);
                        String user = data.getOrDefault("performedBy", "Store Manager");
                        resourceService.markUnderRepair(id, user, reason, cost);
                        sendJsonResponse(ex, 200, "{\"success\":true,\"message\":\"Resource marked under repair\"}");
                    } else {
                        // Add resource
                        String id = data.get("resourceId");
                        String name = data.get("resourceName");
                        String cat = data.get("category");
                        int qty = parseInt(data.get("quantity"), 1);
                        double cost = parseDouble(data.get("purchaseCost"), 1000.0);
                        String user = data.getOrDefault("performedBy", "Store Admin");

                        AbstractResource r = ResourceFactory.createResource(cat, name);
                        r.setResourceId(id);
                        r.setResourceName(name);
                        r.setCategory(cat);
                        r.setDescription(data.get("description"));
                        r.setQuantity(qty);
                        r.setAvailableQuantity(qty);
                        r.setConditionRating(parseInt(data.get("conditionRating"), 5));
                        r.setCurrentStatus(data.getOrDefault("currentStatus", "AVAILABLE"));
                        r.setVerificationStatus(data.getOrDefault("verificationStatus", "VERIFIED"));
                        r.setStorageBuilding(data.getOrDefault("storageBuilding", "Central Store"));
                        r.setStorageRoom(data.getOrDefault("storageRoom", "Room 101"));
                        r.setPurchaseCost(cost);
                        r.setEstimatedRepairCost(parseDouble(data.get("estimatedRepairCost"), 0.0));
                        r.setReuseType(data.getOrDefault("reuseType", "DIRECT_REUSE"));
                        r.setSpecifications(data.get("specifications"));

                        resourceService.addResource(r, user);
                        sendJsonResponse(ex, 200, "{\"success\":true,\"message\":\"Resource registered successfully\"}");
                    }
                } catch (Exception e) {
                    sendError(ex, 400, e.getMessage());
                }
            }
        }
    }

    // 3. Events API
    private static class EventsApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            setCors(ex);
            if (handleOptions(ex)) return;

            String method = ex.getRequestMethod();
            if ("GET".equalsIgnoreCase(method)) {
                try {
                    List<EventModel> events = eventService.getAllEvents();
                    sendJsonResponse(ex, 200, JsonUtil.toJson(events));
                } catch (SQLException e) {
                    sendError(ex, 500, e.getMessage());
                }
            } else if ("POST".equalsIgnoreCase(method)) {
                String body = readBody(ex);
                Map<String, String> data = JsonUtil.parseSimpleJson(body);

                try {
                    EventModel evt = new EventModel(
                            data.get("eventId"),
                            data.get("eventName"),
                            data.get("organizerName"),
                            1,
                            data.get("departmentOrClub"),
                            data.get("eventType"),
                            data.get("eventDate"),
                            data.get("startTime"),
                            data.get("endTime"),
                            data.get("location"),
                            parseInt(data.get("expectedParticipants"), 100)
                    );
                    eventService.createEvent(evt);
                    sendJsonResponse(ex, 200, "{\"success\":true,\"message\":\"Event created successfully\"}");
                } catch (Exception e) {
                    sendError(ex, 400, e.getMessage());
                }
            }
        }
    }

    // 4. Requirements API
    private static class RequirementsApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            setCors(ex);
            if (handleOptions(ex)) return;

            String method = ex.getRequestMethod();
            if ("GET".equalsIgnoreCase(method)) {
                String q = ex.getRequestURI().getQuery();
                String eventId = parseQueryParams(q).getOrDefault("eventId", "");
                try {
                    List<RequirementModel> reqs = eventService.getRequirementsForEvent(eventId);
                    sendJsonResponse(ex, 200, JsonUtil.toJson(reqs));
                } catch (SQLException e) {
                    sendError(ex, 500, e.getMessage());
                }
            } else if ("POST".equalsIgnoreCase(method)) {
                String body = readBody(ex);
                Map<String, String> d = JsonUtil.parseSimpleJson(body);
                try {
                    RequirementModel req = new RequirementModel(
                            0,
                            d.get("eventId"),
                            d.get("category"),
                            d.get("resourceName"),
                            parseInt(d.get("quantity"), 1),
                            parseInt(d.get("requiredCondition"), 3),
                            d.get("specifications"),
                            d.get("requiredDate"),
                            d.get("startTime"),
                            d.get("endTime"),
                            d.getOrDefault("indoorOutdoor", "Indoor"),
                            d.get("notes")
                    );
                    eventService.addRequirement(req);
                    sendJsonResponse(ex, 200, "{\"success\":true,\"message\":\"Requirement attached to event\"}");
                } catch (Exception e) {
                    sendError(ex, 400, e.getMessage());
                }
            }
        }
    }

    // 5. Mandatory Event Closure Guard API
    private static class EventCloseApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            setCors(ex);
            if (handleOptions(ex)) return;

            String body = readBody(ex);
            Map<String, String> d = JsonUtil.parseSimpleJson(body);
            String eventId = d.get("eventId");
            String user = d.getOrDefault("performedBy", "Admin Auditor");

            try {
                eventService.closeEvent(eventId, user);
                sendJsonResponse(ex, 200, "{\"success\":true,\"message\":\"Event closed successfully. All accounts reconciled!\"}");
            } catch (EventClosureException e) {
                sendJsonResponse(ex, 400, "{\"success\":false,\"blocked\":true,\"message\":\"" + escapeJson(e.getMessage()) + "\"}");
            } catch (SQLException e) {
                sendError(ex, 500, e.getMessage());
            }
        }
    }

    // 6. 10-Point Smart Matching API
    private static class MatchingApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            setCors(ex);
            if (handleOptions(ex)) return;

            String body = readBody(ex);
            Map<String, String> d = JsonUtil.parseSimpleJson(body);

            try {
                RequirementModel req = new RequirementModel();
                req.setCategory(d.get("category"));
                req.setResourceName(d.get("resourceName"));
                req.setQuantity(parseInt(d.get("quantity"), 1));
                req.setRequiredCondition(parseInt(d.get("requiredCondition"), 3));
                req.setRequiredDate(d.get("requiredDate"));
                req.setStartTime(d.get("startTime"));
                req.setEndTime(d.get("endTime"));
                req.setSpecifications(d.get("specifications"));

                List<CompatibilityMatch> matches = matchingService.findMatches(req);
                sendJsonResponse(ex, 200, JsonUtil.toJson(matches));
            } catch (Exception e) {
                sendError(ex, 400, e.getMessage());
            }
        }
    }

    // 7. Pre-Purchase Decision Support Matrix API
    private static class DecisionApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            setCors(ex);
            if (handleOptions(ex)) return;

            String body = readBody(ex);
            Map<String, String> d = JsonUtil.parseSimpleJson(body);

            try {
                RequirementModel req = new RequirementModel();
                req.setResourceName(d.get("resourceName"));
                req.setCategory(d.get("category"));
                req.setQuantity(parseInt(d.get("quantity"), 1));

                String resId = d.get("resourceId");
                AbstractResource res = (resId != null && !resId.isEmpty()) ? resourceService.getById(resId) : null;

                CostComparison cc = decisionService.analyzeOptions(req, res);
                sendJsonResponse(ex, 200, JsonUtil.toJson(cc));
            } catch (Exception e) {
                sendError(ex, 400, e.getMessage());
            }
        }
    }

    // 8. Reservations API (with Conflict Detection)
    private static class ReservationsApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            setCors(ex);
            if (handleOptions(ex)) return;

            String method = ex.getRequestMethod();
            if ("GET".equalsIgnoreCase(method)) {
                try {
                    List<ReservationModel> list = reservationService.getAllReservations();
                    sendJsonResponse(ex, 200, JsonUtil.toJson(list));
                } catch (SQLException e) {
                    sendError(ex, 500, e.getMessage());
                }
            } else if ("POST".equalsIgnoreCase(method)) {
                String body = readBody(ex);
                Map<String, String> d = JsonUtil.parseSimpleJson(body);
                try {
                    ReservationModel rsv = new ReservationModel(
                            0,
                            d.get("eventId"),
                            d.get("resourceId"),
                            d.get("resourceName"),
                            parseInt(d.get("quantity"), 1),
                            d.get("startDateTime"),
                            d.get("endDateTime"),
                            d.get("endDateTime"),
                            d.getOrDefault("remarks", "Web Booking")
                    );
                    String user = d.getOrDefault("performedBy", "Club Organizer");
                    reservationService.requestReservation(rsv, user);
                    sendJsonResponse(ex, 200, "{\"success\":true,\"message\":\"Reservation created successfully!\"}");
                } catch (Exception e) {
                    sendJsonResponse(ex, 400, "{\"success\":false,\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
                }
            }
        }
    }

    // 9. Checkout (Pre-Use Inspection) API
    private static class CheckoutApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            setCors(ex);
            if (handleOptions(ex)) return;

            String body = readBody(ex);
            Map<String, String> d = JsonUtil.parseSimpleJson(body);
            int resId = parseInt(d.get("reservationId"), 0);
            String user = d.getOrDefault("handledBy", "Store Manager");

            try {
                // Record pre-use inspection
                ReservationModel rsv = new ReservationDAO().getById(resId);
                if (rsv != null) {
                    InspectionModel ins = new InspectionModel();
                    ins.setReservationId(resId);
                    ins.setResourceId(rsv.getResourceId());
                    ins.setEventId(rsv.getEventId());
                    ins.setInspectionType("PRE_USE");
                    ins.setInspectorName(user);
                    ins.setConditionRating(parseInt(d.get("conditionRating"), 5));
                    ins.setQuantityChecked(rsv.getQuantity());
                    ins.setNotes(d.getOrDefault("notes", "Pre-use inspection certified via web"));
                    ins.setOutcomeStatus("AVAILABLE");
                    inspectionService.recordPreUseInspection(ins);
                }

                reservationService.checkoutResource(resId, user);
                sendJsonResponse(ex, 200, "{\"success\":true,\"message\":\"Resource dispatched and inventory deducted!\"}");
            } catch (Exception e) {
                sendError(ex, 400, e.getMessage());
            }
        }
    }

    // 10. Return (Post-Use Inspection) API
    private static class ReturnApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            setCors(ex);
            if (handleOptions(ex)) return;

            String body = readBody(ex);
            Map<String, String> d = JsonUtil.parseSimpleJson(body);
            int resId = parseInt(d.get("reservationId"), 0);
            int returnedQty = parseInt(d.get("quantityReturned"), 1);
            int cond = parseInt(d.get("conditionRating"), 5);
            String outcome = d.getOrDefault("outcomeStatus", "AVAILABLE");
            String user = d.getOrDefault("handledBy", "Store Auditor");

            try {
                ReservationModel rsv = new ReservationDAO().getById(resId);
                if (rsv != null) {
                    InspectionModel ins = new InspectionModel();
                    ins.setReservationId(resId);
                    ins.setResourceId(rsv.getResourceId());
                    ins.setEventId(rsv.getEventId());
                    ins.setInspectionType("POST_USE");
                    ins.setInspectorName(user);
                    ins.setConditionRating(cond);
                    ins.setQuantityChecked(rsv.getQuantity());
                    ins.setQuantityReturned(returnedQty);
                    ins.setMissingQuantity(rsv.getQuantity() - returnedQty);
                    ins.setOutcomeStatus(outcome);
                    ins.setNotes(d.getOrDefault("notes", "Post-use return recorded via web"));
                    inspectionService.recordPostUseInspection(ins);
                }

                reservationService.returnResource(resId, returnedQty, user);
                sendJsonResponse(ex, 200, "{\"success\":true,\"message\":\"Resource returned and post-inspection certified!\"}");
            } catch (Exception e) {
                sendError(ex, 400, e.getMessage());
            }
        }
    }

    // 11. Reports API
    private static class ReportsApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            setCors(ex);
            if (handleOptions(ex)) return;

            String query = ex.getRequestURI().getQuery();
            int reportIndex = parseInt(parseQueryParams(query).getOrDefault("type", "0"), 0);

            try {
                List<String[]> rows;
                String[] headers;
                switch (reportIndex) {
                    case 0: headers = new String[]{"Resource ID", "Name", "Category", "Available", "Condition", "Location", "Purchase Cost"}; rows = reportService.getAvailableResourcesReport(); break;
                    case 1: headers = new String[]{"Resource ID", "Name", "Category", "Condition", "Est. Repair", "Location", "Notes"}; rows = reportService.getResourcesUnderRepairReport(); break;
                    case 2: headers = new String[]{"Resource ID", "Name", "Category", "Verification", "Last Verified", "Next Due", "Audited By", "Location"}; rows = reportService.getVerificationRequiredReport(); break;
                    case 3: headers = new String[]{"Res ID", "Event ID", "Resource ID", "Name", "Qty", "Start", "End", "Status"}; rows = reportService.getEventResourceUsageReport(); break;
                    case 4: headers = new String[]{"Res ID", "Resource ID", "Name", "Event ID", "Time Window", "Status", "Remarks"}; rows = reportService.getReservationConflictReport(); break;
                    case 5: headers = new String[]{"Res ID", "Event ID", "Resource ID", "Name", "Qty", "Deadline", "Alert"}; rows = reportService.getOverdueReturnReport(); break;
                    case 6: headers = new String[]{"Resource ID", "Name", "Category", "Reuse Classification", "Condition", "Status", "Purchase Cost"}; rows = reportService.getResourceReuseReport(); break;
                    case 7: headers = new String[]{"Resource ID", "Name", "Category", "Est. Repair", "Actual Repair", "Status"}; rows = reportService.getRepairCostReport(); break;
                    case 8: headers = new String[]{"Event ID", "Event Name", "Club/Dept", "Date", "Status", "Purchase Avoided"}; rows = reportService.getPotentialPurchaseAvoidanceReport(); break;
                    default: headers = new String[]{"Log ID", "Resource ID", "Action", "Timestamp", "Audited By", "Details"}; rows = reportService.getResourceHistoryReport(); break;
                }

                Map<String, Object> res = new HashMap<>();
                res.put("headers", headers);
                res.put("rows", rows);
                sendJsonResponse(ex, 200, JsonUtil.toJson(res));
            } catch (SQLException e) {
                sendError(ex, 500, e.getMessage());
            }
        }
    }

    // 12. Login API
    private static class LoginApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            setCors(ex);
            if (handleOptions(ex)) return;

            String body = readBody(ex);
            Map<String, String> d = JsonUtil.parseSimpleJson(body);
            try {
                User u = authService.login(d.get("username"), d.get("password"));
                sendJsonResponse(ex, 200, JsonUtil.toJson(u));
            } catch (Exception e) {
                sendError(ex, 401, e.getMessage());
            }
        }
    }

    // 13. Register API
    private static class RegisterApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            setCors(ex);
            if (handleOptions(ex)) return;

            String body = readBody(ex);
            Map<String, String> d = JsonUtil.parseSimpleJson(body);
            try {
                authService.register(
                        d.get("username"),
                        d.get("password"),
                        d.get("fullName"),
                        d.get("email"),
                        d.getOrDefault("role", "ORGANIZER"),
                        d.getOrDefault("department", "Student Activities")
                );
                sendJsonResponse(ex, 200, "{\"success\":true,\"message\":\"User registered successfully\"}");
            } catch (Exception e) {
                sendError(ex, 400, e.getMessage());
            }
        }
    }

    // 14. OOP & Runtime Polymorphism Demo API
    private static class OopDemoApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            setCors(ex);
            if (handleOptions(ex)) return;

            String query = ex.getRequestURI().getQuery();
            int choice = parseInt(parseQueryParams(query).getOrDefault("choice", "0"), 0);

            AbstractResource obj;
            switch (choice) {
                case 1: obj = new ExtensionCable("EL-ELE-001", "Heavy Duty Extension Spike/Cable", 15, 750.0, "Engineering Block", "Room 201"); break;
                case 2: obj = new Table("EL-FUR-001", "Folding Banquet Table (6ft)", 30, 2800.0, "Warehouse", "Room W-02"); break;
                case 3: obj = new CustomPrintedBanner("EL-DEC-002", "Tech Fest 2025 Printed Flex Banner", "Tech Fest 2025", 4, 1200.0, "Store", "Room D-11"); break;
                default: obj = new Projector("EL-AUD-001", "HD High-Lumen Projector (4K Support)", 4, 45000.0, "Media Center", "Room 102"); break;
            }

            StringBuilder log = new StringBuilder();
            log.append(">> RUNTIME POLYMORPHIC DISPATCH VIA JAVA INTERFACES\n");
            log.append(">> Target Object Class: ").append(obj.getClass().getName()).append("\n\n");

            // 1. Resource interface dispatch
            Resource resRef = obj;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            PrintStream oldOut = System.out;
            System.setOut(ps);
            resRef.displayDetails();
            System.setOut(oldOut);
            log.append("[1] Invoking Resource.displayDetails():\n").append(baos.toString().trim()).append("\n\n");

            // 2. Reservable interface dispatch
            if (obj instanceof Reservable) {
                Reservable rsv = (Reservable) obj;
                log.append("[2] Reservable.reserve(): ").append(rsv.reserve() ? "SUCCESS" : "BLOCKED").append("\n");
                rsv.release();
            } else {
                log.append("[2] Reservable: NOT implemented by ").append(obj.getClass().getSimpleName()).append("\n");
            }

            // 3. Repairable interface dispatch
            if (obj instanceof Repairable) {
                Repairable rep = (Repairable) obj;
                log.append("[3] Repairable.calculateRepairCost(): ₹").append(String.format("%,.2f", rep.calculateRepairCost())).append("\n");
            } else {
                log.append("[3] Repairable: NOT implemented by ").append(obj.getClass().getSimpleName()).append("\n");
            }

            // 4. Reusable interface dispatch
            if (obj instanceof Reusable) {
                Reusable reu = (Reusable) obj;
                log.append("[4] Reusable.getReuseRecommendation():\n    \"").append(reu.getReuseRecommendation()).append("\"\n");
            }

            Map<String, String> result = new HashMap<>();
            result.put("consoleOutput", log.toString());
            sendJsonResponse(ex, 200, JsonUtil.toJson(result));
        }
    }

    // -------------------------------------------------------------
    // UTILITY METHODS
    // -------------------------------------------------------------
    private static void setCors(HttpExchange ex) {
        ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        ex.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        ex.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    private static boolean handleOptions(HttpExchange ex) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(ex.getRequestMethod())) {
            ex.sendResponseHeaders(204, -1);
            return true;
        }
        return false;
    }

    private static void sendJsonResponse(HttpExchange ex, int statusCode, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        ex.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void sendError(HttpExchange ex, int statusCode, String message) throws IOException {
        String json = "{\"error\":\"" + escapeJson(message) + "\"}";
        sendJsonResponse(ex, statusCode, json);
    }

    private static String readBody(HttpExchange ex) throws IOException {
        try (InputStream is = ex.getRequestBody()) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int len;
            while ((len = is.read(buf)) != -1) {
                bos.write(buf, 0, len);
            }
            return bos.toString(StandardCharsets.UTF_8);
        }
    }

    private static Map<String, String> parseQueryParams(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isEmpty()) return map;
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            try {
                if (idx > 0) {
                    map.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
                } else if (!pair.isEmpty()) {
                    map.put(URLDecoder.decode(pair, "UTF-8"), "");
                }
            } catch (Exception ignored) {}
        }
        return map;
    }

    private static byte[] readAllBytes(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[4096];
            int len;
            while ((len = fis.read(b)) != -1) {
                bos.write(b, 0, len);
            }
            return bos.toByteArray();
        }
    }

    private static int parseInt(String val, int def) {
        if (val == null || val.trim().isEmpty()) return def;
        try { return Integer.parseInt(val.trim()); } catch (Exception e) { return def; }
    }

    private static double parseDouble(String val, double def) {
        if (val == null || val.trim().isEmpty()) return def;
        try { return Double.parseDouble(val.trim()); } catch (Exception e) { return def; }
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\"", "\\\"").replace("\n", " ").replace("\r", "");
    }
}
