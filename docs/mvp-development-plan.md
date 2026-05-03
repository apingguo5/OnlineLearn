# OnlineLearn MVP Development Plan

> Minimal Viable Product for an online learning platform that validates the core learning loop: **Learn → Practice → Assess → Discuss**

---

## 1. Core Positioning

**Goal**: Build a system that validates the basic online learning closed loop, covering the core flow of "Learn - Practice - Assess - Discuss".

---

## 2. Core Modules (6 Modules)

### 2.1 User Module ✅

- **Registration / Login**
  - Email / phone registration and login
  - Basic roles: Student, Teacher
  - Profile (basic info, avatar)

- **Authorization**
  - Student: learn, take quizzes, comment
  - Teacher: course management, question creation, grading

### 2.2 Course Learning Module ⭐

- **Course Management (Teacher)**
  - Create course (title, description, cover image)
  - Chapter management: add / edit chapters
  - Resource upload:
    - Video (MP4, auto-generated player)
    - PDF (online preview)
    - Rich text content (WYSIWYG editor)

- **Learning Interface (Student)**
  - Course table of contents navigation
  - Video player (basic controls: play/pause/progress bar)
  - Auto-save learning progress
  - "Mark as completed" feature

### 2.3 Assessment Module ⭐

- **Question Bank (Teacher)**
  - Question types: single choice, multiple choice, true/false, short answer
  - Question categorization: by chapter / knowledge point
  - Batch import (Excel template)

- **Quiz / Assignment**
  - Create quiz: select questions, set time / score
  - Student: online answering interface
  - Auto-grading: objective questions auto-scored
  - Score review: students view scores and answer explanations

### 2.4 Comment & Q&A Module

- **Course Q&A Area**
  - Q&A section under each course chapter
  - Students ask questions, teachers / peers answer
  - Text + image reply support
  - Best answer marker (teacher permission)

- **Learning Notes / Comments**
  - Timestamp-based comments on video timeline
  - Public note sharing
  - Like / reply interaction

### 2.5 Learning Progress Dashboard

- **Personal Dashboard**
  - Completed courses / total courses
  - Recent learning activity
  - Quiz score trends
  - Pending assignment reminders

### 2.6 Notification System (Basic)

- **System Notifications**
  - Assignment published reminder
  - Quiz deadline reminder
  - Q&A reply notification
  - Course update notification

---

## 3. Technical Architecture (Minimum Viable)

### Frontend

| Technology | Purpose |
|------------|---------|
| Vue 3 + TypeScript | Framework |
| Element Plus / Ant Design Vue | UI Library |
| Pinia | State Management |
| Vue Router | Routing |
| Axios | HTTP Client |
| video.js | Video Player |
| PDF.js | Document Preview |
| Nginx + Docker | Deployment |

### Backend

| Technology | Purpose |
|------------|---------|
| Node.js (NestJS) or Go (Gin) | Application Framework |
| MySQL 8.0 | Relational Database |
| Redis | Cache & Session Management |
| Local Storage / OSS / COS | File Storage |
| RabbitMQ | Async Message Queue |
| Swagger | API Documentation |

### Deployment Architecture

```
┌─────────────────────────────────────┐
│         Nginx (Static + Proxy)       │
├─────────────────────────────────────┤
│         Node.js / Go App             │
├─────────────────────────────────────┤
│  MySQL 8.0  │  Redis  │  File Store  │
└─────────────────────────────────────┘
```

Simple monolith + essential middleware (MVP stage).

---

## 4. Core Database Tables (~15 Tables)

### User
| Table | Description |
|-------|-------------|
| `users` | User accounts |
| `user_roles` | User role assignments |

### Course
| Table | Description |
|-------|-------------|
| `courses` | Course metadata |
| `chapters` | Chapter structure |
| `learning_resources` | Video / document resources |
| `user_learning_progress` | Learning progress tracking |

### Assessment
| Table | Description |
|-------|-------------|
| `questions` | Question bank |
| `quizzes` | Quiz metadata |
| `quiz_questions` | Quiz-question mapping |
| `user_quiz_attempts` | Quiz attempt records |
| `user_answers` | Individual answer records |

### Interaction
| Table | Description |
|-------|-------------|
| `comments` | Comments & Q&A |
| `replies` | Replies to comments |
| `likes` | Like records |

### System
| Table | Description |
|-------|-------------|
| `notifications` | System notifications |

---

## 5. Core API Design

### User
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register |
| POST | `/api/auth/login` | Login |
| GET | `/api/users/profile` | Get profile |

### Course
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/courses` | Course list |
| GET | `/api/courses/:id` | Course detail |
| GET | `/api/courses/:id/chapters` | Chapter list |
| POST | `/api/courses` | Create course (teacher) |

### Learning
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/learning/progress` | Update learning progress |
| GET | `/api/learning/progress` | Get learning progress |

### Assessment
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/quizzes` | Quiz list |
| POST | `/api/quizzes/:id/attempt` | Start quiz |
| POST | `/api/quizzes/:id/submit` | Submit answers |
| GET | `/api/quizzes/:id/results` | View results |

### Comment
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/courses/:id/comments` | Get comments |
| POST | `/api/courses/:id/comments` | Post comment |
| POST | `/api/comments/:id/replies` | Reply to comment |

---

## 6. Development Priority

### Phase 1 (Weeks 1-3): Foundation + Core Learning

| Task | Description |
|------|-------------|
| User system | Registration / Login / Profile |
| Course management | CRUD courses, chapters |
| Video & PDF playback | video.js + PDF.js integration |
| Learning progress | Auto-save, mark as completed |

### Phase 2 (Weeks 3-5): Assessment

| Task | Description |
|------|-------------|
| Question bank management | CRUD questions, Excel import |
| Online quiz | Quiz creation, answering interface |
| Auto-grading | Objective question auto-scoring |
| Score review | View scores and answer analysis |

### Phase 3 (Weeks 5-7): Interaction

| Task | Description |
|------|-------------|
| Course Q&A | Ask / answer under chapters |
| Comment system | Video timestamp comments, likes, replies |
| Notifications | Assignment, quiz deadline, reply alerts |
| Dashboard | Learning stats, score trends, reminders |

---

## 7. Deferred Features (Post-MVP)

1. Live streaming / real-time interaction
2. Social features (groups, following)
3. Mobile native app (iOS / Android)
4. Points / level / gamification system
5. Advanced analytics & data visualization
6. Multi-tenant / multi-organization
7. Advanced search & personalized recommendations
8. Certificate generation

---

## 8. Minimum Technical Targets

| Metric | Target |
|--------|--------|
| Concurrent users | 1,000 online simultaneously |
| Video playback | Smooth 720p streaming |
| Page load time | < 2 seconds |
| API response time | < 200 ms |
| Uptime | 99% availability |
| Data security | Password hashing (bcrypt), HTTPS only |

---

## 9. Resource Estimate

| Role | Count |
|------|-------|
| Full-stack developers | 2-3 |
| Total development time | 6-8 weeks |
| Internal testing | 1-2 weeks |

---

## 10. Success Metrics

- User completes full learning loop (register → learn → quiz → review)
- Course creation and content upload works reliably
- Auto-grading accuracy for objective questions
- User retention and return rate
- Feedback from internal testers

---

> **Next Step**: Begin Phase 1 implementation — set up project scaffolding with Vue 3 + TypeScript frontend and NestJS/Go backend, implement user authentication, and build the basic course management CRUD.