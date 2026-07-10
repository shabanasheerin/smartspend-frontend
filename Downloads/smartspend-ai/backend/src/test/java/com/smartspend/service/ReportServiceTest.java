package com.smartspend.service;

import com.smartspend.dto.report.TransactionReportRow;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock private com.smartspend.repository.IncomeRepository incomeRepository;
    @Mock private com.smartspend.repository.ExpenseRepository expenseRepository;

    @InjectMocks
    private ReportService reportService;

    private List<TransactionReportRow> sampleRows() {
        return List.of(
                TransactionReportRow.builder()
                        .date(LocalDate.of(2026, 7, 1))
                        .type("INCOME")
                        .category("SALARY")
                        .amount(new BigDecimal("50000"))
                        .notes("July salary")
                        .build(),
                TransactionReportRow.builder()
                        .date(LocalDate.of(2026, 7, 3))
                        .type("EXPENSE")
                        .category("Food")
                        .amount(new BigDecimal("450.50"))
                        .notes("Groceries, weekly")
                        .build()
        );
    }

    @Test
    void toCsv_producesNonEmptyOutputWithHeader() {
        byte[] csv = reportService.toCsv(sampleRows());
        String content = new String(csv);

        assertTrue(content.startsWith("Date,Type,Category,Amount,Notes"));
        assertTrue(content.contains("SALARY"));
        // Notes containing a comma should be quoted per CSV escaping rules.
        assertTrue(content.contains("\"Groceries, weekly\""));
    }

    @Test
    void toExcel_producesNonEmptyWorkbookBytes() {
        byte[] excel = reportService.toExcel(sampleRows());
        assertTrue(excel.length > 0);
    }

    @Test
    void toPdf_producesNonEmptyDocumentBytes() {
        byte[] pdf = reportService.toPdf(sampleRows(), "Test Report");
        assertTrue(pdf.length > 0);
        // PDF files start with the %PDF- magic bytes.
        assertTrue(new String(pdf, 0, 5).startsWith("%PDF-"));
    }
}
