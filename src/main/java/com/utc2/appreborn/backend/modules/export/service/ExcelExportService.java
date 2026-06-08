package com.utc2.appreborn.backend.modules.export.service;

import java.io.IOException;

public interface ExcelExportService {

    /** Xuất danh sách đăng ký học phần của tất cả sinh viên ra file Excel */
    byte[] exportEnrollments() throws IOException;

    /** Xuất danh sách đăng ký KTX của tất cả sinh viên ra file Excel */
    byte[] exportDormRegistrations() throws IOException;
}