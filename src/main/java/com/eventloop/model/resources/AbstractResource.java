package com.eventloop.model.resources;

import com.eventloop.interfaces.Resource;

/**
 * Base abstract class for all physical event resources.
 * Encapsulates core state, location, verification, and inspection logic.
 */
public abstract class AbstractResource implements Resource {
    protected String resourceId;
    protected String resourceName;
    protected String category;
    protected String description;
    protected int quantity;
    protected int availableQuantity;
    protected String unit;
    protected int conditionRating; // 1 to 5
    protected String currentStatus; // AVAILABLE, RESERVED, IN_USE, UNDER_INSPECTION, UNDER_REPAIR, MISSING, RETIRED, REPURPOSE_REQUIRED
    protected String verificationStatus; // VERIFIED, VERIFICATION_REQUIRED, EXPIRED
    protected String lastVerifiedDate;
    protected String nextVerificationDate;
    protected String verifiedBy;
    protected String verificationNotes;

    // Storage Location
    protected String storageBuilding;
    protected String storageRoom;
    protected String rackNumber;
    protected String shelfNumber;
    protected String boxNumber;

    protected String currentHolder;
    protected double purchaseCost;
    protected double estimatedRepairCost;
    protected double actualRepairCost;
    protected String reuseType; // DIRECT_REUSE, MODIFICATION_REQUIRED, REPURPOSE, RECYCLE, RETIRE
    protected String specifications;
    protected String notes;
    protected String createdDate;
    protected String lastUpdatedDate;

    public AbstractResource() {
        this.unit = "pcs";
        this.conditionRating = 5;
        this.currentStatus = "AVAILABLE";
        this.verificationStatus = "VERIFICATION_REQUIRED";
        this.reuseType = "DIRECT_REUSE";
    }

    public AbstractResource(String resourceId, String resourceName, String category, int quantity, 
                            double purchaseCost, String storageBuilding, String storageRoom) {
        this();
        this.resourceId = resourceId;
        this.resourceName = resourceName;
        this.category = category;
        this.quantity = quantity;
        this.availableQuantity = quantity;
        this.purchaseCost = purchaseCost;
        this.storageBuilding = storageBuilding;
        this.storageRoom = storageRoom;
    }

    @Override
    public void displayDetails() {
        System.out.println("Resource ID: " + resourceId + " | Name: " + resourceName);
        System.out.println("Category: " + category + " | Status: " + currentStatus + " | Condition: " + conditionRating + "/5");
        System.out.println("Available: " + availableQuantity + "/" + quantity + " " + unit + " | Location: " + getFullLocation());
        System.out.println("Verification: " + verificationStatus + " (Last: " + lastVerifiedDate + ", Next: " + nextVerificationDate + ")");
    }

    @Override
    public void inspect() {
        // Base inspection logic
        if (conditionRating <= 2) {
            this.currentStatus = "UNDER_REPAIR";
        } else if (conditionRating == 0) {
            this.currentStatus = "RETIRED";
        } else {
            this.currentStatus = "AVAILABLE";
        }
    }

    public String getFullLocation() {
        StringBuilder sb = new StringBuilder();
        if (storageBuilding != null && !storageBuilding.isEmpty()) sb.append(storageBuilding);
        if (storageRoom != null && !storageRoom.isEmpty()) sb.append(" - Room ").append(storageRoom);
        if (rackNumber != null && !rackNumber.isEmpty()) sb.append(", Rack ").append(rackNumber);
        if (shelfNumber != null && !shelfNumber.isEmpty()) sb.append(", Shelf ").append(shelfNumber);
        if (boxNumber != null && !boxNumber.isEmpty()) sb.append(", Box ").append(boxNumber);
        return sb.length() > 0 ? sb.toString() : "Not specified";
    }

    public boolean isReservableState() {
        if ("MISSING".equalsIgnoreCase(currentStatus) || 
            "RETIRED".equalsIgnoreCase(currentStatus) || 
            "UNDER_REPAIR".equalsIgnoreCase(currentStatus)) {
            return false;
        }
        if (!"VERIFIED".equalsIgnoreCase(verificationStatus)) {
            return false;
        }
        return availableQuantity > 0;
    }

    // Getters and Setters
    public String getResourceId() { return resourceId; }
    public void setResourceId(String resourceId) { this.resourceId = resourceId; }

    public String getResourceName() { return resourceName; }
    public void setResourceName(String resourceName) { this.resourceName = resourceName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public int getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(int availableQuantity) { this.availableQuantity = availableQuantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public int getConditionRating() { return conditionRating; }
    public void setConditionRating(int conditionRating) { this.conditionRating = conditionRating; }

    public String getCurrentStatus() { return currentStatus; }
    public void setCurrentStatus(String currentStatus) { this.currentStatus = currentStatus; }

    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; }

    public String getLastVerifiedDate() { return lastVerifiedDate; }
    public void setLastVerifiedDate(String lastVerifiedDate) { this.lastVerifiedDate = lastVerifiedDate; }

    public String getNextVerificationDate() { return nextVerificationDate; }
    public void setNextVerificationDate(String nextVerificationDate) { this.nextVerificationDate = nextVerificationDate; }

    public String getVerifiedBy() { return verifiedBy; }
    public void setVerifiedBy(String verifiedBy) { this.verifiedBy = verifiedBy; }

    public String getVerificationNotes() { return verificationNotes; }
    public void setVerificationNotes(String verificationNotes) { this.verificationNotes = verificationNotes; }

    public String getStorageBuilding() { return storageBuilding; }
    public void setStorageBuilding(String storageBuilding) { this.storageBuilding = storageBuilding; }

    public String getStorageRoom() { return storageRoom; }
    public void setStorageRoom(String storageRoom) { this.storageRoom = storageRoom; }

    public String getRackNumber() { return rackNumber; }
    public void setRackNumber(String rackNumber) { this.rackNumber = rackNumber; }

    public String getShelfNumber() { return shelfNumber; }
    public void setShelfNumber(String shelfNumber) { this.shelfNumber = shelfNumber; }

    public String getBoxNumber() { return boxNumber; }
    public void setBoxNumber(String boxNumber) { this.boxNumber = boxNumber; }

    public String getCurrentHolder() { return currentHolder; }
    public void setCurrentHolder(String currentHolder) { this.currentHolder = currentHolder; }

    public double getPurchaseCost() { return purchaseCost; }
    public void setPurchaseCost(double purchaseCost) { this.purchaseCost = purchaseCost; }

    public double getEstimatedRepairCost() { return estimatedRepairCost; }
    public void setEstimatedRepairCost(double estimatedRepairCost) { this.estimatedRepairCost = estimatedRepairCost; }

    public double getActualRepairCost() { return actualRepairCost; }
    public void setActualRepairCost(double actualRepairCost) { this.actualRepairCost = actualRepairCost; }

    public String getReuseType() { return reuseType; }
    public void setReuseType(String reuseType) { this.reuseType = reuseType; }

    public String getSpecifications() { return specifications; }
    public void setSpecifications(String specifications) { this.specifications = specifications; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getCreatedDate() { return createdDate; }
    public void setCreatedDate(String createdDate) { this.createdDate = createdDate; }

    public String getLastUpdatedDate() { return lastUpdatedDate; }
    public void setLastUpdatedDate(String lastUpdatedDate) { this.lastUpdatedDate = lastUpdatedDate; }
}
