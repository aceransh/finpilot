# ✈️ FinPilot

**Automated Personal Finance Dashboard**

FinPilot is a full-stack financial management application that allows users to connect their bank accounts, sync transactions in real-time, and visualize their spending habits. Built with a modern React frontend and a robust Spring Boot backend.

---

## 🚀 Live Demo

* **Frontend (Vercel):** [https://finpilot.vercel.app](https://www.google.com/search?q=https://finpilot-hbhp.vercel.app/)
* **Backend API (Render):** [https://finpilot-api.onrender.com](https://www.google.com/search?q=https://finpilot-ze64.onrender.com)

---

## ✨ Features

* **🔐 Secure Authentication:** JWT-based registration and login system.
* **🏦 Bank Integration:** Connect real bank accounts securely using **Plaid** (Sandbox mode enabled).
* **🔄 Real-Time Sync:** Fetch and sync transactions directly from connected accounts.
* **🏷️ Smart Management:**
* **Auto-Categorization:** Transactions are automatically tagged based on Plaid data.
* **Custom Categories:** Create and assign your own custom categories (with colors!).
* **Editing:** Update transaction descriptions and categories on the fly.


* **📊 Visual Insights:** Interactive pie charts and spending summaries.
* **📱 Responsive Design:** Fully responsive UI built with Material UI.

---

## 🛠️ Tech Stack

### **Frontend**

* **Framework:** React (Vite)
* **Language:** TypeScript
* **UI Library:** Material UI (MUI)
* **Charts:** Recharts
* **State Management:** Redux Toolkit
* **Deployment:** Vercel

### **Backend**

* **Framework:** Spring Boot 3 (Java 23)
* **Database:** PostgreSQL (Supabase)
* **ORM:** Hibernate / Spring Data JPA
* **Migrations:** Flyway
* **Security:** Spring Security + JWT
* **Deployment:** Render (Dockerized)

---

## ⚙️ Local Development Setup

### **1. Prerequisites**

* Node.js & npm
* Java JDK 21+ (Recommend JDK 23)
* Maven
* PostgreSQL (Local or Cloud)

### **2. Clone the Repository**

```bash
git clone https://github.com/aceransh/finpilot.git
cd finpilot

```

### **3. Backend Setup**

Navigate to the backend directory:

```bash
cd backend

```

**Environment Variables (IntelliJ / Application Properties):**
You need to set the following variables in your IDE or environment:

```properties
DB_URL=jdbc:postgresql://localhost:5433/finpilot
DB_USERNAME=postgres
DB_PASSWORD=your_password
JWT_SECRET=your_super_long_secret_key_here
PLAID_CLIENT_ID=your_plaid_client_id
PLAID_SECRET=your_plaid_secret
PLAID_ENV=sandbox

```

**Run the App:**

```bash
mvn spring-boot:run

```

*The backend will start at `http://localhost:8080`.*

### **4. Frontend Setup**

Navigate to the frontend directory:

```bash
cd frontend

```

**Install Dependencies:**

```bash
npm install

```

**Run Development Server:**

```bash
npm run dev

```

*The frontend will start at `http://localhost:5173`.*

---

## ☁️ Deployment Guide (The Free Stack)

This project is deployed using the "Forever Free" stack:

1. **Database:** **Supabase** (PostgreSQL).
* *Note:* Use the **Session Pooler** (Port 6543) for compatibility with Render (IPv4).


2. **Backend:** **Render** (Docker).
* Builds from the `backend/Dockerfile`.
* Environment variables injected via Render Dashboard.


3. **Frontend:** **Vercel**.
* Connects to the Render API via `VITE_API_URL`.



---

## 🔮 Future Improvements

* [ ] **Rules Engine:** Create "If/Then" rules to auto-categorize recurring transactions (e.g., "If description contains 'Netflix', set category to 'Subscriptions'").
* [ ] **Budgeting:** Set monthly spend limits per category.
* [ ] **Recurring Transactions:** Detect and forecast monthly bills.

---

## 👤 Author

**Ansh Desai**

* [GitHub](https://www.google.com/search?q=https://github.com/aceransh)
* [LinkedIn](https://www.google.com/search?q=https://linkedin.com/in/anshdesai)

---