#!/bin/bash
 
mvn --batch-mode verify sonar:sonar \
  -Dsonar.projectKey=infra-framework \
  -Dsonar.host.url=http://172.28.87.48:9000 \
  -Dsonar.login=8f56b0919177b3dea5ed5e3218272c7ce5f86a61 \
  -Dsonar.gitlab.project_id=$CI_PROJECT_ID \
  -Dsonar.gitlab.commit_sha=$CI_COMMIT_SHA \
  -Dsonar.gitlab.ref_name=$CI_COMMIT_REF_NAME \
  -Dsonar.branch.name=$CI_COMMIT_REF_NAME

if [ $? -eq 0 ]; then
    echo "sonarqube code-analyze-preview over."
fi
