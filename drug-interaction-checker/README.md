# AI-Based Drug Interaction Checker (Educational Prototype)

An educational, decision-support web app that checks pairs of medicines
for known interactions, classifies severity, explains the interaction
in plain language, and generates a doctor-friendly report.

> **Medical Safety Disclaimer:** This application is intended for
> educational and decision-support purposes only. Interaction
> information may be incomplete or outdated. Do not start, stop,
> change, or substitute medication based solely on this application.
> Always consult a qualified healthcare professional.

## 1. Overview

```
User enters medicines
        ↓
Medicine Name Extraction (Spring Boot)
        ↓
Drug Database (MySQL)
        ↓
Interaction Detection (bidirectional pair matching)
        ↓
Severity Classification (DB first, ML fallback)
        ↓
AI Explanation (grounded in DB data)
        ↓
Safety Warning
        ↓
Doctor-friendly Report (+ PDF)
```

## 2. Features

- Enter 2–10 medicines, add/remove dynamically, case-insensitive matching
- All-pairs interaction checking against a MySQL database, **bidirectional**
  (Aspirin+Warfarin matches a Warfarin+Aspirin record)
- Severity levels: `NONE / LOW / MODERATE / HIGH`
- Database-verified results are always preferred; a Python ML model is
  used **only** as a fallback for pairs with no DB record, and every
  ML-generated result is explicitly labeled `ML_MODEL_PREDICTION` with
  a confidence score — never presented as medical certainty
- Doctor-friendly report screen + "Download Report as PDF"
- Clear separation of verified database data vs. AI-generated text
- Errors handled gracefully (unknown medicine, empty input, DB down,
  AI service down, invalid JSON, duplicates)

## 3. Architecture

```
Browser (HTML/CSS/JS, Bootstrap 5)
        │  REST (fetch)
        ▼
Spring Boot backend (Java 17, port 8080)
        │                       │
        ▼                       ▼
   MySQL (drug data)     Python Flask AI service (port 5000)
                                 │
                            severity_model.pkl (scikit-learn)
```

## 4. Technology Stack

| Layer     | Tech |
|-----------|------|
| Frontend  | HTML5, CSS3, vanilla JS, Bootstrap 5, jsPDF (client-side PDF) |
| Backend   | Java 17, Spring Boot 3.3, Spring Web, Spring Data JPA, MySQL Connector/J, Maven |
| AI/ML     | Python 3.10+, Flask, Pandas, NumPy, scikit-learn, Joblib |
| Database  | MySQL 8 |

## 5. Folder Structure

```
drug-interaction-checker/
├── frontend/
│   ├── index.html
│   ├── style.css
│   └── script.js
├── backend/
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/example/drugchecker/
│       │   ├── controller/   (InteractionController, MedicineController, ReportController, HealthController)
│       │   ├── service/      (InteractionService, MedicineNameExtractionService, AiClientService, ExplanationService, MedicineService, ReportService)
│       │   ├── repository/   (MedicineRepository, DrugInteractionRepository, ReportRepository)
│       │   ├── model/        (Medicine, DrugInteraction, Report, Severity)
│       │   ├── dto/
│       │   ├── exception/    (GlobalExceptionHandler + custom exceptions)
│       │   ├── config/       (CorsConfig)
│       │   └── DrugCheckerApplication.java
│       ├── main/resources/
│       │   ├── application.properties  (env-var driven)
│       │   └── data.sql                (DEMO DATA ONLY, clearly labeled)
│       └── test/java/...     (InteractionServiceTest)
├── ai-service/
│   ├── app.py
│   ├── train_model.py
│   ├── test_app.py
│   ├── requirements.txt
│   ├── data/drug_interactions.csv   (DEMO DATA ONLY)
│   ├── model/                        (created after training)
│   └── README.md
└── README.md   (this file)
```

## 6. Database Setup (MySQL)

```bash
mysql -u root -p
```

```sql
CREATE DATABASE drug_interaction_checker CHARACTER SET utf8mb4;
CREATE USER 'drugapp'@'localhost' IDENTIFIED BY 'change-me';
GRANT ALL PRIVILEGES ON drug_interaction_checker.* TO 'drugapp'@'localhost';
FLUSH PRIVILEGES;
```

Tables (`medicines`, `drug_interactions`, `reports`) are auto-created by
Hibernate (`spring.jpa.hibernate.ddl-auto=update`) on first backend
start, and `data.sql` seeds a small **DEMO DATA — NOT FOR CLINICAL USE**
dataset automatically.

Set environment variables before running the backend:

```bash
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=drug_interaction_checker
export DB_USERNAME=drugapp
export DB_PASSWORD=change-me
export AI_SERVICE_URL=http://localhost:5000
```

## 7. Dataset Preparation (Kaggle or other source)

This prototype ships only a **tiny illustrative demo dataset** — it is
labeled `DEMO DATA — NOT FOR CLINICAL USE` everywhere it appears and
must not be treated as real medical information.

To use a real dataset (e.g. from Kaggle):

1. Search Kaggle for a drug-drug interaction dataset and confirm its
   license permits your intended use.
2. Download the CSV to `ai-service/data/drug_interactions.csv`,
   with (or renamed to) these columns:
   `drug1, drug2, interaction_description, severity`
   — `severity` must be one of `NONE`, `LOW`, `MODERATE`, `HIGH`.
3. If the dataset has **no severity labels**, do not invent them.
   Either look for a dataset that includes labels, or keep the app in
   database-only mode (skip training/using the ML fallback) until a
   labeled dataset is available — `train_model.py` will refuse to
   train on fewer than 2 label classes.
4. For the MySQL side, also load verified interaction rows into the
   `drug_interactions` table (via `data.sql` or an import script) so
   the backend can serve them as `"source": "DATABASE"` results,
   which always take priority over the ML fallback.

I don't have network access to kaggle.com from this environment, so I
couldn't fetch a specific dataset for you — if you upload a Kaggle CSV
here, I can help you clean/map its columns and generate the SQL import.

## 8. Backend Setup

```bash
cd backend
mvn spring-boot:run
```

Runs on `http://localhost:8080`. Requires MySQL running and the env
vars from Section 6 exported (or edit `application.properties`
defaults for local dev only — never commit real credentials).

## 9. AI Service Setup

```bash
cd ai-service
python -m venv venv && source venv/bin/activate
pip install -r requirements.txt
python train_model.py     # creates model/severity_model.pkl
python app.py             # runs on http://localhost:5000
```

## 10. Frontend Setup

The frontend is static — no build step. Options:

```bash
cd frontend
python -m http.server 5500
# open http://localhost:5500
```

Or open `index.html` directly / serve via VS Code "Live Server". If
your frontend origin differs from `http://localhost:5500`, add it to
`CORS_ALLOWED_ORIGINS` and update `API_BASE_URL` in `script.js`.

## 11. API Documentation

| Method | Path | Description |
|--------|------|--------------|
| POST | `/api/interactions/check` | Body: `{"medicines": ["Aspirin","Warfarin"]}` → interaction results |
| GET  | `/api/medicines/search?name=aspirin` | Search known medicines |
| POST | `/api/reports` | Save a report: `{"patientName": "...", "checkResult": {...}}` |
| GET  | `/api/reports/{id}` | Fetch a saved report |
| GET  | `/api/health` | Liveness check |

Example:

```bash
curl -X POST http://localhost:8080/api/interactions/check \
  -H "Content-Type: application/json" \
  -d '{"medicines": ["Aspirin", "Warfarin", "Metformin"]}'
```

AI service:

```bash
curl -X POST http://localhost:5000/predict \
  -H "Content-Type: application/json" \
  -d '{"drug1": "Aspirin", "drug2": "Naproxen", "interaction": ""}'
```

## 12. How to Run All Three Services

```bash
# terminal 1
cd ai-service && source venv/bin/activate && python app.py

# terminal 2
cd backend && mvn spring-boot:run

# terminal 3
cd frontend && python -m http.server 5500
```

Then open `http://localhost:5500`.

## 13. Testing

```bash
# backend
cd backend && mvn test

# AI service
cd ai-service && pytest
```

Frontend behaviors (add/remove medicine, validation, duplicate
detection, API response rendering) are implemented in `script.js` and
are best verified manually in-browser for this prototype; see
`ai-service/README.md` and `InteractionServiceTest.java` for the
automated coverage of the core detection/matching logic.

## 14. Limitations

- The shipped dataset (both `data.sql` and `ai-service/data/drug_interactions.csv`)
  is **demo data only** — small, illustrative, and not clinically validated.
- The ML fallback is a simple baseline model trained on a tiny dataset;
  its accuracy numbers are not meaningful until trained on a real,
  properly labeled dataset.
- Medicine name matching uses simple containment-based fuzzy matching,
  not a full spelling-correction algorithm.
- This is a prototype, not a certified medical device or clinical
  decision support system.

## 15. Medical Safety Disclaimer

This application is intended for educational and decision-support
purposes only. Interaction information may be incomplete or outdated.
Do not start, stop, change, or substitute medication based solely on
this application. Always consult a qualified healthcare professional.

## 16. Common Errors & Fixes

| Error | Fix |
|---|---|
| `Communications link failure` on backend start | MySQL isn't running or `DB_HOST`/`DB_PORT` are wrong |
| Backend returns 503 "AI prediction service is currently unavailable" | Start `ai-service` (`python app.py`), verify `AI_SERVICE_URL` |
| CORS error in browser console | Add your frontend origin to `CORS_ALLOWED_ORIGINS` |
| `/predict` returns 503 "Model is not available" | Run `python train_model.py` first to create `model/severity_model.pkl` |
| Frontend shows "Medicine not found" for a real drug | It's not in the demo dataset — add it to `data.sql` / `medicines` table |
| `mvn spring-boot:run` fails to download dependencies | Needs internet access to Maven Central — check your network/proxy settings |
