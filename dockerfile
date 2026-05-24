FROM jdk:17
WORKDIR /market-analytics-app
COPY /market-analytics-app/target/MarketAnalytics 0.0.1-SNAPSHOT.jar market-analytics.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/market-analytics.jar"]