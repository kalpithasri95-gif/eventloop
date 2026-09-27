package com.eventloop.service;

import com.eventloop.dao.EventDAO;
import com.eventloop.dao.InspectionDAO;
import com.eventloop.dao.ReservationDAO;
import com.eventloop.exception.EventClosureException;
import com.eventloop.exception.InvalidEventDateException;
import com.eventloop.exception.ValidationException;
import com.eventloop.model.EventModel;
import com.eventloop.model.InspectionModel;
import com.eventloop.model.RequirementModel;
import com.eventloop.model.ReservationModel;
import com.eventloop.util.DateUtil;
import com.eventloop.util.ValidationUtil;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class EventService {
    private static EventService instance;
    private final EventDAO eventDAO;
    private final ReservationDAO reservationDAO;
    private final InspectionDAO inspectionDAO;

    private EventService() {
        this.eventDAO = new EventDAO();
        this.reservationDAO = new ReservationDAO();
        this.inspectionDAO = new InspectionDAO();
    }

    public static synchronized EventService getInstance() {
        if (instance == null) {
            instance = new EventService();
        }
        return instance;
    }

    public boolean createEvent(EventModel evt) throws ValidationException, InvalidEventDateException, SQLException {
        ValidationUtil.requireNonEmpty(evt.getEventId(), "Event ID");
        ValidationUtil.requireNonEmpty(evt.getEventName(), "Event Name");
        ValidationUtil.requireNonEmpty(evt.getOrganizerName(), "Organizer Name");
        ValidationUtil.requireNonEmpty(evt.getEventDate(), "Event Date");
        ValidationUtil.requireNonEmpty(evt.getLocation(), "Location");
        ValidationUtil.validateTimeOrder(evt.getStartTime(), evt.getEndTime());

        LocalDate eventDate = DateUtil.parseDate(evt.getEventDate());
        if (eventDate == null) {
            throw new InvalidEventDateException("Event date format must be YYYY-MM-DD.");
        }

        if (eventDAO.getById(evt.getEventId()) != null) {
            throw new ValidationException("Event with ID '" + evt.getEventId() + "' already exists.");
        }

        return eventDAO.insertEvent(evt);
    }

    public boolean updateEvent(EventModel evt) throws ValidationException, SQLException {
        ValidationUtil.requireNonEmpty(evt.getEventId(), "Event ID");
        ValidationUtil.requireNonEmpty(evt.getEventName(), "Event Name");
        ValidationUtil.validateTimeOrder(evt.getStartTime(), evt.getEndTime());
        return eventDAO.updateEvent(evt);
    }

    public EventModel getById(String eventId) throws SQLException {
        return eventDAO.getById(eventId);
    }

    public List<EventModel> getAllEvents() throws SQLException {
        return eventDAO.getAllEvents();
    }

    public List<EventModel> getEventsByOrganizer(int organizerId) throws SQLException {
        return eventDAO.getEventsByOrganizer(organizerId);
    }

    public boolean addRequirement(RequirementModel req) throws ValidationException, SQLException {
        ValidationUtil.requireNonEmpty(req.getEventId(), "Event ID");
        ValidationUtil.requireNonEmpty(req.getResourceName(), "Resource Name");
        ValidationUtil.requireNonEmpty(req.getCategory(), "Category");
        ValidationUtil.validatePositive(req.getQuantity(), "Quantity");
        ValidationUtil.validateConditionRating(req.getRequiredCondition());

        boolean ok = eventDAO.insertRequirement(req);
        if (ok) {
            // Update event status to REQUIREMENTS_ADDED if currently DRAFT
            EventModel evt = eventDAO.getById(req.getEventId());
            if (evt != null && "DRAFT".equalsIgnoreCase(evt.getEventStatus())) {
                eventDAO.updateEventStatus(req.getEventId(), "REQUIREMENTS_ADDED");
            }
        }
        return ok;
    }

    public boolean updateRequirementStatus(int reqId, String status) throws SQLException {
        return eventDAO.updateRequirementStatus(reqId, status);
    }

    public List<RequirementModel> getRequirementsForEvent(String eventId) throws SQLException {
        return eventDAO.getRequirementsForEvent(eventId);
    }

    /**
     * Mandatory Event Closure Rule:
     * Prevents event closure until all resources are returned, inspected, and accounted for.
     */
    public boolean closeEvent(String eventId, String closedBy) throws EventClosureException, SQLException {
        EventModel evt = eventDAO.getById(eventId);
        if (evt == null) {
            throw new EventClosureException("Event '" + eventId + "' does not exist.");
        }

        if ("COMPLETED".equalsIgnoreCase(evt.getEventStatus())) {
            throw new EventClosureException("Event is already marked COMPLETED.");
        }

        List<ReservationModel> reservations = reservationDAO.getReservationsByEvent(eventId);

        // Check 1: Are there active or overdue reservations?
        for (ReservationModel r : reservations) {
            if ("ACTIVE".equalsIgnoreCase(r.getReservationStatus()) || "OVERDUE".equalsIgnoreCase(r.getReservationStatus())) {
                throw new EventClosureException("Cannot close event! Resource '" + r.getResourceName() + 
                        "' (Qty: " + r.getQuantity() + ") is still checked out (" + r.getReservationStatus() + "). It must be returned first.");
            }
        }

        // Check 2: Are there post-use inspections pending?
        List<InspectionModel> inspections = inspectionDAO.getInspectionsByEvent(eventId);
        for (ReservationModel r : reservations) {
            if ("COMPLETED".equalsIgnoreCase(r.getReservationStatus())) {
                boolean hasPostInspection = false;
                for (InspectionModel ins : inspections) {
                    if (ins.getReservationId() == r.getReservationId() && "POST_USE".equalsIgnoreCase(ins.getInspectionType())) {
                        hasPostInspection = true;
                        break;
                    }
                }
                if (!hasPostInspection) {
                    throw new EventClosureException("Cannot close event! Post-use inspection has not been completed for returned resource '" + 
                            r.getResourceName() + "'. Store manager must inspect the resource before event closure.");
                }
            }
        }

        // Check 3: Check for unresolved missing items without acknowledgment
        for (InspectionModel ins : inspections) {
            if (ins.getMissingQuantity() > 0 && "MISSING".equalsIgnoreCase(ins.getOutcomeStatus())) {
                // Warning note or closure log
                System.out.println("Warning: Event " + eventId + " closed with recorded missing items: " + ins.getMissingQuantity() + " units of " + ins.getResourceId());
            }
        }

        // All checks passed! Mark as COMPLETED
        return eventDAO.updateEventStatus(eventId, "COMPLETED");
    }
}
