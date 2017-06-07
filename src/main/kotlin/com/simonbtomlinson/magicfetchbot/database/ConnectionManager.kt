package com.simonbtomlinson.magicfetchbot.database


import com.simonbtomlinson.magicfetchbot.common.use
import java.sql.Connection
import javax.sql.DataSource


class ConnectionManager(private val connectionPool: DataSource) {

	fun <R> withConnection(block: (Connection) -> R) = connectionPool.getConnection().use(block)

}