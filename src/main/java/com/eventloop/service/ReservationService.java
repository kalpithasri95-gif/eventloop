package com.eventloop.service;

import com.eventloop.dao.EventDAO;
import com.eventloop.dao.HistoryDAO;
import com.eventloop.dao.ReservationDAO;
import com.eventloop.dao.ResourceDAO;
import com.eventloop.exception.ReservationConflictException;
import com.eventloop.exception.ResourceNotAvailableException;
import com.eventloop.exception.ValidationException;
import com.eventloop.exception.VerificationRequiredException;
import com.eventloop.model.EventModel;
import com.eventloop.model.ReservationModel;
import com.eventloop.model.resources.AbstractResource;
import com.eventloop.util.DateUtil;
import com.eventloop.util.ValidationUtil;
import java.sql.SQLException;
import java.util.List;

public class ReservationService {
    private static ReservationService instance;
    private final ReservationDAO reservationDAO;
    private final ResourceDAO resourceDAO;
    private final EventDAO eventDAO;
    private final HistoryDAO historyDAO;

    private ReservationService() {
        this.reservationDAO = new ReservationDAO();
        this.resourceDAO = new ResourceDAO();
        this.eventDAO = new EventDAO();
        this.historyDAO = new HistoryDAO();
    }

    public static synchronized ReservationService getInstance() {
        if (instance == null) {
            instance = new ReservationService();
        }
        return instance;
    }

    public boolean requestReservation(ReservationModel res, String requestedBy) 
            throws ValidationException, VerificationRequiredException, ResourceNotAvailableException, 
                   ReservationConflictException, SQLException {
        ValidationUtil.requireNonEmpty(res.getEventId(), "Event ID");
        ValidationUtil.requireNonEmpty(res.getResourceId(), "Resource ID");
        ValidationUtil.validatePositive(res.getQuantity(), "Quantity");
        ValidationUtil.requireNonEmpty(res.getStartDateTime(), "Start Date Time");
        ValidationUtil.requireNonEmpty(res.getEndDateTime(), "End Date Time");

        AbstractResource r = resourceDAO.getById(res.getResourceId());
        if (r == null) {
            throw new ResourceNotAvailableException("Resource with ID '" + res.getResourceId() + "' not found.");
        }

        // 1. Verification Rule: Must be verified and not expired
        if ("EXPIRED".equalsIgnoreCase(r.getVerificationStatus()) || DateUtil.isExpired(r.getNextVerificationDate())) {
            throw new VerificationRequiredException("Verification Required – This resource verification has expired. It cannot be reserved until re-verified by Admin.");
        }
        if (!"VERIFIED".equalsIgnoreCase(r.getVerificationStatus())) {
            throw new VerificationRequiredException("Verification Required – This resource is currently unverified. It cannot be reserved until verified.");
        }

        // 2. Status Rule: MISSING, RETIRED, UNDER_REPAIR cannot be reserved
        String status = r.getCurrentStatus().toUpperCase();
        if ("MISSING".equals(status) || "RETIRED".equals(status) || "UNDER_REPAIR".equals(status)) {
            throw new ResourceNotAvailableException("Resource is currently in '" + status + "' state and cannot be reserved.");
        }

        // 3. Overlap & Conflict Detection Rule
        List<ReservationModel> overlaps = reservationDAO.findOverlappingReservations(
                res.getResourceId(), res.getStartDateTime(), res.getEndDateTime());
        
        int reservedInPeriod = 0;
        for (ReservationModel o : overlaps) {
            reservedInPeriod += o.getQuantity();
        }

        if (reservedInPeriod > 0 && (r.getQuantity() - reservedInPeriod) < res.getQuantity()) {
            throw new ReservationConflictException("Reservation Conflict – Resource '" + r.getResourceName() + 
                    "' is already reserved during the requested time window (" + res.getStartDateTime() + " to " + res.getEndDateTime() + ").");
        }

        // 4. Available Quantity Rule
        if (r.getAvailableQuantity() < res.getQuantity()) {
            throw new ResourceNotAvailableException("Insufficient available quantity. Requested: " + res.getQuantity() + 
                    ", Available: " + r.getAvailableQuantity() + " " + r.getUnit());
        }

        // Set details
        res.setResourceName(r.getResourceName());
        if (res.getReturnDeadline() == null || res.getReturnDeadline().trim().isEmpty()) {
            res.setReturnDeadline(res.getEndDateTime());
        }

        boolean ok = reservationDAO.insertReservation(res);
        if (ok) {
            historyDAO.addHistory(r.getResourceId(), "RESERVATION_REQUESTED", requestedBy, 
                    "Reservation requested for Event: " + res.getEventId() + " (Qty: " + res.getQuantity() + " " + r.getUnit() + ")");

            // Update potential purchase avoided for event
            double avoided = res.getQuantity() * r.getPurchaseCost();
            eventDAO.updatePurchaseAvoided(res.getEventId(), avoided);

            // Update event status to RESOURCES_RESERVED
            EventModel evt = eventDAO.getById(res.getEventId());
            if (evt != null && "DRAFT".equalsIgnoreCase(evt.getEventStatus())) {
                eventDAO.updateEventStatus(res.getEventId(), "RESOURCES_RESERVED");
            }
        }
        return ok;
    }

    public boolean approveReservation(int reservationId, String approvedBy) throws SQLException {
        ReservationModel res = reservationDAO.getById(reservationId);
        if (res != null) {
            boolean ok = reservationDAO.updateStatus(reservationId, "APPROVED");
            if (ok) {
                historyDAO.addHistory(res.getResourceId(), "RESERVATION_APPROVED", approvedBy, 
                        "Reservation #" + reservationId + " approved for Event: " + res.getEventId());
            }
            return ok;
        }
        return false;
    }

    public boolean rejectReservation(int reservationId, String rejectedBy, String reason) throws SQLException {
        ReservationModel res = reservationDAO.getById(reservationId);
        if (res != null) {
            boolean ok = reservationDAO.updateStatus(reservationId, "REJECTED");
            if (ok) {
                historyDAO.addHistory(res.getResourceId(), "RESERVATION_REJECTED", rejectedBy, 
                        "Reservation #" + reservationId + " rejected. Reason: " + reason);
            }
            return ok;
        }
        return false;
    }

    public boolean checkoutResource(int reservationId, String handledBy) throws SQLException, ResourceNotAvailableException {
        ReservationModel res = reservationDAO.getById(reservationId);
        if (res == null) return false;

        AbstractResource r = resourceDAO.getById(res.getResourceId());
        if (r == null || r.getAvailableQuantity() < res.getQuantity()) {
            throw new ResourceNotAvailableException("Cannot checkout: Insufficient physical stock available right now.");
        }

        // Deduct inventory
        int newAvail = r.getAvailableQuantity() - res.getQuantity();
        resourceDAO.updateAvailableQuantity(r.getResourceId(), newAvail);

        // Update reservation to ACTIVE
        reservationDAO.updateStatus(reservationId, "ACTIVE");

        // Update event status to IN_PROGRESS
        eventDAO.updateEventStatus(res.getEventId(), "IN_PROGRESS");

        historyDAO.addHistory(r.getResourceId(), "CHECKED_OUT", handledBy, 
                "Checked out " + res.getQuantity() + " " + r.getUnit() + " for Event: " + res.getEventId());
        return true;
    }

    public boolean returnResource(int reservationId, int returnedQty, String handledBy) throws SQLException {
        ReservationModel res = reservationDAO.getById(reservationId);
        if (res == null) return false;

        AbstractResource r = resourceDAO.getById(res.getResourceId());
        if (r != null) {
            int newAvail = Math.min(r.getQuantity(), r.getAvailableQuantity() + returnedQty);
            resourceDAO.updateAvailableQuantity(r.getResourceId(), newAvail);
        }

        reservationDAO.updateStatus(reservationId, "COMPLETED");

        // Update event status to RETURN_PENDING / INSPECTION_PENDING
        eventDAO.updateEventStatus(res.getEventId(), "INSPECTION_PENDING");

        historyDAO.addHistory(res.getResourceId(), "RETURNED", handledBy, 
                "Returned " + returnedQty + " units from Event: " + res.getEventId() + ". Awaiting final inspection.");
        return true;
    }

    public List<ReservationModel> getAllReservations() throws SQLException {
        return reservationDAO.getAllReservations();
    }

    public List<ReservationModel> getReservationsByEvent(String eventId) throws SQLException {
        return reservationDAO.getReservationsByEvent(eventId);
    }
}
