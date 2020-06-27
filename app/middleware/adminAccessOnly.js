const createError = require("http-errors");
const { hasPermission, Permissions } = require("../api/experiments");

const adminAccessOnly = async (req, res, next) => {
    if (
        !(await hasPermission(
            req.headers["authorization"],
            Permissions.ADMIN.READ.PROJECTS,
        ))
    ) {
        next(createError(403, "Forbidden"));
        return;
    }
    next();
};

module.exports = adminAccessOnly;
