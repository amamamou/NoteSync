package dao;


import entities.Note;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InMemoryNoteDAO implements NoteDAO {
    private java.sql.Connection connection;
    private List<Note> notes; // Initialize the notes variable

    public InMemoryNoteDAO() {
        connection = dao.Connection.getInstance().getConnection(); // Fully qualify Connection class
        notes = getAllNotes(); // Initialize the notes list when the DAO is created
    }

    @Override
    public List<Note> getAllNotes() {
        List<Note> notes = new ArrayList<>();
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM notes");
            while (resultSet.next()) {
                Note note = new Note(
                        resultSet.getInt("id"),
                        resultSet.getString("title"),
                        resultSet.getString("content"),
                        resultSet.getTimestamp("timestamp"),
                        resultSet.getInt("category_id"), // Updated to get category ID
                        resultSet.getInt("user_id") // Get user ID
                );
                notes.add(note);
            }
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notes;
    }
    @Override
    public Note getNoteById(int id) {
        Note note = null;
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM notes WHERE id = ?");
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                note = new Note(
                        resultSet.getInt("id"),
                        resultSet.getString("title"),
                        resultSet.getString("content"),
                        resultSet.getTimestamp("timestamp"),
                        resultSet.getInt("category_id"), // Updated to get category ID
                        resultSet.getInt("user_id") // Get user ID
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return note;
    }

    @Override
    public void addNote(Note note) {
        try {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO notes (title, content, timestamp, category_id, user_id) VALUES (?, ?, ?, ?, ?)");
            statement.setString(1, note.getTitle());
            statement.setString(2, note.getContent());
            statement.setTimestamp(3, new Timestamp(note.getTimestamp().getTime()));
            statement.setInt(4, note.getCategoryId());
            statement.setInt(5, note.getUserId());
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateNote(Note note) {
        try {
            PreparedStatement statement = connection.prepareStatement("UPDATE notes SET title = ?, content = ?, timestamp = ?, category_id = ? WHERE id = ?");
            statement.setString(1, note.getTitle());
            statement.setString(2, note.getContent());
            statement.setTimestamp(3, new Timestamp(note.getTimestamp().getTime()));
            statement.setInt(4, note.getCategoryId());
            statement.setInt(5, note.getId());
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteNote(Note note) {
        try {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM notes WHERE id = ?");
            statement.setInt(1, note.getId());
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Note> getAllNotesByCategory(int categoryId) {
        List<Note> categoryNotes = new ArrayList<>();
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM notes WHERE category_id = ?");
            statement.setInt(1, categoryId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Note note = new Note(
                        resultSet.getInt("id"),
                        resultSet.getString("title"),
                        resultSet.getString("content"),
                        resultSet.getTimestamp("timestamp"),
                        categoryId,
                        resultSet.getInt("user_id") // Get user ID
                );
                categoryNotes.add(note);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categoryNotes;
    }
    public List<Note> getAllNotesByCategoryAndUser(int categoryId, int userId) {
        List<Note> categoryNotes = new ArrayList<>();
        for (Note note : notes) {
            if (note.getCategoryId() == categoryId && note.getUserId() == userId) {
                categoryNotes.add(note);
            }
        }
        return categoryNotes;
    }

}
