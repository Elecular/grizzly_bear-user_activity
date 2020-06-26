const assert = require("assert");
const mongo = require("../../db/mongodb");
const ObjectID = require("mongodb").ObjectID;
const projectStatsController = require("../../controllers/project_stats");

beforeEach(async () => {
    const db = await mongo.connect();
    const collections = await db.listCollections().toArray();
    for (let collection of collections) {
        await db.collection(collection.name).deleteMany({});
    }
});

afterAll(async () => {
    await mongo.disconnect();
});

const projectId = ObjectID().toString();
const projectId2 = ObjectID().toString();

describe("Project Stats Controller", () => {
    it("can get MAU for all projects on given date", async () => {
        const db = await mongo.connect();
        await db
            .collection("project_mau")
            .insertMany([
                mockProjectMauStats(projectId, "01-01-4500", 45),
                mockProjectMauStats(projectId, "02-01-4500", 20),
                mockProjectMauStats(projectId2, "01-01-4500", 65),
            ]);
        const res = await projectStatsController.getMauForAllProjects(
            "01-01-4500",
        );
        assert.equal(res.length, 2);
        assert.equal(res[0].date, "01-01-4500");
        assert.equal(res[1].date, "01-01-4500");
    });

    it("cannot get MAU for all projects when date does not exist", async () => {
        const db = await mongo.connect();
        await db
            .collection("project_mau")
            .insertMany([
                mockProjectMauStats(projectId, "01-01-4500", 45),
                mockProjectMauStats(projectId, "02-01-4500", 20),
                mockProjectMauStats(projectId2, "01-01-4500", 65),
            ]);
        const res = await projectStatsController.getMauForAllProjects(
            "01-01-4600",
        );
        assert.equal(res.length, 0);
    });

    it("can get MAU for a specific project", async () => {
        const db = await mongo.connect();
        await db
            .collection("project_mau")
            .insertMany([
                mockProjectMauStats(projectId, "01-01-4500", 45),
                mockProjectMauStats(projectId, "02-01-4500", 45),
                mockProjectMauStats(projectId2, "01-01-4500", 45),
            ]);
        const res = await projectStatsController.getMauOfProject(projectId);
        assert.equal(res.length, 2);
        assert.equal(res[0].projectId, projectId);
        assert.equal(res[1].projectId, projectId);
    });

    it("cannot get MAU for a specific project when project does not exist", async () => {
        const db = await mongo.connect();
        await db
            .collection("project_mau")
            .insertMany([
                mockProjectMauStats(projectId, "01-01-4500", 45),
                mockProjectMauStats(projectId, "02-01-4500", 45),
            ]);
        const res = await projectStatsController.getMauOfProject(projectId2);
        assert.equal(res.length, 0);
    });

    it("can get performacne stats for a specific project", async () => {
        const db = await mongo.connect();
        await db
            .collection("project_performance")
            .insertMany([
                mockProjectPerformanceStats(
                    projectId,
                    "buy",
                    "01-01-4500",
                    45,
                    60,
                ),
                mockProjectPerformanceStats(
                    projectId,
                    "sell",
                    "01-01-4500",
                    45,
                    60,
                ),
                mockProjectPerformanceStats(
                    projectId2,
                    "buy",
                    "01-01-4500",
                    45,
                    60,
                ),
            ]);
        const res = await projectStatsController.getPerformanceStats(projectId);
        assert.equal(res.length, 2);
        assert.equal(res[0].projectId, projectId);
        assert.equal(res[1].projectId, projectId);
    });
});

const mockProjectMauStats = (projectId, date, count) => ({
    projectId,
    date,
    count,
});

const mockProjectPerformanceStats = (
    projectId,
    userAction,
    date,
    numberOfSessions,
    amount,
) => ({
    projectId,
    userAction,
    date,
    numberOfSessions,
    amount,
});
