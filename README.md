# InternHub Backend

Spring Boot REST API for managing internship applications and connecting students with companies.
Frontend repo: [https://github.com/youssefoubrik/InternHub-Front-End](https://github.com/youssefoubrik/InternHub-Front-End)

## 🚀 Features

- 🔐 **Authentication**: JWT-based, email verification, password reset
- 👥 **User Roles**: Students, Companies, Admins
- 📄 **Internship Management**: Post, search, apply, and track status
- 📬 **Email Notifications**: Verification, welcome, password-reset, contact, and application emails; Resend API delivery with SMTP fallback
- 📁 **File Upload**: CV/document support
- 🗄️ **Supabase Storage**: Authenticated backend uploads with public file URLs saved alongside profiles and records
- 🔍 **Search & Filter**: Advanced query and pagination
- 🛠️ **Admin Tools:** Dashboard with platform stats, user moderation, and flag verification
- 🚩 **User Reports**: Students and companies can report companies; companies can report students
- 📖 **Swagger Docs**: Auto-generated API docs

## 🛠 Tech Stack

- **Java 17**, **Spring Boot 3.4.4**, **Spring Security**, **PostgreSQL**
- **JWT**, **BCrypt**, **JJWT**, **Lombok**, **MapStruct**
- **SpringDoc OpenAPI**, **Thymeleaf**, **Spring Mail**

## ⚙️ Setup

### Prerequisites

- **Java 17+**
- **Docker & Docker Compose**
- **Maven** (or use included wrapper)

### Quick Start

1. **Clone the repository:**

```bash
git clone <repo>
cd InternHub_backend
```

2. **Create environment file:**

```bash
cp .env.example .env
```

3. **Configure environment variables in `.env`:**

```env
# Database
DATABASE_PASSWORD=your-secure-password

# JWT
JWT_SECRET=your-256-bit-secret-here

# Email Configuration
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
RESEND_API_KEY=re_your_api_key
MAIL_FROM=InternHub <no-reply@yourdomain.com>

# Frontend URL
FRONTEND_URL=http://localhost:3000

# Application host
PORT=8080
SPRING_ACTIVE_PROFILE=dev
```

4. **Start database services:**

```bash
docker-compose up -d
```

5. **Run the application:**

```bash
./mvnw spring-boot:run
```

6. **Access services:**
   - Backend API: `http://localhost:8080`
   - Database Admin (Adminer): `http://localhost:8888`
   - Swagger UI: `http://localhost:8080/swagger-ui.html`

### Environment Variables

| Variable                | Description                  | Default                                               |
| ----------------------- | ---------------------------- | ----------------------------------------------------- |
| `DATABASE_URL`          | PostgreSQL connection URL    | `jdbc:postgresql://localhost:5432/postgres`           |
| `DATABASE_USERNAME`     | Database username            | `postgres`                                            |
| `DATABASE_PASSWORD`     | Database password            | `changemeinprod!`                                     |
| `JPA_DDL_AUTO`          | Hibernate schema behavior    | `update`                                              |
| `JWT_SECRET`            | JWT signing secret (256-bit) | Required for production                               |
| `MAIL_HOST`             | SMTP server host             | `smtp.gmail.com`                                      |
| `MAIL_PORT`             | SMTP server port             | `587`                                                 |
| `MAIL_USERNAME`         | Email username               | Required                                              |
| `MAIL_PASSWORD`         | Email app password           | Required                                              |
| `RESEND_API_KEY`        | Resend API key               | Optional; Resend is used when set, with SMTP fallback |
| `MAIL_FROM`             | Sender address/name          | Falls back to `MAIL_USERNAME`                         |
| `FRONTEND_URL`          | Frontend application URL     | `http://localhost:3000`                               |
| `SUPABASE_URL`          | Supabase project URL         | Required for storage                                  |
| `SUPABASE_KEY`          | Supabase secret API key      | Required for storage; server only                     |
| `PORT`                  | HTTP port supplied by host   | `8080`                                                |
| `SPRING_ACTIVE_PROFILE` | Spring profile               | `dev`                                                 |

### Database Access via Adminer

- URL: `http://localhost:8888`
- System: PostgreSQL
- Server: `db`
- Username: `postgres`
- Password: Use your `DATABASE_PASSWORD` from `.env`

## 🔧 Configuration

### Email Setup (Gmail)

1. Enable 2-Factor Authentication on your Gmail account
2. Generate an App Password: Google Account → Security → App passwords
3. Use the app password as `MAIL_PASSWORD` in your `.env` file

### Email Setup (Resend)

Set `RESEND_API_KEY` and `MAIL_FROM` in the backend environment. When a Resend key is configured, the backend sends email through the Resend API first; if that request fails, it attempts SMTP using `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, and `MAIL_PASSWORD`. Without a Resend key, SMTP is used directly. Configure a sender address verified with Resend for production. `MAIL_FROM` defaults to `MAIL_USERNAME`.

The contact form sends to the configured sender address and sets the submitter's email as the reply-to address. Account verification, welcome, password-reset, and application notification emails use the configured mail transport.

For deployment platforms that provide the listening port, set `PORT` to the platform's value. `SPRING_ACTIVE_PROFILE` selects the Spring profile and defaults to `dev`.

### Company reporting

Authenticated students and companies can submit reports against a company at `POST /api/v1/flagged-companies`. Companies can also report students at `POST /api/v1/flagged-students`. Company reports record the reporting account (student or company); a company cannot report itself. Report review, warning, history, overview, and count endpoints are available under `/api/v1/flagged-companies` and `/api/v1/flagged-students` for authenticated moderation workflows.

### Supabase Storage

Set `SUPABASE_URL` and the new `sb_secret_...` API key in the backend `.env` (or the backend host's secret environment settings). Never add the secret key to frontend variables or commit it. The application uploads to the `images` bucket under a per-user folder and returns a public object URL. Keep the bucket public if those URLs should load without authentication. Storage uploads support JPEG, PNG, WebP, GIF, and PDF files up to 10 MB.

The frontend calls `POST /api/v1/storage/images` with a multipart `file` field and its InternHub bearer token. The response is `{ "publicUrl": "..." }`. To delete an uploaded file, it calls `DELETE /api/v1/storage/images?publicUrl=...`; the backend only deletes URLs inside the caller's own folder. Older files uploaded directly by the former frontend flow are left in storage because their object paths do not include the new per-user ownership folder.

### JWT Secret Generation

Generate a secure 256-bit secret:

```bash
# Using OpenSSL
openssl rand -base64 32

# Using Node.js
node -e "console.log(require('crypto').randomBytes(32).toString('base64'))"
```

## 🌐 API Documentation

- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Ensure all tests pass
5. Submit a pull request

## 📝 License

MIT License - see `LICENSE` file for details
