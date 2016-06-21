# Khaled's Wave Software Development Challenge
This repo is an implementation of the se-challenge @ Wave.

## Project Description
This project follows the requirements from the se-challenge repo. In a nutshell, it can receive a csv file via a form upload, stores the contents into a relational database (MySql), then redirects to display the monthly total expenses, based on the contents stored in the database. The total monthly expense is calucated by totalling the pre-tax amount and the tax amount for each month.

## Basic Textual Machine Learning (Additional Feature | Something I'm proud of :)
The original repo mentioned that I should create something I am particularly proud of. There were many options to choose from, and I wanted it to be something that was cool. I thought that it would be interesting if we could find a simple way to apply machine learning to the CSV upload functionality.

The solution I came up with was to use the initial set of CSV data as the training data to train the machine learning component what the data in the different columns look like. Then, the next time a CSV file is uploaded, the server can handle an "unorganized" CSV file. By unorganized, it is meant that the rows in the CSV file are unordered. Note that the header line must exist and the be the same for all cases\*. Only the data rows are allowed be "unordered". A simple example is that if a row is expected to hold an Address followed by an Amount, then the machine learning component can detect that this:

35.00, 1 Yonge Street

should in fact be:

1 Yonge Street, 35.00

This can be useful in cases where the CSV file has issues that has come up through faulty ETL processes.

Other things I am proud of: Clean division of code into different packages, based on the MVC model. This makes maintenance, and modification of code very easy, as it each component is cleanly separated.

\*The header line must look exactly like this: "date, category, employee name, employee address, expense description, pre-tax amount, tax name, and tax amount"

## Prerequisites
1. Server will listen on port 8080. Therefore, port 8080 must be open before starting server.
1. This project must be 'built' with gradle 2.13+.
 1. Install gradle: On Mac OSx using brew: brew install gradle
1. Make sure that JDK 1.8+ is installed.
 1. Check with: java -version
1. A MySql server needs to be running on port 3306, and a database by the name of 'wave\_database' must exist.
 1. There are two ways to handle the MySql authentication:
  1. Option 1: Use default configuration from this repo: username is 'root' and password is '123456'.
  1. Option 2: Change the default behavior to whatever credentials are present by changing values in waveBoot/src/main/resources/application.properties:
   1. spring.datasource.username should be set to the MySql username
   1. spring.datasource.password should be set to the MySql password

## Regular Usage Instructions
1. Simply run this command in the top-level (waveBoot) folder (folder that contains buid.gradle file): gradle bootRun
1. After server is up-and-running, go to a browser, and navigate to: localhost:8080/uploadFile
1. Click on the "upload" button. Select the csv file data\_example.csv, then click on the "submit" button.
1. Based on the contents of the CSV file, the total expenses per-month is shown in the resulting table.
 1. To view the results of the entire raw contents that are stored in the MySql database, navigate to: localhost:8080/resultsAll

## Using Machine Learning component
1. After performing the steps in the "Regular Usage Instructions" above, the machine learning component is now setup.
1. Navigate to: localhost:8080/uploadFile
1. Click on the "upload" button. Select the csv file data\_example\_unordered\_row\_data.csv, then click on the "submit" button.
 1. Note that the data\_example\_unordered\_row\_data.csv contains the same data as data\_example.csv but all of the rows have been re-ordered, except for the header line.
1. The data in the rows has gone through the machine learning component, and then stored in the database, the results of the total per-month expenses (including all previous CSV uploaded contents) can be looked at for accuracy.
 1. To view the results of the entire raw contents that are stored in the MySql database (including data that was uploaded from all previous CSV uploads), navigate to: localhost:8080/resultsAll

## Additional Notes

1. the CSV files that is uploaded must always have the following column format: date, category, employee name, employee address, expense description, pre-tax amount, tax name, and tax amount.
1. the following must always be true:
 1. Columns must always be in that order.
 2. There must always be data in each column.
 3. There must always be a header line.
