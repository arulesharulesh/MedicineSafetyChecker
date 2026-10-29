# AI/ML Service - Drug Interaction Checker

Python Flask microservice that provides a **fallback** severity estimate
via `/predict` when the MySQL database has no verified record for a
drug pair. It never overrides database-verified data.

## Setup

```bash
cd ai-service
python -m venv venv
source venv/bin/activate        # Windows: venv\Scripts\activate
pip install -r requirements.txt
```

## Train the model

```bash
python train_model.py
```

This reads `data/drug_interactions.csv`, trains a RandomForest
classifier, prints accuracy/precision/recall/F1, and writes:
- `model/severity_model.pkl`
- `model/metrics.json`

**The shipped CSV is DEMO DATA ONLY** (every row is tagged
`DEMO DATA - NOT FOR CLINICAL USE`). To train on a real dataset:

1. Download a drug-interaction dataset from Kaggle (or another
   licensed source) that permits this use.
2. Save it as `data/drug_interactions.csv` with these exact columns:
   `drug1, drug2, interaction_description, severity`
   (`severity` must be one of `NONE`, `LOW`, `MODERATE`, `HIGH`).
3. Re-run `python train_model.py`.

If your Kaggle dataset uses different column names or doesn't
include severity labels at all, see "Adapting a Kaggle dataset"
in the top-level README - the short version is: rename columns to
match, and if there's no severity label, this ML fallback path
should stay disabled (database-only mode) rather than inventing labels.

## Run the service

```bash
python app.py
```

Runs on `http://localhost:5000` by default (override with `AI_SERVICE_PORT`).

## Endpoints

- `GET /health` - model load status
- `POST /predict` - body `{"drug1": "...", "drug2": "...", "interaction": "..."}`
  → `{"severity": "...", "confidence": 0.xx, "note": "..."}`

## Tests

```bash
pytest
```
