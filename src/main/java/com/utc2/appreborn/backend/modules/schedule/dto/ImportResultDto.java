package com.utc2.appreborn.backend.modules.schedule.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ImportResultDto {

    /** Số dòng lưu thành công */
    private int success;

    /** Số dòng bị bỏ qua / lỗi */
    private int failed;

    /** Danh sách lỗi chi tiết theo từng dòng */
    private List<RowError> errors;

    @Data
    @Builder
    public static class RowError {
        /** Số thứ tự dòng trong Excel (1-indexed từ phần dữ liệu) */
        private int row;
        /** Tên cột liên quan */
        private String field;
        /** Mô tả lỗi */
        private String message;
    }
}
