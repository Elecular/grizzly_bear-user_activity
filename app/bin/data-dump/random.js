/**
 * This script is used for dumping random data to user-activity service
 * Used for stress and batch testing
 *
 * These are the required arguments:
 *
 * projects: comma seperated list of ids
 * segments: comma seperated list of segments
 * userAction: comma seperated list of user actions
 * userVolume: number of users in the data dump
 * sessionVolume: number of sesseions in the data dump
 * activityVolume: number of user activity
 * min-timestamp (optional): Mninimum timestamp of user session (in milliseconds)
 * max-timestamp (optional): Maximum timestmap of user session (in milliseconds)
 *
 * Example usage:
PORT=80 node bin/data-dump/random.js \
    --projects 5e865ed82a2aeb6436f498dc,5e865ed82a2aeb6436f498de,5e865ed82a2aeb6436f498d7,5e865ed82a2aeb6436f498dd \
    --segments one,two,three,four \
    --userVolume 25 \
    --sessionVolume 100 \
    --minTimestamp 79839129600000 \
    --maxTimestamp 79852176000000 \
    --activityVolume 1000 \
    --userActions buy,click,view

 * This script returns an array of user-sessions that were sent as post request to the user-activity service
 */
const argv = require("yargs").argv;
const userSessionController = require("../../controllers/user_session");
const userActivityController = require("../../controllers/user_activity");

module.exports = async () => {
    //Parsing Args
    const projects = argv.projects.split(",");
    const segments = argv.segments.split(",");
    const userActions = argv.userActions.split(",");
    const users = [];
    for (let count = 0; count < argv.userVolume; count++) {
        users.push(
            Math.random()
                .toString(36)
                .substring(2, 25),
        );
    }
    const environments = ["stage", "prod"];

    console.log(`Adding session data`);
    //All data that is sent to user-activity service
    const sessions = [];
    //Sending user sessions to user-activity service
    for (let count = 0; count < argv.sessionVolume; count++) {
        sessions.push(
            await userSessionController.addUserSession(
                getRandomElement(projects),
                {
                    userId: getRandomElement(users),
                    environment: getRandomElement(environments),
                    segments: getRandomElements(segments),
                    timestamp: getRandomTimestamp(
                        argv.minTimestamp,
                        argv.maxTimestamp,
                    ),
                },
            ),
        );
    }

    console.log(`Adding user activity data`);
    //Sending user activity to user-activity service
    for (let count = 0; count < argv.activityVolume; count++) {
        let session = getRandomElement(sessions);
        await userActivityController.addUserActivity(
            session.projectId.toString(),
            {
                sessionId: session._id.toString(),
                userAction: getRandomElement(userActions),
                timestamp: getRandomTimestamp(
                    argv.minTimestamp,
                    argv.maxTimestamp,
                ),
                amount: Math.random() * 50,
            },
        );
    }

    if (argv["exit-on-completion"]) {
        process.exit(0);
    }
};

//Sending usery activity to user-activity servic

const getRandomElements = array => {
    let selectedArray = [];
    let numberOfElements = Math.round(Math.random() * array.length);
    for (let count = 0; count < numberOfElements; count++) {
        selectedArray.push(getRandomElement(array));
    }
    return selectedArray;
};

const getRandomElement = array => {
    return array[Math.floor(Math.random() * array.length)];
};

const getRandomTimestamp = (minTimestamp, maxTimestamp) =>
    Math.floor(Math.random() * (maxTimestamp - minTimestamp + 1)) +
    minTimestamp;
