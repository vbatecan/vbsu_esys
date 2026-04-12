/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.group5.paul_esys.screens.registrar.cards;

import com.group5.paul_esys.modules.curriculum.model.Curriculum;
import com.group5.paul_esys.modules.semester.model.Semester;
import com.group5.paul_esys.modules.semester.services.SemesterService;
import com.group5.paul_esys.modules.subjects.model.Subject;
import com.group5.paul_esys.modules.subjects.services.SubjectService;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.ZoneId;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingWorker;

/**
 *
 * @author nytri
 */
public class CurriculumCard extends javax.swing.JPanel {

        private final SemesterService semesterService = SemesterService.getInstance();
        private final SubjectService subjectService = SubjectService.getInstance();

        private final Curriculum curriculum;
        private final String courseName;
        private final Runnable onCurriculumChangedCallback;
        private final Map<Integer, Semester> semestersByTabIndex = new LinkedHashMap<>();
        private final JPopupMenu semesterActionsMenu = new JPopupMenu();
        private final JMenuItem menuDeleteSemester = new JMenuItem("Delete Semester");
        private final JMenuItem menuDeleteYearLevel = new JMenuItem("Delete Year Level");
        private boolean semestersLoaded;

	/**
	 * Creates new form CurriculumCard
	 */
	public CurriculumCard() {
                this(null, null, null);
        }

        public CurriculumCard(Curriculum curriculum, String courseName) {
                this(curriculum, courseName, null);
        }

        public CurriculumCard(Curriculum curriculum, String courseName, Runnable onCurriculumChangedCallback) {
                this.curriculum = curriculum;
                this.courseName = courseName;
		this.onCurriculumChangedCallback = onCurriculumChangedCallback;
		initComponents();
                initializeCard();
	}

        private void initializeCard() {
                configureSemesterActionsMenu();

                if (curriculum == null) {
                        txtCurriculumName.setText("");
                        txtCurYear.setText("");
                        cbxCourse.removeAllItems();
                        cbxCourse.addItem("");
                        showSemestersPlaceholder("No curriculum selected.");
                        return;
                }

                txtCurriculumName.setText(safeText(curriculum.getName(), "N/A"));
                txtCurYear.setText(formatYear(curriculum.getCurYear()));

                cbxCourse.removeAllItems();
                cbxCourse.addItem(safeText(courseName, "N/A"));

                showSemestersPlaceholder("Semesters load when this curriculum tab is selected.");
        }

        public Curriculum getCurriculum() {
                return curriculum;
        }

        public boolean isSemestersLoaded() {
                return semestersLoaded;
        }

        public void ensureSemestersLoaded() {
                if (semestersLoaded) {
                        return;
                }

                loadSemestersAsync();
        }

        public void reloadSemesters() {
                semestersLoaded = false;
                loadSemestersAsync();
        }

        private void loadSemestersAsync() {
                tabbedPaneSemesters.removeAll();
                showSemestersPlaceholder("Loading semesters...");

                if (curriculum == null || curriculum.getId() == null) {
                        showSemestersPlaceholder("No curriculum selected.");
                        semestersLoaded = true;
                        return;
                }

                new SwingWorker<SemesterLoadResult, Void>() {
                        @Override
                        protected SemesterLoadResult doInBackground() throws Exception {
                                List<Semester> semesters = semesterService.getSemestersByCurriculum(curriculum.getId());
                                Map<Long, Subject> subjectMap = loadSubjectMapForCurriculum(curriculum.getId());
                                return new SemesterLoadResult(semesters, subjectMap);
                        }

                        @Override
                        protected void done() {
                                try {
                                        SemesterLoadResult result = get();
                                        populateSemesterTabs(result.semesters(), result.subjectMap());
                                } catch (Exception ex) {
                                        showSemestersPlaceholder("Error loading semesters: " + ex.getMessage());
                                        semestersLoaded = true;
                                }
                        }
                }.execute();
        }

        private void populateSemesterTabs(List<Semester> semesters, Map<Long, Subject> subjectMap) {
                tabbedPaneSemesters.removeAll();
                semestersByTabIndex.clear();

                if (semesters.isEmpty()) {
                        showSemestersPlaceholder("No semesters found for this curriculum.");
                        semestersLoaded = true;
                        return;
                }

                for (Semester semester : semesters) {
                        SemesterCard semesterCard = new SemesterCard(semester, subjectMap, null);
                        tabbedPaneSemesters.addTab(buildSemesterTabTitle(semester), semesterCard);
                        semestersByTabIndex.put(tabbedPaneSemesters.getTabCount() - 1, semester);
                }

                semestersLoaded = true;
        }

        private record SemesterLoadResult(List<Semester> semesters, Map<Long, Subject> subjectMap) {
        }

        private Map<Long, Subject> loadSubjectMapForCurriculum(Long curriculumId) {
                List<Subject> subjects = subjectService.getAllSubjects();

                return subjects
                        .stream()
                        .collect(Collectors.toMap(Subject::getId, subject -> subject, (left, right) -> left, LinkedHashMap::new));
        }

        private String buildSemesterTabTitle(Semester semester) {
                return safeText(String.format("Year %s - %s", semester.getYearLevel(), semester.getSemester()), "Semester " + semester.getId());
        }

        private void showSemestersPlaceholder(String message) {
                tabbedPaneSemesters.removeAll();
                semestersByTabIndex.clear();
                JPanel panel = new JPanel(new java.awt.GridBagLayout());
                panel.setBackground(java.awt.Color.WHITE);
                JLabel label = new JLabel(message);
                label.setForeground(new java.awt.Color(120, 120, 120));
                panel.add(label);
                tabbedPaneSemesters.addTab("Semesters", panel);
        }

        private void configureSemesterActionsMenu() {
                menuDeleteSemester.addActionListener(evt -> deleteSelectedSemester("semester"));
                menuDeleteYearLevel.addActionListener(evt -> deleteSelectedSemester("curriculum year level entry"));
                semesterActionsMenu.add(menuDeleteSemester);
                semesterActionsMenu.add(menuDeleteYearLevel);

                tabbedPaneSemesters.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mousePressed(MouseEvent e) {
                                showSemesterActionsMenuIfNeeded(e);
                        }

                        @Override
                        public void mouseReleased(MouseEvent e) {
                                showSemesterActionsMenuIfNeeded(e);
                        }
                });
        }

        private void showSemesterActionsMenuIfNeeded(MouseEvent e) {
                if (!e.isPopupTrigger()) {
                        return;
                }

                int tabIndex = tabbedPaneSemesters.indexAtLocation(e.getX(), e.getY());
                if (tabIndex >= 0) {
                        tabbedPaneSemesters.setSelectedIndex(tabIndex);
                }

                Semester selectedSemester = getSelectedSemester();
                boolean canDelete = selectedSemester != null && selectedSemester.getId() != null;
                menuDeleteSemester.setEnabled(canDelete);
                menuDeleteYearLevel.setEnabled(canDelete);

                semesterActionsMenu.show(tabbedPaneSemesters, e.getX(), e.getY());
        }

        private Semester getSelectedSemester() {
                int selectedTab = tabbedPaneSemesters.getSelectedIndex();
                if (selectedTab < 0) {
                        return null;
                }

                return semestersByTabIndex.get(selectedTab);
        }

        private void deleteSelectedSemester(String targetLabel) {
                Semester selectedSemester = getSelectedSemester();
                if (selectedSemester == null || selectedSemester.getId() == null) {
                        JOptionPane.showMessageDialog(
                                this,
                                "Please select a semester to delete.",
                                "Delete " + targetLabel,
                                JOptionPane.WARNING_MESSAGE
                        );
                        return;
                }

                String semesterName = safeText(selectedSemester.getSemester(), "Semester");
                String yearLevel = selectedSemester.getYearLevel() == null
                        ? "N/A"
                        : String.valueOf(selectedSemester.getYearLevel());

                int option = JOptionPane.showConfirmDialog(
                        this,
                        "Delete " + semesterName + " (Year " + yearLevel + ")?",
                        "Confirm Delete " + targetLabel,
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

                if (option != JOptionPane.YES_OPTION) {
                        return;
                }

                new SwingWorker<Boolean, Void>() {
                        @Override
                        protected Boolean doInBackground() throws Exception {
                                return semesterService.deleteSemester(selectedSemester.getId());
                        }

                        @Override
                        protected void done() {
                                try {
                                        boolean deleted = get();
                                        if (!deleted) {
                                                JOptionPane.showMessageDialog(
                                                        CurriculumCard.this,
                                                        "Failed to delete " + targetLabel + ".",
                                                        "Delete " + targetLabel,
                                                        JOptionPane.ERROR_MESSAGE
                                                );
                                                return;
                                        }

                                        if (onCurriculumChangedCallback != null) {
                                                onCurriculumChangedCallback.run();
                                        } else {
                                                reloadSemesters();
                                        }

                                        JOptionPane.showMessageDialog(
                                                CurriculumCard.this,
                                                "Deleted " + targetLabel + " successfully.",
                                                "Delete " + targetLabel,
                                                JOptionPane.INFORMATION_MESSAGE
                                        );
                                } catch (Exception ex) {
                                        JOptionPane.showMessageDialog(
                                                CurriculumCard.this,
                                                "Error deleting " + targetLabel + ": " + ex.getMessage(),
                                                "Delete " + targetLabel,
                                                JOptionPane.ERROR_MESSAGE
                                        );
                                }
                        }
                }.execute();
        }

        private String safeText(String value, String fallback) {
                if (value == null || value.trim().isEmpty()) {
                        return fallback;
                }
                return value.trim();
        }

        private String formatYear(Date yearDate) {
                if (yearDate == null) {
                        return "N/A";
                }

                if (yearDate instanceof java.sql.Date sqlDate) {
                        return String.valueOf(sqlDate.toLocalDate().getYear());
                }

                return String.valueOf(yearDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getYear());
        }

	/**
	 * This method is called from within the constructor to initialize the
	 * form. WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
        // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
        private void initComponents() {

                tabbedPaneSemesters = new javax.swing.JTabbedPane();
                jLabel1 = new javax.swing.JLabel();
                txtCurriculumName = new javax.swing.JTextField();
                txtCurYear = new javax.swing.JTextField();
                jLabel2 = new javax.swing.JLabel();
                jLabel3 = new javax.swing.JLabel();
                cbxCourse = new javax.swing.JComboBox<>();

                setBackground(new java.awt.Color(255, 255, 255));
                setAutoscrolls(true);
                setPreferredSize(new java.awt.Dimension(696, 504));

                tabbedPaneSemesters.setBackground(new java.awt.Color(255, 255, 255));
                tabbedPaneSemesters.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
                tabbedPaneSemesters.setTabPlacement(javax.swing.JTabbedPane.LEFT);

                jLabel1.setText("Curriculum Name:");

                txtCurriculumName.setBorder(new com.group5.paul_esys.ui.TextFieldRoundBorder());
                txtCurriculumName.setEnabled(false);

                txtCurYear.setBorder(new com.group5.paul_esys.ui.TextFieldRoundBorder());
                txtCurYear.setEnabled(false);

                jLabel2.setText("Curriculum Year:");

                jLabel3.setText("Course / Program");

                cbxCourse.setEnabled(false);
                cbxCourse.addActionListener(this::cbxCourseActionPerformed);

                javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
                this.setLayout(layout);
                layout.setHorizontalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addGap(10, 10, 10)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addComponent(jLabel1)
                                                                .addGap(11, 11, 11)
                                                                .addComponent(txtCurriculumName, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addGap(10, 10, 10)
                                                                .addComponent(jLabel2)
                                                                .addGap(10, 10, 10)
                                                                .addComponent(txtCurYear, javax.swing.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE))
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addComponent(jLabel3)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                .addComponent(cbxCourse, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                                        .addGroup(layout.createSequentialGroup()
                                                .addContainerGap()
                                                .addComponent(tabbedPaneSemesters)))
                                .addContainerGap())
                );
                layout.setVerticalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(txtCurriculumName, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(txtCurYear, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGroup(layout.createSequentialGroup()
                                                .addGap(10, 10, 10)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(jLabel1)
                                                        .addComponent(jLabel2))))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel3)
                                        .addComponent(cbxCourse, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tabbedPaneSemesters, javax.swing.GroupLayout.PREFERRED_SIZE, 450, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );
        }// </editor-fold>//GEN-END:initComponents

        private void cbxCourseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxCourseActionPerformed
                // TODO add your handling code here:
        }//GEN-LAST:event_cbxCourseActionPerformed


        // Variables declaration - do not modify//GEN-BEGIN:variables
        private javax.swing.JComboBox<String> cbxCourse;
        private javax.swing.JLabel jLabel1;
        private javax.swing.JLabel jLabel2;
        private javax.swing.JLabel jLabel3;
        private javax.swing.JTabbedPane tabbedPaneSemesters;
        private javax.swing.JTextField txtCurYear;
        private javax.swing.JTextField txtCurriculumName;
        // End of variables declaration//GEN-END:variables
}
