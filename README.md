# PlaceX 🎓

> **A full-stack smart campus placement platform** that digitizes and streamlines the entire recruitment lifecycle between students, recruiters, and administrators.

---

## Overview

PlaceX transforms traditional, fragmented placement workflows into a **structured, real-time, and fully trackable pipeline**:

```
Apply → Shortlist → Interview → Select → Offer → Placement Lock
```

The platform is a **multi-role system** serving three distinct actors — Students, Recruiters, and Admins — each with a dedicated interface and purpose-built functionality, forming a complete end-to-end placement ecosystem within a single application.

---

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [System Architecture](#system-architecture)
- [Database Schema](#database-schema)
- [Application Status Workflow](#application-status-workflow)
- [Key Design Decisions](#key-design-decisions)
- [Getting Started](#getting-started)
- [API Overview](#api-overview)
- [Roadmap](#roadmap)

---

## Features

### 👨‍🎓 Student Portal
- **Dashboard** — At-a-glance metrics: total applications, interview calls, selected offers, and CGPA
- **Job Discovery & Apply** — Browse live opportunities and apply directly through the app
- **Real-Time Application Tracking** — Live status updates across all stages: `Applied`, `Shortlisted`, `Interview Scheduled`, `Selected`, `Rejected`, `Offer Accepted`
- **Application Timeline View** — Visual progression of each application including interview date, time, and mode
- **Notification Center** — Push-style alerts with unread indicators for shortlisting, interview scheduling, and selections
- **Profile & Resume Management** — Structured interface for maintaining academic and professional details
- **Intelligent Feedback** — Skill improvement suggestions derived from rejection pattern analysis

### 🏢 Recruiter Portal
- **Job Posting** — Create opportunities with role, CTC, required skills, job type, and application deadlines
- **Smart Shortlist Engine** — Multi-factor candidate ranking using skill-match percentage and CGPA; eliminates manual filtering
- **Application Management** — One-click actions: shortlist, schedule interview, select, or reject
- **Automated Interview Slot Generation** — Generates time slots from a defined range and auto-assigns them to shortlisted candidates
- **Real-Time Sync** — All recruiter actions are immediately reflected on the student side

### 🛡️ Admin Panel
- **Company Verification** — Approve or reject recruiter registrations; only verified companies access the platform
- **Platform Analytics** — System-wide insights: total applications, active postings, placement performance
- **Transparency & Oversight** — Full audit trail of placement activity

---

## Tech Stack

| Layer | Technology |
|---|---|
| **Mobile Frontend** | Android (Kotlin + XML Layouts) |
| **Backend** | PHP (REST API) |
| **Database** | MySQL |

---

## System Architecture

```
┌─────────────────────────────────────────────────────┐
│                  Android Client (Kotlin)             │
│      Student UI │ Recruiter UI │ Admin UI            │
└──────────────────────┬──────────────────────────────┘
                       │ HTTPS / REST
┌──────────────────────▼──────────────────────────────┐
│                  PHP REST API Layer                  │
│   Auth │ Jobs │ Applications │ Interviews │ Notifs   │
└──────────────────────┬──────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────┐
│                   MySQL Database                     │
│  Student │ Company │ Job │ Application │ History     │
│  Notification │ ApplicationHistory                  │
└─────────────────────────────────────────────────────┘
```

---

## Database Schema

### Core Entities

```
Student
├── student_id (PK)
├── name, email, password
├── cgpa, skills
├── resume_url
└── is_placed (placement lock flag)

Company
├── company_id (PK)
├── name, email, password
└── is_approved (admin-controlled)

Job
├── job_id (PK)
├── company_id (FK → Company)
├── role, package, skills_required
├── job_type, deadline
└── is_active

Application
├── application_id (PK)
├── student_id (FK → Student)
├── job_id (FK → Job)
├── current_status
└── applied_at

ApplicationHistory          ← Timeline log
├── history_id (PK)
├── application_id (FK → Application)
├── status
├── interview_date, interview_time, interview_mode
└── changed_at

Notification
├── notification_id (PK)
├── student_id (FK → Student)
├── message, is_read
└── created_at
```

---

## Application Status Workflow

```
              ┌─────────┐
              │ APPLIED  │
              └────┬─────┘
                   │
          ┌────────▼────────┐
          │   SHORTLISTED   │
          └────────┬────────┘
                   │
     ┌─────────────▼────────────────┐
     │   INTERVIEW SCHEDULED        │
     └─────────────┬────────────────┘
                   │
        ┌──────────┴──────────┐
        ▼                     ▼
   ┌─────────┐          ┌──────────┐
   │SELECTED │          │ REJECTED │
   └────┬────┘          └──────────┘
        │
        ▼
  ┌─────────────┐
  │OFFER ACCEPTED│
  └──────┬──────┘
         │
         ▼
  ┌──────────────┐
  │PLACEMENT LOCK│  ← student.is_placed = true
  └──────────────┘
```

---

## Key Design Decisions

**Placement Lock** — Once a student accepts an offer, `is_placed` is set to `true` and further job applications are blocked. This enforces fairness and mirrors real-world placement policy.

**Duplicate Application Prevention** — A unique constraint on `(student_id, job_id)` in the Application table ensures a student can apply to a given job exactly once.

**ApplicationHistory as Audit Log** — Rather than overwriting application status, every transition is appended as a new row in `ApplicationHistory`. This enables a full timeline view and is the foundation for future analytics.

**Smart Shortlist Ranking** — Candidates are ranked by a composite score: weighted skill-match percentage + CGPA. This surfaces the strongest candidates without requiring manual review of every profile.

**Automated Interview Slot Generation** — Given a time window from the recruiter, the system generates and assigns individual slots to shortlisted candidates, eliminating scheduling back-and-forth.

**Real-Time Consistency** — Recruiter actions immediately update the `Application` table and trigger `Notification` inserts, ensuring students always see the latest state.

---

## Getting Started

### Prerequisites

- Android Studio (Hedgehog or later)
- PHP 8.x + Apache / Nginx
- MySQL 8.x

### Backend Setup

```bash
# Clone the repository
git clone https://github.com/your-username/placex.git
cd placex/backend

# Import the database schema
mysql -u root -p < database/placex_schema.sql

# Configure environment
cp config/db.example.php config/db.php
# Edit config/db.php with your DB credentials

# Start your local server (example with PHP built-in server)
php -S localhost:8000
```

### Android Setup

```
1. Open the `android/` folder in Android Studio
2. Update BASE_URL in app/src/main/java/com/placex/network/ApiClient.kt
   to point to your backend (e.g., http://10.0.2.2:8000/api/)
3. Build & run on emulator or physical device (API 26+)
```

---

## API Overview

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/auth/student/login` | Student login |
| `POST` | `/auth/recruiter/login` | Recruiter login |
| `GET` | `/jobs` | Fetch active job listings |
| `POST` | `/jobs` | Create a new job posting |
| `POST` | `/applications` | Submit a job application |
| `GET` | `/applications/:id/timeline` | Fetch full application history |
| `PATCH` | `/applications/:id/status` | Update application status |
| `POST` | `/interviews/schedule` | Auto-generate and assign interview slots |
| `GET` | `/notifications/:student_id` | Fetch student notifications |
| `PATCH` | `/admin/companies/:id/approve` | Approve company registration |

---

## Roadmap

- [ ] AI-based candidate analysis and job-fit scoring
- [ ] Advanced placement analytics dashboard (charts, cohort trends)
- [ ] Email/SMS notification integration
- [ ] Cloud deployment (AWS / GCP)
- [ ] Resume parsing and auto-profile population
- [ ] Recruiter-side feedback submission to students
- [ ] Multi-round interview support
- [ ] Web portal (React or Next.js)

---

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.

---

<p align="center">Built with purpose — making campus placements smarter, fairer, and faster.</p>
