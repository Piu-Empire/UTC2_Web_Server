-- ============================================================
-- V16__fix_fee_data_and_recalculate.sql
--
-- Fix data cũ bị lỗi do bug builder userId=NULL:
-- 1. Xóa fee records có user_id = NULL (không biết chủ sở hữu)
-- 2. Với fee "đã đóng đủ": nếu sinh viên đã đăng ký thêm môn sau khi đóng tiền
--    → insert thêm fee record "chưa đóng" cho phần tín chỉ tăng thêm
-- 3. Với fee chưa đóng: tính lại total_amount theo tín chỉ thực tế
-- ============================================================

-- 1. Xóa fee records mồ côi
DELETE FROM fee WHERE user_id IS NULL;

-- 2. Tính lại total_amount cho fee SUBJECT CHƯA ĐÓNG
UPDATE fee f
    JOIN (
    SELECT e.user_id, e.semester_id,
    SUM(c.credits) AS total_credits
    FROM enrollment e
    JOIN course c ON e.course_id = c.course_id
    WHERE e.status != 'đã hủy'
    GROUP BY e.user_id, e.semester_id
    ) ec ON f.user_id = ec.user_id AND f.semester_id = ec.semester_id
    JOIN (
    SELECT s.semester_id,
    COALESCE(
    (SELECT tr.price_per_credit FROM tuition_rate tr
    WHERE tr.academic_year = s.academic_year LIMIT 1),
    (SELECT tr.price_per_credit FROM tuition_rate tr
    WHERE tr.academic_year IS NULL LIMIT 1),
    600000
    ) AS price_per_credit
    FROM semester s
    ) rate ON rate.semester_id = f.semester_id
    SET f.total_amount     = ec.total_credits * rate.price_per_credit,
        f.remaining_amount = (ec.total_credits * rate.price_per_credit) - COALESCE(f.paid_amount, 0),
        f.status           = CASE
        WHEN COALESCE(f.paid_amount, 0) = 0 THEN 'chưa đóng'
        WHEN (ec.total_credits * rate.price_per_credit) > COALESCE(f.paid_amount, 0)
        THEN 'đóng một phần'
        ELSE f.status
END
WHERE f.fee_type = 'SUBJECT'
  AND f.status != 'đã đóng đủ';

-- 3. Tạo fee CHƯA ĐÓNG cho (user, semester) có thêm môn SAU KHI đã đóng tiền
--    Điều kiện: tổng tín chỉ hiện tại > tổng tín chỉ đã được trả (total_amount của fee đã đóng)
INSERT INTO fee (user_id, semester_id, fee_type, total_amount, paid_amount, remaining_amount, due_date, status)
SELECT f.user_id,
       f.semester_id,
       'SUBJECT',
       extra.extra_amount,
       0,
       extra.extra_amount,
       DATE_ADD(CURDATE(), INTERVAL 1 MONTH),
       'chưa đóng'
FROM fee f
         JOIN (
    SELECT e.user_id,
           e.semester_id,
           SUM(c.credits) * COALESCE(
                   (SELECT tr.price_per_credit FROM tuition_rate tr
                                                        JOIN semester s2 ON s2.semester_id = e.semester_id
                    WHERE tr.academic_year = s2.academic_year LIMIT 1),
                   (SELECT tr.price_per_credit FROM tuition_rate tr
                    WHERE tr.academic_year IS NULL LIMIT 1),
                   600000
           ) AS new_total
    FROM enrollment e
             JOIN course c ON e.course_id = c.course_id
    WHERE e.status != 'đã hủy'
    GROUP BY e.user_id, e.semester_id
) ec ON f.user_id = ec.user_id AND f.semester_id = ec.semester_id
         JOIN (
    SELECT (ec2.new_total - f2.total_amount) AS extra_amount,
           f2.user_id, f2.semester_id
    FROM fee f2
             JOIN (
        SELECT e.user_id, e.semester_id,
               SUM(c.credits) * COALESCE(
                       (SELECT tr.price_per_credit FROM tuition_rate tr
                                                            JOIN semester s2 ON s2.semester_id = e.semester_id
                        WHERE tr.academic_year = s2.academic_year LIMIT 1),
                       (SELECT tr.price_per_credit FROM tuition_rate tr
                        WHERE tr.academic_year IS NULL LIMIT 1),
                       600000
               ) AS new_total
        FROM enrollment e
                 JOIN course c ON e.course_id = c.course_id
        WHERE e.status != 'đã hủy'
        GROUP BY e.user_id, e.semester_id
    ) ec2 ON f2.user_id = ec2.user_id AND f2.semester_id = ec2.semester_id
    WHERE f2.fee_type = 'SUBJECT'
      AND f2.status = 'đã đóng đủ'
      AND ec2.new_total > f2.total_amount
) extra ON f.user_id = extra.user_id AND f.semester_id = extra.semester_id
WHERE f.fee_type = 'SUBJECT'
  AND f.status = 'đã đóng đủ'
  AND ec.new_total > f.total_amount
  AND NOT EXISTS (
    SELECT 1 FROM fee f3
    WHERE f3.user_id = f.user_id
      AND f3.semester_id = f.semester_id
      AND f3.fee_type = 'SUBJECT'
      AND f3.status = 'chưa đóng'
);

-- 4. Tạo fee cho (user, semester) có enrollment nhưng CHƯA CÓ fee nào
INSERT INTO fee (user_id, semester_id, fee_type, total_amount, paid_amount, remaining_amount, due_date, status)
SELECT e.user_id,
       e.semester_id,
       'SUBJECT',
       SUM(c.credits) * COALESCE(
               (SELECT tr.price_per_credit FROM tuition_rate tr
                                                    JOIN semester s ON s.semester_id = e.semester_id
                WHERE tr.academic_year = s.academic_year LIMIT 1),
               (SELECT tr.price_per_credit FROM tuition_rate tr
                WHERE tr.academic_year IS NULL LIMIT 1),
               600000
       ),
       0,
       SUM(c.credits) * COALESCE(
               (SELECT tr.price_per_credit FROM tuition_rate tr
                                                    JOIN semester s ON s.semester_id = e.semester_id
                WHERE tr.academic_year = s.academic_year LIMIT 1),
               (SELECT tr.price_per_credit FROM tuition_rate tr
                WHERE tr.academic_year IS NULL LIMIT 1),
               600000
       ),
       DATE_ADD(CURDATE(), INTERVAL 1 MONTH),
       'chưa đóng'
FROM enrollment e
         JOIN course c ON e.course_id = c.course_id
WHERE e.status != 'đã hủy'
  AND NOT EXISTS (
    SELECT 1 FROM fee f WHERE f.user_id = e.user_id
      AND f.semester_id = e.semester_id AND f.fee_type = 'SUBJECT'
)
GROUP BY e.user_id, e.semester_id;
