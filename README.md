
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
* **`hr-service`**: Phân hệ cốt lõi quản lý sơ đồ tổ chức, thông tin chi tiết nhân viên, hợp đồng lao động, bảo hiểm và khen thưởng/kỷ luật.
* **`attendance-service`**: Phân hệ quản lý thời gian. Tiếp nhận dữ liệu chấm công, quản lý ca kíp, tính toán công chuẩn và xử lý vi phạm (đi muộn/về sớm).
* **`request-service`**: Phân hệ quản lý quy trình (Workflow). Tự động hóa luồng phê duyệt đơn từ giữa nhân viên, trưởng bộ phận và phòng HR.
* **`system-service`**: Quản trị danh mục hệ thống, cấu hình tham số (ngày nghỉ lễ, định mức công) và nhật ký hoạt động (Audit Log).

---

## Stack Công nghệ
### Backend
- **Core:** Java 17, Spring Boot 3.x.
- **Microservices Stack:** Spring Cloud Gateway, Eureka/Consul (Service Discovery), OpenFeign.
- **Security:** Spring Security, JWT (Stateless Authentication).
- **Database:** MySQL (Lưu trữ dữ liệu nghiệp vụ).

### Frontend (Dự kiến)
- **Framework:** Vue 3 (Composition API).
- **UI Kit:** Shadcn-vue, Tailwind CSS v4 (Giao diện chuyên nghiệp, tối giản).

---

## Cấu trúc thư mục dự án
```text
attendance-system-api
├── api-gateway          # Phân hệ điều hướng & Bảo mật
├── hr-service           # Phân hệ Quản trị nhân sự (Core HRM)
├── attendance-service   # Phân hệ Chấm công & Quản lý ca kíp
├── request-service      # Phân hệ Phê duyệt đơn từ (Workflow)
├── system-service       # Phân hệ Cấu hình hệ thống & Danh mục
├── .gitignore           # Loại bỏ các file rác khi build
└── pom.xml              # File quản lý thư viện tập trung (Maven Parent)

```

---

## Quy trình triển khai

### 1. Chuẩn bị môi trường

* Cài đặt Java 17+, Maven 3.8+.
* Khởi tạo cơ sở dữ liệu MySQL cho từng dịch vụ.

### 2. Xây dựng dự án

```bash
# Tại thư mục gốc của dự án
mvn clean install

```

### 3. Khởi chạy hệ thống

Khởi chạy lần lượt các service. Đảm bảo các cấu hình trong `application.yml` (DB URL, Port) đã chính xác:

```bash
# Ví dụ chạy service nhân sự
cd hr-service
mvn spring-boot:run

```
