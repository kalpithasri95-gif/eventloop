package com.eventloop.service;

import com.eventloop.dao.ReservationDAO;
import com.eventloop.dao.ResourceDAO;
import com.eventloop.model.CompatibilityMatch;
import com.eventloop.model.RequirementModel;
import com.eventloop.model.ReservationModel;
import com.eventloop.model.resources.AbstractResource;
import com.eventloop.util.DateUtil;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MatchingService {
    private static MatchingService instance;
    private final ResourceDAO resourceDAO;
    private final ReservationDAO reservationDAO;

    private MatchingService() {
        this.resourceDAO = new ResourceDAO();
        this.reservationDAO = new ReservationDAO();
    }

    public static synchronized MatchingService getInstance() {
        if (instance == null) {
            instance = new MatchingService();
        }
        return instance;
    }

    public List<CompatibilityMatch> findMatches(RequirementModel req) throws SQLException {
        List<CompatibilityMatch> matches = new ArrayList<>();
        List<AbstractResource> allResources = resourceDAO.getAllResources();

        String reqStart = req.getRequiredDate() + " " + (req.getStartTime() != null ? req.getStartTime() : "00:00");
        String reqEnd = req.getRequiredDate() + " " + (req.getEndTime() != null ? req.getEndTime() : "23:59");

        for (AbstractResource r : allResources) {
            int score = 0;
            StringBuilder explanation = new StringBuilder();

            // 1. Category check
            boolean categoryMatch = r.getCategory().equalsIgnoreCase(req.getCategory());
            if (categoryMatch) {
                score += 30;
                explanation.append("Category match (").append(r.getCategory()).append("). ");
            } else {
                // Cross-category compatibility check (e.g. Electrical / AudioVisual)
                if ((req.getCategory().equalsIgnoreCase("Audio/Visual") && r.getCategory().equalsIgnoreCase("Electrical")) ||
                    (req.getCategory().equalsIgnoreCase("Equipment") && r.getCategory().equalsIgnoreCase("Furniture"))) {
                    score += 15;
                    explanation.append("Compatible category cross-match. ");
                } else {
                    continue; // Skip irrelevant categories
                }
            }

            // 2. Name similarity
            String reqName = req.getResourceName().toLowerCase();
            String resName = r.getResourceName().toLowerCase();
            boolean exactName = resName.contains(reqName) || reqName.contains(resName);
            if (exactName) {
                score += 25;
                explanation.append("Title alignment: '").append(r.getResourceName()).append("'. ");
            } else {
                score += 10;
                explanation.append("Partial title match. ");
            }

            // 3. Technical specifications check
            if (req.getSpecifications() != null && !req.getSpecifications().trim().isEmpty() &&
                r.getSpecifications() != null && !r.getSpecifications().trim().isEmpty()) {
                String[] reqWords = req.getSpecifications().toLowerCase().split("[,\\s]+");
                int specHits = 0;
                for (String word : reqWords) {
                    if (word.length() > 2 && r.getSpecifications().toLowerCase().contains(word)) {
                        specHits++;
                    }
                }
                if (specHits > 0) {
                    score += 15;
                    explanation.append("Technical specifications match (").append(specHits).append(" tags). ");
                }
            } else {
                score += 10;
            }

            // 4. Indoor / Outdoor compatibility
            if (req.getIndoorOutdoor() != null && r.getSpecifications() != null) {
                if (r.getSpecifications().toLowerCase().contains("outdoor") || req.getIndoorOutdoor().equalsIgnoreCase("Indoor")) {
                    score += 10;
                }
            } else {
                score += 10;
            }

            // 5. Condition evaluation
            if (r.getConditionRating() >= req.getRequiredCondition()) {
                score += 10;
                explanation.append("Condition rating ").append(r.getConditionRating()).append("/5 meets requirement (>= ").append(req.getRequiredCondition()).append("). ");
            } else {
                explanation.append("Condition rating ").append(r.getConditionRating()).append("/5 is below desired (").append(req.getRequiredCondition()).append("/5). ");
            }

            // 6. Reservation conflict check
            List<ReservationModel> conflicts = reservationDAO.findOverlappingReservations(r.getResourceId(), reqStart, reqEnd);
            boolean hasConflict = !conflicts.isEmpty();
            int reservedInPeriod = 0;
            for (ReservationModel c : conflicts) {
                reservedInPeriod += c.getQuantity();
            }

            // 7. Verification check
            boolean isExpired = DateUtil.isExpired(r.getNextVerificationDate()) || "EXPIRED".equalsIgnoreCase(r.getVerificationStatus());
            boolean isVerificationReq = "VERIFICATION_REQUIRED".equalsIgnoreCase(r.getVerificationStatus()) || isExpired;

            // Determine Match Type
            String matchType;
            if ("RETIRED".equalsIgnoreCase(r.getCurrentStatus()) || "MISSING".equalsIgnoreCase(r.getCurrentStatus())) {
                matchType = "NOT_AVAILABLE";
                score = Math.min(score, 20);
                explanation.append("Resource is marked ").append(r.getCurrentStatus()).append(" and cannot be reserved. ");
            } else if (isVerificationReq) {
                matchType = "VERIFICATION_REQUIRED";
                score = Math.min(score, 45);
                explanation.append("Verification Required: Verification expired or pending admin re-verification. Blocked until verified. ");
            } else if ("UNDER_REPAIR".equalsIgnoreCase(r.getCurrentStatus()) || r.getConditionRating() <= 2) {
                matchType = "REPAIR_REQUIRED";
                score = Math.min(score, 50);
                explanation.append("Repair Required: Estimated repair cost ₹").append(r.getEstimatedRepairCost()).append(". Can be reused after servicing. ");
            } else if (hasConflict && (r.getAvailableQuantity() - reservedInPeriod) < req.getQuantity()) {
                matchType = "RESERVATION_CONFLICT";
                score = Math.min(score, 55);
                explanation.append("Reservation Conflict: Already reserved for another event during ").append(reqStart).append(" - ").append(reqEnd).append(". ");
            } else if (r.getAvailableQuantity() >= req.getQuantity() && score >= 75) {
                matchType = "EXACT_MATCH";
                score = Math.min(score + 10, 100);
                explanation.append("Sufficient available inventory (").append(r.getAvailableQuantity()).append(" ").append(r.getUnit()).append("). Verified and ready.");
            } else if (r.getAvailableQuantity() >= req.getQuantity()) {
                matchType = "COMPATIBLE_MATCH";
                explanation.append("Available inventory (").append(r.getAvailableQuantity()).append(" ").append(r.getUnit()).append("). Compatible alternative.");
            } else if (r.getAvailableQuantity() > 0) {
                matchType = "PARTIAL_MATCH";
                explanation.append("Partial inventory: ").append(r.getAvailableQuantity()).append(" of ").append(req.getQuantity()).append(" ").append(r.getUnit()).append(" available.");
            } else {
                matchType = "NOT_AVAILABLE";
                explanation.append("All units currently allocated or in use.");
            }

            CompatibilityMatch cm = new CompatibilityMatch(r, req, matchType, score, explanation.toString());
            cm.setReservationConflict(hasConflict);
            cm.setVerificationRequired(isVerificationReq);
            matches.add(cm);
        }

        // Sort matches descending by compatibility score
        matches.sort((a, b) -> Integer.compare(b.getCompatibilityScore(), a.getCompatibilityScore()));
        return matches;
    }
}
