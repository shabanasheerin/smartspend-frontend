package com.smartspend.dto.receipt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptResponse {
    private Long id;
    private Long expenseId;
    private String fileName;
    private String fileUrl;
    private String contentType;
    private Long fileSizeBytes;
    private LocalDateTime createdAt;
}
