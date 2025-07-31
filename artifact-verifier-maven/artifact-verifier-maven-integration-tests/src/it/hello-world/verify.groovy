/* Copyright (c) 2025, 2025, Oracle and/or its affiliates. */
/* Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/. */

File log = new File(basedir, 'build.log')
assert log.exists()
assert log.text.contains("BUILD SUCCESS")
assert log.text.contains("Rule 0: com.oracle.macaron.artifactverifier.maven.VerifyDependencies passed")
assert log.text.contains("Verified: [io.micronaut:micronaut-inject:4.6.8-oracle-00001, io.micronaut:micronaut-core:4.6.8-oracle-00001]")
assert !log.text.contains("Verification Failed")
