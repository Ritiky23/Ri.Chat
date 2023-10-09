import android.util.Base64

class ChatMessage {
    var senderId: String? = null
    var receiverId: String? = null
    var message: String? = null
    var drawingData: String? = null
    var drawingImageUrl: String? = null

    constructor()

    constructor(
        message: String?,
        senderId: String?,
        receiverId: String?,
        drawingData: ByteArray?,
        drawingImageUrl: String?
    ) {
        this.message = message
        this.senderId = senderId
        this.receiverId = receiverId
        this.drawingData = if (drawingData != null) Base64.encodeToString(drawingData, Base64.DEFAULT) else null
        this.drawingImageUrl = drawingImageUrl
    }
}
