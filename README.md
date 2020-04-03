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