package waveBoot.controller;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;

import waveBoot.exception.WaveBootUploadFileException;
import waveBoot.service.ExpenseCsvService;
import waveBoot.models.Expense;
import waveBoot.models.ExpenseDao;
import waveBoot.user.UserSession;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.IOException;
import java.lang.Double;

@Controller
public class FileUploadController{

    private ExpenseDao expenseDao;
    private UserSession userSession;
    private ExpenseCsvService expenseCsvService;

    @Autowired
    public FileUploadController(ExpenseDao expenseDao, UserSession userSession, ExpenseCsvService expenseCsvService){
        this.expenseDao = expenseDao;
        this.userSession = userSession;
        this.expenseCsvService = expenseCsvService;
    }

    /**
     * Method that will present the file upload page to the user.
     */
    @RequestMapping(value = "/uploadFile", method=RequestMethod.GET)
    public String showFileUploadPage(){
        return "fileUploadPage";
    }

   /**
     * Method that is called when user submits a file from "fileUploadPage".
     */
    @RequestMapping(value = "/uploadFile", method=RequestMethod.POST)
    public String uploadFile(MultipartFile file) throws IOException, WaveBootUploadFileException, Exception{

        if(file.isEmpty()){
            throw new WaveBootUploadFileException("FILE_EMPTY");
        }
        
        ArrayList<Expense> expenses = expenseCsvService.saveCsvFile(file);

        for(Expense expense: expenses){
            expenseDao.save(expense);
        }

        if(expenses == null){
            throw new WaveBootUploadFileException("UNKNOWN_ERROR_PROCESSING_FILE");
        }

        return "redirect:/results";
    }

   /**
     * Method that is used to present monthly total expenses.
     *
     * By default, after uploading a file, the user is redirected to this controller.
     */
    @RequestMapping(value = "/results", method=RequestMethod.GET)
    public ModelAndView showMonthlyTotalExpenseResults(){
        //List<Expense> expenses = new ArrayList<Expense>();
        ModelAndView modelAndView = new ModelAndView("/results");
        Iterable<Expense> expensesIter = expenseDao.findAll();
        HashMap<String,Double> monthlyExpenses = new HashMap<String,Double>();

        for(Expense expense: expensesIter){
            String key;
            double monthExpense;
            expense.sanitizeForFrontEnd();
            key = expense.getDate().getMonth().toString() + " " + expense.getDate().getYear();
            if(monthlyExpenses.containsKey(key)){
                monthExpense = monthlyExpenses.get(key);
                monthlyExpenses.put(key, monthExpense + expense.getPreTaxAmount() + expense.getTaxAmount());
            } else {
                monthlyExpenses.put(key, expense.getPreTaxAmount() + expense.getTaxAmount());
            }
        }

        modelAndView.addObject("monthlyExpenses", monthlyExpenses);
        return modelAndView;
    }

   /**
     * Method that will present the entire contents stored in the MySql server.
     */
    @RequestMapping(value = "/resultsAll", method=RequestMethod.GET)
    public ModelAndView showAllResults(){
        List<Expense> expenses = new ArrayList<Expense>();
        ModelAndView modelAndView = new ModelAndView("/resultsAll");
        Iterable<Expense> expensesIter = expenseDao.findAll();
        
        for(Expense expense: expensesIter){
            expense.sanitizeForFrontEnd();
            expenses.add(expense);
        }
        
        modelAndView.addObject("expenses", expenses);
        return modelAndView;
    }
}
