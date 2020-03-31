let assert = require("assert");
let mongo = require("../../db/mongodb");
let ObjectID = require("mongodb").ObjectID;
let userSessionController = require("../../controllers/user_session");
let userActivityController = require("../../controllers/user_activity");

let projectId = ObjectID().toString();
let session = null;

beforeEach(async () => {
    let db = await mongo.connect();
    let collections = await db.listCollections().toArray();
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
