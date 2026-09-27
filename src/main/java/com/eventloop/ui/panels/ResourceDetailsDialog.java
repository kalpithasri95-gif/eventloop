package com.eventloop.ui.panels;

import com.eventloop.interfaces.Repairable;
import com.eventloop.interfaces.Reservable;
import com.eventloop.interfaces.Reusable;
import com.eventloop.model.resources.AbstractResource;
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
import javax.swing.JTextArea;

public class ResourceDetailsDialog extends JDialog {

    public ResourceDetailsDialog(Frame owner, AbstractResource r) {
        super(owner, "Resource Specifications & Capability Profile", true);
        setSize(680, 560);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(15, 15));
        getContentPane().setBackground(ModernUIUtils.COLOR_BG);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setOpaque(false);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout(5, 5));
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel(r.getResourceName());
        lblTitle.setFont(ModernUIUtils.FONT_TITLE);
        lblTitle.setForeground(ModernUIUtils.COLOR_PRIMARY_DARK);

        JLabel lblSub = new JLabel("ID: " + r.getResourceId() + "  |  Category: " + r.getCategory() + "  |  Current Status: " + r.getCurrentStatus());
        lblSub.setFont(ModernUIUtils.FONT_REGULAR);
        lblSub.setForeground(ModernUIUtils.COLOR_TEXT_MUTED);

        headerPanel.add(lblTitle, BorderLayout.NORTH);
        headerPanel.add(lblSub, BorderLayout.SOUTH);

        // Capability Badges (Demonstrates implemented Java Interfaces!)
        JPanel badgesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        badgesPanel.setOpaque(false);
        badgesPanel.setBorder(BorderFactory.createTitledBorder("OOP Java Interface Capabilities:"));

        badgesPanel.add(createBadge("Resource", ModernUIUtils.COLOR_PRIMARY));
        if (r instanceof Reservable) {
            badgesPanel.add(createBadge("Reservable (reserve / release)", ModernUIUtils.COLOR_SUCCESS));
        }
        if (r instanceof Repairable) {
            badgesPanel.add(createBadge("Repairable (service cost / repair)", ModernUIUtils.COLOR_DANGER));
        }
        if (r instanceof Reusable) {
            badgesPanel.add(createBadge("Reusable (reuse / repurpose evaluation)", ModernUIUtils.COLOR_ACCENT));
        }

        // Details Grid
        JPanel detailsGrid = ModernUIUtils.createCard();
        detailsGrid.setLayout(new GridLayout(6, 2, 10, 8));

        addDetailRow(detailsGrid, "Total Quantity:", r.getQuantity() + " " + r.getUnit());
        addDetailRow(detailsGrid, "Available Quantity:", r.getAvailableQuantity() + " " + r.getUnit());
        addDetailRow(detailsGrid, "Condition Rating:", r.getConditionRating() + " / 5 stars");
        addDetailRow(detailsGrid, "Verification Status:", r.getVerificationStatus() + " (Next: " + (r.getNextVerificationDate() != null ? r.getNextVerificationDate() : "N/A") + ")");
        addDetailRow(detailsGrid, "Storage Location:", r.getFullLocation());
        addDetailRow(detailsGrid, "Current Holder:", r.getCurrentHolder() != null ? r.getCurrentHolder() : "Central Store");
        addDetailRow(detailsGrid, "New Purchase Cost:", "₹" + String.format("%,.2f", r.getPurchaseCost()));
        addDetailRow(detailsGrid, "Estimated Repair Cost:", "₹" + String.format("%,.2f", r.getEstimatedRepairCost()));
        addDetailRow(detailsGrid, "Reuse Type:", r.getReuseType());
        addDetailRow(detailsGrid, "Verified By:", r.getVerifiedBy() != null ? r.getVerifiedBy() : "Pending");
        addDetailRow(detailsGrid, "Last Verified Date:", r.getLastVerifiedDate() != null ? r.getLastVerifiedDate() : "N/A");
        addDetailRow(detailsGrid, "Created Date:", r.getCreatedDate() != null ? r.getCreatedDate() : "N/A");

        // Bottom Info Area: Technical Specs & Interface-based Reuse Recommendation
        JPanel bottomTextPanel = ModernUIUtils.createCard();
        bottomTextPanel.setLayout(new BorderLayout(5, 5));

        JLabel lblRec = new JLabel("Smart Reuse & Decision Recommendation:");
        lblRec.setFont(ModernUIUtils.FONT_HEADER);
        lblRec.setForeground(ModernUIUtils.COLOR_PRIMARY);

        String reuseRec = (r instanceof Reusable) ? ((Reusable) r).getReuseRecommendation() : "Standard resource evaluation applies.";
        JTextArea txtRec = new JTextArea(
                "Recommendation:\n" + reuseRec + "\n\n" +
                "Technical Specifications:\n" + (r.getSpecifications() != null ? r.getSpecifications() : "Standard specs.") + "\n\n" +
                "Notes / Instructions:\n" + (r.getNotes() != null ? r.getNotes() : "No special notes.")
        );
        txtRec.setEditable(false);
        txtRec.setLineWrap(true);
        txtRec.setWrapStyleWord(true);
        txtRec.setFont(ModernUIUtils.FONT_REGULAR);
        txtRec.setBackground(new Color(248, 250, 252));
        txtRec.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        bottomTextPanel.add(lblRec, BorderLayout.NORTH);
        bottomTextPanel.add(new JScrollPane(txtRec), BorderLayout.CENTER);

        JPanel contentBox = new JPanel(new BorderLayout(10, 10));
        contentBox.setOpaque(false);
        contentBox.add(badgesPanel, BorderLayout.NORTH);
        contentBox.add(detailsGrid, BorderLayout.CENTER);
        contentBox.add(bottomTextPanel, BorderLayout.SOUTH);

        // Close button
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(false);
        JButton btnClose = ModernUIUtils.createPrimaryButton("Close");
        btnClose.addActionListener(e -> dispose());
        btnPanel.add(btnClose);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(contentBox, BorderLayout.CENTER);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void addDetailRow(JPanel pnl, String label, String value) {
        JPanel row = new JPanel(new BorderLayout(5, 2));
        row.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(ModernUIUtils.FONT_SMALL);
        lbl.setForeground(ModernUIUtils.COLOR_TEXT_MUTED);
        JLabel val = new JLabel(value);
        val.setFont(ModernUIUtils.FONT_BOLD);
        val.setForeground(ModernUIUtils.COLOR_TEXT_MAIN);
        row.add(lbl, BorderLayout.NORTH);
        row.add(val, BorderLayout.SOUTH);
        pnl.add(row);
    }

    private JLabel createBadge(String text, java.awt.Color color) {
        JLabel badge = new JLabel(" " + text + " ");
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setForeground(color);
        badge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 1),
                BorderFactory.createEmptyBorder(2, 6, 2, 6)
        ));
        return badge;
    }
}
