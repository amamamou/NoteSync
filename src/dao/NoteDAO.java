package dao;

import entities.Note;
import java.util.List;

public interface NoteDAO {
    List<Note> getAllNotes();
    List<Note> getAllNotesByCategory(int categoryId); // New method
    Note getNoteById(int id);
    void addNote(Note note);
    void updateNote(Note note);
    void deleteNote(Note note);
}
