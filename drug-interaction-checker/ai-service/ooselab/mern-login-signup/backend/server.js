const express = require("express");
const cors = require("cors");
const dotenv = require("dotenv");
const path = require("path");

const connectDB = require("./config/db");

const authRoutes = require("./routes/authRoutes");

const protect = require("./middleware/authMiddleware");


dotenv.config();


// Connect MongoDB
connectDB();


const app = express();


// Middleware
app.use(cors());

app.use(express.json());
app.use(express.static(path.join(__dirname, "..", "frontend")));


// =========================
// TEST ROUTE
// =========================

app.get("/", (req, res) => {

    res.sendFile(path.join(__dirname, "..", "frontend", "index.html"));

});


// =========================
// AUTH ROUTES
// =========================

app.use("/api/auth", authRoutes);


// =========================
// PROTECTED TEST ROUTE
// =========================

app.get("/api/profile", protect, (req, res) => {

    res.json({

        message: "You can access this protected route",

        user: req.user

    });

});


// =========================
// START SERVER
// =========================

const PORT = process.env.PORT || 5000;


app.listen(PORT, () => {

    console.log(`Server running on http://localhost:${PORT}`);

});