const fetch = require("node-fetch");
const host = process.env["PUBLIC_EXPERIMENTS_SERVICE_HOST"];
const port = process.env["PUBLIC_EXPERIMENTS_SERVICE_PORT"];
const createError = require("http-errors");
const ObjectID = require("mongodb").ObjectID;

if (!host || !port) {
    throw new Error(
        "PUBLIC_EXPERIMENTS_SERVICE_HOST and PUBLIC_EXPERIMENTS_SERVICE_PORT environment variables are not passed",
    );
}

/**
 * Validates if the given ownerid is the actual owner of the given project
 * @param {String} ownerId
 * @param {String} projectId
 * Returns 200 if everything is ok
 * Returns 401 if user is not authorized
 * Returns 403 if user is forbidden
 */
module.exports.validateOwner = async (authToken, projectId) => {
    if (!projectId || !ObjectID.isValid(projectId))
        throw new createError(400, "Invalid project id");

    const res = await fetch(`http://${host}:${port}/projects`, {
        method: "GET",
        headers: {
            "Content-Type": "application/json",
            Authorization: authToken,
        },
    });
    if (res.status === 200) {
        const projects = await res.json();
        return projects.some(project => project._id === projectId);
    } else {
        throw new createError(401, "Not Authorized");
    }
};
