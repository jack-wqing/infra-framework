#!/bin/bash
 
mvn --batch-mode sonar:sonar \
  -Dsonar.projectKey=infra-framework \
  -Dsonar.host.url=http://172.28.87.48:9000 \
  -Dsonar.login=8f56b0919177b3dea5ed5e3218272c7ce5f86a61 \
  -Dsonar.java.binaries=target/sonar \
  -Dsonar.tests=. \
  -Dsonar.test.inclusions=**/*Test*/** \
  -Dsonar.exclusions=**/*Test*/** \
  -Dsonar.sources=src/main/java  \
  -Dsonar.branch.name=$CI_COMMIT_REF_NAME

if [ $? -eq 0 ]; then
    echo "sonarqube code-publish over."
fi
