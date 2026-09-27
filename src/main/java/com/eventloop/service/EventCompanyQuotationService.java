package com.eventloop.service;

import com.eventloop.model.EventQuotationPackage;
import com.eventloop.model.EventQuotationPackage.PackageItemDetail;
import java.util.ArrayList;
import java.util.List;

/**
 * Enterprise Commercial Engine tailored 100% for Private Event Management Companies.
 * Generates client proposals, cross-hiring vendor cost splits, security deposit terms, and net profit margins.
 */
public class EventCompanyQuotationService {

    private static EventCompanyQuotationService instance;

    private EventCompanyQuotationService() {}

    public static synchronized EventCompanyQuotationService getInstance() {
        if (instance == null) {
            instance = new EventCompanyQuotationService();
        }
        return instance;
    }

    public List<EventQuotationPackage> generateClientQuotations(String eventType, double targetClientBudget, int guestCount) {
        if (targetClientBudget <= 0) targetClientBudget = 300000.0; // Default ₹3 Lakhs
        if (guestCount <= 0) guestCount = 400;
        if (eventType == null || eventType.trim().isEmpty()) eventType = "Wedding & Reception";

        List<EventQuotationPackage> packages = new ArrayList<>();

        // 1. PLATINUM VIP PACKAGE
        packages.add(buildPlatinumPackage(eventType, targetClientBudget, guestCount));

        // 2. GOLD SIGNATURE PACKAGE
        packages.add(buildGoldPackage(eventType, targetClientBudget, guestCount));

        // 3. SILVER VALUE / MAX PROFIT PACKAGE
        packages.add(buildSilverPackage(eventType, targetClientBudget, guestCount));

        return packages;
    }

    private EventQuotationPackage buildPlatinumPackage(String eventType, double budget, int guests) {
        EventQuotationPackage pkg = new EventQuotationPackage();
        pkg.setPackageTier("PLATINUM_VIP");
        pkg.setPackageName("Royal Platinum VIP Production (" + eventType + ")");
        pkg.setClientTargetSuitability("Best for High-End Weddings, Celebrity Galas & Mega Corporate Brand Launches");
        pkg.setSetupDurationHours(8);
        pkg.setCrewTechniciansNeeded(6);
        pkg.setSecurityDepositRequired(50000.0);

        double clientQuote = budget * 0.95; // e.g. ₹2,85,000 on a ₹3L budget
        List<PackageItemDetail> items = new ArrayList<>();

        items.add(new PackageItemDetail("Audio/Sound", "Line Array Sound System (4 Top + 2 Dual Subwoofers)", 1, "IN_HOUSE_STOCK", 12000.0, 45000.0, "Company Central Warehouse"));
        items.add(new PackageItemDetail("Lighting", "Beam 230W 7R Sharpy Moving Heads + Ambient Wash", 12, "IN_HOUSE_STOCK", 6000.0, 36000.0, "Company Lighting Pool"));
        items.add(new PackageItemDetail("Video Wall", "Ultra-HD P3.91 Outdoor LED Video Wall (16ft x 10ft)", 1, "VENDOR_CROSS_HIRE", 38000.0, 75000.0, "Partner: Apex Visuals"));
        items.add(new PackageItemDetail("Truss & Stage", "Box Aluminum Heavy Truss Structure (40ft x 30ft)", 1, "IN_HOUSE_STOCK", 8000.0, 35000.0, "Truss Yard"));
        items.add(new PackageItemDetail("Special FX", "Cold Pyro Spark Machines + Heavy Fog CO2 Jet", 4, "VENDOR_CROSS_HIRE", 12000.0, 25000.0, "Partner: SparkFX"));
        items.add(new PackageItemDetail("Crew & Ops", "Lead Sound Engineer + Lighting Programmer + 4 Riggers", 6, "SUBCONTRACT_CREW", 18000.0, 35000.0, "Direct Crew Payroll"));
        items.add(new PackageItemDetail("Logistics", "Closed Body Truck Transport (Load-in & Load-out)", 2, "SUBCONTRACT_CREW", 9000.0, 18000.0, "City Movers"));

        double directCost = items.stream().mapToDouble(PackageItemDetail::getInternalCost).sum();
        double billedTotal = items.stream().mapToDouble(PackageItemDetail::getClientBillingRate).sum();
        double finalQuote = Math.max(clientQuote, billedTotal);

        pkg.setClientQuotePrice(finalQuote);
        pkg.setInternalDirectCost(directCost);
        pkg.setGrossProfit(finalQuote - directCost);
        pkg.setProfitMarginPct(((finalQuote - directCost) / finalQuote) * 100.0);
        pkg.setInHouseItemsUsed(15);
        pkg.setVendorCrossHiredItems(5);
        pkg.setItemizedList(items);

        return pkg;
    }

    private EventQuotationPackage buildGoldPackage(String eventType, double budget, int guests) {
        EventQuotationPackage pkg = new EventQuotationPackage();
        pkg.setPackageTier("GOLD_BALANCED");
        pkg.setPackageName("Gold Signature Hybrid Package (" + eventType + ")");
        pkg.setClientTargetSuitability("Ideal balance of high-impact visual aesthetics, robust sound, and maximum organizer profit");
        pkg.setSetupDurationHours(5);
        pkg.setCrewTechniciansNeeded(4);
        pkg.setSecurityDepositRequired(30000.0);

        double clientQuote = budget * 0.72; // e.g. ₹2,16,000 on ₹3L budget
        List<PackageItemDetail> items = new ArrayList<>();

        items.add(new PackageItemDetail("Audio/Sound", "Dual Active Powered Speakers + 18\" Subwoofer", 2, "IN_HOUSE_STOCK", 5000.0, 25000.0, "Company Central Warehouse"));
        items.add(new PackageItemDetail("Lighting", "LED Par Cans (Warm White + RGBW Ambience Wash)", 16, "IN_HOUSE_STOCK", 3000.0, 20000.0, "Company Lighting Pool"));
        items.add(new PackageItemDetail("Video Wall", "P3 LED Screen Backdrop (12ft x 8ft)", 1, "VENDOR_CROSS_HIRE", 24000.0, 52000.0, "Partner: Apex Visuals"));
        items.add(new PackageItemDetail("Truss & Stage", "Goalpost Heavy Aluminum Lighting Truss (20ft)", 1, "IN_HOUSE_STOCK", 4000.0, 18000.0, "Truss Yard"));
        items.add(new PackageItemDetail("Microphones", "Sennheiser Wireless Vocal & Collar Mic Set", 4, "IN_HOUSE_STOCK", 1200.0, 10000.0, "Media Center"));
        items.add(new PackageItemDetail("Crew & Ops", "Sound Technician + Lighting Assistant + 2 Crew", 4, "SUBCONTRACT_CREW", 10000.0, 22000.0, "Direct Crew Payroll"));
        items.add(new PackageItemDetail("Logistics", "Single Commercial Mini-Truck Transport", 1, "SUBCONTRACT_CREW", 4500.0, 10000.0, "City Movers"));

        double directCost = items.stream().mapToDouble(PackageItemDetail::getInternalCost).sum();
        double billedTotal = items.stream().mapToDouble(PackageItemDetail::getClientBillingRate).sum();
        double finalQuote = Math.max(clientQuote, billedTotal);

        pkg.setClientQuotePrice(finalQuote);
        pkg.setInternalDirectCost(directCost);
        pkg.setGrossProfit(finalQuote - directCost);
        pkg.setProfitMarginPct(((finalQuote - directCost) / finalQuote) * 100.0);
        pkg.setInHouseItemsUsed(23);
        pkg.setVendorCrossHiredItems(1);
        pkg.setItemizedList(items);

        return pkg;
    }

    private EventQuotationPackage buildSilverPackage(String eventType, double budget, int guests) {
        EventQuotationPackage pkg = new EventQuotationPackage();
        pkg.setPackageTier("SILVER_VALUE");
        pkg.setPackageName("Silver Pure In-House Value Package (" + eventType + ")");
        pkg.setClientTargetSuitability("Maximum Profit Margin: 100% company in-house inventory, zero vendor cross-hiring bleed");
        pkg.setSetupDurationHours(3);
        pkg.setCrewTechniciansNeeded(2);
        pkg.setSecurityDepositRequired(15000.0);

        double clientQuote = budget * 0.45; // e.g. ₹1,35,000 on ₹3L budget
        List<PackageItemDetail> items = new ArrayList<>();

        items.add(new PackageItemDetail("Audio/Sound", "Compact Dual Front-of-House JBL Audio System", 2, "IN_HOUSE_STOCK", 3000.0, 18000.0, "Company Central Warehouse"));
        items.add(new PackageItemDetail("Lighting", "Warm White Stage Spotlights + 8 LED RGB Par Cans", 10, "IN_HOUSE_STOCK", 2000.0, 14000.0, "Company Lighting Pool"));
        items.add(new PackageItemDetail("Projection", "4K Ultra-Lumen Laser Projector + 120\" Motorized Screen", 1, "IN_HOUSE_STOCK", 2500.0, 18000.0, "Company AV Pool"));
        items.add(new PackageItemDetail("Stage & Stands", "Heavy Base T-Bar Lighting Stands & Stage Skirting", 4, "IN_HOUSE_STOCK", 1000.0, 8000.0, "Warehouse"));
        items.add(new PackageItemDetail("Crew & Ops", "Multi-Skilled AV Technician & Rigger", 2, "SUBCONTRACT_CREW", 6000.0, 14000.0, "Direct Crew Payroll"));
        items.add(new PackageItemDetail("Logistics", "Local Warehouse Van Movement", 1, "IN_HOUSE_STOCK", 2500.0, 6000.0, "Company Van"));

        double directCost = items.stream().mapToDouble(PackageItemDetail::getInternalCost).sum();
        double billedTotal = items.stream().mapToDouble(PackageItemDetail::getClientBillingRate).sum();
        double finalQuote = Math.max(clientQuote, billedTotal);

        pkg.setClientQuotePrice(finalQuote);
        pkg.setInternalDirectCost(directCost);
        pkg.setGrossProfit(finalQuote - directCost);
        pkg.setProfitMarginPct(((finalQuote - directCost) / finalQuote) * 100.0);
        pkg.setInHouseItemsUsed(19);
        pkg.setVendorCrossHiredItems(0); // 100% in-house!
        pkg.setItemizedList(items);

        return pkg;
    }
}
