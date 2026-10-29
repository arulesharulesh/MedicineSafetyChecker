-- =====================================================================
-- DEMO DATA — NOT FOR CLINICAL USE
-- This is a small illustrative dataset for local development and
-- testing ONLY. It is not a licensed, authoritative drug-interaction
-- source and must never be used for real clinical decisions.
-- For production, replace with a properly licensed dataset
-- (e.g. DrugBank, an FDA-approved interaction database, or a
-- licensed Kaggle dataset that permits this use).
-- =====================================================================

INSERT INTO medicines (name, generic_name, brand_name, description) VALUES
('Aspirin', 'Acetylsalicylic acid', 'Bayer', 'DEMO DATA — NOT FOR CLINICAL USE. Antiplatelet / NSAID.'),
('Warfarin', 'Warfarin sodium', 'Coumadin', 'DEMO DATA — NOT FOR CLINICAL USE. Anticoagulant.'),
('Metformin', 'Metformin hydrochloride', 'Glucophage', 'DEMO DATA — NOT FOR CLINICAL USE. Antidiabetic (biguanide).'),
('Ibuprofen', 'Ibuprofen', 'Advil', 'DEMO DATA — NOT FOR CLINICAL USE. NSAID.'),
('Lisinopril', 'Lisinopril', 'Prinivil', 'DEMO DATA — NOT FOR CLINICAL USE. ACE inhibitor.'),
('Simvastatin', 'Simvastatin', 'Zocor', 'DEMO DATA — NOT FOR CLINICAL USE. Statin.'),
('Amoxicillin', 'Amoxicillin', 'Amoxil', 'DEMO DATA — NOT FOR CLINICAL USE. Antibiotic.'),
('Paracetamol', 'Acetaminophen', 'Tylenol', 'DEMO DATA — NOT FOR CLINICAL USE. Analgesic/antipyretic.');

INSERT INTO drug_interactions (drug1, drug2, interaction_description, severity, management, warning) VALUES
('Aspirin', 'Warfarin', 'DEMO DATA — NOT FOR CLINICAL USE. Illustrative record: combined use may increase bleeding risk due to additive antiplatelet/anticoagulant effects.', 'HIGH', 'DEMO DATA — Clinical review and monitoring recommended before co-administration.', 'Do not start, stop, or change these medicines without consulting a qualified healthcare professional.'),
('Ibuprofen', 'Warfarin', 'DEMO DATA — NOT FOR CLINICAL USE. Illustrative record: NSAIDs may increase anticoagulant effect and bleeding risk.', 'HIGH', 'DEMO DATA — Avoid combination unless directed and monitored by a clinician.', 'Consult a qualified healthcare professional before combining these medicines.'),
('Aspirin', 'Ibuprofen', 'DEMO DATA — NOT FOR CLINICAL USE. Illustrative record: combined NSAID use may reduce gastroprotective effect and increase GI risk.', 'MODERATE', 'DEMO DATA — Use lowest effective doses; clinician review advised.', 'Consult a qualified healthcare professional before combining these medicines.'),
('Lisinopril', 'Ibuprofen', 'DEMO DATA — NOT FOR CLINICAL USE. Illustrative record: NSAIDs may reduce the antihypertensive effect of ACE inhibitors and affect renal function.', 'MODERATE', 'DEMO DATA — Monitor blood pressure and renal function.', 'Consult a qualified healthcare professional before combining these medicines.'),
('Simvastatin', 'Amoxicillin', 'DEMO DATA — NOT FOR CLINICAL USE. Illustrative record: no clinically significant interaction typically reported.', 'LOW', 'DEMO DATA — Routine use; no special precaution generally required.', 'Always consult a qualified healthcare professional for individual guidance.'),
('Metformin', 'Lisinopril', 'DEMO DATA — NOT FOR CLINICAL USE. Illustrative record: generally considered compatible; monitor renal function periodically.', 'LOW', 'DEMO DATA — Routine monitoring as clinically indicated.', 'Consult a qualified healthcare professional for individual guidance.'),
('Metformin', 'Paracetamol', 'DEMO DATA — NOT FOR CLINICAL USE. Illustrative record: no known clinically significant interaction.', 'NONE', 'DEMO DATA — No specific precaution identified in this demo dataset.', 'Always consult a qualified healthcare professional for individual guidance.');
