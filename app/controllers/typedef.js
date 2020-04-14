/**
 @typedef {{
    _id: String,
    userId: String,
    projectId: String,
    segments: Array<String>,
    hourNumber: number,
    timestamp: Timestamp
 }} UserSession
 */

/**
 @typedef {{
    _id: String,
    sessionId: String,
    userAction: String,
    timestamp: Timestamp
 }} UserActivity
 */
