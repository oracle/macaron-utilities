# Artifact Verifier Maven Plugin

Maven plugin for verifying artifacts. Scans project dependency tree and verifies artifacts against provenances (as per configuration provided).

## Setup
First, ensure that the artifact-verifier library (`../artifact-verifier`) is built and installed.

Build, install and run tests:
```
mvn clean install
```

## Usage

### Verification

Add the plugin to your Maven project by adding the Maven Enforcer plugin and configuring it to add the custom rule for artifact verification, as in the following example:
```
<project ...>
  ...
  <build>
    ...
    <plugins>
      ...
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-enforcer-plugin</artifactId>
        <version>3.5.0</version>
        <dependencies>
          <dependency>
            <groupId>com.oracle.macaron</groupId>
            <artifactId>artifact-verifier-maven-enforcer-rules</artifactId>
            <version>0.1-SNAPSHOT</version>
          </dependency>
        </dependencies>
        <executions>
          <execution>
            <id>enforce</id>
            <configuration>
              <rules>
                <verifyDependencies>
                  <verificationRepos>
                    <item>https://maven.oracle.com/public</item>
                  </verificationRepos>
                  <verifierMode>REQUIRE_VERIFY_IF_ATTESTED_ANY</verifierMode>
                </verifyDependencies>
              </rules>
            </configuration>
            <goals>
              <goal>enforce</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
      ...
    </plugins>
    ...
  </build>
  ...
</project>
```

When a build is run, the plugin will verify the provenances of any artifacts in the project dependency tree, against provenance attestations published in the specified verification repositories, and according to the specified verifier mode (specifying verifierMode is optional, the default value is `REQUIRE_VERIFY_IF_ATTESTED_ANY`). See [/artifact-verifier/README.md] for details on the available verifier modes, and if verification fails (what constitutes failure depends on the mode) for any artifact, the build fails.

If verification succeeds, the plugin will print output like this:
```
Verified: [io.micronaut:micronaut-inject:jar:4.6.8-oracle-00001, io.micronaut:micronaut-core:jar:4.6.8-oracle-00001]
Rule 0: com.oracle.macaron.artifactverifier.maven.VerifyDependencies passed
```

Then the build will proceed as normal. If verification fails (what constitutes failure depends on the verifier mode), the plugin prints a list of the artifacts that failed verification and fails the build.

Note that some artifacts may not have provenances published and so cannot be verified, depending on the verifier mode those verification may be skipped and the plugin will output `Verification Skipped: [...<list of artifacts>...]`. Skipped verifications do not fail the build.

### Artifact usage alerts

(note: this feature is experimental and likely to change substantially in future)

Another check can be enabled to warn or error if there is a dependency on an artifact for which a usage alert is configured (either for an exact version match or for any version).
This feature is enabled by including the following configuration to the Maven Enforcer rule configuration, to specify a configuration file for artifact usage alerts (see the artifact-verifier README for configuration format documentation):
```
...
              <rules>
                <verifyDependencies>
                  <artifactUsageAlertConfigUrl>https://example.com/artifact-usage-alert-config.json</artifactUsageAlertConfigFilePath>
                  <artifactUsageAlertSeverity>WARN_EXACT_VERSION</artifactUsageAlertSeverity>
                </verifyDependencies>
              </rules>
...
```
Can use a local configuration file instead of a URL with:
```
              <rules>
                <verifyDependencies>
                  <artifactUsageAlertConfigFilePath>/path/to/artifact-usage-alert-config.json</artifactUsageAlertConfigFilePath>
                  <artifactUsageAlertSeverity>WARN_EXACT_VERSION</artifactUsageAlertSeverity>
                </verifyDependencies>
              </rules>
```



There are a number of different modes that can be configured for this check via the `artifactUsageAlertSeverity` parameter (the above example shows the `WARN_EXACT_VERSION` mode, which is also the default if the parameter is not specified), as follows:
- `NONE`: do not perform the check
- `WARN_EXACT_VERSION`: report a warning (but do not fail the build), if an artifact is used where there is a configured usage alert for that specific version, e.g.:
```
Warning: using dependency io.micronaut:micronaut-inject:4.6.8 which is configured for usage alert (suggested alternatives: [io.micronaut:micronaut-inject:4.6.8-oracle-00001])
Warning: using dependency io.micronaut:micronaut-core:4.6.8 which is configured for usage alert (suggested alternatives: [io.micronaut:micronaut-core:4.6.8-oracle-00001])
```
- `ERROR_EXACT_VERSION`: report an error and fail the build, if an artifact is used where there is a configured usage alert for that specific version, e.g.:
```
Error: using dependency io.micronaut:micronaut-core:4.6.8 which is configured for usage alert (suggested alternatives: [io.micronaut:micronaut-core:4.6.8-oracle-00001])
...
BUILD FAILED
```
- `WARN_ANY_VERSION`: report a warning (but do not fail the build), if an artifact is used where there is a configured usage alert for any version of that artifact id (but not necessarily one exactly corresponding to the version used by the project), e.g.:
```
Warning: using dependency io.micronaut:micronaut-core:4.6.8 which is configured for usage alert (suggested alternatives: [io.micronaut:micronaut-core:4.6.8-oracle-00001])
Warning: using dependency io.micronaut.security:micronaut-security-annotations:3.8.4 for which any version is configured for usage alert
```
- `ERROR_ANY_VERSION`: report an error and fail the build, if an artifact is used where there is a configured usage alert for any version of that artifact id (but not necessarily one exactly corresponding to the version used by the project), e.g.:
```
Error: using dependency io.micronaut:micronaut-core:4.6.8 which is configured for usage alert (suggested alternatives: [io.micronaut:micronaut-core:4.6.8-oracle-00001])
Error: using dependency io.micronaut.security:micronaut-security-annotations:3.8.4 for which any version is configured for usage alert
...
BUILD FAILED
```
- `WARN_ANY_VERSION_ERROR_EXACT_VERSION` report a warning (but do not fail the build), if an artifact is used where there is a configured usage alert for a different version of that artifact id but not one exactly corresponding to the version used by the project, but report an error and fail the build if an artifact is used where there is a configured usage alert exactly corresponding to the version used by the project, e.g.:
```
Error: using dependency io.micronaut:micronaut-core:4.6.8 which is configured for usage alert (suggested alternatives: [io.micronaut:micronaut-core:4.6.8-oracle-00001])
Warning: using dependency io.micronaut.security:micronaut-security-annotations:3.8.4 for which any version is configured for usage alert
...
BUILD FAILED
```
