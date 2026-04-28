# Kế Hoạch Tách Microservice

Tài liệu này mô tả kế hoạch tách backend hiện tại từ monolith Spring Boot thành kiến trúc microservice. Chi tiết kỹ thuật nền tảng vẫn tham chiếu README của dự án và chỉ bổ sung những phần cần thiết cho quá trình migration.

## 1. Trạng Thái Hiện Tại

- Backend gốc được giữ trong module `monolith-service` để fallback trong giai đoạn migration.
- Các domain chính đã được tách sang service riêng: Auth, HR, Attendance và Request.
- Schema ban đầu đang gộp toàn bộ Auth, HR, Attendance và Leave trong `V1__init_monolith.sql`.
- Các dependency nội bộ đang gọi trực tiếp qua service/repository, ví dụ:
  - `auth-service` gọi `hr-service` qua `HrClient`.
  - `attendance-service` gọi `hr-service` qua `HrClient`.
  - `request-service` gọi `hr-service` qua `HrClient`.
  - `attendance-service` gọi `request-service` qua `RequestClient` trong job đánh dấu vắng mặt.

## 2. Boundary Mục Tiêu

| Service | Trách nhiệm | Sở hữu dữ liệu |
| --- | --- | --- |
| `api-gateway` | Một cổng vào cho frontend, route theo path, đặt lớp xác thực gateway về sau | Không có DB nghiệp vụ |
| `eureka-server` | Service discovery cho môi trường dev/local | Không có DB |
| `auth-service` | Login, refresh token, logout, đổi mật khẩu, user/role | `users`, `roles`, `user_roles`, `token_blacklist` |
| `hr-service` | Hồ sơ nhân viên, phòng ban, chức vụ, nhận diện khuôn mặt | `employees`, `departments`, `positions` |
| `attendance-service` | Check-in/out, ca làm, phân lịch, ngày nghỉ, báo cáo công giai đoạn đầu | `attendances`, `attendance_logs`, `shifts`, `employee_schedules`, `holidays`, `schedule_templates` |
| `request-service` | Đơn nghỉ phép và workflow phê duyệt | `leave_requests`, `leave_types` |
| `common-lib` | DTO envelope, pagination, error contract dùng chung | Không có DB |

## 3. Nguyên Tắc Tách

- Mỗi service sở hữu entity và migration riêng; không share JPA entity qua `common-lib`.
- Không dùng foreign key xuyên service/database. Các trường như `employee_id`, `user_id`, `approved_by` là tham chiếu logic.
- Giao tiếp đồng bộ ban đầu dùng OpenFeign qua Eureka.
- Gateway route public API; các endpoint `/internal/**` chỉ dành cho service-to-service.
- Không dùng distributed transaction ở giai đoạn đầu. Quy trình nhiều service sẽ dùng validate trước, ghi cục bộ sau, rồi bổ sung compensation/event khi cần.

## 4. Contract Nội Bộ Cần Tạo

### HR cho Auth

- `GET /internal/hr/users/{userId}/employee`
- Trả `EmployeeInternalResponse`: `employeeId`, `userId`, `fullName`, `departmentName`, `positionName`.

### HR cho Attendance

- `GET /internal/hr/employees/{employeeId}/snapshot`
- `POST /internal/hr/face-match`
- `GET /internal/hr/employees/{employeeId}/exists`

### Request cho Attendance

- `GET /internal/leaves/approved?employeeId={id}&date={date}`
- Trả boolean để cron đánh dấu `ON_LEAVE`.

### HR cho Report

- `GET /internal/hr/employees/snapshots?departmentId={id}&employeeId={id}&status=ACTIVE`
- Trả danh sách snapshot nhân viên để `attendance-service` xuất báo cáo.

## 5. Lộ Trình Thực Hiện

### Giai đoạn 0: Dựng nền module

- Chuyển root Maven thành parent `packaging=pom`.
- Đưa app hiện tại vào `monolith-service`.
- Tạo `common-lib`, `eureka-server`, `api-gateway`, `auth-service`, `hr-service`, `attendance-service`, `request-service`.
- Chạy build reactor để đảm bảo skeleton build được.

### Giai đoạn 1: Tách common contract

- Chuyển `ApiResponse`, `PageResponse`, `ErrorCodeContract` vào `common-lib`.
- Các service mới dùng package `com.attendance.common.*`.
- Monolith tạm thời vẫn giữ class cũ để giảm rủi ro, sau đó mới đồng bộ.

### Giai đoạn 2: Tách HR trước

- Di chuyển controller/service/repository/entity/mapper của `Department`, `Position`, `Employee`.
- Tách migration `departments`, `positions`, `employees`.
- Đổi `employees.user_id` thành tham chiếu logic, không FK sang `users`.
- Tạo `/internal/hr/**` cho Auth, Attendance và Request dùng.

### Giai đoạn 3: Tách Auth

- Di chuyển `AuthController`, `AuthService`, `JwtService`, `User`, `Role`, `TokenBlacklist`.
- Tách migration auth.
- Thay direct call `EmployeeService` bằng `HrClient`.
- Quyết định chiến lược token:
  - Giai đoạn đầu: mỗi service verify JWT bằng shared secret.
  - Sau đó: Gateway verify và forward identity header có ký nội bộ.

### Giai đoạn 4: Tách Attendance

- Di chuyển attendance, shift, schedule, holiday, report giai đoạn đầu.
- Thay `EmployeeService` bằng `HrClient`.
- Thay đọc leave trực tiếp trong `AbsentCheckJob` bằng `RequestClient`.
- Tách migration attendance.

### Giai đoạn 5: Tách Request

- Di chuyển leave request và leave type.
- Đổi quan hệ `Employee` trong `LeaveRequest` thành `Long employeeId` và `Long approvedById`.
- Tạo endpoint internal cho Attendance kiểm tra nghỉ phép.

### Giai đoạn 6: Hoàn thiện vận hành

- Cập nhật Dockerfile/Docker Compose cho từng service.
- Thêm script `start-stack.ps1`.
- Cập nhật Swagger gateway.
- Thêm test smoke cho route qua Gateway.

## 6. Thứ Tự Chạy Local Mục Tiêu

1. `eureka-server` - port `8761`
2. `hr-service` - port `9001`
3. `request-service` - port `9003`
4. `attendance-service` - port `9002`
5. `auth-service` - port `9004`
6. `api-gateway` - port `9000`

Frontend chỉ gọi gateway tại `http://localhost:9000`.

## 7. Checklist Đang Thực Hiện

- [x] Tạo kế hoạch migration chi tiết.
- [x] Tạo Maven parent và module skeleton.
- [x] Giữ monolith hiện tại trong `monolith-service`.
- [x] Tạo `common-lib` tối thiểu.
- [x] Chạy build reactor.
- [x] Di chuyển HR domain bước đầu sang `hr-service`.
- [x] Tạo HR internal API cho Auth/Attendance/Report dùng về sau.
- [x] Di chuyển Auth domain bước đầu sang `auth-service`.
- [x] Thay direct call từ Auth sang HR bằng Feign client.
- [x] Tách Request/Leave domain sang `request-service`.
- [x] Tạo Request internal API cho Attendance kiểm tra nghỉ phép đã duyệt.
- [x] Tách Attendance domain sang `attendance-service`.
- [x] Thay direct call từ Attendance/Request bằng Feign client.
- [x] Thêm JWT security cho HR, Request và Attendance service.
- [x] Build toàn bộ backend thành công sau khi tách service.
