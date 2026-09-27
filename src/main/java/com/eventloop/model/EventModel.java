package com.eventloop.model;

/**
 * Model representing an Event in EventLoop.
 * Tracks event lifecycle and purchase avoidance metrics.
 */
public class EventModel {
    private String eventId;
    private String eventName;
    private String organizerName;
    private int organizerId;
    private String departmentOrClub;
    private String eventType; // Technical, Cultural, Sports, Workshop, Seminar, Conference, Club Activity, Other
    private String eventDate;
    private String startTime;
    private String endTime;
    private String location;
    private int expectedParticipants;
    private String eventStatus; // DRAFT, REQUIREMENTS_ADDED, RESOURCES_RESERVED, IN_PROGRESS, RETURN_PENDING, INSPECTION_PENDING, COMPLETED, CANCELLED
    private double potentialPurchaseAvoided;
    private String createdDate;

    public EventModel() {
        this.eventStatus = "DRAFT";
        this.potentialPurchaseAvoided = 0.0;
    }

    public EventModel(String eventId, String eventName, String organizerName, int organizerId, 
                      String departmentOrClub, String eventType, String eventDate, 
                      String startTime, String endTime, String location, int expectedParticipants) {
        this();
        this.eventId = eventId;
        this.eventName = eventName;
        this.organizerName = organizerName;
        this.organizerId = organizerId;
        this.departmentOrClub = departmentOrClub;
        this.eventType = eventType;
        this.eventDate = eventDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.expectedParticipants = expectedParticipants;
    }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public String getOrganizerName() { return organizerName; }
    public void setOrganizerName(String organizerName) { this.organizerName = organizerName; }

    public int getOrganizerId() { return organizerId; }
    public void setOrganizerId(int organizerId) { this.organizerId = organizerId; }

    public String getDepartmentOrClub() { return departmentOrClub; }
    public void setDepartmentOrClub(String departmentOrClub) { this.departmentOrClub = departmentOrClub; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getEventDate() { return eventDate; }
    public void setEventDate(String eventDate) { this.eventDate = eventDate; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public int getExpectedParticipants() { return expectedParticipants; }
    public void setExpectedParticipants(int expectedParticipants) { this.expectedParticipants = expectedParticipants; }

    public String getEventStatus() { return eventStatus; }
    public void setEventStatus(String eventStatus) { this.eventStatus = eventStatus; }

    public double getPotentialPurchaseAvoided() { return potentialPurchaseAvoided; }
    public void setPotentialPurchaseAvoided(double potentialPurchaseAvoided) { this.potentialPurchaseAvoided = potentialPurchaseAvoided; }

    public String getCreatedDate() { return createdDate; }
    public void setCreatedDate(String createdDate) { this.createdDate = createdDate; }

    @Override
    public String toString() {
        return eventId + " - " + eventName + " (" + eventDate + ")";
    }
}
