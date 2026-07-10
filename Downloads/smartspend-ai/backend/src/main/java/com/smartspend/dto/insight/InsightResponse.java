package com.smartspend.dto.insight;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsightResponse {
    private String type;      // e.g. "HIGHEST_CATEGORY", "MONTH_OVER_MONTH", "PREDICTED_END_OF_MONTH"
    private String title;
    private String message;
}
