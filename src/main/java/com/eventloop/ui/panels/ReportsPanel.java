package com.eventloop.ui.panels;

import com.eventloop.service.ReportService;
import com.eventloop.ui.components.ModernUIUtils;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class ReportsPanel extends JPanel {
    private final ReportService reportService = ReportService.getInstance();

    private JComboBox<String> cmbReports;
    private JTable tblReport;
    private DefaultTableModel model;
    private String[] currentHeaders = new String[0];
    private List<String[]> currentRows = new ArrayList<>();

    public ReportsPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(ModernUIUtils.COLOR_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        initComponents();
        loadSelectedReport();
    }

    private void initComponents() {
        // Top Toolbar
        JPanel topCard = ModernUIUtils.createCard();
        topCard.setLayout(new BorderLayout(10, 10));

        JPanel header = new JPanel(new BorderLayout(4, 4));
        header.setOpaque(false);
        JLabel lblTitle = new JLabel("Audit & Financial Reports Intelligence Center");
        lblTitle.setFont(ModernUIUtils.FONT_TITLE);
        lblTitle.setForeground(ModernUIUtils.COLOR_PRIMARY_DARK);

        JLabel lblSub = new JLabel("Generate compliance audit manifests, repair liabilities, and potential purchase cost avoidance analytics.");
        lblSub.setFont(ModernUIUtils.FONT_REGULAR);
        lblSub.setForeground(ModernUIUtils.COLOR_TEXT_MUTED);

        header.add(lblTitle, BorderLayout.NORTH);
        header.add(lblSub, BorderLayout.SOUTH);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        controls.setOpaque(false);

        controls.add(new JLabel("Select Report:"));
        cmbReports = new JComboBox<>(new String[]{
                "1. Available Resources Report",
                "2. Resources Under Repair Report",
                "3. Verification Required Audit Report",
                "4. Event Resource Usage Report",
                "5. Reservation Conflict & Bookings Report",
                "6. Overdue Return Report",
                "7. Resource Reuse Classification Report",
                "8. Repair Cost Liability Report",
                "9. Potential Purchase Cost Avoidance Report",
                "10. Campus Resource Audit History Log"
        });
        cmbReports.addActionListener(e -> loadSelectedReport());
        controls.add(cmbReports);

        JButton btnGenerate = ModernUIUtils.createPrimaryButton("Generate");
        btnGenerate.addActionListener(e -> loadSelectedReport());

        JButton btnExport = ModernUIUtils.createSuccessButton("Export to CSV");
        btnExport.addActionListener(e -> exportCSV());

        controls.add(btnGenerate);
        controls.add(btnExport);

        topCard.add(header, BorderLayout.CENTER);
        topCard.add(controls, BorderLayout.SOUTH);

        // Table
        model = new DefaultTableModel();
        tblReport = new JTable(model);
        ModernUIUtils.styleTable(tblReport);

        JPanel tableCard = ModernUIUtils.createCard();
        tableCard.setLayout(new BorderLayout());
        tableCard.add(new JScrollPane(tblReport), BorderLayout.CENTER);

        add(topCard, BorderLayout.NORTH);
        add(tableCard, BorderLayout.CENTER);
    }

    public void loadSelectedReport() {
        int idx = cmbReports.getSelectedIndex();
        try {
            switch (idx) {
                case 0:
                    currentHeaders = new String[]{"Resource ID", "Resource Name", "Category", "Available", "Condition", "Location", "Purchase Unit Cost"};
                    currentRows = reportService.getAvailableResourcesReport();
                    break;
                case 1:
                    currentHeaders = new String[]{"Resource ID", "Resource Name", "Category", "Condition", "Est. Repair Cost", "Storage Location", "Notes"};
                    currentRows = reportService.getResourcesUnderRepairReport();
                    break;
                case 2:
                    currentHeaders = new String[]{"Resource ID", "Resource Name", "Category", "Verification Status", "Last Verified", "Next Due Date", "Audited By", "Location"};
                    currentRows = reportService.getVerificationRequiredReport();
                    break;
                case 3:
                    currentHeaders = new String[]{"Res ID", "Event ID", "Resource ID", "Resource Name", "Quantity", "Start Time", "End Time", "Status"};
                    currentRows = reportService.getEventResourceUsageReport();
                    break;
                case 4:
                    currentHeaders = new String[]{"Res ID", "Resource ID", "Resource Name", "Event ID", "Time Interval", "Status", "Remarks"};
                    currentRows = reportService.getReservationConflictReport();
                    break;
                case 5:
                    currentHeaders = new String[]{"Res ID", "Event ID", "Resource ID", "Resource Name", "Quantity", "Deadline", "Alert"};
                    currentRows = reportService.getOverdueReturnReport();
                    break;
                case 6:
                    currentHeaders = new String[]{"Resource ID", "Resource Name", "Category", "Reuse Classification", "Condition", "Status", "Purchase Cost"};
                    currentRows = reportService.getResourceReuseReport();
                    break;
                case 7:
                    currentHeaders = new String[]{"Resource ID", "Resource Name", "Category", "Estimated Repair", "Actual Repair", "Current Status"};
                    currentRows = reportService.getRepairCostReport();
                    break;
                case 8:
                    currentHeaders = new String[]{"Event ID", "Event Name", "Department / Club", "Event Date", "Status", "Potential Purchase Avoided"};
                    currentRows = reportService.getPotentialPurchaseAvoidanceReport();
                    break;
                case 9:
                    currentHeaders = new String[]{"Log ID", "Resource ID", "Action", "Timestamp", "Performed By", "Audit Trail Details"};
                    currentRows = reportService.getResourceHistoryReport();
                    break;
            }

            model.setDataVector(convertListToData(currentRows), currentHeaders);
            ModernUIUtils.styleTable(tblReport);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error generating report: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Object[][] convertListToData(List<String[]> list) {
        Object[][] data = new Object[list.size()][];
        for (int i = 0; i < list.size(); i++) {
            data[i] = list.get(i);
        }
        return data;
    }

    private void exportCSV() {
        if (currentRows.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No data available to export.", "Empty Report", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("EventLoop_Report.csv"));
        int res = chooser.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            boolean ok = reportService.exportToCSV(f.getAbsolutePath(), currentHeaders, currentRows);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Report successfully exported to:\n" + f.getAbsolutePath(), "Export Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to write CSV file.", "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
