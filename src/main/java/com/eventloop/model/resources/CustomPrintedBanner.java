package com.eventloop.model.resources;

/**
 * Custom Printed Banner (e.g., Tech Fest 2026).
 * Cannot be directly reused for new named events, but can be REPURPOSED
 * as backdrop cloth, stage partition, display board cover, or craft material.
 */
public class CustomPrintedBanner extends Banner {

    protected String eventBranding;

    public CustomPrintedBanner() {
        super();
        this.resourceName = "Past Event Printed Flex Banner";
        this.reuseType = "REPURPOSE";
    }

    public CustomPrintedBanner(String resourceId, String resourceName, String eventBranding, 
                               int quantity, double purchaseCost, String storageBuilding, String storageRoom) {
        super(resourceId, resourceName, quantity, purchaseCost, storageBuilding, storageRoom);
        this.eventBranding = eventBranding;
        this.reuseType = "REPURPOSE";
    }

    public String getEventBranding() {
        return eventBranding;
    }

    public void setEventBranding(String eventBranding) {
        this.eventBranding = eventBranding;
    }

    @Override
    public boolean canBeReused() {
        // Can be reused in a repurposed capacity
        return !"RETIRED".equalsIgnoreCase(currentStatus);
    }

    @Override
    public String getReuseRecommendation() {
        return "REPURPOSE: Printed with specific past event text ('" + (eventBranding != null ? eventBranding : "Past Fest") + 
               "'). Do not purchase new backdrop sheets! Reverse side can serve as blank backdrop cloth, acoustic baffle, display partition, or student art canvas.";
    }
}
