Just my personal try-out for building a Sling-based message archiving and management solution for ServiceMix.

Step-by-step
------------

1. Build the project using "mvn install" as usual

2. Get and build Apache Sling 

3. Start Apache Sling in an empty directory with 
   java -jar ~/.m2/repository/org/apache/sling/org.apache.sling.launchpad.app/2.0.0-incubator-SNAPSHOT/org.apache.sling.launchpad.app-2.0.0-incubator-SNAPSHOT.jar

4. Connect to the Sling Management console at http://localhost:8080/system/console and upload/start the servicemix-sling-console bundle.
   When the bundle is installed, connect to http://localhost:8080/servicemix.html to see the ServiceMix dashboard page (don't mind the layout, I'm not a web designer ;) ).

5. Add the Sling auditor to the ServiceMix classpath by copying these jars to lib/optional
   - jackrabbit-jcr-commons-1.5-SNAPSHOT.jar
   - jackrabbit-jcr-rmi-1.5-SNAPSHOT.jar
   - jcr-1.0.jar
   - servicemix-audit-jcr-1.0-SNAPSHOT.jar

6. Reconfigure ServiceMix
   - add <bean class="org.apache.servicemix.audit.jcr.SlingAuditor"/> to the <services/> section in conf/servicemix.xml
   - change the rmi.port in conf/servicemix.properties to avoid the conflict with the Sling RMI registry


TODO
----
A lot, but these come to mind...
- asynchronous archival in JCR instead of blocking message delivery
- 



