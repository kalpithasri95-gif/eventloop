package com.eventloop.model;

import com.eventloop.model.resources.AbstractResource;

/**
 * Model representing the result of matching an event requirement
 * against the existing resource inventory.
 */
public class CompatibilityMatch {
    private AbstractResource resource;
    private RequirementModel requirement;
    private String matchType; // EXACT_MATCH, COMPATIBLE_MATCH, PARTIAL_MATCH, REPAIR_REQUIRED, NOT_AVAILABLE, VERIFICATION_REQUIRED, RESERVATION_CONFLICT
    private int compatibilityScore; // 0 to 100
    private String explanation;
    private boolean reservationConflict;
    private boolean verificationRequired;

    public CompatibilityMatch() {}

    public CompatibilityMatch(AbstractResource resource, RequirementModel requirement, 
                              String matchType, int compatibilityScore, String explanation) {
        this.resource = resource;
        this.requirement = requirement;
        this.matchType = matchType;
        this.compatibilityScore = compatibilityScore;
        this.explanation = explanation;
    }

    public AbstractResource getResource() { return resource; }
    public void setResource(AbstractResource resource) { this.resource = resource; }

    public RequirementModel getRequirement() { return requirement; }
    public void setRequirement(RequirementModel requirement) { this.requirement = requirement; }

    public String getMatchType() { return matchType; }
    public void setMatchType(String matchType) { this.matchType = matchType; }

    public int getCompatibilityScore() { return compatibilityScore; }
    public void setCompatibilityScore(int compatibilityScore) { this.compatibilityScore = compatibilityScore; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public boolean isReservationConflict() { return reservationConflict; }
    public void setReservationConflict(boolean reservationConflict) { this.reservationConflict = reservationConflict; }

    public boolean isVerificationRequired() { return verificationRequired; }
    public void setVerificationRequired(boolean verificationRequired) { this.verificationRequired = verificationRequired; }
}
