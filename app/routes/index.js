const express = require("express");
const router = express.Router();
const experimentStatsController = require("../controllers/experiment_stats");
const userSessionController = require("../controllers/user_session");
const userActivityController = require("../controllers/user_activity");
const validateOwner = require("../api/experiments").validateOwner;
const createError = require("http-errors");

/* GET home page. */
router.get("/", function(req, res) {
    res.send("Welcome to the User Activity Service API!");
});

router.get("/status", function(req, res) {
    res.status(200);
    res.json();
});

/**
 * Creates a new user session
 */
router.post("/:projectId/user-session", async (req, res, next) => {
    try {
        res.status(201);
        res.json(
            await userSessionController.addUserSession(
                req.params["projectId"],
                req.body,
            ),
        );
    } catch (err) {
        next(err);
    }
});

/**
 * Creates a new user activity
 */
router.post("/:projectId/user-activity", async (req, res, next) => {
    try {
        res.status(201);
        res.json(
            await userActivityController.addUserActivity(
                req.params["projectId"],
                req.body,
            ),
        );
    } catch (err) {
        next(err);
    }
});

/**
 * Gets experiment stats of given project, experiment and environment
 */
router.get(
    "/:projectId/experiment-stats/:experimentName/environment/:environment",
    async (req, res, next) => {
        try {
            if (
                !(await validateOwner(
                    req.headers["ownerid"],
                    req.params["projectId"],
                ))
            ) {
                throw new createError(
                    401,
                    "Not Authorized to get experiment stats",
                );
            }
            res.status(200);
            res.json(
                await experimentStatsController.getExperimentStats(
                    req.params["projectId"],
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
