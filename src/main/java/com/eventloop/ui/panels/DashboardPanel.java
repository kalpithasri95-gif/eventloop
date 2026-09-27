package com.eventloop.ui.panels;

import com.eventloop.dao.EventDAO;
import com.eventloop.dao.HistoryDAO;
import com.eventloop.dao.ReservationDAO;
import com.eventloop.dao.ResourceDAO;
import com.eventloop.model.EventModel;
import com.eventloop.model.HistoryModel;
import com.eventloop.model.ReservationModel;
import com.eventloop.model.resources.AbstractResource;
import com.eventloop.service.AuthService;
import com.eventloop.ui.MainFrame;
import com.eventloop.ui.components.ModernUIUtils;
import com.eventloop.ui.components.StatCard;
import com.eventloop.util.DateUtil;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.sql.SQLException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class DashboardPanel extends JPanel {
    private final ResourceDAO resourceDAO = new ResourceDAO();
    private final EventDAO eventDAO = new EventDAO();
    private final ReservationDAO reservationDAO = new ReservationDAO();
    private final HistoryDAO historyDAO = new HistoryDAO();
    private final MainFrame mainFrame;

    // Hero Callout Label
    private JLabel lblHeroAvoidanceValue;

    // 12 KPI Cards
    private StatCard cardTotalResources;
    private StatCard cardAvailable;
    private StatCard cardInUse;
    private StatCard cardUnderRepair;
    private StatCard cardVerificationReq;
    private StatCard cardActiveEvents;
    private StatCard cardPendingReturns;
    private StatCard cardOverdue;
    private StatCard cardPurchaseAvoided;
    private StatCard cardResourcesReused;
    private StatCard cardAssetValue;
    private StatCard cardTotalRepairs;

    // Visual Inventory Health Bar Panel
    private InventoryHealthBar healthBar;

    private JTable tblUpcomingEvents;
    private DefaultTableModel eventsModel;
    private JTable tblRecentActivity;
    private DefaultTableModel historyModel;

    public DashboardPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(16, 16));
        setBackground(ModernUIUtils.COLOR_BG);
        setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        initComponents();
        refreshData();
    }

    private void initComponents() {
        // 1. TOP HERO GRADIENT BANNER
        JPanel heroBanner = new JPanel(new BorderLayout(15, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Gradient from Deep Navy #1E3A8A to Vibrant Royal Blue #2563EB
                GradientPaint gp = new GradientPaint(0, 0, new Color(23, 37, 84), getWidth(), getHeight(), new Color(37, 99, 235));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        heroBanner.setOpaque(false);
        heroBanner.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // Left Side Title & Quick Actions
        JPanel leftHero = new JPanel(new BorderLayout(8, 8));
        leftHero.setOpaque(false);

        JLabel lblHeroTitle = new JLabel("Smart Resource Reuse & Pre-Purchase Intelligence");
        lblHeroTitle.setFont(ModernUIUtils.FONT_HERO);
        lblHeroTitle.setForeground(Color.WHITE);

        JLabel lblHeroSub = new JLabel("Protecting college procurement budgets by reusing, repairing, and auditing campus inventory before buying new assets.");
        lblHeroSub.setFont(ModernUIUtils.FONT_REGULAR);
        lblHeroSub.setForeground(new Color(226, 232, 240));

        // Action Buttons Bar right on Hero Banner
        JPanel heroActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        heroActions.setOpaque(false);

        JButton btnNewEvent = ModernUIUtils.createButton("+ Create Event", new Color(16, 185, 129), Color.WHITE);
        btnNewEvent.addActionListener(e -> {
            CreateEventDialog dlg = new CreateEventDialog(null);
            dlg.setVisible(true);
            if (dlg.isSaved()) refreshData();
        });

        JButton btnAddRes = ModernUIUtils.createButton("+ Add Resource", Color.WHITE, ModernUIUtils.COLOR_PRIMARY_DARK);
        btnAddRes.addActionListener(e -> {
            AddEditResourceDialog dlg = new AddEditResourceDialog(null, null);
            dlg.setVisible(true);
            if (dlg.isSaved()) refreshData();
        });

        JButton btnSmartMatch = ModernUIUtils.createButton("⚡ Match & Decision Engine", new Color(245, 158, 11), Color.WHITE);
        btnSmartMatch.addActionListener(e -> {
            if (mainFrame != null) mainFrame.showTab(4); // Switch to matching tab
        });

        JButton btnRefresh = ModernUIUtils.createButton("🔄 Refresh", new Color(30, 41, 59, 160), Color.WHITE);
        btnRefresh.addActionListener(e -> refreshData());

        heroActions.add(btnNewEvent);
        heroActions.add(btnAddRes);
        heroActions.add(btnSmartMatch);
        heroActions.add(btnRefresh);

        leftHero.add(lblHeroTitle, BorderLayout.NORTH);
        leftHero.add(lblHeroSub, BorderLayout.CENTER);
        leftHero.add(heroActions, BorderLayout.SOUTH);

        // Right Side Hero Savings Glass Card
        JPanel rightHeroCard = new JPanel(new BorderLayout(5, 5)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 35)); // Semi-transparent glassmorphism
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(new Color(255, 255, 255, 70));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        rightHeroCard.setOpaque(false);
        rightHeroCard.setPreferredSize(new Dimension(280, 100));
        rightHeroCard.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        JLabel lblHeroTag = new JLabel("POTENTIAL PURCHASE AVOIDED");
        lblHeroTag.setFont(ModernUIUtils.FONT_SMALL_BOLD);
        lblHeroTag.setForeground(new Color(209, 250, 229)); // Soft Mint

        lblHeroAvoidanceValue = new JLabel("₹0.00");
        lblHeroAvoidanceValue.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblHeroAvoidanceValue.setForeground(new Color(52, 211, 153)); // Glowing Emerald

        JLabel lblHeroSubtext = new JLabel("Direct budget preserved for campus");
        lblHeroSubtext.setFont(ModernUIUtils.FONT_SMALL);
        lblHeroSubtext.setForeground(Color.WHITE);

        rightHeroCard.add(lblHeroTag, BorderLayout.NORTH);
        rightHeroCard.add(lblHeroAvoidanceValue, BorderLayout.CENTER);
        rightHeroCard.add(lblHeroSubtext, BorderLayout.SOUTH);

        heroBanner.add(leftHero, BorderLayout.CENTER);
        heroBanner.add(rightHeroCard, BorderLayout.EAST);

        // 2. 12 STAT CARDS (3 rows x 4 cols)
        JPanel cardsGrid = new JPanel(new GridLayout(3, 4, 12, 12));
        cardsGrid.setOpaque(false);

        cardTotalResources = new StatCard("Total Resources", "0", "Campus asset base", "📦", ModernUIUtils.COLOR_PRIMARY);
        cardAvailable = new StatCard("Available Stock", "0", "Verified & ready to reserve", "✅", ModernUIUtils.COLOR_SUCCESS);
        cardInUse = new StatCard("Resources In Use", "0", "Dispatched to events", "⏳", ModernUIUtils.COLOR_ACCENT);
        cardUnderRepair = new StatCard("Under Repair", "0", "Awaiting technician fix", "🛠️", ModernUIUtils.COLOR_DANGER);

        cardVerificationReq = new StatCard("Verification Due", "0", "Audit expired/unverified", "🛡️", ModernUIUtils.COLOR_WARNING);
        cardActiveEvents = new StatCard("Active Events", "0", "Ongoing campus programs", "📅", ModernUIUtils.COLOR_PURPLE);
        cardPendingReturns = new StatCard("Pending Returns", "0", "Awaiting post-inspection", "🔄", ModernUIUtils.COLOR_WARNING);
        cardOverdue = new StatCard("Overdue Returns", "0", "Past return deadline", "🚨", ModernUIUtils.COLOR_DANGER);

        cardPurchaseAvoided = new StatCard("Purchase Avoided", "₹0", "Budget saved by reuse", "💰", ModernUIUtils.COLOR_SUCCESS);
        cardResourcesReused = new StatCard("Resources Reused", "0", "Successful allocations", "🔁", ModernUIUtils.COLOR_ACCENT);
        cardAssetValue = new StatCard("Total Asset Value", "₹0", "Capital asset balance", "🏛️", ModernUIUtils.COLOR_PRIMARY);
        cardTotalRepairs = new StatCard("Est. Repair Needs", "₹0", "Maintenance liabilities", "🔧", ModernUIUtils.COLOR_WARNING);

        cardsGrid.add(cardTotalResources);
        cardsGrid.add(cardAvailable);
        cardsGrid.add(cardInUse);
        cardsGrid.add(cardUnderRepair);
        cardsGrid.add(cardVerificationReq);
        cardsGrid.add(cardActiveEvents);
        cardsGrid.add(cardPendingReturns);
        cardsGrid.add(cardOverdue);
        cardsGrid.add(cardPurchaseAvoided);
        cardsGrid.add(cardResourcesReused);
        cardsGrid.add(cardAssetValue);
        cardsGrid.add(cardTotalRepairs);

        // 3. VISUAL HEALTH BAR CARD
        JPanel healthCard = ModernUIUtils.createCard();
        healthCard.setLayout(new BorderLayout(8, 8));

        JLabel lblHealthTitle = new JLabel("Live Campus Inventory Health & Operational Distribution");
        lblHealthTitle.setFont(ModernUIUtils.FONT_SUBTITLE);
        lblHealthTitle.setForeground(ModernUIUtils.COLOR_PRIMARY);

        healthBar = new InventoryHealthBar();

        healthCard.add(lblHealthTitle, BorderLayout.NORTH);
        healthCard.add(healthBar, BorderLayout.CENTER);

        // 4. DATA TABLES SECTION: Upcoming Events & Live Activity History
        JPanel pnlEvents = ModernUIUtils.createCard();
        pnlEvents.setLayout(new BorderLayout(8, 8));
        JLabel lblEvtTitle = new JLabel("Upcoming Campus Events & Allocation Status");
        lblEvtTitle.setFont(ModernUIUtils.FONT_SUBTITLE);
        lblEvtTitle.setForeground(ModernUIUtils.COLOR_PRIMARY);
        pnlEvents.add(lblEvtTitle, BorderLayout.NORTH);

        eventsModel = new DefaultTableModel(new String[]{"Event ID", "Event Name", "Organizer", "Date", "Status", "Avoided Budget"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblUpcomingEvents = new JTable(eventsModel);
        ModernUIUtils.styleTable(tblUpcomingEvents);
        tblUpcomingEvents.getColumnModel().getColumn(4).setCellRenderer(ModernUIUtils.getStatusBadgeRenderer());
        pnlEvents.add(new JScrollPane(tblUpcomingEvents), BorderLayout.CENTER);

        JPanel pnlHistory = ModernUIUtils.createCard();
        pnlHistory.setLayout(new BorderLayout(8, 8));
        JLabel lblHistTitle = new JLabel("Live Resource Audit Movement Timeline");
        lblHistTitle.setFont(ModernUIUtils.FONT_SUBTITLE);
        lblHistTitle.setForeground(ModernUIUtils.COLOR_PRIMARY);
        pnlHistory.add(lblHistTitle, BorderLayout.NORTH);

        historyModel = new DefaultTableModel(new String[]{"Timestamp", "Resource ID", "Action", "Audited By", "Details"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblRecentActivity = new JTable(historyModel);
        ModernUIUtils.styleTable(tblRecentActivity);
        tblRecentActivity.getColumnModel().getColumn(2).setCellRenderer(ModernUIUtils.getStatusBadgeRenderer());
        pnlHistory.add(new JScrollPane(tblRecentActivity), BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, pnlEvents, pnlHistory);
        splitPane.setResizeWeight(0.52);
        splitPane.setDividerSize(8);
        splitPane.setOpaque(false);
        splitPane.setBorder(null);

        // Center Container
        JPanel centerContainer = new JPanel(new BorderLayout(14, 14));
        centerContainer.setOpaque(false);
        centerContainer.add(cardsGrid, BorderLayout.NORTH);

        JPanel middleSection = new JPanel(new BorderLayout(14, 14));
        middleSection.setOpaque(false);
        middleSection.add(healthCard, BorderLayout.NORTH);
        middleSection.add(splitPane, BorderLayout.CENTER);

        centerContainer.add(middleSection, BorderLayout.CENTER);

        add(heroBanner, BorderLayout.NORTH);
        add(centerContainer, BorderLayout.CENTER);
    }

    public void refreshData() {
        try {
            List<AbstractResource> resources = resourceDAO.getAllResources();
            List<EventModel> events = eventDAO.getAllEvents();
            List<ReservationModel> reservations = reservationDAO.getAllReservations();
            List<HistoryModel> histories = historyDAO.getAllHistory();

            int total = 0;
            int available = 0;
            int inUse = 0;
            int underRepair = 0;
            int verifReq = 0;
            double totalAssetVal = 0.0;
            double totalRepairCost = 0.0;

            for (AbstractResource r : resources) {
                total += r.getQuantity();
                totalAssetVal += r.getPurchaseCost() * r.getQuantity();
                totalRepairCost += r.getEstimatedRepairCost();

                if ("UNDER_REPAIR".equalsIgnoreCase(r.getCurrentStatus())) {
                    underRepair += r.getQuantity();
                } else if (r.getAvailableQuantity() > 0 && r.isReservableState()) {
                    available += r.getAvailableQuantity();
                }

                if ("EXPIRED".equalsIgnoreCase(r.getVerificationStatus()) || 
                    "VERIFICATION_REQUIRED".equalsIgnoreCase(r.getVerificationStatus()) || 
                    DateUtil.isExpired(r.getNextVerificationDate())) {
                    verifReq += r.getQuantity();
                }
            }

            int activeEvents = 0;
            double totalPurchaseAvoided = 0.0;
            for (EventModel e : events) {
                totalPurchaseAvoided += e.getPotentialPurchaseAvoided();
                if (!"COMPLETED".equalsIgnoreCase(e.getEventStatus()) && !"CANCELLED".equalsIgnoreCase(e.getEventStatus())) {
                    activeEvents++;
                }
            }

            int pendingReturns = 0;
            int overdue = 0;
            int resourcesReusedCount = 0;

            for (ReservationModel rm : reservations) {
                if ("ACTIVE".equalsIgnoreCase(rm.getReservationStatus())) {
                    inUse += rm.getQuantity();
                    pendingReturns++;
                    if (DateUtil.isOverdue(rm.getReturnDeadline())) {
                        overdue++;
                    }
                } else if ("OVERDUE".equalsIgnoreCase(rm.getReservationStatus())) {
                    inUse += rm.getQuantity();
                    pendingReturns++;
                    overdue++;
                }
                if ("COMPLETED".equalsIgnoreCase(rm.getReservationStatus()) || "ACTIVE".equalsIgnoreCase(rm.getReservationStatus())) {
                    resourcesReusedCount += rm.getQuantity();
                }
            }

            // Update Hero Banner
            lblHeroAvoidanceValue.setText("₹" + String.format("%,.2f", totalPurchaseAvoided));

            // Update Cards
            cardTotalResources.setValue(String.valueOf(total));
            cardAvailable.setValue(String.valueOf(available));
            cardInUse.setValue(String.valueOf(inUse));
            cardUnderRepair.setValue(String.valueOf(underRepair));
            cardVerificationReq.setValue(String.valueOf(verifReq));
            cardActiveEvents.setValue(String.valueOf(activeEvents));
            cardPendingReturns.setValue(String.valueOf(pendingReturns));
            cardOverdue.setValue(String.valueOf(overdue));
            cardPurchaseAvoided.setValue("₹" + String.format("%,.0f", totalPurchaseAvoided));
            cardResourcesReused.setValue(String.valueOf(resourcesReusedCount));
            cardAssetValue.setValue("₹" + String.format("%,.0f", totalAssetVal));
            cardTotalRepairs.setValue("₹" + String.format("%,.0f", totalRepairCost));

            // Update Health Bar
            healthBar.setCounts(available, inUse, underRepair, verifReq, total);

            // Populate Events Table
            eventsModel.setRowCount(0);
            for (EventModel e : events) {
                eventsModel.addRow(new Object[]{
                        e.getEventId(),
                        e.getEventName(),
                        e.getOrganizerName(),
                        e.getEventDate(),
                        e.getEventStatus(),
                        "₹" + String.format("%,.2f", e.getPotentialPurchaseAvoided())
                });
            }

            // Populate History Table
            historyModel.setRowCount(0);
            for (int i = 0; i < Math.min(25, histories.size()); i++) {
                HistoryModel h = histories.get(i);
                historyModel.addRow(new Object[]{
                        h.getActionDate(),
                        h.getResourceId(),
                        h.getActionType(),
                        h.getPerformedBy(),
                        h.getDetails()
                });
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Custom painted visual progress bar displaying the exact proportion of
     * Available vs In-Use vs Under Repair vs Verification-Due inventory.
     */
    private static class InventoryHealthBar extends JPanel {
        private int available = 0;
        private int inUse = 0;
        private int underRepair = 0;
        private int verificationDue = 0;
        private int total = 1;

        public InventoryHealthBar() {
            setOpaque(false);
            setPreferredSize(new Dimension(300, 52));
        }

        public void setCounts(int avail, int inUse, int repair, int verif, int total) {
            this.available = avail;
            this.inUse = inUse;
            this.underRepair = repair;
            this.verificationDue = verif;
            this.total = Math.max(1, total);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int barHeight = 16;
            int y = 6;

            int availW = (int) Math.round((double) available / total * w);
            int inUseW = (int) Math.round((double) inUse / total * w);
            int repairW = (int) Math.round((double) underRepair / total * w);
            int verifW = w - availW - inUseW - repairW;

            int curX = 0;

            // Background
            g2.setColor(new Color(241, 245, 249));
            g2.fillRoundRect(0, y, w, barHeight, 8, 8);

            // 1. Available (Emerald Green)
            if (availW > 0) {
                g2.setColor(ModernUIUtils.COLOR_SUCCESS);
                g2.fillRoundRect(curX, y, availW, barHeight, 6, 6);
                curX += availW;
            }

            // 2. In Use (Electric Blue)
            if (inUseW > 0) {
                g2.setColor(ModernUIUtils.COLOR_ACCENT);
                g2.fillRect(curX, y, inUseW, barHeight);
                curX += inUseW;
            }

            // 3. Under Repair (Red)
            if (repairW > 0) {
                g2.setColor(ModernUIUtils.COLOR_DANGER);
                g2.fillRect(curX, y, repairW, barHeight);
                curX += repairW;
            }

            // 4. Verification Due (Amber)
            if (verifW > 0) {
                g2.setColor(ModernUIUtils.COLOR_WARNING);
                g2.fillRoundRect(curX, y, Math.max(2, w - curX), barHeight, 6, 6);
            }

            // Legend below the bar
            int legendY = y + barHeight + 18;
            drawLegendPill(g2, 10, legendY, "Available (" + available + ")", ModernUIUtils.COLOR_SUCCESS);
            drawLegendPill(g2, 160, legendY, "In Use (" + inUse + ")", ModernUIUtils.COLOR_ACCENT);
            drawLegendPill(g2, 280, legendY, "Under Repair (" + underRepair + ")", ModernUIUtils.COLOR_DANGER);
            drawLegendPill(g2, 430, legendY, "Verification Due (" + verificationDue + ")", ModernUIUtils.COLOR_WARNING);

            g2.dispose();
        }

        private void drawLegendPill(Graphics2D g2, int x, int y, String label, Color color) {
            g2.setColor(color);
            g2.fillOval(x, y - 9, 10, 10);
            g2.setFont(ModernUIUtils.FONT_SMALL_BOLD);
            g2.setColor(ModernUIUtils.COLOR_TEXT_MAIN);
            g2.drawString(label, x + 16, y);
        }
    }
}
