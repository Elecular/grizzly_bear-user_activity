const express = require("express");
const router = express.Router();
const experimentStatsController = require("../controllers/experiment_stats");
const userSessionController = require("../controllers/user_session");
const userActivityController = require("../controllers/user_activity");
const validateOwner = require("../api/experiments").validateOwner;
const createError = require("http-errors");
const { checkSchema, validationResult } = require("express-validator");
const getBatchRuns = require("../controllers/batch_runs").getBatchRuns;

/* GET home page. */
router.get("/", function(req, res) {
    res.send("Welcome to the User Activity Service API!");
});

router.get("/status", function(req, res) {
    res.status(200);
    res.json();
});

router.get(
    "/batch/status",
    [
        checkSchema({
            batchName: {
                isEmpty: {
                    negated: true,
                },
                isString: true,
                in: "query",
            },
        }),
    ],
    async (req, res, next) => {
        const errors = validationResult(req);
        if (!errors.isEmpty()) {
            return res.status(400).json({ errors: errors.array() });
        }

        try {
            res.json(await getBatchRuns(req.query.batchName, 7));
            res.status(200);
        } catch (err) {
            next(err);
        }
    },
);

/**
 * Creates a new user session
 */
router.post(
    "/projects/:projectId/user-session",
    [
        checkSchema({
            userId: {
                isString: true,
                isEmpty: {
                    negated: true,
                },
            },
            environment: {
                isString: true,
                isEmpty: {
                    negated: true,
                },
            },
            segments: {
                isArray: true,
            },
            timestamp: {
                optional: true,
                isNumeric: true,
                toInt: true,
            },
        }),
    ],
    async (req, res, next) => {
        const errors = validationResult(req);
        if (!errors.isEmpty()) {
            return res.status(400).json({ errors: errors.array() });
        }

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
    },
);

/**
 * Creates a new user activity
 */
router.post(
    "/projects/:projectId/user-activity",
    [
        checkSchema({
            sessionId: {
                isString: true,
            },
            userAction: {
                isString: true,
                isEmpty: {
                    negated: true,
                },
            },
            amount: {
                isFloat: true,
                toFloat: true,
            },
            timestamp: {
                optional: true,
                isNumeric: true,
                toInt: true,
            },
        }),
    ],
    async (req, res, next) => {
        const errors = validationResult(req);
        if (!errors.isEmpty()) {
            return res.status(400).json({ errors: errors.array() });
        }

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
    },
);

/**
 * Gets experiment stats of given project, experiment and environment
 */
router.get(
    "/projects/:projectId/experiments/:experimentName/environments/:environment/stats",
    async (req, res, next) => {
        try {
            if (
                !(await validateOwner(
                    req.headers["authorization"],
                    req.params["projectId"],
                ))
            ) {
                throw new createError(403, "Forbidden");
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
