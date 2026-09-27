package com.eventloop.service;

import com.eventloop.dao.HistoryDAO;
import com.eventloop.dao.InspectionDAO;
import com.eventloop.dao.ResourceDAO;
import com.eventloop.model.InspectionModel;
import com.eventloop.model.resources.AbstractResource;
import com.eventloop.util.DateUtil;
import java.sql.SQLException;
import java.util.List;

public class InspectionService {
    private static InspectionService instance;
    private final InspectionDAO inspectionDAO;
    private final ResourceDAO resourceDAO;
    private final HistoryDAO historyDAO;

    private InspectionService() {
        this.inspectionDAO = new InspectionDAO();
        this.resourceDAO = new ResourceDAO();
        this.historyDAO = new HistoryDAO();
    }

    public static synchronized InspectionService getInstance() {
        if (instance == null) {
            instance = new InspectionService();
        }
        return instance;
    }

    public boolean recordPreUseInspection(InspectionModel ins) throws SQLException {
        ins.setInspectionType("PRE_USE");
        ins.setInspectionDate(DateUtil.now());
        boolean ok = inspectionDAO.insertInspection(ins);
        if (ok) {
            historyDAO.addHistory(ins.getResourceId(), "PRE_USE_INSPECTION", ins.getInspectorName(),
                    "Pre-use inspection passed with condition " + ins.getConditionRating() + "/5. Notes: " + ins.getNotes());
        }
        return ok;
    }

    public boolean recordPostUseInspection(InspectionModel ins) throws SQLException {
        ins.setInspectionType("POST_USE");
        ins.setInspectionDate(DateUtil.now());

        // Determine outcome status based on inspection details
        String outcome;
        if (ins.getMissingQuantity() > 0 && ins.getQuantityReturned() == 0) {
            outcome = "MISSING";
        } else if (ins.getConditionRating() <= 1) {
            outcome = "RETIRED";
        } else if (ins.getConditionRating() <= 2 || ins.isRepairRequired()) {
            outcome = "UNDER_REPAIR";
        } else if ("REPURPOSE".equalsIgnoreCase(ins.getNotes()) || ins.isCleaningRequired()) {
            outcome = "REPURPOSE_REQUIRED";
        } else {
            outcome = "AVAILABLE";
        }
        ins.setOutcomeStatus(outcome);

        boolean ok = inspectionDAO.insertInspection(ins);
        if (ok) {
            // Update resource table
            AbstractResource r = resourceDAO.getById(ins.getResourceId());
            if (r != null) {
                r.setConditionRating(ins.getConditionRating());
                r.setCurrentStatus(outcome);
                if (ins.isRepairRequired()) {
                    r.setEstimatedRepairCost(ins.getConditionRating() <= 2 ? 1500.0 : 500.0);
                }
                resourceDAO.updateResource(r);
            }

            historyDAO.addHistory(ins.getResourceId(), "POST_USE_INSPECTION", ins.getInspectorName(),
                    "Post-use inspection complete. Outcome: " + outcome + ", Condition: " + ins.getConditionRating() + 
                    "/5, Returned: " + ins.getQuantityReturned() + ", Missing: " + ins.getMissingQuantity());
        }
        return ok;
    }

    public List<InspectionModel> getInspectionsForEvent(String eventId) throws SQLException {
        return inspectionDAO.getInspectionsByEvent(eventId);
    }

    public List<InspectionModel> getInspectionsForResource(String resourceId) throws SQLException {
        return inspectionDAO.getInspectionsByResource(resourceId);
    }
}
