const assert = require("assert");
const mongo = require("../../db/mongodb");
const ObjectID = require("mongodb").ObjectID;
const userSessionController = require("../../controllers/user_session");
const userActivityController = require("../../controllers/user_activity");

const projectId = ObjectID().toString();
let session = null;

beforeEach(async () => {
    const db = await mongo.connect();
    const collections = await db.listCollections().toArray();
    for (let collection of collections) {
        await db.collection(collection.name).deleteMany({});
    }
    session = await userSessionController.addUserSession(
        projectId,
        mockUserSession("testUser"),
    );
});

afterAll(async () => {
    await mongo.disconnect();
});

describe("UserActivity Controller", () => {
    it("can add user activity", async () => {
        const res = await userActivityController.addUserActivity(projectId, {
            sessionId: session._id,
            userAction: "testAction",
        });
        assert.ok(res);
    });

    it("cannot add user activity to invalid session", async () => {
        try {
            await userActivityController.addUserActivity(projectId, {
                sessionId: ObjectID().toString(),
                userAction: "testAction",
            });
            assert.fail();
        } catch (err) {
            assert.equal(
                err.message,
                "Session Id is not present in given Project Id",
            );
        }
    });
});

const mockUserSession = userId => {
    return {
        userId,
        segments: ["male", "female"],
    };
};
