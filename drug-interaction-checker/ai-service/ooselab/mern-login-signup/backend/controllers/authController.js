const User = require("../model/user");
const bcrypt = require("bcryptjs");
const jwt = require("jsonwebtoken");


// =========================
// SIGNUP
// =========================

const signup = async (req, res) => {

    try {

        const { name, email, password } = req.body;

        // Check fields
        if (!name || !email || !password) {

            return res.status(400).json({
                message: "All fields are required"
            });

        }


        // Check password length
        if (password.length < 6) {

            return res.status(400).json({
                message: "Password must be at least 6 characters"
            });

        }


        // Check existing user
        const existingUser = await User.findOne({ email });

        if (existingUser) {

            return res.status(400).json({
                message: "User already exists"
            });

        }


        // Hash password
        const hashedPassword = await bcrypt.hash(password, 10);


        // Create user
        const user = await User.create({
            name,
            email,
            password: hashedPassword
        });


        res.status(201).json({

            message: "Account created successfully",

            user: {
                id: user._id,
                name: user.name,
                email: user.email
            }

        });

    }

    catch (error) {

        console.error(error);

        res.status(500).json({
            message: "Server error"
        });

    }

};


// =========================
// LOGIN
// =========================

const login = async (req, res) => {

    try {

        const { email, password } = req.body;


        // Check fields
        if (!email || !password) {

            return res.status(400).json({
                message: "Email and password are required"
            });

        }


        // Find user
        const user = await User.findOne({ email });

        if (!user) {

            return res.status(401).json({
                message: "Invalid email or password"
            });

        }


        // Compare password
        const passwordMatch = await bcrypt.compare(
            password,
            user.password
        );


        if (!passwordMatch) {

            return res.status(401).json({
                message: "Invalid email or password"
            });

        }


        // Create JWT
        const token = jwt.sign(

            {
                userId: user._id,
                email: user.email
            },

            process.env.JWT_SECRET,

            {
                expiresIn: "1h"
            }

        );


        res.status(200).json({

            message: "Login successful",

            token,

            user: {
                id: user._id,
                name: user.name,
                email: user.email
            }

        });

    }

    catch (error) {

        console.error(error);

        res.status(500).json({
            message: "Server error"
        });

    }

};


module.exports = {
    signup,
    login
};