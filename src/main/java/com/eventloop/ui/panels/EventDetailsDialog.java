package com.eventloop.ui.panels;

import com.eventloop.dao.ReservationDAO;
import com.eventloop.model.EventModel;
import com.eventloop.model.RequirementModel;
import com.eventloop.model.ReservationModel;
import com.eventloop.service.EventService;
import com.eventloop.ui.components.ModernUIUtils;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class EventDetailsDialog extends JDialog {
    private final EventService eventService = EventService.getInstance();
    private final ReservationDAO reservationDAO = new ReservationDAO();

    public EventDetailsDialog(Frame owner, EventModel e) {
        super(owner, "Event Profile & Resource Manifest: " + e.getEventId(), true);
        setSize(780, 620);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(15, 15));
        getContentPane().setBackground(ModernUIUtils.COLOR_BG);

        JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setOpaque(false);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        // Header Panel with Avoidance Callout
        JPanel headerCard = ModernUIUtils.createCard();
        headerCard.setLayout(new BorderLayout(10, 10));

        JPanel titleInfo = new JPanel(new BorderLayout(4, 4));
        titleInfo.setOpaque(false);
        JLabel lblName = new JLabel(e.getEventName());
        lblName.setFont(ModernUIUtils.FONT_TITLE);
        lblName.setForeground(ModernUIUtils.COLOR_PRIMARY_DARK);

        JLabel lblMeta = new JLabel("ID: " + e.getEventId() + "  |  Date: " + e.getEventDate() + " (" + e.getStartTime() + " - " + e.getEndTime() + ")  |  Venue: " + e.getLocation() + "  |  Status: " + e.getEventStatus());
        lblMeta.setFont(ModernUIUtils.FONT_REGULAR);
        lblMeta.setForeground(ModernUIUtils.COLOR_TEXT_MUTED);

        titleInfo.add(lblName, BorderLayout.NORTH);
        titleInfo.add(lblMeta, BorderLayout.SOUTH);

        JPanel avoidanceBadge = new JPanel(new BorderLayout(2, 2));
        avoidanceBadge.setOpaque(false);
        JLabel lblAvTitle = new JLabel("Potential Purchase Avoided:");
        lblAvTitle.setFont(ModernUIUtils.FONT_SMALL);
        lblAvTitle.setForeground(ModernUIUtils.COLOR_TEXT_MUTED);
        JLabel lblAvVal = new JLabel("₹" + String.format("%,.2f", e.getPotentialPurchaseAvoided()));
        lblAvVal.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblAvVal.setForeground(ModernUIUtils.COLOR_SUCCESS);
        avoidanceBadge.add(lblAvTitle, BorderLayout.NORTH);
        avoidanceBadge.add(lblAvVal, BorderLayout.SOUTH);

        headerCard.add(titleInfo, BorderLayout.CENTER);
        headerCard.add(avoidanceBadge, BorderLayout.EAST);

        // Requirements Table
        DefaultTableModel reqModel = new DefaultTableModel(new String[]{"ID", "Category", "Resource Required", "Qty", "Min Cond.", "Environment", "Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tblReq = new JTable(reqModel);
        ModernUIUtils.styleTable(tblReq);
        tblReq.getColumnModel().getColumn(6).setCellRenderer(ModernUIUtils.getStatusBadgeRenderer());

        try {
            List<RequirementModel> reqs = eventService.getRequirementsForEvent(e.getEventId());
            for (RequirementModel rm : reqs) {
                reqModel.addRow(new Object[]{
                        rm.getRequirementId(),
                        rm.getCategory(),
                        rm.getResourceName(),
                        rm.getQuantity(),
                        rm.getRequiredCondition() + "/5",
                        rm.getIndoorOutdoor(),
                        rm.getStatus()
                });
            }
        } catch (SQLException ex) { ex.printStackTrace(); }

        JPanel reqCard = ModernUIUtils.createCard();
        reqCard.setLayout(new BorderLayout(5, 5));
        JLabel lblReqHeading = new JLabel("Resource Requirements Specification");
        lblReqHeading.setFont(ModernUIUtils.FONT_HEADER);
        lblReqHeading.setForeground(ModernUIUtils.COLOR_PRIMARY);
        reqCard.add(lblReqHeading, BorderLayout.NORTH);
        reqCard.add(new JScrollPane(tblReq), BorderLayout.CENTER);

        // Reservations Table
        DefaultTableModel resModel = new DefaultTableModel(new String[]{"Res ID", "Resource ID", "Resource Name", "Qty", "Time Window", "Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tblRes = new JTable(resModel);
        ModernUIUtils.styleTable(tblRes);
        tblRes.getColumnModel().getColumn(5).setCellRenderer(ModernUIUtils.getStatusBadgeRenderer());

        try {
            List<ReservationModel> resList = reservationDAO.getReservationsByEvent(e.getEventId());
            for (ReservationModel rm : resList) {
                resModel.addRow(new Object[]{
                        rm.getReservationId(),
                        rm.getResourceId(),
                        rm.getResourceName(),
                        rm.getQuantity(),
                        rm.getStartDateTime() + " to " + rm.getEndDateTime(),
                        rm.getReservationStatus()
                });
            }
        } catch (SQLException ex) { ex.printStackTrace(); }

        JPanel resCard = ModernUIUtils.createCard();
        resCard.setLayout(new BorderLayout(5, 5));
        JLabel lblResHeading = new JLabel("Allocated & Reserved Campus Resources");
        lblResHeading.setFont(ModernUIUtils.FONT_HEADER);
        lblResHeading.setForeground(ModernUIUtils.COLOR_PRIMARY);
        resCard.add(lblResHeading, BorderLayout.NORTH);
        resCard.add(new JScrollPane(tblRes), BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, reqCard, resCard);
        splitPane.setResizeWeight(0.5);
        splitPane.setDividerSize(6);
        splitPane.setOpaque(false);
        splitPane.setBorder(null);

        // Close Button
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(false);
        JButton btnClose = ModernUIUtils.createPrimaryButton("Close");
        btnClose.addActionListener(evt -> dispose());
        btnPanel.add(btnClose);

        mainPanel.add(headerCard, BorderLayout.NORTH);
        mainPanel.add(splitPane, BorderLayout.CENTER);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }
}
