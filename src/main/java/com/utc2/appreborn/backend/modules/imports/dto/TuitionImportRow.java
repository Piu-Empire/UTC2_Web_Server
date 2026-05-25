package com.utc2.appreborn.backend.modules.imports.dto;

import lombok.Data;

/**
 * Cột Excel/CSV cho import học phí:
 * student_code | semester_id | total_amount | paid_amount | due_date | payment_method
 */
@Data
public class TuitionImportRow {
    private String studentCode;
    private String semesterId;
    private String totalAmount;
    private String paidAmount;
    private String dueDate;        // yyyy-MM-dd
    private String paymentMethod;
}