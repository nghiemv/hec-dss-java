/**
 * Client-perspective compilation sanity check.
 *
 * <p>This source set is deliberately a separate named module so that JPMS
 * enforces the module boundary: it can only see types from packages the
 * main module explicitly {@code exports}. If anything in {@code .internal}
 * leaks into a public method or field signature, this module fails to
 * compile. Renaming or deleting a public API item that the client-facing
 * sample depends on also fails the build.
 *
 * <p>The sample does not run — it just needs to compile.
 */
module mil.army.usace.hec.dss.clienttest {
    requires mil.army.usace.hec.dss;
}
