# Grabber Service - with REST API for Health
======================================================

Building locally
----------------
mvn clean install

Running locally
---------------
java -Dlogging.configurationFile=./src/main/resources/log4j2.xml -jar ./build/dependency/grabber-1.0-SNAPSHOT.jar %number_of_nodes%
or
chmod +x ./run
./run %number_of_nodes%

Logs Location
-------------
./logs/app.log

REST Api Documentation (Swagger Documentation)
----------------------------------------------
-	Build the project
-	Run the project
-	Acess the swagger documentation at: http://localhost:8080/swagger-ui.html

Design
------
* First thought was to have one DataGrabberService. The DataGrabberService would grab data from each node & persist it in database. e.g. nodesList.stream().forEach(node -> { fetch(); persist();}) Cons: If one of the nodes is down or slow to respond, it would affect refresh time SLAs, as we are going serially & blocking.
* Second thought was to make the task { fetch(); persist();} as async. This would definitely help performance. Cons: Fetching & persisting were tightly coupled, so scaling them at different rates would not be possible.
* Third thought (Final Design), break DataGrabberService into DataGrabProducerService & DataGrabConsumerService. Producer & Consumer share data using a ThreadSafe Queue. Switch DB persistance to use Bulk Writes for better performance. The Consumer (DB Writes) will have one thread. For Producers (data grabbers) we will have a farm (500) of threads. Each thread keeps working on the next available nodeId.
* Final Design - Pros : Producer & Consumer are decoupled & can be scaled independently. Design is geared towards high scale on the number of nodes & aggressive shorter refresh time. If one node is down/slow it would not affect overall system performance.
* Final Design - Cons: If the target vm does not have enough resources, this program will not fix the issue, it will only highlight the issue, using "/health" endpoint. Some resource (human/automation) would then need to scale up. e.g. If the number of threads is very high, too much time would be spent in context switching. We would need to look at load average using top command & adjust the thread count.

Health Output
-------------
* totalNodesCount - The total count of nodes data grabber is monitoring.
* nodesCountCurrentlySuccessful - The count of nodes for which we are successfully grabbing data.
* nodesCountCurrentlyFailing - The count of nodes for which we are failing to grab data.
* dbWritesCurrentlyFailing - Are the db writes currently failing.
* nodesCountNeverFetched - The number of nodes for which we have never fetched data. ***Please Note** If the load average is optimum, this indicates we need more processing power.

Boiler Plate Code
-----------------
There are a few third party open source components that I use very frequently. I have reused some of these due to familiarity & confidence (stabilized over time).
* HttpClient OkHttp Client - No strong preference. Pros: Stable, Open source support. Fewer nested dependencies. (e.g. Other Http Clients like Jersey & Apache are used by 3rd party libraries like AWS, ElasticSearch. Nested dependencies makes it riskier to update these http libraries.)
* HttpClient Retryer - Helps retry Http Calls.
* HttpClient RetryPredicate - Predicate that helps define cases for retry.
* ContextClosedHandler & ThreadSyncService - Helps clean shutdown of worker threads.
* MonitoringConfig - Helps configure monitoring/stats.
* SwaggerConfig - Helps configure Swagger UI for REST Docs.
* ThreadConfig - Helps configure Thread Pool.
* DBSettings - Encapsulates settings for DB Connections. SQLite is in process server-less database, so connection pooling is not very relevant. Kept connection pooling for future support for PostGres etc.
* Tags - Helper for building tags/key values pairs for monitoring.


Grabber Specific Business Logic
-------------------------------
* HttpClient & FetchUsageDataCallable - Service that does the HTTP communication with [dependent service](http://localhost/nodes/<id>/usage).
* GrabberConfig - Configures the Grabber Application. e.g. refreshInterval & refreshIntervalTimeUnit which determine the SLA for refreshing each node.
* UsageData - Defines the model which contains the data returned by dependent Http Services & data that is persisted to DB. Also contains SQL specific mappings like Field Names & Datatypes.
* UsageDataRepository & UsageDataSqlRepository - Encapsulates interactions with DB in this case SQLite DB.
* UsageDataService - Encapsulates operations on usage data. e.g. In future if we want to persist data to both Sqlite & ElasticSearch, we need to modify only this service, Producer, Consumer etc. would not be touched. Also we can add tests here which make code resilient for any unintended refactoring changes etc.
* QueueService - Encapsulates logic for Queue operations. The Producer queues usage data & Consumer dequeues it. Ideally should be interface. This helps if in future we want to move from local queue to AWS SQS or rabbit mq etc.
* HealthService - Maintains in memory stats for each node grabs & and db writes. Produces Summary when requested by HealthController. Main motivation was to identify thread starvation causing us to miss data from some nodes.
* DataGrabConsumerService - Fetches Usage Data from dependent service. Queues it using Queue Service. Reports status (success/failure/delays) to HealthService & stats to Monitoring.
* DataGrabProducerService - DeQueues Usage Data from Queue Service & Bulk Writes it to Database. Reports status (success/failure/delays) to HealthService & stats to Monitoring.
* HealthController - Controller for [Health api](http://localhost:8080/health) , provides a summary of Grabber Applications health.

Key Features
------------
* Used [Spring Boot](https://spring.io/projects/spring-boot) for the REST server for Health endpoint.
* Used [Project Lombok](https://github.com/rzwitserloot/lombok) to generate boiler-plate code.
* Used [Swagger](https://swagger.io/) & [SpringFox](http://springfox.github.io/springfox/) for REST Documentation.
* Used [jooq](https://www.jooq.org/) for interacting with Sqlite for persistent storage.
* Used [HikariCP](https://github.com/brettwooldridge/HikariCP) for Database connection pooling.
* Used [guava-retrying](https://github.com/rholder/guava-retrying) to make communication with dependent service more resilient.
* Used [WireMock](https://github.com/tomakehurst/wiremock) to mock dependent service e.g. .
* Used [Dropwizard](https://metrics.dropwizard.io/4.0.0/) for metrics.


