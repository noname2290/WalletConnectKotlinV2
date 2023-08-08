package com.walletconnect.android.archive

import com.walletconnect.android.internal.common.crypto.codec.Codec
import com.walletconnect.android.internal.common.json_rpc.data.JsonRpcSerializer
import com.walletconnect.android.internal.common.model.ArchiveMessage
import com.walletconnect.android.internal.common.model.sync.ClientJsonRpc
import com.walletconnect.android.internal.common.model.type.ClientParams
import com.walletconnect.android.sync.common.json_rpc.SyncParams
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.util.Logger

internal class ReduceSyncRequestsUseCase(
    private val archiveMessageNotifier: ArchiveMessageNotifier,
    private val chaChaPolyCodec: Codec,
    private val serializer: JsonRpcSerializer,
    private val logger: Logger,
) {

    /**
     * Reduction algorithm:
     * 1. Decrypt all messages, discard any exceptions
     * 2. Serialize messages to know which method it belongs to
     * 3. !Temporary! Remove messages duplicates by json rpc id
     * 4. Split them by method into sets. Method: wc_syncDelete, wc_syncSet and remaining
     * 5. Reduce any wc_syncDelete along with corresponding wc_syncSet requests
     * 6. Recreate order by retaining initial message only if is within remaining wc_syncDelete, wc_syncSet and remaining requests
     */
    suspend operator fun invoke(archiveMessages: List<ArchiveMessage>) {
        val decryptedMessages: List<Triple<ClientJsonRpc, ArchiveMessage, String>> = archiveMessages.decryptMessages()

        // Temporary. Can be removed when Archive Server is Topic-centric instead of being Client-centric
        val decryptedMessagesSetWithoutDuplicates: List<Triple<ClientJsonRpc, ArchiveMessage, String>> = decryptedMessages.distinctBy { (clientJsonRpc, _, _) -> clientJsonRpc.id }
        // Temporary

        val reducedArchiveMessages = decryptedMessagesSetWithoutDuplicates.splitByType().reduceSyncSetsForAnySyncDelete()
        val orderedReducedArchiveMessages = archiveMessages.recreateOrder(reducedArchiveMessages)

        logger.log("Reduced fetched archive message from: ${archiveMessages.size} to: ${orderedReducedArchiveMessages.size}")
        orderedReducedArchiveMessages.onEach { request -> archiveMessageNotifier.requestsSharedFlow.emit(request.toRelay()) }
    }

    private fun List<ArchiveMessage>.decryptMessages(): List<Triple<ClientJsonRpc, ArchiveMessage, String>> = this.map { archiveMessage ->
        try {
            val decryptedMessageString = chaChaPolyCodec.decrypt(Topic(archiveMessage.topic), archiveMessage.message)
            val clientJsonRpc = serializer.tryDeserialize<ClientJsonRpc>(decryptedMessageString) ?: run {
                logger.error(IllegalArgumentException("Unable to deserialize message:$decryptedMessageString"))
                return@map null
            }
            return@map Triple(clientJsonRpc, archiveMessage, decryptedMessageString)
        } catch (e: Exception) {
            logger.error(e)
            return@map null
        }
    }.filterNotNull()

    private data class SplitByTypeResult(
        val syncDeleteMessages: MutableList<Triple<ClientJsonRpc, SyncParams.DeleteParams, ArchiveMessage>>,
        val syncSetMessages: MutableList<Triple<ClientJsonRpc, SyncParams.SetParams, ArchiveMessage>>,
        val remainingMessages: List<ArchiveMessage>,
    )

    private fun List<Triple<ClientJsonRpc, ArchiveMessage, String>>.splitByType(): SplitByTypeResult {
        val syncDeleteMessages: MutableList<Triple<ClientJsonRpc, SyncParams.DeleteParams, ArchiveMessage>> = mutableListOf()
        val syncSetMessages: MutableList<Triple<ClientJsonRpc, SyncParams.SetParams, ArchiveMessage>> = mutableListOf()
        val remainingMessages: MutableList<ArchiveMessage> = mutableListOf()

        this.forEach { (clientJsonRpc, archiveMessage, decryptedMessageString) ->
            when (val clientParams: ClientParams? = serializer.deserialize(clientJsonRpc.method, decryptedMessageString)) {
                is SyncParams.DeleteParams -> syncDeleteMessages.add(Triple(clientJsonRpc, clientParams, archiveMessage))
                is SyncParams.SetParams -> syncSetMessages.add(Triple(clientJsonRpc, clientParams, archiveMessage))
                else -> remainingMessages.add(archiveMessage)
            }
        }
        return SplitByTypeResult(syncDeleteMessages, syncSetMessages, remainingMessages)
    }

    private fun SplitByTypeResult.reduceSyncSetsForAnySyncDelete(): List<ArchiveMessage> {
        syncDeleteMessages.removeAll { (_, deleteParams, deleteArchiveMessage) ->
            syncSetMessages.removeAll { (_, setParams, setArchiveMessage) ->
                deleteParams.key == setParams.key && deleteArchiveMessage.topic == setArchiveMessage.topic
            }
        }
        val reducedDeleteArchiveMessages: List<ArchiveMessage> = syncDeleteMessages.map { (_, _, deleteArchiveMessage) -> deleteArchiveMessage }
        val reducedSetArchiveMessages: List<ArchiveMessage> = syncSetMessages.map { (_, _, setArchiveMessage) -> setArchiveMessage }
        return reducedDeleteArchiveMessages + reducedSetArchiveMessages + remainingMessages
    }

    private fun List<ArchiveMessage>.recreateOrder(reducedArchiveMessages: List<ArchiveMessage>): List<ArchiveMessage> = filter { archiveMessage -> archiveMessage in reducedArchiveMessages }
}