const mongo = require("../db/mongodb");
const ObjectID = require("mongodb").ObjectID;
const logger = require("log4js").getLogger();
const createError = require("http-errors");
const md5 = require("md5");

/**
 * Adds a new user session to the database
 *
 * @param {string} projectId
 * @param {{
    userId: String,
    environment: String,
    segments: Array<String>,
    timestamp: [number]
}} userSession
 * @returns {Promise<UserSession>}
 */
module.exports.addUserSession = async (projectId, userSession) => {
    if (!projectId || !ObjectID.isValid(projectId)) {
        throw new createError(400, "Invalid Project Id");
    }
    if (!isSegmentsValid(userSession.segments)) {
        throw new createError(
            400,
            'Cannot add "all" as segments since it is a reserved key word',
        );
    }
    if (!userSession.environment)
        throw new createError(400, "Please specify an environment");

    const db = await mongo.connect();

    try {
        const timestamp = userSession.timestamp || Date.now();
        const response = await db.collection("user_session").insertOne({
            userId: md5(userSession.userId),
            environment: userSession.environment,
            segments: [...new Set(userSession.segments), "all"],
            projectId: ObjectID(projectId),
            hourNumber: Math.floor(timestamp / (3600 * 1000)),
            timestamp: timestamp,
        });
        return response.ops[0];
    } catch (err) {
        logger.error(err);
        throw new createError(err.code == 121 ? 400 : 500);
    }
};

/**
 *
 * @param {Array<String>} segments
 */
const isSegmentsValid = segments =>
    !segments.map(s => s.toLowerCase()).includes("all");

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
