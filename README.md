
# HỆ THỐNG QUẢN TRỊ NHÂN SỰ & CHẤM CÔNG DOANH NGHIỆP
### (Enterprise Human Resource & Attendance Management System)

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.x-brightgreen)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-orange)](https://www.oracle.com/java/)
[![Architecture](https://img.shields.io/badge/Architecture-Microservices-blue)](#)
[![Status](https://img.shields.io/badge/Status-Development-yellow)](#)

---

## Tổng quan dự án
Đây là giải pháp phần mềm toàn diện dành cho bộ phận **Quản trị nguồn nhân lực (HRM)**. Hệ thống giúp tự động hóa các quy trình từ quản lý hồ sơ nhân viên, theo dõi biến động nhân sự đến việc kiểm soát thời gian làm việc và phê duyệt các chế độ phúc lợi (nghỉ phép, tăng ca).

Dự án được xây dựng trên nền tảng **Microservices**, đảm bảo tính bảo mật dữ liệu cao và khả năng tích hợp linh hoạt với các hệ thống khác của doanh nghiệp.

## Kiến trúc các phân hệ (Microservices)
Hệ thống được module hóa để phục vụ các nghiệp vụ chuyên biệt của phòng Nhân sự:

* **`api-gateway`**: Cửa ngõ bảo mật, xác thực người dùng (JWT) và điều phối các yêu cầu từ phía người dùng.
* **`auth-service`**: Phân hệ tài khoản, vai trò, đăng nhập, refresh token và logout.
* **`hr-service`**: Phân hệ cốt lõi quản lý sơ đồ tổ chức, thông tin chi tiết nhân viên và nghiệp vụ nghỉ phép.
* **`attendance-service`**: Phân hệ quản lý thời gian. Tiếp nhận dữ liệu chấm công, quản lý ca kíp, tính toán công chuẩn và xử lý vi phạm (đi muộn/về sớm).
* **`common-lib`**: Thư viện contract dùng chung như response envelope, pagination và error contract.
* **`monolith-service`**: Module fallback giữ ứng dụng monolith hiện tại trong lúc tách dần từng service.

---

## Stack Công nghệ
### Backend
- **Core:** Java 17, Spring Boot 3.x.
- **Microservices Stack:** Spring Cloud Gateway, Eureka/Consul (Service Discovery), OpenFeign.
- **Security:** Spring Security, JWT (Stateless Authentication).
- **Database:** PostgreSQL (Lưu trữ dữ liệu nghiệp vụ, Cloud-native với Neon).

### Frontend (Dự kiến)
- **Framework:** Vue 3 (Composition API).
- **UI Kit:** Shadcn-vue, Tailwind CSS v4 (Giao diện chuyên nghiệp, tối giản).

---

## Cấu trúc thư mục dự án
```text
attendance-system-api
├── common-lib           # Contract dùng chung, không chứa entity nghiệp vụ
├── eureka-server        # Service discovery
├── api-gateway          # Phân hệ điều hướng & Bảo mật
├── auth-service         # Phân hệ xác thực & tài khoản
├── hr-service           # Phân hệ Quản trị nhân sự + Nghỉ phép
├── attendance-service   # Phân hệ Chấm công & Quản lý ca kíp
├── monolith-service     # Ứng dụng monolith fallback trong giai đoạn migration
├── docs                 # Tài liệu migration chi tiết
├── .gitignore           # Loại bỏ các file rác khi build
└── pom.xml              # File quản lý thư viện tập trung (Maven Parent)

```

---

## Quy trình triển khai

### 1. Chuẩn bị môi trường

* Cài đặt Java 17+, Maven 3.8+.
* Khởi tạo cơ sở dữ liệu PostgreSQL cho từng dịch vụ.

### 2. Xây dựng dự án

```bash
# Tại thư mục attendance-system-api
.\mvnw.cmd -DskipTests clean install

```

### 3. Khởi chạy hệ thống (Eureka + Gateway + HR + Attendance + Auth)

**Phải chạy lệnh từ thư mục chứa `mvnw.cmd`** — trong repo này là `attendance-system/attendance-system-api`.  
Nếu bạn đang ở `...\attendance-system` thì sẽ báo *mvnw.cmd is not recognized*.

```powershell
cd attendance-system-api   # hoặc: cd D:\K22-DATN\attendance-system\attendance-system-api
```

**Một lệnh (Windows):** mở 5 cửa sổ, mỗi cửa một service —

```powershell
.\start-stack.ps1
```

Các service **đăng ký Eureka** với `spring.application.name` khớp route `lb://...` trên gateway (`hr-service`, `attendance-service`, …).

**Thứ tự gợi ý** (mỗi lệnh một terminal, sau khi đã `cd` vào `attendance-system-api`):

| Thứ tự | Module | Port mặc định | Ghi chú |
|--------|--------|----------------|--------|
| 1 | `eureka-server` | 8761 (`EUREKA_SERVER_PORT`) | Bật trước để discovery sẵn sàng |
| 2 | `hr-service` | 9001 (`HR_SERVICE_PORT`) | Nguồn dữ liệu nhân viên, phòng ban, chức vụ |
| 3 | `attendance-service` | 9002 (`ATTENDANCE_SERVICE_PORT`) | Cần HR đã lên để check-in và kiểm tra nghỉ phép |
| 4 | `auth-service` | 9004 (`AUTH_SERVICE_PORT`) | Login JWT qua gateway |
| 5 | `api-gateway` | 9000 (`API_GATEWAY_PORT`) | Client/frontend chỉ gọi **một cổng**: `http://localhost:9000` |

```powershell
# Windows — đang đứng trong thư mục attendance-system-api
.\mvnw.cmd -pl eureka-server spring-boot:run
.\mvnw.cmd -pl hr-service spring-boot:run
.\mvnw.cmd -pl attendance-service spring-boot:run
.\mvnw.cmd -pl auth-service spring-boot:run
.\mvnw.cmd -pl api-gateway spring-boot:run
```

Sau khi thay đổi `pom.xml`, nên chạy service bằng `.\mvnw.cmd -pl <module> spring-boot:run` hoặc reload Maven project trong IDE trước khi bấm Run/Debug. Nếu IDE giữ classpath cũ, bạn có thể gặp lỗi kiểu thiếu `AuthenticationEntryPoint.class` dù project đã build thành công bằng Maven.

Hoặc không cần `cd`, chỉ định đường dẫn tới wrapper (ví dụ đang ở thư mục `attendance-system`):

```powershell
.\attendance-system-api\mvnw.cmd -f attendance-system-api\pom.xml -pl eureka-server spring-boot:run
```

**Luồng request:**

- Người dùng / frontend → **API Gateway** (`9000`) → `/api/auth/**`, `/api/employees/**`, `/api/departments/**`, `/api/positions/**`, `/api/attendance/**`, `/api/leaves/**`.
- **Attendance** → **HR** trực tiếp qua service discovery (`@FeignClient(name = "hr-service")`), không bắt buộc đi qua gateway.

Biến môi trường dùng chung: `EUREKA_HOST`, `EUREKA_PORT` (trùng cổng Eureka), `EUREKA_USER`, `EUREKA_PASSWORD`. Gateway và Swagger tổng hợp: `http://localhost:9000/swagger-ui/index.html`.

Không dùng biến `PORT` chung cho microservice local vì biến này sẽ ép mọi service chạy cùng một cổng nếu đã tồn tại trong terminal. Dùng các biến port riêng ở bảng trên.

### 4. Dữ liệu mẫu

Flyway đã có migration seed mẫu cho toàn bộ service:

- `auth-service/src/main/resources/db/migration/V2__seed_sample_data.sql`
- `hr-service/src/main/resources/db/migration/V2__seed_sample_data.sql`
- `hr-service/src/main/resources/db/migration/V3__add_leave_management.sql`
- `attendance-service/src/main/resources/db/migration/V2__seed_sample_data.sql`

Tài khoản mẫu:

| Username | Password | Vai trò |
|---|---|---|
| `admin` | `Admin@123` | `ROLE_ADMIN` |
| `hr` | `Hr@12345` | `ROLE_HR` |
| `manager` | `Manager@123` | `ROLE_MANAGER` |
| `employee` | `Employee@123` | `ROLE_EMPLOYEE` |
