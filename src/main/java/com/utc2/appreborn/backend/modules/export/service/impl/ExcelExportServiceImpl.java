package com.utc2.appreborn.backend.modules.export.service.impl;

import com.utc2.appreborn.backend.modules.dormitory.repository.DormitoryRegistrationRepository;
import com.utc2.appreborn.backend.modules.enrollment.repository.CourseEnrollmentRepository;
import com.utc2.appreborn.backend.modules.export.service.ExcelExportService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExcelExportServiceImpl implements ExcelExportService {

    private final CourseEnrollmentRepository    enrollmentRepository;
    private final DormitoryRegistrationRepository dormRegistrationRepository;

    // ═══════════════════════════════════════════════════════════════════════
    //  EXPORT ĐĂNG KÝ HỌC PHẦN
    // ═══════════════════════════════════════════════════════════════════════

    @Override
    public byte[] exportEnrollments() throws IOException {
        List<Object[]> rows = enrollmentRepository.findAllEnrollmentsForExport();

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Đăng ký học phần");

            // Styles
            CellStyle titleStyle  = createTitleStyle(workbook);
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle   = createDataStyle(workbook);
            CellStyle altStyle    = createAltDataStyle(workbook);

            // Tiêu đề
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("DANH SÁCH ĐĂNG KÝ HỌC PHẦN");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 10));

            // Header row
            String[] headers = {
                    "STT", "MSSV", "Họ và Tên", "Lớp", "Khoa",
                    "Mã MH", "Tên Môn Học", "Số TC",
                    "Học Kỳ", "Năm HK", "Trạng Thái", "Ngày ĐK"
            };
            Row headerRow = sheet.createRow(2);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            // Column order từ query:
            // 0=student_code, 1=full_name, 2=class_name, 3=faculty,
            // 4=course_code, 5=course_name, 6=credits,
            // 7=semester_name, 8=academic_year, 9=status, 10=registered_at
            for (int i = 0; i < rows.size(); i++) {
                Object[] row  = rows.get(i);
                Row dataRow   = sheet.createRow(i + 3);
                CellStyle cs  = (i % 2 == 0) ? dataStyle : altStyle;

                setCell(dataRow, 0, i + 1, cs);
                setCell(dataRow, 1,  row[0], cs);
                setCell(dataRow, 2,  row[1], cs);
                setCell(dataRow, 3,  row[2], cs);
                setCell(dataRow, 4,  row[3], cs);
                setCell(dataRow, 5,  row[4], cs);
                setCell(dataRow, 6,  row[5], cs);
                setCell(dataRow, 7,  row[6], cs);
                setCell(dataRow, 8,  row[7], cs);
                setCell(dataRow, 9,  row[8], cs);
                setCell(dataRow, 10, row[9], cs);
                setCell(dataRow, 11, row[10], cs);
            }

            autoSizeColumns(sheet, headers.length);
            return toBytes(workbook);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  EXPORT ĐĂNG KÝ KTX
    // ═══════════════════════════════════════════════════════════════════════

    @Override
    public byte[] exportDormRegistrations() throws IOException {
        List<Object[]> rows = dormRegistrationRepository.findAllDormRegistrationsForExport();

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Đăng ký KTX");

            CellStyle titleStyle  = createTitleStyle(workbook);
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle   = createDataStyle(workbook);
            CellStyle altStyle    = createAltDataStyle(workbook);

            // Tiêu đề
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("DANH SÁCH ĐĂNG KÝ KÝ TÚC XÁ");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 12));

            // Header
            String[] headers = {
                    "STT", "MSSV", "Họ và Tên", "Lớp", "Khoa",
                    "Mã Phòng", "Tòa", "Loại Phòng",
                    "Ngày Vào", "Ngày Ra", "Trạng Thái", "Tổng Phí (VNĐ)", "Trạng Thái TT", "Ngày ĐK"
            };
            Row headerRow = sheet.createRow(2);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            // Column order từ query:
            // 0=student_code, 1=full_name, 2=class_name, 3=faculty,
            // 4=room_code, 5=building, 6=room_type,
            // 7=start_date, 8=end_date, 9=status, 10=total_fee, 11=paid_status, 12=registered_at
            for (int i = 0; i < rows.size(); i++) {
                Object[] row = rows.get(i);
                Row dataRow  = sheet.createRow(i + 3);
                CellStyle cs = (i % 2 == 0) ? dataStyle : altStyle;

                setCell(dataRow, 0,  i + 1,  cs);
                setCell(dataRow, 1,  row[0],  cs);
                setCell(dataRow, 2,  row[1],  cs);
                setCell(dataRow, 3,  row[2],  cs);
                setCell(dataRow, 4,  row[3],  cs);
                setCell(dataRow, 5,  row[4],  cs);
                setCell(dataRow, 6,  row[5],  cs);
                setCell(dataRow, 7,  row[6],  cs);
                setCell(dataRow, 8,  row[7],  cs);
                setCell(dataRow, 9,  row[8],  cs);
                setCell(dataRow, 10, row[9],  cs);
                setCell(dataRow, 11, row[10], cs);
                setCell(dataRow, 12, row[11], cs);
                setCell(dataRow, 13, row[12], cs);
            }

            autoSizeColumns(sheet, headers.length);
            return toBytes(workbook);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  HELPERS
    // ═══════════════════════════════════════════════════════════════════════

    private void setCell(Row row, int col, Object value, CellStyle style) {
        Cell cell = row.createCell(col);
        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else {
            cell.setCellValue(value.toString());
        }
        cell.setCellStyle(style);
    }

    private void autoSizeColumns(Sheet sheet, int colCount) {
        for (int i = 0; i < colCount; i++) {
            sheet.autoSizeColumn(i);
            // Thêm padding nhỏ
            int width = sheet.getColumnWidth(i);
            sheet.setColumnWidth(i, Math.min(width + 512, 15000));
        }
    }

    private byte[] toBytes(XSSFWorkbook workbook) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        return out.toByteArray();
    }

    // ── Cell styles ──────────────────────────────────────────────────────

    private CellStyle createTitleStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createHeaderStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorder(style);
        return style;
    }

    private CellStyle createDataStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorder(style);
        return style;
    }

    private CellStyle createAltDataStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorder(style);
        return style;
    }

    private void setBorder(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }
}