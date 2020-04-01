```
                 non history request
               <----------------------+
                 non history request         JVM1 + kie+server                                                      JVM2 + JMSReceiver                         database2
database1      +------------------------>                                                                            +---+-------+                           +------------+
+-------+                                     +----------+           proxy history request                           |           |  Stores audit log into    |            |
|       |                                     |          |  +--------------------------------------------------->    |           | +---------------------->  |            |
|       |         Stores audit log into       |          |                                                           |           |   history request         |            |
|       |       <-------------------------+   |          |               history response                            |           | +-------------------->    |            |
+-------+                                     |          |  <---------------------------------------------------+    |           |   history response        +------------+
                                              |          |                                                           +-----------+ <--------------------+
                                              +----------+
                 non history request                        +                                                              +
             +------------------------------>       +  +    |                                                              |
             |      history request                 |  |    |                                                              |
             |  +--------------------------->       |  |    |                                                              |
             |  |                                   |  |    |                                                              |
             |  |                                   |  |    |duplicates audit logs to                reads audit logs from |
             |  |                                   |  |    +------------------>                <--------------------------+
             |  |                                   |  |                         +-----------+
             |  |                                   |  |                         |           |
             |  |                                   |  |                         |           |
             |  |                                   |  |                         |           |
             |  |                                   |  |                         |           |
             |  |                                   |  |                         |           |
             |  |                                   |  |                         +-----------+
             +  +        non history response       |  |                           JMS Broker
+----------+     <----------------------------------+  |
|          |                                           |
|          |                                           |
|          |                                           |
|          |           history response                |
|          |    <--------------------------------------+
+----------+

  Client


```


- Request to kie-server `http://localhost:8090/rest/history/plog/1` will be forwarded to History server `http://localhost:8095/history/plog/1` which is connected to separate Audit database
