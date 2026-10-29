"""
app.py - AI/ML microservice for the Drug Interaction Checker.

Exposes:
    GET  /health
    POST /predict

The /predict endpoint is a fallback used ONLY when the Spring Boot
backend finds no verified record in the MySQL database for a drug
pair. It returns a model-generated severity ESTIMATE with a confidence
score - never presented as medical certainty, and the caller is
expected to label it clearly as such (the backend does this).

Run:
    pip install -r requirements.txt
    python train_model.py   # first time, to create model/severity_model.pkl
    python app.py
"""

import os

import joblib
import pandas as pd
from flask import Flask, jsonify, request

MODEL_PATH = os.path.join(os.path.dirname(__file__), "model", "severity_model.pkl")

app = Flask(__name__)

_model = None
_model_load_error = None


def get_model():
    global _model, _model_load_error
    if _model is None and _model_load_error is None:
        try:
            _model = joblib.load(MODEL_PATH)
        except Exception as exc:  # noqa: BLE001 - report to caller, don't crash worker
            _model_load_error = str(exc)
    return _model


def normalize_drug_name(name):
    if not isinstance(name, str):
        return ""
    return " ".join(name.strip().split()).lower()


@app.route("/health", methods=["GET"])
def health():
    model = get_model()
    return jsonify({
        "status": "UP" if model is not None else "MODEL_NOT_LOADED",
        "model_loaded": model is not None,
        "error": _model_load_error,
    })


@app.route("/predict", methods=["POST"])
def predict():
    if not request.is_json:
        return jsonify({"error": "Request body must be JSON."}), 400

    payload = request.get_json(silent=True) or {}
    drug1 = payload.get("drug1")
    drug2 = payload.get("drug2")
    interaction_text = payload.get("interaction", "") or ""

    if not drug1 or not drug2:
        return jsonify({"error": "'drug1' and 'drug2' are required fields."}), 400

    model = get_model()
    if model is None:
        return jsonify({
            "error": "Model is not available on the server. "
                     "Run train_model.py first to generate model/severity_model.pkl.",
            "detail": _model_load_error,
        }), 503

    try:
        row = pd.DataFrame([{
            "drug1": normalize_drug_name(drug1),
            "drug2": normalize_drug_name(drug2),
            "interaction_description": interaction_text,
        }])

        prediction = model.predict(row)[0]

        confidence = None
        if hasattr(model, "predict_proba"):
            proba = model.predict_proba(row)[0]
            confidence = float(max(proba))

        return jsonify({
            "severity": str(prediction),
            "confidence": round(confidence, 4) if confidence is not None else None,
            "note": "Model-generated estimate, not verified clinical data. "
                    "Not a substitute for professional medical advice.",
        })
    except Exception as exc:  # noqa: BLE001
        app.logger.exception("Prediction failed")
        return jsonify({"error": "Prediction failed.", "detail": str(exc)}), 500


if __name__ == "__main__":
    port = int(os.environ.get("AI_SERVICE_PORT", 5000))
    debug = os.environ.get("FLASK_DEBUG", "false").lower() == "true"
    app.run(host="0.0.0.0", port=port, debug=debug)
