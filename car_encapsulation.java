import java.util.Scanner;
import java.text.NumberFormat;
import java.util.Locale;
import java.time.Year;

public class Car {
    private String company_name;
    private String model_name;
    private int year;
    private double mileage;
    private double gas_consumption;
    private String plate_number;
    private double price;
    private int horse_power;

    public Car() {
        this.mileage = 0.0;
    }

    public void setCompany_name(String company_name) {
        this.company_name = company_name;
    }

    public String getCompany_name() {
        return company_name;
    }

    public void setModel_name(String model_name) {
        this.model_name = model_name;
    }

    public String getModel_name() {
        return model_name;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getYear() {
        return year;
    }

    public double getMileage() {
        return mileage;
    }

    public void setGas_consumption(double gas_consumption) {
        this.gas_consumption = gas_consumption;
    }

    public double getGas_consumption() {
        return gas_consumption;
    }

    public void setPlate_number(String plate_number) {
        this.plate_number = plate_number;
    }

    public String getPlate_number() {
        return plate_number;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getPrice() {
        return price;
    }

    public void setHorse_power(int horse_power) {
        this.horse_power = horse_power;
    }

    public int getHorse_power() {
        return horse_power;
    }

    public void addMileage(double miles) {
        if (miles > 0) {
            this.mileage += miles;
        }
    }

    public int calculateCarAge() {
        int currentYear = Year.now().getValue();
        return currentYear - year;
    }

    public String getFuelEfficiencyCategory() {
        if (gas_consumption <= 5.0) {
            return "Excellent (Highly Fuel Efficient)";
        } else if (gas_consumption <= 8.0) {
            return "Good (Fuel Efficient)";
        } else if (gas_consumption <= 12.0) {
            return "Average (Moderate Fuel Consumption)";
        } else {
            return "Poor (High Fuel Consumption)";
        }
    }

    public String getMaintenanceReminder() {
        int age = calculateCarAge();
        
        if (mileage >= 100000) {
            return "URGENT: Major service required! Mileage exceeded 100,000 km.";
        } else if (mileage >= 50000) {
            return "Schedule a comprehensive maintenance check soon.";
        } else if (age >= 10) {
            return "Vehicle is over 10 years old. Consider thorough inspection.";
        } else if (age >= 5) {
            return "Regular maintenance recommended for optimal performance.";
        } else {
            return "Vehicle is in good age range. Follow regular service schedule.";
        }
    }

    public String getPerformanceCategory() {
        if (horse_power >= 400) {
            return "High Performance / Sports Car";
        } else if (horse_power >= 250) {
            return "Performance Vehicle";
        } else if (horse_power >= 150) {
            return "Standard Performance";
        } else {
            return "Economy / Fuel Saver";
        }
    }

    public void displayCarInformation() {
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));
        
        System.out.println("\n========== CAR INFORMATION ==========");
        System.out.println("Company: " + company_name);
        System.out.println("Model: " + model_name);
        System.out.println("Year: " + year);
        System.out.println("Car Age: " + calculateCarAge() + " years");
        System.out.println("Plate Number: " + plate_number);
        System.out.println("--------------------------------------");
        System.out.println("Mileage: " + String.format("%.2f", mileage) + " km");
        System.out.println("Gas Consumption: " + gas_consumption + " L/100km");
        System.out.println("Fuel Efficiency: " + getFuelEfficiencyCategory());
        System.out.println("Horse Power: " + horse_power + " HP");
        System.out.println("Performance: " + getPerformanceCategory());
        System.out.println("Price: " + currencyFormatter.format(price));
        System.out.println("--------------------------------------");
        System.out.println("Maintenance Status:");
        System.out.println(getMaintenanceReminder());
        System.out.println("======================================\n");
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Car car = new Car();

        System.out.println("========== CAR MANAGEMENT SYSTEM ==========");
        System.out.println();

        System.out.print("Enter Company Name: ");
        car.setCompany_name(scanner.nextLine());

        System.out.print("Enter Model Name: ");
        car.setModel_name(scanner.nextLine());

        System.out.print("Enter Year: ");
        car.setYear(scanner.nextInt());
        scanner.nextLine();

        System.out.print("Enter Plate Number: ");
        car.setPlate_number(scanner.nextLine());

        System.out.print("Enter Current Mileage (km): ");
        double initialMileage = scanner.nextDouble();
        car.addMileage(initialMileage);

        System.out.print("Enter Gas Consumption (L/100km): ");
        car.setGas_consumption(scanner.nextDouble());

        System.out.print("Enter Horse Power (HP): ");
        car.setHorse_power(scanner.nextInt());

        System.out.print("Enter Price: ");
        car.setPrice(scanner.nextDouble());

        car.displayCarInformation();

        System.out.println("Would you like to add more mileage? (yes/no): ");
        scanner.nextLine();
        String response = scanner.nextLine();

        if (response.equalsIgnoreCase("yes")) {
            System.out.print("Enter additional mileage to add (km): ");
            double additionalMileage = scanner.nextDouble();
            car.addMileage(additionalMileage);
            System.out.println("\nUpdated mileage: " + car.getMileage() + " km");
            System.out.println("New maintenance reminder: " + car.getMaintenanceReminder());
        }

        scanner.close();
    }
}