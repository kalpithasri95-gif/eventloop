package com.eventloop.model.resources;

/**
 * Base class for all general Equipment resources.
 */
public class EquipmentResource extends AbstractResource {
    public EquipmentResource() {
        super();
        this.category = "Equipment";
    }

    public EquipmentResource(String resourceId, String resourceName, int quantity, double purchaseCost, 
                             String storageBuilding, String storageRoom) {
        super(resourceId, resourceName, "Equipment", quantity, purchaseCost, storageBuilding, storageRoom);
    }
}
