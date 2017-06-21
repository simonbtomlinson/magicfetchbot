package com.simonbtomlinson.magicfetchbot.common

import java.lang.Exception
import java.sql.Statement


// Copied from
// https://github.com/lukaseder/kotlin/blob/93463f0677a3b2fb29afcdd2ae2799d9ec5c81bd/libraries/stdlib/src/kotlin/io/ReadWrite.kt
public inline fun <T : AutoCloseable, R> T.use(block: (T) -> R): R {
	var closed = false
	try {
		return block(this)
	} catch (e: Exception) {
		closed = true
		try {
			close()
		} catch (closeException: Exception) {
			// should use e.addSupressed() when kotlin supports it
		}
		throw e
	} finally {
		if (!closed) {
			close()
		}
	}
}


fun loadResourceAsString(resourceName: String): String {
	// Need any classloader, so it doesn't matter that we use the one from String
	return String::class.java.getResource(resourceName).readText()
}