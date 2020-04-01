const mongo = require("../db/mongodb");
const ObjectID = require("mongodb").ObjectID;
const Timestamp = require("mongodb").Timestamp;
const logger = require("log4js").getLogger();
const createError = require("http-errors");

/**
 * Adds a new user session to the database
 *
 * @param {string} projectId
 * @param {Object} userSession
 * @returns {Promise<Object>}
 */
module.exports.addUserSession = async (projectId, userSession) => {
    if (!projectId || !ObjectID.isValid(projectId)) {
        throw new createError(400, "Invalid Project Id");
    }

    const db = await mongo.connect();

    try {
        const timestamp = Date.now();
        const response = await db.collection("user_session").insertOne({
            ...userSession,
            projectId: ObjectID(projectId),
            hourNumber: Math.floor(timestamp / (3600 * 1000)),
            timestamp: Timestamp.fromNumber(timestamp),
        });
        return response.ops[0];
    } catch (err) {
        logger.error(err);
        throw new createError(err.code == 121 ? 400 : 500);
    }
};

/**
 * Checks if the existing sessionId exists under given project id
 *
 * @param {string} projectId
 * @param {string} sessionId
 * @returns {boolean}
 */
module.exports.isValidSession = async (projectId, sessionId) => {
    if (!projectId || !sessionId || !ObjectID.isValid(sessionId)) {
        return false;
    }

    const db = await mongo.connect();

    try {
        const session = await db.collection("user_session").findOne({
            _id: ObjectID(sessionId),
        });
        return session !== null && session.projectId.toString() === projectId;
    } catch (err) {
        logger.error(err);
        throw new createError(500);
    }
};
