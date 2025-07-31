/* Copyright (c) 2025, 2025, Oracle and/or its affiliates. */
/* Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/. */

File log = new File(basedir, 'build.log')
assert log.exists()
assert log.text.contains("BUILD SUCCESS")
assert log.text.contains("Rule 0: com.oracle.macaron.artifactverifier.maven.VerifyDependencies passed")
assert log.text.contains("Verification Skipped: [io.micronaut:micronaut-inject:3.8.5-oracle-00001, org.slf4j:slf4j-api:1.7.36, javax.annotation:javax.annotation-api:1.3.2, jakarta.inject:jakarta.inject-api:2.0.1, jakarta.annotation:jakarta.annotation-api:2.1.1, io.micronaut:micronaut-core:3.8.5-oracle-00001, org.yaml:snakeyaml:1.33]")
assert !log.text.contains("Verified: [")
assert !log.text.contains("Verification Failed")
