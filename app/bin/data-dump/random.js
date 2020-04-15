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

const http = require("http");
const argv = require("yargs").argv;

//Parsing Args
const projects = argv.projects.split(",");
const segments = argv.segments.split(",");
const userActions = argv.userActions.split(",");
const waitInterval = 10;
const users = [];
for (let count = 0; count < argv.userVolume; count++) {
    users.push(
        Math.random()
            .toString(36)
            .substring(2, 25),
    );
}

//All data that is sent to user-activity service
const data = [];
const sessionIds = [];
//Sending user sessions to user-activity service
for (let count = 0; count < argv.sessionVolume; count++) {
    setTimeout(() => {
        let projectId = getRandomElement(projects);
        let userId = getRandomElement(users);
        let selectedSegments = getRandomElements(segments);
        let timestamp = undefined;
        if (argv.minTimestamp && argv.maxTimestamp)
            timestamp =
                Math.floor(
                    Math.random() * (argv.maxTimestamp - argv.minTimestamp + 1),
                ) + argv.minTimestamp;

        let req = http.request(
            {
                host: "localhost",
                port: 80,
                path: "/user-session",
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    projectId: projectId,
                },
            },
            handleUserSessionResponse,
        );

        req.write(
            JSON.stringify({
                userId: userId,
                segments: selectedSegments,
                ...(timestamp && { timestamp }),
            }),
        );
        req.end();
    }, count * waitInterval);
}

const handleUserSessionResponse = res => {
    if (res.statusCode == 201) {
        let resData = "";
        res.on("data", chunk => (resData += chunk));
        res.on("end", () => {
            const addedSession = JSON.parse(resData);
            sessionIds.push({
                sessionId: addedSession._id,
                projectId: addedSession.projectId,
            });
            data.push(addedSession);
            if (data.length == argv.sessionVolume) sendUserActivity();
        });
    } else {
        console.error(`Error while adding user session ${res.statusCode}`);
    }
};

const sendUserActivity = () => {
    //Sending user activity to user-activity service
    for (let count = 0; count < argv.activityVolume; count++) {
        let session = getRandomElement(sessionIds);
        let userAction = getRandomElement(userActions);
        setTimeout(() => {
            let req = http.request(
                {
                    host: argv.host || "localhost",
                    port: process.env.PORT || 3000,
                    path: "/user-activity",
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                        projectId: session.projectId,
                    },
                },
                res => {
                    if (res.statusCode != 201)
                        console.error(
                            `Error while adding user activity ${res.statusCode}`,
                        );
                },
            );

            req.write(
                JSON.stringify({
                    sessionId: session.sessionId,
                    userAction: userAction,
                }),
            );
            req.end();
        }, count * waitInterval);
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
