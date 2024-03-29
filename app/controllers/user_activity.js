const mongo = require("../db/mongodb");
const ObjectID = require("mongodb").ObjectID;
const logger = require("log4js").getLogger();
const createError = require("http-errors");
const userSessionController = require("../controllers/user_session");
/**
 * Adds a new user activity to the database
 *
 * @param {string} projectId
 * @param {{
    sessionId: String,
    userAction: String,
    amount: number,
    timestamp: [number]
 }} userActivity
 * @param {number} timestamp 
 * @returns {Promise<UserActivity>}
 */
module.exports.addUserActivity = async (projectId, userActivity) => {
    const db = await mongo.connect();

    if (!projectId || !ObjectID.isValid(projectId)) {
        throw new createError(400, "Invalid projectId");
    }

    if (!userActivity.sessionId || !ObjectID.isValid(userActivity.sessionId)) {
        throw new createError(400, "Invalid sessionId");
    }

    if (
        !(await userSessionController.isValidSession(
            projectId,
            userActivity.sessionId,
        ))
    ) {
        throw new createError(
            404,
            "Session Id is not present in given Project Id",
        );
    }

    if (!userActivity.amount)
        throw new createError(400, "Please specify amount");

    try {
        const response = await db.collection("user_activity").insertOne({
            ...userActivity,
            sessionId: ObjectID(userActivity.sessionId),
            timestamp: userActivity.timestamp || Date.now(),
        });
        return response.ops[0];
    } catch (err) {
        logger.error(err);
        throw new createError(err.code == 121 ? 400 : 500);
    }
};
