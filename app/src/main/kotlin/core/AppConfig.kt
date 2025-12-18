package core

import java.time.ZoneId

object AppConfig {
    val defaultZoneId: ZoneId = ZoneId.of("Asia/Tokyo")
    const val githubApiBaseUrl: String = "https://api.github.com"
    const val defaultBranch: String = "main"
}
