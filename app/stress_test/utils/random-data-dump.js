/**
 * This script is used for dumping random data to user-activity service
 * Used for stress and batch testing
 *
 * These are the required arguments:
 *
 * projects: comma seperated list of ids
 * segments: comma seperated list of segments
 * userVolume: number of users in the data dump
 * sessionVolume: number of sesseions in the data dump
 * min-timestamp (optional): Mninimum timestamp of user session (in milliseconds)
 * max-timestamp (optional): Maximum timestmap of user session (in milliseconds)
 * time (optional): In how much time should the data-dump be complete? (in milliseconds)
 * host (optinal). Defaults to localhost
 * port (optional): Defaults to 80
 *
 * Example usage:
 * node random-data-dump.js \
 *  --projects 5e865ed82a2aeb6436f498dc,5e865ed82a2aeb6436f498de,5e865ed82a2aeb6436f498d7,5e865ed82a2aeb6436f498dd \
 *  --segments one,two,three,four \
 *  --userVolume 100 \
 *  --sessionVolume 1000 \
 *  --minTimestamp 79839129600000 \
 *  --maxTimestamp 79852176000000
 *
 * This script returns an array of user-sessions that were sent as post request to the user-activity service
 */

const http = require("http");
const argv = require("yargs").argv;

//Parsing Args
const projects = argv.projects.split(",");
const segments = argv.segments.split(",");
const waitInterval = argv.time / argv.sessionVolume || 10;
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
const errors = [];

//Sending requests to user-activity service
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
                host: argv.host || "localhost",
                port: argv.port || 80,
                path: "/user-session",
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    projectId: projectId,
                },
            },
            res => {
                let sentData = {
                    projectId,
                    userId,
                    segments: selectedSegments,
                    ...(timestamp && { timestamp }),
                };
                if (res.statusCode == 201) {
                    data.push(sentData);
                } else {
                    errors.push({
                        body: sentData,
                        statusCode: res.statusCode,
                    });
                }

                if (data.length + errors.length === argv.sessionVolume) {
                    console.log(
                        JSON.stringify({
                            data,
                            errors,
                        }),
                    );
                }
            },
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
