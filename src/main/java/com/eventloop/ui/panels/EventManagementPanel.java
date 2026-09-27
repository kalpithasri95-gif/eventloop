package com.eventloop.ui.panels;

import com.eventloop.model.EventModel;
import com.eventloop.service.AuthService;
import com.eventloop.service.EventService;
import com.eventloop.ui.MainFrame;
import com.eventloop.ui.components.ModernUIUtils;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.sql.SQLException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class EventManagementPanel extends JPanel {
    private final EventService eventService = EventService.getInstance();
    private final AuthService authService = AuthService.getInstance();
    private final MainFrame mainFrame;

    private JTable tblEvents;
    private DefaultTableModel eventsModel;

    public EventManagementPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(15, 15));
        setBackground(ModernUIUtils.COLOR_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        initComponents();
        loadEvents();
    }

    private void initComponents() {
        // Top Toolbar
        JPanel topCard = ModernUIUtils.createCard();
        topCard.setLayout(new BorderLayout(10, 10));

        JPanel headerInfo = new JPanel(new BorderLayout(4, 4));
        headerInfo.setOpaque(false);
        JLabel lblTitle = new JLabel("Event Management & Lifecycle Tracking");
        lblTitle.setFont(ModernUIUtils.FONT_TITLE);
        lblTitle.setForeground(ModernUIUtils.COLOR_PRIMARY_DARK);

        JLabel lblSub = new JLabel("Create events, attach resource requirements, track checkouts/returns, and ensure closed audit loops.");
        lblSub.setFont(ModernUIUtils.FONT_REGULAR);
        lblSub.setForeground(ModernUIUtils.COLOR_TEXT_MUTED);

        headerInfo.add(lblTitle, BorderLayout.NORTH);
        headerInfo.add(lblSub, BorderLayout.SOUTH);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        actions.setOpaque(false);

        JButton btnCreate = ModernUIUtils.createPrimaryButton("+ Create Event");
        btnCreate.addActionListener(e -> openCreateEventDialog());

        JButton btnDetails = ModernUIUtils.createSecondaryButton("Event Details");
        btnDetails.addActionListener(e -> openEventDetails());

        JButton btnAddReq = ModernUIUtils.createSecondaryButton("+ Add Requirements");
        btnAddReq.addActionListener(e -> openAddRequirementDialog());

        JButton btnMatch = ModernUIUtils.createButton("Smart Match & Decide", ModernUIUtils.COLOR_PURPLE, java.awt.Color.WHITE);
        btnMatch.addActionListener(e -> navigateToMatching());

        JButton btnCloseEvent = ModernUIUtils.createDangerButton("Event Closure Guard");
        btnCloseEvent.addActionListener(e -> openEventClosureDialog());

        JButton btnRefresh = ModernUIUtils.createSecondaryButton("Refresh");
        btnRefresh.addActionListener(e -> loadEvents());

        actions.add(btnRefresh);
        actions.add(btnDetails);
        actions.add(btnAddReq);
        actions.add(btnMatch);
        actions.add(btnCloseEvent);
        actions.add(btnCreate);

        topCard.add(headerInfo, BorderLayout.CENTER);
        topCard.add(actions, BorderLayout.SOUTH);

        // Table
        String[] cols = {"Event ID", "Event Name", "Organizer", "Department / Club", "Type", "Date", "Time", "Location", "Status", "Purchase Avoided"};
        eventsModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tblEvents = new JTable(eventsModel);
        ModernUIUtils.styleTable(tblEvents);
        tblEvents.getColumnModel().getColumn(8).setCellRenderer(ModernUIUtils.getStatusBadgeRenderer());

        tblEvents.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    openEventDetails();
                }
            }
        });

        JPanel tableCard = ModernUIUtils.createCard();
        tableCard.setLayout(new BorderLayout());
        tableCard.add(new JScrollPane(tblEvents), BorderLayout.CENTER);

        add(topCard, BorderLayout.NORTH);
        add(tableCard, BorderLayout.CENTER);
    }

    public void loadEvents() {
        try {
            List<EventModel> list = eventService.getAllEvents();
            eventsModel.setRowCount(0);

            for (EventModel e : list) {
                eventsModel.addRow(new Object[]{
                        e.getEventId(),
                        e.getEventName(),
                        e.getOrganizerName(),
                        e.getDepartmentOrClub(),
                        e.getEventType(),
                        e.getEventDate(),
                        e.getStartTime() + " - " + e.getEndTime(),
                        e.getLocation(),
                        e.getEventStatus(),
                        "₹" + String.format("%,.2f", e.getPotentialPurchaseAvoided())
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error fetching events: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public EventModel getSelectedEvent() {
        int row = tblEvents.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an event from the table first.", "Select Event", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        String evtId = (String) eventsModel.getValueAt(row, 0);
        try {
            return eventService.getById(evtId);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    private void openCreateEventDialog() {
        CreateEventDialog dlg = new CreateEventDialog(null);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            loadEvents();
        }
    }

    private void openEventDetails() {
        EventModel evt = getSelectedEvent();
        if (evt != null) {
            EventDetailsDialog dlg = new EventDetailsDialog(null, evt);
            dlg.setVisible(true);
        }
    }

    private void openAddRequirementDialog() {
        EventModel evt = getSelectedEvent();
        if (evt != null) {
            AddRequirementDialog dlg = new AddRequirementDialog(null, evt.getEventId());
            dlg.setVisible(true);
            if (dlg.isSaved()) {
                loadEvents();
            }
        }
    }

    private void navigateToMatching() {
        EventModel evt = getSelectedEvent();
        if (evt != null && mainFrame != null) {
            mainFrame.showMatchingPanelForEvent(evt.getEventId());
        }
    }

    private void openEventClosureDialog() {
        EventModel evt = getSelectedEvent();
        if (evt != null) {
            EventClosureDialog dlg = new EventClosureDialog(null, evt);
            dlg.setVisible(true);
            loadEvents();
        }
    }
}
