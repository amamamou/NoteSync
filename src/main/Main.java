package main;

import javax.swing.*;
import dao.InMemoryNoteDAO;
import entities.Category;
import entities.Note;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private static JFrame frame;
    private static JPanel mainPanel;
    private static JPanel categoryPanel;
    private static JPanel notesPanel;
    private static JLabel categoryLabel;

    private static List<Category> categories;
    private static int currentCategoryId;
    private static String currentCategoryName;
    private static Note selectedNote;
    private static JTextArea notesTextAreaInner;
    private static JTextField titleField;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        frame = new JFrame("NoteSync: Where Ideas Meet Organization");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 600);

        mainPanel = new JPanel(new CardLayout());

        createHomePage();
        mainPanel.add(categoryPanel, "home");

        createNotesPage();
        mainPanel.add(notesPanel, "notes");

        frame.add(mainPanel);
        frame.setVisible(true);
    }

    private static void createHomePage() {
        categoryPanel = new JPanel(new BorderLayout());

        JPanel headerPanel = new JPanel(new GridLayout(0, 1));
        JLabel yourStyleLabel = createHeaderLabel("Your Ideas, Your Style", Color.decode("#AA336A"), Font.BOLD, 36);
        headerPanel.add(yourStyleLabel);

        JLabel advertisingLabel = createAdvertisingLabel("Unlock your creativity with NoteSync ! Effortlessly capture your thoughts and ideas, organize them into categories, and never lose track of your inspiration. Whether you're brainstorming for a project, planning your next adventure, or jotting down daily reminders, NoteSync is your go-to tool for staying organized and productive. Experience seamless collaboration by sharing your notes with friends or colleagues, and let NoteSync become your ultimate creative companion. You can add, update, and delete notes to tailor your experience.");
        headerPanel.add(advertisingLabel);

        categoryPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel gridPanel = new JPanel(new GridLayout(0, 3, 10, 10));
        gridPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        categories = getCategoryList();

        for (Category category : categories) {
            JPanel categoryLabelPanel = createCategoryLabel(category);
            gridPanel.add(categoryLabelPanel);
        }

        categoryPanel.add(gridPanel, BorderLayout.CENTER);

        categoryLabel = new JLabel("", SwingConstants.CENTER);
        categoryLabel.setFont(new Font("Arial", Font.BOLD, 20));
        categoryPanel.add(categoryLabel, BorderLayout.SOUTH);
    }

    private static void createNotesPage() {
        notesPanel = new JPanel(new BorderLayout());

        JPanel headerPanel = new JPanel(new GridLayout(0, 1));
        JLabel yourStyleLabel = createHeaderLabel("Notes", Color.decode("#AA336A"), Font.BOLD, 36);
        headerPanel.add(yourStyleLabel);

        notesPanel.add(headerPanel, BorderLayout.NORTH);

        JButton backButton = new JButton("Back");
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CardLayout cardLayout = (CardLayout) mainPanel.getLayout();
                cardLayout.show(mainPanel, "home");
            }
        });
        notesPanel.add(backButton, BorderLayout.SOUTH);

        JPanel mainNotesPanel = new JPanel(new BorderLayout());

        JPanel notesListPanel = new JPanel();
        notesListPanel.setLayout(new BoxLayout(notesListPanel, BoxLayout.Y_AXIS));
        notesListPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JScrollPane notesListScrollPane = new JScrollPane(notesListPanel);

        JPanel inputPanel = new JPanel(new BorderLayout());

        titleField = new JTextField(); // New JTextField for title
        inputPanel.add(titleField, BorderLayout.NORTH); // Add titleField to inputPanel

        notesTextAreaInner = new JTextArea();
        notesTextAreaInner.setLineWrap(true);
        notesTextAreaInner.setWrapStyleWord(true);
        JScrollPane notesScrollPane = new JScrollPane(notesTextAreaInner);
        notesScrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        inputPanel.add(notesScrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(0, 1, 0, 10));
        JButton addButton = new JButton("+ Add");

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String content = notesTextAreaInner.getText();
                String title = extractTitleFromContent(content);
                Note newNote = new Note(0, title, content, new Timestamp(System.currentTimeMillis()), currentCategoryId);

                SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        InMemoryNoteDAO noteDAO = new InMemoryNoteDAO();
                        noteDAO.addNote(newNote);
                        return null;
                    }

                    @Override
                    protected void done() {
                        showCategoryNotes(currentCategoryId, currentCategoryName, notesTextAreaInner);
                    }
                };

                worker.execute();
            }
        });

        JButton updateButton = new JButton("Update");
        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String content = notesTextAreaInner.getText();
                if (selectedNote != null) {
                    selectedNote.setContent(content);
                    InMemoryNoteDAO noteDAO = new InMemoryNoteDAO();
                    noteDAO.updateNote(selectedNote);
                    showCategoryNotes(currentCategoryId, currentCategoryName, notesTextAreaInner);
                }
            }
        });

        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedNote != null) {
                    InMemoryNoteDAO noteDAO = new InMemoryNoteDAO();
                    noteDAO.deleteNote(selectedNote);
                    showCategoryNotes(currentCategoryId, currentCategoryName, notesTextAreaInner);
                }
            }
        });

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        inputPanel.add(buttonPanel, BorderLayout.SOUTH);

        mainNotesPanel.add(notesListScrollPane, BorderLayout.WEST);
        mainNotesPanel.add(inputPanel, BorderLayout.CENTER);

        notesPanel.add(mainNotesPanel, BorderLayout.CENTER);
    }

    private static List<Category> getCategoryList() {
        List<Category> categories = new ArrayList<>();
        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/NoteTakingApp", "root", "");
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM categories");
            while (resultSet.next()) {
                Category category = new Category(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getBytes("image")
                );
                categories.add(category);
            }
            resultSet.close();
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }

    private static JLabel createHeaderLabel(String text, Color color, int style, int size) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Parisienne", style, size));
        label.setForeground(color);
        label.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        return label;
    }

    private static JLabel createAdvertisingLabel(String text) {
        int startIndex = text.indexOf("NoteSync");
        int endIndex = startIndex + 8;
        JLabel label = new JLabel("<html><div style='text-align: center; font-family: Neuton, serif; font-size: 16pt; color: black;'>"
                + text.substring(0, startIndex)
                + "<span style='font-family: Parisienne; font-size: 28pt; font-style: italic; font-weight: bold;'>"
                + text.substring(startIndex, endIndex) + "</span>"
                + text.substring(endIndex) + "</div></html>", SwingConstants.CENTER);
        label.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        return label;
    }

    private static JPanel createCategoryLabel(Category category) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));

        JLabel nameLabel = new JLabel(category.getName(), SwingConstants.CENTER);
        nameLabel.setFont(new Font("Parisienne", Font.BOLD, 16));
        nameLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.add(nameLabel, BorderLayout.NORTH);

        ImageIcon icon = new ImageIcon(category.getImage());
        Image image = icon.getImage().getScaledInstance(180, 180, Image.SCALE_SMOOTH);
        JLabel imageLabel = new JLabel(new ImageIcon(image), SwingConstants.CENTER);
        imageLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.add(imageLabel, BorderLayout.CENTER);

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                currentCategoryId = category.getId();
                currentCategoryName = category.getName();
                showCategoryNotes(currentCategoryId, currentCategoryName, null);
            }
        });

        return panel;
    }

    private static void showCategoryNotes(int categoryId, String categoryName, JTextArea notesTextArea) {
        currentCategoryId = categoryId;
        currentCategoryName = categoryName;

        InMemoryNoteDAO noteDAO = new InMemoryNoteDAO();
        List<Note> notes = noteDAO.getAllNotesByCategory(categoryId);

        JPanel notesArrayPanel = new JPanel();
        notesArrayPanel.setLayout(new BoxLayout(notesArrayPanel, BoxLayout.Y_AXIS));

        for (Note note : notes) {
            JPanel notePanel = new JPanel(new BorderLayout());
            notePanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));

            JLabel titleLabel = new JLabel("<html><b>" + note.getTitle() + "</b></html>");
            titleLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            JLabel timestampLabel = new JLabel(note.getTimestamp().toString());
            timestampLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            notePanel.add(titleLabel, BorderLayout.NORTH);
            notePanel.add(timestampLabel, BorderLayout.WEST);

            notePanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    selectedNote = note;
                    if (notesTextAreaInner != null) {
                        notesTextAreaInner.setText(note.getContent());
                        titleField.setText(note.getTitle()); // Update titleField with note's title
                    }
                }
            });

            notesArrayPanel.add(notePanel);
        }

        notesPanel.removeAll();
        notesPanel.setLayout(new BorderLayout());

        // Set category name label with Playfair Display font
        JLabel categoryNameLabel = new JLabel(categoryName + " Notes", SwingConstants.CENTER);
        categoryNameLabel.setFont(new Font("Parisienne", Font.BOLD, 28));
        notesPanel.add(categoryNameLabel, BorderLayout.NORTH);

        JScrollPane notesScrollPane = new JScrollPane(notesArrayPanel);
        notesPanel.add(notesScrollPane, BorderLayout.CENTER);

        JPanel mainNotesPanel = new JPanel(new BorderLayout());

        notesTextAreaInner = new JTextArea();
        notesTextAreaInner.setLineWrap(true);
        notesTextAreaInner.setWrapStyleWord(true);
        notesTextAreaInner.setPreferredSize(new Dimension(710, 400));
        mainNotesPanel.add(new JScrollPane(notesTextAreaInner), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(0, 1, 0, 10));
        JButton addButton = new JButton("+ Add");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String content = notesTextAreaInner.getText();
                String title = extractTitleFromContent(content);
                Note newNote = new Note(0, title, content, new Timestamp(System.currentTimeMillis()), categoryId);

                SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        InMemoryNoteDAO noteDAO = new InMemoryNoteDAO();
                        noteDAO.addNote(newNote);
                        return null;
                    }

                    @Override
                    protected void done() {
                        showCategoryNotes(currentCategoryId, currentCategoryName, notesTextAreaInner);
                    }
                };

                worker.execute();
            }
        });

        JButton updateButton = new JButton("Update");
        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String content = notesTextAreaInner.getText();
                if (selectedNote != null) {
                    String newTitle = extractTitleFromContent(content);
                    selectedNote.setContent(content);
                    selectedNote.setTitle(newTitle); // Update the title first
                    
                    InMemoryNoteDAO noteDAO = new InMemoryNoteDAO();
                    noteDAO.updateNote(selectedNote); // Update the note in the database
                    
                    showCategoryNotes(currentCategoryId, currentCategoryName, notesTextAreaInner);
                }
            }
        });


        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedNote != null) {
                    InMemoryNoteDAO noteDAO = new InMemoryNoteDAO();
                    noteDAO.deleteNote(selectedNote);
                    showCategoryNotes(currentCategoryId, currentCategoryName, notesTextAreaInner);
                }
            }
        });

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        mainNotesPanel.add(buttonPanel, BorderLayout.EAST);

        JButton backButton = new JButton("Back");
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CardLayout cardLayout = (CardLayout) mainPanel.getLayout();
                cardLayout.show(mainPanel, "home");
            }
        });

        mainNotesPanel.add(backButton, BorderLayout.SOUTH);

        notesPanel.add(mainNotesPanel, BorderLayout.SOUTH);

        CardLayout cardLayout = (CardLayout) mainPanel.getLayout();
        cardLayout.show(mainPanel, "notes");

        if (notesTextArea != null) {
            notesTextArea.setText("");
        }
    }

    private static String extractTitleFromContent(String content) {
        // Find the index of the first '\'
        int index = content.indexOf('\\');
        
        // If '\' is found, take the substring before it as the title, otherwise take the whole content
        String title = (index != -1) ? content.substring(0, index).trim() : content.trim();
        
        return title;
    }


}
