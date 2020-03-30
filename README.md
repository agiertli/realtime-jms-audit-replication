```
                                                                                                                    
                                             JVM1 - kie-server                                                      JVM2 - JMSReceiver                         database2
database1                                                                                                            +-----------+  Stores audit log into    +------------+
+-------+                                     +----------+                                                           |           |                           |            |
|       |        Stores audit log into        |          |                                                           |           | +---------------------->  |            |
|       |                                     |          |                                                           |           |                           |            |
|       |       <-------------------------+   |          |                                                           |           |                           |            |
+-------+                                     |          |                                                           |           |                           +------------+
                                              |          |                                                           +-----------+
                                              +----------+                                                                                                      
                                                            +                                                              + 
                                                            |                                                              |
                                                            |                                                              |
                                                            |                                                              |
                                                            | duplicates audit logs to            reads audit logs from    |
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
```