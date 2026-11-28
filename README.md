<div align="center">

<br>

# 🩸 HemoLink

### Blood Donor Finder &nbsp;&amp;&nbsp; Emergency Request System

*Connecting people who need blood with voluntary donors: faster, safer, smarter.*

<br>

`☕ Java 21` &nbsp; `🍃 Spring Boot` &nbsp; `🐬 MySQL 8` &nbsp; `🌿 Thymeleaf` &nbsp; `📦 Maven`

`🧪 Software Quality Engineering` &nbsp; `🎓 Sukkur IBA University` &nbsp; `🗓 Fall 2026` &nbsp; `📄 MIT License`

<br>

**[🚀 Quick Start](#-quick-start)** &nbsp;·&nbsp;
**[✨ Features](#-key-features)** &nbsp;·&nbsp;
**[🔄 How It Works](#-how-it-works)** &nbsp;·&nbsp;
**[📁 Structure](#-project-structure)** &nbsp;·&nbsp;
**[👥 Team](#-meet-the-team)**

<br>

</div>

---

<br>

## 🩸 The Problem and Our Answer

<table>
<tr>
<td width="50%" valign="top">

### 😟 The problem

When someone urgently needs blood, families scramble through personal contacts, WhatsApp groups and social media.

<br>

▸ Valuable time is lost<br>
▸ Nobody can tell who is really available<br>
▸ Private phone numbers are posted publicly

</td>
<td width="50%" valign="top">

### 💡 The HemoLink answer

One trusted platform that connects the right people at the right time.

<br>

▸ Donors register and set their availability<br>
▸ Requests are matched by blood group and city<br>
▸ Contact details are shared **only** when a donor agrees

</td>
</tr>
</table>

> [!NOTE]
> HemoLink identifies *potential* donors. It does not replace medical professionals or the clinical procedures used to confirm donor suitability and blood compatibility.

<br>

<div align="center">

<table>
<tr>
<td align="center" width="140"><h2>3</h2><sub><b>USER ROLES</b></sub></td>
<td align="center" width="140"><h2>8</h2><sub><b>BLOOD GROUPS</b></sub></td>
<td align="center" width="140"><h2>2</h2><sub><b>URGENCY LEVELS</b></sub></td>
<td align="center" width="140"><h2>5</h2><sub><b>REQUEST STATUSES</b></sub></td>
</tr>
</table>

</div>

<br>

---

<br>

## ✨ Key Features

> Everything a donor, requester and administrator needs, in one place.

<table>
<tr>
<td width="33%" valign="top">
<h3>🔑 Accounts</h3>
Registration, login, logout and role-based access for donors, requesters and admins.
</td>
<td width="33%" valign="top">
<h3>🩺 Donor Profiles</h3>
Blood group, city, weight, last donation and availability, with advisory eligibility guidance.
</td>
<td width="33%" valign="top">
<h3>🆘 Emergency Requests</h3>
Create requests with blood group, units, hospital, city, date and urgency (Normal or Critical).
</td>
</tr>
<tr>
<td width="33%" valign="top">
<h3>🎯 Smart Matching</h3>
Finds compatible donors in the same city who are active and available right now.
</td>
<td width="33%" valign="top">
<h3>📍 Request Tracking</h3>
A status timeline with donor responses, fulfilled units and full request history.
</td>
<td width="33%" valign="top">
<h3>🔔 Notifications</h3>
In-app alerts for matches, responses, approvals, closures and donations.
</td>
</tr>
<tr>
<td width="33%" valign="top">
<h3>📈 Dashboards</h3>
Separate requester, donor and admin dashboards with trend charts.
</td>
<td width="33%" valign="top">
<h3>🛡️ Administration</h3>
Approve or reject requests, close requests, manage donors and review the disclosure log.
</td>
<td width="33%" valign="top">
<h3>🔒 Privacy First</h3>
Input validation, backend authorization and donor-controlled contact sharing.
</td>
</tr>
</table>

<br>

---

<br>

## 👤 Who Uses HemoLink

<table>
<tr>
<td width="33%" valign="top" align="center">
<h3>🩸 Donor</h3>
<i>A person willing to donate blood</i>
<br><br>
Manages profile and availability<br>
Responds to matched requests<br>
Chooses whether to share contact<br>
Reports completed donations
</td>
<td width="33%" valign="top" align="center">
<h3>🆘 Requester</h3>
<i>A person who needs blood</i>
<br><br>
Creates and closes requests<br>
Sees anonymous match results<br>
Views contacts donors agreed to share<br>
Tracks request progress
</td>
<td width="33%" valign="top" align="center">
<h3>🛡️ Administrator</h3>
<i>An authorized staff member</i>
<br><br>
Reviews and approves requests<br>
Manages donor profiles<br>
Adjusts donation guidance settings<br>
Reviews the disclosure log
</td>
</tr>
</table>

> 💡 A registered user can be both a donor and a requester.

<br>

---

<br>

## 🔄 How It Works

> From emergency to a matched donor in a few simple steps.

```text
  Requester submits a request
              │
              ▼
          Urgency?
        ┌─────┴──────────────┐
     Critical              Normal
        │                    │
        │            Admin reviews and approves
        │                    │
        └─────────┬──────────┘
                  ▼
        Matching starts
                  │
                  ▼
        Compatible donors in the same city are alerted
                  │
                  ▼
        Donor responds
        ┌─────────┴───────────────┐
    Declines                   Accepts
        │                         │
        ▼                         ▼
  Request stays open     Donor chooses to share contact details
  for other donors                │
                                  ▼
                       Requester contacts the donor
                                  │
                                  ▼
                 Donation reported, request fulfilled
```

### 🚦 Request status life cycle

```text
  PENDING ───────────► APPROVED ───────────► FULFILLED
     │                    │
     │                    └───────────────► CLOSED
     │
     ├──────────────────────────────────► REJECTED
     ├──────────────────────────────────► CLOSED
     └──────────────────────────────────► FULFILLED   (Critical requests only)

  REJECTED, CLOSED and FULFILLED are final: no further changes.
```

<details>
<summary><b>🩸 &nbsp;Blood-group compatibility rules used for matching (click to open)</b></summary>

<br>

| Patient needs | Compatible donor groups |
|:---:|---|
| **O-** | O- |
| **O+** | O-, O+ |
| **A-** | O-, A- |
| **A+** | O-, O+, A-, A+ |
| **B-** | O-, B- |
| **B+** | O-, O+, B-, B+ |
| **AB-** | O-, A-, B-, AB- |
| **AB+** | All groups |

These rules identify *potential* donors only. They are not a clinical cross-match. See [`docs/blood-compatibility.md`](docs/blood-compatibility.md).

</details>

<br>

---

<br>

## 🧰 Technology Stack

| | Layer | Technology |
|:---:|---|---|
| ⚙️ | **Backend** | Java 21, Spring Boot, Spring Security |
| 🎨 | **Frontend** | HTML, CSS, JavaScript, Thymeleaf, Chart.js |
| 🗄️ | **Database** | MySQL 8 |
| 🔗 | **Data access** | Spring Data JPA, Hibernate |
| 📦 | **Build** | Maven (wrapper included) |
| 🧪 | **Unit testing** | JUnit 5, Mockito |
| 🌐 | **Integration and web testing** | Spring Boot Test, MockMvc |
| 📮 | **API testing** | Postman (if required) |
| 🔀 | **Version control** | Git and GitHub |

<br>

---

<br>

## 🚀 Quick Start

> Get HemoLink running on your own computer in about ten minutes.

### 📋 Before you begin

| ☕ JDK 21 | 🐬 MySQL 8.0.16+ | 📦 Maven |
|:---:|:---:|:---:|
| Required | Required and running | Not needed (wrapper included) |

<br>

### 1️⃣ &nbsp;Clone the repository

```bash
git clone https://github.com/<owner>/hemolink-bdfers.git
cd hemolink-bdfers
```

### 2️⃣ &nbsp;Create the database

Run the scripts **in this order** (in MySQL Workbench, or with the `mysql` command):

```bash
mysql -u root -p < database/database_schema.sql
mysql -u root -p hemolink_db < database/003_guidance_and_health_checks.sql
mysql -u root -p hemolink_db < database/004_request_review_tracking.sql
mysql -u root -p hemolink_db < database/005_donor_status_history.sql
mysql -u root -p hemolink_db < database/006_notification_type.sql
mysql -u root -p hemolink_db < database/007_query_indexes.sql
mysql -u root -p hemolink_db < database/seed_dummy_data.sql          # optional demo data
mysql -u root -p hemolink_db < database/seed_demo_activity.sql       # optional, after the line above
```

> [!IMPORTANT]
> The application uses `ddl-auto=validate`, so it will **not start** until every required script has been applied. See [`database/README.md`](database/README.md) for details.

### 3️⃣ &nbsp;Add your database password

```bash
cp src/main/resources/application-local.properties.example src/main/resources/application-local.properties
```

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/hemolink_db
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD
```

> [!WARNING]
> `application-local.properties` is ignored by Git. Never commit real passwords.

### 4️⃣ &nbsp;Run the application

```bash
# Windows (PowerShell)
.\mvnw.cmd spring-boot:run

# macOS / Linux
./mvnw spring-boot:run
```

Then open **http://localhost:8080** 🎉

### 5️⃣ &nbsp;Log in with a demo account

All seeded accounts use the password **`password`** (fictional data, for development only).

| Role | Email |
|---|---|
| 🛡️ Admin | `admin@hemolink.test` |
| 🆘 Requester (Sukkur) | `requester.hamza@hemolink.test` |
| 🆘 Requester (Karachi) | `requester.zainab@hemolink.test` |
| 🩸 Donors | `donor.*@hemolink.test` (see `database/seed_dummy_data.sql`) |

<details>
<summary><b>🛠️ &nbsp;Something went wrong? Troubleshooting (click to open)</b></summary>

<br>

| Problem | Fix |
|---|---|
| `Access denied for user 'root'` | The password in `application-local.properties` is wrong |
| `Schema-validation: missing table/column` | A database script was not applied; run them in the order above |
| `Unknown database 'hemolink_db'` | Run `database_schema.sql` first |
| `Communications link failure` | MySQL is not running; start the **MySQL80** service |
| `Port 8080 was already in use` | Close the other program or change `server.port` in `application.properties` |
| Build error about the Java version | Install JDK 21 |

</details>

<br>

---

<br>

## 📁 Project Structure

```text
hemolink-bdfers/
│
├── 📂 .github/                       Issue templates and pull request template
├── 📂 database/                      SQL schema, migrations and demo data
├── 📂 docs/                          Documentation and project deliverables
│
├── 📂 src/
│   ├── 📂 main/
│   │   ├── 📂 java/com/hemolink/bdfers/
│   │   │   ├── config/               Security and time configuration
│   │   │   ├── controller/           Web controllers
│   │   │   ├── dto/                  Request and response objects
│   │   │   ├── entity/               JPA entities
│   │   │   ├── enums/                Statuses, blood groups, roles
│   │   │   ├── exception/            Business exceptions
│   │   │   ├── repository/           Spring Data repositories
│   │   │   ├── service/              Business logic
│   │   │   └── util/                 Helpers
│   │   └── 📂 resources/
│   │       ├── templates/            Thymeleaf pages
│   │       └── static/               CSS, JavaScript
│   └── 📂 test/                      Unit and integration tests
│
├── 📄 README.md
├── 📄 CONTRIBUTING.md
├── 📄 pom.xml
└── 📄 LICENSE
```

<br>

---

<br>

## 🧪 Software Quality Engineering

> Quality is built into every phase of the life cycle, not added at the end.

| Phase | What we do |
|:---:|---|
| 📝 **Requirements** | Requirements analysis, SRS and a requirements traceability matrix |
| 🏗️ **Design** | Architecture and design reviews |
| 💻 **Coding** | Coding standards and code reviews |
| 🔀 **Collaboration** | Feature branches, pull requests and GitHub Issues |
| 🧪 **Testing** | Test planning and test-case design; unit, integration, system and acceptance testing |
| 🐞 **Defects** | Identification, classification, tracking and retesting |
| 📊 **Metrics** | Quality measurement using actual project data |

<br>

---

<br>

## 🗺️ Project Scope

<table>
<tr>
<td width="50%" valign="top">

### ✅ In scope

▸ User registration, authentication and role-based authorization<br>
▸ Donor profile and availability management<br>
▸ Emergency blood request creation and management<br>
▸ Rule-based donor matching and search<br>
▸ Request-status tracking and donor responses<br>
▸ Administrative review and donor profile management<br>
▸ Input validation, error handling and data integrity<br>
▸ Controlled disclosure of donor contact information

</td>
<td width="50%" valign="top">

### ❌ Out of scope

▸ Online payments<br>
▸ Real-time chat and video calling<br>
▸ Email, SMS and push notifications<br>
▸ GPS tracking, live maps and routing<br>
▸ External hospital and blood-bank integration<br>
▸ Native mobile applications<br>
▸ Medical advice and automated clinical decisions<br>
▸ AI-based donor recommendations

</td>
</tr>
</table>

> 🔐 Only **dummy or sample personal data** is used during development and testing.

<br>

---

<br>

## 📌 Project Status

**Current phase:** Implementation and quality evaluation

```text
Accounts, roles and security             ▰▰▰▰▰▰▰▰▰▰   ✅ Done
Donor profiles and availability          ▰▰▰▰▰▰▰▰▰▰   ✅ Done
Blood requests and tracking              ▰▰▰▰▰▰▰▰▰▰   ✅ Done
Matching and consent-based sharing       ▰▰▰▰▰▰▰▰▰▰   ✅ Done
Admin review and donor management        ▰▰▰▰▰▰▰▰▰▰   ✅ Done
Notifications and dashboards             ▰▰▰▰▰▰▰▰▰▰   ✅ Done
Automated test suite                     ▰▰▰▱▱▱▱▱▱▱   🚧 In progress
Test execution, defects and metrics      ▰▰▱▱▱▱▱▱▱▱   🚧 In progress
```

<br>

---

<br>

## 👥 Meet the Team

<table>
<tr>
<td width="33%" align="center">
<h1>👑</h1>
<b>Talha Irfan</b><br>
<sub>Team Lead</sub>
</td>
<td width="33%" align="center">
<h1>🧑‍💻</h1>
<b>Ghulam Mustafa</b><br>
<sub>Team Member</sub>
</td>
<td width="33%" align="center">
<h1>🧑‍💻</h1>
<b>Sumera Naz</b><br>
<sub>Team Member</sub>
</td>
</tr>
</table>

<div align="center">

<sub>All members take part in requirements analysis, design, implementation, testing, documentation and quality reviews.</sub>

</div>

<br>

---

<br>

## 🤝 How We Collaborate

| | Practice |
|:---:|---|
| 🌿 | Create a **feature branch** for every change |
| 💬 | Write **meaningful commit messages**, for example `feat: add donor filter` or `fix: correct request count` |
| 🔍 | Open a **pull request** and ask a teammate to review it |
| 🐞 | Track tasks and defects in **GitHub Issues** using the issue templates |

See [CONTRIBUTING.md](CONTRIBUTING.md) for the full workflow.

<br>

---

<br>

## 📦 Course Deliverables

| # | Deliverable |
|:---:|---|
| 1 | Project Proposal |
| 2 | Software Requirements Specification (SRS) |
| 3 | Software Design and GitHub Repository |
| 4 | Software Quality Assurance Plan |
| 5 | Test Plan and Test Cases |
| 6 | Test Execution and Defect Report |
| 7 | Software Quality Metrics Report |
| 8 | Final Software Product and Report |
| 9 | Final Demonstration and Viva |

<br>

---

<br>

## 📜 License

Released under the **MIT License**. See the [LICENSE](LICENSE) file for details.

<br>

---

<br>

<div align="center">

### 🩸 *Every donation can save a life.*

<br>

**Developed as part of the Software Quality Engineering course**

Department of Computer Science · Sukkur Institute of Business Administration University · Fall 2026

<br>

</div>
