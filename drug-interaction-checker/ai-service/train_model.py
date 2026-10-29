"""
train_model.py

Trains a baseline severity-classification model for the AI-Based Drug
Interaction Checker.

IMPORTANT:
- data/drug_interactions.csv shipped with this project is DEMO DATA ONLY
  (see the "DEMO DATA - NOT FOR CLINICAL USE" markers in every row).
  It exists so the pipeline runs end-to-end locally.
- To train on a real dataset (e.g. one downloaded from Kaggle), replace
  data/drug_interactions.csv with a CSV that has these columns:
      drug1, drug2, interaction_description, severity
  where severity is one of: NONE, LOW, MODERATE, HIGH
  Then just rerun this script - nothing else needs to change.
- We do NOT fabricate labels: rows with missing/unrecognized severity
  values are dropped rather than guessed.

Usage:
    python train_model.py
"""

import json
import os

import joblib
import pandas as pd
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import (
    accuracy_score,
    classification_report,
    f1_score,
    precision_score,
    recall_score,
)
from sklearn.model_selection import train_test_split
from sklearn.pipeline import Pipeline

DATA_PATH = os.path.join(os.path.dirname(__file__), "data", "drug_interactions.csv")
MODEL_DIR = os.path.join(os.path.dirname(__file__), "model")
MODEL_PATH = os.path.join(MODEL_DIR, "severity_model.pkl")
METRICS_PATH = os.path.join(MODEL_DIR, "metrics.json")

VALID_SEVERITIES = {"NONE", "LOW", "MODERATE", "HIGH"}


def normalize_drug_name(name: str) -> str:
    if not isinstance(name, str):
        return ""
    return " ".join(name.strip().split()).lower()


def infer_severity_from_description(description: str) -> str:
    text = "" if description is None else str(description).strip().lower()

    if not text:
        return "LOW"

    none_keywords = [
        "no interaction",
        "no known interaction",
        "no clinically significant",
        "not expected",
        "not known",
        "not be expected",
    ]
    high_keywords = [
        "increase", "increased", "risk or severity", "photosensitizing",
        "cardiotoxic", "adverse effects", "toxic", "serious",
        "severe", "enhance", "fatal",
    ]
    moderate_keywords = [
        "decrease", "decreased",
        "reduce", "reduced",
        "may affect",
        "monitor",
        "metabolism can be increased",
        "can be increased when combined",
        "alter",
        "change",
    ]

    if any(keyword in text for keyword in none_keywords):
        return "NONE"
    if any(keyword in text for keyword in high_keywords):
        return "HIGH"
    if any(keyword in text for keyword in moderate_keywords):
        return "MODERATE"
    return "LOW"


def normalize_column_names(df: pd.DataFrame) -> pd.DataFrame:
    alias_map = {
        "drug1": ["drug1", "Drug 1", "Drug1", "drug 1"],
        "drug2": ["drug2", "Drug 2", "Drug2", "drug 2"],
        "interaction_description": [
            "interaction_description",
            "Interaction Description",
            "interaction description",
            "description",
            "Description",
        ],
        "severity": ["severity", "Severity", "SEVERITY"],
        "label": ["label", "Label"],
    }

    renamed = {}
    for canonical, candidates in alias_map.items():
        for candidate in candidates:
            if candidate in df.columns:
                renamed[candidate] = canonical
                break

    df = df.rename(columns=renamed)

    if "severity" not in df.columns and "label" in df.columns:
        label_map = {
            0: "NONE",
            1: "HIGH",
            "0": "NONE",
            "1": "HIGH",
            "false": "NONE",
            "true": "HIGH",
            False: "NONE",
            True: "HIGH",
        }
        df["severity"] = df["label"].map(label_map)

    if "severity" not in df.columns:
        if "interaction_description" not in df.columns:
            raise ValueError(
                "Dataset must include an interaction description column."
            )
        df["severity"] = df["interaction_description"].apply(infer_severity_from_description)

    required = {"drug1", "drug2", "interaction_description", "severity"}
    missing = required - set(df.columns)
    if missing:
        raise ValueError(
            f"Dataset is missing required columns: {sorted(missing)}. "
            f"Expected columns include: drug1, drug2, interaction_description, severity "
            f"or a compatible Kaggle-style dataset with 'Drug 1', 'Drug 2', 'Interaction Description'."
        )

    return df


def load_and_clean(path_or_df) -> pd.DataFrame:
    df = pd.read_csv(path_or_df) if isinstance(path_or_df, str) else path_or_df
    df = df.copy()
    df = normalize_column_names(df)

    # Drop rows with missing essential fields - never fabricate them
    df = df.dropna(subset=["drug1", "drug2", "severity"])

    df["drug1"] = df["drug1"].apply(normalize_drug_name)
    df["drug2"] = df["drug2"].apply(normalize_drug_name)
    df["interaction_description"] = df["interaction_description"].fillna("")

    df["severity"] = df["severity"].astype(str).str.strip().str.upper()
    before = len(df)
    df = df[df["severity"].isin(VALID_SEVERITIES)]
    dropped = before - len(df)
    if dropped:
        print(f"Dropped {dropped} row(s) with unrecognized/missing severity labels "
              f"(not guessed - simply excluded).")

    df = df[(df["drug1"] != "") & (df["drug2"] != "")]
    df = df.drop_duplicates(subset=["drug1", "drug2", "severity"])

    return df[["drug1", "drug2", "interaction_description", "severity"]].reset_index(drop=True)


def build_pipeline() -> Pipeline:
    vectorizer = TfidfVectorizer(
        ngram_range=(1, 2),
        min_df=2,
        strip_accents="unicode",
        lowercase=True,
        max_features=50000,
    )
    clf = LogisticRegression(
        max_iter=2000,
        class_weight="balanced",
        solver="liblinear",
    )

    return Pipeline(steps=[("tfidf", vectorizer), ("classifier", clf)])


def build_text_features(df: pd.DataFrame) -> pd.Series:
    return (
        df["drug1"].fillna("").astype(str).str.lower() + " " +
        df["drug2"].fillna("").astype(str).str.lower() + " " +
        df["interaction_description"].fillna("").astype(str)
    )


def main():
    if not os.path.exists(DATA_PATH):
        raise FileNotFoundError(
            f"Dataset not found at {DATA_PATH}. "
            f"Place your CSV there (see this file's docstring for the required columns)."
        )

    df = load_and_clean(DATA_PATH)
    print(f"Loaded {len(df)} usable rows after cleaning.")

    if df["severity"].nunique() < 2:
        raise ValueError(
            "Need at least 2 distinct severity classes to train a classifier. "
            "Check your dataset's 'severity' column."
        )

    X = build_text_features(df)
    y = df["severity"]

    try:
        X_train, X_test, y_train, y_test = train_test_split(
            X, y, test_size=0.25, random_state=42, stratify=y
        )
    except ValueError:
        X_train, X_test, y_train, y_test = train_test_split(
            X, y, test_size=0.25, random_state=42
        )

    pipeline = build_pipeline()
    pipeline.fit(X_train, y_train)

    y_pred = pipeline.predict(X_test)

    metrics = {
        "accuracy": accuracy_score(y_test, y_pred),
        "precision_weighted": precision_score(y_test, y_pred, average="weighted", zero_division=0),
        "recall_weighted": recall_score(y_test, y_pred, average="weighted", zero_division=0),
        "f1_weighted": f1_score(y_test, y_pred, average="weighted", zero_division=0),
        "n_train": len(X_train),
        "n_test": len(X_test),
        "classes": sorted(y.unique().tolist()),
        "note": "Severity labels were inferred from interaction text for this Kaggle dataset.",
    }

    print("\n=== Evaluation (on held-out demo data) ===")
    print(json.dumps(metrics, indent=2))
    print("\nClassification report:")
    print(classification_report(y_test, y_pred, zero_division=0))

    os.makedirs(MODEL_DIR, exist_ok=True)
    joblib.dump(pipeline, MODEL_PATH)
    with open(METRICS_PATH, "w") as f:
        json.dump(metrics, f, indent=2)

    print(f"\nModel saved to: {MODEL_PATH}")
    print(f"Metrics saved to: {METRICS_PATH}")


if __name__ == "__main__":
    main()
