import pandas as pd

from train_model import load_and_clean


def test_load_and_clean_supports_standard_columns():
    df = pd.DataFrame(
        [
            {
                "drug1": "Aspirin",
                "drug2": "Warfarin",
                "interaction_description": "Bleeding risk",
                "severity": "HIGH",
            },
            {
                "drug1": "Metformin",
                "drug2": "Paracetamol",
                "interaction_description": "No known interaction",
                "severity": "NONE",
            },
        ]
    )
    result = load_and_clean(df)

    assert list(result.columns) == ["drug1", "drug2", "interaction_description", "severity"]
    assert result["drug1"].tolist() == ["aspirin", "metformin"]
    assert result["severity"].tolist() == ["HIGH", "NONE"]


def test_load_and_clean_supports_kaggle_notebook_columns():
    df = pd.DataFrame(
        [
            {
                "Drug 1": "Aspirin",
                "Drug 2": "Warfarin",
                "Interaction Description": "Bleeding risk",
                "label": 1,
            },
            {
                "Drug 1": "Metformin",
                "Drug 2": "Paracetamol",
                "Interaction Description": "No known interaction",
                "label": 0,
            },
        ]
    )
    result = load_and_clean(df)

    assert list(result.columns) == ["drug1", "drug2", "interaction_description", "severity"]
    assert result["severity"].tolist() == ["HIGH", "NONE"]
