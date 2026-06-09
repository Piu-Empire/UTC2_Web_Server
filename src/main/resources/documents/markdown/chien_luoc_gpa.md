# Hướng dẫn Chiến lược và Đặt mục tiêu GPA

Khi sinh viên yêu cầu tính điểm GPA hiện tại hoặc đặt ra mục tiêu/chiến lược để tăng GPA (đạt bằng Khá, Giỏi, Xuất sắc), AI cần xử lý và tư vấn theo các quy tắc sau:

## 1. Phân loại môn học
Dựa vào dữ liệu học tập, hãy chia rõ thành 2 nhóm:
- **Các môn ĐÃ HỌC XONG:** Là các môn đã có điểm chính thức. Dùng các môn này để tính GPA thực tế đã tích lũy tính đến thời điểm hiện tại.
- **Các môn CHƯA HỌC XONG:** Là các môn đang học trong học kỳ này, chưa có điểm tổng kết (isPassed có thể là false, điểm tổng kết là 0.0 hoặc null do chưa thi). 

## 2. Cách tính điểm dự phóng cho học kỳ hiện tại
- **KHÔNG THỂ** lấy các môn chưa học xong (chưa có điểm) đem vào cộng chung như điểm 0 để chia trung bình. Điều này sẽ làm GPA bị kéo tụt sai lệch.
- **MẶC ĐỊNH GIẢ ĐỊNH LÀ 8.0:** Với các môn chưa học xong, hãy **mặc định giả định môn đó đạt 8.0 (hệ 10, tương đương 3.5 hệ 4)** để tính toán thử xem nếu học kỳ này thi tốt thì GPA sẽ lên được bao nhiêu.
- **BẮT BUỘC THÔNG BÁO:** Bạn phải ghi rõ cho người dùng biết giả định này. Ví dụ: *"Lưu ý: Các môn A, B chưa có điểm tổng kết, tôi sẽ tạm giả định bạn đạt 8.0 (3.5 hệ 4) để tính toán điểm dự kiến cho học kỳ này."*

## 3. Gợi ý chiến lược tăng GPA các kỳ sau
Nếu sinh viên đặt mục tiêu đạt GPA toàn khóa một mức nào đó (ví dụ 3.2 để bằng Giỏi):
- Xác định tổng số tín chỉ toàn khóa (nếu không có số liệu chính xác, hãy giả định khoảng 120 - 130 tín chỉ cho hệ Cử nhân/Kỹ sư).
- Tính tổng điểm hệ 4 đã tích lũy = Tổng (Điểm hệ 4 môn đã học × số tín chỉ tương ứng).
- Tính số điểm hệ 4 cần đạt cho phần tín chỉ còn lại: `Điểm trung bình các kỳ sau = (Mục tiêu GPA × Tổng TC toàn khóa - Tổng điểm hệ 4 đã có) / Số TC còn lại`.
- Phân tích tính khả thi: Nếu con số yêu cầu quá cao (ví dụ > 4.0), hãy nói rõ là không thể và khuyên sinh viên học lại/cải thiện các môn điểm thấp (D, D+) thay vì chỉ cố gắng cho các môn mới.
- Đề xuất lộ trình: Liệt kê số điểm cần duy trì trong các học kỳ tiếp theo (ví dụ: "Mỗi học kỳ bạn cần duy trì trung bình 3.0 (B) trở lên").
