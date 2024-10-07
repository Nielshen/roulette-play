
FROM sbtscala/scala-sbt:eclipse-temurin-jammy-21.0.2_13_1.10.2_3.5.1

RUN apt-get update && apt-get install -y curl git

RUN apt-get clean && rm -rf /var/lib/apt/lists/*

WORKDIR /workspace

COPY . .

EXPOSE 9000 27017