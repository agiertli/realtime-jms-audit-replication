     
                                                                                                                     JVM2 - JMSReceiver
                                            JVM1 - kie-server
                                                                                                                     +-----------+ Stores audit log into     +------------+
+-------+                                     +----------+                                                           |           |                           |            |
|       |        Stores audit log into        |          |                                                           |           | +---------------------->  |            |
|       |                                     |          |                                                           |           |                           |            |
|       |       <-------------------------+   |          |                                                           |           |                           |            |
+-------+                                     |          |                                                           |           |                           +------------+
                                              |          |                                                           +-----------+
Database 1                                    +----------+                                                                                                     Database 2
                                                            +                                                              + 
                                                            |                                                              |
                                                            |                                                              |
                                                            |                                                              |
                                                            | duplicates audit logs to              reads audit logs from  |
                                                            |                                                              |
                                                            +------------------>                <--------------------------+
                                                                                 +-----------+
                                                                                 |           |
                                                                                 |           |
                                                                                 |           |
                                                                                 |           |
                                                                                 |           |
                                                                                 +-----------+

                                                                                  JMS Broker

