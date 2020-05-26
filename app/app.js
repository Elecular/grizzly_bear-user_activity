const express = require("express");
const cookieParser = require("cookie-parser");
const logger = require("morgan");
const log4js = require("log4js").getLogger();
const httpHandler = require("./middleware/httpErrorHandler");
const indexRouter = require("./routes/index");
const cors = require("cors");

const app = express();

app.use(cors());
app.use(logger("dev"));
app.use(express.json());
app.use(express.urlencoded({ extended: false }));
app.use(cookieParser());

app.use("/", indexRouter);
app.use(httpHandler);

log4js.level = process.env.LOG_LEVEL || "debug";

module.exports = app;
