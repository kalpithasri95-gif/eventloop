package com.eventloop.model.resources;

/**
 * Base class for Electrical resources (Extension boxes, cables, distribution units).
 */
public class ElectricalResource extends AbstractResource {
    public ElectricalResource() {
        super();
        this.category = "Electrical";
    }

    public ElectricalResource(String resourceId, String resourceName, int quantity, double purchaseCost, 
                              String storageBuilding, String storageRoom) {
        super(resourceId, resourceName, "Electrical", quantity, purchaseCost, storageBuilding, storageRoom);
    }
}
