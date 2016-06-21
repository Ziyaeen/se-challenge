package waveBoot.service;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

import waveBoot.models.Expense;
import waveBoot.user.UserSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.stereotype.Service;

import weka.classifiers.bayes.NaiveBayesMultinomialUpdateable;

@Service
public class ExpenseCsvService{

    private UserSession userSession;

    @Autowired
    public ExpenseCsvService(UserSession userSession){
        this.userSession = userSession;
    }

    public ExpenseCsvService(){}

    private enum FileType {
        CSV_FILE_TYPE, UNKNOWN_FILE_TYPE
    }

    private Map<FileType,String> FileTypeExtensionMap = new HashMap<FileType,String>()
    {{
         put(FileType.CSV_FILE_TYPE,"csv");
     }};

   /**
    * Method that is used to process a CSV MultipartFile. This method will parse the CSV
    * file, store in the database.
    *
    * The first time this method is called, it will also train the ML component, and save
    * that for the HTTP session.
    */
    public ArrayList<Expense> saveCsvFile(MultipartFile file) throws IOException, Exception{

        ArrayList<Expense> expenses = new ArrayList<Expense>();
        String line;
        String[] lineSplit;
        InputStream inputStream;
        BufferedReader bufferedReader;
        String filename = file.getOriginalFilename();
        FileType filetype = getFileType(filename);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy");
        
        if(filetype == FileType.UNKNOWN_FILE_TYPE)
            return null;
        
        inputStream = file.getInputStream();
        bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        line = bufferedReader.readLine();
        if(line == null)
            return null;
        lineSplit = line.split(",");
        //System.out.println("Header line = " + line);
        if(!headerFormatMatches(lineSplit)){
            return null;
        }
        
        while((line = bufferedReader.readLine()) != null)
        {
            //System.out.println("Line = " + line);
            if(userSession.getCsvIsTrained()){
                lineSplit = myMLCsvSplit(line);
            } else {
                lineSplit = myCsvSplit(line);
            }
            Expense expense = new Expense(LocalDate.parse(lineSplit[0], formatter),
                                          lineSplit[1],
                                          lineSplit[2],
                                          lineSplit[3],
                                          lineSplit[4],
                                          Double.parseDouble(sanitize4Double(lineSplit[5])),
                                          lineSplit[6],
                                          Double.parseDouble(sanitize4Double(lineSplit[7])));
            expenses.add(expense);
        }
        if(!userSession.getCsvIsTrained()){
            userSession.setCsvIsTrained(csvTrain(expenses));
        }
        return expenses;
    }

   /**
    * Method that provides the file type from a filename.
    */
    private FileType getFileType(String filename){
        int dotIndex = filename.lastIndexOf(".");
        if(dotIndex==-1){
            return FileType.UNKNOWN_FILE_TYPE;
        }
        String fileExtension = filename.substring(dotIndex);
        if(fileExtension.contains(FileTypeExtensionMap.get(FileType.CSV_FILE_TYPE))){
            return FileType.CSV_FILE_TYPE;
        }
        return FileType.UNKNOWN_FILE_TYPE;
    }

   /**
    * Method that ensures the CSV uploaded file has the correct header format.
    */ 
    private boolean headerFormatMatches(String[] headerLine){
        int i;

        for(i=0;i<Expense.headerFormat.length;i++){
            if(!Expense.headerFormat[i].equals(headerLine[i])){
                System.out.println(Expense.headerFormat[i] + " does not match " + headerLine[i]);
                return false;
            }
        }
        return true;
    }

    /**
     * This method is used before converting String values to a double, to 
     * remove unexpected chars.
     */
    private String sanitize4Double(String doubleVal){
        return doubleVal.replace("\"","").replace(",","");
    }

   /**
    * Method that is responsible for creating a ML instance, and using the expenses
    * as training data.
    */
    private boolean csvTrain(ArrayList<Expense> expenses){
        try {
            CsvMLService cml = new CsvMLService(new NaiveBayesMultinomialUpdateable());
            String[] categories = Expense.headerFormat;
            for(String category: categories){
                cml.addCategory(category);
            }
            cml.setupAfterCategorysAdded();
            
            for(Expense expense: expenses){
                int i=0;
                cml.addData(expense.getDate().toString(), categories[i++]);
                cml.addData(expense.getCategory(), categories[i++]);
                cml.addData(expense.getEmployeeName(), categories[i++]);
                cml.addData(expense.getEmployeeAddress(), categories[i++]);
                cml.addData(expense.getExpenseDescription(), categories[i++]);
                cml.addData(String.valueOf(expense.getPreTaxAmount()), categories[i++]);
                cml.addData(expense.getTaxName(), categories[i++]);
                cml.addData(String.valueOf(expense.getTaxAmount()), categories[i++]);
            }
            userSession.setCml(cml);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

   /**
    * Method that provides basic parsing of a CSV file line.
    */
    private String[] myCsvSplit(String line){
        String[] lineSplit = new String[1];
        ArrayList<String> lineSplitList = new ArrayList<String>();
        int startIndex=0, endIndex=0;
        while(startIndex<line.length()){

            if(line.charAt(startIndex)=='\"'){
                endIndex = line.indexOf('\"', startIndex+1) + 1;
            } else {
                endIndex = line.indexOf(',', startIndex);
            }
            if(endIndex<0){
                endIndex = line.length();
            }
            lineSplitList.add(line.substring(startIndex, endIndex));
            startIndex = endIndex+1;
        }
        lineSplit = lineSplitList.toArray(lineSplit);
        return lineSplit;
    }

    /**
     * This method is responsible for parsing a line of CSV file, using the previously trained
     * ML component.
     * 
     * Input line: b,c,a
     * Correct line: b,a,c
     * Example prob. matrix
     *     f1   f2   f3
     *   -------------------
     * b | 0.1  0.8  0.1   |
     * c | 0.2  0.1  0.7   |
     * a | 0.0  1.0  0.0   |
     *   -------------------
     *
     */
    private String[] myMLCsvSplit(String line) throws Exception {
        CsvMLService cml = userSession.getCml();
        String[] unorderedLineSplit = myCsvSplit(line);
        int arrLen = unorderedLineSplit.length;
        double[][] probMatrix = new double[arrLen][arrLen];
        String[] lineSplit = new String[arrLen];
        int[] horizontalMax = new int[arrLen];

        for(int i=0;i<arrLen;i++){
            probMatrix[i] = cml.classifyMessage(unorderedLineSplit[i]);
        }
        //print2dMatrix(probMatrix);
        zeroNonVerticalMax(probMatrix);
        
        /*
         * FIXME At this point, the matrix contains non-zero values only on
         * max positions for each row. This is not perfect, and can face
         * issues when two rows are identical, or when a row contains two
         * max positions in two different columns, is that possible?
         */
        //print2dMatrix(probMatrix);
        for(int row=0;row<arrLen;row++){
            for(int col=0;col<arrLen;col++){
                if(probMatrix[row][col]>0.0){
                    String field = unorderedLineSplit[row];
                    lineSplit[col] = field;
                }
            }
        }

        return lineSplit;
    }

   /**
    * Method that invalidates all cells, except for the cells that are chosen to
    * to indicate which category a field belongs to. Refer to 2d matrix representation
    * in the comments for the 'myMLCsvSplit' method.
    */
    private void zeroNonVerticalMax(double[][] probMatrix){
        for(int col=0;col<probMatrix.length;col++){
            int colMaxRow = getColMaxRow(probMatrix, col);
            setOtherRowsToZero(probMatrix, col, colMaxRow);
        }
    }

   /**
    * Method returns which row in the column 'col' should be selected as having
    * the higest ML probablilty based on previous training set.
    */
    private int getColMaxRow(double[][] probMatrix, int col){
        int curMaxRowIndex=0;
        double curMax=-1.0; //Doing this so that even a 0.0 value in matrix gets chosen by default
        int prevCol=0;
        for(int row=0;row<probMatrix.length;row++){
            if(probMatrix[row][col]>curMax){
                for(prevCol=0;prevCol<col;prevCol++){
                    if(probMatrix[row][prevCol]>0.0){
                        //This row is already taken, so choose another row
                        probMatrix[row][col]=-1.0;
                    }
                }
                if(probMatrix[row][col]>curMax){
                    curMaxRowIndex = row;
                    curMax = probMatrix[row][col];
                }
            }
        }
        return curMaxRowIndex;
    }

   /**
    * Method sets every cell in column 'col' to zero, except for cell in column
    * 'col' at row 'colMaxRow'.
    */
    private void setOtherRowsToZero(double[][] probMatrix, int col, int colMaxRow){
        for(int row=0;row<probMatrix.length;row++){
            if(row==colMaxRow)
                continue;
            probMatrix[row][col]=0.0;
        }
    }

    /**
     * Used for debugging purposes
     */
    private void print2dMatrix(double[][] probMatrix){
        for(int row=0;row<probMatrix.length;row++){
            for(int col=0;col<probMatrix.length;col++){
                System.out.print(probMatrix[row][col] + " ");
            }
            System.out.println();
        }
    }
}
