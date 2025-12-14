import java.util.Scanner;
import java.text.NumberFormat;
import java.util.Locale;

public class Employee {
    private int employee_id;
    private String employee_name;
    private double employee_salary;
    private int employee_hoursOfWork;
    private double employee_overtime_rate;
    private int employee_howManyHoursTheOvertime;
    private int employee_dayoff;
    private String employee_department;
    private double employee_bonus_percentage;

    public Employee() {
        this.employee_bonus_percentage = 0.10;
    }

    public void setEmployee_id(int employee_id) {
        this.employee_id = employee_id;
    }

    public int getEmployee_id() {
        return employee_id;
    }

    public void setEmployee_name(String employee_name) {
        this.employee_name = employee_name;
    }

    public String getEmployee_name() {
        return employee_name;
    }

    public void setEmployee_hoursOfWork(int employee_hoursOfWork) {
        this.employee_hoursOfWork = employee_hoursOfWork;
    }

    public int getEmployee_hoursOfWork() {
        return employee_hoursOfWork;
    }

    public void setEmployee_overtime_rate(double employee_overtime_rate) {
        this.employee_overtime_rate = employee_overtime_rate;
    }

    public double getEmployee_overtime_rate() {
        return employee_overtime_rate;
    }

    public void setEmployee_howManyHoursTheOvertime(int employee_howManyHoursTheOvertime) {
        this.employee_howManyHoursTheOvertime = employee_howManyHoursTheOvertime;
    }

    public int getEmployee_howManyHoursTheOvertime() {
        return employee_howManyHoursTheOvertime;
    }

    public void setEmployee_dayoff(int employee_dayoff) {
        this.employee_dayoff = employee_dayoff;
    }

    public int getEmployee_dayoff() {
        return employee_dayoff;
    }

    public void setEmployee_department(String employee_department) {
        this.employee_department = employee_department;
    }

    public String getEmployee_department() {
        return employee_department;
    }

    public void setEmployee_bonus_percentage(double employee_bonus_percentage) {
        this.employee_bonus_percentage = employee_bonus_percentage;
    }

    public double getEmployee_bonus_percentage() {
        return employee_bonus_percentage;
    }

    public String getEmployee_salary() {
        calculateSalary();
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));
        return formatter.format(employee_salary);
    }

    private void calculateSalary() {
        double baseSalary = employee_hoursOfWork * 100;
        double overtimePay = employee_howManyHoursTheOvertime * employee_overtime_rate;
        employee_salary = baseSalary + overtimePay;
    }

    public String calculateSalaryWithBonus() {
        calculateSalary();
        double salaryWithBonus = employee_salary + (employee_salary * employee_bonus_percentage);
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));
        return formatter.format(salaryWithBonus);
    }

    public String calculateAnnualSalary() {
        calculateSalary();
        double annualSalary = employee_salary * 12;
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));
        return formatter.format(annualSalary);
    }

    public String getWorkStatus() {
        if (employee_hoursOfWork >= 40) {
            return "Full-time";
        } else if (employee_hoursOfWork >= 20) {
            return "Part-time";
        } else {
            return "Contractual";
        }
    }

    public void displayEmployeeDetails() {
        System.out.println("\n========== EMPLOYEE DETAILS ==========");
        System.out.println("Employee ID: " + employee_id);
        System.out.println("Name: " + employee_name);
        System.out.println("Department: " + employee_department);
        System.out.println("Work Status: " + getWorkStatus());
        System.out.println("Hours of Work: " + employee_hoursOfWork + " hours/week");
        System.out.println("Overtime Hours: " + employee_howManyHoursTheOvertime + " hours");
        System.out.println("Overtime Rate: ₱" + employee_overtime_rate + "/hour");
        System.out.println("Days Off: " + employee_dayoff + " days/month");
        System.out.println("--------------------------------------");
        System.out.println("Monthly Salary: " + getEmployee_salary());
        System.out.println("Salary with Bonus: " + calculateSalaryWithBonus());
        System.out.println("Annual Salary: " + calculateAnnualSalary());
        System.out.println("======================================\n");
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Employee emp = new Employee();

        System.out.println("========== EMPLOYEE MANAGEMENT SYSTEM ==========");
        System.out.println();

        System.out.print("Enter Employee ID: ");
        emp.setEmployee_id(scanner.nextInt());
        scanner.nextLine();

        System.out.print("Enter Employee Name: ");
        emp.setEmployee_name(scanner.nextLine());

        System.out.print("Enter Department: ");
        emp.setEmployee_department(scanner.nextLine());

        System.out.print("Enter Hours of Work per Week: ");
        emp.setEmployee_hoursOfWork(scanner.nextInt());

        System.out.print("Enter Overtime Hours: ");
        emp.setEmployee_howManyHoursTheOvertime(scanner.nextInt());

        System.out.print("Enter Overtime Rate per Hour: ");
        emp.setEmployee_overtime_rate(scanner.nextDouble());

        System.out.print("Enter Days Off per Month: ");
        emp.setEmployee_dayoff(scanner.nextInt());

        System.out.print("Enter Bonus Percentage (e.g., 0.10 for 10%): ");
        emp.setEmployee_bonus_percentage(scanner.nextDouble());

        emp.displayEmployeeDetails();

        scanner.close();
    }
}