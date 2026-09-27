package com.eventloop.ui.panels;

import com.eventloop.model.CostComparison;
import com.eventloop.model.RequirementModel;
import com.eventloop.model.resources.AbstractResource;
import com.eventloop.service.DecisionSupportService;
import com.eventloop.ui.components.ModernUIUtils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;

public class CostComparisonDialog extends JDialog {

    public CostComparisonDialog(Frame owner, RequirementModel req, AbstractResource resource) {
        super(owner, "Pre-Purchase Decision Matrix: Reuse vs Repair vs Borrow vs Rent vs Buy", true);
        setSize(780, 640);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(15, 15));
        getContentPane().setBackground(ModernUIUtils.COLOR_BG);

        CostComparison cc = DecisionSupportService.getInstance().analyzeOptions(req, resource);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setOpaque(false);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header Banner
        JPanel headerCard = ModernUIUtils.createCard();
        headerCard.setLayout(new BorderLayout(8, 8));

        JPanel titleInfo = new JPanel(new BorderLayout(4, 4));
        titleInfo.setOpaque(false);
        JLabel lblHeading = new JLabel("Economic Decision Analysis: " + req.getQuantity() + "x " + req.getResourceName());
        lblHeading.setFont(ModernUIUtils.FONT_TITLE);
        lblHeading.setForeground(ModernUIUtils.COLOR_PRIMARY_DARK);

        JLabel lblSub = new JLabel("Category: " + req.getCategory() + "  |  Required Date: " + req.getRequiredDate() + "  |  Matched Inventory Resource: " + (resource != null ? resource.getResourceName() : "None"));
        lblSub.setFont(ModernUIUtils.FONT_REGULAR);
        lblSub.setForeground(ModernUIUtils.COLOR_TEXT_MUTED);

        titleInfo.add(lblHeading, BorderLayout.NORTH);
        titleInfo.add(lblSub, BorderLayout.SOUTH);

        // Highlight Potential Purchase Avoided
        JPanel callout = new JPanel(new BorderLayout(2, 2));
        callout.setOpaque(false);
        JLabel lblCap = new JLabel("Potential Purchase Avoided:");
        lblCap.setFont(ModernUIUtils.FONT_SMALL);
        lblCap.setForeground(ModernUIUtils.COLOR_TEXT_MUTED);
        JLabel lblVal = new JLabel("₹" + String.format("%,.2f", cc.getPotentialPurchaseAvoided()));
        lblVal.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblVal.setForeground(ModernUIUtils.COLOR_SUCCESS);
        callout.add(lblCap, BorderLayout.NORTH);
        callout.add(lblVal, BorderLayout.SOUTH);

        headerCard.add(titleInfo, BorderLayout.CENTER);
        headerCard.add(callout, BorderLayout.EAST);

        // Comparison Table
        String[] cols = {"Procurement / Sourcing Option", "Cost (₹)", "Inventory Impact", "Viability & Lead Time"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        model.addRow(new Object[]{
                "1. Direct Campus Reuse",
                "₹" + String.format("%,.2f", cc.getReuseCost()),
                "Uses existing campus stock (" + cc.getAvailableQuantity() + " available)",
                cc.isExistingAvailable() ? "Immediate (0 days)" : "Insufficient Stock / Under Repair"
        });

        model.addRow(new Object[]{
                "2. Repair Existing Stock",
                "₹" + String.format("%,.2f", cc.getRepairCost()),
                "Restores damaged inventory to active state",
                "1 - 3 Days Technician Service"
        });

        model.addRow(new Object[]{
                "3. Inter-Dept Borrowing",
                "₹" + String.format("%,.2f", cc.getBorrowCost()),
                "Temporary internal loan from sister college lab",
                "Same Day Internal Transfer"
        });

        model.addRow(new Object[]{
                "4. Commercial Rental",
                "₹" + String.format("%,.2f", cc.getRentCost()),
                "Vendor rental for event duration",
                "External Vendor Agreement (1 Day)"
        });

        model.addRow(new Object[]{
                "5. New Purchase (Capital Outlay)",
                "₹" + String.format("%,.2f", cc.getTotalBuyCost()),
                "Adds permanent units to college inventory",
                "Tender / PO Approval (3 - 7 Days)"
        });

        JTable table = new JTable(model);
        ModernUIUtils.styleTable(table);

        JPanel tableCard = ModernUIUtils.createCard();
        tableCard.setLayout(new BorderLayout(6, 6));
        JLabel lblTableTitle = new JLabel("Option Matrix & Cost Breakdown");
        lblTableTitle.setFont(ModernUIUtils.FONT_HEADER);
        lblTableTitle.setForeground(ModernUIUtils.COLOR_PRIMARY);
        tableCard.add(lblTableTitle, BorderLayout.NORTH);
        tableCard.add(new JScrollPane(table), BorderLayout.CENTER);

        // Recommendation Box
        JPanel recCard = ModernUIUtils.createCard();
        recCard.setLayout(new BorderLayout(8, 8));

        JPanel recHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        recHeader.setOpaque(false);
        JLabel lblRecTitle = new JLabel("System Recommendation:");
        lblRecTitle.setFont(ModernUIUtils.FONT_HEADER);
        lblRecTitle.setForeground(ModernUIUtils.COLOR_PRIMARY);

        JLabel lblBadge = new JLabel(" " + cc.getRecommendationOption() + " ");
        lblBadge.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblBadge.setForeground(ModernUIUtils.COLOR_SUCCESS);
        lblBadge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ModernUIUtils.COLOR_SUCCESS, 1),
                BorderFactory.createEmptyBorder(2, 8, 2, 8)
        ));

        JLabel lblSavings = new JLabel("Estimated Savings vs Buying: ₹" + String.format("%,.2f", cc.getEstimatedSavings()));
        lblSavings.setFont(ModernUIUtils.FONT_BOLD);
        lblSavings.setForeground(ModernUIUtils.COLOR_SUCCESS);

        recHeader.add(lblRecTitle);
        recHeader.add(lblBadge);
        recHeader.add(lblSavings);

        JTextArea txtReason = new JTextArea(cc.getRecommendationReason());
        txtReason.setEditable(false);
        txtReason.setLineWrap(true);
        txtReason.setWrapStyleWord(true);
        txtReason.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtReason.setBackground(new Color(241, 245, 249));
        txtReason.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        recCard.add(recHeader, BorderLayout.NORTH);
        recCard.add(txtReason, BorderLayout.CENTER);

        // Close Button
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(false);
        JButton btnClose = ModernUIUtils.createPrimaryButton("Dismiss & Return");
        btnClose.addActionListener(e -> dispose());
        btnPanel.add(btnClose);

        JPanel content = new JPanel(new BorderLayout(12, 12));
        content.setOpaque(false);
        content.add(tableCard, BorderLayout.CENTER);
        content.add(recCard, BorderLayout.SOUTH);

        mainPanel.add(headerCard, BorderLayout.NORTH);
        mainPanel.add(content, BorderLayout.CENTER);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }
}
