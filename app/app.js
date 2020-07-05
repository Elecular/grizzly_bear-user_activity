const express = require("express");
const cookieParser = require("cookie-parser");
const logger = require("morgan");
const log4js = require("log4js").getLogger();
const httpHandler = require("./middleware/httpErrorHandler");
const indexRouter = require("./routes/index");
const adminRouter = require("./routes/admin");
const cors = require("cors");
const rateLimit = require("express-rate-limit");
const adminAccessOnly = require("./middleware/adminAccessOnly");

const app = express();
const limiter = rateLimit({
    windowMs: 60 * 60 * 1000, // 15 minutes
    max: 150, // limit each IP to 150 requests per windowMs,
    message:
        "Too many requests. You can log upto 200 events every hour. Please contact info@elecular.com if you need to raise this limit.",
});

app.use(limiter);
app.use(cors());
app.use(logger("dev"));
app.use(express.json());
app.use(express.urlencoded({ extended: false }));
app.use(cookieParser());

app.use("/", indexRouter);
app.use("/admin/", adminAccessOnly);
app.use("/admin/", adminRouter);
app.use(httpHandler);

log4js.level = process.env.LOG_LEVEL || "debug";

module.exports = app;
