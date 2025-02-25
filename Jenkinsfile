def saunter(scriptName) {
  withCredentials([
    usernamePassword(
      credentialsId: 'HEALTH_APIS_RELEASES_NEXUS_USERNAME_PASSWORD',
      usernameVariable: 'NEXUS_USERNAME',
      passwordVariable: 'NEXUS_PASSWORD'),
    usernameColonPassword(
      credentialsId: 'PROMOTATRON_USERNAME_PASSWORD',
      variable: 'PROMOTATRON_USERNAME_PASSWORD'),
    usernamePassword(
      credentialsId: 'LABMASTER_USERNAME_PASSWORD',
      usernameVariable: 'LABMASTER_USERNAME',
      passwordVariable: 'LABMASTER_PASSWORD'),
    usernamePassword(
      credentialsId: 'LABUSER_USERNAME_PASSWORD',
      usernameVariable: 'LABUSER_USERNAME',
      passwordVariable: 'LABUSER_PASSWORD'),
    usernamePassword(
      credentialsId: 'STGLABMASTER_USERNAME_PASSWORD',
      usernameVariable: 'STGLABMASTER_USERNAME',
      passwordVariable: 'STGLABMASTER_PASSWORD'),
    usernamePassword(
      credentialsId: 'STGLABUSER_USERNAME_PASSWORD',
      usernameVariable: 'STGLABUSER_USERNAME',
      passwordVariable: 'STGLABUSER_PASSWORD'),
    usernamePassword(
      credentialsId: 'QAMASTER_USERNAME_PASSWORD',
      usernameVariable: 'QAMASTER_USERNAME',
      passwordVariable: 'QAMASTER_PASSWORD'),
    usernamePassword(
      credentialsId: 'QAUSER_USERNAME_PASSWORD',
      usernameVariable: 'QAUSER_USERNAME',
      passwordVariable: 'QAUSER_PASSWORD'),
   ]) {
    sh script: scriptName
  }
}

pipeline {
  options {
    buildDiscarder(logRotator(numToKeepStr: '30', artifactNumToKeepStr: '30'))
    disableConcurrentBuilds()
    retry(0)
    timeout(time: 120, unit: 'MINUTES')
    timestamps()
  }
  parameters {
    booleanParam(name: 'RUN_DATA_QUERY_TESTS', defaultValue: true, description: "Run data-query-tests after loading the database.")
  }
  agent {
    dockerfile {
      filename './docker/Dockerfile'
      args "--privileged --group-add 497 -v /etc/passwd:/etc/passwd:ro -v /etc/group:/etc/group:ro -v /data/jenkins/.m2/repository:/home/jenkins/.m2/repository -v /var/lib/jenkins/.ssh:/home/jenkins/.ssh -v /var/run/docker.sock:/var/run/docker.sock -v /var/lib/docker:/var/lib/docker"
    }
  }
  environment {
    ENVIRONMENT = "${["qa", "staging_lab", "lab"].contains(env.BRANCH_NAME) ? env.BRANCH_NAME.replaceAll('_','-') : "i-cant-even-w-this"}"
  }
  stages {
    stage('Build') {
      when {
        expression { return env.ENVIRONMENT != 'i-cant-even-w-this' }
      }
      steps {
        saunter('./build.sh')
      }
    }
  }
  post {
    always {
      script {
        def buildName = sh returnStdout: true, script: '''[ -f .jenkins/build-name ] && cat .jenkins/build-name ; exit 0'''
        currentBuild.displayName = "#${currentBuild.number} - ${buildName}"
        def description = sh returnStdout: true, script: '''[ -f .jenkins/description ] && cat .jenkins/description ; exit 0'''
        currentBuild.description = "${description}"
        if (env.ENVIRONMENT != 'i-cant-even-w-this') {
          sendNotifications('shankins')
        }
        if (env.ENVIRONMENT == 'lab') {
          sendNotifications('api_operations')
        }
      }
    }
  }

}
