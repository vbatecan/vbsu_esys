package com.group5.paul_esys.screens.registrar.forms;

import com.group5.paul_esys.modules.departments.model.Department;
import com.group5.paul_esys.modules.departments.services.DepartmentService;
import com.group5.paul_esys.utils.FormValidationUtil;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */

/**
 *
 * @author janea
 */
public class DepartmentForm extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(DepartmentForm.class.getName());
    private static final int MIN_DEPARTMENT_NAME_LENGTH = 2;
    private static final int MAX_DEPARTMENT_NAME_LENGTH = 100;
    private static final int MIN_DEPARTMENT_CODE_LENGTH = 2;
    private static final int MAX_DEPARTMENT_CODE_LENGTH = 20;
    private static final int MAX_DESCRIPTION_LENGTH = 500;
    private static final Pattern DEPARTMENT_NAME_PATTERN = Pattern.compile("^[A-Za-z0-9 .,'()\\-/&]+$");
    private static final Pattern DEPARTMENT_CODE_PATTERN = Pattern.compile("^[A-Za-z0-9._\\-/]+$");

    private final DepartmentService departmentService = DepartmentService.getInstance();
    private final Runnable onSavedCallback;
    private final Department editingDepartment;

    /**
     * Creates new form DepForm
     */
    public DepartmentForm() {
        this(null, null);
    }

    public DepartmentForm(Department editingDepartment, Runnable onSavedCallback) {
        this.editingDepartment = editingDepartment;
        this.onSavedCallback = onSavedCallback;
        this.setUndecorated(true);
        initComponents();
        this.setLocationRelativeTo(null);
        initializeForm();
    }

    private void initializeForm() {
        if (editingDepartment == null) {
            return;
        }

        jLabel1.setText("Update Department");
        jLabel2.setText("Update existing department");
        btnSave.setText("Update");

        txtDepart.setText(editingDepartment.getDepartmentName());
        txtDepartmentCode.setText(
            editingDepartment.getDepartmentCode() == null ? "" : editingDepartment.getDepartmentCode()
        );
        txtAreaDescription.setText(
            editingDepartment.getDescription() == null ? "" : editingDepartment.getDescription()
        );
    }

    private boolean isValidForm() {
        if (showValidationError(
            FormValidationUtil.validateRequiredText(
                "Department name",
                txtDepart.getText(),
                MIN_DEPARTMENT_NAME_LENGTH,
                MAX_DEPARTMENT_NAME_LENGTH,
                DEPARTMENT_NAME_PATTERN,
                "letters, numbers, spaces, and . , ' ( ) - / &"
            )
        )) {
            return false;
        }

        if (showValidationError(
            FormValidationUtil.validateRequiredText(
                "Department code",
                txtDepartmentCode.getText(),
                MIN_DEPARTMENT_CODE_LENGTH,
                MAX_DEPARTMENT_CODE_LENGTH,
                DEPARTMENT_CODE_PATTERN,
                "letters, numbers, and . _ - /"
            )
        )) {
            return false;
        }

        if (showValidationError(
            FormValidationUtil.validateOptionalText(
                "Description",
                txtAreaDescription.getText(),
                1,
                MAX_DESCRIPTION_LENGTH,
                null,
                ""
            )
        )) {
            return false;
        }

        return true;
    }

    private void saveDepartment() {
        if (!isValidForm()) {
            return;
        }

        Department department = editingDepartment == null ? new Department() : editingDepartment;
        department
            .setDepartmentName(FormValidationUtil.normalizeOptionalText(txtDepart.getText()))
            .setDepartmentCode(FormValidationUtil.normalizeOptionalText(txtDepartmentCode.getText()).toUpperCase())
            .setDescription(FormValidationUtil.normalizeOptionalText(txtAreaDescription.getText()));

        boolean success =
            editingDepartment == null
                ? departmentService.createDepartment(department)
                : departmentService.updateDepartment(department);

        if (!success) {
            JOptionPane.showMessageDialog(
                this,
                "Failed to save department. Please try again.",
                "Save Failed",
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        JOptionPane.showMessageDialog(
            this,
            editingDepartment == null
                ? "Department created successfully."
                : "Department updated successfully.",
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
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
        // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
        private void initComponents() {

                windowBar1 = new com.group5.paul_esys.components.WindowBar();
                jPanel1 = new javax.swing.JPanel();
                jLabel4 = new javax.swing.JLabel();
                jLabel5 = new javax.swing.JLabel();
                jLabel6 = new javax.swing.JLabel();
                txtDepart = new javax.swing.JTextField();
                txtDepartmentCode = new javax.swing.JTextField();
                jLabel1 = new javax.swing.JLabel();
                btnCancel = new javax.swing.JButton();
                btnSave = new javax.swing.JButton();
                jLabel2 = new javax.swing.JLabel();
                jScrollPane1 = new javax.swing.JScrollPane();
                txtAreaDescription = new javax.swing.JTextArea();

                setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
                setResizable(false);
                setSize(new java.awt.Dimension(514, 510));

                windowBar1.setTitle("Department Form");

                jPanel1.setBackground(new java.awt.Color(255, 255, 255));
                jPanel1.setAlignmentY(0.5F);
                jPanel1.setMaximumSize(new java.awt.Dimension(500, 500));
                jPanel1.setPreferredSize(new java.awt.Dimension(400, 400));
                jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

                jLabel4.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
                jLabel4.setText("Description");
                jPanel1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 250, -1, -1));

                jLabel5.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
                jLabel5.setText("Department Name");
                jPanel1.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 110, -1, -1));

                jLabel6.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
                jLabel6.setText("Department Code");
                jPanel1.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 180, -1, -1));

                txtDepart.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
                txtDepart.setBorder(new com.group5.paul_esys.ui.TextFieldRoundBorder());
                jPanel1.add(txtDepart, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 140, 250, -1));

                txtDepartmentCode.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
                txtDepartmentCode.setBorder(new com.group5.paul_esys.ui.TextFieldRoundBorder());
                jPanel1.add(txtDepartmentCode, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 210, 250, -1));

                jLabel1.setFont(new java.awt.Font("Poppins", 0, 18)); // NOI18N
                jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                jLabel1.setText("Department Form");
                jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 30, 250, -1));

                btnCancel.setBackground(new java.awt.Color(255, 234, 234));
                btnCancel.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
                btnCancel.setText("Cancel");
                btnCancel.addActionListener(this::btnCancelActionPerformed);
                jPanel1.add(btnCancel, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 410, 100, -1));

                btnSave.setBackground(new java.awt.Color(255, 234, 234));
                btnSave.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
                btnSave.setText("Save");
                btnSave.addActionListener(this::btnSaveActionPerformed);
                jPanel1.add(btnSave, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 410, 100, -1));

                jLabel2.setForeground(new java.awt.Color(102, 102, 102));
                jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                jLabel2.setText("Create/Update Department");
                jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 60, 250, -1));

                txtAreaDescription.setColumns(20);
                txtAreaDescription.setRows(5);
                txtAreaDescription.addKeyListener(new java.awt.event.KeyAdapter() {
                        public void keyReleased(java.awt.event.KeyEvent evt) {
                                txtAreaDescriptionKeyReleased(evt);
                        }
                });
                jScrollPane1.setViewportView(txtAreaDescription);

                jPanel1.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 280, 250, 120));

                javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
                getContentPane().setLayout(layout);
                layout.setHorizontalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(windowBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                );
                layout.setVerticalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(windowBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, 0)
                                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 467, javax.swing.GroupLayout.PREFERRED_SIZE))
                );

                pack();
        }// </editor-fold>//GEN-END:initComponents

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        dispose();
    }//GEN-LAST:event_btnCancelActionPerformed

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        saveDepartment();
    }//GEN-LAST:event_btnSaveActionPerformed

        private void txtAreaDescriptionKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtAreaDescriptionKeyReleased
            if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                saveDepartment();
            }
        }//GEN-LAST:event_txtAreaDescriptionKeyReleased

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
        java.awt.EventQueue.invokeLater(() -> new DepartmentForm().setVisible(true));
    }

        // Variables declaration - do not modify//GEN-BEGIN:variables
        private javax.swing.JButton btnCancel;
        private javax.swing.JButton btnSave;
        private javax.swing.JLabel jLabel1;
        private javax.swing.JLabel jLabel2;
        private javax.swing.JLabel jLabel4;
        private javax.swing.JLabel jLabel5;
        private javax.swing.JLabel jLabel6;
        private javax.swing.JPanel jPanel1;
        private javax.swing.JScrollPane jScrollPane1;
        private javax.swing.JTextArea txtAreaDescription;
        private javax.swing.JTextField txtDepart;
        private javax.swing.JTextField txtDepartmentCode;
        private com.group5.paul_esys.components.WindowBar windowBar1;
        // End of variables declaration//GEN-END:variables
}
