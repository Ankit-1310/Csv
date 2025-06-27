package com.csvCompare.service;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CsvCompareService {

    /**
     * Compares two CSV files and writes a comparison report to a result CSV file.
     *
     * @param file1Path     The file path of the first CSV file.
     * @param file2Path     The file path of the second CSV file.
     * @param resultFilePath The file path where the result CSV file should be saved.
     * @throws IOException  If an I/O error occurs during file operations.
     * @throws CsvException If an error occurs while reading the CSV files.
     */
    public void compareCsvFiles(String file1Path, String file2Path, String resultFilePath) throws IOException, CsvException {
        Path path1 = Paths.get(file1Path);
        Path path2 = Paths.get(file2Path);
        Path resultPath = Paths.get(resultFilePath);

        try (CSVReader reader1 = new CSVReader(new FileReader(path1.toFile()));
             CSVReader reader2 = new CSVReader(new FileReader(path2.toFile()));
             CSVWriter writer = new CSVWriter(new FileWriter(resultPath.toFile()))) {

            // Read all rows from both CSV files
            List<String[]> file1Rows = reader1.readAll();
            List<String[]> file2Rows = reader2.readAll();

            // Create maps for easy lookup by row data
            Map<String, String[]> file1Map = createRowMap(file1Rows);
            Map<String, String[]> file2Map = createRowMap(file2Rows);

            // Write header to the result CSV file
            writer.writeNext(new String[]{"Comparison Type", "File 1 Row/Column", "File 2 Row/Column", "Details"});

            // Compare rows present in both files
            for (String key : file1Map.keySet()) {
                if (file2Map.containsKey(key)) {
                    compareRows(key, file1Map.get(key), file2Map.get(key), writer);
                } else {
                    writer.writeNext(new String[]{"Missing in File 2", String.join(",", file1Map.get(key)), "", "Row present in File 1 but missing in File 2"});
                }
            }

            // Identify rows present in file2 but not in file1
            for (String key : file2Map.keySet()) {
                if (!file1Map.containsKey(key)) {
                    writer.writeNext(new String[]{"Missing in File 1", "", String.join(",", file2Map.get(key)), "Row present in File 2 but missing in File 1"});
                }
            }
        }
    }

    /**
     * Creates a map where the key is a row represented as a comma-separated string
     * and the value is the row itself.
     *
     * @param rows The list of rows to be mapped.
     * @return A map with row data as keys and row arrays as values.
     */
    private Map<String, String[]> createRowMap(List<String[]> rows) {
        Map<String, String[]> rowMap = new HashMap<>();
        for (String[] row : rows) {
            // Use the entire row data as the key
            String key = String.join(",", row);
            rowMap.put(key, row);
        }
        return rowMap;
    }

    /**
     * Compares two rows and writes differences to the result CSV file.
     *
     * @param key     The key representing the row data.
     * @param row1    The first row to compare.
     * @param row2    The second row to compare.
     * @param writer  The CSVWriter instance to write the results.
     */
    private void compareRows(String key, String[] row1, String[] row2, CSVWriter writer) {
        // Compare each cell in the rows
        for (int i = 0; i < Math.max(row1.length, row2.length); i++) {
            String col1 = i < row1.length ? row1[i] : "";
            String col2 = i < row2.length ? row2[i] : "";

            // Write differences to the result CSV file
            if (!col1.equals(col2)) {
                writer.writeNext(new String[]{
                        "Modified",
                        col1,
                        col2,
                        "Difference in row: " + key + ", Column: " + (i + 1)
                });
            }
        }
    }
}
