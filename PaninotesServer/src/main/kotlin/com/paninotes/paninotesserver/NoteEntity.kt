package com.paninotes.paninotesserver


import com.paninotes.paninotesserver.backupState.BackupState
import org.hibernate.Hibernate
import org.hibernate.annotations.Nationalized
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.lang.NonNull
import java.io.File
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "note")
data class Note(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,
    @Column(name = "title")
    @NonNull
    var title: String? = null,
    @Column(name = "htmltext")
    @NonNull
    @Lob
    @Basic
    @Nationalized
    var htmlText: String? = null, //gives nclob
    @Column(name = "filepath")
    var filePath: File? = null,
    @UpdateTimestamp
    var lastBackupTime: LocalDateTime? = null,
    @Enumerated(EnumType.STRING)
    var backupState: BackupState = BackupState.NOT_BACKED_UP,
    var isOpen: Boolean?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as Note

        return id != null && id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(id = $id )"
    }
}