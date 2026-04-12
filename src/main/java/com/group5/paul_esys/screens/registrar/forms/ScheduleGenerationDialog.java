package com.group5.paul_esys.screens.registrar.forms;

import com.group5.paul_esys.modules.registrar.model.ScheduleGenerationPlanRow;
import com.group5.paul_esys.modules.registrar.model.ScheduleGenerationRequest;
import com.group5.paul_esys.modules.registrar.model.ScheduleGenerationResult;
import com.group5.paul_esys.modules.registrar.model.ScheduleGenerationSectionOption;
import com.group5.paul_esys.modules.registrar.services.RegistrarScheduleManagementService;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

public class ScheduleGenerationDialog extends JDialog {

  private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

  private final RegistrarScheduleManagementService scheduleManagementService;
  private final Runnable onGenerationCommitted;

  private final JComboBox<String> cbxSectionPeriod = new JComboBox<>();
  private final JFormattedTextField txtMinStartTime = new JFormattedTextField();
  private final JFormattedTextField txtMaxEndTime = new JFormattedTextField();
  private final JLabel lblSummary = new JLabel("Run preview to view generated schedule rows.");

  private final DefaultTableModel tableModel = new DefaultTableModel(
      new Object[][]{},
      new String[]{"Subject Code", "Subject Name", "Pattern", "Block", "Estimated (min)", "Day", "Time", "Room", "Status", "Details"}
  ) {
    @Override
    public boolean isCellEditable(int row, int column) {
      return false;
    }
  };

  private final JTable tblPreview = new JTable(tableModel);
  private final List<ScheduleGenerationSectionOption> sectionOptions = new ArrayList<>();

  private ScheduleGenerationResult lastPreviewResult;

  public ScheduleGenerationDialog(
      Frame parent,
      RegistrarScheduleManagementService scheduleManagementService,
      Runnable onGenerationCommitted
  ) {
    super(parent, true);
    this.scheduleManagementService = scheduleManagementService;
    this.onGenerationCommitted = onGenerationCommitted;

    setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    setTitle("Auto Schedule Generator");

    initializeUi();
    initializeData();
    pack();
    setLocationRelativeTo(parent);
  }

  private void initializeUi() {
    JPanel rootPanel = new JPanel(new BorderLayout(0, 10));
    rootPanel.setBorder(BorderFactory.createEmptyBorder(14, 14, 12, 14));
    rootPanel.setBackground(Color.WHITE);

    JLabel lblTitle = new JLabel("Generate Section Schedules");
    lblTitle.setFont(new java.awt.Font("Poppins", 0, 20));

    JLabel lblSubtitle = new JLabel("Preview and generate conflict-aware schedules within your selected time window.");
    lblSubtitle.setFont(new java.awt.Font("Poppins", 0, 12));
    lblSubtitle.setForeground(new Color(120, 120, 120));

    JPanel headerPanel = new JPanel(new BorderLayout(0, 2));
    headerPanel.setOpaque(false);
    headerPanel.add(lblTitle, BorderLayout.NORTH);
    headerPanel.add(lblSubtitle, BorderLayout.CENTER);

    JPanel filtersPanel = new JPanel(new GridBagLayout());
    filtersPanel.setOpaque(false);

    cbxSectionPeriod.setFont(new java.awt.Font("Poppins", 0, 12));

    txtMinStartTime.setFont(new java.awt.Font("Poppins", 0, 12));
    txtMaxEndTime.setFont(new java.awt.Font("Poppins", 0, 12));
    txtMinStartTime.setToolTipText("Use HH:mm format (example: 07:30)");
    txtMaxEndTime.setToolTipText("Use HH:mm format (example: 18:00)");

    txtMinStartTime.setText("07:30");
    txtMaxEndTime.setText("18:00");

    addField(filtersPanel, "Section + Enrollment Period", cbxSectionPeriod, 0);
    addField(filtersPanel, "Minimum Start Time (HH:mm)", txtMinStartTime, 1);
    addField(filtersPanel, "Maximum End Time (HH:mm)", txtMaxEndTime, 2);

    tblPreview.setRowHeight(24);
    tblPreview.setFillsViewportHeight(true);
    JScrollPane scrollPane = new JScrollPane(tblPreview);
    scrollPane.setPreferredSize(new Dimension(920, 300));

    lblSummary.setFont(new java.awt.Font("Poppins", 0, 12));
    lblSummary.setHorizontalAlignment(SwingConstants.LEFT);
    lblSummary.setForeground(new Color(75, 75, 75));

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
    buttonPanel.setOpaque(false);

    JButton btnPreview = new JButton("Preview");
    JButton btnGenerate = new JButton("Generate");
    JButton btnClose = new JButton("Close");

    btnPreview.addActionListener(evt -> previewGeneration());
    btnGenerate.addActionListener(evt -> executeGeneration());
    btnClose.addActionListener(evt -> dispose());

    buttonPanel.add(btnPreview);
    buttonPanel.add(btnGenerate);
    buttonPanel.add(btnClose);

    JPanel centerPanel = new JPanel(new BorderLayout(0, 8));
    centerPanel.setOpaque(false);
    centerPanel.add(filtersPanel, BorderLayout.NORTH);
    centerPanel.add(scrollPane, BorderLayout.CENTER);
    centerPanel.add(lblSummary, BorderLayout.SOUTH);

    rootPanel.add(headerPanel, BorderLayout.NORTH);
    rootPanel.add(centerPanel, BorderLayout.CENTER);
    rootPanel.add(buttonPanel, BorderLayout.SOUTH);

    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(rootPanel, BorderLayout.CENTER);
  }

  private void initializeData() {
    sectionOptions.clear();
    sectionOptions.addAll(scheduleManagementService.getSectionGenerationOptions());

    cbxSectionPeriod.removeAllItems();
    if (sectionOptions.isEmpty()) {
      cbxSectionPeriod.addItem("No section options available");
      cbxSectionPeriod.setEnabled(false);
      lblSummary.setText("No offerings found. Create offerings first before generating schedules.");
      return;
    }

    cbxSectionPeriod.setEnabled(true);
    for (ScheduleGenerationSectionOption option : sectionOptions) {
      String capacity = option.sectionCapacity() == null || option.sectionCapacity() <= 0
          ? "Open"
          : String.valueOf(option.sectionCapacity());
      cbxSectionPeriod.addItem(
          option.sectionCode() + " | " + option.enrollmentPeriodLabel() + " | Cap " + capacity
      );
    }
  }

  private void previewGeneration() {
    ScheduleGenerationRequest request = buildRequest();
    if (request == null) {
      return;
    }

    lblSummary.setText("Preparing preview...");
    runGenerationTask(request, false);
  }

  private void executeGeneration() {
    ScheduleGenerationRequest request = buildRequest();
    if (request == null) {
      return;
    }

    if (lastPreviewResult == null) {
      int choice = JOptionPane.showConfirmDialog(
          this,
          "No preview found yet. Generate now anyway?",
          "Generate Without Preview",
          JOptionPane.YES_NO_OPTION,
          JOptionPane.WARNING_MESSAGE
      );

      if (choice != JOptionPane.YES_OPTION) {
        return;
      }
    }

    int confirm = JOptionPane.showConfirmDialog(
        this,
        "Generate schedules for the selected section now?",
        "Confirm Generation",
        JOptionPane.YES_NO_OPTION,
        JOptionPane.QUESTION_MESSAGE
    );

    if (confirm != JOptionPane.YES_OPTION) {
      return;
    }

    lblSummary.setText("Generating schedules...");
    runGenerationTask(request, true);
  }

  private void runGenerationTask(ScheduleGenerationRequest request, boolean persist) {
    new SwingWorker<ScheduleGenerationResult, Void>() {
      @Override
      protected ScheduleGenerationResult doInBackground() {
        return persist
            ? scheduleManagementService.generateSectionSchedules(request)
            : scheduleManagementService.previewSectionScheduleGeneration(request);
      }

      @Override
      protected void done() {
        try {
          ScheduleGenerationResult result = get();
          if (!persist) {
            lastPreviewResult = result;
          }

          populatePreviewRows(result.rows());
          lblSummary.setText(result.message());

          if (!result.successful()) {
            JOptionPane.showMessageDialog(
                ScheduleGenerationDialog.this,
                result.message(),
                "Schedule Generator",
                JOptionPane.ERROR_MESSAGE
            );
            return;
          }

          if (persist && onGenerationCommitted != null) {
            onGenerationCommitted.run();
          }
        } catch (Exception ex) {
          JOptionPane.showMessageDialog(
              ScheduleGenerationDialog.this,
              "Error processing generation: " + ex.getMessage(),
              "Schedule Generator",
              JOptionPane.ERROR_MESSAGE
          );
        }
      }
    }.execute();
  }

  private ScheduleGenerationRequest buildRequest() {
    int selectedIndex = cbxSectionPeriod.getSelectedIndex();
    if (selectedIndex < 0 || selectedIndex >= sectionOptions.size()) {
      JOptionPane.showMessageDialog(
          this,
          "Please select a section and enrollment period.",
          "Schedule Generator",
          JOptionPane.WARNING_MESSAGE
      );
      return null;
    }

    LocalTime minStart = parseTime(txtMinStartTime.getText());
    LocalTime maxEnd = parseTime(txtMaxEndTime.getText());

    if (minStart == null || maxEnd == null) {
      JOptionPane.showMessageDialog(
          this,
          "Please use HH:mm format for both time fields.",
          "Schedule Generator",
          JOptionPane.WARNING_MESSAGE
      );
      return null;
    }

    if (!minStart.isBefore(maxEnd)) {
      JOptionPane.showMessageDialog(
          this,
          "Minimum start time must be earlier than maximum end time.",
          "Schedule Generator",
          JOptionPane.WARNING_MESSAGE
      );
      return null;
    }

    ScheduleGenerationSectionOption selected = sectionOptions.get(selectedIndex);
    return new ScheduleGenerationRequest(
        selected.sectionId(),
        selected.enrollmentPeriodId(),
        minStart,
        maxEnd,
        30
    );
  }

  private void populatePreviewRows(List<ScheduleGenerationPlanRow> rows) {
    tableModel.setRowCount(0);

    for (ScheduleGenerationPlanRow row : rows) {
      String timeRange = row.startTime() == null || row.endTime() == null
          ? "N/A"
          : row.startTime().format(TIME_FORMATTER) + " - " + row.endTime().format(TIME_FORMATTER);

      tableModel.addRow(new Object[]{
          row.subjectCode(),
          row.subjectName(),
          formatPattern(row.schedulePattern()),
          row.blockLabel() == null ? "N/A" : row.blockLabel(),
          row.estimatedMinutes() == null ? "N/A" : row.estimatedMinutes(),
          row.day() == null ? "N/A" : row.day(),
          timeRange,
          row.roomLabel() == null ? "N/A" : row.roomLabel(),
          row.status(),
          row.details()
      });
    }
  }

  private LocalTime parseTime(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }

    try {
      return LocalTime.parse(value.trim(), TIME_FORMATTER);
    } catch (DateTimeParseException ex) {
      return null;
    }
  }

  private String formatPattern(String pattern) {
    if (pattern == null || pattern.isBlank()) {
      return "LECTURE ONLY";
    }

    return pattern.replace('_', ' ');
  }

  private void addField(JPanel panel, String labelText, java.awt.Component component, int row) {
    GridBagConstraints gbcLabel = new GridBagConstraints();
    gbcLabel.gridx = 0;
    gbcLabel.gridy = row;
    gbcLabel.insets = new Insets(2, 2, 4, 8);
    gbcLabel.anchor = GridBagConstraints.WEST;

    JLabel label = new JLabel(labelText);
    label.setFont(new java.awt.Font("Poppins", 0, 12));
    panel.add(label, gbcLabel);

    GridBagConstraints gbcField = new GridBagConstraints();
    gbcField.gridx = 1;
    gbcField.gridy = row;
    gbcField.weightx = 1.0;
    gbcField.fill = GridBagConstraints.HORIZONTAL;
    gbcField.insets = new Insets(2, 0, 6, 2);
    panel.add(component, gbcField);
  }
}
