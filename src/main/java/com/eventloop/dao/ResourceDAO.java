package com.eventloop.dao;

import com.eventloop.model.resources.AbstractResource;
import com.eventloop.model.resources.ResourceFactory;
import com.eventloop.util.DateUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ResourceDAO {

    public boolean insertResource(AbstractResource r) throws SQLException {
        String sql = "INSERT INTO resources (" +
                "resource_id, resource_name, category, description, quantity, available_quantity, unit, " +
                "condition_rating, current_status, verification_status, last_verified_date, next_verification_date, " +
                "verified_by, verification_notes, storage_building, storage_room, rack_number, shelf_number, box_number, " +
                "current_holder, purchase_cost, estimated_repair_cost, actual_repair_cost, reuse_type, specifications, notes, created_date, last_updated_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            setResourcePreparedStatement(ps, r);
            ps.setString(27, DateUtil.today());
            ps.setString(28, DateUtil.today());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateResource(AbstractResource r) throws SQLException {
        String sql = "UPDATE resources SET " +
                "resource_name = ?, category = ?, description = ?, quantity = ?, available_quantity = ?, unit = ?, " +
                "condition_rating = ?, current_status = ?, verification_status = ?, last_verified_date = ?, next_verification_date = ?, " +
                "verified_by = ?, verification_notes = ?, storage_building = ?, storage_room = ?, rack_number = ?, shelf_number = ?, box_number = ?, " +
                "current_holder = ?, purchase_cost = ?, estimated_repair_cost = ?, actual_repair_cost = ?, reuse_type = ?, specifications = ?, notes = ?, last_updated_date = ? " +
                "WHERE resource_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, r.getResourceName());
            ps.setString(2, r.getCategory());
            ps.setString(3, r.getDescription());
            ps.setInt(4, r.getQuantity());
            ps.setInt(5, r.getAvailableQuantity());
            ps.setString(6, r.getUnit());
            ps.setInt(7, r.getConditionRating());
            ps.setString(8, r.getCurrentStatus());
            ps.setString(9, r.getVerificationStatus());
            ps.setString(10, r.getLastVerifiedDate());
            ps.setString(11, r.getNextVerificationDate());
            ps.setString(12, r.getVerifiedBy());
            ps.setString(13, r.getVerificationNotes());
            ps.setString(14, r.getStorageBuilding());
            ps.setString(15, r.getStorageRoom());
            ps.setString(16, r.getRackNumber());
            ps.setString(17, r.getShelfNumber());
            ps.setString(18, r.getBoxNumber());
            ps.setString(19, r.getCurrentHolder());
            ps.setDouble(20, r.getPurchaseCost());
            ps.setDouble(21, r.getEstimatedRepairCost());
            ps.setDouble(22, r.getActualRepairCost());
            ps.setString(23, r.getReuseType());
            ps.setString(24, r.getSpecifications());
            ps.setString(25, r.getNotes());
            ps.setString(26, DateUtil.today());
            ps.setString(27, r.getResourceId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteResource(String resourceId) throws SQLException {
        String sql = "DELETE FROM resources WHERE resource_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, resourceId);
            return ps.executeUpdate() > 0;
        }
    }

    public AbstractResource getById(String resourceId) throws SQLException {
        String sql = "SELECT * FROM resources WHERE resource_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, resourceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResource(rs);
                }
            }
        }
        return null;
    }

    public boolean exists(String resourceId) throws SQLException {
        return getById(resourceId) != null;
    }

    public List<AbstractResource> getAllResources() throws SQLException {
        List<AbstractResource> list = new ArrayList<>();
        String sql = "SELECT * FROM resources ORDER BY resource_id";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResource(rs));
            }
        }
        return list;
    }

    public List<AbstractResource> searchResources(String keyword, String category, String status, String verification) throws SQLException {
        List<AbstractResource> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM resources WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (resource_id LIKE ? OR resource_name LIKE ? OR description LIKE ? OR specifications LIKE ?) ");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }

        if (category != null && !category.equalsIgnoreCase("All Categories") && !category.trim().isEmpty()) {
            sql.append("AND category = ? ");
            params.add(category);
        }

        if (status != null && !status.equalsIgnoreCase("All Statuses") && !status.trim().isEmpty()) {
            sql.append("AND current_status = ? ");
            params.add(status);
        }

        if (verification != null && !verification.equalsIgnoreCase("All") && !verification.trim().isEmpty()) {
            sql.append("AND verification_status = ? ");
            params.add(verification);
        }

        sql.append("ORDER BY resource_id");

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResource(rs));
                }
            }
        }
        return list;
    }

    public boolean updateAvailableQuantity(String resourceId, int newAvailQty) throws SQLException {
        String sql = "UPDATE resources SET available_quantity = ?, last_updated_date = ? WHERE resource_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newAvailQty);
            ps.setString(2, DateUtil.today());
            ps.setString(3, resourceId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateVerification(String resourceId, String status, String lastDate, String nextDate, String verifiedBy, String notes) throws SQLException {
        String sql = "UPDATE resources SET verification_status = ?, last_verified_date = ?, next_verification_date = ?, " +
                "verified_by = ?, verification_notes = ?, last_updated_date = ? WHERE resource_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, lastDate);
            ps.setString(3, nextDate);
            ps.setString(4, verifiedBy);
            ps.setString(5, notes);
            ps.setString(6, DateUtil.today());
            ps.setString(7, resourceId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateConditionAndStatus(String resourceId, int condition, String status) throws SQLException {
        String sql = "UPDATE resources SET condition_rating = ?, current_status = ?, last_updated_date = ? WHERE resource_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, condition);
            ps.setString(2, status);
            ps.setString(3, DateUtil.today());
            ps.setString(4, resourceId);
            return ps.executeUpdate() > 0;
        }
    }

    private void setResourcePreparedStatement(PreparedStatement ps, AbstractResource r) throws SQLException {
        ps.setString(1, r.getResourceId());
        ps.setString(2, r.getResourceName());
        ps.setString(3, r.getCategory());
        ps.setString(4, r.getDescription());
        ps.setInt(5, r.getQuantity());
        ps.setInt(6, r.getAvailableQuantity());
        ps.setString(7, r.getUnit());
        ps.setInt(8, r.getConditionRating());
        ps.setString(9, r.getCurrentStatus());
        ps.setString(10, r.getVerificationStatus());
        ps.setString(11, r.getLastVerifiedDate());
        ps.setString(12, r.getNextVerificationDate());
        ps.setString(13, r.getVerifiedBy());
        ps.setString(14, r.getVerificationNotes());
        ps.setString(15, r.getStorageBuilding());
        ps.setString(16, r.getStorageRoom());
        ps.setString(17, r.getRackNumber());
        ps.setString(18, r.getShelfNumber());
        ps.setString(19, r.getBoxNumber());
        ps.setString(20, r.getCurrentHolder());
        ps.setDouble(21, r.getPurchaseCost());
        ps.setDouble(22, r.getEstimatedRepairCost());
        ps.setDouble(23, r.getActualRepairCost());
        ps.setString(24, r.getReuseType());
        ps.setString(25, r.getSpecifications());
        ps.setString(26, r.getNotes());
    }

    public AbstractResource mapResource(ResultSet rs) throws SQLException {
        String category = rs.getString("category");
        String name = rs.getString("resource_name");

        // Runtime Polymorphism: Instantiate specific concrete subtype via Factory
        AbstractResource r = ResourceFactory.createResource(category, name);

        r.setResourceId(rs.getString("resource_id"));
        r.setResourceName(name);
        r.setCategory(category);
        r.setDescription(rs.getString("description"));
        r.setQuantity(rs.getInt("quantity"));
        r.setAvailableQuantity(rs.getInt("available_quantity"));
        r.setUnit(rs.getString("unit"));
        r.setConditionRating(rs.getInt("condition_rating"));
        r.setCurrentStatus(rs.getString("current_status"));
        r.setVerificationStatus(rs.getString("verification_status"));
        r.setLastVerifiedDate(rs.getString("last_verified_date"));
        r.setNextVerificationDate(rs.getString("next_verification_date"));
        r.setVerifiedBy(rs.getString("verified_by"));
        r.setVerificationNotes(rs.getString("verification_notes"));
        r.setStorageBuilding(rs.getString("storage_building"));
        r.setStorageRoom(rs.getString("storage_room"));
        r.setRackNumber(rs.getString("rack_number"));
        r.setShelfNumber(rs.getString("shelf_number"));
        r.setBoxNumber(rs.getString("box_number"));
        r.setCurrentHolder(rs.getString("current_holder"));
        r.setPurchaseCost(rs.getDouble("purchase_cost"));
        r.setEstimatedRepairCost(rs.getDouble("estimated_repair_cost"));
        r.setActualRepairCost(rs.getDouble("actual_repair_cost"));
        r.setReuseType(rs.getString("reuse_type"));
        r.setSpecifications(rs.getString("specifications"));
        r.setNotes(rs.getString("notes"));
        r.setCreatedDate(rs.getString("created_date"));
        r.setLastUpdatedDate(rs.getString("last_updated_date"));

        // If next verification date has passed, automatically evaluate verification status
        if ("VERIFIED".equalsIgnoreCase(r.getVerificationStatus()) && DateUtil.isExpired(r.getNextVerificationDate())) {
            r.setVerificationStatus("EXPIRED");
        }

        return r;
    }
}
