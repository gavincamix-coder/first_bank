import java.time.LocalDate;
import java.time.Period;

// --- Abstract Account Class ---
public abstract class Account {
    protected String firstName;
    protected String lastName;
    protected String nin;
    protected String email;
    protected String phoneNumber;
    protected LocalDate dob;
    protected String branch;
    protected double openingDeposit;

    public Account(String firstName, String lastName, String nin, String email, 
                   String phoneNumber, LocalDate dob, String branch, double openingDeposit) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.nin = nin;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.dob = dob;
        this.branch = branch;
        this.openingDeposit = openingDeposit;
    }

    public abstract double getMinimumDeposit();
    public abstract String getAccountTypeName();

    public int getAge() {
        return Period.between(this.dob, LocalDate.now()).getYears();
    }

    // Custom business rule validation hook per account type
    public String validateSpecificRules() {
        return ""; // Override if specific rules apply
    }
}

// --- Concrete Subclasses ---
class SavingsAccount extends Account {
    public SavingsAccount(String f, String l, String n, String e, String p, LocalDate d, String b, double dep) {
        super(f, l, n, e, p, d, b, dep);
    }
    @Override public double getMinimumDeposit() { return 50000; }
    @Override public String getAccountTypeName() { return "Savings"; }
}

class CurrentAccount extends Account {
    public CurrentAccount(String f, String l, String n, String e, String p, LocalDate d, String b, double dep) {
        super(f, l, n, e, p, d, b, dep);
    }
    @Override public double getMinimumDeposit() { return 200000; }
    @Override public String getAccountTypeName() { return "Current"; }
}

class FixedDepositAccount extends Account {
    public FixedDepositAccount(String f, String l, String n, String e, String p, LocalDate d, String b, double dep) {
        super(f, l, n, e, p, d, b, dep);
    }
    @Override public double getMinimumDeposit() { return 1000000; }
    @Override public String getAccountTypeName() { return "Fixed Deposit"; }
}

class StudentAccount extends Account {
    public StudentAccount(String f, String l, String n, String e, String p, LocalDate d, String b, double dep) {
        super(f, l, n, e, p, d, b, dep);
    }
    @Override public double getMinimumDeposit() { return 10000; }
    @Override public String getAccountTypeName() { return "Student"; }
    
    @Override
    public String validateSpecificRules() {
        int age = getAge();
        if (age < 18 || age > 25) {
            return "Student account applicants must be between 18 and 25 years old (Current age: " + age + ").\n";
        }
        return "";
    }
}

class JointAccount extends Account {
    private String secondNin;

    public JointAccount(String f, String l, String n, String e, String p, LocalDate d, String b, double dep, String secondNin) {
        super(f, l, n, e, p, d, b, dep);
        this.secondNin = secondNin;
    }
    @Override public double getMinimumDeposit() { return 100000; }
    @Override public String getAccountTypeName() { return "Joint"; }

    @Override
    public String validateSpecificRules() {
        if (secondNin == null || secondNin.trim().length() != 14 || !secondNin.equals(secondNin.toUpperCase())) {
            return "Joint accounts require a valid Second NIN (exactly 14 uppercase alphanumeric characters).\n";
        }
        return "";
    }
}
