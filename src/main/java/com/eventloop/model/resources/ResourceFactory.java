package com.eventloop.model.resources;

/**
 * Factory class for creating specific polymorphic Resource instances based on
 * category and resource name.
 */
public class ResourceFactory {

    public static AbstractResource createResource(String category, String name) {
        String lowerName = (name != null) ? name.toLowerCase() : "";
        String cat = (category != null) ? category.trim() : "";

        if (lowerName.contains("projector")) {
            return new Projector();
        } else if (lowerName.contains("cable") || lowerName.contains("extension")) {
            return new ExtensionCable();
        } else if (lowerName.contains("table")) {
            return new Table();
        } else if (lowerName.contains("chair")) {
            return new Chair();
        } else if (lowerName.contains("custom") || lowerName.contains("printed banner") || lowerName.contains("fest")) {
            return new CustomPrintedBanner();
        } else if (lowerName.contains("banner")) {
            return new Banner();
        } else if (lowerName.contains("standee")) {
            return new StandeeFrame();
        } else if (lowerName.contains("badge") || lowerName.contains("lanyard")) {
            return new NameBadge();
        } else if (lowerName.contains("speaker") || lowerName.contains("audio") || lowerName.contains("pa")) {
            return new Speaker();
        } else if (lowerName.contains("mic") || lowerName.contains("microphone")) {
            return new Microphone();
        } else if (lowerName.contains("kit") || lowerName.contains("folder")) {
            return new RegistrationKit();
        }

        // Default fallbacks based on category
        switch (cat) {
            case "Equipment":
                return new EquipmentResource();
            case "Furniture":
                return new FurnitureResource();
            case "Decoration":
                return new DecorationResource();
            case "Stationery":
            case "Registration Material":
                return new StationeryResource();
            case "Electrical":
                return new ElectricalResource();
            case "Audio/Visual":
                return new AudioVisualResource();
            default:
                return new EquipmentResource();
        }
    }
}
