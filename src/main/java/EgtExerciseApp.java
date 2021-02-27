package main.java;

import javax.swing.*;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class EgtExerciseApp {

    private static final Logger log = Logger.getLogger(EgtExerciseApp.class.getName());

    public static void main(String[] args) {
        List<String> filePaths = Arrays.asList(args);

        // If no arguments provided, trigger a file picker. (i.e. via command line or batch process)
        if (filePaths.size() == 0) {
            JFileChooser file = new JFileChooser();
            file.setMultiSelectionEnabled(true);
            file.setFileSelectionMode(JFileChooser.FILES_ONLY);
            file.setFileHidingEnabled(true);
            String filePath = "";
            if (file.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                File[] files = file.getSelectedFiles();
                filePaths = Arrays.stream(files).map(File::getPath).collect(Collectors.toList());
            }
        }

        // Loop over provided filePaths and create a Vehicle report for each.
        if (filePaths.size() > 0) {
            for (String filePath : filePaths) {
                log.info("Attempting to Process: " + filePath);
                ReportBuilder builder = new ReportBuilder();
                VehicleReport reportData = builder.getReportDataFromFile(filePath);
                if (reportData != null && reportData.getVehiclesGroupedByYear().size() > 0) {
                    String reportFilePath = builder.writeTxtReport(reportData);
                    if (reportFilePath != null) {
                        log.info("Report Created: " + reportFilePath);
                    } else {
                        log.info("FAILED to create report for : " + filePath);
                    }
                }
            }
        }

        return;
    }
}
