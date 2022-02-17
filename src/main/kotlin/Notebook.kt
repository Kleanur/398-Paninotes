class Notebook(id: Int, title: String) {
    var notebookId: Int = 0
    private set

    var title: String = ""
    val notes = ArrayList<Note>()

    init {
        this.title = title
        this.notebookId = id
    }

    fun addNote(note: Note) {
        notes.add(note)
    }
}