package com.eventloop.ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class StatCard extends JPanel {
    private final JLabel lblIcon;
    private final JLabel lblTitle;
    private final JLabel lblValue;
    private final JLabel lblSubtitle;
    private final Color accentColor;
    private boolean isHovered = false;

    public StatCard(String title, String initialValue, String subtitle, String icon, Color accentColor) {
        this.accentColor = accentColor;
        setLayout(new BorderLayout(8, 8));
        setOpaque(false);
        setPreferredSize(new Dimension(195, 115));
        setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Top Row: Title + Icon Badge
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        lblTitle = new JLabel(title);
        lblTitle.setFont(ModernUIUtils.FONT_SMALL_BOLD);
        lblTitle.setForeground(ModernUIUtils.COLOR_TEXT_MUTED);

        // Circular Icon Badge
        lblIcon = new JLabel(icon) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 30));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        lblIcon.setPreferredSize(new Dimension(28, 28));
        lblIcon.setHorizontalAlignment(JLabel.CENTER);

        topRow.add(lblTitle, BorderLayout.CENTER);
        topRow.add(lblIcon, BorderLayout.EAST);

        // Center: Bold Metric Value
        lblValue = new JLabel(initialValue);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblValue.setForeground(ModernUIUtils.COLOR_TEXT_MAIN);

        // Bottom: Status Context Subtitle
        lblSubtitle = new JLabel(subtitle);
        lblSubtitle.setFont(ModernUIUtils.FONT_SMALL);
        lblSubtitle.setForeground(ModernUIUtils.COLOR_TEXT_MUTED);

        add(topRow, BorderLayout.NORTH);
        add(lblValue, BorderLayout.CENTER);
        add(lblSubtitle, BorderLayout.SOUTH);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }
        });
    }

    public void setValue(String value) {
        lblValue.setText(value);
    }

    public void setSubtitle(String text) {
        lblSubtitle.setText(text);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Soft drop shadow
        g2.setColor(new Color(0, 0, 0, isHovered ? 18 : 8));
        g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 14, 14);

        // Card Body
        g2.setColor(isHovered ? new Color(254, 254, 255) : Color.WHITE);
        g2.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 14, 14);

        // Left accent bar
        g2.setColor(accentColor);
        g2.fillRoundRect(0, 4, 4, getHeight() - 10, 4, 4);

        // Border
        g2.setColor(isHovered ? accentColor : ModernUIUtils.COLOR_BORDER);
        g2.drawRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 14, 14);

        g2.dispose();
    }
}
