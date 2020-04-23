const express = require("express");
const router = express.Router();
const experimentStatsController = require("../controllers/experiment_stats");
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

router.get(
    "/experiment-stats/:experimentName/environment/:environment",
    async (req, res, next) => {
        try {
            res.status(201);
            res.json(
                await experimentStatsController.getExperimentStats(
                    req.headers["projectid"],
                    req.params["experimentName"],
                    req.params["environment"],
                ),
            );
        } catch (err) {
            next(err);
        }
    },
);

module.exports = router;
