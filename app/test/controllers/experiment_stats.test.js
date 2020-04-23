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
    it("can aggregate experiment stats for both variations", async () => {
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
        assert.ok();
        console.log(JSON.stringify(res, null, 4));
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
