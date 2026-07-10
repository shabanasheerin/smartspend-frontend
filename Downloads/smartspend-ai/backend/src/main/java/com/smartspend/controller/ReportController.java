package com.smartspend.controller;

import com.smartspend.dto.common.ApiResponse;
import com.smartspend.dto.report.TransactionReportRow;
import com.smartspend.entity.User;
import com.smartspend.security.AuthenticatedUserProvider;
import com.smartspend.security.CustomUserDetails;
import com.smartspend.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Daily/weekly/monthly/yearly transaction reports with CSV export")
public class ReportController {

    private final ReportService reportService;
    private final AuthenticatedUserProvider userProvider;

    @GetMapping
    @Operation(summary = "Get a transaction report as JSON for a given period")
    public ResponseEntity<ApiResponse<List<TransactionReportRow>>> getReport(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam ReportService.ReportPeriod period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        User user = userProvider.resolve(principal);
        return ResponseEntity.ok(ApiResponse.success(reportService.buildReport(user, period, date)));
    }

    @GetMapping("/export/csv")
    @Operation(summary = "Export a transaction report as a downloadable CSV file")
    public ResponseEntity<byte[]> exportCsv(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam ReportService.ReportPeriod period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        User user = userProvider.resolve(principal);
        List<TransactionReportRow> rows = reportService.buildReport(user, period, date);
        byte[] csv = reportService.toCsv(rows);

        String filename = "smartspend-report-" + period.name().toLowerCase() + ".csv";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(filename).build().toString())
                .body(csv);
    }

    @GetMapping("/export/excel")
    @Operation(summary = "Export a transaction report as a downloadable Excel (.xlsx) file")
    public ResponseEntity<byte[]> exportExcel(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam ReportService.ReportPeriod period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        User user = userProvider.resolve(principal);
        List<TransactionReportRow> rows = reportService.buildReport(user, period, date);
        byte[] excel = reportService.toExcel(rows);

        String filename = "smartspend-report-" + period.name().toLowerCase() + ".xlsx";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(filename).build().toString())
                .body(excel);
    }

    @GetMapping("/export/pdf")
    @Operation(summary = "Export a transaction report as a downloadable PDF file")
    public ResponseEntity<byte[]> exportPdf(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam ReportService.ReportPeriod period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        User user = userProvider.resolve(principal);
        List<TransactionReportRow> rows = reportService.buildReport(user, period, date);
        byte[] pdf = reportService.toPdf(rows, "SmartSpend AI — " + period.name() + " Report");

        String filename = "smartspend-report-" + period.name().toLowerCase() + ".pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(filename).build().toString())
                .body(pdf);
    }
}
