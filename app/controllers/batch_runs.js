const mongo = require("../db/mongodb");

/**
 * Gets all batch runs in the past n days
 */
module.exports.getBatchRuns = async (batchName, numberOfDays) => {
    const db = await mongo.connect();
    return await db
        .collection("batch_run")
        .find({
            batchName,
            startTime: {
                $gt: Date.now() - 3600 * 1000 * 24 * numberOfDays,
            },
        })
        .sort({
            startTime: -1,
        })
        .toArray();
};
