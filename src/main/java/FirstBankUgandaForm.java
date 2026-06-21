import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

public class FirstBankUgandaForm extends JFrame {

    // Form Input UI Elements
    private final JTextField txtFirstName;
    private final JTextField txtLastName;
    private final JTextField txtNIN;
    private final JTextField txtEmail;
    private final JTextField txtConfirmEmail;
    private final JTextField txtPhone;
    private final JTextField txtDeposit;
    private JTextField txtSecondNIN;
    private final JPasswordField txtPIN;
    private final JPasswordField txtConfirmPIN;
    private final JComboBox<Integer> cbYear;
    private final JComboBox<Integer> cbDay;
    private final JComboBox<String> cbMonth;
    private final JComboBox<String> cbAccountType;
    private final JComboBox<String> cbBranch;
    private final JTextArea txtSummary;
    private JLabel lblSecondNIN; // visible only for joint accounts

    // Dummy tracking counter for automated generation of chronological per-year, per-branch IDs
    private static final Map<String, Integer> sequenceCounters = new HashMap<>();

    public FirstBankUgandaForm() {
        setTitle("First Bank Uganda - New Account Registration Form");
        setSize(850, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // --- Row Components Initializations ---
        int r = 0;
        addFormRow(mainPanel, gbc, r++, "First Name:", txtFirstName = new JTextField(20));
        addFormRow(mainPanel, gbc, r++, "Last Name:", txtLastName = new JTextField(20));
        addFormRow(mainPanel, gbc, r++, "National ID (NIN):", txtNIN = new JTextField(20));
        addFormRow(mainPanel, gbc, r++, "Email Address:", txtEmail = new JTextField(20));
        addFormRow(mainPanel, gbc, r++, "Confirm Email:", txtConfirmEmail = new JTextField(20));
        addFormRow(mainPanel, gbc, r++, "Phone Number (+256...):", txtPhone = new JTextField(20));
        addFormRow(mainPanel, gbc, r++, "PIN (4-6 digits):", txtPIN = new JPasswordField(20));
        addFormRow(mainPanel, gbc, r++, "Confirm PIN:", txtConfirmPIN = new JPasswordField(20));

        // Date of Birth Dropdown Builders
        JPanel dobPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        cbYear = new JComboBox<>();
        for (int y = LocalDate.now().getYear(); y >= LocalDate.now().getYear() - 90; y--) cbYear.addItem(y);
        cbMonth = new JComboBox<>(new String[]{"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"});
        cbDay = new JComboBox<>();
        
        dobPanel.add(new JLabel("Year:")); dobPanel.add(cbYear);
        dobPanel.add(new JLabel("Month:")); dobPanel.add(cbMonth);
        dobPanel.add(new JLabel("Day:")); dobPanel.add(cbDay);
        
        // Setup Date Dropdown Auto-Correction triggers
        ActionListener dateConfigurator = e -> updateDaysInComboBox();
        cbYear.addActionListener(dateConfigurator);
        cbMonth.addActionListener(dateConfigurator);
        updateDaysInComboBox(); // initial execution payload setup

        gbc.gridx = 0; gbc.gridy = r; mainPanel.add(new JLabel("Date of Birth:"), gbc);
        gbc.gridx = 1; mainPanel.add(dobPanel, gbc);
        r++;

        // Select Account types and branch configurations
        cbAccountType = new JComboBox<>(new String[]{"Savings", "Current", "Fixed Deposit", "Student", "Joint"});
        addFormRow(mainPanel, gbc, r++, "Account Type:", cbAccountType);

        // Joint context secondary conditional field
        lblSecondNIN = new JLabel("Second NIN (Joint Only):");
        txtSecondNIN = new JTextField(20);
        lblSecondNIN.setVisible(false);
        txtSecondNIN.setVisible(false);
        addFormRow(mainPanel, gbc, r++, lblSecondNIN, txtSecondNIN);

        cbAccountType.addActionListener(e -> {
            boolean isJoint = cbAccountType.getSelectedItem().toString().equals("Joint");
            lblSecondNIN.setVisible(isJoint);
            txtSecondNIN.setVisible(isJoint);
            revalidate(); repaint();
        });

        cbBranch = new JComboBox<>(new String[]{"Kampala", "Gulu", "Mbarara", "Jinja", "Mbale"});
        addFormRow(mainPanel, gbc, r++, "Branch:", cbBranch);

        addFormRow(mainPanel, gbc, r++, "Opening Deposit (UGX):", txtDeposit = new JTextField(20));

        // --- Operational Action Row ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton btnSubmit = new JButton("Submit Registration");
        JButton btnReset = new JButton("Reset Form");
        buttonPanel.add(btnSubmit);
        buttonPanel.add(btnReset);

        // Bottom Display Panel
        JPanel southPanel = new JPanel(new BorderLayout(5, 5));
        southPanel.setBorder(BorderFactory.createTitledBorder("Account Summary Below:"));
        txtSummary = new JTextArea(4, 50);
        txtSummary.setEditable(false);
        txtSummary.setFont(new Font("Monospaced", Font.PLAIN, 12));
        southPanel.add(new JScrollPane(txtSummary), BorderLayout.CENTER);

        // Frame Layout bindings
        add(new JScrollPane(mainPanel), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        southPanel.add(buttonPanel, BorderLayout.NORTH);
        add(southPanel, BorderLayout.SOUTH);

        // Event Handling Hooks
        btnReset.addActionListener(e -> resetAllFields());
        btnSubmit.addActionListener(e -> processSubmission());
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, Component comp) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.1; panel.add(new JLabel(labelText), gbc);
        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 0.9; panel.add(comp, gbc);
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, JLabel lbl, Component comp) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.1; panel.add(lbl, gbc);
        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 0.9; panel.add(comp, gbc);
    }

    // Recalculates exact days available based on Year, Month, and Leap Year definitions
    private void updateDaysInComboBox() {
        if (cbYear.getSelectedItem() == null || cbMonth.getSelectedItem() == null) return;
        int selectedYear = (int) cbYear.getSelectedItem();
        int selectedMonthIndex = cbMonth.getSelectedIndex() + 1;
        int existingSelectedDay = (cbDay.getSelectedItem() != null) ? (int) cbDay.getSelectedItem() : 1;

        YearMonth yearMonthObject = YearMonth.of(selectedYear, selectedMonthIndex);
        int daysInMonth = yearMonthObject.lengthOfMonth();

        cbDay.removeAllItems();
        for (int d = 1; d <= daysInMonth; d++) {
            cbDay.addItem(d);
        }
        if (existingSelectedDay <= daysInMonth) {
            cbDay.setSelectedItem(existingSelectedDay);
        } else {
            cbDay.setSelectedIndex(daysInMonth - 1);
        }
    }

    private void resetAllFields() {
        txtFirstName.setText(""); txtLastName.setText(""); txtNIN.setText("");
        txtEmail.setText(""); txtConfirmEmail.setText(""); txtPhone.setText("");
        txtPIN.setText(""); txtConfirmPIN.setText(""); txtDeposit.setText("");
        txtSecondNIN.setText(""); txtSummary.setText("");
        cbAccountType.setSelectedIndex(0); cbBranch.setSelectedIndex(0);
        cbYear.setSelectedIndex(0); cbMonth.setSelectedIndex(0);
    }

    private void processSubmission() {
        StringBuilder errors = new StringBuilder();

        // 1. Data Scraping & Sanitization
        String fName = txtFirstName.getText().trim();
        String lName = txtLastName.getText().trim();
        String nin = txtNIN.getText().trim();
        String email = txtEmail.getText().trim();
        String confirmEmail = txtConfirmEmail.getText().trim();
        String phone = txtPhone.getText().trim();
        String pin = new String(txtPIN.getPassword()).trim();
        String confirmPin = new String(txtConfirmPIN.getPassword()).trim();
        String accType = cbAccountType.getSelectedItem().toString();
        String branch = cbBranch.getSelectedItem().toString();
        String depStr = txtDeposit.getText().trim();
        String secondNin = txtSecondNIN.getText().trim();

        // 2. Comprehensive Validation Constraints Checks
        if (!fName.matches("^[a-zA-Z]{2,30}$")) errors.append("- First Name must be letters only (2-30 chars).\n");
        if (!lName.matches("^[a-zA-Z]{2,30}$")) errors.append("- Last Name must be letters only (2-30 chars).\n");
        if (!nin.matches("^[A-Z0-9]{14}$")) errors.append("- NIN must be exactly 14 characters, completely UPPERCASE alphanumeric.\n");
        
        if (email.isEmpty() || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) errors.append("- Provide a valid Primary Email structure.\n");
        if (!email.equalsIgnoreCase(confirmEmail)) errors.append("- Email Confirmation field does not match primary string.\n");
        
        if (!phone.matches("^\\+256\\d{9}$")) errors.append("- Phone format must follow standard Ugandan sequence +256XXXXXXXXX (12 following digits).\n");
        
        if (!pin.matches("^\\d{4,6}$")) errors.append("- PIN must be numbers only, strictly between 4-6 digits length.\n");
        if (!pin.equals(confirmPin)) errors.append("- Confirmed PIN input entry does not match.\n");
        if (pin.matches("^(\\d)\\1+$")) errors.append("- PIN security check failed: Repeated single digits (e.g., 0000) are banned.\n");

        // Parse selections into LocalDate object instances
        int dobYear = (int) cbYear.getSelectedItem();
        int dobMonth = cbMonth.getSelectedIndex() + 1;
        int dobDay = (int) cbDay.getSelectedItem();
        LocalDate birthDate = LocalDate.of(dobYear, dobMonth, dobDay);
        
        double depositAmount = -1;
        try {
            depositAmount = Double.parseDouble(depStr);
        } catch (NumberFormatException ex) {
            errors.append("- Opening deposit must be typed as a pure numeric value.\n");
        }

        // Return early if basic type formatting validations fail
        if (errors.length() > 0) {
            JOptionPane.showMessageDialog(this, errors.toString(), "Input Form Parsing Failures", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 3. Polymorphic Object Instantiation & Business Logic Evaluation
        Account accountObj = null;
        switch (accType) {
            case "Savings" -> accountObj = new SavingsAccount(fName, lName, nin, email, phone, birthDate, branch, depositAmount);
            case "Current" -> accountObj = new CurrentAccount(fName, lName, nin, email, phone, birthDate, branch, depositAmount);
            case "Fixed Deposit" -> accountObj = new FixedDepositAccount(fName, lName, nin, email, phone, birthDate, branch, depositAmount);
            case "Student" -> accountObj = new StudentAccount(fName, lName, nin, email, phone, birthDate, branch, depositAmount);
            case "Joint" -> accountObj = new JointAccount(fName, lName, nin, email, phone, birthDate, branch, depositAmount, secondNin);
        }

        // Age Verification Rules
        int calculatedAge = accountObj.getAge();
        if (calculatedAge < 18 || calculatedAge > 75) {
            errors.append("- General eligible target age range must fall within 18-75 boundaries (Current: ").append(calculatedAge).append(").\n");
        }

        // Enforce Subclass Polymorphic Minimum Deposit Bounds Check
        if (depositAmount < accountObj.getMinimumDeposit()) {
            errors.append("- Selected [").append(accountObj.getAccountTypeName())
                  .append("] requires a minimum layout of UGX ").append(String.format("%,.0f", accountObj.getMinimumDeposit())).append(".\n");
        }

        // Subclass rule evaluations (Student Age Bounds / Joint Second NIN validation verification)
        errors.append(accountObj.validateSpecificRules());

        if (errors.length() > 0) {
            JOptionPane.showMessageDialog(this, errors.toString(), "Account Strategy Rule Failures", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 4. Sequence Key Generation & Output Formulation
        String branchCode = getBranchCode(branch);
        int localYear = LocalDate.now().getYear();
        String counterKey = branchCode + "-" + localYear;
        int currentSequence = sequenceCounters.getOrDefault(counterKey, 141) + 1; // Starting mock window index context offset matching sample text
        sequenceCounters.put(counterKey, currentSequence);
        
        String generatedAccountNumber = String.format("%s-%d-%06d", branchCode, localYear, currentSequence);

        // Render Summary String
        String printableSummary = String.format("ACC: %s | %s %s | %s | %s | DOB %s | %s | Deposit %,.0f | %s",
                generatedAccountNumber, accountObj.firstName, accountObj.lastName, accountObj.getAccountTypeName(),
                accountObj.branch, accountObj.dob.toString(), accountObj.phoneNumber, accountObj.openingDeposit, accountObj.email);

        txtSummary.setText(printableSummary);

        // 5. Database Connection Layer Integration
        saveToAccessDatabase (generatedAccountNumber, accountObj);
        JOptionPane.showMessageDialog(this, "Record generated and synchronized successfully to the central register database ledger.", "Process Completed", JOptionPane.INFORMATION_MESSAGE);
    }

    private String getBranchCode(String branchName) {
        return switch (branchName) {
            case "Kampala" -> "KLA";
            case "Gulu" -> "GUL";
            case "Mbarara" -> "MBR";
            case "Jinja" -> "JNJ";
            case "Mbale" -> "MBL";
            default -> "GEN";
        };
    }
    // Integration blueprint connector for MS Access operations
    // Integration blueprint connector for MS Access operations


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FirstBankUgandaForm().setVisible(true));
    }

    private void saveToAccessDatabase(String generatedAccountNumber, Account accountObj) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

}