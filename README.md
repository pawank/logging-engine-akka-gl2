logging-engine-akka-gl2
=======================

Logging engine using Akka and Graylog2 to save incoming logs (as messages) into logging backend and graylog2 server

Steps to run:
1. cd logging-engine-akka-gl2
2. ./activator
3. project logModels
4. package
5. cp log-models/target/scala-2.10/logmodels_2.10-0.1-SNAPSHOT.jar lib/
6. project root
7. compile
8. run


clone logging-engine-akka-gl2-dependencies and follow respective README files.
Each program has "run.sh" which is a shell script wrapper for running the respective application.

