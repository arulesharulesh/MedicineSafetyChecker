"""
Tests for the AI service. Run with: pytest
Assumes train_model.py has been run at least once so
model/severity_model.pkl exists (test_predict_* will skip gracefully
via a 503 check if not).
"""
import os
import sys

sys.path.insert(0, os.path.dirname(__file__))

import pytest

from app import app, normalize_drug_name
from train_model import load_and_clean, DATA_PATH


@pytest.fixture
def client():
    app.config["TESTING"] = True
    with app.test_client() as c:
        yield c


def test_normalize_drug_name():
    assert normalize_drug_name("  Aspirin  ") == "aspirin"
    assert normalize_drug_name("WARFARIN") == "warfarin"
    assert normalize_drug_name(None) == ""


def test_health_endpoint(client):
    resp = client.get("/health")
    assert resp.status_code == 200
    assert "status" in resp.get_json()


def test_predict_missing_fields(client):
    resp = client.post("/predict", json={"drug1": "Aspirin"})
    assert resp.status_code == 400


def test_predict_non_json(client):
    resp = client.post("/predict", data="not json")
    assert resp.status_code == 400


def test_predict_valid_input_shape(client):
    resp = client.post("/predict", json={"drug1": "Aspirin", "drug2": "Warfarin", "interaction": "demo"})
    # Either a successful prediction, or a clean 503 if the model hasn't been trained yet
    assert resp.status_code in (200, 503)
    body = resp.get_json()
    if resp.status_code == 200:
        assert "severity" in body
        assert "confidence" in body


def test_dataset_loading_and_cleaning():
    df = load_and_clean(DATA_PATH)
    assert len(df) > 0
    assert set(df.columns) >= {"drug1", "drug2", "interaction_description", "severity"}
    assert df["severity"].isin(["NONE", "LOW", "MODERATE", "HIGH"]).all()
