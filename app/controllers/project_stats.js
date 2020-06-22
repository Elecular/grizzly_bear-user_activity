const createError = require("http-errors");
const mongo = require("../db/mongodb");

/*
 * Gets monthly active users
 */
module.exports.getMau = async projectId => {
    if (!projectId) {
        throw new createError(400, "Invalid projectId");
    }

    const db = await mongo.connect();
    const mauStats = await db.collection("project_mau").find({
        projectId,
    });

    return await mauStats.toArray();
};
