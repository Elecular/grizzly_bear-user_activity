const mongo = require("../db/mongodb");
const ObjectID = require("mongodb").ObjectID;
const createError = require("http-errors");
const assignKeyToObject = require("../util/object_utils").assignKeyToObject;
const getValueFromObject = require("../util/object_utils").getValueFromObject;

/**
 *
 * @param {String} projectId
 * @param {String} experimentName
 * @param {String} environment
 */
module.exports.getExperimentStats = async (
    projectId,
    experimentName,
    environment,
) => {
    if (
        !experimentName ||
        !environment ||
        !projectId ||
        !ObjectID.isValid(projectId)
    )
        throw new createError(
            400,
            "Invalid projectId, experimentName and/or environment",
        );

    const experimentSessionStats = await getExperimentSessionStats(
        projectId,
        experimentName,
        environment,
    );
    const experimentActivityStats = await getExperimentActivityStats(
        projectId,
        experimentName,
        environment,
    );

    const stats = {
        projectId,
        experimentName,
        environment,
        variations: {},
    };
    for (const experimentSessionStat of experimentSessionStats) {
        const { variation, segment } = experimentSessionStat._id;
        const count = experimentSessionStat.count;

        assignKeyToObject(
            stats.variations,
            [variation, "segments", segment, "sessions"],
            count,
        );
        Object.assign(
            stats.variations[variation]["segments"][segment],
            getValueFromObject(
                experimentActivityStats,
                [variation, segment],
                {},
            ),
        );
    }

    return stats;
};

/**
 *
 * @param {String} projectId
 * @param {String} experimentName
 * @param {String} environment
 */
const getExperimentSessionStats = async (
    projectId,
    experimentName,
    environment,
) => {
    const db = await mongo.connect();

    return await db
        .collection("experiment_session_stats")
        .aggregate([
            {
                $match: {
                    projectId: projectId,
                    environment: environment,
                    experimentName: experimentName,
                },
            },
            {
                $group: {
                    _id: {
                        projectId: "$projectId",
                        environment: "$environment",
                        experimentName: "$experimentName",
                        variation: "$variation",
                        segment: "$segment",
                    },
                    count: {
                        $sum: "$count",
                    },
                },
            },
        ])
        .toArray();
};

/**
 *
 * @param {String} projectId
 * @param {String} experimentName
 * @param {String} environment
 */
const getExperimentActivityStats = async (
    projectId,
    experimentName,
    environment,
) => {
    const db = await mongo.connect();
    let results = {};
    const activityStats = await db
        .collection("experiment_activity_stats")
        .aggregate([
            {
                $match: {
                    projectId: projectId,
                    environment: environment,
                    experimentName: experimentName,
                },
            },
            {
                $group: {
                    _id: {
                        projectId: "$projectId",
                        environment: "$environment",
                        experimentName: "$experimentName",
                        variation: "$variation",
                        segment: "$segment",
                        userAction: "$userAction",
                    },
                    count: {
                        $sum: "$count",
                    },
                    amount: {
                        $sum: "$amount",
                    },
                },
            },
        ])
        .toArray();

    activityStats.forEach(doc => {
        const { variation, segment, userAction } = doc._id;
        const path = [variation, segment, userAction];
        assignKeyToObject(results, path, {
            count: doc.count,
            amount: doc.amount,
        });
    });
    return results;
};
