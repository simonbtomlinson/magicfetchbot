package com.simonbtomlinson.magicfetchbot.bot

import com.nhaarman.mockito_kotlin.mock
import com.simonbtomlinson.telegram.api.client.TelegramClient
import com.simonbtomlinson.telegram.api.types.Chat
import com.simonbtomlinson.telegram.api.types.Message
import com.simonbtomlinson.telegram.api.types.User
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import java.time.Instant
import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.equalTo

// Completely immutable, useful for partial edits later
val emptyMsg = Message(
        messageId = 1,
        from = null,
        chat = Chat(id = 1, type = Chat.Type.PRIVATE, title = null, username = null, firstName = null, lastName = null),
        date = Instant.now(),
        forwardFrom = null,
        forwardDate = null,
        forwardFromChat = null,
        replyToMessage = null,
        editDate = null,
        text = null,
        entities = null,
        audio = null,
        document = null,
        photo = null,
        sticker = null,
        video = null,
        location = null,
        caption = null,
        contact = null,
        voice = null
)

object MessageHandlerTests : Spek({
    given("A message handler") {
        val tgClient = mock<TelegramClient>()
        val setLoader = mock<MagicSetLoader>()
        val msgHandler = MessageHandler(tgClient, listOf(100, 200, 300), setLoader)
        on("A message from an admin") {
            val msg = emptyMsg.copy(from = User(id = 100, firstName = null, lastName = null, username = null))
            it("is authenticated") {
                assert.that(msgHandler.senderIsAuthenticated(msg), equalTo(true))
            }
        }
        on("A message from someone not on the admin list") {
            val msg = emptyMsg.copy(from = User(id = 150, firstName = null, lastName = null, username = null))
            it("is not authenticated") {
                assert.that(msgHandler.senderIsAuthenticated(msg), equalTo(false))
            }
        }
    }
})