# HemoLink – Blood Donor Finder and Emergency Request System (BDFERS)

A web-based platform designed to connect people in need of blood with potential voluntary blood donors through organized donor information, rule-based matching, and emergency request management.

> **Academic Project:** Software Quality Engineering (SQE) — Sukkur IBA University, Fall 2026.

---

## 1. Project Overview

Finding suitable blood donors during medical emergencies can be difficult. People often depend on personal contacts, WhatsApp groups, and social media platforms, which can cause delays, make donor availability difficult to verify, and expose personal contact information.

**HemoLink** aims to address these challenges by providing a centralized platform where voluntary donors can register, manage their profiles, and update their availability. Requesters can submit emergency blood requests, while the system identifies potential donors using predefined blood-group compatibility rules, location, availability, and configured eligibility criteria.

The system also provides request-status tracking, administrative management, and controlled disclosure of donor contact information through an approval and consent workflow.

HemoLink identifies potential donors but does not replace medical professionals or clinical procedures for confirming donor suitability and blood compatibility.

## 2. Project Objectives

- Provide a centralized platform for donor registration and profile management.
- Facilitate the creation and tracking of emergency blood requests.
- Identify potential donors using predefined matching and eligibility rules.
- Protect donor information through role-based access and controlled disclosure.
- Apply Software Quality Engineering practices throughout the software development lifecycle.
- Evaluate software quality through systematic testing, defect management, and measurable quality metrics.

## 3. Key Features

| Feature | Description |
|---|---|
| User Account Management | Registration, login, logout, and role-based access. |
| Donor Management | Manage donor profiles, blood groups, eligibility information, and availability. |
| Emergency Requests | Create and manage emergency blood requests. |
| Donor Matching | Identify potential donors using blood-group compatibility, location, and availability. |
| Request Tracking | Track request history, donor responses, and request statuses. |
| Administration | Review requests and manage invalid, inactive, or reported donor profiles. |
| Privacy and Validation | Protect donor contact information and enforce input validation and backend authorization. |

## 4. Target Users

- **Donors:** Individuals willing to register as potential blood donors.
- **Requesters:** Individuals seeking potential blood donors during emergencies.
- **Administrators:** Authorized users responsible for request review, donor management, and privacy enforcement.

A registered user may act as both a donor and a requester.

## 5. Technology Stack

| Component | Technology |
|---|---|
| Backend | Java, Spring Boot |
| Frontend | HTML, CSS, JavaScript, Thymeleaf |
| Database | MySQL |
| Database Connectivity | Spring Data JPA, Hibernate |
| Build Management | Maven |
| Unit Testing | JUnit 5, Mockito |
| Integration and Web Testing | Spring Boot Test, MockMvc |
| API Testing | Postman (if required) |
| Version Control | Git and GitHub |

## 6. Software Quality Engineering

This project emphasizes software quality throughout the Software Development Life Cycle (SDLC), rather than focusing only on application development.

The following activities will be incorporated:

- Requirements analysis and specification.
- Software architecture and design reviews.
- Coding standards and code reviews.
- Version control and collaborative development.
- Test planning and test-case design.
- Unit, integration, system, and user acceptance testing, where applicable.
- Defect identification, classification, tracking, and retesting.
- Quality measurement and evaluation using actual project data.

## 7. Project Scope

### In Scope

- User registration, authentication, and role-based authorization.
- Donor profile and availability management.
- Emergency blood request creation and management.
- Rule-based donor matching and search.
- Request-status tracking and donor responses.
- Administrative review and donor profile management.
- Input validation, error handling, and data integrity.
- Controlled disclosure of donor contact information.

### Out of Scope

- Online payments and payment processing.
- Real-time chat and video calling.
- Email, SMS, and push notifications.
- GPS tracking, live maps, and route calculation.
- External hospital and blood-bank integration.
- Native mobile applications.
- Medical advice and automated clinical decisions.
- AI-based donor recommendations.

Only dummy or sample personal data will be used during development and testing.

## 8. Repository Structure

The repository is organized to maintain application source code, project documentation, testing resources, and Software Quality Engineering evidence.

```text
hemolink-bdfers/
│
├── .github/                 # GitHub templates and workflows
│
├── docs/                    # Project documentation and deliverables
│   ├── proposal/
│   ├── requirements/
│   ├── design/
│   ├── quality-assurance/
│   ├── testing/
│   ├── metrics/
│   └── reports/
│
├── src/                     # Application source code and tests
│
├── database/                # Database scripts and sample data
│
├── README.md
├── CONTRIBUTING.md
├── .gitignore
└── pom.xml
```

The structure will evolve as the project progresses.

## 9. Team Members

| Name | Role |
|---|---|
| Talha Irfan | Team Lead |
| Ghulam Mustafa | Team Member |
| Sumera Naz | Team Member |

All team members will participate in requirements analysis, design, implementation, testing, documentation, and quality reviews.

## 10. Development and Collaboration

Git and GitHub will be used to manage source code and coordinate collaborative development.

The repository will maintain:

- Feature branches and meaningful commits.
- Pull requests and code reviews.
- GitHub Issues for task and defect tracking.
- Documentation of project decisions and development activities.
- Individual contribution history.

Refer to [CONTRIBUTING.md](CONTRIBUTING.md) for the team's development workflow and contribution guidelines.

## 11. Project Deliverables

The project will follow the deliverables defined by the Software Quality Engineering course:

1. Project Proposal
2. Software Requirements Specification (SRS)
3. Software Design and GitHub Repository
4. Software Quality Assurance Plan
5. Test Plan and Test Cases
6. Test Execution and Defect Report
7. Software Quality Metrics Report
8. Final Software Product and Report
9. Final Demonstration and Viva

## 12. Project Status

**Current Phase:** Initial Project Setup and Requirements Planning

This repository will be updated throughout the project lifecycle as documentation, application components, testing activities, and quality evaluation progress.

## 13. License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

---

**Developed as part of the Software Quality Engineering course**

Department of Computer Science  
Sukkur Institute of Business Administration University  
Fall 2026
