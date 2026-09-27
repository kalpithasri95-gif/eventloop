package com.eventloop.model.resources;

/**
 * Base class for Furniture resources (Tables, Chairs, Podiums, etc.).
 */
public class FurnitureResource extends AbstractResource {
    public FurnitureResource() {
        super();
        this.category = "Furniture";
    }

    public FurnitureResource(String resourceId, String resourceName, int quantity, double purchaseCost, 
                             String storageBuilding, String storageRoom) {
        super(resourceId, resourceName, "Furniture", quantity, purchaseCost, storageBuilding, storageRoom);
    }
}
