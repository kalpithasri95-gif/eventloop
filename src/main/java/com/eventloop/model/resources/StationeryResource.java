package com.eventloop.model.resources;

/**
 * Base class for Stationery and registration resources.
 */
public class StationeryResource extends AbstractResource {
    public StationeryResource() {
        super();
        this.category = "Stationery";
    }

    public StationeryResource(String resourceId, String resourceName, int quantity, double purchaseCost, 
                              String storageBuilding, String storageRoom) {
        super(resourceId, resourceName, "Stationery", quantity, purchaseCost, storageBuilding, storageRoom);
    }
}
