package com.eventloop.ui.panels;

import com.eventloop.exception.DuplicateResourceException;
import com.eventloop.exception.ValidationException;
import com.eventloop.model.resources.AbstractResource;
import com.eventloop.model.resources.ResourceFactory;
import com.eventloop.service.AuthService;
import com.eventloop.service.ResourceService;
import com.eventloop.ui.components.ModernUIUtils;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

public class AddEditResourceDialog extends JDialog {
    private final ResourceService resourceService = ResourceService.getInstance();
    private final AuthService authService = AuthService.getInstance();
    private final AbstractResource existingResource;
    private boolean saved = false;

    private JTextField txtId;
    private JTextField txtName;
    private JComboBox<String> cmbCategory;
    private JTextField txtDesc;
    private JSpinner spnQuantity;
    private JTextField txtUnit;
    private JSpinner spnCondition;
    private JComboBox<String> cmbStatus;
    private JComboBox<String> cmbVerification;
    private JTextField txtBuilding;
    private JTextField txtRoom;
    private JTextField txtRack;
    private JTextField txtShelf;
    private JTextField txtBox;
    private JTextField txtBuyCost;
    private JTextField txtRepairCost;
    private JComboBox<String> cmbReuseType;
    private JTextArea txtSpecs;
    private JTextArea txtNotes;

    public AddEditResourceDialog(Frame owner, AbstractResource resourceToEdit) {
        super(owner, resourceToEdit == null ? "Add New Inventory Resource" : "Edit Resource: " + resourceToEdit.getResourceId(), true);
        this.existingResource = resourceToEdit;
        setSize(720, 680);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        getContentPane().setBackground(ModernUIUtils.COLOR_BG);

        initComponents();
        if (existingResource != null) {
            populateFields();
        } else {
            generateDefaultId();
        }
    }

    private void initComponents() {
        JPanel form = new JPanel(new GridLayout(13, 2, 10, 8));
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));

        txtId = new JTextField();
        txtName = new JTextField();
        cmbCategory = new JComboBox<>(new String[]{
                "Audio/Visual", "Electrical", "Furniture", "Equipment", 
                "Decoration", "Stationery", "Sports", "Registration Material", "Other"
        });
        cmbCategory.addActionListener(e -> {
            if (existingResource == null) generateDefaultId();
        });

        txtDesc = new JTextField();
        spnQuantity = new JSpinner(new SpinnerNumberModel(1, 1, 10000, 1));
        txtUnit = new JTextField("pcs");
        spnCondition = new JSpinner(new SpinnerNumberModel(5, 1, 5, 1));
        cmbStatus = new JComboBox<>(new String[]{
                "AVAILABLE", "RESERVED", "IN_USE", "UNDER_REPAIR", "REPURPOSE_REQUIRED", "MISSING", "RETIRED"
        });
        cmbVerification = new JComboBox<>(new String[]{"VERIFIED", "VERIFICATION_REQUIRED", "EXPIRED"});

        txtBuilding = new JTextField("Central Store");
        txtRoom = new JTextField("Room 101");
        txtRack = new JTextField("Rack A");
        txtShelf = new JTextField("Shelf 1");
        txtBox = new JTextField("Box 1");

        txtBuyCost = new JTextField("1500.00");
        txtRepairCost = new JTextField("0.00");

        cmbReuseType = new JComboBox<>(new String[]{
                "DIRECT_REUSE", "MODIFICATION_REQUIRED", "REPURPOSE", "RECYCLE", "RETIRE"
        });

        txtSpecs = new JTextArea(2, 20);
        txtNotes = new JTextArea(2, 20);

        addField(form, "Resource ID (e.g. EL-EQP-001):*", txtId);
        addField(form, "Resource Name:*", txtName);
        addField(form, "Category:*", cmbCategory);
        addField(form, "Description:", txtDesc);
        addField(form, "Total Quantity:*", spnQuantity);
        addField(form, "Unit (pcs, sets, rolls):", txtUnit);
        addField(form, "Condition Rating (1-5):*", spnCondition);
        addField(form, "Current Status:*", cmbStatus);
        addField(form, "Verification Status:*", cmbVerification);
        addField(form, "Storage Building / Block:", txtBuilding);
        addField(form, "Storage Room Number:", txtRoom);
        addField(form, "Rack / Shelf / Box:", createLocationRow());
        addField(form, "New Purchase Cost (₹):*", txtBuyCost);

        JPanel bottomForm = new JPanel(new GridLayout(3, 2, 10, 8));
        bottomForm.setOpaque(false);
        bottomForm.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 20));

        addField(bottomForm, "Estimated Repair Cost (₹):", txtRepairCost);
        addField(bottomForm, "Reuse Classification:", cmbReuseType);

        JPanel specsPanel = new JPanel(new BorderLayout());
        specsPanel.setOpaque(false);
        specsPanel.add(new JLabel("Technical Specs:"), BorderLayout.NORTH);
        specsPanel.add(new JScrollPane(txtSpecs), BorderLayout.CENTER);

        JPanel notesPanel = new JPanel(new BorderLayout());
        notesPanel.setOpaque(false);
        notesPanel.add(new JLabel("Notes / Advice:"), BorderLayout.NORTH);
        notesPanel.add(new JScrollPane(txtNotes), BorderLayout.CENTER);

        bottomForm.add(specsPanel);
        bottomForm.add(notesPanel);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(form, BorderLayout.NORTH);
        centerPanel.add(bottomForm, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnPanel.setOpaque(false);

        JButton btnCancel = ModernUIUtils.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = ModernUIUtils.createPrimaryButton("Save Resource");
        btnSave.addActionListener(e -> saveResource());

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);

        add(new JScrollPane(centerPanel), BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private JPanel createLocationRow() {
        JPanel p = new JPanel(new GridLayout(1, 3, 4, 0));
        p.setOpaque(false);
        p.add(txtRack);
        p.add(txtShelf);
        p.add(txtBox);
        return p;
    }

    private void addField(JPanel pnl, String label, java.awt.Component comp) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(ModernUIUtils.FONT_BOLD);
        pnl.add(lbl);
        pnl.add(comp);
    }

    private void generateDefaultId() {
        String cat = (String) cmbCategory.getSelectedItem();
        String prefix = "EQP";
        if (cat != null) {
            if (cat.contains("Audio")) prefix = "AUD";
            else if (cat.contains("Elect")) prefix = "ELE";
            else if (cat.contains("Furn")) prefix = "FUR";
            else if (cat.contains("Deco")) prefix = "DEC";
            else if (cat.contains("Stat") || cat.contains("Reg")) prefix = "STA";
            else if (cat.contains("Sport")) prefix = "SPT";
        }
        int rand = (int) (Math.random() * 900) + 100;
        txtId.setText("EL-" + prefix + "-" + rand);
    }

    private void populateFields() {
        txtId.setText(existingResource.getResourceId());
        txtId.setEditable(false);
        txtName.setText(existingResource.getResourceName());
        cmbCategory.setSelectedItem(existingResource.getCategory());
        txtDesc.setText(existingResource.getDescription());
        spnQuantity.setValue(existingResource.getQuantity());
        txtUnit.setText(existingResource.getUnit());
        spnCondition.setValue(existingResource.getConditionRating());
        cmbStatus.setSelectedItem(existingResource.getCurrentStatus());
        cmbVerification.setSelectedItem(existingResource.getVerificationStatus());
        txtBuilding.setText(existingResource.getStorageBuilding());
        txtRoom.setText(existingResource.getStorageRoom());
        txtRack.setText(existingResource.getRackNumber());
        txtShelf.setText(existingResource.getShelfNumber());
        txtBox.setText(existingResource.getBoxNumber());
        txtBuyCost.setText(String.valueOf(existingResource.getPurchaseCost()));
        txtRepairCost.setText(String.valueOf(existingResource.getEstimatedRepairCost()));
        cmbReuseType.setSelectedItem(existingResource.getReuseType());
        txtSpecs.setText(existingResource.getSpecifications());
        txtNotes.setText(existingResource.getNotes());
    }

    private void saveResource() {
        try {
            String id = txtId.getText().trim();
            String name = txtName.getText().trim();
            String cat = (String) cmbCategory.getSelectedItem();
            int qty = (Integer) spnQuantity.getValue();
            int cond = (Integer) spnCondition.getValue();
            double buyCost = Double.parseDouble(txtBuyCost.getText().trim());
            double repCost = Double.parseDouble(txtRepairCost.getText().trim());

            AbstractResource r = (existingResource != null) ? existingResource : ResourceFactory.createResource(cat, name);
            r.setResourceId(id);
            r.setResourceName(name);
            r.setCategory(cat);
            r.setDescription(txtDesc.getText().trim());
            r.setQuantity(qty);
            if (existingResource == null) {
                r.setAvailableQuantity(qty);
            }
            r.setUnit(txtUnit.getText().trim());
            r.setConditionRating(cond);
            r.setCurrentStatus((String) cmbStatus.getSelectedItem());
            r.setVerificationStatus((String) cmbVerification.getSelectedItem());
            r.setStorageBuilding(txtBuilding.getText().trim());
            r.setStorageRoom(txtRoom.getText().trim());
            r.setRackNumber(txtRack.getText().trim());
            r.setShelfNumber(txtShelf.getText().trim());
            r.setBoxNumber(txtBox.getText().trim());
            r.setPurchaseCost(buyCost);
            r.setEstimatedRepairCost(repCost);
            r.setReuseType((String) cmbReuseType.getSelectedItem());
            r.setSpecifications(txtSpecs.getText().trim());
            r.setNotes(txtNotes.getText().trim());

            String user = authService.isLoggedIn() ? authService.getCurrentUser().getFullName() : "Admin";

            if (existingResource == null) {
                resourceService.addResource(r, user);
                JOptionPane.showMessageDialog(this, "Resource " + id + " registered successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                resourceService.updateResource(r, user);
                JOptionPane.showMessageDialog(this, "Resource " + id + " updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
            saved = true;
            dispose();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numeric costs.", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (ValidationException | DuplicateResourceException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Warning", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
