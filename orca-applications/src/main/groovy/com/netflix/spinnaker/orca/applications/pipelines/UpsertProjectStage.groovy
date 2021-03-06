/*
 * Copyright 2015 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.netflix.spinnaker.orca.applications.pipelines

import com.google.common.annotations.VisibleForTesting
import com.netflix.spinnaker.orca.DefaultTaskResult
import com.netflix.spinnaker.orca.ExecutionStatus
import com.netflix.spinnaker.orca.Task
import com.netflix.spinnaker.orca.TaskResult
import com.netflix.spinnaker.orca.front50.Front50Service
import com.netflix.spinnaker.orca.pipeline.LinearStage
import com.netflix.spinnaker.orca.pipeline.model.Stage
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.batch.core.Step
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@CompileStatic
class UpsertProjectStage extends LinearStage {
  public static final String PIPELINE_CONFIG_TYPE = "upsertProject"

  UpsertProjectStage() {
    super(PIPELINE_CONFIG_TYPE)
  }

  @Override
  public List<Step> buildSteps(Stage stage) {
    [buildStep(stage, "upsertProject", UpsertProjectTask)]
  }

  @Slf4j
  @Component
  @VisibleForTesting
  public static class UpsertProjectTask implements Task {
    @Autowired
    Front50Service front50Service

    UpsertProjectTask() {}

    @Override
    TaskResult execute(Stage stage) {
      def projectId = stage.mapTo("/project", Front50Service.Project)
      def project = stage.mapTo("/project", Map)

      if (projectId.id) {
        front50Service.updateProject(projectId.id, project)
      } else {
        front50Service.createProject(project)
      }

      def outputs = [
        "notification.type": "upsertproject"
      ]

      return new DefaultTaskResult(ExecutionStatus.SUCCEEDED, outputs)
    }
  }
}
