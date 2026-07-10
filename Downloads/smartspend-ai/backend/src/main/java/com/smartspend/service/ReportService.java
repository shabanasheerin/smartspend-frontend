package com.smartspend.service;

import com.smartspend.dto.report.TransactionReportRow;
import com.smartspend.entity.User;
import com.smartspend.repository.ExpenseRepository;
import com.smartspend.repository.IncomeRepository;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;

    public enum ReportPeriod {DAILY, WEEKLY, MONTHLY, YEARLY}

    public List<TransactionReportRow> buildReport(User user, ReportPeriod period, LocalDate anchorDate) {
        LocalDate[] range = resolveRange(period, anchorDate);
        return buildReportForRange(user, range[0], range[1]);
    }

    public List<TransactionReportRow> buildReportForRange(User user, LocalDate start, LocalDate end) {
        List<TransactionReportRow> rows = new ArrayList<>();

        incomeRepository.findByUserAndIncomeDateBetween(user, start, end).forEach(income ->
                rows.add(TransactionReportRow.builder()
                        .date(income.getIncomeDate())
                        .type("INCOME")
                        .category(income.getSource().name())
                        .amount(income.getAmount())
                        .notes(income.getNotes())
                        .build()));

        expenseRepository.findByUserAndExpenseDateBetween(user, start, end).forEach(expense ->
                rows.add(TransactionReportRow.builder()
                        .date(expense.getExpenseDate())
                        .type("EXPENSE")
                        .category(expense.getCategory().getName())
                        .amount(expense.getAmount())
                        .notes(expense.getNotes())
                        .build()));

        rows.sort(Comparator.comparing(TransactionReportRow::getDate));
        return rows;
    }

    public byte[] toCsv(List<TransactionReportRow> rows) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(baos, true, StandardCharsets.UTF_8)) {
            writer.println("Date,Type,Category,Amount,Notes");
            for (TransactionReportRow row : rows) {
                writer.printf("%s,%s,%s,%s,%s%n",
                        row.getDate(),
                        row.getType(),
                        escapeCsv(row.getCategory()),
                        row.getAmount(),
                        escapeCsv(row.getNotes() == null ? "" : row.getNotes()));
            }
        }
        return baos.toByteArray();
    }

    public byte[] toExcel(List<TransactionReportRow> rows) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("Transactions");

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);

            Row header = sheet.createRow(0);
            String[] columns = {"Date", "Type", "Category", "Amount", "Notes"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIndex = 1;
            for (TransactionReportRow row : rows) {
                Row excelRow = sheet.createRow(rowIndex++);
                excelRow.createCell(0).setCellValue(row.getDate().toString());
                excelRow.createCell(1).setCellValue(row.getType());
                excelRow.createCell(2).setCellValue(row.getCategory());
                excelRow.createCell(3).setCellValue(row.getAmount().doubleValue());
                excelRow.createCell(4).setCellValue(row.getNotes() == null ? "" : row.getNotes());
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(baos);
            return baos.toByteArray();
        } catch (IOException ex) {
            throw new RuntimeException("Failed to generate Excel report: " + ex.getMessage(), ex);
        }
    }

    public byte[] toPdf(List<TransactionReportRow> rows, String reportTitle) {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            float margin = 50;
            float yStart = page.getMediaBox().getHeight() - margin;
            float rowHeight = 18;
            float tableWidth = page.getMediaBox().getWidth() - 2 * margin;
            float[] columnWidths = {70, 70, 110, 80, tableWidth - 330};

            PDPageContentStream content = new PDPageContentStream(document, page);
            content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
            content.beginText();
            content.newLineAtOffset(margin, yStart);
            content.showText(reportTitle);
            content.endText();

            float y = yStart - 30;
            content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 9);
            String[] headers = {"Date", "Type", "Category", "Amount", "Notes"};
            y = writeRow(content, headers, margin, y, columnWidths, rowHeight);

            content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 9);

            for (TransactionReportRow row : rows) {
                if (y < margin + rowHeight) {
                    content.close();
                    PDPage newPage = new PDPage(PDRectangle.A4);
                    document.addPage(newPage);
                    page = newPage;
                    y = yStart;
                    content = new PDPageContentStream(document, page);
                    content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 9);
                }

                String[] values = {
                        row.getDate().toString(),
                        row.getType(),
                        truncate(row.getCategory(), 18),
                        row.getAmount().toString(),
                        truncate(row.getNotes() == null ? "" : row.getNotes(), 28)
                };
                y = writeRow(content, values, margin, y, columnWidths, rowHeight);
            }

            content.close();
            document.save(baos);
            return baos.toByteArray();
        } catch (IOException ex) {
            throw new RuntimeException("Failed to generate PDF report: " + ex.getMessage(), ex);
        }
    }

    private float writeRow(PDPageContentStream content, String[] values, float startX, float y,
                            float[] columnWidths, float rowHeight) throws IOException {
        float x = startX;
        for (int i = 0; i < values.length; i++) {
            content.beginText();
            content.newLineAtOffset(x, y);
            content.showText(values[i] == null ? "" : values[i]);
            content.endText();
            x += columnWidths[i];
        }
        return y - rowHeight;
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        return value.length() > maxLength ? value.substring(0, maxLength - 1) + "…" : value;
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private LocalDate[] resolveRange(ReportPeriod period, LocalDate anchor) {
        LocalDate date = anchor != null ? anchor : LocalDate.now();
        return switch (period) {
            case DAILY -> new LocalDate[]{date, date};
            case WEEKLY -> new LocalDate[]{date.minusDays(6), date};
            case MONTHLY -> new LocalDate[]{date.withDayOfMonth(1), date.withDayOfMonth(date.lengthOfMonth())};
            case YEARLY -> new LocalDate[]{date.withDayOfYear(1), date.withDayOfYear(date.lengthOfYear())};
        };
    }
}
