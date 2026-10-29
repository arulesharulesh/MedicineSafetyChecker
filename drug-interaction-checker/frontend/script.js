// =====================================================================
// Configuration
// =====================================================================
const API_BASE_URL = "http://localhost:8080/api";
const MIN_MEDICINES = 2;
const MAX_MEDICINES = 10;

// =====================================================================
// State
// =====================================================================
let medicineInputCount = 0;
let lastCheckResult = null; // holds the last /interactions/check response
let lastPatientName = "";

// =====================================================================
// DOM refs
// =====================================================================
const homeSection = document.getElementById("homeSection");
const inputSection = document.getElementById("inputSection");
const resultsSection = document.getElementById("resultsSection");
const reportSection = document.getElementById("reportSection");
const loadingIndicator = document.getElementById("loadingIndicator");
const errorBanner = document.getElementById("errorBanner");
const medicineList = document.getElementById("medicineList");
const validationMessages = document.getElementById("validationMessages");

// =====================================================================
// Navigation helpers
// =====================================================================
function showOnly(sectionEl) {
  [homeSection, inputSection, resultsSection, reportSection].forEach(s => s.classList.add("d-none"));
  loadingIndicator.classList.add("d-none");
  errorBanner.textContent = "";
  errorBanner.classList.add("d-none");
  if (sectionEl) sectionEl.classList.remove("d-none");
}

function showError(message) {
  errorBanner.textContent = message;
  errorBanner.classList.remove("d-none");
}

// =====================================================================
// Medicine input rows
// =====================================================================
function addMedicineRow(prefill = "") {
  if (medicineList.children.length >= MAX_MEDICINES) return;
  medicineInputCount++;
  const row = document.createElement("div");
  row.className = "medicine-row";
  row.innerHTML = `
    <span class="text-muted small" style="width: 90px;">Medicine ${medicineList.children.length + 1}</span>
    <input type="text" class="form-control medicine-input" placeholder="e.g. Aspirin" value="${escapeHtml(prefill)}">
    <button type="button" class="btn btn-outline-danger btn-sm remove-medicine-btn" title="Remove">
      <i class="bi bi-trash"></i>
    </button>
  `;
  row.querySelector(".remove-medicine-btn").addEventListener("click", () => {
    if (medicineList.children.length > MIN_MEDICINES) {
      row.remove();
      renumberRows();
    }
  });
  medicineList.appendChild(row);
}

function renumberRows() {
  [...medicineList.children].forEach((row, idx) => {
    row.querySelector("span").textContent = `Medicine ${idx + 1}`;
  });
}

function escapeHtml(str) {
  const div = document.createElement("div");
  div.textContent = str;
  return div.innerHTML;
}

function initMedicineInputs() {
  medicineList.innerHTML = "";
  addMedicineRow();
  addMedicineRow();
}

function collectMedicineNames() {
  return [...medicineList.querySelectorAll(".medicine-input")]
    .map(input => input.value.trim().replace(/\s+/g, " "))
    .filter(name => name.length > 0);
}

function validateMedicines(names) {
  const errors = [];
  const validNames = names.filter(name => name && name.trim().length > 0);

  if (validNames.length < MIN_MEDICINES) {
    errors.push(`Please enter at least ${MIN_MEDICINES} medicines.`);
  }
  if (validNames.length > MAX_MEDICINES) {
    errors.push(`Please enter at most ${MAX_MEDICINES} medicines.`);
  }

  const lower = validNames.map(n => n.toLowerCase());
  const duplicates = [...new Set(lower.filter((n, i) => lower.indexOf(n) !== i))];
  if (duplicates.length > 0) {
    errors.push(`Duplicate medicine names detected: ${duplicates.join(", ")}`);
  }
  return errors;
}

// =====================================================================
// API calls
// =====================================================================
async function checkInteractions(medicines) {
  const response = await fetch(`${API_BASE_URL}/interactions/check`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ medicines })
  });

  const data = await response.json().catch(() => null);

  if (!response.ok) {
    const message = data && data.message ? data.message : `Request failed (${response.status})`;
    throw new Error(message);
  }
  return data;
}

async function saveReport(patientName, checkResult) {
  const response = await fetch(`${API_BASE_URL}/reports`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ patientName, checkResult })
  });
  if (!response.ok) {
    throw new Error("Could not save report.");
  }
  return response.json();
}

// =====================================================================
// Rendering: results
// =====================================================================
function renderResults(result) {
  if (!result || !Array.isArray(result.interactions) || !Array.isArray(result.medicines)) {
    showError("The server returned an unexpected response for this medicine combination.");
    showOnly(inputSection);
    return;
  }

  lastCheckResult = result;

  const medListEl = document.getElementById("resultMedicinesList");
  medListEl.innerHTML = result.medicines
    .map(m => `<span class="badge">${escapeHtml(m)}</span>`)
    .join("");

  const unknownBox = document.getElementById("unknownMedicinesBox");
  if (result.unknownMedicines && result.unknownMedicines.length > 0) {
    unknownBox.classList.remove("d-none");
    unknownBox.innerHTML = `
      <div class="alert alert-secondary mb-0">
        <i class="bi bi-info-circle"></i> Medicine(s) not found in database, please verify spelling:
        <strong>${result.unknownMedicines.map(escapeHtml).join(", ")}</strong>
      </div>`;
  } else {
    unknownBox.classList.add("d-none");
    unknownBox.innerHTML = "";
  }

  const summaryEl = document.getElementById("resultSummary");
  const highCount = result.interactions.filter(item => (item.severity || "NONE").toUpperCase() === "HIGH").length;
  const moderateCount = result.interactions.filter(item => (item.severity || "NONE").toUpperCase() === "MODERATE").length;
  const lowCount = result.interactions.filter(item => (item.severity || "NONE").toUpperCase() === "LOW").length;

  summaryEl.innerHTML = `
    <div class="col-md-3">
      <div class="summary-tile">
        <span class="tile-icon total"><i class="bi bi-clipboard2-data"></i></span>
        <div>
          <h6>${result.interactions.length}</h6>
          <p>Total checks</p>
        </div>
      </div>
    </div>
    <div class="col-md-3">
      <div class="summary-tile">
        <span class="tile-icon high"><i class="bi bi-exclamation-octagon"></i></span>
        <div>
          <h6>${highCount}</h6>
          <p>High risk</p>
        </div>
      </div>
    </div>
    <div class="col-md-3">
      <div class="summary-tile">
        <span class="tile-icon moderate"><i class="bi bi-heart-pulse"></i></span>
        <div>
          <h6>${moderateCount}</h6>
          <p>Moderate</p>
        </div>
      </div>
    </div>
    <div class="col-md-3">
      <div class="summary-tile">
        <span class="tile-icon low"><i class="bi bi-check2-circle"></i></span>
        <div>
          <h6>${lowCount}</h6>
          <p>Low risk</p>
        </div>
      </div>
    </div>
  `;

  document.getElementById("interactionCountLabel").textContent =
    `Interactions Found: ${result.interactions.length}`;

  const listEl = document.getElementById("interactionResultsList");
  if (result.interactions.length === 0) {
    listEl.innerHTML = `<div class="alert alert-info">No pairs to compare - add more medicines.</div>`;
  } else {
    listEl.innerHTML = result.interactions.map(renderInteractionCard).join("");
  }

  document.getElementById("resultsDisclaimer").innerHTML =
    `<i class="bi bi-exclamation-triangle-fill"></i> ${escapeHtml(result.disclaimer || "")}`;

  showOnly(resultsSection);
}

function renderInteractionCard(item) {
  const severity = (item.severity || "NONE").toUpperCase();
  const sourceLabel = {
    DATABASE: "Verified: Database",
    ML_MODEL_PREDICTION: "AI Estimate (Model)",
    NO_RECORD: "No Record"
  }[item.source] || item.source;

  const confidenceLine = (item.source === "ML_MODEL_PREDICTION" && item.confidence != null)
    ? `<div class="small text-muted mt-1">Model confidence: ${(item.confidence * 100).toFixed(1)}% (not a certainty)</div>`
    : "";

  const managementLine = item.management
    ? `<div class="mt-2"><strong>Database-supported management:</strong> ${escapeHtml(item.management)}</div>`
    : "";

  return `
    <div class="card interaction-card severity-border-${severity} shadow-sm mb-3">
      <div class="card-body">
        <div class="d-flex justify-content-between align-items-start flex-wrap gap-2">
          <h6 class="mb-1">${escapeHtml(item.drug1)} + ${escapeHtml(item.drug2)}</h6>
          <div class="d-flex gap-2 align-items-center">
            <span class="source-tag source-${item.source}">${escapeHtml(sourceLabel)}</span>
            <span class="severity-badge severity-${severity}">${severity}</span>
          </div>
        </div>
        <div class="mt-2"><strong>Interaction:</strong> ${escapeHtml(item.interaction || "")}</div>
        <div class="mt-2"><strong>Explanation:</strong> ${escapeHtml(item.explanation || "")}</div>
        ${managementLine}
        <div class="mt-2"><strong>Safety Warning:</strong> ${escapeHtml(item.warning || "")}</div>
        ${confidenceLine}
      </div>
    </div>`;
}

// =====================================================================
// Rendering: report
// =====================================================================
function renderReport() {
  if (!lastCheckResult) return;
  const now = new Date().toLocaleString();

  const rows = lastCheckResult.interactions.map(item => `
    <tr>
      <td>${escapeHtml(item.drug1)} + ${escapeHtml(item.drug2)}</td>
      <td><span class="severity-badge severity-${item.severity}">${item.severity}</span></td>
      <td>${escapeHtml(item.interaction || "")}</td>
      <td>${escapeHtml(item.explanation || "")}</td>
      <td>${escapeHtml(item.warning || "")}</td>
    </tr>`).join("");

  document.getElementById("reportContent").innerHTML = `
    <h5>Drug Interaction Report</h5>
    <p class="text-muted mb-1">Generated: ${now}</p>
    ${lastPatientName ? `<p class="mb-1"><strong>Patient:</strong> ${escapeHtml(lastPatientName)}</p>` : ""}
    <p><strong>Medicines:</strong> ${lastCheckResult.medicines.map(escapeHtml).join(", ")}</p>
    <div class="table-responsive">
      <table class="table table-bordered align-middle">
        <thead class="table-light">
          <tr><th>Pair</th><th>Severity</th><th>Interaction</th><th>Explanation</th><th>Warning / Recommended Action</th></tr>
        </thead>
        <tbody>${rows}</tbody>
      </table>
    </div>
    <div class="alert alert-warning mt-3">${escapeHtml(lastCheckResult.disclaimer || "")}</div>
  `;
  showOnly(reportSection);
}

function downloadReportAsPdf() {
  if (!lastCheckResult) return;
  const { jsPDF } = window.jspdf;
  const doc = new jsPDF();
  let y = 15;

  doc.setFontSize(16);
  doc.text("Drug Interaction Report", 14, y); y += 8;
  doc.setFontSize(10);
  doc.text(`Generated: ${new Date().toLocaleString()}`, 14, y); y += 6;
  if (lastPatientName) { doc.text(`Patient: ${lastPatientName}`, 14, y); y += 6; }
  doc.text(`Medicines: ${lastCheckResult.medicines.join(", ")}`, 14, y); y += 10;

  lastCheckResult.interactions.forEach(item => {
    if (y > 260) { doc.addPage(); y = 15; }
    doc.setFontSize(12);
    doc.text(`${item.drug1} + ${item.drug2}  (Severity: ${item.severity})`, 14, y); y += 6;
    doc.setFontSize(9);
    y = wrapText(doc, `Interaction: ${item.interaction || ""}`, 14, y);
    y = wrapText(doc, `Explanation: ${item.explanation || ""}`, 14, y);
    y = wrapText(doc, `Warning: ${item.warning || ""}`, 14, y);
    y += 4;
  });

  if (y > 250) { doc.addPage(); y = 15; }
  doc.setFontSize(9);
  y = wrapText(doc, `Disclaimer: ${lastCheckResult.disclaimer || ""}`, 14, y);

  doc.save("drug-interaction-report.pdf");
}

function wrapText(doc, text, x, y, maxWidth = 180, lineHeight = 5) {
  const lines = doc.splitTextToSize(text, maxWidth);
  lines.forEach(line => {
    if (y > 280) { doc.addPage(); y = 15; }
    doc.text(line, x, y);
    y += lineHeight;
  });
  return y + 2;
}

// =====================================================================
// Event wiring
// =====================================================================
document.getElementById("startCheckBtn").addEventListener("click", () => {
  initMedicineInputs();
  validationMessages.innerHTML = "";
  showOnly(inputSection);
});

document.getElementById("navHome").addEventListener("click", (e) => {
  e.preventDefault();
  showOnly(homeSection);
});

document.getElementById("addMedicineBtn").addEventListener("click", () => addMedicineRow());

document.getElementById("backToHomeBtn").addEventListener("click", () => showOnly(homeSection));

document.getElementById("checkInteractionBtn").addEventListener("click", async () => {
  const names = collectMedicineNames();
  const errors = validateMedicines(names);

  if (errors.length > 0) {
    validationMessages.innerHTML = `<div class="alert alert-danger">${errors.map(escapeHtml).join("<br>")}</div>`;
    return;
  }
  validationMessages.innerHTML = "";

  showOnly(null);
  loadingIndicator.classList.remove("d-none");

  try {
    const result = await checkInteractions(names);
    renderResults(result);
  } catch (err) {
    showOnly(inputSection);
    showError(err.message || "Something went wrong while checking interactions.");
  }
});

document.getElementById("newCheckBtn").addEventListener("click", () => {
  initMedicineInputs();
  showOnly(inputSection);
});

document.getElementById("viewReportBtn").addEventListener("click", () => {
  lastPatientName = prompt("Patient name (optional):") || "";
  renderReport();
});

document.getElementById("backToResultsBtn").addEventListener("click", () => showOnly(resultsSection));

document.getElementById("downloadPdfBtn").addEventListener("click", downloadReportAsPdf);

// Initial state
showOnly(homeSection);
