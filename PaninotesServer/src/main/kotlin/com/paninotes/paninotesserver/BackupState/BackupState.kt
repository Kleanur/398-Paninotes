package BackupState

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty

enum class BackupState {
    @JsonProperty("NOT_BACKED_UP")
    NOT_BACKED_UP,
    @JsonProperty("OUT_OF_SYNC")
    OUT_OF_SYNC,
    @JsonProperty("BACKED_UP")
    BACKED_UP
}
