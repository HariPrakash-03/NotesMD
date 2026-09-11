package com.notesmd.core.domain.exception

/**
 * Thrown when an external file exceeds the 5MB size ceiling.
 * Use cases catch this to map it to a user-facing Snackbar message.
 */
class FileTooLargeException(
    val actualBytes: Long
) : Exception("File exceeds 5MB limit ($actualBytes bytes)")
