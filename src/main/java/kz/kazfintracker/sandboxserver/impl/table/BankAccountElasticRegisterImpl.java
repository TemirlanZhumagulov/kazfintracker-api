package kz.kazfintracker.sandboxserver.impl.table;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import kz.kazfintracker.sandboxserver.elastic.ElasticIndexes;
import kz.kazfintracker.sandboxserver.elastic.ElasticWorker;
import kz.kazfintracker.sandboxserver.elastic.model.ClientResponse;
import kz.kazfintracker.sandboxserver.elastic.model.EsBodyWrapper;
import kz.kazfintracker.sandboxserver.migration.util.RND;
import kz.kazfintracker.sandboxserver.model.elastic.*;
import kz.kazfintracker.sandboxserver.model.web.ClientsTableRequest;
import kz.kazfintracker.sandboxserver.model.web.Paging;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class BankAccountElasticRegisterImpl {

    private static final String[] MONTH = {
            "January",
            "February",
            "March",
            "April",
            "May",
            "June",
            "July",
            "August",
            "September",
            "October",
            "November",
            "December"};

    @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
    @Autowired
    private ElasticWorker elasticWorker;

    public static String of(int month) {
        if (month >= 1 && month <= 12) {
            return MONTH[month - 1];
        } else {
            throw new DateTimeException("Invalid value for MonthOfYear: " + month);
        }
    }

    // region retrieval tools
    @Tool("Retrieves all bank accounts")
    public List<BankAccountElastic> loadAllBankAccounts() {
        log.info("start load all bank accounts");
        EsBodyWrapper bodyWrapper = elasticWorker.findAll(ElasticIndexes.INDEX_BANK_ACCOUNT, Paging.of(0, 100));
        log.info("finish load all bank accounts");
        return bodyWrapper.hits.hits()
                .stream()
                .map(hit -> hit._source)
                .map(BankAccountElastic::fromMap)
                .collect(Collectors.toList());
    }

    @Tool("Retrieves all transaction categories")
    public List<CategoryTransactionElastic> loadAllCategoryTransactions() {
        log.info("start load all categories");
        EsBodyWrapper bodyWrapper = elasticWorker.findAll(ElasticIndexes.INDEX_CATEGORY_TRANSACTION, Paging.of(0, 100));
        log.info("finish load all categories");
        return bodyWrapper.hits.hits()
                .stream()
                .map(hit -> hit._source)
                .map(CategoryTransactionElastic::fromMap)
                .collect(Collectors.toList());
    }

    @Tool("Retrieves all currencies")
    public List<CurrencyElastic> loadAllCurrencies() {
        log.info("start load all currencies");
        EsBodyWrapper bodyWrapper = elasticWorker.findAll(ElasticIndexes.INDEX_CURRENCY, Paging.of(0, 100));
        log.info("finish load all currencies");
        return bodyWrapper.hits.hits()
                .stream()
                .map(hit -> hit._source)
                .map(CurrencyElastic::fromMap)
                .collect(Collectors.toList());
    }

    @Tool("Retrieves all budgets")
    public List<BudgetElastic> loadAllBudgets() {
        log.info("start load all budgets");
        EsBodyWrapper bodyWrapper = elasticWorker.findAll(ElasticIndexes.INDEX_BUDGET, Paging.of(0, 100));
        log.info("finish load all budgets");
        return bodyWrapper.hits.hits()
                .stream()
                .map(hit -> hit._source)
                .map(BudgetElastic::fromMap)
                .collect(Collectors.toList());
    }

    @Tool("Retrieves all recurring transaction amounts")
    public List<RecurringTransactionAmountElastic> loadAllRecurringTransactionAmounts() {
        log.info("start load all recurring transaction amounts");
        EsBodyWrapper bodyWrapper = elasticWorker.findAll(ElasticIndexes.INDEX_RECUR_TRANSACTION, Paging.of(0, 100));
        log.info("finish load all recurring transaction amounts");
        return bodyWrapper.hits.hits()
                .stream()
                .map(hit -> hit._source)
                .map(RecurringTransactionAmountElastic::fromMap)
                .collect(Collectors.toList());
    }
    // endregion retrieval tools

    // region calc tools
    @Tool("Calculates the total expenses between the specified start and end dates. Dates are expected in the format \"yyyy-MM-dd\"")
    public double calculateTotalExpenses(@P("The start date as a String \"yyyy-MM-dd\"") String startDate,
                                         @P("The end data as a String \"yyyy-MM-dd\"") String endDate) {
        return elasticWorker.calculateTotalTransactions("OUT", startDate, endDate);
    }

    @Tool("Calculates the total income between the specified start and end dates. Dates are expected in the format \"yyyy-MM-dd\"")
    public double calculateTotalIncome(@P("The start date as a String \"yyyy-MM-dd\"") String startDate,
                                       @P("The end data as a String \"yyyy-MM-dd\"") String endDate) {
        return elasticWorker.calculateTotalTransactions("IN", startDate, endDate);
    }

    @Tool("Calculates current account balance")
    public double calculateCurrentBalance() {
        double income = calculateTotalIncome(LocalDate.ofYearDay(2000, 1).toString(), LocalDate.now().toString());
        double expenses = calculateTotalExpenses(LocalDate.ofYearDay(2000, 1).toString(), LocalDate.now().toString());
        return income - expenses;
    }

    @Tool("Checks budget utilization for each category")
    public Map<String, Double> checkBudgetUtilization() {
        Map<String, Double> budgetUsage = new HashMap<>();
        List<BudgetElastic> budgets = elasticWorker.findAll(ElasticIndexes.INDEX_BUDGET, Paging.of(0, 100)).hits.hits()
                .stream()
                .map(hit -> hit._source)
                .map(BudgetElastic::fromMap)
                .collect(Collectors.toList());

        for (BudgetElastic budget : budgets) {
            double spent = elasticWorker.sumFieldWithQuery(ElasticIndexes.INDEX_TRANSACTION, "amount", "idCategory:" + budget.getIdCategory());
            budgetUsage.put(budget.getName(), spent);
        }
        return budgetUsage;
    }

//    @Tool("Alerts if current month's spending is unusually high")
//    public boolean alertUnusualSpending() {
//        Map<LocalDate, Double> monthlyReport = monthlySpendingReport();
//        double average = monthlyReport.values().stream().mapToDouble(v -> v).average().orElse(0);
//        double currentMonthSpending = monthlyReport.get(LocalDate.now().withDayOfMonth(1));
//        return currentMonthSpending > average * 1.5;  // 50% more than average
//    }

    // endregion calc tools
    public ClientResponse load(ClientsTableRequest tableRequest, Paging paging) {
        log.info("ClientTableRequest's sorting is received: " + tableRequest.sorting);
        log.info("ClientTableRequest's rndTestingId is received: " + tableRequest.rndTestingId);
        EsBodyWrapper bodyWrapper = elasticWorker.find(ElasticIndexes.INDEX_CLIENT, tableRequest, paging);
        log.info("EsBodyWrapper is made: " + bodyWrapper.toString());
        return new ClientResponse(bodyWrapper.hits.hits()
                .stream()
                .map(hit -> hit._source)
                .map(ClientElastic::fromMap)
                .collect(Collectors.toList()), bodyWrapper.hits.total.value);
    }

    @Tool("Returns current date")
    public String getCurrentDate() {
        return LocalDate.now().toString();
    }

    @Tool("Generates a financial report between the specified dates and provides a download link.")
    public String monthlySpendingReport(@P("The start date as a String 'yyyy-MM-dd'") String startDate,
                                        @P("The end date as a String 'yyyy-MM-dd'") String endDate) {
        return "Financial summary report from {" + startDate + "} to {" + endDate +
                "} is available by the link {http://www.kazfintracker.files/" + RND.str(10) + "?startDate=" + startDate + "&endDate" + endDate + "}";
    }

    public byte[] generateReport(String startDate, String endDate) throws IOException {
        Map<String, Double> dailyTotals = elasticWorker.fetchTransactionsAsMonthHistorgram(TransactionType.OUT, startDate, endDate);

        if (dailyTotals.isEmpty()) {
            throw new RuntimeException("HSc8u4DxSq :: No transactions!");
        }

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        dailyTotals.forEach((date, amount) -> dataset.addValue(amount, "Amount", date));

        JFreeChart chart = createChart(dataset, "Financial Summary");

        CategoryPlot plot = (CategoryPlot) chart.getPlot();

        plot.getDomainAxis().setTickLabelFont(new Font("Dialog", Font.PLAIN, 14));
        plot.getRangeAxis().setTickLabelFont(new Font("Dialog", Font.PLAIN, 14));
        chart.getTitle().setFont(new Font("Dialog", Font.BOLD, 16));

        LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
        renderer.setSeriesStroke(0, new BasicStroke(2.0f));

//        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
//            XSSFSheet sheet = workbook.createSheet("Report");
//            // Add chart to the Excel sheet
//            addChartToSheet(chart, sheet, workbook);
//
//            // Write textual data
//            writeDataToSheet(transactions, sheet);
//
//            // Write the output to a file
//            try (FileOutputStream outputStream = new FileOutputStream(outputPath.toFile())) {
//                workbook.write(outputStream);
//            }
//        }

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("Report");

            addChartToSheet(chart, sheet, workbook);

//            writeDataToSheet(transactions, sheet);

            workbook.write(bos);
            return bos.toByteArray();
        }

    }

    private JFreeChart createChart(DefaultCategoryDataset dataset, String title) {
        return ChartFactory.createLineChart(title, "Days of the " + of(3), "Amount", dataset);
    }

    private void addChartToSheet(JFreeChart chart, XSSFSheet sheet, XSSFWorkbook workbook) throws IOException {
        ByteArrayOutputStream chartOut = new ByteArrayOutputStream();
        ChartUtils.writeChartAsPNG(chartOut, chart, 1000, 600);
        int pictureIdx = workbook.addPicture(chartOut.toByteArray(), Workbook.PICTURE_TYPE_PNG);
        chartOut.close();

        XSSFDrawing drawing = sheet.createDrawingPatriarch();
        XSSFClientAnchor anchor = new XSSFClientAnchor();
// Adjust the anchor to better control the size and positioning of the chart
        anchor.setCol1(0);
        anchor.setRow1(5);
        anchor.setCol2(10);  // Specifies the column where the right side of the chart should end
        anchor.setRow2(20);  // Specifies the row where the bottom of the chart should end
        XSSFPicture picture = drawing.createPicture(anchor, pictureIdx);
        picture.resize();
    }

    private void writeDataToSheet(List<TransactionElastic> transactions, XSSFSheet sheet) {
        // Implement writing transaction data to the sheet
    }


}
