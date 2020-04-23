const fetch = require("node-fetch");
const host = process.env["PRIVATE_EXPERIMENTS_SERVICE_HOST"];
const port = process.env["PRIVATE_EXPERIMENTS_SERVICE_PORT"];
const createError = require("http-errors");
const ObjectID = require("mongodb").ObjectID;

/**
 * Validates if the given ownerid is the actual owner of the given project
 * @param {String} ownerId
 * @param {String} projectId
 *
 */
module.exports.validateOwner = async (ownerId, projectId) => {
    if (!ownerId || !projectId || !ObjectID.isValid(projectId))
        throw new createError(400, "Invalid owner or project id");
    try {
        const res = await fetch(
            `http://${host}:${port}/project/${projectId}/validateOwner/${ownerId}`,
        );
        const json = await res.json();
        return json.isOwner;
    } catch (err) {
        console.log(err);
        throw new createError(500, "Could not validate owner");
    }
};
