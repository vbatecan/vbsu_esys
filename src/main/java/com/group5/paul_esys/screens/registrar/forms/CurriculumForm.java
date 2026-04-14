/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.group5.paul_esys.screens.registrar.forms;

import com.group5.paul_esys.modules.courses.model.Course;
import com.group5.paul_esys.modules.courses.services.CourseService;
import com.group5.paul_esys.modules.curriculum.model.Curriculum;
import com.group5.paul_esys.modules.curriculum.services.CurriculumService;
import com.group5.paul_esys.utils.FormValidationUtil;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;

/**
 *
 * @author nytri
 */
public class CurriculumForm extends javax.swing.JFrame {
	
	private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(CurriculumForm.class.getName());
        private static final int MIN_CURRICULUM_NAME_LENGTH = 4;
        private static final int MAX_CURRICULUM_NAME_LENGTH = 30;
        private static final Pattern CURRICULUM_NAME_PATTERN = Pattern.compile("^[A-Za-z0-9]+$");

        private final CourseService courseService = CourseService.getInstance();
        private final CurriculumService curriculumService = CurriculumService.getInstance();
        private final Map<String, Long> courseIdByName = new LinkedHashMap<>();
        private final Runnable onSavedCallback;
        private final Curriculum editingCurriculum;

	/**
	 * Creates new form Curriculum
	 */
	public CurriculumForm() {
                this(null, null);
        }

        public CurriculumForm(Curriculum editingCurriculum, Runnable onSavedCallback) {
                this.editingCurriculum = editingCurriculum;
                this.onSavedCallback = onSavedCallback;
                this.setUndecorated(true);
		initComponents();
                this.setLocationRelativeTo(null);
                btnSave.addActionListener(this::btnSaveActionPerformed);
                initializeForm();
        }

        private void initializeForm() {
                int currentYear = LocalDate.now().getYear();
                spinnerYear.setModel(new javax.swing.SpinnerNumberModel(currentYear, 2000, 2100, 1));
                loadCourses();

                if (editingCurriculum == null) {
                        return;
                }

                jLabel3.setText("Update Curriculum");
                btnSave.setText("Update");

                if (editingCurriculum.getCurYear() != null) {
                        LocalDate localDate = editingCurriculum
                                .getCurYear()
                                .toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate();
                        spinnerYear.setValue(localDate.getYear());
                }

                courseService
                        .getCourseById(editingCurriculum.getCourse())
                        .ifPresent(course -> cbxCourse.setSelectedItem(course.getCourseName()));
        }

        private void loadCourses() {
                cbxCourse.removeAllItems();
                courseIdByName.clear();

                for (Course course : courseService.getAllCourses()) {
                        cbxCourse.addItem(course.getCourseName());
                        courseIdByName.put(course.getCourseName(), course.getId());
                }
        }

        private String buildCurriculumName(String courseName, int year) {
                String cleaned = courseName.replaceAll("[^A-Za-z0-9 ]", " ").trim();
                if (cleaned.isEmpty()) {
                        return "CUR" + year;
                }

                String[] parts = cleaned.split("\\s+");
                StringBuilder code = new StringBuilder();
                for (String part : parts) {
                        if (!part.isEmpty() && code.length() < 6) {
                                code.append(Character.toUpperCase(part.charAt(0)));
                        }
                }

                if (code.length() < 2) {
                        String compact = cleaned.replaceAll("\\s+", "").toUpperCase();
                        code = new StringBuilder(compact.substring(0, Math.min(3, compact.length())));
                }

                return code + String.valueOf(year);
        }

        private void saveCurriculum() {
                Object selectedCourse = cbxCourse.getSelectedItem();
                if (showValidationError(FormValidationUtil.validateMappedSelection("Course", selectedCourse, courseIdByName))) {
                        return;
                }

                if (showValidationError(FormValidationUtil.validateNumberRange(
                        "Year",
                        (Number) spinnerYear.getValue(),
                        1900,
                        3000
                ))) {
                        return;
                }

                int selectedYear = (Integer) spinnerYear.getValue();
                Long selectedCourseId = courseIdByName.get(selectedCourse.toString());
                Date curriculumYear = java.sql.Date.valueOf(LocalDate.of(selectedYear, 1, 1));
                String generatedCurriculumName = buildCurriculumName(selectedCourse.toString(), selectedYear);

                if (showValidationError(
                        FormValidationUtil.validateRequiredText(
                                "Curriculum name",
                                generatedCurriculumName,
                                MIN_CURRICULUM_NAME_LENGTH,
                                MAX_CURRICULUM_NAME_LENGTH,
                                CURRICULUM_NAME_PATTERN,
                                "letters and numbers"
                        )
                )) {
                        return;
                }

                Curriculum curriculum = editingCurriculum == null ? new Curriculum() : editingCurriculum;
                curriculum
                        .setName(generatedCurriculumName)
                        .setCurYear(curriculumYear)
                        .setCourse(selectedCourseId);

                boolean success = editingCurriculum == null
                        ? curriculumService.createCurriculum(curriculum)
                        : curriculumService.updateCurriculum(curriculum);

                if (!success) {
                        JOptionPane.showMessageDialog(
                                this,
                                "Failed to save curriculum. Please try again.",
                                "Save Failed",
                                JOptionPane.ERROR_MESSAGE
                        );
                        return;
                }

                JOptionPane.showMessageDialog(
                        this,
                        editingCurriculum == null
                                ? "Curriculum created successfully."
                                : "Curriculum updated successfully.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                );

                if (onSavedCallback != null) {
                        onSavedCallback.run();
                }

                dispose();
	}

        private boolean showValidationError(Optional<String> validationError) {
                if (validationError.isEmpty()) {
                        return false;
                }

                JOptionPane.showMessageDialog(
                        this,
                        validationError.get(),
                        "Validation Error",
                        JOptionPane.WARNING_MESSAGE
                );
                return true;
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
                spinnerYear = new javax.swing.JSpinner();
                jLabel2 = new javax.swing.JLabel();
                cbxCourse = new javax.swing.JComboBox<>();
                btnSave = new javax.swing.JButton();
                btnCancel = new javax.swing.JButton();
                jLabel3 = new javax.swing.JLabel();

                setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
                setBackground(new java.awt.Color(255, 255, 255));

                windowBar1.setTitle("Curriculum Form");

                jPanel1.setBackground(new java.awt.Color(255, 255, 255));
                jPanel1.setAlignmentX(0.5F);
                jPanel1.setAlignmentY(0.5F);
                jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

                jLabel1.setFont(new java.awt.Font("Poppins", 0, 12)); // NOI18N
                jLabel1.setText("Year");
                jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(63, 72, 300, -1));

                spinnerYear.setModel(new javax.swing.SpinnerNumberModel());
                spinnerYear.setBorder(new com.group5.paul_esys.ui.TextFieldRoundBorder());
                jPanel1.add(spinnerYear, new org.netbeans.lib.awtextra.AbsoluteConstraints(63, 97, 300, -1));

                jLabel2.setFont(new java.awt.Font("Poppins", 0, 12)); // NOI18N
                jLabel2.setText("Course");
                jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(63, 142, 300, -1));

                cbxCourse.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
                jPanel1.add(cbxCourse, new org.netbeans.lib.awtextra.AbsoluteConstraints(63, 167, 300, -1));

                btnSave.setBackground(new java.awt.Color(255, 234, 234));
                btnSave.setText("Save");
                jPanel1.add(btnSave, new org.netbeans.lib.awtextra.AbsoluteConstraints(291, 208, -1, 36));

                btnCancel.setText("Cancel");
                btnCancel.addActionListener(this::btnCancelActionPerformed);
                jPanel1.add(btnCancel, new org.netbeans.lib.awtextra.AbsoluteConstraints(213, 208, -1, 36));

                jLabel3.setFont(new java.awt.Font("Poppins", 0, 18)); // NOI18N
                jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                jLabel3.setText("Create new Curriculum");
                jPanel1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(63, 26, 300, -1));

                javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
                getContentPane().setLayout(layout);
                layout.setHorizontalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 428, Short.MAX_VALUE)
                        .addComponent(windowBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                );
                layout.setVerticalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(windowBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 276, Short.MAX_VALUE))
                );

                pack();
        }// </editor-fold>//GEN-END:initComponents

        private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
		this.dispose();
        }//GEN-LAST:event_btnCancelActionPerformed

        private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {
                saveCurriculum();
        }

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

		/* Create and display the form */
		java.awt.EventQueue.invokeLater(() -> new CurriculumForm().setVisible(true));
	}

        // Variables declaration - do not modify//GEN-BEGIN:variables
        private javax.swing.JButton btnCancel;
        private javax.swing.JButton btnSave;
        private javax.swing.JComboBox<String> cbxCourse;
        private javax.swing.JLabel jLabel1;
        private javax.swing.JLabel jLabel2;
        private javax.swing.JLabel jLabel3;
        private javax.swing.JPanel jPanel1;
        private javax.swing.JSpinner spinnerYear;
        private com.group5.paul_esys.components.WindowBar windowBar1;
        // End of variables declaration//GEN-END:variables
}
