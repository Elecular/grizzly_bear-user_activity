const express = require("express");
const router = express.Router();
const projectStatsController = require("../controllers/project_stats");
const { hasPermission, Permissions } = require("../api/experiments");
const createError = require("http-errors");

/**
 * Gets monthly active users for given project
 * This gives a MAU for all recorded dates
 */
router.get("/projects/:projectId/stats/mau", async (req, res, next) => {
    try {
        if (
            !(await hasPermission(
                req.headers["authorization"],
                Permissions.READ_ALL_PROJECTS,
            ))
        ) {
            throw new createError(403, "Forbidden");
        }
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
 */
router.get("/projects/stats/mau", async (req, res, next) => {
    try {
        if (
            !(await hasPermission(
                req.headers["authorization"],
                Permissions.READ_ALL_PROJECTS,
            ))
        ) {
            throw new createError(403, "Forbidden");
        }
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
        if (
            !(await hasPermission(
                req.headers["authorization"],
                Permissions.READ_ALL_PROJECTS,
            ))
        ) {
            throw new createError(403, "Forbidden");
        }
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
