package com.eventloop.model;

/**
 * Model representing pre-use or post-use physical inspections.
 */
public class InspectionModel {
    private int inspectionId;
    private int reservationId;
    private String resourceId;
    private String eventId;
    private String inspectionType; // "PRE_USE", "POST_USE"
    private String inspectorName;
    private String inspectionDate;
    private int conditionRating;
    private int quantityChecked;
    private int quantityReturned;
    private int missingQuantity;
    private boolean damageReported;
    private boolean cleaningRequired;
    private boolean repairRequired;
    private String outcomeStatus; // AVAILABLE, UNDER_REPAIR, MISSING, RETIRED, REPURPOSE_REQUIRED
    private String notes;

    public InspectionModel() {
        this.conditionRating = 5;
        this.damageReported = false;
        this.cleaningRequired = false;
        this.repairRequired = false;
        this.outcomeStatus = "AVAILABLE";
    }

    public int getInspectionId() { return inspectionId; }
    public void setInspectionId(int inspectionId) { this.inspectionId = inspectionId; }

    public int getReservationId() { return reservationId; }
    public void setReservationId(int reservationId) { this.reservationId = reservationId; }

    public String getResourceId() { return resourceId; }
    public void setResourceId(String resourceId) { this.resourceId = resourceId; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getInspectionType() { return inspectionType; }
    public void setInspectionType(String inspectionType) { this.inspectionType = inspectionType; }

    public String getInspectorName() { return inspectorName; }
    public void setInspectorName(String inspectorName) { this.inspectorName = inspectorName; }

    public String getInspectionDate() { return inspectionDate; }
    public void setInspectionDate(String inspectionDate) { this.inspectionDate = inspectionDate; }

    public int getConditionRating() { return conditionRating; }
    public void setConditionRating(int conditionRating) { this.conditionRating = conditionRating; }

    public int getQuantityChecked() { return quantityChecked; }
    public void setQuantityChecked(int quantityChecked) { this.quantityChecked = quantityChecked; }

    public int getQuantityReturned() { return quantityReturned; }
    public void setQuantityReturned(int quantityReturned) { this.quantityReturned = quantityReturned; }

    public int getMissingQuantity() { return missingQuantity; }
    public void setMissingQuantity(int missingQuantity) { this.missingQuantity = missingQuantity; }

    public boolean isDamageReported() { return damageReported; }
    public void setDamageReported(boolean damageReported) { this.damageReported = damageReported; }

    public boolean isCleaningRequired() { return cleaningRequired; }
    public void setCleaningRequired(boolean cleaningRequired) { this.cleaningRequired = cleaningRequired; }

    public boolean isRepairRequired() { return repairRequired; }
    public void setRepairRequired(boolean repairRequired) { this.repairRequired = repairRequired; }

    public String getOutcomeStatus() { return outcomeStatus; }
    public void setOutcomeStatus(String outcomeStatus) { this.outcomeStatus = outcomeStatus; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
