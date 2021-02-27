package main.java;

import main.java.model.Vehicle;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Logger;

public class ReportBuilder {
    private static final Logger log = Logger.getLogger(ReportBuilder.class.getName());

    public VehicleReport getReportDataFromFile(String filePath) {
        VehicleReport report = new VehicleReport(filePath);
        if (!filePath.endsWith(".csv")) {
            return null;
        }

        int lineNumber = 0;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(filePath));

            // Check if header is correct.
            String newLine = reader.readLine();
            lineNumber++;
            if (newLine == null || newLine.isBlank()) { log.info("No Header Found"); return null; }
            if (!newLine.equalsIgnoreCase(report.EXPECTED_HEADERS)) { log.info("Incorrect Headers"); return null; }

            // Parse everything below the header.
            newLine = reader.readLine();
            lineNumber++;
            while (newLine != null) {
                // Check if header has the correct number of columns
                String[] lineItems = newLine.split(",");
                if (lineItems.length != report.EXPECTED_HEADER_COUNT) {
                    log.info("Incorrect number of columns at line " + lineNumber);
                    reader.close();
                    return null;
                }


                // Parse new line and store in Vehicle object
                Vehicle vehicle = new Vehicle();
                int year = 0;
                try {
                    year = Integer.parseInt(lineItems[0].trim());
                    vehicle.setYear(year);
                } catch (NumberFormatException nfeInt) { log.info("Invalid year"); reader.close(); return null; }
                vehicle.setMake(lineItems[1].trim());
                if (vehicle.getMake() == null || vehicle.getMake().isEmpty()) {
                    log.info("Make value missing at line " + lineNumber);
                    reader.close();
                    return null;
                }
                vehicle.setModel(lineItems[2].trim());
                if (vehicle.getModel() == null || vehicle.getModel().isEmpty()) {
                    log.info("Model value missing at line " + lineNumber);
                    reader.close();
                    return null;
                }
                try {
                    vehicle.setMsrp(new BigDecimal(lineItems[3].trim()));
                } catch (NumberFormatException nfeBd) {
                    log.info("Invalid msrp at line " + lineNumber);
                    reader.close();
                    return null;
                }


                // A TreeMap of TreeMaps!!! { year : { make : Vehicle } }
                // Adding Vehicle to collection for it's given year,
                // otherwise create a new collection for that year and put it in the map
                // Using this data structure so we it's more scalable and avoids needing to do
                // sorting on each list of Vechicles within each year on larger data sets.
                // Since cars have only been around for about a century, and it's trivial to sort
                // 100 numbers I could have used a Map, or a bit more messy, A prebuilt array or List
                // with the indices representing years... inxex 0 = 2000, 1 = 2001, 99 = 1999, etc.
                // But this approach is easier to read and implement
                if (report.getVehiclesGroupedByYear().containsKey(year)) {
                    report.getVehiclesGroupedByYear().get(year).put(vehicle.getMake(), vehicle);
                } else {
                    // Could use a List to store Vehicles, and then Sort them for the report
                    // But the TreeSet should be better for larger data sets.
                    Map<String, Vehicle> vehicles = new TreeMap<>() ;
                    vehicles.put(vehicle.getMake(), vehicle);
                    report.getVehiclesGroupedByYear().put(year, vehicles);
                }

                newLine = reader.readLine();
                lineNumber++;
            }
        } catch (Exception e) {
            log.info(e.getMessage());
            if (reader != null) { try {reader.close(); } catch (IOException ioe) {log.info(ioe.getMessage()); } }
        }

        log.info("Reached End of File. Processed " + (lineNumber - 1) + " lines");
        if (reader != null) { try {reader.close(); } catch (IOException ioe) {log.info(ioe.getMessage()); } }

        return report;
    }

    public String writeTxtReport(VehicleReport report) {
        String reportFilePath = report.getReportFilePath();
        FileWriter writer = null;
        try {
            writer = new FileWriter(reportFilePath);
            createReportHeader(writer, report);
            createReportBody(writer, report);
            createReportFooter(writer, report);
            writer.close();
            return report.getReportFilePath();
        } catch (IOException error) {
            // Close the FileWriter
            if (writer != null) { try { writer.close(); } catch (IOException e) { log.info(e.getMessage()); } }
            log.info("Failed to Create File at " + reportFilePath);
        }
        return null;
    }

    private void createReportHeader(FileWriter writer, VehicleReport report) {
        try {
            writer.write(String.format(report.HEADER_TEMPLATE,
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern(report.REPORT_DATE_PATTERN))
            ));
            writer.write(report.LINE_BREAK);
            writer.write(report.LINE_BREAK);

        } catch (IOException error) {
            // Close the FileWriter
            if (writer != null) { try { writer.close(); } catch (IOException e) { log.info(e.getMessage()); } }
            log.info("Failed to Create File at " + report.getReportFilePath());
        }
    }

    private void createReportBody(FileWriter writer, VehicleReport report) {
        // This will also calculate the total msrp needed for the Footer,
        // so no need to loop through all the data again.
        Set<Integer> years = report.getVehiclesGroupedByYear().keySet();
        for (int year : years) {
            try {
                writer.write(String.valueOf(year));
                writer.write(report.LINE_BREAK);
                Set<String> makes = report.getVehiclesGroupedByYear().get(year).keySet();
                for (String make : makes) {
                    Vehicle vehicle = report.getVehiclesGroupedByYear().get(year).get(make);
                    writer.write(report.FAKE_TAB);
                    writer.write(report.getLineItem(vehicle));
                    writer.write(report.LINE_BREAK);
                    report.setMsrpTotal(report.getMsrpTotal().add(vehicle.getMsrp()));
                }
            } catch (IOException error) {
                // Close the FileWriter
                if (writer != null) { try { writer.close(); } catch (IOException e) { log.info(e.getMessage()); } }
                log.info("Failed to Create File at " + report.getReportFilePath());
            }
        }
    }

    private void createReportFooter(FileWriter writer, VehicleReport report) {
        try {
            writer.write(report.LINE_BREAK);
            writer.write(report.FOOTER_TITLE);
            writer.write(report.LINE_BREAK);
            writer.write(report.getMsrpSumTotalLine());
            writer.write(report.LINE_BREAK);
            writer.write(report.getListPriceSumTotalLine());

        } catch (IOException error) {
            // Close the FileWriter
            if (writer != null) { try { writer.close(); } catch (IOException e) { log.info(e.getMessage()); } }
            log.info("Failed to Create File at " + report.getReportFilePath());
        }
    }

}
