#  Hướng Dẫn Chi Tiết — Movie Web Service

## Tư tưởng bài toán nối tiếp Ở bạn đã cào dữ liệu và lưu vào SQLite. yêu cầu bạn **"mở cửa" cho người khác truy vấn dữ liệu đó** thông qua một Web Service (giao diện lập trình ứng dụng - API).

Đây chính là nền tảng của mọi ứng dụng hiện đại:
- **Mobile app** gọi API để lấy danh sách phim → hiển thị lên màn hình điện thoại
- **Website** gọi API để lấy thông tin phim → render ra trang web
- **Hệ thống khác** gọi API để tích hợp dữ liệu

```
[Crawler] → movies.db → [Web Service] → JSON → [Client/Browser]
```

Ngoài ra, bài này còn kiểm tra kỹ năng **Debug nâng cao** với Conditional Breakpoint — một kỹ năng cực kỳ quan trọng khi làm việc với dữ liệu lớn.

---

## PHẦN A — Chuẩn bị

### A1. Copy database từ

Web Service cần đọc dữ liệu từ file `movies.db` đã crawl ở. Mở Terminal trong IntelliJ (**Alt+F12**):

```bash
cp ~/workspace/movie-webservice/crawl-data/movies.db ~/workspace/movie-webservice/basic-webservice/
```

### A2. Mở project trong IntelliJ

Nếu bạn đang mở thư mục gốc `movie-webservice` trong IntelliJ, thì IntelliJ sẽ tự nhận diện file `pom.xml` mới trong `basic-webservice`. 

Nếu không, bạn click phải vào file `basic-webservice/pom.xml` → **Add as Maven Project**.

Đợi IntelliJ import Maven xong (thanh progress góc dưới phải).

---

## PHẦN B — Chạy Web Service

### B1. Chạy trong IntelliJ

1. Mở file `basic-webservice/src/main/java/com/example/movieservice/Main.java`
2. Click nút ** Run** xanh bên cạnh `public static void main`
3. Xem tab **Run** ở dưới cùng, bạn sẽ thấy:

```
=== KHỞI ĐỘNG MOVIE WEB SERVICE ===
Database: movies.db
Port: 8080
Database có 100 phim.
=== SERVER ĐANG CHẠY TẠI http://localhost:8080 ===
Thử truy cập:
  http://localhost:8080/movies        → Danh sách tất cả phim
  http://localhost:8080/movie?id=1     → Phim có ID = 1
```

### B2. Test bằng trình duyệt

Mở trình duyệt (Chrome/Firefox) và truy cập các URL sau:

**Xem tất cả phim:**
```
http://localhost:8080/movies
```

**Xem phim theo ID:**
```
http://localhost:8080/movie?id=1
```

**Xem phim theo URL đã crawl:**
```
http://localhost:8080/movie?url=https://toivote.com/movie/xxx
```
*(Thay URL bằng một URL thực trong database của bạn)*

### B3. Kết quả JSON format đẹp

Kết quả trả về sẽ là JSON đã được format đẹp (indent 4 spaces):

```json
{
    "id": 1,
    "title": "Phép Thuật",
    "releaseYear": "2007",
    "country": "Hoa Kỳ",
    "genres": [
        "Nhạc kịch",
        "Viễn tưởng",
        "Hài hước"
    ],
    "directors": [
        "Kevin Lima"
    ],
    "actors": [
        "Amy Adams",
        "Patrick Dempsey"
    ],
    "sourceUrl": "https://toivote.com/movie/..."
}
```

---

## PHẦN C — Debug với Conditional Breakpoint (QUAN TRỌNG)

Đây là phần trọng tâm chấm điểm của. Đề bài yêu cầu:
> *"Sử dụng đặt điều kiện vào breakpoint để dừng chương trình tại diễn viên có tên bắt đầu bằng chữ 'A'. Lưu ý: không thêm các câu lệnh rẽ nhánh vào code để thực hiện debug."*

Nghĩa là: **KHÔNG ĐƯỢC** thêm `if (actorName.startsWith("A"))` vào code. Thay vào đó, dùng tính năng Conditional Breakpoint của IntelliJ.

### C1. Dừng server (nếu đang chạy)

Nhấn nút ** Stop** (hình vuông đỏ) trong tab Run để dừng server hiện tại.

### C2. Mở file cần đặt breakpoint

Mở file `MovieHandler.java` và tìm đến dòng code sau (trong method `handleGetAllMovies`):

```java
logger.debug("Processing actor: {} (Movie: {})", actorName, movie.getTitle());
```

### C3. Đặt Conditional Breakpoint

1. **Click vào cột số dòng** (bên trái dòng `logger.debug(...)`) để đặt một breakpoint bình thường (xuất hiện chấm tròn đỏ ).

2. **Click chuột PHẢI** vào chấm tròn đỏ đó. Một popup sẽ hiện ra.

3. Trong ô **Condition**, gõ điều kiện sau:

```
actorName.startsWith("A")
```

4. Nhấn **Done** (hoặc Enter).

Lúc này chấm đỏ sẽ có thêm dấu `?` bên trong, cho biết đây là **Conditional Breakpoint**.

### C4. Chạy ở chế độ Debug

1. Nhấn nút ** Debug** (hình con bọ) hoặc nhấn **Shift + F9**.
2. Server sẽ khởi động ở chế độ Debug.
3. Trên trình duyệt, truy cập: `http://localhost:8080/movies`

### C5. Quan sát kết quả

Chương trình sẽ **tự động dừng lại** khi gặp diễn viên có tên bắt đầu bằng "A" (ví dụ: "Amy Adams").

IntelliJ sẽ:
- Highlight dòng code đang dừng (màu xanh dương)
- Hiển thị tab **Debugger** phía dưới với các biến:
  - `actorName` = "Amy Adams" (hoặc tên diễn viên bắt đầu bằng A)
  - `movie` = đối tượng Movie chứa phim đó
- Bạn có thể **xem giá trị tất cả biến** trong tab Variables

### C6. Tiếp tục chạy

- Nhấn **F9** (Resume Program) để tiếp tục → sẽ dừng lại ở diễn viên tiếp theo bắt đầu bằng "A"
- Nhấn **F8** (Step Over) để chạy từng dòng một
- Nhấn ** Stop** để dừng hoàn toàn

---

## Tóm tắt — dạy bạn điều gì?

| Kỹ năng | Ý nghĩa |
|---------|---------|
| **HTTP Server** | Hiểu cách Web Service hoạt động (nhận request → xử lý → trả response) |
| **REST API** | Thiết kế endpoint URL có ý nghĩa (`/movies`, `/movie?id=1`) |
| **JSON formatting** | Trả dữ liệu dạng JSON đẹp — chuẩn giao tiếp của mọi ứng dụng hiện đại |
| **Conditional Breakpoint** | Debug thông minh trong dữ liệu lớn — không cần sửa code |
| **Đọc DB từ bài khác** | Tái sử dụng dữ liệu giữa các module — tư duy hệ thống |
