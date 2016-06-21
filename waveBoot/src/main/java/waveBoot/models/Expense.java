package waveBoot.models;

import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.GenerationType;
import javax.persistence.Column;
import java.time.LocalDate;

@Entity
@Table(name = "expenses")
public class Expense {

    public static String[] headerFormat = {"date",
                                           "category",
                                           "employee name",
                                           "employee address",
                                           "expense description",
                                           "pre-tax amount",
                                           "tax name",
                                           "tax amount"};

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(nullable=false)
    private LocalDate date;

    @Column(nullable=false)
    private String category;

    @Column(nullable=false)
    private String employeeName;

    @Column(nullable=false)
    private String employeeAddress;

    @Column(nullable=false)
    private String expenseDescription;

    @Column(nullable=false)
    private double preTaxAmount;

    @Column(nullable=false)
    private String taxName;

    @Column(nullable=false)
    private double taxAmount;

    public Expense() { }

    public Expense(long id) { 
        this.id = id;
    }

    public Expense(LocalDate date, String category, String employeeName, String employeeAddress,
                   String expenseDescription, double preTaxAmount, String taxName, double taxAmount) {
        this.date = date;
        this.category = category;
        this.employeeName = employeeName;
        this.employeeAddress = employeeAddress;
        this.expenseDescription = expenseDescription;
        this.preTaxAmount = preTaxAmount;
        this.taxName = taxName;
        this.taxAmount = taxAmount;
    }

    public long getId(){
        return this.id;
    }

    public String getEmployeeName(){
        return employeeName;
    }

    public String getEmployeeAddress(){
        return employeeAddress;
    }

    public String getExpenseDescription(){
        return expenseDescription;
    }

    public double getPreTaxAmount(){
        return preTaxAmount;
    }

    public double getTaxAmount(){
        return taxAmount;
    }

    public String getTaxName(){
        return taxName;
    }

    public LocalDate getDate(){
        return date;
    }

    public String getCategory(){
        return this.category;
    }

    public void setId(long id){
        this.id = id;
    }

    public void setCategory(String category){
        this.category = category;
    }
    
    public void setEmployeeName(String employeeName){
        this.employeeName = employeeName;
    }

    public void setEmployeeAddress(String employeeAddress){
        this.employeeAddress = employeeAddress;
    }

    public void setExpenseDescription(String expenseDescription){
        this.expenseDescription = expenseDescription;
    }

    public void setPreTaxAmount(double preTaxAmount){
        this.preTaxAmount = preTaxAmount;
    }

    public void setTaxAmount(double taxAmount){
        this.taxAmount = taxAmount;
    }

    public void setTaxName(String taxName){
        this.taxName = taxName;
    }

    public void setDate(LocalDate date){
        this.date = date;
    }

    /**
     * Removes surrounding '"' from the address field. Method should be called
     * before passing list of expenses to front-end Thymeleaf.
     *
     * Add future front-end sanitization changes in this function.
     */
    public void sanitizeForFrontEnd(){
        if(this.employeeAddress.startsWith("\"") &&
           (this.employeeAddress.charAt(this.employeeAddress.length()-1) == '\"') ){
            this.employeeAddress = this.employeeAddress.substring(1,this.employeeAddress.length()-1);
        }
    }
}
