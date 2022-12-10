import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Finestra implements ActionListener {
    String link = "jdbc:mysql://localhost:3306/subs?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC";
    String name = "root";
    String pass = "Luca";
    JFrame frame;
    JLabel nomeTxt;
    JTextField nomeIn;
    JLabel cognomeTxt;
    JTextField cognomeIn;
    JSlider ageSld;
    JButton confirmBtn;
    JButton setBtn;
    JButton getBtn;
    JTextField ageIn;
    List<String> people = new ArrayList<>();
    JList persone;
    int lastId;

    public Finestra() {
        // Initialize the elements
        frame = new JFrame();
        nomeTxt = new JLabel("Nome");
        cognomeTxt = new JLabel("Cognome");
        nomeIn = new JTextField();
        cognomeIn = new JTextField();
        ageSld = new JSlider();
        confirmBtn = new JButton("Aggiungi");
        ageIn = new JTextField("0");
        setBtn = new JButton("Modifica");
        getBtn = new JButton("Elimina");
        persone = new JList();

        // Set slider settings
        JLabel ageTxt = new JLabel("Età");
        ageSld.setMaximum(100);
        ageSld.setMinimum(1);

        // Set elements' functions
        confirmBtn.addActionListener(this);
        getBtn.addActionListener(this::delUser);
        setBtn.addActionListener(this::setUser);
        ageSld.addChangeListener(this::sliderChanged);
        persone.addListSelectionListener(this::listSelected);

        // GUI settings
        nomeTxt.setBounds(10, 10, 100, 10);
        cognomeTxt.setBounds(150, 10, 100, 10);
        nomeIn.setBounds(10, 30, 100, 20);
        cognomeIn.setBounds(150, 30, 100, 20);
        confirmBtn.setBounds(300, 150, 100, 20);
        getBtn.setBounds(200, 150, 100, 20);
        setBtn.setBounds(400, 150, 100, 20);
        ageTxt.setBounds(15, 130, 50, 10);
        ageSld.setBounds(10, 160, 100, 20);
        ageIn.setBounds(120, 160, 50, 20);
        persone.setBounds(130, 200, 260, 400);

        // Add elements in the frame
        frame.add(nomeTxt);
        frame.add(getBtn);
        frame.add(cognomeTxt);
        frame.add(nomeIn);
        frame.add(cognomeIn);
        frame.add(confirmBtn);
        frame.add(persone);
        frame.add(ageTxt);
        frame.add(setBtn);
        frame.add(ageSld);
        frame.add(ageIn);

        // Set the frame
        frame.setSize(520, 430);
        frame.setLayout(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        // Create new frame
        Finestra f = new Finestra();
        try (
                // Create a connection to the SQL Server
                Connection conn = DriverManager.getConnection(f.link, f.name, f.pass);
                Statement stmt = conn.createStatement()
        ) {

            // Get the table "people"
            String strSelect = "select nome, cognome, eta from people";
            ResultSet rset = stmt.executeQuery(strSelect);
            System.out.println("The sql statement is: " + strSelect + "\n");
            System.out.println("The records selected are: ");
            int rowCount = 0;

            // Get every person in the people table
            while (rset.next()) {
                String nome = rset.getString("nome");
                String cognome = rset.getString("cognome");
                int eta = rset.getInt("eta");
                System.out.println(nome + " " + cognome + " " + eta);
                ++rowCount;
            }
            System.out.println("Total number of records = " + rowCount);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        f.updateList();
    }

    // ADD BUTTON FUNCTION
    public void actionPerformed(ActionEvent e) {
        // IF the text box are empty, throw an error
        if (nomeIn.getText().equals("") || cognomeIn.getText().equals("") || ageIn.getText().equals("")) {
            JOptionPane.showMessageDialog(frame, "Hai lasciato una casella vuota", "Errore di inserimento", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // Try to connect to the SQL server
        try (Connection conn = DriverManager.getConnection(link, name, pass);
             Statement stmt = conn.createStatement()) {
            boolean got_it = false;
            int get_id = 0;

            // Get all the people from people table
            String strGet = "select * from people";
            ResultSet rset = stmt.executeQuery(strGet);

            // Search if there are some empty ids in the table
            int pid = 0;
            while (rset.next()) {
                int cid = rset.getInt("id");
                if (pid + 1 != cid) {
                    got_it = true;
                    get_id = cid - 1;
                    break;
                }
                pid = cid;
            }

            // If there are no empty ids, use the next id
            String toUse = Integer.toString(lastId + 1);
            if (got_it) {
                // If there are some empty ids, use the next empty id
                toUse = Integer.toString(get_id);
            }
            // Update the SQL Server
            String strInsert = "insert into people values ('" + nomeIn.getText() + "', '" + cognomeIn.getText() + "', " + ageIn.getText() + ", " + (toUse) + ")";
            System.out.print("The SQL statement is: " + strInsert);
            int counInserted = stmt.executeUpdate(strInsert);
            System.out.println(counInserted + "records inserted \n");

            // Update the list
            updateList();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // SLIDER FUNCTION
    public void sliderChanged(ChangeEvent e) {
        ageIn.setText(Integer.toString(ageSld.getValue()));
    }


    // DELETE USER BUTTON FUNCTION
    public void delUser(ActionEvent e) {
        // Try to connect to the SQL Server
        try (Connection conn = DriverManager.getConnection(link, name, pass);
             Statement stmt = conn.createStatement()) {
            // Get the person selected
            String selected = (String) persone.getSelectedValue();
            // If they didn't select anything, throw an error
            if (selected == null) {
                if (persone.getSelectedValue() == null) {
                    JOptionPane.showMessageDialog(frame, "Non hai selezionato la persona", "Errore", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
            // Split the selected person row in words
            String[] infos = selected.split(" ");
            // Delete the person from the table
            String strDelete = "delete from people where id = " + infos[infos.length - 1];
            int countEdit = stmt.executeUpdate(strDelete);
            System.out.println("Elementi elimination = " + countEdit);
            // Update list
            updateList();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void updateList() {
        int id = 0;
        // Try to connect at the SQL Server
        try (Connection conn = DriverManager.getConnection(link, name, pass);
             Statement stmt = conn.createStatement()) {
            // Get all the people in the table
            String strSelect = "select * from people";
            ResultSet rset = stmt.executeQuery(strSelect);
            // Clear the list
            people.clear();
            // Put every person from the table to the list
            while (rset.next()) {
                String nome = rset.getString("nome");
                String cognome = rset.getString("cognome");
                int eta = rset.getInt("eta");
                id = rset.getInt("id");
                people.add(nome + " " + cognome + " " + eta + " " + id);
            }
            // Save the last id
            lastId = id;
            // Update the list
            persone.setListData(people.toArray());
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void setUser(ActionEvent e) {
        // Connect to the SQL Server
        try (Connection conn = DriverManager.getConnection(link, name, pass);
             Statement stmt = conn.createStatement()) {
            // If none got selected, throw an error
            if (persone.getSelectedValue() == null) {
                JOptionPane.showMessageDialog(frame, "Non hai selezionato la persone", "Errore", JOptionPane.WARNING_MESSAGE);
                return;
            }
            // If the text field are empty, throw an error
            if (nomeIn.getText().equals("") || cognomeIn.getText().equals("") || ageIn.getText().equals("")) {
                JOptionPane.showMessageDialog(frame, "Hai lasciato una casella vuota", "Errore di inserimento", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // Get the person selected string
            String selected = (String) persone.getSelectedValue();
            // Divide it in words
            String[] infos = selected.split(" ");
            // Change the person name
            String strUpdate = "update people set nome = '" + nomeIn.getText() + "', cognome = '" + cognomeIn.getText() + "', eta = " + ageIn.getText() + " where id = " + infos[infos.length - 1];
            int infChanged = stmt.executeUpdate(strUpdate);
            System.out.println("info cambiate = " + infChanged);
            updateList();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        //updateList();
    }

    // LIST SELECTION CHANGE FUNCTION
    public void listSelected(ListSelectionEvent e) {
        // GET THE SELECTED PERSON STRING
        String selected = (String) persone.getSelectedValue();
        // If it's null just return
        if (selected == null) {
            return;
        }
        // Split it in words
        String[] infos = selected.split(" ");
        // Set all the text fields
        nomeIn.setText(infos[0]);
        cognomeIn.setText(infos[1]);
        ageSld.setValue(Integer.parseInt(infos[2]));
        ageIn.setText(infos[2]);
    }
}