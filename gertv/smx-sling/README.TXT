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
   - modify conf/servicemix.xml
     - add <bean id="JcrClientFactory" class="org.apache.jackrabbit.rmi.client.ClientRepositoryFactory"/>
     - add <bean id="JcrRepository" factory-bean="JcrClientFactory" factory-method="getRepository"><constructor-arg value="rmi://localhost:1099/jackrabbit"/></bean>
     - add to the <services/> section:
           <bean class="org.apache.servicemix.audit.jcr.SlingAuditor">
             <property name="repository" ref="JcrRepository"/>
             <property name="strategy">
              <bean class="org.apache.servicemix.audit.jcr.SlingJcrAuditorStrategy">
             </property>
           </bean>
   - change the rmi.port in conf/servicemix.properties to avoid the conflict with the Sling RMI registry


TODO
----
A lot, but these come to mind...

On the auditor...
- asynchronous archival in JCR instead of blocking message delivery
  Instead of accessing the JCR directly when exchangeSent() is called, you store the MessageExchange in a JMS Queue.  This will allow another thread to pick up the message from the queue
  and store it in the JCR repository -- so the storing of the message exchange no longer interferes with the ESB throughput

- refactor JCR auditor (with a configurable RepositoryFactory) to separate Sling specific details from the basic JCR stuff
  Part of this has already been done, but we should be able to configure the RepositoryFactory so we can point to another JCR repo when we need to.  This will make the JCR repository more
  versatile.

- archiving message flows based on the correlation id
  Messages that belong to the same 'flow' (i.e. have the same correlation id) should be archived together.  It would be very nice to allow the users to see all the flows and drill down
  to the exchanges and messages whenever they need to.  

- allow the auditor to be configured to only audit metadata for some exchanges (to allow usage of stream-based messages)
  Ideally, we would have a /config node in the repository where all the config data is to be found.  We can build Sling forms to update the config and use JCR Observation to propagate this
  to the sling auditor.  In a normal runtime scenario, it is often enough for users to see what happened to the message flow, without seeing the actual normalized message content.  This will
  allow the use of StreamSources and the like for performance.  Whever the user configures the Sling /config node, this will change the audit behavior to include all message details.

On the web console...
- build a dashboard
  Not sure what has to go there: failed exchanges, throughput, last x exchanges, ... just things to give users a first entry point in the app.  

- add information about the endpoints themselves
  If we add an EndpointListener implementation to ServiceMix, we can get feedback on endpoints being added/removed.  We should have a web page for every endpoint that we can link to from the exchange
  pages.  If we could a reverse link -- showing the list of exchanges that passed through a given endpoint -- that would a great feature!

- make it possible to edit/resend exchanges
  This is only possible if we can access the JBIContainer either locally or through JMS Flow.  However, if we audited the the MessageExchange, we can build a form to edit the normalized message content
  and send this new content to the endpoint again.

- making the web UI look good
  no ideas here... anything beyond my web designer skills will be a huge improvement

