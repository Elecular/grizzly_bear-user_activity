let express = require("express");
let cookieParser = require("cookie-parser");
let logger = require("morgan");
let log4js = require("log4js").getLogger();

let indexRouter = require("./routes/index");

let app = express();

app.use(logger("dev"));
app.use(express.json());
app.use(express.urlencoded({ extended: false }));
app.use(cookieParser());

app.use("/", indexRouter);

log4js.level = process.env.LOG_LEVEL || "debug";

module.exports = app;
