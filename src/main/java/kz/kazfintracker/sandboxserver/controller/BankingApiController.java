package kz.kazfintracker.sandboxserver.controller;

import kz.kazfintracker.sandboxserver.impl.crud.*;
import kz.kazfintracker.sandboxserver.impl.table.BankAccountElasticRegisterImpl;
import kz.kazfintracker.sandboxserver.model.web.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/card")
@RequiredArgsConstructor
public class BankingApiController {

    @Autowired
    private BankAccountRegisterImpl bankAccountRegister;

    @Autowired
    private CategoryTransactionRegisterImpl categoryTransactionRegister;

    @Autowired
    private CurrencyRegisterImpl currencyRegister;

    @Autowired
    private BudgetRegisterImpl budgetRegister;

    @Autowired
    private TransactionRegisterImpl transactionRegister;

    @Autowired
    private BankAccountElasticRegisterImpl elasticRegister;

    @PostMapping("/link-card")
    public ResponseEntity<String> linkCard(@RequestBody @Valid CardDetails cardDetails) {
        // Basic validation logic (you can enhance this with custom validation as needed)
        if (!cardDetails.getCardNumber().matches("\\d{16}")) {  // Simple regex to check 16 digit number
            return ResponseEntity.badRequest().body("Invalid card number");
        }

        if (!cardDetails.getExpirationDate().matches("(0[1-9]|1[0-2])/[0-9]{2}")) {  // MM/YY format
            return ResponseEntity.badRequest().body("Invalid expiration date");
        }

        if (!cardDetails.getSecurityCode().matches("\\d{3,4}")) {  // 3 or 4 digits
            return ResponseEntity.badRequest().body("Invalid security code");
        }

        fillDemoData(10000);

        // Assume validation passes
        return ResponseEntity.ok("Card linked successfully");
    }


    @PostMapping("/generate-report")
    public ResponseEntity<ByteArrayResource> generateReport(@RequestParam(value = "startDate", required = false, defaultValue = "2024-03-01") String startDate,
                                                            @RequestParam(value = "endDate", required = false, defaultValue = "2024-03-31") String endDate) throws IOException {
        byte[] reportBytes = elasticRegister.generateReport(startDate, endDate);

        ByteArrayResource resource = new ByteArrayResource(reportBytes);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report.xlsx");
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(reportBytes.length)
                .body(resource);
    }


    public void fillDemoData(int countOfGeneratedTransaction) {

        // Creating some fake bank accounts
        bankAccountRegister.create(new BankAccount(70, "Revolut", "payments", 1, 1235.10, 1, 1, LocalDateTime.now().toString(), LocalDateTime.now().toString()));
        bankAccountRegister.create(new BankAccount(71, "N26", "credit_card", 2, 3823.56, 1, 0, LocalDateTime.now().toString(), LocalDateTime.now().toString()));
        bankAccountRegister.create(new BankAccount(72, "Fineco", "account_balance", 3, 0.00, 1, 0, LocalDateTime.now().toString(), LocalDateTime.now().toString()));

        // Creating some fake categories
        categoryTransactionRegister.create(new CategoryTransaction(10, "Out", "restaurant", 0, "", null, LocalDateTime.now().toString(), LocalDateTime.now().toString()));
        categoryTransactionRegister.create(new CategoryTransaction(11, "Home", "home", 1, "", null, LocalDateTime.now().toString(), LocalDateTime.now().toString()));
        categoryTransactionRegister.create(new CategoryTransaction(12, "Furniture", "home", 2, "", 11, LocalDateTime.now().toString(), LocalDateTime.now().toString()));
        categoryTransactionRegister.create(new CategoryTransaction(13, "Shopping", "shopping_cart", 3, "", null, LocalDateTime.now().toString(), LocalDateTime.now().toString()));
        categoryTransactionRegister.create(new CategoryTransaction(14, "Leisure", "subscriptions", 4, "", null, LocalDateTime.now().toString(), LocalDateTime.now().toString()));
        categoryTransactionRegister.create(new CategoryTransaction(15, "Salary", "work", 5, "", null, LocalDateTime.now().toString(), LocalDateTime.now().toString()));

        // Creating some currencies
        currencyRegister.create(new Currency(1, "€", "EUR", "Euro", 1));
        currencyRegister.create(new Currency(2, "$", "USD", "United States Dollar", 0));
        currencyRegister.create(new Currency(3, "CHF", "CHF", "Switzerland Franc", 0));
        currencyRegister.create(new Currency(4, "£", "GBP", "United Kingdom Pound", 0));

        // Create fake budgets
        budgetRegister.create(new Budget(1, 13, "Grocery", 400.00, 1, LocalDateTime.now().toString(), LocalDateTime.now().toString()));
        budgetRegister.create(new Budget(2, 11, "Home", 123.45, 0, LocalDateTime.now().toString(), LocalDateTime.now().toString()));

        // Generating transactions
        generateTransactions(countOfGeneratedTransaction);

    }

    private void generateTransactions(int countOfGeneratedTransaction) {
        Random rnd = new Random();
        int[] accounts = {70, 71, 72};
        String[] outNotes = {"Grocery", "Tolls", "Toys", "ETF Consultant Parcel", "Concert", "Clothing", "Pizza", "Drugs", "Laundry", "Taxes", "Health insurance", "Furniture", "Car Fuel", "Train", "Amazon", "Delivery", "CHEK dividends", "Babysitter", "sono.pove.ro Fees", "Quingentole trip"};
        int[] categories = {10, 11, 12, 13, 14, 15};

        int dateInPastMaxRange = (countOfGeneratedTransaction / 90) * 30;
        double fakeSalary = 5000.0;
        LocalDateTime now = LocalDateTime.now();

        List<Transaction> transactions = new ArrayList<>();

        for (int i = 0; i < countOfGeneratedTransaction; i++) {

            double randomAmount = generateRandomAmount(rnd);

            String randomType = "OUT";
            int randomBankAccountId = accounts[rnd.nextInt(accounts.length)];
            String randomOutNote = outNotes[rnd.nextInt(outNotes.length)];
            int randomCategoryId = categories[rnd.nextInt(categories.length)];
            Integer idBankAccountTransfer = null;

            String randomDate = now.minusDays(rnd.nextInt(dateInPastMaxRange)).minusHours(rnd.nextInt(20)).minusMinutes(rnd.nextInt(50)).toString();

            if (i % (countOfGeneratedTransaction / 100) == 0) { // Every 1% of transactions
                randomType = "TRSF";
                randomOutNote = "Transfer";
                randomBankAccountId = 70; // Sender account
                randomCategoryId = 0; // No category for transfers
                idBankAccountTransfer = accounts[rnd.nextInt(accounts.length)];
                randomAmount = (fakeSalary / 100) * 70;

                while (idBankAccountTransfer == randomBankAccountId) { // Ensure FROM/TO are not the same
                    idBankAccountTransfer = accounts[rnd.nextInt(accounts.length)];
                }
            }

            transactions.add(new Transaction(i, randomDate, randomAmount, randomType, randomOutNote, randomCategoryId, randomBankAccountId, idBankAccountTransfer, 0, null, null, null, null, randomDate, randomDate));
        }

        // Batch insert transactions to the database
        transactions.forEach(transaction -> transactionRegister.create(transaction));

        // Add salary every month
        for (int i = 1; i <= dateInPastMaxRange / 30; i++) {
            String salaryDateTime = now.minusMonths(i).withDayOfMonth(27).toString();
            countOfGeneratedTransaction += i;
            transactions.add(new Transaction(countOfGeneratedTransaction, salaryDateTime, fakeSalary, "IN", "Salary", 15, 70, null, (byte) 0, null, null, null, null, salaryDateTime, salaryDateTime));
        }

        String date = now.toString();

        // Add recurring payments
        transactions.add(new Transaction(++countOfGeneratedTransaction, null, 7.99, "OUT", "Netflix", 14, 71, null, (byte) 1, "monthly", 19, "2022-11-14", null, date, date));
        transactions.add(new Transaction(++countOfGeneratedTransaction, null, 292.39, "OUT", "Car Loan", 13, 70, null, (byte) 1, "monthly", 27, "2019-10-03", "2024-10-02", date, date));

    }

    private double generateRandomAmount(Random rnd) {
        if (rnd.nextInt(10) < 8) {
            return rnd.nextDouble() * (19.99 - 1) + 1; // More likely to give lower amounts
        } else {
            return rnd.nextDouble() * (250 - 100) + 100; // Occasionally give larger amounts. 250 - maxAmountOfSingleTransaction

        }
    }

}
