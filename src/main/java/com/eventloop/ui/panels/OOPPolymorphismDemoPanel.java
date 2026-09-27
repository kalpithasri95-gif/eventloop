package com.eventloop.ui.panels;

import com.eventloop.interfaces.Repairable;
import com.eventloop.interfaces.Reservable;
import com.eventloop.interfaces.Resource;
import com.eventloop.interfaces.Reusable;
import com.eventloop.model.resources.AbstractResource;
import com.eventloop.model.resources.AudioVisualResource;
import com.eventloop.model.resources.Banner;
import com.eventloop.model.resources.Chair;
import com.eventloop.model.resources.CustomPrintedBanner;
import com.eventloop.model.resources.DecorationResource;
import com.eventloop.model.resources.ElectricalResource;
import com.eventloop.model.resources.EquipmentResource;
import com.eventloop.model.resources.ExtensionCable;
import com.eventloop.model.resources.FurnitureResource;
import com.eventloop.model.resources.Microphone;
import com.eventloop.model.resources.NameBadge;
import com.eventloop.model.resources.Projector;
import com.eventloop.model.resources.RegistrationKit;
import com.eventloop.model.resources.Speaker;
import com.eventloop.model.resources.StandeeFrame;
import com.eventloop.model.resources.StationeryResource;
import com.eventloop.model.resources.Table;
import com.eventloop.ui.components.ModernUIUtils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

public class OOPPolymorphismDemoPanel extends JPanel {
    private JComboBox<String> cmbObjectSelector;
    private JTextArea txtArchitectureDiagram;
    private JTextArea txtLiveOutput;

    public OOPPolymorphismDemoPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(ModernUIUtils.COLOR_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        initComponents();
    }

    private void initComponents() {
        // Top Header
        JPanel topCard = ModernUIUtils.createCard();
        topCard.setLayout(new BorderLayout(5, 5));

        JLabel lblTitle = new JLabel("OOP Architecture, Java Interfaces & Runtime Polymorphism Lab");
        lblTitle.setFont(ModernUIUtils.FONT_TITLE);
        lblTitle.setForeground(ModernUIUtils.COLOR_PRIMARY_DARK);

        JLabel lblSub = new JLabel("Interactive demonstration of Multiple Interface Implementation, Dynamic Method Dispatch, and Interface References pointing to diverse Subtypes.");
        lblSub.setFont(ModernUIUtils.FONT_REGULAR);
        lblSub.setForeground(ModernUIUtils.COLOR_TEXT_MUTED);

        topCard.add(lblTitle, BorderLayout.NORTH);
        topCard.add(lblSub, BorderLayout.SOUTH);

        // Control Toolbar
        JPanel controlCard = ModernUIUtils.createCard();
        controlCard.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 6));

        controlCard.add(new JLabel("Select Target Object Instance:"));
        cmbObjectSelector = new JComboBox<>(new String[]{
                "1. Projector (AudioVisual -> Resource, Reservable, Repairable, Reusable)",
                "2. ExtensionCable (Electrical -> Resource, Reservable, Repairable, Reusable)",
                "3. Table (Furniture -> Resource, Reservable, Reusable)",
                "4. Chair (Furniture -> Resource, Reservable, Reusable)",
                "5. CustomPrintedBanner (Decoration -> Resource, Reusable [Repurpose])",
                "6. StandeeFrame (Equipment -> Resource, Reservable, Reusable)",
                "7. NameBadge (Stationery -> Resource, Reusable)",
                "8. Speaker (AudioVisual -> Resource, Reservable, Repairable, Reusable)",
                "9. Microphone (AudioVisual -> Resource, Reservable, Repairable, Reusable)",
                "10. RegistrationKit (Stationery -> Resource, Reusable)"
        });
        controlCard.add(cmbObjectSelector);

        JButton btnTestPolymorphism = ModernUIUtils.createPrimaryButton("Invoke Polymorphic Methods");
        btnTestPolymorphism.addActionListener(e -> runPolymorphicDemo());
        controlCard.add(btnTestPolymorphism);

        JButton btnRunAll = ModernUIUtils.createButton("Run Full Polymorphic Batch Loop", ModernUIUtils.COLOR_PURPLE, Color.WHITE);
        btnRunAll.addActionListener(e -> runAllBatchDemo());
        controlCard.add(btnRunAll);

        JButton btnClear = ModernUIUtils.createSecondaryButton("Clear Console");
        btnClear.addActionListener(e -> txtLiveOutput.setText(""));
        controlCard.add(btnClear);

        // Center Split: Left Architecture Matrix, Right Live Output
        JPanel leftPanel = ModernUIUtils.createCard();
        leftPanel.setLayout(new BorderLayout(6, 6));
        JLabel lblLeft = new JLabel("Java Interface Implementation Hierarchy");
        lblLeft.setFont(ModernUIUtils.FONT_HEADER);
        lblLeft.setForeground(ModernUIUtils.COLOR_PRIMARY);

        txtArchitectureDiagram = new JTextArea();
        txtArchitectureDiagram.setEditable(false);
        txtArchitectureDiagram.setFont(new Font("Consolas", Font.PLAIN, 12));
        txtArchitectureDiagram.setBackground(new Color(248, 250, 252));
        txtArchitectureDiagram.setText(getHierarchyText());
        leftPanel.add(lblLeft, BorderLayout.NORTH);
        leftPanel.add(new JScrollPane(txtArchitectureDiagram), BorderLayout.CENTER);

        JPanel rightPanel = ModernUIUtils.createCard();
        rightPanel.setLayout(new BorderLayout(6, 6));
        JLabel lblRight = new JLabel("Live JVM Polymorphic Dispatch Console Output");
        lblRight.setFont(ModernUIUtils.FONT_HEADER);
        lblRight.setForeground(ModernUIUtils.COLOR_SUCCESS);

        txtLiveOutput = new JTextArea();
        txtLiveOutput.setEditable(false);
        txtLiveOutput.setFont(new Font("Consolas", Font.PLAIN, 12));
        txtLiveOutput.setBackground(new Color(15, 23, 42)); // Dark console #0F172A
        txtLiveOutput.setForeground(new Color(241, 245, 249)); // Clean bright text
        txtLiveOutput.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        txtLiveOutput.setText("=== EventLoop OOP & Polymorphic Engine Initialized ===\nClick 'Invoke Polymorphic Methods' or 'Run Full Polymorphic Batch Loop' above to see live runtime dispatch.\n");

        rightPanel.add(lblRight, BorderLayout.NORTH);
        rightPanel.add(new JScrollPane(txtLiveOutput), BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setResizeWeight(0.44);
        splitPane.setDividerSize(6);
        splitPane.setOpaque(false);
        splitPane.setBorder(null);

        JPanel topContainer = new JPanel(new BorderLayout(8, 8));
        topContainer.setOpaque(false);
        topContainer.add(topCard, BorderLayout.NORTH);
        topContainer.add(controlCard, BorderLayout.SOUTH);

        add(topContainer, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
    }

    private void runPolymorphicDemo() {
        int idx = cmbObjectSelector.getSelectedIndex();
        AbstractResource obj;
        switch (idx) {
            case 0: obj = new Projector("EL-AUD-001", "HD High-Lumen Projector (4K Support)", 4, 45000.0, "Media Center", "Room 102"); break;
            case 1: obj = new ExtensionCable("EL-ELE-001", "Heavy Duty Extension Spike/Cable", 15, 750.0, "Engineering Block", "Room 201"); break;
            case 2: obj = new Table("EL-FUR-001", "Folding Banquet Table (6ft)", 30, 2800.0, "Warehouse", "Room W-02"); break;
            case 3: obj = new Chair("EL-FUR-002", "Cushioned Event Chair", 200, 1200.0, "Warehouse", "Room W-01"); break;
            case 4: obj = new CustomPrintedBanner("EL-DEC-002", "Tech Fest 2025 Printed Flex Banner", "Tech Fest 2025", 4, 1200.0, "Store", "Room D-11"); break;
            case 5: obj = new StandeeFrame("EL-EQP-001", "Aluminium Roll-up Standee Frame (6x3 ft)", 12, 1800.0, "Store", "Room D-10"); break;
            case 6: obj = new NameBadge("EL-STA-001", "PVC Name Badge Holder with Lanyard", 350, 25.0, "Store", "Room S-05"); break;
            case 7: obj = new Speaker("EL-AUD-004", "Portable PA Powered Speaker (300W)", 4, 22000.0, "Auditorium", "Room A-01"); break;
            case 8: obj = new Microphone("EL-AUD-003", "Wireless UHF Handheld Microphone Set", 8, 6500.0, "Auditorium", "Room A-01"); break;
            case 9: obj = new RegistrationKit("EL-STA-002", "Executive Delegate Folder & Kit", 80, 120.0, "Store", "Room S-05"); break;
            default: obj = new Projector(); break;
        }

        StringBuilder log = new StringBuilder();
        log.append("\n--------------------------------------------------------------\n");
        log.append(">> RUNTIME POLYMORPHIC DISPATCH DEMO\n");
        log.append(">> Instantiated Concrete Class: ").append(obj.getClass().getName()).append("\n");
        log.append(">> Superclass: ").append(obj.getClass().getSuperclass().getSimpleName()).append("\n");

        // 1. Interface reference: Resource
        Resource resRef = obj; // Polymorphic Assignment!
        log.append("\n[1] Invoking via 'Resource' interface reference (resRef.displayDetails()):\n");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        PrintStream oldOut = System.out;
        System.setOut(ps);
        resRef.displayDetails();
        System.setOut(oldOut);
        log.append("    ").append(baos.toString().replace("\n", "\n    "));

        // 2. Interface reference: Reservable
        if (obj instanceof Reservable) {
            Reservable reservableRef = (Reservable) obj; // Polymorphic reference!
            log.append("\n[2] Invoking via 'Reservable' interface reference:\n");
            boolean resOk = reservableRef.reserve();
            log.append("    -> reservableRef.reserve() executed: ").append(resOk ? "SUCCESS (Stock locked)" : "BLOCKED").append("\n");
            reservableRef.release();
            log.append("    -> reservableRef.release() executed: Stock restored.\n");
        } else {
            log.append("\n[2] 'Reservable' interface NOT implemented by ").append(obj.getClass().getSimpleName()).append("\n");
        }

        // 3. Interface reference: Repairable
        if (obj instanceof Repairable) {
            Repairable repairableRef = (Repairable) obj; // Polymorphic reference!
            log.append("\n[3] Invoking via 'Repairable' interface reference:\n");
            double repCost = repairableRef.calculateRepairCost();
            log.append("    -> repairableRef.calculateRepairCost(): ₹").append(String.format("%,.2f", repCost)).append("\n");
        } else {
            log.append("\n[3] 'Repairable' interface NOT implemented by ").append(obj.getClass().getSimpleName()).append("\n");
        }

        // 4. Interface reference: Reusable
        if (obj instanceof Reusable) {
            Reusable reusableRef = (Reusable) obj; // Polymorphic reference!
            log.append("\n[4] Invoking via 'Reusable' interface reference:\n");
            log.append("    -> reusableRef.canBeReused(): ").append(reusableRef.canBeReused()).append("\n");
            log.append("    -> reusableRef.getReuseRecommendation():\n");
            log.append("       \"").append(reusableRef.getReuseRecommendation()).append("\"\n");
        } else {
            log.append("\n[4] 'Reusable' interface NOT implemented by ").append(obj.getClass().getSimpleName()).append("\n");
        }

        txtLiveOutput.append(log.toString());
        txtLiveOutput.setCaretPosition(txtLiveOutput.getDocument().getLength());
    }

    private void runAllBatchDemo() {
        List<Resource> resourceList = new ArrayList<>();
        resourceList.add(new Projector("EL-AUD-001", "HD High-Lumen Projector (4K Support)", 4, 45000.0, "Media Center", "Room 102"));
        resourceList.add(new ExtensionCable("EL-ELE-001", "Heavy Duty Extension Spike/Cable", 15, 750.0, "Engineering Block", "Room 201"));
        resourceList.add(new Table("EL-FUR-001", "Folding Banquet Table (6ft)", 30, 2800.0, "Warehouse", "Room W-02"));
        resourceList.add(new Chair("EL-FUR-002", "Cushioned Event Chair", 200, 1200.0, "Warehouse", "Room W-01"));
        resourceList.add(new CustomPrintedBanner("EL-DEC-002", "Tech Fest 2025 Printed Flex Banner", "Tech Fest 2025", 4, 1200.0, "Store", "Room D-11"));
        resourceList.add(new StandeeFrame("EL-EQP-001", "Aluminium Roll-up Standee Frame (6x3 ft)", 12, 1800.0, "Store", "Room D-10"));
        resourceList.add(new NameBadge("EL-STA-001", "PVC Name Badge Holder with Lanyard", 350, 25.0, "Store", "Room S-05"));
        resourceList.add(new Speaker("EL-AUD-004", "Portable PA Powered Speaker (300W)", 4, 22000.0, "Auditorium", "Room A-01"));
        resourceList.add(new Microphone("EL-AUD-003", "Wireless UHF Handheld Microphone Set", 8, 6500.0, "Auditorium", "Room A-01"));
        resourceList.add(new RegistrationKit("EL-STA-002", "Executive Delegate Folder & Kit", 80, 120.0, "Store", "Room S-05"));

        StringBuilder log = new StringBuilder();
        log.append("\n========================================================================\n");
        log.append(">> BATCH POLYMORPHIC EXECUTION ACROSS JAVA INTERFACES\n");
        log.append(">> Iterating over List<Resource> pointing to 10 distinct concrete classes:\n");
        log.append("========================================================================\n");

        for (Resource r : resourceList) {
            AbstractResource ar = (AbstractResource) r;
            log.append("\n* Object: ").append(ar.getClass().getSimpleName())
               .append(" [ID: ").append(ar.getResourceId()).append("] - ").append(ar.getResourceName()).append("\n");

            // Polymorphic dispatch to displayDetails()
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            PrintStream oldOut = System.out;
            System.setOut(ps);
            r.displayDetails();
            System.setOut(oldOut);
            log.append("  ").append(baos.toString().trim().replace("\n", "\n  ")).append("\n");

            // Polymorphic dispatch to Reusable if implemented
            if (r instanceof Reusable) {
                Reusable reu = (Reusable) r;
                log.append("  [Reusable Recommendation] => ").append(reu.getReuseRecommendation()).append("\n");
            }

            // Polymorphic dispatch to Repairable if implemented
            if (r instanceof Repairable) {
                Repairable rep = (Repairable) r;
                log.append("  [Repairable Estimate]     => ₹").append(String.format("%,.2f", rep.calculateRepairCost())).append("\n");
            }
        }

        txtLiveOutput.append(log.toString());
        txtLiveOutput.setCaretPosition(txtLiveOutput.getDocument().getLength());
    }

    private String getHierarchyText() {
        return "=========================================================\n" +
               "          EVENTLOOP JAVA OOP & INTERFACE ARCHITECTURE    \n" +
               "=========================================================\n\n" +
               "+-------------------------------------------------------+\n" +
               "|                     <<INTERFACE>>                     |\n" +
               "|                       Resource                        |\n" +
               "|   + void displayDetails()                             |\n" +
               "|   + void inspect()                                    |\n" +
               "+-------------------------------------------------------+\n" +
               "                           ^                             \n" +
               "                           | implements                  \n" +
               "+-------------------------------------------------------+\n" +
               "|                   AbstractResource                    |\n" +
               "|   - resourceId, name, category, quantity, cost...     |\n" +
               "|   - conditionRating (1-5), currentStatus...           |\n" +
               "+-------------------------------------------------------+\n" +
               "           ^         ^          ^          ^             \n" +
               "  +--------+         |          |          +---------+   \n" +
               "  | extends          | extends  | extends            |   \n" +
               "AudioVisual    Electrical   Furniture           Decoration\n" +
               "Resource       Resource     Resource            Resource  \n" +
               "  |                |          |                     |     \n" +
               "  +-- Projector    +-- Ext.   +-- Table             +-- Banner\n" +
               "  +-- Speaker          Cable  +-- Chair             +-- Custom\n" +
               "  +-- Microphone                                        Banner\n\n" +
               "---------------------------------------------------------\n" +
               "INTERFACES IMPLEMENTED BY CONCRETE CLASSES:\n" +
               "---------------------------------------------------------\n" +
               "1. Projector:\n" +
               "   -> implements Resource, Reservable, Repairable, Reusable\n" +
               "2. ExtensionCable:\n" +
               "   -> implements Resource, Reservable, Repairable, Reusable\n" +
               "3. Table / Chair:\n" +
               "   -> implements Resource, Reservable, Reusable\n" +
               "4. StandeeFrame:\n" +
               "   -> implements Resource, Reservable, Reusable\n" +
               "5. CustomPrintedBanner:\n" +
               "   -> implements Resource, Reusable (Repurpose output)\n" +
               "6. NameBadge / RegistrationKit:\n" +
               "   -> implements Resource, Reusable\n" +
               "=========================================================\n";
    }
}
