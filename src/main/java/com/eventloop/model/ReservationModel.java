package com.eventloop.model;

/**
 * Model representing a date and time-based resource reservation.
 */
public class ReservationModel {
    private int reservationId;
    private String eventId;
    private String resourceId;
    private String resourceName;
    private int quantity;
    private String startDateTime;
    private String endDateTime;
    private String returnDeadline;
    private String reservationStatus; // REQUESTED, APPROVED, REJECTED, ACTIVE, COMPLETED, CANCELLED, OVERDUE
    private String approvedBy;
    private String remarks;
    private String createdDate;

    public ReservationModel() {
        this.reservationStatus = "REQUESTED";
    }

    public ReservationModel(int reservationId, String eventId, String resourceId, String resourceName, 
                            int quantity, String startDateTime, String endDateTime, 
                            String returnDeadline, String remarks) {
        this();
        this.reservationId = reservationId;
        this.eventId = eventId;
        this.resourceId = resourceId;
        this.resourceName = resourceName;
        this.quantity = quantity;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.returnDeadline = returnDeadline;
        this.remarks = remarks;
    }

    public int getReservationId() { return reservationId; }
    public void setReservationId(int reservationId) { this.reservationId = reservationId; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getResourceId() { return resourceId; }
    public void setResourceId(String resourceId) { this.resourceId = resourceId; }

    public String getResourceName() { return resourceName; }
    public void setResourceName(String resourceName) { this.resourceName = resourceName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getStartDateTime() { return startDateTime; }
    public void setStartDateTime(String startDateTime) { this.startDateTime = startDateTime; }

    public String getEndDateTime() { return endDateTime; }
    public void setEndDateTime(String endDateTime) { this.endDateTime = endDateTime; }

    public String getReturnDeadline() { return returnDeadline; }
    public void setReturnDeadline(String returnDeadline) { this.returnDeadline = returnDeadline; }

    public String getReservationStatus() { return reservationStatus; }
    public void setReservationStatus(String reservationStatus) { this.reservationStatus = reservationStatus; }

    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public String getCreatedDate() { return createdDate; }
    public void setCreatedDate(String createdDate) { this.createdDate = createdDate; }
}
