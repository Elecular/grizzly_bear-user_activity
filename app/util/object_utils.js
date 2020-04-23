module.exports.assignKeyToObject = (obj, keyPath, value) => {
    const lastKeyIndex = keyPath.length - 1;
    for (var i = 0; i < lastKeyIndex; ++i) {
        const key = keyPath[i];
        if (!(key in obj)) {
            obj[key] = {};
        }
        obj = obj[key];
    }
    obj[keyPath[lastKeyIndex]] = value;
};

module.exports.getValueFromObject = (obj, keyPath, defaultValue) => {
    for (var i = 0; i < keyPath.length; ++i) {
        const key = keyPath[i];
        if (!(key in obj)) {
            return defaultValue;
        }
        obj = obj[key];
    }
    return obj;
};
