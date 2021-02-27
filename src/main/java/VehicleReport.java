package main.java;

import main.java.model.Vehicle;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class VehicleReport {

    private Map<Integer, Map<String, Vehicle>> vehiclesMappedByYear;
    private String originalFilePath;
    private BigDecimal msrpTotal;

    // Logic and Validation
    public final BigDecimal TAX_RATE = new BigDecimal(1.07);
    public final String EXPECTED_HEADERS = "year,make,model,msrp";
    public final int EXPECTED_HEADER_COUNT = 4;

    // File Naming
    public final String FILENAME_TIMPSTAMP_PATTERN = "yyyy-MM-dd_HH:mm:ss_Z";
    public final String REPORT_EXTENSION = ".txt";
    public final String UNDERSCORE = "_";
    public final String SLASH = "/";
    public final String PERIOD = ".";

    // directory, date, originalName, originalExt, newExt
    public final String REPORT_FILENAME_TEMPLATE = "%sREPORT_%s__%s(%s)%s";

    //Report Writing
    public final String HEADER_TEMPLATE = "--- Vehicle Report ---                                        Date: %s";
    public final String LINE_ITEM_TEMPLATE = "%-30sMSRP: %-15sList Price: %s";
    public final String FAKE_TAB = "     ";
    public final String LINE_BREAK = "\n";
    public final String PRETTY_CURRENCY_TEMPLATE = "$%,.2f";
    public final String REPORT_DATE_PATTERN = "MM/dd/YYYY";
    public final String FOOTER_TITLE = "--- Grand Total ---";
    public final String SUM_TOTAL_TITLE_MSRP = "MSRP:";
    public final String SUM_TOTAL_TITLE_LIST_PRICE = "List Price:";
    public final String SUM_TOTAL_TITLE_TEMPLATE = "     %-15s%s";


    public VehicleReport(String originalFilePath) {
        this.originalFilePath = originalFilePath;
        // Stores all Vehicles by year in descending order.
        this.vehiclesMappedByYear = new TreeMap<>(Collections.reverseOrder());
        this.msrpTotal = new BigDecimal(0);
    }


    public Map<Integer, Map<String, Vehicle>> getVehiclesGroupedByYear() {
        return vehiclesMappedByYear;
    }

    public void setVehiclesMappedByYear(Map<Integer, Map<String, Vehicle>> vehiclesMappedByYear) {
        this.vehiclesMappedByYear = vehiclesMappedByYear;
    }

    public String getOriginalFilePath() {
        return originalFilePath;
    }

    public void setOriginalFilePath(String originalFilePath) {
        this.originalFilePath = originalFilePath;
    }

    public BigDecimal getMsrpTotal() {
        return msrpTotal;
    }

    public void setMsrpTotal(BigDecimal msrpTotal) {
        this.msrpTotal = msrpTotal;
    }

    public String getReportFilePath() {
        return String.format(REPORT_FILENAME_TEMPLATE,
                getDirectory(),
                ZonedDateTime.now().format(DateTimeFormatter.ofPattern(FILENAME_TIMPSTAMP_PATTERN)),
                getOriginalName(),
                getOriginalFileExtention(),
                REPORT_EXTENSION);
    }

    private Object getOriginalName() {
        return originalFilePath.substring(originalFilePath.lastIndexOf(SLASH)+1,
                originalFilePath.lastIndexOf(PERIOD));
    }

    public String getDirectory() {
        return originalFilePath.substring(0, originalFilePath.lastIndexOf(SLASH) + 1);
    }

    public String getOriginalFileExtention() {
        return originalFilePath.substring(originalFilePath.lastIndexOf(PERIOD) + 1);
    }

    public String getLineItem(Vehicle vehicle) {
        return String.format(LINE_ITEM_TEMPLATE,
                vehicle.getMakeModel(),
                getPrettyCurrency(vehicle.getMsrp()),
                getPrettyCurrency(vehicle.getListPrice(TAX_RATE)));
    }

    public String getPrettyCurrency(BigDecimal money) {
        return String.format(Locale.ENGLISH, PRETTY_CURRENCY_TEMPLATE, money);
    }

    public String getPrettyCurrencyListPriceTotal() {
        return getPrettyCurrency(msrpTotal.multiply(TAX_RATE));
    }

    public String getMsrpSumTotalLine() {
        return String.format(SUM_TOTAL_TITLE_TEMPLATE, SUM_TOTAL_TITLE_MSRP, getPrettyCurrency(msrpTotal));
    }

    public String getListPriceSumTotalLine() {
        return String.format(SUM_TOTAL_TITLE_TEMPLATE, SUM_TOTAL_TITLE_LIST_PRICE, getPrettyCurrencyListPriceTotal());
    }
}
