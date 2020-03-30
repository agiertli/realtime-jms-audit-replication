![README](readme.png)
```
                                                                                                                     JVM2 - JMSReceiver.
                                               JVM1 - kie-server
                                                                                                                     +-----------+ Stores audit log into     +------------+
+-------+                                     +----------+                                                           |           |                           |            |
|       |        Stores audit log into        |          |                                                           |           | +---------------------->  |            |
|       |                                     |          |                                                           |           |                           |            |
|       |       <-------------------------+   |          |                                                           |           |                           |            |
+-------+                                     |          |                                                           |           |                           +------------+
                                              |          |                                                           +-----------+
  Database 1                                  +----------+                                                                                                        Database 2
                                                            +                                                              + reads audit logs from
                                                            |                                                              |
                                                            |                                                              |
                                                            |                                                              |
                                                            | duplicates audit logs to                                     |
                                                            |                                                              |
                                                            +------------------+                +--------------------------+
                                                                                 +-----------+
                                                                                 |           |
                                                                                 |           |
                                                                                 |           |
                                                                                 |           |
                                                                                 |           |
                                                                                 +-----------+

                                                                                JMS Broker
```