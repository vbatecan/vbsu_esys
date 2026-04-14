package com.group5.paul_esys.screens.shared.panels;

import com.group5.paul_esys.modules.users.models.user.UserInformation;
import com.group5.paul_esys.modules.users.services.AccountSecurityService;
import com.group5.paul_esys.modules.users.services.UserSession;
import com.group5.paul_esys.ui.PanelRoundBorder;
import com.group5.paul_esys.ui.TextFieldRoundBorder;
import com.group5.paul_esys.utils.FormValidationUtil;
import com.group5.paul_esys.utils.ThemeManager;
import com.group5.paul_esys.utils.ThemeOption;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;

public class SecurityPanel extends javax.swing.JPanel {

    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_LENGTH = 128;

    private final AccountSecurityService accountSecurityService = AccountSecurityService.getInstance();

    private JButton btnApplyTheme;
    private JButton btnChangePassword;
    private JComboBox<ThemeOption> cmbTheme;
    private JLabel lblChangePassword;
    private JLabel lblCurrentPassword;
    private JLabel lblNewPassword;
    private JLabel lblConfirmPassword;
    private JLabel lblTheme;
    private JLabel lblThemeSection;
    private JPanel passwordPanel;
    private JPanel themePanel;
    private JScrollPane scrollPane;
    private JPanel rootPanel;
    private JPanel contentPanel;
    private JPasswordField txtConfirmPassword;
    private JPasswordField txtCurrentPassword;
    private JPasswordField txtNewPassword;

    public SecurityPanel() {
        initComponents();
        initializePanel();
    }

    private void initializePanel() {
        bindActions();
        loadThemeOptions();
    }

    private void bindActions() {
        btnChangePassword.addActionListener(evt -> submitPasswordChange());
        btnApplyTheme.addActionListener(evt -> applySelectedTheme());
    }

    private void loadThemeOptions() {
        cmbTheme.removeAllItems();
        for (ThemeOption option : ThemeManager.getThemeOptions()) {
            cmbTheme.addItem(option);
        }
        cmbTheme.setSelectedItem(ThemeManager.getSavedThemeOption());
    }

    private void submitPasswordChange() {
        String currentPassword = FormValidationUtil.normalizeOptionalText(new String(txtCurrentPassword.getPassword()));
        String newPassword = FormValidationUtil.normalizeOptionalText(new String(txtNewPassword.getPassword()));
        String confirmPassword = FormValidationUtil.normalizeOptionalText(new String(txtConfirmPassword.getPassword()));

        if (currentPassword == null) {
            showValidationError("Current Password is required.");
            return;
        }

        String validationError = FormValidationUtil
            .validatePassword(
                "New Password",
                newPassword,
                true,
                MIN_PASSWORD_LENGTH,
                MAX_PASSWORD_LENGTH
            )
            .orElse(null);

        if (validationError != null) {
            showValidationError(validationError);
            return;
        }

        if (confirmPassword == null) {
            showValidationError("Confirm Password is required.");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showValidationError("New Password and Confirm Password do not match.");
            return;
        }

        if (newPassword.equals(currentPassword)) {
            showValidationError("New Password must be different from Current Password.");
            return;
        }

        UserInformation<?> userInformation = UserSession.getInstance().getUserInformation();
        if (userInformation == null || userInformation.getId() == null) {
            JOptionPane.showMessageDialog(
                this,
                "No active user session found.",
                "Change Password",
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        Long userId = userInformation.getId();
        setPasswordControlsEnabled(false);

        AtomicReference<String> failureReason = new AtomicReference<>("Unable to update password.");
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                boolean currentPasswordValid = accountSecurityService.verifyCurrentPassword(userId, currentPassword);
                if (!currentPasswordValid) {
                    failureReason.set("Current Password is incorrect.");
                    return false;
                }

                boolean updated = accountSecurityService.updatePassword(userId, newPassword);
                if (!updated) {
                    failureReason.set("Unable to update password. Please try again.");
                    return false;
                }

                return true;
            }

            @Override
            protected void done() {
                setPasswordControlsEnabled(true);
                try {
                    boolean success = get();
                    if (!success) {
                        JOptionPane.showMessageDialog(
                            SecurityPanel.this,
                            failureReason.get(),
                            "Change Password",
                            JOptionPane.ERROR_MESSAGE
                        );
                        return;
                    }

                    clearPasswordFields();
                    JOptionPane.showMessageDialog(
                        SecurityPanel.this,
                        "Password updated successfully.",
                        "Change Password",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                        SecurityPanel.this,
                        "Unable to update password. Please try again.",
                        "Change Password",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };
        worker.execute();
    }

    private void applySelectedTheme() {
        ThemeOption selected = (ThemeOption) cmbTheme.getSelectedItem();
        if (selected == null) {
            return;
        }

        boolean applied = ThemeManager.applyTheme(selected.getClassName());
        if (!applied) {
            JOptionPane.showMessageDialog(
                this,
                "Unable to apply theme.",
                "Theme",
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        JOptionPane.showMessageDialog(
            this,
            "Theme updated successfully.",
            "Theme",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void showValidationError(String message) {
        JOptionPane.showMessageDialog(
            this,
            message,
            "Validation Error",
            JOptionPane.WARNING_MESSAGE
        );
    }

    private void setPasswordControlsEnabled(boolean enabled) {
        txtCurrentPassword.setEnabled(enabled);
        txtNewPassword.setEnabled(enabled);
        txtConfirmPassword.setEnabled(enabled);
        btnChangePassword.setEnabled(enabled);
    }

    private void clearPasswordFields() {
        txtCurrentPassword.setText("");
        txtNewPassword.setText("");
        txtConfirmPassword.setText("");
    }

    private void initComponents() {
        rootPanel = new JPanel(new BorderLayout());
        contentPanel = new JPanel(new GridBagLayout());
        scrollPane = new JScrollPane(contentPanel);

        passwordPanel = new JPanel(new GridBagLayout());
        lblChangePassword = new JLabel("Change Password");
        lblCurrentPassword = new JLabel("Current Password");
        lblNewPassword = new JLabel("New Password");
        lblConfirmPassword = new JLabel("Confirm Password");
        txtCurrentPassword = new JPasswordField();
        txtNewPassword = new JPasswordField();
        txtConfirmPassword = new JPasswordField();
        btnChangePassword = new JButton("Update Password");

        themePanel = new JPanel(new GridBagLayout());
        lblThemeSection = new JLabel("Application Theme");
        lblTheme = new JLabel("Theme");
        cmbTheme = new JComboBox<>();
        btnApplyTheme = new JButton("Apply Theme");

        setLayout(new BorderLayout());
        setBackground(new java.awt.Color(255, 255, 255));

        rootPanel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        rootPanel.setOpaque(false);

        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        lblChangePassword.setFont(new Font("Poppins", Font.BOLD, 20));
        lblThemeSection.setFont(new Font("Poppins", Font.BOLD, 20));

        txtCurrentPassword.setFont(new Font("Poppins", Font.PLAIN, 14));
        txtNewPassword.setFont(new Font("Poppins", Font.PLAIN, 14));
        txtConfirmPassword.setFont(new Font("Poppins", Font.PLAIN, 14));

        txtCurrentPassword.setBorder(new TextFieldRoundBorder());
        txtNewPassword.setBorder(new TextFieldRoundBorder());
        txtConfirmPassword.setBorder(new TextFieldRoundBorder());

        cmbTheme.setFont(new Font("Poppins", Font.PLAIN, 14));

        btnChangePassword.putClientProperty("JButton.buttonType", "roundRect");
        btnApplyTheme.putClientProperty("JButton.buttonType", "roundRect");

        passwordPanel.setBorder(new PanelRoundBorder());
        passwordPanel.setBackground(new java.awt.Color(255, 255, 255));
        passwordPanel.setOpaque(true);

        themePanel.setBorder(new PanelRoundBorder());
        themePanel.setBackground(new java.awt.Color(255, 255, 255));
        themePanel.setOpaque(true);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 12, 8, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        passwordPanel.add(lblChangePassword, gbc);

        gbc.gridy = 1;
        gbc.gridwidth = 1;
        passwordPanel.add(lblCurrentPassword, gbc);

        gbc.gridy = 2;
        gbc.weightx = 1.0;
        passwordPanel.add(txtCurrentPassword, gbc);

        gbc.gridy = 3;
        gbc.weightx = 0;
        passwordPanel.add(lblNewPassword, gbc);

        gbc.gridy = 4;
        gbc.weightx = 1.0;
        passwordPanel.add(txtNewPassword, gbc);

        gbc.gridy = 5;
        gbc.weightx = 0;
        passwordPanel.add(lblConfirmPassword, gbc);

        gbc.gridy = 6;
        gbc.weightx = 1.0;
        passwordPanel.add(txtConfirmPassword, gbc);

        gbc.gridy = 7;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.LINE_END;
        passwordPanel.add(btnChangePassword, gbc);

        GridBagConstraints themeGbc = new GridBagConstraints();
        themeGbc.insets = new Insets(8, 12, 8, 12);
        themeGbc.fill = GridBagConstraints.HORIZONTAL;
        themeGbc.anchor = GridBagConstraints.NORTHWEST;

        themeGbc.gridx = 0;
        themeGbc.gridy = 0;
        themeGbc.gridwidth = 2;
        themePanel.add(lblThemeSection, themeGbc);

        themeGbc.gridy = 1;
        themeGbc.gridwidth = 1;
        themePanel.add(lblTheme, themeGbc);

        themeGbc.gridy = 2;
        themeGbc.weightx = 1.0;
        themePanel.add(cmbTheme, themeGbc);

        themeGbc.gridy = 3;
        themeGbc.weightx = 0;
        themeGbc.anchor = GridBagConstraints.LINE_END;
        themePanel.add(btnApplyTheme, themeGbc);

        GridBagConstraints contentGbc = new GridBagConstraints();
        contentGbc.gridx = 0;
        contentGbc.gridy = 0;
        contentGbc.weightx = 1.0;
        contentGbc.fill = GridBagConstraints.HORIZONTAL;
        contentGbc.insets = new Insets(0, 0, 16, 0);
        contentPanel.add(passwordPanel, contentGbc);

        contentGbc.gridy = 1;
        contentGbc.insets = new Insets(0, 0, 0, 0);
        contentPanel.add(themePanel, contentGbc);

        contentGbc.gridy = 2;
        contentGbc.weighty = 1.0;
        contentGbc.fill = GridBagConstraints.BOTH;
        contentPanel.add(new JPanel(), contentGbc);

        rootPanel.add(scrollPane, BorderLayout.CENTER);
        add(rootPanel, BorderLayout.CENTER);
    }
}
