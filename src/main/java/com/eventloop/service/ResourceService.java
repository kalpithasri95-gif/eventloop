package com.eventloop.service;

import com.eventloop.dao.HistoryDAO;
import com.eventloop.dao.ResourceDAO;
import com.eventloop.exception.DuplicateResourceException;
import com.eventloop.exception.ValidationException;
import com.eventloop.model.resources.AbstractResource;
import com.eventloop.util.DateUtil;
import com.eventloop.util.ValidationUtil;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ResourceService {
    private static ResourceService instance;
    private final ResourceDAO resourceDAO;
    private final HistoryDAO historyDAO;

    private ResourceService() {
        this.resourceDAO = new ResourceDAO();
        this.historyDAO = new HistoryDAO();
    }

    public static synchronized ResourceService getInstance() {
        if (instance == null) {
            instance = new ResourceService();
        }
        return instance;
    }

    public boolean addResource(AbstractResource r, String performedBy) 
            throws ValidationException, DuplicateResourceException, SQLException {
        ValidationUtil.requireNonEmpty(r.getResourceId(), "Resource ID");
        ValidationUtil.requireNonEmpty(r.getResourceName(), "Resource Name");
        ValidationUtil.requireNonEmpty(r.getCategory(), "Category");
        ValidationUtil.validatePositive(r.getQuantity(), "Quantity");
        ValidationUtil.validateNonNegative(r.getPurchaseCost(), "Purchase Cost");
        ValidationUtil.validateConditionRating(r.getConditionRating());
        ValidationUtil.validateResourceIdFormat(r.getResourceId());

        if (resourceDAO.exists(r.getResourceId())) {
            throw new DuplicateResourceException("Resource with ID '" + r.getResourceId() + "' already exists.");
        }

        r.setAvailableQuantity(r.getQuantity());
        boolean ok = resourceDAO.insertResource(r);
        if (ok) {
            historyDAO.addHistory(r.getResourceId(), "CREATED", performedBy, 
                    "Resource created with initial quantity: " + r.getQuantity() + " " + r.getUnit());
        }
        return ok;
    }

    public boolean updateResource(AbstractResource r, String performedBy) 
            throws ValidationException, SQLException {
        ValidationUtil.requireNonEmpty(r.getResourceId(), "Resource ID");
        ValidationUtil.requireNonEmpty(r.getResourceName(), "Resource Name");
        ValidationUtil.validatePositive(r.getQuantity(), "Quantity");
        ValidationUtil.validateNonNegative(r.getPurchaseCost(), "Purchase Cost");
        ValidationUtil.validateConditionRating(r.getConditionRating());

        boolean ok = resourceDAO.updateResource(r);
        if (ok) {
            historyDAO.addHistory(r.getResourceId(), "UPDATED", performedBy, 
                    "Resource specifications and details updated.");
        }
        return ok;
    }

    public boolean deleteResource(String resourceId, String performedBy) throws SQLException {
        boolean ok = resourceDAO.deleteResource(resourceId);
        if (ok) {
            historyDAO.addHistory(resourceId, "DELETED", performedBy, "Resource permanently removed from database.");
        }
        return ok;
    }

    public boolean verifyResource(String resourceId, String verifiedBy, String notes, String nextDate) throws SQLException {
        String today = DateUtil.today();
        String nextVerify = (nextDate != null && !nextDate.trim().isEmpty()) ? nextDate.trim() : LocalDate.now().plusMonths(3).toString();
        
        boolean ok = resourceDAO.updateVerification(resourceId, "VERIFIED", today, nextVerify, verifiedBy, notes);
        if (ok) {
            historyDAO.addHistory(resourceId, "VERIFIED", verifiedBy, 
                    "Resource verified. Next verification scheduled for: " + nextVerify + ". Notes: " + notes);
        }
        return ok;
    }

    public boolean markUnderRepair(String resourceId, String performedBy, String reason, double estRepairCost) throws SQLException {
        AbstractResource r = resourceDAO.getById(resourceId);
        if (r != null) {
            r.setCurrentStatus("UNDER_REPAIR");
            r.setEstimatedRepairCost(estRepairCost);
            resourceDAO.updateResource(r);
            historyDAO.addHistory(resourceId, "MARKED_UNDER_REPAIR", performedBy, 
                    "Resource sent for repair. Reason: " + reason + " (Est. Cost: ₹" + estRepairCost + ")");
            return true;
        }
        return false;
    }

    public boolean markAvailable(String resourceId, String performedBy, int conditionRating) throws SQLException {
        AbstractResource r = resourceDAO.getById(resourceId);
        if (r != null) {
            r.setCurrentStatus("AVAILABLE");
            r.setConditionRating(conditionRating);
            resourceDAO.updateResource(r);
            historyDAO.addHistory(resourceId, "MARKED_AVAILABLE", performedBy, 
                    "Resource condition restored to " + conditionRating + "/5 and marked AVAILABLE.");
            return true;
        }
        return false;
    }

    public boolean markMissing(String resourceId, String performedBy, String details) throws SQLException {
        AbstractResource r = resourceDAO.getById(resourceId);
        if (r != null) {
            r.setCurrentStatus("MISSING");
            resourceDAO.updateResource(r);
            historyDAO.addHistory(resourceId, "MARKED_MISSING", performedBy, 
                    "Resource marked MISSING. Details: " + details);
            return true;
        }
        return false;
    }

    public boolean retireResource(String resourceId, String performedBy, String reason) throws SQLException {
        AbstractResource r = resourceDAO.getById(resourceId);
        if (r != null) {
            r.setCurrentStatus("RETIRED");
            r.setAvailableQuantity(0);
            resourceDAO.updateResource(r);
            historyDAO.addHistory(resourceId, "RETIRED", performedBy, 
                    "Resource retired from active pool. Reason: " + reason);
            return true;
        }
        return false;
    }

    public boolean repurposeResource(String resourceId, String performedBy, String newPurpose) throws SQLException {
        AbstractResource r = resourceDAO.getById(resourceId);
        if (r != null) {
            r.setCurrentStatus("REPURPOSE_REQUIRED");
            r.setReuseType("REPURPOSE");
            r.setNotes(newPurpose);
            resourceDAO.updateResource(r);
            historyDAO.addHistory(resourceId, "REPURPOSED", performedBy, 
                    "Resource classified for repurposing: " + newPurpose);
            return true;
        }
        return false;
    }

    public List<AbstractResource> getAllResources() throws SQLException {
        return resourceDAO.getAllResources();
    }

    public List<AbstractResource> searchResources(String keyword, String category, String status, String verification) throws SQLException {
        return resourceDAO.searchResources(keyword, category, status, verification);
    }

    public AbstractResource getById(String resourceId) throws SQLException {
        return resourceDAO.getById(resourceId);
    }
}
