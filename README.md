<div align="center">

<h1>PlaceX</h1>

<p><strong>Smart Campus Placement Platform</strong></p>

<p>A full-stack Android application that digitizes and streamlines the entire recruitment lifecycle between students, recruiters, and administrators.</p>

![Android](https://img.shields.io/badge/Android-Kotlin-3DDC84?style=flat-square&logo=android&logoColor=white)
![Backend](https://img.shields.io/badge/Backend-PHP-777BB4?style=flat-square&logo=php&logoColor=white)
![Database](https://img.shields.io/badge/Database-MySQL-4479A1?style=flat-square&logo=mysql&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-blue?style=flat-square)
![Status](https://img.shields.io/badge/Status-Active-success?style=flat-square)

<br/>

```
Apply  →  Shortlist  →  Interview  →  Select  →  Offer  →  Placement Lock
```

</div>

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [System Architecture](#system-architecture)
- [Database Schema](#database-schema)
- [Status Workflow](#status-workflow)
- [Key Design Decisions](#key-design-decisions)
- [Getting Started](#getting-started)
- [API Reference](#api-reference)
- [Roadmap](#roadmap)
- [License](#license)

---

## Overview

PlaceX transforms traditional, fragmented placement workflows into a **structured, real-time, and fully trackable pipeline**. It is a multi-role system serving three distinct actors — **Students**, **Recruiters**, and **Admins** — each with a dedicated interface and purpose-built functionality, forming a complete end-to-end placement ecosystem within a single application.

---

## Features

### Student Portal
| Feature | Description |
|---|---|
| Live Dashboard | At-a-glance metrics: total applications, interview calls, selected offers, and CGPA |
| Application Tracking | Real-time status: Applied → Shortlisted → Interview Scheduled → Selected → Rejected → Offer Accepted |
| Timeline View | Visual progression of each application with interview date, time, and mode |
| Notification Center | Push-style alerts with unread indicators for shortlisting and interview scheduling |
| Profile & Resume | Structured interface for managing academic details and uploading resumes |
| Smart Feedback | Skill improvement suggestions derived from rejection pattern analysis |

### Recruiter Portal
| Feature | Description |
|---|---|
| Job Posting | Create opportunities with role, CTC, required skills, job type, and deadlines |
| Smart Shortlist Engine | Multi-factor candidate ranking by skill-match % and CGPA — no manual filtering |
| Interview Scheduling | Auto-generates time slots from a defined range and assigns them to shortlisted candidates |
| Application Actions | One-click shortlist, schedule, select, or reject — all reflected in real time |
| Real-Time Sync | Every recruiter action immediately updates the student-facing view |

### Admin Panel
| Feature | Description |
|---|---|
| Company Verification | Approve or reject recruiter registrations — only verified companies can post jobs |
| Platform Analytics | System-wide view: total applications, active postings, placement performance |
| Audit Trail | Full transparency across the placement lifecycle for every actor |

---

## Tech Stack

| Layer | Technology | Details |
|---|---|---|
| Mobile Frontend | Android · Kotlin | XML layouts, multi-role UI |
| Backend | PHP | Stateless REST API |
| Database | MySQL | Relational, normalized schema |

---

## System Architecture

```
┌──────────────────────────────────────────────────────┐
│               Android Client (Kotlin)                │
│     Student UI  │  Recruiter UI  │  Admin UI         │
└─────────────────────┬────────────────────────────────┘
                      │  HTTPS / REST (JSON)
┌─────────────────────▼────────────────────────────────┐
│                 PHP REST API Layer                   │
│   Auth  │  Jobs  │  Applications  │  Interviews      │
│              Notifications  │  Admin                 │
└─────────────────────┬────────────────────────────────┘
                      │
┌─────────────────────▼────────────────────────────────┐
│                  MySQL Database                      │
│  Student │ Company │ Job │ Application │ AppHistory  │
│                   Notification                       │
└──────────────────────────────────────────────────────┘
```

---

## Database Schema

```
Student
├── student_id      PK
├── name, email, password
├── cgpa, skills
├── resume_url
└── is_placed       ← placement lock flag

Company
├── company_id      PK
├── name, email, password
└── is_approved     ← admin-controlled

Job
├── job_id          PK
├── company_id      FK → Company
├── role, package, skills_required
├── job_type, deadline
└── is_active

Application
├── application_id  PK
├── student_id      FK → Student
├── job_id          FK → Job    (UNIQUE on student_id + job_id)
├── current_status
└── applied_at

ApplicationHistory              ← timeline / audit log
├── history_id      PK
├── application_id  FK → Application
├── status
├── interview_date, interview_time, interview_mode
└── changed_at

Notification
├── notification_id PK
├── student_id      FK → Student
├── message, is_read
└── created_at
```

---

## Status Workflow

```
         ┌──────────┐
         │  APPLIED  │
         └─────┬─────┘
               │
      ┌─────────▼──────────┐
      │    SHORTLISTED      │
      └─────────┬───────────┘
                │
   ┌────────────▼────────────────┐
   │   INTERVIEW SCHEDULED       │
   └────────────┬────────────────┘
                │
       ┌────────┴────────┐
       ▼                 ▼
  ┌──────────┐      ┌──────────┐
  │ SELECTED │      │ REJECTED │
  └─────┬────┘      └──────────┘
        │
        ▼
  ┌───────────────┐
  │ OFFER ACCEPTED│
  └───────┬───────┘
          │
          ▼
  ┌────────────────────────────────┐
  │  PLACEMENT LOCK                │
  │  student.is_placed = true      │  ← no further applications allowed
  └────────────────────────────────┘
```

---

## Key Design Decisions

**Placement Lock**
Once a student accepts an offer, `is_placed` is set to `true` and further applications are blocked. This enforces fairness and mirrors real-world campus placement policy.

**History as Audit Log**
Rather than overwriting `current_status`, every transition appends a new row to `ApplicationHistory` with a timestamp. This powers the timeline view and is the foundation for future analytics.

**Duplicate Application Prevention**
A unique constraint on `(student_id, job_id)` enforces one application per student per job at the database level — not just in application logic.

**Smart Shortlist Ranking**
Candidates are ranked by a composite score: weighted skill-match percentage + CGPA. This surfaces the strongest candidates without requiring manual review of every profile.

**Automated Interview Slot Generation**
Given a time window from the recruiter, the system generates and assigns individual interview slots to shortlisted candidates, eliminating scheduling coordination entirely.

**Real-Time Consistency**
Recruiter actions immediately update the `Application` table and trigger `Notification` inserts, ensuring students always see the current state without polling.

---

## Getting Started

### Prerequisites

- Android Studio Hedgehog or later
- PHP 8.x + Apache / Nginx
- MySQL 8.x

### Backend Setup

```bash
# Clone the repository
git clone https://github.com/your-username/placex.git
cd placex/backend

# Import the database schema
mysql -u root -p < database/placex_schema.sql

# Configure your database connection
cp config/db.example.php config/db.php
# Edit config/db.php with your credentials

# Start the development server
php -S localhost:8000
```

### Android Setup

```
1. Open the android/ folder in Android Studio

2. Update BASE_URL in:
   app/src/main/java/com/placex/network/ApiClient.kt
   → set it to your backend URL (e.g. http://10.0.2.2:8000/api/)

3. Build and run on an emulator or physical device (API 26+)
```

---

## API Reference

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/auth/student/login` | Student authentication |
| `POST` | `/auth/recruiter/login` | Recruiter authentication |
| `GET` | `/jobs` | Fetch all active job listings |
| `POST` | `/jobs` | Create a new job posting |
| `POST` | `/applications` | Submit a job application |
| `GET` | `/applications/:id/timeline` | Fetch full application history |
| `PATCH` | `/applications/:id/status` | Update application status |
| `POST` | `/interviews/schedule` | Auto-generate and assign interview slots |
| `GET` | `/notifications/:student_id` | Fetch student notifications |
| `PATCH` | `/admin/companies/:id/approve` | Approve a company registration |

---

## Roadmap

- [x] Multi-role auth — Student, Recruiter, Admin
- [x] End-to-end placement pipeline with real-time status tracking
- [x] Smart shortlist engine
- [x] Automated interview slot generation
- [x] Placement lock mechanism
- [ ] AI-based candidate fit scoring
- [ ] Advanced analytics dashboard (cohorts, trends, placement rates)
- [ ] Email / SMS notification integration
- [ ] Resume parsing and auto-profile population
- [ ] Multi-round interview support
- [ ] Web portal (React / Next.js)
- [ ] Cloud deployment — AWS / GCP

---

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.

---

<div align="center">
  <sub>Built with purpose — making campus placements smarter, fairer, and faster.</sub>
</div>
