const assert = require("assert");
const mongo = require("../../db/mongodb");
const ObjectID = require("mongodb").ObjectID;
const experimentStatsController = require("../../controllers/experiment_stats");

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

describe("Experiment Stats Controller", () => {
    it("can aggregate experiment stats without any user session or user activity", async () => {
        const res = await experimentStatsController.getExperimentStats(
            projectId,
            "exp1",
            "prod",
        );
        assert.equal(Object.keys(res.variations).length, 0);
    });

    it("can aggregate experiment stats without any user activity", async () => {
        const db = await mongo.connect();
        await db
            .collection("experiment_session_stats")
            .insertMany([
                mockSessionStat(projectId, "prod", "exp1", "variation1", 45, 3),
                mockSessionStat(projectId, "prod", "exp1", "variation1", 57, 6),
                mockSessionStat(
                    projectId,
                    "prod",
                    "exp1",
                    "variation1",
                    45,
                    2,
                    "one",
                ),
                mockSessionStat(
                    projectId,
                    "prod",
                    "exp1",
                    "variation1",
                    57,
                    1,
                    "one",
                ),
                mockSessionStat(projectId, "prod", "exp1", "variation2", 24, 5),
                mockSessionStat(projectId, "prod", "exp1", "variation2", 28, 1),
                mockSessionStat(
                    projectId,
                    "stage",
                    "exp1",
                    "variation2",
                    24,
                    7,
                ),
                mockSessionStat(
                    projectId,
                    "stage",
                    "exp1",
                    "variation2",
                    28,
                    1,
                ),
            ]);

        const res = await experimentStatsController.getExperimentStats(
            projectId,
            "exp1",
            "prod",
        );
        assert.ok(
            res.variations["variation1"]["segments"]["all"]["sessions"] === 9,
        );
        assert.ok(
            res.variations["variation1"]["segments"]["one"]["sessions"] === 3,
        );
        assert.ok(
            res.variations["variation2"]["segments"]["all"]["sessions"] === 6,
        );
    });

    it("can aggregate experiment stats with user session and user activity", async () => {
        const db = await mongo.connect();
        await db
            .collection("experiment_session_stats")
            .insertMany([
                mockSessionStat(projectId, "prod", "exp1", "variation1", 45, 3),
                mockSessionStat(projectId, "prod", "exp1", "variation1", 57, 6),
                mockSessionStat(
                    projectId,
                    "prod",
                    "exp1",
                    "variation1",
                    45,
                    2,
                    "one",
                ),
                mockSessionStat(
                    projectId,
                    "prod",
                    "exp1",
                    "variation1",
                    57,
                    1,
                    "one",
                ),
                mockSessionStat(projectId, "prod", "exp1", "variation2", 24, 5),
                mockSessionStat(projectId, "prod", "exp1", "variation2", 28, 1),
                mockSessionStat(
                    projectId,
                    "stage",
                    "exp1",
                    "variation2",
                    24,
                    7,
                ),
                mockSessionStat(
                    projectId,
                    "stage",
                    "exp1",
                    "variation2",
                    28,
                    1,
                ),
            ]);

        await db
            .collection("experiment_activity_stats")
            .insertMany([
                mockActivityStat(
                    projectId,
                    "prod",
                    "exp1",
                    "variation1",
                    "buy",
                    45,
                    3,
                    15.5,
                ),
                mockActivityStat(
                    projectId,
                    "prod",
                    "exp1",
                    "variation1",
                    "buy",
                    12,
                    2,
                    25,
                ),
                mockActivityStat(
                    projectId,
                    "prod",
                    "exp1",
                    "variation1",
                    "click",
                    23,
                    4,
                    5.7,
                ),
                mockActivityStat(
                    projectId,
                    "stage",
                    "exp1",
                    "variation1",
                    "click",
                    23,
                    4,
                    3,
                ),
                mockActivityStat(
                    projectId,
                    "prod",
                    "exp1",
                    "variation2",
                    "view",
                    45,
                    1,
                    0.6,
                ),
                mockActivityStat(
                    projectId,
                    "prod",
                    "exp1",
                    "variation2",
                    "view",
                    12,
                    6,
                    1.8,
                ),
            ]);

        const res = await experimentStatsController.getExperimentStats(
            projectId,
            "exp1",
            "prod",
        );

        assert.ok(
            res.variations["variation1"]["segments"]["all"]["sessions"] === 9,
        );
        assert.ok(
            res.variations["variation1"]["segments"]["all"]["click"][
                "count"
            ] === 4,
        );
        assert.ok(
            res.variations["variation1"]["segments"]["all"]["click"][
                "amount"
            ] === 5.7,
        );
        assert.ok(
            res.variations["variation1"]["segments"]["all"]["buy"]["count"] ===
                5,
        );
        assert.ok(
            res.variations["variation1"]["segments"]["all"]["buy"]["amount"] ===
                40.5,
        );
        assert.ok(
            res.variations["variation1"]["segments"]["all"]["view"] ===
                undefined,
        );

        assert.ok(
            res.variations["variation1"]["segments"]["one"]["sessions"] === 3,
        );
        assert.ok(
            res.variations["variation1"]["segments"]["one"]["click"] ===
                undefined,
        );
        assert.ok(
            res.variations["variation1"]["segments"]["one"]["buy"] ===
                undefined,
        );

        assert.ok(
            res.variations["variation2"]["segments"]["all"]["sessions"] === 6,
        );
        assert.ok(
            res.variations["variation2"]["segments"]["all"]["view"]["count"] ===
                7,
        );
        assert.ok(
            res.variations["variation2"]["segments"]["all"]["view"][
                "amount"
            ] === 2.4,
        );
    });
});

const mockSessionStat = (
    projectId,
    environment,
    experimentName,
    variation,
    hourNumber,
    count,
    segment = "all",
) => ({
    projectId,
    environment,
    experimentName,
    variation,
    segment,
    hourNumber,
    dayNumber: Math.floor(hourNumber / 24),
    count,
});

const mockActivityStat = (
    projectId,
    environment,
    experimentName,
    variation,
    userAction,
    hourNumber,
    count,
    amount,
    segment = "all",
) => ({
    projectId,
    environment,
    experimentName,
    variation,
    segment,
    hourNumber,
    userAction,
    dayNumber: Math.floor(hourNumber / 24),
    count,
    amount,
});
