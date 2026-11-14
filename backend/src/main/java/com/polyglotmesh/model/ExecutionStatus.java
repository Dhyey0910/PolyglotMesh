package com.polyglotmesh.model;

/** Outcome of one sandboxed execution. */
public enum ExecutionStatus {
    SUCCESS,
    ERROR,
    SYNTAX_ERROR,
    TIMEOUT,
    LIMIT_EXCEEDED,
    BLOCKED
}
