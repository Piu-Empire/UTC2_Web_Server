# Module: Assessment (Đánh giá rèn luyện)

## API Endpoints (tất cả cần JWT)

| Method |          Endpoint                            | Luồng         | Mô tả                         |
|--------|----------------------------------------------|---------------|-----------------------------  |
| `GET`  | `/api/v1/assessment/periods`                 | → App         | Danh sách học kỳ              |
| `POST` | `/api/v1/assessment/student`                 | App → Server  | SV lưu tự đánh giá            |
| `GET`  | `/api/v1/assessment/student?periodId=`       | → App         | SV xem lại dữ liệu đã lưu     |
| `POST` | `/api/v1/assessment/advisor`                 | App → Server  | SV lưu đánh giá CVHT          |
| `POST` | `/api/v1/assessment/external/import`         | Admin → Server| Import điểm Khoa/Lớp/Trường   |
| `GET`  | `/api/v1/assessment/external?periodId=`      | → App         | App đọc điểm readonly         |
| `GET`  | `/api/v1/assessment/admin/student?periodId=` | → Admin       | Admin xem/export SV           |
| `GET`  | `/api/v1/assessment/admin/advisor?periodId=` | → Admin       | Admin xem/export CVHT         |

## Luồng dữ liệu

```
App → Server → DB   (SV tự đánh giá, SV đánh giá CVHT)   — Admin chỉ đọc, không sửa
Admin → Server → DB → App   (điểm Khoa/Lớp/Trường)       — App chỉ đọc, không sửa
```