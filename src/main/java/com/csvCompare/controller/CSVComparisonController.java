package com.csvCompare.controller;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

@RestController
public class CSVComparisonController {

    /**
     * Endpoint to compare two CSV files and generate a comparison report.
     *
     * @param file1Path        The file path of the first CSV file.
     * @param file2Path        The file path of the second CSV file.
     * @param resultFilePath   The file path where the result CSV file should be saved.
     * @param resultExcelPath  The file path where the result Excel file should be saved.
     * @return A success message or an error message if an exception occurs.
     */
	@GetMapping("/compare-csv")
	public String compareCSVFiles() {
	    String resultFilePath = "comparison_result.csv";
	    String resultExcelPath = "comparison_result.xlsx";

	    try (
	        BufferedReader file1Reader = new BufferedReader(
	            new InputStreamReader(new ClassPathResource("28thJune - Sheet1.csv").getInputStream())
	        );
	        BufferedReader file2Reader = new BufferedReader(
	            new InputStreamReader(new ClassPathResource("Inv_28thJune_cleaned.csv").getInputStream())
	        );
	        BufferedWriter resultWriter = new BufferedWriter(new FileWriter(resultFilePath));
	        Workbook workbook = new XSSFWorkbook()
	    ) {
	        Sheet sheet = workbook.createSheet("Comparison Result");
	        int rowNum = 0;
	        String line;

	        // Read headers and log them
	        String header1 = file1Reader.readLine();
	        String header2 = file2Reader.readLine();
	        System.out.println("Header1: " + header1);
	        System.out.println("Header2: " + header2);

	        // Store file1 data into a Map
	        Map<String, String> file1Map = new HashMap<>();
	        while ((line = file1Reader.readLine()) != null) {
	            String[] parts = line.split(",", -1); // handles empty columns
	            if (parts.length < 3) continue;

	            String key = parts[0].trim() + "-" + parts[1].trim(); // ussid-slaveid
	            String isAllocated1 = parts[2].trim();
	            file1Map.put(key, isAllocated1);
	        }

	        // Compare with file2
	        while ((line = file2Reader.readLine()) != null) {
	            String[] parts = line.split(",", -1);
	            if (parts.length < 3) continue;

	            String key = parts[0].trim() + "-" + parts[1].trim();
	            String isAllocated2 = parts[2].trim();

	            if (file1Map.containsKey(key)) {
	                String isAllocated1 = file1Map.get(key);
	                if (!isAllocated1.equalsIgnoreCase(isAllocated2)) {
	                    // Log mismatch
	                    System.out.println("Mismatch Found: " + key + " | File1: " + isAllocated1 + ", File2: " + isAllocated2);

	                    // Write to Excel and CSV
	                    Row row = sheet.createRow(rowNum++);
	                    for (int i = 0; i < parts.length; i++) {
	                        Cell excelCell = row.createCell(i);
	                        excelCell.setCellValue(parts[i]);
	                        excelCell.setCellStyle(createCellStyle(workbook, IndexedColors.YELLOW.getIndex()));
	                        resultWriter.write(parts[i] + (i < parts.length - 1 ? "," : "\n"));
	                    }
	                }
	            }
	        }

	        // Save Excel result
	        try (FileOutputStream excelFileOut = new FileOutputStream(resultExcelPath)) {
	            workbook.write(excelFileOut);
	        }

	        System.out.println("Total Mismatched Rows Written: " + rowNum);

	    } catch (IOException e) {
	        e.printStackTrace();
	        return "Error during comparison: " + e.getMessage();
	    }

	    return "CSV files compared successfully. Results saved to " + resultFilePath + " and " + resultExcelPath;
	}

//    @GetMapping("/compare-csv")
//    public String compareCSVFiles(
//            @RequestParam("file1Path") String file1Path,
//            @RequestParam("file2Path") String file2Path,
//            @RequestParam("resultFilePath") String resultFilePath,
//            @RequestParam("resultExcelPath") String resultExcelPath) {
//
//        // Try-with-resources to ensure all streams are closed automatically.
//        try (BufferedReader file1Reader = new BufferedReader(new FileReader(file1Path));
//             BufferedReader file2Reader = new BufferedReader(new FileReader(file2Path));
//             BufferedWriter resultWriter = new BufferedWriter(new FileWriter(resultFilePath));
//             Workbook workbook = new XSSFWorkbook()) {
//
//            // Create an Excel sheet to store the comparison result.
//            Sheet sheet = workbook.createSheet("Comparison Result");
//            int rowNum = 0;
//
//            String line1;
//            String line2;
//
//            // Loop through both files line by line.
//            while ((line1 = file1Reader.readLine()) != null && (line2 = file2Reader.readLine()) != null) {
//                String[] cells1 = line1.split(",");
//                String[] cells2 = line2.split(",");
//
//                // Create a new row in the Excel sheet for each line in the CSV files.
//                Row row = sheet.createRow(rowNum++);
//                for (int i = 0; i < cells1.length; i++) {
//                    String cell1 = cells1[i];
//                    String cell2 = i < cells2.length ? cells2[i] : "";
//
//                    // Create a cell in the Excel sheet and set its value.
//                    Cell excelCell = row.createCell(i);
//                    excelCell.setCellValue(cell1);
//
//                    // Highlight the cell based on whether the values match.
//                    if (cell1 != null && cell1.equals(cell2)) {
//                        // Matching cells get a white background.
//                        excelCell.setCellStyle(createCellStyle(workbook, IndexedColors.WHITE.getIndex()));
//                    } else {
//                        // Non-matching cells get a yellow background.
//                        excelCell.setCellStyle(createCellStyle(workbook, IndexedColors.YELLOW.getIndex()));
//                    }
//
//                    // Write the cell value to the result CSV file.
//                    resultWriter.write(cell1 + (i < cells1.length - 1 ? "," : "\n"));
//                }
//            }
//
//            // Write any remaining lines from the second file to the result file and highlight them in red in the Excel file.
//            while ((line2 = file2Reader.readLine()) != null) {
//                Row row = sheet.createRow(rowNum++);
//                String[] cells2 = line2.split(",");
//
//                for (int i = 0; i < cells2.length; i++) {
//                    String cell2 = cells2[i];
//
//                    // Create a cell and set its value.
//                    Cell excelCell = row.createCell(i);
//                    excelCell.setCellValue(cell2);
//
//                    // Highlight the entire row in red to indicate it's missing from the first file.
//                    excelCell.setCellStyle(createCellStyle(workbook, IndexedColors.RED.getIndex()));
//
//                    // Write the line to the result CSV file.
//                    resultWriter.write(cell2 + (i < cells2.length - 1 ? "," : "\n"));
//                }
//            }
//
//            // Save the Excel file to the specified path.
//            try (FileOutputStream excelFileOut = new FileOutputStream(resultExcelPath)) {
//                workbook.write(excelFileOut);
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//            return "Error during comparison: " + e.getMessage();
//        }
//
//        // Return a success message if the process completes without exceptions.
//        return "CSV files compared successfully. Results saved to " + resultFilePath + " and " + resultExcelPath;
//    }

    /**
     * Helper method to create a CellStyle with a specific background color.
     *
     * @param workbook The workbook in which the style will be used.
     * @param color    The index of the color to be applied.
     * @return A CellStyle with the specified background color.
     */
    private CellStyle createCellStyle(Workbook workbook, short color) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(color);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }
    
    @PostMapping(value = "/compare-csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String compareCSVFiles(
            @RequestParam("file1") MultipartFile file1,
            @RequestParam("file2") MultipartFile file2
    ) {
        try (
            BufferedReader file1Reader = new BufferedReader(new InputStreamReader(file1.getInputStream()));
            BufferedReader file2Reader = new BufferedReader(new InputStreamReader(file2.getInputStream()));
            Workbook workbook = new XSSFWorkbook()
        ) {
            Sheet sheet = workbook.createSheet("Comparison Result");
            int rowNum = 0;
            String line1;
            String line2;

            while ((line1 = file1Reader.readLine()) != null && (line2 = file2Reader.readLine()) != null) {
                String[] cells1 = line1.split(",");
                String[] cells2 = line2.split(",");

                Row row = sheet.createRow(rowNum++);
                for (int i = 0; i < cells1.length; i++) {
                    String cell1 = cells1[i];
                    String cell2 = i < cells2.length ? cells2[i] : "";

                    Cell excelCell = row.createCell(i);
                    excelCell.setCellValue(cell1);
                    excelCell.setCellStyle(
                        createCellStyle(workbook, cell1.equals(cell2) ? IndexedColors.WHITE.getIndex() : IndexedColors.YELLOW.getIndex())
                    );
                }
            }

            while ((line2 = file2Reader.readLine()) != null) {
                Row row = sheet.createRow(rowNum++);
                String[] cells2 = line2.split(",");

                for (int i = 0; i < cells2.length; i++) {
                    Cell cell = row.createCell(i);
                    cell.setCellValue(cells2[i]);
                    cell.setCellStyle(createCellStyle(workbook, IndexedColors.RED.getIndex()));
                }
            }

            // Save to a fixed location or return as a stream
            File resultExcel = new File("comparison_result.xlsx");
            try (FileOutputStream out = new FileOutputStream(resultExcel)) {
                workbook.write(out);
            }

            return "Comparison complete. Excel saved at: " + resultExcel.getAbsolutePath();

        } catch (IOException e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

}
