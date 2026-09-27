package com.eventloop.ui.panels;

import com.eventloop.dao.HistoryDAO;
import com.eventloop.model.HistoryModel;
import com.eventloop.ui.components.ModernUIUtils;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.sql.SQLException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class ResourceHistoryDialog extends JDialog {
    private final HistoryDAO historyDAO = new HistoryDAO();

    public ResourceHistoryDialog(Frame owner, String resourceId, String resourceName) {
        super(owner, "Audit History Timeline: " + resourceId, true);
        setSize(720, 520);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(12, 12));
        getContentPane().setBackground(ModernUIUtils.COLOR_BG);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setOpaque(false);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        // Header
        JPanel headerCard = ModernUIUtils.createCard();
        headerCard.setLayout(new BorderLayout(4, 4));

        JLabel lblTitle = new JLabel("Audit & Movement Log: " + resourceName);
        lblTitle.setFont(ModernUIUtils.FONT_TITLE);
        lblTitle.setForeground(ModernUIUtils.COLOR_PRIMARY_DARK);

        JLabel lblSub = new JLabel("Resource ID: " + resourceId + " | Complete chronological record of verifications, reservations, inspections, and status transitions.");
        lblSub.setFont(ModernUIUtils.FONT_REGULAR);
        lblSub.setForeground(ModernUIUtils.COLOR_TEXT_MUTED);

        headerCard.add(lblTitle, BorderLayout.NORTH);
        headerCard.add(lblSub, BorderLayout.SOUTH);

        // Table
        String[] cols = {"Log ID", "Action", "Timestamp", "Performed By", "Details"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tblHistory = new JTable(model);
        ModernUIUtils.styleTable(tblHistory);
        tblHistory.getColumnModel().getColumn(1).setCellRenderer(ModernUIUtils.getStatusBadgeRenderer());

        try {
            List<HistoryModel> list = historyDAO.getHistoryForResource(resourceId);
            for (HistoryModel h : list) {
                model.addRow(new Object[]{
                        h.getHistoryId(),
                        h.getActionType(),
                        h.getActionDate(),
                        h.getPerformedBy(),
                        h.getDetails()
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        JPanel tableCard = ModernUIUtils.createCard();
        tableCard.setLayout(new BorderLayout());
        tableCard.add(new JScrollPane(tblHistory), BorderLayout.CENTER);

        // Close Button
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(false);
        JButton btnClose = ModernUIUtils.createPrimaryButton("Close");
        btnClose.addActionListener(e -> dispose());
        btnPanel.add(btnClose);

        mainPanel.add(headerCard, BorderLayout.NORTH);
        mainPanel.add(tableCard, BorderLayout.CENTER);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }
}
