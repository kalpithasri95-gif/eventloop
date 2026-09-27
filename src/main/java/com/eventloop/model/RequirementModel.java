package com.eventloop.model;

/**
 * Model representing an organizer's resource requirement for an event.
 */
public class RequirementModel {
    private int requirementId;
    private String eventId;
    private String category;
    private String resourceName;
    private int quantity;
    private int requiredCondition; // 1 to 5
    private String specifications;
    private String requiredDate;
    private String startTime;
    private String endTime;
    private String indoorOutdoor; // "Indoor", "Outdoor", "Both"
    private String notes;
    private String status; // "PENDING", "MATCHED", "RESERVED", "FULFILLED"

    public RequirementModel() {
        this.requiredCondition = 3;
        this.indoorOutdoor = "Indoor";
        this.status = "PENDING";
    }

    public RequirementModel(int requirementId, String eventId, String category, String resourceName, 
                            int quantity, int requiredCondition, String specifications, 
                            String requiredDate, String startTime, String endTime, 
                            String indoorOutdoor, String notes) {
        this();
        this.requirementId = requirementId;
        this.eventId = eventId;
        this.category = category;
        this.resourceName = resourceName;
        this.quantity = quantity;
        this.requiredCondition = requiredCondition;
        this.specifications = specifications;
        this.requiredDate = requiredDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.indoorOutdoor = indoorOutdoor;
        this.notes = notes;
    }

    public int getRequirementId() { return requirementId; }
    public void setRequirementId(int requirementId) { this.requirementId = requirementId; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getResourceName() { return resourceName; }
    public void setResourceName(String resourceName) { this.resourceName = resourceName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public int getRequiredCondition() { return requiredCondition; }
    public void setRequiredCondition(int requiredCondition) { this.requiredCondition = requiredCondition; }

    public String getSpecifications() { return specifications; }
    public void setSpecifications(String specifications) { this.specifications = specifications; }

    public String getRequiredDate() { return requiredDate; }
    public void setRequiredDate(String requiredDate) { this.requiredDate = requiredDate; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getIndoorOutdoor() { return indoorOutdoor; }
    public void setIndoorOutdoor(String indoorOutdoor) { this.indoorOutdoor = indoorOutdoor; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
