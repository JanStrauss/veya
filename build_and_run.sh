mvn clean
mvn package
java -Djava.library.path=target/natives -jar target/veya-0.0.1-SNAPSHOT.jar 