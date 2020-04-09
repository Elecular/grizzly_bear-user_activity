const assert = require("assert");
const mongo = require("../../db/mongodb");
const ObjectID = require("mongodb").ObjectID;
const userSessionController = require("../../controllers/user_session");

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

describe("UserSession Controller", () => {
    it("can add new session", async () => {
        const projectId = ObjectID().toString();
        const res = await userSessionController.addUserSession(
            projectId,
            mockUserSession("testUser"),
        );
        assert.equal(res.projectId, projectId);
        assert.equal(res.userId, "testUser");
        assert.ok(Math.abs(res.timestamp - Date.now()) < 10000);
        assert.ok(res.hourNumber);
    });

    it("can add new session with duplicate segments", async () => {
        const projectId = ObjectID().toString();
        const res = await userSessionController.addUserSession(projectId, {
            ...mockUserSession("testUser"),
            segments: ["test", "test1", "test1"],
        });
        assert.deepEqual(res.segments, ["test", "test1", "all"]);
    });

    it("can add new session with no segments", async () => {
        const projectId = ObjectID().toString();
        const res = await userSessionController.addUserSession(projectId, {
            ...mockUserSession("testUser"),
            segments: [],
        });
        assert.deepEqual(res.segments, ["all"]);
    });

    it("cannot add new session with 'all' segment", async () => {
        const projectId = ObjectID().toString();
        try {
            await userSessionController.addUserSession(projectId, {
                ...mockUserSession("testUser"),
                segments: ["test", "test1", "test1", "aLl"],
            });
            assert.fail();
        } catch (err) {
            console.log(err);
            assert.equal(
                err.message,
                'Cannot add "all" as segments since it is a reserved key word',
            );
        }
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
