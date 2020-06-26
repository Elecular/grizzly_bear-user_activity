const express = require("express");
const router = express.Router();
const projectStatsController = require("../controllers/project_stats");

/**
 * Gets monthly active users for given project
 * This gives a MAU for all recorded dates
 */
router.get("/projects/:projectId/stats/mau", async (req, res, next) => {
    console.log("Hello");
    try {
        res.status(200);
        res.json(
            await projectStatsController.getMauOfProject(
                req.params["projectId"],
            ),
        );
    } catch (err) {
        next(err);
    }
});

/**
 * Gets monthly active users for all project at given date
 * Date must be in format dd-MM-yyyy
 */
router.get("/projects/stats/mau", async (req, res, next) => {
    try {
        res.status(200);
        res.json(
            await projectStatsController.getMauForAllProjects(
                req.query["date"],
            ),
        );
    } catch (err) {
        next(err);
    }
});

/**
 * Gets performance for given project
 */
router.get("/projects/:projectId/stats/performance", async (req, res, next) => {
    try {
        res.status(200);
        res.json(
            await projectStatsController.getPerformanceStats(
                req.params["projectId"],
            ),
        );
    } catch (err) {
        next(err);
    }
});

module.exports = router;
