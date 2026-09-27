package com.eventloop.ui.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

public class ModernUIUtils {
    // Rich Curated Modern Palette
    public static final Color COLOR_PRIMARY = new Color(30, 58, 138); // Deep Navy #1E3A8A
    public static final Color COLOR_PRIMARY_DARK = new Color(15, 23, 42); // Slate 900 #0F172A
    public static final Color COLOR_PRIMARY_LIGHT = new Color(238, 242, 255); // Indigo 50
    public static final Color COLOR_ACCENT = new Color(37, 99, 235); // Electric Blue #2563EB
    public static final Color COLOR_ACCENT_HOVER = new Color(29, 78, 216);

    public static final Color COLOR_SUCCESS = new Color(5, 150, 105); // Emerald Green #059669
    public static final Color COLOR_SUCCESS_BG = new Color(236, 253, 245); // Emerald 50
    public static final Color COLOR_WARNING = new Color(217, 119, 6); // Amber #D97706
    public static final Color COLOR_WARNING_BG = new Color(254, 243, 199); // Amber 50
    public static final Color COLOR_DANGER = new Color(220, 38, 38); // Red #DC2626
    public static final Color COLOR_DANGER_BG = new Color(254, 242, 242); // Red 50
    public static final Color COLOR_PURPLE = new Color(124, 58, 237); // Purple #7C3AED
    public static final Color COLOR_PURPLE_BG = new Color(245, 243, 255);

    public static final Color COLOR_BG = new Color(241, 245, 249); // Slate 100 #F1F5F9
    public static final Color COLOR_CARD_BG = Color.WHITE;
    public static final Color COLOR_BORDER = new Color(226, 232, 240); // Slate 200 #E2E8F0
    public static final Color COLOR_TEXT_MAIN = new Color(15, 23, 42); // Slate 900
    public static final Color COLOR_TEXT_MUTED = new Color(100, 116, 139); // Slate 500

    public static final Font FONT_HERO = new Font("Segoe UI", Font.BOLD, 26);
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_SMALL_BOLD = new Font("Segoe UI", Font.BOLD, 11);

    public static JButton createButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(bg.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(
                            Math.min(255, bg.getRed() + 15),
                            Math.min(255, bg.getGreen() + 15),
                            Math.min(255, bg.getBlue() + 15)
                    ));
                } else {
                    g2.setColor(bg);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BOLD);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMargin(new Insets(7, 16, 7, 16));
        return btn;
    }

    public static JButton createPrimaryButton(String text) {
        return createButton(text, COLOR_ACCENT, Color.WHITE);
    }

    public static JButton createSuccessButton(String text) {
        return createButton(text, COLOR_SUCCESS, Color.WHITE);
    }

    public static JButton createDangerButton(String text) {
        return createButton(text, COLOR_DANGER, Color.WHITE);
    }

    public static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(new Color(248, 250, 252));
                } else {
                    g2.setColor(Color.WHITE);
                }
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.setColor(COLOR_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BOLD);
        btn.setForeground(COLOR_TEXT_MAIN);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMargin(new Insets(7, 15, 7, 15));
        return btn;
    }

    public static JPanel createCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Soft Drop Shadow
                g2.setColor(new Color(0, 0, 0, 8));
                g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 14, 14);
                // Card Body
                g2.setColor(COLOR_CARD_BG);
                g2.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 14, 14);
                // Border
                g2.setColor(COLOR_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 14, 14);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        return card;
    }

    public static void styleTable(JTable table) {
        table.setFont(FONT_REGULAR);
        table.setRowHeight(38);
        table.setShowGrid(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(241, 245, 249));
        table.setSelectionBackground(new Color(238, 242, 255));
        table.setSelectionForeground(COLOR_TEXT_MAIN);
        table.setBackground(Color.WHITE);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_HEADER);
        header.setBackground(new Color(248, 250, 252));
        header.setForeground(new Color(71, 85, 105)); // Slate 600
        header.setPreferredSize(new Dimension(header.getWidth(), 42));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER));
    }

    /**
     * Enhanced Modern Pill Badge Renderer with rounded background fills and clean typography.
     */
    public static DefaultTableCellRenderer getStatusBadgeRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                final String val = value != null ? value.toString().trim() : "";

                Color badgeFg;
                Color badgeBg;

                if ("AVAILABLE".equalsIgnoreCase(val) || "VERIFIED".equalsIgnoreCase(val) || "APPROVED".equalsIgnoreCase(val) || "COMPLETED".equalsIgnoreCase(val) || "EXACT_MATCH".equalsIgnoreCase(val)) {
                    badgeFg = COLOR_SUCCESS;
                    badgeBg = COLOR_SUCCESS_BG;
                } else if ("UNDER_REPAIR".equalsIgnoreCase(val) || "REPAIR_REQUIRED".equalsIgnoreCase(val) || "OVERDUE".equalsIgnoreCase(val) || "MISSING".equalsIgnoreCase(val) || "NOT_AVAILABLE".equalsIgnoreCase(val)) {
                    badgeFg = COLOR_DANGER;
                    badgeBg = COLOR_DANGER_BG;
                } else if ("EXPIRED".equalsIgnoreCase(val) || "VERIFICATION_REQUIRED".equalsIgnoreCase(val) || "REQUESTED".equalsIgnoreCase(val) || "REPURPOSE_REQUIRED".equalsIgnoreCase(val) || "PARTIAL_MATCH".equalsIgnoreCase(val)) {
                    badgeFg = COLOR_WARNING;
                    badgeBg = COLOR_WARNING_BG;
                } else if ("ACTIVE".equalsIgnoreCase(val) || "IN_PROGRESS".equalsIgnoreCase(val) || "RESOURCES_RESERVED".equalsIgnoreCase(val) || "COMPATIBLE_MATCH".equalsIgnoreCase(val)) {
                    badgeFg = COLOR_ACCENT;
                    badgeBg = COLOR_PRIMARY_LIGHT;
                } else if ("REPURPOSE".equalsIgnoreCase(val) || "DIRECT_REUSE".equalsIgnoreCase(val)) {
                    badgeFg = COLOR_PURPLE;
                    badgeBg = COLOR_PURPLE_BG;
                } else {
                    badgeFg = COLOR_TEXT_MUTED;
                    badgeBg = new Color(241, 245, 249);
                }

                JPanel panel = new JPanel() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(badgeBg);
                        int w = Math.min(getWidth() - 12, 140);
                        int x = (getWidth() - w) / 2;
                        g2.fillRoundRect(x, 6, w, getHeight() - 12, 12, 12);
                        g2.setColor(badgeFg);
                        g2.drawRoundRect(x, 6, w, getHeight() - 12, 12, 12);
                        g2.dispose();
                    }
                };
                panel.setOpaque(isSelected);
                if (isSelected) {
                    panel.setBackground(table.getSelectionBackground());
                } else {
                    panel.setBackground(Color.WHITE);
                }

                JLabel label = new JLabel(val);
                label.setFont(FONT_SMALL_BOLD);
                label.setForeground(badgeFg);
                label.setHorizontalAlignment(SwingConstants.CENTER);

                panel.setLayout(new java.awt.BorderLayout());
                panel.add(label, java.awt.BorderLayout.CENTER);
                return panel;
            }
        };
    }
}
