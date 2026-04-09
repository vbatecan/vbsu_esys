/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package com.group5.paul_esys.screens.registrar.forms;

import com.group5.paul_esys.modules.curriculum.model.Curriculum;
import com.group5.paul_esys.modules.curriculum.services.CurriculumService;
import com.group5.paul_esys.modules.departments.model.Department;
import com.group5.paul_esys.modules.departments.services.DepartmentService;
import com.group5.paul_esys.modules.subjects.model.Subject;
import com.group5.paul_esys.modules.subjects.services.SubjectService;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import javax.swing.JOptionPane;

/**
 *
 * @author nytri
 */
public class SubjectForm extends javax.swing.JDialog {
	
	private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(SubjectForm.class.getName());
        private final SubjectService subjectService = SubjectService.getInstance();
        private final DepartmentService departmentService = DepartmentService.getInstance();
        private final CurriculumService curriculumService = CurriculumService.getInstance();

        private final Map<String, Long> curriculumIdByLabel = new LinkedHashMap<>();
        private final Map<String, Long> departmentIdByLabel = new LinkedHashMap<>();
        private final Map<Long, String> curriculumLabelById = new LinkedHashMap<>();
        private final Map<Long, String> departmentLabelById = new LinkedHashMap<>();

        private final Subject editingSubject;
        private final Runnable onSavedCallback;

	/**
	 * Creates new form SubjectForm
	 */
	public SubjectForm(java.awt.Frame parent, boolean modal) {
		this(parent, modal, null, null);
	}

        public SubjectForm(java.awt.Frame parent, boolean modal, Subject editingSubject, Runnable onSavedCallback) {
		super(parent, modal);
		this.editingSubject = editingSubject;
		this.onSavedCallback = onSavedCallback;
		this.setUndecorated(true);
		initComponents();
		this.setLocationRelativeTo(null);
		initializeForm();
	}

        private void initializeForm() {
                jButton1.addActionListener(this::jButton1ActionPerformed);
                jButton2.addActionListener(this::jButton2ActionPerformed);

                jLabel4.setText("Subject Code");
                jSpinner1.setModel(new javax.swing.SpinnerNumberModel(Float.valueOf(1.0f), Float.valueOf(0.5f), Float.valueOf(25.0f), Float.valueOf(0.5f)));

                loadCurriculums();
                loadDepartments();

                if (editingSubject == null) {
                        return;
                }

                jLabel1.setText("Update Subject");
                jLabel2.setText("Update existing subject");
                jButton1.setText("Update");

                jTextField1.setText(editingSubject.getSubjectName());
                jTextField2.setText(editingSubject.getSubjectCode());
                jTextArea1.setText(editingSubject.getDescription() == null ? "" : editingSubject.getDescription());

                if (editingSubject.getUnits() != null) {
                        jSpinner1.setValue(editingSubject.getUnits());
                }

                String curriculumLabel = curriculumLabelById.get(editingSubject.getCurriculumId());
                if (curriculumLabel != null) {
                        jComboBox1.setSelectedItem(curriculumLabel);
                }

                String departmentLabel = departmentLabelById.get(editingSubject.getDepartmentId());
                if (departmentLabel != null) {
                        jComboBox2.setSelectedItem(departmentLabel);
                }
        }

        private void loadCurriculums() {
                jComboBox1.removeAllItems();
                curriculumIdByLabel.clear();
                curriculumLabelById.clear();

                for (Curriculum curriculum : curriculumService.getAllCurriculums()) {
                        String label = buildCurriculumLabel(curriculum);
                        jComboBox1.addItem(label);
                        curriculumIdByLabel.put(label, curriculum.getId());
                        curriculumLabelById.put(curriculum.getId(), label);
                }
        }

        private String buildCurriculumLabel(Curriculum curriculum) {
                String name = curriculum.getName() == null || curriculum.getName().trim().isEmpty()
                        ? "Curriculum"
                        : curriculum.getName().trim();

                String yearLabel = "N/A";
                if (curriculum.getCurYear() != null) {
                        yearLabel = String.valueOf(extractYear(curriculum.getCurYear()));
                }

                return name + " (" + yearLabel + ") - ID " + curriculum.getId();
	}

        private int extractYear(Date date) {
                if (date instanceof java.sql.Date sqlDate) {
                        return sqlDate.toLocalDate().getYear();
                }

                LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                return localDate.getYear();
        }

        private void loadDepartments() {
                jComboBox2.removeAllItems();
                departmentIdByLabel.clear();
                departmentLabelById.clear();

                for (Department department : departmentService.getAllDepartments()) {
                        String label = buildDepartmentLabel(department);
                        jComboBox2.addItem(label);
                        departmentIdByLabel.put(label, department.getId());
                        departmentLabelById.put(department.getId(), label);
                }
	}

        private String buildDepartmentLabel(Department department) {
                String name = department.getDepartmentName() == null || department.getDepartmentName().trim().isEmpty()
                        ? "Department"
                        : department.getDepartmentName().trim();
                return name + " - ID " + department.getId();
	}

        private boolean isValidForm() {
                if (jTextField1.getText() == null || jTextField1.getText().trim().isEmpty()) {
                        JOptionPane.showMessageDialog(
                                this,
                                "Subject name is required.",
                                "Validation Error",
                                JOptionPane.WARNING_MESSAGE
                        );
                        return false;
                }

                if (jTextField2.getText() == null || jTextField2.getText().trim().isEmpty()) {
                        JOptionPane.showMessageDialog(
                                this,
                                "Subject code is required.",
                                "Validation Error",
                                JOptionPane.WARNING_MESSAGE
                        );
                        return false;
                }

                if (!hasValidUnits()) {
                        JOptionPane.showMessageDialog(
                                this,
                                "Units must be greater than zero.",
                                "Validation Error",
                                JOptionPane.WARNING_MESSAGE
                        );
                        return false;
                }

                if (!isValidCurriculumSelection()) {
                        JOptionPane.showMessageDialog(
                                this,
                                "Please select a curriculum.",
                                "Validation Error",
                                JOptionPane.WARNING_MESSAGE
                        );
                        return false;
                }

                if (!isValidDepartmentSelection()) {
                        JOptionPane.showMessageDialog(
                                this,
                                "Please select a department.",
                                "Validation Error",
                                JOptionPane.WARNING_MESSAGE
                        );
                        return false;
                }

                if (!isSubjectCodeAvailable()) {
                        JOptionPane.showMessageDialog(
                                this,
                                "Subject code already exists. Please use a unique code.",
                                "Validation Error",
                                JOptionPane.WARNING_MESSAGE
                        );
                        return false;
                }

                return true;
	}

        private boolean hasValidUnits() {
                return readUnits() > 0;
        }

        private float readUnits() {
                Object unitsValue = jSpinner1.getValue();
                if (unitsValue instanceof Number number) {
                        return number.floatValue();
                }

                try {
                        return Float.parseFloat(unitsValue.toString());
                } catch (NumberFormatException ex) {
                        return 0;
                }
        }

        private boolean isValidCurriculumSelection() {
                Object selectedCurriculum = jComboBox1.getSelectedItem();
                return selectedCurriculum != null
                        && curriculumIdByLabel.containsKey(selectedCurriculum.toString());
        }

        private boolean isValidDepartmentSelection() {
                Object selectedDepartment = jComboBox2.getSelectedItem();
                return selectedDepartment != null
                        && departmentIdByLabel.containsKey(selectedDepartment.toString());
        }

        private boolean isSubjectCodeAvailable() {
                String subjectCode = jTextField2.getText().trim().toUpperCase();
                Optional<Subject> existingSubject = subjectService.getSubjectByCode(subjectCode);
                if (existingSubject.isEmpty()) {
                        return true;
                }

                return editingSubject != null && existingSubject.get().getId().equals(editingSubject.getId());
        }

        private void saveSubject() {
                if (!isValidForm()) {
                        return;
                }

                Long curriculumId = curriculumIdByLabel.get(jComboBox1.getSelectedItem().toString());
                Long departmentId = departmentIdByLabel.get(jComboBox2.getSelectedItem().toString());

                Subject subject = editingSubject == null ? new Subject() : editingSubject;
                subject
                        .setSubjectName(jTextField1.getText().trim())
                        .setSubjectCode(jTextField2.getText().trim().toUpperCase())
                        .setUnits(readUnits())
                        .setDescription(jTextArea1.getText().trim())
                        .setCurriculumId(curriculumId)
                        .setDepartmentId(departmentId);

                boolean success = editingSubject == null
                        ? subjectService.createSubject(subject)
                        : subjectService.updateSubject(subject);

                if (!success) {
                        JOptionPane.showMessageDialog(
                                this,
                                "Failed to save subject. Please try again.",
                                "Save Failed",
                                JOptionPane.ERROR_MESSAGE
                        );
                        return;
                }

                JOptionPane.showMessageDialog(
                        this,
                        editingSubject == null
                                ? "Subject created successfully."
                                : "Subject updated successfully.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                );

                if (onSavedCallback != null) {
                        onSavedCallback.run();
                }

                dispose();
	}

        private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
                saveSubject();
	}

        private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {
                dispose();
	}

	/**
	 * This method is called from within the constructor to initialize the
	 * form. WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
        // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
        private void initComponents() {

                windowBar1 = new com.group5.paul_esys.components.WindowBar();
                jPanel1 = new javax.swing.JPanel();
                jLabel1 = new javax.swing.JLabel();
                jLabel2 = new javax.swing.JLabel();
                jLabel3 = new javax.swing.JLabel();
                jTextField1 = new javax.swing.JTextField();
                jTextField2 = new javax.swing.JTextField();
                jLabel4 = new javax.swing.JLabel();
                jLabel5 = new javax.swing.JLabel();
                jLabel6 = new javax.swing.JLabel();
                jSpinner1 = new javax.swing.JSpinner();
                jComboBox1 = new javax.swing.JComboBox<>();
                jComboBox2 = new javax.swing.JComboBox<>();
                jLabel7 = new javax.swing.JLabel();
                jLabel8 = new javax.swing.JLabel();
                jScrollPane1 = new javax.swing.JScrollPane();
                jTextArea1 = new javax.swing.JTextArea();
                jButton1 = new javax.swing.JButton();
                jButton2 = new javax.swing.JButton();

                setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
                getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.Y_AXIS));

                windowBar1.setTitle("Subject Form");
                getContentPane().add(windowBar1);

                jPanel1.setBackground(new java.awt.Color(255, 255, 255));
                jPanel1.setAlignmentY(-5000.0F);

                jLabel1.setFont(new java.awt.Font("Poppins", 0, 18)); // NOI18N
                jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                jLabel1.setText("Subject Form");

                jLabel2.setForeground(new java.awt.Color(102, 102, 102));
                jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                jLabel2.setText("Add/Update Subject");

                jLabel3.setText("Subject Name");

                jTextField1.setBorder(new com.group5.paul_esys.ui.TextFieldRoundBorder());

                jTextField2.setBorder(new com.group5.paul_esys.ui.TextFieldRoundBorder());

                jLabel4.setText("Subject Name");

                jLabel5.setText("Units");

                jLabel6.setText("Curriculum");

                jSpinner1.setModel(new javax.swing.SpinnerNumberModel(Float.valueOf(0.0f), Float.valueOf(0.0f), Float.valueOf(25.0f), Float.valueOf(1.0f)));
                jSpinner1.setBorder(new com.group5.paul_esys.ui.TextFieldRoundBorder());

                jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

                jComboBox2.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

                jLabel7.setText("Department");

                jLabel8.setText("Description");

                jTextArea1.setColumns(20);
                jTextArea1.setRows(5);
                jScrollPane1.setViewportView(jTextArea1);

                jButton1.setBackground(new java.awt.Color(119, 0, 0));
                jButton1.setForeground(new java.awt.Color(255, 255, 255));
                jButton1.setText("Save");

                jButton2.setBackground(new java.awt.Color(119, 0, 0));
                jButton2.setForeground(new java.awt.Color(255, 255, 255));
                jButton2.setText("Cancel");

                javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
                jPanel1.setLayout(jPanel1Layout);
                jPanel1Layout.setHorizontalGroup(
                        jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                .addGroup(jPanel1Layout.createSequentialGroup()
                                                        .addContainerGap()
                                                        .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 319, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                                        .addGap(42, 42, 42)
                                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                                .addComponent(jLabel5)
                                                                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 320, Short.MAX_VALUE)
                                                                .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                                .addComponent(jTextField1)
                                                                .addComponent(jTextField2)
                                                                .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                                .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                                .addComponent(jSpinner1)
                                                                .addComponent(jLabel6))))
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addGap(43, 43, 43)
                                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                        .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                                                .addGap(1, 1, 1)
                                                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                                                .addComponent(jLabel8)
                                                                                .addComponent(jComboBox2, 0, 319, Short.MAX_VALUE)
                                                                                .addComponent(jScrollPane1))
                                                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                                                                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)))))))
                                .addContainerGap(67, Short.MAX_VALUE))
                );
                jPanel1Layout.setVerticalGroup(
                        jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(21, 21, 21)
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel2)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(10, 10, 10)
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel8)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(39, Short.MAX_VALUE))
                );

                getContentPane().add(jPanel1);

                pack();
        }// </editor-fold>//GEN-END:initComponents

	/**
	 * @param args the command line arguments
	 */
	public static void main(String args[]) {
		/* Set the Nimbus look and feel */
		//<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
		/* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
		 */
		try {
			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
			logger.log(java.util.logging.Level.SEVERE, null, ex);
		}
		//</editor-fold>

		/* Create and display the dialog */
		java.awt.EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				SubjectForm dialog = new SubjectForm(new javax.swing.JFrame(), true);
				dialog.addWindowListener(new java.awt.event.WindowAdapter() {
					@Override
					public void windowClosing(java.awt.event.WindowEvent e) {
						System.exit(0);
					}
				});
				dialog.setVisible(true);
			}
		});
	}

        // Variables declaration - do not modify//GEN-BEGIN:variables
        private javax.swing.JButton jButton1;
        private javax.swing.JButton jButton2;
        private javax.swing.JComboBox<String> jComboBox1;
        private javax.swing.JComboBox<String> jComboBox2;
        private javax.swing.JLabel jLabel1;
        private javax.swing.JLabel jLabel2;
        private javax.swing.JLabel jLabel3;
        private javax.swing.JLabel jLabel4;
        private javax.swing.JLabel jLabel5;
        private javax.swing.JLabel jLabel6;
        private javax.swing.JLabel jLabel7;
        private javax.swing.JLabel jLabel8;
        private javax.swing.JPanel jPanel1;
        private javax.swing.JScrollPane jScrollPane1;
        private javax.swing.JSpinner jSpinner1;
        private javax.swing.JTextArea jTextArea1;
        private javax.swing.JTextField jTextField1;
        private javax.swing.JTextField jTextField2;
        private com.group5.paul_esys.components.WindowBar windowBar1;
        // End of variables declaration//GEN-END:variables
}
