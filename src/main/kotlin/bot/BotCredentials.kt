package bot

import kotlinx.serialization.Serializable


@Serializable
data class BotCredentials(
    val name: String,
    val token: String
)