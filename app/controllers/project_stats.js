const mongo = require("../db/mongodb");

/*
 * Gets monthly active users with given filter.
 * Example filter: {projectId: 507f1f77bcf86cd799439011}
 */
module.exports.getMau = async filter => {
    const db = await mongo.connect();
    const mauStats = await db.collection("project_mau").find(filter);
    return await mauStats.toArray();
};
