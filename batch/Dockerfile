FROM amazonlinux

RUN yum install -y java-1.8.0-openjdk-devel
RUN yum install -y https://dl.bintray.com/sbt/rpm/sbt-1.3.8.rpm
RUN yum install -y wget
RUN yum install -y tar

COPY . code
WORKDIR /code
 
RUN sbt app/assembly

ADD http://www-eu.apache.org/dist/spark/spark-2.4.5/spark-2.4.5-bin-hadoop2.7.tgz .
RUN tar -xzf spark-2.4.5-bin-hadoop2.7.tgz

CMD ./spark-2.4.5-bin-hadoop2.7/bin/spark-submit --class "UserActivityProcessor" target/scala-2.11/build.jar HourlyExperimentStats
