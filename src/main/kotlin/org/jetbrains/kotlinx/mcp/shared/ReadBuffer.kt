package org.jetbrains.kotlinx.mcp.shared

import org.jetbrains.kotlinx.mcp.JSONRPCMessage
import io.ktor.utils.io.core.*
import kotlinx.io.Buffer
import kotlinx.io.indexOf
import kotlinx.io.readString
import kotlinx.serialization.encodeToString

/**
 * Buffers a continuous stdio stream into discrete JSON-RPC messages.
 */
class ReadBuffer {
    private val buffer: Buffer = Buffer()

    fun append(chunk: ByteArray) {
        buffer.writeFully(chunk)
    }

    fun readMessage(): JSONRPCMessage? {
        if (buffer.exhausted()) return null
        var lfIndex = buffer.indexOf('\n'.code.toByte())
        val line = when (lfIndex) {
            -1L -> return null
            0L -> {
                buffer.skip(1)
                ""
            }

            else -> {
                var skipBytes = 1
                if (buffer[lfIndex - 1] == '\r'.code.toByte()) {
                    lfIndex -= 1
                    skipBytes += 1
                }
                val string = buffer.readString(lfIndex)
                buffer.skip(skipBytes.toLong())
                string
            }
        }
        return deserializeMessage(line)
    }

    fun clear() {
        buffer.clear()
    }
}

internal fun deserializeMessage(line: String): JSONRPCMessage {
    return McpJson.decodeFromString<JSONRPCMessage>(line)
}

internal fun serializeMessage(message: JSONRPCMessage): String {
    return McpJson.encodeToString(message) + "\n"
}
