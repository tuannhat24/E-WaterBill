# E-WaterBill - Android Kotlin Project

---

## Cấu trúc nhánh
- `main`:  
  - Chỉ chứa code hoàn thiện, **protected branch**.  
  - Chỉ **chủ repo** mới có quyền merge vào nhánh này.
- `develop`:  
  - Nhánh chính để dev bình thường.  
  - Mọi người có thể push, merge vào nhánh này.
- Feature branches:  
  - Tách từ `develop` để làm từng task riêng.  
  - Merge vào `develop` khi hoàn thiện.

**Workflow minh họa:**

---

## Hướng dẫn push/pull

1. **Pull nhánh mới nhất trước khi bắt đầu làm việc:**
```bash
git checkout develop
git pull origin develop

2. Tạo feature branch từ develop:
git checkout -b feature/<tên-feature>

3. Commit code:
git add .
git commit -m "Mô tả công việc"

4. Push feature branch lên remote:
git push -u origin feature/<tên-feature>

5. Merge feature branch vào develop:
Tạo Pull Request trên GitHub → review → merge.
Không merge trực tiếp vào main.

6. Cập nhật nhánh develop:
git checkout develop
git pull origin develop

Run Project
Mở Android Studio → Import Project hoặc mở folder repo.
Chờ Gradle sync hoàn tất.
Chạy app trên emulator hoặc device thật.

Lưu ý
Không push trực tiếp vào main.
Không commit local.properties hoặc các file build outputs (*.apk, *.dex, build/, *.iml).
Luôn pull nhánh develop mới nhất trước khi tạo feature branch.
Giữ .gitignore đầy đủ để tránh push file không cần thiết.
