// Aggregates settings by their frequency across all DBs.
// Usage: mongo --host <hostname> --port <port> settings-agg.js
allDatabases = db.adminCommand({ "listDatabases": 1 }).databases

collection = 'PREFERENCES'

acc = {}

allDatabases.forEach(function(d) {
    //database = connect('localhost:27017/' + d.name)
    database = db.getSiblingDB(d.name)
    collections = database.getCollectionNames()
    if (collections.indexOf(collection) >= 0) {
        usage = database.getCollection(collection).aggregate([{$unwind: "$KEYS_AND_VALUES"}, {$group: {_id:"$KEYS_AND_VALUES.VALUE", total:{$sum:1}}}])

        usage.forEach(function(u) {
            if (u._id[0] == '{') {
                if (u._id in acc) {
                    acc[u._id] += u.total
                } else {
                    acc[u._id] = u.total
                }
            }
        })
    }
})

result = []
for (var key in acc) {
    if (acc.hasOwnProperty(key)) {
        result.push({"setting": key, "count": acc[key]})
    }
}

result.sort(function(a, b) { return b.count - a.count })

for (var i in result) {
    print("Setting: " + result[i].setting + "\nCount: " + result[i].count + "\n")
}

