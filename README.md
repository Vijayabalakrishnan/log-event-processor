## Application Description ##
- Log Event Processor application takes a log file in text format as input and process it
- Every line in the file is a JSON object
- Each line in the log file will have information about an event
- JSON Fields (id - the unique event identifier, state - "STARTED" or "FINISHED", timestamp - the timestamp of the event in milliseconds, type - type of log (Optional), host - host name (Optional))

## Softwares Required ##
- Java 8
- Maven 3+
- Git
- hsqldb-2.4.1 (Recent version won't version since they use Java 11+)

## Frameworks Used ##
- Spring Boot
- Sprint Boot Data JPA
- Spring Boot Test (Mockito)
- JUnit 4

## Notes ##
- 100% Unit test coverage added only for service package.
- Explicitly left all other packages

## Build the Project ##
- Clone the repository (git clone https://github.com/Vijayabalakrishnan/log-event-processor.git). Please reach out to ``kvijayabalakrishnan@gmail.com`` if you face any issue with repository access
- Build the jar (mvn clean install). Assumption - ``M2_HOME & JAVA_HOME`` is set
- Make sure build is successful and a jar file named ``log-event-processor.jar`` is generated in ``log-event-processor/target`` directory

## Start HSQLDB ##
- Extract hsqldb-2.4.1.zip available in the cloned repository
- Open command prompt and navigate to ``hsqldb-2.4.1\hsqldb\data`` directory
- Run the command ``java -cp ../lib/hsqldb.jar org.hsqldb.server.Server --database.0 file:assessment/assessment --dbname.0 assessment``
- This should start the HSQLDB

## Prepare Test Data ##
- Create a log file in text format
- Please refer ``log-event-processor/src/test/resources/files/valid-events.txt``

## Run the Jar File ##
- Open a new command prompt
- Navigate to the directory where ``log-event-processor.jar`` exists (log-event-processor/target directory)
- Copy the command ``java -jar -Dlog.file.path=<path_of_the_log_event_file> log-event-processor.jar`` to a notepad
- Replace ``<path_of_the_log_event_file>`` with valid path
- Execute the command in the command prompt
- Should see the application starts by printing few logs on the screen & after processing the file exits normally

## Result ##
- When the application exits normally, you should see a ``logs`` directory and ``app.log`` file created by the app.
- Should see the list of valid events stored in database in DEBUG log
