/* Copyright (c) 2025, 2025, Oracle and/or its affiliates. */
/* Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/. */

File log = new File(basedir, 'build.log')
assert log.exists()
assert log.text.contains("BUILD SUCCESS")
assert log.text.contains("Rule 0: com.oracle.macaron.artifactverifier.maven.VerifyDependencies passed")
assert log.text.contains("Warning: using dependency io.micronaut.security:micronaut-security-annotations:3.8.4 which is configured for usage alert")
assert !log.text.contains("Warning: using dependency io.micronaut:micronaut-inject")
assert !log.text.contains("Warning: using dependency io.micronaut:micronaut-core")
assert !log.text.contains("Error: using dependency io.micronaut:micronaut-inject")
assert !log.text.contains("Error: using dependency io.micronaut:micronaut-core")
assert !log.text.contains("Error: using dependency io.micronaut:micronaut-security")
