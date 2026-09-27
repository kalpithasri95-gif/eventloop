package com.eventloop.model;

/**
 * Model representing an audit history log entry for a resource.
 */
public class HistoryModel {
    private int historyId;
    private String resourceId;
    private String actionType; // CREATED, VERIFIED, RESERVED, CHECKED_OUT, RETURNED, INSPECTED, REPAIRED, MARKED_MISSING, RETIRED, REPURPOSED
    private String actionDate;
    private String performedBy;
    private String details;

    public HistoryModel() {}

    public HistoryModel(int historyId, String resourceId, String actionType, String actionDate, 
                        String performedBy, String details) {
        this.historyId = historyId;
        this.resourceId = resourceId;
        this.actionType = actionType;
        this.actionDate = actionDate;
        this.performedBy = performedBy;
        this.details = details;
    }

    public int getHistoryId() { return historyId; }
    public void setHistoryId(int historyId) { this.historyId = historyId; }

    public String getResourceId() { return resourceId; }
    public void setResourceId(String resourceId) { this.resourceId = resourceId; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public String getActionDate() { return actionDate; }
    public void setActionDate(String actionDate) { this.actionDate = actionDate; }

    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}
