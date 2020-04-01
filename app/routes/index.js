const express = require("express");
const router = express.Router();
const userSessionController = require("../controllers/user_session");
const userActivityController = require("../controllers/user_activity");

/* GET home page. */
router.get("/", function(req, res) {
    res.send("Welcome to the User Activity Service API!");
});

router.get("/status", function(req, res) {
    res.status(200);
    res.json();
});

router.post("/user-session", async (req, res, next) => {
    try {
        res.status(201);
        res.json(
            await userSessionController.addUserSession(
                req.headers["projectid"],
                req.body,
            ),
        );
    } catch (err) {
        next(err);
    }
});

router.post("/user-activity", async (req, res, next) => {
    try {
        res.status(201);
        res.json(
            await userActivityController.addUserActivity(
                req.headers["projectid"],
                req.body,
            ),
        );
    } catch (err) {
        next(err);
    }
});

module.exports = router;
