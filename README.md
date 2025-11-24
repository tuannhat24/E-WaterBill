# BillManager - E-WaterBill

## Mục đích
Đây là đồ án quản lý hóa đơn nước (E-WaterBill), phục vụ việc học tập và thực hành Android.

## Cấu trúc project
- `app/` : module chính Android
- `gradle/libs.versions.toml` : quản lý dependency
- `build.gradle.kts` : cấu hình Gradle module app
- `.gitignore` : loại trừ file không cần thiết
- `README.md` : hướng dẫn project

## Cách clone và làm việc với Git
```bash
1. Clone project về:
git clone https://github.com/tuannhat24/E-WaterBill.git

2. Tạo branch mới từ nhánh develop, ví dụ:
Sao chép mã
git checkout develop
git checkout -b feature/ten-tinh-nang

3. Không đụng vào nhánh main trực tiếp.

4. Sau khi hoàn tất feature → merge vào develop → sau đó mới merge develop vào main khi cần.

Chạy project
1. Mở project bằng Android Studio

2. Sync Gradle → Build → Run trên máy ảo hoặc thiết bị Android

Lưu ý
Nhóm thống nhất sử dụng API 34 (compileSdk = 34, targetSdk = 34, minSdk = 24)
Chỉ tạo branch từ develop, tránh thay đổi trực tiếp trên main
Sync Gradle sau khi clone để các dependency đúng version
