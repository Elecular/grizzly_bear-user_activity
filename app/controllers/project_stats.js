const mongo = require("../db/mongodb");

/*
 * Gets monthly active users for given project
 */
module.exports.getMauOfProject = async projectId => {
    const db = await mongo.connect();
    const mauStats = await db.collection("project_mau").find({
        projectId,
    });
    return await mauStats.toArray();
};

/*
 * Gets monthly active users for all projects at given date
 */
module.exports.getMauForAllProjects = async date => {
    const db = await mongo.connect();
    const mauStats = await db.collection("project_mau").find({
        date,
    });
    return await mauStats.toArray();
};

/*
 * Gets project performance stats
 */
module.exports.getPerformanceStats = async projectId => {
    const db = await mongo.connect();
    const performance = await db.collection("project_performance").find({
        projectId,
    });
    return await performance.toArray();
};
