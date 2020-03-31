let assert = require("assert");
let mongo = require("../../db/mongodb");
let ObjectID = require("mongodb").ObjectID;
let userSessionController = require("../../controllers/user_session");

beforeEach(async () => {
    let db = await mongo.connect();
    let collections = await db.listCollections().toArray();
    for (let collection of collections) {
        await db.collection(collection.name).deleteMany({});
    }
});

afterAll(async () => {
    await mongo.disconnect();
});

describe("UserSession Controller", () => {
    it("can add new session", async () => {
        let projectId = ObjectID().toString();
        const res = await userSessionController.addUserSession(
            projectId,
            mockUserSession("testUser"),
        );
        assert.equal(res.projectId, projectId);
        assert.equal(res.userId, "testUser");
        assert.ok(Math.abs(res.timestamp - Date.now()) < 10000);
        assert.ok(res.hourNumber);
    });

    it("can validate a session", async () => {
        const projectId = ObjectID().toString();
        const res = await userSessionController.addUserSession(
            projectId,
            mockUserSession("testUser"),
        );
        const sessionId = res._id.toString();

        assert.ok(
            await userSessionController.isValidSession(projectId, sessionId),
        );
        assert.ok(
            !(await userSessionController.isValidSession(
                ObjectID().toString(),
                sessionId,
            )),
        );
        assert.ok(
            !(await userSessionController.isValidSession(
                projectId,
                ObjectID().toString(),
            )),
        );
    });
});

const mockUserSession = userId => {
    return {
        userId,
        segments: ["male", "female"],
    };
};
