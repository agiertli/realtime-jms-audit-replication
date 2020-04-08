Architecture:
```
            /runtime request
   <-------------------------------^        /history request
   |                               |     ^-----------------------------------+
   |                               |     |                                   |
   |                               |     |                                   |
   v                               |     |                                   |
                                 +-+-----+----+                              |
+--------+                       |            |                              |
|        |                       |            |                              |
|        | Runtime/History data  |            |                              |
|        | <------------------+  |            |                              |
|        |  stored to            |            | audit data replicated        |
+--------+                       |            +-------------+                |
RuntimeDB                        +------------+             |                v
                                   kie-server               | via
                                  ^                         v
                                  |   ^             +-------++             +---------+
                                  |   |             |        |             |         |
                                  |   |             |        |   to        |         |
                                  |   |             |        +------------>+         |
                                  |   |             |        |             |         |
                     /runtime req |   |             +--------+             +---------+
                                  |   |             JMSBroker               HistoryDatabase
           +---------+ +----------+   |
           |         |                |
           |         | +--------------+
           |         |   /history request
           |         |
           +---------+
             client
```


### Example endpoints:

Processes:
```
GET rest/history/processes/instancesByDate?status=1&startFrom=2019-04-08&startTo=2020-04-9
GET /rest/history/processes/instances?status=1
GET /rest/history/processes/instances/1?withVars=true
```
Tasks:
```
GET /rest/history/tasks/allinstances
GET /rest/history/tasks/instances/1/events
GET /rest/history/tasks/instances/2
GET /rest/history/tasks/instancesByUser
```
